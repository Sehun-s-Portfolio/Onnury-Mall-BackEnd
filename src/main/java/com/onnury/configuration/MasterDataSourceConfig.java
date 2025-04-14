package com.onnury.configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Slf4j
@Order(1)
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@Configuration
public class MasterDataSourceConfig {
    private final String MASTER_DATA_SOURCE = "MasterDataSource";
    private final String MASTER_TRANSACTION_MANAGER = "MasterTransactionManager";

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Value("${spring.datasource.username}")
    private String userName;

    @Value("${spring.datasource.password}")
    private String password;

    // master database DataSource
    @Primary
    @Bean(MASTER_DATA_SOURCE)
//    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
                .url(url) // URL을 명시적으로 지정
                .driverClassName(driver) // 드라이버 클래스명을 명시적으로 지정
                .username(userName)
                .password(password)
                .build();
    }

    // Replication Master Transaction Manager
    @Primary
    @Bean(MASTER_TRANSACTION_MANAGER)
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        log.info("MASTER DB - JPA 트랜잭션 매니저 Bean 등록");
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager){
        log.info("QueryDSL 설정 - EntityManager : {})", entityManager);
        return new JPAQueryFactory(entityManager);
    }
}
