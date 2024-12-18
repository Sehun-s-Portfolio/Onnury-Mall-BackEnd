package com.onnury.supplier.controller;

import com.onnury.configuration.AES128Config;
import com.onnury.supplier.request.SupplierCreateRequestDto;
import com.onnury.supplier.request.SupplierUpdateRequestDto;
import com.onnury.supplier.response.SupplierCreateResponseDto;
import com.onnury.supplier.response.SupplierListUpResponseDto;
import com.onnury.supplier.response.SupplierUpdateResponseDto;
import com.onnury.supplier.service.SupplierService;
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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/supplier")
@RestController
public class SupplierController {

    private final SupplierService supplierService;
    private final AES128Config aes128Config;

    // 공급사 생성 api
    @Operation(summary = "공급사 생성 api", tags = { "SupplierController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SupplierCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> createSupplier(
            HttpServletRequest request,
            @Parameter(description = "공급사 정보") @RequestPart SupplierCreateRequestDto supplierInfo) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("공급자 생성 api");

        SupplierCreateResponseDto supplierCreateResponseDto = supplierService.createSupplier(request, supplierInfo);

        if(supplierCreateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, supplierCreateResponseDto), HttpStatus.OK);
        }
    }


    // 공급사 수정 api
    @Operation(summary = "공급사 수정 api", tags = { "SupplierController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SupplierUpdateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PutMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> updateSupplier(
            HttpServletRequest request,
            @Parameter(description = "공급사 id") @RequestParam Long supplierId,
            @Parameter(description = "공급사 수정 정보") @RequestPart SupplierUpdateRequestDto supplierInfo) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("공급자 수정 api");

        SupplierUpdateResponseDto supplierUpdateResponseDto = supplierService.updateSupplier(request, supplierId, supplierInfo);

        if(supplierUpdateResponseDto == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_CREATE_BANNER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, supplierUpdateResponseDto), HttpStatus.OK);
        }
    }


    // 공급사 삭제 api
    @Operation(summary = "공급사 삭제 api", tags = { "SupplierController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteSupplier(
            HttpServletRequest request,
            @Parameter(description = "삭제 공급사 id") @RequestParam Long supplierId) {
        log.info("공급자 수정 api");

        boolean deleteSuccess = supplierService.deleteSupplier(request, supplierId);

        if(deleteSuccess){
            log.info("공급사 삭제 실패");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_BANNER, null), HttpStatus.OK);
        }else{
            log.info("공급사 삭제 성공");
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "정상적으로 삭제되었습니다."), HttpStatus.OK);
        }
    }


    // 관리자 공급사 페이지 리스트업 api
    @Operation(summary = "관리자 공급사 페이지 리스트업 api", tags = { "SupplierController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SupplierListUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpSupplier(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("관리자 공급사 리스트업 페이지 api");

        SupplierListUpResponseDto responseSupplierList = supplierService.listUpSupplier(request, page);

        if(responseSupplierList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, responseSupplierList), HttpStatus.OK);
        }
    }


    // 공급사 ID 중복 체크 api
    @Operation(summary = "공급사 ID 중복 체크 api", tags = { "SupplierController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SupplierCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/duplicatecheck", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> checkDuplicateSupplierLoginId(
            @Parameter(description = "중복 확인할 로그인 아이디") @RequestParam String checkSupplierLoginId) {
        log.info("공급사 ID 중복 체크 api");

        if(supplierService.checkDuplicateSupplierLoginId(checkSupplierLoginId)){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGIST_SUPPLIER, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "가입 가능한 공급사 계정 아이디 입니다."), HttpStatus.OK);
        }
    }


    // 공급사 관리자 계정 긴급 생성 api
    @GetMapping("/urgent/create/account")
    public ResponseEntity<ResponseBody> urgentCreateAccount(@RequestParam String password) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        log.info("긴급 공급사 관리자 계정 생성");

        String encPassword = aes128Config.encryptAes(password);

        log.info("{}", encPassword);

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, encPassword), HttpStatus.OK);
    }
}
