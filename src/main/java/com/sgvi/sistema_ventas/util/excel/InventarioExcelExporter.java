package com.sgvi.sistema_ventas.util.excel;

import com.sgvi.sistema_ventas.model.entity.Inventario;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exportador de movimientos de inventario a Excel.
 * RF-014: Reporte de Movimientos
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class InventarioExcelExporter extends ExcelExporter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ByteArrayOutputStream exportar(List<Inventario> movimientos) throws IOException {
        inicializarWorkbook("Movimientos de Inventario");

        CellStyle estiloEncabezado = crearEstiloEncabezado();
        CellStyle estiloDatos = crearEstiloDatos();
        CellStyle estiloNumero = crearEstiloNumero();

        crearEncabezado(estiloEncabezado);
        llenarDatos(movimientos, estiloDatos, estiloNumero);
        ajustarAnchoColumnas(9);

        return convertirABytes();
    }

    private void crearEncabezado(CellStyle estilo) {
        Row headerRow = sheet.createRow(0);

        String[] columnas = {
                "Fecha", "Producto", "Tipo Movimiento", "Cantidad",
                "Stock Anterior", "Stock Nuevo", "Usuario",
                "ID Venta", "Observación"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }

    private void llenarDatos(List<Inventario> movimientos,
                             CellStyle estiloDatos, CellStyle estiloNumero) {
        int rowNum = 1;

        for (Inventario mov : movimientos) {
            Row row = sheet.createRow(rowNum++);

            crearCelda(row, 0, mov.getFechaMovimiento().format(FORMATTER), estiloDatos);
            crearCelda(row, 1, mov.getProducto().getNombre(), estiloDatos);
            crearCelda(row, 2, mov.getTipoMovimiento().getDescripcion(), estiloDatos);
            crearCelda(row, 3, mov.getCantidad(), estiloNumero);
            crearCelda(row, 4, mov.getStockAnterior(), estiloNumero);
            crearCelda(row, 5, mov.getStockNuevo(), estiloNumero);
            crearCelda(row, 6, mov.getUsuario().getUsername(), estiloDatos);
            crearCelda(row, 7, mov.getVenta() != null ?
                    mov.getVenta().getCodigoVenta() : "", estiloDatos);
            crearCelda(row, 8, mov.getObservacion(), estiloDatos);
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
        }

        cell.setCellStyle(estilo);
    }
}
