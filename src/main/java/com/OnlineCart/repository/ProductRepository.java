package com.OnlineCart.repository;

import com.OnlineCart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {
    public List<Product> findByIsActiveTrue();

    public Page<Product> findByIsActiveTrue(Pageable pageable);

    public Page<Product> findByCategory(Pageable pageable, String category);

    public Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
                                                                                       Pageable pageable);

    public List<Product> findByCategory(String category);

    public Page<Product> findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
                                                                                               Pageable pageable);
    public List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);
}
