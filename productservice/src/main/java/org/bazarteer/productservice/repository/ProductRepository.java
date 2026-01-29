package org.bazarteer.productservice.repository;

import java.util.List;

import org.bazarteer.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.content ORDER BY p.createdAt DESC")
    List<Product> findTop10ByOrderByCreatedAtDescWithContent();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.content WHERE p.ownerid = :ownerid ORDER BY p.createdAt DESC")
    List<Product> findByOwnerid(String ownerid);
}
