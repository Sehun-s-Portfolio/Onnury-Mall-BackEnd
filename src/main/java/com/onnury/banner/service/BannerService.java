package com.onnury.banner.service;

import com.onnury.banner.domain.Banner;
import com.onnury.banner.repository.BannerRepository;
import com.onnury.banner.request.BannerCreateRequestDto;
import com.onnury.banner.request.BannerUpdateRequestDto;
import com.onnury.banner.response.*;
import com.onnury.common.util.LogUtil;
import com.onnury.exception.banner.BannerException;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUpload;
import com.onnury.query.banner.BannerQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BannerService {

    private final JwtTokenException jwtTokenException;
    private final BannerException bannerException;
    private final BannerRepository bannerRepository;
    private final MediaRepository mediaRepository;
    private final MediaUpload mediaUpload;
    private final BannerQueryData bannerQueryData;

    // 배너 생성
    public BannerCreateResponseDto createBanner(
            HttpServletRequest request, MultipartFile appBannerImg, MultipartFile webBannerImg, MultipartFile slideBannerImg, BannerCreateRequestDto bannerInfo, HashMap<String, String> requestParam) throws IOException {
        log.info("배너 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 배너 등록 이미지 검증
        if(appBannerImg == null || webBannerImg == null){
            log.info("배너 이미지 정보가 옳바르지 않음");
            LogUtil.logError("배너 이미지 정보가 옳바르지 않음", request, requestParam);
            return null;
        }

        // 생성하고자 하는 배너의 정보가 옳바른지 확인
        if (bannerException.checkCreateBannerInfo(bannerInfo)) {
            log.info("배너 생성 요청 정보가 옳바르지 않음");
            LogUtil.logError("배너 생성 요청 정보가 옳바르지 않음", request, bannerInfo);
            return null;
        }

        HashMap<String, MultipartFile> bannerCreateInfo = new HashMap<>();
        bannerCreateInfo.put("app", appBannerImg);
        bannerCreateInfo.put("web", webBannerImg);
        bannerCreateInfo.put("slide", slideBannerImg);

        // 업로드한 배너 이미지 정보
        List<HashMap<String, String>> uploadBannerImg = mediaUpload.uploadBannerImage(bannerCreateInfo);

        // 일자 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        // 프로모션 배너 게시 시작일, 종료일
        String startPostDate = bannerInfo.getStartPostDate() + " 00:00:00";
        String endPostDate = bannerInfo.getEndPostDate() + " 23:59:59";

        // 게시 시작일, 종료일 LocalDateTime 변환
        LocalDateTime convertStartPostDate = LocalDateTime.parse(startPostDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
        LocalDateTime convertEndPostDate = LocalDateTime.parse(endPostDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 노출 여부 초기값 설정
        String expressionCheck = "N";

        // 만약 게시일이 현재 일자 범위 내에 있으면 노출 여부 Y
        if(LocalDateTime.now().isAfter(convertStartPostDate) && LocalDateTime.now().isBefore(convertEndPostDate)){
            expressionCheck = "Y";
        }

        // 배너 정보 저장
        Banner banner = Banner.builder()
                .title(bannerInfo.getTitle())
                .linkUrl(bannerInfo.getLinkUrl())
                .expressionOrder(bannerInfo.getExpressionOrder())
                .expressionCheck(expressionCheck)
                .startPostDate(startPostDate)
                .endPostDate(endPostDate)
                .build();

        bannerRepository.save(banner);

        List<BannerMediaResponseDto> responseBannerImages = new ArrayList<>();

        for(HashMap<String, String> eachBannerImgInfo : uploadBannerImg){
            // 이미지 데이터 저장
            Media media = Media.builder()
                    .imgUploadUrl(eachBannerImgInfo.get("imgUploadUrl"))
                    .imgUrl(eachBannerImgInfo.get("imgUrl"))
                    .imgTitle(eachBannerImgInfo.get("imgTitle"))
                    .imgUuidTitle(eachBannerImgInfo.get("imgUuidTitle"))
                    .representCheck("N")
                    .type(eachBannerImgInfo.get("purpose") + "banner")
                    .mappingContentId(banner.getBannerId())
                    .build();

            mediaRepository.save(media);

            responseBannerImages.add(
                    BannerMediaResponseDto.builder()
                            .mediaId(media.getMediaId())
                            .imgUploadUrl(media.getImgUploadUrl())
                            .imgUrl(media.getImgUrl())
                            .imgTitle(media.getImgTitle())
                            .imgUuidTitle(media.getImgUuidTitle())
                            .type(media.getType())
                            .build()
            );
        }

        return BannerCreateResponseDto.builder()
                .title(banner.getTitle())
                .linkUrl(banner.getLinkUrl())
                .expressionOrder(banner.getExpressionOrder())
                .startPostDate(banner.getStartPostDate())
                .endPostDate(banner.getEndPostDate())
                .bannerImages(responseBannerImages)
                .build();
    }


    // 배너 수정
    @Transactional
    public BannerUpdateResponseDto updateBanner(
            HttpServletRequest request, Long bannerId, MultipartFile updateAppBannerImg, MultipartFile updateWebBannerImg, MultipartFile updateSlideBannerImg, BannerUpdateRequestDto updateBannerInfo, HashMap<String, String> requestParam) throws IOException {
        log.info("배너 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 수정한 배너 정보 추출
        return bannerQueryData.updateBanner(bannerId, updateAppBannerImg, updateWebBannerImg, updateSlideBannerImg, updateBannerInfo);
    }


    // 배너 삭제
    @Transactional
    public boolean deleteBanner(HttpServletRequest request, Long deleteBannerId, HashMap<String, String> requestParam) {
        log.info("배너 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request, requestParam);
            return true;
        }

        return bannerQueryData.deleteBanner(deleteBannerId);
    }


    // 관리자 배너 페이지 리스트업
    public TotalBannerResponseDto listUpBanner(HttpServletRequest request, int page, HashMap<String, String> requestParam) {
        log.info("관리자 배너 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return bannerQueryData.adminGetTotalMainBannerList(page);
    }


    // 메인 페이지 배너 리스트 service
    public TotalMainPageBannerResponseDto mainPageBannerList(HttpServletRequest request){
        log.info("메인 페이지 배너 리스트 service");
        log.info("접속 중인 플랫폼 : {}", request.getHeader("user-agent"));

        return bannerQueryData.listUpBanner(request);
    }


    // 프로모션 배너 생성 service
    public PromotionBannerCreateResponseDto createPromotionBanner(HttpServletRequest request, MultipartFile bannerImg, BannerCreateRequestDto bannerInfo, HashMap<String, String> requestParam) throws IOException {
        log.info("프로모션 배너 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 배너 등록 이미지 검증
        if(bannerImg == null){
            log.info("배너 이미지 정보가 옳바르지 않음");
            LogUtil.logError("배너 이미지 정보가 옳바르지 않음", request, requestParam);
            return null;
        }

        // 생성하고자 하는 배너의 정보가 옳바른지 확인
        if (bannerException.checkCreateBannerInfo(bannerInfo)) {
            log.info("배너 생성 요청 정보가 옳바르지 않음");
            LogUtil.logError("배너 생성 요청 정보가 옳바르지 않음", request, bannerInfo);
            return null;
        }

        // 프로모션 이미지를 업로드하기 위해 HashMap에 담기
        HashMap<String, MultipartFile> bannerCreateInfo = new HashMap<>();
        bannerCreateInfo.put("promotion", bannerImg);

        // 업로드한 배너 이미지 정보
        List<HashMap<String, String>> uploadBannerImg = mediaUpload.uploadBannerImage(bannerCreateInfo);

        // 일자 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식
        // 프로모션 배너 게시 시작일, 종료일
        String startPostDate = bannerInfo.getStartPostDate() + " 00:00:00";
        String endPostDate = bannerInfo.getEndPostDate() + " 23:59:59";

        // 게시 시작일, 종료일 LocalDateTime 변환
        LocalDateTime convertStartPostDate = LocalDateTime.parse(startPostDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
        LocalDateTime convertEndPostDate = LocalDateTime.parse(endPostDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

        // 노출 여부 초기값 설정
        String expressionCheck = "N";

        // 만약 게시일이 현재 일자 범위 내에 있으면 노출 여부 Y
        if(LocalDateTime.now().isAfter(convertStartPostDate) && LocalDateTime.now().isBefore(convertEndPostDate)){
            expressionCheck = "Y";
        }

        // 배너 정보 저장
        Banner promotionBanner = Banner.builder()
                .title(bannerInfo.getTitle())
                .linkUrl(bannerInfo.getLinkUrl())
                .expressionOrder(bannerInfo.getExpressionOrder())
                .expressionCheck(expressionCheck)
                .startPostDate(startPostDate)
                .endPostDate(endPostDate)
                .build();

        bannerRepository.save(promotionBanner);

        // 실제 업로드된 프로모션 배너 이미지들 HashMap화
        HashMap<String, String> promotionBannerImg = uploadBannerImg.get(0);

        // 이미지 데이터 저장
        Media promotionMedia = Media.builder()
                .imgUploadUrl(promotionBannerImg.get("imgUploadUrl"))
                .imgUrl(promotionBannerImg.get("imgUrl"))
                .imgTitle(promotionBannerImg.get("imgTitle"))
                .imgUuidTitle(promotionBannerImg.get("imgUuidTitle"))
                .representCheck("N")
                .type(promotionBannerImg.get("purpose") + "banner")
                .mappingContentId(promotionBanner.getBannerId())
                .build();

        mediaRepository.save(promotionMedia);

        // 저장된 프로모션 이미지 데이터 반환 결과 객체에 저장
        BannerMediaResponseDto promotionMediaInfo = BannerMediaResponseDto.builder()
                .mediaId(promotionMedia.getMediaId())
                .imgUploadUrl(promotionMedia.getImgUploadUrl())
                .imgUrl(promotionMedia.getImgUrl())
                .imgTitle(promotionMedia.getImgTitle())
                .imgUuidTitle(promotionMedia.getImgUuidTitle())
                .type(promotionMedia.getType())
                .mappingContentId(promotionMedia.getMappingContentId())
                .build();

        return PromotionBannerCreateResponseDto.builder()
                .title(promotionBanner.getTitle())
                .linkUrl(promotionBanner.getLinkUrl())
                .expressionOrder(promotionBanner.getExpressionOrder())
                .startPostDate(promotionBanner.getStartPostDate())
                .endPostDate(promotionBanner.getEndPostDate())
                .bannerImage(promotionMediaInfo)
                .build();
    }


    // 프로모션 배너 수정 service
    @Transactional
    public PromotionBannerUpdateResponseDto updatePromotionBanner(HttpServletRequest request, Long bannerId, MultipartFile updateBannerImg, BannerUpdateRequestDto updateBannerInfo, HashMap<String, String> requestParam) throws IOException {
        log.info("프로모션 배너 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 수정한 배너 정보 추출
        return bannerQueryData.updatePromotionBanner(bannerId, updateBannerImg, updateBannerInfo);
    }


    // 메인 페이지 프로모션 배너 리스트 service
    public TotalMainPagePromotionBannerResponseDto mainPagePromotionBannerList(){
        log.info("메인 페이지 프로모션 배너 리스트 service");

        return bannerQueryData.listUpPromotionBanner();
    }


    // 프로모션 배너 삭제 service
    @Transactional
    public boolean deletePromotionBanner(HttpServletRequest request, Long deleteBannerId, HashMap<String, String> requestParam) {
        log.info("배너 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request, requestParam);
            return true;
        }

        return bannerQueryData.deletePromotionBanner(deleteBannerId);
    }
}
