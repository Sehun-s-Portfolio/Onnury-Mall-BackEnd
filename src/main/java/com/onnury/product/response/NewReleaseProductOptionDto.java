package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewReleaseProductOptionDto {
    private Long product_option_id;
    private String option_title;
    private String necessary_check;
}
