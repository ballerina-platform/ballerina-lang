/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.utils;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Utilities related to files.
 *
 * @since 2.0.0
 */
public class FileUtils {
    /**
     * Get the name of the without the extension.
     *
     * @param filePath Path of the file.
     * @return File name without extension.
     */
    public static String getFileNameWithoutExtension(Path filePath) {
        Path fileName = filePath.getFileName();
        if (null != fileName) {
            int index = indexOfExtension(fileName.toString());
            return index == -1 ? fileName.toString() :
                   fileName.toString().substring(0, index);
        } else {
            return null;
        }
    }
    
    public static String getExtension(Path filePath) {
        Path fileName = filePath.getFileName();
        if (null == fileName) {
            return "";
        }
        Optional<String> extension = Optional.ofNullable(fileName.toString())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.toString().lastIndexOf(".") + 1));
        return extension.orElse("");
    }

    public static boolean hasExtension(Path filePath) {
        if (filePath.toFile().isDirectory()) {
            return false;
        }

        Path fileName = filePath.getFileName();
        if (null != fileName) {
            int index = indexOfExtension(fileName.toString());
            return index != -1;
        } else {
            return false;
        }
    }
    
    private static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }
    
    private static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }

    public static String readSchema(String toolName, ClassLoader classLoader) throws IOException {
        String schemaFilePath = toolName + "-options-schema.json";
        InputStream inputStream = classLoader.getResourceAsStream(schemaFilePath);

        if (inputStream == null) {
            throw new FileNotFoundException("Schema file for tool options inputStream not found: " + schemaFilePath);
        }
        StringBuilder sb = new StringBuilder();
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(inputStreamReader);
        ) {
            String content;
            while ((content = br.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(content);
            }
        }

        return sb.toString();
    }

    public static void validateToml(Toml optionsToml, String toolName, ClassLoader classLoader) throws IOException {
        Schema schema = Schema.from(FileUtils.readSchema(toolName, classLoader));
        TomlValidator tomlValidator = new TomlValidator(schema);
        tomlValidator.validate(optionsToml);
    }
}
