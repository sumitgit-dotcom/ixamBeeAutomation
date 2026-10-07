package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

    @FindBy(xpath = "//h1[contains(text(),'Govt. Job Prep')]")
    private WebElement mainHeading;

    @FindBy(id = "online_course_dropdown")
    private WebElement onlineCourseDropdown;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    // ---------------- Navigation ----------------

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

    // ---------------- Hover ----------------

    public void hoverOnOnlineCourseDropdown() throws Exception {
        try {
            WebElement dropdown = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("online_course_dropdown")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", dropdown);

            // Native hover
            actions.moveToElement(dropdown).pause(Duration.ofMillis(800)).perform();
            System.out.println("🖱️ Hovered on Online Course dropdown via Actions");

            // Give React/Angular time to render
            Thread.sleep(2000);

            // Debug: how many visible anchors exist under the header/nav?
            Long visibleAnchorCount = (Long) ((JavascriptExecutor) driver).executeScript(
                "const t = document.getElementById('online_course_dropdown');" +
                "if (!t) return 0;" +
                "const scope = t.closest('header, nav, div[class*=\"header\"], div[class*=\"nav\"]') || document.body;" +
                "return Array.from(scope.querySelectorAll('a')).filter(a => a.offsetParent !== null).length;");
            System.out.println("🔎 Visible anchors in header block: " + visibleAnchorCount);

            // If native hover didn't reveal anything, force hover events via JS
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

    // ---------------- Dropdown item collection ----------------

    /**
     * Collects all unique text items from the Online Course dropdown,
     * scrolling the container (and page) until no new items appear.
     * Handles: <ul><li><a>, mega-menu <div>, and shadow-DOM roots.
     */
    public List<String> getOnlineCourseDropdownItems() {
        Set<String> uniqueTexts = new LinkedHashSet<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // 1) Best-effort: find the actual container <ul> or <div> that scrolls
            WebElement container = findDropdownContainer(js);

            if (container != null) {
                // Force visibility + make it scrollable in case CSS hides it until hover
                js.executeScript(
                    "const el = arguments[0];" +
                    "el.style.display='block';" +
                    "el.style.visibility='visible';" +
                    "el.style.opacity='1';" +
                    "if (!el.style.maxHeight) el.style.maxHeight='500px';" +
                    "el.style.overflowY='auto';",
                    container);
            }

            // 2) Collect initial batch
            collectItems(js, uniqueTexts);

            int previousCount = -1;
            int stableRounds = 0;
            int maxRounds = 40;

            for (int round = 0; round < maxRounds; round++) {

                // Scroll the container if we have one
                if (container != null) {
                    js.executeScript(
                        "const el = arguments[0];" +
                        "el.scrollTop = el.scrollTop + el.clientHeight;",
                        container);
                }

                // Also nudge the page (helps if dropdown is body-scrolled)
                js.executeScript("window.scrollBy(0, 80);");
                Thread.sleep(300);

                // Re-collect
                collectItems(js, uniqueTexts);

                // Stability check
                if (uniqueTexts.size() == previousCount) stableRounds++;
                else { stableRounds = 0; previousCount = uniqueTexts.size(); }

                // Bottom check (only meaningful if we have a container)
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

            // Final pass — scroll container back to top and collect once more
            if (container != null) {
                js.executeScript("arguments[0].scrollTop = 0;", container);
                Thread.sleep(250);
                collectItems(js, uniqueTexts);
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to fetch dropdown items: " + e.getMessage());
            e.printStackTrace();
        }

        // ---- Debug: if still empty, dump all visible anchors ----
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

            // And dump everything inside the toggle's ancestor block via JS
            System.out.println("❌ JS dump of anything after the toggle:");
            @SuppressWarnings("unchecked")
            List<String> jsDump = (List<String>) js.executeScript(
                "const t = document.getElementById('online_course_dropdown');" +
                "if (!t) return [];" +
                "const out = [];" +
                "let n = t.nextElementSibling;" +
                "while (n && out.length < 60) {" +
                "  if (n.offsetParent !== null) {" +
                "    const txt = (n.textContent || '').trim();" +
                "    if (txt && txt.length < 60) out.push(txt);" +
                "  }" +
                "  n = n.nextElementSibling;" +
                "}" +
                "return out;");
            if (jsDump != null) {
                for (String s : jsDump) System.out.println("   SIBLING: '" + s + "'");
            }
        }

        List<String> result = new ArrayList<>(uniqueTexts);
        System.out.println("📋 Total dropdown items found: " + result.size());
        return result;
    }

    /**
     * Locates the real scrollable dropdown container, walking up from the toggle
     * and picking the first ancestor whose subtree has the most visible links.
     * Returns null if we can't confidently identify one.
     */
    private WebElement findDropdownContainer(JavascriptExecutor js) {
        // Try candidate XPaths in priority order
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

        // JS-based walker: return the ancestor with the most descendant anchors
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

    /**
     * Collects visible anchor/item texts via JS.
     * - Scans the toggle's ancestor block first (targeted).
     * - Falls back to a full-page scan if nothing was found.
     * - Only adds texts shorter than 60 chars to avoid paragraphs.
     */
    private void collectItems(JavascriptExecutor js, Set<String> sink) {
        // Targeted scan: header/nav block containing the toggle
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

        // If nothing was collected from the scope, fall back to full-page anchors
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
}