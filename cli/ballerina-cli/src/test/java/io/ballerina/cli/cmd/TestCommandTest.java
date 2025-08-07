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

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.readFileAsString;
import static io.ballerina.cli.cmd.CommandOutputUtils.replaceDependenciesTomlContent;
import static io.ballerina.cli.cmd.CommandUtil.USER_HOME;
import static io.ballerina.cli.utils.OsUtils.isWindows;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TEST_RUNTIME_MAIN_ARGS_FILE;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;

/**
 * Test command tests.
 *
 * @since 2.0.0
 */
public class TestCommandTest extends BaseCommandTest {
    private Path testResources;
    private Path testDistCacheDirectory;
    ProjectEnvironmentBuilder projectEnvironmentBuilder;

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("test-cmd-test-resources");
            Path testBuildDirectory = Path.of("build").toAbsolutePath();
            this.testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            Path customUserHome = Path.of("build/user-home");
            Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
            projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Path.of(testResourcesURI), new TestCommandTest.Copy(Path.of(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test a valid ballerina file", dataProvider = "optimizeDependencyCompilation")
    public void testTestBalFile(Boolean optimizeDependencyCompilation) {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file/sample_tests.bal");

        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        // set valid source root
        TestCommand testCommand = new TestCommand(validBalFilePath, false, optimizeDependencyCompilation);
        // name of the file as argument
        new CommandLine(testCommand).parseArgs(validBalFilePath.toString());
        testCommand.execute();
    }

    @Test(description = "Test a valid ballerina file with periods in the file name")
    public void testTestBalFileWithPeriods() {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file/sample.tests.bal");

        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        // set valid source root
        TestCommand testCommand = new TestCommand(validBalFilePath, false);
        // name of the file as argument
        new CommandLine(testCommand).parseArgs(validBalFilePath.toString());
        testCommand.execute();
    }

    @Test(description = "Test non .bal file")
    public void testNonBalFileTest() throws IOException {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file/hello_world.txt");
        TestCommand testCommand = new TestCommand(nonBalFilePath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs(nonBalFilePath.toString());
        testCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("Invalid Ballerina source file(.bal): " + nonBalFilePath));
    }

    @Test(description = "Test non existing bal file")
    public void testNonExistingBalFile() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-non-bal-file/xyz.bal");
        TestCommand testCommand = new TestCommand(validBalFilePath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs(validBalFilePath.toString());
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("The file does not exist: " + validBalFilePath));

    }

    @Test(description = "Test bal file containing syntax error")
    public void testBalFileWithSyntaxError() {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-file-with-syntax-error/sample_tests.bal");
        TestCommand testCommand = new TestCommand(balFilePath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs(balFilePath.toString());
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("compilation contains errors"));
        }
    }

    @Test(description = "Test a valid ballerina project")
    public void testBuildProjectWithTests() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("test-project.txt"));
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildMultiModuleProject() {
        Path projectPath = this.testResources.resolve("validMultiModuleProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
    }

    @Test(description = "Test a valid ballerina project from a different directory")
    public void testTestBalProjectFromADifferentDirectory() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        TestCommand buildCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs(projectPath.toString());
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("test-project.txt"));
    }

    @Test(description = "Test a project with a build tool execution")
    public void testTestProjectWithBuildTool() throws IOException {
        Path projectPath = this.testResources.resolve("proper-build-tool-with-tests");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("test-project-with-build-tool.txt"));
    }

    @Test(description = "Test the heap dump generation for a project with an OOM error")
    public void testHeapDumpGenerationForOOM() {
        Path projectPath = this.testResources.resolve("oom-project");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();

        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            File projectDir = new File(projectPath.toString());
            FileFilter fileFilter = WildcardFileFilter.builder().setWildcards("java_pid*.hprof").get();
            Assert.assertTrue(Objects.requireNonNull(projectDir.listFiles(fileFilter)).length > 0);
        }
    }

    @Test(description = "Check test command is preserving bin jar in target directory")
    public void testTestCommandPreservingBinJarInTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve("validMultiModuleProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        // build the project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false , false);
        new CommandLine(buildCommand).parseArgs();
        buildCommand.execute();
        Assert.assertTrue(projectPath.resolve("target/bin/winery.jar").toFile().exists());
        String md5BinJar = DigestUtils.md5Hex(Files.newInputStream(projectPath.resolve("target/bin/winery.jar")));

        // Run tests
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
        Assert.assertTrue(projectPath.resolve("target/bin/winery.jar").toFile().exists());
        Assert.assertEquals(md5BinJar, DigestUtils.md5Hex(
                Files.newInputStream(projectPath.resolve("target/bin/winery.jar"))));
        Assert.assertTrue(projectPath.resolve("target/cache/foo/winery/0.1.0/java21/foo-winery-0.1.0.jar")
                .toFile().exists());
        Assert.assertTrue(projectPath.resolve("target/cache/foo/winery/0.1.0/java21/foo-winery-0.1.0-testable.jar")
                .toFile().exists());
    }

    @Test(description = "Test a ballerina project with an invalid argument for --coverage-format",
            dataProvider = "optimizeDependencyCompilation")
    public void testUnsupportedCoverageFormat(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        TestCommand testCommand = new TestCommand(
                projectPath, printStream, printStream, false, false, true, "html",
                optimizeDependencyCompilation);

        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("unsupported coverage report format 'html' found. Only 'xml' format is " +
                "supported."));
    }

    @Test (description = "Test a ballerina project with a custom value for --target-dir")
    public void testCustomTargetDirWithTestCmd() {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        Path customTargetDir = projectPath.resolve("customTargetDir3");
        System.setProperty("user.dir", projectPath.toString());

        TestCommand testCommand = new TestCommand(
                projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();

        Assert.assertFalse(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache/tests_cache/test_suit.json")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("report")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("report/test_results.json")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("rerun_test.json")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache/tests_cache/test_suit.json")));
    }

    @Test(description = "Test a ballerina project with --test-report", dataProvider = "optimizeDependencyCompilation")
    public void testTestWithReport(Boolean optimizeDependencyCompilation) {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        TestCommand testCommand = new TestCommand(
                projectPath, printStream, printStream, false, true, false, null, optimizeDependencyCompilation);
        new CommandLine(testCommand).parseArgs();
        try (MockedStatic<TestUtils> testUtilsMockedStatic = Mockito.mockStatic(
                TestUtils.class, Mockito.CALLS_REAL_METHODS)) {
            testUtilsMockedStatic.when(TestUtils::getReportToolsPath)
                    .thenReturn(projectPath.resolve("resources/coverage/report.zip"));
            testCommand.execute();
        }
        Path reportDir = projectPath.resolve("target/report");

        Assert.assertTrue(Files.exists(reportDir));
        Assert.assertTrue(Files.exists(reportDir.resolve("favicon.ico")));
        Assert.assertTrue(Files.exists(reportDir.resolve("index.html")));
        Assert.assertTrue(Files.exists(reportDir.resolve("test_results.json")));
        Assert.assertTrue(Files.exists(reportDir.resolve("manifest.json")));
        Assert.assertTrue(Files.exists(reportDir.resolve("static/css/2.d5162072.chunk.css")));
        Assert.assertTrue(Files.exists(reportDir.resolve("static/css/main.15691da7.chunk.css")));
        Assert.assertTrue(Files.exists(reportDir.resolve("static/js/2.bc541f30.chunk.js")));
        Assert.assertTrue(Files.exists(reportDir.resolve("static/js/main.ea323a3b.chunk.js")));
    }

    @Test(description = "tests bal test command with sticky flag")
    public void testBalTestWithStickyFlag() throws IOException {
        if (isWindows()) {
            throw new SkipException("Currently failing on Windows");
        }
        // Cache package pkg_a 1.0.0
        Path balTestWithStickyFlagPath = testResources.resolve("balTestWithStickyFlag");
        BCompileUtil.compileAndCacheBala(balTestWithStickyFlagPath.resolve("pkg_a_100"),
                testDistCacheDirectory, projectEnvironmentBuilder);

        Path projectPath = balTestWithStickyFlagPath.resolve("project_b_100");
        System.setProperty("user.dir", projectPath.toString());

        // Run bal test command
        TestCommand firstTestCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(firstTestCommand).parseArgs();
        firstTestCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("bal-test-project.txt"));
        Assert.assertTrue(projectPath.resolve(DEPENDENCIES_TOML).toFile().exists());
        Assert.assertEquals(readFileAsString(projectPath.resolve(DEPENDENCIES_TOML)).trim(),
                readFileAsString(projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDeps.toml"))
                        .trim().replace("INSERT_VERSION_HERE", RepoUtils.getBallerinaShortVersion()));

        // remove build file
        Files.deleteIfExists(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));

        // Cache package pkg_a 2.0.0
        BCompileUtil.compileAndCacheBala(balTestWithStickyFlagPath.resolve("pkg_a_200"),
                testDistCacheDirectory, projectEnvironmentBuilder);

        // Run bal test command with sticky flag
        TestCommand secondTestCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(secondTestCommand).parseArgs("--sticky");
        secondTestCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertEquals(secondBuildLog.replace("\r", ""), getOutput("bal-test-project.txt"));
        Assert.assertTrue(projectPath.resolve(DEPENDENCIES_TOML).toFile().exists());
        Assert.assertEquals(readFileAsString(projectPath.resolve(DEPENDENCIES_TOML)).trim(),
                readFileAsString(projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDeps.toml"))
                        .trim().replace("INSERT_VERSION_HERE", RepoUtils.getBallerinaShortVersion()));
    }

    @Test(description = "Test a ballerina project with the flag dump-graph")
    public void testTestBalProjectWithDumpGraphFlag() throws IOException {
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        Path projectPath = dumpGraphResourcePath.resolve("package_a");
        System.setProperty("user.dir", projectPath.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--dump-graph");
        testCommand.execute();
        String buildLog = readOutput(true).replace("\r", "").strip();

        Assert.assertEquals(buildLog, getOutput("test-project-with-dump-graph.txt"));
        Assert.assertTrue(projectPath.resolve("target/cache/foo/package_a/0.1.0/java21/foo-package_a-0.1.0.jar")
                .toFile().exists());

        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Test a ballerina project with the flag dump-raw-graphs")
    public void testTestBalProjectWithDumpRawGraphsFlag() throws IOException {
        if (isWindows()) {
            throw new SkipException("Currently failing on Windows");
        }
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        Path projectPath = dumpGraphResourcePath.resolve("package_a");
        System.setProperty("user.dir", projectPath.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--dump-raw-graphs");
        testCommand.execute();
        String buildLog = readOutput(true).replace("\r", "").replace("\\", "/").strip();

        Assert.assertEquals(buildLog, getOutput("test-project-with-dump-raw-graphs.txt"));
        Assert.assertTrue(projectPath.resolve("target/cache/foo/package_a/0.1.0/java21/foo-package_a-0.1.0.jar")
                .toFile().exists());
        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Test an empty package")
    public void testTestEmptyPackage() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty("user.dir", projectPath.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-package.txt"));
        }
    }

    @Test(description = "Test an empty project with build tools")
    public void testTestEmptyProjectWithBuildTools() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithBuildTool");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**",
                RepoUtils.getBallerinaShortVersion());
        System.setProperty("user.dir", projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("test-empty-project-with-build-tools.txt"));
    }

    @Test(description = "Test --cloud=k8s flag with a project with tests")
    public void testTestWithCloudK8s() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        Path mockedLocalRepo = this.testResources.resolve("mocked-local-repo");
        System.setProperty(USER_HOME, mockedLocalRepo.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--cloud=k8s");
        testCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("test-project.txt"));

        Path targetDir = projectPath.resolve("target");
        Path testableJar = targetDir.resolve("bin/tests/winery-testable.jar");
        Assert.assertFalse(Files.exists(testableJar));
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);
        Assert.assertFalse(Files.exists(mainArgsFile));
    }

    @Test(description = "Test the emission of testable fat jar for a project with tests",
            dependsOnMethods = "testTestWithCloudK8s")
    public void testTestableFatJarEmission() {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());

        Path mockedLocalRepo = this.testResources.resolve("mocked-local-repo");
        System.setProperty(USER_HOME, mockedLocalRepo.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--cloud=docker");
        testCommand.execute();
        Path targetDir = projectPath.resolve("target");
        Path testableJar = targetDir.resolve("bin/tests/winery-testable.jar");
        Assert.assertTrue(Files.exists(testableJar));
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);
        Assert.assertTrue(Files.exists(mainArgsFile));
    }

    @Test(description = "Test the correct execution of the generated testable fat jar",
            dependsOnMethods = "testTestableFatJarEmission")
    public void testTestableFatJarExecution() throws IOException {
        if (isWindows()) {
            throw new SkipException("Currently failing on Windows");
        }
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        Path testableJar = projectPath.resolve("target/bin/tests/winery-testable.jar");
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);

        // Read the main args from the file (line separated)
        List<String> mainArgs = Files.readAllLines(mainArgsFile);
        mainArgs.set(TesterinaConstants.RunTimeArgs.IS_FAT_JAR_EXECUTION, "true");
        mainArgs.set(TesterinaConstants.RunTimeArgs.TEST_SUITE_JSON_PATH, TestUtils.getJsonFilePathInFatJar("/"));
        mainArgs.set(TesterinaConstants.RunTimeArgs.TARGET_DIR, projectPath.resolve("target").toString());

        List<String> pbArgs = new ArrayList<>(TestUtils.getInitialCmdArgs(null, null));
        pbArgs.add("-jar");
        pbArgs.add(testableJar.toString());
        pbArgs.addAll(mainArgs);

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs).redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        int exitCode = -1;
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                br.lines().forEach(line -> output.append(line).append("\n"));
            }
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertEquals(exitCode, 0);
        Assert.assertTrue(output.toString().contains("[pass] testRunMain"));
    }

    @Test(description = "Test the emission of testable fat jar for a project with mocks")
    public void testEmissionOfTestableFatJarForProjectWithMocking() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithMocks");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());

        Path mockedLocalRepo = this.testResources.resolve("mocked-local-repo");
        System.setProperty(USER_HOME, mockedLocalRepo.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--cloud=docker");
        testCommand.execute();
        Path targetDir = projectPath.resolve("target");
        Path testableJar = targetDir.resolve("bin/tests/projectWithMocks-testable.jar");
        Assert.assertTrue(Files.exists(testableJar));
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);
        Assert.assertTrue(Files.exists(mainArgsFile));
        //should exist only one testable jar
        try (Stream<Path> testableJars = Files.list(testableJar.getParent())) {
            Assert.assertEquals(testableJars.filter(path -> path.toString().endsWith(".jar"))
                                    .map(Path::toFile).toList().size(), 1);
        }
    }

    @Test(description = "Test the execution of testable fat jar for a project with tests and mocks",
            dependsOnMethods = "testEmissionOfTestableFatJarForProjectWithMocking")
    public void testExecutionOfTestableFatJarForProjectWithMocking() throws IOException {
        if (isWindows()) {
            throw new SkipException("Currently failing on Windows");
        }
        Path projectPath = this.testResources.resolve("projectWithMocks");
        Path testableJar = projectPath.resolve("target/bin/tests/projectWithMocks-testable.jar");
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);

        // Read the main args from the file (line separated)
        List<String> mainArgs = Files.readAllLines(mainArgsFile);
        mainArgs.set(TesterinaConstants.RunTimeArgs.IS_FAT_JAR_EXECUTION, "true");
        mainArgs.set(TesterinaConstants.RunTimeArgs.TEST_SUITE_JSON_PATH, TestUtils.getJsonFilePathInFatJar("/"));
        mainArgs.set(TesterinaConstants.RunTimeArgs.TARGET_DIR, projectPath.resolve("target").toString());

        List<String> pbArgs = new ArrayList<>(TestUtils.getInitialCmdArgs(null, null));
        pbArgs.add("-jar");
        pbArgs.add(testableJar.toString());
        pbArgs.addAll(mainArgs);

        ProcessBuilder processBuilder = new ProcessBuilder(pbArgs).redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        int exitCode = -1;
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                br.lines().forEach(line -> output.append(line).append("\n"));
            }
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertEquals(exitCode, 0);
        Assert.assertTrue(output.toString().contains("[pass] testMockedIntSub"));
        Assert.assertTrue(output.toString().contains("[pass] testMockedIntAdd"));
        Assert.assertTrue(output.toString().contains("[pass] testRealIntSub"));
    }

    @Test(description = "Test the emission of a single fat jar when both cloud and graalvm flags are enabled")
    public void testEmissionOfSingleFatJarForCloudAndGraalVM() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());

        Path mockedLocalRepo = this.testResources.resolve("mocked-local-repo");
        System.setProperty(USER_HOME, mockedLocalRepo.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--cloud=docker", "--graalvm");
        testCommand.execute();
        Path targetDir = projectPath.resolve("target");
        Path testableJar = targetDir.resolve("bin/tests/winery-testable.jar");
        Assert.assertTrue(Files.exists(testableJar));
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);
        Assert.assertTrue(Files.exists(mainArgsFile));
        //should exist only one testable jar
        try (Stream<Path> testableJars = Files.list(testableJar.getParent())) {
            Assert.assertEquals(testableJars.filter(path -> path.toString().endsWith(".jar"))
                                    .map(Path::toFile).toList().size(), 1);
        }
    }

    @Test(description = "Test the emission of multiple testable fat jars for a project with mocks when " +
            "both cloud and graalvm flags are enabled", priority = 1)
    public void testEmissionOfMultipleFatJarsForProjectWithMockingForCloudAndGraalVM() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithMocks");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());

        Path mockedLocalRepo = this.testResources.resolve("mocked-local-repo");
        System.setProperty(USER_HOME, mockedLocalRepo.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--cloud=docker", "--graalvm");
        testCommand.execute();
        Path targetDir = projectPath.resolve("target");
        Path mainArgsFile = targetDir.resolve("bin/tests").resolve(TEST_RUNTIME_MAIN_ARGS_FILE);
        Assert.assertTrue(Files.exists(mainArgsFile));
        //should exist only one testable jar
        try (Stream<Path> files = Files.list(mainArgsFile.getParent())) {
            List<File> testableJars = files.filter(path -> path.toString().endsWith(".jar"))
                    .map(Path::toFile).toList();
            Assert.assertEquals(testableJars.size(), 2);   //2 because default module and 1 sub module
            List<String> jarFileNames = Arrays.asList("projectWithMocks-testable.jar",
                    "projectWithMocks.mod1-testable.jar");
            for (File testableJar : testableJars) {
                Assert.assertTrue(jarFileNames.contains(testableJar.getName()));
            }
        }
    }

    @Test(description = "Test the execution of multiple testable fat jars for a project with tests and mocks",
            dependsOnMethods = "testEmissionOfMultipleFatJarsForProjectWithMockingForCloudAndGraalVM", priority = 1)
    public void testExecutionOfMultipleTestableFatJarsForProjectWithTestsAndMocks() throws IOException {
        if (isWindows()) {
            throw new SkipException("Currently failing on Windows");
        }
        Path projectPath = this.testResources.resolve("projectWithMocks");
        Path mainArgsFile = projectPath.resolve("target/bin/tests").resolve(TEST_RUNTIME_MAIN_ARGS_FILE);

        // Read the main args from the file (line separated)
        List<String> mainArgs = Files.readAllLines(mainArgsFile);
        mainArgs.set(TesterinaConstants.RunTimeArgs.IS_FAT_JAR_EXECUTION, "true");
        mainArgs.set(TesterinaConstants.RunTimeArgs.TEST_SUITE_JSON_PATH, TestUtils.getJsonFilePathInFatJar("/"));
        mainArgs.set(TesterinaConstants.RunTimeArgs.TARGET_DIR, projectPath.resolve("target").toString());

        try (Stream<Path> files = Files.list(mainArgsFile.getParent())) {
            List<Path> testableJars = files.filter(path -> path.toString().endsWith(".jar")).toList();
            for (Path testableJar : testableJars) {
                List<String> pbArgs = new ArrayList<>(TestUtils.getInitialCmdArgs(null, null));
                pbArgs.add("-jar");
                pbArgs.add(testableJar.toString());
                pbArgs.addAll(mainArgs);

                ProcessBuilder processBuilder = new ProcessBuilder(pbArgs).redirectErrorStream(true);
                Process process = processBuilder.start();
                StringBuilder output = new StringBuilder();
                int exitCode = -1;
                try {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        br.lines().forEach(line -> output.append(line).append("\n"));
                    }
                    exitCode = process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Assert.assertEquals(exitCode, 0);
                if (testableJar.getFileName().toString().equals("projectWithMocks-testable.jar")) {
                    Assert.assertTrue(output.toString().contains("[pass] testMockedIntAdd"));
                    Assert.assertTrue(output.toString().contains("[pass] testRealIntSub"));
                } else {
                    Assert.assertTrue(output.toString().contains("[pass] testMockedIntSub"));
                }
            }
        }
    }

    @Test(description = "Test the emission of testable fat jar for a single test bal file")
    public void testEmissionOfTestableFatJarForSingleTestBalFile() {
        Path projectPath = this.testResources.resolve("validProjectWithTests")
                .resolve("tests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());

        Path mockedLocalRepo = this.testResources.resolve("mocked-local-repo");
        System.setProperty(USER_HOME, mockedLocalRepo.toString());

        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parseArgs("--cloud=docker", "main_tests.bal");
        testCommand.execute();
        Path targetDir = projectPath.resolve("target");
        Path testableJar = targetDir.resolve("bin/tests/winery-testable.jar");
        Assert.assertFalse(Files.exists(testableJar));  //should not exist
        Path mainArgsFile = testableJar.getParent().resolve(TEST_RUNTIME_MAIN_ARGS_FILE);
        Assert.assertFalse(Files.exists(mainArgsFile)); //should not exist
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private final Path fromPath;
        private final Path toPath;
        private final StandardCopyOption copyOption;

        public Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {

            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }

    @Test(description = "Test Graalvm incompatible ballerina project")
    public void testGraalVMIncompatibleProject() throws IOException {
        Path projectPath = this.testResources.resolve("validGraalvmCompatibleProject");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(testCommand).parseArgs("--graalvm");
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("WARNING: Package is not compatible with GraalVM."));
        }
    }

    @Test(description = "Execute tests of a workspace")
    public void testWorkspaceProject() throws IOException {
        Path projectPath = this.testResources.resolve("workspaces/wp-with-tests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand);
        testCommand.execute();
        String output = readOutput();
        Assert.assertTrue(output.contains(getOutput("wp-with-tests.txt")), output);
    }

    @Test(description = "Execute tests of a specific package in the workspace")
    public void testSpecificPackageInTheWorkspace() throws IOException {
        Path projectRoot = this.testResources.resolve("workspaces/wp-with-tests");
        Path projectPath = projectRoot.resolve("hello-app");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand);
        testCommand.execute();
        String output = readOutput();
        Assert.assertTrue(output.contains(getOutput("wp-with-tests-hello-app.txt")), output);
    }

    @Test(description = "Execute tests of a specific package in the workspace that has no tests")
    public void testSpecificPackageWithNoTestsInTheWorkspace() throws IOException {
        Path projectRoot = this.testResources.resolve("workspaces/wp-with-tests");
        Path projectPath = projectRoot.resolve("bye");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand);
        testCommand.execute();
        String output = readOutput();
        Assert.assertTrue(output.contains(getOutput("wp-with-tests-bye.txt")), output);
    }
}
