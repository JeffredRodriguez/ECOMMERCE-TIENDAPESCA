package com.tiendapesca.APItiendapesca.Dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.tiendapesca.APItiendapesca.Entities.OrderStatus;
import com.tiendapesca.APItiendapesca.Entities.PaymentMethod;

public class OrderResponseDTO {
	private Integer orderId;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String phone;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    private List<OrderDetailDTO> orderDetails;

    // Getters y Setters
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderDetailDTO> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetailDTO> orderDetails) {
        this.orderDetails = orderDetails;
    }
	    
	  
		
		
	
	    
}
