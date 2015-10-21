package net.openright.conferencer.application;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.openqa.selenium.WebDriver;

import lombok.SneakyThrows;
import net.openright.conferencer.application.ConferencerConfigFile;

public class ConferencerTestConfig extends ConferencerConfigFile {

    private static ConferencerTestConfig instance;

    private ConferencerTestConfig() {
        super(Paths.get("test-conferencer.properties"));
    }

    @Override
    public DataSource createDataSource() {
        return createTestDataSource("conferencer");
    }

    public String getWebDriverName() {
        String webdriverClass = WebDriver.class.getName();
        return System.getProperty(webdriverClass, getProperty(webdriverClass, "org.openqa.selenium.chrome.ChromeDriver"));
    }

    @SneakyThrows
    public synchronized static ConferencerTestConfig instance() {
        if (instance == null) {
            Files.createDirectories(Paths.get("logs"));
            instance = new ConferencerTestConfig();
        }
        return instance;
    }

    public String getTestUser() {
        return getRequiredProperty("conferencer.test.user.email");
    }

    public String getTestUserPassword() {
        return getRequiredProperty("conferencer.test.user.password");
    }
}
