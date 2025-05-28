package com.tiendapesca.APItiendapesca.Entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "date", columnDefinition = "DATETIME")
    private LocalDateTime date;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "total_without_tax", precision = 10, scale = 2)
    private BigDecimal totalWithoutTax;

    @Column(name = "tax", precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "final_total", precision = 10, scale = 2)
    private BigDecimal finalTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    // --- Constructores ---
    public Orders() {}

    public Orders(Users user, LocalDateTime date, String shippingAddress, String phone,
                  BigDecimal totalWithoutTax, BigDecimal tax, BigDecimal finalTotal, PaymentMethod paymentMethod) {
        this.user = user;
        this.date = date;
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.totalWithoutTax = totalWithoutTax;
        this.tax = tax;
        this.finalTotal = finalTotal;
        this.paymentMethod = paymentMethod;
    }

    // --- Getters y Setters ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getTotalWithoutTax() {
        return totalWithoutTax;
    }

    public void setTotalWithoutTax(BigDecimal totalWithoutTax) {
        this.totalWithoutTax = totalWithoutTax;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
