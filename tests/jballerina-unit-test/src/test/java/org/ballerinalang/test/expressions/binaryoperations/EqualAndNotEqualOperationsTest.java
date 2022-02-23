/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BNewArray;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Class to test functionality of "==" and "!=".
 *
 * @since 0.985.0
 */
public class EqualAndNotEqualOperationsTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/equal_and_not_equal_operation.bal");
        resultNegative = BCompileUtil.compile
                ("test-src/expressions/binaryoperations/equal_and_not_equal_operation_negative.bal");
    }

    @Test(description = "Test equals/unequals operation with simple values", dataProvider = "value-functions")
    public void testSimpleValueEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "value-functions")
    public Object[] getValueTestFunctions() {
        return new String[]{
                "checkBooleanEquality", "checkIntEquality", "checkByteEquality", "checkFloatEquality",
                "checkStringEquality", "checkEqualityToNil", "checkAnyDataEquality", "testIntByteEqualityPositive",
                "testIntByteEqualityNegative", "testIntersectingUnionEquality", "testTableEquality",
                "testEqualityWithNonAnydataType", "testEqualityByteWithIntSubTypes"
        };
    }

    @Test(description = "Test equals/unequals operation with record values", dataProvider = "record-equality-functions")
    public void testRecordValueEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "record-equality-functions")
    public Object[] getRecordTestFunctions() {
        return new String[]{
                "checkOpenRecordEqualityPositive", "checkOpenRecordEqualityNegative",
                "testOpenRecordWithOptionalFieldsEqualityPositive", "testOpenRecordWithOptionalFieldsEqualityNegative",
                "testClosedRecordWithOptionalFieldsEqualityPositive",
                "testClosedRecordWithOptionalFieldsEqualityNegative", "checkClosedRecordEqualityPositive",
                "checkClosedRecordEqualityNegative"
        };
    }

    @Test(description = "Test equals/unequals operation with two equal arrays", dataProvider = "equalArrayValues")
    public void test1DArrayEqualityPositive(BNewArray i, BNewArray j) {
        BValue[] args = {i, j};
        BRunUtil.invoke(result, "check1DArrayEqualityPositive", args);
    }

    @Test(description = "Test equals/unequals operation with two unequal arrays", dataProvider =
            "unequalArrayValues")
    public void test1DArrayEqualityNegative(BValue i, BValue j) {
        BValue[] args = {i, j};
        BRunUtil.invoke(result, "check1DArrayEqualityNegative", args);
    }

    @Test(description = "Test equals/unequals operation with array and tuple values", dataProvider = "array-equality" +
            "-functions")
    public void testArrayValueEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "array-equality-functions")
    public Object[] getArrayTestFunctions() {
        return new String[]{
                "check1DClosedArrayEqualityPositive", "check1DClosedArrayEqualityNegative",
                "check1DAnyArrayEqualityPositive", "check1DAnyArrayEqualityNegative",
                "checkOpenClosedArrayEqualityPositive", "checkOpenClosedArrayEqualityNegative",
                "check2DBooleanArrayEqualityPositive", "check2DBooleanArrayEqualityNegative",
                "check2DIntArrayEqualityPositive", "check2DIntArrayEqualityNegative",
                "check2DByteArrayEqualityPositive", "check2DByteArrayEqualityNegative",
                "check2DFloatArrayEqualityPositive", "check2DFloatArrayEqualityNegative",
                "check2DStringArrayEqualityPositive", "check2DStringArrayEqualityNegative",
                "checkComplex2DArrayEqualityPositive", "checkComplex2DArrayEqualityNegative",
                "checkTupleEqualityPositive" , "checkTupleEqualityNegative", "checkUnionArrayPositive",
                "checkUnionArrayNegative", "checkTupleWithUnionPositive", "checkTupleWithUnionNegative",
                "testArrayTupleEqualityPositive", "testArrayTupleEqualityNegative"
        };
    }


    @Test(description = "Test equals/unequals operation with map values", dataProvider = "map-equality-functions")
    public void testMapValueEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "map-equality-functions")
    public Object[] getMapTestFunctions() {
        return new String[]{
                "checkMapEqualityPositive", "checkMapEqualityNegative", "checkComplexMapEqualityPositive",
                "checkComplexMapEqualityNegative", "checkUnionConstrainedMapsPositive",
                "checkUnionConstrainedMapsNegative", "testEmptyMapAndRecordEquality"
        };
    }

    @Test(description = "Test equals/unequals operation with json values", dataProvider = "json-equality-functions")
    public void testJsonValueEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "json-equality-functions")
    public Object[] getJsonTestFunctions() {
        return new String[]{
                "checkJsonEquality", "testPrimitiveAndJsonEqualityPositive", "testPrimitiveAndJsonEqualityNegative",
                "testJsonRecordMapEqualityNegative", "testJsonRecordMapEqualityPositive",
                "testTupleJSONEquality"
        };
    }

    @Test(description = "Test equals/unequals operation with two equal json arrays", dataProvider = "equalArrayValues")
    public void test1DJsonArrayEqualityPositive(BNewArray i, BNewArray j) {
        BValue[] args = {i, j};
        BRunUtil.invoke(result, "checkJsonEqualityPositive", args);
}

    @Test(description = "Test equals/unequals operation with two unequal json arrays", dataProvider =
            "unequalArrayValues")
    public void test1DJsonArrayEqualityNegative(BValue i, BValue j) {
        BValue[] args = {i, j};
        BRunUtil.invoke(result, "checkJsonEqualityNegative", args);
    }

    @Test(description = "Test equals/unequals operation with two json objects")
    public void testJsonObjectEqualityPositive() {
        BRefType jsonVal = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");
        BRefType jsonValTwo = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\"}");
        BRunUtil.invoke(result, "checkJsonEqualityPositive", new BValue[]{jsonVal, jsonValTwo});

        jsonValTwo = JsonParser.parse("{\"helloTwo\": \"worldTwo\", \"hello\": \"world\"}");
        BRunUtil.invoke(result, "checkJsonEqualityPositive", new BValue[]{jsonVal, jsonValTwo});

        jsonValTwo = JsonParser.parse("{\"hello\": \"world\"}");
        BRunUtil.invoke(result, "checkJsonEqualityNegative", new BValue[]{jsonVal, jsonValTwo});

        jsonValTwo = JsonParser.parse("{\"hello\": \"world\", \"helloTwo\": \"worldTwo\", \"helloThree\": " +
                                              "\"worldThree\"}");
        BRunUtil.invoke(result, "checkJsonEqualityNegative", new BValue[]{jsonVal, jsonValTwo});
    }

    @Test(description = "Test equals/unequals operation with xml values", dataProvider = "xml-equality-functions")
    public void testXmlValueEquality(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "xml-equality-functions")
    public Object[] getXmlTestFunctions() {
        return new String[]{
                "testSimpleXmlPositive", "testXmlStringNegative", "testReferenceEqualityXml",
                "testXmlNeverAndXmlSequenceEquality", "testSimpleXmlNegative",
                "testEqualNestedXml", "testUnequalNestedXml", "testEqualXmlWithComments",
                "testUnequalXmlWithUnequalComment", "testEqualXmlIgnoringAttributeOrder",
                "testUnequalXmlIgnoringAttributeOrder", "testEqualXmlWithPI", "testUnequalXmlWithUnequalPI",
                "testUnequalXmlWithPIInWrongOrder", "testUnequalXmlWithMultiplePIInWrongOrder",
                "testUnequalXmlWithMissingPI", "testXmlWithNamespacesPositive", "testXmlWithNamespacesNegative",
                "testXmlSequenceAndXmlItemEqualityPositive", "testXmlSequenceAndXmlItemEqualityNegative"
        };
    }

    @Test(dataProvider = "selfAndCyclicReferencingFunctions")
    public void selfAndCyclicReferencingFunctions(String testFunctionName) {
        BRunUtil.invoke(result, testFunctionName);
    }

    @Test(description = "Test equal and not equal with errors")
    public void testEqualAndNotEqualNegativeCases() {
        int i = 0;
        validateError(resultNegative, i++, "operator '==' not defined for 'int' and 'string'", 20, 12);
        validateError(resultNegative, i++, "operator '!=' not defined for 'int' and 'string'", 20, 24);
        validateError(resultNegative, i++, "operator '==' not defined for 'int[2]' and 'string[2]'", 26, 21);
        validateError(resultNegative, i++, "operator '!=' not defined for 'int[2]' and 'string[2]'", 26, 33);
        validateError(resultNegative, i++, "operator '==' not defined for 'map<int>' and 'map<float>'", 38, 21);
        validateError(resultNegative, i++, "operator '!=' not defined for 'map<int>' and 'map<float>'", 38, 33);
        validateError(resultNegative, i++, "operator '==' not defined for 'map<(string|int)>' and 'map<float>'",
                      42, 21);
        validateError(resultNegative, i++, "operator '!=' not defined for 'map<(string|int)>' and 'map<float>'",
                      42, 33);
        validateError(resultNegative, i++, "operator '==' not defined for '[string,int]' and '[boolean,float]'",
                      50, 21);
        validateError(resultNegative, i++, "operator '!=' not defined for '[string,int]' and '[boolean,float]'",
                      50, 33);
        validateError(resultNegative, i++, "operator '==' not defined for '[(float|int),int]' and '[boolean,int]'",
                      54, 21);
        validateError(resultNegative, i++, "operator '!=' not defined for '[(float|int),int]' and '[boolean,int]'",
                      54, 33);
        validateError(resultNegative, i++, "operator '==' not defined for 'Employee' and 'Person'", 62, 17);
        validateError(resultNegative, i++, "operator '!=' not defined for 'Employee' and 'Person'", 62, 29);
        validateError(resultNegative, i++, "operator '==' not defined for 'EmployeeWithOptionalId' and " +
                "'PersonWithOptionalId'", 66, 17);
        validateError(resultNegative, i++, "operator '!=' not defined for 'EmployeeWithOptionalId' and " +
                "'PersonWithOptionalId'", 66, 31);
        validateError(resultNegative, i++, "operator '==' not defined for 'map<boolean>' and 'ClosedDept'", 75, 23);
        validateError(resultNegative, i++, "operator '!=' not defined for 'ClosedDept' and 'map<boolean>'", 75, 35);
        validateError(resultNegative, i++, "operator '==' not defined for 'int[]' and '[float,float]'", 82, 23);
        validateError(resultNegative, i++, "operator '!=' not defined for 'int[]' and '[float,float]'", 82, 35);
        validateError(resultNegative, i++, "operator '==' not defined for 'int[]' and '[int,float]'", 85, 23);
        validateError(resultNegative, i++, "operator '!=' not defined for '[int,float]' and 'int[]'", 85, 35);
        validateError(resultNegative, i++, "operator '==' not defined for 'Employee' and '()'", 138, 9);
        validateError(resultNegative, i++, "operator '==' not defined for 'Foo' and '()'", 144, 9);
        validateError(resultNegative, i++, "operator '==' not defined for 'function () returns (string)' and '()'",
                      150, 9);
        validateError(resultNegative, i++, "operator '!=' not defined for 'readonly' and 'map<int>'",
                168, 12);
        validateError(resultNegative, i++, "operator '==' not defined for '[int,map<int>]' and '[int,float]'", 179,
                23);
        validateError(resultNegative, i++, "operator '!=' not defined for '[int,float]' and '[int,map<int>]'", 179,
                35);
        validateError(resultNegative, i++, "operator '==' not defined for 'MyObject' and '()'", 182,
                15);
        validateError(resultNegative, i++, "operator '!=' not defined for 'MyObject' and '()'", 182,
                30);
        validateError(resultNegative, i++, "operator '==' not defined for 'MyObject' and 'MyObject'", 184,
                15);
        validateError(resultNegative, i++, "operator '!=' not defined for 'MyObject' and 'MyObject'", 184,
                32);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @DataProvider(name = "equalArrayValues")
    public Object[][] equalArrayValues() {
        return new Object[][]{
                {new BValueArray(new long[]{1, 2, 3}), new BValueArray(new long[]{1, 2, 3})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0}), new BValueArray(new double[]{1.11, 12.2, 3.0})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""})},
                {new BValueArray(new int[]{0, 1}), new BValueArray(new int[]{0, 1})},
                {new BValueArray(new byte[]{0, 25, 23}), new BValueArray(new byte[]{0, 25, 23})}
        };
    }

    @DataProvider(name = "unequalArrayValues")
    public Object[][] unequalArrayValues() {
        return new Object[][]{
                {new BValueArray(new long[]{1, 2, 3}), new BValueArray(new long[]{3, 2, 1})},
                {new BValueArray(new long[]{1, 2, 3, 4, 5, 6}), new BValueArray(new long[]{1, 2, 3})},
                {new BValueArray(new long[]{1, 2, 3}), new BValueArray(new long[]{1, 2, 3, 4, 5, 6})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0}), new BValueArray(new double[]{3.0, 12.2, 1.11})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0, 3.2}), new BValueArray(new double[]{1.11, 12.2, 3.0})},
                {new BValueArray(new double[]{1.11, 12.2, 3.0}), new BValueArray(new double[]{1.11, 12.2, 3.0, 3.2})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BValueArray(new String[]{"\"ballerina\"", "\"from\"", "\"hi\""})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\"", "\"!\""}),
                        new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""})},
                {new BValueArray(new String[]{"\"hi\"", "\"from\"", "\"ballerina\""}),
                        new BValueArray(new String[]{"\"first\"", "\"hi\"", "\"from\"", "\"ballerina\""})},
                {new BValueArray(new int[]{0, 1}), new BValueArray(new int[]{1, 0})},
                {new BValueArray(new int[]{0, 1, 1}), new BValueArray(new int[]{0, 1})},
                {new BValueArray(new int[]{0, 1}), new BValueArray(new int[]{0, 1, 0})},
                {new BValueArray(new byte[]{0, 123, 22}), new BValueArray(new byte[]{0, 22, 123})},
                {new BValueArray(new byte[]{0, 123, 22, 9}), new BValueArray(new byte[]{0, 123, 22})},
                {new BValueArray(new byte[]{0, 123, 22}), new BValueArray(new byte[]{0, 123})}
        };
    }

    @DataProvider(name = "selfAndCyclicReferencingFunctions")
    public Object[] selfAndCyclicReferencingFunctions() {
        return new String[]{
                "testSelfAndCyclicReferencingMapEqualityPositive",
                "testSelfAndCyclicReferencingJsonEqualityPositive",
                "testSelfAndCyclicReferencingArrayEqualityPositive",
                "testSelfAndCyclicReferencingTupleEqualityPositive",
                "testSelfAndCyclicReferencingMapEqualityNegative",
                "testSelfAndCyclicReferencingJsonEqualityNegative",
                "testSelfAndCyclicReferencingArrayEqualityNegative",
                "testSelfAndCyclicReferencingTupleEqualityNegative"
        };
    }

    @Test(dataProvider = "functionsWithUnionEqualityChecks")
    public void testFunctionsWithUnionEqualityChecks(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider
    public  Object[] functionsWithUnionEqualityChecks() {
        return new String[] {
                "testEqualityWithFloatUnion",
                "testNotEqualityWithFloatUnion",
                "testExactEqualityWithFloatUnion",
                "testNotExactEqualityWithFloatUnion",
                "testEqualityWithDecimalUnion",
                "testNotEqualityWithDecimalUnion",
                "testExactEqualityWithDecimalUnion",
                "testNotExactEqualityWithDecimalUnion",
                "testEqualityWithUnionOfSimpleTypes",
                "testExactEqualityWithUnionOfNonSimpleTypes"
        };
    }
}
