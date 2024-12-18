package com.onnury.banner.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BannerMediaResponseDto {
    private Long mediaId;
    private String imgUploadUrl; // 이미지 업로드 경로
    private String imgUrl; // 이미지 호출 경로
    private String imgTitle; // 이미지 원본 명
    private String imgUuidTitle; // 난수화된 이미지 명
    private String type; // 배너 이미지 사용 플랫폼 타입
    private Long mappingContentId; // 연관된 엔티티 id
}
