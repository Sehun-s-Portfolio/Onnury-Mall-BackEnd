package com.onnury.payment.repository;

import com.onnury.payment.domain.PaymentZppReq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentZppReqRepository extends JpaRepository<PaymentZppReq, Long> {
}
