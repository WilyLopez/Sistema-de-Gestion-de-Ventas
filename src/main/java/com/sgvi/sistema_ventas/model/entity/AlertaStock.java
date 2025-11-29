package com.sgvi.sistema_ventas.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import com.sgvi.sistema_ventas.util.converter.NivelUrgenciaConverter;
import com.sgvi.sistema_ventas.util.converter.TipoAlertaConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una alerta de stock generada en el sistema.
 * Corresponde a la tabla 'alertastock' en la base de datos.
 *
 * Registra las alertas automáticas o manuales relacionadas
 * con el stock de productos en el inventario.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "alertastock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"producto", "usuarioNotificado"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AlertaStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idalerta")
    @EqualsAndHashCode.Include
    private Long idAlerta;

    /**
     * Producto relacionado con la alerta
     * Relación ManyToOne con la entidad Producto
     */
    @ManyToOne(fetch = FetchType.EAGER)  
    @JoinColumn(name = "idproducto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Producto producto;

    /**
     * Tipo de alerta generada
     * Usa el enum TipoAlerta para los valores permitidos
     */
    @Convert(converter = TipoAlertaConverter.class)
    @Column(name = "tipoalerta", nullable = false, length = 30)
    private TipoAlerta tipoAlerta;


    /**
     * Nivel de urgencia de la alerta
     * Usa el enum NivelUrgencia para los valores permitidos
     */
    @Column(name = "nivelurgencia", length = 20)
    @Convert(converter = NivelUrgenciaConverter.class)
    private NivelUrgencia nivelUrgencia;

    /**
     * Mensaje descriptivo de la alerta
     * Información detallada sobre la alerta generada
     */
    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    /**
     * Stock actual del producto al momento de generar la alerta
     * Valor entero no negativo
     */
    @Column(name = "stockactual", nullable = false)
    private Integer stockActual;

    /**
     * Stock umbral que activó la alerta
     * Valor entero no negativo
     */
    @Column(name = "stockumbral", nullable = false)
    private Integer stockUmbral;

    /**
     * Indica si la alerta ha sido leída por un usuario
     * Controla el estado de lectura de la alerta
     */
    @Column(name = "leida")
    @Builder.Default
    private Boolean leida = false;

    /**
     * Fecha y hora de generación de la alerta
     * Se establece automáticamente al crear la alerta
     */
    @Column(name = "fechaalerta", nullable = false)
    @Builder.Default
    private LocalDateTime fechaAlerta = LocalDateTime.now();

    /**
     * Fecha y hora de lectura de la alerta
     * Se establece cuando un usuario marca la alerta como leída
     */
    @Column(name = "fechalectura")
    private LocalDateTime fechaLectura;

    /**
     * Usuario notificado de la alerta
     * Relación ManyToOne con la entidad Usuario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuarionotificado")
    private Usuario usuarioNotificado;

    /**
     * Acción tomada en respuesta a la alerta
     * Descripción de la acción realizada
     */
    @Column(name = "acciontomada", length = 100)
    private String accionTomada;

    // Métodos de negocio

    /**
     * Marca la alerta como leída
     * @param usuario Usuario que marca la alerta como leída
     */
    public void marcarComoLeida(Usuario usuario) {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
        this.usuarioNotificado = usuario;
    }

    /**
     * Marca la alerta como no leída
     */
    public void marcarComoNoLeida() {
        this.leida = false;
        this.fechaLectura = null;
        this.usuarioNotificado = null;
    }

    /**
     * Registra una acción tomada en respuesta a la alerta
     * @param accion Descripción de la acción realizada
     */
    public void registrarAccion(String accion) {
        this.accionTomada = accion;
    }

    /**
     * Verifica si la alerta está pendiente de lectura
     * @return true si la alerta no ha sido leída
     */
    public boolean estaPendiente() {
        return !Boolean.TRUE.equals(this.leida);
    }

    /**
     * Obtiene la clase CSS para mostrar la alerta según su nivel de urgencia
     * @return Clase CSS correspondiente al nivel de urgencia
     */
    public String getClaseCss() {
        return this.nivelUrgencia != null ? this.nivelUrgencia.getBadgeClass() : "badge-secondary";
    }

    /**
     * Constructor con parámetros esenciales
     */
    @Builder
    public AlertaStock(Producto producto, TipoAlerta tipoAlerta, String mensaje,
                       Integer stockActual, Integer stockUmbral) {
        this.producto = producto;
        this.tipoAlerta = tipoAlerta;
        this.mensaje = mensaje;
        this.stockActual = stockActual;
        this.stockUmbral = stockUmbral;
        this.nivelUrgencia = tipoAlerta.getNivelUrgenciaPorDefecto();
        this.leida = false;
        this.fechaAlerta = LocalDateTime.now();
    }
}
