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
        testCases.add(new String[]{"i*2 + 10", "80"});
        testCases.add(new String[]{"/exit", ""});

        PipedOutputStream testOut = new PipedOutputStream();
        PipedInputStream shellIn = new PipedInputStream(testOut);
        PipedOutputStream shellOut = new PipedOutputStream();
        PipedInputStream testIn = new PipedInputStream(shellOut);

        Thread testIntegratorThread = new Thread(() -> {
            try {
                String shellPrompt = "=$ ";
                PrintStream testPrint = new PrintStream(testOut, true, Charset.defaultCharset());
                InputStreamReader inStreamReader = new InputStreamReader(testIn, Charset.defaultCharset());
                BufferedReader testReader = new BufferedReader(inStreamReader);

                for (String[] testCase : testCases) {
                    testPrint.println(testCase[0] + System.lineSeparator());
                    StringBuilder recordedInput = new StringBuilder();
                    while (true) {
                        String line = Objects.requireNonNull(testReader.readLine());
                        recordedInput.append(line).append(System.lineSeparator());
                        if (line.endsWith(shellPrompt)) {
                            break;
                        }
                    }

                    // recordedContent: [GARBAGE][=$ ][INPUT][OUTPUT][=$ ]\n
                    String recordedContent = filteredString(recordedInput.toString());
                    // recordedContent: [=$ ][INPUT][OUTPUT][=$ ]\n
                    recordedContent = recordedContent.substring(recordedContent.indexOf(shellPrompt));
                    // recordedContent: [INPUT][OUTPUT]
                    recordedContent = recordedContent.substring(shellPrompt.length(),
                            recordedContent.length() - shellPrompt.length() - System.lineSeparator().length());
                    // shellOutput: [OUTPUT]
                    String shellOutput = recordedContent.substring(testCase[0].length()).trim();
                    String expectedOutput = Objects.requireNonNullElse(testCase[1], "");
                    Assert.assertEquals(shellOutput, expectedOutput);
                }
            } catch (IOException ignored) {
            }
        });
        testIntegratorThread.start();

        try {
            BShellConfiguration configuration = new BShellConfiguration.Builder()
                    .setDebug(false).setInputStream(shellIn).setOutputStream(shellOut)
                    .setTreeParsingTimeoutMs(10000).build();
            ReplShellApplication.execute(configuration);
        } catch (EndOfFileException ignored) {
        }

        testIntegratorThread.interrupt();
        testIntegratorThread.join();
    }

    private String filteredString(String rawString) {
        return rawString.replaceAll("(\\x9B|\\x1B\\[)[0-?]*[ -/]*[@-~]", "");
    }
}
