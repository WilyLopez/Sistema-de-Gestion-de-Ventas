package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.model.entity.Proveedor;
import com.sgvi.sistema_ventas.repository.ProveedorRepository;
import com.sgvi.sistema_ventas.service.interfaces.IProveedorService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de proveedores.
 * Incluye validación de RUC peruano.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProveedorServiceImpl implements IProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    public Proveedor crear(Proveedor proveedor) {
        log.info("Creando proveedor: {}", proveedor.getRazonSocial());

        validarProveedorNuevo(proveedor);

        proveedor.setEstado(true);
        proveedor.setFechaCreacion(LocalDateTime.now());

        Proveedor proveedorCreado = proveedorRepository.save(proveedor);
        log.info("Proveedor creado exitosamente con ID: {}", proveedorCreado.getIdProveedor());

        return proveedorCreado;
    }

    @Override
    public Proveedor actualizar(Long id, Proveedor proveedor) {
        log.info("Actualizando proveedor con ID: {}", id);

        Proveedor proveedorExistente = obtenerPorId(id);

        // Validar RUC si cambió
        if (proveedor.getRuc() != null
                && !proveedorExistente.getRuc().equals(proveedor.getRuc())
                && existeRuc(proveedor.getRuc())) {
            throw new IllegalArgumentException("El RUC ya está registrado: " + proveedor.getRuc());
        }

        proveedorExistente.setRuc(proveedor.getRuc());
        proveedorExistente.setRazonSocial(proveedor.getRazonSocial());
        proveedorExistente.setDireccion(proveedor.getDireccion());
        proveedorExistente.setTelefono(proveedor.getTelefono());
        proveedorExistente.setCorreo(proveedor.getCorreo());

        Proveedor proveedorActualizado = proveedorRepository.save(proveedorExistente);
        log.info("Proveedor actualizado exitosamente: {}", id);

        return proveedorActualizado;
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando (soft delete) proveedor con ID: {}", id);

        Proveedor proveedor = obtenerPorId(id);
        proveedor.setEstado(false);

        proveedorRepository.save(proveedor);
        log.info("Proveedor eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor buscarPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new IllegalArgumentException( "Proveedor no encontrado con RUC: " + ruc));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Proveedor> listarTodos(Pageable pageable) {
        return proveedorRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByEstadoTrueOrderByRazonSocialAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Proveedor> buscarPorRazonSocial(String razonSocial, Pageable pageable) {
        List<Proveedor> proveedores = proveedorRepository.findByRazonSocialContainingIgnoreCase(razonSocial);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), proveedores.size());
        return new PageImpl<>(proveedores.subList(start, end), pageable, proveedores.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }

    @Override
    public boolean validarRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }

        // RUC peruano debe tener 11 dígitos
        if (ruc.length() != 11) {
            return false;
        }

        // Solo números
        if (!ruc.matches("\\d+")) {
            return false;
        }

        // RUC empresarial empieza con 10 o 20
        String prefijo = ruc.substring(0, 2);
        return prefijo.equals("10") || prefijo.equals("20");
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarProveedorNuevo(Proveedor proveedor) {
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().trim().isEmpty()) {
            throw new ValidationException("La razón social es obligatoria");
        }

        if (proveedor.getRuc() != null && !proveedor.getRuc().trim().isEmpty()) {
            if (!validarRuc(proveedor.getRuc())) {
                throw new ValidationException("El RUC no es válido");
            }

            if (existeRuc(proveedor.getRuc())) {
                throw new IllegalArgumentException("El RUC ya está registrado: " + proveedor.getRuc());
            }
        }
    }
}

