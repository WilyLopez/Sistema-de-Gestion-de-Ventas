package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades MetodoPago en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas relacionadas con métodos de pago.
 */
@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    /**
     * Busca un método de pago por su nombre.
     *
     * @param nombre el nombre del método de pago a buscar
     * @return Optional con el método de pago encontrado o vacío si no existe
     */
    Optional<MetodoPago> findByNombre(String nombre);

    /**
     * Verifica si existe un método de pago con el nombre especificado.
     *
     * @param nombre el nombre del método de pago a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca métodos de pago por estado activo/inactivo.
     *
     * @param estado true para métodos activos, false para inactivos
     * @return lista de métodos de pago que coinciden con el estado
     */
    List<MetodoPago> findByEstado(Boolean estado);

    /**
     * Busca métodos de pago cuyos nombres contengan el texto especificado.
     *
     * @param nombre texto a buscar en los nombres de métodos de pago
     * @return lista de métodos de pago que coinciden con el criterio
     */
    @Query("SELECT mp FROM MetodoPago mp WHERE LOWER(mp.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<MetodoPago> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Obtiene todos los métodos de pago ordenados por nombre.
     *
     * @return lista de métodos de pago ordenados alfabéticamente
     */
    List<MetodoPago> findAllByOrderByNombreAsc();
}