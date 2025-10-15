package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.Permiso;
import com.sgvi.sistema_ventas.model.entity.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

/**
 * Interfaz de servicio para la gestión de roles.
 * Define los contratos según RF-002: Gestión de Roles y Permisos
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IRolService {

    /**
     * RF-002: Crear un nuevo rol
     * @param rol Rol a crear
     * @return Rol creado
     */
    Rol crear(Rol rol);

    /**
     * RF-002: Actualizar rol existente
     * @param id ID del rol
     * @param rol Datos actualizados
     * @return Rol actualizado
     */
    Rol actualizar(Long id, Rol rol);

    /**
     * RF-002: Eliminar rol
     * @param id ID del rol a eliminar
     */
    void eliminar(Long id);

    /**
     * RF-002: Obtener rol por ID
     * @param id ID del rol
     * @return Rol encontrado
     */
    Rol obtenerPorId(Long id);

    /**
     * RF-002: Obtener rol por nombre
     * @param nombre Nombre del rol
     * @return Rol encontrado
     */
    Rol obtenerPorNombre(String nombre);

    /**
     * RF-002: Listar todos los roles
     * @param pageable Parámetros de paginación
     * @return Página de roles
     */
    Page<Rol> listarTodos(Pageable pageable);

    /**
     * RF-002: Listar roles activos
     * @return Lista de roles activos
     */
    List<Rol> listarActivos();

    /**
     * RF-002: Asignar permisos a un rol
     * @param idRol ID del rol
     * @param idsPermisos Lista de IDs de permisos
     */
    void asignarPermisos(Long idRol, List<Long> idsPermisos);

    /**
     * RF-002: Remover permiso de un rol
     * @param idRol ID del rol
     * @param idPermiso ID del permiso
     */
    void removerPermiso(Long idRol, Long idPermiso);

    /**
     * RF-002: Obtener permisos de un rol
     * @param idRol ID del rol
     * @return Set de permisos del rol
     */
    Set<Permiso> obtenerPermisos(Long idRol);

    /**
     * Verificar si un rol tiene un permiso específico
     * @param idRol ID del rol
     * @param nombrePermiso Nombre del permiso
     * @return true si tiene el permiso
     */
    boolean tienePermiso(Long idRol, String nombrePermiso);

    /**
     * Verificar si rol existe por nombre
     * @param nombre Nombre del rol
     * @return true si existe
     */
    boolean existePorNombre(String nombre);
}
