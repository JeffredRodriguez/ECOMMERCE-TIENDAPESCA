package com.tiendapesca.APItiendapesca.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.tiendapesca.APItiendapesca.Dtos.AddToCartRequestDTO;
import com.tiendapesca.APItiendapesca.Dtos.CartItemRespoDTO;
import com.tiendapesca.APItiendapesca.Entities.Cart;
import com.tiendapesca.APItiendapesca.Entities.Product;
import com.tiendapesca.APItiendapesca.Entities.Users;
import com.tiendapesca.APItiendapesca.Repository.Cart_Repository;
import com.tiendapesca.APItiendapesca.Repository.Product_Repository;
import com.tiendapesca.APItiendapesca.Repository.Users_Repository;

@Service
public class Cart_Service {

    private final Cart_Repository cartRepository;
    private final Product_Repository productRepository;
    private final Users_Repository userRepository;

    @Autowired
    public Cart_Service(Cart_Repository cartRepository, 
                      Product_Repository productRepository,
                      Users_Repository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void addProductToCart(Users user, AddToCartRequestDTO request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente stock para este producto");
        }

        Optional<Cart> existingCartItem = cartRepository.findByUserAndProduct(user, product);

        if (existingCartItem.isPresent()) {
            Cart cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartRepository.save(cartItem);
        } else {
            Cart newCartItem = new Cart();
            newCartItem.setUser(user);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(request.getQuantity());
            cartRepository.save(newCartItem);
        }
    }

    
    //Valida que el usuario exista
    public List<CartItemRespoDTO> getCartItems(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        
        return cartRepository.findCartItemsByUserId(userId);
    }

    
    //Actualiza el carrito
    public void updateCartItemQuantity(Users user, Integer cartItemId, Integer quantity) {
        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este item del carrito");
        }

        if (quantity <= 0) {
            cartRepository.delete(cartItem);
        } else {
            Product product = cartItem.getProduct();
            if (product.getStock() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente stock para este producto");
            }
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
        }
    }

    
     //borra el carrito
    public void removeCartItem(Users user, Integer cartItemId) {
        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este item del carrito");
        }

        cartRepository.delete(cartItem);
    }

    public void clearCart(Users user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado");
        }
        
        cartRepository.deleteByUser(user);
    }

    // Método para calcular el total del carrito 
    public BigDecimal calculateCartTotal(Users user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado");
        }
        
        return cartRepository.findCartItemsByUserId(user.getId())
                .stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}