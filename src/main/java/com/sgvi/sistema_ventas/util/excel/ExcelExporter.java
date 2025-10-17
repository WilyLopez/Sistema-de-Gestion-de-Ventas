package com.sgvi.sistema_ventas.util.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Clase base abstracta para exportadores de Excel.
 * Proporciona métodos comunes para crear archivos Excel.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public abstract class ExcelExporter {

    protected Workbook workbook;
    protected Sheet sheet;

    /**
     * Inicializa un nuevo workbook
     */
    protected void inicializarWorkbook(String nombreHoja) {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet(nombreHoja);
    }

    /**
     * Crea estilo para encabezados
     */
    protected CellStyle crearEstiloEncabezado() {
        CellStyle style = workbook.createCellStyle();

        // Fondo azul
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Fuente blanca y negrita
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // Bordes
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alineación
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Crea estilo para datos
     */
    protected CellStyle crearEstiloDatos() {
        CellStyle style = workbook.createCellStyle();

        // Bordes
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Crea estilo para números
     */
    protected CellStyle crearEstiloNumero() {
        CellStyle style = crearEstiloDatos();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    /**
     * Crea estilo para fechas
     */
    protected CellStyle crearEstiloFecha() {
        CellStyle style = crearEstiloDatos();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }

    /**
     * Ajusta automáticamente el ancho de las columnas
     */
    protected void ajustarAnchoColumnas(int numeroColumnas) {
        for (int i = 0; i < numeroColumnas; i++) {
            sheet.autoSizeColumn(i);
            // Agregar un poco de padding extra
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 1000);
        }
    }

    /**
     * Convierte el workbook a ByteArrayOutputStream
     */
    protected ByteArrayOutputStream convertirABytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream;
    }
}
