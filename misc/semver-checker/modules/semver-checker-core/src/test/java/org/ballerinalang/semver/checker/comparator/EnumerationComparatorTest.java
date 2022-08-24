/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 * Enumeration declaration comparator related test cases.
 *
 * @since 2201.2.0
 */
public class EnumerationComparatorTest {

    private static final String ENUMERATION_TEST_DATA_ROOT = "src/test/resources/testcases/enumerationDeclaration/";
    private static final String ENUMERATION_ANNOTATION_TESTCASE = ENUMERATION_TEST_DATA_ROOT + "annotation.json";
    private static final String ENUMERATION_DOCUMENTATION_TESTCASE = ENUMERATION_TEST_DATA_ROOT + "documentation.json";
    private static final String ENUMERATION_IDENTIFIER_TESTCASE = ENUMERATION_TEST_DATA_ROOT + "identifier.json";
    private static final String ENUMERATION_QUALIFIER_TESTCASE = ENUMERATION_TEST_DATA_ROOT + "qualifier.json";
    private static final String ENUMERATION_MEMBER_TESTCASE = ENUMERATION_TEST_DATA_ROOT + "enumMember.json";
    private static final String ADVANCE_ENUMERATION_TESTCASE = ENUMERATION_TEST_DATA_ROOT + "advanceEnumeration.json";

    @Test(dataProvider = "enumerationTestDataProvider")
    public void testEnumerationAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "enumerationTestDataProvider")
    public void testEnumerationDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "enumerationTestDataProvider")
    public void testEnumerationIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "enumerationTestDataProvider")
    public void testEnumerationMember(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "enumerationTestDataProvider")
    public void testEnumerationQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "enumerationTestDataProvider")
    public void testAdvanceEnumeration(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @DataProvider(name = "enumerationTestDataProvider")
    public Object[] enumerationTestDataProvider(Method method) throws SemverTestException {
        String filePath;
        switch (method.getName()) {
            case "testEnumerationAnnotation":
                filePath = ENUMERATION_ANNOTATION_TESTCASE;
                break;
            case "testEnumerationDocumentation":
                filePath = ENUMERATION_DOCUMENTATION_TESTCASE;
                break;
            case "testEnumerationIdentifier":
                filePath = ENUMERATION_IDENTIFIER_TESTCASE;
                break;
            case "testEnumerationQualifier":
                filePath = ENUMERATION_QUALIFIER_TESTCASE;
                break;
            case "testEnumerationMember":
                filePath = ENUMERATION_MEMBER_TESTCASE;
                break;
            case "testAdvanceEnumeration":
                filePath = ADVANCE_ENUMERATION_TESTCASE;
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
