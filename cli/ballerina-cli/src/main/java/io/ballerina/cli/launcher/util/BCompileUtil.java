/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.launcher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility methods for compile Ballerina files.
 *
 * @since 0.94
 */
public final class BCompileUtil {

    private BCompileUtil() {
    }

    /**
     * Reads the content of a file from the system class loader.
     *
     * @param path        the path to the file
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String readFileAsString(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(ClassLoader.getSystemResourceAsStream(path), StandardCharsets.UTF_8))) {
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }

            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append('\n').append(content);
            }
        }
        return sb.toString();
    }

    /**
     * Reads the content of a file as a string from the specified class loader.
     *
     * @param path        the path to the file
     * @param classLoader the class loader to use for loading the file
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs
     */
    public static String readFileAsString(String path, ClassLoader classLoader) throws IOException {
        try (InputStream inputStream = classLoader.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

}
