package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.Genero;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa la tabla Producto.
 * Almacena los detalles de los artículos de ropa disponibles para la venta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "producto")
public class Producto {

    /** Identificador único del producto (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Integer idProducto;

    /** Código de producto único (e.g., SKU). */
    @Column(name = "codigo", nullable = false, length = 50, unique = true)
    private String codigo;

    /** Nombre comercial del producto. */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Marca del producto. */
    @Column(name = "marca", length = 50)
    private String marca;

    /** Talla o tamaño del producto. */
    @Column(name = "talla", length = 10)
    private String talla;

    /** Color del producto. */
    @Column(name = "color", length = 30)
    private String color;

    /** Material de fabricación. */
    @Column(name = "material", length = 50)
    private String material;

    /**
     * Género al que está dirigido el producto.
     * Mapeado como String en la BD, usando el Enum Genero.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 20)
    private Genero genero;

    /** Precio al que se compra el producto al proveedor. */
    @Column(name = "preciocompra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    /** Precio al que se vende el producto al cliente (Obligatorio). */
    @Column(name = "precioventa", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    /** Cantidad de producto en inventario (Stock actual). */
    @Column(name = "stock", nullable = false)
    private Integer stock;

    /** Cantidad mínima para disparar alertas de reabastecimiento. */
    @Column(name = "stockminimo")
    private Integer stockMinimo = 5;

    /** Descripción extendida del producto. */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /** URL de la imagen del producto. */
    @Column(name = "imagenurl", length = 255)
    private String imagenUrl;

    /** Estado del producto (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;

    /** Clave foránea a Categoria (Obligatoria). */
    @Column(name = "idcategoria", nullable = false)
    private Integer idCategoria;

    /** Clave foránea a Proveedor (Opcional). */
    @Column(name = "idproveedor")
    private Integer idProveedor;

    /** Fecha de creación del registro. */
    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha de la última actualización del registro. */
    @Column(name = "fechaactualizacion")
    private LocalDateTime fechaActualizacion;

    // --- Relaciones ---

    /** Relación ManyToOne con Categoria. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoria", insertable = false, updatable = false)
    private Categoria categoria;

    /** Relación ManyToOne con Proveedor. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproveedor", insertable = false, updatable = false)
    private Proveedor proveedor;
}