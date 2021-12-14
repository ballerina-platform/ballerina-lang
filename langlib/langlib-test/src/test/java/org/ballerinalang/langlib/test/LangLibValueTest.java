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

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test cases for value lib functions.
 *
 * @since 1.0
 */
@Test
public class LangLibValueTest {

    private CompileResult compileResult, file;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/valuelib_test.bal");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }
    }

    @Test void testNegativeCases() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/valuelib_test_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: expected 'any', found " +
                "'ballerina/lang.value:0.0.0:Cloneable'", 21, 13);
        validateError(negativeResult, index++, "incompatible type for parameter 't' with inferred typedesc value: " +
                "expected 'typedesc<anydata>', found 'typedesc<MyClass>'", 30, 23);
        validateError(negativeResult, index++, "incompatible type for parameter 't' with inferred typedesc value: " +
                "expected 'typedesc<anydata>', found 'typedesc<MyClass>'", 31, 23);
        validateWarning(negativeResult, index++, "invalid usage of the 'check' expression operator: " +
                "no expression type is equivalent to error type", 40, 21);
        assertEquals(negativeResult.getErrorCount(), index - 1);
        assertEquals(negativeResult.getWarnCount(), 1);
    }

    @Test
    public void testToJsonString() {

        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testToJsonString");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap<String, BString> arr = (BMap<String, BString>) returns[0];
        assertEquals(arr.get("aNil").stringValue(), "null");
        assertEquals(arr.get("aString").stringValue(), "\"aString\"");
        assertEquals(arr.get("aNumber").stringValue(), "10");
        assertEquals(arr.get("aFloatNumber").stringValue(), "10.5");
        assertEquals(arr.get("anArray").stringValue(), "[\"hello\", \"world\"]");
        assertEquals(arr.get("anObject").stringValue(),
                "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");
        assertEquals(arr.get("anotherMap").stringValue(),
                "{\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\", " +
                        "\"intVal\":2324, \"boolVal\":true, \"floatVal\":45.4, " +
                        "\"nestedMap\":{\"xx\":\"XXStr\", \"n\":343, \"nilVal\":null}}");
        assertEquals(arr.get("aStringMap").stringValue(),
                "{\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\"}");
        assertEquals(arr.get("aArr").stringValue(),
                "[{\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\", \"intVal\":2324, " +
                        "\"boolVal\":true, \"floatVal\":45.4, \"nestedMap\":{\"xx\":\"XXStr\", \"n\":343, " +
                        "\"nilVal\":null}}, {\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\"}]");
        assertEquals(arr.get("iArr").stringValue(), "[0, 1, 255]");
        assertEquals(arr.get("arr1").stringValue(), "{\"country\":\"x\", \"city\":\"y\", \"street\":\"z\", \"no\":3}");
        assertEquals(arr.size(), 11);
    }

    @Test
    public void testToJsonForNonJsonTypes() {
        BRunUtil.invokeFunction(compileResult, "testToJsonStringForNonJsonTypes");
    }

    @Test
    public void testToStringOnCycles() {
        BRunUtil.invokeFunction(compileResult, "testToStringOnCycles");
    }

    @Test
    public void testFromJsonString() {

        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFromJsonString");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap<String, BValue> arr = (BMap<String, BValue>) returns[0];
        assertEquals(arr.get("aNil").getType().getTag(), TypeTags.ERROR_TAG);
        assertNull(arr.get("aNull"));
        assertEquals(arr.get("aString").stringValue(), "aString");
        assertEquals(arr.get("aNumber").stringValue(), "10");
        assertEquals(arr.get("aFloatNumber").stringValue(), "10.5");
        assertEquals(arr.get("positiveZero").stringValue(), "0");
        assertEquals(arr.get("negativeZero").stringValue(), "-0.0");
        assertEquals(arr.get("negativeNumber").stringValue(), "-25");
        assertEquals(arr.get("negativeFloatNumber").stringValue(), "-10.5");
        assertEquals(arr.get("anArray").stringValue(), "[\"hello\", \"world\"]");
        assertEquals(arr.get("anObject").stringValue(),
                "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");
        assertEquals(arr.get("anInvalid").getType().getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 12);
    }

    @Test
    public void testFromJsonStringNegative() {
        BRunUtil.invokeFunction(compileResult, "testFromJsonStringNegative");
    }

    @Test
    public void testFromJsonFloatString() {

        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFromJsonFloatString");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap<String, BValue> arr = (BMap<String, BValue>) returns[0];
        assertEquals(arr.get("aNil").getType().getTag(), TypeTags.ERROR_TAG);
        assertNull(arr.get("aNull"));
        assertEquals(arr.get("aString").stringValue(), "aString");
        assertEquals(arr.get("aNumber").stringValue(), "10.0");
        assertEquals(arr.get("aFloatNumber").stringValue(), "10.5");
        assertEquals(arr.get("positiveZero").stringValue(), "0.0");
        assertEquals(arr.get("negativeZero").stringValue(), "-0.0");
        assertEquals(arr.get("negativeNumber").stringValue(), "-25.0");
        assertEquals(arr.get("negativeFloatNumber").stringValue(), "-10.5");
        assertEquals(arr.get("anArray").stringValue(), "[\"hello\", \"world\"]");
        assertEquals(arr.get("anObject").stringValue(),
                "{\"name\":\"anObject\", \"value\":10.0, \"sub\":{\"subName\":\"subObject\", \"subValue\":10.0}}");
        assertEquals(arr.get("anInvalid").getType().getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 12);
    }

    @Test
    public void testFromJsonDecimalString() {

        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFromJsonDecimalString");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap<String, BValue> arr = (BMap<String, BValue>) returns[0];
        assertEquals(arr.get("aNil").getType().getTag(), TypeTags.ERROR_TAG);
        assertNull(arr.get("aNull"));
        assertEquals(arr.get("aString").stringValue(), "aString");
        assertEquals(arr.get("aNumber").stringValue(), "10");
        assertEquals(arr.get("aFloatNumber").stringValue(), "10.5");
        assertEquals(arr.get("positiveZero").stringValue(), "0.0");
        assertEquals(arr.get("negativeZero").stringValue(), "0.0");
        assertEquals(arr.get("negativeNumber").stringValue(), "-25");
        assertEquals(arr.get("negativeFloatNumber").stringValue(), "-10.5");
        assertEquals(arr.get("anArray").stringValue(), "[\"hello\", \"world\"]");
        assertEquals(arr.get("anObject").stringValue(),
                "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");
        assertEquals(arr.get("anInvalid").getType().getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 12);
    }

    @Test
    public void testToString() {
        BRunUtil.invokeFunction(compileResult, "testToStringMethod");

        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testToString");
        BValueArray array = (BValueArray) returns[0];
        int i = 0;
        Assert.assertEquals(array.getString(i++), "6");
        Assert.assertEquals(array.getString(i++), "6.0");
        Assert.assertEquals(array.getString(i++), "toString");
        Assert.assertEquals(array.getString(i++), "");
        Assert.assertEquals(array.getString(i++), "true");
        Assert.assertEquals(array.getString(i++), "345.2425341");
        Assert.assertEquals(array.getString(i++), "{\"a\":\"STRING\",\"b\":12,\"c\":12.4,\"d\":true," +
                "\"e\":{\"x\":\"x\",\"y\":null}}");
        Assert.assertEquals(array.getString(i++),
                "<CATALOG>" +
                        "<CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>" +
                        "<CD><TITLE>Hide your heart</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD>" +
                        "<CD><TITLE>Greatest Hits</TITLE><ARTIST>Dolly Parton</ARTIST></CD>" +
                        "</CATALOG>");
        Assert.assertEquals(array.getString(i++), "[\"str\",23,23.4,true]");
        Assert.assertEquals(array.getString(i++), "error FirstError (\"Reason1\",error(\"ExampleError\")," +
                "message=\"Test passing error union to a function\")");
        Assert.assertEquals(array.getString(i++), "object Student");
        Assert.assertEquals(array.getString(i++), "Rola from MMV");
        Assert.assertEquals(array.getString(i++), "[object Student,Rola from MMV]");
        Assert.assertEquals(array.getString(i++),
                "{\"name\":\"Gima\",\"address\":{\"country\":\"Sri Lanka\",\"city\":\"Colombo\"," +
                        "\"street\":\"Palm Grove\"},\"age\":12}");
        Assert.assertEquals(array.getString(i),
                "{\"varInt\":6,\"varFloat\":6.0," +
                        "\"varStr\":\"toString\"," +
                        "\"varNil\":null," +
                        "\"varBool\":true," +
                        "\"varDecimal\":345.2425341," +
                        "\"varjson\":{\"a\":\"STRING\",\"b\":12,\"c\":12.4," +
                        "\"d\":true,\"e\":{\"x\":\"x\",\"y\":null}}," +
                        "\"varXml\":`<CATALOG><CD><TITLE>Empire Burlesque</TITLE>" +
                        "<ARTIST>Bob Dylan</ARTIST></CD><CD><TITLE>Hide your heart" +
                        "</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD><CD><TITLE>Greatest Hits</TITLE>" +
                        "<ARTIST>Dolly Parton</ARTIST></CD></CATALOG>`," +
                        "\"varArr\":[\"str\",23,23.4,true],\"varErr\":error FirstError (\"Reason1\"," +
                        "error(\"ExampleError\"),message=\"Test passing error union to a function\")," +
                        "\"varObj\":object Student,\"varObj2\":Rola from MMV," +
                        "\"varObjArr\":[object Student,Rola from MMV]," +
                        "\"varRecord\":{\"name\":\"Gima\",\"address\":{\"country\":\"Sri Lanka\"," +
                        "\"city\":\"Colombo\",\"street\":\"Palm Grove\"},\"age\":12}}");
    }

    @Test
    public void testToStringOnSubTypes() {
        BRunUtil.invoke(compileResult, "testToStringOnSubTypes");
    }

    @Test
    public void testToStringOnFiniteTypes() {
        BRunUtil.invoke(compileResult, "testToStringOnFiniteTypes");
    }

    @Test
    public void testXMLToStringWithXMLTextContainingAngleBrackets() {
        BRunUtil.invoke(compileResult, "testXMLWithAngleBrackets");
    }

    @Test
    public void testToStringForTable() {
        BRunUtil.invokeFunction(compileResult, "testToStringMethodForTable");
    }

    @Test(dataProvider = "mergeJsonFunctions")
    public void testMergeJson(String function) {
        BValue[] returns = BRunUtil.invoke(compileResult, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void xmlSequenceFragmentToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "xmlSequenceFragmentToString");
        Assert.assertEquals((returns[0]).stringValue(), "<def>DEF</def><ghi>1</ghi>");
    }

    @Test
    public void testToBalStringMethod() {
        CompileResult testFile = BCompileUtil.compile("test-src/valuelib_toBalString_test.bal");
        BRunUtil.invokeFunction(testFile, "testIntValueToBalString");
        BRunUtil.invokeFunction(testFile, "testStringValueToBalString");
        BRunUtil.invokeFunction(testFile, "testFloatingPointNumbersToBalString");
        BRunUtil.invokeFunction(testFile, "testAnyAnydataNilToBalString");
        BRunUtil.invokeFunction(testFile, "testTableToBalString");
        BRunUtil.invokeFunction(testFile, "testErrorToBalString");
        BRunUtil.invokeFunction(testFile, "testArrayToBalString");
        BRunUtil.invokeFunction(testFile, "testTupleToBalString");
        BRunUtil.invokeFunction(testFile, "testJsonToBalString");
        BRunUtil.invokeFunction(testFile, "testXmlToBalString");
        BRunUtil.invokeFunction(testFile, "testObjectToBalString");
        BRunUtil.invokeFunction(testFile, "testToBalStringOnCycles");
    }

    @Test
    public void testFromBalString() {
        CompileResult file = BCompileUtil.compile("test-src/valuelib_fromBalString_test.bal");
        BRunUtil.invokeFunction(file, "testIntValueFromBalString");
        BRunUtil.invokeFunction(file, "testStringValueFromBalString");
        BRunUtil.invokeFunction(file, "testFloatingPointNumbersFromBalString");
        BRunUtil.invokeFunction(file, "testAnydataNilFromBalString");
        BRunUtil.invokeFunction(file, "testMapFromBalString");
        BRunUtil.invokeFunction(file, "testTableFromBalString");
        BRunUtil.invokeFunction(file, "testArrayFromBalString");
        BRunUtil.invokeFunction(file, "testTupleFromBalString");
        BRunUtil.invokeFunction(file, "testJsonFromBalString");
        BRunUtil.invokeFunction(file, "testXmlFromBalString");
        BRunUtil.invokeFunction(file, "testObjectFromString");
        BRunUtil.invokeFunction(file, "testFromBalStringOnCycles");
        BRunUtil.invokeFunction(file, "testFromBalStringNegative");
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

    @Test(dataProvider = "cloneWithTypeFunctions")
    public void testCloneWithType(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider(name = "cloneWithTypeFunctions")
    public Object[] cloneWithTypeFunctions() {
        return new String[]{
                "testCloneWithTypeJsonRec1", "testCloneWithTypeJsonRec2",
                "testCloneWithTypeOptionalFieldToMandotoryField", "testCloneWithTypeAmbiguousTargetType",
                "testCloneWithTypeForNilPositive", "testCloneWithTypeForNilNegative", "testCloneWithTypeNumeric1",
                "testCloneWithTypeNumeric2", "testCloneWithTypeNumeric3", "testCloneWithTypeNumeric4",
                "testCloneWithTypeNumeric5", "testCloneWithTypeNumeric6", "testCloneWithTypeNumeric7",
                "testCloneWithTypeToArrayOfRecord", "testCloneWithTypeToArrayOfMap",
                "testCloneWithTypeIntArrayToUnionArray", "testCloneWithTypeIntSubTypeArray",
                "testCloneWithTypeStringArray", "testCloneWithTypeWithInferredArgument",
                "testCloneWithTypeWithImmutableTypes", "testCloneWithTypeDecimalToInt",
                "testCloneWithTypeDecimalToIntNegative", "testCloneWithTypeDecimalToByte",
                "testCloneWithTypeDecimalToIntSubType", "testCloneWithTypeTupleToJSON",
                "testCloneWithTypeImmutableStructuredTypes", "testCloneWithTypeWithFiniteArrayTypeFromIntArray",
                "testCloneWithTypeWithFiniteType", "testCloneWithTypeWithUnionOfFiniteType",
                "testCloneWithTypeWithFiniteArrayTypeFromIntArray",
                "testCloneWithTypeWithUnionOfFiniteTypeArraysFromIntArray",
                "testCloneWithTypeWithUnionTypeArrayFromIntArray",
                "testCloneWithTypeWithFiniteTypeArrayFromIntArrayNegative", "testConvertJsonToNestedRecordsWithErrors",
                "testCloneWithTypeNestedStructuredTypesNegative", "testCloneWithTypeJsonToRecordRestField"
        };
    }

    @Test(dataProvider = "cloneWithTypeToTupleTypeFunctions")
    public void testCloneWithTypeToTuple(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider(name = "cloneWithTypeToTupleTypeFunctions")
    public Object[][] cloneWithTypeToTupleTypeFunctions() {
        return new Object[][] {
                { "testCloneWithTypeArrayToTupleWithRestType" },
                { "testCloneWithTypeArrayToTupleWithRestTypeUnionType" },
                { "testCloneWithTypeArrayToUnionTupleNegative" },
                { "testCloneWithTypeArrayToTupleWithMoreTargetTypes" },
                { "testCloneWithTypeArrayToTupleWithUnionRestTypeNegative" },
                { "testCloneWithTypeArrayToTupleNegative" },
                { "testCloneWithTypeArrayToTupleWithStructureRestTypeNegative" },
                { "testCloneWithTypeTupleRestType" },
                { "testCloneWithTypeUnionTuple" },
                { "testCloneWithTypeTupleRestTypeNegative" },
                { "testCloneWithTypeUnionTupleRestTypeNegative" }
        };
    }

    @Test(dataProvider = "fromJsonWithTypeFunctions")
    public void testFromJsonWithType(String function) {
        file = BCompileUtil.compile("test-src/valuelib_fromJson_test.bal");
        BRunUtil.invoke(file, function);
    }

    @Test
    public void testAssigningCloneableToAnyOrError() {
        BRunUtil.invokeFunction(compileResult, "testAssigningCloneableToAnyOrError");
        BRunUtil.invokeFunction(compileResult, "testUsingCloneableReturnType");
    }

    @Test
    public void testDestructuredNamedArgs() {
        BRunUtil.invokeFunction(compileResult, "testDestructuredNamedArgs");
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
                { "testFromJsonWithTypeNestedRecordsNegative" }
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

    @Test(dataProvider = "toJsonFunctions")
    public void testToJson(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider(name = "toJsonFunctions")
    public Object[][] toJsonFunctions() {
        return new Object[][] {
                { "testToJsonWithRecord1" },
                { "testToJsonWithRecord2" },
                { "testToJsonWithLiterals" },
                { "testToJsonWithArray" },
                { "testToJsonWithXML" },
                { "testToJsonWithMap" },
                { "testToJsonWithMapInt" },
                { "testToJsonWithStringArray" },
                { "testToJsonWithIntArray" },
                { "testToJsonWithTable" },
                { "testToJsonWithCyclicParameter" },
                { "testTableToJsonConversion" },
                { "testToJsonConversionError" }
        };
    }

    @Test(dataProvider = "ensureTypeFunctions")
    public void testEnsureType(String function) {
        BRunUtil.invokeFunction(compileResult, function);
    }

    @DataProvider(name = "ensureTypeFunctions")
    public Object[][] ensureTypeFunctions() {
        return new Object[][] {
                { "testEnsureType" },
                { "testEnsureTypeWithInferredArgument" }
        };
    }

    @Test(dataProvider = "ensureTypeNegativeFunctions")
    public void testEnsureTypeNegative(String function) {
        BRunUtil.invokeFunction(compileResult, function);
    }

    @DataProvider(name = "ensureTypeNegativeFunctions")
    public Object[] ensureTypeNegativeFunctions() {
        return new String[]{
                "testEnsureTypeNegative", "testEnsureTypeJsonToNestedRecordsWithErrors",
                "testEnsureTypeFloatToIntNegative"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        file = null;
    }
}
