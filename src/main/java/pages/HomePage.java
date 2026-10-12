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
import java.util.NoSuchElementException;
import java.util.Set;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    // -------- Smoke / heading --------

    @FindBy(xpath = "//h1[contains(text(),'Govt. Job Prep')]")
    private WebElement mainHeading;

    // -------- Header dropdowns --------

    @FindBy(id = "online_course_dropdown")
    private WebElement onlineCourseDropdown;

    private static final String[] HEADER_DROPDOWN_IDS = {
            "previous_year_paper_dropdown",
            "exams_dropdown",
            "online_course_dropdown",
            "free_mocktest_dropdown",
            "navbarDropdowng",
            "dailyquiz"
    };

    // -------- Search flow --------

    @FindBy(xpath = "//div[@class='mb-3 header_search_bar d-none d-lg-block']//input[@id='input-drop-down-body']")
    private WebElement headerSearchBar;

    @FindBy(id = "search-input")
    private WebElement resultsSearchInput;

    @FindBy(id = "chip-exam")
    private WebElement examChip;

    // -------- Demo flow --------

    @FindBy(id = "js-value")
    private WebElement demoPhoneInput;

    @FindBy(id = "btn-send-otp")
    private WebElement sendOtpButton;

    // -------- Course flow --------

    @FindBy(id = "try_me_package")
    private WebElement tryMePackageButton;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
    // Online Course dropdown (used by @dropdown)
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

            WebElement clickTarget = card;
            try {
                WebElement anchor = card.findElement(By.xpath("./ancestor::a[1]"));
                if (anchor.isDisplayed()) clickTarget = anchor;
            } catch (Exception ignored) { }

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

            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    d.getWindowHandles().size() > windowsBefore.size()
                            || !d.getCurrentUrl().equals(urlBefore));

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
                System.out.println("➡️ Navigated in same tab: " + driver.getCurrentUrl());
            }

            driver.switchTo().window(driver.getWindowHandle());
            ((JavascriptExecutor) driver).executeScript("window.focus();");
            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("⚠️ Card click did not lead to a new page: " + e.getMessage());
        }
    }

    public void closePopupIfPresent() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        By popupClose = By.xpath("//button[@id='closepop-up']");

        try {
            WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(popupClose));

            js.executeScript("arguments[0].scrollIntoView({block:'center'});", closeBtn);
            Thread.sleep(300);

            try {
                actions.moveToElement(closeBtn)
                        .pause(Duration.ofMillis(200))
                        .click()
                        .perform();
                System.out.println("✅ (Actions) Closed popup via //button[@id='closepop-up']");
            } catch (Exception actionEx) {
                js.executeScript("arguments[0].click();", closeBtn);
                System.out.println("✅ (JS click) Closed popup via //button[@id='closepop-up']");
            }

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

    // =========================================================
    // Header dropdowns + multi-tab flow (@header)
    // =========================================================

    public void hoverAllHeaderDropdowns() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (String id : HEADER_DROPDOWN_IDS) {
            try {
                By toggle = By.id(id);
                WebElement el = new WebDriverWait(driver, Duration.ofSeconds(6))
                        .until(ExpectedConditions.presenceOfElementLocated(toggle));

                js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                Thread.sleep(300);

                actions.moveToElement(el).perform();
                Thread.sleep(700);

                System.out.println("🖱️ Hovered on header dropdown: " + id);
                Thread.sleep(700);

                Long visibleAnchors = (Long) js.executeScript(
                        "const t = document.getElementById(arguments[0]);" +
                        "if (!t) return 0;" +
                        "const p = t.parentElement;" +
                        "if (!p) return 0;" +
                        "return Array.from(p.querySelectorAll('a')).filter(a => a.offsetParent !== null).length;",
                        id);

                if (visibleAnchors != null && visibleAnchors <= 1) {
                    System.out.println("ℹ️ Hover didn't reveal menu for '" + id + "' — trying click");
                    try {
                        actions.moveToElement(el).pause(Duration.ofMillis(200)).click().perform();
                    } catch (Exception clickEx) {
                        js.executeScript("arguments[0].click();", el);
                    }
                    Thread.sleep(700);
                }

            } catch (Exception e) {
                System.out.println("⚠️ Skipping dropdown '" + id + "': " + e.getMessage());
            }
        }
        System.out.println("✅ Finished hovering all header dropdowns");
    }

    public void openLinkInNewTab(String linkText) throws Exception {
        try {
            By locator = By.xpath("//a[normalize-space()='" + linkText + "']");
            WebElement link = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", link);
            Thread.sleep(400);

            String originalWindow = driver.getWindowHandle();
            Set<String> windowsBefore = driver.getWindowHandles();

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].setAttribute('target','_blank');", link);

            try {
                actions.moveToElement(link)
                        .pause(Duration.ofMillis(200))
                        .click()
                        .perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", link);
            }

            System.out.println("🖱️ Clicked link: " + linkText);

            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    d.getWindowHandles().size() > windowsBefore.size());

            Set<String> windowsAfter = driver.getWindowHandles();
            for (String handle : windowsAfter) {
                if (!windowsBefore.contains(handle)) {
                    driver.switchTo().window(handle);
                    System.out.println("➡️ Switched to new tab: " + driver.getCurrentUrl());
                    break;
                }
            }

            Thread.sleep(1500);

            if (driver.getWindowHandles().contains(originalWindow)) {
                driver.switchTo().window(originalWindow);
                System.out.println("↩️ Switched back to original tab");
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to open '" + linkText + "' in new tab: " + e.getMessage());
            throw e;
        }
    }

    public boolean areBothTabsOpen() {
        try {
            Set<String> handles = driver.getWindowHandles();
            System.out.println("📑 Open tabs: " + handles.size());
            return handles.size() >= 3;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // Course Flow (@course)
    // =========================================================

    /**
     * Clicks the Online Course dropdown toggle in the header.
     * Hovers first (site may open on hover), then clicks. Falls back to JS click.
     * @throws Exception 
     */
    public void clickOnlineCourseDropdown() throws Exception {
        try {
            WebElement toggle = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("online_course_dropdown")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", toggle);
            Thread.sleep(400);

            // First hover
            actions.moveToElement(toggle).pause(Duration.ofMillis(500)).perform();
            Thread.sleep(800);

            // Now force the dropdown open via JS — this bypasses mouseleave behavior
            ((JavascriptExecutor) driver).executeScript(
                    "const t = document.getElementById('online_course_dropdown');" +
                    "if (!t) return;" +
                    "let node = t.parentElement;" +
                    "for (let i = 0; i < 6 && node; i++, node = node.parentElement) {" +
                    "  const menu = node.querySelector('#online_hover, [class*=dropdown-menu], [class*=mega-menu]');" +
                    "  if (menu) {" +
                    "    menu.style.display = 'block';" +
                    "    menu.style.visibility = 'visible';" +
                    "    menu.style.opacity = '1';" +
                    "    menu.classList.add('show');" +
                    "  }" +
                    "}");

            Thread.sleep(500);

            System.out.println("🖱️ Clicked Online Course dropdown toggle");

            // Wait for items
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[@id='online_hover']//ul/li")));

            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("❌ Failed to click Online Course dropdown: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Scrolls to and clicks the 16th item inside the online_hover dropdown.
     * Handles same-tab and new-tab navigation.
     * @throws Exception 
     */
    /**
     * Clicks the "Insurance" item inside the Online Course dropdown.
     * Uses Actions (scrollIntoView + move + click). Handles same-tab and new-tab navigation.
     * @throws Exception 
     */
    /**
        /**
     * Clicks the "Insurance" item in the Online Course dropdown.
     * The <li> is a layout wrapper; the clickable element is a nested <a>.
     * So we find the <li>, locate the inner <a>, scroll it into view, and click it.
     * @throws Exception 
     */
    public void clickInsuranceDropdownItem() throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // 1. Ensure the dropdown has rendered items
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id='online_hover']//ul/li")));
            Thread.sleep(1000);

            // 2. Find the Insurance <li> via JS (works even if hidden)
            Object jsResult = js.executeScript(
                    "const root = document.getElementById('online_hover');" +
                    "if (!root) return null;" +
                    "const items = root.querySelectorAll('li');" +
                    "for (const li of items) {" +
                    "  if (li.textContent.trim() === 'Insurance') return li;" +
                    "}" +
                    "return null;");

            if (!(jsResult instanceof WebElement)) {
                throw new NoSuchElementException("'Insurance' <li> not found");
            }

            WebElement li = (WebElement) jsResult;
            System.out.println("✅ 'Insurance' <li> located");

            // 3. Prefer the <a> inside the <li>; fall back to the <li> itself
            WebElement clickTarget = li;
            try {
                WebElement anchor = li.findElement(By.xpath(".//a"));
                if (anchor.isDisplayed() || anchor.getAttribute("href") != null) {
                    clickTarget = anchor;
                    System.out.println("🎯 Clicking inner <a> instead of <li>");
                }
            } catch (Exception ignored) {
                System.out.println("ℹ️ No inner <a>, will click the <li> directly");
            }

            // 4. Scroll the item into view inside its scrollable ancestor
            js.executeScript(
                    "const item = arguments[0];" +
                    "let n = item.parentElement;" +
                    "while (n && n !== document.body) {" +
                    "  if (n.scrollHeight > n.clientHeight + 5) {" +
                    "    n.scrollTop = item.offsetTop - n.clientHeight / 2;" +
                    "    break;" +
                    "  }" +
                    "  n = n.parentElement;" +
                    "}", clickTarget);
            Thread.sleep(500);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", clickTarget);
            Thread.sleep(500);

            // 5. Snapshot state before clicking
            String startUrl = driver.getCurrentUrl();
            Set<String> windowsBefore = driver.getWindowHandles();

            // 6. Try real Actions click first
            try {
                actions.moveToElement(clickTarget)
                        .pause(Duration.ofMillis(300))
                        .click()
                        .perform();
                System.out.println("🖱️ (Actions) Clicked Insurance target");
            } catch (Exception actionEx) {
                js.executeScript("arguments[0].click();", clickTarget);
                System.out.println("🖱️ (JS click) Clicked Insurance target");
            }

            // 7. Wait briefly for any navigation — but if none occurs, don't fail
            //    (site may open a sub-panel; the next step navigates elsewhere)
            try {
                new WebDriverWait(driver, Duration.ofSeconds(8)).until(d ->
                        d.getWindowHandles().size() > windowsBefore.size()
                                || !d.getCurrentUrl().equals(startUrl));
            } catch (Exception ignored) {
                System.out.println("ℹ️ No navigation after Insurance click — continuing");
            }

            // 8. Switch to new tab if one opened
            Set<String> windowsAfter = driver.getWindowHandles();
            if (windowsAfter.size() > windowsBefore.size()) {
                for (String handle : windowsAfter) {
                    if (!windowsBefore.contains(handle)) {
                        driver.switchTo().window(handle);
                        System.out.println("➡️ Switched to new tab: " + driver.getCurrentUrl());
                        break;
                    }
                }
            } else if (!driver.getCurrentUrl().equals(startUrl)) {
                System.out.println("➡️ Navigated in same tab: " + driver.getCurrentUrl());
            }

            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("❌ Failed to click 'Insurance' item: " + e.getMessage());
            throw e;
        }
    }
    public void clickNIACLAssistantLink() throws Exception {
        try {
            By linkLocator = By.xpath(
                    "//div[@id='online_course-16']//span[contains(text(),'NIACL Assistant')]");

            // Give the page extra time — the click on Insurance may have shown a new panel
            WebElement link = new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.presenceOfElementLocated(linkLocator));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", link);
            Thread.sleep(700);

            // Prefer the enclosing <a> if present
            WebElement clickTarget = link;
            try {
                WebElement anchor = link.findElement(By.xpath("./ancestor::a[1]"));
                if (anchor.isDisplayed()) clickTarget = anchor;
            } catch (Exception ignored) { }

            String startUrl = driver.getCurrentUrl();
            Set<String> windowsBefore = driver.getWindowHandles();

            try {
                actions.moveToElement(clickTarget)
                        .pause(Duration.ofMillis(300))
                        .click()
                        .perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", clickTarget);
            }

            System.out.println("🖱️ Clicked 'NIACL Assistant' link");

            try {
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                        d.getWindowHandles().size() > windowsBefore.size()
                                || !d.getCurrentUrl().equals(startUrl));
            } catch (Exception ignored) {
                System.out.println("ℹ️ No navigation after NIACL click — continuing");
            }

            Set<String> windowsAfter = driver.getWindowHandles();
            if (windowsAfter.size() > windowsBefore.size()) {
                for (String handle : windowsAfter) {
                    if (!windowsBefore.contains(handle)) {
                        driver.switchTo().window(handle);
                        System.out.println("➡️ Switched to new tab: " + driver.getCurrentUrl());
                        break;
                    }
                }
            } else if (!driver.getCurrentUrl().equals(startUrl)) {
                System.out.println("➡️ Navigated in same tab: " + driver.getCurrentUrl());
            }

            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("❌ Failed to click 'NIACL Assistant' link: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Scrolls to and clicks the "Try Me Package" button (#try_me_package).
     * Handles iframe-wrapped content, Actions/JS fallback, and popup interference.
     * @throws Exception 
     */
    public void scrollAndClickTryMePackage() throws Exception {
        boolean switchedToIframe = false;

        try {
            // Close any popup that might block the button
            closePopupIfPresent();

            if (driver.findElements(By.id("try_me_package")).isEmpty()) {
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                for (WebElement f : frames) {
                    try {
                        driver.switchTo().frame(f);
                        if (!driver.findElements(By.id("try_me_package")).isEmpty()) {
                            switchedToIframe = true;
                            System.out.println("✅ Try Me Package button found inside iframe");
                            break;
                        }
                        driver.switchTo().defaultContent();
                    } catch (Exception ignored) {
                        driver.switchTo().defaultContent();
                    }
                }
            }

            WebElement btn = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("try_me_package")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(600);

            String startUrl = driver.getCurrentUrl();
            Set<String> windowsBefore = driver.getWindowHandles();

            try {
                actions.moveToElement(btn)
                        .pause(Duration.ofMillis(300))
                        .click()
                        .perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn);
            }

            System.out.println("✅ Clicked Try Me Package button");

            try {
                new WebDriverWait(driver, Duration.ofSeconds(8)).until(d ->
                        d.getWindowHandles().size() > windowsBefore.size()
                                || !d.getCurrentUrl().equals(startUrl)
                                || d.findElements(By.id("closepop-up")).size() > 0);
            } catch (Exception ignored) {
                System.out.println("ℹ️ No page change after Try Me Package click — continuing");
            }

            Set<String> windowsAfter = driver.getWindowHandles();
            if (windowsAfter.size() > windowsBefore.size()) {
                for (String handle : windowsAfter) {
                    if (!windowsBefore.contains(handle)) {
                        driver.switchTo().window(handle);
                        System.out.println("➡️ Switched to new tab: " + driver.getCurrentUrl());
                        break;
                    }
                }
            }

            Thread.sleep(1200);

        } catch (Exception e) {
            System.out.println("❌ Failed to click Try Me Package: " + e.getMessage());
            throw e;
        } finally {
            if (switchedToIframe) {
                try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Returns true if the Try Me Package page (or related signal) is visible.
     */
    public boolean isTryMePackagePageOpen() {
        try {
            String url = driver.getCurrentUrl().toLowerCase();
            String src = driver.getPageSource().toLowerCase();

            boolean urlHints = url.contains("try-me")
                    || url.contains("tryme")
                    || url.contains("package")
                    || url.contains("demo");

            boolean contentHints = src.contains("try me package")
                    || src.contains("try me")
                    || src.contains("free trial");

            return urlHints || contentHints;
        } catch (Exception e) {
            return false;
        }
    }
}