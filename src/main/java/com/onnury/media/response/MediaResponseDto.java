package com.onnury.media.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MediaResponseDto {
    private Long mediaId;
    private String imgUploadUrl; // 이미지 업로드 경로
    private String imgUrl; // 이미지 호출 경로
    private String imgTitle; // 이미지 원본 명
    private String imgUuidTitle; // 난수화된 이미지 명
    private String representCheck; // 대표 이미지 구분 (Y/N)
}
