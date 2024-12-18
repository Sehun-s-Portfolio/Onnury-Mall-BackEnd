package com.onnury.exception.banner;

import com.onnury.banner.request.BannerCreateRequestDto;
import com.onnury.banner.request.BannerUpdateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface BannerExceptioInterface {

    // 생성하고자 하는 배너의 정보가 옳바른지 확인
    boolean checkCreateBannerInfo(BannerCreateRequestDto bannerCreateRequestDto);

    // 수정하고자 하는 배너의 정보가 옳바른지 확인
    boolean checkUpdateBannerInfo(MultipartFile updateBannerImg, BannerUpdateRequestDto bannerUpdateRequestDto);
}
