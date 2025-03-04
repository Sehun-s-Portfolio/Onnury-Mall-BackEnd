package com.onnury.cart.controller;

import com.onnury.cart.request.CartAddRequestDto;
import com.onnury.cart.response.CartAddResponseDto;
import com.onnury.cart.response.CartDataResponseDto;
import com.onnury.cart.service.CartService;
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
import java.util.List;

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
    @PostMapping(value = "/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> addCart(
            HttpServletRequest request,
            @Parameter(description = "장바구니에 담긴 제품 정보 리스트") @RequestPart List<CartAddRequestDto> cartAddRequestDtoList){
        log.info("장바구니 담기 api");

        List<CartAddResponseDto> cartAddResult = cartService.addCart(request, cartAddRequestDtoList);

        if(cartAddResult == null || cartAddResult.isEmpty()){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_ADD_CART, "장바구니에 담지 못하였습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, cartAddResult), HttpStatus.OK);
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
    @DeleteMapping(value = "/delete", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseBody> deleteCartProduct(
            HttpServletRequest request,
            @Parameter(description = "삭제 장바구니 제품 id") @RequestParam Long cartId){
        log.info("장바구니 제품 삭제 api");

        String deleteCheck = cartService.deleteCartProduct(request, cartId);

        if(deleteCheck.equals("FAIL")){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_DELETE_CART, "장바구니 덜어내기에 실패하셨습니다."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, "장바구니 덜어내기"), HttpStatus.OK);
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
    @GetMapping("/list")
    public ResponseEntity<ResponseBody> getCartList(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int page){
        log.info("장바구니 리스트 호출 api");

        List<CartDataResponseDto> cartList = cartService.getCartList(request, page);

        if(cartList == null){
            return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_GET_CART_LIST, "장바구니 데이터를 조회할 수 없습니다. 재 로그인 해주십시오."), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new ResponseBody(StatusCode.OK, cartList), HttpStatus.OK);
        }
    }
}
