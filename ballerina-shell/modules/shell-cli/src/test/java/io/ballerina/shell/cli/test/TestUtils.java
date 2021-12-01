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

package io.ballerina.shell.cli.test;

import com.google.gson.Gson;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

/**
 * Class with utility functions required for
 * other tests.
 *
 * @since 2.0.0
 */
public abstract class TestUtils {
    private static final String SPECIAL_DELIMITER = "\\A";

    /**
     * Loads a JSON fie with the given class format.
     *
     * @param fileName       JSON file to load.
     * @param testCasesClazz Class def of the required type.
     * @param <T>            Type of the return class.
     * @return The instance of the required type.
     */
    public static <T> T loadTestCases(String fileName, Class<T> testCasesClazz) {
        Gson gson = new Gson();
        return gson.fromJson(readFile(fileName), testCasesClazz);
    }

    /**
     * Reads a file from resources with the given file name.
     *
     * @param fileName Resource name.
     * @return File content.
     */
    public static String readFile(String fileName) {
        InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(fileName);
        Objects.requireNonNull(inputStream, "Test file does not exist: " + fileName);
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter(SPECIAL_DELIMITER)) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public static File getFile(String fileName) {
        URL url = TestUtils.class.getClassLoader().getResource(fileName);
        Objects.requireNonNull(url, "Test file does not exist: " + fileName);
        return new File(url.getPath());
    }
}
