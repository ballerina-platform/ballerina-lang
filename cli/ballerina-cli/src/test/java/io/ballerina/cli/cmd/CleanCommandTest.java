/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.USER_HOME;

/**
 * Clean command tests.
 *
 * @since 2.0.0
 */
public class CleanCommandTest extends BaseCommandTest {
    private Path testResources;

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Path.of(testResourcesURI), new BuildCommandTest.Copy(Path.of(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test clean command on a ballerina project.")
    public void testCleanCommandInProject() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        FileUtils.copyDirectory(projectPath.resolve("target-dir").toFile(),
                projectPath.resolve("target").toFile());
        FileUtils.copyDirectory(projectPath.resolve("generated-dir").toFile(),
                projectPath.resolve("generated").toFile());

        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("report").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("generated").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, false);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(projectPath.resolve("target")));
        Assert.assertFalse(Files.exists(projectPath.resolve("generated")));
    }

    @Test(description = "Test clean command on a ballerina project with custom target dir.")
    public void testCleanCommandInProjectWithCustomTarget() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Path customTargetDir = projectPath.resolve("customTargetDir4");
        FileUtils.copyDirectory(projectPath.resolve("target-dir").toFile(), customTargetDir.toFile());

        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("report").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, customTargetDir);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(customTargetDir));
    }

    @Test(description = "Test clean command on a ballerina project with custom target dir.")
    public void testCleanCommandNonExistentTargetAndGenerated() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Path customTargetDir = Path.of("customTargetDirNotExists");
        Assert.assertTrue(Files.notExists(customTargetDir));

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, customTargetDir);
        cleanCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("clean-non-existent-target.txt")
                .replace("%TARGET_LOCATION%", customTargetDir.toString()));
    }

    @Test(description = "Test clean command on a regular file as the custom target dir.")
    public void testCleanCommandOnRegularFile() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Path customTargetDir = projectPath.resolve("main.bal");
        Assert.assertTrue(Files.exists(customTargetDir));

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, customTargetDir);
        cleanCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("clean-regular-file.txt")
                .replace("%TARGET_LOCATION%", customTargetDir.toString()));
    }

    @Test
    public void testCleanInValidTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/bala")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/cache")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/report")));
        Assert.assertTrue(Files.exists(projectPath.resolve("generated-dir/gen.bal")));
        Assert.assertTrue(Files.notExists(projectPath.resolve("tests/main_test.bal")));
        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, projectPath);
        cleanCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("provided target directory '" + projectPath + "' is not a valid target directory."));
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/bala")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/cache")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/report")));
        Assert.assertTrue(Files.exists(projectPath.resolve("generated-dir/gen.bal")));
        Assert.assertTrue(Files.notExists(projectPath.resolve("tests/main_test.bal")));
    }

    @Test (enabled = false)
    public void testDependencyCacheDelete() throws IOException {
        Path projectsRoot = this.testResources.resolve("clean-cache-project");
        Path projectPath = projectsRoot.resolve("mainPackage");
        String userHomeDir = System.getProperty(USER_HOME);
        System.setProperty(USER_HOME, testDotBallerina.getParent().toString());

        BCompileUtil.compileAndCacheBala(projectsRoot.resolve("depFromDist").toString());
        BCompileUtil.compileAndCacheBala(projectsRoot.resolve("depFromCentral").toString(), testCentralRepoCache);

        String userDir = System.getProperty(USER_DIR_PROPERTY);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        // Execute the build command to cache the dependencies
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail("build command failed: " + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_DIR_PROPERTY, userDir); // reset user dir for other tests
        }

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, true, false);
        try {
            cleanCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail("clean command failed: " + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_HOME, userHomeDir); // reset user home for other tests
            System.setProperty(USER_DIR_PROPERTY, userDir); // reset user dir for other tests
        }
        String buildLog = readOutput();

        Path testDistRepoCache = Paths.get(userHomeDir).resolve(DIST_CACHE_DIRECTORY);
        Path depFromDistBalaPath = testDistRepoCache.resolve("bala/testorg/depFromDist/2.0.0/any");
        Path depFromDistCachePath = testDistRepoCache.resolve(
                "cache-" + RepoUtils.getBallerinaShortVersion() + "/testorg/depFromDist/2.0.0/");
        Path depFromCentralBalaPath = testCentralRepoCache.resolve("bala/testorg/depFromCentral/2.0.0/any");
        Path depFromCentralCachePath = testCentralRepoCache.resolve(
                "cache-" + RepoUtils.getBallerinaShortVersion() + "/testorg/depFromCentral/2.0.0/");

        Assert.assertTrue(Files.exists(depFromDistBalaPath), depFromDistBalaPath.toString());
        Assert.assertFalse(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromDistBalaPath), buildLog);
        Assert.assertTrue(Files.exists(depFromDistCachePath), depFromDistCachePath.toString());
        Assert.assertFalse(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromDistCachePath), buildLog);

        Assert.assertFalse(Files.exists(depFromCentralBalaPath), depFromCentralBalaPath.toString());
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromCentralBalaPath), buildLog);
        Assert.assertFalse(Files.exists(depFromCentralCachePath), depFromCentralCachePath.toString());
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromCentralCachePath), buildLog);
    }
}
