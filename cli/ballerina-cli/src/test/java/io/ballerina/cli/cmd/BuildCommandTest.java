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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

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
import static io.ballerina.cli.utils.FileUtils.deleteDirectory;
import static io.ballerina.projects.util.ProjectConstants.USER_NAME;

/**
 * Build command tests.
 *
 * @since 2.0.0
 */
public class BuildCommandTest extends BaseCommandTest {
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
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        Files.copy(validBalFilePath, Files.createDirectory(
                        this.testResources.resolve("valid-bal-file-no-permission")).resolve("hello_world.bal"));
        Path validProjectPath = this.testResources.resolve("validProject");
        Files.copy(validProjectPath, this.testResources.resolve("validProject-no-permission"));
    }

    @Test(description = "Build a valid ballerina file")
    public void testBuildBalFile() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");

        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        // name of the file as argument
        new CommandLine(buildCommand).parse(validBalFilePath.toString());
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-hello-world-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar")));

        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar"));
    }

    @Test(description = "Build a valid ballerina file with output flag")
    public void testBuildBalFileWithOutputFlag() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");

        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true,
                "foo.jar");
        // name of the file as argument
        new CommandLine(buildCommand).parse("-o", "foo.jar", validBalFilePath.toString());
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-foo-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("foo.jar")));
        long executableSize = Files.size(this.testResources.resolve("valid-bal-file").resolve("foo.jar"));
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("foo.jar"));

        // only give the name of the file without extension
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true, "bar");
        new CommandLine(buildCommand).parse("-o", "bar", validBalFilePath.toString());
        buildCommand.execute();

        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-bar-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("bar.jar")));
        Assert.assertEquals(Files.size(this.testResources.resolve("valid-bal-file").resolve("bar.jar")),
                executableSize);
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("bar.jar"));

        // create executable in a different path
        Path helloExecutableTmpDir = Files.createTempDirectory("hello_executable-");
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true,
                helloExecutableTmpDir.toAbsolutePath().toString());
        new CommandLine(buildCommand).parse("-o", helloExecutableTmpDir.toAbsolutePath().toString(),
                validBalFilePath.toString());
        buildCommand.execute();

        buildLog = readOutput(true);
        String helloWorldJarLog = getOutput("build-bal-with-absolute-jar-path.txt")
                .replace("<ABSOLUTE_JAR_PATH>",
                         helloExecutableTmpDir.toAbsolutePath().resolve("hello_world.jar").toString());
        Assert.assertEquals(buildLog.replaceAll("\r", ""), helloWorldJarLog);

        Assert.assertTrue(Files.exists(helloExecutableTmpDir.toAbsolutePath().resolve("hello_world.jar")));

        // create executable in a different path with .jar extension
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true,
                helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString());
        new CommandLine(buildCommand).parse("-o",
                helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString(),
                validBalFilePath.toString());
        buildCommand.execute();

        buildLog = readOutput(true);
        String hippoJarLog = getOutput("build-bal-with-absolute-jar-path.txt")
                .replace("<ABSOLUTE_JAR_PATH>",
                         helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString());
        Assert.assertEquals(buildLog.replaceAll("\r", ""), hippoJarLog);

        Assert.assertTrue(Files.exists(helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar")));
        deleteDirectory(helloExecutableTmpDir);
    }

    @Test(description = "Build non .bal file")
    public void testNonBalFileBuild() throws IOException {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file").resolve("hello_world.txt");
        BuildCommand buildCommand = new BuildCommand(nonBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse(nonBalFilePath.toString());
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replaceAll("\r", "")
                .contains("Invalid Ballerina source file(.bal): " + nonBalFilePath.toString()));
    }

    @Test(description = "Build non existing bal file")
    public void testNonExistingBalFile() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("xyz.bal");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse(validBalFilePath.toString());
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replaceAll("\r", "")
                .contains("The file does not exist: " + validBalFilePath.toString()));
    }

    @Test(enabled = false, description = "Build bal file with no entry")
    public void testBuildBalFileWithNoEntry() {
        // valid source root path
        Path projectPath = this.testResources.resolve("valid-bal-file-with-no-entry").resolve("hello_world.bal");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse(projectPath.toString());
        try {
            buildCommand.execute();

        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("no entrypoint found in package"));
        }
    }

    @Test(description = "Build bal file containing syntax error")
    public void testBalFileWithSyntaxError() throws IOException {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-file-with-syntax-error").resolve("hello_world.bal");
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse(balFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-syntax-err-bal.txt"));
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("compilation contains errors"));
        }
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildBalProject() throws IOException {
        Path projectPath = this.testResources.resolve("validProject");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                            getOutput("build-bal-project.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with java imports")
    public void testBuildJava11BalProject() throws IOException {
        Path projectPath = this.testResources.resolve("validJava11Project");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "\nCompiling source\n" +
                "\tfoo/winery:0.1.0\n" +
                "\n" +
                "Running Tests\n\n" +
                "\twinery\n" +
                "\tNo tests found\n" +
                "\n" +
                "Creating balo\n" +
                "\ttarget/balo/foo-winery-java11-0.1.0.balo\n" +
                "\n" +
                "Generating executable\n" +
                "\ttarget/bin/winery.jar\n");

        Assert.assertTrue(projectPath.resolve("target").resolve("balo").resolve("foo-winery-java11-0.1.0.balo")
                                  .toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                                  .resolve("winery").resolve("0.1.0").resolve("java11")
                                  .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                                  .resolve("winery").resolve("0.1.0").resolve("bir")
                                  .resolve("winery.bir").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildBalProjectFromADifferentDirectory() throws IOException {
        Path projectPath = this.testResources.resolve("validProject");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse(projectPath.toString());
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-bal-project.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildProjectWithTests() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-bal-project-with-tests.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo")
                                  .toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project")
    public void testArtifactCreationWhenTestsFail() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithFailingTests");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        try {
            buildCommand.execute();
            Assert.fail("exception expected");
        } catch (BLauncherException e) {
            Assert.assertFalse(projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo")
                    .toFile().exists());
            Assert.assertFalse(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        }
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildMultiModuleProject() throws IOException {
        Path projectPath = this.testResources.resolve("validMultiModuleProject");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        String actualOutput1 = getOutput("build-multi-module-project-winery.txt");
        String actualOutput2 = getOutput("build-multi-module-project-winery-storage.txt");
        Assert.assertTrue(buildLog.replaceAll("\r", "").equals(actualOutput1)
                || buildLog.replaceAll("\r", "").equals(actualOutput2));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());

        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.storage.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.storage.bir").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testBuildProjectWithDefaultBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, null, null);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-project-default-build-options.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertFalse(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery-testable.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testBuildProjectOverrideBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        String expectedLog = getOutput("build-bal-project-override-build-options.txt")
                .replace("<TEST_RESULTS_JSON_PATH>",
                         projectPath.resolve("target").resolve("report").resolve("test_results.json").toString());
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expectedLog);

        Assert.assertTrue(projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo")
                                  .toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery-testable.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());

        Assert.assertTrue(
                projectPath.resolve("target").resolve("report").resolve("test_results.json").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testSingleFileWithBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        String expectedLog = getOutput("build-bal-project-override-build-options.txt")
                .replace("<TEST_RESULTS_JSON_PATH>",
                         projectPath.resolve("target").resolve("report").resolve("test_results.json").toString());
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expectedLog);

        Assert.assertTrue(projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo")
                                  .toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("winery-testable.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("bir")
                .resolve("winery.bir").toFile().exists());

        Assert.assertTrue(
                projectPath.resolve("target").resolve("report").resolve("test_results.json").toFile().exists());
    }

    @Test(description = "Build a ballerina file that has no write permission")
    public void testBuildBalFileNoWritePermission() {
        Path balFilePath = this.testResources.resolve("valid-bal-file-no-permission").resolve("hello_world.bal");

        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file-no-permission").toString());
        balFilePath.getParent().toFile().setWritable(false, false);
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false, true);
        // name of the file as argument
        new CommandLine(buildCommand).parse(balFilePath.toString());

        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("does not have write permissions"));
        }
    }

    @Test(description = "Build a ballerina project that has no write permission")
    public void testBuildPrjectWithNoWritePermission() {
        Path balFilePath = this.testResources.resolve("validProject-no-permission");

        System.setProperty("user.dir", this.testResources.resolve("validProject-no-permission").toString());
        balFilePath.getParent().toFile().setWritable(false, false);
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false, true);
        // name of the file as argument
        new CommandLine(buildCommand).parse(balFilePath.toString());

        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("does not have write permissions"));
        }
    }

    @Test(description = "Build a valid ballerina project with empty ballerina.toml")
    public void testBuildProjectWithEmptyBallerinaToml() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithEmptyBallerinaToml");

        System.setProperty("user.dir", projectPath.toString());
        System.setProperty(USER_NAME, "john");

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, null, null);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-project-with-empty-ballerina-toml.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("balo")
                                  .resolve("john-validProjectWithEmptyBallerinaToml-any-0.1.0.balo").toFile().exists());
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bin").resolve("validProjectWithEmptyBallerinaToml.jar").toFile()
                        .exists());
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
}
