package com.onnury.banner.controller;

import com.onnury.banner.request.BannerCreateRequestDto;
import com.onnury.banner.request.BannerUpdateRequestDto;
import com.onnury.banner.response.*;
import com.onnury.banner.service.BannerService;
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

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/banner")
@RestController
public class BannerController {

    private final BannerService bannerService;

    // 배너 생성 api
    @Operation(summary = "배너 생성 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BannerCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createBanner(
            HttpServletRequest request,
            @Parameter(description = "배너용 앱 이미지") @RequestPart MultipartFile appBannerImg,
            @Parameter(description = "배너용 웹 이미지") @RequestPart MultipartFile webBannerImg,
            @Parameter(description = "배너용 슬라이드 이미지") @RequestPart MultipartFile slideBannerImg,
            @Parameter(description = "배너 등록 정보") @RequestPart BannerCreateRequestDto bannerInfo) throws IOException {
        log.info("배너 생성 api");

        BannerCreateResponseDto bannerCreateResponseDto = bannerService.createBanner(request, appBannerImg, webBannerImg, slideBannerImg, bannerInfo);

        if(bannerCreateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, bannerCreateResponseDto), HttpStatus.OK);
        }
    }


    // 배너 수정 api
    @Operation(summary = "배너 수정 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BannerUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> updateBanner(
            HttpServletRequest request,
            @Parameter(description = "배너 id") @RequestParam Long bannerId,
            @Parameter(description = "수정 배너 앱 이미지") @RequestPart(required = false) MultipartFile updateAppBannerImg,
            @Parameter(description = "수정 배너 웹 이미지") @RequestPart(required = false) MultipartFile updateWebBannerImg,
            @Parameter(description = "수정 배너 슬라이드 이미지") @RequestPart(required = false) MultipartFile updateSlideBannerImg,
            @Parameter(description = "수정 배너 정보") @RequestPart BannerUpdateRequestDto updateBannerInfo) throws IOException {
        log.info("배너 수정 api");

        BannerUpdateResponseDto bannerUpdateResponseDto = bannerService.updateBanner(request, bannerId, updateAppBannerImg, updateWebBannerImg, updateSlideBannerImg, updateBannerInfo);

        if(bannerUpdateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, bannerUpdateResponseDto), HttpStatus.OK);
        }
    }


    // 배너 삭제 api
    @Operation(summary = "배너 삭제 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> deleteBanner(
            HttpServletRequest request,
            @Parameter(description = "삭제 배너 id") @RequestParam Long deleteBannerId){
        log.info("배너 삭제 api");

        boolean deleteSuccess = bannerService.deleteBanner(request, deleteBannerId);

        if(deleteSuccess){
            log.info("배너 삭제 실패");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
        }else{
            log.info("배너 삭제 성공");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
        }
    }


    // 관리자 배너 페이지 리스트업 api
    @Operation(summary = "관리자 배너 페이지 리스트업 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalBannerResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpBanner(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page){
        log.info("관리자 배너 리스트업 페이지 api");

        TotalBannerResponseDto responseBannerList = bannerService.listUpBanner(request, page);

        if(responseBannerList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, bannerService.listUpBanner(request, page)), HttpStatus.OK);
        }
    }


    /** REDIS 반영 여지 있음 **/
    // 메인 페이지 배너 리스트 api
    @Operation(summary = "메인 페이지 배너 리스트 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalMainPageBannerResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/mainlist", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> mainPageBannerList(HttpServletRequest request){
        log.info("메인 페이지 배너 리스트 api");

        TotalMainPageBannerResponseDto mainPageBannerList = bannerService.mainPageBannerList(request);

        if(mainPageBannerList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "배너 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, mainPageBannerList), HttpStatus.OK);
        }
    }


    // 프로모션 배너 생성 api
    @Operation(summary = "프로모션 배너 생성 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = PromotionBannerCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/promotion/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createPromotionBanner(
            HttpServletRequest request,
            @Parameter(description = "프로모션 배너용 이미지") @RequestPart MultipartFile bannerImg,
            @Parameter(description = "프로모션 배너 등록 정보") @RequestPart BannerCreateRequestDto bannerInfo) throws IOException {
        log.info("프로모션 배너 생성 api");

        PromotionBannerCreateResponseDto promotionBannerCreateResponseDto = bannerService.createPromotionBanner(request, bannerImg, bannerInfo);

        if(promotionBannerCreateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, promotionBannerCreateResponseDto), HttpStatus.OK);
        }
    }


    // 프로모션 배너 수정 api
    @Operation(summary = "프로모션 배너 수정 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = PromotionBannerUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/promotion/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> updatePromotionBanner(
            HttpServletRequest request,
            @Parameter(description = "배너 id") @RequestParam Long bannerId,
            @Parameter(description = "수정 프로모션 배너 이미지") @RequestPart(required = false) MultipartFile updateBannerImg,
            @Parameter(description = "수정 프로모션 배너 정보") @RequestPart BannerUpdateRequestDto updateBannerInfo) throws IOException {
        log.info("배너 수정 api");

        PromotionBannerUpdateResponseDto promotionBannerUpdateResponseDto = bannerService.updatePromotionBanner(request, bannerId, updateBannerImg, updateBannerInfo);

        if(promotionBannerUpdateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, promotionBannerUpdateResponseDto), HttpStatus.OK);
        }
    }


    /** REDIS 반영 여지 있음 **/
    // 메인 페이지 프로모션 배너 리스트 api
    @Operation(summary = "메인 페이지 프로모션 배너 리스트 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = TotalMainPageBannerResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/promotion/mainlist", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> mainPagePromotionBannerList(){
        log.info("메인 페이지 프로모션 배너 리스트 api");

        TotalMainPagePromotionBannerResponseDto mainPagePromotionBannerList = bannerService.mainPagePromotionBannerList();

        if(mainPagePromotionBannerList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_BANNER, "배너 데이터를 조회할 수 없습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, mainPagePromotionBannerList), HttpStatus.OK);
        }
    }


    // 프로모션 배너 삭제 api
    @Operation(summary = "프로모션 배너 삭제 api", tags = { "BannerController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/promotion/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> deletePromotionBanner(
            HttpServletRequest request,
            @Parameter(description = "삭제 배너 id") @RequestParam Long deleteBannerId){
        log.info("프로모션 배너 삭제 api");

        boolean deleteSuccess = bannerService.deletePromotionBanner(request, deleteBannerId);

        if(deleteSuccess){
            log.info("배너 삭제 실패");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
        }else{
            log.info("배너 삭제 성공");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
        }
    }
}