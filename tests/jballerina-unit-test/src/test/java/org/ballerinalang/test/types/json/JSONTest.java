/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.types.json;

import io.ballerina.runtime.JSONDataSource;
import io.ballerina.runtime.values.StreamingJsonValue;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native functions in ballerina.model.json.
 */
@SuppressWarnings("javadoc")
@Test
public class JSONTest {

    private CompileResult compileResult;
    private static final String json1 = "{'name':{'fname':'Jack','lname':'Taylor'}, 'state':'CA', 'age':20}";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/json-test.bal");
    }

    // Test Remove-Function.
    @Test(description = "Remove an element in a valid jsonpath")
    public void testRemove() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "remove", args);

        final String expected = "{\"state\":\"CA\", \"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    // Test toString-Function.
    @Test(description = "Get string representation of json")
    public void testToString() {
        BValue[] args = {JsonParser.parse(json1)};
        BValue[] returns = BRunUtil.invoke(compileResult, "toString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\", \"lname\":\"Taylor\"}, \"state\":\"CA\", \"age\":20}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Get JSON string from a string")
    public void testParseString() {
        BValue[] args = { new BString("\"hello\"") };
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.STRING_TAG);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test(description = "Get JSON boolean from a string")
    public void testParseBoolean() {
        BValue[] args = {new BString("true")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.BOOLEAN_TAG);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test(description = "Get JSON number from a string")
    public void testParseNumber() {
        BValue[] args = {new BString("45678")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(returns[0].stringValue(), "45678");
    }

    @Test(description = "Get JSON null from a string")
    public void testParseNull() {
        BValue[] args = { new BString("null") };
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Get JSON object from a string")
    public void testParseObject() {
        BValue[] args = {new BString("{\"name\":\"supun\"}")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
    }

    @Test(description = "Get JSON array from a string")
    public void testParseArray() {
        BValue[] args = {new BString("[\"supun\", 45, true, null]")};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        Assert.assertEquals(returns[0].stringValue(), "[\"supun\", 45, true, null]");
    }

    @Test(description = "Get complex JSON from a string")
    public void testParseComplexObject() {
        BValue[] args = {new BString("{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}, " +
                                             "\"marks\":[78, 45, 87]}")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\", \"address\":{\"street\":\"Palm Grove\"}, " +
                "\"marks\":[78, 45, 87]}");
    }

    @Test(description = "Get JSON from a malformed string")
    public void testParseMalformedString() {
        BValue[] args = {new BString("some words without quotes")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BError) returns[0]).getMessage();
        Assert.assertEquals(errorMsg, "unrecognized token 'some' at line: 1 column: 6");
    }

    private String getJsonAsString(BValue bValue) {
        return bValue.stringValue().replace("\\r|\\n|\\t| ", "");
    }

    @Test(description = "Get keys from a JSON")
    public void testGetKeys() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetKeys", args);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray keys = (BValueArray) returns[0];
        Assert.assertEquals(keys.size(), 3);
        Assert.assertEquals(keys.getString(0), "fname");
        Assert.assertEquals(keys.getString(1), "lname");
        Assert.assertEquals(keys.getString(2), "age");
    }

    @Test(description = "Convert a string to json")
    public void testStringToJSONConversion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringToJSONConversion");
        Assert.assertEquals(returns[0].stringValue(), "{\"foo\":\"bar\"}");
    }

    @Test
    public void testJSONArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testJSONArrayToJsonAssignment");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[{\"a\":\"b\"}, {\"c\":\"d\"}]");
    }

    @Test
    public void testCloseRecordToMapJsonAssigment() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testCloseRecordToMapJsonAssigment");
        BValueArray array = (BValueArray) returns[0];
        Assert.assertEquals(array.getRefValue(0).stringValue(), "{name:\"\", age:10}");
        Assert.assertEquals(array.getRefValue(1).stringValue(), "{name:\"\", age:30}");
    }

    @Test
    public void testFieldAccessOfNullableJSON() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/jsontype/nullable-json-test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testFieldAccessOfNullableJSON");
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "JSON value is not a mapping");
    }

    @Test
    public void testStreamingJsonType() {
        StreamingJsonValue jsonValue = new StreamingJsonValue(Mockito.mock(JSONDataSource.class));
        Assert.assertEquals(jsonValue.getType().toString(), "map<json>[]");
    }
}
