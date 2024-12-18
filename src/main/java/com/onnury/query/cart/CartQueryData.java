package com.onnury.query.cart;

import com.onnury.cart.domain.Cart;
import com.onnury.cart.repository.CartRepository;
import com.onnury.cart.request.CartAddRequestDto;
import com.onnury.cart.response.CartAddResponseDto;
import com.onnury.cart.response.CartDataResponseDto;
import com.onnury.cart.response.RelateOptionDataResponseDto;
import com.onnury.member.domain.Member;
import com.onnury.product.domain.Product;
import com.onnury.product.domain.ProductDetailOption;
import com.onnury.product.domain.ProductOption;
import com.onnury.query.product.ProductQueryData;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.onnury.product.domain.QProduct.product;
import static com.onnury.product.domain.QProductOption.productOption;
import static com.onnury.product.domain.QProductDetailOption.productDetailOption;
import static com.onnury.cart.domain.QCart.cart;
import static com.onnury.media.domain.QMedia.media;
import static com.onnury.cart.domain.QProductInCart.productInCart;

@Slf4j
@RequiredArgsConstructor
@Component
public class CartQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final CartRepository cartRepository;
    private final EntityManager entityManager;

    // 장바구니 담기 처리 쿼리 함수
    @Transactional
    public List<CartAddResponseDto> addCart(Member authMember, List<CartAddRequestDto> cartAddRequestDtoList) {

        List<CartAddResponseDto> cartResponseDtoList = new ArrayList<>();

        // 장바구니 담기 요청 데이터 리스트를 조회하여 Cart 데이터 처리
        cartAddRequestDtoList.forEach(eachAddProductInfo -> {

            Product cartProduct = jpaQueryFactory
                    .selectFrom(product)
                    .where(product.productId.eq(eachAddProductInfo.getProductId()))
                    .fetchOne();

            assert cartProduct != null;

            if (cartProduct.getStatus().equals("Y")) {
                List<Cart> prevExistCartData = jpaQueryFactory
                        .selectFrom(cart)
                        .where(cart.memberId.eq(authMember.getMemberId())
                                .and(cart.productId.eq(eachAddProductInfo.getProductId()))
                                .and(checkInCartData(eachAddProductInfo.getProductOptionIds(), eachAddProductInfo.getProductDetailOptionIds()))
                        ).fetch();

                if (!prevExistCartData.isEmpty()) {

                    // 장바구니와 장바구니에 담긴 제품들 정보를 통한 장바구니 id 리스트 추출
                    List<Long> checkCartIds = jpaQueryFactory
                            .select(cart.cartId)
                            .from(cart)
                            .where(cart.cartId.in(
                                            prevExistCartData.stream()
                                                    .map(Cart::getCartId)
                                                    .collect(Collectors.toList()))
                                    .and(checkExistPrevOption(authMember, eachAddProductInfo.getProductId(), eachAddProductInfo.getProductOptionIds(), eachAddProductInfo.getProductDetailOptionIds()))
                            )
                            .fetch();

                    // 기존 장바구니 데이터가 존재할 경우
                    checkCartIds.forEach(eachPrevCartData -> {

                        // 기존 장바구니 데이터 호출
                        Cart existPrevCartData = jpaQueryFactory
                                .selectFrom(cart)
                                .where(cart.cartId.eq(eachPrevCartData))
                                .fetchOne();


                        if (existPrevCartData != null) {
                            // 장바구니에 존재한 제품들의 수량을 업데이트
                            jpaQueryFactory
                                    .update(cart)
                                    .set(cart.quantity, existPrevCartData.getQuantity() + eachAddProductInfo.getQuantity())
                                    .set(cart.productDetailOptionPrice, existPrevCartData.getProductDetailOptionId() == null ? Integer.valueOf(existPrevCartData.getProductDetailOptionPrice()) :
                                            jpaQueryFactory
                                                    .select(productDetailOption.optionPrice)
                                                    .from(productDetailOption)
                                                    .where(productDetailOption.productDetailOptionId.eq(existPrevCartData.getProductDetailOptionId()))
                                                    .fetchOne())
                                    .set(cart.productPrice, LocalDateTime.now().isAfter(cartProduct.getEventStartDate()) && LocalDateTime.now().isBefore(cartProduct.getEventEndDate()) ? cartProduct.getEventPrice() : cartProduct.getSellPrice())
                                    .where(cart.cartId.eq(eachPrevCartData))
                                    .execute();

                            entityManager.flush();
                            entityManager.clear();
                        }
                    });

                } else {
                    // 장바구니에 담길 제품의 일부 정보 추출
                    Product addCartProductInfo = jpaQueryFactory
                            .selectFrom(product)
                            .where(product.productId.eq(eachAddProductInfo.getProductId()))
                            .fetchOne();

                    if (addCartProductInfo != null) {
                        int price = 0;

                        // 만약 이벤트 진행 중인 기간에 걸친 제품이라면 이벤트 제품 가격으로 반영, 아니라면 기존 가격 반영
                        if (addCartProductInfo.getEventStartDate() != null && addCartProductInfo.getEventEndDate() != null && addCartProductInfo.getEventPrice() != 0) {
                            if (LocalDateTime.now().isAfter(addCartProductInfo.getEventStartDate()) && LocalDateTime.now().isBefore(addCartProductInfo.getEventEndDate())) {
                                price = addCartProductInfo.getEventPrice();
                            } else { // 이벤트 기간에 속해있지 않거나 이벤트 가격이 0원일 경우 기존 판매가격으로 등록
                                price = addCartProductInfo.getSellPrice();
                            }
                        } else { // 이벤트 진행 중인 기간에 걸치지 않았을 경우 판매 가격으로 등록
                            price = addCartProductInfo.getSellPrice();
                        }

                        // 장바구니 담기 요청 데이터에 옵션 정보가 존재할 경우
                        if (!eachAddProductInfo.getProductOptionIds().isEmpty()) {
                            // 모든 옵션 명을 합칠 변수 생성
                            AtomicReference<String> totalOptionTitle = new AtomicReference<>("");
                            // 모든 상세 옵션 명을 합칠 변수 생성
                            AtomicReference<String> totalDetailOptionTitle = new AtomicReference<>("");
                            // 모든 상세 옵션 가격을 합칠 변수 생성
                            AtomicInteger totalDetailOptionPrice = new AtomicInteger(0);

                            // 담기 요청받은 제품의 옵션들을 기준으로 담기 로직 처리
                            int finalPrice = price;
                            eachAddProductInfo.getProductOptionIds().forEach(eachProductOptionId -> {

                                // 장바구니에 담길 제품의 옵션 추출
                                ProductOption addCartProductOptionInfo = jpaQueryFactory
                                        .selectFrom(productOption)
                                        .where(productOption.productOptionId.eq(eachProductOptionId))
                                        .fetchOne();

                                Long productOptionId;

                                if (addCartProductOptionInfo != null) {
                                    productOptionId = addCartProductOptionInfo.getProductOptionId();

                                    // 장바구니에 한 줄로 등록될 총 제품 옵션 명 정보에서 처음 옵션이 아닐 경우 / 기호와 함께 옵션 명 추가
                                    if (eachAddProductInfo.getProductOptionIds().indexOf(eachProductOptionId) != 0) {
                                        totalOptionTitle.getAndSet(totalOptionTitle + " / " + addCartProductOptionInfo.getOptionTitle());
                                    } else { // 장바구니에 한 줄로 등록될 총 제품 옵션 명 정보에서 처음 옵션일 경우 그대로 옵션 명 초기 등록
                                        totalOptionTitle.getAndSet(totalOptionTitle + addCartProductOptionInfo.getOptionTitle());
                                    }
                                } else {
                                    productOptionId = null;
                                }

                                // 추출한 옵션의 상세 옵션 정보 추출
                                List<ProductDetailOption> addCartProductDetailOptionInfo = jpaQueryFactory
                                        .selectFrom(productDetailOption)
                                        .where(productDetailOption.productOption.eq(addCartProductOptionInfo)
                                                .and(productDetailOption.productDetailOptionId.in(eachAddProductInfo.getProductDetailOptionIds())))
                                        .fetch();

                                // 상세 옵션 정보가 존재할 경우 진입
                                if (!addCartProductDetailOptionInfo.isEmpty()) {
                                    AtomicReference<Long> productDetailOptionId = new AtomicReference<>();

                                    addCartProductDetailOptionInfo.forEach(eachProductDetailOption -> {

                                        productDetailOptionId.set(eachProductDetailOption.getProductDetailOptionId());

                                        // 총 상세 옵션 명 정보가 아예 존재하지 않을 경우 초기 상세 옵션 명 설정
                                        if (totalDetailOptionTitle.get() == null) {
                                            totalDetailOptionTitle.getAndSet(eachProductDetailOption.getDetailOptionName());
                                        } else { // 총 상세 옵션 명 정보가 존재할 경우, / 기호 기준으로 상세 옵션 명 추가
                                            totalDetailOptionTitle.getAndSet(totalDetailOptionTitle + " / " + eachProductDetailOption.getDetailOptionName());
                                        }

                                        // 총 상세 옵션 가격에 현재 처리 중인 상세 옵션의 가격을 더하기
                                        totalDetailOptionPrice.addAndGet(eachProductDetailOption.getOptionPrice());

                                        // 카트 정보에 등록될 총 상세 옵션 타이틀명 변수 생성
                                        String productDetailOptionTitle = "";

                                        // 기존에 만들어진 총 상세 정보 타이틀 명 길이가 3 이상일 경우 3번째 자리까지 잘라서 변수에 등록
                                        if (totalDetailOptionTitle.get().length() >= 3) {
                                            productDetailOptionTitle = totalDetailOptionTitle.get().substring(3);
                                        } else { // 기존에 만들어진 총 상세 정보 타이틀 명 길이가 3 미만일 경우 그대로 변수에 등록
                                            productDetailOptionTitle = totalDetailOptionTitle.get();
                                        }

                                        // 장바구니에 담길 정보 저장
                                        Cart cartInsertHaveProductOption = Cart.builder()
                                                .memberId(authMember.getMemberId())
                                                .productId(addCartProductInfo.getProductId())
                                                .productName(addCartProductInfo.getProductName())
                                                .productCode(addCartProductInfo.getClassificationCode())
                                                .productOptionId(productOptionId)
                                                .productOptionTitle(totalOptionTitle.get())
                                                .productDetailOptionId(productDetailOptionId.get())
                                                .productDetailOptionTitle(productDetailOptionTitle)
                                                .productDetailOptionPrice(totalDetailOptionPrice.get())
                                                .productPrice(finalPrice)
                                                .quantity(eachAddProductInfo.getQuantity())
                                                .build();

                                        // 리스트에 Cart 데이터 저장
                                        cartRepository.save(cartInsertHaveProductOption);

                                        cartResponseDtoList.add(
                                                CartAddResponseDto.builder()
                                                        .cartId(cartInsertHaveProductOption.getCartId())
                                                        .memberId(cartInsertHaveProductOption.getMemberId())
                                                        .productId(cartInsertHaveProductOption.getProductId())
                                                        .productCode(cartInsertHaveProductOption.getProductCode())
                                                        .productOptionTitle(cartInsertHaveProductOption.getProductOptionTitle())
                                                        .productDetailOptionTitle(cartInsertHaveProductOption.getProductDetailOptionTitle())
                                                        .productDetailOptionPrice(cartInsertHaveProductOption.getProductDetailOptionPrice())
                                                        .productPrice(cartInsertHaveProductOption.getProductPrice())
                                                        .quantity(cartInsertHaveProductOption.getQuantity())
                                                        .build()
                                        );

                                    });
                                } else {
                                    // 장바구니에 담길 정보 저장
                                    Cart cartInsertHaveProductOption = Cart.builder()
                                            .memberId(authMember.getMemberId())
                                            .productId(addCartProductInfo.getProductId())
                                            .productName(addCartProductInfo.getProductName())
                                            .productCode(addCartProductInfo.getClassificationCode())
                                            .productOptionId(productOptionId)
                                            .productOptionTitle(totalOptionTitle.get())
                                            .productDetailOptionPrice(totalDetailOptionPrice.get())
                                            .productPrice(finalPrice)
                                            .quantity(eachAddProductInfo.getQuantity())
                                            .build();

                                    // 리스트에 Cart 데이터 저장
                                    cartRepository.save(cartInsertHaveProductOption);

                                    cartResponseDtoList.add(
                                            CartAddResponseDto.builder()
                                                    .cartId(cartInsertHaveProductOption.getCartId())
                                                    .memberId(cartInsertHaveProductOption.getMemberId())
                                                    .productId(cartInsertHaveProductOption.getProductId())
                                                    .productCode(cartInsertHaveProductOption.getProductCode())
                                                    .productOptionTitle(cartInsertHaveProductOption.getProductOptionTitle())
                                                    .productDetailOptionTitle(cartInsertHaveProductOption.getProductDetailOptionTitle())
                                                    .productDetailOptionPrice(cartInsertHaveProductOption.getProductDetailOptionPrice())
                                                    .productPrice(cartInsertHaveProductOption.getProductPrice())
                                                    .quantity(cartInsertHaveProductOption.getQuantity())
                                                    .build()
                                    );
                                }

                            });

                        } else {
                            // 장바구니에 담길 정보 저장
                            Cart cartInsertDontHaveProductOption = Cart.builder()
                                    .memberId(authMember.getMemberId())
                                    .productId(addCartProductInfo.getProductId())
                                    .productName(addCartProductInfo.getProductName())
                                    .productCode(addCartProductInfo.getClassificationCode())
                                    .productPrice(price)
                                    .quantity(eachAddProductInfo.getQuantity())
                                    .build();

                            // 리스트에 Cart 데이터 저장
                            cartRepository.save(cartInsertDontHaveProductOption);

                            cartResponseDtoList.add(
                                    CartAddResponseDto.builder()
                                            .cartId(cartInsertDontHaveProductOption.getCartId())
                                            .memberId(cartInsertDontHaveProductOption.getMemberId())
                                            .productId(cartInsertDontHaveProductOption.getProductId())
                                            .productCode(cartInsertDontHaveProductOption.getProductCode())
                                            .productOptionTitle(cartInsertDontHaveProductOption.getProductOptionTitle())
                                            .productDetailOptionTitle(cartInsertDontHaveProductOption.getProductDetailOptionTitle())
                                            .productDetailOptionPrice(cartInsertDontHaveProductOption.getProductDetailOptionPrice())
                                            .productPrice(cartInsertDontHaveProductOption.getProductPrice())
                                            .quantity(cartInsertDontHaveProductOption.getQuantity())
                                            .build()
                            );

                        }
                    }
                }
            }
        });

        if (cartResponseDtoList.isEmpty()) {
            return null;
        } else {
            // 반환 객체 리스트로 변환 후 반환
            return cartResponseDtoList;
        }
    }


    // 카트에 담긴 제품들의 상세 옵션까지 동일하게 이미 존재할 경우를 확인
    private BooleanExpression checkInCartData(List<Long> productOptionIds, List<Long> productDetailOptionIds) {

        if (!productOptionIds.isEmpty()) {
            if (!productDetailOptionIds.isEmpty()) {
                return cart.productOptionId.in(productOptionIds)
                        .and(cart.productDetailOptionId.in(productDetailOptionIds));
            }
            return cart.productOptionId.in(productOptionIds);
        }

        return null;
    }


    // 장바구니에 기존에 담은 동일 제품의, 동일 옵션의, 동일 상세 옵션인지 판별하는 동적 조건
    private BooleanExpression checkExistPrevOption(Member authMember, Long productId, List<Long> productOptionIds, List<Long> productDetailOptionIds) {

        if (!productOptionIds.isEmpty()) {
            List<Cart> alreadyExistOptionCartList = jpaQueryFactory
                    .selectFrom(cart)
                    .where(cart.productId.eq(productId)
                            .and(cart.memberId.eq(authMember.getMemberId()))
                            .and(cart.productOptionId.in(productOptionIds)))
                    .fetch();

            if (!alreadyExistOptionCartList.isEmpty()) {
                if (!productDetailOptionIds.isEmpty()) {
                    if (alreadyExistOptionCartList.stream().anyMatch(eachCartExistOption -> eachCartExistOption.getProductDetailOptionId() != null && productDetailOptionIds.contains(eachCartExistOption.getProductDetailOptionId()))) {
                        return cart.productOptionId.in(productOptionIds)
                                .and(cart.productDetailOptionId.in(productDetailOptionIds));
                    } else {
                        return cart.productOptionId.in(productOptionIds);
                    }
                } else {
                    return cart.productOptionId.in(productOptionIds);
                }
            }
        }

        return null;
    }


    // 장바구니 덜어내기
    @Transactional
    public String deleteCartProduct(Member authMember, Long cartId) {

        // 삭제할 장바구니 정보 호출
        Cart checkCart = jpaQueryFactory
                .selectFrom(cart)
                .where(cart.memberId.eq(authMember.getMemberId())
                        .and(cart.cartId.eq(cartId)))
                .fetchOne();

        // 장바구니 정보가 존재할 경우 삭제 처리
        if (checkCart != null) {
            jpaQueryFactory
                    .delete(cart)
                    .where(cart.memberId.eq(authMember.getMemberId())
                            .and(cart.cartId.eq(cartId)))
                    .execute();

            entityManager.flush();
            entityManager.clear();

            // 삭제 완료 텍스트 반환
            return "SUCCESS";
        }

        // 장바구니 정보가 존재하지 않을 경우 삭제 불가 텍스트 반환
        return "FAIL";
    }


    // 장바구니 정보 리스트 조회
    @Transactional
    public List<CartDataResponseDto> getCartList(Member authMember, int page) {

        // 로그인한 유저의 장바구니 데이터 정보 리스트 호출
        List<Cart> haveCart = jpaQueryFactory
                .selectFrom(cart)
                .where(cart.memberId.eq(authMember.getMemberId()))
                .offset((page * 10L) - 10L)
                .limit(10L)
                .orderBy(cart.createdAt.desc())
                .fetch();

        // 반환용 리스트 객체 생성
        List<CartDataResponseDto> cartDataList = new ArrayList<>();

        // 로그인한 유저의 장바구니 데이터가 존재할 경우 진입
        if (!haveCart.isEmpty()) {
            // 장바구니 데이터 하나씩 조회 후 반환 용 객체에 저장 처리
            cartDataList = haveCart.stream()
                    .filter(eachCartProduct ->
                            jpaQueryFactory
                                    .selectFrom(product)
                                    .where(product.expressionCheck.eq("Y")
                                            .and(product.status.eq("Y"))
                                            .and(product.productId.eq(eachCartProduct.getProductId())))
                                    .fetchOne() != null)
                    .map(eachCartData -> {

                        // 장바구니에 담긴 각 제품의 대표 이미지 추출
                        String productImage = jpaQueryFactory
                                .select(media.imgUrl)
                                .from(media)
                                .where(media.type.eq("product")
                                        .and(media.mappingContentId.eq(eachCartData.getProductId()))
                                        .and(media.representCheck.eq("Y")))
                                .fetchOne();

                        if (productImage == null) {
                            String relateProductImages = jpaQueryFactory
                                    .select(product.relateImgIds)
                                    .from(product)
                                    .where(product.productId.eq(eachCartData.getProductId()))
                                    .fetchOne();

                            List<Long> RelateImgIdsList = ProductQueryData.convertStringToList(relateProductImages);

                            productImage = jpaQueryFactory
                                    .select(media.imgUrl)
                                    .from(media)
                                    .where(media.mediaId.eq(RelateImgIdsList.get(0)))
                                    .fetchOne();
                        }

                        List<RelateOptionDataResponseDto> relateOptionList = new ArrayList<>();


                        jpaQueryFactory
                                .selectFrom(cart)
                                .where(cart.memberId.eq(authMember.getMemberId())
                                        .and(cart.cartId.eq(eachCartData.getCartId())))
                                .fetch()
                                .forEach(eachProductInCart -> {

                                    if (eachProductInCart.getProductOptionId() != null) {
                                        ProductOption relateProductOption = jpaQueryFactory
                                                .selectFrom(productOption)
                                                .where(productOption.productOptionId.eq(eachProductInCart.getProductOptionId()))
                                                .fetchOne();

                                        if (relateProductOption != null) {

                                            jpaQueryFactory
                                                    .update(cart)
                                                    .set(cart.productOptionTitle, relateProductOption.getOptionTitle())
                                                    .where(cart.cartId.eq(eachProductInCart.getCartId()))
                                                    .execute();

                                            entityManager.flush();
                                            entityManager.clear();

                                        } else {
                                            jpaQueryFactory
                                                    .update(cart)
                                                    .set(cart.productOptionTitle, "")
                                                    .set(cart.productOptionId, 0L)
                                                    .where(cart.cartId.eq(eachProductInCart.getCartId()))
                                                    .execute();

                                            entityManager.flush();
                                            entityManager.clear();
                                        }


                                        if (eachProductInCart.getProductDetailOptionId() != null) {

                                            ProductDetailOption relateProductDetailOption = jpaQueryFactory
                                                    .selectFrom(productDetailOption)
                                                    .where(productDetailOption.productDetailOptionId.eq(eachProductInCart.getProductDetailOptionId()))
                                                    .fetchOne();


                                            if (relateProductDetailOption != null) {

                                                jpaQueryFactory
                                                        .update(cart)
                                                        .set(cart.productDetailOptionTitle, relateProductDetailOption.getDetailOptionName())
                                                        .set(cart.productDetailOptionPrice, relateProductDetailOption.getOptionPrice())
                                                        .where(cart.cartId.eq(eachProductInCart.getCartId()))
                                                        .execute();

                                                entityManager.flush();
                                                entityManager.clear();

                                            } else {

                                                jpaQueryFactory
                                                        .update(cart)
                                                        .set(cart.productDetailOptionTitle, "")
                                                        .set(cart.productDetailOptionPrice, 0)
                                                        .set(cart.productDetailOptionId, 0L)
                                                        .where(cart.cartId.eq(eachProductInCart.getCartId()))
                                                        .execute();

                                                entityManager.flush();
                                                entityManager.clear();

                                            }

                                            Cart updateCart = jpaQueryFactory
                                                    .selectFrom(cart)
                                                    .where(cart.cartId.eq(eachProductInCart.getCartId()))
                                                    .fetchOne();

                                            relateOptionList.add(
                                                    RelateOptionDataResponseDto.builder()
                                                            .productOptionId(updateCart.getProductOptionId())
                                                            .productOptionTitle(updateCart.getProductOptionTitle())
                                                            .productDetailOptionId(updateCart.getProductDetailOptionId())
                                                            .productDetailOptionTitle(updateCart.getProductDetailOptionTitle())
                                                            .build()
                                            );
                                        } else {

                                            Cart updateCart = jpaQueryFactory
                                                    .selectFrom(cart)
                                                    .where(cart.cartId.eq(eachProductInCart.getCartId()))
                                                    .fetchOne();

                                            relateOptionList.add(
                                                    RelateOptionDataResponseDto.builder()
                                                            .productOptionId(updateCart.getProductOptionId())
                                                            .productOptionTitle(updateCart.getProductOptionTitle())
                                                            .productDetailOptionId(null)
                                                            .productDetailOptionTitle(null)
                                                            .build()
                                            );
                                        }

                                    }
                                });

                        Tuple statusInfo = jpaQueryFactory
                                .select(product.expressionCheck, product.status, product.productName, product.sellPrice)
                                .from(product)
                                .where(product.productId.eq(eachCartData.getProductId()))
                                .fetchOne();


                        jpaQueryFactory
                                .update(cart)
                                .set(cart.productName, statusInfo.get(product.productName))
                                .set(cart.productPrice, statusInfo.get(product.sellPrice))
                                .where(cart.cartId.eq(eachCartData.getCartId()))
                                .execute();

                        entityManager.flush();
                        entityManager.clear();

                        Cart updateCart = jpaQueryFactory
                                .selectFrom(cart)
                                .where(cart.cartId.eq(eachCartData.getCartId()))
                                .fetchOne();

                        // 장바구니 정보를 반환용 리스트 객체에 저장
                        return CartDataResponseDto.builder()
                                .cartId(updateCart.getCartId())
                                .memberId(updateCart.getMemberId())
                                .productId(updateCart.getProductId())
                                .productName(updateCart.getProductName())
                                .productCode(updateCart.getProductCode())
                                .productImage(productImage)
                                .productOptionList(relateOptionList)
                                .productDetailOptionPrice(updateCart.getProductDetailOptionPrice())
                                .productPrice(updateCart.getProductPrice())
                                .quantity(updateCart.getQuantity())
                                .status(statusInfo.get(product.status))
                                .expressionCheck(statusInfo.get(product.expressionCheck))
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        return cartDataList;
    }

}
