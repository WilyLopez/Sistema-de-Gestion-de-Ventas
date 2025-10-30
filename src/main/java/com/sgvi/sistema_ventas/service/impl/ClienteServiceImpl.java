package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import com.sgvi.sistema_ventas.repository.ClienteRepository;
import com.sgvi.sistema_ventas.service.interfaces.IClienteService;
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.DniValidator;
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
 * Implementación del servicio de gestión de clientes.
 * Incluye validaciones de documentos peruanos utilizando validadores especializados.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteServiceImpl implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final DniValidator dniValidator;
    private final RucValidator rucValidator;
    private final EmailValidator emailValidator;

    /**
     * Crea un nuevo cliente en el sistema.
     * Valida documento, correo y datos obligatorios antes de guardar.
     *
     * @param cliente Datos del cliente a crear
     * @return Cliente creado con ID asignado
     * @throws ValidationException Si los datos del cliente no son válidos
     * @throws DuplicateResourceException Si el documento o correo ya existen
     */
    @Override
    public Cliente crear(Cliente cliente) {
        log.info("Creando cliente: {} {}", cliente.getNombre(), cliente.getApellido());

        validarClienteNuevo(cliente);

        cliente.setEstado(true);
        cliente.setFechaRegistro(LocalDateTime.now());

        Cliente clienteCreado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con ID: {}", clienteCreado.getIdCliente());

        return clienteCreado;
    }

    /**
     * Actualiza los datos de un cliente existente.
     * Valida cambios en documento y correo para evitar duplicados.
     *
     * @param id Identificador del cliente
     * @param cliente Nuevos datos del cliente
     * @return Cliente actualizado
     * @throws ResourceNotFoundException Si el cliente no existe
     * @throws DuplicateResourceException Si el nuevo documento o correo ya existen
     */
    @Override
    public Cliente actualizar(Long id, Cliente cliente) {
        log.info("Actualizando cliente con ID: {}", id);

        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (cliente.getCorreo() != null
                && !cliente.getCorreo().equals(clienteExistente.getCorreo())
                && clienteRepository.existsByCorreo(cliente.getCorreo())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        clienteExistente.setTipoDocumento(cliente.getTipoDocumento());
        clienteExistente.setNumeroDocumento(cliente.getNumeroDocumento());
        clienteExistente.setNombre(cliente.getNombre());
        clienteExistente.setApellido(cliente.getApellido());
        clienteExistente.setCorreo(cliente.getCorreo());
        clienteExistente.setTelefono(cliente.getTelefono());
        clienteExistente.setDireccion(cliente.getDireccion());
        clienteExistente.setFechaNacimiento(cliente.getFechaNacimiento());

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        log.info("Cliente actualizado exitosamente: {}", id);

        return clienteActualizado;
    }

    /**
     * Realiza una eliminación lógica del cliente.
     * Cambia el estado a inactivo sin eliminar el registro de la base de datos.
     *
     * @param id Identificador del cliente a eliminar
     * @throws ResourceNotFoundException Si el cliente no existe
     */
    @Override
    public void eliminar(Long id) {
        log.info("Eliminando (soft delete) cliente con ID: {}", id);

        Cliente cliente = obtenerPorId(id);
        cliente.setEstado(false);

        clienteRepository.save(cliente);
        log.info("Cliente eliminado exitosamente: {}", id);
    }

    /**
     * Obtiene un cliente por su identificador.
     *
     * @param id Identificador del cliente
     * @return Cliente encontrado
     * @throws ResourceNotFoundException Si el cliente no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    /**
     * Busca un cliente por tipo y número de documento.
     *
     * @param tipoDocumento Tipo de documento (DNI, RUC, CE)
     * @param numeroDocumento Número del documento
     * @return Cliente encontrado
     * @throws ResourceNotFoundException Si no existe cliente con ese documento
     */
    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        return clienteRepository.findByTipoDocumentoAndNumeroDocumento(
                        tipoDocumento, numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con documento: "
                                + tipoDocumento + " " + numeroDocumento));
    }

    /**
     * Lista todos los clientes con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de clientes
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listarTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    /**
     * Busca clientes por nombre o apellido con paginación.
     *
     * @param nombre Texto a buscar en nombre o apellido
     * @param pageable Configuración de paginación
     * @return Página de clientes que coinciden con la búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorNombre(String nombre, Pageable pageable) {
        List<Cliente> clientes = clienteRepository.findByNombreOrApellidoContainingIgnoreCase(nombre);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), clientes.size());
        return new PageImpl<>(clientes.subList(start, end), pageable, clientes.size());
    }

    /**
     * Lista solo los clientes activos con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de clientes activos
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listarActivos(Pageable pageable) {
        List<Cliente> clientes = clienteRepository.findByEstado(true);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), clientes.size());
        return new PageImpl<>(clientes.subList(start, end), pageable, clientes.size());
    }

    /**
     * Verifica si ya existe un cliente con el documento especificado.
     *
     * @param tipoDocumento Tipo de documento
     * @param numeroDocumento Número del documento
     * @return true si el documento ya está registrado
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        return clienteRepository.existsByTipoDocumentoAndNumeroDocumento(
                tipoDocumento, numeroDocumento);
    }

    /**
     * Verifica si ya existe un cliente con el correo especificado.
     *
     * @param correo Correo electrónico a verificar
     * @return true si el correo ya está registrado
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        return clienteRepository.existsByCorreo(correo);
    }

    /**
     * Valida un documento según su tipo utilizando los validadores especializados.
     *
     * @param tipoDocumento Tipo de documento (DNI, RUC, CE)
     * @param numeroDocumento Número del documento a validar
     * @return true si el documento es válido según las reglas del tipo
     */
    @Override
    public boolean validarDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return false;
        }

        if (!tipoDocumento.validarLongitud(numeroDocumento)) {
            return false;
        }

        switch (tipoDocumento) {
            case DNI:
                return dniValidator.validar(numeroDocumento);
            case RUC:
                return rucValidator.validar(numeroDocumento);
            case CE:
                return numeroDocumento.matches("\\d+");
            default:
                return false;
        }
    }

    /**
     * Valida todos los datos de un cliente nuevo antes de crearlo.
     * Verifica documento, nombre, apellido y correo electrónico.
     *
     * @param cliente Datos del cliente a validar
     * @throws ValidationException Si algún dato no es válido
     * @throws DuplicateResourceException Si el documento o correo ya existen
     */
    private void validarClienteNuevo(Cliente cliente) {
        if (cliente.getTipoDocumento() == null) {
            throw new ValidationException("El tipo de documento es obligatorio");
        }

        if (cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().trim().isEmpty()) {
            throw new ValidationException("El número de documento es obligatorio");
        }

        if (!validarDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
            throw new ValidationException("El número de documento no es válido para el tipo: "
                    + cliente.getTipoDocumento());
        }

        if (existeDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre es obligatorio");
        }

        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new ValidationException("El apellido es obligatorio");
        }

        if (cliente.getCorreo() != null && !cliente.getCorreo().trim().isEmpty()) {
            String correoNormalizado = emailValidator.normalizar(cliente.getCorreo());

            if (!emailValidator.validar(correoNormalizado)) {
                throw new ValidationException("El formato del correo es inválido");
            }

            if (existeCorreo(correoNormalizado)) {
                throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
            }

            cliente.setCorreo(correoNormalizado);
        }
    }
}