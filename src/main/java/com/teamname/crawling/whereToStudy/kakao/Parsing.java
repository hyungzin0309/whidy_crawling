package com.teamname.crawling.whereToStudy.kakao;

import com.google.gson.Gson;
import org.openqa.selenium.json.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class Parsing {

    public static void main(String[] args) {
        String filePath = "/Users/ddong_goo/Desktop/document/personal_project/crawling/data/cafe.json";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Cafe>>() {}.getType();
            List<Cafe> cafes = gson.fromJson(reader, listType);
            System.out.println(cafes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
