package com.onnury.notice.service;

import com.onnury.exception.notice.NoticeExceptionInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.notice.request.NoticeRequestDto;
import com.onnury.notice.request.NoticeUpdateRequestDto;
import com.onnury.notice.response.NoticeDetailResponseDto;
import com.onnury.notice.response.NoticeResponseDto;
import com.onnury.notice.response.TotalNoticeResponseDto;
import com.onnury.product.response.ProductDetailImageInfoResponseDto;
import com.onnury.query.notice.NoticeQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final NoticeQueryData noticeQueryData;
    private final NoticeExceptionInterface noticeExceptionInterface;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;
    // 관리자 공지사항 작성 service
    public NoticeResponseDto writeNotice(HttpServletRequest request, NoticeRequestDto noticeRequestDto){
        log.info("관리자 공지사항 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            return null;
        }

        // 공지사항 작성 정보 검증
        if(noticeExceptionInterface.checkWriteNoticeInfo(noticeRequestDto)){
            log.info("공지사항 작성 정보가 옳바르지 않음");
            return null;
        }

        return noticeQueryData.writeNotice(noticeRequestDto);
    }


    // 고객 측 공지사항 리스트 조회 service
    public TotalNoticeResponseDto getNoticeList(HttpServletRequest request, int page){
        log.info("고객 측 공지사항 리스트 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            return null;
        }

        return noticeQueryData.getNoticeList(page);
    }


    // 관리자 공지사항 수정 service
    @Transactional
    public NoticeResponseDto updateNotice(HttpServletRequest request,NoticeUpdateRequestDto noticeUpdateRequestDto){
        log.info("관리자 공지사항 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            return null;
        }

        // 공지사항 작성 정보 검증
        if(noticeExceptionInterface.checkUpdateNoticeInfo(noticeUpdateRequestDto)){
            log.info("공지사항 수정 정보가 옳바르지 않음");
            return null;
        }

        return noticeQueryData.updateNotice(noticeUpdateRequestDto);
    }


    // 관리자 공지사항 리스트 호출 service
    public TotalNoticeResponseDto getAdminNoticeList(HttpServletRequest request, int page){
        log.info("관리자 공지사항 리스트 호출 servjice");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            return null;
        }

        return noticeQueryData.getNoticeList(page);
    }


    // 관리자 공지사항 삭제 service
    @Transactional
    public String deleteNotice(HttpServletRequest request, Long noticeId){
        log.info("관리자 공지사항 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request) || jwtTokenProvider.getAdminAccountFromAuthentication() == null) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            return null;
        }

        return noticeQueryData.deleteNotice(noticeId);
    }


    // 공지사항 상세 조회 service
    public NoticeDetailResponseDto getNoticeDetail(HttpServletRequest request, Long noticeId){
        log.info("공지사항 상세 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패 및 관리자 계정 정보 검증 실패");
            return null;
        }

        return noticeQueryData.getNoticeDetail(noticeId);
    }

    public List<ProductDetailImageInfoResponseDto> saveDetailImage(HttpServletRequest request, List<MultipartFile> detailImages) throws IOException {
        log.info("공지사항 이미지 링크 반환 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 전달받은 이미지 파일들을 기준으로 이미지 업로드 처리 후 정보들을 추출하여 HashMap 리스트로 전달
        List<HashMap<String, String>> saveProductDetailInfoImages = mediaUploadInterface.uploadNoticeDetailInfoImage(detailImages);
        List<Media> saveMediaList = new ArrayList<>();

        // 업로드한 이미지들의 정보들을 조회하여 Media 데이터 저장 처리
        for (HashMap<String, String> eachProductDetailInfoImage : saveProductDetailInfoImages) {
            String imgUploadUrl = eachProductDetailInfoImage.get("imgUploadUrl");
            String imgUrl = eachProductDetailInfoImage.get("imgUrl");
            String imgTitle = eachProductDetailInfoImage.get("imgTitle");
            String imgUuidTitle = eachProductDetailInfoImage.get("imgUuidTitle");

            // 제품 상세 정보 이미지 정보들 기입
            Media saveMedia = Media.builder()
                    .imgUploadUrl(imgUploadUrl)
                    .imgUrl(imgUrl)
                    .imgTitle(imgTitle)
                    .imgUuidTitle(imgUuidTitle)
                    .representCheck("N")
                    .type("notices")
                    .mappingContentId(0L)
                    .build();

            saveMediaList.add(saveMedia);
        }

        List<Media> createProductDetailInfoImages = mediaRepository.saveAll(saveMediaList);

        return createProductDetailInfoImages.stream()
                .map(eachDetailInfoImage ->
                        ProductDetailImageInfoResponseDto.builder()
                                .productDetailImageId(eachDetailInfoImage.getMediaId())
                                .type(eachDetailInfoImage.getType())
                                .imgUrl(eachDetailInfoImage.getImgUrl())
                                .build()
                )
                .collect(Collectors.toList());
    }
}
