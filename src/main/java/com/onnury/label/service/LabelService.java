package com.onnury.label.service;

import com.onnury.exception.label.LabelExceptioInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.label.domain.Label;
import com.onnury.label.repository.LabelRepository;
import com.onnury.label.request.LabelCreateRequestDto;
import com.onnury.label.request.LabelUpdateRequestDto;
import com.onnury.label.response.LabelCreateResponseDto;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.label.response.LabelListUpResponseDto;
import com.onnury.label.response.LabelUpdateResponseDto;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.query.banner.BannerQueryData;
import com.onnury.query.label.LabelQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LabelService {

    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final LabelExceptioInterface labelExceptioInterface;
    private final LabelRepository labelRepository;
    private final MediaRepository mediaRepository;
    private final MediaUploadInterface mediaUploadInterface;
    private final LabelQueryData labelQueryData;

    // 라벨 생성 service
    public LabelCreateResponseDto createLabel(HttpServletRequest request, MultipartFile labelImg, LabelCreateRequestDto labelInfo) throws IOException {
        log.info("라벨 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 생성하고자 하는 라벨의 정보가 옳바른지 확인
        if (labelExceptioInterface.checkCreateLabelInfo(labelImg, labelInfo)) {
            log.info("라벨 생성 요청 정보가 옳바르지 않음");
            return null;
        }

        // 업로드한 배너 이미지 정보
        HashMap<String, String> uploadBannerImg = mediaUploadInterface.uploadLabelImage(labelImg);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        LocalDateTime startPostDate = LocalDateTime.parse(labelInfo.getStartPostDate() + " 00:00:00", formatter); // 추출한 날짜 데이터에 포맷 형식 적용
        LocalDateTime endPostDate = LocalDateTime.parse(labelInfo.getStartPostDate() + " 23:59:59", formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 배너 정보 저장
        Label label = Label.builder()
                .labelTitle(labelInfo.getLabelTitle())
                .colorCode("DEFAULT")
                .startPostDate(startPostDate)
                .endPostDate(endPostDate)
                .imgUrl(uploadBannerImg.get("imgUrl"))
                .topExpression(labelInfo.getTopExpression())
                .build();

        labelRepository.save(label);

        // 이미지 데이터 저장
        Media media = Media.builder()
                .imgUploadUrl(uploadBannerImg.get("imgUploadUrl"))
                .imgUrl(uploadBannerImg.get("imgUrl"))
                .imgTitle(uploadBannerImg.get("imgTitle"))
                .imgUuidTitle(uploadBannerImg.get("imgUuidTitle"))
                .representCheck("N")
                .type("label")
                .mappingContentId(label.getLabelId())
                .build();

        mediaRepository.save(media);

        return LabelCreateResponseDto.builder()
                .labelTitle(label.getLabelTitle())
                .colorCode(label.getColorCode())
                .startPostDate(label.getStartPostDate())
                .endPostDate(label.getEndPostDate())
                .imgUrl(uploadBannerImg.get("imgUrl"))
                .topExpression(label.getTopExpression())
                .build();
    }


    // 라벨 수정 service
    @Transactional
    public LabelUpdateResponseDto updateLabel(HttpServletRequest request, Long labelId, MultipartFile updateLabelImg, LabelUpdateRequestDto updateLabelInfo) throws IOException {
        log.info("라벨 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 수정한 배너 정보 추출
        Label newLabel = labelQueryData.updateLabel(labelId, updateLabelImg, updateLabelInfo);

        return LabelUpdateResponseDto.builder()
                .labelTitle(newLabel.getLabelTitle())
                .colorCode(newLabel.getColorCode())
                .startPostDate(newLabel.getStartPostDate())
                .endPostDate(newLabel.getEndPostDate())
                .imgUrl(newLabel.getImgUrl())
                .topExpression(newLabel.getTopExpression())
                .build();
    }


    // 라벨 삭제 service
    @Transactional
    public boolean deleteLabel(HttpServletRequest request, Long deleteLabelId) {
        log.info("라벨 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return true;
        }

        return labelQueryData.deleteLabel(deleteLabelId);
    }


    // 관리자 라벨 페이지 리스트업 service
    public LabelListUpResponseDto listUpLabel(HttpServletRequest request, int page) {
        log.info("관리자 라벨 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        return labelQueryData.listUpLabel(page);
    }


    // 상위 노출 라벨 리스트 호출 service
    public List<LabelDataResponseDto> topExpressionLabelList(HttpServletRequest request){
        log.info("상위 노출 라벨 리스트 호출 service");



        return labelQueryData.topExpressionLabelList();
    }
}
