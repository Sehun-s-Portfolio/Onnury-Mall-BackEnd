package com.onnury.media.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Component
public interface MediaUploadInterface {
    /** 업로드 될 경로 **/
    String getFullPath(String filename, String fileDir);

    /** 업로드 시킬 파일 (로컬 환경) **/
    //HashMap<String, String> uploadImage(MultipartFile multipartFile) throws IOException;

    List<HashMap<String, String>> uploadBannerImage(HashMap<String, MultipartFile> bannerImages) throws IOException;

    // 라벨 이미지
    HashMap<String, String> uploadLabelImage(MultipartFile multipartFile) throws IOException;

    // 카테고리 이미지
    HashMap<String, String> uploadCategoryImage(MultipartFile multipartFile) throws IOException;

    // 제품 이미지
    List<HashMap<String, String>> uploadProductImage(List<MultipartFile> multipartFile) throws IOException;

    // 제품 상세 정보 이미지
    List<HashMap<String, String>> uploadProductDetailInfoImage(List<MultipartFile> multipartFile) throws IOException;

    // 공지사항 정보 이미지
    List<HashMap<String, String>> uploadNoticeDetailInfoImage(List<MultipartFile> multipartFile) throws IOException;

    // FAQ 정보 이미지
    List<HashMap<String, String>> uploadFaqDetailInfoImage(List<MultipartFile> multipartFile) throws IOException;

    // 문의 파일 업로드
    List<HashMap<String, String>> uploadInquiryFiles(List<MultipartFile> multipartFile) throws IOException;

    // 브랜드 이미지 업로드
    List<HashMap<String, String>> uploadBrandImage(HashMap<String, MultipartFile> brandImages) throws IOException;

    /** 난수화한 업로드할 파일 이름 **/
    String createServerFileName(String originalFilename);

    /** 업로드 파일 확장자 정보 추출 **/
    String extractExt(String originalFilename);
}
