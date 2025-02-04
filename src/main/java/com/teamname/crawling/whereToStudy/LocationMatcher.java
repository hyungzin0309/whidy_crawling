package com.teamname.crawling.whereToStudy;

import com.google.gson.Gson;
import com.teamname.crawling.whereToStudy.kakao.Cafe;
import com.teamname.crawling.whereToStudy.naver.CafeDetail;
import org.openqa.selenium.json.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocationMatcher {

    private static Map<String, Cafe> cafes;
    public static Gson gson = new Gson();

    public static void main(String[] args) throws Exception{
        List<Cafe> cafeList = getCafeList();
        cafes = cafeList.stream().collect(Collectors.toMap(k -> k.getAddressName() + " " + k.getPlaceName(), v -> v));
        List<CafeDetail> details = getCollectedDetails();
        for(CafeDetail detail : details){
            String keyword = detail.getKeyword();
            Cafe cafe = cafes.get(keyword);
            detail.setLongitude(Double.parseDouble(cafe.getX()));
            detail.setLatitude(Double.parseDouble(cafe.getY()));
        }

        String totalDataFile = "/Users/ddong_goo/Desktop/document/personal_project/crawling/data/totalCafeDetail.json";
        try (FileWriter resultData = new FileWriter(totalDataFile)){
            resultData.write(gson.toJson(details));
        }
    }

    private static List<CafeDetail> getCollectedDetails() {
        String cafeDataFilePath = "/Users/ddong_goo/Desktop/document/personal_project/crawling/data/totalCafeDetail_.json";

        try (BufferedReader reader = new BufferedReader(new FileReader(cafeDataFilePath))) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<CafeDetail>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Cafe> getCafeList() {
        String cafeDataFilePath = "/Users/ddong_goo/Desktop/document/personal_project/crawling/data/cafe.json";

        try (BufferedReader reader = new BufferedReader(new FileReader(cafeDataFilePath))) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Cafe>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
