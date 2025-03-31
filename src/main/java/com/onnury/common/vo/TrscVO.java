package com.onnury.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onnury.common.base.AbstractVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class TrscVO extends AbstractVO {
	private String trscId = null;		// 트랜잭션 ID
	private String prgmTy = null;		// 프로그램 구분
	private String prgmId = null;		// 프로그램 ID
	private String scrnId = null;		// 화면 ID
	private String userIpAddr = null;	// 사용자IP
	private String bswrNm = null;		// 브라우저명
	private String webSrvHostNm = null;	// 웹서버 호스트명
	private String admin = null;		// 시스템 관리자 유무
	private String svrNm = null;		// 서버명(현재)
	private String sysCode = null;		// 시스템코드
	private String trscDttm = null;		// 트랜잭션일시
	private String userId = null;		// 사용자ID
} 