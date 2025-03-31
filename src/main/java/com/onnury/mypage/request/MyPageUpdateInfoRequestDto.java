package com.onnury.mypage.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class MyPageUpdateInfoRequestDto extends AbstractVO {
    private String address; // 주소
    private String detailAddress; // 상세 주소
    private String postNumber; // 우편 번호
    private String manager; // (기업용) 담당자 명
    private String email; // 기업 : 담당자 이메일, 일반 : 고객 이메일
    private String phone; // 기업 : 담당자 연락처, 일반 : 고객 연락처
}
