package com.example.Algorithm;

import java.util.HashMap;
import java.util.Map;

public enum addDesign {
        RED("red"),
        BLACK("black"),
    CYAN("cyan"),
    MAGENTA("magenta"),
    GOLD("gold"),
    SKYBLUE("deepskyblue");
    private String col = "";
    public static final Map<addDesign,String[]> convertdesg=new HashMap<>();
    public static void init(addDesign d,String[] val){
        convertdesg.put(d,val);
    }
    public static String convert(String str){
        addDesign[] li=new addDesign[]{MAGENTA,GOLD,SKYBLUE};
        for(addDesign i:li){
            for (String str0 : convertdesg.get(i)){
                if(str.trim().equals(str0.trim())){
                    return i.getcol();
                }
            }
        }
        return "black";
    }
    addDesign(String col0) {
        col = col0;

    }
    public String getcol() {
        return col;
    }
}
