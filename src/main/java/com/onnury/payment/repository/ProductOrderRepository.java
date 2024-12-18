package com.onnury.payment.repository;

import com.onnury.payment.domain.PaymentApproval;
import com.onnury.payment.domain.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
}
