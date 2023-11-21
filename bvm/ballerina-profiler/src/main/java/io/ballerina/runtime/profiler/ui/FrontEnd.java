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

import io.ballerina.runtime.profiler.runtime.ProfilerRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class contains the front end of the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class FrontEnd {

    private static final String PROFILE_DATA = "${profile_data}";
    private static final String FILE_LOCATION = "profiler_output.html";

    String getSiteData(String contents) {
        String htmlContent;
        try {
            htmlContent = readFileAsString();
        } catch (IOException e) {
            throw new ProfilerRuntimeException("error while reading profiler output.", e);
        }
        return htmlContent.replace(PROFILE_DATA, contents);
    }

    public String readFileAsString() throws IOException {
        StringBuilder sb = new StringBuilder();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_LOCATION)) {
            if (inputStream == null) {
                throw new ProfilerRuntimeException("resource file not found: " + FILE_LOCATION);
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(inputStreamReader)) {
                String content = br.readLine();
                if (content == null) {
                    return sb.toString();
                }
                sb.append(content);
                while ((content = br.readLine()) != null) {
                    sb.append('\n').append(content);
                }
            }
        }
        return sb.toString();
    }
}
