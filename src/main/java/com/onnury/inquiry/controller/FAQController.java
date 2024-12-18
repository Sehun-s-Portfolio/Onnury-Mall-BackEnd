package com.onnury.inquiry.controller;

import com.onnury.banner.response.TotalBannerResponseDto;
import com.onnury.inquiry.request.FaqCreateRequestDto;
import com.onnury.inquiry.request.FaqUpdateRequestDto;
import com.onnury.inquiry.response.FaqCreateResponseDto;
import com.onnury.inquiry.response.FaqListUpResponseDto;
import com.onnury.inquiry.response.FaqUpdateResponseDto;
import com.onnury.inquiry.response.TotalFaqListResponseDto;
import com.onnury.inquiry.service.FaqService;
import com.onnury.product.response.ProductDetailImageInfoResponseDto;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/faq")
@RestController
public class FAQController {

    private final FaqService faqService;

    // FAQ 생성 api
    @Operation(summary = "FAQ 생성 api", tags = { "FAQController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FaqCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createFaq(
            HttpServletRequest request,
            @Parameter(description = "FAQ 내용") @RequestPart FaqCreateRequestDto faqInfo) {
        log.info("FAQ 생성 api");

        FaqCreateResponseDto faqCreateResponseDto = faqService.createFaq(request, faqInfo);

        if(faqCreateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, faqCreateResponseDto), HttpStatus.OK);
        }
    }


    // FAQ 수정 api
    @Operation(summary = "FAQ 수정 api", tags = { "FAQController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FaqUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateFaq(
            HttpServletRequest request,
            @Parameter(description = "수정할 FAQ id") @RequestParam Long faqId,
            @Parameter(description = "수정할 FAQ 내용") @RequestPart FaqUpdateRequestDto faqInfo) throws IOException {
        log.info("FAQ 수정 api");

        FaqUpdateResponseDto faqUpdateResponseDto = faqService.updateFaq(request, faqId, faqInfo);

        if(faqUpdateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, faqUpdateResponseDto), HttpStatus.OK);
        }
    }


    // FAQ 삭제 api
    @Operation(summary = "FAQ 삭제 api", tags = { "FAQController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteFaq(
            HttpServletRequest request,
            @Parameter(description = "삭제할 FAQ id") @RequestParam Long faqId) {
        log.info("FAQ 수정 api");

        boolean deleteSuccess = faqService.deleteFaq(request, faqId);

        if(deleteSuccess){
            log.info("브랜드 삭제 실패");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
        }else{
            log.info("브랜드 삭제 성공");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
        }
    }


    // 관리자 FAQ 페이지 리스트업 api
    @Operation(summary = "관리자 FAQ 페이지 리스트업 api", tags = { "FAQController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = FaqListUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpFaq(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "FAQ 타입") @RequestParam(required = false, defaultValue = "") String type){
        log.info("관리자 FAQ 리스트업 페이지 api");

        FaqListUpResponseDto responseFaqList = faqService.listUpFaq(request, page, type);

        if(responseFaqList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, responseFaqList), HttpStatus.OK);
        }
    }


    // [ TYPE ]
    // 이용안내 - 1
    // 회원관련 - 2
    // 주문/결제 - 3
    // 배송 - 4
    // 취소/반품 - 5
    // 교환/AS - 6
    // 혜택/이벤트 - 7
    // 대량주문/제휴 - 8
    // 고객 FAQ 리스트 조회 api
    @Operation(summary = "고객 FAQ 리스트 조회 api", tags = { "FAQController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalFaqListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getFaqList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "FAQ 타입") @RequestParam(required = false, defaultValue = "") String type){
        log.info("고객 FAQ 리스트 조회 api");

        TotalFaqListResponseDto getFaqList = faqService.getFaqList(request, page, type);

        if(getFaqList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_FAQ, "FAQ가 존재하지 않습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, getFaqList), HttpStatus.OK);
        }
    }


    // 공지사항 상세 정보 이미지 리스트 호출 api
    @Operation(summary = "공지사항 상세 정보 이미지 리스트 호출 api", tags = { "FAQController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProductDetailImageInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/detailImage", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> saveDetailImage(
            HttpServletRequest request,
            @Parameter(description = "공지사항 상세 정보 이미지들") @RequestPart List<MultipartFile> detailImages) throws IOException {
        log.info("재품 상세 정보 이미지 링크 반환 api");

        List<ProductDetailImageInfoResponseDto> detailInfoImages = faqService.saveDetailImage(request, detailImages);

        if(detailInfoImages == null || detailInfoImages.isEmpty()){
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_SAVE_DETAIL_INFO_IMAGES, "공지사항 이미지를 생성하지 못하였습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, detailInfoImages), HttpStatus.OK);
        }

    }
}
