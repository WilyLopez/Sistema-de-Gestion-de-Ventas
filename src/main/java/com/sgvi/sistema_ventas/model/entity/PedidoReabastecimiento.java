package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pedido de reabastecimiento a proveedores en el sistema.
 * Corresponde a la tabla 'pedidoreabastecimiento' en la base de datos.
 *
 * Un pedido de reabastecimiento puede contener múltiples productos
 * y está asociado a un proveedor específico y al usuario que lo solicita.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "pedidoreabastecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"detallesReabastecimiento"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PedidoReabastecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpedidoreab")
    @EqualsAndHashCode.Include
    private Long idPedidoReab;

    /**
     * Código único del pedido de reabastecimiento (ej: "REAB-2024-00001")
     * Único y no nulo
     */
    @Column(name = "codigopedido", unique = true, nullable = false, length = 20)
    private String codigoPedido;

    /**
     * Proveedor al que se realiza el pedido
     * Relación ManyToOne con la entidad Proveedor
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproveedor", nullable = false)
    private Proveedor proveedor;

    /**
     * Usuario que solicita el reabastecimiento
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariosolicitante", nullable = false)
    private Usuario usuarioSolicitante;

    /**
     * Fecha de solicitud del pedido
     * Se establece automáticamente al crear el pedido
     */
    @Column(name = "fechasolicitud", nullable = false)
    @Builder.Default
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    /**
     * Fecha estimada de entrega del proveedor
     * Campo opcional para programar recepciones
     */
    @Column(name = "fechaestimadaentrega")
    private LocalDateTime fechaEstimadaEntrega;

    /**
     * Estado actual del pedido de reabastecimiento
     * Usa el enum EstadoPedido para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    /**
     * Total estimado del pedido
     * Valor numérico con 2 decimales
     */
    @Column(name = "totalestimado", precision = 12, scale = 2)
    private BigDecimal totalEstimado;

    /**
     * Observaciones adicionales del pedido
     * Campo de texto opcional
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Prioridad del pedido de reabastecimiento
     * Controla la urgencia del pedido
     */
    @Column(name = "prioridad", length = 20)
    @Builder.Default
    private String prioridad = "normal";

    /**
     * Lista de detalles del pedido de reabastecimiento
     * Relación OneToMany con la entidad DetallePedidoReabastecimiento
     */
    @OneToMany(mappedBy = "pedidoReabastecimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedidoReabastecimiento> detallesReabastecimiento = new ArrayList<>();

    // Métodos de negocio

    /**
     * Calcula el total estimado del pedido basado en los detalles
     */
    public void calcularTotalEstimado() {
        if (this.detallesReabastecimiento != null && !this.detallesReabastecimiento.isEmpty()) {
            this.totalEstimado = this.detallesReabastecimiento.stream()
                    .map(DetallePedidoReabastecimiento::getSubtotalEstimado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalEstimado = BigDecimal.ZERO;
        }
    }

    /**
     * Agrega un detalle al pedido de reabastecimiento
     * @param detalle Detalle de reabastecimiento a agregar
     */
    public void agregarDetalle(DetallePedidoReabastecimiento detalle) {
        detallesReabastecimiento.add(detalle);
        detalle.setPedidoReabastecimiento(this);
        calcularTotalEstimado();
    }

    /**
     * Verifica si el pedido puede ser cancelado
     * @return true si el pedido puede ser cancelado
     */
    public boolean puedeCancelarse() {
        return this.estado.puedeCancelarse();
    }

    /**
     * Verifica si el pedido está completamente recibido
     * @return true si todos los detalles están recibidos
     */
    public boolean estaCompletamenteRecibido() {
        if (this.detallesReabastecimiento == null || this.detallesReabastecimiento.isEmpty()) {
            return false;
        }

        return this.detallesReabastecimiento.stream()
                .allMatch(detalle -> detalle.getEstado() == EstadoPedido.RECIBIDO_COMPLETO);
    }

    /**
     * Verifica si el pedido está parcialmente recibido
     * @return true si al menos un detalle está recibido parcialmente
     */
    public boolean estaParcialmenteRecibido() {
        if (this.detallesReabastecimiento == null) {
            return false;
        }

        return this.detallesReabastecimiento.stream()
                .anyMatch(detalle -> detalle.getEstado() == EstadoPedido.RECIBIDO_PARCIAL ||
                        detalle.getEstado() == EstadoPedido.RECIBIDO_COMPLETO);
    }

    /**
     * Actualiza el estado del pedido basado en los detalles
     */
    public void actualizarEstado() {
        if (estaCompletamenteRecibido()) {
            this.estado = EstadoPedido.RECIBIDO_COMPLETO;
        } else if (estaParcialmenteRecibido()) {
            this.estado = EstadoPedido.RECIBIDO_PARCIAL;
        }
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public PedidoReabastecimiento(String codigoPedido, Proveedor proveedor,
                                  Usuario usuarioSolicitante, String prioridad) {
        this.codigoPedido = codigoPedido;
        this.proveedor = proveedor;
        this.usuarioSolicitante = usuarioSolicitante;
        this.prioridad = prioridad != null ? prioridad : "normal";
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
        this.detallesReabastecimiento = new ArrayList<>();
        this.totalEstimado = BigDecimal.ZERO;
    }
}
