package com.sgvi.sistema_ventas.util.excel;

import com.sgvi.sistema_ventas.model.entity.Producto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Exportador de reporte de stock a Excel.
 * RF-014: Reporte de Stock
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class StockExcelExporter extends ExcelExporter {

    /**
     * Exporta reporte de stock bajo o general
     */
    public ByteArrayOutputStream exportar(List<Producto> productos, boolean soloStockBajo)
            throws IOException {

        String nombreHoja = soloStockBajo ? "Productos Stock Bajo" : "Reporte de Stock";
        inicializarWorkbook(nombreHoja);

        CellStyle estiloEncabezado = crearEstiloEncabezado();
        CellStyle estiloDatos = crearEstiloDatos();
        CellStyle estiloNumero = crearEstiloNumero();
        CellStyle estiloAlerta = crearEstiloAlerta();

        crearEncabezado(estiloEncabezado);
        llenarDatos(productos, estiloDatos, estiloNumero, estiloAlerta);
        ajustarAnchoColumnas(7);

        return convertirABytes();
    }

    private void crearEncabezado(CellStyle estilo) {
        Row headerRow = sheet.createRow(0);

        String[] columnas = {
                "Código", "Producto", "Categoría", "Stock Actual",
                "Stock Mínimo", "Estado", "Nivel"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }

    private void llenarDatos(List<Producto> productos, CellStyle estiloDatos,
                             CellStyle estiloNumero, CellStyle estiloAlerta) {
        int rowNum = 1;

        for (Producto producto : productos) {
            Row row = sheet.createRow(rowNum++);

            crearCelda(row, 0, producto.getCodigo(), estiloDatos);
            crearCelda(row, 1, producto.getNombre(), estiloDatos);
            crearCelda(row, 2, producto.getCategoria().getNombre(), estiloDatos);

            // Stock actual con color según nivel
            Cell cellStock = row.createCell(3);
            cellStock.setCellValue(producto.getStock());
            cellStock.setCellStyle(producto.getStock() == 0 ? estiloAlerta : estiloNumero);

            crearCelda(row, 4, producto.getStockMinimo(), estiloNumero);
            crearCelda(row, 5, producto.getEstado() ? "Activo" : "Inactivo", estiloDatos);

            // Nivel de stock
            String nivel = determinarNivelStock(producto);
            Cell cellNivel = row.createCell(6);
            cellNivel.setCellValue(nivel);
            cellNivel.setCellStyle(nivel.equals("CRÍTICO") ? estiloAlerta : estiloDatos);
        }
    }

    private CellStyle crearEstiloAlerta() {
        CellStyle style = workbook.createCellStyle();

        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private String determinarNivelStock(Producto producto) {
        int stock = producto.getStock();
        int minimo = producto.getStockMinimo();

        if (stock == 0) return "AGOTADO";
        if (stock <= 2) return "CRÍTICO";
        if (stock <= minimo) return "BAJO";
        if (stock > minimo * 3) return "EXCESIVO";
        return "NORMAL";
    }

    private void crearCelda(Row row, int columna, Object valor, CellStyle estilo) {
        Cell cell = row.createCell(columna);

        if (valor == null) {
            cell.setCellValue("");
        } else if (valor instanceof String) {
            cell.setCellValue((String) valor);
        } else if (valor instanceof Integer) {
            cell.setCellValue((Integer) valor);
        }

        cell.setCellStyle(estilo);
    }
}
