package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.AlertaStock;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz de servicio para la gestión de alertas de stock.
 * Define los contratos según RF-011: Sistema de Alertas de Stock
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IAlertaService {

    /**
     * RF-011: Generar alerta de stock
     * @param alerta Alerta a generar
     * @return Alerta creada
     */
    AlertaStock generarAlerta(AlertaStock alerta);

    /**
     * RF-011: Generar alerta automática por stock bajo
     * @param idProducto ID del producto
     * @return Alerta generada (o null si no se requiere)
     */
    AlertaStock generarAlertaStockBajo(Long idProducto);

    /**
     * RF-011: Generar alerta automática por stock agotado
     * @param idProducto ID del producto
     * @return Alerta generada
     */
    AlertaStock generarAlertaStockAgotado(Long idProducto);

    /**
     * RF-011: Verificar y generar alertas para todos los productos
     * Método ejecutado periódicamente
     * @return Lista de alertas generadas
     */
    List<AlertaStock> verificarYGenerarAlertas();

    /**
     * RF-011: Obtener alertas no leídas
     * @param pageable Parámetros de paginación
     * @return Página de alertas no leídas
     */
    Page<AlertaStock> obtenerAlertasNoLeidas(Pageable pageable);

    /**
     * RF-011: Obtener alertas por producto
     * @param idProducto ID del producto
     * @param pageable Parámetros de paginación
     * @return Página de alertas del producto
     */
    Page<AlertaStock> obtenerAlertasPorProducto(Long idProducto, Pageable pageable);

    /**
     * RF-011: Buscar alertas con filtros
     * @param idProducto ID del producto (opcional)
     * @param tipoAlerta Tipo de alerta (opcional)
     * @param nivelUrgencia Nivel de urgencia (opcional)
     * @param leida Estado de lectura (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de alertas filtradas
     */
    Page<AlertaStock> buscarAlertasConFiltros(Long idProducto, TipoAlerta tipoAlerta,
                                              NivelUrgencia nivelUrgencia, Boolean leida,
                                              LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                              Pageable pageable);

    /**
     * RF-011: Marcar alerta como leída
     * @param idAlerta ID de la alerta
     * @param idUsuario ID del usuario que lee la alerta
     */
    void marcarComoLeida(Long idAlerta, Long idUsuario);

    /**
     * RF-011: Marcar múltiples alertas como leídas
     * @param idsAlertas Lista de IDs de alertas
     * @param idUsuario ID del usuario
     */
    void marcarVariasComoLeidas(List<Long> idsAlertas, Long idUsuario);

    /**
     * RF-011: Registrar acción tomada sobre una alerta
     * @param idAlerta ID de la alerta
     * @param accion Descripción de la acción
     */
    void registrarAccion(Long idAlerta, String accion);

    /**
     * RF-011: Obtener conteo de alertas no leídas por nivel de urgencia
     * @return Map con nivel de urgencia y cantidad
     */
    Map<NivelUrgencia, Long> contarAlertasNoLeidasPorUrgencia();

    /**
     * RF-011: Obtener alertas críticas no leídas
     * @return Lista de alertas críticas
     */
    List<AlertaStock> obtenerAlertasCriticas();

    /**
     * Verificar si ya existe alerta similar no leída
     * @param idProducto ID del producto
     * @param tipoAlerta Tipo de alerta
     * @return true si ya existe
     */
    boolean existeAlertaSimilar(Long idProducto, TipoAlerta tipoAlerta);

    /**
     * RF-014: Obtener alertas por período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de alertas en el período
     */
    List<AlertaStock> obtenerAlertasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}