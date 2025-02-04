package com.teamname.crawling.whereToStudy.naver;

import lombok.Getter;

@Getter
public enum Tab {

    HOME("홈"), MENU("메뉴"), REVIEW("리뷰"), INFO("정보");

    private String name;
    Tab(String name){
        this.name = name;
    }

}
