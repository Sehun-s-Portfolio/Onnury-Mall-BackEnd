package com.onnury.label.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NewReleaseProductLabelResponseDto {
    private Long labelId;
    private String labelTitle;
    private String colorCode;
    private LocalDateTime startPostDate;
    private LocalDateTime endPostDate;
}
