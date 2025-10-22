package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Producto en la base de datos.
 * <p>
 * Proporciona métodos para operaciones CRUD y consultas avanzadas de productos,
 * incluyendo búsquedas por múltiples criterios (código, categoría, precio, stock),
 * filtros para alertas de inventario y análisis de productos.
 * </p>
 * <p>
 * Este repositorio soporta los siguientes requisitos funcionales:
 * <ul>
 *   <li>RF-005: CRUD de Productos</li>
 *   <li>RF-011: Sistema de Alertas de Stock</li>
 *   <li>RF-012: Gestión de Inventario</li>
 *   <li>RF-014: Generación de Reportes</li>
 * </ul>
 * </p>
 *
 * @author Wilian Lopez
 * @version 1.1
 * @since 2024
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ==================== BÚSQUEDAS POR IDENTIFICADORES ====================

    /**
     * Busca un producto por su código único.
     * <p>
     * RF-005: El código debe ser único en el sistema.
     * </p>
     *
     * @param codigo el código único del producto (ej: "PRO-001", "CAMI-2024-001")
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Verifica si existe un producto con el código especificado.
     * <p>
     * Útil para validaciones antes de crear o actualizar productos.
     * </p>
     *
     * @param codigo el código del producto a verificar
     * @return true si existe un producto con ese código, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    // ==================== BÚSQUEDAS POR ESTADO Y CATEGORIZACIÓN ====================

    /**
     * Busca productos por estado activo/inactivo.
     * <p>
     * RF-005: Los productos inactivos no deben aparecer en ventas.
     * </p>
     *
     * @param estado true para productos activos, false para inactivos
     * @return lista de productos que coinciden con el estado especificado
     */
    List<Producto> findByEstado(Boolean estado);

    /**
     * Busca todos los productos activos del sistema.
     * <p>
     * Método de conveniencia equivalente a {@code findByEstado(true)}.
     * Usado frecuentemente en listados de productos disponibles para venta.
     * </p>
     *
     * @return lista de todos los productos activos
     */
    List<Producto> findByEstadoTrue();

    /**
     * Busca productos por categoría específica.
     * <p>
     * RF-005: Permite filtrar productos por categoría (Camisetas, Pantalones, etc.).
     * </p>
     *
     * @param idCategoria el identificador de la categoría
     * @return lista de productos pertenecientes a la categoría especificada
     */
    List<Producto> findByIdCategoria(Long idCategoria);

    /**
     * Busca productos por proveedor específico.
     * <p>
     * RF-006: Permite identificar todos los productos de un proveedor.
     * </p>
     *
     * @param idProveedor el identificador del proveedor
     * @return lista de productos suministrados por el proveedor especificado
     */
    List<Producto> findByIdProveedor(Long idProveedor);

    // ==================== BÚSQUEDAS POR ATRIBUTOS DE ROPA ====================

    /**
     * Busca productos por marca.
     * <p>
     * RF-005: Permite filtrar productos por marca específica.
     * </p>
     *
     * @param marca la marca de los productos a buscar (ej: "Nike", "Adidas")
     * @return lista de productos de la marca especificada
     */
    List<Producto> findByMarca(String marca);

    /**
     * Busca productos por género.
     * <p>
     * RF-005: Filtra productos según el género al que están dirigidos.
     * </p>
     *
     * @param genero el género de los productos a buscar (String del Enum Genero: "hombre", "mujer", "unisex", "niño", "niña")
     * @return lista de productos del género especificado
     */
    List<Producto> findByGenero(String genero);

    /**
     * Busca productos por talla específica.
     * <p>
     * RF-005: Permite filtrar por tallas (XS, S, M, L, XL, XXL).
     * </p>
     *
     * @param talla la talla de los productos a buscar
     * @return lista de productos de la talla especificada
     */
    List<Producto> findByTalla(String talla);

    /**
     * Busca productos por color específico.
     * <p>
     * RF-005: Permite filtrar por colores disponibles.
     * </p>
     *
     * @param color el color de los productos a buscar
     * @return lista de productos del color especificado
     */
    List<Producto> findByColor(String color);

    // ==================== BÚSQUEDAS POR RANGOS ====================

    /**
     * Busca productos con stock dentro de un rango específico.
     * <p>
     * Útil para análisis de inventario y reportes de stock.
     * </p>
     *
     * @param stockMinimo límite inferior del rango de stock (inclusivo)
     * @param stockMaximo límite superior del rango de stock (inclusivo)
     * @return lista de productos cuyo stock está dentro del rango especificado
     */
    List<Producto> findByStockBetween(Integer stockMinimo, Integer stockMaximo);

    /**
     * Busca productos con precio de venta dentro de un rango específico.
     * <p>
     * RF-005: Permite filtrar productos por rango de precios para búsquedas de clientes.
     * </p>
     *
     * @param precioMinimo límite inferior del rango de precio (inclusivo)
     * @param precioMaximo límite superior del rango de precio (inclusivo)
     * @return lista de productos cuyo precio de venta está dentro del rango especificado
     */
    List<Producto> findByPrecioVentaBetween(BigDecimal precioMinimo, BigDecimal precioMaximo);

    // ==================== CONSULTAS PERSONALIZADAS PARA ALERTAS Y STOCK ====================

    /**
     * RF-011: Busca productos con stock por debajo del nivel mínimo establecido.
     * <p>
     * Esta consulta identifica productos que requieren reabastecimiento urgente.
     * Solo considera productos activos para evitar alertas innecesarias.
     * </p>
     * <p>
     * <strong>Condición:</strong> {@code stock <= stockMinimo AND estado = true}
     * </p>
     *
     * @return lista de productos activos con stock bajo que requieren atención
     */
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.estado = true")
    List<Producto> findProductosConStockBajo();

    /**
     * RF-011: Busca productos completamente agotados (stock = 0).
     * <p>
     * Esta consulta identifica productos sin existencias que requieren
     * reabastecimiento crítico e inmediato. Solo considera productos activos.
     * </p>
     * <p>
     * <strong>Nivel de urgencia:</strong> CRÍTICO
     * </p>
     *
     * @return lista de productos activos sin stock disponible
     */
    @Query("SELECT p FROM Producto p WHERE p.stock = 0 AND p.estado = true")
    List<Producto> findProductosAgotados();

    /**
     * RF-005 y RF-011: Busca productos por nombre o descripción (búsqueda case-insensitive).
     * <p>
     * Búsqueda flexible que permite encontrar productos mediante texto parcial
     * en el nombre o descripción. Útil para funcionalidades de búsqueda y autocompletado.
     * Solo retorna productos activos.
     * </p>
     * <p>
     * <strong>Ejemplo:</strong> Buscar "camisa" encontrará "Camisa Polo", "Camisa Formal", etc.
     * </p>
     *
     * @param texto el texto a buscar en nombre o descripción (búsqueda parcial)
     * @return lista de productos activos que coinciden con el criterio de búsqueda
     */
    @Query("SELECT p FROM Producto p WHERE " +
            "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "AND p.estado = true")
    List<Producto> findByNombreOrDescripcionContainingIgnoreCase(@Param("texto") String texto);

    // ==================== MÉTODOS DE CONTEO ====================

    /**
     * RF-014: Cuenta el total de productos activos en el sistema.
     * <p>
     * Usado en reportes y dashboard para mostrar estadísticas generales de inventario.
     * </p>
     *
     * @return el número total de productos activos
     */
    Long countByEstadoTrue();

    /**
     * RF-011: Cuenta productos activos con stock menor o igual a un límite específico.
     * <p>
     * Útil para generar alertas y estadísticas de stock crítico.
     * Permite determinar cuántos productos están cerca de agotarse.
     * </p>
     * <p>
     * <strong>Uso común:</strong>
     * <ul>
     *   <li>{@code countByEstadoTrueAndStockLessThanEqual(2)} - Stock crítico</li>
     *   <li>{@code countByEstadoTrueAndStockLessThanEqual(5)} - Stock bajo</li>
     * </ul>
     * </p>
     *
     * @param stock el límite de stock para el conteo
     * @return el número de productos activos con stock menor o igual al límite especificado
     */
    Long countByEstadoTrueAndStockLessThanEqual(Integer stock);

    /**
     * RF-011: Cuenta productos activos con stock menor o igual a su stock mínimo establecido.
     * <p>
     * Este método es fundamental para el sistema de alertas automáticas.
     * Identifica productos que han alcanzado o superado su umbral de reabastecimiento.
     * </p>
     * <p>
     * <strong>Diferencia con el método anterior:</strong>
     * <ul>
     *   <li>Este método compara contra el {@code stockMinimo} de cada producto</li>
     *   <li>El método anterior usa un valor fijo para todos los productos</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Usado en:</strong>
     * <ul>
     *   <li>Dashboard para mostrar alertas pendientes</li>
     *   <li>Reportes de stock bajo</li>
     *   <li>Generación automática de alertas (RF-011)</li>
     * </ul>
     * </p>
     *
     * @return el número de productos activos que requieren reabastecimiento según su stock mínimo individual
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true AND p.stock <= p.stockMinimo")
    Long countByEstadoTrueAndStockLessThanEqual();


}