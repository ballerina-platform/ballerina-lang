/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.SettingsBuilder;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Pull command tests.
 *
 * @since 2.0.0
 */
public class PullCommandTest extends BaseCommandTest {

    private static final String TEST_PKG_NAME = "wso2/winery:1.2.3";

    @Test(description = "Pull package without package name")
    public void testPullWithoutPackage() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse();
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: no package given"));
        Assert.assertTrue(actual.contains("bal pull <package-name>"));
    }

    @Test(description = "Pull package with too many args")
    public void testPullWithTooManyArgs() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse(TEST_PKG_NAME, "tests");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal pull <package-name>"));
    }

    @Test(description = "Pull package with invalid package name")
    public void testPullInvalidPackage() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse("wso2/winery/1.0.0");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(
                actual.contains("ballerina: invalid package name. Provide the package name with the organization."));
        Assert.assertTrue(
                actual.contains("bal pull {<org-name>/<package-name> | <org-name>/<package-name>:<version>}"));
    }

    @Test(description = "Pull package with invalid org")
    public void testPullPackageWithInvalidOrg() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse("wso2-dev/winery");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(
                actual.contains("ballerina: invalid organization. Provide the package name with the organization."));
        Assert.assertTrue(
                actual.contains("bal pull {<org-name>/<package-name> | <org-name>/<package-name>:<version>}"));
    }

    @Test(description = "Pull package with invalid name")
    public void testPullPackageWithInvalidName() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse("wso2/winery$:1.0.0");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(
                actual.contains("ballerina: invalid package name. Provide the package name with the organization."));
        Assert.assertTrue(
                actual.contains("bal pull {<org-name>/<package-name> | <org-name>/<package-name>:<version>}"));
    }

    @Test(description = "Pull package with invalid version")
    public void testPullPackageWithInvalidVersion() throws IOException {
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse("wso2/winery:1.0.0.0");
        pullCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: invalid package version. Invalid version: '1.0.0.0'. "
                                                  + "Unexpected character 'DOT(.)' at position '5', "
                                                  + "expecting '[HYPHEN, PLUS, EOI]'"));
        Assert.assertTrue(
                actual.contains("bal pull {<org-name>/<package-name> | <org-name>/<package-name>:<version>}"));
    }

    @Test(description = "Test pull command with argument and a help")
    public void testPullCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse(args);
        pullCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-pull - Fetch packages from Ballerina Central"));
    }

    @Test(description = "Test pull command with help flag")
    public void testPullCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "-h" };
        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse(args);
        pullCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-pull - Fetch packages from Ballerina Central"));
    }

    @Test(description = "Pull a package from custom remote repository")
    public void testPullCustom() throws SettingsTomlException, IOException {

        Path customrRepoPath = Paths.get("src", "test", "resources", "test-resources", "custom-repo",
                "repositories", "repo-push-pull");
        Path settingsTomlPath = Paths.get("src", "test", "resources", "test-resources", "custom-repo",
                "Settings.toml");
        Path mockBallerinaHome = Paths.get("build").resolve("ballerina-home");

        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse("luheerathan/pact1:0.1.0", "--repository=repo-push-pull");
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            repoUtils.when(RepoUtils::readSettings).thenReturn(readMockSettings(settingsTomlPath,
                    customrRepoPath.toAbsolutePath().toString().replace("\\", "/")));
            pullCommand.execute();
        }
         Path pulledCacheDir = mockBallerinaHome.resolve("repositories").resolve("repo-push-pull")
                .resolve("bala").resolve("luheerathan").resolve("pact1").resolve("0.1.0");
        Assert.assertTrue(Files.exists(pulledCacheDir.resolve("any")));
    }

    @Test(description = "Pull a package from custom remote repository(not exist in Settings.toml)")
    public void testPullNonExistingCustom() throws SettingsTomlException, IOException {

        Path customrRepoPath = Paths.get("src", "test", "resources", "test-resources", "custom-repo",
                "repositories", "repo-push-pull");
        Path settingsTomlPath = Paths.get("src", "test", "resources", "test-resources", "custom-repo",
                "Settings.toml");
        Path mockBallerinaHome = Paths.get("build").resolve("ballerina-home");

        PullCommand pullCommand = new PullCommand(printStream, false);
        new CommandLine(pullCommand).parse("luheerathan/pact1:0.1.0", "--repository=repo-push-pul");
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            repoUtils.when(RepoUtils::readSettings).thenReturn(readMockSettings(settingsTomlPath,
                    customrRepoPath.toAbsolutePath().toString()));
            pullCommand.execute();
        }
        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: unsupported repository 'repo-push-pul' found. " +
                "Only repositories mentioned in the Settings.toml are supported.\n"));
    }

    private static Settings readMockSettings(Path settingsFilePath, String repoPath) throws SettingsTomlException {
        try {
            String settingString = Files.readString(settingsFilePath);
            settingString = settingString.replaceAll("REPO_PATH", repoPath);
            TomlDocument settingsTomlDocument = TomlDocument
                    .from(String.valueOf(settingsFilePath.getFileName()), settingString);
            SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
            return settingsBuilder.settings();
        } catch (IOException e) {
            return Settings.from();
        }
    }


}
