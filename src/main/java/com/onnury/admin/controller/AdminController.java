package com.onnury.admin.controller;

import com.onnury.admin.request.AdminAccountRegisterRequestDto;
import com.onnury.admin.response.AdminAccountLoginResponseDto;
import com.onnury.admin.response.AdminAccountRegisterResponseDto;
import com.onnury.admin.response.DashBoardResponseDto;
import com.onnury.admin.service.AdminService;
import com.onnury.share.ResponseBody;
import com.onnury.share.StatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
public class AdminController {

    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;

    // 관리자 계정 회원가입 api
    @Operation(summary = "관리자 계정 회원가입 api", tags = { "AdminController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminAccountRegisterResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/register", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> adminRegister(
            @Parameter(description = "관리자 계정 회원가입 정보") @RequestBody AdminAccountRegisterRequestDto adminAccountRegisterRequestDto
    ) {
        log.info("관리자 계정 회원가입 api - {} / {}", adminAccountRegisterRequestDto.getLoginId(), adminAccountRegisterRequestDto.getPassword());

        AdminAccountRegisterResponseDto adminAccountRegisterResponseDto = adminService.adminRegister(adminAccountRegisterRequestDto);

        if (adminAccountRegisterResponseDto == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGISTER_ADMIN_ACCOUNT, null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, adminAccountRegisterResponseDto), HttpStatus.OK);
        }
    }


    // 관리자(공급사) 계정 로그인 api
    @Operation(summary = "관리자 계정 로그인 api", tags = { "AdminController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = AdminAccountLoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> adminLogin(
            HttpServletResponse response,
            @Parameter(description = "관리자(공급사) 계정 로그인 아이디") @RequestParam String loginId,
            @Parameter(description = "관리자(공급사) 계정 로그인 비밀번호") @RequestParam String password) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        log.info("관리자 계정 로그인 api");

        AdminAccountLoginResponseDto loginSuccess = adminService.adminLogin(response, loginId, password);

        if (loginSuccess == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_ADMIN_LOGIN, null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, loginSuccess), HttpStatus.OK);
        }
    }


    // 대시 보드 api
    @Operation(summary = "관리자 계정 대시 보드 api", tags = { "AdminController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = DashBoardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/dashboard", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> adminDashBoard(
            HttpServletRequest request,
            @Parameter(description = "대시보드 조회 범위 시작 일자 (YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "대시보드 조회 범위 끝 일자 (YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String endDate,
            @Parameter(description = "대시보드 브랜드 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> brandIdList,
            @Parameter(description = "대시보드 공급사 id 리스트") @RequestParam(required = false, defaultValue = "") List<Long> supplierIdList) {
        log.info("대시 보드 api");

        DashBoardResponseDto dashBoardResponse = adminService.adminDashBoard(request, startDate, endDate, brandIdList, supplierIdList);

        if(dashBoardResponse == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_DASHBOARD_DATA, null), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, dashBoardResponse), HttpStatus.OK);
        }
    }


    // 유저 비밀번호 재설정 api
    @PutMapping("/change/user/password")
    public ResponseEntity<ResponseBody> adminChangeUserPassword(HttpServletRequest request, @RequestParam Long memberId){
        log.info("유저 비밀번호 재설정 api");

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, adminService.adminChangeUserPassword(request, memberId)), HttpStatus.OK);
    }

    // 계정 비밀번호 수정 (어드민 관리자 경우)
    @PutMapping("/change/admin/password")
    public ResponseEntity<ResponseBody> testChangePassword(@RequestParam String password){
        log.info("관리자 유저 비밀번호 재설정 api");

        return new ResponseEntity<>(new ResponseBody(StatusCode.OK, passwordEncoder.encode(password)), HttpStatus.OK);
    }

    // 주문 이력 긴급 최종 확정 api
    @PutMapping("/change/order/confirm")
    public ResponseEntity<ResponseBody> emergencyUpdateOrderProduct(){
        log.info("주문 이력 긴급 최종 확정 api");

        boolean checkUpdate = adminService.emergencyUpdateOrderProduct();

        if(checkUpdate){
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "긴급 주문 이력 최종 확정 처리"), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "최종 확정 안됨"), HttpStatus.OK);
        }
    }

}
