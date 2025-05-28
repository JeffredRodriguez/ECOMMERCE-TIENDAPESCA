package com.tiendapesca.APItiendapesca.Controller;
import com.tiendapesca.APItiendapesca.Entities.Product;
import com.tiendapesca.APItiendapesca.Service.Product_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class Product_Controller {

	 @Autowired
	    private Product_Service productService;

	 
	    //Trae productos paginados 
	    @GetMapping("/get")
	    public Page<Product> AllProducts(@RequestParam(defaultValue = "0") int page,
	                                        @RequestParam(defaultValue = "10") int size) {
	        Pageable pageable = PageRequest.of(page, size);
	        return productService.AllProducts(pageable);
	    }
	    
         
	    
	   //Crea productos
	    @PostMapping("/create")
	    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
	        Product saved = productService.saveProduct(product);
	        return new ResponseEntity<>(saved, HttpStatus.CREATED);
	    }
	    
	    
	    
	    //Actualiza productos
	    @PutMapping("/update/{id}")
	    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product product) {
	        Product updated = productService.updateProduct(id, product);
	        return ResponseEntity.ok(updated);
	    }
	    

	    
	    @DeleteMapping("/delete/{id}")
	    public ResponseEntity<String> eliminarProducto(@PathVariable int id) {
	        try {
	        	productService.deleteProduct(id);  
	            return ResponseEntity.ok("Producto eliminado correctamente.");
	        } catch (RuntimeException e) {
	            return ResponseEntity.status(404).body(e.getMessage());
	        }
	    }

	    }
