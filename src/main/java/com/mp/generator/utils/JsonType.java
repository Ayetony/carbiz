package com.mp.generator.utils;

import com.mp.generator.entity.AlibabaProductInfoPo;

import java.util.Collection;
import java.util.Map;

public class JsonType {

    private AlibabaProductInfoPo alibabaProductInfoPo;
    private Map<String, Collection<String>> skus;

    public AlibabaProductInfoPo getAlibabaProductInfoPo() {
        return alibabaProductInfoPo;
    }

    public void setAlibabaProductInfoPo(AlibabaProductInfoPo alibabaProductInfoPo) {
        this.alibabaProductInfoPo = alibabaProductInfoPo;
    }

    public Map<String, Collection<String>> getSkus() {
        return skus;
    }

    public void setSkus(Map<String, Collection<String>> skus) {
        this.skus = skus;
    }

}
