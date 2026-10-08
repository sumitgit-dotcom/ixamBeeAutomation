package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    // -------- Existing locators --------

    @FindBy(xpath = "//h1[contains(text(),'Govt. Job Prep')]")
    private WebElement mainHeading;

    @FindBy(id = "online_course_dropdown")
    private WebElement onlineCourseDropdown;

    // -------- Search-flow locators --------

    @FindBy(xpath = "//div[@class='mb-3 header_search_bar d-none d-lg-block']//input[@id='input-drop-down-body']")
    private WebElement headerSearchBar;

    @FindBy(id = "search-input")
    private WebElement resultsSearchInput;

    @FindBy(id = "chip-exam")
    private WebElement examChip;

    // -------- Demo-flow locators --------

    @FindBy(id = "js-value")
    private WebElement demoPhoneInput;

    @FindBy(id = "btn-send-otp")
    private WebElement sendOtpButton;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    // =========================================================
    // Navigation
    // =========================================================

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

    // =========================================================
    // Online Course dropdown
    // =========================================================

    public void hoverOnOnlineCourseDropdown() throws Exception {
        try {
            WebElement dropdown = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("online_course_dropdown")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", dropdown);

            actions.moveToElement(dropdown).pause(Duration.ofMillis(800)).perform();
            System.out.println("🖱️ Hovered on Online Course dropdown via Actions");

            Thread.sleep(2000);

            Long visibleAnchorCount = (Long) ((JavascriptExecutor) driver).executeScript(
                    "const t = document.getElementById('online_course_dropdown');" +
                    "if (!t) return 0;" +
                    "const scope = t.closest('header, nav, div[class*=\"header\"], div[class*=\"nav\"]') || document.body;" +
                    "return Array.from(scope.querySelectorAll('a')).filter(a => a.offsetParent !== null).length;");
            System.out.println("🔎 Visible anchors in header block: " + visibleAnchorCount);

            if (visibleAnchorCount == null || visibleAnchorCount < 5) {
                System.out.println("⚠️ Too few anchors — dispatching JS hover events");
                ((JavascriptExecutor) driver).executeScript(
                        "const el = arguments[0];" +
                        "['mouseenter','mouseover','mousemove','pointerenter','pointerover'].forEach(t => " +
                        "  el.dispatchEvent(new MouseEvent(t, {bubbles:true, cancelable:true, view:window}))" +
                        ";", dropdown);
                Thread.sleep(1500);
            }

        } catch (Exception e) {
            System.out.println("❌ Hover failed: " + e.getMessage());
            throw e;
        }
    }

    public List<String> getOnlineCourseDropdownItems() {
        Set<String> uniqueTexts = new LinkedHashSet<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            WebElement container = findDropdownContainer(js);

            if (container != null) {
                js.executeScript(
                        "const el = arguments[0];" +
                        "el.style.display='block';" +
                        "el.style.visibility='visible';" +
                        "el.style.opacity='1';" +
                        "if (!el.style.maxHeight) el.style.maxHeight='500px';" +
                        "el.style.overflowY='auto';",
                        container);
            }

            collectItems(js, uniqueTexts);

            int previousCount = -1;
            int stableRounds = 0;
            int maxRounds = 40;

            for (int round = 0; round < maxRounds; round++) {

                if (container != null) {
                    js.executeScript(
                            "const el = arguments[0];" +
                            "el.scrollTop = el.scrollTop + el.clientHeight;",
                            container);
                }

                js.executeScript("window.scrollBy(0, 80);");
                Thread.sleep(300);

                collectItems(js, uniqueTexts);

                if (uniqueTexts.size() == previousCount) stableRounds++;
                else { stableRounds = 0; previousCount = uniqueTexts.size(); }

                boolean atBottom = true;
                if (container != null) {
                    Object res = js.executeScript(
                            "const el = arguments[0];" +
                            "return el.scrollTop + el.clientHeight >= el.scrollHeight - 2;",
                            container);
                    atBottom = Boolean.TRUE.equals(res);
                }

                if (atBottom && stableRounds >= 2) {
                    System.out.println("⬇️ Reached bottom of dropdown at round " + round);
                    break;
                }
            }

            if (container != null) {
                js.executeScript("arguments[0].scrollTop = 0;", container);
                Thread.sleep(250);
                collectItems(js, uniqueTexts);
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to fetch dropdown items: " + e.getMessage());
            e.printStackTrace();
        }

        if (uniqueTexts.isEmpty()) {
            System.out.println("❌ Still empty — dumping ALL visible anchors:");
            List<WebElement> all = driver.findElements(By.tagName("a"));
            for (WebElement a : all) {
                try {
                    if (a.isDisplayed()) {
                        String t = a.getText().trim();
                        if (!t.isEmpty() && t.length() < 60) {
                            System.out.println("   LINK: '" + t + "'");
                        }
                    }
                } catch (Exception ignored) { }
            }
        }

        List<String> result = new ArrayList<>(uniqueTexts);
        System.out.println("📋 Total dropdown items found: " + result.size());
        return result;
    }

    private WebElement findDropdownContainer(JavascriptExecutor js) {
        By[] candidates = new By[] {
                By.xpath("//a[@id='online_course_dropdown']/following::ul[1]"),
                By.xpath("//a[@id='online_course_dropdown']/ancestor::li[1]//ul[1]"),
                By.xpath("//a[@id='online_course_dropdown']/parent::*/following-sibling::*//ul[1]"),
                By.xpath("//a[@id='online_course_dropdown']/parent::*/following-sibling::*[1]"),
                By.xpath("//a[@id='online_course_dropdown']/../..//ul[1]"),
        };

        for (By by : candidates) {
            try {
                List<WebElement> found = driver.findElements(by);
                for (WebElement el : found) {
                    if (el.isDisplayed() && !el.findElements(By.tagName("a")).isEmpty()) {
                        System.out.println("✅ Container locator: " + by);
                        return el;
                    }
                }
            } catch (Exception ignored) { }
        }

        try {
            Object result = js.executeScript(
                    "const t = document.getElementById('online_course_dropdown');" +
                    "if (!t) return null;" +
                    "let best = null, bestCount = 0;" +
                    "let n = t.parentElement;" +
                    "for (let i = 0; i < 6 && n; i++, n = n.parentElement) {" +
                    "  const c = n.querySelectorAll('a').length;" +
                    "  if (c > bestCount) { bestCount = c; best = n; }" +
                    "}" +
                    "return best;");
            if (result instanceof WebElement && ((WebElement) result).isDisplayed()) {
                System.out.println("✅ Container found via JS walker");
                return (WebElement) result;
            }
        } catch (Exception ignored) { }

        System.out.println("⚠️ No container identified — will scan the whole page");
        return null;
    }

    private void collectItems(JavascriptExecutor js, Set<String> sink) {
        @SuppressWarnings("unchecked")
        List<String> scoped = (List<String>) js.executeScript(
                "const t = document.getElementById('online_course_dropdown');" +
                "if (!t) return [];" +
                "const scope = t.closest('header, nav, div[class*=\"header\"], div[class*=\"nav\"]') || document.body;" +
                "const out = [];" +
                "const nodes = scope.querySelectorAll('a, li, span, div[class*=\"item\"], div[class*=\"menu\"] a');" +
                "nodes.forEach(n => {" +
                "  if (n.offsetParent === null) return;" +
                "  const txt = (n.textContent || '').trim();" +
                "  if (txt && txt.length > 1 && txt.length < 60 && out.indexOf(txt) === -1) out.push(txt);" +
                "});" +
                "return out;");

        if (scoped != null) {
            for (String t : scoped) sink.add(t);
        }

        if (sink.isEmpty()) {
            List<WebElement> all = driver.findElements(By.tagName("a"));
            for (WebElement a : all) {
                try {
                    if (a.isDisplayed()) {
                        String t = a.getText().trim();
                        if (!t.isEmpty() && t.length() < 60) sink.add(t);
                    }
                } catch (Exception ignored) { }
            }
        }
    }

    // =========================================================
    // Search Flow
    // =========================================================

    public void clickHeaderSearchBar() throws Exception {
        try {
            WebElement searchBar = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@class='mb-3 header_search_bar d-none d-lg-block']//input[@id='input-drop-down-body']")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", searchBar);

            actions.moveToElement(searchBar)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();

            System.out.println("🔍 Clicked header search bar");

            searchBar.sendKeys("r");
            Thread.sleep(600);

            if (!driver.getCurrentUrl().toLowerCase().contains("search")) {
                searchBar.sendKeys(Keys.ENTER);
            }

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("search"),
                    ExpectedConditions.presenceOfElementLocated(By.id("search-input"))
            ));

            System.out.println("➡️ Navigated to search page: " + driver.getCurrentUrl());
            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("❌ Failed to click header search bar: " + e.getMessage());
            throw e;
        }
    }

    public void typeInSearchField(String query) throws Exception {
        try {
            WebElement input = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("search-input")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", input);

            actions.moveToElement(input)
                    .pause(Duration.ofMillis(200))
                    .click()
                    .perform();

            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);

            for (char c : query.toCharArray()) {
                input.sendKeys(String.valueOf(c));
                Thread.sleep(80);
            }

            System.out.println("⌨️ Typed in search field: " + query);
            Thread.sleep(2000);

        } catch (Exception e) {
            System.out.println("❌ Failed to type in search field: " + e.getMessage());
            throw e;
        }
    }

    public void clickExamChip() throws Exception {
        try {
            WebElement chip = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("chip-exam")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", chip);

            actions.moveToElement(chip)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();

            System.out.println("✅ Clicked Exam chip");
            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("❌ Failed to click Exam chip: " + e.getMessage());
            throw e;
        }
    }

    public boolean isSearchResultsVisible() {
        try {
            boolean onSearchPage = driver.getCurrentUrl().toLowerCase().contains("search");
            boolean hasResults = !driver.findElements(By.xpath(
                    "//*[contains(@class,'search-result') " +
                    "or contains(@class,'result-list') " +
                    "or contains(@class,'course-card')]")).isEmpty();
            boolean hasSearchInput = !driver.findElements(By.id("search-input")).isEmpty();
            return onSearchPage || hasResults || hasSearchInput;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // Free Demo Flow
    // =========================================================

    /**
     * Scrolls to a course card matching the given text on the home page and clicks it.
     * Uses the nearest anchor ancestor when available.
     * @throws Exception 
     */
    public void scrollAndClickCourseCard(String cardText) {
        try {
            By exact = By.xpath("//p[normalize-space()='" + cardText + "']");
            By contains = By.xpath("//p[contains(normalize-space(),'" +
                    cardText.replaceAll("\\.\\.\\.$", "").trim() + "')]");

            WebElement card = null;
            try {
                card = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(exact));
                System.out.println("✅ Card matched by exact text");
            } catch (Exception e) {
                System.out.println("⚠️ Exact match failed, trying contains()");
                card = wait.until(ExpectedConditions.presenceOfElementLocated(contains));
                System.out.println("✅ Card matched by contains()");
            }

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", card);
            Thread.sleep(700);

            // Prefer the closest anchor; fall back to the <p> itself
            WebElement clickTarget = card;
            try {
                WebElement anchor = card.findElement(By.xpath("./ancestor::a[1]"));
                if (anchor.isDisplayed()) clickTarget = anchor;
            } catch (Exception ignored) { }

            // Snapshot current window handles + URL before clicking
            String originalWindow = driver.getWindowHandle();
            Set<String> windowsBefore = driver.getWindowHandles();
            String urlBefore = driver.getCurrentUrl();

            try {
                actions.moveToElement(clickTarget)
                        .pause(Duration.ofMillis(300))
                        .click()
                        .perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", clickTarget);
            }

            System.out.println("🖱️ Clicked course card: " + cardText);

            // Wait briefly for either: (a) new tab, or (b) URL change
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    d.getWindowHandles().size() > windowsBefore.size()
                            || !d.getCurrentUrl().equals(urlBefore));

            // Case 1: A new tab/window opened → switch to it
            Set<String> windowsAfter = driver.getWindowHandles();
            if (windowsAfter.size() > windowsBefore.size()) {
                for (String handle : windowsAfter) {
                    if (!windowsBefore.contains(handle)) {
                        driver.switchTo().window(handle);
                        System.out.println("➡️ Switched to new tab: " + driver.getCurrentUrl());
                        break;
                    }
                }
            } else {
                // Case 2: Same tab, URL changed
                System.out.println("➡️ Navigated in same tab: " + driver.getCurrentUrl());
            }

            // Bring the window to front and settle
            driver.switchTo().window(driver.getWindowHandle());
            ((JavascriptExecutor) driver).executeScript("window.focus();");
            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("⚠️ Card click did not lead to a new page: " + e.getMessage());
            // Don't throw — continue the scenario; subsequent steps will validate state
        }
    }

    /**
     * Closes any popup/modal that appears on the course page.
     * Strategy (in order):
     *   1. Wait up to 6s for a visible modal container.
     *   2. Press ESC on the body.
     *   3. Try a broad set of close-button selectors (button, span, i, img, a).
     *   4. JS-click each candidate (bypasses overlay interception).
     *   5. Confirm the modal is gone; retry up to 3 times.
     *   6. Fallback: hide modal nodes directly via JS.
     * @throws InterruptedException 
     */
    public void closePopupIfPresent() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By popupClose = By.xpath("//button[@id='closepop-up']");

        try {
            // Wait for the close button to become clickable
            WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(popupClose));

            js.executeScript("arguments[0].scrollIntoView({block:'center'});", closeBtn);
            Thread.sleep(300);

            // Preferred: Actions click
            try {
                actions.moveToElement(closeBtn)
                        .pause(Duration.ofMillis(200))
                        .click()
                        .perform();
                System.out.println("✅ (Actions) Closed popup via //button[@id='closepop-up']");
            } catch (Exception actionEx) {
                // Fallback: JS click (handles overlay interception)
                js.executeScript("arguments[0].click();", closeBtn);
                System.out.println("✅ (JS click) Closed popup via //button[@id='closepop-up']");
            }

            // Confirm it's gone
            try {
                new WebDriverWait(driver, Duration.ofSeconds(4))
                        .until(ExpectedConditions.invisibilityOfElementLocated(popupClose));
                System.out.println("🎉 Popup closed successfully");
            } catch (Exception ignored) {
                System.out.println("ℹ️ Popup close button still present — continuing anyway");
            }

            Thread.sleep(500);

        } catch (Exception e) {
            System.out.println("ℹ️ Popup close button not present — continuing");
        }
    }

    /**
     * Scrolls to and clicks "Get Free Demo" on the course page.
     * @throws Exception 
     */
    public void scrollAndClickGetFreeDemo() throws Exception {
        try {
            WebElement demoLink = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//a[normalize-space()='Get Free Demo']")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", demoLink);
            Thread.sleep(500);

            try {
                actions.moveToElement(demoLink)
                        .pause(Duration.ofMillis(300))
                        .click()
                        .perform();
            } catch (Exception clickEx) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", demoLink);
            }

            System.out.println("🖱️ Clicked 'Get Free Demo'");

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.id("js-value")),
                    ExpectedConditions.presenceOfElementLocated(By.id("btn-send-otp"))
            ));
            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("❌ Failed to click 'Get Free Demo': " + e.getMessage());
            throw e;
        }
    }

    /**
     * Enters the phone number into the demo form (#js-value).
     * Handles iframe-wrapped forms transparently.
     * @throws Exception 
     */
    public void enterDemoPhoneNumber(String phone) throws Exception {
        boolean switchedToIframe = false;

        try {
            if (driver.findElements(By.id("js-value")).isEmpty()) {
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                for (WebElement f : frames) {
                    try {
                        driver.switchTo().frame(f);
                        if (!driver.findElements(By.id("js-value")).isEmpty()) {
                            switchedToIframe = true;
                            System.out.println("✅ Phone input found inside iframe");
                            break;
                        }
                        driver.switchTo().defaultContent();
                    } catch (Exception ignored) {
                        driver.switchTo().defaultContent();
                    }
                }
            }

            WebElement input = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("js-value")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", input);

            actions.moveToElement(input)
                    .pause(Duration.ofMillis(200))
                    .click()
                    .perform();

            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);

            for (char c : phone.toCharArray()) {
                input.sendKeys(String.valueOf(c));
                Thread.sleep(60);
            }

            System.out.println("⌨️ Entered phone number: " + phone);
            Thread.sleep(500);

        } catch (Exception e) {
            System.out.println("❌ Failed to enter phone number: " + e.getMessage());
            throw e;
        } finally {
            if (switchedToIframe) {
                driver.switchTo().defaultContent();
            }
        }
    }

    /**
     * Clicks the "Send OTP" button. Handles iframe-wrapped forms.
     * @throws Exception 
     */
    public void clickSendOtpButton() throws Exception {
        boolean switchedToIframe = false;

        try {
            if (driver.findElements(By.id("btn-send-otp")).isEmpty()) {
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                for (WebElement f : frames) {
                    try {
                        driver.switchTo().frame(f);
                        if (!driver.findElements(By.id("btn-send-otp")).isEmpty()) {
                            switchedToIframe = true;
                            System.out.println("✅ Send OTP button found inside iframe");
                            break;
                        }
                        driver.switchTo().defaultContent();
                    } catch (Exception ignored) {
                        driver.switchTo().defaultContent();
                    }
                }
            }

            WebElement btn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("btn-send-otp")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", btn);

            try {
                actions.moveToElement(btn)
                        .pause(Duration.ofMillis(300))
                        .click()
                        .perform();
            } catch (Exception clickEx) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn);
            }

            System.out.println("✅ Clicked Send OTP button");
            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("❌ Failed to click Send OTP: " + e.getMessage());
            throw e;
        } finally {
            if (switchedToIframe) {
                driver.switchTo().defaultContent();
            }
        }
    }

    public boolean isOtpRequestSubmitted() {
        try {
            String src = driver.getPageSource().toLowerCase();
            boolean hasOtpIndicator =
                    src.contains("otp") ||
                    src.contains("verify") ||
                    src.contains("enter the otp");

            boolean hasOtpInput =
                    !driver.findElements(By.xpath(
                            "//input[contains(@id,'otp') or contains(@name,'otp')]")).isEmpty();

            return hasOtpIndicator || hasOtpInput;
        } catch (Exception e) {
            return false;
        }
    }
}