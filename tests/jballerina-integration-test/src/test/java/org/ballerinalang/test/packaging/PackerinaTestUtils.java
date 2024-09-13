/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.packaging;

import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Util methods needed for Packaging test cases.
 *
 * @since 0.982.0
 */
public class PackerinaTestUtils {

    /**
     * Delete files inside directories.
     *
     * @param dirPath directory path
     * @throws IOException throw an exception if an issue occurs
     */
    public static void deleteFiles(Path dirPath) throws IOException {
        if (dirPath == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });
        }
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    public static Map<String, String> getEnvVariables() {
        Map<String, String> envVarMap = System.getenv();
        Map<String, String> retMap = new HashMap<>();
        envVarMap.forEach(retMap::put);
        return retMap;
    }

    /**
     * Copy directory to target directory.
     *
     * @param src  source file
     * @param dest destination path to copy.
     * @throws IOException throw if there is any error occur while copying directories.
     */
    public static void copyFolder(Path src, Path dest) throws IOException {
        try (Stream<Path> paths = Files.walk(src)) {
            paths.forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        }
    }

    /**
     * Copy a file to a target file.
     *
     * @param src  source file
     * @param dest destination path to copy.
     */
    public static void copy(Path src, Path dest) {
        try {
            Files.copy(src, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
