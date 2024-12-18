package com.onnury.category.repository;

import com.onnury.category.domain.CategoryInBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryInBrandRepository extends JpaRepository<CategoryInBrand, Long> {
}
