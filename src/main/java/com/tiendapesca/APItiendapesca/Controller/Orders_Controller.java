package com.tiendapesca.APItiendapesca.Controller;

import com.tiendapesca.APItiendapesca.Dtos.OrderRequestDTO;
import com.tiendapesca.APItiendapesca.Dtos.OrderResponseDTO;
import com.tiendapesca.APItiendapesca.Entities.Users;
import com.tiendapesca.APItiendapesca.Service.Orders_Service;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class Orders_Controller {

    private final Orders_Service ordersService;

    @Autowired
    public Orders_Controller(Orders_Service ordersService) {
        this.ordersService = ordersService;
    }
    
    // Crea la orden con los datos del carrito de compras
    @PostMapping("/add")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @AuthenticationPrincipal Users user,
            @Valid @RequestBody OrderRequestDTO orderRequest) {
        OrderResponseDTO orderResponse = ordersService.createOrderFromCart(user, orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
    
    // Consulta ordenes
    @GetMapping("/get")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
            @AuthenticationPrincipal Users user) {
        List<OrderResponseDTO> orders = ordersService.getUserOrders(user.getId());
        return ResponseEntity.ok(orders);
    }

    // Consulta orden por ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderDetails(
            @AuthenticationPrincipal Users user,
            @PathVariable Integer orderId) {
        OrderResponseDTO order = ordersService.getOrderDetails(orderId, user);
        return ResponseEntity.ok(order);
    }
     
    //Actualiza el estado de la orden a cancelada
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal Users user,
            @PathVariable Integer orderId) {
        ordersService.cancelOrder(orderId, user);
        return ResponseEntity.noContent().build();
    }

    // Endpoint adicional para obtener detalles completos de una orden con items
    @GetMapping("/{orderId}/items")
    public ResponseEntity<OrderResponseDTO> getOrderWithItems(
            @AuthenticationPrincipal Users user,
            @PathVariable Integer orderId) {
        OrderResponseDTO order = ordersService.getOrderDetails(orderId, user);
        return ResponseEntity.ok(order);
    }
}