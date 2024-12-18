package com.onnury.notice.domain;

import com.onnury.share.TimeStamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@org.springframework.data.relational.core.mapping.Table
@Entity
public class Notice extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long noticeId;

    @Column(nullable = false)
    private String noticeTitle; // 공지사항 타이틀

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String noticeContent; // 공지사항 내용

}
