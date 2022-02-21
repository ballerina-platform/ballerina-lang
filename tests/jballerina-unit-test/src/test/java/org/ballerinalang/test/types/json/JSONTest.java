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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.JsonDataSource;
import io.ballerina.runtime.internal.JsonParser;
import io.ballerina.runtime.internal.values.StreamingJsonValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Test Native functions in ballerina.model.json.
 */
@SuppressWarnings("javadoc")
@Test
public class JSONTest {

    private CompileResult compileResult;
    private static final String json1 = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"}, \"state\":\"CA\"," +
            " \"age\":20}";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/json-test.bal");
    }

    // Test Remove-Function.
    @Test(description = "Remove an element in a valid jsonpath")
    public void testRemove() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "remove", args);

        final String expected = "{\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns), expected);
    }

    // Test toString-Function.
    @Test(description = "Get string representation of json")
    public void testToString() {
        Object[] args = {JsonParser.parse(json1)};
        Object returns = BRunUtil.invoke(compileResult, "toString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\", \"lname\":\"Taylor\"}, \"state\":\"CA\", \"age\":20}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Get JSON string from a string")
    public void testParseString() {
        Object[] args = {StringUtils.fromString("\"hello\"")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.STRING_TAG);
        Assert.assertEquals(returns.toString(), "hello");
    }

    @Test
    public void testJsonAssignabilityToUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testAssignabilityToUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(returns.toString(), "1");
    }

    @Test(description = "Get JSON boolean from a string")
    public void testParseBoolean() {
        Object[] args = {StringUtils.fromString("true")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.BOOLEAN_TAG);
        Assert.assertEquals(returns.toString(), "true");
    }

    @Test(description = "Get JSON number from a string")
    public void testParseNumber() {
        Object[] args = {StringUtils.fromString("45678")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(returns.toString(), "45678");
    }

    @Test(description = "Get JSON null from a string")
    public void testParseNull() {
        Object[] args = {StringUtils.fromString("null")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        Assert.assertNull(returns);
    }

    @Test(description = "Get JSON object from a string")
    public void testParseObject() {
        Object[] args = {StringUtils.fromString("{\"name\":\"supun\"}")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);
        Assert.assertEquals(returns.toString(), "{\"name\":\"supun\"}");
    }

    @Test(description = "Get JSON array from a string")
    public void testParseArray() {
        Object[] args = {StringUtils.fromString("[\"supun\",45,true,null]")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);
        Assert.assertEquals(returns.toString(), "[\"supun\",45,true,null]");
    }

    @Test(description = "Get complex JSON from a string")
    public void testParseComplexObject() {
        Object[] args = {StringUtils.fromString("{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}, " +
                "\"marks\":[78, 45, 87]}")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);
        Assert.assertEquals(returns.toString(), "{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}," +
                "\"marks\":[78,45,87]}");
    }

    @Test(description = "Get JSON from a malformed string")
    public void testParseMalformedString() {
        Object[] args = {StringUtils.fromString("some words without quotes")};
        Object returns = BRunUtil.invoke(compileResult, "testParse", args);
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                (((BMap) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))).toString();
        Assert.assertEquals(errorMsg, "unrecognized token 'some' at line: 1 column: 6");
    }

    private String getJsonAsString(Object bValue) {
        return bValue.toString().replace("\\r|\\n|\\t| ", "");
    }

    @Test(description = "Get keys from a JSON")
    public void testGetKeys() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testGetKeys", args);

        Assert.assertTrue(returns instanceof BArray);
        BArray keys = (BArray) returns;
        Assert.assertEquals(keys.size(), 3);
        Assert.assertEquals(keys.getString(0), "fname");
        Assert.assertEquals(keys.getString(1), "lname");
        Assert.assertEquals(keys.getString(2), "age");
    }

    @Test(description = "Convert a string to json")
    public void testStringToJSONConversion() {
        Object returns = BRunUtil.invoke(compileResult, "testStringToJSONConversion");
        Assert.assertEquals(returns.toString(), "{\"foo\":\"bar\"}");
    }

    @Test
    public void testJSONArrayToJsonAssignment() {
        Object returns = BRunUtil.invoke(compileResult, "testJSONArrayToJsonAssignment");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "[{\"a\":\"b\"},{\"c\":\"d\"}]");
    }

    @Test
    public void testCloseRecordToMapJsonAssigment() {
        Object returns = BRunUtil.invoke(compileResult, "testCloseRecordToMapJsonAssigment");
        BArray array = (BArray) returns;
        Assert.assertEquals(array.getRefValue(0).toString(), "{\"name\":\"\",\"age\":10}");
        Assert.assertEquals(array.getRefValue(1).toString(), "{\"name\":\"\",\"age\":30}");
    }

    @Test
    public void testFieldAccessOfNullableJSON() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/jsontype/nullable-json-test.bal");
        Object returns = BRunUtil.invoke(compileResult, "testFieldAccessOfNullableJSON");
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "JSON value is not a mapping");
    }

    @Test
    public void testStreamingJsonType() {
        StreamingJsonValue jsonValue = new StreamingJsonValue(Mockito.mock(JsonDataSource.class));
        Assert.assertEquals(getType(jsonValue).toString(), "map<json>[]");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
