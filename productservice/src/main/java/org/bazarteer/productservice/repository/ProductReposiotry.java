package org.bazarteer.productservice.repository;

import org.bazarteer.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReposiotry extends JpaRepository<Product, Integer> {

}
