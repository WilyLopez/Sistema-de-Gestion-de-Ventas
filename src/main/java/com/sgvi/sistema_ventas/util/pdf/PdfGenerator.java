package com.sgvi.sistema_ventas.util.pdf;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Clase base abstracta para generadores de PDF.
 * Utiliza iText 7 para crear documentos PDF.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public abstract class PdfGenerator {

    protected PdfDocument pdfDocument;
    protected Document document;
    protected ByteArrayOutputStream outputStream;

    // Colores corporativos
    protected static final Color COLOR_PRIMARIO = new DeviceRgb(41, 128, 185);
    protected static final Color COLOR_SECUNDARIO = new DeviceRgb(52, 73, 94);
    protected static final Color COLOR_EXITO = new DeviceRgb(46, 204, 113);
    protected static final Color COLOR_PELIGRO = new DeviceRgb(231, 76, 60);
    protected static final Color COLOR_GRIS = new DeviceRgb(149, 165, 166);

    /**
     * Inicializa el documento PDF
     */
    protected void inicializarDocumento() throws IOException {
        outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        pdfDocument = new PdfDocument(writer);
        document = new Document(pdfDocument);
    }

    /**
     * Crea un encabezado estándar
     */
    protected void crearEncabezado(String titulo, String subtitulo) {
        Paragraph tituloParrafo = new Paragraph(titulo)
                .setFontSize(18)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(tituloParrafo);

        if (subtitulo != null && !subtitulo.isEmpty()) {
            Paragraph subtituloParrafo = new Paragraph(subtitulo)
                    .setFontSize(12)
                    .setFontColor(COLOR_SECUNDARIO)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subtituloParrafo);
        }
    }

    /**
     * Crea una tabla con el ancho especificado
     */
    protected Table crearTabla(float[] anchos) {
        Table table = new Table(UnitValue.createPercentArray(anchos));
        table.setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    /**
     * Agrega texto con estilo
     */
    protected Paragraph crearParrafo(String texto, int fontSize, boolean bold) {
        Paragraph p = new Paragraph(texto).setFontSize(fontSize);
        if (bold) {
            p.setBold();
        }
        return p;
    }

    /**
     * Cierra el documento y retorna el ByteArrayOutputStream
     */
    protected ByteArrayOutputStream finalizarDocumento() {
        document.close();
        return outputStream;
    }

    /**
     * Método abstracto que deben implementar las clases hijas
     */
    public abstract ByteArrayOutputStream generar() throws IOException;
}
