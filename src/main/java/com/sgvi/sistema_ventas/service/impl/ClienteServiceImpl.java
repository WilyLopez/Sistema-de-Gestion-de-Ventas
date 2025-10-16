package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import com.sgvi.sistema_ventas.repository.ClienteRepository;
import com.sgvi.sistema_ventas.service.interfaces.IClienteService;
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
 * Incluye validaciones de documentos peruanos (DNI, RUC, CE).
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

    @Override
    public Cliente actualizar(Long id, Cliente cliente) {
        log.info("Actualizando cliente con ID: {}", id);

        Cliente clienteExistente = obtenerPorId(id);

        // Validar documento si cambió
        if (!clienteExistente.getNumeroDocumento().equals(cliente.getNumeroDocumento())
                && existeDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
            throw new DuplicateResourceException("El documento ya está registrado");
        }

        // Validar correo si cambió
        if (cliente.getCorreo() != null
                && !cliente.getCorreo().equals(clienteExistente.getCorreo())
                && existeCorreo(cliente.getCorreo())) {
            throw new DuplicateResourceException("El correo ya está registrado");
        }

        // Actualizar campos
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

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando (soft delete) cliente con ID: {}", id);

        Cliente cliente = obtenerPorId(id);
        cliente.setEstado(false);

        clienteRepository.save(cliente);
        log.info("Cliente eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        return clienteRepository.findByTipoDocumentoAndNumeroDocumento(
                        tipoDocumento.getCodigo(), numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con documento: " + tipoDocumento + " " + numeroDocumento));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listarTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorNombre(String nombre, Pageable pageable) {
        List<Cliente> clientes = clienteRepository.findByNombreOrApellidoContainingIgnoreCase(nombre);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), clientes.size());
        return new PageImpl<>(clientes.subList(start, end), pageable, clientes.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listarActivos(Pageable pageable) {
        List<Cliente> clientes = clienteRepository.findByEstado(true);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), clientes.size());
        return new PageImpl<>(clientes.subList(start, end), pageable, clientes.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        return clienteRepository.existsByTipoDocumentoAndNumeroDocumento(
                tipoDocumento.getCodigo(), numeroDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        return clienteRepository.existsByCorreo(correo);
    }

    @Override
    public boolean validarDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return false;
        }

        // Validar longitud según tipo
        if (!tipoDocumento.validarLongitud(numeroDocumento)) {
            return false;
        }

        // Validar que solo contenga números
        if (!numeroDocumento.matches("\\d+")) {
            return false;
        }

        return true;
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    private void validarClienteNuevo(Cliente cliente) {
        if (cliente.getTipoDocumento() == null) {
            throw new ValidationException("El tipo de documento es obligatorio");
        }

        if (cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().trim().isEmpty()) {
            throw new ValidationException("El número de documento es obligatorio");
        }

        if (!validarDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
            throw new ValidationException("El número de documento no es válido para el tipo: " + cliente.getTipoDocumento());
        }

        if (existeDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
            throw new DuplicateResourceException("El documento ya está registrado");
        }

        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre es obligatorio");
        }

        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new ValidationException("El apellido es obligatorio");
        }

        if (cliente.getCorreo() != null && !cliente.getCorreo().trim().isEmpty()) {
            if (existeCorreo(cliente.getCorreo())) {
                throw new DuplicateResourceException("El correo ya está registrado");
            }
            validarEmail(cliente.getCorreo());
        }
    }

    private void validarEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new ValidationException("El formato del correo es inválido");
        }
    }
}