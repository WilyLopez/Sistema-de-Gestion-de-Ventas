package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una devolución de productos en el sistema.
 * Corresponde a la tabla 'devolucion' en la base de datos.
 *
 * Una devolución puede contener múltiples productos y está asociada
 * a una venta específica y al usuario que procesa la devolución.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "devolucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"detallesDevolucion"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddevolucion")
    @EqualsAndHashCode.Include
    private Long idDevolucion;

    /**
     * Venta asociada a la devolución
     * Relación ManyToOne con la entidad Venta
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idventa", nullable = false)
    private Venta venta;

    /**
     * Usuario que procesa la devolución
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora de la devolución
     * Se establece automáticamente al crear la devolución
     */
    @Column(name = "fechadevolucion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaDevolucion = LocalDateTime.now();

    /**
     * Motivo de la devolución
     * Descripción detallada del motivo
     */
    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    /**
     * Estado actual de la devolución
     * Usa el enum EstadoDevolucion para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoDevolucion estado = EstadoDevolucion.PENDIENTE;

    /**
     * Monto total de la devolución
     * Valor numérico con 2 decimales
     */
    @Column(name = "montodevolucion", precision = 12, scale = 2)
    private BigDecimal montoDevolucion;

    /**
     * Lista de detalles de la devolución (productos devueltos)
     * Relación OneToMany con la entidad DetalleDevolucion
     */
    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleDevolucion> detallesDevolucion = new ArrayList<>();

    // Métodos de negocio

    /**
     * Calcula el monto total de la devolución basado en los detalles
     */
    public void calcularMontoDevolucion() {
        if (this.detallesDevolucion != null && !this.detallesDevolucion.isEmpty()) {
            this.montoDevolucion = this.detallesDevolucion.stream()
                    .map(detalle -> detalle.getProducto().getPrecioVenta()
                            .multiply(BigDecimal.valueOf(detalle.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.montoDevolucion = BigDecimal.ZERO;
        }
    }

    /**
     * Agrega un detalle a la devolución y establece la relación bidireccional
     * @param detalle Detalle de devolución a agregar
     */
    public void agregarDetalle(DetalleDevolucion detalle) {
        detallesDevolucion.add(detalle);
        detalle.setDevolucion(this);
        calcularMontoDevolucion();
    }

    /**
     * Verifica si la devolución puede ser procesada
     * @return true si la devolución puede ser procesada
     */
    public boolean puedeProcesarse() {
        return this.estado.puedeProcesarse();
    }

    /**
     * Verifica si la devolución puede ser modificada
     * @return true si la devolución puede ser modificada
     */
    public boolean puedeModificarse() {
        return this.estado.puedeModificarse();
    }

    /**
     * Verifica si la devolución actualiza el stock
     * @return true si la devolución actualiza el stock
     */
    public boolean actualizaStock() {
        return this.estado.actualizaStock();
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public Devolucion(Venta venta, Usuario usuario, String motivo, EstadoDevolucion estado) {
        this.venta = venta;
        this.usuario = usuario;
        this.motivo = motivo;
        this.estado = estado != null ? estado : EstadoDevolucion.PENDIENTE;
        this.fechaDevolucion = LocalDateTime.now();
        this.detallesDevolucion = new ArrayList<>();
    }
}
