package com.tiendapesca.APItiendapesca.Service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tiendapesca.APItiendapesca.Entities.Users;
import com.tiendapesca.APItiendapesca.Repository.Users_Repository;

@Service
public class Users_Service implements UserDetailsService{
	  private final Users_Repository repo;
	    private final PasswordEncoder encoder;

	    public Users_Service(Users_Repository repo, PasswordEncoder encoder) {
	        this.repo = repo;
	        this.encoder = encoder;
	    }

	    
	    //Registra usuarios
	    public Users register(Users user) {
	        user.setPassword(encoder.encode(user.getPassword()));
	        return repo.save(user);
	    }

	   //Verifica con email y contraseña para login
	    public Users findByEmail(String email) {
	        return repo.findByEmail(email).orElseThrow();
	    }

	 
	 

	    @Override
	    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	        return repo.findByEmail(email)
	                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
	    }
	    
	    
	    
	    
	    
	
}
