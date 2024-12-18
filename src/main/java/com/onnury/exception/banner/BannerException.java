package com.onnury.exception.banner;

import com.onnury.banner.request.BannerCreateRequestDto;
import com.onnury.banner.request.BannerUpdateRequestDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BannerException implements BannerExceptioInterface {

    private final JPAQueryFactory jpaQueryFactory;

    // 생성하고자 하는 배너의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateBannerInfo(BannerCreateRequestDto bannerCreateRequestDto) {

        if(bannerCreateRequestDto.getTitle().isEmpty()
        || bannerCreateRequestDto.getLinkUrl().isEmpty() || bannerCreateRequestDto.getExpressionOrder() == 0
        || bannerCreateRequestDto.getStartPostDate().isEmpty() || bannerCreateRequestDto.getEndPostDate().isEmpty()){
                return true;
        }

        return false;
    }

    // 수정하고자 하는 배너의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateBannerInfo(MultipartFile updateBannerImg, BannerUpdateRequestDto bannerUpdateRequestDto) {

        if(updateBannerImg == null || bannerUpdateRequestDto.getTitle().isEmpty()
                || bannerUpdateRequestDto.getLinkUrl().isEmpty() || bannerUpdateRequestDto.getExpressionOrder() == 0
                || bannerUpdateRequestDto.getStartPostDate().isEmpty() || bannerUpdateRequestDto.getEndPostDate().isEmpty()){
            return true;
        }

        return false;
    }
}
