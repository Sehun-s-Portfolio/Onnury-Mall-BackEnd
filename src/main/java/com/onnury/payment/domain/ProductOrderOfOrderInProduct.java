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
public class ProductOrderOfOrderInProduct extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productOrderOfOrderInProductId;

    @Column(nullable = false)
    private Long productOrderId; // 수령자 정보를 포함한 ProductOrder 도메인 id

    @Column(nullable = false)
    private Long orderInProductId; // ProductOrder 와 연관된 제품들 정보를 가진 OrderInProduct 도메인 id

    @Column
    private Long memberId; // 주문한 유저 id
}
