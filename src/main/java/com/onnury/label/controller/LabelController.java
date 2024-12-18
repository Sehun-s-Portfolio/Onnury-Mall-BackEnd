package com.onnury.label.controller;

import com.onnury.label.request.LabelCreateRequestDto;
import com.onnury.label.request.LabelUpdateRequestDto;
import com.onnury.label.response.LabelCreateResponseDto;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.label.response.LabelListUpResponseDto;
import com.onnury.label.response.LabelUpdateResponseDto;
import com.onnury.label.service.LabelService;
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
@RequestMapping("/api/label")
@RestController
public class LabelController {

    private final LabelService labelService;

    // 라벨 생성 api
    @Operation(summary = "라벨 생성 api", tags = { "LabelController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LabelCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createLabel(
            HttpServletRequest request,
            @Parameter(description = "라벨 이미지 파일") @RequestPart MultipartFile labelImg,
            @Parameter(description = "라벨 생성 정보") @RequestPart LabelCreateRequestDto labelInfo) throws IOException {
        log.info("배너 생성 api");

        LabelCreateResponseDto bannerCreateResponseDto = labelService.createLabel(request, labelImg, labelInfo);

        if(bannerCreateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, bannerCreateResponseDto), HttpStatus.OK);
        }
    }


    // 라벨 수정 api
    @Operation(summary = "라벨 수정 api", tags = { "LabelController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LabelUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updatelabel(
            HttpServletRequest request,
            @Parameter(description = "수정할 라벨 id") @RequestParam Long labelId,
            @Parameter(description = "수정할 라벨 이미지 파일") @RequestPart(required = false) MultipartFile updateLabelImg,
            @Parameter(description = "수정할 라벨 내용") @RequestPart LabelUpdateRequestDto updateLabelInfo) throws IOException {
        log.info("라벨 수정 api");

        LabelUpdateResponseDto bannerUpdateResponseDto = labelService.updateLabel(request, labelId, updateLabelImg, updateLabelInfo);

        if(bannerUpdateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, bannerUpdateResponseDto), HttpStatus.OK);
        }
    }


    // 라벨 삭제 api
    @Operation(summary = "라벨 삭제 api", tags = { "LabelController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteLabel(
            HttpServletRequest request,
            @Parameter(description = "삭제할 라벨 id") @RequestParam Long deleteLabelId){
        log.info("라벨 삭제 api");

        boolean deleteSuccess = labelService.deleteLabel(request, deleteLabelId);

        if(deleteSuccess){
            log.info("라벨 삭제 실패");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
        }else{
            log.info("라벨 삭제 성공");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
        }
    }


    // 관리자 라벨 페이지 리스트 호출 api
    @Operation(summary = "관리자 라벨 페이지 리스트 호출 api", tags = { "LabelController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LabelListUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpLabel(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page){
        log.info("관리자 라벨 리스트업 페이지 api");

        LabelListUpResponseDto responseBannerList = labelService.listUpLabel(request, page);

        if(responseBannerList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, labelService.listUpLabel(request, page)), HttpStatus.OK);
        }
    }


    // 상위 노출 라벨 리스트 호출 api
    @Operation(summary = "상위 노출 라벨 리스트 호출 api", tags = { "LabelController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LabelDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/topexpression", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> topExpressionLabelList(HttpServletRequest request){
        log.info("상위 노출 라벨 리스트 호출 api");

        List<LabelDataResponseDto> topExpressionLabelList = labelService.topExpressionLabelList(request);

        if(topExpressionLabelList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, topExpressionLabelList), HttpStatus.OK);
        }
    }
}