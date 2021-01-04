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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Shell command tests.
 */
public class ShellCommandTest extends BaseCommandTest {
    public static final String BALLERINA_HOME = "ballerina.home";

    @Test(description = "Test shell command initialization.")
    public void testShellCommandSmoke() throws IOException {
        PrintStream outStreamOrig = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(baos));
            ShellCommand shellCommand = new ShellCommand(this.printStream, true);
            shellCommand.execute();
        } finally {
            System.setOut(outStreamOrig);
        }

        // Gives the prompt
        Assert.assertTrue(baos.toString().endsWith("=$ "));
        // Exits because of EOF
        Assert.assertEquals(readOutput().trim(), "" +
                "Something went wrong while executing REPL: org.jline.reader.EndOfFileException: " +
                "org.jline.utils.ClosedException");
    }


    @Test(description = "Test shell command fail if ballerina.home is wrongly set.")
    public void testShellCommandFail() throws IOException {
        PrintStream outStreamOrig = System.out;
        String ballerinaHomeOrig = System.getProperty(BALLERINA_HOME);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            System.setProperty(BALLERINA_HOME, "not/existing/dir/abc");
            System.setOut(new PrintStream(baos));
            ShellCommand shellCommand = new ShellCommand(this.printStream, true);
            shellCommand.execute();
        } finally {
            System.setOut(outStreamOrig);
            System.setProperty(BALLERINA_HOME, ballerinaHomeOrig);
        }

        // Does not give the prompt
        Assert.assertFalse(baos.toString().endsWith("=$ "));
        // Exit because home dir err
        Assert.assertEquals(readOutput().trim(), "" +
                "Something went wrong while executing REPL: " +
                "io.ballerina.projects.ProjectException: Ballerina distribution directory does not exists in `" +
                "not/existing/dir/abc'");
    }
}
