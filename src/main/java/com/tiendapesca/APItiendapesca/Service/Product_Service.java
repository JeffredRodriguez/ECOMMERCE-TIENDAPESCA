package com.tiendapesca.APItiendapesca.Service;

import com.tiendapesca.APItiendapesca.Entities.Product;
import com.tiendapesca.APItiendapesca.Repository.Product_Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class Product_Service {

    @Autowired
    private Product_Repository productRepository;
    
    
    //Trae los productos paginados 
    public Page<Product> AllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    
    //Guarda productos
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}
	
	
	
	public Product updateProduct(int id, Product updatedProduct) {
	    Product existingProduct = productRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

	    // Actualizar solo los campos no nulos
	    Optional.ofNullable(updatedProduct.getBrand()).ifPresent(existingProduct::setBrand);
	    Optional.ofNullable(updatedProduct.getName()).ifPresent(existingProduct::setName);
	    Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existingProduct::setDescription);
	    Optional.ofNullable(updatedProduct.getFeatures()).ifPresent(existingProduct::setFeatures);
	    Optional.ofNullable(updatedProduct.getPrice()).ifPresent(existingProduct::setPrice);
	    Optional.ofNullable(updatedProduct.getStock()).ifPresent(existingProduct::setStock);
	    Optional.ofNullable(updatedProduct.getImage_url()).ifPresent(existingProduct::setImage_url);
	    Optional.ofNullable(updatedProduct.getCategorie()).ifPresent(existingProduct::setCategorie);
	    
	    existingProduct.setDate(LocalDateTime.now());
	    
	    return productRepository.save(existingProduct);
	}
	
	
	 // Elimina productos 
	 public void deleteProduct(int id) {
	        if (!productRepository.existsById(id)) {
	            throw new RuntimeException("Producto con ID " + id + " no existe.");
	        }
	        productRepository.deleteById(id);
	    }

}
