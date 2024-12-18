package com.onnury.payment.repository;

import com.onnury.payment.domain.OrderInProduct;
import com.onnury.payment.domain.ProductOrderOfOrderInProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOrderOfOrderInProductRepository extends JpaRepository<ProductOrderOfOrderInProduct, Long> {
}
