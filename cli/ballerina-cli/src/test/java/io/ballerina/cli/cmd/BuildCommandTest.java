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
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;

import static io.ballerina.cli.cmd.CommandOutputUtils.assertTomlFilesEquals;
import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.replaceDependenciesTomlContent;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;
import static io.ballerina.projects.util.ProjectConstants.USER_NAME;
import static io.ballerina.projects.util.ProjectUtils.deleteDirectory;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * Build command tests.
 *
 * @since 2.0.0
 */
public class    BuildCommandTest extends BaseCommandTest {
    private Path testResources;
    private static final Path testBuildDirectory = Path.of("build").toAbsolutePath();
    private static final Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
    Path customUserHome = Path.of("build", "user-home");
    Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
    ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

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
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        Files.copy(validBalFilePath, Files.createDirectory(
                this.testResources.resolve("valid-bal-file-no-permission")).resolve("hello_world.bal"));
        Path validProjectPath = this.testResources.resolve("validApplicationProject");
        Files.copy(validProjectPath, this.testResources.resolve("validProject-no-permission"));
    }

    @Test(description = "Build a valid ballerina file", dataProvider = "optimizeDependencyCompilation")
    public void testBuildBalFile(Boolean optimizeDependencyCompilation) throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");

        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("valid-bal-file").toString());
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false,
                optimizeDependencyCompilation);
        // name of the file as argument
        new CommandLine(buildCommand).parseArgs(validBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-hello-world-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar")));

        // copying the executable to a different location before deleting
        // to use for testCodeGeneratorForSingleFile test case
        Files.copy(this.testResources.resolve("valid-bal-file").resolve("hello_world.jar"),
                this.testResources.resolve("valid-bal-file").resolve("hello_world-for-codegen-test.jar"),
                StandardCopyOption.REPLACE_EXISTING);

        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar"));
    }

    @Test(description = "Build a valid ballerina file with output flag")
    public void testBuildBalFileWithOutputFlag() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");

        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("valid-bal-file").toString());
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, "foo.jar");
        // name of the file as argument
        new CommandLine(buildCommand).parseArgs("-o", "foo.jar", validBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-foo-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("foo.jar")));
        long executableSize = Files.size(this.testResources.resolve("valid-bal-file").resolve("foo.jar"));
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("foo.jar"));

        // only give the name of the file without extension
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, "bar");
        new CommandLine(buildCommand).parseArgs("-o", "bar", validBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-bar-bal.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("bar.jar")));
        Assert.assertEquals(Files.size(this.testResources.resolve("valid-bal-file").resolve("bar.jar")),
                executableSize);
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("bar.jar"));

        // create executable in a different path
        Path helloExecutableTmpDir = Files.createTempDirectory("hello_executable-");
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false,
                helloExecutableTmpDir.toAbsolutePath().toString());
        new CommandLine(buildCommand).parseArgs("-o", helloExecutableTmpDir.toAbsolutePath().toString(),
                validBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        buildLog = readOutput(true);
        String helloWorldJarLog = getOutput("build-bal-with-absolute-jar-path.txt")
                .replace("<ABSOLUTE_JAR_PATH>",
                        helloExecutableTmpDir.toAbsolutePath().resolve("hello_world.jar").toString());
        Assert.assertEquals(buildLog.replace("\r", ""), helloWorldJarLog);

        Assert.assertTrue(Files.exists(helloExecutableTmpDir.toAbsolutePath().resolve("hello_world.jar")));

        // create executable in a different path with .jar extension
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false,
                helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString());
        new CommandLine(buildCommand).parseArgs("-o",
                helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString(),
                validBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        buildLog = readOutput(true);
        String hippoJarLog = getOutput("build-bal-with-absolute-jar-path.txt")
                .replace("<ABSOLUTE_JAR_PATH>",
                        helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString());
        Assert.assertEquals(buildLog.replace("\r", ""), hippoJarLog);

        Assert.assertTrue(Files.exists(helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar")));
        ProjectUtils.deleteDirectory(helloExecutableTmpDir);
    }

    @Test(description = "Build non .bal file")
    public void testNonBalFileBuild() throws IOException {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file").resolve("hello_world.txt");
        BuildCommand buildCommand = new BuildCommand(nonBalFilePath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs(nonBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("invalid source provided: " + nonBalFilePath +
                        ". Please provide a valid Ballerina package, workspace or a standalone file."));
    }

    @Test(description = "Build non existing bal file")
    public void testNonExistingBalFile() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file").resolve("xyz.bal");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs(validBalFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("invalid source provided: " + validBalFilePath +
                        ". Please provide a valid Ballerina package, workspace or a standalone file."));
    }

    @Test(enabled = false, description = "Build bal file with no entry")
    public void testBuildBalFileWithNoEntry() {
        // valid source root path
        Path projectPath = this.testResources.resolve("valid-bal-file-with-no-entry").resolve("hello_world.bal");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs(projectPath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("no entrypoint found in package"));
        }
    }

    @Test(description = "Build bal file containing syntax error", dataProvider = "optimizeDependencyCompilation")
    public void testBalFileWithSyntaxError(Boolean optimizeDependencyCompilation) throws IOException {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-file-with-syntax-error").resolve("hello_world.bal");
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false,
                optimizeDependencyCompilation);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs(balFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-syntax-err-bal.txt"));
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("compilation contains errors"));
        }
    }

    @Test(description = "Build bal package containing syntax error", dataProvider = "optimizeDependencyCompilation")
    public void testBalProjectWithSyntaxError(Boolean optimizeDependencyCompilation) throws IOException {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-project-with-syntax-error");
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false,
                optimizeDependencyCompilation);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs(balFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-syntax-err-package.txt"));
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("compilation contains errors"));
        }
    }

    @Test(description = "Build a valid ballerina project", dataProvider = "optimizeDependencyCompilation")
    public void testBuildBalProject(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-bal-project.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(dependsOnMethods = "testBuildBalFile")
    public void testCodeGeneratorForSingleFile() throws IOException {
        Path execPath = this.testResources.resolve("valid-bal-file").resolve("hello_world-for-codegen-test.jar");
        String generatedSource = "dummyfunc-generated_1.class";
        try (JarFile execJar = new JarFile(execPath.toString())) {
            Assert.assertNull(execJar.getJarEntry(generatedSource));
        }
    }

    @Test(dependsOnMethods = "testBuildBalProject")
    public void testCodeGeneratorForBuildProject() throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        Path thinJarPath = projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar");
        Path execPath = projectPath.resolve("target").resolve("bin").resolve("winery.jar");
        String generatedSource = "foo/winery/0/dummyfunc-generated_1.class";
        try (JarFile thinJar = new JarFile(thinJarPath.toString())) {
            Assert.assertNotNull(thinJar.getJarEntry(generatedSource));
        }
        try (JarFile execJar = new JarFile(execPath.toString())) {
            Assert.assertNotNull(execJar.getJarEntry(generatedSource));
        }
    }

    /**
     * Test jar conflicts of platform libs.
     * <pre>
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
     * </pre>
     */
    @Test(description = "Build a ballerina project with conflicted jars")
    public void testBuildBalProjectWithJarConflicts() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithConflictedJars");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-bal-project-with-jar-conflicts.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("bin").resolve("conflictProject.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("pramodya")
                .resolve("conflictProject").resolve("0.1.7").resolve(JvmTarget.JAVA_21.code())
                .resolve("pramodya-conflictProject-0.1.7.jar").toFile().exists());
    }

    @Test(description = "Build a ballerina project with provided scope platform jars",
            dataProvider = "optimizeDependencyCompilation")
    public void testBuildProjectWithProvidedJars(Boolean optimizeDependencyCompilation) {
        Path projectPath = this.testResources.resolve("projectWithProvidedScope");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("error: compilation contains errors"));
        }
    }

    @Test(description = "Build a valid ballerina project with provided scope platform jars in dependencies")
    public void testBuildProjectWithProvidedWarning() throws IOException {
        BCompileUtil.compileAndCacheBala(testResources.resolve("projectWithProvidedDependency")
                .resolve("pkg_a"), testDistCacheDirectory, projectEnvironmentBuilder);
        Path projectPath = this.testResources.resolve("projectWithProvidedDependency").resolve("pkg_b");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("project-with-provided-warning.txt"));
    }

    @Test(description = "Build a valid ballerina project with java imports",
            dataProvider = "optimizeDependencyCompilation")
    public void testBuildJavaBalProject(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validJavaProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-bal-project.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(dataProvider = "optimizeDependencyCompilation")
    public void testBuildBalProjectFromADifferentDirectory(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs(projectPath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-bal-project.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(dataProvider = "optimizeDependencyCompilation")
    public void testBuildProjectWithTests(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-bal-project-with-tests.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(dataProvider = "optimizeDependencyCompilation")
    public void testBuildMultiModuleProject(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validMultiModuleProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        String actualOutput1 = getOutput("build-multi-module-project-winery.txt");
        String actualOutput2 = getOutput("build-multi-module-project-winery-storage.txt");
        Assert.assertTrue(buildLog.replace("\r", "").equals(actualOutput1)
                || buildLog.replace("\r", "").equals(actualOutput2));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());

        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery.storage-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testBuildProjectWithDefaultBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        cleanTarget(projectPath);
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-project-default-build-options.txt"));

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
        Assert.assertFalse(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-testable-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testBuildProjectOverrideBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithBuildOptions");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(
                projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        String expectedLog = getOutput("build-bal-project-override-build-options.txt")
                .replace("<TEST_RESULTS_JSON_PATH>",
                        projectPath.resolve("target").resolve("report").resolve("test_results.json").toString());
        Assert.assertEquals(buildLog.replace("\r", ""), expectedLog);

        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("winery.jar").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());

        Assert.assertFalse(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0-testable.jar").toFile().exists());
        Assert.assertFalse(
                projectPath.resolve("target").resolve("report").resolve("test_results.json").toFile().exists());
    }

    @Test(description = "Build a valid ballerina project with build options in toml")
    public void testSingleFileWithDefaultBuildOptions() throws IOException {
        Path projectPath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("valid-bal-file").toString());
        BuildCommand buildCommand = new BuildCommand(
                projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-hello-world-bal.txt"));

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
        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("valid-bal-file").toString());
        BuildCommand buildCommand = new BuildCommand(
                projectPath, printStream, printStream, false, Boolean.TRUE);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-hello-world-bal-with-build-options.txt"));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("hello_world.jar")));
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("hello_world.jar"));
    }

    @Test(description = "Build a ballerina file that has no write permission")
    public void testBuildBalFileNoWritePermission() {
        Path balFilePath = this.testResources.resolve("valid-bal-file-no-permission").resolve("hello_world.bal");

        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("valid-bal-file-no-permission").toString());
        balFilePath.getParent().toFile().setWritable(false, false);
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false);
        // name of the file as argument
        new CommandLine(buildCommand).parseArgs(balFilePath.toString());

        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("does not have write permissions"));
        }
    }

    @Test(description = "Build a ballerina project that has no write permission")
    public void testBuildProjectWithNoWritePermission() {
        Path balFilePath = this.testResources.resolve("validProject-no-permission");

        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("validProject-no-permission").toString());
        balFilePath.getParent().toFile().setWritable(false, false);
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false);
        // name of the file as argument
        new CommandLine(buildCommand).parseArgs(balFilePath.toString());

        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("does not have write permissions"));
        }
    }

    @Test(description = "Build a valid ballerina project with empty ballerina.toml")
    public void testBuildProjectWithEmptyBallerinaToml() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithEmptyBallerinaToml");

        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        System.setProperty(USER_NAME, "john");

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        cleanTarget(projectPath);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-project-with-empty-ballerina-toml.txt"));

        Assert.assertTrue(
                projectPath.resolve("target").resolve("bin").resolve("validProjectWithEmptyBallerinaToml.jar").toFile()
                        .exists());
    }

    @Test(description = "Build an empty package with compiler plugin")
    public void testBuildEmptyProjectWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithCompilerPlugin");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-project-with-compiler-plugin.txt"));
        }
    }

    @Test(description = "Build an empty package with tests only", dataProvider = "optimizeDependencyCompilation")
    public void testBuildEmptyProjectWithTestsOnly(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithTestsOnly");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-empty-project-with-tests-only.txt"));
    }

    @Test(description = "Build an empty package with Non Default modules")
    public void testBuildEmptyProjectWithNonDefaultModules() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModules");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-empty-project-with-nondefault-modules.txt"));
    }

    @Test(description = "Build an empty package with Non Default modules with Tests only")
    public void testBuildEmptyProjectWithNonDefaultModulesTestOnly() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModulesTestOnly");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-empty-project-with-nondefault-modules-tests-only.txt"));
    }

    @Test(description = "Build an empty package with empty Non Default")
    public void testBuildEmptyNonDefaultModule() throws IOException {
        Path projectPath = this.testResources.resolve("emptyNonDefaultModule");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-nondefault-module.txt"));
        }
    }

    @Test(description = "Build a package with an invalid user name")
    public void testBuildWithInvalidOrgName() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithEmptyBallerinaToml");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        System.setProperty("user.name", "$org");
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        cleanTarget(projectPath);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);

        Assert.assertTrue(buildLog.contains("_org/validProjectWithEmptyBallerinaToml:0.1.0"));
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("_org")
                .resolve("validProjectWithEmptyBallerinaToml").resolve("0.1.0")
                .resolve(JvmTarget.JAVA_21.code())
                .resolve("_org-validProjectWithEmptyBallerinaToml-0.1.0.jar").toFile().exists());
    }

    @Test(description = "tests consistent conflicted jars reporting")
    public void testConsistentConflictedJarsReporting() throws IOException {
        // Cache packages
        BCompileUtil.compileAndCacheBala(testResources.resolve("inconsistentConflictedJars")
                .resolve("uberJarPackage"), testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(testResources.resolve("inconsistentConflictedJars")
                .resolve("correctJarPackage"), testDistCacheDirectory, projectEnvironmentBuilder);

        Path projectPath = this.testResources.resolve("inconsistentConflictedJars").resolve("conflictedJarsProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        // Check build output which contains conflicted jars for 10 consecutive builds
        for (int i = 0; i < 10; i++) {
            cleanTarget(projectPath);
            BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, false);
            new CommandLine(buildCommand).parseArgs();
            buildCommand.execute();
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-conflicted-jars-project.txt"));
        }
    }

    @Test
    public void testDumpBuildTimeForPackage() throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false , true);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "").contains(getOutput("dump-build-time-package.txt")));
        validateBuildTimeInfo(buildLog.replace("\r", ""));

        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("build-time.json")));
        Assert.assertTrue(projectPath.resolve("target").resolve("build-time.json").toFile().length() > 0);
    }

    @Test
    public void testDumpBuildTimeForStandaloneFile() throws IOException {
        Path balFilePath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");

        System.setProperty(USER_DIR_PROPERTY, this.testResources.resolve("valid-bal-file").toString());
        BuildCommand buildCommand = new BuildCommand(balFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parseArgs(balFilePath.toString());
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "").contains(getOutput("dump-build-time-standalone.txt")));
        validateBuildTimeInfo(buildLog.replace("\r", ""));

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("build-time.json")));
        Assert.assertTrue(
                this.testResources.resolve("valid-bal-file").resolve("build-time.json").toFile().length() > 0);
    }

    @Test(dependsOnMethods = "testBuildBalProjectFromADifferentDirectory")
    public void testCustomTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        Path customTargetDir = projectPath.resolve("customTargetDir");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, customTargetDir);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        Assert.assertTrue(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("report")));
    }

    @Test(dependsOnMethods = "testCustomTargetDir")
    public void testCustomTargetDirWithTests() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        Path customTargetDir = projectPath.resolve("customTargetDir2");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, customTargetDir);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }

        Assert.assertTrue(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("report")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("rerun_test.json")));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("cache").resolve("tests_cache").resolve("test_suit" +
                ".json")));
    }

    @Test(description = "Build an empty package")
    public void testBuildEmptyPackage() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-package.txt"));
        }
    }

    @Test(description = "Build an empty package with compiler plugin")
    public void testBuildEmptyPackageWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackageWithCompilerPlugin");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-package.txt"));
        }
    }

    @Test(dataProvider = "optimizeDependencyCompilation")
    public void testBuildBalProjectWithDumpGraphFlag(Boolean optimizeDependencyCompilation) throws IOException {
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        Path projectPath = dumpGraphResourcePath.resolve("package_a");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs("--dump-graph");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true).replace("\r", "").strip();

        Assert.assertEquals(buildLog, getOutput("build-project-with-dump-graph.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("package_a").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-package_a-0.1.0.jar").toFile().exists());

        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(dataProvider = "optimizeDependencyCompilation")
    public void testBuildBalProjectWithDumpRawGraphsFlag(Boolean optimizeDependencyCompilation) throws IOException {
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        Path projectPath = dumpGraphResourcePath.resolve("package_a");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);
        new CommandLine(buildCommand).parseArgs("--dump-raw-graphs");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true).replace("\r", "").strip();

        Assert.assertEquals(buildLog, getOutput("build-project-with-dump-raw-graphs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("package_a").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-package_a-0.1.0.jar").toFile().exists());

        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a package with corrupted Dependencies.toml file")
    public void testBuildWithCorruptedDependenciesToml() throws IOException {
        Path projectPath = this.testResources.resolve("corrupted-dependecies-toml-file");
        cleanTarget(projectPath);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        Path sourcePath = projectPath.resolve("Dependencies-corrupt.toml");
        Path destinationPath = projectPath.resolve("Dependencies.toml");
        Files.copy(sourcePath, destinationPath);
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(
                buildLog.replaceAll("\r", ""),
                getOutput("corrupted-dependencies-toml.txt").replaceAll("\r", ""));
        String depContent = Files.readString(projectPath.resolve("Dependencies.toml"), Charset.defaultCharset())
                .replace("\r", "");
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        String corrcetDepContent = Files.readString(projectPath.resolve("Dependencies-corrected.toml"),
                        Charset.defaultCharset()).replace("\r", "")
                .replace("DIST_VERSION", ballerinaShortVersion);
        Assert.assertEquals(depContent, corrcetDepContent);
        Files.delete(destinationPath);
    }

    @Test(description = "Build a valid ballerina project with a custom maven repo",
            dataProvider = "optimizeDependencyCompilation")
    public void testBuildBalProjectWithCustomMavenRepo(Boolean optimizeDependencyCompilation) throws IOException {
        String username = System.getenv("publishUser");
        String password = System.getenv("publishPAT");

        if (username != null && password != null) {
            Path projectPath = this.testResources.resolve("validProjectWithCustomMavenRepo");
            String content = Files.readString(projectPath.resolve("Ballerina.toml"), Charset.defaultCharset())
                    .replace("{{username}}", username).replace("{{password}}", password);
            Files.writeString(projectPath.resolve("Ballerina.toml"), content, Charset.defaultCharset());
            BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                    optimizeDependencyCompilation);
            new CommandLine(buildCommand).parseArgs(projectPath.toString());
            buildCommand.execute();
            Assert.assertTrue(projectPath.resolve("target").resolve("platform-libs").resolve("org")
                    .resolve("ballerinalang").resolve("ballerina-command-distribution")
                    .resolve("0.8.14").resolve("ballerina-command-distribution-0.8.14.jar").toFile().exists());
        }
    }

    @BeforeGroups(value = {"proj-with-deps-update-policy"})
    public void publishDependencies() {
        Path distVersionTestResources = testResources.resolve("dep-dist-version-projects");
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depA_1_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depB_1_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depA_1_0_1"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depB_1_0_1"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depA_1_1_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depB_1_1_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depA_2_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(distVersionTestResources.resolve("depB_2_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
    }

    @Test(description = "Build a new ballerina project without sticky flag", groups = {"proj-with-deps-update-policy"},
            dataProvider = "optimizeDependencyCompilation")
    public void testBuildNewBalProjectWithoutStickyFlag(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("newPackage");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);

        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-new-proj-with-dep.txt"));

        // Dependencies.toml should have the latest versions of the dependencies
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("new-pkg.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Delete dependencies.toml from the project
        Files.delete(actualDependenciesToml);
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a new ballerina project with sticky flag", groups = {"proj-with-deps-update-policy"},
            dataProvider = "optimizeDependencyCompilation")
    public void testBuildNewBalProjectWithStickyFlag(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("newPackage");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);

        new CommandLine(buildCommand).parseArgs("--sticky");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-new-proj-with-dep.txt"));

        // Dependencies.toml should have the latest versions of the dependencies
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("new-pkg.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Delete dependencies.toml from the project
        Files.delete(actualDependenciesToml);
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a project already built with an older distribution without sticky flag",
            groups = {"proj-with-deps-update-policy"}, dataProvider = "optimizeDependencyCompilation")
    public void testBuildProjectPrecompiledWithOlderDistWithoutStickyFlag(Boolean optimizeDependencyCompilation)
            throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("preCompiledPackage");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**", "2201.5.0");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);

        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(
                buildLog.replace("\r", ""),
                getOutput("build-old-dist-precomp-proj-without-sticky.txt")
                        .replace("INSERT_NEW_DIST_VERSION_HERE", getNewVersionForOldDistWarning())
                        .replace("INSERT_OLD_DIST_VERSION_HERE", getOldVersionForOldDistWarning("2201.5.0")));

        // Dependencies.toml should be updated to the latest minor versions.
        // depA:1.0.0 -> depA:1.1.0
        // depB:1.0.0 -> depB:1.1.0
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("old-dist-precomp-pkg-with-no-sticky.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Revert the distribution version and dependency versions to the placeholder values
        replaceDependenciesTomlContent(projectPath, RepoUtils.getBallerinaShortVersion(),
                "**INSERT_DISTRIBUTION_VERSION_HERE**");
        replaceDependenciesTomlContent(projectPath, "1.1.0", "1.0.0");
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a project already built with an older distribution with sticky flag",
            groups = {"proj-with-deps-update-policy"}, dataProvider = "optimizeDependencyCompilation")
    public void testBuildProjectPrecompiledWithOlderDistWithStickyFlag(Boolean optimizeDependencyCompilation)
            throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("preCompiledPackage");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**", "2201.5.0");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);

        new CommandLine(buildCommand).parseArgs("--sticky");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(
                buildLog.replace("\r", ""),
                getOutput("build-old-dist-precomp-proj-with-sticky.txt")
                        .replace("INSERT_NEW_DIST_VERSION_HERE", getNewVersionForOldDistWarning())
                        .replace("INSERT_OLD_DIST_VERSION_HERE", getOldVersionForOldDistWarning("2201.5.0")));

        // Dependencies should stick to the ones already in Dependencies.toml.
        // depA:1.0.0 -> depA:1.0.0
        // depB:1.0.0 -> depB:1.0.0
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("precomp-pkg-with-sticky.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Revert the distribution version and dependency versions to the placeholder values
        replaceDependenciesTomlContent(projectPath, RepoUtils.getBallerinaShortVersion(),
                "**INSERT_DISTRIBUTION_VERSION_HERE**");
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a project already built with an U4 or older distribution without sticky flag",
            groups = {"proj-with-deps-update-policy"}, dataProvider = "optimizeDependencyCompilation")
    public void testBuildProjectPrecompiledWithNoDistWithoutStickyFlag(Boolean optimizeDependencyCompilation)
            throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("preCompiledPackage");
        replaceDependenciesTomlContent(
                projectPath, "distribution-version = \"**INSERT_DISTRIBUTION_VERSION_HERE**\"", "");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false,
                optimizeDependencyCompilation);

        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(
                buildLog.replace("\r", ""),
                getOutput("build-old-dist-precomp-proj-without-sticky.txt")
                        .replace("INSERT_NEW_DIST_VERSION_HERE", getNewVersionForOldDistWarning())
                        .replace("INSERT_OLD_DIST_VERSION_HERE", "4 or an older Update"));

        // Dependencies.toml should be updated to the latest minor versions.
        // depA:1.0.0 -> depA:1.1.0
        // depB:1.0.0 -> depB:1.1.0
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("old-dist-precomp-pkg-with-no-sticky.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Revert the distribution version and dependency versions to the placeholder values
        replaceDependenciesTomlContent(projectPath, RepoUtils.getBallerinaShortVersion(),
                "**INSERT_DISTRIBUTION_VERSION_HERE**");
        replaceDependenciesTomlContent(projectPath, "1.1.0", "1.0.0");
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a project already built with U4 or older distribution with sticky flag",
            groups = {"proj-with-deps-update-policy"})
    public void testBuildProjectPrecompiledWithNoDistWithStickyFlag() throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("preCompiledPackage");
        replaceDependenciesTomlContent(
                projectPath, "distribution-version = \"**INSERT_DISTRIBUTION_VERSION_HERE**\"", "");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);

        new CommandLine(buildCommand).parseArgs("--sticky");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(
                buildLog.replace("\r", ""),
                getOutput("build-old-dist-precomp-proj-with-sticky.txt")
                        .replace("INSERT_NEW_DIST_VERSION_HERE", getNewVersionForOldDistWarning())
                        .replace("INSERT_OLD_DIST_VERSION_HERE", "4 or an older Update"));

        // Dependencies should stick to the ones already in Dependencies.toml.
        // depA:1.0.0 -> depA:1.0.0
        // depB:1.0.0 -> depB:1.0.0
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("precomp-pkg-with-sticky.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Revert the distribution version and dependency versions to the placeholder values
        replaceDependenciesTomlContent(projectPath, RepoUtils.getBallerinaShortVersion(),
                "**INSERT_DISTRIBUTION_VERSION_HERE**");
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a project already built with the current distribution without sticky flag",
            groups = {"proj-with-deps-update-policy"})
    public void testBuildProjectPrecompiledWithCurrentDistWithoutStickyFlag() throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("preCompiledPackage");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**",
                RepoUtils.getBallerinaShortVersion());
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);

        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-curr-dist-precomp-proj-with-dep.txt"));

        // Dependencies.toml should be updated to the latest patch versions.
        // depA:1.0.0 -> depA:1.0.1
        // depB:1.0.0 -> depB:1.0.1
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("curr-dist-precomp-pkg-with-no-sticky.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Revert the distribution version and dependency versions to the placeholder values
        replaceDependenciesTomlContent(projectPath, RepoUtils.getBallerinaShortVersion(),
                "**INSERT_DISTRIBUTION_VERSION_HERE**");
        replaceDependenciesTomlContent(projectPath, "1.0.1", "1.0.0");
        deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Build a project already built with an older distribution with sticky flag",
            groups = {"proj-with-deps-update-policy"})
    public void testBuildProjectPrecompiledWithCurrentDistWithStickyFlag() throws IOException {
        Path projectPath = testResources.resolve("dep-dist-version-projects").resolve("preCompiledPackage");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**",
                RepoUtils.getBallerinaShortVersion());
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);

        new CommandLine(buildCommand).parseArgs("--sticky");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("build-curr-dist-precomp-proj-with-dep.txt"));

        // Dependencies should stick to the ones already in Dependencies.toml.
        // depA:1.0.0 -> depA:1.0.0
        // depB:1.0.0 -> depB:1.0.0
        Path actualDependenciesToml = projectPath.resolve("Dependencies.toml");
        Path expectedDependenciesToml = testResources.resolve("dep-dist-version-projects").resolve("expected-dep-tomls")
                .resolve("precomp-pkg-with-sticky.toml");
        Assert.assertTrue(actualDependenciesToml.toFile().exists());
        assertTomlFilesEquals(actualDependenciesToml, expectedDependenciesToml);

        // Revert the distribution version and dependency versions to the placeholder values
        replaceDependenciesTomlContent(projectPath, RepoUtils.getBallerinaShortVersion(),
                "**INSERT_DISTRIBUTION_VERSION_HERE**");
        deleteDirectory(projectPath.resolve("target"));
    }

    @DataProvider(name = "toolPropertiesDiagnostics")
    public Object[][] provideToolPropertiesDiagnostics() {
        return new Object[][]{
                {
                        "build-tool-with-invalid-missing-toml-properties",
                        "build-tool-with-invalid-missing-toml-properties.txt",
                        "error: build tool execution contains errors"
                },
                {
                        "build-tool-with-invalid-missing-optional-toml-properties",
                        "build-tool-with-invalid-missing-optional.txt",
                        "error: compilation contains errors"
                },
                {
                        "build-tool-with-diagnostics",
                        "build-tool-with-diagnostics.txt",
                        "error: build tool execution contains errors"
                },
                {
                        "build-tool-with-recurring-tool-properties",
                        "build-tool-with-recurring-tool-properties.txt",
                        "error: build tool execution contains errors"
                }
        };
    }

    @Test(description = "Build a project with invalid or missing toml entries for build tools",
            dataProvider = "toolPropertiesDiagnostics")
    public void testBuildProjectWithBuildToolTomlPropertyDiagnostics(String projectName, String outputFile,
                                                                     String error) throws IOException {
        Path projectPath = this.testResources.resolve(projectName);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replace("\r", ""),
                    getOutput(outputFile));
            Assert.assertEquals(error, e.getDetailedMessages().get(0));
        }
    }

    @Test(description = "Build a project with a build tool execution")
    public void testBuildProjectWithBuildTool() throws IOException {
        Path projectPath = this.testResources.resolve("proper-build-tool");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", "").replace("\\", "/"),
                getOutput("build-bal-project-with-build-tool.txt"));
    }

    @Test(description = "Build a project with a build tool not found")
    public void testBuildProjectWithBuildToolNotFound() throws IOException {
        Path projectPath = this.testResources.resolve("build-tool-not-found");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replace("\r", ""),
                    getOutput("build-bal-project-with-build-tool-not-found.txt"));
            Assert.assertEquals("error: build tool execution contains errors", e.getDetailedMessages().get(0));
        }
    }

    @Test(description = "Build a project with invalid fields in TOML array 'dependency'")
    public void testBuildProjectWithInvalidDependencyArrayInBallerinaToml() throws IOException {
        Path projectPath = this.testResources.resolve("invalid-dependency-array-project");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertEquals("error: compilation contains errors", e.getDetailedMessages().get(0));
            String buildLog = readOutput(true);
            Assert.assertEquals(buildLog.replace("\r", ""),
                    getOutput("build-bal-project-with-invalid-dependency-array.txt"));
        }
    }

    private String getNewVersionForOldDistWarning() {
        SemanticVersion currentDistributionVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        String currentVersionForDiagnostic = String.valueOf(currentDistributionVersion.minor());
        if (currentDistributionVersion.patch() != 0) {
            currentVersionForDiagnostic += DOT + currentDistributionVersion.patch();
        }
        return currentVersionForDiagnostic;
    }

    private String getOldVersionForOldDistWarning(String version) {
        SemanticVersion prevDistributionVersion = SemanticVersion.from(version);
        String prevVersionForDiagnostic;
        prevVersionForDiagnostic = String.valueOf(prevDistributionVersion.minor());
        if (prevDistributionVersion.patch() != 0) {
            prevVersionForDiagnostic += DOT + prevDistributionVersion.patch();
        }
        return prevVersionForDiagnostic;
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

    private void validateBuildTimeInfo(String buildLog) {
        Assert.assertTrue(buildLog.contains("timestamp"),
                "Missing timestamp field in build time logs");
        Assert.assertTrue(buildLog.contains("offline"),
                "Missing offline field in build time logs");
        Assert.assertTrue(buildLog.contains("compile"),
                "Missing compile field in build time logs");
        Assert.assertTrue(buildLog.contains("projectLoadDuration"),
                "Missing projectLoadDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("packageResolutionDuration"),
                "Missing packageResolutionDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("codeGeneratorPluginDuration"),
                "Missing codeGeneratorPluginDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("codeModifierPluginDuration"),
                "Missing codeModifierPluginDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("packageCompilationDuration"),
                "Missing packageCompilationDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("codeGenDuration"), "Missing codeGenDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("emitArtifactDuration"),
                "Missing emitArtifactDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("testingExecutionDuration"),
                "Missing testingExecutionDuration field in build time logs");
        Assert.assertTrue(buildLog.contains("totalDuration"), "Missing totalDuration field in build time logs");
    }

    @Test(description = "Check GraalVM compatibility of build project")
    public void testGraalVMCompatibilityOfJavaImportedProject() throws IOException {
        // Project contains only dist provided Java dependencies
        Path projectPath = this.testResources.resolve("validJavaProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs("--graalvm");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("Compiling source") && buildLog.contains("foo/winery:0.1.0") &&
                    !buildLog.contains("WARNING: Package is not verified with GraalVM"));
        }
    }

    @DataProvider(name = "validProjectWithPlatformLibs")
    public Object[][] provideValidProjectWithPlatformLibs() {
        String notVerifiedWaring = "WARNING: Package is not verified with GraalVM";
        String notCompatibleWarning = "WARNING: Package is not compatible with GraalVM";
        return new Object[][]{
                {"validProjectWithPlatformLibs1", notVerifiedWaring},
                {"validProjectWithPlatformLibs2", notCompatibleWarning},
                {"validProjectWithPlatformLibs3", notCompatibleWarning},
                {"validProjectWithPlatformLibs4", notVerifiedWaring}
        };
    }

    @Test(description = "Check GraalVM compatibility of build project",
            dataProvider = "validProjectWithPlatformLibs")
    public void testGraalVMCompatibilityOfJavaProject(String projectName, String warning) throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve(projectName);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parseArgs("--graalvm");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("Compiling source") &&
                    buildLog.contains("sameera/myproject:0.1.0") &&
                    buildLog.contains(warning));
        }
    }

    @Test(description = "Check GraalVM compatibility of build project")
    public void testGraalVMCompatibilityOfAnyProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand).parseArgs("--graalvm");
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("Compiling source") && buildLog.contains("foo/winery:0.1.0")
                    && !buildLog.contains("WARNING: Package is not verified with GraalVM"));
        }
    }

    @Test(description = "Build a simple workspace project")
    public void testSimpleWorkspaceProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve("workspaces/wp-simple");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        cleanTarget(projectPath);
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-simple.txt"));
    }

    @Test(description = "Build a single root workspace project")
    public void testSingleRootWorkspaceProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve("workspaces/wp-one-root");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-one-root.txt"));
    }

    @Test(description = "Build a workspace project containing multiple roots")
    public void testMultipleRootWorkspaceProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve("workspaces/wp-multiple-roots");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        cleanTarget(projectPath);
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-multiple-roots.txt"));
    }

    @Test(description = "Build workspace project containing nested paths")
    public void testNestedPathWorkspaceProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve("workspaces/wp-nested-path");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-nested-path.txt"));
    }

    @Test(description = "Build a specific package in a simple workspace project")
    public void testSimpleWorkspaceProjectBuildSpecificProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectRoot = this.testResources.resolve("workspaces/wp-simple");
        System.setProperty(USER_DIR_PROPERTY, projectRoot.toString());
        Path projectPath = projectRoot.resolve("hello-app");
        cleanTarget(projectPath);
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-simple-hello-app.txt"));
    }

    @Test(description = "Build a specific package in a multiple root workspace project")
    public void testMultipleRootProjectBuildSpecificProject() throws IOException {
        // Project contains platform Java dependencies
        Path projectRoot = this.testResources.resolve("workspaces");
        System.setProperty(USER_DIR_PROPERTY, projectRoot.toString());
        Path projectPath = projectRoot.resolve("wp-multiple-roots/hello-app");
        cleanTarget(projectPath);
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-multiple-roots-hello-app.txt"));
    }

    @Test(description = "Build a specific package in a multiple root workspace project")
    public void testWorkspaceProjectWithWrongPackagePath() throws IOException {
        // Project contains platform Java dependencies
        Path projectPath = this.testResources.resolve("workspaces/wp-wrong-path");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        // non-existing bal file
        new CommandLine(buildCommand);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertEquals(readOutput().replace("\r", ""), getOutput("wp-wrong-path.txt"));
        }
    }

    @Test(description = "Build a project twice with the same flags and different flags")
    public void testBuildAProjectTwiceWithFlags() throws IOException {
        String[] argsList1 = {
                "--offline",
                "--sticky",
                // "--locking-mode=SOFT", // TODO: enable after fixing #42467
                "--experimental",
                "--optimize-dependency-compilation",
                "--remote-management",
                "--observability-included"
        };

        // Use the same flag that affects jar generation similarly in the consecutive builds
        for (String arg : argsList1) {
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs(arg);
            buildCommand.execute();
            String firstBuildLog = readOutput(true);
            buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs(arg);
            buildCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
            Assert.assertTrue(secondBuildLog.contains("Generating executable (UP-TO-DATE)"),
                    "Second build is not up-to-date for " + arg);
        }

        // Use different flags that affect jar generation differently in the consecutive builds
        for (String arg : argsList1) {
            if (arg.equals("--sticky") || arg.equals("--offline")) {
                // Skip --sticky since the second build will sticky anyway within 24 hours
                // Skip --offline since tests are always run offline
                continue;
            }
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs(arg);
            buildCommand.execute();
            String firstBuildLog = readOutput(true);
            buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs();
            buildCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
            Assert.assertTrue(secondBuildLog.contains("buildAProjectTwice.jar"));
            Assert.assertFalse(secondBuildLog.contains("Generating executable (UP-TO-DATE)"),
                    "Second build should not be up-to-date for " + arg);
        }

        String[] argsList2 = {
                "--dump-bir",
                "--dump-bir-file",
                "--dump-graph",
                "--dump-raw-graphs",
                "--generate-config-schema",
                "--show-dependency-diagnostics",
                "--list-conflicted-classes",
                "--dump-build-time",
                "--export-openapi",
                "--export-component-model",
                "--disable-syntax-tree-caching"
        };

        // Use different flags that doesn't affect jar generation in the consecutive builds
        for (String arg : argsList2) {
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs(arg);
            buildCommand.execute();
            String firstBuildLog = readOutput(true);
            buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs();
            buildCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("Compiling source"));
            Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
            Assert.assertTrue(secondBuildLog.contains("Generating executable (UP-TO-DATE)"),
                    "Second build should be up-to-date for " + arg);
        }

        String[] argsList3 = {
                "--dump-bir",
                "--dump-bir-file",
                "--dump-graph",
                "--dump-raw-graphs",
                "--generate-config-schema",
                "--show-dependency-diagnostics",
                "--list-conflicted-classes",
                "--dump-build-time",
                "--export-openapi",
                "--export-component-model",
                "--disable-syntax-tree-caching",
        };

        for (String arg : argsList3) {
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs();
            buildCommand.execute();
            String firstBuildLog = readOutput(true);
            buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
            new CommandLine(buildCommand).parseArgs(arg);
            buildCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
            Assert.assertTrue(secondBuildLog.contains("Compiling source"));
            Assert.assertFalse(secondBuildLog.contains("Generating executable (UP-TO-DATE)"),
                    "Second build should not be up-to-date for " + arg);
        }
    }

    @Test(description = "Build a project after 24 hours of the last build")
    public void testBuildAProjectTwiceBeforeAfter24Hr() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        cleanTarget(projectPath);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String firstBuildLog = readOutput(true);

        // Second build within 24 hours
        buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String secondBuildLog = readOutput(true);

        Path buildFilePath = projectPath.resolve("target").resolve("build");
        BuildJson buildJson = readBuildJson(buildFilePath);
        buildJson.setLastUpdateTime(buildJson.lastUpdateTime() - (24 * 60 * 60 * 1000 + 1));
        ProjectUtils.writeBuildFile(buildFilePath, buildJson);
        buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String thirdBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
        Assert.assertTrue(secondBuildLog.contains("Generating executable (UP-TO-DATE)"));
        Assert.assertTrue(thirdBuildLog.contains("Generating executable"));
        Assert.assertFalse(thirdBuildLog.contains("Generating executable (UP-TO-DATE)"));
    }

    @Test(description = "Build a project with a new file within 24 hours of the last build")
    public void testBuildAProjectWithFileAddition() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String firstBuildLog = readOutput(true);
        Path balFilePath = projectPath.resolve("main2.bal");
        String balContent = "public function main2() {\n}\n";
        Files.writeString(balFilePath, balContent);
        buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
        Assert.assertTrue(secondBuildLog.contains("Generating executable"));
        Assert.assertFalse(secondBuildLog.contains("Generating executable (UP-TO-DATE)"));
    }

    @Test(description = "Build a project with a new file within 24 hours of the last build")
    public void testBuildAProjectWithFileModification() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String firstBuildLog = readOutput(true);
        Path balFilePath = projectPath.resolve("main.bal");
        String balContent = "public function math() {\n}\n";
        Files.writeString(balFilePath, balContent);
        buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
        Assert.assertTrue(secondBuildLog.contains("Generating executable"));
        Assert.assertFalse(secondBuildLog.contains("Generating executable (UP-TO-DATE)"));
    }

    @Test(description = "Build a project with no content change. The file is modified with same content")
    public void testBuildAProjectWithFileNoContentChange() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String firstBuildLog = readOutput(true);
        Path balFilePath = projectPath.resolve("main.bal");
        String balContent = "public function main() {\n\n}\n";
        Files.writeString(balFilePath, balContent);
        buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            String failureLog = readOutput(true);
            Assert.fail(failureLog + "\n error message: " + e.getDetailedMessages().get(0));
        }
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
        // Though the content is the same, the file modification time is changed.
        // Hence, the build should not be up-to-date
        Assert.assertTrue(secondBuildLog.contains("Generating executable"));
        Assert.assertFalse(secondBuildLog.contains("Generating executable (UP-TO-DATE)"));
    }

    @Test(description = "Build a project twice with the pack command in the middle")
    public void testBuildAProjectTwiceWithPackCommandMiddle() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        buildCommand.execute();
        String firstBuildLog = readOutput(true);
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String middleBuildLog = readOutput(true);
        buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        new CommandLine(buildCommand).parseArgs();
        buildCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("Compiling source"));
        Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(middleBuildLog.contains("Compiling source"));
        Assert.assertFalse(middleBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }
}
