package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.service.interfaces.IUsuarioService;
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
 * Aplica principios SOLID y manejo de transacciones.
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

    @Override
    public Usuario crear(Usuario usuario) {
        log.info("Creando usuario: {}", usuario.getUsername());

        // Validaciones de negocio
        validarUsuarioNuevo(usuario);

        // Encriptar contraseña
        usuario.setContrasena(encriptarContrasena(usuario.getContrasena()));

        // Establecer valores por defecto
        usuario.setEstado(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());

        Usuario usuarioCreado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioCreado.getIdUsuario());

        return usuarioCreado;
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario usuarioExistente = obtenerPorId(id);

        // Validar que no se duplique username o correo (excepto el mismo usuario)
        if (!usuarioExistente.getUsername().equals(usuario.getUsername())
                && existeUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El username ya existe: " + usuario.getUsername());
        }

        if (!usuarioExistente.getCorreo().equals(usuario.getCorreo())
                && existeCorreo(usuario.getCorreo())) {
            throw new IllegalArgumentException("El correo ya existe: " + usuario.getCorreo());
        }

        // Actualizar campos permitidos (no se actualiza contraseña aquí)
        usuarioExistente.setUsername(usuario.getUsername());
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setApellido(usuario.getApellido());
        usuarioExistente.setCorreo(usuario.getCorreo());
        usuarioExistente.setTelefono(usuario.getTelefono());
        usuarioExistente.setDireccion(usuario.getDireccion());
        usuarioExistente.setIdRol(usuario.getIdRol());
        usuarioExistente.setFechaActualizacion(LocalDateTime.now());

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        log.info("Usuario actualizado exitosamente: {}", id);

        return usuarioActualizado;
    }

    @Override
    public void desactivar(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(false);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Usuario desactivado exitosamente: {}", id);
    }

    @Override
    public void activar(Long id) {
        log.info("Activando usuario con ID: {}", id);

        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(true);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Usuario activado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarPorEstado(Boolean estado, Pageable pageable) {
        List<Usuario> usuarios = usuarioRepository.findByEstado(estado)
                .stream()
                .toList();

        return new PageImpl<>(usuarios, pageable, usuarios.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorNombre(String nombre, Pageable pageable) {
        List<Usuario> usuarios = usuarioRepository.findByNombreOrApellidoContainingIgnoreCase(nombre)
                .stream()
                .toList();

        return new PageImpl<>(usuarios, pageable, usuarios.size());
    }


    @Override
    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String contrasena) {
        log.info("Autenticando usuario: {}", username);

        Usuario usuario = obtenerPorUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!usuario.getEstado()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        log.info("Usuario autenticado exitosamente: {}", username);
        return usuario;
    }

    @Override
    public void cambiarContrasena(Long id, String contrasenaActual, String contrasenaNueva) {
        log.info("Cambiando contraseña para usuario ID: {}", id);

        Usuario usuario = obtenerPorId(id);

        // Verificar contraseña actual
        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        // Validar nueva contraseña
        validarContrasena(contrasenaNueva);

        // Actualizar contraseña
        usuario.setContrasena(encriptarContrasena(contrasenaNueva));
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
        log.info("Contraseña cambiada exitosamente para usuario: {}", id);
    }

    @Override
    public void registrarLogin(Long id, LocalDateTime fechaLogin) {
        Usuario usuario = obtenerPorId(id);
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

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    private void validarUsuarioNuevo(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El username es obligatorio");
        }

        if (existeUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El username ya existe: " + usuario.getUsername());
        }

        if (existeCorreo(usuario.getCorreo())) {
            throw new IllegalArgumentException("El correo ya existe: " + usuario.getCorreo());
        }

        validarContrasena(usuario.getContrasena());
        validarEmail(usuario.getCorreo());
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    private void validarEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (email == null || !email.matches(emailRegex)) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }
    }
}