package io.ballerina.cli.utils;

import io.ballerina.projects.Settings;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;

/**
 * Test cases to test utilities.
 */
@PrepareForTest({ RepoUtils.class, System.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class TestCentralUtils extends PowerMockTestCase {

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/central-utils");

    @Test(description = "Test get access token from Settings.toml")
    public void testGetAccessTokenOfCliFromSettings() throws SettingsTomlException {
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(UTILS_TEST_RESOURCES);
        Settings settings = RepoUtils.readSettings();

        Assert.assertEquals(getAccessTokenOfCLI(settings), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
    }

    @Test(description = "Test read settings")
    public void testReadSettings() throws SettingsTomlException {
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(UTILS_TEST_RESOURCES);

        Settings settings = RepoUtils.readSettings();
        Assert.assertEquals(settings.getCentral().getAccessToken(), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
    }
}
