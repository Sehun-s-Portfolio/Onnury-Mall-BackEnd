package com.onnury.label.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LabelUpdateRequestDto {
    private String labelTitle; // 라벨 명
    private String startPostDate; // 라벨 게시일
    private String endPostDate; // 라벨 종료일
    private String topExpression; // 상위 노출 (Y / N)
}
