package com.example.Algorithm.DTO;

import lombok.ToString;

public class DTO {
    private String code;
    private String lang;
    public DTO(String code,String lang){
        this.code=code;
        this.lang=lang;
    }
    @Override
    public String toString(){
        return "code="+code+ "lang="+lang;

    }
}
