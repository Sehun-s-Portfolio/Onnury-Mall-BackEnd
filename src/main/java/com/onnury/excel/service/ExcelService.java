package com.onnury.excel.service;

import com.onnury.banner.domain.Banner;
import com.onnury.banner.repository.BannerRepository;
import com.onnury.banner.request.BannerCreateRequestDto;
import com.onnury.banner.request.BannerUpdateRequestDto;
import com.onnury.banner.response.*;
import com.onnury.category.response.CategoryDataExcelResponseDto;
import com.onnury.category.response.UpCategoryInfoResponseDto;
import com.onnury.configuration.BatchConfig;
import com.onnury.excel.response.BannerExcelResponseDto;
import com.onnury.excel.response.FaqExcelResponseDto;
import com.onnury.excel.response.InquiryExcelResponseDto;
import com.onnury.excel.response.LabelExcelResponseDto;
import com.onnury.exception.banner.BannerExceptioInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.inquiry.domain.Faq;
import com.onnury.inquiry.domain.Inquiry;
import com.onnury.inquiry.response.InquiryDataResponseDto;
import com.onnury.label.domain.Label;
import com.onnury.media.domain.Media;
import com.onnury.media.repository.MediaRepository;
import com.onnury.media.service.MediaUploadInterface;
import com.onnury.member.domain.Member;
import com.onnury.payment.response.AdminSupplierPaymentResponseExcelQDto;
import com.onnury.product.request.ProductSearchRequestDto;
import com.onnury.product.response.ProductExcelResponseDto;
import com.onnury.query.banner.BannerQueryData;
import com.onnury.query.excel.ExcelQueryData;
import com.onnury.supplier.domain.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExcelService {

    private final ExcelQueryData excelQueryData;

    // 배너 리스트 excel service
    public List<BannerExcelResponseDto> excelBannerList(HttpServletRequest request){
        log.info("배너 리스트 excel service");

        return excelQueryData.listUpBanner(request);
    }


    // 공급사 리스트 excel service
    public List<Supplier> excelSupplierList(HttpServletRequest request){
        log.info("공급사 리스트 excel service");

        return excelQueryData.listUpSupplier(request);
    }


    // 라벨 리스트 excel service
    public List<LabelExcelResponseDto> excelLabelList(HttpServletRequest request){
        log.info("라벨 리스트 excel service");

        return excelQueryData.listUpLabel(request);
    }


    // 카테고리 리스트 excel service
    public List<CategoryDataExcelResponseDto> excelCategoryList(HttpServletRequest request){
        log.info("카테고리 리스트 excel service");

        return excelQueryData.listUpCategory(request);
    }


    // 제품 리스트 excel service
    public List<ProductExcelResponseDto> excelProductList(HttpServletRequest request, ProductSearchRequestDto productSearchRequestDto){
        log.info("상품 리스트 excel service");

        return excelQueryData.listUpProduct(productSearchRequestDto);
    }


    // 회원 리스트 excel service
    public List<Member> excelMemberList(HttpServletRequest request, String searchtype, String search){
        log.info("회원 리스트 excel service");

        return excelQueryData.listUpMember(request,searchtype, search);
    }


    // 문의 리스트 excel service
    public List<InquiryExcelResponseDto> excelInquiryList(HttpServletRequest request, String searchType, String searchType2, String searchKeyword){
        log.info("문의사항 리스트 excel service");

        return excelQueryData.listUpInquriy(request,searchType, searchType2, searchKeyword);
    }


    // FAQ 리스트 excel service
    public List<FaqExcelResponseDto> excelFaqList(HttpServletRequest request, String type){
        log.info("문의사항 리스트 excel service");

        return excelQueryData.listUpFaq(request,type);
    }


    // 결제 리스트 excel service
    public List<AdminSupplierPaymentResponseExcelQDto> excelPaymentList(
            HttpServletRequest request,
            Long supplierId,
            String startDate,
            String endDate,
            String searchType,
            String searchKeyword){
        log.info("결제 리스트 excel service");

        return excelQueryData.listUpPayment(supplierId, startDate, endDate, searchType, searchKeyword);
    }


    // 정산 리스트 excel service
    public List<AdminSupplierPaymentResponseExcelQDto> excelTotalOrderList(
            HttpServletRequest request,
            Long supplierId,
            String startDate,
            String endDate,
            String searchType,
            String searchKeyword){
        log.info("정산 리스트 excel service");

        return excelQueryData.listUpTotalOrder(supplierId, startDate, endDate, searchType, searchKeyword);
    }
}
