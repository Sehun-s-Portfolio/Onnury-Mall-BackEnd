package com.onnury.inquiry.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class Inquiry extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long inquiryId;

    @Column(nullable = false)
    private String type; // 문의타입

    @Column(nullable = false)
    private String inquiryTitle; // 문의 제목

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String inquiryContent; // 문의 내용

    @Column(columnDefinition = "LONGTEXT")
    private String answer; // 문의 답변

    @Column
    private LocalDateTime answerAt; // 문의 답변 시간

    @Column(nullable = false)
    private Long memberId; // 문의자 id
}
