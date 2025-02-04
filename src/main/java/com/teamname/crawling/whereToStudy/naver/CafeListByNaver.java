package com.teamname.crawling.whereToStudy.naver;

import com.google.gson.Gson;
import com.teamname.crawling.whereToStudy.kakao.Cafe;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
public class CafeListByNaver {

    private static final String CLIENT_ID = "PJUADu4WW8ky8G6T_0gi";
    private static final String CLIENT_SECRET = "n4UAJlFepb";
    private static final String API_URL = "https://openapi.naver.com/v1/search/local.json";

    // 검색 반경 (미터)
    private static final double BASE_RADIUS = 2000.0; // 2km


    public static void main(String[] args) throws Exception {
        List<double[]> gridCenters = calculateGridCenters();
        log.info("start");
        // API 호출
        int index = 0;
        for (double[] center : gridCenters) {
            double lat = center[0];
            double lon = center[1];
            searchCafes(lat, lon, BASE_RADIUS);
            log.info("@@@@@@@@@@@@@@@@@ 호출횟수 : {} @@@@@@@@@@@@@@@@@@", api_num);
            if(++index==2){
                break;
            };
        }

        String resultDataFilePath = "/Users/ddong_goo/Desktop/cafe.json";
        String cafeNameFilePath = "/Users/ddong_goo/Desktop/cafe_names.txt";

        try (FileWriter resultData = new FileWriter(resultDataFilePath);
             FileWriter cafeNameData = new FileWriter(cafeNameFilePath)) {
            resultData.write(gson.toJson(responses));
            cafeNameData.write("");
            for (Cafe cafe : responses) {
                cafeNameData.append(cafe.getPlaceName()).append(", ").append(cafe.getAddressName()).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("##### api 호출횟수 : " + api_num);
        for (String key : regions.keySet()) {
            log.info("##### 중복지역 정보, {} : {}", key, regions.get(key));
        }
        System.out.println();
    }

    private static Map<String, Integer> regions = new HashMap<>();
    private static Set<Cafe> responses = new HashSet<>();
    private static Gson gson = new Gson();
    private static int api_num = 0; // api 호출 횟수 확인

    private static void searchCafes(double lat, double lon, double radius) throws Exception {
        int display = 100; // 한 번에 가져올 데이터 수
        int start = 1;       // 시작 페이지 (1 ~ 1000)
        radius/=1000;
        String apiUrl = String.format(
//                "https://openapi.naver.com/v1/search/local.json?query=%s&display=%d&start=%d",
//                URLEncoder.encode("카페", "UTF-8"), display, start
                "https://openapi.naver.com/v1/search/local.json?query=%s&x=%f&y=%f&radius=%d&display=%d&start=%d",
                URLEncoder.encode("카페", "UTF-8"), lat, lon, 2000, display, start
        );
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
        conn.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        System.out.println(response);
        br.close();


    }


    private static void processResponse(String jsonResponse, Set<String> uniqueCafes) {
        // JSON 데이터 파싱 (간단한 파싱 예제, Gson/Jackson을 사용하는 것이 좋음)
        if (jsonResponse.contains("\"title\"")) {
            String[] titles = jsonResponse.split("\"title\"");
            for (int i = 1; i < titles.length; i++) {
                String title = titles[i].split("\",")[0].replaceAll("[^가-힣a-zA-Z0-9 ]", "").trim();
                uniqueCafes.add(title);
            }
        }
    }


    // 서울 대략적인 경계 좌표 (남서쪽과 북동쪽)
    private static final double EARTH_RADIUS_LAT = 111000.0;
    private static final double START_LAT = 37.413294; // 남서쪽 위도
    private static final double START_LON = 126.734086; // 남서쪽 경도
    private static final double END_LAT = 37.715133;   // 북동쪽 위도
    private static final double END_LON = 127.269311; // 북동쪽 경도
    private static final float BASE_STEP_RATIO = 0.7f; // 탐색 반경에 대비되는 각 격자의 중심간 거리 비율 (빈 공간 없이 탐색위해 overlap ratio 적용, 2km 대비 완전 탐색 : 1.5km)


    /**
     * 격자 중심 좌표를 계산
     */
    private static List<double[]> calculateGridCenters() {
        List<double[]> gridCenters = new ArrayList<>();

        // 1도 위도 ≈ 111km, 경도는 위도에 따라 다름
        // 탐색 범위가 원형을 띄므로 빈공간이 생기지 않게 중심좌표를 1km 로만 잡고 계산. 중복되는 지역 생기도록
        double stepLat = (BASE_RADIUS * BASE_STEP_RATIO) / EARTH_RADIUS_LAT; // 위도 1도 ≈ 111,000m
        double stepLon = (BASE_RADIUS * BASE_STEP_RATIO) / (EARTH_RADIUS_LAT * Math.cos(Math.toRadians(START_LAT))); // 경도는 위도에 따라 다름

        // 격자 중심 좌표 계산
        for (double lat = START_LAT; lat <= END_LAT; lat += stepLat) {
            for (double lon = START_LON; lon <= END_LON; lon += stepLon) {
                gridCenters.add(new double[]{lat, lon});
            }
        }
        return gridCenters;
    }
}
