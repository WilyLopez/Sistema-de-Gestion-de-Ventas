package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.UnauthorizedException;
import com.sgvi.sistema_ventas.model.dto.auth.RegisterRequestDTO;
import com.sgvi.sistema_ventas.model.dto.usuario.UsuarioDTO;
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
     * @return UsuarioDTO creado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws DuplicateResourceException si username o correo ya existen
     */
    UsuarioDTO crear(Usuario usuario);

    /**
     * RF-003: Actualizar información de un usuario existente
     * @param id ID del usuario a actualizar
     * @param usuario Datos actualizados del usuario
     * @return UsuarioDTO actualizado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    UsuarioDTO actualizar(Long id, Usuario usuario);

    /**
     * RF-003: Desactivar un usuario (soft delete)
     * @param id ID del usuario a desactivar
     * @throws ResourceNotFoundException si el usuario no existe
     */
    void desactivar(Long id);

    /**
     * RF-003: Activar un usuario previamente desactivado
     * @param id ID del usuario a activar
     * @throws ResourceNotFoundException si el usuario no existe
     */
    void activar(Long id);

    /**
     * RF-003: Obtener usuario por ID
     * @param id ID del usuario
     * @return UsuarioDTO encontrado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    UsuarioDTO obtenerPorId(Long id);

    /**
     * RF-003: Obtener usuario por ID (entidad)
     * @param id ID del usuario
     * @return Entidad Usuario encontrada
     * @throws ResourceNotFoundException si el usuario no existe
     */
    Usuario obtenerEntidadPorId(Long id);

    /**
     * RF-003: Obtener usuario por username
     * @param username Nombre de usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> obtenerPorUsername(String username);

    /**
     * RF-003: Listar todos los usuarios con paginación
     * @param pageable Parámetros de paginación
     * @return Página de usuarios (DTO)
     */
    Page<UsuarioDTO> listarTodos(Pageable pageable);

    /**
     * RF-003: Listar usuarios por estado (activo/inactivo)
     * @param estado Estado a filtrar
     * @param pageable Parámetros de paginación
     * @return Página de usuarios filtrados (DTO)
     */
    Page<UsuarioDTO> listarPorEstado(Boolean estado, Pageable pageable);

    /**
     * RF-003: Buscar usuarios por nombre o apellido
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de usuarios que coinciden (DTO)
     */
    Page<UsuarioDTO> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * RF-001: Autenticar usuario (validación de credenciales)
     * @param username Nombre de usuario
     * @param contrasena Contraseña sin encriptar
     * @return Usuario autenticado
     * @throws UnauthorizedException si las credenciales son inválidas
     */
    Usuario autenticar(String username, String contrasena);

    /**
     * RF-001: Cambiar contraseña de usuario
     * @param id ID del usuario
     * @param contrasenaActual Contraseña actual
     * @param contrasenaNueva Nueva contraseña
     * @throws UnauthorizedException si la contraseña actual no coincide
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

    /**
     * Registrar nuevo usuario desde DTO de registro
     * @param registerRequest DTO con datos de registro
     * @return UsuarioDTO registrado
     */
    UsuarioDTO registrarUsuario(RegisterRequestDTO registerRequest);
}