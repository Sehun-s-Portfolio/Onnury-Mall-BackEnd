package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewReleaseProductDetailOptionDto {
    private Long product_detail_option_id;
    private String detail_option_name;
    private int option_price;
}
