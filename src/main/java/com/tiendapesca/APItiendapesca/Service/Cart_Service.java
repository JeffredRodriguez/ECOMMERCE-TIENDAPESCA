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

import jakarta.transaction.Transactional;

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

    /**
     * Agrega un producto al carrito o actualiza la cantidad si ya existe
     */
    public void addProductToCart(Users user, AddToCartRequestDTO request) {
        validateUserAndRequest(user, request);
        
        Product product = getProductById(request.getProductId());
        validateStock(product, request.getQuantity());
        
        Optional<Cart> existingCartItem = cartRepository.findByUserAndProduct(user, product);

        if (existingCartItem.isPresent()) {
            updateExistingCartItem(existingCartItem.get(), request.getQuantity(), product);
        } else {
            createNewCartItem(user, product, request.getQuantity());
        }
    }

    /**
     * Obtiene todos los items del carrito para un usuario
     */
    public List<CartItemRespoDTO> getCartItems(Integer userId) {
        validateUserExists(userId);
        return cartRepository.findCartItemsByUserId(userId);
    }

    /**
     * Actualiza la cantidad de un item específico en el carrito
     */
    public void updateCartItemQuantity(Users user, Integer cartItemId, Integer quantity) {
        Cart cartItem = getCartItemById(cartItemId);
        validateUserOwnership(user, cartItem);
        
        if (quantity <= 0) {
            cartRepository.delete(cartItem);
        } else {
            updateItemQuantity(cartItem, quantity);
        }
    }

    /**
     * Elimina un item específico del carrito
     */
    public void removeCartItem(Users user, Integer cartItemId) {
        Cart cartItem = getCartItemById(cartItemId);
        validateUserOwnership(user, cartItem);
        cartRepository.delete(cartItem);
    }

    /**
     * Vacía todo el carrito de un usuario
     */
    @Transactional  // <-- Añade esta anotación
    public void clearCart(Users user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado");
        }
        cartRepository.deleteByUser(user);
    }
    

    /**
     * Calcula el total del carrito sumando todos los items
     */
    public BigDecimal calculateCartTotal(Users user) {
        validateUser(user);
        return cartRepository.findCartItemsByUserId(user.getId())
                .stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Métodos auxiliares privados para mejor organización
    
    private void validateUserAndRequest(Users user, AddToCartRequestDTO request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado");
        }
        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a cero");
        }
    }

    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                String.format("No hay suficiente stock para %s. Stock disponible: %d", 
                    product.getName(), product.getStock()));
        }
    }

    private void updateExistingCartItem(Cart cartItem, int additionalQuantity, Product product) {
        int newQuantity = cartItem.getQuantity() + additionalQuantity;
        validateStock(product, newQuantity);
        cartItem.setQuantity(newQuantity);
        cartRepository.save(cartItem);
    }

    private void createNewCartItem(Users user, Product product, int quantity) {
        Cart newCartItem = new Cart();
        newCartItem.setUser(user);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        cartRepository.save(newCartItem);
    }

    private void validateUserExists(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }

    private Cart getCartItemById(Integer cartItemId) {
        return cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));
    }

    private void validateUserOwnership(Users user, Cart cartItem) {
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "No tienes permiso para modificar este item del carrito");
        }
    }

    private void validateUser(Users user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado");
        }
    }

    private void updateItemQuantity(Cart cartItem, int quantity) {
        Product product = cartItem.getProduct();
        validateStock(product, quantity);
        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
    }
}