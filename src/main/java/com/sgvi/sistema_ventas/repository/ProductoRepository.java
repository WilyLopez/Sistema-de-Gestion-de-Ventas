package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Producto en la base de datos.
 * Proporciona métodos para operaciones CRUD y consultas avanzadas de productos.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su código único.
     *
     * @param codigo el código del producto a buscar
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Verifica si existe un producto con el código especificado.
     *
     * @param codigo el código del producto a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca productos por estado activo/inactivo.
     *
     * @param estado true para productos activos, false para inactivos
     * @return lista de productos que coinciden con el estado
     */
    List<Producto> findByEstado(Boolean estado);

    /**
     * Busca productos por categoría.
     *
     * @param idCategoria el identificador de la categoría
     * @return lista de productos de la categoría especificada
     */
    List<Producto> findByIdCategoria(Long idCategoria);

    /**
     * Busca productos por proveedor.
     *
     * @param idProveedor el identificador del proveedor
     * @return lista de productos del proveedor especificado
     */
    List<Producto> findByIdProveedor(Long idProveedor);

    /**
     * Busca productos por marca.
     *
     * @param marca la marca de los productos a buscar
     * @return lista de productos de la marca especificada
     */
    List<Producto> findByMarca(String marca);

    /**
     * Busca productos por género.
     *
     * @param genero el género de los productos a buscar
     * @return lista de productos del género especificado
     */
    List<Producto> findByGenero(String genero);

    /**
     * Busca productos por talla.
     *
     * @param talla la talla de los productos a buscar
     * @return lista de productos de la talla especificada
     */
    List<Producto> findByTalla(String talla);

    /**
     * Busca productos por color.
     *
     * @param color el color de los productos a buscar
     * @return lista de productos del color especificado
     */
    List<Producto> findByColor(String color);

    /**
     * Busca productos cuyo stock esté por debajo del stock mínimo.
     *
     * @return lista de productos con stock bajo
     */
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.estado = true")
    List<Producto> findProductosConStockBajo();

    /**
     * Busca productos agotados (stock = 0).
     *
     * @return lista de productos agotados
     */
    @Query("SELECT p FROM Producto p WHERE p.stock = 0 AND p.estado = true")
    List<Producto> findProductosAgotados();

    /**
     * Busca productos por nombre o descripción (búsqueda case-insensitive).
     *
     * @param texto el texto a buscar en nombre o descripción
     * @return lista de productos que coinciden con el criterio
     */
    @Query("SELECT p FROM Producto p WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) AND p.estado = true")
    List<Producto> findByNombreOrDescripcionContainingIgnoreCase(@Param("texto") String texto);

    /**
     * Busca productos con stock entre un rango específico.
     *
     * @param stockMinimo stock mínimo del rango
     * @param stockMaximo stock máximo del rango
     * @return lista de productos dentro del rango de stock
     */
    List<Producto> findByStockBetween(Integer stockMinimo, Integer stockMaximo);

    List<Producto> findByEstadoTrue();
    List<Producto> findByEstadoTrueAndStockLessThanEqual();
    Long countByEstadoTrue();
    Long countByEstadoTrueAndStockLessThanEqual(int stock);
    Long countByEstadoTrueAndStockLessThanEqual();

    /**
     * Busca productos con precio de venta dentro de un rango específico.
     *
     * @param precioMinimo precio mínimo del rango
     * @param precioMaximo precio máximo del rango
     * @return lista de productos dentro del rango de precios
     */
    List<Producto> findByPrecioVentaBetween(Double precioMinimo, Double precioMaximo);
}