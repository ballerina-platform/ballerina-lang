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
import java.util.Objects;

/**
 * Doc command tests.
 *
 * @since 2.0.0
 */
public class DocCommandTest extends BaseCommandTest {
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
    public void testDocCommand() throws IOException {
        Path projectPath = this.testResources.resolve("doc_project");
        System.setProperty("user.dir", projectPath.toString());
        DocCommand docCommand = new DocCommand(this.printStream, this.printStream, false);
        docCommand.execute();

        Assert.assertTrue(Files.exists(this.testResources.resolve("doc_project").resolve("target")
                .resolve("apidocs").resolve("foo").resolve("winery").resolve("0.1.0").resolve("index.html")));

        Files.delete(this.testResources.resolve("doc_project").resolve("target")
                .resolve("apidocs").resolve("foo").resolve("winery").resolve("0.1.0").resolve("index.html"));
    }

    @Test(description = "Test doc command on a ballerina project with custom target dir.")
    public void testDocCommandWithCustomTarget() throws IOException {
        Path projectPath = this.testResources.resolve("doc_project");
        Path customTargetPath = projectPath.resolve("custom");
        System.setProperty("user.dir", projectPath.toString());

        DocCommand docCommand = new DocCommand(this.printStream, this.printStream, false, customTargetPath);
        docCommand.execute();

        Assert.assertTrue(Files.exists(customTargetPath.resolve("apidocs").resolve("foo").resolve("winery")
                .resolve("0.1.0").resolve("index.html")));

        Files.delete(customTargetPath.resolve("apidocs").resolve("foo").resolve("winery").resolve("0.1.0")
                .resolve("index.html"));
    }
}
