package com.onnury.product.repository;

import com.onnury.product.domain.ProductDetailInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailInfoRepository extends JpaRepository<ProductDetailInfo, Long> {
}
