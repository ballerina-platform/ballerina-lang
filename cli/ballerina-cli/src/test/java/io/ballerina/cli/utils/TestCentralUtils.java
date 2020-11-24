package io.ballerina.cli.utils;

import org.ballerinalang.toml.model.Settings;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.utils.CentralUtils.readSettings;

/**
 * Test cases to test utilities.
 */
@PrepareForTest({ RepoUtils.class, System.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class TestCentralUtils extends PowerMockTestCase {

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/central-utils");

    @Test(description = "Test initialize proxy")
    public void testInitializeProxy() {
        org.ballerinalang.toml.model.Proxy proxy = new org.ballerinalang.toml.model.Proxy();
        proxy.setHost("http://localhost");
        proxy.setPort(8080);
        proxy.setUserName("pramodya");
        proxy.setPassword("Welcome@123");

        Proxy netProxy = CentralUtils.initializeProxy(proxy);
        Assert.assertNotNull(netProxy);
        Assert.assertEquals(netProxy.type().toString(), "HTTP");
        Assert.assertEquals(netProxy.address().toString(), "http://localhost:8080");
    }

    @Test(description = "Test get access token from Settings.toml")
    public void testGetAccessTokenOfCliFromSettings() {
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(UTILS_TEST_RESOURCES);
        Settings settings = readSettings();

        Assert.assertEquals(CentralUtils.getAccessTokenOfCLI(settings), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
    }

    @Test(description = "Test read settings")
    public void testReadSettings() {
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(UTILS_TEST_RESOURCES);

        Settings settings = readSettings();
        Assert.assertEquals(settings.getCentral().getAccessToken(), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
    }
}
