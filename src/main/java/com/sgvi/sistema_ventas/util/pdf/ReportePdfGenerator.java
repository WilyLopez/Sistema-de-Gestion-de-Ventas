package com.sgvi.sistema_ventas.util.pdf;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.util.validation.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador de reportes en PDF.
 * RF-014: Generación de Reportes en PDF
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
@RequiredArgsConstructor
public class ReportePdfGenerator extends PdfGenerator {

    private final NumberUtil numberUtil;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera reporte de ventas en PDF
     */
    public ByteArrayOutputStream generarReporteVentas(
            List<Venta> ventas,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) throws IOException {

        inicializarDocumento();

        // Encabezado
        crearEncabezado("REPORTE DE VENTAS",
                "Período: " + fechaInicio.format(DATE_FORMATTER) +
                        " al " + fechaFin.format(DATE_FORMATTER));

        // Resumen
        crearResumenVentas(ventas);

        // Tabla de ventas
        crearTablaVentas(ventas);

        return finalizarDocumento();
    }

    private void crearResumenVentas(List<Venta> ventas) {
        int totalVentas = ventas.size();
        BigDecimal totalMonto = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioVenta = totalVentas > 0 ?
                totalMonto.divide(BigDecimal.valueOf(totalVentas), 2,
                        java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Paragraph titulo = new Paragraph("RESUMEN EJECUTIVO")
                .setFontSize(12)
                .setBold()
                .setFontColor(COLOR_SECUNDARIO)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(titulo);

        Table resumenTable = crearTabla(new float[]{40, 60});

        resumenTable.addCell(crearCeldaResumen("Total de Ventas:", true));
        resumenTable.addCell(crearCeldaResumen(String.valueOf(totalVentas), false));

        resumenTable.addCell(crearCeldaResumen("Monto Total:", true));
        resumenTable.addCell(crearCeldaResumen(numberUtil.formatearMoneda(totalMonto), false));

        resumenTable.addCell(crearCeldaResumen("Promedio por Venta:", true));
        resumenTable.addCell(crearCeldaResumen(numberUtil.formatearMoneda(promedioVenta), false));

        document.add(resumenTable);
        document.add(new Paragraph("\n"));
    }

    private void crearTablaVentas(List<Venta> ventas) {
        Paragraph titulo = new Paragraph("DETALLE DE VENTAS")
                .setFontSize(12)
                .setBold()
                .setFontColor(COLOR_SECUNDARIO)
                .setMarginBottom(5);
        document.add(titulo);

        Table ventasTable = crearTabla(new float[]{15, 20, 30, 15, 20});

        // Encabezados
        ventasTable.addHeaderCell(crearCeldaEncabezado("Código"));
        ventasTable.addHeaderCell(crearCeldaEncabezado("Fecha"));
        ventasTable.addHeaderCell(crearCeldaEncabezado("Cliente"));
        ventasTable.addHeaderCell(crearCeldaEncabezado("Estado"));
        ventasTable.addHeaderCell(crearCeldaEncabezado("Total"));

        // Datos
        for (Venta venta : ventas) {
            ventasTable.addCell(crearCeldaTabla(venta.getCodigoVenta()));
            ventasTable.addCell(crearCeldaTabla(
                    venta.getFechaCreacion().format(DATE_FORMATTER)));
            ventasTable.addCell(crearCeldaTabla(
                    venta.getCliente().getNombre() + " " + venta.getCliente().getApellido()));
            ventasTable.addCell(crearCeldaTabla(venta.getEstado().getDescripcion()));
            ventasTable.addCell(crearCeldaTabla(numberUtil.formatearMoneda(venta.getTotal())));
        }

        document.add(ventasTable);
    }

    private Cell crearCeldaResumen(String texto, boolean bold) {
        Paragraph p = new Paragraph(texto).setFontSize(10);
        if (bold) p.setBold();

        return new Cell()
                .add(p)
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(236, 240, 241))
                .setPadding(5);
    }

    private Cell crearCeldaEncabezado(String texto) {
        return new Cell()
                .add(new Paragraph(texto)
                        .setFontSize(10)
                        .setBold()
                        .setFontColor(DeviceRgb.WHITE))
                .setBackgroundColor(COLOR_SECUNDARIO)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Cell crearCeldaTabla(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9))
                .setBorder(new SolidBorder(COLOR_GRIS, 0.5f))
                .setPadding(3);
    }

    @Override
    public ByteArrayOutputStream generar() throws IOException {
        // Implementación por defecto vacía
        // Se usa el método específico generarReporteVentas
        return new ByteArrayOutputStream();
    }
}
