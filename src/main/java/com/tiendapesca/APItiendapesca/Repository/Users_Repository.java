package com.tiendapesca.APItiendapesca.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiendapesca.APItiendapesca.Entities.Users;

@Repository
public interface Users_Repository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
    
}