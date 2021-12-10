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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Clean command tests.
 *
 * @since 2.0.0
 */
public class CleanCommandTest extends BaseCommandTest {
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BuildCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test clean command on a ballerina project.")
    public void testCleanCommandInProject() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTarget");
        FileUtils.copyDirectory(projectPath.resolve("target-dir").toFile(), projectPath.resolve("target").toFile());

        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("report").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, false);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(projectPath.resolve("target")));
    }

    @Test(description = "Test clean command on a ballerina project with custom target dir.",
            dependsOnMethods = "testCleanCommandInProject")
    public void testCleanCommandInProjectWithCustomTarget() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTarget");
        Path customTargetDir = projectPath.resolve("customTargetDir4");
        FileUtils.copyDirectory(projectPath.resolve("target-dir").toFile(), customTargetDir.toFile());


        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("report").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, false, customTargetDir);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(customTargetDir));
    }

    @Test(description = "Test clean command on a ballerina project with custom target dir.")
    public void testCleanCommandNonExistentTarget() {
        Path projectPath = this.testResources.resolve("validProjectWithTarget");
        Path customTargetDir = Paths.get("customTargetDirNotExists");
        Assert.assertTrue(Files.notExists(customTargetDir));

        CleanCommand cleanCommand = new CleanCommand(projectPath, false, customTargetDir);
        try {
            cleanCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains(
                    "provided target directory '" + customTargetDir + "' does not exist."));
        }
    }

    @Test(description = "Test clean command on a regular file as the custom target dir.")
    public void testCleanCommandOnRegularFile() {
        Path projectPath = this.testResources.resolve("validProjectWithTarget");
        Path customTargetDir = projectPath.resolve("main.bal");
        Assert.assertTrue(Files.exists(customTargetDir));

        CleanCommand cleanCommand = new CleanCommand(projectPath, false, customTargetDir);
        try {
            cleanCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains(
                    "provided target path '" + customTargetDir + "' is not a directory."));
        }
    }
}
