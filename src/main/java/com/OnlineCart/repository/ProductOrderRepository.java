package com.OnlineCart.repository;

import com.OnlineCart.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder,Integer> {

    public List<ProductOrder> findByUserId(Integer userId);

    public ProductOrder findByOrderId(String orderId);
}
