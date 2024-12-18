package com.onnury.product.repository;

import com.onnury.product.domain.ProductOfOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfOptionRepository extends JpaRepository<ProductOfOption, Long> {
}
