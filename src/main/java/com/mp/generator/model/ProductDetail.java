package com.mp.generator.model;

import com.google.common.collect.Multimap;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetail {

    private Long product_id;
    private String product_url;
    private String product_name;
    private String product_image;
    private String product_weight;
    private String product_category;
    private Float logistic_fee;
    private Float product_min_price;
    private Float product_max_price;

    private List<Multimap<String,String>> props;

    private List<Sku> skus;

    private SaleInfo sale_info;

    private ShopInfo shop_info;




}
