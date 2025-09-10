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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;

/**
 * Command tests super class.
 *
 * @since 2.0.0
 */
public abstract class BaseCommandTest {
    protected Path tmpDir;
    private ByteArrayOutputStream console;
    protected PrintStream printStream;
    protected Path homeCache;
    private final String userDir = System.getProperty("user.dir");
    final Path testDotBallerina = Paths.get(System.getenv(ProjectDirConstants.HOME_REPO_ENV_KEY));
    final Path testCentralRepoCache = testDotBallerina.resolve(REPOSITORIES_DIR)
            .resolve(CENTRAL_REPOSITORY_CACHE_NAME);

    @BeforeClass
    public void setup() throws IOException {
        System.setProperty("java.command", "java");
        this.tmpDir = Files.createTempDirectory("b7a-cmd-test-" + System.nanoTime());
        this.homeCache = Path.of("build", "userHome");
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @BeforeSuite
    public void copyBalTools() throws IOException {
        Path testResourcesSrc = Paths.get("src/test/resources/test-resources/buildToolResources/tools");
        Path testResources = Files.createTempDirectory("b7a-cmd-test-" + System.nanoTime());
        // copy the bal-tools.toml to <user-home>/.ballerina/.config/
        Path balToolsTomlSrcPath = Paths.get("src/test/resources/test-resources/" +
                "buildToolResources/tools/bal-tools.toml");
        Path balToolsTomlDstPath = testDotBallerina.resolve(".config");
        Files.createDirectories(balToolsTomlDstPath);
        Files.copy(balToolsTomlSrcPath, balToolsTomlDstPath.resolve("bal-tools.toml"));


        FileUtils.copyDirectory(testResourcesSrc.toFile(), testResources.toFile());
        // compile and cache test build tools
        String sampleBuildToolJar = "sample-build-tool-1.0.0.jar";
        Path sampleBuildToolJarPath = Paths.get("build/tool-libs").resolve(sampleBuildToolJar);
        Path destPath = testResources.resolve("sample-build-tool-pkg")
                .resolve("lib").resolve(sampleBuildToolJar);
        Files.createDirectories(destPath.getParent());
        Files.copy(sampleBuildToolJarPath, destPath);
        destPath = testResources.resolve("dummy-tool-pkg-higher-dist")
                .resolve("lib").resolve(sampleBuildToolJar);
        Files.createDirectories(destPath.getParent());
        Files.copy(sampleBuildToolJarPath, destPath);

        BCompileUtil.compileAndCacheBala(
                testResources.resolve("dummy-tool-pkg").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("hidden-cmd-tool-pkg").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("missing-interface-tool-pkg").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("no-options-tool-pkg").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("invalid-name-tool-pkg").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("dummy-tool-pkg-higher-dist").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("ballerina-generate-file").toString(), testCentralRepoCache);
        BCompileUtil.compileAndCacheBala(
                testResources.resolve("sample-build-tool-pkg").toString(), testCentralRepoCache);
    }

    @BeforeMethod
    public void beforeMethod() {
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @DataProvider(name = "optimizeDependencyCompilation")
    public Object [] [] provideOptimizeDependencyCompilation() {
        return new Object [][] {{ false }, { true }};
    }

    protected String readOutput() throws IOException {
        return readOutput(false);
    }

    protected String readOutput(boolean silent) throws IOException {
        return readOutput(silent, true);
    }

    protected String readOutput(boolean silent, boolean closeConsole) throws IOException {
        String output = "";
        output = console.toString();
        if (closeConsole) {
            console.close();
            console = new ByteArrayOutputStream();
            printStream = new PrintStream(console);
        }
        if (!silent) {
            PrintStream out = System.out;
            out.println(output);
        }
        return output;
    }

    /**
     * Execute a command and get the exception.
     *
     * @param cmd The command.
     * @return The error message.
     */
    public String executeAndGetException(BLauncherCmd cmd) throws IOException {
        try {
            cmd.execute();
            Assert.fail("Expected exception did not occur.");
        } catch (BLauncherException e) {
            if (e.getMessages().size() == 1) {
                readOutput(true);
                return e.getMessages().get(0);
            }
        } catch (BLangCompilerException e) {
            readOutput(true);
            return e.getMessage();
        } catch (Exception e) {
            Assert.fail("Invalid exception found: " + e.getClass().toString() + "-" + e.getMessage());
        }
        return null;
    }

    protected void cacheBalaToCentralRepository(Path balaProjectDirectory, String org, String name,
                                                String version, String platform) throws IOException {
        Path centralRepoPath = homeCache.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(CENTRAL_REPOSITORY_CACHE_NAME).resolve(ProjectConstants.BALA_DIR_NAME);
        Path balaDestPath = centralRepoPath.resolve(org).resolve(name).resolve(version).resolve(platform);
        Files.createDirectories(balaDestPath);

        try (Stream<Path> files = Files.walk(balaProjectDirectory)) {
            files.forEach(a -> {
                Path b = Path.of(String.valueOf(balaDestPath),
                        a.toString().substring(balaProjectDirectory.toString().length()));
                try {
                    if (!a.toString().equals(String.valueOf(balaProjectDirectory))) {
                        Files.copy(a, b, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    Assert.fail("Cache bala to central repository failed", e);
                }
            });
        } catch (IOException e) {
            Assert.fail("Cache bala to central repository failed", e);
        }
    }

    @AfterMethod (alwaysRun = true)
    public void afterMethod() throws IOException {
        if (console != null) {
            console.close();
        }
        if (printStream != null) {
            printStream.close();
        }
    }

    @AfterClass (alwaysRun = true)
    public void cleanup() {
        ProjectUtils.deleteDirectory(this.tmpDir);
        System.setProperty("user.dir", userDir);
    }

    protected void cleanTarget(Path projectPath) {
        CleanCommand cleanCommand = new CleanCommand(projectPath, false);
        new CommandLine(cleanCommand).parseArgs();
        cleanCommand.execute();
    }
}
