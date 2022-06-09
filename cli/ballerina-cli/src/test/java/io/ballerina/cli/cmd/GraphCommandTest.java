/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

public class GraphCommandTest extends BaseCommandTest {
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        this.testResources = super.tmpDir.resolve("build-test-resources");
        try {
            copyTestResourcesToTmpDir();
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
            return;
        }
        compileAndCacheTestDependencies();
    }

    @Test(description = "Print the dependency graph of a valid single ballerina file with no dependencies")
    public void testPrintGraphForBalFileWithNoDependencies() throws IOException {
        Path directoryPath = this.testResources.resolve("valid-bal-file");
        Path validBalFilePath = directoryPath.resolve("hello-world.bal");

        // set the user root
        System.setProperty("user.dir", directoryPath.toString());

        GraphCommand graphCommand = new GraphCommand(validBalFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(validBalFilePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-file-with-no-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a valid single ballerina file with dependencies")
    public void testPrintGraphForBalFileWithDependencies() throws IOException {
        Path directoryPath = this.testResources.resolve("valid-bal-file-with-dependencies");
        Path balFilePath = directoryPath.resolve("hello-world.bal");

        // set the user root
        System.setProperty("user.dir", directoryPath.toString());

        GraphCommand graphCommand = new GraphCommand(balFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balFilePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-file-with-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a valid ballerina project with no dependencies")
    public void testPrintGraphForBalProjectWithNoDependencies() throws IOException {
        Path balProjectPath = this.testResources.resolve("validApplicationProject");

        // set the user root
        System.setProperty("user.dir", balProjectPath.toString());

        GraphCommand graphCommand = new GraphCommand(balProjectPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-no-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a valid ballerina project with dependencies")
    public void testPrintGraphForBalProjectWithDependencies() throws IOException {
        Path balProjectPath = this.testResources.resolve("projectsForDumpGraph").resolve("package_a");

        // set the user root
        System.setProperty("user.dir", balProjectPath.toString());

        GraphCommand graphCommand = new GraphCommand(balProjectPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    private void copyTestResourcesToTmpDir() throws URISyntaxException, IOException {
        URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                .toURI();
        Path originalTestResources = Paths.get(testResourcesURI);
        Files.walkFileTree(originalTestResources, new BuildCommandTest.Copy(originalTestResources,
                this.testResources));
    }

    private void compileAndCacheTestDependencies() {
        Path testBuildDirectory = Paths.get("build").toAbsolutePath();
        Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);

        Path customUserHome = Paths.get("build", "user-home");
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);
    }

    private String readFormattedOutput() throws IOException {
        String output = readOutput(true);
        return output.replaceAll("\r", "").strip();
    }
    // @Test(description = "Print the dependency graph of a valid single ballerina file")
    // @Test(description = "Print the dependency graph of a valid ballerina project")
    // valid bala file
    // @Test(description = "Print the dependency graph of a non ballerina file")
    // non existing bal file
    // @Test(description = "Print the dependency graph of a bal file with no entry")
    // @Test(description = "Print the dependency graph of a ballerina file with syntax error")
    // @Test(description = "Print the dependency graph of a ballerina project with syntax error")
    // @Test(description = "Print the dependency graph of a valid ballerina file")
    // @Test(description = "Print the dependency graph with code generation of a single ballerina file")
    // @Test(description = "Print the dependency graph with code generation of a ballerina project")
    // sticky flag
    // offline flag
    // dump-raw-graphs
    // jar conflicts?
    // java11bal project ?
    // get graph from a different dir ?
    // with tests
    // multimodule
    // no write permission??
    // empty package with compiler plugin?
    // empty package with tests only ?
    // java imports??
}