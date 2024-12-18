package com.onnury.product.repository;

import com.onnury.product.domain.ProductDetailOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailOptionRepository extends JpaRepository<ProductDetailOption, Long> {
}
