package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.Proveedor;
import com.sgvi.sistema_ventas.repository.ProveedorRepository;
import com.sgvi.sistema_ventas.service.interfaces.IProveedorService;
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.EmailValidator;
import com.sgvi.sistema_ventas.util.validation.RucValidator;
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
 * Incluye validación de RUC peruano utilizando validador especializado.
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
    private final RucValidator rucValidator;
    private final EmailValidator emailValidator;


    /**
     * Crea un nuevo proveedor en el sistema.
     * Valida RUC, correo y razón social antes de guardar.
     *
     * @param proveedor Datos del proveedor a crear
     * @return Proveedor creado con ID asignado
     * @throws ValidationException Si los datos del proveedor no son válidos
     * @throws DuplicateResourceException Si el RUC ya existe
     */
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

    /**
     * Actualiza los datos de un proveedor existente.
     * Valida cambio de RUC para evitar duplicados.
     *
     * @param id Identificador del proveedor
     * @param proveedor Nuevos datos del proveedor
     * @return Proveedor actualizado
     * @throws ResourceNotFoundException Si el proveedor no existe
     * @throws DuplicateResourceException Si el nuevo RUC ya existe
     */
    @Override
    public Proveedor actualizar(Long id, Proveedor proveedor) {
        log.info("Actualizando proveedor con ID: {}", id);

        Proveedor proveedorExistente = obtenerPorId(id);

        if (proveedor.getRuc() != null
                && !proveedorExistente.getRuc().equals(proveedor.getRuc())
                && existeRuc(proveedor.getRuc())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        if (proveedor.getRuc() != null && !rucValidator.validar(proveedor.getRuc())) {
            throw new ValidationException("El RUC no es válido");
        }

        if (proveedor.getCorreo() != null && !proveedor.getCorreo().trim().isEmpty()) {
            String correoNormalizado = emailValidator.normalizar(proveedor.getCorreo());
            if (!emailValidator.validar(correoNormalizado)) {
                throw new ValidationException("El formato del correo es inválido");
            }
            proveedor.setCorreo(correoNormalizado);
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

    /**
     * Realiza una eliminación lógica del proveedor.
     * Cambia el estado a inactivo sin eliminar el registro de la base de datos.
     *
     * @param id Identificador del proveedor a eliminar
     * @throws ResourceNotFoundException Si el proveedor no existe
     */
    @Override
    public void eliminar(Long id) {
        log.info("Eliminando (soft delete) proveedor con ID: {}", id);

        Proveedor proveedor = obtenerPorId(id);
        proveedor.setEstado(false);

        proveedorRepository.save(proveedor);
        log.info("Proveedor eliminado exitosamente: {}", id);
    }

    /**
     * Obtiene un proveedor por su identificador.
     *
     * @param id Identificador del proveedor
     * @return Proveedor encontrado
     * @throws ResourceNotFoundException Si el proveedor no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    /**
     * Busca un proveedor por su número de RUC.
     *
     * @param ruc Número de RUC del proveedor
     * @return Proveedor encontrado
     * @throws ResourceNotFoundException Si no existe proveedor con ese RUC
     */
    @Override
    @Transactional(readOnly = true)
    public Proveedor buscarPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con RUC: " + ruc));
    }

    /**
     * Lista todos los proveedores con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de proveedores
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Proveedor> listarTodos(Pageable pageable) {
        return proveedorRepository.findAll(pageable);
    }

    /**
     * Lista todos los proveedores activos ordenados por razón social.
     *
     * @return Lista de proveedores activos
     */
    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByEstadoTrueOrderByRazonSocialAsc();
    }

    /**
     * Busca proveedores por razón social con paginación.
     *
     * @param razonSocial Texto a buscar en la razón social
     * @param pageable Configuración de paginación
     * @return Página de proveedores que coinciden con la búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Proveedor> buscarPorRazonSocial(String razonSocial, Pageable pageable) {
        List<Proveedor> proveedores = proveedorRepository.findByRazonSocialContainingIgnoreCase(razonSocial);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), proveedores.size());
        return new PageImpl<>(proveedores.subList(start, end), pageable, proveedores.size());
    }

    /**
     * Verifica si ya existe un proveedor con el RUC especificado.
     *
     * @param ruc Número de RUC a verificar
     * @return true si el RUC ya está registrado
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }

    /**
     * Valida un RUC peruano utilizando el validador especializado.
     * Verifica formato, longitud y dígito verificador.
     *
     * @param ruc Número de RUC a validar
     * @return true si el RUC es válido
     */
    @Override
    public boolean validarRuc(String ruc) {
        return rucValidator.validar(ruc);
    }

    /**
     * Valida todos los datos de un proveedor nuevo antes de crearlo.
     * Verifica razón social, RUC y correo electrónico.
     *
     * @param proveedor Datos del proveedor a validar
     * @throws ValidationException Si algún dato no es válido
     * @throws DuplicateResourceException Si el RUC ya existe
     */
    private void validarProveedorNuevo(Proveedor proveedor) {
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().trim().isEmpty()) {
            throw new ValidationException("La razón social es obligatoria");
        }

        if (proveedor.getRuc() != null && !proveedor.getRuc().trim().isEmpty()) {
            if (!rucValidator.validar(proveedor.getRuc())) {
                throw new ValidationException("El RUC no es válido");
            }

            if (existeRuc(proveedor.getRuc())) {
                throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
            }
        }

        if (proveedor.getCorreo() != null && !proveedor.getCorreo().trim().isEmpty()) {
            String correoNormalizado = emailValidator.normalizar(proveedor.getCorreo());

            if (!emailValidator.validar(correoNormalizado)) {
                throw new ValidationException("El formato del correo es inválido");
            }

            proveedor.setCorreo(correoNormalizado);
        }
    }
}