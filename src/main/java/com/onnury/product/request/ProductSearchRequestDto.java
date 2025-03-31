package com.onnury.product.request;

import com.onnury.common.base.AbstractVO;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductSearchRequestDto extends AbstractVO {
    private int page;
    private Long upCategoryId;
    private Long middleCategoryId;
    private Long downCategoryId;
    private Long brandId;
    private Long supplierId;
    private String searchKeyword;
}
