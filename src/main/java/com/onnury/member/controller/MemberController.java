package com.onnury.member.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.common.util.LogUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/listup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> listUpMember(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam int page,
            @Parameter(description = "회원 타입") @RequestParam(required = false, defaultValue = "전체") String searchtype,
            @Parameter(description = "회원 검색 키워드") @RequestParam(required = false, defaultValue = "") String search,
            @Parameter(description = "회원 가입 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "회원 가입 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("관리자 회원 리스트업 페이지 api");

        // 요청 파라미터 확인용 HashMap
        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지", Integer.toString(page));
        requestParam.put("회원 타입", searchtype);
        requestParam.put("회원 검색 키워드", search);
        requestParam.put("회원 가입 시작 일자", startDate);
        requestParam.put("회원 가입 끝 일자", endDate);

        try{
            // 관리자 회원 리스트업 객체
            MemberListUpResponseDto responseSupplierList = memberService.listUpMember(request, page, searchtype, search, startDate, endDate, requestParam);

            // 관리자 회원 리스트업 객체가 하나도 없을 시
            if (responseSupplierList == null) {
                LogUtil.logError(StatusCode.CANT_GET_BANNER_LISTUP.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_BANNER_LISTUP, null), HttpStatus.OK);
            } else { // 관리자 회원 리스트업 객체가 하나라도 존재할 시
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, responseSupplierList), HttpStatus.OK);
            }
        } catch (Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/regist",  produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> registMember(
            @Parameter(description = "고객 회원가입 정보") @Valid @RequestBody MemberRegistRequestDto memberRegistRequestDto){
        log.info("고객 회원가입 api");

        try{
            ResponseBody registResponse = memberService.registMember(memberRegistRequestDto);

            if(!registResponse.getStatusCode().equals("O-200")){
                LogUtil.logError(StatusCode.CANT_REGIST_MEMBER.getMessage(), memberRegistRequestDto);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGIST_MEMBER, null), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(registResponse, HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, memberRegistRequestDto);
            return null;
        }
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
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> loginMember(
            HttpServletResponse response,
            @Parameter(description = "고객 로그인 정보") @RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        log.info("고객 로그인 api");

        try{
            MemberLoginResponseDto loginMember = memberService.loginMember(response, memberLoginRequestDto);

            if (loginMember == null) {
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_LOGIN_MEMBER, "로그인 하실 수 없습니다."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, loginMember), HttpStatus.OK);
            }
        }catch (Exception e){
            LogUtil.logException(e, memberLoginRequestDto);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/duplicatecheck", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> checkDuplicateLoginId(
            @Parameter(description = "중복 확인할 로그인 아이디") @RequestParam String checkLoginId) {
        log.info("로그인 id 중복 체크 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("중복 확인 로그인 아이디", checkLoginId);

        try{
            boolean duplicateCheck = memberService.checkDuplicateLoginId(checkLoginId);

            if (duplicateCheck) {
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGIST_MEMBER, "이미 존재한 계정 아이디이므로 다른 계정 아이디를 입력해주십시오."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "가입 가능한 계정 아이디 입니다."), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/find/loginid", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> findLoginId(
            @Parameter(description = "이메일") @RequestParam String email,
            @Parameter(description = "전화번호") @RequestParam String phone) {
        log.info("로그인 id 찾기 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("로그인 ID 확인 이메일", email);
        requestParam.put("로그인 ID 확인 전화번호", phone);

        try{
            String findLoginIdCheck = memberService.findLoginId(email, phone, requestParam);

            if (findLoginIdCheck == null) {
                log.info("계정 아이디를 확인할 수 없습니다.");
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_FIND_LOGINID, "계정 아이디를 확인할 수 없습니다."), HttpStatus.OK);
            } else {
                log.info(findLoginIdCheck);
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, findLoginIdCheck), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/find/password", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> findPassword(
            @Parameter(description = "로그인 아이디") @RequestParam String loginId,
            @Parameter(description = "이메일") @RequestParam String email,
            @Parameter(description = "전화번호") @RequestParam String phone) {
        log.info("비밀번호 찾기 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("로그인 아이디", loginId);
        requestParam.put("이메일", email);
        requestParam.put("전화번호", phone);

        try{
            String findPasswordCheck = memberService.findPassword(loginId, email, phone, requestParam);

            if (findPasswordCheck == null) {
                log.info("비밀번호를 확인할 수 없습니다.");
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_FIND_PASSWORD, "비밀번호를 확인할 수 없습니다."), HttpStatus.OK);
            } else {
                log.info(findPasswordCheck);
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, findPasswordCheck), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, requestParam);
            return null;
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
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping(value = "/dashboard", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> getDashboard(
            HttpServletRequest request,
            @Parameter(description = "회원 가입 시작 일자") @RequestParam(required = false, defaultValue = "") String startDate,
            @Parameter(description = "회원 가입 끝 일자") @RequestParam(required = false, defaultValue = "") String endDate) {
        log.info("회원 대시보드 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("회원 가입 시작 일자", startDate);
        requestParam.put("회원 가입 끝 일자", endDate);

        try{
            MemberDashboardResponseDto memberDashboardResponseDto = memberService.getDashboard(request, startDate, endDate, requestParam);

            if (memberDashboardResponseDto == null) {
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_MEMBER_DASHBOARD_DATA, null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, memberDashboardResponseDto), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }

}
