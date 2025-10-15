package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository para la entidad Usuario.
 * Proporciona métodos CRUD y de búsqueda para la gestión de usuarios y autenticación.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su nombre de usuario (username), esencial para el login (RF-001).
     * @param username El nombre de usuario a buscar.
     * @return Un Optional que contiene el Usuario si existe.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si existe un usuario con un nombre de usuario específico.
     * @param username El nombre de usuario a verificar.
     * @return true si el username ya existe, false en caso contrario.
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con un correo electrónico específico.
     * @param correo El correo electrónico a verificar (RF-003).
     * @return true si el correo ya existe, false en caso contrario.
     */
    boolean existsByCorreo(String correo);
}
