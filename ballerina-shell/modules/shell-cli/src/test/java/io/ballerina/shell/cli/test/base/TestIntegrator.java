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
import io.ballerina.shell.cli.utils.FileUtils;
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

import static io.ballerina.shell.cli.PropertiesLoader.HEADER_FILE;
import static io.ballerina.shell.cli.PropertiesLoader.REPL_PROMPT;

/**
 * Class that will integrate tests with the shell
 * by piping input/output streams.
 *
 * @since 2.0.0
 */
public class TestIntegrator extends Thread {
    private static final String[] SPECIAL_CONTROL_STRINGS = {"\\x1B>", "\\x1B=", "\\x08"};
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

            // Assert welcome text
            String headerText = FileUtils.readResource(PropertiesLoader.getProperty(HEADER_FILE));
            sendRequest(testPrint, "");
            String response = readResponse(testReader);
            response = response.substring(0, response.indexOf(shellPrompt));
            Assert.assertEquals(response.trim(), headerText.trim(), "Welcome text should be same");

            readResponse(testReader);

            for (TestCase testCase : testCases) {
                sendRequest(testPrint, testCase.getCode());
                String exprResponse = stripTillPrompt(readResponse(testReader));
                // Expected format: [PROMPT][INPUT]\n\n[OUTPUT]\n\n[PROMPT]\n
                String expectedExprResponse = String.format("%s%s%n%n%s%n%s%n",
                        shellPrompt, testCase.getCode(), testCase.getExpr(), shellPrompt);

                Assert.assertEquals(replaceNewLine(exprResponse), replaceNewLine(expectedExprResponse),
                        errorMessage(exprResponse, expectedExprResponse, testCase.getDescription()));
                Assert.assertEquals(replaceNewLine(stdoutStream.toString()), replaceNewLine(testCase.getStdout()),
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
        return filteredString(data.toString());
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
        return string;
    }

    /**
     * Strip string till the first appearance of prompt.
     */
    private String stripTillPrompt(String string) {
        return string.substring(string.indexOf(shellPrompt));
    }

    /**
     * Replace new line with escaped newline.
     */
    private String replaceNewLine(String string) {
        return string.replace("\r\n", "\n")
                .replace("\n", "\\n");
    }

    private String errorMessage(String original, String expected, String description) {
        return description + " failed because " +
                Arrays.toString(replaceNewLine(original).getBytes(StandardCharsets.UTF_8)) + "!=" +
                Arrays.toString(replaceNewLine(expected).getBytes(StandardCharsets.UTF_8));
    }
}
