package com.onnury.cart.domain;

import com.onnury.share.TimeStamped;
import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class ProductInCart extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long productInCartId;

    @Column
    private Long productId;

    @Column
    private Long productOptionId;

    @Column
    private Long productDetailOptionId;

    @Column
    private Long cartId;
}
