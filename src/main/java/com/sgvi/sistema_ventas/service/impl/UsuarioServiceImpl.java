package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.exception.UnauthorizedException;
import com.sgvi.sistema_ventas.exception.ValidationException;
import com.sgvi.sistema_ventas.model.dto.auth.RegisterRequestDTO;
import com.sgvi.sistema_ventas.model.dto.usuario.UsuarioDTO;
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
import java.util.stream.Collectors;

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

    @Override
    public UsuarioDTO crear(Usuario usuario) {
        log.info("Creando usuario: {}", usuario.getUsername());

        validarUsuarioNuevo(usuario);

        usuario.setContrasena(encriptarContrasena(usuario.getContrasena()));
        usuario.setEstado(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());

        Usuario usuarioCreado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioCreado.getIdUsuario());

        return convertToDTO(usuarioCreado);
    }

    @Override
    public UsuarioDTO actualizar(Long id, Usuario usuario) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario usuarioExistente = obtenerEntidadPorId(id);

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

        return convertToDTO(usuarioActualizado);
    }

    @Override
    public void desactivar(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setEstado(false);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Usuario desactivado exitosamente: {}", id);
    }

    @Override
    public void activar(Long id) {
        log.info("Activando usuario con ID: {}", id);

        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setEstado(true);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Usuario activado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MSG_RECURSO_NO_ENCONTRADO + " con ID: " + id));
        return convertToDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> listarTodos(Pageable pageable) {
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
        return usuarios.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> listarPorEstado(Boolean estado, Pageable pageable) {
        List<Usuario> usuarios = usuarioRepository.findByEstado(estado);
        List<UsuarioDTO> usuariosDTO = usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(usuariosDTO, pageable, usuarios.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> buscarPorNombre(String nombre, Pageable pageable) {
        List<Usuario> usuarios = usuarioRepository.findByNombreOrApellidoContainingIgnoreCase(nombre);
        List<UsuarioDTO> usuariosDTO = usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(usuariosDTO, pageable, usuarios.size());
    }

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

    @Override
    public void cambiarContrasena(Long id, String contrasenaActual, String contrasenaNueva) {
        log.info("Cambiando contraseña para usuario ID: {}", id);

        Usuario usuario = obtenerEntidadPorId(id);

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new UnauthorizedException("Contraseña actual incorrecta");
        }

        validarContrasena(contrasenaNueva);

        usuario.setContrasena(encriptarContrasena(contrasenaNueva));
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Contraseña cambiada exitosamente para usuario: {}", id);
    }

    @Override
    public void registrarLogin(Long id, LocalDateTime fechaLogin) {
        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setUltimoLogin(fechaLogin);
        usuarioRepository.save(usuario);
    }

    @Override
    public String encriptarContrasena(String contrasena) {
        return passwordEncoder.encode(contrasena);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    @Override
    public UsuarioDTO registrarUsuario(RegisterRequestDTO registerRequest) {
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

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ValidationException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .direccion(usuario.getDireccion())
                .estado(usuario.getEstado())
                .idRol(usuario.getIdRol())
                .nombreRol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .fechaCreacion(usuario.getFechaCreacion())
                .ultimoLogin(usuario.getUltimoLogin())
                .build();
    }
}