/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Graph command tests.
 *
 * @since 2201.2.0
 */
public class GraphCommandTest extends BaseCommandTest {
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        this.testResources = super.tmpDir.resolve("build-test-resources");
        Path projectsWithDependencyConflicts = this.testResources.resolve("projectsWithDependencyConflicts")
                .resolve("package_p");
        try {
            copyTestResourcesToTmpDir();
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
            return;
        }
        compileAndCacheTestDependencies();
        replaceDependenciesTomlVersion(projectsWithDependencyConflicts);
    }

    @Test(description = "Print the dependency graph of a valid single ballerina file with no dependencies")
    public void testPrintGraphForBalFileWithNoDependencies() throws IOException {
        Path directoryPath = this.testResources.resolve("valid-bal-file");
        Path validBalFilePath = directoryPath.resolve("hello_world.bal");

        GraphCommand graphCommand = new GraphCommand(validBalFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(validBalFilePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-file-with-no-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a valid single ballerina file with dependencies")
    public void testPrintGraphForBalFileWithDependencies() throws IOException {
        Path balFilePath = this.testResources.resolve("valid-bal-file-with-dependencies").resolve("hello-world.bal");

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

        GraphCommand graphCommand = new GraphCommand(balProjectPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-no-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a ballerina project with build tools")
    public void testPrintGraphForBalProjectWithBuildTools() throws IOException {
        Path balProjectPath = this.testResources.resolve("proper-build-tool");

        GraphCommand graphCommand = new GraphCommand(balProjectPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-with-build-tool.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a valid ballerina project with dependencies")
    public void testPrintGraphForBalProjectWithDependencies() throws IOException {
        Path balProjectPath = this.testResources.resolve("projectsForDumpGraph").resolve("package_a");

        GraphCommand graphCommand = new GraphCommand(balProjectPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print all dependency graphs of a valid single ballerina file with dependencies")
    public void testPrintAllRawGraphsForBalFile() throws IOException {
        Path balFilePath = this.testResources.resolve("valid-bal-file-with-dependencies").resolve("hello-world.bal");

        GraphCommand graphCommand = new GraphCommand(balFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs("--dump-raw-graphs", balFilePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-file-with-dump-raw-graphs.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print all dependency graphs of a valid ballerina project with dependencies")
    public void testPrintAllRawGraphsForBalProject() throws IOException {
        Path balProjectPath = this.testResources.resolve("projectsForDumpGraph").resolve("package_a");

        GraphCommand graphCommand = new GraphCommand(balProjectPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs("--dump-raw-graphs", balProjectPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-dump-raw-graphs.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Call bal graph for a non ballerina file")
    public void testNonBalFileGraph() throws IOException {
        Path directoryPath = this.testResources.resolve("non-bal-file");
        Path nonBalFilePath = directoryPath.resolve("hello_world.txt");

        GraphCommand graphCommand = new GraphCommand(nonBalFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(nonBalFilePath.toString());
        graphCommand.execute();

        String buildLog = readFormattedOutput();
        String expectedLog = "ballerina: Invalid Ballerina source file(.bal): " + nonBalFilePath;
        Assert.assertEquals(buildLog, expectedLog);
    }

    @Test(description = "Cal bal graph for a non-existing ballerina file")
    public void testNonExistingBalFileGraph() throws IOException {
        Path nonExistingBalFilePath = this.testResources.resolve("valid-bal-file").resolve("xyz.bal");

        GraphCommand graphCommand = new GraphCommand(nonExistingBalFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(nonExistingBalFilePath.toString());
        graphCommand.execute();

        String buildLog = readFormattedOutput();
        String expectedLog = "ballerina: The file does not exist: " + nonExistingBalFilePath;
        Assert.assertEquals(buildLog, expectedLog);
    }

    @Test(description = "Call bal graph for a bal file with no entry")
    public void testBalFileWithNoEntryGraph() throws IOException {
        Path noEntryBalFilePath = this.testResources.resolve("valid-bal-file-with-no-entry")
                .resolve("hello_world.bal");

        GraphCommand graphCommand = new GraphCommand(noEntryBalFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(noEntryBalFilePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-file-with-no-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Call bal graph for an empty package")
    public void testEmptyBalProjectGraph() throws IOException {
        Path emptyPackagePath = this.testResources.resolve("emptyPackage");

        GraphCommand graphCommand = new GraphCommand(emptyPackagePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(emptyPackagePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = "ballerina: package is empty. Please add at least one .bal file.";
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a ballerina project with tests")
    public void testPrintGraphForBalProjectWithTests() throws IOException {
        Path balProjectWithTestsPath = this.testResources.resolve("validProjectWithTests");

        GraphCommand graphCommand = new GraphCommand(balProjectWithTestsPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectWithTestsPath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-tests.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a ballerina project without sticky flag")
    public void testPrintGraphForBalProjectWithoutSticky() throws IOException {
        Path directoryPath = this.testResources.resolve("projectsWithDependencyConflicts");
        Path balProjectWithSticky = directoryPath.resolve("package_p");

        GraphCommand graphCommand = new GraphCommand(balProjectWithSticky, printStream, printStream, false);

        // use the dependency package_o 1.0.2 patch instead of 1.0.0 specified in the Dependencies.toml
        // will not use 1.1.0 since it is a minor version update
        new CommandLine(graphCommand).parseArgs(balProjectWithSticky.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-without-sticky.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a ballerina project with sticky flag")
    public void testPrintGraphForBalProjectWithSticky() throws IOException {
        Path directoryPath = this.testResources.resolve("projectsWithDependencyConflicts");
        Path balProjectWithSticky = directoryPath.resolve("package_p");

        GraphCommand graphCommand = new GraphCommand(balProjectWithSticky, printStream, printStream, false);

        // stick to dependency package_o 1.0.0 specified in Dependencies.toml
        new CommandLine(graphCommand).parseArgs("--sticky", balProjectWithSticky.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-with-sticky.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a ballerina project with a minor version dependency conflict")
    public void testPrintGraphForBalProjectWithMinorVersionDependencyConflict() throws IOException {
        Path directoryPath = this.testResources.resolve("projectsWithDependencyConflicts");
        Path balProjectWithSticky = directoryPath.resolve("package_q");

        GraphCommand graphCommand = new GraphCommand(balProjectWithSticky, printStream, printStream, false);

        // Dependencies.toml specifies 1.0.0 while Ballerina.toml specifies 1.1.0 (minor version jump)
        // Should use 1.1.0
        new CommandLine(graphCommand).parseArgs(balProjectWithSticky.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-minor-conflict.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print the dependency graph of a ballerina project with a major version dependency conflict")
    public void testPrintGraphForBalProjectWithMajorVersionDependencyConflict() throws IOException {
        Path directoryPath = this.testResources.resolve("projectsWithDependencyConflicts");
        Path balProjectWithSticky = directoryPath.resolve("package_r");

        GraphCommand graphCommand = new GraphCommand(balProjectWithSticky, printStream, printStream, false);

        // Dependencies.toml specifies 1.0.0 while Ballerina.toml specifies 2.1.0 (major version jump)
        new CommandLine(graphCommand).parseArgs(balProjectWithSticky.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-major-conflict.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Print graph for a ballerina file that has no write permission")
    public void testPrintGraphForBalFileNoWritePermission() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        Path directory = this.testResources.resolve("valid-bal-file-no-permission");
        Files.copy(validBalFilePath, Files.createDirectory(directory).resolve("hello_world.bal"));
        Path noPermissionBalFilePath = directory.resolve("hello_world.bal");

        noPermissionBalFilePath.getParent().toFile().setWritable(false, false);

        GraphCommand graphCommand = new GraphCommand(noPermissionBalFilePath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(noPermissionBalFilePath.toString());
        graphCommand.execute();

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-file-with-no-dependencies.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Call bal graph when a direct dependency is missing")
    public void testBalGraphWithMissingDirectDependency() throws IOException {
        // foo/package_y --> foo/package_z (z is missing)
        Path balProjectWithMissingDep = this.testResources.resolve("projectsWithMissingDependencies")
                .resolve("package_y");

        GraphCommand graphCommand = new GraphCommand(balProjectWithMissingDep, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(balProjectWithMissingDep.toString());
        graphCommand.execute();

        // dependency graph without package_z should be received.
        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-missing-dir-dep.txt");
        Assert.assertEquals(actualLog, expectedLog);
    }

    @Test(description = "Call bal graph when a transitive dependency is missing")
    public void testBalGraphWithMissingTransitiveDependency() throws IOException {
        // foo:package_x -> foo:package_y --> foo:package_z (z is missing)
        Path directoryPath = this.testResources.resolve("projectsWithMissingDependencies");
        Path packageXPath = directoryPath.resolve("package_x");
        Path packageYPath = directoryPath.resolve("package_y");

        // prebuilt bala of package_y
        Path packageYBalaPath = packageYPath.resolve("bala").resolve("foo-package_y-any-0.1.0.bala");
        BCompileUtil.copyBalaToDistRepository(packageYBalaPath, "foo", "package_y", "0.1.0");

        GraphCommand graphCommand = new GraphCommand(packageXPath, printStream, printStream, false);
        new CommandLine(graphCommand).parseArgs(packageXPath.toString());

        try {
            graphCommand.execute();
        } catch (BLangCompilerException e) {
            Assert.assertEquals(e.getMessage(), "failed to load the module 'foo/package_y:0.1.0' from its BIR due " +
                    "to: cannot resolve module foo/package_z:0.1.0");
        }

        String actualLog = readFormattedOutput();
        String expectedLog = CommandOutputUtils.getOutput("graph-for-bal-project-missing-trans-dep.txt");
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
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c").toString());
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b").toString());

        Path dependencyConflictPath = this.testResources.resolve("projectsWithDependencyConflicts");
        BCompileUtil.compileAndCacheBala(dependencyConflictPath.resolve("package_o_1_0_0").toString());
        BCompileUtil.compileAndCacheBala(dependencyConflictPath.resolve("package_o_1_0_2").toString());
        BCompileUtil.compileAndCacheBala(dependencyConflictPath.resolve("package_o_1_1_0").toString());
        BCompileUtil.compileAndCacheBala(dependencyConflictPath.resolve("package_o_2_1_0").toString());
    }

    private String readFormattedOutput() throws IOException {
        return readOutput(true).replaceAll("\n\t\n", "\n\n").replaceAll("\r", "").strip();
    }

    private void replaceDependenciesTomlVersion(Path projectPath) throws IOException {
        String currentDistrVersion = RepoUtils.getBallerinaShortVersion();
        Path dependenciesTomlTemplatePath = projectPath.resolve("Dependencies-template.toml");
        Path dependenciesTomlPath = projectPath.resolve("Dependencies.toml");

        try (FileInputStream input = new FileInputStream(dependenciesTomlTemplatePath.toString());
            FileOutputStream output = new FileOutputStream(dependenciesTomlPath.toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("**INSERT_DISTRIBUTION_VERSION_HERE**", currentDistrVersion);
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
