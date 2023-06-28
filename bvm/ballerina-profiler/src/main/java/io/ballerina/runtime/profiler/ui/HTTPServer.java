/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.profiler.ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.ballerina.runtime.profiler.ui.FrontEnd.getSiteData;

/**
 * This class contains the HTTP server of the ballerina profiler.
 *
 * @since 2201.7.0
 */
public class HTTPServer {
    public static final String ANSI_YELLOW = "\033[1;38;2;255;255;0m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static void initializeHTMLExport() throws IOException {
        System.out.printf(" ○ Output: " + ANSI_YELLOW + "target/bin/ProfilerOutput.html" + ANSI_RESET + "%n");
        String content = readData();
        String htmlData = getSiteData(content);
        String fileName = "ProfilerOutput.html";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName, StandardCharsets.UTF_8);
            fileWriter.write(htmlData);
        } catch (IOException e) {
            System.out.printf(e + "%n");
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    // Handle the exception if closing the writer fails
                    System.out.printf(e + "%n");
                }
            }
        }
    }

    private static String readData() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader("performance_report.json", StandardCharsets.UTF_8))) {
            StringBuilder contents = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contents.append(line);
            }
            return contents.toString();
        }
    }
}
