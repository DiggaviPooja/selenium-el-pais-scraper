package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ElPaisScraper {
    public static void main(String[] args) {

        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://elpais.com");
            driver.manage().window().maximize();

            // Accept cookies if prompted
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'Aceptar')]")
                ));
                acceptCookies.click();
                System.out.println("Cookies accepted.");
            } catch (Exception e) {
                System.out.println("No cookie popup found or already accepted.");
            }

            // Navigate to Opinion section
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/opinion/') and contains(text(), 'Opinión')]")
            ));
            opinionLink.click();

            // Wait for articles to load
            Thread.sleep(3000);

            // Fetch first 5 article titles and contents
            List<WebElement> articles = driver.findElements(By.xpath("//article[contains(@class, 'c')]//h2/a"));

            int count = Math.min(articles.size(), 5);

            for (int i = 0; i < count; i++) {
                WebElement article = articles.get(i);
                String articleUrl = article.getAttribute("href");

                assert articleUrl != null;
                driver.navigate().to(articleUrl);
                Thread.sleep(2000);

                String title = driver.findElement(By.tagName("h1")).getText();
                String content = driver.findElement(By.tagName("article")).getText();

                System.out.println("\nTitle: " + title);
                System.out.println("Content: " + content.substring(0, Math.min(300, content.length())) + "...");

                // Optionally download cover image
                try {
                    WebElement img = driver.findElement(By.xpath("//article//img"));
                    String imgUrl = img.getAttribute("src");
                    System.out.println("Cover Image URL: " + imgUrl);
                } catch (Exception e) {
                    System.out.println("No cover image found.");
                }

                driver.navigate().back();
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
