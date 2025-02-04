package com.teamname.crawling.whereToStudy.kakao;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KakaoResponse {
    @SerializedName("documents")
    private List<Cafe> cafes;

    @SerializedName("meta")
    private Meta meta;

    @Override
    public String toString() {
        return "\"cafes\":" + cafes +
                ", \"meta\":" + meta +
                '}';
    }
}