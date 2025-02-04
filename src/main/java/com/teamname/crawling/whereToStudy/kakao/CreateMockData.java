package com.teamname.crawling.whereToStudy.kakao;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CreateMockData {

    private static Set<Cafe> responses = new HashSet<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        String resultDataFilePath = "/Users/ddong_goo/Desktop/cafe_mock.json";
        for(int i = 0; i<10000000; i++){
            String placeName = UUID.randomUUID().toString();
            Cafe cafe = Cafe.builder()
                    .placeName(placeName)
                    .addressName(placeName)
                    .roadAddressName(placeName)
                    .phone(placeName)
                    .categoryGroupName(placeName)
                    .placeUrl(placeName)
                    .x(placeName)
                    .y(placeName)
                    .build();
            responses.add(cafe);
        }
        try (FileWriter resultData = new FileWriter(resultDataFilePath)) {
            resultData.write(gson.toJson(responses));
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
