package com.onnury.label.repository;

import com.onnury.label.domain.LabelOfProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelOfProductRepository extends JpaRepository<LabelOfProduct, Long> {
}
