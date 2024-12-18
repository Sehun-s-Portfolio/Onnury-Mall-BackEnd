package com.onnury.payment.repository;

import com.onnury.payment.domain.PaymentApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentApprovalRepository extends JpaRepository<PaymentApproval, Long> {
}
