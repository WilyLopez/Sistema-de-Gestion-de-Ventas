package com.sgvi.sistema_ventas.model.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgvi.sistema_ventas.model.enums.Genero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta completa de un Producto.
 * Incluye nombres de las entidades relacionadas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    /** Identificador único del producto. */
    private Long idProducto;

    /** Código de producto (SKU). */
    private String codigo;

    /** Nombre comercial. */
    private String nombre;

    /** Marca del producto. */
    private String marca;

    /** Talla. */
    private String talla;

    /** Color. */
    private String color;

    /** Material. */
    private String material;

    /** Género (Enum). */
    private Genero genero;

    /** Precio de compra. */
    private BigDecimal precioCompra;

    /** Precio de venta al público. */
    private BigDecimal precioVenta;

    /** Cantidad actual en inventario. */
    private Integer stock;

    /** Stock mínimo para alerta. */
    private Integer stockMinimo;

    /** Descripción extendida. */
    private String descripcion;

    /** URL de la imagen. */
    private String imagenUrl;

    /** Estado del producto (Activo/Inactivo). */
    private Boolean estado;

    /** ID de la categoría. */
    private Long idCategoria;

    /** Nombre de la categoría. */
    private String nombreCategoria;

    /** ID del proveedor. */
    private Long idProveedor;

    /** Razón social del proveedor. */
    private String razonSocialProveedor;

    /** Fecha de creación. */
    private LocalDateTime fechaCreacion;
}