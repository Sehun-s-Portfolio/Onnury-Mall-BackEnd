package com.onnury.exception.brand;


import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandException implements BrandExceptioInterface {

    private final JPAQueryFactory jpaQueryFactory;

    // 생성하고자 하는 브랜드의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateBrandInfo(BrandCreateRequestDto brandInfo) {

        if(brandInfo.getBrandTitle().isEmpty()){
                return true;
        }

        return false;
    }

    // 수정하고자 하는 브랜드의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateBrandInfo(BrandUpdateRequestDto brandInfo) {

        if(brandInfo.getBrandTitle().isEmpty()){
            return true;
        }

        return false;
    }
}
