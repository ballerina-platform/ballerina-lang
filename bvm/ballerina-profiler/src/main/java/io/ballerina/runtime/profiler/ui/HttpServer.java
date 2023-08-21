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

import io.ballerina.runtime.profiler.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.ballerina.runtime.profiler.util.Constants.OUT_STREAM;

/**
 * This class contains the HTTP server of the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class HttpServer {

    public void initializeHTMLExport() throws IOException {
        OUT_STREAM.printf(" ○ Output: " + Constants.ANSI_YELLOW
                + "target/bin/ProfilerOutput.html" + Constants.ANSI_RESET + "%n");
        String content = readData();
        FrontEnd frontEnd = new FrontEnd();
        String htmlData = frontEnd.getSiteData(content);
        String fileName = "ProfilerOutput.html";
        try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
            writer.write(htmlData);
        } catch (IOException e) {
            OUT_STREAM.println(e + "%n");
        }
    }

    private String readData() throws IOException {
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
