package com.tiendapesca.APItiendapesca.Repository;

import com.tiendapesca.APItiendapesca.Entities.Invoice;
import com.tiendapesca.APItiendapesca.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Invoice_Repository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findByOrder(Orders order);
    Optional<Invoice> findByOrderId(Integer orderId);
    boolean existsByOrder(Orders order);
}