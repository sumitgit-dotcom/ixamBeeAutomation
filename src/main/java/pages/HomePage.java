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

    // -------- New search-flow locators --------

    @FindBy(xpath = "//div[@class='mb-3 header_search_bar d-none d-lg-block']//input[@id='input-drop-down-body']")
    private WebElement headerSearchBar;

    @FindBy(id = "search-input")
    private WebElement resultsSearchInput;

    @FindBy(id = "chip-exam")
    private WebElement examChip;

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

    /**
     * Clicks the header search bar on the home page and waits for navigation
     * to the search-results screen. Uses Actions class per requirement.
     * @throws Exception 
     */
    public void clickHeaderSearchBar() throws Exception {
        try {
            WebElement searchBar = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@class='mb-3 header_search_bar d-none d-lg-block']//input[@id='input-drop-down-body']")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", searchBar);

            // Actions: move + click
            actions.moveToElement(searchBar)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();

            System.out.println("🔍 Clicked header search bar");

            // Some sites navigate when the user starts typing. Trigger it.
            searchBar.sendKeys("r");
            Thread.sleep(600);

            // If not navigated yet, press Enter
            if (!driver.getCurrentUrl().toLowerCase().contains("search")) {
                searchBar.sendKeys(Keys.ENTER);
            }

            // Wait for the search results screen (URL or search-input presence)
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

    /**
     * Types the given query into the search field on the results page.
     * Uses Actions (click to focus) + sendKeys (character-by-character for
     * React/Angular apps that filter on keyup).
     * @throws Exception 
     */
    public void typeInSearchField(String query) throws Exception {
        try {
            WebElement input = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("search-input")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", input);

            // Focus via Actions
            actions.moveToElement(input)
                    .pause(Duration.ofMillis(200))
                    .click()
                    .perform();

            // Clear any pre-existing text
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);

            // Type character-by-character so JS-driven filters fire
            for (char c : query.toCharArray()) {
                input.sendKeys(String.valueOf(c));
                Thread.sleep(80);
            }

            System.out.println("⌨️ Typed in search field: " + query);

            // Give results time to render
            Thread.sleep(2000);

        } catch (Exception e) {
            System.out.println("❌ Failed to type in search field: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Clicks the "Exam" chip on the search results page.
     * @throws Exception 
     */
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

    /**
     * Returns true if the search results page is loaded/visible.
     */
    public boolean isSearchResultsVisible() {
        try {
            boolean onSearchPage = driver.getCurrentUrl().toLowerCase().contains("search");

            boolean hasResults =
                    !driver.findElements(By.xpath(
                            "//*[contains(@class,'search-result') " +
                            "or contains(@class,'result-list') " +
                            "or contains(@class,'course-card')]")).isEmpty();

            // Fallback: the search input should still be present
            boolean hasSearchInput = !driver.findElements(By.id("search-input")).isEmpty();

            return onSearchPage || hasResults || hasSearchInput;

        } catch (Exception e) {
            return false;
        }
    }
}