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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
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
    public void testToJsonString() {

        Object returns = BRunUtil.invoke(compileResult, "testToJsonString");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap<BString, BString> arr = (BMap<BString, BString>) returns;
        assertEquals(arr.get(StringUtils.fromString("aNil")).toString(), "null");
        assertEquals(arr.get(StringUtils.fromString("aString")).toString(), "\"aString\"");
        assertEquals(arr.get(StringUtils.fromString("aNumber")).toString(), "10");
        assertEquals(arr.get(StringUtils.fromString("aFloatNumber")).toString(), "10.5");
        assertEquals(arr.get(StringUtils.fromString("anArray")).toString(), "[\"hello\", \"world\"]");
        assertEquals(arr.get(StringUtils.fromString("anObject")).toString(),
                "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");
        assertEquals(arr.get(StringUtils.fromString("anotherMap")).toString(),
                "{\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\", " +
                        "\"intVal\":2324, \"boolVal\":true, \"floatVal\":45.4, " +
                        "\"nestedMap\":{\"xx\":\"XXStr\", \"n\":343, \"nilVal\":null}}");
        assertEquals(arr.get(StringUtils.fromString("aStringMap")).toString(),
                "{\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\"}");
        assertEquals(arr.get(StringUtils.fromString("aArr")).toString(),
                "[{\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\", \"intVal\":2324, " +
                        "\"boolVal\":true, \"floatVal\":45.4, \"nestedMap\":{\"xx\":\"XXStr\", \"n\":343, " +
                        "\"nilVal\":null}}, {\"name\":\"anObject\", \"value\":\"10\", \"sub\":\"Science\"}]");
        assertEquals(arr.get(StringUtils.fromString("iArr")).toString(), "[0, 1, 255]");
        assertEquals(arr.get(StringUtils.fromString("arr1")).toString(),
                "{\"country\":\"x\", \"city\":\"y\", \"street\":\"z\", \"no\":3}");
        assertEquals(arr.size(), 11);
    }

    @Test
    public void testToJsonForNonJsonTypes() {
        BRunUtil.invoke(compileResult, "testToJsonStringForNonJsonTypes");
    }

    @Test
    public void testToStringOnCycles() {
        BRunUtil.invoke(compileResult, "testToStringOnCycles");
    }

    @Test
    public void testFromJsonString() {

        Object returns = BRunUtil.invoke(compileResult, "testFromJsonString");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap<BString, Object> arr = (BMap<BString, Object>) returns;
        assertEquals(getType(arr.get(StringUtils.fromString("aNil"))).getTag(), TypeTags.ERROR_TAG);
        assertNull(arr.get(StringUtils.fromString("aNull")));
        assertEquals(arr.get(StringUtils.fromString("aString")).toString(), "aString");
        assertEquals(arr.get(StringUtils.fromString("aNumber")).toString(), "10");
        assertEquals(arr.get(StringUtils.fromString("aFloatNumber")).toString(), "10.5");
        assertEquals(arr.get(StringUtils.fromString("positiveZero")).toString(), "0");
        assertEquals(arr.get(StringUtils.fromString("negativeZero")).toString(), "-0.0");
        assertEquals(arr.get(StringUtils.fromString("negativeNumber")).toString(), "-25");
        assertEquals(arr.get(StringUtils.fromString("negativeFloatNumber")).toString(), "-10.5");
        assertEquals(arr.get(StringUtils.fromString("anArray")).toString(), "[\"hello\",\"world\"]");
        assertEquals(arr.get(StringUtils.fromString("anObject")).toString(),
                "{\"name\":\"anObject\",\"value\":10,\"sub\":{\"subName\":\"subObject\",\"subValue\":10}}");
        assertEquals(getType(arr.get(StringUtils.fromString("anInvalid"))).getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 12);
    }

    @Test
    public void testFromJsonStringNegative() {
        BRunUtil.invoke(compileResult, "testFromJsonStringNegative");
    }

    @Test
    public void testFromJsonFloatString() {

        Object returns = BRunUtil.invoke(compileResult, "testFromJsonFloatString");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap<BString, Object> arr = (BMap<BString, Object>) returns;
        assertEquals(getType(arr.get(StringUtils.fromString("aNil"))).getTag(), TypeTags.ERROR_TAG);
        assertNull(arr.get(StringUtils.fromString("aNull")));
        assertEquals(arr.get(StringUtils.fromString("aString")).toString(), "aString");
        assertEquals(arr.get(StringUtils.fromString("aNumber")).toString(), "10.0");
        assertEquals(arr.get(StringUtils.fromString("aFloatNumber")).toString(), "10.5");
        assertEquals(arr.get(StringUtils.fromString("positiveZero")).toString(), "0.0");
        assertEquals(arr.get(StringUtils.fromString("negativeZero")).toString(), "-0.0");
        assertEquals(arr.get(StringUtils.fromString("negativeNumber")).toString(), "-25.0");
        assertEquals(arr.get(StringUtils.fromString("negativeFloatNumber")).toString(), "-10.5");
        assertEquals(arr.get(StringUtils.fromString("anArray")).toString(), "[\"hello\",\"world\"]");
        assertEquals(arr.get(StringUtils.fromString("anObject")).toString(),
                "{\"name\":\"anObject\",\"value\":10.0,\"sub\":{\"subName\":\"subObject\",\"subValue\":10.0}}");
        assertEquals(getType(arr.get(StringUtils.fromString("anInvalid"))).getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 12);
    }

    @Test
    public void testFromJsonDecimalString() {

        Object returns = BRunUtil.invoke(compileResult, "testFromJsonDecimalString");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap<BString, Object> arr = (BMap<BString, Object>) returns;
        assertEquals(getType(arr.get(StringUtils.fromString("aNil"))).getTag(), TypeTags.ERROR_TAG);
        assertNull(arr.get(StringUtils.fromString("aNull")));
        assertEquals(arr.get(StringUtils.fromString("aString")).toString(), "aString");
        assertEquals(arr.get(StringUtils.fromString("aNumber")).toString(), "10");
        assertEquals(arr.get(StringUtils.fromString("aFloatNumber")).toString(), "10.5");
        assertEquals(arr.get(StringUtils.fromString("positiveZero")).toString(), "0");
        assertEquals(arr.get(StringUtils.fromString("negativeZero")).toString(), "0");
        assertEquals(arr.get(StringUtils.fromString("negativeNumber")).toString(), "-25");
        assertEquals(arr.get(StringUtils.fromString("negativeFloatNumber")).toString(), "-10.5");
        assertEquals(arr.get(StringUtils.fromString("anArray")).toString(), "[\"hello\",\"world\"]");
        assertEquals(arr.get(StringUtils.fromString("anObject")).toString(),
                "{\"name\":\"anObject\",\"value\":10,\"sub\":{\"subName\":\"subObject\",\"subValue\":10}}");
        assertEquals(getType(arr.get(StringUtils.fromString("anInvalid"))).getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 12);
    }

    @Test
    public void testToString() {
        BRunUtil.invoke(compileResult, "testToStringMethod");

        Object returns = BRunUtil.invoke(compileResult, "testToString");
        BArray array = (BArray) returns;
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
        BRunUtil.invoke(compileResult, "testToStringMethodForTable");
    }

    @Test(dataProvider = "mergeJsonFunctions")
    public void testMergeJson(String function) {
        Object returns = BRunUtil.invoke(compileResult, function);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void xmlSequenceFragmentToString() {
        Object returns = BRunUtil.invoke(compileResult, "xmlSequenceFragmentToString");
        Assert.assertEquals((returns).toString(), "<def>DEF</def><ghi>1</ghi>");
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
                "testCloneWithTypeNestedStructuredTypesNegative", "testCloneWithTypeJsonToRecordRestField",
                "testCloneWithTypeWithAmbiguousUnion",
                "testCloneWithTypeWithTuples",
                "testCloneWithTypeToUnion", "testCloneWithTypeToUnionOfTypeReference"
        };
    }

    @Test(dataProvider = "cloneWithTypeToTupleTypeFunctions")
    public void testCloneWithTypeToTuple(String function) {
        BRunUtil.invoke(compileResult, function);
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

    @Test
    public void testAssigningCloneableToAnyOrError() {
        BRunUtil.invoke(compileResult, "testAssigningCloneableToAnyOrError");
        BRunUtil.invoke(compileResult, "testUsingCloneableReturnType");
    }

    @Test
    public void testDestructuredNamedArgs() {
        BRunUtil.invoke(compileResult, "testDestructuredNamedArgs");
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
                {"testFromJsonWithTypeToUnionOfTypeReference"}
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
                { "testTableToJsonConversion" }
        };
    }

    @Test(dataProvider = "ensureTypeFunctions")
    public void testEnsureType(String function) {
        BRunUtil.invoke(compileResult, function);
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
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider(name = "ensureTypeNegativeFunctions")
    public Object[] ensureTypeNegativeFunctions() {
        return new String[]{
                "testEnsureTypeNegative", "testEnsureTypeJsonToNestedRecordsWithErrors",
                "testEnsureTypeFloatToIntNegative"
        };
    }

    @Test
    public void testDecimalToString() {
        BRunUtil.invoke(compileResult, "testDecimalZeroToString");
        BRunUtil.invoke(compileResult, "testDecimalNonZeroToString");
    }
}
