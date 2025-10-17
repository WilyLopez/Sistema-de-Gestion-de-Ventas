package com.sgvi.sistema_ventas.util.excel;

import com.sgvi.sistema_ventas.model.entity.Devolucion;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exportador de devoluciones a Excel.
 * RF-014: Reporte de Devoluciones
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class DevolucionExcelExporter extends ExcelExporter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ByteArrayOutputStream exportar(List<Devolucion> devoluciones) throws IOException {
        inicializarWorkbook("Reporte de Devoluciones");

        CellStyle estiloEncabezado = crearEstiloEncabezado();
        CellStyle estiloDatos = crearEstiloDatos();
        CellStyle estiloNumero = crearEstiloNumero();

        crearEncabezado(estiloEncabezado);
        llenarDatos(devoluciones, estiloDatos, estiloNumero);
        ajustarAnchoColumnas(8);

        return convertirABytes();
    }

    private void crearEncabezado(CellStyle estilo) {
        Row headerRow = sheet.createRow(0);

        String[] columnas = {
                "Fecha", "Código Venta", "Cliente", "Motivo",
                "Monto", "Estado", "Usuario", "Observaciones"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }

    private void llenarDatos(List<Devolucion> devoluciones,
                             CellStyle estiloDatos, CellStyle estiloNumero) {
        int rowNum = 1;

        for (Devolucion dev : devoluciones) {
            Row row = sheet.createRow(rowNum++);

            crearCelda(row, 0, dev.getFechaDevolucion().format(FORMATTER), estiloDatos);
            crearCelda(row, 1, dev.getVenta().getCodigoVenta(), estiloDatos);
            crearCelda(row, 2, dev.getVenta().getCliente().getNombre() + " " +
                    dev.getVenta().getCliente().getApellido(), estiloDatos);
            crearCelda(row, 3, dev.getMotivo(), estiloDatos);
            crearCelda(row, 4, dev.getMontoDevolucion(), estiloNumero);
            crearCelda(row, 5, dev.getEstado().getDescripcion(), estiloDatos);
            crearCelda(row, 6, dev.getUsuario().getUsername(), estiloDatos);
            crearCelda(row, 7, "", estiloDatos);
        }
    }

    private void crearCelda(Row row, int columna, Object valor, CellStyle estilo) {
        Cell cell = row.createCell(columna);

        if (valor == null) {
            cell.setCellValue("");
        } else if (valor instanceof String) {
            cell.setCellValue((String) valor);
        } else if (valor instanceof java.math.BigDecimal) {
            cell.setCellValue(((java.math.BigDecimal) valor).doubleValue());
        }

        cell.setCellStyle(estilo);
    }
}
