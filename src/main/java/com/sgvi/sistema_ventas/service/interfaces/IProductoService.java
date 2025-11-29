package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.enums.Genero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de productos.
 * Define los contratos según RF-005: CRUD de Productos
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IProductoService {

    /**
     * RF-005: Crear un nuevo producto
     * @param producto Producto a crear
     * @return Producto creado
     */
    Producto crear(Producto producto);

    /**
     * RF-005: Actualizar producto existente
     * @param id ID del producto
     * @param producto Datos actualizados
     * @return Producto actualizado
     */
    Producto actualizar(Long id, Producto producto);

    /**
     * RF-005: Eliminar producto (soft delete)
     * @param id ID del producto
     */
    void eliminar(Long id);

    /**
     * RF-005: Obtener producto por ID
     * @param id ID del producto
     * @return Producto encontrado
     */
    Producto obtenerPorId(Long id);

    /**
     * RF-005: Obtener producto por código
     * @param codigo Código del producto
     * @return Producto encontrado
     */
    Producto obtenerPorCodigo(String codigo);

    /**
     * RF-005: Listar todos los productos
     * @param pageable Parámetros de paginación
     * @return Página de productos
     */
    Page<Producto> listarTodos(Pageable pageable);

    /**
     * RF-005: Buscar productos por múltiples criterios.
     * @param texto Texto a buscar en nombre, código o descripción.
     * @param idCategoria ID de la categoría para filtrar.
     * @param precioMin Precio mínimo para filtrar.
     * @param precioMax Precio máximo para filtrar.
     * @param pageable Parámetros de paginación.
     * @return Página de productos que coinciden con los criterios.
     */
    Page<Producto> buscar(String texto, Long idCategoria, BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);

    /**
     * RF-005: Filtrar productos por categoría
     * @param idCategoria ID de la categoría
     * @param pageable Parámetros de paginación
     * @return Página de productos
     */
    Page<Producto> filtrarPorCategoria(Long idCategoria, Pageable pageable);

    /**
     * RF-005: Filtrar productos por género
     * @param genero Género del producto
     * @param pageable Parámetros de paginación
     * @return Página de productos
     */
    Page<Producto> filtrarPorGenero(Genero genero, Pageable pageable);

    /**
     * RF-005: Filtrar productos por marca
     * @param marca Marca del producto
     * @param pageable Parámetros de paginación
     * @return Página de productos
     */
    Page<Producto> filtrarPorMarca(String marca, Pageable pageable);

    /**
     * RF-005: Filtrar productos por rango de precio
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param pageable Parámetros de paginación
     * @return Página de productos
     */
    Page<Producto> filtrarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);

    /**
     * RF-011: Obtener productos con stock bajo
     * @return Lista de productos con stock <= stock mínimo
     */
    List<Producto> obtenerProductosConStockBajo();

    /**
     * RF-011: Obtener productos agotados
     * @return Lista de productos con stock = 0
     */
    List<Producto> obtenerProductosAgotados();

    /**
     * RF-012: Actualizar stock de un producto
     * @param idProducto ID del producto
     * @param cantidad Cantidad a sumar (positiva) o restar (negativa)
     * @return Producto con stock actualizado
     */
    Producto actualizarStock(Long idProducto, Integer cantidad);

    /**
     * RF-012: Verificar disponibilidad de stock
     * @param idProducto ID del producto
     * @param cantidadRequerida Cantidad solicitada
     * @return true si hay stock suficiente
     */
    boolean verificarStock(Long idProducto, Integer cantidadRequerida);

    /**
     * Calcular margen de ganancia del producto
     * @param idProducto ID del producto
     * @return Margen de ganancia en porcentaje
     */
    BigDecimal calcularMargenGanancia(Long idProducto);

    /**
     * Verificar si código de producto ya existe
     * @param codigo Código a verificar
     * @return true si existe
     */
    boolean existeCodigo(String codigo);
}
