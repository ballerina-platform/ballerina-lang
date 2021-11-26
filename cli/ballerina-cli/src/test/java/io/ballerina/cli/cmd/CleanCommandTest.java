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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Test(description = "Test doc command on a ballerina project.")
    public void testCleanCommandInProject() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTarget");
        Files.move(projectPath.resolve("target-dir"), projectPath.resolve("target"),
                StandardCopyOption.REPLACE_EXISTING);

        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("report").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, false);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(projectPath.resolve("target").resolve("bala")));
        Assert.assertFalse(Files.exists(projectPath.resolve("target").resolve("cache")));
        Assert.assertFalse(Files.exists(projectPath.resolve("target").resolve("report")));
    }

    @Test(description = "Test doc command on a ballerina project with custom target dir.")
    public void testCleanCommandInProjectWithCustomTarget() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTarget");
        Path customTargetDir = projectPath.resolve("customTargetDir4");
        Files.move(projectPath.resolve("target-dir"), customTargetDir,
                StandardCopyOption.REPLACE_EXISTING);

        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("report").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, false, customTargetDir);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(customTargetDir.resolve("bala")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("report")));
    }
}
