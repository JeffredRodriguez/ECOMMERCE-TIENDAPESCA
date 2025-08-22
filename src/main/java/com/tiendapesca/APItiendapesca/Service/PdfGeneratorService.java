package com.tiendapesca.APItiendapesca.Service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.tiendapesca.APItiendapesca.Entities.Invoice;
import com.tiendapesca.APItiendapesca.Entities.OrderDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

@Service
public class PdfGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorService.class);
    
    // Configuración fija
    private static final String INVOICE_DIRECTORY = "invoices";
    private static final String COMPANY_LOGO_PATH = "src/main/resources/static/img/logoKraken.png";
    private static final String COMPANY_NAME = "Kraken Lures";
    private static final String COMPANY_ADDRESS = "San José, Costa Rica";
    private static final String COMPANY_PHONE = "+506 2222-5555";
    private static final String COMPANY_EMAIL = "info@krakenlures.com";
    private static final String COMPANY_WEBSITE = "www.krakenlures.com";
    
    // Esquema de colores corporativos
    private static final BaseColor PRIMARY_COLOR = new BaseColor(0, 51, 102);
    private static final BaseColor SECONDARY_COLOR = new BaseColor(220, 220, 220);
    private static final BaseColor ACCENT_COLOR = new BaseColor(255, 153, 0);
    
    public byte[] generateInvoicePdf(Invoice invoice) throws DocumentException, IOException {
        // Configuración del documento con margen superior aumentado
        Document document = new Document(PageSize.A4, 40, 40, 120, 40); // Margen superior aumentado a 120
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        
        writer.setPageEvent(new InvoicePageEventHandler());
        document.open();
        
        // Agregar espacio en blanco inicial para bajar el título
        Paragraph initialSpace = new Paragraph(" ");
        initialSpace.setSpacingAfter(60f); // Espacio adicional antes del título
        document.add(initialSpace);
        
        // Agregar contenido principal
        addInvoiceHeader(document);
        addInvoiceInfo(document, invoice);
        addCustomerInfo(document, invoice);
        addProductsTable(document, invoice);
        addTotalsSection(document, invoice);
        addTermsAndConditions(document);
        
        document.close();
        return outputStream.toByteArray();
    }

    public String savePdfToStorage(byte[] pdfBytes, String invoiceNumber) throws IOException {
        try {
            Path directory = Paths.get(INVOICE_DIRECTORY);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                logger.info("Directorio de facturas creado: {}", directory.toAbsolutePath());
            }
            
            String fileName = "factura_" + invoiceNumber.replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
            Path filePath = directory.resolve(fileName);
            Files.write(filePath, pdfBytes);
            logger.info("Factura guardada en: {}", filePath);
            
            return filePath.toString();
        } catch (IOException e) {
            logger.error("Error al guardar el PDF: {}", e.getMessage());
            throw new IOException("No se pudo guardar el archivo PDF", e);
        }
    }

    private void addInvoiceHeader(Document document) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
        Paragraph title = new Paragraph("FACTURA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30f); // Espacio después del título reducido
        document.add(title);
    }

    private void addInvoiceInfo(Document document, Invoice invoice) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15f);
        
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        
        addInfoRow(infoTable, "Número de Factura:", invoice.getInvoiceNumber(), labelFont);
        addInfoRow(infoTable, "Fecha de Emisión:", 
                 invoice.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), labelFont);
        addInfoRow(infoTable, "Términos de Pago:", 
                 invoice.getOrder().getPaymentMethod() + " - Contado", labelFont);
        
        document.add(infoTable);
    }
    
    private void addCustomerInfo(Document document, Invoice invoice) throws DocumentException {
        PdfPTable customerTable = new PdfPTable(2);
        customerTable.setWidthPercentage(100);
        customerTable.setSpacingAfter(20f);
        
        PdfPCell sectionHeader = new PdfPCell(new Phrase("INFORMACIÓN DEL CLIENTE", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
        sectionHeader.setColspan(2);
        sectionHeader.setBorder(Rectangle.NO_BORDER);
        sectionHeader.setPaddingBottom(8f);
        customerTable.addCell(sectionHeader);
        
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        
        addInfoRow(customerTable, "Nombre:", invoice.getOrder().getUser().getName(), labelFont);
        addInfoRow(customerTable, "Email:", invoice.getOrder().getUser().getEmail(), labelFont);
        addInfoRow(customerTable, "Dirección:", invoice.getOrder().getShippingAddress(), labelFont);
        addInfoRow(customerTable, "Teléfono:", invoice.getOrder().getPhone(), labelFont);
        
        document.add(customerTable);
    }
    
    private void addProductsTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15f);
        table.setSpacingAfter(25f);
        
        float[] columnWidths = {3f, 1.5f, 1f, 1.5f, 1.5f};
        table.setWidths(columnWidths);
        
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        
        addTableHeaderCell(table, "Producto", headerFont);
        addTableHeaderCell(table, "Precio Unitario", headerFont);
        addTableHeaderCell(table, "Cantidad", headerFont);
        addTableHeaderCell(table, "Subtotal", headerFont);
        addTableHeaderCell(table, "Impuesto", headerFont);
        
        Font productFont = new Font(Font.FontFamily.HELVETICA, 10);
        for (OrderDetail detail : invoice.getOrder().getOrderDetails()) {
            addProductRow(table, detail, productFont);
        }
        
        document.add(table);
    }
    
    private void addTotalsSection(Document document, Invoice invoice) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingAfter(15f);
        
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10);
        
        addTotalRow(totalsTable, "Subtotal:", invoice.getOrder().getTotalWithoutTax(), labelFont, valueFont);
        addTotalRow(totalsTable, "Impuesto (13%):", invoice.getOrder().getTax(), labelFont, valueFont);
        
        PdfPCell dividerCell = new PdfPCell(new Phrase(" "));
        dividerCell.setColspan(2);
        dividerCell.setBorder(PdfPCell.TOP);
        dividerCell.setBorderColor(BaseColor.LIGHT_GRAY);
        dividerCell.setPaddingTop(5f);
        dividerCell.setPaddingBottom(5f);
        totalsTable.addCell(dividerCell);
        
        Font totalLabelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR);
        Font totalValueFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR);
        addTotalRow(totalsTable, "TOTAL:", invoice.getOrder().getFinalTotal(), totalLabelFont, totalValueFont);
        
        document.add(totalsTable);
    }
    
    private void addTermsAndConditions(Document document) throws DocumentException {
        Paragraph terms = new Paragraph();
        terms.setSpacingBefore(25f);
        
        terms.add(new Chunk("Términos y Condiciones:\n", 
            new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.DARK_GRAY)));
        
        terms.add(new Chunk("1. Todos los precios están en dólares estadounidenses (USD).\n" +
                          "2. Pago debido inmediatamente al recibir la factura.\n" +
                          "3. Productos no retornables una vez abiertos.\n" +
                          "4. Garantía limitada a 30 días contra defectos de fabricación.\n",
            new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.DARK_GRAY)));
        
        document.add(terms);
    }
    
    
    
    // Métodos auxiliares...Vista del PDF
    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3f);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3f);
        table.addCell(valueCell);
    }
    
    private void addTableHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(6f);
        table.addCell(cell);
    }
    
    private void addProductRow(PdfPTable table, OrderDetail detail, Font font) {
        table.addCell(createTableCell(detail.getProduct().getName(), Element.ALIGN_LEFT, font));
        table.addCell(createTableCell(String.format("$%.2f", detail.getUnitPrice()), Element.ALIGN_RIGHT, font));
        table.addCell(createTableCell(String.valueOf(detail.getQuantity()), Element.ALIGN_CENTER, font));
        table.addCell(createTableCell(String.format("$%.2f", detail.getSubtotal()), Element.ALIGN_RIGHT, font));
        table.addCell(createTableCell(String.format("$%.2f", detail.getTax()), Element.ALIGN_RIGHT, font));
    }
    
    private PdfPCell createTableCell(String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5f);
        return cell;
    }
    
    private void addTotalRow(PdfPTable table, String label, BigDecimal value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(3f);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(String.format("$%.2f", value), valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3f);
        table.addCell(valueCell);
    }
    
    private class InvoicePageEventHandler extends PdfPageEventHelper {
        private Image logo;
        
        public InvoicePageEventHandler() {
            try {
                this.logo = Image.getInstance(COMPANY_LOGO_PATH);
                this.logo.scaleToFit(120, 60);
            } catch (Exception e) {
                logger.warn("No se pudo cargar el logo: {}", e.getMessage());
            }
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                
                // Logo más abajo para coincidir con el nuevo diseño
                if (logo != null) {
                    logo.setAbsolutePosition(document.left(), document.top() - 40);
                    cb.addImage(logo);
                }
                
                // Información de la empresa ajustada
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(COMPANY_NAME, 
                        new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)),
                    document.right(), document.top() - 30, 0);
                
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(COMPANY_ADDRESS, 
                        new Font(Font.FontFamily.HELVETICA, 8)),
                    document.right(), document.top() - 40, 0);
                
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Tel: " + COMPANY_PHONE, 
                        new Font(Font.FontFamily.HELVETICA, 8)),
                    document.right(), document.top() - 50, 0);
                
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(COMPANY_EMAIL, 
                        new Font(Font.FontFamily.HELVETICA, 8)),
                    document.right(), document.top() - 60, 0);
                
                // Línea separadora ajustada
                cb.setColorStroke(PRIMARY_COLOR);
                cb.setLineWidth(0.8f);
                cb.moveTo(document.left(), document.top() - 70);
                cb.lineTo(document.right(), document.top() - 70);
                cb.stroke();
                
                // Pie de página
                Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
                
                cb.setColorStroke(SECONDARY_COLOR);
                cb.setLineWidth(0.5f);
                cb.moveTo(document.left(), document.bottom() - 20);
                cb.lineTo(document.right(), document.bottom() - 20);
                cb.stroke();
                
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Gracias por su compra", footerFont),
                    document.left(), document.bottom() - 30, 0);
                
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(COMPANY_NAME + " - " + COMPANY_WEBSITE, footerFont),
                    (document.right() - document.left()) / 2 + document.left(), 
                    document.bottom() - 30, 0);
                
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), footerFont),
                    document.right(), document.bottom() - 30, 0);
                
            } catch (Exception e) {
                logger.error("Error al generar encabezado/pie de página: {}", e.getMessage());
            }
        }
    }
}