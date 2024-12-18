package com.onnury.payment.repository;

import com.onnury.payment.domain.CancleOrder;
import com.onnury.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancleOrderRepository extends JpaRepository<CancleOrder, Long> {
}
