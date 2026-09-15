package io.ballerina.cli.utils;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.Settings;
import org.ballerinalang.central.client.CentralAPIClient;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.net.Proxy;
import java.nio.file.Path;

import static io.ballerina.cli.utils.CentralUtils.authenticate;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;

/**
 * Test cases to test utilities.
 */
public class TestCentralUtils {

    private static final Path UTILS_TEST_RESOURCES = Path.of("src/test/resources/test-resources/central-utils");

    @Test(description = "Test get access token from Settings.toml")
    public void testGetAccessTokenOfCliFromSettings() {
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(UTILS_TEST_RESOURCES);
            Settings settings = RepoUtils.readSettings();
            Assert.assertEquals(getAccessTokenOfCLI(settings), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
        }
    }

    @Test(description = "Test read settings")
    public void testReadSettings() {
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(UTILS_TEST_RESOURCES);
            Settings settings = RepoUtils.readSettings();
            Assert.assertEquals(settings.getCentral().getAccessToken(),
                    "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
        }
    }

    @Test(description = "Test authenticate fails when the access token is missing")
    public void testAuthenticateWithMissingAccessToken() {
        CentralAPIClient client = new CentralAPIClient("https://api.central.ballerina.io/2.0/registry",
                Proxy.NO_PROXY, "");
        Path settingsTomlFilePath = UTILS_TEST_RESOURCES.resolve("Settings.toml");
        BLauncherException exception = Assert.expectThrows(BLauncherException.class,
                () -> authenticate(settingsTomlFilePath, client));
        String message = exception.getMessages().get(0);
        Assert.assertTrue(message.contains("Ballerina Central access token is missing in " + settingsTomlFilePath),
                message);
        Assert.assertTrue(message.contains(CentralUtils.getBallerinaCentralCliTokenUrl()), message);
        Assert.assertTrue(message.contains("BALLERINA_CENTRAL_ACCESS_TOKEN"), message);
    }

    @Test(description = "Test authenticate passes when the access token is available")
    public void testAuthenticateWithAccessToken() {
        CentralAPIClient client = new CentralAPIClient("https://api.central.ballerina.io/2.0/registry",
                Proxy.NO_PROXY, "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
        authenticate(UTILS_TEST_RESOURCES.resolve("Settings.toml"), client);
    }
}
