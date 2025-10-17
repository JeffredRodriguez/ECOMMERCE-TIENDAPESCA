package com.tiendapesca.APItiendapesca.Service;

import com.tiendapesca.APItiendapesca.Dtos.InvoicePdfDTO;
import com.tiendapesca.APItiendapesca.Dtos.InvoiceResponseDTO;
import com.tiendapesca.APItiendapesca.Dtos.OrderDetailDTO;
import com.tiendapesca.APItiendapesca.Dtos.ProductItemDTO;
import com.tiendapesca.APItiendapesca.Entities.Invoice;
import com.tiendapesca.APItiendapesca.Entities.Orders;
import com.tiendapesca.APItiendapesca.Repository.Invoice_Repository;
import com.tiendapesca.APItiendapesca.Repository.Orders_Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con facturas
 * Proporciona funcionalidades para generar, cancelar, enviar y consultar facturas
 */
@Service
@Transactional
public class Invoice_Service {

    private static final Logger logger = LoggerFactory.getLogger(Invoice_Service.class);

    private final Invoice_Repository invoiceRepository;
    private final Orders_Repository orderRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final Email_Service emailService;

    /**
     * Constructor para inyección de dependencias
     * @param invoiceRepository Repositorio de facturas
     * @param orderRepository Repositorio de órdenes
     * @param pdfGeneratorService Servicio para generación de PDF
     * @param emailService Servicio para envío de emails
     */
    @Autowired
    public Invoice_Service(Invoice_Repository invoiceRepository,
                           Orders_Repository orderRepository,
                           PdfGeneratorService pdfGeneratorService,
                           Email_Service emailService) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.emailService = emailService;
    }

    /**
     * Genera un número único de factura
     * @return Número de factura en formato INV-YYYY-UUID
     */
    public String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Genera y guarda una factura para una orden específica
     * @param orderId ID de la orden
     * @return Factura generada
     * @throws Exception Si ocurre un error durante la generación
     */
    @Transactional
    public Invoice generateAndSaveInvoice(Integer orderId) throws Exception {
        logger.info("Generando factura para orden ID: {}", orderId);

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Orden no encontrada con ID: {}", orderId);
                    return new RuntimeException("Orden no encontrada con ID: " + orderId);
                });

        if (invoiceRepository.existsByOrder(order)) {
            logger.warn("Ya existe una factura para la orden ID: {}", orderId);
            throw new RuntimeException("Ya existe una factura para esta orden");
        }

        String invoiceNumber = generateInvoiceNumber();
        logger.debug("Número de factura generado: {}", invoiceNumber);

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setDate(LocalDateTime.now());
        invoice.setInvoiceNumber(invoiceNumber);

        try {
            // PRIMERO guardar la factura en la base de datos
            Invoice savedInvoice = invoiceRepository.save(invoice);
            
            // LUEGO generar el PDF usando el DTO seguro (sin recursión)
            InvoicePdfDTO invoicePdfDTO = convertToInvoicePdfDTO(savedInvoice);
            byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(invoicePdfDTO);
            
            // Guardar el PDF y actualizar la URL
            String pdfPath = pdfGeneratorService.savePdfToStorage(pdfBytes, invoiceNumber);
            savedInvoice.setPdfUrl(pdfPath);
            
            // Actualizar la factura con la URL del PDF
            Invoice finalInvoice = invoiceRepository.save(savedInvoice);
            
            logger.info("Factura generada exitosamente con ID: {}", finalInvoice.getId());
            return finalInvoice;
            
        } catch (Exception e) {
            logger.error("Error al generar factura para orden ID: {}", orderId, e);
            throw new Exception("Error al generar la factura", e);
        }
    }

    /**
     * Convierte una entidad Invoice a InvoicePdfDTO para generar PDF sin recursión
     * @param invoice Factura a convertir
     * @return DTO seguro para generación de PDF
     */
    private InvoicePdfDTO convertToInvoicePdfDTO(Invoice invoice) {
        // Convertir los detalles del pedido a ProductItemDTO
        List<ProductItemDTO> productItems = invoice.getOrder().getOrderDetails().stream()
            .map(detail -> new ProductItemDTO(
                detail.getProduct().getName(),
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getSubtotal(),
                detail.getTax()
            ))
            .collect(Collectors.toList());

        return new InvoicePdfDTO(
            invoice.getInvoiceNumber(),
            invoice.getDate(),
            invoice.getOrder().getPaymentMethod().toString(),
            invoice.getOrder().getUser().getName(),
            invoice.getOrder().getUser().getEmail(),
            invoice.getOrder().getShippingAddress(),
            invoice.getOrder().getPhone(),
            productItems,
            invoice.getOrder().getTotalWithoutTax(),
            invoice.getOrder().getTax(),
            invoice.getOrder().getFinalTotal()
        );
    }

    /**
     * Cancela una factura existente
     * @param orderId ID de la orden asociada a la factura
     */
    @Transactional
    public void cancelInvoice(Integer orderId) {
        try {
            logger.info("Cancelando factura para orden ID: {}", orderId);

            Invoice invoice = invoiceRepository.findByOrderId(orderId).orElse(null);

            if (invoice != null) {
                invoice.setIsCanceled(true);
                invoice.setCancelationDate(LocalDateTime.now());
                invoiceRepository.save(invoice);
                logger.info("Factura cancelada exitosamente para orden ID: {}", orderId);
            } else {
                logger.warn("No se encontró factura para cancelar con orden ID: {}", orderId);
            }
        } catch (Exception e) {
            logger.error("Error al cancelar factura para orden ID: {}", orderId, e);
            throw new RuntimeException("Error al cancelar la factura", e);
        }
    }

    /**
     * Envía una factura por correo electrónico
     * @param orderId ID de la orden asociada a la factura
     * @param emailAddress Dirección de correo del destinatario
     * @throws Exception Si ocurre un error durante el envío
     */
    @Transactional
    public void sendInvoiceByEmail(Integer orderId, String emailAddress) throws Exception {
        logger.info("Enviando factura por email para orden ID: {} a {}", orderId, emailAddress);

        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.error("Factura no encontrada para orden ID: {}", orderId);
                    return new RuntimeException("Factura no encontrada");
                });

        try {
            byte[] pdfBytes = Files.readAllBytes(Paths.get(invoice.getPdfUrl()));

            emailService.sendEmailWithAttachment(
                    emailAddress,
                    "Factura #" + invoice.getInvoiceNumber(),
                    "Adjunto encontrará la factura de su compra reciente.",
                    "factura_" + invoice.getInvoiceNumber() + ".pdf",
                    pdfBytes
            );

            logger.info("Factura enviada exitosamente a {}", emailAddress);
        } catch (Exception e) {
            logger.error("Error al enviar factura por email para orden ID: {}", orderId, e);
            throw new Exception("Error al enviar la factura por email", e);
        }
    }

    /**
     * Obtiene la factura asociada a una orden
     * @param orderId ID de la orden
     * @return Factura encontrada
     */
    public Invoice getInvoiceForOrder(Integer orderId) {
        logger.debug("Buscando factura para orden ID: {}", orderId);

        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.error("Factura no encontrada para orden ID: {}", orderId);
                    return new RuntimeException("Factura no encontrada para la orden: " + orderId);
                });
    }

    /**
     * Obtiene el archivo PDF de una factura
     * @param orderId ID de la orden asociada
     * @return Bytes del archivo PDF
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public byte[] getInvoicePdf(Integer orderId) throws IOException {
        logger.debug("Obteniendo PDF de factura para orden ID: {}", orderId);

        Invoice invoice = getInvoiceForOrder(orderId);
        return Files.readAllBytes(Paths.get(invoice.getPdfUrl()));
    }

    /**
     * Convierte una entidad Invoice a un DTO de respuesta
     * @param invoice Factura a convertir
     * @return DTO con la información de la factura
     */
    public InvoiceResponseDTO convertInvoiceToResponseDTO(Invoice invoice) {
        Orders order = invoice.getOrder();

        List<OrderDetailDTO> detailDTOs = order.getOrderDetails().stream().map(item -> {
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setSubtotal(item.getSubtotal());
            dto.setTax(item.getTax());
            dto.setTotal(item.getTotal());
            return dto;
        }).collect(Collectors.toList());

        return new InvoiceResponseDTO(
            order.getUser().getEmail(),
            order.getFinalTotal(),
            order.getId(),
            invoice.getInvoiceNumber(),
            invoice.getDate(),
            detailDTOs
        );
    }

    /**
     * Método alternativo para obtener el PDF usando DTO seguro
     * @param orderId ID de la orden
     * @return Bytes del archivo PDF
     * @throws Exception Si ocurre un error
     */
    public byte[] generateInvoicePdfSafe(Integer orderId) throws Exception {
        logger.debug("Generando PDF seguro para orden ID: {}", orderId);

        Invoice invoice = getInvoiceForOrder(orderId);
        InvoicePdfDTO invoicePdfDTO = convertToInvoicePdfDTO(invoice);
        return pdfGeneratorService.generateInvoicePdf(invoicePdfDTO);
    }
}