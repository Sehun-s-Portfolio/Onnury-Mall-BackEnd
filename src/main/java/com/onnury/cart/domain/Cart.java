package com.onnury.cart.domain;

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
public class Cart extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long cartId;

    @Column(nullable = false)
    private Long memberId; // 장바구니에 담은 고객 id

    @Column(nullable = false)
    private Long productId; // 장바구니에 담은 제품 id

    @Column(nullable = false)
    private String productName; // 장바구니에 담긴 제품 명

    @Column(nullable = false)
    private String productCode; // 장바구니에 담은 제품 분류 코드

    @Column
    private Long productOptionId; // 장바구니에 담은 제품 옵션 id

    @Column
    private String productOptionTitle; // 장바구니에 담은 제품 옵션 타이틀

    @Column
    private Long productDetailOptionId; // 제품 상세 옵션 id

    @Column
    private String productDetailOptionTitle; // 제품 상세 옵션 타이틀

    @Column
    private int productDetailOptionPrice; // 제품 상세 옵션 가격

    @Column(nullable = false)
    private int productPrice; // 제품 가격

    @Column(nullable = false)
    private int quantity; // 제품 수량
}
