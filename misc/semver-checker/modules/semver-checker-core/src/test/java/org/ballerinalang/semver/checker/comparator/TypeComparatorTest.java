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
 * Type definition comparator related test cases.
 *
 * @since 2201.2.0
 */
public class TypeComparatorTest {

    private static final String TEST_ROOT = "src/test/resources/testcases/typeDefinition/";
    private static final String TYPE_TEST_ROOT = TEST_ROOT + "typeDescriptor/";
    private static final String BEHAVIOURAL_TEST_ROOT = TYPE_TEST_ROOT + "behaviouralTypeDescriptor/";
    private static final String SEQUENCE_TEST_ROOT = TYPE_TEST_ROOT + "sequenceTypeDescriptor/";
    private static final String SIMPLE_TEST_ROOT = TYPE_TEST_ROOT + "simpleTypeDescriptor/";
    private static final String STRUCTURAL_TEST_ROOT = TYPE_TEST_ROOT + "structuredTypeDescriptor/";
    private static final String OTHER_TEST_ROOT = TYPE_TEST_ROOT + "otherTypeDescriptor/";
    private static final String BEHAVIOURAL_FUNCTION_TEST_ROOT = BEHAVIOURAL_TEST_ROOT + "function/";
    private static final String BEHAVIOURAL_OBJ_TEST_ROOT = BEHAVIOURAL_TEST_ROOT + "object/";
    private static final String BEHAVIOURAL_OBJ_METHOD_TEST_ROOT = BEHAVIOURAL_OBJ_TEST_ROOT + "methodDefinition/";
    private static final String BEHAVIOURAL_OBJ_FIELD_TEST_ROOT = BEHAVIOURAL_OBJ_TEST_ROOT + "objectField/";

    private static final String TYPE_ANNOTATION_TESTCASE = TEST_ROOT + "annotation.json";
    private static final String TYPE_DOCUMENTATION_TESTCASE = TEST_ROOT + "documentation.json";
    private static final String TYPE_IDENTIFIER_TESTCASE = TEST_ROOT + "identifier.json";
    private static final String TYPE_QUALIFIER_TESTCASE = TEST_ROOT + "qualifier.json";
    private static final String ADVANCE_TESTCASE = TEST_ROOT + "advanceType.json";

    private static final String SIMPLE_BOOLEAN_TESTCASE = SIMPLE_TEST_ROOT + "boolean.json";
    private static final String SIMPLE_FLOAT_TESTCASE = SIMPLE_TEST_ROOT + "float.json";
    private static final String SIMPLE_INT_TESTCASE = SIMPLE_TEST_ROOT + "int.json";
    private static final String SIMPLE_NIL_TESTCASE = SIMPLE_TEST_ROOT + "nil.json";

    private static final String SEQUENCE_STRING_TESTCASE = SEQUENCE_TEST_ROOT + "string.json";
    private static final String SEQUENCE_XML_TESTCASE = SEQUENCE_TEST_ROOT + "xml.json";

    private static final String STRUCTURED_LIST_TESTCASE = STRUCTURAL_TEST_ROOT + "list.json";
    private static final String STRUCTURED_MAP_TESTCASE = STRUCTURAL_TEST_ROOT + "map.json";
    private static final String STRUCTURED_TABLE_TESTCASE = STRUCTURAL_TEST_ROOT + "table.json";

    private static final String BEHAVIOURAL_ERROR_TESTCASE = BEHAVIOURAL_TEST_ROOT + "error.json";
    private static final String BEHAVIOURAL_FUTURE_TESTCASE = BEHAVIOURAL_TEST_ROOT + "future.json";
    private static final String BEHAVIOURAL_HANDLE_TESTCASE = BEHAVIOURAL_TEST_ROOT + "handle.json";
    private static final String BEHAVIOURAL_STREAM_TESTCASE = BEHAVIOURAL_TEST_ROOT + "stream.json";
    private static final String BEHAVIOURAL_TYPE_DESC_TESTCASE = BEHAVIOURAL_TEST_ROOT + "typeDesc.json";

    private static final String BEHAVIOURAL_FUNCTION_PARAMETER_TESTCASE = BEHAVIOURAL_FUNCTION_TEST_ROOT
            + "parameter.json";
    private static final String BEHAVIOURAL_FUNCTION_QUALIFIER_TESTCASE = BEHAVIOURAL_FUNCTION_TEST_ROOT
            + "qualifier.json";
    private static final String BEHAVIOURAL_FUNCTION_RETURN_TESTCASE = BEHAVIOURAL_FUNCTION_TEST_ROOT
            + "returnType.json";

    private static final String BEHAVIOURAL_OBJ_METHOD_ANNOTATION_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "annotation.json";
    private static final String BEHAVIOURAL_OBJ_METHOD_DOCUMENTATION_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "documentation.json";
    private static final String BEHAVIOURAL_OBJ_METHOD_BODY_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "body.json";
    private static final String BEHAVIOURAL_OBJ_METHOD_IDENTIFIER_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "identifier.json";
    private static final String BEHAVIOURAL_OBJ_METHOD_PARAMETER_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "parameter.json";
    private static final String BEHAVIOURAL_OBJ_METHOD_QUALIFIER_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "qualifier.json";
    private static final String BEHAVIOURAL_OBJ_METHOD_RETURN_TESTCASE = BEHAVIOURAL_OBJ_METHOD_TEST_ROOT +
            "returnType.json";

    private static final String BEHAVIOURAL_OBJ_FIELD_ANNOTATION_TESTCASE = BEHAVIOURAL_OBJ_FIELD_TEST_ROOT +
            "annotation.json";
    private static final String BEHAVIOURAL_OBJ_FIELD_DOCUMENTATION_TESTCASE = BEHAVIOURAL_OBJ_FIELD_TEST_ROOT +
            "documentation.json";
    private static final String BEHAVIOURAL_OBJ_FIELD_IDENTIFIER_TESTCASE = BEHAVIOURAL_OBJ_FIELD_TEST_ROOT +
            "identifier.json";
    private static final String BEHAVIOURAL_OBJ_FIELD_QUALIFIER_TESTCASE = BEHAVIOURAL_OBJ_FIELD_TEST_ROOT +
            "qualifier.json";
    private static final String BEHAVIOURAL_OBJ_FIELD_TESTCASE = BEHAVIOURAL_OBJ_FIELD_TEST_ROOT + "type.json";

    private static final String BEHAVIOURAL_OBJ_QUALIFIER_TESTCASE = BEHAVIOURAL_OBJ_TEST_ROOT + "qualifier.json";

    private static final String OTHER_ANY_DATA_TESTCASE = OTHER_TEST_ROOT + "anyData.json";
    private static final String OTHER_ANY_TESTCASE = OTHER_TEST_ROOT + "anyType.json";
    private static final String OTHER_BYTE_TESTCASE = OTHER_TEST_ROOT + "byte.json";
    private static final String OTHER_DISTINCT_TESTCASE = OTHER_TEST_ROOT + "distinct.json";
    private static final String OTHER_INTERSECTION_TESTCASE = OTHER_TEST_ROOT + "intersection.json";
    private static final String OTHER_JSON_TESTCASE = OTHER_TEST_ROOT + "jsonType.json";
    private static final String OTHER_NEVER_TESTCASE = OTHER_TEST_ROOT + "neverType.json";
    private static final String OTHER_OPTIONAL_TESTCASE = OTHER_TEST_ROOT + "optional.json";
    private static final String OTHER_READ_ONLY_TESTCASE = OTHER_TEST_ROOT + "readOnly.json";
    private static final String OTHER_RECORD_TESTCASE = OTHER_TEST_ROOT + "record.json";
    private static final String OTHER_SINGLETON_TESTCASE = OTHER_TEST_ROOT + "singleton.json";
    private static final String OTHER_TYPE_REFERENCE_TESTCASE = OTHER_TEST_ROOT + "typeReference.json";
    private static final String OTHER_UNION_TESTCASE = OTHER_TEST_ROOT + "union.json";

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testTypeAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testTypeDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testTypeIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testTypeQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testAdvanceType(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testSimpleTypeBoolean(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testSimpleTypeFloat(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testSimpleTypeInt(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testSimpleTypeNil(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testSequenceTypeString(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testSequenceTypeXml(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testStructuredTypeList(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testStructuredTypeMap(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testStructuredTypeTable(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeError(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeFuture(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeHandle(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeStream(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeTypeDesc(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeFunctionParameter(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeFunctionQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeFunctionReturn(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodBody(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodParameter(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectMethodReturn(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectFieldAnnotation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectFieldDocumentation(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectFieldIdentifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectFieldQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectFieldType(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testBehaviouralTypeObjectQualifier(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeAnyData(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeAnyType(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeByte(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeDistinct(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeIntersection(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeJsonType(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeNeverType(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeOptional(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeReadOnly(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeRecord(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeSingleton(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeTypeReference(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @Test(dataProvider = "typeDefinitionTestDataProvider")
    public void testOtherTypeUnion(JsonElement testData) throws Exception {
        executeTestData(testData);
    }

    @DataProvider(name = "typeDefinitionTestDataProvider")
    public Object[] typeDefinitionTestDataProvider(Method method) throws SemverTestException {
        String filePath;
        switch (method.getName()) {
            case "testTypeAnnotation":
                filePath = TYPE_ANNOTATION_TESTCASE;
                break;
            case "testTypeDocumentation":
                filePath = TYPE_DOCUMENTATION_TESTCASE;
                break;
            case "testTypeIdentifier":
                filePath = TYPE_IDENTIFIER_TESTCASE;
                break;
            case "testTypeQualifier":
                filePath = TYPE_QUALIFIER_TESTCASE;
                break;
            case "testAdvanceType":
                filePath = ADVANCE_TESTCASE;
                break;

            case "testSimpleTypeBoolean":
                filePath = SIMPLE_BOOLEAN_TESTCASE;
                break;
            case "testSimpleTypeFloat":
                filePath = SIMPLE_FLOAT_TESTCASE;
                break;
            case "testSimpleTypeInt":
                filePath = SIMPLE_INT_TESTCASE;
                break;
            case "testSimpleTypeNil":
                filePath = SIMPLE_NIL_TESTCASE;
                break;

            case "testSequenceTypeString":
                filePath = SEQUENCE_STRING_TESTCASE;
                break;
            case "testSequenceTypeXml":
                filePath = SEQUENCE_XML_TESTCASE;
                break;

            case "testStructuredTypeList":
                filePath = STRUCTURED_LIST_TESTCASE;
                break;
            case "testStructuredTypeMap":
                filePath = STRUCTURED_MAP_TESTCASE;
                break;
            case "testStructuredTypeTable":
                filePath = STRUCTURED_TABLE_TESTCASE;
                break;

            case "testBehaviouralTypeError":
                filePath = BEHAVIOURAL_ERROR_TESTCASE;
                break;
            case "testBehaviouralTypeFuture":
                filePath = BEHAVIOURAL_FUTURE_TESTCASE;
                break;
            case "testBehaviouralTypeHandle":
                filePath = BEHAVIOURAL_HANDLE_TESTCASE;
                break;
            case "testBehaviouralTypeStream":
                filePath = BEHAVIOURAL_STREAM_TESTCASE;
                break;
            case "testBehaviouralTypeTypeDesc":
                filePath = BEHAVIOURAL_TYPE_DESC_TESTCASE;
                break;

            case "testBehaviouralTypeFunctionParameter":
                filePath = BEHAVIOURAL_FUNCTION_PARAMETER_TESTCASE;
                break;
            case "testBehaviouralTypeFunctionQualifier":
                filePath = BEHAVIOURAL_FUNCTION_QUALIFIER_TESTCASE;
                break;
            case "testBehaviouralTypeFunctionReturn":
                filePath = BEHAVIOURAL_FUNCTION_RETURN_TESTCASE;
                break;

            case "testBehaviouralTypeObjectMethodAnnotation":
                filePath = BEHAVIOURAL_OBJ_METHOD_ANNOTATION_TESTCASE;
                break;
            case "testBehaviouralTypeObjectMethodDocumentation":
                filePath = BEHAVIOURAL_OBJ_METHOD_DOCUMENTATION_TESTCASE;
                break;
            case "testBehaviouralTypeObjectMethodBody":
                filePath = BEHAVIOURAL_OBJ_METHOD_BODY_TESTCASE;
                break;
            case "testBehaviouralTypeObjectMethodIdentifier":
                filePath = BEHAVIOURAL_OBJ_METHOD_IDENTIFIER_TESTCASE;
                break;
            case "testBehaviouralTypeObjectMethodParameter":
                filePath = BEHAVIOURAL_OBJ_METHOD_PARAMETER_TESTCASE;
                break;
            case "testBehaviouralTypeObjectMethodQualifier":
                filePath = BEHAVIOURAL_OBJ_METHOD_QUALIFIER_TESTCASE;
                break;
            case "testBehaviouralTypeObjectMethodReturn":
                filePath = BEHAVIOURAL_OBJ_METHOD_RETURN_TESTCASE;
                break;

            case "testBehaviouralTypeObjectFieldAnnotation":
                filePath = BEHAVIOURAL_OBJ_FIELD_ANNOTATION_TESTCASE;
                break;
            case "testBehaviouralTypeObjectFieldDocumentation":
                filePath = BEHAVIOURAL_OBJ_FIELD_DOCUMENTATION_TESTCASE;
                break;
            case "testBehaviouralTypeObjectFieldIdentifier":
                filePath = BEHAVIOURAL_OBJ_FIELD_IDENTIFIER_TESTCASE;
                break;
            case "testBehaviouralTypeObjectFieldQualifier":
                filePath = BEHAVIOURAL_OBJ_FIELD_QUALIFIER_TESTCASE;
                break;
            case "testBehaviouralTypeObjectFieldType":
                filePath = BEHAVIOURAL_OBJ_FIELD_TESTCASE;
                break;

            case "testBehaviouralTypeObjectQualifier":
                filePath = BEHAVIOURAL_OBJ_QUALIFIER_TESTCASE;
                break;

            case "testOtherTypeAnyData":
                filePath = OTHER_ANY_DATA_TESTCASE;
                break;
            case "testOtherTypeAnyType":
                filePath = OTHER_ANY_TESTCASE;
                break;
            case "testOtherTypeByte":
                filePath = OTHER_BYTE_TESTCASE;
                break;
            case "testOtherTypeDistinct":
                filePath = OTHER_DISTINCT_TESTCASE;
                break;
            case "testOtherTypeIntersection":
                filePath = OTHER_INTERSECTION_TESTCASE;
                break;
            case "testOtherTypeJsonType":
                filePath = OTHER_JSON_TESTCASE;
                break;
            case "testOtherTypeNeverType":
                filePath = OTHER_NEVER_TESTCASE;
                break;
            case "testOtherTypeOptional":
                filePath = OTHER_OPTIONAL_TESTCASE;
                break;
            case "testOtherTypeReadOnly":
                filePath = OTHER_READ_ONLY_TESTCASE;
                break;
            case "testOtherTypeRecord":
                filePath = OTHER_RECORD_TESTCASE;
                break;
            case "testOtherTypeSingleton":
                filePath = OTHER_SINGLETON_TESTCASE;
                break;
            case "testOtherTypeTypeReference":
                filePath = OTHER_TYPE_REFERENCE_TESTCASE;
                break;
            case "testOtherTypeUnion":
                filePath = OTHER_UNION_TESTCASE;
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
