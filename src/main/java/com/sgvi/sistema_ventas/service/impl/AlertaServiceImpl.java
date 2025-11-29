package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.dto.common.AlertaStockResponseDTO;
import com.sgvi.sistema_ventas.model.entity.AlertaStock;
import com.sgvi.sistema_ventas.service.interfaces.IAlertaService;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import com.sgvi.sistema_ventas.repository.ProductoRepository;
import com.sgvi.sistema_ventas.repository.AlertaStockRepository;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de alertas de stock.
 * Genera alertas automáticas cuando el stock alcanza niveles críticos.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertaServiceImpl implements IAlertaService {

    private final AlertaStockRepository alertaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public AlertaStock generarAlerta(AlertaStock alerta) {
        log.info("Generando alerta tipo: {} para producto ID: {}",
                alerta.getTipoAlerta(), alerta.getProducto().getIdProducto());

        // Evitar duplicados de alertas no leídas
        if (existeAlertaSimilar(alerta.getProducto().getIdProducto(), alerta.getTipoAlerta())) {
            log.info("Ya existe alerta similar no leída. No se genera duplicado.");
            return null;
        }

        alerta.setFechaAlerta(LocalDateTime.now());
        alerta.setLeida(false);

        // Determinar nivel de urgencia si no está definido
        if (alerta.getNivelUrgencia() == null) {
            alerta.setNivelUrgencia(alerta.getTipoAlerta().getNivelUrgenciaPorDefecto());
        }

        AlertaStock alertaGuardada = alertaRepository.save(alerta);
        log.info("Alerta generada con ID: {}", alertaGuardada.getIdAlerta());

        return alertaGuardada;
    }

    @Override
    public AlertaStock generarAlertaStockBajo(Long idProducto) {
        Producto producto = obtenerProducto(idProducto);

        String mensaje = String.format(
                "Producto '%s' con stock BAJO. Stock actual: %d, Stock mínimo: %d. Se requiere reabastecimiento.",
                producto.getNombre(), producto.getStock(), producto.getStockMinimo()
        );

        NivelUrgencia urgencia = NivelUrgencia.determinarPorStock(
                producto.getStock(), producto.getStockMinimo()
        );

        AlertaStock alerta = AlertaStock.builder()
                .producto(producto)
                .tipoAlerta(TipoAlerta.STOCK_MINIMO)
                .nivelUrgencia(urgencia)
                .mensaje(mensaje)
                .stockActual(producto.getStock())
                .stockUmbral(producto.getStockMinimo())
                .build();

        return generarAlerta(alerta);
    }

    @Override
    public AlertaStock generarAlertaStockAgotado(Long idProducto) {
        Producto producto = obtenerProducto(idProducto);

        String mensaje = String.format(
                "CRÍTICO: Producto '%s' AGOTADO. Se requiere reabastecimiento URGENTE.",
                producto.getNombre()
        );

        AlertaStock alerta = AlertaStock.builder()
                .producto(producto)
                .tipoAlerta(TipoAlerta.STOCK_AGOTADO)
                .nivelUrgencia(NivelUrgencia.CRITICO)
                .mensaje(mensaje)
                .stockActual(0)
                .stockUmbral(producto.getStockMinimo())
                .build();

        return generarAlerta(alerta);
    }

    @Override
    public List<AlertaStock> verificarYGenerarAlertas() {
        log.info("Verificando stock de todos los productos para generar alertas...");

        List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();
        List<AlertaStock> alertasGeneradas = new java.util.ArrayList<>();

        for (Producto producto : productosStockBajo) {
            AlertaStock alerta;

            if (producto.getStock() == 0) {
                alerta = generarAlertaStockAgotado(producto.getIdProducto());
            } else {
                alerta = generarAlertaStockBajo(producto.getIdProducto());
            }

            if (alerta != null) {
                alertasGeneradas.add(alerta);
            }
        }

        log.info("Verificación completa. Se generaron {} alertas nuevas", alertasGeneradas.size());
        return alertasGeneradas;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaStockResponseDTO> obtenerAlertasNoLeidasDTO(Pageable pageable) {
        Page<AlertaStock> alertas = alertaRepository.findByLeidaFalse(pageable);
        return alertas.map(AlertaStockResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaStockResponseDTO> obtenerTodasLasAlertasDTO(Pageable pageable) {
        Page<AlertaStock> alertas = alertaRepository.findAll(pageable);
        return alertas.map(AlertaStockResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaStock> obtenerAlertasPorProducto(Long idProducto, Pageable pageable) {
        return alertaRepository.findByProductoIdProducto(idProducto, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaStockResponseDTO> buscarAlertasConFiltrosDTO(Long idProducto, TipoAlerta tipoAlerta,
                                                                NivelUrgencia nivelUrgencia, Boolean leida,
                                                                LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                                Pageable pageable) {
        Page<AlertaStock> alertas = alertaRepository.buscarAlertasConFiltros(
                idProducto, tipoAlerta, nivelUrgencia, leida, fechaInicio, fechaFin, pageable
        );
        return alertas.map(AlertaStockResponseDTO::fromEntity);
    }

    @Override
    public void marcarComoLeida(Long idAlerta, Long idUsuario) {
        log.info("Marcando alerta {} como leída por usuario {}", idAlerta, idUsuario);

        AlertaStock alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con ID: " + idAlerta));

        Usuario usuario = obtenerUsuario(idUsuario);

        alerta.marcarComoLeida(usuario);
        alertaRepository.save(alerta);
    }

    @Override
    public void marcarVariasComoLeidas(List<Long> idsAlertas, Long idUsuario) {
        log.info("Marcando {} alertas como leídas", idsAlertas.size());

        for (Long idAlerta : idsAlertas) {
            try {
                marcarComoLeida(idAlerta, idUsuario);
            } catch (Exception e) {
                log.error("Error al marcar alerta {} como leída: {}", idAlerta, e.getMessage());
            }
        }
    }

    @Override
    public void registrarAccion(Long idAlerta, String accion) {
        AlertaStock alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con ID: " + idAlerta));

        alerta.registrarAccion(accion);
        alertaRepository.save(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<NivelUrgencia, Long> contarAlertasNoLeidasPorUrgencia() {
        List<Object[]> resultados = alertaRepository.countAlertasNoLeidasPorUrgencia();
        Map<NivelUrgencia, Long> mapa = new HashMap<>();

        for (Object[] resultado : resultados) {
            NivelUrgencia nivel = (NivelUrgencia) resultado[0];
            Long cantidad = (Long) resultado[1];
            mapa.put(nivel, cantidad);
        }

        return mapa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaStockResponseDTO> obtenerAlertasCriticasDTO() {
        List<AlertaStock> alertas = alertaRepository.findByNivelUrgenciaAndLeidaFalse(NivelUrgencia.CRITICO);
        return alertas.stream()
                .map(AlertaStockResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAlertaSimilar(Long idProducto, TipoAlerta tipoAlerta) {
        return alertaRepository.existsAlertaSimilarNoLeida(idProducto, tipoAlerta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaStock> obtenerAlertasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return alertaRepository.findAlertasPorPeriodo(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaStock> obtenerTodasLasAlertas(Pageable pageable) {
        return alertaRepository.findAll(pageable);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Producto obtenerProducto(Long idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + idProducto));
    }

    private Usuario obtenerUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));
    }
}