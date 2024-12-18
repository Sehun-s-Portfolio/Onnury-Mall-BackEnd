package com.onnury.payment.repository;

import com.onnury.payment.domain.EasyPaymentBasketInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EasyPaymentBasketInfoRepository extends JpaRepository<EasyPaymentBasketInfo, Long> {
}
