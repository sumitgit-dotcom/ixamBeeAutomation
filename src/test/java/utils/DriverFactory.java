package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriverFactory {

    private static WebDriver driver;
    private static Actions actions;

    public static WebDriver initDriver() {

       
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.OFF);

       
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.setExperimentalOption("excludeSwitches",
                new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

        actions = new Actions(driver);

        System.out.println("🚀 Browser launched: Chrome");
        return driver;
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static Actions getActions() {
        if (actions == null) actions = new Actions(driver);
        return actions;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            actions = null;
            System.out.println("🛑 Browser closed");
        }
    }
}