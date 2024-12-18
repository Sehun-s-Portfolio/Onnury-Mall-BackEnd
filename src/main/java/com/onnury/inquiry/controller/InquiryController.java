package com.onnury.inquiry.controller;

import com.onnury.inquiry.request.InquiryAnswerRequestDto;
import com.onnury.inquiry.request.InquiryRequestDto;
import com.onnury.inquiry.response.InquiryDataResponseDto;
import com.onnury.inquiry.response.InquiryListUpResponseDto;
import com.onnury.inquiry.response.InquiryUpdateResponseDto;
import com.onnury.inquiry.service.InquiryService;
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
@RequestMapping("/api/inquiry")
@RestController
public class InquiryController {

    private final InquiryService inquiryService;

    // 관리자 문의 답변 등록 api
    @Operation(summary = "관리자 문의 답변 등록 api", tags = { "InquiryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = InquiryUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateInquiry(
            HttpServletRequest request,
            @Parameter(description = "문의 답변 내용") @RequestPart InquiryAnswerRequestDto inquiryAnswerRequestDto) throws IOException {
        log.info("문의 수정 api");

        InquiryUpdateResponseDto inquiryUpdateResponseDto = inquiryService.updateInquiry(request, inquiryAnswerRequestDto);

        if(inquiryUpdateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, inquiryUpdateResponseDto), HttpStatus.OK);
        }
    }


    // 관리자 문의 페이지 리스트업 api
    @Operation(summary = "관리자 문의 페이지 리스트업 api", tags = { "InquiryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = InquiryListUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpInquiry(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "문의 타입 1") @RequestParam(required = false, defaultValue = "전체") String searchType,
            @Parameter(description = "문의 타입 2") @RequestParam(required = false, defaultValue = "전체") String searchType2,
            @Parameter(description = "문의 검색 키워드") @RequestParam(required = false, defaultValue = "") String searchKeyword){
        log.info("관리자 문의 리스트업 페이지 api");

        InquiryListUpResponseDto responseSupplierList = inquiryService.listUpInquiry(request, page, searchType, searchType2, searchKeyword );

        if(responseSupplierList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, responseSupplierList), HttpStatus.OK);
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
    // 고객 문의 작성 api
    @Operation(summary = "고객 문의 작성 api", tags = { "InquiryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = InquiryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> writeInquiry(
            HttpServletRequest request,
            @Parameter(description = "고객 문의 작성 내용") @RequestPart InquiryRequestDto inquiryRequestDto,
            @Parameter(description = "문의 첨부 파일 리스트") @RequestPart(required = false) List<MultipartFile> inquiryFile) throws IOException {
        log.info("고객 문의 작성 api");

        InquiryDataResponseDto inquiryResult = inquiryService.writeInquiry(request, inquiryRequestDto, inquiryFile);

        if(inquiryResult == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_WRITE_INQUIRY, "문의하실 수 없습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, inquiryResult), HttpStatus.OK);
        }
    }

}
