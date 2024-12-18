package com.onnury.inquiry.service;

import com.onnury.exception.inquiry.FaqExceptioInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.inquiry.domain.Faq;
import com.onnury.inquiry.repository.FaqRepository;
import com.onnury.inquiry.request.FaqCreateRequestDto;
import com.onnury.inquiry.request.FaqUpdateRequestDto;
import com.onnury.inquiry.response.FaqCreateResponseDto;
import com.onnury.inquiry.response.FaqListUpResponseDto;
import com.onnury.inquiry.response.FaqUpdateResponseDto;
import com.onnury.inquiry.response.TotalFaqListResponseDto;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.product.response.ProductDetailImageInfoResponseDto;
import com.onnury.query.inquiry.FaqQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
public class FaqService {

    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final FaqExceptioInterface faqExceptioInterface;
    private final FaqRepository faqRepository;
    private final FaqQueryData faqQueryData;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;
    //FAQ 생성
    public FaqCreateResponseDto createFaq(HttpServletRequest request, FaqCreateRequestDto faqInfo) {
        log.info("FAQ 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }
        // 생성하고자 하는 FAQ 정보가 옳바른지 확인
        if (faqExceptioInterface.checkCreateFaqInfo(faqInfo)) {
            log.info("FAQ 생성 요청 정보가 옳바르지 않음");
            return null;
        }
        //브랜드 정보 저장
        Faq faq = Faq.builder()
                .type(faqInfo.getType())
                .question(faqInfo.getQuestion())
                .answer(faqInfo.getAnswer())
                .expressCheck(faqInfo.getExpressCheck())
                .build();
        faqRepository.save(faq);

        return FaqCreateResponseDto.builder()
                .faqId(faq.getFaqId())
                .type(faqInfo.getType())
                .question(faqInfo.getQuestion())
                .answer(faqInfo.getAnswer())
                .build();
    }

    //FAQ 수정
    public FaqUpdateResponseDto updateFaq(HttpServletRequest request, Long Faqid, FaqUpdateRequestDto faqInfo) throws IOException {
        log.info("FAQ 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }
        // 수정하고자 하는 공급사의 정보가 옳바른지 확인
        if (faqExceptioInterface.checkUpdateFaqInfo(faqInfo)) {
            log.info("FAQ 수정 요청 정보가 옳바르지 않음");
            return null;
        }
        //수정한 공급사 정보 추출
         Faq newFaq = faqQueryData.updateFaq(Faqid, faqInfo);

        return FaqUpdateResponseDto.builder()
                .faqId(newFaq.getFaqId())
                .type(newFaq.getType())
                .question(newFaq.getQuestion())
                .answer(newFaq.getAnswer())
                .expressCheck(newFaq.getExpressCheck())
                .build();

    }

    //FAQ 삭제
    @Transactional
    public boolean deleteFaq(HttpServletRequest request, Long Faqid) {
        log.info("FAQ 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return true;
        }

        return faqQueryData.deleteFaq(Faqid);
    }

    // 관리자 FAQ 페이지 리스트업
    public FaqListUpResponseDto listUpFaq(HttpServletRequest request, int page, String type) {
        log.info("관리자 브랜드 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        return faqQueryData.listUpFaq(page,type);
    }


    // 고객 FAQ 리스트 조회 service
    public TotalFaqListResponseDto getFaqList(HttpServletRequest request, int page, String type){
        log.info("고객 FAQ 리스트 조회 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        return faqQueryData.getFaqList(page, type);
    }

    public List<ProductDetailImageInfoResponseDto> saveDetailImage(HttpServletRequest request, List<MultipartFile> detailImages) throws IOException {
        log.info("공지사항 이미지 링크 반환 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 전달받은 이미지 파일들을 기준으로 이미지 업로드 처리 후 정보들을 추출하여 HashMap 리스트로 전달
        List<HashMap<String, String>> saveProductDetailInfoImages = mediaUploadInterface.uploadFaqDetailInfoImage(detailImages);
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
                    .type("faq")
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

