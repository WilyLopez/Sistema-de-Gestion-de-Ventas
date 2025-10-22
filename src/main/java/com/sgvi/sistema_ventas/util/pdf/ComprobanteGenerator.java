package com.sgvi.sistema_ventas.util.pdf;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.util.validation.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Generador de comprobantes de venta en PDF.
 * RF-014: Generación de comprobantes (Boleta/Factura)
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
@RequiredArgsConstructor
public class ComprobanteGenerator extends PdfGenerator {

    private final NumberUtil numberUtil;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Venta venta;

    /**
     * Genera comprobante PDF para una venta
     */
    public ByteArrayOutputStream generarComprobante(Venta venta) throws IOException {
        this.venta = venta;
        return generar();
    }

    @Override
    public ByteArrayOutputStream generar() throws IOException {
        inicializarDocumento();

        // Encabezado de empresa
        crearEncabezadoEmpresa();

        // Información del comprobante
        crearInfoComprobante();

        // Datos del cliente
        crearDatosCliente();

        // Detalle de productos
        crearDetalleProductos();

        // Totales
        crearTotales();

        // Pie de página
        crearPiePagina();

        return finalizarDocumento();
    }

    private void crearEncabezadoEmpresa() {
        // Logo y datos de empresa
        Paragraph empresa = new Paragraph("SGVIA - Sistema de Ventas")
                .setFontSize(16)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(empresa);

        Paragraph ruc = new Paragraph("RUC: 20123456789")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(ruc);

        Paragraph direccion = new Paragraph("Av. Principal 123, Lima - Perú")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(direccion);

        // Línea separadora
        document.add(new Paragraph("\n"));
    }

    private void crearInfoComprobante() {
        // Tipo de comprobante
        String tipoComprobante = venta.getTipoComprobante() != null ?
                venta.getTipoComprobante().getDescripcion() : "COMPROBANTE";

        Paragraph tipo = new Paragraph(tipoComprobante.toUpperCase())
                .setFontSize(14)
                .setBold()
                .setFontColor(COLOR_SECUNDARIO)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(tipo);

        Paragraph codigo = new Paragraph(venta.getCodigoVenta())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(codigo);

        // Fecha y vendedor
        Table infoTable = crearTabla(new float[]{50, 50});

        infoTable.addCell(crearCeldaSinBorde("Fecha: " +
                venta.getFechaCreacion().format(DATE_FORMATTER), false));
        infoTable.addCell(crearCeldaSinBorde("Vendedor: " +
                venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido(), false));

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void crearDatosCliente() {
        Paragraph titulo = new Paragraph("DATOS DEL CLIENTE")
                .setFontSize(11)
                .setBold()
                .setFontColor(COLOR_SECUNDARIO)
                .setMarginBottom(5);
        document.add(titulo);

        Table clienteTable = crearTabla(new float[]{30, 70});

        clienteTable.addCell(crearCeldaSinBorde("Nombre:", true));
        clienteTable.addCell(crearCeldaSinBorde(
                venta.getCliente().getNombre() + " " + venta.getCliente().getApellido(), false));

        clienteTable.addCell(crearCeldaSinBorde("Documento:", true));
        clienteTable.addCell(crearCeldaSinBorde(
                venta.getCliente().getTipoDocumento().getCodigo() + ": " +
                        venta.getCliente().getNumeroDocumento(), false));

        if (venta.getCliente().getDireccion() != null) {
            clienteTable.addCell(crearCeldaSinBorde("Dirección:", true));
            clienteTable.addCell(crearCeldaSinBorde(venta.getCliente().getDireccion(), false));
        }

        document.add(clienteTable);
        document.add(new Paragraph("\n"));
    }

    private void crearDetalleProductos() {
        Paragraph titulo = new Paragraph("DETALLE DE PRODUCTOS")
                .setFontSize(11)
                .setBold()
                .setFontColor(COLOR_SECUNDARIO)
                .setMarginBottom(5);
        document.add(titulo);

        // Tabla de productos
        Table productoTable = crearTabla(new float[]{10, 40, 15, 15, 20});

        // Encabezados
        Color colorEncabezado = new DeviceRgb(52, 73, 94);
        productoTable.addHeaderCell(crearCeldaEncabezado("Cant.", colorEncabezado));
        productoTable.addHeaderCell(crearCeldaEncabezado("Producto", colorEncabezado));
        productoTable.addHeaderCell(crearCeldaEncabezado("P. Unit.", colorEncabezado));
        productoTable.addHeaderCell(crearCeldaEncabezado("Desc.", colorEncabezado));
        productoTable.addHeaderCell(crearCeldaEncabezado("Subtotal", colorEncabezado));

        // Detalles
        for (DetalleVenta detalle : venta.getDetallesVenta()) {
            productoTable.addCell(crearCelda(detalle.getCantidad().toString(), false));
            productoTable.addCell(crearCelda(detalle.getProducto().getNombre(), false));
            productoTable.addCell(crearCelda(
                    numberUtil.formatearMoneda(detalle.getPrecioUnitario()), false));
            productoTable.addCell(crearCelda(
                    numberUtil.formatearMoneda(detalle.getDescuento()), false));
            productoTable.addCell(crearCelda(
                    numberUtil.formatearMoneda(detalle.getSubtotal()), false));
        }

        document.add(productoTable);
        document.add(new Paragraph("\n"));
    }

    private void crearTotales() {
        Table totalesTable = crearTabla(new float[]{70, 30});

        // Subtotal
        totalesTable.addCell(crearCeldaDerecha("Subtotal:", true));
        totalesTable.addCell(crearCeldaDerecha(
                numberUtil.formatearMoneda(venta.getSubtotal()), false));

        // IGV
        totalesTable.addCell(crearCeldaDerecha("IGV (18%):", true));
        totalesTable.addCell(crearCeldaDerecha(
                numberUtil.formatearMoneda(venta.getIgv()), false));

        // Total
        Cell totalLabel = new Cell()
                .add(new Paragraph("TOTAL:").setBold().setFontSize(12))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(236, 240, 241));

        Cell totalValor = new Cell()
                .add(new Paragraph(numberUtil.formatearMoneda(venta.getTotal()))
                        .setBold()
                        .setFontSize(12)
                        .setFontColor(COLOR_PRIMARIO))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(236, 240, 241));

        totalesTable.addCell(totalLabel);
        totalesTable.addCell(totalValor);

        document.add(totalesTable);
        document.add(new Paragraph("\n"));
    }

    private void crearPiePagina() {
        // Método de pago
        if (venta.getMetodoPago() != null) {
            Paragraph metodoPago = new Paragraph("Método de Pago: " +
                    venta.getMetodoPago().getNombre())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(metodoPago);
        }

        // Observaciones
        if (venta.getObservaciones() != null && !venta.getObservaciones().isEmpty()) {
            Paragraph observaciones = new Paragraph("Observaciones: " +
                    venta.getObservaciones())
                    .setFontSize(9)
                    .setFontColor(COLOR_GRIS)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(observaciones);
        }

        // Mensaje de agradecimiento
        document.add(new Paragraph("\n"));
        Paragraph gracias = new Paragraph("¡Gracias por su compra!")
                .setFontSize(10)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(gracias);

        Paragraph sistema = new Paragraph("Generado por SGVIA - Sistema de Gestión de Ventas")
                .setFontSize(8)
                .setFontColor(COLOR_GRIS)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(sistema);
    }

    // Métodos auxiliares para celdas
    private Cell crearCeldaSinBorde(String texto, boolean bold) {
        Paragraph p = new Paragraph(texto).setFontSize(10);
        if (bold) p.setBold();
        return new Cell().add(p).setBorder(Border.NO_BORDER);
    }

    private Cell crearCeldaDerecha(String texto, boolean bold) {
        Paragraph p = new Paragraph(texto).setFontSize(10);
        if (bold) p.setBold();
        return new Cell()
                .add(p)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER);
    }

    private Cell crearCelda(String texto, boolean bold) {
        Paragraph p = new Paragraph(texto).setFontSize(9);
        if (bold) p.setBold();
        return new Cell()
                .add(p)
                .setBorder(new SolidBorder(COLOR_GRIS, 0.5f));
    }

    private Cell crearCeldaEncabezado(String texto, Color color) {
        return new Cell()
                .add(new Paragraph(texto)
                        .setFontSize(10)
                        .setBold()
                        .setFontColor(DeviceRgb.WHITE))
                .setBackgroundColor(color)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER);
    }
}
