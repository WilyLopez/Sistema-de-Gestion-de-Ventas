package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Cliente en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas relacionadas con clientes.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por tipo y número de documento.
     *
     * @param tipoDocumento el tipo de documento (DNI, RUC, CE)
     * @param numeroDocumento el número de documento
     * @return Optional con el cliente encontrado o vacío si no existe
     */
    Optional<Cliente> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Busca un cliente por número de documento (sin importar el tipo).
     *
     * @param numeroDocumento el número de documento a buscar
     * @return Optional con el cliente encontrado o vacío si no existe
     */
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    /**
     * Busca un cliente por correo electrónico.
     *
     * @param correo el correo electrónico a buscar
     * @return Optional con el cliente encontrado o vacío si no existe
     */
    Optional<Cliente> findByCorreo(String correo);

    /**
     * Verifica si existe un cliente con el tipo y número de documento especificados.
     *
     * @param tipoDocumento el tipo de documento
     * @param numeroDocumento el número de documento
     * @return true si existe, false en caso contrario
     */
    boolean existsByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Verifica si existe un cliente con el correo electrónico especificado.
     *
     * @param correo el correo electrónico a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCorreo(String correo);

    /**
     * Busca clientes por estado activo/inactivo.
     *
     * @param estado true para clientes activos, false para inactivos
     * @return lista de clientes que coinciden con el estado
     */
    List<Cliente> findByEstado(Boolean estado);

    /**
     * Busca clientes por tipo de documento.
     *
     * @param tipoDocumento el tipo de documento (DNI, RUC, CE)
     * @return lista de clientes con el tipo de documento especificado
     */
    List<Cliente> findByTipoDocumento(String tipoDocumento);

    /**
     * Busca clientes por nombre o apellido (búsqueda case-insensitive).
     *
     * @param nombre el nombre o apellido a buscar
     * @return lista de clientes que coinciden con el criterio
     */
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Cliente> findByNombreOrApellidoContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca clientes cuyo número de documento contenga el texto especificado.
     *
     * @param numeroDocumento texto a buscar en los números de documento
     * @return lista de clientes que coinciden con el criterio
     */
    @Query("SELECT c FROM Cliente c WHERE c.numeroDocumento LIKE CONCAT('%', :numeroDocumento, '%')")
    List<Cliente> findByNumeroDocumentoContaining(@Param("numeroDocumento") String numeroDocumento);
}