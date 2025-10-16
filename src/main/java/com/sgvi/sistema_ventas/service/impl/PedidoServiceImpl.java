package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.*;
import com.sgvi.sistema_ventas.model.entity.Pedido;
import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import com.sgvi.sistema_ventas.repository.PedidoRepository;
import com.sgvi.sistema_ventas.repository.DetallePedidoRepository;
import com.sgvi.sistema_ventas.service.interfaces.IPedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementación del servicio de gestión de pedidos.
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

    @Override
    public Pedido crearPedido(Pedido pedido, List<DetallePedido> detalles) {
        log.info("Creando pedido para cliente ID: {}", pedido.getCliente().getIdCliente());

        validarPedido(pedido, detalles);

        // Generar código
        pedido.setCodigoPedido(generarCodigoPedido());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Calcular totales
        calcularTotales(pedido, detalles);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Guardar detalles
        for (DetallePedido detalle : detalles) {
            detalle.setPedido(pedidoGuardado);
            detalle.calcularSubtotal();
            detallePedidoRepository.save(detalle);
        }

        log.info("Pedido creado exitosamente: {}", pedidoGuardado.getCodigoPedido());
        return pedidoGuardado;
    }

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

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Pedido> buscarConFiltros(String codigoPedido, Long idCliente, Long idUsuario,
                                         EstadoPedido estado, LocalDateTime fechaInicio,
                                         LocalDateTime fechaFin, Pageable pageable) {
        return pedidoRepository.buscarPedidosConFiltros(
                codigoPedido, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
        );
    }

    @Override
    public String generarCodigoPedido() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = pedidoRepository.count() + 1;
        return String.format("PED-%s-%05d", fecha, count);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPedidosAtrasados() {
        return pedidoRepository.findPedidosAtrasados(LocalDateTime.now());
    }

    // ========== MÉTODOS PRIVADOS ==========

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
    }

    private void calcularTotales(Pedido pedido, List<DetallePedido> detalles) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetallePedido detalle : detalles) {
            detalle.calcularSubtotal();
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        pedido.setSubtotal(subtotal);
        pedido.setTotal(subtotal);
    }
}