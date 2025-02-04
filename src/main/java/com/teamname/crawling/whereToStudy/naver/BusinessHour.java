package com.teamname.crawling.whereToStudy.naver;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
public class BusinessHour {
    private String dayOfWeek;
    private String content;

    public BusinessHour(String dayOfWeek, String content){
        this.content = content;
        this.dayOfWeek = convertDayOfWeek(dayOfWeek);
    }

    private String convertDayOfWeek(String origin){
        String result;
        if(origin.contains("월")) {
            result = "월";
        }else if(origin.contains("화")){
            result = "화";
        }else if(origin.contains("수")){
            result = "수";
        }else if(origin.contains("목")){
            result = "목";
        }else if(origin.contains("금")){
            result = "금";
        }else if(origin.contains("토")){
            result = "토";
        }else if(origin.contains("일")){
            result = "일";
        }else{
            result = origin;
        }
        return result;
    }
}
