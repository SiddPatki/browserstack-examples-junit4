package com.browserstack.examples.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import com.browserstack.utils.LocalTesting;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


/**
 * Factory class that is responsible for parsing the capability configuration and creating {@link WebDriver} instances.
 */
public class WebDriverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverFactory.class);

    private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String WEBDRIVER_GECKO_DRIVER = "webdriver.gecko.driver";
    private static final String WEBDRIVER_IE_DRIVER = "webdriver.ie.driver";
    private static final String WEBDRIVER_EDGE_DRIVER = "webdriver.edge.driver";
    private static final String CAPABILITIES_FILE_PROP = "capabilities.config";
    private static final String DEFAULT_CAPABILITIES_FILE = "capabilities.yml";
    private static final String BROWSERSTACK_USERNAME = "BROWSERSTACK_USERNAME";
    private static final String BROWSERSTACK_ACCESS_KEY = "BROWSERSTACK_ACCESS_KEY";
    private static final String BUILD_ID = "BUILD_ID";
    private static final String DEFAULT_BUILD_NAME = "browserstack-examples-junit4";

    private static WebDriverFactory instance;
    private final WebDriverConfiguration webDriverConfiguration;
    private final String DEFAULTBUILDSUFFIX;


    public static WebDriverFactory getInstance() {
        if (instance == null) {
            synchronized (WebDriverFactory.class) {
                if (instance == null) {
                    instance = new WebDriverFactory();
                }
            }
        }
        return instance;
    }

    private WebDriverFactory() {
        this.DEFAULTBUILDSUFFIX = String.valueOf(System.currentTimeMillis());
        this.webDriverConfiguration = parseWebDriverConfig();
        List<Platform> platforms = webDriverConfiguration.getActivePlatforms();
        LOGGER.debug("Running tests on {} active platforms.", platforms.size());
    }

    private WebDriverConfiguration parseWebDriverConfig() {
        String capabilitiesConfigFile = System.getProperty(CAPABILITIES_FILE_PROP, DEFAULT_CAPABILITIES_FILE);
        LOGGER.debug("Using capabilities configuration from FILE :: {}", capabilitiesConfigFile);
        URL resourceURL = WebDriverFactory.class.getClassLoader().getResource(capabilitiesConfigFile);
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        WebDriverConfiguration webDriverConfiguration = null;
        try {
            webDriverConfiguration = objectMapper.readValue(resourceURL, WebDriverConfiguration.class);
        } catch (IOException ioe) {
            throw new Error("Unable to parse capabilities file " + capabilitiesConfigFile, ioe);
        }
        return webDriverConfiguration;
    }

    public WebDriver createWebDriverForPlatform(Platform platform, String testName) {
        WebDriver webDriver = null;
        switch (this.webDriverConfiguration.getDriverType()) {
            case localDriver:
                webDriver = createLocalWebDriver(platform);
                break;
            case remoteDriver:
                webDriver = createRemoteWebDriver(platform, testName);
                break;
            case onPremGridDriver:
                webDriver = createOnPremGridDriver(platform, testName);
                webDriver.manage().window().maximize();
                break;
        }
        return webDriver;
    }

    public String getTestEndpoint() {
        return this.webDriverConfiguration.getTestEndpoint();
    }

    /**
     * Instantiates Local Driver for different browser types
     */
    private WebDriver createLocalWebDriver(Platform platform) {
        WebDriver webDriver = null;
        switch (BrowserType.valueOf(platform.getName())) {
            case chrome:
                System.setProperty(WEBDRIVER_CHROME_DRIVER, Paths.get(platform.getDriverPath()).toString());
                ChromeOptions chromeOptions = new ChromeOptions();
                if (platform.getCapabilities() != null) {
                    platform.getCapabilities().getCapabilityMap().forEach(chromeOptions::setCapability);
                }
                webDriver = new ChromeDriver(chromeOptions);
                break;
            case firefox:
                System.setProperty(WEBDRIVER_GECKO_DRIVER, Paths.get(platform.getDriverPath()).toString());
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (platform.getCapabilities() != null) {
                    platform.getCapabilities().getCapabilityMap().forEach(firefoxOptions::setCapability);
                }
                webDriver = new FirefoxDriver(firefoxOptions);
                break;
            case ie:
                System.setProperty(WEBDRIVER_IE_DRIVER, Paths.get(platform.getDriverPath()).toString());
                InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions();
                if (platform.getCapabilities() != null) {
                    platform.getCapabilities().getCapabilityMap().forEach(internetExplorerOptions::setCapability);
                }
                webDriver = new InternetExplorerDriver(internetExplorerOptions);
                break;
            case edge:
                System.setProperty(WEBDRIVER_EDGE_DRIVER, Paths.get(platform.getDriverPath()).toString());
                EdgeOptions edgeOptions = new EdgeOptions();
                if (platform.getCapabilities() != null) {
                    platform.getCapabilities().getCapabilityMap().forEach(edgeOptions::setCapability);
                }
                webDriver = new EdgeDriver(edgeOptions);
                break;
            case safari:
                SafariOptions safariOptions = new SafariOptions();
                if (platform.getCapabilities() != null) {
                    platform.getCapabilities().getCapabilityMap().forEach(safariOptions::setCapability);
                }
                webDriver = new SafariDriver(safariOptions);
                break;
            case opera:
                OperaOptions operaOptions = new OperaOptions();
                if (platform.getCapabilities() != null) {
                    platform.getCapabilities().getCapabilityMap().forEach(operaOptions::setCapability);
                }
                webDriver = new OperaDriver(operaOptions);
                break;
        }
        return webDriver;
    }


    public WebDriver createOnPremGridDriver(Platform platform, String testName) {
        RemoteDriverConfig remoteDriverConfig = webDriverConfiguration.getRemoteDriverConfig();
        DesiredCapabilities platformCapabilities = new DesiredCapabilities();
        platformCapabilities.setBrowserName(platform.getBrowser());
        try {
            return new RemoteWebDriver(new URL(remoteDriverConfig.getHubUrl()), platformCapabilities);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid Hub URL");
        }
    return null;
    }


    public WebDriver createRemoteWebDriver(Platform platform, String testName) {
        RemoteDriverConfig remoteDriverConfig = webDriverConfiguration.getRemoteDriverConfig();
        CommonCapabilities commonCapabilities = remoteDriverConfig.getCommonCapabilities();
        DesiredCapabilities platformCapabilities = new DesiredCapabilities();
        LocalTunnelConfig localTunnelConfig = remoteDriverConfig.getLocalTunnel();
        if (localTunnelConfig.getEnable()) {
            platformCapabilities.setCapability("browserstack.local", localTunnelConfig.getEnable());
            LocalTesting.createInstance(remoteDriverConfig, platformCapabilities);
        }


        if (StringUtils.isNotEmpty(platform.getDevice())) {
            platformCapabilities.setCapability("device", platform.getDevice());
        }
        platformCapabilities.setCapability("browser", platform.getBrowser());
        platformCapabilities.setCapability("browser_version", platform.getBrowserVersion());
        platformCapabilities.setCapability("os", platform.getOs());
        platformCapabilities.setCapability("os_version", platform.getOsVersion());
        platformCapabilities.setCapability("name", testName);
        platformCapabilities.setCapability("project", commonCapabilities.getProject());
        platformCapabilities.setCapability("build", createBuildName(commonCapabilities.getBuildPrefix()));

        if (commonCapabilities.getCapabilities() != null) {
            commonCapabilities.getCapabilities().getCapabilityMap().forEach(platformCapabilities::setCapability);
        }

        if (platform.getCapabilities() != null) {
            platform.getCapabilities().getCapabilityMap().forEach(platformCapabilities::setCapability);
        }
        String user = remoteDriverConfig.getUser();
        if (StringUtils.isNoneEmpty(System.getenv(BROWSERSTACK_USERNAME))) {
            user = System.getenv(BROWSERSTACK_USERNAME);
        }
        String accessKey = remoteDriverConfig.getAccessKey();
        if (StringUtils.isNoneEmpty(System.getenv(BROWSERSTACK_ACCESS_KEY))) {
            accessKey = System.getenv(BROWSERSTACK_ACCESS_KEY);
        }
        platformCapabilities.setCapability("browserstack.user", user);
        platformCapabilities.setCapability("browserstack.key", accessKey);
        RemoteWebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL(remoteDriverConfig.getHubUrl()), platformCapabilities);
        } catch (MalformedURLException e) {
            LOGGER.error("Hub URL is invalid");
        }
        return driver;
    }


    public List<Platform> getPlatforms() {
        return this.webDriverConfiguration.getActivePlatforms();
    }

    private String createBuildName(String buildPrefix) {

        if(StringUtils.isNotEmpty(System.getenv("BROWSERSTACK_BUILD_NAME"))){
            return System.getenv("BROWSERSTACK_BUILD_NAME");
        }

        if (StringUtils.isEmpty(buildPrefix)) {
            buildPrefix = DEFAULT_BUILD_NAME;
        }
        String buildName = buildPrefix;

        String buildSuffix = this.DEFAULTBUILDSUFFIX;
        if (StringUtils.isNotEmpty(System.getenv(BUILD_ID))) {
            buildSuffix = System.getenv(BUILD_ID);
        }
        return buildName + "-" + buildSuffix;
    }



    public DriverType getDriverType() {
        return webDriverConfiguration.getDriverType();
    }

}
