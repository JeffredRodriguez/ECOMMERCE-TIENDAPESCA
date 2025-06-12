package com.tiendapesca.APItiendapesca.Repository;

import com.tiendapesca.APItiendapesca.Entities.OrderStatus;
import com.tiendapesca.APItiendapesca.Entities.Orders;
import com.tiendapesca.APItiendapesca.Entities.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Orders_Repository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUser(Users user);
    List<Orders> findByUserAndStatus(Users user, OrderStatus status);
}