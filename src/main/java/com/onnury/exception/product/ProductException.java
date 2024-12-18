package com.onnury.exception.product;

import com.onnury.product.request.ProductCreateRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
public class ProductException implements ProductExceptionInterface{

    // 제품 생성 이미지 정보 정합성 검증
    @Override
    public boolean checkProductImages(
            List<MultipartFile> productImgs) {

        if(productImgs.isEmpty()){
            return true;
        }

        return false;
    }
}
