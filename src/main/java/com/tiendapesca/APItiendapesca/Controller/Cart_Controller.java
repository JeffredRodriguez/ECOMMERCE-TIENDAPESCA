package com.tiendapesca.APItiendapesca.Controller;

import com.tiendapesca.APItiendapesca.Dtos.AddToCartRequestDTO;
import com.tiendapesca.APItiendapesca.Dtos.CartItemRespoDTO;
import com.tiendapesca.APItiendapesca.Entities.Users;
import com.tiendapesca.APItiendapesca.Service.Cart_Service;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class Cart_Controller {

    private final Cart_Service cartService;

    @Autowired
    public Cart_Controller(Cart_Service cartService) {
        this.cartService = cartService;
    }

    //Agrega Productos al carrito de compras
    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@AuthenticationPrincipal Users user,
                                        @Valid @RequestBody AddToCartRequestDTO request) {
        cartService.addProductToCart(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //Consulta datos del carrito de compras
    @GetMapping("/get")
    public ResponseEntity<List<CartItemRespoDTO>> getCart(@AuthenticationPrincipal Users user) {
        return ResponseEntity.ok(cartService.getCartItems(user.getId()));
    }
    
    //Calcula total del pedido
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal(@AuthenticationPrincipal Users user) {
        return ResponseEntity.ok(cartService.calculateCartTotal(user));
    }
  
  //Actualiza items del carrito de compras
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(@AuthenticationPrincipal Users user,
                                            @PathVariable Integer cartItemId,
                                            @RequestParam @Min(1) Integer quantity) {
        cartService.updateCartItemQuantity(user, cartItemId, quantity);
        return ResponseEntity.noContent().build();
    }
    
 //Elimina items del carrito de compras
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Users user,
                                            @PathVariable Integer cartItemId) {
        cartService.removeCartItem(user, cartItemId);
        return ResponseEntity.noContent().build();
    }

 // Endpoint para limpiar todo el carrito de compras
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Users user) {
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}