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
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final ByteArrayOutputStream stdoutStream;
    private final List<TestCase> testCases;

    public TestIntegrator(InputStream inputStream, OutputStream outputStream,
                          ByteArrayOutputStream stdoutStream, List<TestCase> testCases) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.stdoutStream = stdoutStream;
        this.testCases = testCases;
    }

    @Override
    public void run() {
        try {
            String shellPrompt = PropertiesLoader.getProperty(REPL_PROMPT);
            PrintStream testPrint = new PrintStream(outputStream, true, Charset.defaultCharset());
            InputStreamReader inStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
            BufferedReader testReader = new BufferedReader(inStreamReader);

            for (TestCase testCase : testCases) {
                // Give input and record shell in/out.
                testPrint.println(testCase.getCode() + System.lineSeparator());

                String recordedContent;
                while (true) {
                    StringBuilder recordedInput = new StringBuilder();
                    while (true) {
                        String line = Objects.requireNonNull(testReader.readLine());
                        recordedInput.append(line).append(System.lineSeparator());
                        if (line.endsWith(shellPrompt)) {
                            break;
                        }
                    }

                    // The input will be in format "[GARBAGE][PROMPT][INPUT][OUTPUT][PROMPT][GARBAGE]".
                    recordedContent = filteredString(recordedInput.toString());
                    // Remove all unnecessary prefix/prompt strings. (Remove GARBAGE and PROMPT)
                    // recordedContent = [INPUT][OUTPUT][PROMPT][GARBAGE]
                    recordedContent = recordedContent.substring(recordedContent.indexOf(shellPrompt) +
                            shellPrompt.length());
                    if (recordedContent.indexOf(shellPrompt) <= 0) {
                        continue;
                    }

                    // recordedContent = [INPUT][OUTPUT]
                    recordedContent = recordedContent.substring(0, recordedContent.indexOf(shellPrompt));
                    break;
                }

                // Extract INPUT and verify.
                String recordedContentInput = recordedContent.substring(0, testCase.getCode().length());
                Assert.assertEquals(recordedContentInput, testCase.getCode(), testCase.getDescription());

                // Extract OUTPUT and test.
                String shellOutput = recordedContent.substring(testCase.getCode().length()).trim();
                String expectedOutput = Objects.requireNonNullElse(testCase.getExpr(), "");
                Assert.assertEquals(shellOutput.trim(), expectedOutput.trim(), testCase.getDescription());

                Assert.assertEquals(stdoutStream.toString().trim(), testCase.getStdout().trim(),
                        testCase.getDescription());
                stdoutStream.reset();
            }
        } catch (IOException ignored) {
        }
    }

    private String filteredString(String rawString) {
        return rawString
                .replaceAll("(\\x9B|\\x1B\\[)[0-?]*[ -/]*[@-~]", "")
                .replace("\r\n", "\n");
    }
}
