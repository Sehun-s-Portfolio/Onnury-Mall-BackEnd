package com.onnury.product.repository;

import com.onnury.product.domain.ProductOfMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfMediaRepository extends JpaRepository<ProductOfMedia, Long> {
}
