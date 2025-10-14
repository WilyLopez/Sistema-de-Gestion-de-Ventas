package com.sgvi.sistema_ventas.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la configuración de alertas de stock en el sistema.
 * Corresponde a la tabla 'configuracionalerta' en la base de datos.
 *
 * Define los umbrales y parámetros para la generación automática
 * de alertas de stock en el sistema.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "configuracionalerta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConfiguracionAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idconfiguracion")
    @EqualsAndHashCode.Include
    private Long idConfiguracion;

    /**
     * Tipo de alerta configurada
     * Valor único que identifica el tipo de alerta
     */
    @Column(name = "tipoalerta", unique = true, nullable = false, length = 50)
    private String tipoAlerta;

    /**
     * Descripción de la configuración de alerta
     * Información detallada sobre el propósito de la alerta
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Stock mínimo global para alertas
     * Valor umbral para alertas de stock mínimo
     */
    @Column(name = "stockminimoglobal")
    @Builder.Default
    private Integer stockMinimoGlobal = 5;

    /**
     * Porcentaje de umbral para cálculos de alertas
     * Valor decimal entre 0 y 1 (ej: 0.1 = 10%)
     */
    @Column(name = "porcentajeumbral", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeUmbral = BigDecimal.valueOf(0.1);

    /**
     * Indica si la alerta está activa en el sistema
     * Permite habilitar/deshabilitar tipos de alertas
     */
    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    /**
     * Usuario responsable de esta configuración
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuarioresponsable")
    private Usuario usuarioResponsable;

    /**
     * Fecha de creación de la configuración
     * Se establece automáticamente al crear el registro
     */
    @Column(name = "fechacreacion", updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Métodos de negocio

    /**
     * Calcula el stock mínimo específico para un producto
     * @param stockPromedio Stock promedio histórico del producto
     * @return Stock mínimo calculado para el producto
     */
    public Integer calcularStockMinimoProducto(Integer stockPromedio) {
        if (stockPromedio == null) {
            return this.stockMinimoGlobal;
        }

        BigDecimal stockMinimoCalculado = BigDecimal.valueOf(stockPromedio)
                .multiply(this.porcentajeUmbral);

        return Math.max(stockMinimoCalculado.intValue(), this.stockMinimoGlobal);
    }

    /**
     * Verifica si la configuración está activa
     * @return true si la configuración está activa
     */
    public boolean estaActiva() {
        return Boolean.TRUE.equals(this.activo);
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public ConfiguracionAlerta(String tipoAlerta, String descripcion, Integer stockMinimoGlobal,
                               BigDecimal porcentajeUmbral, Boolean activo) {
        this.tipoAlerta = tipoAlerta;
        this.descripcion = descripcion;
        this.stockMinimoGlobal = stockMinimoGlobal != null ? stockMinimoGlobal : 5;
        this.porcentajeUmbral = porcentajeUmbral != null ? porcentajeUmbral : BigDecimal.valueOf(0.1);
        this.activo = activo != null ? activo : true;
        this.fechaCreacion = LocalDateTime.now();
    }
}
