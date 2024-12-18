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
public class ProductOfMedia extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productOfMediaId;

    @Column(nullable = false)
    private Long productId; // 제품 id

    @Column(nullable = false)
    private Long mediaId; // 이미지 id

}
