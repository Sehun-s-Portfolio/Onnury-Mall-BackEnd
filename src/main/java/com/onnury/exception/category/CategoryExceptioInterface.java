package com.onnury.exception.category;

import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.category.request.CategoryCreateRequestDto;
import com.onnury.category.request.CategoryUpdateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface CategoryExceptioInterface {

    // 생성하고자 하는 카테고리(대, 중)의 정보가 옳바른지 확인
    boolean checkCreateCategoryInfo(MultipartFile categoryImg, CategoryCreateRequestDto categoryInfo);

    // 생성하고자 하는 카테고리(소)의 정보가 옳바른지 확인
    boolean checkCreateCategoryInfo2(CategoryCreateRequestDto categoryInfo);

    // 수정하고자 하는 카테고리의 정보가 옳바른지 확인
    boolean checkUpdateCategoryInfo(CategoryUpdateRequestDto categoryInfo);
}
