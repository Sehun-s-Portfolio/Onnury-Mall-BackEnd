package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MemberListUpResponseDto{

    private List<MemberDataResponseDto> memberDataResponseDto; // 페이지 조건에따른 리스트
    private Long total ; // 데이터 총 갯수

}