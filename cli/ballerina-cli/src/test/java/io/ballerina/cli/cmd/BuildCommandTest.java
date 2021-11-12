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
import io.ballerina.projects.util.ProjectUtils;
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
        Path validProjectPath = this.testResources.resolve("validApplicationProject");
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
        ProjectUtils.deleteDirectory(helloExecutableTmpDir);
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
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                            getOutput("build-bal-project.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    /**
     * Test jar conflicts of platform libs.
     *
     * one-1.0.0.jar
     * .
     * ├── META-INF
     * │   ├── MANIFEST.MF
     * │   ├── maven
     * │   │   └── test-sample
     * │   │       └── one
     * │   │           ├── pom.properties
     * │   │           └── pom.xml
     * │   └── versions
     * │       └── 9
     * │           └── module-info.class
     * └── test
     *     └── sample
     *         ├── Sample2.class ---> conflicted class file
     *         ├── Sample3.class ---> conflicted class file
     *         └── Sample4.class ---> conflicted class file
     *
     * two-1.0.0.jar
     *
     * .
     * ├── META-INF
     * │   ├── MANIFEST.MF
     * │   ├── maven
     * │   │   └── test-sample
     * │   │       └── two
     * │   │           ├── pom.properties
     * │   │           └── pom.xml
     * │   └── versions
     * │       └── 9
     * │           └── module-info.class
     * └── test
     *     └── sample
     *         ├── Sample2.class ---> conflicted class file
     *         ├── Sample3.class ---> conflicted class file
     *         └── Sample4.class ---> conflicted class file
     */
    @Test(description = "Build a valid ballerina project")
    public void testBuildBalProjectWithJarConflicts() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithConflictedJars");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                            getOutput("build-bal-project-with-jar-conflicts.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("bin").resolve("conflictProject.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("pramodya")
                                  .resolve("conflictProject").resolve("0.1.7").resolve("java11")
                                  .resolve("pramodya-conflictProject-0.1.7.jar").toFile().exists());
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
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-bal-project.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                                  .resolve("winery").resolve("0.1.0").resolve("java11")
                                  .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project")
    public void testBuildBalProjectFromADifferentDirectory() throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse(projectPath.toString());
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-bal-project.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
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

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
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

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());

        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery.storage-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testBuildProjectWithDefaultBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-project-default-build-options.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
        Assert.assertFalse(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-testable-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testBuildProjectOverrideBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(
                projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        String expectedLog = getOutput("build-bal-project-override-build-options.txt")
                .replace("<TEST_RESULTS_JSON_PATH>",
                         projectPath.resolve("target").resolve("report").resolve("test_results.json").toString());
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expectedLog);

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());

        Assert.assertFalse(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0-testable.jar").toFile().exists());
        Assert.assertFalse(
                projectPath.resolve("target").resolve("report").resolve("test_results.json").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testSingleFileWithDefaultBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        BuildCommand buildCommand = new BuildCommand(
                projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-hello-world-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar")));

        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar"));
    }

    @Test(description = "Build a valid Standalone ballerina file with build options")
    public void testSingleFileOverrideBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        BuildCommand buildCommand = new BuildCommand(
                projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-hello-world-bal-with-build-options.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("hello_world.jar")));
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("hello_world.jar"));
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
    public void testBuildProjectWithNoWritePermission() {
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

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-project-with-empty-ballerina-toml.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("bin").resolve("validProjectWithEmptyBallerinaToml.jar").toFile()
                        .exists());
    }

    @Test(description = "Build an empty package with compiler plugin")
    public void testBuildEmptyProjectWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithCompilerPlugin");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-empty-project-with-compiler-plugin.txt"));
    }

    @Test(description = "Build an empty package with tests only")
    public void testBuildEmptyProjectWithTestsOnly() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithTestsOnly");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-empty-project-with-tests-only.txt"));
    }

    @Test(description = "Build an empty package with Non Default modules")
    public void testBuildEmptyProjectWithNonDefaultModules() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModules");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-empty-project-with-nondefault-modules.txt"));
    }

    @Test(description = "Build an empty package with Non Default modules with Tests only")
    public void testBuildEmptyProjectWithNonDefaultModulesTestOnly() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModulesTestOnly");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-empty-project-with-nondefault-modules-tests-only.txt"));
    }

    @Test(description = "Build an empty package with empty Non Default")
    public void testBuildEmptyNonDefaultModule() throws IOException {
        Path projectPath = this.testResources.resolve("emptyNonDefaultModule");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-empty-nondefault-module.txt"));
    }

    @Test(description = "Build a package with an invalid user name")
    public void testBuildWithInvalidOrgName() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithEmptyBallerinaToml");
        System.setProperty("user.dir", projectPath.toString());
        System.setProperty("user.name", "$org");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(buildLog.contains("_org/validProjectWithEmptyBallerinaToml:0.1.0"));
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("_org")
                                  .resolve("validProjectWithEmptyBallerinaToml").resolve("0.1.0").resolve("java11")
                                  .resolve("_org-validProjectWithEmptyBallerinaToml-0.1.0.jar").toFile().exists());
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
    public void testDumpBuildTimeForPackage() throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty("user.dir", projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("dump-build-time-package.txt"));

        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("build-time.json")));
        Assert.assertTrue(projectPath.resolve("target").resolve("build-time.json").toFile().length() > 0);
    }

    @Test
    public void testDumpBuildTimeForStandaloneFile() throws IOException {
        Path balFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");

        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, true);
        new CommandLine(buildCommand).parse(balFilePath.toString());
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("dump-build-time-standalone.txt"));
        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("build-time.json")));
        Assert.assertTrue(
                this.testResources.resolve("valid-bal-file").resolve("build-time.json").toFile().length() > 0);
    }

    @Test (dependsOnMethods = "testBuildBalProjectFromADifferentDirectory")
    public void testCustomTargetDir() {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        Path customTargetDir = projectPath.resolve("customTargetDir");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();

        Assert.assertTrue(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("report")));
    }

    @Test (dependsOnMethods = "testCustomTargetDir")
    public void testCustomTargetDirWithTests() {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        Path customTargetDir = projectPath.resolve("customTargetDir2");
        System.setProperty("user.dir", projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();

        Assert.assertTrue(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("report")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("rerun_test.json")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("cache").resolve("tests_cache").resolve("test_suit" +
                ".json")));
    }

}
