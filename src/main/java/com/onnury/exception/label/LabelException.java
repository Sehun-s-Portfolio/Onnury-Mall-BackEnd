package com.onnury.exception.label;

import com.onnury.banner.request.BannerUpdateRequestDto;
import com.onnury.label.request.LabelCreateRequestDto;
import com.onnury.label.request.LabelUpdateRequestDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class LabelException implements LabelExceptioInterface {

    private final JPAQueryFactory jpaQueryFactory;

    // 생성하고자 하는 배너의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateLabelInfo(MultipartFile labelImg, LabelCreateRequestDto labelCreateRequestDto) {

        if (labelImg == null || labelCreateRequestDto.getLabelTitle().isEmpty()
                || labelCreateRequestDto.getStartPostDate().toString().isEmpty()
                || labelCreateRequestDto.getEndPostDate().toString().isEmpty()) {
            return true;
        }

        return false;
    }

    // 수정하고자 하는 배너의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateLabelInfo(MultipartFile labelImg, LabelUpdateRequestDto labelUpdateRequestDto) {

        if (labelImg == null || labelUpdateRequestDto.getLabelTitle().isEmpty()
                || labelUpdateRequestDto.getStartPostDate().toString().isEmpty()
                || labelUpdateRequestDto.getEndPostDate().toString().isEmpty()) {
            return true;
        }

        return false;
    }
}
