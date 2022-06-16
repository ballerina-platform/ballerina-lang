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
    private static final String SERVICE_DECLARATION_TEST_DATA_ROOT  = "src/test/resources/testcases/serviceDeclaration/";
    private static final String SERVICE_MEMBERS_TEST_DATA_ROOT  = "src/test/resources/testcases/serviceDeclaration/serviceMembers/";
    private static final String SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT  = SERVICE_MEMBERS_TEST_DATA_ROOT + "methodDefinition/";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT  = SERVICE_MEMBERS_TEST_DATA_ROOT + "remoteMethodDefinition/";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT  = SERVICE_MEMBERS_TEST_DATA_ROOT + "resourceMethodDefinition/";
    private static final String SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  = SERVICE_MEMBERS_TEST_DATA_ROOT + "objectField/";

    private static final String SERVICE_DECLARATION_ANNOTATION_TESTCASE = SERVICE_DECLARATION_TEST_DATA_ROOT  + "annotation.json";
    private static final String SERVICE_DECLARATION_DOCUMENTATION_TESTCASE = SERVICE_DECLARATION_TEST_DATA_ROOT  +  "documentation.json";
    private static final String SERVICE_DECLARATION_ATTACH_POINT_TESTCASE = SERVICE_DECLARATION_TEST_DATA_ROOT  + "attachPoint.json";
    private static final String SERVICE_DECLARATION_ISOLATED_QUALIFIER_TESTCASE = SERVICE_DECLARATION_TEST_DATA_ROOT  + "isolatedQualifier.json";
    private static final String SERVICE_DECLARATION_LISTENER_EXPRESSION_LIST_TESTCASE = SERVICE_DECLARATION_TEST_DATA_ROOT  + "listenerExpressionList.json";
    private static final String ADVANCE_SERVICE_DECLARATION_TESTCASE = SERVICE_DECLARATION_TEST_DATA_ROOT  + "advanceServiceDeclaration.json";

    private static final String SERVICE_MEMBER_OBJECT_FIELD_ANNOTATION_TESTCASE = SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  + "annotation.json";
    private static final String SERVICE_MEMBER_OBJECT_FIELD_DOCUMENTATION_TESTCASE = SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  + "documentation.json";
    private static final String SERVICE_MEMBER_OBJECT_FIELD_IDENTIFIER_TESTCASE = SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  + "identifier.json";
    private static final String SERVICE_MEMBER_OBJECT_FIELD_QUALIFIER_TESTCASE = SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  + "qualifier.json";
    private static final String SERVICE_MEMBER_OBJECT_FIELD_TYPE_TESTCASE = SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  + "type.json";
    private static final String SERVICE_MEMBER_OBJECT_FIELD_VALUE_TESTCASE = SERVICE_MEMBER_OBJECT_FIELD_TEST_DATA_ROOT  + "value.json";

    private static final String SERVICE_METHOD_DEFINITION_ANNOTATION_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "annotation.json";
    private static final String SERVICE_METHOD_DEFINITION_DOCUMENTATION_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "documentation.json";
    private static final String SERVICE_METHOD_DEFINITION_BODY_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "body.json";
    private static final String SERVICE_METHOD_DEFINITION_IDENTIFIER_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "identifier.json";
    private static final String SERVICE_METHOD_DEFINITION_PARAMETER_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "parameter.json";
    private static final String SERVICE_METHOD_DEFINITION_QUALIFIER_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "qualifier.json";
    private static final String SERVICE_METHOD_DEFINITION_RETURN_TESTCASE = SERVICE_METHOD_DEFINITION_TEST_DATA_ROOT + "returnType.json";

    private static final String SERVICE_REMOTE_METHOD_DEFINITION_ANNOTATION_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "annotation.json";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_DOCUMENTATION_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "documentation.json";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_BODY_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "body.json";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_IDENTIFIER_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "identifier.json";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_PARAMETER_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "parameter.json";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_QUALIFIER_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "qualifier.json";
    private static final String SERVICE_REMOTE_METHOD_DEFINITION_RETURN_TESTCASE = SERVICE_REMOTE_METHOD_DEFINITION_TEST_DATA_ROOT + "returnType.json";

    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_ANNOTATION_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "annotation.json";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_DOCUMENTATION_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "documentation.json";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_BODY_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "body.json";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_IDENTIFIER_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "identifier.json";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_PARAMETER_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "parameter.json";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_QUALIFIER_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "qualifier.json";
    private static final String SERVICE_RESOURCE_METHOD_DEFINITION_RETURN_TESTCASE = SERVICE_RESOURCE_METHOD_DEFINITION_TEST_DATA_ROOT + "returnType.json";


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
    public void testServiceIsolatedQualifier(JsonElement testData) throws Exception {
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
        String filePath;
        switch (method.getName()) {
            case "testServiceAnnotation":
                filePath = SERVICE_DECLARATION_ANNOTATION_TESTCASE;
                break;
            case "testServiceDocumentation":
                filePath = SERVICE_DECLARATION_DOCUMENTATION_TESTCASE;
                break;
            case "testServiceAttachPoint":
                filePath = SERVICE_DECLARATION_ATTACH_POINT_TESTCASE;
                break;
            case "testServiceListenerExpressionList":
                filePath = SERVICE_DECLARATION_LISTENER_EXPRESSION_LIST_TESTCASE;
                break;
            case "testServiceIsolatedQualifier":
                filePath = SERVICE_DECLARATION_ISOLATED_QUALIFIER_TESTCASE;
                break;
            case "testAdvanceServiceDeclaration":
                filePath = ADVANCE_SERVICE_DECLARATION_TESTCASE;
                break;

            case "testServiceMemberObjectFieldAnnotation":
                filePath = SERVICE_MEMBER_OBJECT_FIELD_ANNOTATION_TESTCASE;
                break;
            case "testServiceMemberObjectFieldDocumentation":
                filePath = SERVICE_MEMBER_OBJECT_FIELD_DOCUMENTATION_TESTCASE;
                break;
            case "testServiceMemberObjectFieldIdentifier":
                filePath = SERVICE_MEMBER_OBJECT_FIELD_IDENTIFIER_TESTCASE;
                break;
            case "testServiceMemberObjectFieldQualifier":
                filePath = SERVICE_MEMBER_OBJECT_FIELD_QUALIFIER_TESTCASE;
                break;
            case "testServiceMemberObjectFieldType":
                filePath = SERVICE_MEMBER_OBJECT_FIELD_TYPE_TESTCASE;
                break;
            case "testServiceMemberObjectFieldValue":
                filePath = SERVICE_MEMBER_OBJECT_FIELD_VALUE_TESTCASE;
                break;

            case "testServiceMethodDocumentation":
                filePath = SERVICE_METHOD_DEFINITION_DOCUMENTATION_TESTCASE;
                break;
            case "testServiceMethodBody":
                filePath = SERVICE_METHOD_DEFINITION_BODY_TESTCASE;
                break;
            case "testServiceMethodIdentifier":
                filePath = SERVICE_METHOD_DEFINITION_IDENTIFIER_TESTCASE;
                break;
            case "testServiceMethodParameter":
                filePath = SERVICE_METHOD_DEFINITION_PARAMETER_TESTCASE;
                break;
            case "testServiceMethodQualifier":
                filePath = SERVICE_METHOD_DEFINITION_QUALIFIER_TESTCASE;
                break;
            case "testServiceMethodReturn":
                filePath = SERVICE_METHOD_DEFINITION_RETURN_TESTCASE;
                break;
            case "testServiceMethodAnnotation":
                filePath = SERVICE_METHOD_DEFINITION_ANNOTATION_TESTCASE;
                break;

            case "testRemoteServiceMethodDocumentation":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_DOCUMENTATION_TESTCASE;
                break;
            case "testRemoteServiceMethodBody":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_BODY_TESTCASE;
                break;
            case "testRemoteServiceMethodIdentifier":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_IDENTIFIER_TESTCASE;
                break;
            case "testRemoteServiceMethodParameter":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_PARAMETER_TESTCASE;
                break;
            case "testRemoteServiceMethodQualifier":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_QUALIFIER_TESTCASE;
                break;
            case "testRemoteServiceMethodReturn":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_RETURN_TESTCASE;
                break;
            case "testRemoteServiceMethodAnnotation":
                filePath = SERVICE_REMOTE_METHOD_DEFINITION_ANNOTATION_TESTCASE;
                break;

            case "testResourceServiceMethodDocumentation":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_DOCUMENTATION_TESTCASE;
                break;
            case "testResourceServiceMethodBody":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_BODY_TESTCASE;
                break;
            case "testResourceServiceMethodIdentifier":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_IDENTIFIER_TESTCASE;
                break;
            case "testResourceServiceMethodParameter":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_PARAMETER_TESTCASE;
                break;
            case "testResourceServiceMethodQualifier":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_QUALIFIER_TESTCASE;
                break;
            case "testResourceServiceMethodReturn":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_RETURN_TESTCASE;
                break;
            case "testResourceServiceMethodAnnotation":
                filePath = SERVICE_RESOURCE_METHOD_DEFINITION_ANNOTATION_TESTCASE;
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
