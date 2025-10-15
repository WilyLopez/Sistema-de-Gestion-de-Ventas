package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de proveedores.
 * Define los contratos según RF-006: Gestión de Proveedores
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IProveedorService {

    /**
     * RF-006: Crear un nuevo proveedor
     * @param proveedor Proveedor a crear
     * @return Proveedor creado
     */
    Proveedor crear(Proveedor proveedor);

    /**
     * RF-006: Actualizar proveedor existente
     * @param id ID del proveedor
     * @param proveedor Datos actualizados
     * @return Proveedor actualizado
     */
    Proveedor actualizar(Long id, Proveedor proveedor);

    /**
     * RF-006: Eliminar proveedor (soft delete)
     * @param id ID del proveedor
     */
    void eliminar(Long id);

    /**
     * RF-006: Obtener proveedor por ID
     * @param id ID del proveedor
     * @return Proveedor encontrado
     */
    Proveedor obtenerPorId(Long id);

    /**
     * RF-006: Buscar proveedor por RUC
     * @param ruc RUC del proveedor
     * @return Proveedor encontrado
     */
    Proveedor buscarPorRuc(String ruc);

    /**
     * RF-006: Listar todos los proveedores
     * @param pageable Parámetros de paginación
     * @return Página de proveedores
     */
    Page<Proveedor> listarTodos(Pageable pageable);

    /**
     * RF-006: Listar proveedores activos
     * @return Lista de proveedores activos
     */
    List<Proveedor> listarActivos();

    /**
     * RF-006: Buscar proveedores por razón social
     * @param razonSocial Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de proveedores
     */
    Page<Proveedor> buscarPorRazonSocial(String razonSocial, Pageable pageable);

    /**
     * Verificar si RUC ya existe
     * @param ruc RUC a verificar
     * @return true si existe
     */
    boolean existeRuc(String ruc);

    /**
     * Validar formato de RUC
     * @param ruc RUC a validar
     * @return true si es válido
     */
    boolean validarRuc(String ruc);
}

