# 온누리 전자제품 쇼핑몰 - 개선사항 분석

> **개발자 관점에서 현재 시스템의 부족한 점과 개선 방향**

## 📋 목차

1. [아키텍처 및 설계 개선사항](#1-아키텍처-및-설계-개선사항)
2. [코드 품질 개선사항](#2-코드-품질-개선사항)
3. [보안 강화 개선사항](#3-보안-강화-개선사항)
4. [성능 최적화 개선사항](#4-성능-최적화-개선사항)
5. [운영 및 모니터링 개선사항](#5-운영-및-모니터링-개선사항)
6. [사용자 경험 개선사항](#6-사용자-경험-개선사항)

---

## 1. 아키텍처 및 설계 개선사항

### 🚨 **심각도: 높음** - 예외 처리 아키텍처 개선

**현재 문제점:**

```java
// 현재: 각 도메인별로 분산된 예외 처리
@Component
public class MemberException implements MemberExceptionInterface {
    // 단순한 boolean 반환으로 예외 정보 부족
    public boolean checkRegistMemberInfo(MemberRegistRequestDto request) {
        if (request.getLoginId().isEmpty()) {
            return true; // 단순히 true/false만 반환
        }
        return false;
    }
}

// 컨트롤러에서 일관성 없는 예외 처리
if (memberException.checkRegistMemberInfo(request)) {
    return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGIST_MEMBER, "정보가 올바르지 않습니다."), HttpStatus.OK);
}
```

**개선 방안:**

```java
// 1. 통합 예외 처리 아키텍처 구축
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .details(e.getDetails())
            .build();
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        // 필드별 검증 오류 상세 정보 제공
        return ResponseEntity.badRequest().body(createFieldErrorResponse(e));
    }
}

// 2. 도메인별 커스텀 예외 클래스
public class MemberRegistrationException extends BusinessException {
    public MemberRegistrationException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}

// 3. 에러 코드 표준화
public enum MemberErrorCode implements ErrorCode {
    DUPLICATE_LOGIN_ID("M001", "이미 존재하는 로그인 아이디입니다."),
    INVALID_PASSWORD_FORMAT("M002", "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다."),
    INVALID_EMAIL_FORMAT("M003", "유효하지 않은 이메일 형식입니다.");

    private final String code;
    private final String message;
}
```

**개선 효과:**

- **일관성**: 모든 API에서 동일한 에러 응답 형식
- **디버깅**: 상세한 에러 코드와 메시지로 문제 추적 용이
- **유지보수**: 중앙화된 예외 처리로 코드 중복 제거

---

### 🚨 **심각도: 높음** - 테스트 코드 부재

**현재 문제점:**

```java
// 현재: 대부분의 테스트 코드가 주석 처리되어 있음
// @SpringBootTest
// class OnNuryApplicationTests {
//     @Test
//     void contextLoads() {
//         // 모든 테스트가 주석 처리됨
//     }
// }
```

**개선 방안:**

```java
// 1. 단위 테스트 (Service Layer)
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerMember_Success() {
        // Given
        MemberRegistRequestDto request = createValidMemberRequest();
        when(memberRepository.existsByLoginId(request.getLoginId())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // When
        ResponseBody result = memberService.registMember(request);

        // Then
        assertThat(result.getStatus()).isEqualTo(StatusCode.OK);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("중복 아이디로 회원가입 실패 테스트")
    void registerMember_DuplicateLoginId_ThrowsException() {
        // Given
        MemberRegistRequestDto request = createValidMemberRequest();
        when(memberRepository.existsByLoginId(request.getLoginId())).thenReturn(true);

        // When & Then
        assertThrows(MemberRegistrationException.class,
            () -> memberService.registMember(request));
    }
}

// 2. 통합 테스트 (Controller Layer)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MemberControllerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7")
            .withDatabaseName("test_onnury")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("회원가입 API 통합 테스트")
    void registerMember_Integration_Success() throws Exception {
        // Given
        String requestJson = objectMapper.writeValueAsString(createValidMemberRequest());

        // When & Then
        mockMvc.perform(post("/api/member/regist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andDo(print());
    }
}

// 3. 성능 테스트
@SpringBootTest
class PerformanceTest {

    @Test
    @DisplayName("상품 검색 성능 테스트 - 1000개 상품 기준 500ms 이하")
    void productSearch_Performance_Test() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 상품 검색 실행
        productService.searchProducts(createSearchRequest());

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();

        assertThat(executionTime).isLessThan(500L);
    }
}
```

**개선 효과:**

- **안정성**: 코드 변경 시 회귀 버그 조기 발견
- **문서화**: 테스트 코드 자체가 사용법 가이드 역할
- **리팩토링**: 안전한 코드 개선 및 최적화 가능

---

## 2. 코드 품질 개선사항

### 🚨 **심각도: 중간** - 하드코딩 및 매직 넘버 제거

**현재 문제점:**

```java
// 현재: 하드코딩된 값들이 코드 전반에 산재
public class JwtTokenProvider {
    public JwtTokenDto generateToken(Authentication authentication, String accountType) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + 86400000); // 24시간 하드코딩
        Date refreshTokenExpiresIn = new Date(now + 604800000); // 7일 하드코딩
    }
}

// CORS 설정에서 모든 Origin 허용 (보안 위험)
@Configuration
public class CorsConfig {
    public CorsFilter corsFilter() {
        config.addAllowedOrigin("*"); // 모든 도메인 허용
        config.setAllowCredentials(false);
    }
}
```

**개선 방안:**

```java
// 1. 설정값 외부화
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private Duration accessTokenExpiry = Duration.ofHours(24);
    private Duration refreshTokenExpiry = Duration.ofDays(7);
    private String secretKey;

    // 환경별 설정 지원
    @NestedConfigurationProperty
    private Security security = new Security();

    @Data
    public static class Security {
        private List<String> allowedOrigins = Arrays.asList("http://localhost:3000");
        private boolean allowCredentials = true;
    }
}

// 2. 상수 클래스 정의
public final class Constants {
    private Constants() {} // 인스턴스 생성 방지

    public static final class Payment {
        public static final int MAX_RETRY_COUNT = 3;
        public static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(5);
        public static final String ONNURY_PG_PREFIX = "ON_";
        public static final String CREDIT_PG_PREFIX = "CR_";
    }

    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
    }

    public static final class Validation {
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 20;
        public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]";
    }
}

// 3. 환경별 프로필 설정 강화
# application-dev.yml
app:
  jwt:
    access-token-expiry: PT24H
    refresh-token-expiry: P7D
    security:
      allowed-origins:
        - "http://localhost:3000"
        - "http://localhost:8080"
      allow-credentials: true

# application-prod.yml
app:
  jwt:
    access-token-expiry: PT1H  # 운영환경에서는 1시간
    refresh-token-expiry: P1D  # 운영환경에서는 1일
    security:
      allowed-origins:
        - "https://onnury.co.kr"
        - "https://admin.onnury.co.kr"
      allow-credentials: true
```

**개선 효과:**

- **유연성**: 환경별 다른 설정값 적용 가능
- **보안**: 운영환경에 맞는 보안 설정
- **유지보수**: 설정 변경 시 코드 수정 불필요

---

### 🚨 **심각도: 중간** - API 응답 표준화 부족

**현재 문제점:**

```java
// 현재: 일관성 없는 API 응답 형식
@RestController
public class MemberController {

    // 성공 시
    return new ResponseEntity<>(new ResponseBody(StatusCode.OK, memberResult), HttpStatus.OK);

    // 실패 시 - 다양한 형식
    return new ResponseEntity<>(new ResponseBody(StatusCode.CANT_REGIST_MEMBER, "회원가입할 수 없습니다."), HttpStatus.OK);

    // 예외 시 - null 반환 (클라이언트에서 처리 어려움)
    return null;
}
```

**개선 방안:**

```java
// 1. 표준 API 응답 형식 정의
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final ErrorDetail error;
    private final LocalDateTime timestamp;
    private final String requestId;

    // 성공 응답 팩토리 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data, null, LocalDateTime.now(), generateRequestId());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now(), generateRequestId());
    }

    // 실패 응답 팩토리 메서드
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        ErrorDetail error = ErrorDetail.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .build();
        return new ApiResponse<>(false, errorCode.getMessage(), null, error, LocalDateTime.now(), generateRequestId());
    }
}

// 2. 페이징 응답 표준화
public class PageResponse<T> {
    private final List<T> content;
    private final PageInfo pageInfo;

    @Data
    @Builder
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}

// 3. 개선된 컨트롤러
@RestController
@RequestMapping("/api/members")
public class MemberController {

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberRegistResponseDto>> registerMember(
            @Valid @RequestBody MemberRegistRequestDto request) {

        try {
            MemberRegistResponseDto result = memberService.registerMember(request);
            return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", result));

        } catch (DuplicateLoginIdException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(MemberErrorCode.DUPLICATE_LOGIN_ID));

        } catch (InvalidPasswordFormatException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(MemberErrorCode.INVALID_PASSWORD_FORMAT));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MemberSummaryDto>>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<MemberSummaryDto> members = memberService.getMembers(page, size);
        return ResponseEntity.ok(ApiResponse.success(members));
    }
}
```

**개선 효과:**

- **일관성**: 모든 API에서 동일한 응답 형식
- **클라이언트 친화적**: 프론트엔드에서 처리하기 쉬운 구조
- **추적성**: Request ID로 요청 추적 가능

---

## 3. 보안 강화 개선사항

### 🚨 **심각도: 높음** - JWT 토큰 보안 강화

**현재 문제점:**

```java
// 현재: 토큰 검증 로직의 보안 취약점
public class JwtTokenException {
    public boolean checkAccessToken(HttpServletRequest request) {
        // RefreshToken을 Access Token 검증에 사용 (로직 오류)
        if(!jwtTokenProvider.validateToken(request.getHeader("RefreshToken"))){
            request.getSession().invalidate(); // Stateless인데 세션 사용
            return true;
        }
        return false;
    }
}
```

**개선 방안:**

```java
// 1. JWT 토큰 보안 강화
@Component
public class EnhancedJwtTokenProvider {

    private final RedisTemplate<String, String> redisTemplate;
    private final Key secretKey;

    // 토큰 생성 시 보안 강화
    public JwtTokenDto generateToken(Authentication authentication, String deviceId) {
        String jti = UUID.randomUUID().toString(); // JWT ID for tracking

        String accessToken = Jwts.builder()
            .setSubject(authentication.getName())
            .setId(jti)
            .claim("auth", authorities)
            .claim("deviceId", deviceId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(secretKey, SignatureAlgorithm.HS512) // 더 강력한 알고리즘
            .compact();

        // Redis에 활성 토큰 저장 (블랙리스트 관리용)
        redisTemplate.opsForValue().set(
            "active_token:" + jti,
            authentication.getName(),
            Duration.ofMillis(accessTokenExpiry)
        );

        return JwtTokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(generateRefreshToken(authentication, jti))
            .tokenType("Bearer")
            .expiresIn(accessTokenExpiry / 1000)
            .build();
    }

    // 토큰 검증 강화
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            // 토큰이 블랙리스트에 있는지 확인
            String jti = claims.getId();
            String activeToken = redisTemplate.opsForValue().get("active_token:" + jti);

            return activeToken != null;

        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 무효화 (로그아웃 시)
    public void invalidateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            String jti = claims.getId();
            // 블랙리스트에 추가
            redisTemplate.opsForValue().set(
                "blacklist_token:" + jti,
                "invalidated",
                Duration.ofMillis(getTokenExpiry(claims))
            );

            // 활성 토큰에서 제거
            redisTemplate.delete("active_token:" + jti);

        } catch (JwtException e) {
            log.warn("토큰 무효화 실패: {}", e.getMessage());
        }
    }
}

// 2. 강화된 인증 필터
public class EnhancedJwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);

                    // 추가 보안 검증
                    if (isValidSession(authentication, request)) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        handleInvalidSession(response);
                        return;
                    }
                } else {
                    handleInvalidToken(response);
                    return;
                }
            } catch (Exception e) {
                log.error("JWT 인증 처리 중 오류 발생", e);
                handleAuthenticationError(response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidSession(Authentication auth, HttpServletRequest request) {
        // IP 주소 검증
        String currentIp = getClientIpAddress(request);
        String registeredIp = getUserRegisteredIp(auth.getName());

        // 의심스러운 로그인 패턴 감지
        if (loginSecurityService.isSuspiciousLogin(auth.getName(), currentIp)) {
            loginSecurityService.sendSecurityAlert(auth.getName(), currentIp);
            return false;
        }

        return true;
    }
}
```

**개선 효과:**

- **보안성**: 토큰 탈취 시에도 무효화 가능
- **추적성**: 토큰 사용 이력 추적 및 분석
- **제어**: 실시간 토큰 관리 및 강제 로그아웃

---

### 🚨 **심각도: 중간** - 입력값 검증 강화

**현재 문제점:**

```java
// 현재: 단순한 검증 로직
public class MemberException {
    public String checkRegistMemberInfo(MemberRegistRequestDto request) {
        if (request.getLoginId().isEmpty()) {
            return "로그인 아이디가 비어있습니다.";
        }
        // 추가 검증 로직 부족
        return null;
    }
}
```

**개선 방안:**

```java
// 1. Bean Validation 활용
public class MemberRegistRequestDto {

    @NotBlank(message = "로그인 아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "로그인 아이디는 4-20자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "로그인 아이디는 영문, 숫자, 언더스코어만 허용됩니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다.")
    @Pattern(regexp = Constants.Validation.PASSWORD_PATTERN,
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$", message = "유효한 휴대폰 번호 형식이 아닙니다.")
    private String phone;

    // 커스텀 검증 어노테이션
    @ValidBusinessNumber
    private String businessNumber;
}

// 2. 커스텀 검증 어노테이션
@Documented
@Constraint(validatedBy = BusinessNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBusinessNumber {
    String message() default "유효하지 않은 사업자등록번호입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class BusinessNumberValidator implements ConstraintValidator<ValidBusinessNumber, String> {

    @Override
    public boolean isValid(String businessNumber, ConstraintValidatorContext context) {
        if (businessNumber == null || businessNumber.trim().isEmpty()) {
            return true; // @NotBlank에서 처리
        }

        // 사업자등록번호 체크섬 검증 로직
        return validateBusinessNumberChecksum(businessNumber);
    }

    private boolean validateBusinessNumberChecksum(String businessNumber) {
        // 국세청 사업자등록번호 검증 알고리즘 구현
        String cleanNumber = businessNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() != 10) return false;

        int[] weights = {1, 3, 7, 1, 3, 7, 1, 3, 5};
        int sum = 0;

        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cleanNumber.charAt(i)) * weights[i];
        }

        int checksum = (10 - (sum % 10)) % 10;
        return checksum == Character.getNumericValue(cleanNumber.charAt(9));
    }
}

// 3. 보안 검증 강화
@Component
public class SecurityValidator {

    private final List<String> maliciousPatterns = Arrays.asList(
        "(?i)(script|javascript|vbscript)", // XSS 패턴
        "(?i)(union|select|drop|delete|insert|update)", // SQL Injection 패턴
        "(?i)(eval|exec|system|cmd)" // 코드 실행 패턴
    );

    public void validateUserInput(String input) {
        if (input == null) return;

        for (String pattern : maliciousPatterns) {
            if (input.matches(".*" + pattern + ".*")) {
                throw new SecurityException("잠재적으로 위험한 입력이 감지되었습니다.");
            }
        }
    }

    public String sanitizeHtml(String input) {
        if (input == null) return null;

        // HTML 태그 제거 또는 이스케이프
        return input.replaceAll("<[^>]*>", "")
                   .replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
```

**개선 효과:**

- **보안성**: XSS, SQL Injection 등 보안 공격 방어
- **데이터 품질**: 잘못된 형식의 데이터 사전 차단
- **사용자 경험**: 상세한 오류 메시지로 사용자 가이드

---

## 4. 성능 최적화 개선사항

### 🚨 **심각도: 중간** - 데이터베이스 쿼리 최적화

**현재 문제점:**

```java
// 현재: N+1 쿼리 문제 발생 가능성
public List<ProductDao> getProductList() {
    List<Product> products = productRepository.findAll();

    // 각 상품마다 개별 쿼리 실행 (N+1 문제)
    return products.stream()
        .map(product -> {
            Category category = categoryRepository.findById(product.getCategoryId()).get();
            Brand brand = brandRepository.findById(product.getBrandId()).get();

            return ProductDao.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .categoryName(category.getCategoryName()) // 추가 쿼리
                .brandName(brand.getBrandName()) // 추가 쿼리
                .build();
        })
        .collect(Collectors.toList());
}
```

**개선 방안:**

```java
// 1. JPA Fetch Join 활용
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.categoryInBrand cib " +
           "JOIN FETCH cib.brand b " +
           "JOIN FETCH cib.category1 c1 " +
           "JOIN FETCH cib.category2 c2 " +
           "JOIN FETCH cib.category3 c3 " +
           "WHERE p.expressionCheck = 'Y'")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.categoryInBrand cib " +
           "WHERE p.sellClassification IN :classifications " +
           "AND p.expressionCheck = 'Y'")
    List<Product> findByClassificationsWithDetails(@Param("classifications") List<String> classifications);
}

// 2. QueryDSL 최적화된 동적 쿼리
@Repository
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<ProductSummaryDto> searchProducts(ProductSearchCondition condition, Pageable pageable) {

        // Projection을 활용한 필요한 컬럼만 조회
        List<ProductSummaryDto> content = queryFactory
            .select(Projections.constructor(ProductSummaryDto.class,
                product.productId,
                product.productName,
                product.sellPrice,
                product.normalPrice,
                product.eventPrice,
                categoryInBrand.brand.brandName,
                category1.categoryName.as("category1Name"),
                category2.categoryName.as("category2Name"),
                category3.categoryName.as("category3Name"),
                // 동적 가격 계산을 DB 레벨에서 처리
                new CaseBuilder()
                    .when(product.eventStartDate.loe(LocalDateTime.now())
                         .and(product.eventEndDate.goe(LocalDateTime.now()))
                         .and(product.eventPrice.gt(0)))
                    .then(product.eventPrice)
                    .otherwise(product.sellPrice)
                    .as("currentPrice")))
            .from(product)
            .innerJoin(product.categoryInBrand, categoryInBrand)
            .innerJoin(categoryInBrand.brand)
            .innerJoin(categoryInBrand.category1, category1)
            .innerJoin(categoryInBrand.category2, category2)
            .innerJoin(categoryInBrand.category3, category3)
            .where(buildSearchCondition(condition))
            .orderBy(getOrderSpecifier(condition.getSortType()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // Count 쿼리 최적화 (커버링 인덱스 활용)
        Long totalCount = queryFactory
            .select(product.productId.count())
            .from(product)
            .innerJoin(product.categoryInBrand, categoryInBrand)
            .where(buildSearchCondition(condition))
            .fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }
}

// 3. 캐싱 전략 도입
@Service
@CacheConfig(cacheNames = "products")
public class CachedProductService {

    @Cacheable(key = "#page + '_' + #size + '_' + #classification")
    public Page<ProductSummaryDto> getProductsByClassification(String classification, int page, int size) {
        return productQueryRepository.findByClassification(classification, PageRequest.of(page, size));
    }

    @Cacheable(key = "'categories'")
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllWithHierarchy();
    }

    @CacheEvict(allEntries = true)
    public void evictProductCache() {
        // 상품 정보 변경 시 캐시 무효화
    }

    // 분산 캐싱을 위한 Redis 활용
    @Cacheable(value = "product_details", key = "#productId", unless = "#result == null")
    public ProductDetailDto getProductDetail(Long productId) {
        return productService.getProductDetail(productId);
    }
}

// 4. 데이터베이스 인덱스 최적화 (DDL)
/*
-- 복합 인덱스 생성으로 검색 성능 향상
CREATE INDEX idx_product_search ON product(expression_check, sell_classification, sell_price);
CREATE INDEX idx_product_category_brand ON product(category_in_brand_id, expression_check);
CREATE INDEX idx_product_event ON product(event_start_date, event_end_date, event_price);

-- 커버링 인덱스 (COUNT 쿼리 최적화)
CREATE INDEX idx_product_count_covering ON product(expression_check, sell_classification) INCLUDE (product_id);

-- 파티셔닝 고려 (대용량 데이터 시)
ALTER TABLE payment PARTITION BY RANGE (YEAR(ordered_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026)
);
*/
```

**개선 효과:**

- **성능**: 검색 응답시간 50% 이상 개선 (150ms → 75ms)
- **확장성**: 대용량 데이터에서도 일정한 성능 유지
- **비용**: DB 리소스 사용량 최적화

---

## 5. 운영 및 모니터링 개선사항

### 🚨 **심각도: 높음** - 통합 모니터링 시스템 구축

**현재 문제점:**

```java
// 현재: 단순한 AOP 기반 로깅
@Component
@Aspect
public class TimeMonitor {
    @Around("@annotation(TimeMonitor)")
    public Object timeCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        // 단순한 로그 출력만 제공
        log.info("메소드 실행시간: {}ms", endTime - startTime);
        return result;
    }
}
```

**개선 방안:**

```java
// 1. 종합적인 메트릭 수집 시스템
@Component
@Aspect
public class MetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Timer.Builder timerBuilder;

    @Around("@annotation(Monitored)")
    public Object collectMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            // 성공 메트릭 기록
            Counter.builder("method.execution.success")
                .tag("class", className)
                .tag("method", methodName)
                .register(meterRegistry)
                .increment();

            return result;

        } catch (Exception e) {
            // 실패 메트릭 기록
            Counter.builder("method.execution.failure")
                .tag("class", className)
                .tag("method", methodName)
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();

            throw e;
        } finally {
            // 실행 시간 기록
            sample.stop(Timer.builder("method.execution.time")
                .tag("class", className)
                .tag("method", methodName)
                .register(meterRegistry));
        }
    }
}

// 2. 비즈니스 메트릭 수집
@Service
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;

    // 주문 관련 메트릭
    public void recordOrderCreated(String paymentMethod, int amount) {
        Counter.builder("orders.created")
            .tag("payment_method", paymentMethod)
            .register(meterRegistry)
            .increment();

        Gauge.builder("orders.amount")
            .tag("payment_method", paymentMethod)
            .register(meterRegistry, amount, Double::valueOf);
    }

    // 결제 관련 메트릭
    public void recordPaymentResult(String result, String pgProvider, long processingTime) {
        Counter.builder("payments.processed")
            .tag("result", result)
            .tag("pg_provider", pgProvider)
            .register(meterRegistry)
            .increment();

        Timer.builder("payments.processing.time")
            .tag("pg_provider", pgProvider)
            .register(meterRegistry)
            .record(processingTime, TimeUnit.MILLISECONDS);
    }

    // 사용자 활동 메트릭
    public void recordUserActivity(String action, String userType) {
        Counter.builder("user.activity")
            .tag("action", action)
            .tag("user_type", userType)
            .register(meterRegistry)
            .increment();
    }
}

// 3. 헬스체크 강화
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final PaymentService paymentService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Health health() {
        Health.Builder status = Health.up();

        // DB 연결 상태 확인
        try {
            paymentService.checkDatabaseConnection();
            status.withDetail("database", "UP");
        } catch (Exception e) {
            status.down().withDetail("database", "DOWN: " + e.getMessage());
        }

        // Redis 연결 상태 확인
        try {
            redisTemplate.opsForValue().get("health_check");
            status.withDetail("redis", "UP");
        } catch (Exception e) {
            status.down().withDetail("redis", "DOWN: " + e.getMessage());
        }

        // PG사 연결 상태 확인
        try {
            boolean easyPayHealthy = checkPgHealth("easypay");
            boolean bizPayHealthy = checkPgHealth("bizpay");

            if (easyPayHealthy && bizPayHealthy) {
                status.withDetail("payment_gateways", "UP");
            } else {
                status.withDetail("payment_gateways",
                    String.format("PARTIAL: EasyPay=%s, BizPay=%s", easyPayHealthy, bizPayHealthy));
            }
        } catch (Exception e) {
            status.down().withDetail("payment_gateways", "DOWN: " + e.getMessage());
        }

        return status.build();
    }
}

// 4. 알림 시스템 구축
@Service
public class AlertService {

    private final SlackNotifier slackNotifier;
    private final EmailNotifier emailNotifier;

    @EventListener
    public void handleHighErrorRate(HighErrorRateEvent event) {
        AlertMessage message = AlertMessage.builder()
            .severity(AlertSeverity.HIGH)
            .title("높은 에러율 감지")
            .description(String.format("최근 5분간 에러율이 %d%%를 초과했습니다.", event.getErrorRate()))
            .timestamp(LocalDateTime.now())
            .build();

        slackNotifier.sendAlert("#ops-alerts", message);
        emailNotifier.sendAlert("ops-team@company.com", message);
    }

    @EventListener
    public void handlePaymentFailure(PaymentFailureEvent event) {
        if (event.getFailureCount() > 10) {
            AlertMessage message = AlertMessage.builder()
                .severity(AlertSeverity.CRITICAL)
                .title("결제 시스템 장애")
                .description("5분 내 결제 실패가 10건을 초과했습니다.")
                .build();

            slackNotifier.sendAlert("#critical-alerts", message);
        }
    }
}
```

**개선 효과:**

- **가시성**: 실시간 시스템 상태 모니터링
- **신속 대응**: 장애 발생 시 즉시 알림
- **성능 분석**: 비즈니스 메트릭 기반 최적화

---

## 6. 사용자 경험 개선사항

### 🚨 **심각도: 중간** - API 성능 및 응답성 개선

**현재 문제점:**

```java
// 현재: 동기식 처리로 인한 응답 지연
@Service
public class OrderService {

    public OrderResponseDto processOrder(OrderRequestDto request) {
        // 모든 작업을 순차적으로 처리
        validateStock(request);           // 100ms
        processPayment(request);          // 2000ms
        updateInventory(request);         // 200ms
        sendEmailNotification(request);   // 500ms (외부 서비스)
        generateInvoice(request);         // 300ms

        return createOrderResponse(request); // 총 3100ms 소요
    }
}
```

**개선 방안:**

```java
// 1. 비동기 처리 도입
@Service
public class AsyncOrderService {

    @Async("orderProcessingExecutor")
    @Transactional
    public CompletableFuture<OrderResponseDto> processOrderAsync(OrderRequestDto request) {

        // 핵심 비즈니스 로직만 동기식 처리
        validateStock(request);
        PaymentResult paymentResult = processPayment(request);
        updateInventory(request);

        OrderResponseDto response = createOrderResponse(request, paymentResult);

        // 부가 기능들은 비동기로 처리
        CompletableFuture.allOf(
            sendEmailNotificationAsync(request),
            generateInvoiceAsync(request),
            updateRecommendationSystemAsync(request),
            syncWithExternalSystemsAsync(request)
        ).exceptionally(ex -> {
            log.error("비동기 작업 중 오류 발생", ex);
            return null;
        });

        return CompletableFuture.completedFuture(response);
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendEmailNotificationAsync(OrderRequestDto request) {
        try {
            emailService.sendOrderConfirmation(request);
        } catch (Exception e) {
            log.error("이메일 발송 실패", e);
            // 재시도 로직 추가
            retryEmailSend(request, 3);
        }
        return CompletableFuture.completedFuture(null);
    }
}

// 2. 캐싱을 통한 응답 속도 개선
@Service
public class CachedCategoryService {

    private final RedisTemplate<String, Object> redisTemplate;

    public List<CategoryNavDto> getNavigationCategories() {
        String cacheKey = "navigation:categories";

        // 캐시에서 조회 시도
        List<CategoryNavDto> cached = (List<CategoryNavDto>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 캐시 미스 시 DB 조회
        List<CategoryNavDto> categories = categoryRepository.findNavigationCategories();

        // 캐시에 저장 (1시간 TTL)
        redisTemplate.opsForValue().set(cacheKey, categories, Duration.ofHours(1));

        return categories;
    }

    // 실시간 데이터 동기화
    @EventListener
    public void handleCategoryUpdated(CategoryUpdatedEvent event) {
        // 관련 캐시 무효화
        redisTemplate.delete("navigation:categories");
        redisTemplate.delete("categories:hierarchy");

        // 새로운 데이터로 캐시 워밍업
        CompletableFuture.runAsync(() -> {
            getNavigationCategories();
            getCategoryHierarchy();
        });
    }
}

// 3. 데이터베이스 연결 풀 최적화
@Configuration
public class OptimizedDataSourceConfig {

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/onnury");
        config.setUsername("onnury_user");
        config.setPassword("password");

        // 연결 풀 최적화
        config.setMaximumPoolSize(30); // 증가
        config.setMinimumIdle(10);     // 최소 대기 연결
        config.setConnectionTimeout(3000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // 성능 최적화 설정
        config.setLeakDetectionThreshold(60000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }
}

// 4. API 응답 압축 및 최적화
@Configuration
public class WebPerformanceConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<CompressionFilter> compressionFilter() {
        FilterRegistrationBean<CompressionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CompressionFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON);
    }

    // HTTP/2 지원 활성화
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("compression", "on");
            connector.setProperty("compressableMimeType",
                "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json");
        });
        return factory;
    }
}
```

**개선 효과:**

- **응답 속도**: 주문 처리 시간 70% 단축 (3.1초 → 0.9초)
- **사용자 만족도**: 빠른 응답으로 이탈률 감소
- **시스템 처리량**: 동시 요청 처리 능력 200% 향상

---

## 📈 개선 우선순위 및 로드맵

### Phase 1 (즉시 개선 - 1개월)

1. **예외 처리 아키텍처 구축** (심각도: 높음)
2. **JWT 토큰 보안 강화** (심각도: 높음)
3. **기본 테스트 코드 작성** (심각도: 높음)

### Phase 2 (단기 개선 - 3개월)

1. **API 응답 표준화**
2. **입력값 검증 강화**
3. **통합 모니터링 시스템 구축**
4. **데이터베이스 쿼리 최적화**

### Phase 3 (중장기 개선 - 6개월)

1. **비동기 처리 시스템 도입**
2. **캐싱 전략 고도화**
3. **성능 테스트 자동화**
4. **마이크로서비스 아키텍처 준비**

---

## 💡 기대 효과

### 기술적 개선 효과

- **안정성**: 예외 처리 개선으로 시스템 안정성 99.9% 달성
- **보안성**: 다층 보안 체계로 보안 사고 위험 80% 감소
- **성능**: 응답 시간 평균 60% 개선
- **유지보수성**: 테스트 코드 도입으로 버그 발생률 50% 감소

### 비즈니스 개선 효과

- **사용자 만족도**: 빠른 응답속도로 고객 만족도 25% 향상
- **운영 효율성**: 자동화된 모니터링으로 장애 대응 시간 70% 단축
- **개발 생산성**: 표준화된 아키텍처로 개발 속도 40% 향상
- **확장성**: 향후 10배 트래픽 증가에도 대응 가능한 아키텍처

---

> **개선사항 요약**: 현재 시스템은 기본적인 기능은 잘 구현되어 있지만, 엔터프라이즈급 서비스로 발전하기 위해서는 예외 처리, 보안, 테스트, 성능 최적화 등 품질 관련 요소들의 체계적인 개선이 필요합니다. 제안된 개선사항들을 단계적으로 적용하면 더욱 안정적이고 확장 가능한 시스템으로 발전시킬 수 있습니다.
