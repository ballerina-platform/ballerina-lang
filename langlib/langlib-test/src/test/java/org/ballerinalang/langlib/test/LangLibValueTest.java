/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.test;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for value lib functions.
 *
 * @since 1.0
 */
@Test
public class LangLibValueTest {

    private CompileResult file;

    @BeforeClass
    public void setup() {

    }

    @AfterClass
    public void tearDown() {
        file = null;
    }

    @Test void testNegativeCases() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/valuelib_test_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: expected 'any', found " +
                "'ballerina/lang.value:0.0.0:Cloneable'", 22, 13);
        validateError(negativeResult, index++, "incompatible type for parameter 't' with inferred typedesc value: " +
                "expected 'typedesc<anydata>', found 'typedesc<MyClass>'", 31, 23);
        validateError(negativeResult, index++, "incompatible type for parameter 't' with inferred typedesc value: " +
                "expected 'typedesc<anydata>', found 'typedesc<MyClass>'", 32, 23);
        validateWarning(negativeResult, index++, "invalid usage of the 'check' expression operator: " +
                "no expression type is equivalent to error type", 41, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'anydata', " +
                "found 'table<RecordWithHandleField>'", 55, 13);
        assertEquals(negativeResult.getErrorCount(), index - 1);
        assertEquals(negativeResult.getWarnCount(), 1);
    }

    @Test
    public void testToBalStringMethod() {
        CompileResult testFile = BCompileUtil.compile("test-src/valuelib_toBalString_test.bal");
        BRunUtil.invoke(testFile, "testIntValueToBalString");
        BRunUtil.invoke(testFile, "testStringValueToBalString");
        BRunUtil.invoke(testFile, "testFloatingPointNumbersToBalString");
        BRunUtil.invoke(testFile, "testAnyAnydataNilToBalString");
        BRunUtil.invoke(testFile, "testTableToBalString");
        BRunUtil.invoke(testFile, "testErrorToBalString");
        BRunUtil.invoke(testFile, "testArrayToBalString");
        BRunUtil.invoke(testFile, "testTupleToBalString");
        BRunUtil.invoke(testFile, "testJsonToBalString");
        BRunUtil.invoke(testFile, "testXmlToBalString");
        BRunUtil.invoke(testFile, "testObjectToBalString");
        BRunUtil.invoke(testFile, "testToBalStringOnCycles");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithLiterals");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithEscapes");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCharacterClass");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCharacterClass2");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCapturingGroups");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCapturingGroups2");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCapturingGroups3");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCapturingGroups4");
        BRunUtil.invoke(testFile, "testToBalStringOnRegExpValueWithCapturingGroups5");
        BRunUtil.invoke(testFile, "testToBalStringOnComplexRegExpValue");
        BRunUtil.invoke(testFile, "testToBalStringComplexRegExpValue2");
    }

    @Test
    public void testFromBalString() {
        CompileResult file = BCompileUtil.compile("test-src/valuelib_fromBalString_test.bal");
        BRunUtil.invoke(file, "testIntValueFromBalString");
        BRunUtil.invoke(file, "testStringValueFromBalString");
        BRunUtil.invoke(file, "testFloatingPointNumbersFromBalString");
        BRunUtil.invoke(file, "testAnydataNilFromBalString");
        BRunUtil.invoke(file, "testMapFromBalString");
        BRunUtil.invoke(file, "testTableFromBalString");
        BRunUtil.invoke(file, "testArrayFromBalString");
        BRunUtil.invoke(file, "testTupleFromBalString");
        BRunUtil.invoke(file, "testJsonFromBalString");
        BRunUtil.invoke(file, "testXmlFromBalString");
        BRunUtil.invoke(file, "testObjectFromString");
        BRunUtil.invoke(file, "testFromBalStringOnCycles");
        BRunUtil.invoke(file, "testFromBalStringNegative");
        BRunUtil.invoke(file, "testFromStringOnRegExp");
        BRunUtil.invoke(file, "testFromStringOnRegExpNegative");
    }

    @DataProvider(name = "mergeJsonFunctions")
    public Object[][] mergeJsonFunctions() {
        return new Object[][] {
            { "testNilAndNonNilJsonMerge" },
            { "testNonNilNonMappingJsonMerge" },
            { "testMappingJsonAndNonMappingJsonMerge1" },
            { "testMappingJsonAndNonMappingJsonMerge2" },
            { "testMappingJsonNoIntersectionMergeSuccess" },
            { "testMappingJsonWithIntersectionMergeFailure1" },
            { "testMappingJsonWithIntersectionMergeFailure2" },
            { "testMappingJsonWithIntersectionMergeSuccess" },
            { "testMergeJsonSuccessForValuesWithNonIntersectingCyclicRefererences" },
            { "testMergeJsonFailureForValuesWithIntersectingCyclicRefererences" }
        };
    }

    @DataProvider(name = "cloneWithTypeToTupleTypeFunctions")
    public Object[][] cloneWithTypeToTupleTypeFunctions() {
        return new Object[][]{
                {"testCloneWithTypeArrayToTupleWithRestType"},
                {"testCloneWithTypeArrayToTupleWithRestTypeUnionType"},
                {"testCloneWithTypeArrayToUnionTupleNegative"},
                {"testCloneWithTypeArrayToTupleWithMoreTargetTypes"},
                {"testCloneWithTypeArrayToTupleWithUnionRestTypeNegative"},
                {"testCloneWithTypeArrayToTupleNegative"},
                {"testCloneWithTypeArrayToTupleWithStructureRestTypeNegative"},
                {"testCloneWithTypeTupleRestType"},
                {"testCloneWithTypeUnionTuple"},
                {"testCloneWithTypeTupleRestTypeNegative"},
                {"testCloneWithTypeUnionTupleRestTypeNegative"},
                {"testCloneWithTypeToTupleTypeWithFiniteTypesNegative"},
                {"testCloneWithTypeTupleConsideringFillerValues"},
                {"testCloneWithTypeConsideringReadOnlyFillerValues"}
        };
    }

    @Test(dataProvider = "fromJsonWithTypeFunctions")
    public void testFromJsonWithType(String function) {
        file = BCompileUtil.compile("test-src/valuelib_fromJson_test.bal");
        BRunUtil.invoke(file, function);
    }

    @DataProvider(name = "fromJsonWithTypeFunctions")
    public Object[][] fromJsonWithTypeFunctions() {
        return new Object[][] {
                { "testFromJsonWIthTypeNegative" },
                { "testFromJsonWithTypeRecord1" },
                { "testFromJsonWithTypeRecord2" },
                { "testFromJsonWithTypeRecord3" },
                { "testFromJsonWithTypeAmbiguousTargetType" },
                { "testFromJsonWithTypeXML" },
                { "testFromJsonWithTypeRecordWithXMLField" },
                { "testFromJsonWithTypeMap" },
                { "testFromJsonWithTypeStringArray" },
                { "testFromJsonWithTypeArrayNegative" },
                { "testFromJsonWithTypeIntArray" },
                { "testFromJsonWithTypeArrayNegative" },
                { "testFromJsonWithTypeTable" },
                { "tesFromJsonWithTypeMapWithDecimal" },
                { "testConvertJsonToAmbiguousType" },
                { "testFromJsonWithTypeWithNullValues" },
                { "testFromJsonWithTypeWithNullValuesNegative" },
                { "testFromJsonWithTypeWithInferredArgument" },
                { "testFromJsonWithTypeWithTypeReferences" },
                { "testFromJsonWithTypeNestedRecordsNegative" },
                { "testFromJsonWithTypeOnRegExp" },
                { "testFromJsonWithTypeOnRegExpNegative" },
                {"testFromJsonWithTypeToUnionOfTypeReference"},
                {"testFromJsonStringWithUnexpectedChars"}
        };
    }

    @Test(dataProvider = "fromJsonStringWithTypeFunctions")
    public void testFromJsonStringWithType(String function) {
        file = BCompileUtil.compile("test-src/valuelib_fromJson_test.bal");
        BRunUtil.invoke(file, function);
    }

    @DataProvider(name = "fromJsonStringWithTypeFunctions")
    public Object[][] fromJsonStringWithTypeFunctions() {
        return new Object[][] {
                { "testFromJsonStringWithTypeJson" },
                { "testFromJsonStringWithTypeRecord" },
                { "testFromJsonStringWithAmbiguousType" },
                { "testFromJsonStringWithTypeMap" },
                { "testFromJsonStringWithTypeStringArray" },
                { "testFromJsonStringWithTypeArrayNegative" },
                { "testFromJsonStringWithTypeIntArray" },
                { "testFromJsonStringWithTypeWithInferredArgument" }
        };
    }


}
