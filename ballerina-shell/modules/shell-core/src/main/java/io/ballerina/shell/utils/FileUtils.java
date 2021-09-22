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

package io.ballerina.shell.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Utility class to read resource files.
 *
 * @since 2.0.0
 */
public class FileUtils {
    private static final String SPECIAL_DELIMITER = "\\A";

    /**
     * Reads the file content from the resources.
     *
     * @param path Path of the file to read.
     * @return Read text.
     */
    public static String readResource(String path) {
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(path);
        Objects.requireNonNull(inputStream, "File does not exist: " + path);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(reader).useDelimiter(SPECIAL_DELIMITER);
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Read a comma separated list of keywords from a file.
     *
     * @param file Keyword file name.
     * @return List of keywords.
     */
    public static List<String> readKeywords(String file) {
        String content = readResource(file);
        Scanner scanner = new Scanner(content).useDelimiter(",");
        List<String> keywords = new ArrayList<>();
        while (scanner.hasNext()) {
            String keyword = scanner.next().trim();
            if (!keyword.isBlank()) {
                keywords.add(keyword);
            }
        }
        return keywords;
    }
}
