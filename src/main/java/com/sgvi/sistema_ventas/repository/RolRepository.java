package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Rol en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas relacionadas con roles.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre.
     *
     * @param nombre el nombre del rol a buscar
     * @return Optional con el rol encontrado o vacío si no existe
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Verifica si existe un rol con el nombre especificado.
     *
     * @param nombre el nombre del rol a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca roles por estado activo/inactivo.
     *
     * @param estado true para roles activos, false para inactivos
     * @return lista de roles que coinciden con el estado
     */
    List<Rol> findByEstado(Boolean estado);

    /**
     * Busca roles por nivel de acceso.
     *
     * @param nivelAcceso el nivel de acceso a buscar
     * @return lista de roles con el nivel de acceso especificado
     */
    List<Rol> findByNivelAcceso(Integer nivelAcceso);

    /**
     * Busca roles con nivel de acceso mayor o igual al especificado.
     *
     * @param nivelAccesoMinimo el nivel de acceso mínimo
     * @return lista de roles que cumplen con el criterio
     */
    List<Rol> findByNivelAccesoGreaterThanEqual(Integer nivelAccesoMinimo);
}