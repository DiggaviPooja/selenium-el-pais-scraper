package org.example;

import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class ElPaisScraper {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        Map<String, Integer> wordCount = new HashMap<>();
        List<String> translatedTitles = new ArrayList<>();

        try {
            driver.get("https://elpais.com");
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Accept cookies
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id("didomi-notice-agree-button")));
                acceptCookies.click();
                System.out.println("Cookies accepted.");
            } catch (Exception e) {
                System.out.println("No cookie popup.");
            }

            // Navigate to Opinión section
            WebElement opinionLink = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/opinion/') and contains(text(), 'Opinión')]")));
            opinionLink.click();
            Thread.sleep(3000);

            int count = 5;
            for (int i = 0; i < count; i++) {
                List<WebElement> articles = driver.findElements(By.xpath("//article[contains(@class, 'c')]//h2/a"));
                if (i >= articles.size()) continue;

                String articleUrl = articles.get(i).getAttribute("href");
                if (articleUrl == null || articleUrl.isEmpty()) continue;

                driver.navigate().to(articleUrl);
                Thread.sleep(2000);

                try {
                    String title = driver.findElement(By.tagName("h1")).getText();
                    String content = driver.findElement(By.tagName("article")).getText();

                    System.out.println("\nTitle " + (i + 1) + ": " + title);
                    System.out.println("Content: " + content.substring(0, Math.min(300, content.length())) + "...");

                    // Download cover image if available
                    try {
                        WebElement imageElement = driver.findElement(By.xpath("//figure//img"));
                        String imgSrc = imageElement.getAttribute("src");
                        if (imgSrc != null && !imgSrc.isEmpty()) {
                            downloadImage(imgSrc, "article_" + (i + 1) + ".jpg");
                            System.out.println("Downloaded cover image.");
                        }
                    } catch (Exception e) {
                        System.out.println("No image found for article " + (i + 1));
                    }

                    // Translate the title
                    String translated = translateText(title, "en");
                    translatedTitles.add(translated);
                    System.out.println("Translated Title: " + translated);

                    // Count words
                    for (String word : translated.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+")) {
                        if (!word.isEmpty()) {
                            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Failed to extract title/content.");
                    e.printStackTrace();
                }

                driver.navigate().back();
                Thread.sleep(2000);
            }

            // Print repeated words
            System.out.println("\nWords repeated more than twice:");
            wordCount.entrySet().stream()
                    .filter(entry -> entry.getValue() > 2)
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // Download image
    private static void downloadImage(String imageUrl, String fileName) throws IOException {
        InputStream in = new URL(imageUrl).openStream();
        Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        in.close();
    }

    // Translate text using Google Translate API
    private static String translateText(String text, String targetLang) throws IOException {
        String apiKey = "YOUR_API_KEY";
        String urlStr = "https://translation.googleapis.com/language/translate/v2?q=" +
                URLEncoder.encode(text, "UTF-8") +
                "&target=" + targetLang +
                "&key=" + apiKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) responseBuilder.append(line);
        in.close();

        JSONObject jsonObject = new JSONObject(responseBuilder.toString());
        return jsonObject.getJSONObject("data").getJSONArray("translations")
                .getJSONObject(0).getString("translatedText");
    }
}