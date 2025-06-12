package com.tiendapesca.APItiendapesca.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tiendapesca.APItiendapesca.Dtos.CartItemRespoDTO;
import com.tiendapesca.APItiendapesca.Entities.Cart;
import com.tiendapesca.APItiendapesca.Entities.Product;
import com.tiendapesca.APItiendapesca.Entities.Users;

@Repository
public interface Cart_Repository extends JpaRepository<Cart, Integer> {

	    
	@Query("SELECT new com.tiendapesca.APItiendapesca.Dtos.CartItemRespoDTO(" +
		       "c.id, p.id, p.name, p.image_url, p.brand, c.quantity, p.price) " +
		       "FROM Cart c JOIN c.product p WHERE c.user.id = :userId")
		List<CartItemRespoDTO> findCartItemsByUserId(Integer userId);
	    Optional<Cart> findByUserAndProduct(Users user, Product product);
	    
	   
	    
	    void deleteByUser(Users user);
	    void deleteByUserId(Integer userId);
	    List<Cart> findByUser(Users user);
}
