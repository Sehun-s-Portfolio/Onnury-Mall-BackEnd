package com.onnury.link.domain;

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
public class Link extends TimeStamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long linkId;

    @Column(nullable = false)
    private String type; // 링크 type

    @Column(nullable = false)
    private String linkCompany; // 링크처

    @Column
    private String link; // 풀링크

}
