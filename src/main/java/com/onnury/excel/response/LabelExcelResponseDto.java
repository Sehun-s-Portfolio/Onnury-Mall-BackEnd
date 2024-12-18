package com.onnury.excel.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LabelExcelResponseDto {
    private Long labelId;
    private String labelTitle; // 라벨 명
    private String colorCode; // 라벨컬러코드
    private String startPostDate; // 라벨 게시일
    private String endPostDate; // 라벨 종료일
    private String imgUrl; // 라벨 이미지 호출 경로
    private String topExpression; // 상위 노출 (Y / N)
    private String createdAt;
    private String modifiedAt;
}
