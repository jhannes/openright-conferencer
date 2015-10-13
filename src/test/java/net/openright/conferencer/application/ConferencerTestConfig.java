package net.openright.conferencer.application;

import java.nio.file.Paths;

import javax.sql.DataSource;

import org.openqa.selenium.WebDriver;

import net.openright.conferencer.application.ConferencerConfigFile;

public class ConferencerTestConfig extends ConferencerConfigFile {

    private static ConferencerTestConfig instance;

    private ConferencerTestConfig() {
        super(Paths.get("conferencer-test.properties"));
    }

    @Override
    public DataSource createDataSource() {
        return createTestDataSource("conferencer");
    }

    public String getWebDriverName() {
        String webdriverClass = WebDriver.class.getName();
        return System.getProperty(webdriverClass, getProperty(webdriverClass, "org.openqa.selenium.chrome.ChromeDriver"));
    }

    public synchronized static ConferencerTestConfig instance() {
        if (instance == null) {
            instance = new ConferencerTestConfig();
            instance.start();
        }
        return instance;
    }
}
