package com.sgvi.sistema_ventas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa el detalle de una venta en el sistema.
 * Corresponde a la tabla 'detalleventa' en la base de datos.
 *
 * Cada detalle de venta representa un producto vendido en una venta específica,
 * incluyendo cantidad, precio unitario y subtotal.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "detalleventa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle")
    @EqualsAndHashCode.Include
    private Long idDetalle;

    /**
     * Venta a la que pertenece este detalle
     * Relación ManyToOne con la entidad Venta
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idventa", nullable = false)
    @ToString.Exclude
    private Venta venta;

    /**
     * Producto vendido en este detalle
     * Relación ManyToOne con la entidad Producto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    private Producto producto;

    /**
     * Cantidad del producto vendido
     * Valor entero positivo mayor a cero
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario del producto al momento de la venta
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "preciounitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Descuento aplicado al producto en esta venta
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "descuento", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Subtotal del detalle (cantidad * precio unitario - descuento)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // Métodos de negocio

    /**
     * Calcula el subtotal del detalle basado en cantidad, precio y descuento
     */
    public void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            BigDecimal totalSinDescuento = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
            this.subtotal = totalSinDescuento.subtract(this.descuento != null ? this.descuento : BigDecimal.ZERO);

            // Validar que el subtotal no sea negativo
            if (this.subtotal.compareTo(BigDecimal.ZERO) < 0) {
                this.subtotal = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Verifica si hay stock suficiente para este detalle
     * @return true si hay stock suficiente, false en caso contrario
     */
    public boolean hayStockSuficiente() {
        return this.producto != null && this.producto.getStock() >= this.cantidad;
    }

    /**
     * Actualiza el stock del producto después de la venta
     */
    public void actualizarStockProducto() {
        if (this.producto != null && this.cantidad != null) {
            int nuevoStock = this.producto.getStock() - this.cantidad;
            this.producto.setStock(Math.max(nuevoStock, 0));
        }
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public DetalleVenta(Venta venta, Producto producto, Integer cantidad,
                        BigDecimal precioUnitario, BigDecimal descuento) {
        this.venta = venta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
        calcularSubtotal();
    }
}
