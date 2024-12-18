package com.onnury.configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.onnury.payment.domain.QOrderInProduct.orderInProduct;
import static com.onnury.product.domain.QProduct.product;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SchedulerConfig {

    private final JobLauncher jobLauncher;
    private final BatchConfig batchConfig;
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    // 스케줄링 Job 파라미터 Bean 등록
    public JobParameters setJobParameter() {

        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));

        return new JobParameters(confMap);
    }


    // 매일 오전 12시에 배너 노출 여부 확인 후 노출 형성 스케줄
    // 매일 오전 12시에 7일 혹은 20일 이상 주문 결제 중인 상태인 주문 이력 확정 업데이트 스케줄

    //@Scheduled(cron = "0 * * * * *") // 1분 주기
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 12시 주기
    public void runJob() {
        try {
            log.info("스케줄러 실행");

            jobLauncher.run(batchConfig.bannerExpressJob(1), setJobParameter());
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | JobRestartException e) {
            log.error(e.getMessage());
        }
    }

}
