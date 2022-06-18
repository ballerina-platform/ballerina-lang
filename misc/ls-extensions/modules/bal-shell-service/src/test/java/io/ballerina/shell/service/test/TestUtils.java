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
package io.ballerina.shell.service.test;

import com.google.gson.Gson;
import io.ballerina.shell.service.test.getresult.GetResultTestCase;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility functions for tests.
 *
 * @since 2201.1.1
 */
public class TestUtils {

    /**
     * Loads GetResultsTestCases from a given file path.
     *
     * @param filePath Path of the source file
     * @return GetResultsTestCase list
     * @throws IOException if an I/O error occurs while reading from the file
     */
    public static GetResultTestCase[] loadResultTestCases(Path filePath) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(readFile(filePath), GetResultTestCase[].class);
    }

    /**
     * Loads GetVariableTestCase from a given file path.
     *
     * @param filePath Path of the source file
     * @return GetVariableTestCase list
     * @throws IOException if an I/O error occurs while reading from the file
     */
    public static GetVariableTestCase[] loadVariableTestCases(Path filePath) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(readFile(filePath), GetVariableTestCase[].class);
    }

    /**
     * Read and return a given file specified by the path.
     *
     * @param filePath path for the file
     * @return content of the file
     * @throws IOException if an I/O error occurs while reading from the file
     */
    public static String readFile(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    /**
     * Assert given two objects are equal by comparing json string values for each.
     *
     * @param generated generated result
     * @param expected expected result
     */
    public static void assertJsonValues(Object generated, Object expected) {
        Gson gson = new Gson();
        String jsonGenerated = gson.toJson(generated).replace("\\r\\n", "\\n");
        String jsonExpected = gson.toJson(expected);
        Assert.assertEquals(jsonGenerated, jsonExpected);
    }

    private TestUtils() {
    }
}
