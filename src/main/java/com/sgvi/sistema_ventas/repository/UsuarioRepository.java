package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Usuario en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username el nombre de usuario a buscar
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param correo el correo electrónico a buscar
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     *
     * @param username el nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el correo electrónico especificado.
     *
     * @param correo el correo electrónico a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCorreo(String correo);

    /**
     * Busca usuarios por estado activo/inactivo.
     *
     * @param estado true para usuarios activos, false para inactivos
     * @return lista de usuarios que coinciden con el estado
     */
    List<Usuario> findByEstado(Boolean estado);

    /**
     * Busca usuarios por rol.
     *
     * @param idRol el identificador del rol
     * @return lista de usuarios con el rol especificado
     */
    List<Usuario> findByIdRol(Long idRol);

    /**
     * Busca usuarios por nombre o apellido (búsqueda case-insensitive).
     *
     * @param nombre el nombre o apellido a buscar
     * @return lista de usuarios que coinciden con el criterio
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Usuario> findByNombreOrApellidoContainingIgnoreCase(@Param("nombre") String nombre);
}