package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ElPaisScraperDemo {
    public static void main(String[] args) {

        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://elpais.com");
            driver.manage().window().maximize();
            //driver.manage().deleteAllCookies();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Accept cookies if prompted
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@id='didomi-notice-agree-button']")
                ));
                //button[@id='didomi-notice-agree-button'] //button[contains(text(), 'Aceptar')]
                acceptCookies.click();
                System.out.println("Cookies accepted.");
            } catch (Exception e) {
                System.out.println("No cookie popup found or already accepted.");
            }

            // Navigate to Opinion section
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/opinion/') and contains(text(), 'Opinión')]") //there are 3 elements found but clicks first one
            ));
            opinionLink.click();

            // Wait for articles to load
            Thread.sleep(3000);

            // Fetch first 5 article titles and contents
            List<WebElement> articles1 = driver.findElements(By.xpath("//article[contains(@class, 'c')]//h2/a"));

            int count = Math.min(articles1.size(), 5);
            //StaleElementReferenceException because the articles list (with its stored WebElements) is created before you navigate to the article
            // — and after you navigate away and come back, those references become stale.

            //Best Fix: Re-fetch the article links inside the loop each time — and only use hrefs, not the WebElement references.

            for (int i = 0; i < count; i++) {
                // Re-fetch the list of articles on every loop to avoid stale references
                List<WebElement> articles = driver.findElements(By.xpath("//article[contains(@class, 'c')]//h2/a"));

                // check to avoid IndexOutOfBoundsException
                if (i >= articles.size()) {
                    System.out.println("Article list size changed. Skipping index " + i);
                    continue;
                }
                //  If the new list of articles is smaller than expected,  skip processing that index (continue) instead of crashing

                // Use the href string directly
                String articleUrl = articles.get(i).getAttribute("href");
                if (articleUrl == null || articleUrl.isEmpty()) {
                    System.out.println("No valid href found for article " + i);
                    continue;
                }

                // Navigate to the article
                driver.navigate().to(articleUrl);
                Thread.sleep(2000);

                // Get title and content
                try {
                    String title = driver.findElement(By.tagName("h1")).getText();
                    String content = driver.findElement(By.tagName("article")).getText();

                    System.out.println("\nTitle: " + i  + ":" + title);
                    System.out.println("Content: " + content.substring(0, Math.min(300, content.length())) + "...");
                    //This line prints only the first 300 characters of the content, or less if the content is shorter than 300 characters — followed by "...".

                } catch (Exception e) {
                    System.out.println("Failed to extract title/content for article " + i);
                    e.printStackTrace();
                }

                // Navigate back
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
