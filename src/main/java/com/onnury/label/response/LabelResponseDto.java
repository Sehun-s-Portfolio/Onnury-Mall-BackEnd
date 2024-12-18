package com.onnury.label.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LabelResponseDto {
    private Long labelId; // 라벨 id
    private String labelTitle; // 라벨 명
}
