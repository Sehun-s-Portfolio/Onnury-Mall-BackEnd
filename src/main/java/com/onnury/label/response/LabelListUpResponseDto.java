package com.onnury.label.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class LabelListUpResponseDto {
    private List<LabelDataResponseDto> labelDataResponseDto; // 라벨 데이터 리스트
    private Long total ; // 데이터 총 갯수
}