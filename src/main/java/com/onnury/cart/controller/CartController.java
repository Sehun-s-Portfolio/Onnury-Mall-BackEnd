package com.onnury.cart.controller;

import com.onnury.aop.MethodCallMonitor;
import com.onnury.aop.TimeMonitor;
import com.onnury.cart.request.CartAddRequestDto;
import com.onnury.cart.response.CartAddResponseDto;
import com.onnury.cart.response.CartDataResponseDto;
import com.onnury.cart.service.CartService;
import com.onnury.common.base.AbstractVO;
import com.onnury.common.util.LogUtil;
import com.onnury.common.util.VoUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@RestController
public class CartController {

    private final CartService cartService;


    // 장바구니 담기 api
    // #!! 구매할 경우 장바구니에 담긴 데이터도 완료 처리되어 삭제되기 때문에 장바구니에 여전히 동일한 제품의, 옵션의, 상세 옵션이 존재한다면 수량을 추가하여 업데이트
    @Operation(summary = "장바구니 담기 api", tags = { "CartController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CartAddResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @PostMapping(value = "/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> addCart(
            HttpServletRequest request,
            @Parameter(description = "장바구니에 담긴 제품 정보 리스트") @RequestPart List<CartAddRequestDto> cartAddRequestDtoList){
        log.info("장바구니 담기 api");

        try{
            List<CartAddResponseDto> cartAddResult = cartService.addCart(request, cartAddRequestDtoList);

            if(cartAddResult == null || cartAddResult.isEmpty()){
                LogUtil.logError(StatusCode.CANT_ADD_CART.getMessage(), request, cartAddRequestDtoList);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_ADD_CART, "장바구니에 담지 못하였습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, cartAddResult), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, cartAddRequestDtoList);
            return null;
        }
    }


    // 장바구니 제품 삭제 api
    @Operation(summary = "장바구니 제품 삭제 api", tags = { "CartController" })
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
    public ResponseEntity<ResponseBody> deleteCartProduct(
            HttpServletRequest request,
            @Parameter(description = "삭제 장바구니 제품 id") @RequestParam Long cartId){
        log.info("장바구니 제품 삭제 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("삭제 장바구니 제품 id", Long.toString(cartId));

        try{
            String deleteCheck = cartService.deleteCartProduct(request, cartId);

            if(deleteCheck.equals("FAIL")){
                LogUtil.logError(StatusCode.CANT_DELETE_CART.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_CART, "장바구니 덜어내기에 실패하셨습니다."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "장바구니 덜어내기"), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }


    // 장바구니 리스트 호출 api
    @Operation(summary = "장바구니 리스트 호출 api", tags = { "CartController" })
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CartDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    @MethodCallMonitor
    @TimeMonitor
    @GetMapping("/list")
    public ResponseEntity<ResponseBody> getCartList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page){
        log.info("장바구니 리스트 호출 api");

        HashMap<String, String> requestParam = new HashMap<>();
        requestParam.put("페이지 번호", Integer.toString(page));

        try{
            List<CartDataResponseDto> cartList = cartService.getCartList(request, page);

            if(cartList == null){
                LogUtil.logError(StatusCode.CANT_GET_CART_LIST.getMessage(), request, requestParam);
                return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_CART_LIST, "장바구니 데이터를 조회할 수 없습니다. 재 로그인 해주십시오."), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ResponseBody(StatusCode.OK, cartList), HttpStatus.OK);
            }
        }catch(Exception e){
            LogUtil.logException(e, request, requestParam);
            return null;
        }
    }
}
