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
 * Variable declaration comparator related test cases.
 *
 * @since 2201.2.0
 */
public class VariableComparatorTest {
    private static final String VARIABLE_TEST_DATA_ROOT = "src/test/resources/testcases/variableDeclaration/";
    private static final String VARIABLE_ANNOTATION_TESTCASE = VARIABLE_TEST_DATA_ROOT + "annotation.json";
    private static final String VARIABLE_DOCUMENTATION_TESTCASE = VARIABLE_TEST_DATA_ROOT + "documentation.json";
    private static final String VARIABLE_VALUE_TESTCASE = VARIABLE_TEST_DATA_ROOT + "value.json";
    private static final String VARIABLE_IDENTIFIER_TESTCASE = VARIABLE_TEST_DATA_ROOT + "identifier.json";
    private static final String VARIABLE_QUALIFIER_TESTCASE = VARIABLE_TEST_DATA_ROOT + "qualifier.json";
    private static final String VARIABLE_TYPE_DESCRIPTOR_TESTCASE = VARIABLE_TEST_DATA_ROOT + "typeDescriptor.json";
    private static final String ADVANCE_VARIABLE_TESTCASE = VARIABLE_TEST_DATA_ROOT + "advanceVariable.json";

    @Test(dataProvider = "variableTestDataProvider")
    public void testVariableAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "variableTestDataProvider")
    public void testVariableDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "variableTestDataProvider")
    public void testVariableValue(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "variableTestDataProvider")
    public void testVariableIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "variableTestDataProvider")
    public void testVariableQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "variableTestDataProvider")
    public void testVariableTypeDescriptor(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "variableTestDataProvider")
    public void testAdvanceVariable(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @DataProvider(name = "variableTestDataProvider")
    public Object[] functionTestDataProvider(Method method) throws SemverTestException {
        String filePath;
        switch (method.getName()) {
            case "testVariableAnnotation":
                filePath = VARIABLE_ANNOTATION_TESTCASE;
                break;
            case "testVariableDocumentation":
                filePath = VARIABLE_DOCUMENTATION_TESTCASE;
                break;
            case "testVariableValue":
                filePath = VARIABLE_VALUE_TESTCASE;
                break;
            case "testVariableIdentifier":
                filePath = VARIABLE_IDENTIFIER_TESTCASE;
                break;
            case "testVariableQualifier":
                filePath = VARIABLE_QUALIFIER_TESTCASE;
                break;
            case "testVariableTypeDescriptor":
                filePath = VARIABLE_TYPE_DESCRIPTOR_TESTCASE;
                break;
            case "testAdvanceVariable":
                filePath = ADVANCE_VARIABLE_TESTCASE;
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
