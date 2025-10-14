package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import com.sgvi.sistema_ventas.model.enums.TipoComprobante;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una venta en el sistema.
 * Corresponde a la tabla 'venta' en la base de datos.
 *
 * Una venta puede tener múltiples detalles de venta (DetalleVenta)
 * y está asociada a un cliente, usuario (vendedor) y método de pago.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"detallesVenta", "comprobante"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idventa")
    @EqualsAndHashCode.Include
    private Long idVenta;

    /**
     * Código único de la venta (ej: V-2024-00001)
     * Único y no nulo
     */
    @Column(name = "codigoventa", unique = true, nullable = false, length = 20)
    private String codigoVenta;

    /**
     * Cliente que realiza la compra
     * Relación ManyToOne con la entidad Cliente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcliente", nullable = false)
    private Cliente cliente;

    /**
     * Usuario (vendedor) que registra la venta
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora en que se realizó la venta
     * Se establece automáticamente al crear la venta
     */
    @Column(name = "fechaventa", nullable = false)
    @Builder.Default
    private LocalDateTime fechaVenta = LocalDateTime.now();

    /**
     * Subtotal de la venta (sin IGV)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Impuesto General a las Ventas (IGV)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "igv", precision = 12, scale = 2)
    private BigDecimal igv;

    /**
     * Total de la venta (subtotal + IGV)
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Método de pago utilizado en la venta
     * Relación ManyToOne con la entidad MetodoPago
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmetodopago", nullable = false)
    private MetodoPago metodoPago;

    /**
     * Estado actual de la venta
     * Usa el enum EstadoVenta para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.EN_PROCESO;

    /**
     * Tipo de comprobante generado (Boleta o Factura)
     * Usa el enum TipoComprobante para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipocomprobante", length = 10)
    private TipoComprobante tipoComprobante;

    /**
     * Observaciones o notas adicionales sobre la venta
     * Campo de texto opcional
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Lista de detalles de la venta (productos vendidos)
     * Relación OneToMany con la entidad DetalleVenta
     * Cascade: las operaciones se propagan a los detalles
     * OrphanRemoval: si se elimina un detalle, se elimina de la BD
     */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    /**
     * Comprobante asociado a la venta
     * Relación OneToOne con la entidad Comprobante
     */
    @OneToOne(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Comprobante comprobante;

    /**
     * Fecha de creación del registro
     * Se establece automáticamente al crear la entidad
     */
    @Column(name = "fechacreacion", updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha de última actualización del registro
     * Se actualiza automáticamente al modificar la entidad
     */
    @Column(name = "fechaactualizacion")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    // Métodos de negocio

    /**
     * Calcula el total de la venta basado en el subtotal e IGV
     */
    public void calcularTotal() {
        if (this.subtotal != null && this.igv != null) {
            this.total = this.subtotal.add(this.igv);
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Agrega un detalle a la venta y establece la relación bidireccional
     * @param detalle Detalle de venta a agregar
     */
    public void agregarDetalle(DetalleVenta detalle) {
        detallesVenta.add(detalle);
        detalle.setVenta(this);
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Remueve un detalle de la venta
     * @param detalle Detalle de venta a remover
     */
    public void removerDetalle(DetalleVenta detalle) {
        detallesVenta.remove(detalle);
        detalle.setVenta(null);
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Verifica si la venta puede ser anulada
     * @return true si la venta puede ser anulada
     */
    public boolean puedeAnularse() {
        return this.estado.puedeAnularse();
    }

    /**
     * Verifica si la venta puede ser modificada
     * @return true si la venta puede ser modificada
     */
    public boolean puedeModificarse() {
        return this.estado.puedeModificarse();
    }

    // Callbacks JPA
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.fechaVenta == null) {
            this.fechaVenta = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
