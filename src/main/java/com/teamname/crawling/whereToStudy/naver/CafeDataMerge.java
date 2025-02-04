package com.teamname.crawling.whereToStudy.naver;

import com.google.gson.Gson;
import org.openqa.selenium.json.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CafeDataMerge {

    private static final List<String> cafeDataFiles = List.of("cafeDetail1.json","cafeDetail4.json","cafeDetail7.json","cafeDetail12.json","cafeDetail60.json","cafeDetail120.json","cafeDetail150.json","cafeDetail175.json");
    private static final List<CafeDetail> totalData = new ArrayList<>();
    public static Gson gson = new Gson();

    public static void main(String[] args) throws Exception{
        Path directory = Paths.get("/Users/ddong_goo/Desktop/document/personal_project/crawling/data/cafeDetail/");
        for(Path fileName : Files.list(directory).toList()){
            String filePath = fileName.toString();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<CafeDetail>>() {}.getType();
                List<CafeDetail> data = gson.fromJson(reader, listType);
                totalData.addAll(data);
            }
        }

        String totalDataFile = "/Users/ddong_goo/Desktop/document/personal_project/crawling/data/totalCafeDetail.json";
        try (FileWriter resultData = new FileWriter(totalDataFile)){
             resultData.write(gson.toJson(totalData));
        }
    }
}
