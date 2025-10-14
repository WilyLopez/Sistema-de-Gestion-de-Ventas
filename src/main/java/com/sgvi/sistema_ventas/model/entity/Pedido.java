package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pedido de cliente en el sistema.
 * Corresponde a la tabla 'pedido' en la base de datos.
 *
 * Un pedido puede contener múltiples productos y está asociado
 * a un cliente específico y al usuario que registra el pedido.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"detallesPedido"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpedido")
    @EqualsAndHashCode.Include
    private Long idPedido;

    /**
     * Código único del pedido (ej: "PED-2024-00001")
     * Único y no nulo
     */
    @Column(name = "codigopedido", unique = true, nullable = false, length = 20)
    private String codigoPedido;

    /**
     * Cliente que realiza el pedido
     * Relación ManyToOne con la entidad Cliente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcliente", nullable = false)
    private Cliente cliente;

    /**
     * Usuario que registra el pedido
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora del pedido
     * Se establece automáticamente al crear el pedido
     */
    @Column(name = "fechapedido", nullable = false)
    @Builder.Default
    private LocalDateTime fechaPedido = LocalDateTime.now();

    /**
     * Fecha estimada de entrega
     * Campo opcional para programar entregas
     */
    @Column(name = "fechaentrega")
    private LocalDateTime fechaEntrega;

    /**
     * Estado actual del pedido
     * Usa el enum EstadoPedido para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    /**
     * Subtotal del pedido (sin impuestos)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Total del pedido (con impuestos)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Dirección de envío del pedido
     * Información de entrega obligatoria
     */
    @Column(name = "direccionenvio", nullable = false, length = 255)
    private String direccionEnvio;

    /**
     * Observaciones adicionales del pedido
     * Campo de texto opcional
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Lista de detalles del pedido (productos solicitados)
     * Relación OneToMany con la entidad DetallePedido
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detallesPedido = new ArrayList<>();

    // Métodos de negocio

    /**
     * Calcula los totales del pedido basado en los detalles
     */
    public void calcularTotales() {
        if (this.detallesPedido != null && !this.detallesPedido.isEmpty()) {
            this.subtotal = this.detallesPedido.stream()
                    .map(DetallePedido::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Por simplicidad, asumimos mismo subtotal y total
            // En un caso real se calcularían impuestos y descuentos
            this.total = this.subtotal;
        } else {
            this.subtotal = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
        }
    }

    /**
     * Agrega un detalle al pedido y establece la relación bidireccional
     * @param detalle Detalle de pedido a agregar
     */
    public void agregarDetalle(DetallePedido detalle) {
        detallesPedido.add(detalle);
        detalle.setPedido(this);
        calcularTotales();
    }

    /**
     * Verifica si el pedido puede ser cancelado
     * @return true si el pedido puede ser cancelado
     */
    public boolean puedeCancelarse() {
        return this.estado.puedeCancelarse();
    }

    /**
     * Verifica si el pedido puede ser modificado
     * @return true si el pedido puede ser modificado
     */
    public boolean puedeModificarse() {
        return this.estado.puedeModificarse();
    }

    /**
     * Verifica si el pedido está en estado final
     * @return true si el pedido está en estado final
     */
    public boolean esEstadoFinal() {
        return this.estado.isEsFinal();
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public Pedido(String codigoPedido, Cliente cliente, Usuario usuario,
                  String direccionEnvio, EstadoPedido estado) {
        this.codigoPedido = codigoPedido;
        this.cliente = cliente;
        this.usuario = usuario;
        this.direccionEnvio = direccionEnvio;
        this.estado = estado != null ? estado : EstadoPedido.PENDIENTE;
        this.fechaPedido = LocalDateTime.now();
        this.detallesPedido = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }
}
