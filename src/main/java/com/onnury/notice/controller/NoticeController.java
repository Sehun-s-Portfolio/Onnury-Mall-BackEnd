package com.onnury.notice.controller;

import com.onnury.notice.request.NoticeRequestDto;
import com.onnury.notice.request.NoticeUpdateRequestDto;
import com.onnury.notice.response.NoticeDetailResponseDto;
import com.onnury.notice.response.NoticeResponseDto;
import com.onnury.notice.response.TotalNoticeResponseDto;
import com.onnury.notice.service.NoticeService;
import com.onnury.product.response.ProductDetailImageInfoResponseDto;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

import static com.onnury.notice.domain.QNotice.notice;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notice")
@RestController
public class NoticeController {

    private final NoticeService noticeService;
    private final JPAQueryFactory jpaQueryFactory;

    // 관리자 공지사항 작성 api
    @Operation(summary = "관리자 공지사항 작성 api", tags = {"NoticeController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = NoticeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/write", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> writeNotice(
            HttpServletRequest request,
            @Parameter(description = "작성 공지사항 내용") @RequestPart NoticeRequestDto noticeRequestDto) {
        log.info("관리자 공지사항 작성 api");

        NoticeResponseDto notice = noticeService.writeNotice(request, noticeRequestDto);

        if (notice == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_ADMIN_ACCOUNT_FOR_NOTICE, "공지사항 작성 가능한 계정 정보가 아닙니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, notice), HttpStatus.OK);
        }
    }


    // 고객 측 공지사항 리스트 조회 api
    @Operation(summary = "고객 측 공지사항 리스트 조회 api", tags = {"NoticeController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalNoticeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getNoticeList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("고객 측 공지사항 리스트 조회 api");

        TotalNoticeResponseDto noticeList = noticeService.getNoticeList(request, page);

        if (noticeList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_NOTICES, "공지사항 데이터를 불러올 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, noticeList), HttpStatus.OK);
        }
    }


    // 관리자 공지사항 수정 api
    @Operation(summary = "관리자 공지사항 수정 api", tags = {"NoticeController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = NoticeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateNotice(
            HttpServletRequest request,
            @Parameter(description = "수정 공지사항 내용") @RequestPart NoticeUpdateRequestDto noticeUpdateRequestDto) {
        log.info("관리자 공지사항 수정 api");

        NoticeResponseDto notice = noticeService.updateNotice(request, noticeUpdateRequestDto);

        if (notice == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_ADMIN_ACCOUNT_FOR_NOTICE, "공지사항 작성 가능한 계정 정보가 아닙니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, notice), HttpStatus.OK);
        }
    }


    // 관리자 공지사항 리스트 호출 api
    @Operation(summary = "관리자 공지사항 리스트 호출 api", tags = {"NoticeController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalNoticeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/admin/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getAdminNoticeList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("관리자 공지사항 리스트 호출 api");

        TotalNoticeResponseDto noticeList = noticeService.getAdminNoticeList(request, page);

        if (noticeList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_NOTICES, "공지사항 데이터를 불러올 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, noticeList), HttpStatus.OK);
        }
    }


    // 관리자 공지사항 삭제 api
    @Operation(summary = "관리자 공지사항 삭제 api", tags = {"NoticeController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteNotice(
            HttpServletRequest request,
            @Parameter(description = "삭제할 공지사항 id") @RequestParam Long noticeId) {
        log.info("관리자 공지사항 삭제 api");

        String deleteCheck = noticeService.deleteNotice(request, noticeId);

        if (deleteCheck == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_ADMIN_ACCOUNT_FOR_NOTICE, "공지사항 작성 가능한 계정 정보가 아닙니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, deleteCheck), HttpStatus.OK);
        }
    }


    // 공지사항 상세 조회 api
    @Operation(summary = "공지사항 상세 조회 api", tags = {"NoticeController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = NoticeDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/get", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getNoticeDetail(
            HttpServletRequest request,
            @Parameter(description = "조회할 공지사항 id") @RequestParam Long noticeId) {
        log.info("공지사항 상세 조회 api");

        NoticeDetailResponseDto noticeDetail = noticeService.getNoticeDetail(request, noticeId);

        if (noticeDetail == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_NOTICES, "공지사항 데이터를 불러올 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, noticeDetail), HttpStatus.OK);
        }
    }


    // 공지사항 상세 정보 이미지 등록 및 리스트 조회 api
    @Operation(summary = "공지사항 상세 정보 이미지 등록 및 리스트 조회 api", tags = {"NoticeController"})
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
            @Parameter(description = "등록할 공지사항 상세 정보 이미지 파일 리스트") @RequestPart List<MultipartFile> detailImages) throws IOException {
        log.info("재품 상세 정보 이미지 링크 반환 api");

        List<ProductDetailImageInfoResponseDto> detailInfoImages = noticeService.saveDetailImage(request, detailImages);

        if (detailInfoImages == null || detailInfoImages.isEmpty()) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_SAVE_DETAIL_INFO_IMAGES, "공지사항 이미지를 생성하지 못하였습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, detailInfoImages), HttpStatus.OK);
        }

    }


    // 공지사항 컨텐츠 텍스트 스플릿 테스트
    @GetMapping(value = "/test/splice/notice/content")
    public void testSplitNoticeContent(@RequestParam Long noticeId) {
        log.info("공지사항 컨텐츠 텍스트 스플릿 테스트");

        String contentText = jpaQueryFactory
                .select(notice.noticeContent)
                .from(notice)
                .where(notice.noticeId.eq(noticeId))
                .fetchOne();

        assert contentText != null;

        String splitContentText = contentText.replace("<p><img src=\"", "").replace("\"></p><p><br></p>", "");

        log.info("========================================================");
        log.info("자르기 전 텍스트 : ");
        log.info("{}", contentText);
        log.info("========================================================");

        log.info("");

        log.info("========================================================");
        log.info("자른 후 텍스트 : ");
        log.info("{}", splitContentText);
        log.info("========================================================");

    }


}
