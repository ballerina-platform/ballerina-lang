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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test cases for value lib functions.
 *
 * @since 1.0
 */
@Test
public class LangLibValueTest {

    private CompileResult compileResult, testFile;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/valuelib_test.bal");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }
    }

    @Test
    public void testToJsonString() {

        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testToJsonString");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap<String, BString> arr = (BMap<String, BString>) returns[0];
        assertEquals(arr.get("aNil").stringValue(), "null");
        assertEquals(arr.get("aString").stringValue(), "aString");
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
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testToStringMethod");
        BValueArray array = (BValueArray) returns[0];
        assertEquals(array.getRefValue(0).stringValue(), "4");
        assertEquals(array.getRefValue(1).stringValue(), "4");
        assertEquals(array.getRefValue(2).stringValue(), "4");
        assertEquals(array.getRefValue(3).stringValue(), "4");

        returns = BRunUtil.invokeFunction(compileResult, "testToString");
        array = (BValueArray) returns[0];
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
        Assert.assertEquals(array.getString(i++), "error FirstError (\"Reason1\",message=\"Test passing error " +
                "union to a function\")");
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
                        "message=\"Test passing error union to a function\")," +
                        "\"varObj\":object Student,\"varObj2\":Rola from MMV," +
                        "\"varObjArr\":[object Student,Rola from MMV]," +
                        "\"varRecord\":{\"name\":\"Gima\",\"address\":{\"country\":\"Sri Lanka\"," +
                        "\"city\":\"Colombo\",\"street\":\"Palm Grove\"},\"age\":12}}");
    }

    @Test
    public void testToStringForTable() {
        BRunUtil.invokeFunction(compileResult, "testToStringMethodForTable");
    }

    @Test(dataProvider = "mergeJsonFunctions", enabled = false)
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
        testFile = BCompileUtil.compile("test-src/valuelib_toBalString_test.bal");
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
        BRunUtil.invokeFunction(testFile, "testObjectToString");
        BRunUtil.invokeFunction(testFile, "testToBalStringOnCycles");
    }

    @Test
    public void testXmlFromBalString() {
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

    @Test(dataProvider = "cloneWithTypeFunctions", enabled = false)
    public void testCloneWithType(String function) {
        BValue[] returns = BRunUtil.invoke(compileResult, function);
    }

    @DataProvider(name = "cloneWithTypeFunctions")
    public Object[][] cloneWithTypeFunctions() {
        return new Object[][] {
                { "testCloneWithTypeJsonRec1" },
                { "testCloneWithTypeJsonRec2" },
                { "testCloneWithTypeOptionalFieldToMandotoryField" },
                { "testCloneWithTypeAmbiguousTargetType" },
                { "testCloneWithTypeForNilPositive" },
                { "testCloneWithTypeForNilNegative" },
                { "testCloneWithTypeNumeric1" },
                { "testCloneWithTypeNumeric2" },
                { "testCloneWithTypeNumeric3" },
                { "testCloneWithTypeNumeric4" },
                { "testCloneWithTypeNumeric5" },
                { "testCloneWithTypeNumeric6" },
                { "testCloneWithTypeNumeric7" },
                { "testCloneWithTypeStringArray" }
        };
    }

    @Test(dataProvider = "fromJsonWithTypeFunctions")
    public void testFromJsonWithType(String function) {
        BRunUtil.invoke(compileResult, function);
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
                { "testFromJsonWithTypeTable" }
        };
    }

    @Test(dataProvider = "fromJsonStringWithTypeFunctions")
    public void testFromJsonStringWithType(String function) {
        BRunUtil.invoke(compileResult, function);
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
                { "testToJsonWithTable" }
        };
    }

    @Test
    public void testEnsureType() {
        BRunUtil.invokeFunction(compileResult, "testEnsureType");
    }

    @Test
    public void testEnsureTypeNegative() {
        BRunUtil.invokeFunction(compileResult, "testEnsureTypeNegative");
    }
}
