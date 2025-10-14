package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.TipoComprobante;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un comprobante fiscal (Boleta o Factura) en el sistema.
 * Corresponde a la tabla 'comprobante' en la base de datos.
 *
 * Cada comprobante está asociado a una venta y contiene la información
 * fiscal requerida para la emisión de documentos tributarios.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "comprobante",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"serie", "numero"}),
                @UniqueConstraint(columnNames = {"idventa"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcomprobante")
    @EqualsAndHashCode.Include
    private Long idComprobante;

    /**
     * Venta asociada a este comprobante
     * Relación OneToOne con la entidad Venta
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idventa", nullable = false, unique = true)
    @ToString.Exclude
    private Venta venta;

    /**
     * Tipo de comprobante (Boleta o Factura)
     * Usa el enum TipoComprobante para los valores permitidos
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoComprobante tipo;

    /**
     * Serie del comprobante (ej: "B001", "F001")
     * Parte de la clave única del comprobante
     */
    @Column(name = "serie", nullable = false, length = 10)
    private String serie;

    /**
     * Número del comprobante (ej: "00000001")
     * Parte de la clave única del comprobante
     */
    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    /**
     * Fecha de emisión del comprobante
     * Se establece automáticamente al crear el comprobante
     */
    @Column(name = "fechaemision", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEmision = LocalDateTime.now();

    /**
     * RUC del emisor (tienda)
     * Información fiscal de la empresa
     */
    @Column(name = "rucemisor", length = 15)
    private String rucEmisor;

    /**
     * Razón social del emisor (nombre de la empresa)
     * Información fiscal de la empresa
     */
    @Column(name = "razonsocialemisor", length = 150)
    private String razonSocialEmisor;

    /**
     * Dirección fiscal del emisor
     * Información fiscal de la empresa
     */
    @Column(name = "direccionemisor", length = 200)
    private String direccionEmisor;

    /**
     * Monto del IGV del comprobante
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "igv", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal igv = BigDecimal.ZERO;

    /**
     * Total del comprobante
     * Valor numérico con 2 decimales, no negativo
     */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Estado del comprobante (activo, anulado, etc.)
     */
    @Column(name = "estado", length = 20)
    @Builder.Default
    private String estado = "activo";

    // Métodos de negocio

    /**
     * Genera el formato completo de serie-número del comprobante
     * @return String en formato "Serie-Número" (ej: "B001-00000001")
     */
    public String getNumeroCompleto() {
        return String.format("%s-%s", this.serie, this.numero);
    }

    /**
     * Verifica si el comprobante es una factura
     * @return true si es factura, false si es boleta
     */
    public boolean esFactura() {
        return this.tipo == TipoComprobante.FACTURA;
    }

    /**
     * Verifica si el comprobante es una boleta
     * @return true si es boleta, false si es factura
     */
    public boolean esBoleta() {
        return this.tipo == TipoComprobante.BOLETA;
    }

    /**
     * Valida que la información fiscal esté completa según el tipo de comprobante
     * @return true si la información es válida, false en caso contrario
     */
    public boolean validarInformacionFiscal() {
        if (this.tipo == TipoComprobante.FACTURA) {
            return this.rucEmisor != null && !this.rucEmisor.trim().isEmpty() &&
                    this.razonSocialEmisor != null && !this.razonSocialEmisor.trim().isEmpty();
        }
        return true; // Para boletas no se requiere información fiscal del emisor
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public Comprobante(Venta venta, TipoComprobante tipo, String serie,
                       String numero, BigDecimal total) {
        this.venta = venta;
        this.tipo = tipo;
        this.serie = serie;
        this.numero = numero;
        this.total = total;
        this.fechaEmision = LocalDateTime.now();
        this.estado = "activo";
    }
}
