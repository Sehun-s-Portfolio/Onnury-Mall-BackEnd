package com.onnury.product.response;

import com.onnury.brand.response.BrandResponseDto;
import com.onnury.category.response.DownCategoryResponseDto;
import com.onnury.category.response.MiddleCategoryResponseDto;
import com.onnury.category.response.UpCategoryResponseDto;
import com.onnury.label.response.LabelResponseDto;
import com.onnury.supplier.response.SupplierResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ProductReadyInfoResponseDto {
    private List<SupplierResponseDto> supplierList;
    private List<BrandResponseDto> brandList;
    private List<UpCategoryResponseDto> upCategoryList;
    private List<MiddleCategoryResponseDto> middleCategoryList;
    private List<DownCategoryResponseDto> downCategoryList;
    private List<LabelResponseDto> labelList;
}
