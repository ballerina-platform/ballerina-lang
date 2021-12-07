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
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;

/**
 * Build command tests.
 *
 * @since 2.0.0
 */
public class TestCommandTest extends BaseCommandTest {
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("test-cmd-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new TestCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test a valid ballerina file")
    public void testTestBalFile() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file").resolve("sample_tests.bal");

        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        // set valid source root
        TestCommand testCommand = new TestCommand(validBalFilePath, false);
        // name of the file as argument
        new CommandLine(testCommand).parse(validBalFilePath.toString());
        testCommand.execute();
    }

    @Test(description = "Test a valid ballerina file with periods in the file name")
    public void testTestBalFileWithPeriods() {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file").resolve("sample.tests.bal");

        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        // set valid source root
        TestCommand testCommand = new TestCommand(validBalFilePath, false);
        // name of the file as argument
        new CommandLine(testCommand).parse(validBalFilePath.toString());
        testCommand.execute();
    }

    @Test(description = "Test non .bal file")
    public void testNonBalFileTest() throws IOException {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file").resolve("hello_world.txt");
        TestCommand testCommand = new TestCommand(nonBalFilePath, printStream, printStream, false);
        new CommandLine(testCommand).parse(nonBalFilePath.toString());
        testCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replaceAll("\r", "")
                .contains("Invalid Ballerina source file(.bal): " + nonBalFilePath.toString()));
    }

    @Test(description = "Test non existing bal file")
    public void testNonExistingBalFile() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-non-bal-file").resolve("xyz.bal");
        TestCommand testCommand = new TestCommand(validBalFilePath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(testCommand).parse(validBalFilePath.toString());
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replaceAll("\r", "")
                .contains("The file does not exist: " + validBalFilePath.toString()));

    }

    @Test(description = "Test bal file containing syntax error")
    public void testBalFileWithSyntaxError() {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-file-with-syntax-error").resolve("sample_tests.bal");
        TestCommand testCommand = new TestCommand(balFilePath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(testCommand).parse(balFilePath.toString());
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
        // non existing bal file
        new CommandLine(testCommand).parse();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("test-project.txt"));
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildMultiModuleProject() {
        Path projectPath = this.testResources.resolve("validMultiModuleProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(testCommand).parse();
        testCommand.execute();
    }

    @Test(description = "Test a valid ballerina project from a different directory")
    public void testTestBalProjectFromADifferentDirectory() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        TestCommand buildCommand = new TestCommand(projectPath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parse(projectPath.toString());
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("test-project.txt"));
    }

    @Test(description = "Test the heap dump generation for a project with an OOM error")
    public void testHeapDumpGenerationForOOM() {
        Path projectPath = this.testResources.resolve("oom-project");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parse();

        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            File projectDir = new File(projectPath.toString());
            FileFilter fileFilter = new WildcardFileFilter("java_pid*.hprof");
            Assert.assertTrue(Objects.requireNonNull(projectDir.listFiles(fileFilter)).length > 0);
        }
    }

    @Test(description = "Check test command is preserving bin jar in target directory")
    public void testTestCommandPreservingBinJarInTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve("validMultiModuleProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        // build the project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, true , false);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        String md5BinJar = DigestUtils.md5Hex(
                Files.newInputStream(projectPath.resolve("target").resolve("bin").resolve("winery.jar")));

        // Run tests
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false);
        new CommandLine(testCommand).parse();
        testCommand.execute();
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertEquals(md5BinJar, DigestUtils.md5Hex(
                Files.newInputStream(projectPath.resolve("target").resolve("bin").resolve("winery.jar"))));
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0-testable.jar").toFile().exists());
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;


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

    @Test
    public void testUnsupportedCoverageFormat() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        TestCommand testCommand = new TestCommand(
                projectPath, printStream, printStream, false, false, true, "html");

        new CommandLine(testCommand).parse();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("unsupported coverage report format 'html' found. Only 'xml' format is " +
                "supported."));
    }

    @Test ()
    public void testCustomTargetDirWithTestCmd() {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        Path customTargetDir = projectPath.resolve("customTargetDir3");
        System.setProperty("user.dir", projectPath.toString());

        TestCommand testCommand = new TestCommand(
                projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(testCommand).parse();
        testCommand.execute();

        Assert.assertFalse(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache").resolve("tests_cache").resolve("test_suit" +
                ".json")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("report")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("report").resolve("test_results.json")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("rerun_test.json")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache").resolve("tests_cache").resolve("test_suit" +
                ".json")));
    }

}
