package com.sgvi.sistema_ventas.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa el detalle de una devolución en el sistema.
 * Corresponde a la tabla 'detalledevolucion' en la base de datos.
 *
 * Cada detalle de devolución representa un producto devuelto
 * en una devolución específica, incluyendo cantidad y motivo.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "detalledevolucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetalleDevolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalledevolucion")
    @EqualsAndHashCode.Include
    private Long idDetalleDevolucion;

    /**
     * Devolución a la que pertenece este detalle
     * Relación ManyToOne con la entidad Devolucion
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddevolucion", nullable = false)
    @ToString.Exclude
    private Devolucion devolucion;

    /**
     * Producto devuelto en este detalle
     * Relación ManyToOne con la entidad Producto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    private Producto producto;

    /**
     * Cantidad del producto devuelto
     * Valor entero positivo mayor a cero
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Motivo específico para la devolución de este producto
     * Campo de texto opcional
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    // Métodos de negocio

    /**
     * Verifica si la cantidad devuelta es válida
     * @return true si la cantidad es válida, false en caso contrario
     */
    public boolean validarCantidad() {
        return this.cantidad != null && this.cantidad > 0;
    }

    /**
     * Actualiza el stock del producto después de la devolución
     */
    public void actualizarStockProducto() {
        if (this.producto != null && this.cantidad != null && this.devolucion.actualizaStock()) {
            int nuevoStock = this.producto.getStock() + this.cantidad;
            this.producto.setStock(nuevoStock);
        }
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public DetalleDevolucion(Devolucion devolucion, Producto producto, Integer cantidad, String motivo) {
        this.devolucion = devolucion;
        this.producto = producto;
        this.cantidad = cantidad;
        this.motivo = motivo;
    }
}
