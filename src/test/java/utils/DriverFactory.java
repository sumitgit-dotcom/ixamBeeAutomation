package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriverFactory {

    private static WebDriver driver;
    private static Actions actions;
    private static String currentBrowser;

    public static WebDriver initDriver(String browser) {

       
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.OFF);

        currentBrowser = browser == null ? "chrome" : browser.trim().toLowerCase();
        System.out.println("🌐 Launching browser: " + currentBrowser);

        switch (currentBrowser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOpts = new ChromeOptions();
                chromeOpts.addArguments("--start-maximized");
                chromeOpts.addArguments("--disable-notifications");
                chromeOpts.addArguments("--disable-blink-features=AutomationControlled");
                chromeOpts.addArguments("--remote-allow-origins=*");
                chromeOpts.setExperimentalOption("excludeSwitches",
                        new String[]{"enable-automation"});
                chromeOpts.setExperimentalOption("useAutomationExtension", false);
                driver = new ChromeDriver(chromeOpts);
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOpts = new EdgeOptions();
                edgeOpts.addArguments("--start-maximized");
                edgeOpts.addArguments("--disable-notifications");
                edgeOpts.addArguments("--disable-blink-features=AutomationControlled");
                edgeOpts.setExperimentalOption("excludeSwitches",
                        new String[]{"enable-automation"});
                edgeOpts.setExperimentalOption("useAutomationExtension", false);
                driver = new EdgeDriver(edgeOpts);
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOpts = new FirefoxOptions();
                firefoxOpts.addArguments("--width=1920");
                firefoxOpts.addArguments("--height=1080");
               
                driver = new FirefoxDriver(firefoxOpts);
                driver.manage().window().maximize();
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

        actions = new Actions(driver);

        System.out.println("🚀 Browser ready: " + currentBrowser);
        return driver;
    }

    
    public static WebDriver initDriver() {
        return initDriver("chrome");
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static Actions getActions() {
        if (actions == null) actions = new Actions(driver);
        return actions;
    }

    public static String getCurrentBrowser() {
        return currentBrowser;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            actions = null;
            System.out.println("🛑 Browser closed: " + currentBrowser);
        }
    }
}