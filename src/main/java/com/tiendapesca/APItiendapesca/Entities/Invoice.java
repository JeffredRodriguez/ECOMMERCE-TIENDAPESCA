package com.tiendapesca.APItiendapesca.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Orders order;

    @Column(name = "date", columnDefinition = "DATETIME")
    private LocalDateTime date;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    
    public Invoice() {}

    public Invoice(Orders order, LocalDateTime date, String invoiceNumber, String pdfUrl) {
        this.order = order;
        this.date = date;
        this.invoiceNumber = invoiceNumber;
        this.pdfUrl = pdfUrl;
    }

   
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
