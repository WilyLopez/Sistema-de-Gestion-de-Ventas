package com.sgvi.sistema_ventas.util.excel;

import com.sgvi.sistema_ventas.model.entity.Venta;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exportador de ventas a Excel.
 * RF-014: Generación de Reportes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class VentaExcelExporter extends ExcelExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporta lista de ventas a Excel
     * @param ventas Lista de ventas
     * @return ByteArrayOutputStream con el archivo Excel
     */
    public ByteArrayOutputStream exportar(List<Venta> ventas) throws IOException {
        inicializarWorkbook("Reporte de Ventas");

        // Crear estilos
        CellStyle estiloEncabezado = crearEstiloEncabezado();
        CellStyle estiloDatos = crearEstiloDatos();
        CellStyle estiloNumero = crearEstiloNumero();

        // Crear encabezado
        crearEncabezado(estiloEncabezado);

        // Llenar datos
        llenarDatos(ventas, estiloDatos, estiloNumero);

        // Agregar fila de totales
        agregarFilaTotales(ventas, estiloEncabezado, estiloNumero);

        // Ajustar columnas
        ajustarAnchoColumnas(8);

        return convertirABytes();
    }

    private void crearEncabezado(CellStyle estilo) {
        Row headerRow = sheet.createRow(0);

        String[] columnas = {
                "Código", "Fecha", "Cliente", "Vendedor",
                "Subtotal", "IGV", "Total", "Estado"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }

    private void llenarDatos(List<Venta> ventas, CellStyle estiloDatos, CellStyle estiloNumero) {
        int rowNum = 1;

        for (Venta venta : ventas) {
            Row row = sheet.createRow(rowNum++);

            // Código
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(venta.getCodigoVenta());
            cell0.setCellStyle(estiloDatos);

            // Fecha
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(venta.getFechaVenta().format(FORMATTER));
            cell1.setCellStyle(estiloDatos);

            // Cliente
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido());
            cell2.setCellStyle(estiloDatos);

            // Vendedor
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido());
            cell3.setCellStyle(estiloDatos);

            // Subtotal
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(venta.getSubtotal().doubleValue());
            cell4.setCellStyle(estiloNumero);

            // IGV
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(venta.getIgv().doubleValue());
            cell5.setCellStyle(estiloNumero);

            // Total
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(venta.getTotal().doubleValue());
            cell6.setCellStyle(estiloNumero);

            // Estado
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(venta.getEstado().getDescripcion());
            cell7.setCellStyle(estiloDatos);
        }
    }

    private void agregarFilaTotales(List<Venta> ventas, CellStyle estiloEncabezado, CellStyle estiloNumero) {
        int rowNum = sheet.getLastRowNum() + 2; // Dejar una fila en blanco
        Row totalRow = sheet.createRow(rowNum);

        // Etiqueta "TOTAL"
        Cell labelCell = totalRow.createCell(3);
        labelCell.setCellValue("TOTAL:");
        labelCell.setCellStyle(estiloEncabezado);

        // Calcular totales
        double totalSubtotal = ventas.stream()
                .mapToDouble(v -> v.getSubtotal().doubleValue())
                .sum();

        double totalIgv = ventas.stream()
                .mapToDouble(v -> v.getIgv().doubleValue())
                .sum();

        double totalGeneral = ventas.stream()
                .mapToDouble(v -> v.getTotal().doubleValue())
                .sum();

        // Subtotal
        Cell cell4 = totalRow.createCell(4);
        cell4.setCellValue(totalSubtotal);
        cell4.setCellStyle(estiloNumero);

        // IGV
        Cell cell5 = totalRow.createCell(5);
        cell5.setCellValue(totalIgv);
        cell5.setCellStyle(estiloNumero);

        // Total
        Cell cell6 = totalRow.createCell(6);
        cell6.setCellValue(totalGeneral);
        cell6.setCellStyle(estiloNumero);
    }
}
