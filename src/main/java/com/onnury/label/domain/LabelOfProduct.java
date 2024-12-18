package com.onnury.label.domain;

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
public class LabelOfProduct extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long labelOfProductId;

    @Column(nullable = false)
    private Long labelId; // 라벨ID

    @Column(nullable = false)
    private Long productId; // 상품ID
}
