package com.sgvi.sistema_ventas.security;

import com.sgvi.sistema_ventas.model.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetails de Spring Security.
 * Encapsula los datos del usuario para el sistema de seguridad.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String email;
    private boolean activo;
    private String nombreRol;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Crea un UserPrincipal a partir de una entidad Usuario
     * @param usuario Entidad Usuario
     * @return UserPrincipal configurado
     */
    public static UserPrincipal build(Usuario usuario) {
        // Crear autoridad basada en el rol
        // Spring Security requiere el prefijo "ROLE_"
        String roleName = "ROLE_" + usuario.getRol().getNombre().toUpperCase();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        return new UserPrincipal(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getContrasena(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getEstado(),
                usuario.getRol().getNombre(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    /**
     * Obtiene el nombre completo del usuario
     * @return Nombre completo
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}