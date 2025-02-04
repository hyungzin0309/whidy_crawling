package com.teamname.crawling.whereToStudy.kakao;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Slf4j
public class CafeListByKakao {

    // 카카오 REST API 키
    private static final String API_KEY = "eef99fda30e748a2371ff3bc0609eca4";

    // 검색 반경 (미터)
    private static final double BASE_RADIUS = 2000.0; // 2km


    public static void main(String[] args) {
        List<double[]> gridCenters = calculateGridCenters();
        log.info("start");
        // API 호출
        for (double[] center : gridCenters) {
            double lat = center[0];
            double lon = center[1];
            searchCafes(lat, lon, BASE_RADIUS);
            log.info("@@@@@@@@@@@@@@@@@ 호출횟수 : {} @@@@@@@@@@@@@@@@@@", api_num);
        }

        String resultDataFilePath = "/Users/ddong_goo/Desktop/cafe.json";
        String cafeNameFilePath = "/Users/ddong_goo/Desktop/cafe_names.txt";

        try (FileWriter resultData = new FileWriter(resultDataFilePath);
                FileWriter cafeNameData = new FileWriter(cafeNameFilePath)) {
            resultData.write(gson.toJson(responses));
            cafeNameData.write("");
            for(Cafe cafe : responses){
                cafeNameData.append(cafe.getPlaceName()).append(", ").append(cafe.getAddressName()).append("\n");
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        log.info("##### api 호출횟수 : " + api_num);
        for(String key : regions.keySet()){
            log.info("##### 중복지역 정보, {} : {}", key, regions.get(key));
        }
        System.out.println();
    }

    private static Map<String, Integer> regions = new HashMap<>();
    private static Set<Cafe> responses = new HashSet<>();
    private static Gson gson = new Gson();
    private static int api_num = 0; // api 호출 횟수 확인

    private static void searchCafes(double lat, double lon, double radius) {
        boolean isEnd = false;
        int page = 1;

        while (!isEnd) {
            String apiUrl = "https://dapi.kakao.com/v2/local/search/category.json"
                    + "?category_group_code=CE7" // 카테고리 코드 (카페)
                    + "&x=" + lon
                    + "&y=" + lat
                    + "&page=" + page
                    + "&radius=" + radius;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "KakaoAK " + API_KEY);
                api_num++;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                KakaoResponse response = gson.fromJson(br.readLine(), KakaoResponse.class);

                if(page == 1 && response.getMeta().getTotalCount() > 45){
                    String region = response.getCafes().get(0).getAddressName().substring(0,10);
                    regions.put(region, regions.getOrDefault(region,0)+1);
                    log.error("카페 45개 초과, 4등분하여 재탐색함. (카페 수 : {}, 지역 : {})",response.getMeta().getTotalCount(), region);
                    double newRadius = radius / 2; // 반경 축소
                    double stepLat = newRadius / EARTH_RADIUS_LAT; // 새로운 중심 좌표 간격 (위도 기준)
                    double stepLon = newRadius / (EARTH_RADIUS_LAT * Math.cos(Math.toRadians(lat))); // 새로운 경도 이동 거리

                    searchCafes(lat + stepLat, lon + stepLon, newRadius); // 1분면
                    searchCafes(lat + stepLat, lon - stepLon, newRadius); // 2분면
                    searchCafes(lat - stepLat, lon + stepLon, newRadius); // 3분면
                    searchCafes(lat - stepLat, lon - stepLon, newRadius); // 4분면
                }

                for(Cafe cafe : response.getCafes()){
                    if(cafe.getAddressName().startsWith("서울")
                            && !cafe.getPlaceName().contains("키즈")
                            && !cafe.getPlaceName().contains("보드")
                            && !cafe.getPlaceName().contains("게임")){
                        responses.add(cafe);
                    }
                }
                isEnd = response.getMeta().isEnd();
                br.close();

                page++;
            } catch (Exception e) {
                throw new RuntimeException(e);
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
