package com.onnury.brand.service;

import com.onnury.brand.domain.Brand;
import com.onnury.brand.repository.BrandRepository;
import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.brand.response.*;
import com.onnury.exception.brand.BrandExceptioInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.member.domain.Member;
import com.onnury.query.brand.BrandQueryData;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class BrandService {

    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final BrandExceptioInterface brandExceptioInterface;
    private final BrandRepository brandRepository;
    private final BrandQueryData brandQueryData;
    private final MediaUploadInterface mediaUploadInterface;
    private final MediaRepository mediaRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 브랜드 생성
    public BrandCreateResponseDto createBrand(HttpServletRequest request, BrandCreateRequestDto brandInfo, MultipartFile brandImage) throws IOException {
        log.info("브랜드 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }
        // 생성하고자 하는 브랜드 정보가 옳바른지 확인
        if (brandExceptioInterface.checkCreateBrandInfo(brandInfo)) {
            log.info("브랜드 생성 요청 정보가 옳바르지 않음");
            return null;
        }

        //브랜드 정보 저장
        Brand brand = Brand.builder()
                .brandTitle(brandInfo.getBrandTitle())
                .status("Y")
                .build();

        brandRepository.save(brand);

        // 브랜드 이미지들 HashMap 담기
        HashMap<String, MultipartFile> brandImagesInfo = new HashMap<>();
        brandImagesInfo.put("brand", brandImage);

        // 브랜드 관련 이미지들 업로드 후 List 화
        List<HashMap<String, String>> uploadBrandImages = mediaUploadInterface.uploadBrandImage(brandImagesInfo);
        List<BrandMediaResponseDto> brandImages = new ArrayList<>();

        // 업로드된 브랜드 이미지들마다 데이터 처리
        for (HashMap<String, String> eachBrandImgInfo : uploadBrandImages) {
            // 이미지 데이터 저장
            Media media = Media.builder()
                    .imgUploadUrl(eachBrandImgInfo.get("imgUploadUrl"))
                    .imgUrl(eachBrandImgInfo.get("imgUrl"))
                    .imgTitle(eachBrandImgInfo.get("imgTitle"))
                    .imgUuidTitle(eachBrandImgInfo.get("imgUuidTitle"))
                    .representCheck("N")
                    .type(eachBrandImgInfo.get("purpose"))
                    .mappingContentId(brand.getBrandId())
                    .build();

            mediaRepository.save(media);

            brandImages.add(
                    BrandMediaResponseDto.builder()
                            .mediaId(media.getMediaId())
                            .imgUploadUrl(media.getImgUploadUrl())
                            .imgUrl(media.getImgUrl())
                            .imgTitle(media.getImgTitle())
                            .imgUuidTitle(media.getImgUuidTitle())
                            .type(media.getType())
                            .mappingContentId(media.getMappingContentId())
                            .build()
            );
        }

        return BrandCreateResponseDto.builder()
                .brandTitle(brand.getBrandTitle())
                .status(brand.getStatus())
                .brandImages(brandImages)
                .build();
    }


    // 브랜드 수정
    public BrandUpdateResponseDto updateBrand(HttpServletRequest request, Long brandId, BrandUpdateRequestDto brandInfo, MultipartFile updateBrandImage) throws IOException {
        log.info("공급사 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 수정하고자 하는 공급사의 정보가 옳바른지 확인
        if (brandExceptioInterface.checkUpdateBrandInfo(brandInfo)) {
            log.info("공급사 수정 요청 정보가 옳바르지 않음");
            return null;
        }

        // 수정한 공급사 정보 반환
        return brandQueryData.updateBrand(brandId, brandInfo, updateBrandImage);
    }


    // 브랜드 삭제
    @Transactional
    public boolean deleteBrand(HttpServletRequest request, Long brandid) {
        log.info("브랜드 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return true;
        }

        return brandQueryData.deleteBrand(brandid);
    }


    // 관리자 브랜드 페이지 리스트업
    public BrandListUpResponseDto listUpBrand(HttpServletRequest request, int page) {
        log.info("관리자 브랜드 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        return brandQueryData.listUpBrand(page);
    }


    // 메인 페이지 브랜드 리스트 service
    public List<MainPageBrandResponseDto> mainPageBrandList(HttpServletRequest request){
        log.info("관리자 브랜드 리스트업 페이지 service");
        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenExceptionInterface.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                return null;
            }

            Member authMember = jwtTokenProvider.getMemberFromAuthentication();

            return brandQueryData.mainPageBrandList(authMember.getType());
        } else {
            return brandQueryData.mainPageBrandList("C");
        }
    }
}

