package com.onnury.payment.repository;

import com.onnury.payment.domain.OrderInProduct;
import com.onnury.payment.domain.PaymentApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInProductRepository extends JpaRepository<OrderInProduct, Long> {
}
