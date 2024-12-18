package com.onnury.payment.repository;

import com.onnury.payment.domain.OrderInDeliveryAddPrice;
import com.onnury.payment.domain.OrderInProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInDeliveryAddPriceRepository extends JpaRepository<OrderInDeliveryAddPrice, Long> {
}
