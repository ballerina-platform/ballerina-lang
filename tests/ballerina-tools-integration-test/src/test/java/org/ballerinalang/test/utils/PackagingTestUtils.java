/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.utils;

import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Util methods needed for Packaging test cases.
 *
 * @since 0.982.0
 */
public class PackagingTestUtils {

    /**
     * Delete files inside directories.
     *
     * @param dirPath directory path
     * @throws IOException throw an exception if an issue occurs
     */
    public static void deleteFiles(Path dirPath) throws IOException {
        Files.walk(dirPath)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
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
     * Generate random module name.
     *
     * @param count number of characters required
     * @return generated name
     */
    public static String randomModuleName(int count) {
        String upperCaseAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseAlpha = "abcdefghijklmnopqrstuvwxyz";
        String alpha = upperCaseAlpha + lowerCaseAlpha;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * alpha.length());
            builder.append(alpha.charAt(character));
        }
        return builder.toString();
    }
}
