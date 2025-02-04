package com.teamname.crawling.whereToStudy.kakao;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of={"placeName", "addressName"})
public class Cafe {
    @SerializedName("place_name")
    private String placeName;

    @SerializedName("address_name")
    private String addressName;

    @SerializedName("road_address_name")
    private String roadAddressName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("category_group_name")
    private String categoryGroupName;

    @SerializedName("place_url")
    private String placeUrl;

    @SerializedName("x")
    private String x;

    @SerializedName("y")
    private String y;

    @Override
    public String toString() {
        return "{" +
                "placeName:'" + placeName + '\'' +
                ", addressName:'" + addressName + '\'' +
                ", roadAddressName:'" + roadAddressName + '\'' +
                ", phone:'" + phone + '\'' +
                ", categoryGroupName:'" + categoryGroupName + '\'' +
                ", placeUrl:'" + placeUrl + '\'' +
                ", x:'" + x + '\'' +
                ", y:'" + y + '\'' +
                '}';
    }
}