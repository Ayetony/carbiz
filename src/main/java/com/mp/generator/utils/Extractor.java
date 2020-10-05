package com.mp.generator.utils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extractor {

    public static int extractNumber(String extra){
        if(StringUtils.isNotEmpty(extra)){
            String regEx="[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(extra);
            if(StringUtils.equals(m.replaceAll(""),"")){
                return 0;
            }
            return Integer.parseInt(m.replaceAll(""));
        }else {
            return 0;
        }
    }


    public static void mainDemo(String[] args) {

        System.out.println(extractNumber("4年"));
        System.out.println(StringUtils.equals("", ""));
    }

    //¥2.60 - ¥3.26¥2.95 - ¥3.70
    public static List<String> getMinMax(String productPrice){

        String[] strs = {};
        productPrice = productPrice.replace("-","");
        strs = StringUtils.splitByWholeSeparator(productPrice, "¥");
        double max = strs.length >= 2 ? Double.parseDouble(strs[1]) : 0.0;
        List<String> list = new ArrayList<>();
        double min = Double.parseDouble(strs[0]);
        for (String s : strs) {
            double newVal  = Double.parseDouble(s);
            if(newVal > max){
                double tempMax = max;
                max = newVal;
                newVal = tempMax;
            }
            if(newVal < min){
                min = newVal;
            }
        }
        list.add(String.valueOf(min));
        list.add(String.valueOf(max));
        list.forEach(System.out::println);
        return list;
    }

    public static void main(String[] args) {
//        trimToString("¥2.960 - ¥3.26¥2.95 ¥3.70");
        System.out.println("3000万+");

    }

    public static List<String> trimToString(String productPrice){

        productPrice = StringUtils.deleteWhitespace(productPrice);
        List<String> stringList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        for (char c : productPrice.toCharArray()) {
            if(Character.isDigit(c) || c == '.'){
                stringBuffer.append(c);
            }else{
                if(StringUtils.isNotBlank(stringBuffer.toString())) {
                    stringList.add(stringBuffer.toString());
                }
                stringBuffer = new StringBuffer();
            }
        }
        stringList.add(stringBuffer.toString());
        Collections.sort(stringList);
        System.out.println(stringList.size());
        stringList.forEach(System.out::println);
        return stringList;
    }



}
