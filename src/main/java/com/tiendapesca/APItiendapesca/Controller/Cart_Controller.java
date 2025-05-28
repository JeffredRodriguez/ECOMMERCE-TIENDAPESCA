package com.tiendapesca.APItiendapesca.Controller;

import com.tiendapesca.APItiendapesca.Dtos.AddToCartRequestDTO;
import com.tiendapesca.APItiendapesca.Dtos.CartItemRespoDTO;
import com.tiendapesca.APItiendapesca.Entities.Users;
import com.tiendapesca.APItiendapesca.Service.Cart_Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class Cart_Controller {

    private final Cart_Service cartService;

    @Autowired
    public Cart_Controller(Cart_Service cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@AuthenticationPrincipal Users user,
                                         @Valid @RequestBody AddToCartRequestDTO request) {
        cartService.addProductToCart(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    
    
    @GetMapping("/get")
    public ResponseEntity<List<CartItemRespoDTO>> getCartItems(@AuthenticationPrincipal Users user) {
        List<CartItemRespoDTO> cartItems = cartService.getCartItems(user.getId());
        return ResponseEntity.ok(cartItems);
    }
    
    @PutMapping("/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(@AuthenticationPrincipal Users user,
                                             @PathVariable Integer cartItemId,
                                             @RequestParam Integer quantity) {
        cartService.updateCartItemQuantity(user, cartItemId, quantity);
        return ResponseEntity.noContent().build();
    }
    
    
    //elimina el carrito
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Users user,
                                             @PathVariable Integer cartItemId) {
        cartService.removeCartItem(user, cartItemId);
        return ResponseEntity.noContent().build();
    }

    
    //
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Users user) {
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}