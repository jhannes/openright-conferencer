package net.openright.conferencer.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.DatabaseEventRepository;
import net.openright.conferencer.domain.events.Event;
import net.openright.conferencer.domain.events.EventRepository;
import net.openright.conferencer.domain.talks.DatabaseTalkRepository;
import net.openright.conferencer.domain.talks.Talk;
import net.openright.conferencer.domain.talks.TalkComment;
import net.openright.conferencer.domain.talks.TalkRepository;
import net.openright.infrastructure.test.SampleData;
import net.openright.infrastructure.test.WebTestUtil;
import net.openright.infrastructure.test.WebTests;

@Category(WebTests.class)
public class TalksWebTest {

    private static ConferencerTestConfig config = ConferencerTestConfig.instance();
    private static ConferencerTestServer server = new ConferencerTestServer(config);
    private static WebDriver browser;
    private static WebDriverWait wait;
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());
    private TalkRepository talkRepository = new DatabaseTalkRepository(config.getDatabase());

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
    public void shouldAddTalk() throws Exception {
        UserProfile creator = SampleData.sampleProfile();
        Event event = SampleData.sampleEvent(creator);
        eventRepository.insert(event);

        browser.get(server.getURI() + "/simulateLogin?username=" + creator.getEmail());
        click(By.linkText(event.getTitle()));
        click(By.linkText("Add talk"));
        browser.findElement(By.name("talk[title]"))
            .sendKeys("A nice little talk");
        browser.findElement(By.name("talk[title]"))
            .submit();

        assertThat(browser.findElements(By.cssSelector("#event_talks .event_talk")))
            .extracting(e -> e.getText())
            .contains("A nice little talk");
    }

    @Test
    public void shouldEditTalk() throws Exception {
        UserProfile creator = SampleData.sampleProfile();
        Event event = SampleData.sampleEvent(creator);
        eventRepository.insert(event);
        Talk talk = SampleData.sampleTalk(event);
        talkRepository.insert(talk);

        browser.get(server.getURI() + "/simulateLogin?username=" + creator.getEmail());
        click(By.linkText(event.getTitle()));
        click(By.linkText(talk.getTitle()));
        click(By.linkText("Update talk"));

        String speakerEmail = SampleData.randomEmail();
        String speakerName = "John Doe";
        browser.findElement(By.name("talk[speaker][name]")).sendKeys(speakerName);
        browser.findElement(By.name("talk[speaker][email]")).sendKeys(speakerEmail);
        browser.findElement(By.name("talk[speaker][email]")).submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".notify.confirm")));

        assertThat(talkRepository.retrieve(talk.getId()).getSpeakerEmail())
            .isEqualTo(speakerEmail);
    }

    @Test
    public void shouldCommentOnTalk() throws Exception {
        UserProfile creator = SampleData.sampleProfile();
        UserProfile commentor = SampleData.sampleProfile();
        Event event = SampleData.sampleEvent(creator);
        event.addCollaborator(commentor.getEmail());
        eventRepository.insert(event);
        Talk talk = SampleData.sampleTalk(event);
        talkRepository.insert(talk);

        browser.get(server.getURI() + "/simulateLogin?username=" + commentor.getEmail());
        click(By.linkText(event.getTitle()));
        click(By.linkText(talk.getTitle()));
        click(By.linkText("Add comment"));

        browser.findElement(By.name("comment[title]")).sendKeys("Some input");
        browser.findElement(By.name("comment[content]")).sendKeys("Here is some more input");
        browser.findElement(By.name("comment[title]")).submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".notify.confirm")));

        List<TalkComment> comments = talkRepository.retrieve(talk.getId()).getComments();
        assertThat(comments).extracting(TalkComment::getAuthor)
            .contains(commentor.getEmail());
        assertThat(comments).extracting(TalkComment::getTitle)
            .contains("Some input");
    }

    private void click(By by) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-overlay")));
        wait.until(ExpectedConditions.elementToBeClickable(by));
        browser.findElement(by).click();
    }

}
