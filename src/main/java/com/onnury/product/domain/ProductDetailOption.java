package com.onnury.product.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ProductDetailOption extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productDetailOptionId;

    @Column(nullable = false)
    private String detailOptionName; // 상세 옵션 명

    @Column(nullable = false)
    private int optionPrice; // 옵션 가격

    @JsonIgnore
    @JoinColumn(name = "productOptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductOption productOption; // 연관된 상위 옵션 id
}
