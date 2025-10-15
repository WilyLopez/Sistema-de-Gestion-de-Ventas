package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de clientes.
 * Define los contratos según RF-010: Gestión de Clientes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IClienteService {

    /**
     * RF-010: Crear un nuevo cliente
     * @param cliente Cliente a crear
     * @return Cliente creado
     */
    Cliente crear(Cliente cliente);

    /**
     * RF-010: Actualizar cliente existente
     * @param id ID del cliente
     * @param cliente Datos actualizados
     * @return Cliente actualizado
     */
    Cliente actualizar(Long id, Cliente cliente);

    /**
     * RF-010: Eliminar cliente (soft delete)
     * @param id ID del cliente
     */
    void eliminar(Long id);

    /**
     * RF-010: Obtener cliente por ID
     * @param id ID del cliente
     * @return Cliente encontrado
     */
    Cliente obtenerPorId(Long id);

    /**
     * RF-010: Buscar cliente por documento
     * @param tipoDocumento Tipo de documento
     * @param numeroDocumento Número de documento
     * @return Cliente encontrado
     */
    Cliente buscarPorDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    /**
     * RF-010: Listar todos los clientes
     * @param pageable Parámetros de paginación
     * @return Página de clientes
     */
    Page<Cliente> listarTodos(Pageable pageable);

    /**
     * RF-010: Buscar clientes por nombre o apellido
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de clientes
     */
    Page<Cliente> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * RF-010: Listar clientes activos
     * @param pageable Parámetros de paginación
     * @return Página de clientes activos
     */
    Page<Cliente> listarActivos(Pageable pageable);

    /**
     * Verificar si documento ya existe
     * @param tipoDocumento Tipo de documento
     * @param numeroDocumento Número de documento
     * @return true si existe
     */
    boolean existeDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    /**
     * Verificar si correo ya existe
     * @param correo Correo a verificar
     * @return true si existe
     */
    boolean existeCorreo(String correo);

    /**
     * Validar número de documento según tipo
     * @param tipoDocumento Tipo de documento
     * @param numeroDocumento Número a validar
     * @return true si es válido
     */
    boolean validarDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
}
