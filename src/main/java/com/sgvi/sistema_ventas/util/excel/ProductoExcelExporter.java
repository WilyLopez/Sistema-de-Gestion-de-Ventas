package com.sgvi.sistema_ventas.util.excel;

import com.sgvi.sistema_ventas.model.entity.Producto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Exportador de productos a Excel.
 * RF-014: Generación de Reportes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class ProductoExcelExporter extends ExcelExporter {

    /**
     * Exporta lista de productos a Excel
     */
    public ByteArrayOutputStream exportar(List<Producto> productos) throws IOException {
        inicializarWorkbook("Catálogo de Productos");

        CellStyle estiloEncabezado = crearEstiloEncabezado();
        CellStyle estiloDatos = crearEstiloDatos();
        CellStyle estiloNumero = crearEstiloNumero();

        crearEncabezado(estiloEncabezado);
        llenarDatos(productos, estiloDatos, estiloNumero);
        ajustarAnchoColumnas(12);

        return convertirABytes();
    }

    private void crearEncabezado(CellStyle estilo) {
        Row headerRow = sheet.createRow(0);

        String[] columnas = {
                "Código", "Nombre", "Marca", "Categoría", "Talla",
                "Color", "Género", "Stock", "Stock Mínimo",
                "Precio Compra", "Precio Venta", "Estado"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }

    private void llenarDatos(List<Producto> productos, CellStyle estiloDatos, CellStyle estiloNumero) {
        int rowNum = 1;

        for (Producto producto : productos) {
            Row row = sheet.createRow(rowNum++);

            crearCelda(row, 0, producto.getCodigo(), estiloDatos);
            crearCelda(row, 1, producto.getNombre(), estiloDatos);
            crearCelda(row, 2, producto.getMarca(), estiloDatos);
            crearCelda(row, 3, producto.getCategoria() != null ?
                    producto.getCategoria().getNombre() : "", estiloDatos);
            crearCelda(row, 4, producto.getTalla(), estiloDatos);
            crearCelda(row, 5, producto.getColor(), estiloDatos);
            crearCelda(row, 6, producto.getGenero() != null ?
                    producto.getGenero().getDescripcion() : "", estiloDatos);
            crearCelda(row, 7, producto.getStock(), estiloDatos);
            crearCelda(row, 8, producto.getStockMinimo(), estiloDatos);
            crearCelda(row, 9, producto.getPrecioCompra(), estiloNumero);
            crearCelda(row, 10, producto.getPrecioVenta(), estiloNumero);
            crearCelda(row, 11, producto.getEstado() ? "Activo" : "Inactivo", estiloDatos);
        }
    }

    private void crearCelda(Row row, int columna, Object valor, CellStyle estilo) {
        Cell cell = row.createCell(columna);

        if (valor == null) {
            cell.setCellValue("");
        } else if (valor instanceof String) {
            cell.setCellValue((String) valor);
        } else if (valor instanceof Integer) {
            cell.setCellValue((Integer) valor);
        } else if (valor instanceof java.math.BigDecimal) {
            cell.setCellValue(((java.math.BigDecimal) valor).doubleValue());
        }

        cell.setCellStyle(estilo);
    }
}
