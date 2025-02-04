package com.teamname.crawling.whereToStudy.naver;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Builder
@ToString
@Data
public class CafeDetail {

    private Long id;
    private String keyword;
    private String name;
    private String etc;
    private String phone;
    private String address;
    private String mainImage;
    private double latitude;
    private double longitude;
    private int reviewNum;
    private List<BusinessHour> times;
    private List<Menu> menuList;
    private List<Review> reviews;
    private List<String> infos;
    private List<String> images;

}
