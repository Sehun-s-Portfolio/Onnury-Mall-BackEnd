package com.onnury.brand.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.brand.request.BrandCreateRequestDto;
import com.onnury.brand.request.BrandUpdateRequestDto;
import com.onnury.brand.response.BrandCreateResponseDto;
import com.onnury.brand.response.BrandListUpResponseDto;
import com.onnury.brand.response.BrandUpdateResponseDto;
import com.onnury.brand.response.MainPageBrandResponseDto;
import com.onnury.brand.service.BrandService;
import com.onnury.common.util.LogUtil;
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
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/brand")
@RestController
public class BrandController {

    private final BrandService brandService;

    // 브랜드 생성 api
    @Operation(summary = "브랜드 생성 api", tags = {"BrandController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BrandCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createBrand(
            HttpServletRequest request,
            @Parameter(description = "브랜드 등록 정보") @RequestPart BrandCreateRequestDto brandInfo,
            @Parameter(description = "브랜드 등록 이미지 ") @RequestPart(required = false) MultipartFile brandImage) throws IOException {
        log.info("브랜드 생성 api");
        log.info("브랜드 생성 이미지 : {}", brandImage.getOriginalFilename());

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("브랜드 등록 이미지", brandImage.getOriginalFilename() + " : " + brandImage.getContentType());

        try{
            BrandCreateResponseDto brandCreateResponseDto = brandService.createBrand(request, brandInfo, brandImage, requestParam);

            if (brandCreateResponseDto == null) {
                LogUtil.logError(StatusCode.CANT_CREATE_BANNER.getMessage(), request, requestParam, brandInfo);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, brandCreateResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam, brandInfo);
            return null;
        }
    }


    // 브랜드 수정 api
    @Operation(summary = "브랜드 수정 api", tags = {"BrandController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BrandUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateBrand(
            HttpServletRequest request,
            @Parameter(description = "수정 브랜드 id") @RequestParam Long Brandid,
            @Parameter(description = "수정 브랜드 정보") @RequestPart BrandUpdateRequestDto brandInfo,
            @Parameter(description = "수정 브랜드 이미지 ") @RequestPart(required = false) MultipartFile updateBrandImage) throws IOException {
        log.info("브랜드 수정 api");
        log.info("브랜드 수정 이미지 파일 : {}", updateBrandImage.getOriginalFilename());

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("수정 브랜드 id", Long.toString(Brandid));
        requestParam.put("수정 브랜드 이미지", updateBrandImage.getOriginalFilename() + " : " + updateBrandImage.getContentType());

        try{
            BrandUpdateResponseDto brandUpdateResponseDto = brandService.updateBrand(request, Brandid, brandInfo, updateBrandImage, requestParam);

            if (brandUpdateResponseDto == null) {
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, brandUpdateResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam, brandInfo);
            return null;
        }
    }


    // 브랜드 삭제 api
    @Operation(summary = "브랜드 삭제 api", tags = {"BrandController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteBrand(
            HttpServletRequest request,
            @Parameter(description = "삭제 브랜드 id") @RequestParam Long Brandid) {
        log.info("브랜드 수정 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("삭제 브랜드 id", Long.toString(Brandid));

        try{
            boolean deleteSuccess = brandService.deleteBrand(request, Brandid, requestParam);

            if (deleteSuccess) {
                log.info("브랜드 삭제 실패");
                LogUtil.logError(StatusCode.CANT_DELETE_BANNER.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
            } else {
                log.info("브랜드 삭제 성공");
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 관리자 브랜드 페이지 리스트업 api
    @Operation(summary = "관리자 브랜드 페이지 리스트업 api", tags = {"BrandController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = BrandListUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpSupplier(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) {
        log.info("관리자 공급사 리스트업 페이지 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));

        try{
            BrandListUpResponseDto responseSupplierList = brandService.listUpBrand(request, page, requestParam);

            if (responseSupplierList == null) {
                LogUtil.logError(StatusCode.CANT_GET_BANNER_LISTUP.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, responseSupplierList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 메인 페이지 브랜드 리스트 api
    @Operation(summary = "메인 페이지 브랜드 리스트 api", tags = {"BrandController"})
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MainPageBrandResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/mainlist", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> mainPageBrandList(HttpServletRequest request) {
        log.info("메인 페이지 브랜드 리스트 api");

        try{
            List<MainPageBrandResponseDto> mainPageBrandList = brandService.mainPageBrandList(request);

            if (mainPageBrandList == null) {
                LogUtil.logError(StatusCode.CANT_GET_BANNER_LISTUP.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, mainPageBrandList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request);
            return null;
        }
    }

}
