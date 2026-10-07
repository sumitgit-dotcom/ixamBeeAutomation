package pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

   
    @FindBy(xpath = "//h1[contains(text(),'Govt. Job Prep')]")
    private WebElement mainHeading;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

   
    public void open(String url) {
        driver.get(url);

       
        wait.until(ExpectedConditions.urlContains("ixambee"));

        
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete"));

        System.out.println("✅ Opened: " + url);
        System.out.println("📄 Actual title: " + driver.getTitle());
        System.out.println("🔗 Current URL : " + driver.getCurrentUrl());
    }

    
    public String getPageTitle() {
        return driver.getTitle();
    }

   
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isMainHeadingDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(mainHeading));
            return mainHeading.isDisplayed();
        } catch (Exception e) {
            System.out.println("⚠️ Main heading not found: " + e.getMessage());
            return false;
        }
    }
}