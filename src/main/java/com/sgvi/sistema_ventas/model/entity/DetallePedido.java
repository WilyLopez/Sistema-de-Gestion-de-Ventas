package com.sgvi.sistema_ventas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa el detalle de un pedido en el sistema.
 * Corresponde a la tabla 'detallepedido' en la base de datos.
 *
 * Cada detalle de pedido representa un producto solicitado
 * en un pedido específico, incluyendo cantidad y precio.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "detallepedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetallepedido")
    @EqualsAndHashCode.Include
    private Long idDetallePedido;

    /**
     * Pedido al que pertenece este detalle
     * Relación ManyToOne con la entidad Pedido
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpedido", nullable = false)
    @ToString.Exclude
    private Pedido pedido;

    /**
     * Producto solicitado en este detalle
     * Relación ManyToOne con la entidad Producto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    private Producto producto;

    /**
     * Cantidad del producto solicitado
     * Valor entero positivo mayor a cero
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario del producto al momento del pedido
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "preciounitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Subtotal del detalle (cantidad * precio unitario)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // Métodos de negocio

    /**
     * Calcula el subtotal del detalle basado en cantidad y precio
     */
    public void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        }
    }

    /**
     * Verifica si hay stock suficiente para este detalle de pedido
     * @return true si hay stock suficiente, false en caso contrario
     */
    public boolean hayStockSuficiente() {
        return this.producto != null && this.producto.getStock() >= this.cantidad;
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public DetallePedido(Pedido pedido, Producto producto, Integer cantidad, BigDecimal precioUnitario) {
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
}
