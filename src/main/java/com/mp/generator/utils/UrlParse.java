package com.mp.generator.utils;

import org.apache.commons.lang.StringUtils;

public class UrlParse {

    public static String productRefToId(String productRef) {
        if (StringUtils.isNotBlank(productRef))
            return StringUtils.substringBetween(productRef, "offer/", ".html");
        else
            return "";
    }

    public static String shopRefToId(String shopRef) {

        if (StringUtils.isNotBlank(shopRef))
            return StringUtils.substringBetween(shopRef, "shop", ".1688");
        else
            return "";
    }

    public static void main(String[] args) {
        System.out.println(productRefToId("https://detail.1688.com/offer/615318815095.html"));
        System.out.println(shopRefToId("http://shop1468429237850.1688.com/"));
    }

}
