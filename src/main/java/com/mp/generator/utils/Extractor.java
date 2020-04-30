package com.mp.generator.utils;

import org.apache.commons.lang.StringUtils;

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

}
