package com.onnury.exception.category;


import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.category.request.CategoryCreateRequestDto;
import com.onnury.category.request.CategoryUpdateRequestDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class CategoryException implements CategoryExceptioInterface {

    private final JPAQueryFactory jpaQueryFactory;

    // 생성하고자 하는 카테고리(대, 중)의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateCategoryInfo(MultipartFile categoryImg, CategoryCreateRequestDto categoryInfo) {

        if(categoryImg == null || categoryInfo.getCategoryName().isEmpty()){
                return true;
        }

        return false;
    }

    // 생성하고자 하는 카테고리(소)의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateCategoryInfo2(CategoryCreateRequestDto categoryInfo) {

        if(categoryInfo.getCategoryName().isEmpty() || categoryInfo.getMotherCode().isEmpty()){
            return true;
        }

        return false;
    }

    // 수정하고자 하는 카테고리의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateCategoryInfo(CategoryUpdateRequestDto categoryInfo) {

        if(categoryInfo.getCategoryName().isEmpty()){
            return true;
        }

        return false;
    }
}
