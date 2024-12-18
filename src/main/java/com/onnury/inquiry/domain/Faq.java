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
public class Faq extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long faqId;

    @Column(nullable = false)
    private String type; // FAQ타입

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String question; // 질문

    @Column(columnDefinition = "LONGTEXT")
    private String answer; // 답변

    @Column(nullable = false)
    private String expressCheck; // 노출유무

}
