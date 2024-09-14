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
 * Constant declaration comparator related test cases.
 *
 * @since 2201.2.0
 */
public class ConstantComparatorTest {
    private static final String CONSTANT_TEST_DATA_ROOT = "src/test/resources/testcases/constantDeclaration/";
    private static final String CONSTANT_ANNOTATION_TESTCASE = CONSTANT_TEST_DATA_ROOT + "annotation.json";
    private static final String CONSTANT_DOCUMENTATION_TESTCASE = CONSTANT_TEST_DATA_ROOT + "documentation.json";
    private static final String CONSTANT_EXPRESSION_TESTCASE = CONSTANT_TEST_DATA_ROOT + "constantExpression.json";
    private static final String CONSTANT_IDENTIFIER_TESTCASE = CONSTANT_TEST_DATA_ROOT + "identifier.json";
    private static final String CONSTANT_QUALIFIER_TESTCASE = CONSTANT_TEST_DATA_ROOT + "qualifier.json";
    private static final String CONSTANT_TYPE_DESCRIPTOR_TESTCASE = CONSTANT_TEST_DATA_ROOT + "typeDescriptor.json";
    private static final String ADVANCE_CONSTANT_TESTCASE = CONSTANT_TEST_DATA_ROOT + "advanceConstant.json";

    @Test(dataProvider = "constantTestDataProvider")
    public void testConstantAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "constantTestDataProvider")
    public void testConstantDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "constantTestDataProvider")
    public void testConstantExpression(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "constantTestDataProvider")
    public void testConstantIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "constantTestDataProvider")
    public void testConstantQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "constantTestDataProvider")
    public void testConstantTypeDescriptor(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "constantTestDataProvider")
    public void testAdvanceConstant(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @DataProvider(name = "constantTestDataProvider")
    public Object[] functionTestDataProvider(Method method) throws SemverTestException {
        String filePath = switch (method.getName()) {
            case "testConstantAnnotation" -> CONSTANT_ANNOTATION_TESTCASE;
            case "testConstantDocumentation" -> CONSTANT_DOCUMENTATION_TESTCASE;
            case "testConstantExpression" -> CONSTANT_EXPRESSION_TESTCASE;
            case "testConstantIdentifier" -> CONSTANT_IDENTIFIER_TESTCASE;
            case "testConstantQualifier" -> CONSTANT_QUALIFIER_TESTCASE;
            case "testConstantTypeDescriptor" -> CONSTANT_TYPE_DESCRIPTOR_TESTCASE;
            case "testAdvanceConstant" -> ADVANCE_CONSTANT_TESTCASE;
            default -> throw new SemverTestException("Failed to load dataset for method: " + method.getName());
        };

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
