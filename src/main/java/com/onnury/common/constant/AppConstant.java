package com.onnury.common.constant;

public class AppConstant {
	///// Transaction Core
	public static final String TRSC_ID = "trscId";		// 트랜잭션ID
	public static final String ACES_DVCD = "acesDvcd";	// 내/외부망 구분	
	public static final String PRGM_TY = "prgmTy";		// 프로그램 구분
	public static final String PRGM_ID = "prgmId";		// 프로그램 ID
	public static final String SVR_NM = "svrNm";		// 서버명	
	public static final String BRWS_NM = "brwsNm";		// 브라우저명
	public static final String SCRN_ID = "scrnId";		// 스크린 ID
	
	///// 개발환경 
	public static final String EDMS_ENV_LOCAL = "L";	// 로컬
	public static final String EDMS_ENV_DEV = "D";		// 개발
	public static final String EDMS_ENV_QA = "Q";		// QA
	public static final String EDMS_ENV_PROD = "P";		// 리얼
	
	///// 프로그램 구분
	public static final String EDMS_PRGM_ADM = "admin";	
	public static final String EDMS_PRGM_API = "api";
	public static final String EDMS_PRGM_CS = "cs";
	public static final String EDMS_PRGM_ONL = "online";
	
	///// HTTP 헤더
	public static final String HTTP_HEADER_X_FORWARDED_FOR = "x-forwarded-for";
	public static final String HTTP_HEADER_X_WEB_HOSTNAME = "x-web-hostname";
	
	///// 내/외부망 구분
	public static final String ACCESS_INSD = "INSD";	// 내부망	
	public static final String ACCESS_EXTL = "EXTL";	// 외부망

	public static final String YN_Y = "Y";
	public static final String YN_N = "N";
	
	// 사용자정보
	public static final String USER_ID = "userId";	
	public static final String USER_NM = "userNm";	
	
	// 로그인 방식
	public static final String AUTHE_SSO 		= "sso";
	public static final String AUTHE_DB 		= "db";
	public static final String AUTHE_JWT 		= "jwt";	
	
	// 쿠키명
	public static final String SESSION_EXPIRY_COOKIE	= "SESSIONEXP";
}

