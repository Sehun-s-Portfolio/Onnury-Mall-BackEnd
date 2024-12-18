package com.onnury.inquiry.repository;

import com.onnury.inquiry.domain.Faq;
import com.onnury.inquiry.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Column;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
}
