package io.ballerina.cli.utils;

import io.ballerina.projects.Settings;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;

/**
 * Test cases to test utilities.
 */
public class TestCentralUtils {

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/central-utils");

    @Test(description = "Test get access token from Settings.toml")
    public void testGetAccessTokenOfCliFromSettings() {
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(UTILS_TEST_RESOURCES);
            Settings settings = RepoUtils.readSettings();
            Assert.assertEquals(getAccessTokenOfCLI(settings), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
        }
    }

    @Test(description = "Test read settings")
    public void testReadSettings() {
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(UTILS_TEST_RESOURCES);
            Settings settings = RepoUtils.readSettings();
            Assert.assertEquals(settings.getCentral().getAccessToken(),
                    "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
        }
    }
}
