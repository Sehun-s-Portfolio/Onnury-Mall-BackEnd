package com.onnury.category.controller;

import com.onnury.category.request.CategoryCreateRequestDto;
import com.onnury.category.request.CategoryUpdateRequestDto;
import com.onnury.category.response.*;
import com.onnury.category.service.CategoryService;
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
@RequestMapping("/api/category")
@RestController
public class CategoryController {

    private final CategoryService categoryService;


    // 카테고리 생성
    @Operation(summary = "카테고리 생성 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createCategory(
            HttpServletRequest request,
            @Parameter(description = "카테고리 이미지 파일") @RequestPart(required = false) MultipartFile categoryImg,
            @Parameter(description = "카테고리 정보") @RequestPart CategoryCreateRequestDto categoryInfo) throws IOException {
        log.info("카테고리 생성 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("카테고리 이미지 파일", categoryImg.getOriginalFilename() + " : " + categoryImg.getContentType());

        try{
            CategoryCreateResponseDto categoryCreateResponseDto = categoryService.createCategory(request, categoryImg, categoryInfo, requestParam);

            if(categoryCreateResponseDto == null){
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryCreateResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam, categoryInfo);
            return null;
        }
    }


    // 카테고리 수정
    @Operation(summary = "카테고리 수정 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateCategory(
            HttpServletRequest request,
            @Parameter(description = "수정할 카테고리 id") @RequestParam Long categoryId,
            @Parameter(description = "수정할 카테고리 이미지 파일") @RequestPart(required = false) MultipartFile categoryImg,
            @Parameter(description = "수정할 카테고리 정보") @RequestPart CategoryUpdateRequestDto updateCategoryInfo) {
        log.info("카테고리 수정 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("수정할 카테고리 id", Long.toString(categoryId));
        requestParam.put("수정할 카테고리 이미지 파일", categoryImg.getOriginalFilename() + " : " + categoryImg.getContentType());

        try{
            CategoryUpdateResponseDto categoryUpdateResponseDto = categoryService.updateCategory(request, categoryId, categoryImg, updateCategoryInfo, requestParam);

            if(categoryUpdateResponseDto == null){
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_BANNER, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryUpdateResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam, updateCategoryInfo);
            return null;
        }
    }


    // 카테고리 삭제
    @Operation(summary = "카테고리 삭제 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteCategory(
            HttpServletRequest request,
            @Parameter(description = "삭제할 카테고리 id") @RequestParam Long deleteCategoryId){
        log.info("카테고리 삭제 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("삭제할 카테고리 id", Long.toString(deleteCategoryId));

        try{
            boolean deleteSuccess = categoryService.deleteCategory(request, deleteCategoryId);

            if(deleteSuccess){
                log.info("카테고리 삭제 실패");
                LogUtil.logError(StatusCode.CANT_DELETE_BANNER.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
            }else{
                log.info("카테고리 삭제 성공");
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 대분류 카테고리 리스트업
    @Operation(summary = "대분류 카테고리 리스트업 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup/onedepth", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpCategoryOneDepth(HttpServletRequest request){
        log.info("관리자 대분류 리스트업 api");

        try{
            List<CategoryDataResponseDto> responseCategoryOneDepthList = categoryService.listUpCategoryOneDepth(request);

            if(responseCategoryOneDepthList == null){
                LogUtil.logError(StatusCode.CANT_GET_BANNER_LISTUP.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryService.listUpCategoryOneDepth(request)), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request);
            return null;
        }
    }


    // 중분류 카테고리 리스트업
    @Operation(summary = "중분류 카테고리 리스트업 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup/twodepth", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpCategoryTwoDepth(
            HttpServletRequest request,
            @Parameter(description = "중분류가 속한 대분류 코드") @RequestParam String motherCode){
        log.info("관리자 중분류 리스트업 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("중분류가 속한 대분류 코드", motherCode);

        try{
            List<CategoryDataResponseDto> responseCategoryOneDepthList = categoryService.listUpCategoryTwoDepth(request, motherCode);

            if(responseCategoryOneDepthList == null){
                LogUtil.logError(StatusCode.CANT_GET_BANNER_LISTUP.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryService.listUpCategoryTwoDepth(request, motherCode)), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 소분류 카테고리 리스트업
    @Operation(summary = "소분류 카테고리 리스트업 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup/threedepth", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpCategoryThreeDepth(
            HttpServletRequest request,
            @Parameter(description = "소분류가 속한 중분류 코드") @RequestParam String motherCode){
        log.info("관리자 소분류 리스트업 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("소분류가 속한 중분류 코드", motherCode);

        try{
            List<CategoryDataResponseDto> responseCategoryOneDepthList = categoryService.listUpCategoryThreeDepth(request, motherCode);

            if(responseCategoryOneDepthList == null){
                LogUtil.logError(StatusCode.CANT_GET_BANNER_LISTUP.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, categoryService.listUpCategoryThreeDepth(request, motherCode)), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    /** REDIS 반영 여지 있음 **/
    // 네비게이션 카테고리 리스트 api
    @Operation(summary = "네비게이션 카테고리 리스트 api", tags = { "CategoryController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = UpCategoryInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/navigation", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> navigationCategory(HttpServletRequest request){
        log.info("네비게이션 카테고리 리스트 api");

        try{
            List<UpCategoryInfoResponseDto> navigationCategorys = categoryService.navigationCategory(request);

            if(navigationCategorys.isEmpty()){
                LogUtil.logError(StatusCode.NOT_EXIST_NAVIGATION_CATEGORIES.getMessage(), request);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_NAVIGATION_CATEGORIES, "네비게이션 카테고리가 존재하지 않습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, navigationCategorys), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request);
            return null;
        }
    }


    /** REDIS 반영 여지 있음 **/
    // 메인 페이지 대분류 카테고리 리스트 호출 api
    @Operation(summary = "메인 페이지 대분류 카테고리 리스트 호출 api", tags = { "CategoryController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/mainlist", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> mainPageQuickUpCategory(){
        log.info("메인 페이지 대분류 카테고리 리스트 호출 api");

        try{
            List<CategoryDataResponseDto> mainPageUpCategories = categoryService.mainPageQuickUpCategory();

            if(mainPageUpCategories.isEmpty()){
                LogUtil.logError(StatusCode.NOT_EXIST_QUICK_UP_CATEGORIES.getMessage());
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_QUICK_UP_CATEGORIES, mainPageUpCategories), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, mainPageUpCategories), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e);
            return null;
        }
    }


    /** REDIS 반영 여지 있음 **/
    // 제품 페이지 대분류 기준 중분류 카테고리 리스트 조회 api
    @Operation(summary = "제품 페이지 대분류 기준 중분류 카테고리 리스트 조회 api", tags = { "CategoryController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CategoryDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/page/{upCategoryId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> middleCategoryByUpCategory(
            @Parameter(description = "대분류 카테고리 id") @PathVariable Long upCategoryId){
        log.info("제품 페이지 대분류 기준 중분류 카테고리 리스트 조회 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("대분류 카테고리 id", Long.toString(upCategoryId));

        try{
            List<CategoryDataResponseDto> relatedMiddleCategories = categoryService.middleCategoryByUpCategory(upCategoryId);

            if(relatedMiddleCategories == null){
                LogUtil.logError(StatusCode.NOT_EXIST_MIDDLE_CATEGORIES_RELATED_UP_CATEGORY.getMessage(), requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.NOT_EXIST_MIDDLE_CATEGORIES_RELATED_UP_CATEGORY, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, relatedMiddleCategories), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, requestParam);
            return null;
        }
    }
}