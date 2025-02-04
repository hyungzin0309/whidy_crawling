package com.teamname.crawling.whereToStudy.kakao;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("pageable_count")
    private int pageableCount;

    @SerializedName("is_end")
    private boolean isEnd;

    @Override
    public String toString() {
        return "{" +
                "\"totalCount\":\"" + totalCount + "\"" +
                ", \"pageableCount\":\"" + pageableCount + "\"" +
                ", \"isEnd\":\"" + isEnd + "\"" +
                '}';
    }
}