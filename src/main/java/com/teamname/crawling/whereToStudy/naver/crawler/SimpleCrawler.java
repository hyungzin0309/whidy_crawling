package com.teamname.crawling.whereToStudy.naver.crawler;

import com.google.gson.Gson;
import com.teamname.crawling.whereToStudy.kakao.Cafe;
import com.teamname.crawling.whereToStudy.naver.*;
import com.teamname.crawling.whereToStudy.util.PathConverter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.json.TypeToken;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class SimpleCrawler {

    public static String root = "/Users/ddong_goo/Desktop/document/personal_project/crawling";
    public static String driver = "/Users/ddong_goo/desktop/document/tool/chrome_driver/133/chromedriver";

    public static Gson gson = new Gson();
    public static List<CafeDetail> cafeDetails = new ArrayList<>();
    private static final Map<String, StringBuilder> logPerThread = new HashMap<>();
    private static final List<String> notCrawlingKeywords = new ArrayList<>();

    private static final StringBuilder totalLog = new StringBuilder();
    private static final StringBuilder errorKeywordsLogs = new StringBuilder();
    private static final StringBuilder notCrawledList = new StringBuilder();
    private static int currentIndex = 0;

    public static void main(String[] args) throws Exception{
        int startIndex = 8;
        int lastIndex = 14;
        int threads = 5;
        List<Cafe> cafes = getCafeList();
        setNotCrawlingKeyword();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
//        for (int i = 0; i < cafes.size(); i++) {
        for (int i = startIndex; i < lastIndex; i++) {
            execute(executor, i, cafes);
        }
        executor.shutdown();
        while(!executor.isTerminated()){
            Thread.sleep(10000);
            log.info("탐색 카페 수 {}",currentIndex);
        }

        String resultDataFilePath = PathConverter.convertPath(String.format(root + "/data/simple_data/cafeDetail/cafeDetail%d-%d.json", startIndex, lastIndex));
        String logFilePath = PathConverter.convertPath(String.format(root + "/data/simple_data/logs/log%d-%d.log", startIndex, lastIndex));
        String errorKeywords = PathConverter.convertPath(String.format(root + "/data/simple_data/recrawl_target/errorKeywords.txt"));
        String notCrawledListFilePath = PathConverter.convertPath(String.format(root + "/data/simple_data/recrawl_target/notCrawled.txt"));

        try (FileWriter resultData = new FileWriter(resultDataFilePath);
             FileWriter errorKeywordsFile = new FileWriter(errorKeywords,true);
             FileWriter notCrawledListFile = new FileWriter(notCrawledListFilePath,true);
             FileWriter logFile = new FileWriter(logFilePath)) {
            resultData.write(gson.toJson(cafeDetails));
            logFile.write(totalLog.toString());
            errorKeywordsFile.write(errorKeywordsLogs.toString());
            notCrawledListFile.write(notCrawledList.toString());
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        log.info("최종 수집 카페 수 : {}", cafeDetails.size());
    }

    private static void execute(ExecutorService executor, int index, List<Cafe> cafes) {
        executor.submit(() -> {
            currentIndex = index;
            Cafe cafe = cafes.get(index);
            String cafeName = cafe.getAddressName() + " " + cafe.getPlaceName();
//                String cafeName = cafesList.get(finalI);
            if(notCrawlingKeywords.stream().filter(cafeName::contains).findFirst().isEmpty()){
                WebDriver driver = getDriver();
                StringBuilder logBuilder = new StringBuilder();
                logPerThread.put(driver.toString(), logBuilder);
                try {
                    Long id = (long)index;
                    logBuilder.append(String.format("\n################### [%d. %s] 카페 서치 시작\n",id, cafeName));
                    crawlCafe(driver, cafeName, id, cafe.getX(), cafe.getY());
                    logBuilder.append(String.format("################### [%d. %s] 카페 서치 완료\n",id, cafeName));
                }catch (TimeoutException e){
                    if(e.getMessage().startsWith("Expected condition failed: waiting for element to be clickable: By.cssSelector: #searchIframe")) {
                        logBuilder.append(String.format("################### [%s] 발견된 대상 없어 서치 종료 (발견된 리스트 없어 서치 종료) : %s\n", cafeName, e.getMessage()));
                        notCrawledList.append(cafe.getAddressName()).append(",").append(cafe.getRoadAddressName()).append(",").append(cafe.getPlaceName()).append("\n");
                    }else {
                        logBuilder.append(String.format("################### [%s] 에러 발생 : %s\n", cafeName, e.getMessage()));
                        errorKeywordsLogs.append(cafeName).append("\n");
                    }
                } catch (Exception e) {
                    logBuilder.append(String.format("################### [%s] 에러 발생 : %s\n", cafeName, e.getMessage()));
                    errorKeywordsLogs.append(cafeName).append("\n");
                } finally {
//                    log.info(logBuilder.toString());
                    totalLog.append(logBuilder);
                    driver.quit();
                }
            }

        });
    }


    private static void crawlCafe(WebDriver driver, String searchKeyword, Long id, String lon, String lat) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        // 지도 페이지 접속 후 검색어 입력, 제출
        driver.get("https://map.naver.com/v5/");
        WebElement element = driver.findElement(By.cssSelector("input.input_search"));

        element.sendKeys(searchKeyword);
        element.sendKeys(Keys.RETURN);
        Thread.sleep(2000);
        element.sendKeys(Keys.RETURN);

        // 검색결과 list iframe
        try{
            element = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#searchIframe")));
            driver.switchTo().frame("searchIframe");
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("span.YwYLL"))).click();
        }catch (ElementClickInterceptedException e){
            logPerThread.get(driver.toString()).append("[INFO] 검색결과 리스트 없음 \n");
        }

        // 정보 iframe
        driver.switchTo().defaultContent();
        element = new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#entryIframe")));
        driver.switchTo().frame("entryIframe");

//        String etc = findText(driver, wait, ".xPvPE", "기타 정보 수집");
        String phone = findText(driver, wait, ".xlx7Q", "전화번호 수집");
        String address = findText(driver, wait, ".LDgIH", "주소 수집");
        String cafeName = findText(driver, wait, ".GHAhO", "카페 이름");
        String reviewNumString = findText(driver, wait, ".PXMot>a", "방문자 리뷰 수 수집");
        String image = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".QX0J7>.K0PDV"))).getAttribute("src");
        List<String> images = findElements(driver, new WebDriverWait(driver, Duration.ofSeconds(5)), ".QX0J7>.K0PDV", "사진 수집")
                .stream().map(e -> e.getAttribute("src")).toList();

        if(reviewNumString == null){
            logPerThread.get(driver.toString()).append(String.format("################### [%s] 카페 서치 완료 (리뷰 수가 적어 추가되지 않음)\n", cafeName));
            return; // 너무 적은 수의 리뷰 카페는 skip
        }

        int reviewNum = Integer.parseInt(reviewNumString.substring("방문자 리뷰 ".length()).replace(",",""));
        if(reviewNum <40) {
            logPerThread.get(driver.toString()).append(String.format("################### [%s] 카페 서치 완료 (리뷰 수가 적어 추가되지 않음)\n", cafeName));
            return; // 너무 적은 수의 리뷰 카페는 skip
        }

        Map<String, WebElement> tabs = getTabs(driver);
        // 탭3 (리뷰)
        List<Review> reviews = null;
        if(tabs.get(Tab.REVIEW.getName()) != null) {
            tabs.get(Tab.REVIEW.getName()).click();
            click(driver, wait, ".dP0sq", "리뷰 탭 클릭");
            reviews = findElements(driver, new WebDriverWait(driver, Duration.ofSeconds(10)), ".MHaAm", "리뷰 리스트 수집")
                    .stream().map(e -> getReview(driver, new WebDriverWait(driver, Duration.ofSeconds(10)), e)).toList();
        }

        if(reviews.stream().filter(r->r.getContent().contains("집중하기 좋아요")).findFirst().isEmpty()){
            logPerThread.get(driver.toString()).append(String.format("################### [%s] 카페 서치 완료 ('집중하기 좋아요' 키워드가 없음.)\n", cafeName));
            return; // 너무 적은 수의 리뷰 카페는 skip
        }

        tabs = getTabs(driver);
        // 탭2 (메뉴)
        List<Menu> menuList = null;
        if(tabs.get(Tab.MENU.getName()) != null){
            tabs.get(Tab.MENU.getName()).click();
            menuList = findElements(driver, new WebDriverWait(driver, Duration.ofSeconds(10)),".E2jtL","메뉴 리스트 수집").stream().map(e -> getMenu(driver, wait, e)).toList();
        }

        CafeDetail detail = CafeDetail.builder()
                .id(id)
                .keyword(searchKeyword)
                .phone(phone)
                .address(address)
                .name(cafeName)
                .reviews(reviews)
                .reviewNum(reviewNum)
                .menuList(menuList)
                .longitude(Double.parseDouble(lon))
                .longitude(Double.parseDouble(lat))
                .images(images)
                .build();
        cafeDetails.add(detail);
    }

    private static Map<String, WebElement> getTabs(WebDriver driver) {
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".veBoZ")))
//                .stream().collect(Collectors.toMap(WebElement::getText, v -> v, (existing, replacement) -> existing));
        List<WebElement> elements = driver.findElements(By.cssSelector(".veBoZ"));
        if(elements == null || elements.isEmpty()){
            elements = driver.findElements(By.cssSelector(".tab>.txt"));
        }
        return elements.stream().collect(Collectors.toMap(WebElement::getText, v -> v, (existing, replacement) -> existing));
    }

    private static WebDriver getDriver() {
        System.setProperty("webdriver.chrome.driver", driver);
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless"); // GUI 없는 환경에서 실행
//        options.addArguments("--disable-gpu"); // GPU 비활성화
        options.addArguments("--no-sandbox"); // 보안 샌드박스 비활성화 (필요시)
        options.addArguments("--disable-dev-shm-usage"); // 공유 메모리 문제 방지
        options.addArguments("--window-size=1920x1080"); // 브라우저 해상도 설정
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }

    private static void click(WebDriver driver, WebDriverWait wait, String selector, String step) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector))).click();
        } catch (Exception e) {
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 클릭 실패. (%s) : %s \n", step, e.getClass(), e.getMessage()));
        }
    }

    private static List<WebElement> findElements(WebDriver driver, WebDriverWait wait, String selector, String step) {
        try {
            return wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.cssSelector(selector))));
        } catch (Exception e) {
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] Elements 얻기 실패. (%s) : %s \n", step, e.getClass(), e.getMessage()));
            return new ArrayList<>();
        }
    }

    private static Optional<WebElement> findElement(WebDriver driver, WebDriverWait wait, String selector, String step) {
        try {
            return Optional.of(driver.findElement(By.cssSelector(selector)));
        } catch (NoSuchElementException e) {
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 단일 Element 얻기 실패. (%s) : %s \n", step, e.getClass(), e.getMessage()));
            return Optional.empty();
        }
    }

    private static String findText(WebDriver driver, WebDriverWait wait, String selector, String step) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector))).getText();
        } catch (Exception e) {
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 단일 text 얻기 실패. (%s) : %s \n", step, e.getClass(), e.getMessage()));
            return null;
        }
    }

    private static String findText(WebDriver driver, WebDriverWait wait, WebElement parentElement, String selector, String step) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(parentElement.findElement(By.cssSelector(selector)))).getText();
        } catch (Exception e) {
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 단일 text 얻기 실패. (%s) : %s \n", step, e.getClass(), e.getMessage()));
            return null;
        }
    }

    private static Review getReview(WebDriver driver, WebDriverWait wait, WebElement element) {
        try {
            String content = Objects.requireNonNull(findText(driver, wait, element, ".t3JSf", "리뷰 Content ('디저트가 맛있어요, 등')")).replaceAll("\"","");
            String numString = findText(driver, wait, element, ".CUoLy", "리뷰 NumString");
            String numStringContent = findText(driver, wait, element, ".place_blind", "리뷰 NumStringContent ('이 키워드를 선택한 인원')");
            try {
                int num = Integer.parseInt(numString.substring(numStringContent.length() + 1));
                return new Review(content, num);
            }catch (IndexOutOfBoundsException e){
                logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] index out 문제 발생 : content : %s, numString : %s, numStringContent : %s \n", "리뷰", content, numString, numStringContent));
                return null;
            }
        }catch (Exception e){
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 문제 발생 (%s): %s \n", "리뷰 객체 생성", e.getClass(), e.getMessage()));
            return null;
        }
    }

    private static BusinessHour getBusinessHour(WebDriver driver, WebDriverWait wait, WebElement parent) {
        try {
            String dayOfWeek = findText(driver, wait, parent, "span.i8cJw", "영업 요일");
            String content = findText(driver, wait, parent, "div.H3ua4", "영업 시간");
            return new BusinessHour(dayOfWeek, content);
        }catch (Exception e){
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 문제 발생 (%s): %s \n", "영업시간 객체 생성", e.getClass(), e.getMessage()));
            return null;
        }
    }

    private static Menu getMenu(WebDriver driver, WebDriverWait wait, WebElement parent) {
        try {
            String name = findText(driver, wait, parent, ".lPzHi", "메뉴 이름");
            String price = findText(driver, wait, parent, ".GXS1X", "메뉴 가격");
            String image = null;
//            try{
//                image = new WebDriverWait(driver, Duration.ofSeconds(1))
//                        .until(ExpectedConditions.visibilityOf(parent.findElement(By.cssSelector(".K0PDV")))).getAttribute("src");
//            }catch (Exception e){
//                 메뉴 사진 없는 경우도 있음
//            }
            return new Menu(name, price, image);
        }catch (Exception e){
            logPerThread.get(driver.toString()).append(String.format("[ERROR] [%s 단계] 문제 발생 (%s): %s \n", "메뉴 객체 생성", e.getClass(), e.getMessage()));
            return null;
        }
    }

    private static void setNotCrawlingKeyword(){
        String notCrawlingKeyword = PathConverter.convertPath(root + "/수집제외대상.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(notCrawlingKeyword))) {
            String line;
            while ((line = br.readLine()) != null) {
                notCrawlingKeywords.add(line);
            }
        } catch (Exception e) {
            System.err.println("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private static List<Cafe> getCafeList() {
        String cafeDataFilePath = PathConverter.convertPath(root + "/cafe-list.json");

        try (BufferedReader reader = new BufferedReader(new FileReader(cafeDataFilePath))) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Cafe>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
