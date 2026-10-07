package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import pages.HomePage;
import utils.ConfigReader;
import utils.DriverFactory;

import java.util.List;

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

    // ---------------- Smoke test ----------------

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

    // ---------------- Dropdown test ----------------

    @When("I hover on the {string} dropdown")
    public void i_hover_on_the_dropdown(String dropdownName) throws Exception {
        if (dropdownName.equalsIgnoreCase("Online Course")) {
            homePage.hoverOnOnlineCourseDropdown();
        } else {
            throw new IllegalArgumentException("Unsupported dropdown: " + dropdownName);
        }
    }

    @Then("I should see the Online Course dropdown items listed")
    public void i_should_see_the_online_course_dropdown_items_listed() {
        List<String> items = homePage.getOnlineCourseDropdownItems();

        System.out.println("──────── Online Course Dropdown Items ────────");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i));
        }
        System.out.println("──────────────────────────────────────────────");

        assertTrue(!items.isEmpty(),
                "❌ No items found in Online Course dropdown");
    }
}