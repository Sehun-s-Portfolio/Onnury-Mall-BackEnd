package com.onnury.product.domain;

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
public class ProductDetailInfo extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productDetailInfoId;

    @Column(columnDefinition = "LONGTEXT")
    private String content; // 상품 상세 정보

    @Column(nullable = false)
    private Long productId; // 관련 제품 id
}
