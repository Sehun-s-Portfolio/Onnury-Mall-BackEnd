package com.onnury.exception.brand;

import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public interface BrandExceptioInterface {

    // 생성하고자 하는 브랜드의 정보가 옳바른지 확인
    boolean checkCreateBrandInfo(BrandCreateRequestDto brandInfo);

    // 수정하고자 하는 브랜드의 정보가 옳바른지 확인
    boolean checkUpdateBrandInfo(BrandUpdateRequestDto brandInfo);
}
