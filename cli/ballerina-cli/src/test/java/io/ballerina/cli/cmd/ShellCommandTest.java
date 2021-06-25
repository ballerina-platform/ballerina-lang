/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.cli.ReplShellApplication;
import org.jline.reader.EndOfFileException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shell command tests.
 *
 * @since 2.0.0
 */
public class ShellCommandTest extends BaseCommandTest {
    public static final String BALLERINA_HOME = "ballerina.home";

    @Test(description = "Test shell command fail if ballerina.home is wrongly set.")
    public void testShellCommandFail() throws IOException {
        PrintStream outStreamOrig = System.out;
        String ballerinaHomeOrig = System.getProperty(BALLERINA_HOME);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            System.setProperty(BALLERINA_HOME, "not/existing/dir/abc");
            System.setOut(new PrintStream(baos));
            ShellCommand shellCommand = new ShellCommand(this.printStream, true, 1000);
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

    @Test
    public void testShellExecution() throws Exception {
        List<String[]> testCases = new ArrayList<>();
        testCases.add(new String[]{"int i = 35", ""});
        testCases.add(new String[]{"i*2 + 10", "80\n"});
        testCases.add(new String[]{"function add(int a, int b) returns int => a + b", ""});
        testCases.add(new String[]{"add(i, 200)", "235\n"});
        testCases.add(new String[]{"/exit", ""});

        String shellPrompt = "=$ ";

        PipedOutputStream testOut = new PipedOutputStream();
        PipedInputStream shellIn = new PipedInputStream(testOut);
        PipedOutputStream shellOut = new PipedOutputStream();
        PipedInputStream testIn = new PipedInputStream(shellOut);

        Thread testIntegratorThread = new Thread(() -> {
            try {
                PrintStream testPrint = new PrintStream(testOut, true, Charset.defaultCharset());
                InputStreamReader inStreamReader = new InputStreamReader(testIn, Charset.defaultCharset());
                BufferedReader testReader = new BufferedReader(inStreamReader);

                sendRequest(testPrint, "");
                readResponse(testReader, shellPrompt);
                readResponse(testReader, shellPrompt);

                for (String[] testCase : testCases) {
                    sendRequest(testPrint, testCase[0]);
                    String exprResponse = readResponse(testReader, shellPrompt);
                    exprResponse = exprResponse.substring(exprResponse.indexOf(shellPrompt));
                    // Expected format: [PROMPT][INPUT]\n\n[OUTPUT]\n\n[PROMPT]\n
                    String expectedExprResponse = String.format("%s%s%n%n%s%n%s%n",
                            shellPrompt, testCase[0], testCase[1], shellPrompt);
                    Assert.assertEquals(filteredString(exprResponse), filteredString(expectedExprResponse));
                }
            } catch (IOException | InterruptedException ignored) {
            }
        });
        testIntegratorThread.start();

        try {
            BShellConfiguration configuration = new BShellConfiguration.Builder()
                    .setDebug(false).setInputStream(shellIn).setOutputStream(shellOut)
                    .setDumb(true).setTreeParsingTimeoutMs(10000).build();
            ReplShellApplication.execute(configuration);
        } catch (EndOfFileException ignored) {
        }

        testIntegratorThread.interrupt();
        testIntegratorThread.join();
    }

    private String readResponse(BufferedReader stream, String shellPrompt) throws IOException {
        String line = "";
        StringBuilder data = new StringBuilder();
        while (!line.endsWith(shellPrompt)) {
            line = Objects.requireNonNull(stream.readLine());
            data.append(line).append(System.lineSeparator());
        }
        return data.toString();
    }

    private void sendRequest(PrintStream stream, String string) throws InterruptedException {
        stream.append(string);
        stream.println(System.lineSeparator());
        stream.flush();
    }

    private String filteredString(String string) {
        // Remove all ANSI codes
        string = string.replaceAll("(\\x9B|\\x1B\\[)[0-?]*[ -/]*[@-~]", "");
        string = string.replaceAll("\\x1B>", "");
        string = string.replaceAll("\\x1B=", "");
        string = string.replaceAll("\\x08", "");
        string = string.replaceAll("\r", "");
        return string;
    }
}
