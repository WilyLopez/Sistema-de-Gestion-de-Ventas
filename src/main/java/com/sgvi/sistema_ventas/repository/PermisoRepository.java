package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para la entidad Permiso.
 * Contiene la lista de permisos que pueden ser asignados a los roles.
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    // Métodos de búsqueda comunes si fueran necesarios, ej: findByNombre(String nombre)
}
