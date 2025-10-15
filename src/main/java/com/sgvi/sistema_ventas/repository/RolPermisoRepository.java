package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.RolPermiso;

import com.sgvi.sistema_ventas.model.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de la relación entre Roles y Permisos.
 * Proporciona métodos para gestionar la asignación de permisos a roles.
 */
@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermiso.RolPermisoId> {

    /**
     * Busca todas las relaciones rol-permiso por ID de rol.
     *
     * @param idRol el identificador del rol
     * @return lista de relaciones rol-permiso para el rol especificado
     */
    List<RolPermiso> findByIdRol(Long idRol);

    /**
     * Busca todas las relaciones rol-permiso por ID de permiso.
     *
     * @param idPermiso el identificador del permiso
     * @return lista de relaciones rol-permiso para el permiso especificado
     */
    List<RolPermiso> findByIdPermiso(Long idPermiso);

    /**
     * Verifica si existe una relación entre un rol y un permiso específicos.
     *
     * @param idRol el identificador del rol
     * @param idPermiso el identificador del permiso
     * @return true si existe la relación, false en caso contrario
     */
    boolean existsByIdRolAndIdPermiso(Long idRol, Long idPermiso);

    /**
     * Elimina todas las relaciones rol-permiso para un rol específico.
     *
     * @param idRol el identificador del rol
     */
    @Modifying
    @Query("DELETE FROM RolPermiso rp WHERE rp.idRol = :idRol")
    void deleteByIdRol(@Param("idRol") Long idRol);

    /**
     * Elimina todas las relaciones rol-permiso para un permiso específico.
     *
     * @param idPermiso el identificador del permiso
     */
    @Modifying
    @Query("DELETE FROM RolPermiso rp WHERE rp.idPermiso = :idPermiso")
    void deleteByIdPermiso(@Param("idPermiso") Long idPermiso);

    /**
     * Elimina una relación específica rol-permiso.
     *
     * @param idRol el identificador del rol
     * @param idPermiso el identificador del permiso
     */
    @Modifying
    @Query("DELETE FROM RolPermiso rp WHERE rp.idRol = :idRol AND rp.idPermiso = :idPermiso")
    void deleteByIdRolAndIdPermiso(@Param("idRol") Long idRol, @Param("idPermiso") Long idPermiso);

    /**
     * Obtiene la lista de IDs de permisos asignados a un rol.
     *
     * @param idRol el identificador del rol
     * @return lista de IDs de permisos asignados al rol
     */
    @Query("SELECT rp.idPermiso FROM RolPermiso rp WHERE rp.idRol = :idRol")
    List<Long> findPermisoIdsByIdRol(@Param("idRol") Long idRol);
}