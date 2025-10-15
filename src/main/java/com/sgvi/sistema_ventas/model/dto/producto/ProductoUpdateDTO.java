package com.sgvi.sistema_ventas.model.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgvi.sistema_ventas.model.enums.Genero;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

/**
 * DTO para la actualización de un Producto existente.
 * Permite la actualización parcial de atributos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoUpdateDTO {

    /** Nombre comercial. */
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    /** Marca. */
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres.")
    private String marca;

    /** Talla. */
    @Size(max = 10, message = "La talla no puede exceder los 10 caracteres.")
    private String talla;

    /** Color. */
    @Size(max = 30, message = "El color no puede exceder los 30 caracteres.")
    private String color;

    /** Material. */
    @Size(max = 50, message = "El material no puede exceder los 50 caracteres.")
    private String material;

    /** Género. */
    private Genero genero;

    /** Precio de compra. */
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio de compra no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio de compra inválido (máx 8 enteros, 2 decimales).")
    private BigDecimal precioCompra;

    /** Precio de venta. */
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio de venta no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio de venta inválido (máx 8 enteros, 2 decimales).")
    private BigDecimal precioVenta;

    /** Stock mínimo para alerta. */
    @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
    private Integer stockMinimo;

    /** Descripción. */
    private String descripcion;

    /** URL de la imagen. */
    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.")
    private String imagenUrl;

    /** Estado del producto (Activo/Inactivo). */
    private Boolean estado;

    /** ID de la categoría. */
    @Positive(message = "El ID de la categoría debe ser positivo.")
    private Long idCategoria;

    /** ID del proveedor. */
    @Positive(message = "El ID del proveedor debe ser positivo.")
    private Long idProveedor;
}