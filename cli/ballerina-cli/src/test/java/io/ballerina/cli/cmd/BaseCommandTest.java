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
import org.ballerinalang.compiler.BLangCompilerException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;

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
    
    @BeforeClass
    public void setup() throws IOException {
        System.setProperty("java.command", "java");
        this.tmpDir = Files.createTempDirectory("b7a-cmd-test-" + System.nanoTime());
        this.homeCache = Paths.get("build", "userHome");
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @BeforeMethod
    public void beforeMethod() {
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    protected String readOutput() throws IOException {
        return readOutput(false);
    }

    protected String readOutput(boolean silent) throws IOException {
        String output = "";
        output = console.toString();
        console.close();
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
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

        try {
            Files.walk(balaProjectDirectory).forEach(a -> {
                Path b = Paths.get(String.valueOf(balaDestPath),
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
        console.close();
        printStream.close();
    }

    @AfterClass (alwaysRun = true)
    public void cleanup() {
        ProjectUtils.deleteDirectory(this.tmpDir);
        System.setProperty("user.dir", userDir);
    }
}
