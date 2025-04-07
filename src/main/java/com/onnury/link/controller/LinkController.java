package com.onnury.link.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.common.util.LogUtil;
import com.onnury.link.request.LinkCreateRequestDto;
import com.onnury.link.request.LinkUpdateRequestDto;
import com.onnury.link.response.LinkCreateResponseDto;
import com.onnury.link.response.LinkListResponseDto;
import com.onnury.link.response.LinkResponseDto;
import com.onnury.link.service.LinkCodeccService;
import com.onnury.link.service.LinkService;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import com.onnury.supplier.request.SupplierCreateRequestDto;
import com.onnury.supplier.request.SupplierUpdateRequestDto;
import com.onnury.supplier.response.SupplierCreateResponseDto;
import com.onnury.supplier.response.SupplierListUpResponseDto;
import com.onnury.supplier.response.SupplierUpdateResponseDto;
import com.onnury.supplier.service.SupplierService;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/link")
@RestController
public class LinkController {

    private final LinkService linkService;
    private final LinkCodeccService linkCodeccService;

    // 링크 생성 api
    @Operation(summary = "링크 생성 api", tags = { "LinkController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LinkCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createLink(
            HttpServletRequest request,
            @Parameter(description = "링크 정보") @RequestPart LinkCreateRequestDto linkInfo) {
        log.info("링크 생성 api");

        try{
            LinkCreateResponseDto linkCreateResponseDto = linkService.createLink(request, linkInfo);

            if(linkCreateResponseDto == null){
                LogUtil.logError(StatusCode.CANT_CREATE_LINK.getMessage(), request, linkInfo);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_LINK, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, linkCreateResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, linkInfo);
            return null;
        }
    }


    // 링크 수정 api
    @Operation(summary = "링크 수정 api", tags = { "LinkController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LinkResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateLink(
            HttpServletRequest request,
            @Parameter(description = "링크 수정 정보") @RequestPart LinkUpdateRequestDto linkInfo) throws IOException {
        log.info("링크 수정 api");

        try{
            LinkResponseDto linkResponseDto = linkService.updateLink(request, linkInfo);

            if(linkResponseDto == null){
                LogUtil.logError(StatusCode.CANT_UPDATE_LINK.getMessage(), request, linkInfo);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_UPDATE_LINK, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, linkResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, linkInfo);
            return null;
        }
    }


    // 링크 삭제 api
    @Operation(summary = "링크 삭제 api", tags = { "LinkController" })
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
    public ResponseEntity<ResponseBody> deleteLink(
            HttpServletRequest request,
            @Parameter(description = "삭제 공급사 id") @RequestParam Long linkId) {
        log.info("링크 수정 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("삭제 공급사 id", Long.toString(linkId));

        try{
            boolean deleteSuccess = linkService.deleteLink(request, linkId);

            if(deleteSuccess){
                log.info("링크 삭제 실패");
                LogUtil.logError(StatusCode.CANT_DELETE_LINK.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_LINK, null), HttpStatus.OK);
            }else{
                log.info("링크 삭제 성공");
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 관리자 링크 리스트업 api
    @Operation(summary = "관리자 링크 리스트업 api", tags = { "LinkController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = LinkListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpLink(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page){
        log.info("관리자 링크 리스트업 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));

        try{
            LinkListResponseDto linkListResponseDto = linkService.listUpLink(request, page);

            if(linkListResponseDto == null){
                LogUtil.logError(StatusCode.CANT_GET_LINK_LIST.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_LINK_LIST, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, linkListResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }

    @MethodCallMonitor
    @TimeMonitor
    @GetMapping("/EncCode")
    public ResponseEntity<ResponseBody> bizPaymentReserv2e(@RequestParam String data) throws Exception {
        // 결제 정보 암호화
        String encCodeData = linkCodeccService.EncCode(data);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, encCodeData), HttpStatus.OK);
    }

    @MethodCallMonitor
    @TimeMonitor
    @GetMapping("/DecCode")
    public ResponseEntity<ResponseBody> bizPaymentReserv3e(@RequestParam String data) throws Exception {
        // 결제 정보 복호화
        String decCodeData = linkCodeccService.DecCode(data);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, decCodeData), HttpStatus.OK);
    }
}
