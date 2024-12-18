package com.onnury.member.controller;

import com.onnury.member.request.MemberLoginRequestDto;
import com.onnury.member.request.MemberRegistRequestDto;
import com.onnury.member.response.MemberDashboardResponseDto;
import com.onnury.member.response.MemberListUpResponseDto;
import com.onnury.member.response.MemberLoginResponseDto;
import com.onnury.member.service.MemberService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/member")
@RestController
public class MemberController {

    private final MemberService memberService;


    // 관리자 회원 페이지 리스트업 api
    @Operation(summary = "관리자 회원 페이지 리스트업 api", tags = { "MemberController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MemberListUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpMember(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam int page,
            @Parameter(description = "회원 타입") @RequestParam(required = false, defaultValue = "전체") String searchtype,
            @Parameter(description = "회원 검색 키워드") @RequestParam(required = false, defaultValue = "") String search,
            @Parameter(description = "회원 가입 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "회원 가입 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("관리자 회원 리스트업 페이지 api");

        MemberListUpResponseDto responseSupplierList = memberService.listUpMember(request, page, searchtype, search, startDate, endDate);

        if (responseSupplierList == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, responseSupplierList), HttpStatus.OK);
        }
    }


    // 고객 회원가입 api
    @Operation(summary = "고객 회원가입 api", tags = { "MemberController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/regist",  produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> registMember(
            @Parameter(description = "고객 회원가입 정보") @Valid @RequestBody MemberRegistRequestDto memberRegistRequestDto) {
        log.info("고객 회원가입 api");

        ResponseBody registMember = memberService.registMember(memberRegistRequestDto);

        return new ResponseEntity<>(registMember, HttpStatus.OK);
    }


    // 고객 로그인 api
    @Operation(summary = "고객 로그인 api", tags = { "MemberController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MemberLoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @PostMapping(value = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> loginMember(
            HttpServletResponse response,
            @Parameter(description = "고객 로그인 정보") @RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        log.info("고객 로그인 api");

        MemberLoginResponseDto loginMember = memberService.loginMember(response, memberLoginRequestDto);

        if (loginMember == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_LOGIN_MEMBER, "로그인 하실 수 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, loginMember), HttpStatus.OK);
        }
    }


    // 로그인 id 중복 체크 api
    @Operation(summary = "로그인 id 중복 체크 api", tags = { "MemberController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/duplicatecheck", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> checkDuplicateLoginId(
            @Parameter(description = "중복 확인할 로그인 아이디") @RequestParam String checkLoginId) {
        log.info("로그인 id 중복 체크 api");

        if (memberService.checkDuplicateLoginId(checkLoginId)) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGIST_MEMBER, "이미 존재한 계정 아이디이므로 다른 계정 아이디를 입력해주십시오."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "가입 가능한 계정 아이디 입니다."), HttpStatus.OK);
        }
    }


    // 로그인 id 찾기 api
    @Operation(summary = "로그인 id 찾기 api", tags = { "MemberController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/find/loginid", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> findLoginId(
            @Parameter(description = "이메일") @RequestParam String email,
            @Parameter(description = "전화번호") @RequestParam String phone) {
        log.info("로그인 id 찾기 api");

        String findLoginIdCheck = memberService.findLoginId(email, phone);

        if (findLoginIdCheck == null) {
            log.info("계정 아이디를 확인할 수 없습니다.");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_FIND_LOGINID, "계정 아이디를 확인할 수 없습니다."), HttpStatus.OK);
        } else {
            log.info(findLoginIdCheck);
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, findLoginIdCheck), HttpStatus.OK);
        }
    }


    // 비밀번호 찾기 api
    @Operation(summary = "비밀번호 찾기 api", tags = { "MemberController" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/find/password", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> findPassword(
            @Parameter(description = "로그인 아이디") @RequestParam String loginId,
            @Parameter(description = "이메일") @RequestParam String email,
            @Parameter(description = "전화번호") @RequestParam String phone) {
        log.info("비밀번호 찾기 api");

        String findPasswordCheck = memberService.findPassword(loginId, email, phone);

        if (findPasswordCheck == null) {
            log.info("비밀번호를 확인할 수 없습니다.");
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_FIND_PASSWORD, "비밀번호를 확인할 수 없습니다."), HttpStatus.OK);
        } else {
            log.info(findPasswordCheck);
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, findPasswordCheck), HttpStatus.OK);
        }
    }


    // 회원 대시보드 api
    @Operation(summary = "회원 대시보드 api", tags = { "MemberController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MemberDashboardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @GetMapping(value = "/dashboard", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getDashboard(
            HttpServletRequest request,
            @Parameter(description = "회원 가입 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "회원 가입 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("회원 대시보드 api");

        MemberDashboardResponseDto memberDashboardResponseDto = memberService.getDashboard(request, startDate, endDate);

        if (memberDashboardResponseDto == null) {
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_MEMBER_DASHBOARD_DATA, null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, memberDashboardResponseDto), HttpStatus.OK);
        }
    }

}
