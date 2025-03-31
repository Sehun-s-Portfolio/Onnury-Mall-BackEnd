package com.onnury.brand.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class BrandUpdateRequestDto extends AbstractVO {
    private String brandTitle; // 브랜드명
}
