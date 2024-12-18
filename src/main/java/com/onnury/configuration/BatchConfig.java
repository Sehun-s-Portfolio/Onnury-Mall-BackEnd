package com.onnury.configuration;

import com.onnury.banner.domain.Banner;
import com.onnury.banner.response.BannerDataResponseDto;
import com.onnury.banner.response.TotalBannerResponseDto;
import com.onnury.media.domain.QMedia;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.onnury.banner.domain.QBanner.banner;
import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.product.domain.QProduct.product;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    public static TotalBannerResponseDto totalBannerResponseDto;

    // 스케줄러와 배치를 통한 작업 (배너 자동 노출)
    public Job bannerExpressJob(int pageNo) {
        return jobBuilderFactory.get("job")
                .start(step(pageNo))
                .build();
    }

    // 배너와 주문 확정 job을 수행하기 위한 step 과정
    public Step step(int page) {
        // 프로젝트 종료일과 비교할 현재 날짜
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        String nowDate = LocalDateTime.now().toString().replace('T', ' '); // 날짜 시간 문자열 중간에 있는 T 문자 삭제
        String[] dateSplit = nowDate.split("\\."); // 포맷 형식에 맞게끔 . 기호를 기준으로 필요한 문자열만 추출

        String schedulingNowDateTime = dateSplit[0].split(" ")[0] + " 00:00:00";
        LocalDateTime nowDateTime = LocalDateTime.parse(schedulingNowDateTime, formatter);

        log.info(nowDate);
        log.info(schedulingNowDateTime);
        log.info(String.valueOf(nowDateTime));

        // 스케줄러를 통해 수행될 step 작업
        return stepBuilderFactory.get("step")
                .tasklet((contribution, chunkContext) -> {
                    log.info("step!");

                    // 총 배너 카운트
                    Long bannerTotalCount = jpaQueryFactory
                            .select(banner.count())
                            .from(banner)
                            .fetchOne();

                    // 현재 진행 중인 상태이고 현재 날짜가 프로젝트 종료일을 넘긴 프로젝트들 조회
                    List<Banner> bannerList = jpaQueryFactory
                            .selectFrom(banner)
                            .orderBy(banner.expressionOrder.asc(), banner.createdAt.desc())
                            .offset((page * 10L) - 10)
                            .limit(10)
                            .fetch();

                    List<BannerDataResponseDto> expressBannerList = new ArrayList<>();
                    List<BannerDataResponseDto> nonExpressBannerList = new ArrayList<>();
                    List<BannerDataResponseDto> resultBannerList = new ArrayList<>();

                    bannerList.forEach(eachBanner -> {
                        LocalDateTime startDate = LocalDateTime.parse(eachBanner.getStartPostDate(), formatter);
                        LocalDateTime endDate = LocalDateTime.parse(eachBanner.getEndPostDate(), formatter);

                        if (nowDateTime.isAfter(startDate) && nowDateTime.isBefore(endDate)) {
                            if (!eachBanner.getExpressionCheck().equals("Y")) {
                                jpaQueryFactory
                                        .update(banner)
                                        .set(banner.expressionCheck, "Y")
                                        .where(banner.bannerId.eq(eachBanner.getBannerId()))
                                        .execute();
                            }

                            List<Tuple> bannerImages = jpaQueryFactory
                                    .select(QMedia.media.imgUrl, QMedia.media.type)
                                    .from(QMedia.media)
                                    .where(QMedia.media.mappingContentId.eq(eachBanner.getBannerId())
                                            .and(QMedia.media.type.contains("banner")))
                                    .fetch();

                            AtomicReference<String> appBannerImgUrl = new AtomicReference<>("");
                            AtomicReference<String> webBannerImgUrl = new AtomicReference<>("");
                            AtomicReference<String> promotionBannerImgUrl = new AtomicReference<>("");
                            AtomicReference<String> slideBannerImgUrl = new AtomicReference<>("");

                            if (!bannerImages.isEmpty()) {
                                bannerImages.forEach(eachImage -> {
                                    if (Objects.equals(eachImage.get(QMedia.media.type), "webbanner")) {
                                        webBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    } else if (Objects.equals(eachImage.get(QMedia.media.type), "appbanner")) {
                                        appBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    } else if (Objects.equals(eachImage.get(QMedia.media.type), "promotionbanner")) {
                                        promotionBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    } else if (Objects.equals(eachImage.get(QMedia.media.type), "slidebanner")) {
                                        slideBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    }
                                });
                            }

                            expressBannerList.add(
                                    BannerDataResponseDto.builder()
                                            .bannerId(eachBanner.getBannerId())
                                            .title(eachBanner.getTitle())
                                            .linkUrl(eachBanner.getLinkUrl())
                                            .expressionOrder(eachBanner.getExpressionOrder())
                                            .expressionCheck("Y")
                                            .appBannerImgUrl(String.valueOf(appBannerImgUrl))
                                            .webBannerImgUrl(String.valueOf(webBannerImgUrl))
                                            .promotionBannerImgUrl(String.valueOf(promotionBannerImgUrl))
                                            .slideBannerImgUrl(String.valueOf(slideBannerImgUrl))
                                            .startPostDate(eachBanner.getStartPostDate())
                                            .endPostDate(eachBanner.getEndPostDate())
                                            .imgType("banner")
                                            .build()
                            );
                        } else {
                            if (!eachBanner.getExpressionCheck().equals("N")) {
                                jpaQueryFactory
                                        .update(banner)
                                        .set(banner.expressionCheck, "N")
                                        .where(banner.bannerId.eq(eachBanner.getBannerId()))
                                        .execute();
                            }

                            List<Tuple> bannerImages = jpaQueryFactory
                                    .select(QMedia.media.imgUrl, QMedia.media.type)
                                    .from(QMedia.media)
                                    .where(QMedia.media.mappingContentId.eq(eachBanner.getBannerId())
                                            .and(QMedia.media.type.contains("banner")))
                                    .fetch();

                            AtomicReference<String> appBannerImgUrl = new AtomicReference<>("");
                            AtomicReference<String> webBannerImgUrl = new AtomicReference<>("");
                            AtomicReference<String> promotionBannerImgUrl = new AtomicReference<>("");
                            AtomicReference<String> slideBannerImgUrl = new AtomicReference<>("");

                            if (!bannerImages.isEmpty()) {
                                bannerImages.forEach(eachImage -> {
                                    if (Objects.equals(eachImage.get(QMedia.media.type), "webbanner")) {
                                        webBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    } else if (Objects.equals(eachImage.get(QMedia.media.type), "appbanner")) {
                                        appBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    } else if (Objects.equals(eachImage.get(QMedia.media.type), "protmotionbanner")) {
                                        promotionBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    } else if (Objects.equals(eachImage.get(QMedia.media.type), "slidebanner")) {
                                        slideBannerImgUrl.set(eachImage.get(QMedia.media.imgUrl));
                                    }
                                });
                            }

                            nonExpressBannerList.add(
                                    BannerDataResponseDto.builder()
                                            .bannerId(eachBanner.getBannerId())
                                            .title(eachBanner.getTitle())
                                            .linkUrl(eachBanner.getLinkUrl())
                                            .expressionOrder(eachBanner.getExpressionOrder())
                                            .expressionCheck("N")
                                            .appBannerImgUrl(String.valueOf(appBannerImgUrl))
                                            .webBannerImgUrl(String.valueOf(webBannerImgUrl))
                                            .promotionBannerImgUrl(String.valueOf(promotionBannerImgUrl))
                                            .slideBannerImgUrl(String.valueOf(slideBannerImgUrl))
                                            .startPostDate(eachBanner.getStartPostDate())
                                            .endPostDate(eachBanner.getEndPostDate())
                                            .imgType("banner")
                                            .build()
                            );
                        }
                    });

                    ////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////////////////////////////////

                    // [주문 이력 자동 최종 구매 확정]

                    // 설치 제품들
                    List<String> buildProducts = jpaQueryFactory
                            .select(product.classificationCode)
                            .from(product)
                            .where(product.deliveryType.eq("S"))
                            .fetch();

                    // 배송 제품들
                    List<String> deliveryProducts = jpaQueryFactory
                            .select(product.classificationCode)
                            .from(product)
                            .where(product.deliveryType.eq("D"))
                            .fetch();

                    // 설치 제품 구매 이력들
                    jpaQueryFactory
                            .selectFrom(orderInProduct)
                            .where(orderInProduct.cancelAmount.eq(0)
                                    .and(orderInProduct.productClassificationCode.in(buildProducts))
                                    .and(orderInProduct.completePurchaseCheck.eq("N"))
                                    .and(orderInProduct.transportNumber.ne(""))
                            )
                            .fetch()
                            .forEach(eachBuildOrderProduct -> {

                                String buildProductCreatedDate = eachBuildOrderProduct.getCreatedAt().toString().replace('T', ' '); // 날짜 시간 문자열 중간에 있는 T 문자 삭제
                                String[] buildProductCreatedDateSplit = buildProductCreatedDate.split("\\."); // 포맷 형식에 맞게끔 . 기호를 기준으로 필요한 문자열만 추출
                                LocalDateTime buildProductNowDateTime = LocalDateTime.parse(buildProductCreatedDateSplit[0], formatter);

                                // 설치 제품들 주문 확정 자동화 (20일 기준)
                                if (buildProductNowDateTime.isBefore(nowDateTime.minusDays(20)) ||
                                        (buildProductNowDateTime.getYear() == nowDateTime.minusDays(20).getYear() &&
                                                buildProductNowDateTime.getMonthValue() == nowDateTime.minusDays(20).getMonthValue() &&
                                                buildProductNowDateTime.getDayOfMonth() == nowDateTime.minusDays(20).getDayOfMonth())) {

                                    log.info("설치 제품 구매 확정 처리 진입 ");
                                    log.info("주문 번호 : {}", eachBuildOrderProduct.getOrderNumber());
                                    log.info("제품 코드 : {}", eachBuildOrderProduct.getProductClassificationCode());

                                    jpaQueryFactory
                                            .update(orderInProduct)
                                            .set(orderInProduct.completePurchaseAt, LocalDateTime.now())
                                            .set(orderInProduct.completePurchaseCheck, "Y")
                                            .where(orderInProduct.orderInProductId.eq(eachBuildOrderProduct.getOrderInProductId()))
                                            .execute();
                                }
                            });


                    // 배송 제품 구매 이력들
                    jpaQueryFactory
                            .selectFrom(orderInProduct)
                            .where(orderInProduct.cancelAmount.eq(0)
                                    .and(orderInProduct.productClassificationCode.in(deliveryProducts))
                                    .and(orderInProduct.completePurchaseCheck.eq("N"))
                                    .and(orderInProduct.transportNumber.ne(""))
                            )
                            .fetch()
                            .forEach(eachDeliveryOrderProduct -> {
                                String deliveryProductCreatedDate = eachDeliveryOrderProduct.getCreatedAt().toString().replace('T', ' '); // 날짜 시간 문자열 중간에 있는 T 문자 삭제
                                String[] deliveryProductCreatedDateSplit = deliveryProductCreatedDate.split("\\."); // 포맷 형식에 맞게끔 . 기호를 기준으로 필요한 문자열만 추출
                                LocalDateTime deliveryProductNowDateTime = LocalDateTime.parse(deliveryProductCreatedDateSplit[0], formatter);

                                // 배송 제품들 주문 확정 자동화 (7일 기준)
                                if (deliveryProductNowDateTime.isBefore(nowDateTime.minusDays(7)) ||
                                        (deliveryProductNowDateTime.getYear() == nowDateTime.minusDays(7).getYear() &&
                                                deliveryProductNowDateTime.getMonthValue() == nowDateTime.minusDays(7).getMonthValue() &&
                                                deliveryProductNowDateTime.getDayOfMonth() == nowDateTime.minusDays(7).getDayOfMonth())) {

                                    log.info("배송 제품 구매 확정 처리 진입 ");
                                    log.info("주문 번호 : {}", eachDeliveryOrderProduct.getOrderNumber());
                                    log.info("제품 코드 : {}", eachDeliveryOrderProduct.getProductClassificationCode());

                                    jpaQueryFactory
                                            .update(orderInProduct)
                                            .set(orderInProduct.completePurchaseAt, LocalDateTime.now())
                                            .set(orderInProduct.completePurchaseCheck, "Y")
                                            .where(orderInProduct.orderInProductId.eq(eachDeliveryOrderProduct.getOrderInProductId()))
                                            .execute();
                                }
                            });


                    entityManager.flush();
                    entityManager.clear();

                    if (!expressBannerList.isEmpty()) {
                        resultBannerList.addAll(expressBannerList);
                    }

                    if (!nonExpressBannerList.isEmpty()) {
                        resultBannerList.addAll(nonExpressBannerList);
                    }

                    totalBannerResponseDto = TotalBannerResponseDto.builder()
                            .totalBannerCount(bannerTotalCount)
                            .responseBannerList(resultBannerList)
                            .build();

                    return RepeatStatus.FINISHED;
                }).build();
    }

}
