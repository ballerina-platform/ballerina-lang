/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.test.base;

import io.ballerina.shell.cli.PropertiesLoader;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.ballerina.shell.cli.PropertiesLoader.REPL_PROMPT;

/**
 * Class that will integrate tests with the shell
 * by piping input/output streams.
 *
 * @since 2.0.0
 */
public class TestIntegrator extends Thread {
    private static final String[] SPECIAL_CONTROL_STRINGS = {"\\x1B>", "\\x1B=", "\\x08", "\r"};
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final ByteArrayOutputStream stdoutStream;
    private final List<TestCase> testCases;
    private final String shellPrompt;

    public TestIntegrator(InputStream inputStream, OutputStream outputStream, ByteArrayOutputStream stdoutStream,
                          List<TestCase> testCases) {
        this.shellPrompt = PropertiesLoader.getProperty(REPL_PROMPT);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.stdoutStream = stdoutStream;
        this.testCases = testCases;
    }

    @Override
    public void run() {
        try {
            PrintStream testPrint = new PrintStream(outputStream, true, Charset.defaultCharset());
            InputStreamReader inStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
            BufferedReader testReader = new BufferedReader(inStreamReader);

            // The response here is not testable because it can change.
            // The first readResponse is to read and ignore all the initial
            // output from the CLI. (header text, etc...)
            // The second readResponse is to read and ignore any response to the
            // first request.
            sendRequest(testPrint, "");
            readResponse(testReader);
            readResponse(testReader);

            for (TestCase testCase : testCases) {
                sendRequest(testPrint, testCase.getCode());

                // Read input (ignore till first shell prompt)
                String exprResponse = readResponse(testReader);
                exprResponse = exprResponse.substring(exprResponse.indexOf(shellPrompt));

                // Expected format: [PROMPT][INPUT]\n\n[OUTPUT]\n\n[PROMPT]\n
                String expectedExprResponse = String.format("%s%s%n%n%s%n%s%n",
                        shellPrompt, testCase.getCode(), testCase.getExpr(), shellPrompt);

                // Remove special sequences
                exprResponse = filteredString(exprResponse);
                expectedExprResponse = filteredString(expectedExprResponse);

                Assert.assertEquals(exprResponse, expectedExprResponse,
                        errorMessage(exprResponse, expectedExprResponse, testCase.getDescription()));
                Assert.assertEquals(stdoutStream.toString(), testCase.getStdout(),
                        testCase.getDescription());
                stdoutStream.reset();
            }
        } catch (IOException | InterruptedException ignored) {
        }
    }

    /**
     * Reads data from the stream specified until if finds shell prompt as the EOL.
     */
    private String readResponse(BufferedReader stream) throws IOException {
        String line = "";
        StringBuilder data = new StringBuilder();
        while (!line.endsWith(shellPrompt)) {
            line = Objects.requireNonNull(stream.readLine());
            data.append(line).append(System.lineSeparator());
        }
        return data.toString();
    }

    /**
     * Send the data given to the specific stream.
     */
    private void sendRequest(PrintStream stream, String string) throws InterruptedException {
        stream.append(string);
        stream.println(System.lineSeparator());
        stream.flush();
    }

    /**
     * Remove invisible characters from the string.
     * The removed characters are ansi characters that are added by jline.
     */
    private String filteredString(String string) {
        // Remove all ANSI codes
        string = string.replaceAll("(\\x9B|\\x1B\\[)[0-?]*[ -/]*[@-~]", "");
        // Disable ANSI escape characters and Backspace character
        string = string
                .replaceAll("\\x9B", "\\\\x9B")
                .replaceAll("\\x1B", "\\\\x1B")
                .replaceAll("\\x08", "\\\\x08");
        // Remove any special sequences known
        for (String controlString : SPECIAL_CONTROL_STRINGS) {
            string = string.replace(controlString, "");
        }
        // Remove new lines because new lines can vary
        return string.replace("\n", "");
    }

    private String errorMessage(String original, String expected, String description) {
        return description + " failed because " +
                Arrays.toString(original.getBytes(StandardCharsets.UTF_8)) + "!=" +
                Arrays.toString(expected.getBytes(StandardCharsets.UTF_8));
    }
}
