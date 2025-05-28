package com.tiendapesca.APItiendapesca.Repository;




import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tiendapesca.APItiendapesca.Entities.Product;




@Repository
public interface Product_Repository extends JpaRepository<Product, Integer>  {
	// Buscar productos por categorías
    Page<Product> findByCategorie_NameIn(List<String> categories, Pageable pageable);

    // Obtener categorías distintas con stock > 0
    @Query("SELECT DISTINCT(p.categorie.name) FROM Product p WHERE p.stock > 0")
    List<String> findDistinctCategory();


    // Búsqueda por nombre
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> searchByName(String name, Pageable pageable);
    
    // Trae todos los productos
    Page<Product> findAll(Pageable pageable);

}
