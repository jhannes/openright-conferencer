package net.openright.conferencer.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.DatabaseEventRepository;
import net.openright.conferencer.domain.events.Event;
import net.openright.conferencer.domain.events.EventRepository;
import net.openright.infrastructure.test.SampleData;
import net.openright.infrastructure.test.WebTestUtil;

public class ConferencerWebTest {

    private static ConferencerTestConfig config = ConferencerTestConfig.instance();
    private static ConferencerTestServer server = new ConferencerTestServer(config);
    private static WebDriver browser;
    private static WebDriverWait wait;
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());

    @BeforeClass
    public static void startServer() throws Exception {
        server.start(10080);
    }

    @BeforeClass
    public static void startBrowser() throws IOException {
        browser = WebTestUtil.createDriver(config.getWebDriverName());
        wait = new WebDriverWait(browser, 4);
    }

    @AfterClass
    public static void stopClient() {
        browser.quit();
    }

    @Before
    public void goToFrontPage() {
        browser.manage().deleteAllCookies();
        browser.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
    }

    @Test
    public void shouldLoginWithGmail() throws Exception {
        browser.get(server.getURI().toString());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("nav")));

        click(By.linkText("My page"));
        browser.findElement(By.linkText("Login with gmail")).click();
        browser.findElement(By.id("Email")).sendKeys(config.getTestUser());
        browser.findElement(By.id("next")).click();
        browser.findElement(By.id("Passwd")).sendKeys(config.getTestUserPassword());
        browser.findElement(By.id("signIn")).click();
        browser.findElement(By.id("submit_approve_access")).click();

        assertThat(browser.findElement(By.id("username")).getText())
            .isEqualTo(config.getTestUser());
    }

    @Test
    public void shouldAddContributorToEvent() throws Exception {
        UserProfile creator = SampleData.sampleProfile();
        Event event = SampleData.sampleEvent();
        try (AutoCloseable ignore = creator.setAsCurrent()) {
            eventRepository.insert(event);
        }

        browser.get(server.getURI() + "/simulateLogin?username=" + creator.getEmail());
        click(By.linkText("update"));
        UserProfile collaborator = SampleData.sampleProfile();
        browser.findElement(By.name("event[collaborators][][email]")).sendKeys(collaborator.getEmail());
        browser.findElement(By.name("event[collaborators][][email]")).submit();

        browser.manage().deleteAllCookies();
        browser.get(server.getURI() + "/simulateLogin?username=" + collaborator.getEmail());
        assertThat(browser.findElements(By.cssSelector("#events .event a")))
            .extracting(e -> e.getText())
            .contains(event.getTitle());
    }

    @Test
    public void shouldAddEvent() throws Exception {
        browser.get(server.getURI() + "/simulateLogin?username=" + config.getTestUser());

        click(By.linkText("Create Event"));
        browser.findElement(By.name("event[title]"))
            .sendKeys("My test event");
        browser.findElement(By.name("event[title]"))
            .submit();

        assertThat(browser.findElements(By.cssSelector("#events .event a")))
            .extracting(e -> e.getText())
            .contains("My test event");
    }

    private void click(By by) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-overlay")));
        wait.until(ExpectedConditions.elementToBeClickable(by));
        browser.findElement(by).click();
    }

}
