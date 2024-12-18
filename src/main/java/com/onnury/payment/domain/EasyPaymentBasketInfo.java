package com.onnury.payment.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class EasyPaymentBasketInfo extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long easyPaymentBasketInfoId;

    @Column
    private String productNo;

    @Column
    private String productPgCno;

    @Column
    private String sellerId;

    @Column
    private Long easyPaymentApprovalId;
}
