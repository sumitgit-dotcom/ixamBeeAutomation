package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import pages.HomePage;
import utils.ConfigReader;
import utils.DriverFactory;

import static org.testng.Assert.assertTrue;

public class HomePageSteps {

    private WebDriver driver;
    private HomePage homePage;

    @Before
    public void setUp() {
        driver = DriverFactory.initDriver();
        homePage = new HomePage(driver);
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    @Given("I open the ixamBee home page")
    public void i_open_the_ixambee_home_page() {
        String url = ConfigReader.get("base.url");
        homePage.open(url);
    }

    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String expectedTitle) {
        String actualTitle = homePage.getPageTitle();

        System.out.println("Expected (contains): " + expectedTitle);
        System.out.println("Actual title       : " + actualTitle);

       
        assertTrue(
                actualTitle.toLowerCase().contains(expectedTitle.toLowerCase()),
                "❌ Title mismatch!\nExpected to contain: " + expectedTitle
                        + "\nActual title       : " + actualTitle
        );

        System.out.println("🎉 Title verified successfully");
    }
}