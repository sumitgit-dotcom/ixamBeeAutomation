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

    // -------- Smoke test --------

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

        assertTrue(actualTitle.toLowerCase().contains(expectedTitle.toLowerCase()),
                "❌ Title mismatch!\nExpected to contain: " + expectedTitle
                        + "\nActual title       : " + actualTitle);

        System.out.println("🎉 Title verified successfully");
    }

    // -------- Online Course dropdown --------

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

        assertTrue(!items.isEmpty(), "❌ No items found in Online Course dropdown");
    }

    // -------- Search flow --------

    @When("I click the header search bar")
    public void i_click_the_header_search_bar() throws Exception {
        homePage.clickHeaderSearchBar();
    }

    @When("I type {string} in the search field")
    public void i_type_in_the_search_field(String query) throws Exception {
        homePage.typeInSearchField(query);
    }

    @When("I click the {string} chip")
    public void i_click_the_chip(String chipName) throws Exception {
        if (chipName.equalsIgnoreCase("Exam")) {
            homePage.clickExamChip();
        } else {
            throw new IllegalArgumentException("Unsupported chip: " + chipName);
        }
    }

    @Then("the search results should be filtered by Exam")
    public void the_search_results_should_be_filtered_by_exam() {
        assertTrue(homePage.isSearchResultsVisible(),
                "❌ Search results page not visible after clicking Exam chip");
        System.out.println("🎉 Search flow completed successfully");
    }

    // -------- Free Demo Flow --------

    @When("I scroll and click the {string} card")
    public void i_scroll_and_click_the_card(String cardText) throws Exception {
        homePage.scrollAndClickCourseCard(cardText);
    }

    @When("I close the popup on the course page")
    public void i_close_the_popup_on_the_course_page() throws InterruptedException {
        homePage.closePopupIfPresent();
    }

    @When("I scroll and click {string}")
    public void i_scroll_and_click(String linkText) throws Exception {
        if (linkText.equalsIgnoreCase("Get Free Demo")) {
            // Popup may re-appear during scroll — try closing again
            homePage.closePopupIfPresent();
            homePage.scrollAndClickGetFreeDemo();
        } else {
            throw new IllegalArgumentException("Unsupported link: " + linkText);
        }
    }

    @When("I enter phone number {string} in the demo form")
    public void i_enter_phone_number_in_the_demo_form(String phone) throws Exception {
        homePage.enterDemoPhoneNumber(phone);
    }

    @When("I click the {string} button")
    public void i_click_the_button(String buttonText) throws Exception {
        if (buttonText.equalsIgnoreCase("Send OTP")) {
            homePage.clickSendOtpButton();
        } else {
            throw new IllegalArgumentException("Unsupported button: " + buttonText);
        }
    }

    @Then("the OTP request should be submitted")
    public void the_otp_request_should_be_submitted() {
        assertTrue(homePage.isOtpRequestSubmitted(),
                "❌ OTP request was not submitted / no OTP indicator found");
        System.out.println("🎉 Demo OTP flow completed");
    }
}