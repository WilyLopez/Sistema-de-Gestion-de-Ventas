package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.model.dto.common.PageResponseDTO;
import com.sgvi.sistema_ventas.model.dto.common.ResponseDTO;
import com.sgvi.sistema_ventas.model.dto.venta.PedidoDTO;
import com.sgvi.sistema_ventas.model.dto.venta.PedidoCreateDTO;
import com.sgvi.sistema_ventas.model.dto.venta.DetallePedidoDTO;
import com.sgvi.sistema_ventas.model.entity.*;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import com.sgvi.sistema_ventas.repository.*;
import com.sgvi.sistema_ventas.service.interfaces.IPedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation para la gestión de pedidos.
 * Implementa la lógica de negocio para pedidos de clientes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public ResponseDTO<PedidoDTO> crearPedido(PedidoCreateDTO pedidoCreateDTO, Long idUsuario) {
        try {
            log.info("Creando nuevo pedido para cliente: {} por usuario: {}",
                    pedidoCreateDTO.getIdCliente(), idUsuario);

            // Validar stock para cada producto
            for (DetallePedidoDTO detalleDTO : pedidoCreateDTO.getDetalles()) {
                Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleDTO.getIdProducto()));

                if (!producto.hayStockSuficiente(detalleDTO.getCantidad())) {
                    return ResponseDTO.error("Stock insuficiente para el producto: " + producto.getNombre());
                }
            }

            // Obtener entidades relacionadas
            Cliente cliente = clienteRepository.findById(pedidoCreateDTO.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Crear pedido
            Pedido pedido = Pedido.builder()
                    .codigoPedido(generarCodigoPedido())
                    .cliente(cliente)
                    .usuario(usuario)
                    .direccionEnvio(pedidoCreateDTO.getDireccionEnvio())
                    .fechaEntrega(pedidoCreateDTO.getFechaEntrega())
                    .observaciones(pedidoCreateDTO.getObservaciones())
                    .estado(EstadoPedido.PENDIENTE)
                    .build();

            // Agregar detalles del pedido
            for (DetallePedidoDTO detalleDTO : pedidoCreateDTO.getDetalles()) {
                Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleDTO.getIdProducto()));

                DetallePedido detalle = DetallePedido.builder()
                        .pedido(pedido)
                        .producto(producto)
                        .cantidad(detalleDTO.getCantidad())
                        .precioUnitario(detalleDTO.getPrecioUnitario())
                        .build();
                detalle.calcularSubtotal();

                pedido.agregarDetalle(detalle);
            }

            // Calcular totales
            pedido.calcularTotales();

            // Guardar pedido
            Pedido pedidoGuardado = pedidoRepository.save(pedido);

            log.info("Pedido creado exitosamente: {}", pedidoGuardado.getCodigoPedido());
            return ResponseDTO.success(convertirAPedidoDTO(pedidoGuardado), "Pedido creado exitosamente");

        } catch (Exception e) {
            log.error("Error al crear pedido: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al crear pedido: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO<PedidoDTO> actualizarEstadoPedido(Long idPedido, EstadoPedido nuevoEstado, Long idUsuario) {
        try {
            log.info("Actualizando estado del pedido: {} a {} por usuario: {}", idPedido, nuevoEstado, idUsuario);

            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (pedido.getEstado().isEsFinal()) {
                return ResponseDTO.error("No se puede modificar un pedido en estado final: " + pedido.getEstado());
            }

            // Actualizar estado
            pedido.setEstado(nuevoEstado);
            Pedido pedidoActualizado = pedidoRepository.save(pedido);

            log.info("Estado del pedido actualizado exitosamente");
            return ResponseDTO.success(convertirAPedidoDTO(pedidoActualizado), "Estado del pedido actualizado exitosamente");

        } catch (Exception e) {
            log.error("Error al actualizar estado del pedido: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al actualizar estado del pedido: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResponseDTO<PedidoDTO>> obtenerPedidos(String codigoPedido, Long idCliente, Long idUsuario,
                                                                  EstadoPedido estado, LocalDateTime fechaInicio,
                                                                  LocalDateTime fechaFin, Pageable pageable) {
        try {
            log.info("Obteniendo pedidos con filtros");

            Page<Pedido> pedidosPage = pedidoRepository.buscarPedidosConFiltros(
                    codigoPedido, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable);

            List<PedidoDTO> pedidosDTO = pedidosPage.getContent().stream()
                    .map(this::convertirAPedidoDTO)
                    .collect(Collectors.toList());

            PageResponseDTO<PedidoDTO> response = PageResponseDTO.of(
                    pedidosDTO,
                    pedidosPage.getNumber(),
                    pedidosPage.getSize(),
                    pedidosPage.getTotalElements()
            );

            return ResponseDTO.success(response, "Pedidos obtenidos exitosamente");

        } catch (Exception e) {
            log.error("Error al obtener pedidos: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al obtener pedidos: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<PedidoDTO>> obtenerPedidosPendientesEntrega() {
        try {
            log.info("Obteniendo pedidos pendientes de entrega");

            LocalDateTime fechaLimite = LocalDateTime.now().plusDays(2); // Próximos 2 días
            List<Pedido> pedidos = pedidoRepository.findPedidosPendientesEntrega(fechaLimite);
            List<PedidoDTO> pedidosDTO = pedidos.stream()
                    .map(this::convertirAPedidoDTO)
                    .collect(Collectors.toList());

            return ResponseDTO.success(pedidosDTO, "Pedidos pendientes de entrega obtenidos exitosamente");

        } catch (Exception e) {
            log.error("Error al obtener pedidos pendientes de entrega: {}", e.getMessage(), e);
            return ResponseDTO.error("Error al obtener pedidos pendientes de entrega: " + e.getMessage());
        }
    }

    // Métodos auxiliares privados
    private String generarCodigoPedido() {
        String prefijo = "PED-" + LocalDateTime.now().getYear() + "-";
        long numeroPedidos = pedidoRepository.count();
        return prefijo + String.format("%05d", numeroPedidos + 1);
    }

    // Métodos de conversión DTO
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
        List<DetallePedidoDTO> detallesDTO = pedido.getDetallesPedido().stream()
                .map(this::convertirADetallePedidoDTO)
                .collect(Collectors.toList());

        return PedidoDTO.builder()
                .idPedido(pedido.getIdPedido())
                .codigoPedido(pedido.getCodigoPedido())
                .idCliente(pedido.getCliente().getIdCliente())
                .nombreCliente(pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido())
                .idUsuario(pedido.getUsuario().getIdUsuario())
                .nombreUsuario(pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido())
                .fechaPedido(pedido.getFechaPedido())
                .fechaEntrega(pedido.getFechaEntrega())
                .estado(pedido.getEstado())
                .subtotal(pedido.getSubtotal())
                .total(pedido.getTotal())
                .direccionEnvio(pedido.getDireccionEnvio())
                .observaciones(pedido.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }

    private DetallePedidoDTO convertirADetallePedidoDTO(DetallePedido detalle) {
        return DetallePedidoDTO.builder()
                .idDetallePedido(detalle.getIdDetallePedido())
                .idProducto(detalle.getProducto().getIdProducto())
                .codigoProducto(detalle.getProducto().getCodigo())
                .nombreProducto(detalle.getProducto().getNombre())
                .marca(detalle.getProducto().getMarca())
                .talla(detalle.getProducto().getTalla())
                .color(detalle.getProducto().getColor())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build();
    }

    // Implementación de otros métodos del interface
    @Override
    public ResponseDTO<PedidoDTO> cancelarPedido(Long idPedido, String motivo, Long idUsuario) {
        // Implementación
        return null;
    }

    @Override
    public ResponseDTO<PedidoDTO> obtenerPedidoPorId(Long idPedido) {
        // Implementación
        return null;
    }

    @Override
    public ResponseDTO<PedidoDTO> obtenerPedidoPorCodigo(String codigoPedido) {
        // Implementación
        return null;
    }

    @Override
    public ResponseDTO<List<PedidoDTO>> obtenerPedidosAtrasados() {
        // Implementación
        return null;
    }

    @Override
    public ResponseDTO<Long> obtenerCantidadPedidosPorEstado(EstadoPedido estado) {
        // Implementación
        return null;
    }
}