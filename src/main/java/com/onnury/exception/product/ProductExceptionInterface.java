package com.onnury.exception.product;

import com.onnury.product.request.ProductCreateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface ProductExceptionInterface {

    // 제품 생성 이미지 정보 정합성 검증
    boolean checkProductImages(List<MultipartFile> productImgs);
}
