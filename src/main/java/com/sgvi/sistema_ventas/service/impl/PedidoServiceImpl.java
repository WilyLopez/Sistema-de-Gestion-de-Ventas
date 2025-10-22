package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.*;
import com.sgvi.sistema_ventas.model.entity.Pedido;
import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import com.sgvi.sistema_ventas.repository.PedidoRepository;
import com.sgvi.sistema_ventas.repository.DetallePedidoRepository;
import com.sgvi.sistema_ventas.service.interfaces.IPedidoService;
import com.sgvi.sistema_ventas.util.CodeGenerator;
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.NumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de pedidos.
 * Administra el ciclo completo de pedidos desde su creación hasta la entrega.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CodeGenerator codeGenerator;
    private final NumberUtil numberUtil;

    /**
     * Crea un nuevo pedido en el sistema.
     * Genera código único, calcula totales y guarda detalles.
     *
     * @param pedido Datos principales del pedido
     * @param detalles Lista de productos y cantidades del pedido
     * @return Pedido creado con todos sus detalles
     * @throws ValidationException Si los datos del pedido no son válidos
     */
    @Override
    public Pedido crearPedido(Pedido pedido, List<DetallePedido> detalles) {
        log.info("Creando pedido para cliente ID: {}", pedido.getCliente().getIdCliente());

        validarPedido(pedido, detalles);

        pedido.setCodigoPedido(generarCodigoPedido());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        calcularTotales(pedido, detalles);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        guardarDetallesPedido(pedidoGuardado, detalles);

        log.info("Pedido creado exitosamente: {}", pedidoGuardado.getCodigoPedido());
        return pedidoGuardado;
    }

    /**
     * Actualiza el estado de un pedido existente.
     * Registra automáticamente la fecha de entrega si el estado es ENTREGADO.
     *
     * @param idPedido ID del pedido a actualizar
     * @param nuevoEstado Nuevo estado del pedido
     * @return Pedido con estado actualizado
     * @throws ResourceNotFoundException Si el pedido no existe
     * @throws BusinessException Si el pedido está en estado final
     */
    @Override
    public Pedido actualizarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        log.info("Actualizando estado de pedido {} a {}", idPedido, nuevoEstado);

        Pedido pedido = obtenerPorId(idPedido);

        if (pedido.esEstadoFinal()) {
            throw new BusinessException("No se puede cambiar el estado de un pedido finalizado");
        }

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoPedido.ENTREGADO) {
            pedido.setFechaEntrega(LocalDateTime.now());
        }

        return pedidoRepository.save(pedido);
    }

    /**
     * Cancela un pedido si está en estado válido para cancelación.
     *
     * @param idPedido ID del pedido a cancelar
     * @param motivo Motivo de la cancelación
     * @return Pedido cancelado
     * @throws ResourceNotFoundException Si el pedido no existe
     * @throws BusinessException Si el pedido no puede cancelarse
     */
    @Override
    public Pedido cancelarPedido(Long idPedido, String motivo) {
        log.info("Cancelando pedido ID: {}", idPedido);

        Pedido pedido = obtenerPorId(idPedido);

        if (!pedido.puedeCancelarse()) {
            throw new BusinessException("El pedido no puede ser cancelado en su estado actual");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setObservaciones("CANCELADO: " + motivo);

        return pedidoRepository.save(pedido);
    }

    /**
     * Obtiene un pedido por su identificador.
     *
     * @param id Identificador del pedido
     * @return Pedido encontrado
     * @throws ResourceNotFoundException Si el pedido no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    /**
     * Busca pedidos aplicando múltiples filtros opcionales.
     *
     * @param codigoPedido Código del pedido
     * @param idCliente ID del cliente
     * @param idUsuario ID del usuario que creó el pedido
     * @param estado Estado del pedido
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @param pageable Configuración de paginación
     * @return Página de pedidos que coinciden con los filtros
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Pedido> buscarConFiltros(String codigoPedido, Long idCliente, Long idUsuario,
                                         EstadoPedido estado, LocalDateTime fechaInicio,
                                         LocalDateTime fechaFin, Pageable pageable) {
        return pedidoRepository.buscarPedidosConFiltros(
                codigoPedido, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
        );
    }

    /**
     * Genera un código único para el pedido.
     * Formato: PED-YYYYMMDD-NNNNN
     *
     * @return Código generado
     */
    @Override
    public String generarCodigoPedido() {
        long count = pedidoRepository.count();
        return codeGenerator.generarCodigoPedido(count);
    }

    /**
     * Obtiene todos los pedidos que están atrasados.
     * Un pedido está atrasado si su fecha de entrega estimada ya pasó
     * y no está en estado ENTREGADO o CANCELADO.
     *
     * @return Lista de pedidos atrasados
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPedidosAtrasados() {
        return pedidoRepository.findPedidosAtrasados(LocalDateTime.now());
    }

    /**
     * Valida que todos los datos del pedido sean correctos.
     *
     * @param pedido Datos del pedido
     * @param detalles Detalles del pedido
     * @throws ValidationException Si algún dato no es válido
     */
    private void validarPedido(Pedido pedido, List<DetallePedido> detalles) {
        if (pedido.getCliente() == null) {
            throw new ValidationException("El cliente es obligatorio");
        }

        if (pedido.getDireccionEnvio() == null || pedido.getDireccionEnvio().trim().isEmpty()) {
            throw new ValidationException("La dirección de envío es obligatoria");
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new ValidationException("El pedido debe tener al menos un producto");
        }

        for (DetallePedido detalle : detalles) {
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new ValidationException("La cantidad debe ser mayor a cero");
            }

            if (detalle.getPrecioUnitario() == null
                    || numberUtil.esMenor(detalle.getPrecioUnitario(), BigDecimal.ZERO)
                    || numberUtil.sonIguales(detalle.getPrecioUnitario(), BigDecimal.ZERO)) {
                throw new ValidationException("El precio unitario debe ser mayor a cero");
            }
        }
    }

    /**
     * Calcula el subtotal y total del pedido.
     * Suma todos los subtotales de los detalles.
     *
     * @param pedido Pedido a calcular
     * @param detalles Detalles del pedido
     */
    private void calcularTotales(Pedido pedido, List<DetallePedido> detalles) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetallePedido detalle : detalles) {
            detalle.calcularSubtotal();
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        pedido.setSubtotal(numberUtil.redondearMoneda(subtotal));
        pedido.setTotal(numberUtil.redondearMoneda(subtotal));
    }

    /**
     * Guarda todos los detalles del pedido en la base de datos.
     *
     * @param pedido Pedido al que pertenecen los detalles
     * @param detalles Lista de detalles a guardar
     */
    private void guardarDetallesPedido(Pedido pedido, List<DetallePedido> detalles) {
        for (DetallePedido detalle : detalles) {
            detalle.setPedido(pedido);
            detalle.calcularSubtotal();
            detallePedidoRepository.save(detalle);
        }
    }
}