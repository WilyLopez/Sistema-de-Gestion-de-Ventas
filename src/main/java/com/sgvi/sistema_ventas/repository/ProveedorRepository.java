package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Proveedor en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas relacionadas con proveedores.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Busca un proveedor por su RUC.
     *
     * @param ruc el RUC del proveedor a buscar
     * @return Optional con el proveedor encontrado o vacío si no existe
     */
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Busca un proveedor por su razón social.
     *
     * @param razonSocial la razón social del proveedor a buscar
     * @return Optional con el proveedor encontrado o vacío si no existe
     */
    Optional<Proveedor> findByRazonSocial(String razonSocial);

    /**
     * Verifica si existe un proveedor con el RUC especificado.
     *
     * @param ruc el RUC del proveedor a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByRuc(String ruc);

    /**
     * Verifica si existe un proveedor con la razón social especificada.
     *
     * @param razonSocial la razón social del proveedor a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByRazonSocial(String razonSocial);

    /**
     * Busca proveedores por estado activo/inactivo.
     *
     * @param estado true para proveedores activos, false para inactivos
     * @return lista de proveedores que coinciden con el estado
     */
    List<Proveedor> findByEstado(Boolean estado);

    /**
     * Busca proveedores cuyas razones sociales contengan el texto especificado.
     *
     * @param razonSocial texto a buscar en las razones sociales
     * @return lista de proveedores que coinciden con el criterio
     */
    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :razonSocial, '%'))")
    List<Proveedor> findByRazonSocialContainingIgnoreCase(@Param("razonSocial") String razonSocial);

    /**
     * Cuenta la cantidad de productos activos de un proveedor.
     *
     * @param idProveedor el identificador del proveedor
     * @return número de productos activos del proveedor
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.idProveedor = :idProveedor AND p.estado = true")
    Long countProductosActivosByProveedor(@Param("idProveedor") Long idProveedor);

    /**
     * Busca proveedores activos ordenados por razón social.
     *
     * @return lista de proveedores activos ordenados alfabéticamente
     */
    List<Proveedor> findByEstadoTrueOrderByRazonSocialAsc();
}