package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.UnauthorizedException;
import com.sgvi.sistema_ventas.exception.ValidationException;
import com.sgvi.sistema_ventas.model.dto.auth.RegisterRequestDTO;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.service.interfaces.IUsuarioService;
import com.sgvi.sistema_ventas.util.Constants;
import com.sgvi.sistema_ventas.util.validation.EmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de usuarios.
 * Maneja autenticación, autorización y gestión del ciclo de vida de usuarios.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailValidator emailValidator;

    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    /**
     * Crea un nuevo usuario en el sistema.
     * Valida username, correo y contraseña, luego encripta la contraseña.
     *
     * @param usuario Datos del usuario a crear
     * @return Usuario creado con ID asignado
     * @throws ValidationException Si los datos del usuario no son válidos
     * @throws DuplicateResourceException Si el username o correo ya existen
     */
    @Override
    public Usuario crear(Usuario usuario) {
        log.info("Creando usuario: {}", usuario.getUsername());

        validarUsuarioNuevo(usuario);

        usuario.setContrasena(encriptarContrasena(usuario.getContrasena()));

        usuario.setEstado(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());

        Usuario usuarioCreado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioCreado.getIdUsuario());

        return usuarioCreado;
    }

    /**
     * Actualiza los datos de un usuario existente.
     * No permite actualizar la contraseña, usar cambiarContrasena para eso.
     *
     * @param id Identificador del usuario
     * @param usuario Nuevos datos del usuario
     * @return Usuario actualizado
     * @throws ResourceNotFoundException Si el usuario no existe
     * @throws DuplicateResourceException Si el nuevo username o correo ya existen
     */
    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario usuarioExistente = obtenerPorId(id);

        if (!usuarioExistente.getUsername().equals(usuario.getUsername())
                && existeUsername(usuario.getUsername())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        if (!usuarioExistente.getCorreo().equals(usuario.getCorreo())
                && existeCorreo(usuario.getCorreo())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        String correoNormalizado = emailValidator.normalizar(usuario.getCorreo());
        if (!emailValidator.validar(correoNormalizado)) {
            throw new ValidationException("El formato del correo es inválido");
        }

        usuarioExistente.setUsername(usuario.getUsername());
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setApellido(usuario.getApellido());
        usuarioExistente.setCorreo(correoNormalizado);
        usuarioExistente.setTelefono(usuario.getTelefono());
        usuarioExistente.setDireccion(usuario.getDireccion());
        usuarioExistente.setIdRol(usuario.getIdRol());
        usuarioExistente.setFechaActualizacion(LocalDateTime.now());

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        log.info("Usuario actualizado exitosamente: {}", id);

        return usuarioActualizado;
    }

    /**
     * Desactiva un usuario del sistema.
     * El usuario no podrá autenticarse pero sus datos se conservan.
     *
     * @param id Identificador del usuario a desactivar
     * @throws ResourceNotFoundException Si el usuario no existe
     */
    @Override
    public void desactivar(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(false);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Usuario desactivado exitosamente: {}", id);
    }

    /**
     * Reactiva un usuario previamente desactivado.
     *
     * @param id Identificador del usuario a activar
     * @throws ResourceNotFoundException Si el usuario no existe
     */
    @Override
    public void activar(Long id) {
        log.info("Activando usuario con ID: {}", id);

        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(true);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Usuario activado exitosamente: {}", id);
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id Identificador del usuario
     * @return Usuario encontrado
     * @throws ResourceNotFoundException Si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    /**
     * Obtiene un usuario por su username.
     *
     * @param username Username del usuario
     * @return Optional con el usuario si existe
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Lista todos los usuarios con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Lista usuarios filtrados por estado con paginación.
     *
     * @param estado Estado del usuario (activo/inactivo)
     * @param pageable Configuración de paginación
     * @return Página de usuarios con el estado especificado
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarPorEstado(Boolean estado, Pageable pageable) {
        List<Usuario> usuarios = usuarioRepository.findByEstado(estado)
                .stream()
                .toList();

        return new PageImpl<>(usuarios, pageable, usuarios.size());
    }

    /**
     * Busca usuarios por nombre o apellido con paginación.
     *
     * @param nombre Texto a buscar en nombre o apellido
     * @param pageable Configuración de paginación
     * @return Página de usuarios que coinciden con la búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorNombre(String nombre, Pageable pageable) {
        List<Usuario> usuarios = usuarioRepository.findByNombreOrApellidoContainingIgnoreCase(nombre)
                .stream()
                .toList();

        return new PageImpl<>(usuarios, pageable, usuarios.size());
    }

    /**
     * Autentica un usuario validando sus credenciales.
     * Verifica que el usuario exista, esté activo y la contraseña sea correcta.
     *
     * @param username Username del usuario
     * @param contrasena Contraseña sin encriptar
     * @return Usuario autenticado
     * @throws UnauthorizedException Si las credenciales son inválidas o el usuario está inactivo
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String contrasena) {
        log.info("Autenticando usuario: {}", username);

        Usuario usuario = obtenerPorUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!usuario.getEstado()) {
            throw new UnauthorizedException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        log.info("Usuario autenticado exitosamente: {}", username);
        return usuario;
    }

    /**
     * Cambia la contraseña de un usuario.
     * Valida la contraseña actual antes de establecer la nueva.
     *
     * @param id ID del usuario
     * @param contrasenaActual Contraseña actual sin encriptar
     * @param contrasenaNueva Nueva contraseña sin encriptar
     * @throws ResourceNotFoundException Si el usuario no existe
     * @throws UnauthorizedException Si la contraseña actual es incorrecta
     * @throws ValidationException Si la nueva contraseña no cumple los requisitos
     */
    @Override
    public void cambiarContrasena(Long id, String contrasenaActual, String contrasenaNueva) {
        log.info("Cambiando contraseña para usuario ID: {}", id);

        Usuario usuario = obtenerPorId(id);

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new UnauthorizedException("Contraseña actual incorrecta");
        }

        validarContrasena(contrasenaNueva);

        usuario.setContrasena(encriptarContrasena(contrasenaNueva));
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Contraseña cambiada exitosamente para usuario: {}", id);
    }

    /**
     * Registra la fecha y hora del último login de un usuario.
     *
     * @param id ID del usuario
     * @param fechaLogin Fecha y hora del login
     * @throws ResourceNotFoundException Si el usuario no existe
     */
    @Override
    public void registrarLogin(Long id, LocalDateTime fechaLogin) {
        Usuario usuario = obtenerPorId(id);
        usuario.setUltimoLogin(fechaLogin);
        usuarioRepository.save(usuario);
    }

    /**
     * Encripta una contraseña usando BCrypt.
     *
     * @param contrasena Contraseña en texto plano
     * @return Contraseña encriptada
     */
    @Override
    public String encriptarContrasena(String contrasena) {
        return passwordEncoder.encode(contrasena);
    }

    /**
     * Verifica si ya existe un usuario con el username especificado.
     *
     * @param username Username a verificar
     * @return true si el username ya existe
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * Verifica si ya existe un usuario con el correo especificado.
     *
     * @param correo Correo electrónico a verificar
     * @return true si el correo ya existe
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    @Override
    public Usuario registrarUsuario(RegisterRequestDTO registerRequest) {
        Usuario usuario = Usuario.builder()
                .username(registerRequest.getUsername())
                .nombre(registerRequest.getNombre())
                .apellido(registerRequest.getApellido())
                .correo(registerRequest.getEmail())
                .contrasena(registerRequest.getPassword())
                .telefono(registerRequest.getTelefono())
                .direccion(registerRequest.getDireccion())
                .idRol(2L) // Rol por defecto: EMPLEADO
                .build();

        return crear(usuario);
    }

    /**
     * Valida todos los datos de un usuario nuevo antes de crearlo.
     *
     * @param usuario Datos del usuario a validar
     * @throws ValidationException Si algún dato no es válido
     * @throws DuplicateResourceException Si el username o correo ya existen
     */
    private void validarUsuarioNuevo(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new ValidationException("El username es obligatorio");
        }

        if (existeUsername(usuario.getUsername())) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        String correoNormalizado = emailValidator.normalizar(usuario.getCorreo());

        if (!emailValidator.validar(correoNormalizado)) {
            throw new ValidationException("El formato del correo es inválido");
        }

        if (existeCorreo(correoNormalizado)) {
            throw new DuplicateResourceException(Constants.ERR_DUPLICADO);
        }

        usuario.setCorreo(correoNormalizado);

        validarContrasena(usuario.getContrasena());
    }

    /**
     * Valida que una contraseña cumpla con los requisitos mínimos.
     * La contraseña debe tener al menos 6 caracteres.
     *
     * @param contrasena Contraseña a validar
     * @throws ValidationException Si la contraseña no cumple los requisitos
     */
    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ValidationException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
    }
}