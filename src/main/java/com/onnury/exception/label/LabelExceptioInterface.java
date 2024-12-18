package com.onnury.exception.label;

import com.onnury.banner.request.BannerUpdateRequestDto;
import com.onnury.label.request.LabelCreateRequestDto;
import com.onnury.label.request.LabelUpdateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface LabelExceptioInterface {

    // 생성하고자 하는 배너의 정보가 옳바른지 확인
    boolean checkCreateLabelInfo(MultipartFile labelImg, LabelCreateRequestDto labelCreateRequestDto);

    // 수정하고자 하는 배너의 정보가 옳바른지 확인
    boolean checkUpdateLabelInfo(MultipartFile labelImg, LabelUpdateRequestDto labelUpdateRequestDto);
}
