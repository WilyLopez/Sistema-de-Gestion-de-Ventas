package com.sgvi.sistema_ventas.service.interfaces;


import com.sgvi.sistema_ventas.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de usuarios.
 * Define los contratos según RF-003: Gestión de Usuarios
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IUsuarioService {

    /**
     * RF-003: Crear un nuevo usuario en el sistema
     * @param usuario Entidad Usuario a crear
     * @return Usuario creado con ID asignado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    Usuario crear(Usuario usuario);

    /**
     * RF-003: Actualizar información de un usuario existente
     * @param id ID del usuario a actualizar
     * @param usuario Datos actualizados del usuario
     * @return Usuario actualizado
     */
    Usuario actualizar(Long id, Usuario usuario);

    /**
     * RF-003: Desactivar un usuario (soft delete)
     * @param id ID del usuario a desactivar
     */
    void desactivar(Long id);

    /**
     * RF-003: Activar un usuario previamente desactivado
     * @param id ID del usuario a activar
     */
    void activar(Long id);

    /**
     * RF-003: Obtener usuario por ID
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    Usuario obtenerPorId(Long id);

    /**
     * RF-003: Obtener usuario por username
     * @param username Nombre de usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> obtenerPorUsername(String username);

    /**
     * RF-003: Listar todos los usuarios con paginación
     * @param pageable Parámetros de paginación
     * @return Página de usuarios
     */
    Page<Usuario> listarTodos(Pageable pageable);

    /**
     * RF-003: Listar usuarios por estado (activo/inactivo)
     * @param estado Estado a filtrar
     * @param pageable Parámetros de paginación
     * @return Página de usuarios filtrados
     */
    Page<Usuario> listarPorEstado(Boolean estado, Pageable pageable);

    /**
     * RF-003: Buscar usuarios por nombre o apellido
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de usuarios que coinciden
     */
    Page<Usuario> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * RF-001: Autenticar usuario (validación de credenciales)
     * @param username Nombre de usuario
     * @param contrasena Contraseña sin encriptar
     * @return Usuario autenticado
     */
    Usuario autenticar(String username, String contrasena);

    /**
     * RF-001: Cambiar contraseña de usuario
     * @param id ID del usuario
     * @param contrasenaActual Contraseña actual
     * @param contrasenaNueva Nueva contraseña
     */
    void cambiarContrasena(Long id, String contrasenaActual, String contrasenaNueva);

    /**
     * RF-001: Registrar último login del usuario
     * @param id ID del usuario
     * @param fechaLogin Fecha y hora del login
     */
    void registrarLogin(Long id, LocalDateTime fechaLogin);

    /**
     * RNF-002.1: Encriptar contraseña con BCrypt
     * @param contrasena Contraseña en texto plano
     * @return Contraseña encriptada
     */
    String encriptarContrasena(String contrasena);

    /**
     * Verificar si un username ya existe
     * @param username Username a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existeUsername(String username);

    /**
     * Verificar si un correo ya existe
     * @param correo Correo a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existeCorreo(String correo);
}
