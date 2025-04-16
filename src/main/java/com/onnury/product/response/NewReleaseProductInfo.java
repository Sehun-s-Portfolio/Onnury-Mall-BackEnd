package com.onnury.product.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NewReleaseProductInfo {
    private Long product_id;
    private String product_name;
    private Long category_in_brand_id;
    private Long brand_id;
    private Long category1id;
    private String up_category_name;
    private Long category2id;
    private String middle_category_name;
    private Long category3id;
    private String down_category_name;
    private Long supplier_id;
    private String classification_code;
    private String model_number;
    private String delivery_type;
    private String sell_classification;
    private String expression_check;
    private int sell_price;
    private int normal_price;
    private int delivery_price;
    private int purchase_price;
    private LocalDateTime event_start_date;
    private LocalDateTime event_end_date;
    private int event_price;
    private String event_description;
    private String option_check;
    private String manufacturer;
    private String made_in_origin;
    private String consignment_store;
    private String memo;
    private String status;
    private String relate_img_ids;
    private String brand_title;
    private String content;
}
