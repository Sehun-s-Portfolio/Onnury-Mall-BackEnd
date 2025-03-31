package com.onnury.common.constant;

import lombok.Getter;

@Getter
public enum AppErrorCode {
	/////
	// KB 에러(2001~9991)
	KB_NO_GOODS_ID("3301", "상품아이디가 존재하지 않습니다."),
	KB_INPUT_DATA("3999", "파라메터 체크 중 오류 발생"),
	KB_INSERT_PRIZE("2999", "상품권 발행 처리 중 오류발생"),
	KB_SELECT_FAIL("4002", "상품권 조회 실패"),
	KB_SUCCESS("1000", "성공"),

	/////
	// 즐거운 에러
	JGN_SUCCESS("0000", "성공"),
	JGN_SELECT_COUPON_DETAIL("0001", "쿠폰 상세조회에 실패하였습니다."),
	JGN_ERROR("9999", "실패"),


	/////
	// 내부에러(0~999)
	SUCCESS("0", "성공"),
	COUPON_INFO("100", "쿠폰 정보 오류"),
	INPUT_DATA("101", "입력값 오류"),
	ISSUE_COUPON("102", "쿠폰 발급에 실패하였습니다."),
	GET_PRIZE_INFO("103", "쿠폰 정보를 찾을 수 없습니다."),
	ALREADY_ISSUED_COUPON("104", "발급이 완료된 쿠폰입니다."),
	ZLGOON_HTTP_ERROR("105", "쿠폰 발급 요청에 실패하였습니다."),
	ZLGOON_SELLECT_ERROR("106", "쿠폰 조회에 실패하였습니다."),
	DETAIL_SELECT_FAIL("107","상세 조회에 실패하였습니다."),
	GOODS_SELECT_FAIL("108", "선택 쿠폰 제품들 정보 조회에 실패하였습니다."),
	AVAILABLE_COUPON_SELECT_FAIL("109", "쿠폰함 정보 조회에 실패하였습니다."),
	HISTORY_COUPON_SELECT_FAIL("110", "쿠폰 히스토리 정보 조회에 실패하였습니다."),
	FAQ_SELECT_FAIL("111", "FAQ 정보 조회에 실패하였습니다."),
	INQUIRY_WRITE_FAIL("112", "문의 작성에 실패하였습니다."),
	INQUIRY_SELECT_FAIL("113", "문의 이력 정보 조회에 실패하였습니다."),
	GET_GOODS_INFO("114", "쿠폰발급 상품정보 조회에 실패하였습니다."),
	DATA_NOT_FOUND("999","데이터가 없습니다.");

    private final String errCode;
	private final String errMsg;

	private AppErrorCode(String errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}
}
