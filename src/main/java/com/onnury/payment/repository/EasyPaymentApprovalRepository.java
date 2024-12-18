package com.onnury.payment.repository;

import com.onnury.payment.domain.EasyPaymentApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EasyPaymentApprovalRepository extends JpaRepository<EasyPaymentApproval, Long> {
}
