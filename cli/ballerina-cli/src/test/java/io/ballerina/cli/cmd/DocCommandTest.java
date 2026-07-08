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
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;

/**
 * Doc command tests.
 *
 * @since 2.0.0
 */
public class DocCommandTest extends BaseCommandTest {
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

    @Test(description = "Test doc command on a ballerina project.")
    public void testDocCommand() {
        Path projectPath = this.testResources.resolve("doc_project");
        System.setProperty("user.dir", projectPath.toString());
        Path destinationPath = projectPath.resolve("target")
                .resolve("apidocs").resolve("foo").resolve("winery").resolve("0.1.0");
        DocCommand docCommand = new DocCommand(this.printStream, this.printStream, false);
        docCommand.execute();

        Assert.assertTrue(Files.exists(destinationPath.resolve("api-docs.js")));
        Assert.assertTrue(Files.exists(destinationPath.resolve("api-docs.json")));

        /* Verify if all the UI components are present. */
        Assert.assertTrue(Files.exists(destinationPath.resolve("bundle.js")));
        Assert.assertTrue(Files.exists(destinationPath.resolve("favicon.ico")));
        Assert.assertTrue(Files.exists(destinationPath.resolve("globals.css")));
        Assert.assertTrue(Files.exists(destinationPath.resolve("index.html")));
        Assert.assertTrue(Files.exists(destinationPath.resolve("vercel.svg")));
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

    @Test(description = "Test doc command on a ballerina project with build tool execution.")
    public void testDocCommandWithBuildTool() throws IOException {
        Path projectPath = this.testResources.resolve("doc_project_with_build_tool");
        System.setProperty("user.dir", projectPath.toString());
        DocCommand docCommand = new DocCommand(this.printStream, this.printStream, false);
        docCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("doc-with-build-tool.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("doc_project_with_build_tool").resolve("target")
                .resolve("apidocs").resolve("foo").resolve("winery").resolve("0.1.0").resolve("index.html")));

        Files.delete(this.testResources.resolve("doc_project_with_build_tool").resolve("target")
                .resolve("apidocs").resolve("foo").resolve("winery").resolve("0.1.0").resolve("index.html"));
    }

    @Test (description = "Test doc command on a workspace project")
    public void testSimpleWorkspaceProject() throws IOException {
        Path projectPath = this.testResources.resolve("workspaces/wp-simple");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        cleanTarget(projectPath);
        DocCommand docCommand = new DocCommand(printStream, printStream, false);
        docCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(Files.exists(projectPath.resolve("hello-app/target/apidocs")
                .resolve("asmaj/hello_app/0.1.0").resolve("index.html")), buildLog);
        Files.delete(projectPath.resolve("hello-app/target/apidocs").resolve("asmaj/hello_app/0.1.0")
                .resolve("index.html"));

        Assert.assertTrue(Files.exists(projectPath.resolve("pkgA/target/apidocs")
                .resolve("asmaj/pkgA/0.1.0").resolve("index.html")), buildLog);
        Files.delete(projectPath.resolve("pkgA/target/apidocs").resolve("asmaj/pkgA/0.1.0")
                .resolve("index.html"));

        Assert.assertTrue(Files.exists(projectPath.resolve("pkgB/target/apidocs")
                .resolve("asmaj/pkgB/0.1.0").resolve("index.html")), buildLog);
        Files.delete(projectPath.resolve("pkgB/target/apidocs").resolve("asmaj/pkgB/0.1.0")
                .resolve("index.html"));
    }

    @Test (description = "Test doc command on a single project within a workspace")
    public void testGenerateForOneProjectInWorkspace() throws IOException {
        Path projectPath = this.testResources.resolve("workspaces/wp-simple/pkgA");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        cleanTarget(projectPath);
        DocCommand docCommand = new DocCommand(printStream, printStream, false);
        docCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(Files.exists(projectPath.resolve("target/apidocs")
                .resolve("asmaj/pkgA/0.1.0").resolve("index.html")), buildLog);
        Files.delete(projectPath.resolve("target/apidocs").resolve("asmaj/pkgA/0.1.0")
                .resolve("index.html"));
    }
}
