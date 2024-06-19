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
 * Service declaration comparator related test cases.
 *
 * @since 2201.2.0
 */
public class ServiceComparatorTest {

    private static final String TEST_ROOT = "src/test/resources/testcases/serviceDeclaration/";
    private static final String MEMBER_TEST_ROOT = TEST_ROOT + "serviceMembers/";
    private static final String METHOD_TEST_ROOT = MEMBER_TEST_ROOT + "method/";
    private static final String REMOTE_METHOD_TEST_ROOT = MEMBER_TEST_ROOT + "remoteMethod/";
    private static final String RESOURCE_METHOD_TEST_ROOT = MEMBER_TEST_ROOT + "resourceMethod/";
    private static final String OBJECT_FIELD_TEST_ROOT = MEMBER_TEST_ROOT + "objectField/";

    private static final String SERVICE_ANNOTATION_TESTCASE = TEST_ROOT + "annotation.json";
    private static final String SERVICE_DOCUMENTATION_TESTCASE = TEST_ROOT + "documentation.json";
    private static final String SERVICE_ATTACH_POINT_TESTCASE = TEST_ROOT + "attachPoint.json";
    private static final String SERVICE_ISOLATED_QUALIFIER_TESTCASE = TEST_ROOT + "qualifiers.json";
    private static final String SERVICE_LISTENER_TESTCASE = TEST_ROOT + "listenerExpressionList.json";
    private static final String ADVANCE_SERVICE_TESTCASE = TEST_ROOT + "advanceServiceDeclaration.json";

    private static final String OBJECT_FIELD_ANNOTATION_TESTCASE = OBJECT_FIELD_TEST_ROOT + "annotation.json";
    private static final String OBJECT_FIELD_DOCUMENTATION_TESTCASE = OBJECT_FIELD_TEST_ROOT + "documentation.json";
    private static final String OBJECT_FIELD_IDENTIFIER_TESTCASE = OBJECT_FIELD_TEST_ROOT + "identifier.json";
    private static final String OBJECT_FIELD_QUALIFIER_TESTCASE = OBJECT_FIELD_TEST_ROOT + "qualifier.json";
    private static final String OBJECT_FIELD_TYPE_TESTCASE = OBJECT_FIELD_TEST_ROOT + "type.json";
    private static final String OBJECT_FIELD_VALUE_TESTCASE = OBJECT_FIELD_TEST_ROOT + "value.json";

    private static final String METHOD_ANNOTATION_TESTCASE = METHOD_TEST_ROOT + "annotation.json";
    private static final String METHOD_DOCUMENTATION_TESTCASE = METHOD_TEST_ROOT + "documentation.json";
    private static final String METHOD_BODY_TESTCASE = METHOD_TEST_ROOT + "body.json";
    private static final String METHOD_IDENTIFIER_TESTCASE = METHOD_TEST_ROOT + "identifier.json";
    private static final String METHOD_PARAMETER_TESTCASE = METHOD_TEST_ROOT + "parameter.json";
    private static final String METHOD_QUALIFIER_TESTCASE = METHOD_TEST_ROOT + "qualifier.json";
    private static final String METHOD_RETURN_TESTCASE = METHOD_TEST_ROOT + "returnType.json";

    private static final String REMOTE_METHOD_ANNOTATION_TESTCASE = REMOTE_METHOD_TEST_ROOT + "annotation.json";
    private static final String REMOTE_METHOD_DOCUMENTATION_TESTCASE = REMOTE_METHOD_TEST_ROOT + "documentation.json";
    private static final String REMOTE_METHOD_BODY_TESTCASE = REMOTE_METHOD_TEST_ROOT + "body.json";
    private static final String REMOTE_METHOD_IDENTIFIER_TESTCASE = REMOTE_METHOD_TEST_ROOT + "identifier.json";
    private static final String REMOTE_METHOD_PARAMETER_TESTCASE = REMOTE_METHOD_TEST_ROOT + "parameter.json";
    private static final String REMOTE_METHOD_QUALIFIER_TESTCASE = REMOTE_METHOD_TEST_ROOT + "qualifier.json";
    private static final String REMOTE_METHOD_RETURN_TESTCASE = REMOTE_METHOD_TEST_ROOT + "returnType.json";

    private static final String RESOURCE_METHOD_ANNOTATION_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "annotation.json";
    private static final String RESOURCE_METHOD_DOC_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "documentation.json";
    private static final String RESOURCE_METHOD_BODY_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "body.json";
    private static final String RESOURCE_METHOD_IDENTIFIER_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "identifier.json";
    private static final String RESOURCE_METHOD_PARAMETER_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "parameter.json";
    private static final String RESOURCE_METHOD_QUALIFIER_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "qualifier.json";
    private static final String RESOURCE_METHOD_RETURN_TESTCASE = RESOURCE_METHOD_TEST_ROOT + "returnType.json";

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceAttachPoint(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceListenerExpressionList(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testAdvanceServiceDeclaration(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMemberObjectFieldAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMemberObjectFieldDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMemberObjectFieldIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMemberObjectFieldQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMemberObjectFieldType(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMemberObjectFieldValue(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodBody(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodParameter(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testServiceMethodReturn(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodBody(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodParameter(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testRemoteServiceMethodReturn(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodBody(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodParameter(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "serviceTestDataProvider")
    public void testResourceServiceMethodReturn(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @DataProvider(name = "serviceTestDataProvider")
    public Object[] serviceTestDataProvider(Method method) throws SemverTestException {
        String filePath = switch (method.getName()) {
            case "testServiceAnnotation" -> SERVICE_ANNOTATION_TESTCASE;
            case "testServiceDocumentation" -> SERVICE_DOCUMENTATION_TESTCASE;
            case "testServiceAttachPoint" -> SERVICE_ATTACH_POINT_TESTCASE;
            case "testServiceListenerExpressionList" -> SERVICE_LISTENER_TESTCASE;
            case "testServiceQualifier" -> SERVICE_ISOLATED_QUALIFIER_TESTCASE;
            case "testAdvanceServiceDeclaration" -> ADVANCE_SERVICE_TESTCASE;
            case "testServiceMemberObjectFieldAnnotation" -> OBJECT_FIELD_ANNOTATION_TESTCASE;
            case "testServiceMemberObjectFieldDocumentation" -> OBJECT_FIELD_DOCUMENTATION_TESTCASE;
            case "testServiceMemberObjectFieldIdentifier" -> OBJECT_FIELD_IDENTIFIER_TESTCASE;
            case "testServiceMemberObjectFieldQualifier" -> OBJECT_FIELD_QUALIFIER_TESTCASE;
            case "testServiceMemberObjectFieldType" -> OBJECT_FIELD_TYPE_TESTCASE;
            case "testServiceMemberObjectFieldValue" -> OBJECT_FIELD_VALUE_TESTCASE;
            case "testServiceMethodDocumentation" -> METHOD_DOCUMENTATION_TESTCASE;
            case "testServiceMethodBody" -> METHOD_BODY_TESTCASE;
            case "testServiceMethodIdentifier" -> METHOD_IDENTIFIER_TESTCASE;
            case "testServiceMethodParameter" -> METHOD_PARAMETER_TESTCASE;
            case "testServiceMethodQualifier" -> METHOD_QUALIFIER_TESTCASE;
            case "testServiceMethodReturn" -> METHOD_RETURN_TESTCASE;
            case "testServiceMethodAnnotation" -> METHOD_ANNOTATION_TESTCASE;
            case "testRemoteServiceMethodDocumentation" -> REMOTE_METHOD_DOCUMENTATION_TESTCASE;
            case "testRemoteServiceMethodBody" -> REMOTE_METHOD_BODY_TESTCASE;
            case "testRemoteServiceMethodIdentifier" -> REMOTE_METHOD_IDENTIFIER_TESTCASE;
            case "testRemoteServiceMethodParameter" -> REMOTE_METHOD_PARAMETER_TESTCASE;
            case "testRemoteServiceMethodQualifier" -> REMOTE_METHOD_QUALIFIER_TESTCASE;
            case "testRemoteServiceMethodReturn" -> REMOTE_METHOD_RETURN_TESTCASE;
            case "testRemoteServiceMethodAnnotation" -> REMOTE_METHOD_ANNOTATION_TESTCASE;
            case "testResourceServiceMethodDocumentation" -> RESOURCE_METHOD_DOC_TESTCASE;
            case "testResourceServiceMethodBody" -> RESOURCE_METHOD_BODY_TESTCASE;
            case "testResourceServiceMethodIdentifier" -> RESOURCE_METHOD_IDENTIFIER_TESTCASE;
            case "testResourceServiceMethodParameter" -> RESOURCE_METHOD_PARAMETER_TESTCASE;
            case "testResourceServiceMethodQualifier" -> RESOURCE_METHOD_QUALIFIER_TESTCASE;
            case "testResourceServiceMethodReturn" -> RESOURCE_METHOD_RETURN_TESTCASE;
            case "testResourceServiceMethodAnnotation" -> RESOURCE_METHOD_ANNOTATION_TESTCASE;
            default -> null;
        };

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
