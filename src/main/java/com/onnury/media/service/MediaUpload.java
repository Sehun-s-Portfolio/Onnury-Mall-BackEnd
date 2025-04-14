package com.onnury.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class MediaUpload implements MediaUploadInterface {

    @Value("${deploy.upload.file.path}")
    private String uploadFilePath;

    @Value("${deploy.call.file.path}")
    private String deployCallPath;

    // 업로드 될 경로
    @Override
    public String getFullPath(String fileDir, String filename) {
        // 파일명과 업로드될 경로 반환
        return fileDir + filename;
    }

    // 배너 이미지 업로드
    @Override
    public List<HashMap<String, String>> uploadBannerImage(HashMap<String, MultipartFile> bannerImages) throws IOException {

        List<HashMap<String, String>> bannerUploadInfo = new ArrayList<>();

        // 앱 배너 이미지 업로드
        if (bannerImages.get("app") != null) {
            MultipartFile appBannerImage = bannerImages.get("app");

            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String appFilename = appBannerImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadAppFileName = createServerFileName(appFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File appFile = new File(getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadAppFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            appBannerImage.transferTo(appFile); // 배포 서버 파일 등록

            HashMap<String, String> appResult = new HashMap<>();
            appResult.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadAppFileName));
            appResult.put("imgUrl", deployCallPath + "banner" + File.separator + serverUploadAppFileName);
            appResult.put("imgTitle", appFilename);
            appResult.put("imgUuidTitle", serverUploadAppFileName);
            appResult.put("purpose", "app");

            bannerUploadInfo.add(appResult);
        }

        // 웹 배너 이미지 업로드
        if (bannerImages.get("web") != null) {
            MultipartFile webBannerImage = bannerImages.get("web");

            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String webFilename = webBannerImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadWebFileName = createServerFileName(webFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File webFile = new File(getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadWebFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            webBannerImage.transferTo(webFile); // 배포 서버 파일 등록

            HashMap<String, String> webResult = new HashMap<>();
            webResult.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadWebFileName));
            webResult.put("imgUrl", deployCallPath + "banner" + File.separator + serverUploadWebFileName);
            webResult.put("imgTitle", webFilename);
            webResult.put("imgUuidTitle", serverUploadWebFileName);
            webResult.put("purpose", "web");

            bannerUploadInfo.add(webResult);
        }

        // 슬라이드 배너 이미지 업로드
        if (bannerImages.get("slide") != null) {
            MultipartFile slideBannerImage = bannerImages.get("slide");

            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String slideFilename = slideBannerImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadSlideFileName = createServerFileName(slideFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File webFile = new File(getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadSlideFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            slideBannerImage.transferTo(webFile); // 배포 서버 파일 등록

            HashMap<String, String> webResult = new HashMap<>();
            webResult.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadSlideFileName));
            webResult.put("imgUrl", deployCallPath + "banner" + File.separator + serverUploadSlideFileName);
            webResult.put("imgTitle", slideFilename);
            webResult.put("imgUuidTitle", serverUploadSlideFileName);
            webResult.put("purpose", "slide");

            bannerUploadInfo.add(webResult);
        }

        // 프로모션 배너 이미지 업로드
        if (bannerImages.get("promotion") != null) {
            MultipartFile promotionBannerImage = bannerImages.get("promotion");

            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String promotionBannerFileName = promotionBannerImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadPromotionFileName = createServerFileName(promotionBannerFileName);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File promotionFile = new File(getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadPromotionFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            promotionBannerImage.transferTo(promotionFile); // 배포 서버 파일 등록

            HashMap<String, String> promotionResult = new HashMap<>();
            promotionResult.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "banner" + File.separator, serverUploadPromotionFileName));
            promotionResult.put("imgUrl", deployCallPath + "banner" + File.separator + serverUploadPromotionFileName);
            promotionResult.put("imgTitle", promotionBannerFileName);
            promotionResult.put("imgUuidTitle", serverUploadPromotionFileName);
            promotionResult.put("purpose", "promotion");

            bannerUploadInfo.add(promotionResult);
        }

        return bannerUploadInfo; // 배포 서버 업로드 이미지 경로 호출
    }

    // 라벨 이미지 업로드
    @Override
    public HashMap<String, String> uploadLabelImage(MultipartFile image) throws IOException {
        // 업로드할 프로필 이미지 파일의 진짜 이름 추출
        String originalFilename = image.getOriginalFilename();
        // 난수화된 파일 이름과 확장자를 합친 파일명 추출
        String serverUploadFileName = createServerFileName(originalFilename);

        // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
        //[배포 서버]
        File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "label" + File.separator, serverUploadFileName)); // 배포 서버 File
        // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
        image.transferTo(file); // 배포 서버 파일 등록

        //[배포 서버]
        HashMap<String, String> result = new HashMap<>();
        result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "label" + File.separator, serverUploadFileName));
        result.put("imgUrl", deployCallPath + "label" + File.separator + serverUploadFileName);
        result.put("imgTitle", originalFilename);
        result.put("imgUuidTitle", serverUploadFileName);

        return result; // 배포 서버 업로드 이미지 경로 호출
    }

    // 카테고리 이미지 업로드
    @Override
    public HashMap<String, String> uploadCategoryImage(MultipartFile image) throws IOException {
        // 업로드할 프로필 이미지 파일의 진짜 이름 추출
        String originalFilename = image.getOriginalFilename();
        // 난수화된 파일 이름과 확장자를 합친 파일명 추출
        String serverUploadFileName = createServerFileName(originalFilename);

        // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
        //[배포 서버]
        File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "category" + File.separator, serverUploadFileName)); // 배포 서버 File

        // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
        image.transferTo(file); // 배포 서버 파일 등록

        //[배포 서버]
        HashMap<String, String> result = new HashMap<>();
        result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "category" + File.separator, serverUploadFileName));
        result.put("imgUrl", deployCallPath + "category" + File.separator + serverUploadFileName);
        result.put("imgTitle", originalFilename);
        result.put("imgUuidTitle", serverUploadFileName);

        return result; // 배포 서버 업로드 이미지 경로 호출
    }

    // 제품 이미지 업로드
    @Override
    public List<HashMap<String, String>> uploadProductImage(List<MultipartFile> productImages) throws IOException {

        List<HashMap<String, String>> productImageCheckList = new ArrayList<>();

        for (MultipartFile eachProductImage : productImages) {
            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String originalFilename = eachProductImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadFileName = createServerFileName(originalFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "product" + File.separator, serverUploadFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            eachProductImage.transferTo(file); // 배포 서버 파일 등록

            //[배포 서버]
            HashMap<String, String> result = new HashMap<>();
            result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "product" + File.separator, serverUploadFileName));
            result.put("imgUrl", deployCallPath + "product" + File.separator + serverUploadFileName);
            result.put("imgTitle", originalFilename);
            result.put("imgUuidTitle", serverUploadFileName);

            productImageCheckList.add(result);
        }

        return productImageCheckList; // 배포 서버 업로드 이미지 경로 호출
    }

    // 제품 상세 정보 이미지 업로드
    @Override
    public List<HashMap<String, String>> uploadProductDetailInfoImage(List<MultipartFile> productDetailInfoImages) throws IOException {

        List<HashMap<String, String>> productDetailInfoImageCheckList = new ArrayList<>();

        for (MultipartFile eachProductDetailInfoImage : productDetailInfoImages) {
            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String originalFilename = eachProductDetailInfoImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadFileName = createServerFileName(originalFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "product" + File.separator + "detail" + File.separator, serverUploadFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            eachProductDetailInfoImage.transferTo(file); // 배포 서버 파일 등록

            //[배포 서버]
            HashMap<String, String> result = new HashMap<>();
            result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "product" + File.separator + "detail" + File.separator, serverUploadFileName));
            result.put("imgUrl", deployCallPath + "product" + File.separator + "detail" + File.separator + serverUploadFileName);
            result.put("imgTitle", originalFilename);
            result.put("imgUuidTitle", serverUploadFileName);

            productDetailInfoImageCheckList.add(result);
        }

        return productDetailInfoImageCheckList; // 배포 서버 업로드 이미지 경로 호출
    }

    // 공지사항 정보 이미지 업로드
    @Override
    public List<HashMap<String, String>> uploadNoticeDetailInfoImage(List<MultipartFile> productDetailInfoImages) throws IOException {

        List<HashMap<String, String>> productDetailInfoImageCheckList = new ArrayList<>();

        for (MultipartFile eachProductDetailInfoImage : productDetailInfoImages) {
            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String originalFilename = eachProductDetailInfoImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadFileName = createServerFileName(originalFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "notices" + File.separator + "detail" + File.separator, serverUploadFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            eachProductDetailInfoImage.transferTo(file); // 배포 서버 파일 등록

            //[배포 서버]
            HashMap<String, String> result = new HashMap<>();
            result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "notices" + File.separator + "detail" + File.separator, serverUploadFileName));
            result.put("imgUrl", deployCallPath + "notices" + File.separator + "detail" + File.separator + serverUploadFileName);
            result.put("imgTitle", originalFilename);
            result.put("imgUuidTitle", serverUploadFileName);

            productDetailInfoImageCheckList.add(result);
        }

        return productDetailInfoImageCheckList; // 배포 서버 업로드 이미지 경로 호출
    }

    // FAQ 정보 이미지 업로드
    @Override
    public List<HashMap<String, String>> uploadFaqDetailInfoImage(List<MultipartFile> productDetailInfoImages) throws IOException {

        List<HashMap<String, String>> productDetailInfoImageCheckList = new ArrayList<>();

        for (MultipartFile eachProductDetailInfoImage : productDetailInfoImages) {
            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String originalFilename = eachProductDetailInfoImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadFileName = createServerFileName(originalFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "faq" + File.separator + "detail" + File.separator, serverUploadFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            eachProductDetailInfoImage.transferTo(file); // 배포 서버 파일 등록

            //[배포 서버]
            HashMap<String, String> result = new HashMap<>();
            result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "faq" + File.separator + "detail" + File.separator, serverUploadFileName));
            result.put("imgUrl", deployCallPath + "faq" + File.separator + "detail" + File.separator + serverUploadFileName);
            result.put("imgTitle", originalFilename);
            result.put("imgUuidTitle", serverUploadFileName);

            productDetailInfoImageCheckList.add(result);
        }

        return productDetailInfoImageCheckList; // 배포 서버 업로드 이미지 경로 호출
    }

    // 문의 파일 업로드
    @Override
    public List<HashMap<String, String>> uploadInquiryFiles(List<MultipartFile> inquiryFiles) throws IOException, IllegalStateException {

        List<HashMap<String, String>> uploadInquiryResult = new ArrayList<>();

        for(MultipartFile eachInquiryFile : inquiryFiles){
            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String originalFilename = eachInquiryFile.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadFileName = createServerFileName(originalFilename);

            // 업로드한 파일과 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            //[배포 서버]
            File file = new File(getFullPath(uploadFilePath + "image" + File.separator + "inquiry" + File.separator, serverUploadFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            eachInquiryFile.transferTo(file); // 배포 서버 파일 등록

            //[배포 서버]
            HashMap<String, String> result = new HashMap<>();
            result.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "inquiry" + File.separator, serverUploadFileName));
            result.put("imgUrl", deployCallPath + "inquiry" + File.separator + serverUploadFileName);
            result.put("imgTitle", originalFilename);
            result.put("imgUuidTitle", serverUploadFileName);

            uploadInquiryResult.add(result);
        }

        return uploadInquiryResult; // 배포 서버 업로드 이미지 경로 호출
    }


    // 브랜드 이미지 업로드
    @Override
    public List<HashMap<String, String>> uploadBrandImage(HashMap<String, MultipartFile> brandImages) throws IOException {

        List<HashMap<String, String>> brandUploadInfo = new ArrayList<>();

        // 앱 브랜드 이미지 업로드
        if (brandImages.get("brand") != null) {
            MultipartFile brandImage = brandImages.get("brand");

            // 업로드할 프로필 이미지 파일의 진짜 이름 추출
            String brandFilename = brandImage.getOriginalFilename();
            // 난수화된 파일 이름과 확장자를 합친 파일명 추출
            String serverUploadBrandFileName = createServerFileName(brandFilename);

            // 업로드한 프로필 이미지와 경로를 합친 업로드 경로를 File 객체에 넣어 생성
            File brandImageFile = new File(getFullPath(uploadFilePath + "image" + File.separator + "brand" + File.separator, serverUploadBrandFileName)); // 배포 서버 File

            // multipartfile에서 지원하는 transferTo 함수 사용. (해당 파일을 지정한 경로로 전송)
            brandImage.transferTo(brandImageFile); // 배포 서버 파일 등록

            HashMap<String, String> brandImageResult = new HashMap<>();
            brandImageResult.put("imgUploadUrl", getFullPath(uploadFilePath + "image" + File.separator + "brand" + File.separator, serverUploadBrandFileName));
            brandImageResult.put("imgUrl", deployCallPath + "brand" + File.separator + serverUploadBrandFileName);
            brandImageResult.put("imgTitle", brandFilename);
            brandImageResult.put("imgUuidTitle", serverUploadBrandFileName);
            brandImageResult.put("purpose", "brand");

            brandUploadInfo.add(brandImageResult);
        }

        return brandUploadInfo;
    }


    // 난수화한 업로드할 파일 이름
    @Override
    public String createServerFileName(String originalFilename) {
        // 원래 이름이 아닌 난수화한 uuid 이름 추출
        String uuid = UUID.randomUUID().toString();
        // 파일의 원래 이름 중에 . 기호 기준으로 확장자 추출
        String ext = extractExt(originalFilename);

        // 난수화된 이름과 확장자를 합쳐 난수화된 파일명 반환
        return uuid + "." + ext;
    }

    // 업로드 파일 확장자 정보 추출
    @Override
    public String extractExt(String originalFilename) {
        // 파일명의 . 기호가 몇번째에 존재하는지 인덱스 값 추출
        int pos = originalFilename.lastIndexOf(".");

        // 원래 이름에서 뽑은 인덱스값에 위치한 . 기호 다음 확장자 추출
        return originalFilename.substring(pos + 1);
    }
}
