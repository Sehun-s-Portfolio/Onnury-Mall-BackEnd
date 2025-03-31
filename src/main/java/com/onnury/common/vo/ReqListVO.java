package com.onnury.common.vo;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqListVO extends AbstractVO {
	protected long pageStart;   // 시작페이지
	protected long pageLength;  // 페이지당 row수

	protected String searchKeyword;  // 검색1
	protected String searchKeyword2; // 검색2
	protected String searchKeyword3; // 검색3
}
