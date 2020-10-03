package com.mp.generator.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class HotAPIEntity implements Serializable {

    private HotCrossborderProduct hotCrossborderProduct;
    private HotProduct hotProduct;
    private String minPrice;
    private String maxPrice;


}
