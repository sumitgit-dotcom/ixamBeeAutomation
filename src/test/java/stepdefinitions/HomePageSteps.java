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
        String browser = System.getProperty("browser");
        if (browser == null || browser.isEmpty()) {
            browser = ConfigReader.get("browser");
        }
        if (browser == null || browser.isEmpty()) {
            browser = "chrome";
        }

        System.out.println("══════════════════════════════════════════════");
        System.out.println("▶️  Starting scenario in browser: " + browser);
        System.out.println("══════════════════════════════════════════════");

        driver = DriverFactory.initDriver(browser);
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

    // -------- Online Course dropdown (@dropdown) --------

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

    // -------- Search flow (@search) --------

    @When("I open the search page")
    public void i_open_the_search_page() throws Exception {
        homePage.openSearchPage();
    }
    @When("I type {string} in the search field")
    public void i_type_in_the_search_field(String query) throws Exception {
        homePage.typeInSearchField(query);
    }

    @When("I click the {string} chip")
    public void i_click_the_chip(String chipName) throws Exception {
        if (chipName.equalsIgnoreCase("Exam")) {
            // Works from the search page and from a course-results page
            homePage.clickExamChipOnResults();
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

    // -------- Free Demo Flow (@demo) --------

    @When("I scroll and click the {string} card")
    public void i_scroll_and_click_the_card(String cardText) {
        homePage.scrollAndClickCourseCard(cardText);
    }

    @When("I close the popup on the course page")
    public void i_close_the_popup_on_the_course_page() {
        homePage.closePopupIfPresent();
    }

    @When("I scroll and click {string}")
    public void i_scroll_and_click(String linkText) throws Exception {
        if (linkText.equalsIgnoreCase("Get Free Demo")) {
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
        } else if (buttonText.equalsIgnoreCase("Try Me Package")) {
            homePage.scrollAndClickTryMePackage();
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

    // -------- Header dropdowns + multi-tab (@header) --------

    @When("I hover on all header dropdowns")
    public void i_hover_on_all_header_dropdowns() {
        homePage.hoverAllHeaderDropdowns();
    }

    @When("I open {string} in a new tab")
    public void i_open_in_a_new_tab(String linkText) throws Exception {
        homePage.openLinkInNewTab(linkText);
    }

    @Then("both new tabs should be open")
    public void both_new_tabs_should_be_open() {
        assertTrue(homePage.areBothTabsOpen(),
                "❌ Expected at least 3 tabs (1 original + 2 new) but found fewer");
        System.out.println("🎉 Multi-tab flow completed");
    }

    // -------- Course Flow (@course) --------

    @When("I click the {string} dropdown")
    public void i_click_the_dropdown(String dropdownName) throws Exception {
        if (dropdownName.equalsIgnoreCase("Online Course")) {
            homePage.clickOnlineCourseDropdown();
        } else {
            throw new IllegalArgumentException("Unsupported dropdown: " + dropdownName);
        }
    }

    @When("I scroll and click the Insurance item in the online course dropdown")
    public void i_scroll_and_click_insurance_item() throws Exception {
        homePage.clickInsuranceDropdownItem();
    }

    @When("I scroll and click the NIACL Assistant link for online course 16")
    public void i_scroll_and_click_niacl_assistant_link() throws Exception {
        homePage.clickNIACLAssistantLink();
    }

    @When("I scroll and click the {string} button")
    public void i_scroll_and_click_the_button(String buttonName) throws Exception {
        if (buttonName.equalsIgnoreCase("Try Me Package")) {
            homePage.scrollAndClickTryMePackage();
        } else {
            throw new IllegalArgumentException("Unsupported button: " + buttonName);
        }
    }

    @Then("the Try Me Package page should be open")
    public void the_try_me_package_page_should_be_open() {
        assertTrue(homePage.isTryMePackagePageOpen(),
                "❌ Try Me Package page not detected after click");
        System.out.println("🎉 Course flow completed");
    }
    
    // -------- Syllabus Flow (@syllabus) --------

    @When("I click the {string} link")
    public void i_click_the_link(String linkText) throws Exception {
        if (linkText.equalsIgnoreCase("# Bank Po Course")) {
            homePage.clickBankPoCourseLink();
        } else {
            throw new IllegalArgumentException("Unsupported link: " + linkText);
        }
    }

    @When("I click the syllabus result link")
    public void i_click_the_syllabus_result_link() throws Exception {
        homePage.clickSyllabusResultLink();
    }

    @Then("the syllabus page should be open")
    public void the_syllabus_page_should_be_open() {
        assertTrue(homePage.isSyllabusPageOpen(),
                "❌ Syllabus page not detected after clicking result link");
        System.out.println("🎉 Syllabus flow completed");
    }
}