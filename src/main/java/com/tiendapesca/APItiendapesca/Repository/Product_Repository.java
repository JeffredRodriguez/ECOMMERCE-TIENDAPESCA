package com.tiendapesca.APItiendapesca.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tiendapesca.APItiendapesca.Entities.Product;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con productos
 * Extiende JpaRepository para operaciones CRUD básicas
 */
@Repository
public interface Product_Repository extends JpaRepository<Product, Integer> {
    
    /**
     * Busca productos por categorías específicas con paginación
     * @param categories Lista de nombres de categorías a filtrar
     * @param pageable Configuración de paginación
     * @return Página de productos que pertenecen a las categorías especificadas
     */
    Page<Product> findByCategorie_NameIn(List<String> categories, Pageable pageable);

    /**
     * Obtiene una lista de categorías distintas que tienen productos con stock disponible
     * @return Lista de nombres de categorías únicas con stock > 0
     */
    @Query("SELECT DISTINCT(p.categorie.name) FROM Product p WHERE p.stock > 0")
    List<String> findDistinctCategory();

    /**
     * Busca productos por nombre (búsqueda parcial case-insensitive)
     * @param name Texto a buscar en los nombres de productos
     * @param pageable Configuración de paginación
     * @return Página de productos que coinciden con el criterio de búsqueda
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> searchByName(String name, Pageable pageable);
    
    /**
     * Obtiene todos los productos con paginación
     * @param pageable Configuración de paginación
     * @return Página con todos los productos
     */
    Page<Product> findAll(Pageable pageable);
}