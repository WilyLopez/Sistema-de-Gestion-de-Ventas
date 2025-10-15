package com.sgvi.sistema_ventas.model.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgvi.sistema_ventas.model.enums.Genero;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

/**
 * DTO para la creación de un nuevo Producto.
 * Incluye validaciones básicas de campos obligatorios y rangos numéricos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCreateDTO {

    /** Código de producto único (SKU). */
    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 50, message = "El código no puede exceder los 50 caracteres.")
    private String codigo;

    /** Nombre comercial. */
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    /** Marca (Opcional). */
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres.")
    private String marca;

    /** Género. */
    @NotNull(message = "El género es obligatorio.")
    private Genero genero; // Se espera el enum (HOMBRE, MUJER, etc.)

    /** Precio de compra. */
    @NotNull(message = "El precio de compra es obligatorio.")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio de compra no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio de compra inválido (máx 8 enteros, 2 decimales).")
    private BigDecimal precioCompra;

    /** Precio de venta al público (Obligatorio). */
    @NotNull(message = "El precio de venta es obligatorio.")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio de venta no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio de venta inválido (máx 8 enteros, 2 decimales).")
    private BigDecimal precioVenta;

    /** Stock inicial (Obligatorio). */
    @NotNull(message = "El stock inicial es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    /** ID de la categoría (Obligatorio). */
    @NotNull(message = "La categoría es obligatoria.")
    @Positive(message = "El ID de la categoría debe ser positivo.")
    private Long idCategoria;

    /** ID del proveedor (Opcional). */
    @Positive(message = "El ID del proveedor debe ser positivo.")
    private Long idProveedor;

    // Campos opcionales sin validación de NotBlank, solo Size
    @Size(max = 10, message = "La talla no puede exceder los 10 caracteres.")
    private String talla;
    @Size(max = 30, message = "El color no puede exceder los 30 caracteres.")
    private String color;
    @Size(max = 50, message = "El material no puede exceder los 50 caracteres.")
    private String material;
    private String descripcion; // TEXT
    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.")
    private String imagenUrl;
}