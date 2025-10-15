package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository para la entidad Rol.
 * Permite la gestión de roles en el sistema (RF-002).
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Busca un rol por su nombre.
     * @param nombre El nombre del rol a buscar.
     * @return Un Optional que contiene el Rol si existe.
     */
    Optional<Rol> findByNombre(String nombre);
}