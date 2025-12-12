package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.dto.producto.ProductoDTO;
import com.sgvi.sistema_ventas.model.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    @Query("""
            SELECT new com.sgvi.sistema_ventas.model.dto.producto.ProductoDTO(
                p.idProducto, p.codigo, p.nombre, p.marca, p.talla, p.color,
                p.material, p.genero, p.precioCompra, p.precioVenta, p.stock,
                p.stockMinimo, p.descripcion, p.imagenUrl, p.estado, c.idCategoria,
                c.nombre, pv.idProveedor, pv.razonSocial, p.fechaCreacion
            )
            FROM Producto p
            LEFT JOIN Categoria c ON p.idCategoria = c.idCategoria
            LEFT JOIN Proveedor pv ON p.idProveedor = pv.idProveedor
            """)
    Page<ProductoDTO> findAllProjectedBy(Pageable pageable);
    
    // Búsquedas por identificadores
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    // Búsquedas por estado y categorización
    List<Producto> findByEstado(Boolean estado);
    List<Producto> findByEstadoTrue();
    List<Producto> findByIdCategoria(Long idCategoria);
    List<Producto> findByIdProveedor(Long idProveedor);

    // Búsquedas por atributos de ropa
    List<Producto> findByMarca(String marca);
    List<Producto> findByGenero(String genero);
    List<Producto> findByTalla(String talla);
    List<Producto> findByColor(String color);

    // Búsquedas por rangos
    List<Producto> findByStockBetween(Integer stockMinimo, Integer stockMaximo);
    List<Producto> findByPrecioVentaBetween(BigDecimal precioMinimo, BigDecimal precioMaximo);

    // Consultas personalizadas para alertas y stock
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.estado = true")
    List<Producto> findProductosConStockBajo();

    @Query("SELECT p FROM Producto p WHERE p.stock = 0 AND p.estado = true")
    List<Producto> findProductosAgotados();

    @Query("SELECT p FROM Producto p WHERE " +
            "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "AND p.estado = true")
    List<Producto> findByNombreOrDescripcionContainingIgnoreCase(@Param("texto") String texto);

    // Métodos de conteo
    Long countByEstadoTrue();
    Long countByEstadoTrueAndStockLessThanEqual(Integer stock);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true AND p.stock <= p.stockMinimo")
    Long countByEstadoTrueAndStockLessThanEqual();

    // NUEVOS MÉTODOS PARA DASHBOARD
    Long countByEstadoTrueAndStockEquals(Integer stock);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true AND p.stock = 0")
    Long countProductosAgotados();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true AND p.stock > 0")
    Long countProductosConStockDisponible();
}