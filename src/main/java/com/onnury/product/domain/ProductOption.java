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
public class ProductOption extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productOptionId;

    @Column(nullable = false)
    private String optionTitle; // 옵션 명

    @Column(nullable = false)
    private String necessaryCheck; // 옵션 선택 필수 유무

}
