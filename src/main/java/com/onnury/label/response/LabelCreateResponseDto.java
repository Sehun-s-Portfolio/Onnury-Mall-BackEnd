package com.onnury.label.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class LabelCreateResponseDto {
    private String labelTitle; // 라벨 명
    private String colorCode; // 라벨컬러코드
    private LocalDateTime startPostDate; // 라벨 게시일
    private LocalDateTime endPostDate; // 라벨 종료일
    private String imgUrl; // 라벨 이미지 호출 경로
    private String topExpression; // 상위 노출 (Y / N)
}
