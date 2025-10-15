package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository para la entidad Producto.
 * Extiende JpaSpecificationExecutor para búsquedas avanzadas y filtrado (RF-005 Read).
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {

    /**
     * Busca un producto por su código único (SKU).
     * @param codigo El código del producto a buscar.
     * @return Un Optional que contiene el Producto si existe.
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Verifica si un código de producto ya existe (RF-005 Validaciones).
     * @param codigo El código a verificar.
     * @return true si el código ya existe, false en caso contrario.
     */
    boolean existsByCodigo(String codigo);
}