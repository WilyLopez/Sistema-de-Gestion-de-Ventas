package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa el detalle de un pedido de reabastecimiento en el sistema.
 * Corresponde a la tabla 'detallepedidoreabastecimiento' en la base de datos.
 *
 * Cada detalle de reabastecimiento representa un producto solicitado
 * a un proveedor, incluyendo cantidades y precios estimados.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "detallepedidoreabastecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetallePedidoReabastecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetallepedidoreab")
    @EqualsAndHashCode.Include
    private Long idDetallePedidoReab;

    /**
     * Pedido de reabastecimiento al que pertenece este detalle
     * Relación ManyToOne con la entidad PedidoReabastecimiento
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpedidoreab", nullable = false)
    @ToString.Exclude
    private PedidoReabastecimiento pedidoReabastecimiento;

    /**
     * Producto solicitado en el reabastecimiento
     * Relación ManyToOne con la entidad Producto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    private Producto producto;

    /**
     * Cantidad solicitada del producto
     * Valor entero positivo mayor a cero
     */
    @Column(name = "cantidadsolicitada", nullable = false)
    private Integer cantidadSolicitada;

    /**
     * Cantidad recibida del producto
     * Valor entero no negativo
     */
    @Column(name = "cantidadrecibida")
    @Builder.Default
    private Integer cantidadRecibida = 0;

    /**
     * Precio de compra estimado del producto
     * Valor numérico con 2 decimales
     */
    @Column(name = "preciocompraestimado", precision = 10, scale = 2)
    private BigDecimal precioCompraEstimado;

    /**
     * Subtotal estimado del detalle (cantidad * precio estimado)
     * Valor numérico con 2 decimales
     */
    @Column(name = "subtotalestimado", precision = 12, scale = 2)
    private BigDecimal subtotalEstimado;

    /**
     * Estado actual del detalle del pedido
     * Usa el enum EstadoPedido para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    // Métodos de negocio

    /**
     * Calcula el subtotal estimado del detalle
     */
    public void calcularSubtotalEstimado() {
        if (this.cantidadSolicitada != null && this.precioCompraEstimado != null) {
            this.subtotalEstimado = this.precioCompraEstimado
                    .multiply(BigDecimal.valueOf(this.cantidadSolicitada));
        }
    }

    /**
     * Registra la recepción de una cantidad del producto
     * @param cantidadRecibida Cantidad recibida
     */
    public void registrarRecepcion(Integer cantidadRecibida) {
        if (cantidadRecibida != null && cantidadRecibida > 0) {
            this.cantidadRecibida = cantidadRecibida;

            if (this.cantidadRecibida.equals(this.cantidadSolicitada)) {
                this.estado = EstadoPedido.RECIBIDO_COMPLETO;
            } else if (this.cantidadRecibida > 0) {
                this.estado = EstadoPedido.RECIBIDO_PARCIAL;
            }
        }
    }

    /**
     * Verifica si el detalle está completamente recibido
     * @return true si la cantidad recibida es igual a la solicitada
     */
    public boolean estaCompletamenteRecibido() {
        return this.cantidadRecibida != null &&
                this.cantidadRecibida.equals(this.cantidadSolicitada);
    }

    /**
     * Verifica si el detalle está parcialmente recibido
     * @return true si se ha recibido al menos una unidad pero no todas
     */
    public boolean estaParcialmenteRecibido() {
        return this.cantidadRecibida != null &&
                this.cantidadRecibida > 0 &&
                this.cantidadRecibida < this.cantidadSolicitada;
    }

    /**
     * Verifica si el detalle está pendiente de recepción
     * @return true si no se ha recibido ninguna unidad
     */
    public boolean estaPendiente() {
        return this.cantidadRecibida == null || this.cantidadRecibida == 0;
    }

    /**
     * Calcula la cantidad pendiente de recibir
     * @return Cantidad que falta por recibir
     */
    public Integer getCantidadPendiente() {
        if (this.cantidadSolicitada == null || this.cantidadRecibida == null) {
            return this.cantidadSolicitada;
        }
        return Math.max(0, this.cantidadSolicitada - this.cantidadRecibida);
    }

    /**
     * Actualiza el stock del producto con la cantidad recibida
     */
    public void actualizarStockProducto() {
        if (this.producto != null && this.cantidadRecibida != null && this.cantidadRecibida > 0) {
            int nuevoStock = this.producto.getStock() + this.cantidadRecibida;
            this.producto.setStock(nuevoStock);
        }
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public DetallePedidoReabastecimiento(PedidoReabastecimiento pedidoReabastecimiento,
                                         Producto producto, Integer cantidadSolicitada,
                                         BigDecimal precioCompraEstimado) {
        this.pedidoReabastecimiento = pedidoReabastecimiento;
        this.producto = producto;
        this.cantidadSolicitada = cantidadSolicitada;
        this.precioCompraEstimado = precioCompraEstimado;
        this.cantidadRecibida = 0;
        this.estado = EstadoPedido.PENDIENTE;
        calcularSubtotalEstimado();
    }
}
