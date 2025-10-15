package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository para la entidad RolPermiso.
 * Gestiona las asignaciones de permisos a roles (RF-002).
 */
@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    /**
     * Encuentra todos los permisos asociados a un Rol específico.
     * @param rolId El ID del rol.
     * @return Lista de relaciones RolPermiso.
     */
    List<RolPermiso> findByRolId(Integer rolId);
}
