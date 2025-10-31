package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento de inventario en el sistema.
 * Corresponde a la tabla 'inventario' en la base de datos.
 *
 * Registra todos los movimientos de stock (entradas, salidas, ajustes, devoluciones)
 * para mantener el tracking completo del inventario.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idmovimiento")
    @EqualsAndHashCode.Include
    private Long idMovimiento;

    /**
     * Producto afectado por el movimiento
     * Relación ManyToOne con la entidad Producto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", nullable = false)
    private Producto producto;

    /**
     * Tipo de movimiento (entrada, salida, ajuste, devolución)
     * Usa el enum TipoMovimiento para los valores permitidos
     */
    @Column(name = "tipomovimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    /**
     * Cantidad movida en el inventario
     * Valor entero positivo
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Stock anterior antes del movimiento
     * Valor entero no negativo
     */
    @Column(name = "stockanterior", nullable = false)
    private Integer stockAnterior;

    /**
     * Nuevo stock después del movimiento
     * Valor entero no negativo
     */
    @Column(name = "stocknuevo", nullable = false)
    private Integer stockNuevo;

    /**
     * Fecha y hora del movimiento
     * Se establece automáticamente al crear el registro
     */
    @Column(name = "fechamovimiento", nullable = false)
    @Builder.Default
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    /**
     * Usuario que realizó el movimiento
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

    /**
     * Venta asociada al movimiento (opcional)
     * Solo aplica para movimientos de salida por ventas
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idventa")
    @ToString.Exclude
    private Venta venta;

    /**
     * Observación o motivo del movimiento
     * Campo de texto opcional
     */
    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    // Métodos de negocio

    /**
     * Calcula el nuevo stock basado en el tipo de movimiento
     */
    public void calcularStockNuevo() {
        this.stockNuevo = this.tipoMovimiento.calcularNuevoStock(this.stockAnterior, this.cantidad);
    }

    /**
     * Verifica si el movimiento incrementa el stock
     * @return true si incrementa el stock, false si lo decrementa
     */
    public boolean incrementaStock() {
        return this.tipoMovimiento.getIncrementaStock();
    }

    /**
     * Verifica si el movimiento requiere autorización especial
     * @return true si requiere autorización, false en caso contrario
     */
    public boolean requiereAutorizacion() {
        return this.tipoMovimiento.requiereAutorizacion();
    }

    /**
     * Valida que la cantidad sea válida para el movimiento
     * @return true si la cantidad es válida, false en caso contrario
     */
    public boolean validarCantidad() {
        if (this.cantidad <= 0) {
            return false;
        }

        if (this.tipoMovimiento == TipoMovimiento.SALIDA && this.cantidad > this.stockAnterior) {
            return false; // No se puede vender más del stock disponible
        }

        return true;
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public Inventario(Producto producto, TipoMovimiento tipoMovimiento, Integer cantidad,
                      Integer stockAnterior, Usuario usuario, Venta venta, String observacion) {
        this.producto = producto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.usuario = usuario;
        this.venta = venta;
        this.observacion = observacion;
        this.fechaMovimiento = LocalDateTime.now();
        calcularStockNuevo();
    }
}
