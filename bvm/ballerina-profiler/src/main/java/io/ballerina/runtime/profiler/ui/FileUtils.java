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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    // Maximum wait time for the file to be created. This will wait 600*100 ms = 60 s.
    private static final int MAX_WAIT_TIME_FOR_FILE = 600;

    private FileUtils() {
    }

    static String readFileAsString(String file) throws IOException {
        Path path = Paths.get(file);
        int count = 0;
        while (!Files.exists(path)) {
            if (count++ > MAX_WAIT_TIME_FOR_FILE) {
                throw new ProfilerRuntimeException("File not found: " + file);
            }
            waitForFile();
        }
        return Files.readString(path);
    }

    private static void waitForFile() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
