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

import net.openright.infrastructure.test.WebTestUtil;

public class ConferencerWebTest {

    private static ConferencerTestConfig config = ConferencerTestConfig.instance();
    private static ConferencerServer server = new ConferencerServer(config);
    private static WebDriver browser;
    private static WebDriverWait wait;

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
        browser.get(server.getURI().toString());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("nav")));
    }

    @Test
    public void shouldLoginWithGmail() throws Exception {
        browser.findElement(By.linkText("My page")).click();
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
    public void shouldAddEvent() throws Exception {

    }

}
