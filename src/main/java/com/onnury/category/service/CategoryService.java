package com.onnury.category.service;

import com.onnury.category.domain.Category;
import com.onnury.category.repository.CategoryRepository;
import com.onnury.category.request.CategoryCreateRequestDto;
import com.onnury.category.request.CategoryUpdateRequestDto;
import com.onnury.category.response.*;
import com.onnury.common.util.LogUtil;
import com.onnury.exception.category.CategoryException;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUpload;
import com.onnury.member.domain.Member;
import com.onnury.query.category.CategoryQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final JwtTokenException jwtTokenException;
    private final CategoryException categoryException;
    private final CategoryRepository categoryRepository;
    private final MediaRepository mediaRepository;
    private final MediaUpload mediaUpload;
    private final CategoryQueryData categoryQueryData;
    private final JwtTokenProvider jwtTokenProvider;


    // 카테고리 생성
    public CategoryCreateResponseDto createCategory(HttpServletRequest request, MultipartFile categoryImg, CategoryCreateRequestDto categoryInfo, HashMap<String, String> requestParam) throws IOException {
        log.info("대분류 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }


        if( categoryInfo.getCategoryGroup() == 0){
            // 생성하고자 하는 대분류의 정보가 옳바른지 확인
            if (categoryException.checkCreateCategoryInfo(categoryImg, categoryInfo)) {
                log.info("대분류 생성 요청 정보가 옳바르지 않음");
                LogUtil.logError("대분류 생성 요청 정보가 옳바르지 않음", request, requestParam, categoryInfo);
                return null;
            }
            String motherCode = "";
            if (categoryInfo.getCategoryGroup() == 1){
                motherCode = categoryInfo.getMotherCode();
            }
            // 업로드한 대분류 이미지 정보
            HashMap<String, String> uploadCategoryImg = mediaUpload.uploadCategoryImage(categoryImg);
            String uuid = UUID.randomUUID().toString();
            // 배너 정보 저장
            Category category = Category.builder()
                    .categoryGroup(categoryInfo.getCategoryGroup())
                    .categoryName(categoryInfo.getCategoryName())
                    .classficationCode(uuid)   // 난수 생성해야함
                    .motherCode(motherCode)
                    .imgUrl(uploadCategoryImg.get("imgUrl"))
                    .build();

            categoryRepository.save(category);

            // 이미지 데이터 저장
            Media media = Media.builder()
                    .imgUploadUrl(uploadCategoryImg.get("imgUploadUrl"))
                    .imgUrl(uploadCategoryImg.get("imgUrl"))
                    .imgTitle(uploadCategoryImg.get("imgTitle"))
                    .imgUuidTitle(uploadCategoryImg.get("imgUuidTitle"))
                    .representCheck("N")
                    .type("category")
                    .mappingContentId(category.getCategoryId())
                    .build();

            mediaRepository.save(media);

            return CategoryCreateResponseDto.builder()
                    .categoryGroup(category.getCategoryGroup())
                    .categoryName(category.getCategoryName())
                    .classificationCode(category.getClassficationCode())   // 난수 생성해야함
                    .motherCode(category.getMotherCode())
                    .imgUrl(category.getImgUrl())
                    .type(media.getType())
                    .build();
        }else {
            // 생성하고자 하는 소분류의 정보가 옳바른지 확인
            if (categoryException.checkCreateCategoryInfo2(categoryInfo)) {
                log.info("소분류 생성 요청 정보가 옳바르지 않음");
                LogUtil.logError("소분류 생성 요청 정보가 옳바르지 않음", request, categoryInfo);
                return null;
            }

            // 자코드를 위한 난수 생성
            String uuid = UUID.randomUUID().toString();
            // 배너 정보 저장
            Category category = Category.builder()
                    .categoryGroup(categoryInfo.getCategoryGroup())
                    .categoryName(categoryInfo.getCategoryName())
                    .classficationCode(uuid)
                    .motherCode(categoryInfo.getMotherCode())
                    .imgUrl("")
                    .build();

            categoryRepository.save(category);


            return CategoryCreateResponseDto.builder()
                    .categoryGroup(category.getCategoryGroup())
                    .categoryName(category.getCategoryName())
                    .classificationCode(category.getClassficationCode())
                    .motherCode(category.getMotherCode())
                    .imgUrl(category.getImgUrl())
                    .type("category")
                    .build();
        }
    }


    // 카테고리 수정
    @Transactional
    public CategoryUpdateResponseDto updateCategory(HttpServletRequest request, Long categoryId, MultipartFile categoryImg, CategoryUpdateRequestDto updatecaegoryInfo, HashMap<String, String> requestParam) throws IOException {
        log.info("카테고리 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 수정하고자 하는 정보가 옳바른지 확인
        if (categoryException.checkUpdateCategoryInfo(updatecaegoryInfo)) {
            log.info("카테고리 수정 요청 정보가 옳바르지 않음");
            LogUtil.logError("카테고리 수정 요청 정보가 옳바르지 않음", request, updatecaegoryInfo, requestParam);
            return null;
        }

        // 수정한 카테고리 정보 추출
        Category newCategory = categoryQueryData.updateCategory(categoryId, categoryImg, updatecaegoryInfo);

        return CategoryUpdateResponseDto.builder()
                .categoryGroup(newCategory.getCategoryGroup())
                .categoryName(newCategory.getCategoryName())
                .classificationCode(newCategory.getClassficationCode())
                .motherCode(newCategory.getMotherCode())
                .imgUrl(newCategory.getImgUrl())
                .build();
    }


    // 카테고리 삭제
    @Transactional
    public boolean deleteCategory(HttpServletRequest request, Long deleteCategoryId) {
        log.info("카테고리 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return true;
        }

        // 상품 개발후 해당 카테고리 상품이 있는지 검증!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return categoryQueryData.deleteCategory(deleteCategoryId);
    }


    // 관리자 대분류 리스트업
    public List<CategoryDataResponseDto> listUpCategoryOneDepth(HttpServletRequest request) {
        log.info("관리자 대분류 리스트업 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return categoryQueryData.listUpCategoryOneDepth();
    }


    // 관리자 중분류 리스트업
    public List<CategoryDataResponseDto> listUpCategoryTwoDepth(HttpServletRequest request, String MotherCode) {
        log.info("관리자 중분류 리스트업 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return categoryQueryData.listUpCategoryTwoDepth(MotherCode);
    }


    // 관리자 소분류 리스트업
    public List<CategoryDataResponseDto> listUpCategoryThreeDepth(HttpServletRequest request, String MotherCode) {
        log.info("관리자 소분류 리스트업 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return categoryQueryData.listUpCategoryThreeDepth(MotherCode);
    }


    // 네비게이션 카테고리 리스트 service
    public List<UpCategoryInfoResponseDto> navigationCategory(HttpServletRequest request){
        log.info("네비게이션 카테고리 리스트 service");
        if(request.getHeader("RefreshToken") != null) {
            // 정합성이 검증된 토큰인지 확인
            if (jwtTokenException.checkAccessToken(request)) {
                log.info("토큰 정합성 검증 실패");
                LogUtil.logError("토큰 정합성 검증 실패", request);
                return null;
            }

            Member authMember = jwtTokenProvider.getMemberFromAuthentication();

            return categoryQueryData.getNavigationCategories(authMember.getType());
        } else {
            return categoryQueryData.getNavigationCategories("C");
        }
    }


    // 메인 페이지 대분류 카테고리 리스트 호출 service
    public List<CategoryDataResponseDto> mainPageQuickUpCategory(){
        log.info("메인 페이지 대분류 카테고리 리스트 호출 service");

        return categoryQueryData.mainPageQuickUpCategory();
    }


    // 제품 페이지 대분류 기준 중분류 카테고리 리스트 조회 service
    public List<CategoryDataResponseDto> middleCategoryByUpCategory(Long upCategoryId){
        log.info("제품 페이지 대분류 기준 중분류 카테고리 리스트 조회 service");

        return categoryQueryData.middleCategoryByUpCategory(upCategoryId);
    }
}
