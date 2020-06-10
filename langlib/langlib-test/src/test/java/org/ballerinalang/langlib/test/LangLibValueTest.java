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

import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
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
public class LangLibValueTest {

    private CompileResult compileResult;

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
        assertEquals(arr.size(), 10);
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
//        assertEquals(arr.get("aFloatNumber").stringValue(), "10.5"); // TODO : Enable this. Bug in JVM Type checker.
        assertEquals(arr.get("anArray").stringValue(), "[\"hello\", \"world\"]");
        assertEquals(arr.get("anObject").stringValue(),
                "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");
        assertEquals(arr.get("anInvalid").getType().getTag(), TypeTags.ERROR_TAG);
        assertEquals(arr.size(), 7);
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
        Assert.assertEquals(array.getString(i++), "a=STRING b=12 c=12.4 d=true e=x=x y=");
        Assert.assertEquals(array.getString(i++),
                "<CATALOG>" +
                "<CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>" +
                "<CD><TITLE>Hide your heart</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD>" +
                "<CD><TITLE>Greatest Hits</TITLE><ARTIST>Dolly Parton</ARTIST></CD>" +
                "</CATALOG>");
        Assert.assertEquals(array.getString(i++), "str 23 23.4 true");
        Assert.assertEquals(array.getString(i++), "error Reason1 message=Test passing error union to a function");
        Assert.assertEquals(array.getString(i++), "object Student");
        Assert.assertEquals(array.getString(i++), "Rola from MMV");
        Assert.assertEquals(array.getString(i++), "object Student Rola from MMV");
        Assert.assertEquals(array.getString(i++),
                "name=Gima address=country=Sri Lanka city=Colombo street=Palm Grove age=12");
        Assert.assertEquals(array.getString(i),
                            "varInt=6 " +
                            "varFloat=6.0 " +
                            "varStr=toString " +
                            "varNil= " +
                            "varBool=true " +
                            "varDecimal=345.2425341 " +
                            "varjson=a=STRING b=12 c=12.4 d=true e=x=x y= " +
                            "varXml=<CATALOG>" +
                            "<CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>" +
                            "<CD><TITLE>Hide your heart</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD>" +
                            "<CD><TITLE>Greatest Hits</TITLE><ARTIST>Dolly Parton</ARTIST></CD>" +
                            "</CATALOG> " +
                            "varArr=str 23 23.4 true " +
                            "varErr=error Reason1 message=Test passing error union to a function " +
                            "varObj=object Student " +
                            "varObj2=Rola from MMV " +
                            "varObjArr=object Student Rola from MMV " +
                            "varRecord=name=Gima address=country=Sri Lanka city=Colombo street=Palm Grove age=12");
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

    @Test()
    public void xmlSequenceFragmentToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "xmlSequenceFragmentToString");
        Assert.assertEquals((returns[0]).stringValue(), "<def>DEF</def><ghi>1</ghi>");
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
                { "testCloneWithTypeNumeric7" }
        };
    }
}
