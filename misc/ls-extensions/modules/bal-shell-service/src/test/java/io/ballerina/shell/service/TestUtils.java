/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.shell.service;

import com.google.gson.Gson;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility functions for tests.
 *
 * @since 2.0.0
 */
public class TestUtils {
    public static GetResultTestCase[] loadResultTestCases(Path filePath) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(readFile(filePath), GetResultTestCase[].class);
    }

    public static GetVariableTestCase[] loadVariableTestCases(Path filePath) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(readFile(filePath), GetVariableTestCase[].class);
    }

    public static String readFile(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    public static void assertJsonValues(Object generated, Object expected) {
        Gson gson = new Gson();
        String jsonGenerated = gson.toJson(generated)
                .replace("\\r\\n", "\\n");
        String jsonExpected = gson.toJson(expected);
        Assert.assertEquals(jsonGenerated, jsonExpected);
    }
}
