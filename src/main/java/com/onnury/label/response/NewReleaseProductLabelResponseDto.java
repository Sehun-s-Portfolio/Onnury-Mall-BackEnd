package com.onnury.label.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NewReleaseProductLabelResponseDto {
    private String label_title;
    private String color_code;
    private LocalDateTime start_post_date;
    private LocalDateTime end_post_date;
}
