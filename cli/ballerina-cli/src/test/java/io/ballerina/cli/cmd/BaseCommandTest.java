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

import static io.ballerina.cli.utils.FileUtils.deleteDirectory;


/**
 * Command tests super class.
 *
 * @since 2.0.0
 */
public abstract class BaseCommandTest {
    protected Path tmpDir;
    private ByteArrayOutputStream console;
    protected PrintStream printStream;
    
    @BeforeClass
    public void setup() throws IOException {
        System.setProperty("java.command", "java");
        this.tmpDir = Files.createTempDirectory("b7a-cmd-test-" + System.nanoTime());
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

    @AfterMethod (alwaysRun = true)
    public void afterMethod() throws IOException {
        console.close();
        printStream.close();
    }

    @AfterClass (alwaysRun = true)
    public void cleanup() throws IOException {
        deleteDirectory(this.tmpDir);
    }
}
