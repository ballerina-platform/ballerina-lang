/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.semver.checker.comparator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.ballerinalang.semver.checker.exception.SemverTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static org.ballerinalang.semver.checker.util.TestUtils.executeTestData;

/**
 * Function definition comparator related test cases.
 *
 * @since 2201.2.0
 */
public class FunctionComparatorTest {

    private static final String FUNCTION_TEST_DATA_ROOT = "src/test/resources/testcases/functionDefinition/";
    protected static final String FUNCTION_ANNOTATION_TESTCASE = FUNCTION_TEST_DATA_ROOT + "annotation.json";
    protected static final String FUNCTION_DOCUMENTATION_TESTCASE = FUNCTION_TEST_DATA_ROOT + "documentation.json";
    protected static final String FUNCTION_BODY_TESTCASE = FUNCTION_TEST_DATA_ROOT + "functionBody.json";
    protected static final String FUNCTION_IDENTIFIER_TESTCASE = FUNCTION_TEST_DATA_ROOT + "identifier.json";
    protected static final String FUNCTION_PARAMETER_TESTCASE = FUNCTION_TEST_DATA_ROOT + "parameter.json";
    protected static final String FUNCTION_QUALIFIER_TESTCASE = FUNCTION_TEST_DATA_ROOT + "qualifier.json";
    protected static final String FUNCTION_RETURN_TESTCASE = FUNCTION_TEST_DATA_ROOT + "returnType.json";
    protected static final String ADVANCE_FUNCTION_TESTCASE = FUNCTION_TEST_DATA_ROOT + "advanceFunction.json";

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionBody(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionParameter(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testFunctionReturn(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "functionTestDataProvider")
    public void testAdvanceFunction(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @DataProvider(name = "functionTestDataProvider")
    public Object[] functionTestDataProvider(Method method) throws SemverTestException {
        String filePath;
        switch (method.getName()) {
            case "testFunctionAnnotation":
                filePath = FUNCTION_ANNOTATION_TESTCASE;
                break;
            case "testFunctionDocumentation":
                filePath = FUNCTION_DOCUMENTATION_TESTCASE;
                break;
            case "testFunctionBody":
                filePath = FUNCTION_BODY_TESTCASE;
                break;
            case "testFunctionIdentifier":
                filePath = FUNCTION_IDENTIFIER_TESTCASE;
                break;
            case "testFunctionParameter":
                filePath = FUNCTION_PARAMETER_TESTCASE;
                break;
            case "testFunctionQualifier":
                filePath = FUNCTION_QUALIFIER_TESTCASE;
                break;
            case "testFunctionReturn":
                filePath = FUNCTION_RETURN_TESTCASE;
                break;
            case "testAdvanceFunction":
                filePath = ADVANCE_FUNCTION_TESTCASE;
                break;
            default:
                filePath = null;
        }

        if (filePath == null) {
            throw new SemverTestException("Failed to load dataset for method: " + method.getName());
        }
        try (FileReader reader = new FileReader(filePath)) {
            Object testCaseObject = JsonParser.parseReader(reader);
            JsonArray fileData = (JsonArray) testCaseObject;
            List<JsonElement> elementList = new LinkedList<>();
            fileData.forEach(elementList::add);
            return elementList.toArray();
        } catch (IOException e) {
            throw new SemverTestException("failed to load test data");
        }
    }
}
