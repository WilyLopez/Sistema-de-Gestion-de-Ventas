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
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private Long idProducto;
    private String codigo;
    private String nombre;
    private String marca;
    private String talla;
    private String color;
    private String material;
    private Genero genero;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private String descripcion;
    private String imagenUrl;
    private Boolean estado;
    private Long idCategoria;
    private String nombreCategoria;
    private Long idProveedor;
    private String razonSocialProveedor;
    private LocalDateTime fechaCreacion;
}