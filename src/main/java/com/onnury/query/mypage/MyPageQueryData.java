package com.onnury.query.mypage;

import com.onnury.brand.domain.Brand;
import com.onnury.category.domain.Category;
import com.onnury.category.domain.CategoryInBrand;
import com.onnury.inquiry.domain.Inquiry;
import com.onnury.label.domain.Label;
import com.onnury.label.response.LabelDataResponseDto;
import com.onnury.media.domain.Media;
import com.onnury.media.response.MediaResponseDto;
import com.onnury.member.domain.Member;
import com.onnury.mypage.request.ConfirmPaymentRequestDto;
import com.onnury.mypage.request.MyPageChangePasswordRequestDto;
import com.onnury.mypage.request.MyPageUpdateInfoRequestDto;
import com.onnury.mypage.request.UserCancleRequestDto;
import com.onnury.mypage.response.*;
import com.onnury.payment.domain.CancleOrder;
import com.onnury.payment.domain.OrderInDeliveryAddPrice;
import com.onnury.payment.domain.OrderInProduct;
import com.onnury.payment.domain.Payment;
import com.onnury.payment.repository.CancleOrderRepository;
import com.onnury.product.domain.Product;
import com.onnury.product.response.ProductDetailImageInfoResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.brand.domain.QBrand.brand;
import static com.onnury.category.domain.QCategory.category;
import static com.onnury.category.domain.QCategoryInBrand.categoryInBrand;
import static com.onnury.label.domain.QLabel.label;
import static com.onnury.label.domain.QLabelOfProduct.labelOfProduct;
import static com.onnury.member.domain.QMember.member;
import static com.onnury.cart.domain.QCart.cart;
import static com.onnury.inquiry.domain.QInquiry.inquiry;
import static com.onnury.media.domain.QMedia.media;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.product.domain.QProduct.product;
import static com.onnury.product.domain.QProductDetailInfo.productDetailInfo;
import static com.onnury.payment.domain.QPayment.payment;
import static com.onnury.supplier.domain.QSupplier.supplier;
import static com.onnury.payment.domain.QCancleOrder.cancleOrder;
import static com.onnury.payment.domain.QOrderInDeliveryAddPrice.orderInDeliveryAddPrice;
import static com.onnury.product.domain.QProductOfMedia.productOfMedia;


@Slf4j
@RequiredArgsConstructor
@Component
public class MyPageQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final CancleOrderRepository cancleOrderRepository;
    // 마이페이지 비밀번호 재설정
    @Transactional
    public String chageMyPassword(Member authMember, MyPageChangePasswordRequestDto myPageChangePasswordRequestDto) {

        jpaQueryFactory
                .update(member)
                .set(member.password, passwordEncoder.encode(myPageChangePasswordRequestDto.getNewPassword()))
                .where(member.memberId.eq(authMember.getMemberId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return "비밀번호 설정을 완료하였습니다.";
    }


    // 마이페이지 회원 탈퇴
    @Transactional
    public String withdrawalAccount(Member authMember) {

        if (jpaQueryFactory
                .selectFrom(cart)
                .where(cart.memberId.eq(authMember.getMemberId()))
                .fetchOne() != null) {

            jpaQueryFactory
                    .delete(cart)
                    .where(cart.memberId.eq(authMember.getMemberId()))
                    .execute();
        }

        jpaQueryFactory
                .update(member)
                .set(member.status, "N")
                .where(member.memberId.eq(authMember.getMemberId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return "탈퇴에 성공하셨습니다. 다음에 다시 만나뵙길 기대하겠습니다.";
    }


    // 마이페이지 회원 정보 수정
    @Transactional
    public MyPageUpdateInfoResponseDto updateAccountInfo(Member authMember, MyPageUpdateInfoRequestDto myPageUpdateInfoRequestDto) {
        // 수정하고자 하는 회원 정보 호출
        Member tryUpdateMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.memberId.eq(authMember.getMemberId()))
                .fetchOne();

        assert tryUpdateMember != null;

        // 동적 수정을 위한 JPAUpdateClause 생성
        JPAUpdateClause memberClause = jpaQueryFactory
                .update(member)
                .where(member.memberId.eq(authMember.getMemberId()));

        // 수정할 내용이 있는지 확인하기 위한 boolean 변수
        boolean existMemberUpdateContent = false;

        // 주소 수정 세팅
        if (!myPageUpdateInfoRequestDto.getAddress().isEmpty() && !tryUpdateMember.getAddress().equals(myPageUpdateInfoRequestDto.getAddress())) {
            existMemberUpdateContent = true;
            memberClause.set(member.address, myPageUpdateInfoRequestDto.getAddress());
        }

        // 상세 주소 수정 세팅
        if (!myPageUpdateInfoRequestDto.getDetailAddress().isEmpty() && !tryUpdateMember.getDetailAddress().equals(myPageUpdateInfoRequestDto.getDetailAddress())) {
            existMemberUpdateContent = true;
            memberClause.set(member.detailAddress, myPageUpdateInfoRequestDto.getDetailAddress());
        }

        // 우편 번호 수정 세팅
        if (!myPageUpdateInfoRequestDto.getPostNumber().isEmpty() && !tryUpdateMember.getPostNumber().equals(myPageUpdateInfoRequestDto.getPostNumber())) {
            existMemberUpdateContent = true;
            memberClause.set(member.postNumber, myPageUpdateInfoRequestDto.getPostNumber());
        }

        // 만약 기업 고객인 경우
        if (tryUpdateMember.getType().equals("B")) {
            // 담당자 명 수정 세팅
            if (!myPageUpdateInfoRequestDto.getManager().isEmpty() && !tryUpdateMember.getManager().equals(myPageUpdateInfoRequestDto.getManager())) {
                existMemberUpdateContent = true;
                memberClause.set(member.manager, myPageUpdateInfoRequestDto.getManager());
            }
        }

        // 이메일 수정 세팅
        if (!myPageUpdateInfoRequestDto.getEmail().isEmpty() && !tryUpdateMember.getEmail().equals(myPageUpdateInfoRequestDto.getEmail())) {
            existMemberUpdateContent = true;
            memberClause.set(member.email, myPageUpdateInfoRequestDto.getEmail());
        }

        // 연락처 수정 세팅
        if (!myPageUpdateInfoRequestDto.getPhone().isEmpty() && !tryUpdateMember.getPhone().equals(myPageUpdateInfoRequestDto.getPhone())) {
            existMemberUpdateContent = true;
            memberClause.set(member.phone, myPageUpdateInfoRequestDto.getPhone());
        }

        if (existMemberUpdateContent) {
            log.info("회원 정보 수정 성공");
            memberClause.execute();
        } else {
            log.info("수정할 내용이 존재하지 않아 회원 정보 수정 불가");
        }

        entityManager.flush();
        entityManager.clear();

        Member updateMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.memberId.eq(tryUpdateMember.getMemberId()))
                .fetchOne();

        assert updateMember != null;

        return MyPageUpdateInfoResponseDto.builder()
                .loginId(updateMember.getLoginId())
                .birth(updateMember.getBirth())
                .address(updateMember.getAddress())
                .detailAddress(updateMember.getDetailAddress())
                .postNumber(updateMember.getPostNumber())
                .userName(updateMember.getUserName())
                .email(updateMember.getEmail())
                .phone(updateMember.getPhone())
                .businessNumber(updateMember.getBusinessNumber())
                .manager(updateMember.getManager())
                .build();
    }


    // 마이페이지 문의 내역 리스트 조회
    public TotalInquiryListResponseDto getMyInquiryList(Member authMember, int page) {

        Long totalInquiryCount = 0L;

        // 로그인한 고객의 문의 내역 리스트 추출
        List<Inquiry> myInquirys = jpaQueryFactory
                .selectFrom(inquiry)
                .where(inquiry.memberId.eq(authMember.getMemberId()))
                .offset((page * 10L) - 10L)
                .limit(10)
                .orderBy(inquiry.createdAt.desc())
                .fetch();

        String userName;

        if (authMember.getType().equals("B")) {
            userName = authMember.getManager();
        } else {
            userName = authMember.getUserName();
        }

        List<MyPageInquiryResponseDto> myInquiryList = new ArrayList<>();

        // 만약 문의 내역이 하나라도 존재할 경우 진입
        if (!myInquirys.isEmpty()) {

            totalInquiryCount = jpaQueryFactory
                    .select(inquiry.count())
                    .from(inquiry)
                    .where(inquiry.memberId.eq(authMember.getMemberId()))
                    .fetchOne();

            // 반환 리스트에 문의 내역 정보를 알맞게 매핑 후 저장하여 리스트화
            myInquiryList = myInquirys.stream()
                    .map(eachInquiry -> {

                        // 만약 답변하지 않은 문의 내역일 경우 answerCheck는 답변 대기, answerAt은 null로
                        if (eachInquiry.getAnswerAt() == null && eachInquiry.getAnswer() == null) {
                            return MyPageInquiryResponseDto.builder()
                                    .inquiryId(eachInquiry.getInquiryId())
                                    .type(eachInquiry.getType())
                                    .inquiryTitle(eachInquiry.getInquiryTitle())
                                    .answerCheck("답변 대기")
                                    .answerAt(null)
                                    .memberId(eachInquiry.getMemberId())
                                    .userName(userName)
                                    .createdAt(eachInquiry.getCreatedAt())
                                    .build();
                        } else {
                            // 답변된 문의 내역일 경우 answerCheck는 답변 완료, answerAt은 답변 일자 그대로
                            return MyPageInquiryResponseDto.builder()
                                    .inquiryId(eachInquiry.getInquiryId())
                                    .type(eachInquiry.getType())
                                    .inquiryTitle(eachInquiry.getInquiryTitle())
                                    .answerCheck("답변 완료")
                                    .answerAt(eachInquiry.getAnswerAt())
                                    .memberId(eachInquiry.getMemberId())
                                    .userName(userName)
                                    .createdAt(eachInquiry.getCreatedAt())
                                    .build();
                        }

                    })
                    .collect(Collectors.toList());
        }

        return TotalInquiryListResponseDto.builder()
                .totalInquiryCount(totalInquiryCount)
                .inquiryList(myInquiryList)
                .build();
    }


    // 자신이 작성한 문의 내용 상세 조회
    public MyPageInquiryDetailResponseDto getMyInquiryDetail(Member authMember, Long inquiryId) {

        // 조회할 문의 내용 호출
        Inquiry getInquiry = jpaQueryFactory
                .selectFrom(inquiry)
                .where(inquiry.memberId.eq(authMember.getMemberId())
                        .and(inquiry.inquiryId.eq(inquiryId)))
                .fetchOne();

        // 고객 유형에 따라 회원 명을 설정할 변수
        String userName;

        // 고객 유형이 기업일 경우 담당자 명 반환
        if (authMember.getType().equals("B")) {
            userName = authMember.getManager();
        } else { // 고객 유형이 일반일 경우 고객 명 반환
            userName = authMember.getUserName();
        }

        assert getInquiry != null;

        // 문의 연관된 파일 호출
        List<Media> relateMediaFiles = jpaQueryFactory
                .selectFrom(media)
                .where(media.mappingContentId.eq(getInquiry.getInquiryId())
                        .and(media.type.eq("inquiry")))
                .fetch();

        // 연관 파일이 존재할 경우 반환할 리스트 생성
        List<MediaResponseDto> relateFilesResult = new ArrayList<>();

        // 연관 파일이 존재할 경우 진입
        if (!relateMediaFiles.isEmpty()) {
            // 연관 파일 호출 후, 반환 리스트 객체로 매핑 후 저장
            relateFilesResult = relateMediaFiles.stream()
                    .map(eachMedia ->
                            MediaResponseDto.builder()
                                    .mediaId(eachMedia.getMediaId())
                                    .imgUploadUrl(eachMedia.getImgUploadUrl())
                                    .imgUrl(eachMedia.getImgUrl())
                                    .imgTitle(eachMedia.getImgTitle())
                                    .imgUuidTitle(eachMedia.getImgUuidTitle())
                                    .representCheck(eachMedia.getRepresentCheck())
                                    .build()
                    )
                    .collect(Collectors.toList());
        }

        return MyPageInquiryDetailResponseDto.builder()
                .inquiryId(getInquiry.getInquiryId())
                .type(getInquiry.getType())
                .inquiryTitle(getInquiry.getInquiryTitle())
                .inquiryContent(getInquiry.getInquiryContent())
                .answer(getInquiry.getAnswer())
                .createdAt(getInquiry.getCreatedAt())
                .answerAt(getInquiry.getAnswerAt())
                .memberId(getInquiry.getMemberId())
                .userName(userName)
                .relateFiles(relateFilesResult)
                .build();
    }


    // 마이페이지 구매 이력 리스트 조회 쿼리 함수
    public JSONObject getMyPaymentList(Member authMember, int page, String startDate, String endDate) {


        JSONObject result = new JSONObject();
        long totalCount = jpaQueryFactory
                .selectFrom(payment)
                .where(payment.buyMemberLoginId.eq(String.valueOf(authMember.getLoginId()))
                        .and(rangeDate(startDate, endDate)))
                .fetch()
                .size();

        ArrayList<JSONObject> orderProResultList = new ArrayList<>();

        if (totalCount != 0L) {
            result.put("totalCount", totalCount);

            List<Payment> orderNumberList = jpaQueryFactory
                    .selectFrom(payment)
                    .where(payment.buyMemberLoginId.eq(String.valueOf(authMember.getLoginId()))
                            .and(rangeDate(startDate, endDate)))
                    .orderBy(payment.orderedAt.desc())
                    .offset((page * 5L) - 5L)
                    .limit(5)
                    .fetch();

            for(int i = 0; i < orderNumberList.size(); i++){
                JSONObject Info = new JSONObject();
                Info.put("orderNumber", orderNumberList.get(i).getOrderNumber());
                Info.put("paymentInfo", orderNumberList.get(i));

                /**
                Integer totalDangerPrice = jpaQueryFactory
                       .select(orderInDeliveryAddPrice.amount.sum())
                        .from(orderInDeliveryAddPrice)
                       .where(orderInDeliveryAddPrice.orderNumber.eq(orderNumberList.get(i).getOrderNumber()))
                        .fetchOne();
                 **/

                List<OrderInDeliveryAddPrice> dangerPriceList = jpaQueryFactory
                        .selectFrom(orderInDeliveryAddPrice)
                        .where(orderInDeliveryAddPrice.orderNumber.eq(orderNumberList.get(i).getOrderNumber()))
                        .fetch();

                Info.put("DangerPriceList", dangerPriceList);

                //Info.put("totalDangerPrice", totalDangerPrice != null ? totalDangerPrice : 0);
                //Info.put("DangerPriceList", totalDangerPrice);

                List<OrderInProduct> OrdersProductListInfo = jpaQueryFactory
                        .selectFrom(orderInProduct)
                        .from(orderInProduct)
                        .where(orderInProduct.orderNumber.eq(orderNumberList.get(i).getOrderNumber()))
                        .fetch();


                ArrayList<MyPageOrderProductListResponseDto> Ord = new ArrayList<>();

               for(int p = 0; p < OrdersProductListInfo.size(); p++){

                   String proRelateImgIdsList = jpaQueryFactory
                           .select(product.relateImgIds)
                           .from(product)
                           .where(product.classificationCode.eq(OrdersProductListInfo.get(p).getProductClassificationCode()))
                           .fetch().toString();

                   List<Long> RelateImgIdsList = convertStringToList(proRelateImgIdsList);

                   MyPageOrderProductListResponseDto OrdersProductList = jpaQueryFactory
                           .select(Projections.constructor(MyPageOrderProductListResponseDto.class,
                                   orderInProduct.orderNumber,
                                   orderInProduct.seq,
                                   orderInProduct.productName,
                                   media.imgUrl.as("productImgurl"),
                                   orderInProduct.productClassificationCode,
                                   orderInProduct.detailOptionTitle,
                                   supplier.supplierCompany.as("supplierName"),
                                   supplier.supplierId,
                                   orderInProduct.productAmount,
                                   orderInProduct.productOptionAmount,
                                   orderInProduct.quantity,
                                   orderInProduct.deliveryPrice,
                                   orderInProduct.dangerPlacePrice,
                                   orderInProduct.onnurypay,
                                   orderInProduct.productTotalAmount,
                                   orderInProduct.memo,
                                   orderInProduct.transportNumber,
                                   orderInProduct.parcelName,
                                   orderInProduct.completePurchaseCheck,
                                   orderInProduct.completePurchaseAt,
                                   orderInProduct.cancelAmount
                           ))
                           .from(orderInProduct,media, supplier)
                           .where(orderInProduct.orderNumber.eq(orderNumberList.get(i).getOrderNumber())
                                   .and(orderInProduct.seq.eq(OrdersProductListInfo.get(p).getSeq()))
                                   .and(orderInProduct.productClassificationCode.eq(OrdersProductListInfo.get(p).getProductClassificationCode()))
                                   .and(media.mediaId.eq(RelateImgIdsList.get(0)))
                                   .and(orderInProduct.supplierId.eq(supplier.supplierId.stringValue())))
                           .fetchOne();

                   Ord.add(OrdersProductList);
               };

                Info.put("orderProduct",Ord);

                orderProResultList.add(Info);
            }

            result.put("paymentList", orderProResultList);

        }

        return result;
    }
    // 마이페이지 구매 이력 리스트 조회 쿼리 함수
    public JSONObject getMyCancleList(Member authMember, int page, String startDate, String endDate) {


        JSONObject result = new JSONObject();
        long totalCount = jpaQueryFactory
                .select(cancleOrder.orderNumber)
                .from(cancleOrder, payment)
                .where(cancleOrder.orderNumber.eq(payment.orderNumber)
                        .and(payment.buyMemberLoginId.eq(String.valueOf(authMember.getLoginId())))
                        .and(rangeDate(startDate, endDate)))
                .fetch()
                .size();


        if (totalCount != 0L) {
            result.put("totalCount", totalCount);

            List<CancleOrder> orderNumberList = jpaQueryFactory
                    .select(cancleOrder)
                    .from(cancleOrder, payment)
                    .where(cancleOrder.orderNumber.eq(payment.orderNumber)
                            .and(payment.buyMemberLoginId.eq(String.valueOf(authMember.getLoginId())))
                            .and(rangeDate(startDate, endDate)))
                    .orderBy(cancleOrder.cancelRequestAt.desc())
                    .offset((page * 5L) - 5L)
                    .limit(5)
                    .fetch();

            ArrayList<MyPageCancletListResponseDto> Ord = new ArrayList<>();

            for(int c = 0; c < orderNumberList.size(); c++){

                String proRelateImgIdsList = jpaQueryFactory
                        .select(product.relateImgIds)
                        .from(product)
                        .where(product.classificationCode.eq(orderNumberList.get(c).getProductClassificationCode()))
                        .fetchOne();

                List<Long> RelateImgIdsList = convertStringToList(proRelateImgIdsList);

                MyPageCancletListResponseDto CancleProductList = jpaQueryFactory
                        .select(Projections.constructor(MyPageCancletListResponseDto.class,
                                cancleOrder.orderNumber,
                                cancleOrder.productName,
                                media.imgUrl.as("productImgurl"),
                                cancleOrder.productClassificationCode,
                                cancleOrder.detailOptionTitle,
                                cancleOrder.cancelAmount,
                                cancleOrder.cancelAt,
                                cancleOrder.cancelCheck,
                                cancleOrder.onNuryCanclePrice,
                                cancleOrder.creditCanclePrice,
                                cancleOrder.cancelRequestAt
                        ))
                        .from(cancleOrder,media)
                        .where(cancleOrder.cancleOrderId.eq(orderNumberList.get(c).getCancleOrderId())
                                .and(media.mediaId.eq(RelateImgIdsList.get(0))))
                        .fetchOne();

                Ord.add(CancleProductList);
            }


            result.put("cancleList", Ord);

        } else {
            result.put("totalCount", 0);
            result.put("cancleList", "");
        }

        return result;
    }

    @Transactional
    public JSONObject getMyCancleRequest( UserCancleRequestDto userCancleRequestDto) {


        JSONObject result = new JSONObject();

        OrderInProduct op = jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.orderNumber.eq(userCancleRequestDto.getOrderNumber()).and(orderInProduct.seq.eq(userCancleRequestDto.getSeq())))
                .fetchOne();

        jpaQueryFactory
                .update(orderInProduct)
                .set(orderInProduct.cancelAmount, userCancleRequestDto.getQuantity())
                .where(orderInProduct.orderInProductId.eq(op.getOrderInProductId()))
                .execute();

        entityManager.flush();
        entityManager.clear();

        CancleOrder co = CancleOrder.builder()
                .orderNumber(userCancleRequestDto.getOrderNumber())
                .seq(userCancleRequestDto.getSeq())
                .productName(op.getProductName())
                .productClassificationCode(op.getProductClassificationCode())
                .detailOptionTitle(op.getDetailOptionTitle())
                .supplierId(op.getSupplierId())
                .productAmount(op.getProductAmount())
                .productOptionAmount(op.getProductOptionAmount())
                .deliveryPrice(op.getDeliveryPrice())
                .dangerPlacePrice(op.getDangerPlacePrice())
                .cancelAmount(userCancleRequestDto.getQuantity())
                .linkCompany(userCancleRequestDto.getLinkCompany())
                .totalPrice(op.getProductTotalAmount())
                .cancelCheck("N")
                .cancelAt(LocalDateTime.now())
                .build();

        cancleOrderRepository.save(co);

        return result;
    }
    // 결제 이력 일자 범위 조건
    private BooleanExpression rangeDate(String startDate, String endDate) {

        if (startDate.isEmpty() && endDate.isEmpty()) {
            return null;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

            if (startDate.isEmpty() && !endDate.isEmpty()) {
                LocalDateTime endRangeDate = LocalDateTime.parse(endDate + " 23:59:59", formatter);
                return payment.orderedAt.before(endRangeDate);

            } else if (!startDate.isEmpty() && endDate.isEmpty()) {
                LocalDateTime startRangeDate = LocalDateTime.parse(startDate + " 00:00:00", formatter);
                return payment.orderedAt.after(startRangeDate);

            } else {
                LocalDateTime startRangeDate = LocalDateTime.parse(startDate + " 00:00:00", formatter);
                LocalDateTime endRangeDate = LocalDateTime.parse(endDate + " 23:59:59", formatter);
                return payment.orderedAt.after(startRangeDate).and(payment.orderedAt.before(endRangeDate));
            }
        }

    }

    // 제품 조회
    public MyPageOrderInProductOfPaymentResponseDto getProduct(OrderInProduct orderProduct, Product getProduct, String checkNeedProductDetailInfo) {
        // 생성된 제품 호출
        Product createProduct = jpaQueryFactory
                .selectFrom(product)
                .where(product.productId.eq(getProduct.getProductId()))
                .fetchOne();

        assert createProduct != null;

        // 제품에 연관된 카테고리 및 브랜드 매핑 정보 호출
        CategoryInBrand getCategoryInBrand = jpaQueryFactory
                .selectFrom(categoryInBrand)
                .where(categoryInBrand.categoryInBrandId.eq(createProduct.getCategoryInBrandId()))
                .fetchOne();

        assert getCategoryInBrand != null;

        // 제품과 매핑된 브랜드 정보 호출
        Brand getBrand = jpaQueryFactory
                .selectFrom(brand)
                .where(brand.brandId.eq(getCategoryInBrand.getBrandId()))
                .fetchOne();

        // 브랜드 id 초기 값 설정 (브랜드가 삭제 및 존재하지 않게 되었을 경우 0으로 초기값 부여)
        Long brandId = 0L;
        // 브랜드 명 초기 값 설정 (브랜드가 삭제 및 존재하지 않게 되었을 경우 공백으로 초기값 부여)
        String brandTitle = "";

        // 연관된 브랜드 정보가 존재할 경우 진입
        if (getBrand != null) {
            // 브랜드 id 및 브랜드 명 설정
            brandId = getBrand.getBrandId();
            brandTitle = getBrand.getBrandTitle();
        }

        // 제품과 연관된 대분류 카테고리 호출
        Category upCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory1Id()))
                .fetchOne();

        // 제품과 연관된 중분류 카테고리 호출
        Category middleCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory2Id()))
                .fetchOne();

        // 제품과 연관된 소분류 카테고리 호출
        Category downCategory = jpaQueryFactory
                .selectFrom(category)
                .where(category.categoryId.eq(getCategoryInBrand.getCategory3Id()))
                .fetchOne();

        // 제품에 연관된 라벨 id 리스트 추출
        List<Long> getLabelOfProduct = jpaQueryFactory
                .select(labelOfProduct.labelId)
                .from(labelOfProduct)
                .where(labelOfProduct.productId.eq(createProduct.getProductId()))
                .fetch();

        // 제품과 연관된 라벨 정보들을 저장할 리스트 생성
        List<LabelDataResponseDto> labelList = new ArrayList<>();

        // 제품과 연관된 라벨 정보들이 존재할 경우 진입
        if (!getLabelOfProduct.isEmpty()) {
            // 라벨 정보들을 하나씩 조회하며 라벨 정보들 추출
            getLabelOfProduct.forEach(eachLabelOfProduct -> {
                // 라벨 정보 추출
                Label getLabel = jpaQueryFactory
                        .selectFrom(label)
                        .where(label.labelId.eq(eachLabelOfProduct)
                                .and((label.startPostDate.before(LocalDateTime.now()).and(label.endPostDate.after(LocalDateTime.now())))))
                        .fetchOne();

                if (getLabel != null) {
                    // 반환 리스트에 라벨 정보 저장
                    labelList.add(
                            LabelDataResponseDto.builder()
                                    .labelId(getLabel.getLabelId())
                                    .labelTitle(getLabel.getLabelTitle())
                                    .colorCode(getLabel.getColorCode())
                                    .startPostDate(getLabel.getStartPostDate())
                                    .endPostDate(getLabel.getEndPostDate())
                                    .imgUrl(getLabel.getImgUrl())
                                    .topExpression(getLabel.getTopExpression())
                                    .build()
                    );
                }

            });
        }

        String productDetailInfoContent = "";

        // 제품의 상세 정보 내용 추출
        if (checkNeedProductDetailInfo.equals("Y")) {
            productDetailInfoContent = jpaQueryFactory
                    .select(productDetailInfo.content)
                    .from(productDetailInfo)
                    .where(productDetailInfo.productId.eq(createProduct.getProductId()))
                    .fetchOne();
        }

        // 제품에 해당되는 이미지 및 상세 정보 이미지 추출
        List<Media> getMediaList = jpaQueryFactory
                .selectFrom(media)
                .where((media.type.eq("product").or(media.type.eq("productdetail")))
                        .and(media.mappingContentId.eq(createProduct.getProductId())))
                .fetch();


        // 제품 이미지 정보를 저장할 리스트 생성
        List<MediaResponseDto> productImageList = new ArrayList<>();
        // 제품 상세 정보 이미지를 저장할 리스트 생성
        List<ProductDetailImageInfoResponseDto> productDetailInfoImageList = new ArrayList<>();

        if (createProduct.getRelateImgIds() != null) {
            List<Long> RelateImgIdsList = convertStringToList(createProduct.getRelateImgIds());

            productImageList.addAll(
                    RelateImgIdsList.stream()
                            .map(eachRelateImage -> {
                                Media eachImage = jpaQueryFactory
                                        .selectFrom(media)
                                        .where((media.mediaId.eq(eachRelateImage))
                                                .and(media.mappingContentId.eq(createProduct.getProductId())))
                                        .fetchOne();

                                return MediaResponseDto.builder()
                                        .mediaId(eachImage.getMediaId())
                                        .imgUploadUrl(eachImage.getImgUploadUrl())
                                        .imgUrl(eachImage.getImgUrl())
                                        .imgTitle(eachImage.getImgTitle())
                                        .imgUuidTitle(eachImage.getImgUuidTitle())
                                        .representCheck(eachImage.getRepresentCheck())
                                        .build();
                            })
                            .collect(Collectors.toList())
            );
        } else {
            // 만약 제품에 연관된 이미지들이 존재할 경우 진입
            if (!getMediaList.isEmpty()) {
                getMediaList.forEach(eachMedia -> {
                    // 제품 이미지 정보 반환 데이터 저장
                    if (eachMedia.getType().equals("product")) {
                        productImageList.add(
                                MediaResponseDto.builder()
                                        .mediaId(eachMedia.getMediaId())
                                        .imgUploadUrl(eachMedia.getImgUploadUrl())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .imgTitle(eachMedia.getImgTitle())
                                        .imgUuidTitle(eachMedia.getImgUuidTitle())
                                        .representCheck(eachMedia.getRepresentCheck())
                                        .build()
                        );
                    } else if (eachMedia.getType().equals("productdetail")) {
                        // 제품 상세 정보 이미지 반환 데이터 저장
                        productDetailInfoImageList.add(
                                ProductDetailImageInfoResponseDto.builder()
                                        .productDetailImageId(eachMedia.getMediaId())
                                        .type(eachMedia.getType())
                                        .imgUrl(eachMedia.getImgUrl())
                                        .build()
                        );
                    }
                });
            }
        }

        return MyPageOrderInProductOfPaymentResponseDto.builder()
                .supplierId(createProduct.getSupplierId())
                .brandId(brandId)
                .brand(brandTitle)
                .upCategoryId(upCategory.getCategoryId())
                .upCategory(upCategory.getCategoryName())
                .middleCategoryId(middleCategory.getCategoryId())
                .middleCategory(middleCategory.getCategoryName())
                .downCategoryId(downCategory.getCategoryId())
                .downCategory(downCategory.getCategoryName())
                .productId(createProduct.getProductId())
                .productName(createProduct.getProductName())
                .productDetailOptionTitle(orderProduct.getDetailOptionTitle())
                .classificationCode(createProduct.getClassificationCode())
                .quantity(orderProduct.getQuantity())
                .labelList(labelList)
                .modelNumber(createProduct.getModelNumber())
                .deliveryType(createProduct.getDeliveryType())
                .sellClassification(createProduct.getSellClassification())
                .expressionCheck(createProduct.getExpressionCheck())
                .normalPrice(createProduct.getNormalPrice())
                .sellPrice(createProduct.getSellPrice())
                .deliveryPrice(createProduct.getDeliveryPrice())
                .purchasePrice(createProduct.getPurchasePrice())
                .eventStartDate(createProduct.getEventStartDate())
                .eventEndDate(createProduct.getEventEndDate())
                .eventDescription(createProduct.getEventDescription())
                .optionCheck(createProduct.getOptionCheck())
                .productDetailInfo(productDetailInfoContent)
                .mediaList(productImageList)
                .relateImgIds(createProduct.getRelateImgIds())
                .manufacturer(createProduct.getManufacturer())
                .madeInOrigin(createProduct.getMadeInOrigin())
                .consignmentStore(createProduct.getConsignmentStore())
                .memo(createProduct.getMemo())
                .build();
    }


    private List<Long> convertStringToList(String str) {
        // 문자열에서 대괄호 제거
        str = str.replace("[", "").replace("]", "");

        // 쉼표로 분리
        String[] parts = str.split(",");

        // 결과 리스트
        List<Long> list = new ArrayList<>();

        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                // 각 부분을 Long으로 변환하고 리스트에 추가
                list.add(Long.parseLong(part.trim()));
            }
        }

        return list;
    }


    // 마이페이지 결제 주문 확정 쿼리
    @Transactional
    public ConfirmPaymentResponseDto confirmMyPayment(Member authMember, ConfirmPaymentRequestDto confirmPaymentRequestDto){

        // 주문 확정 처리된 주문 이력 조회
        OrderInProduct confirmOrder = jpaQueryFactory
                .selectFrom(orderInProduct)
                .where(orderInProduct.orderNumber.eq(confirmPaymentRequestDto.getOrderNumber())
                        .and(orderInProduct.seq.eq(confirmPaymentRequestDto.getSeq())))
                .fetchOne();

        assert confirmOrder != null;

        // 취소 이력이 존재할 경우 불가 처리
        if(confirmOrder.getCancelAmount() >= 1){
            return ConfirmPaymentResponseDto.builder()
                    .confirmPurchaseStatus("C")
                    .build();
        }else if(confirmOrder.getCancelAmount() == 0){ // 정상적으로 주문 확정 가능 시 처리
            // 고객 주문 확정 처리
            jpaQueryFactory
                    .update(orderInProduct)
                    .set(orderInProduct.completePurchaseAt, LocalDateTime.now())
                    .set(orderInProduct.completePurchaseCheck, "Y")
                    .where(orderInProduct.orderInProductId.eq(confirmOrder.getOrderInProductId()))
                    .execute();

            entityManager.flush();
            entityManager.clear();

            OrderInProduct finalConfirmOrder = jpaQueryFactory
                    .selectFrom(orderInProduct)
                    .where(orderInProduct.orderInProductId.eq(confirmOrder.getOrderInProductId()))
                    .fetchOne();

            assert finalConfirmOrder != null;

            // 반환 값 빌드
            return ConfirmPaymentResponseDto.builder()
                    .memberId(authMember.getMemberId())
                    .name(authMember.getUserName() != null ? authMember.getUserName() : authMember.getManager())
                    .orderNumber(finalConfirmOrder.getOrderNumber())
                    .seq(finalConfirmOrder.getSeq())
                    .confirmPurchaseAt(finalConfirmOrder.getCompletePurchaseAt().toString())
                    .confirmPurchaseStatus(finalConfirmOrder.getCompletePurchaseCheck())
                    .build();

        }else { // 그 외의 상황으로 인해 주문 확정이 불가할 경우
            return ConfirmPaymentResponseDto.builder()
                    .confirmPurchaseStatus("N")
                    .build();
        }
    }

}
