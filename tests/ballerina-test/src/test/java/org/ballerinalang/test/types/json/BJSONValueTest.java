/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.json;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class BJSONValueTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/json-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/jsontype/json-value-negative.bal");
    }

    @Test
    public void testJsonInitWithUnsupportedtypes() {
        Assert.assertEquals(negativeResult.getErrorCount(), 6);

        // testJsonArrayWithUnsupportedtypes
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'json', found 'table'", 3, 30);

        // testJsonInitWithUnsupportedtypes
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'json', found 'table'", 9, 39);

        // testIntArrayToJsonAssignment
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'json', found 'int[]'", 15, 14);

        // testFloatArrayToJsonAssignment
        BAssertUtil.validateError(negativeResult, 3, "incompatible types: expected 'json', found 'float[]'", 21, 14);

        // testStringArrayToJsonAssignment
        BAssertUtil.validateError(negativeResult, 4, "incompatible types: expected 'json', found 'string[]'", 27, 14);

        // testBooleanArrayToJsonAssignment
        BAssertUtil.validateError(negativeResult, 5, "incompatible types: expected 'json', found 'boolean[]'", 33, 14);
    }

    @Test
    public void testJSONWithExpressionKey() {
        CompileResult result = BCompileUtil.compile("test-src/types/jsontype/json-literal-with-expr-key.bal");
        BValue[] returns = BRunUtil.invoke(result, "testJSONWithExpressionKey");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"a\":\"Lion\",\"key1\":\"Cat\",\"key2\":\"Dog\"}");
    }

    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asText(), "Supun");
    }

    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().longValue(), 5);
    }

    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().doubleValue(), 7.65);
    }

    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().booleanValue(), true);
    }

    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullAsJsonVal");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedJsonInit");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.toString(), "{\"name\":\"aaa\",\"age\":25," +
                "\"parent\":{\"name\":\"bbb\",\"age\":50},\"address\":{\"city\":\"Colombo\"," +
                "\"country\":\"SriLanka\"}," +
                "\"array\":[1,5,7]}");
    }

    @Test
    public void testJsonWithNull() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonWithNull");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\":null}");

        Assert.assertEquals(returns[1], null);
    }

    @Test(enabled = false)
    public void testGetString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertEquals(returns[1].stringValue(), "Setunga");
    }

    @Test(enabled = false)
    public void testGetInt() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 43);
    }

    @Test(enabled = false)
    public void testGetFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.73, DELTA);
    }

    @Test(enabled = false)
    public void testGetBoolean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test
    public void testGetJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }

    @Test
    public void testGetNonExistingElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetNonExistingElement");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testAddString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testAddInt() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testAddFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testAddBoolean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testAddJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testUpdateInt() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testUpdateFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testUpdateBoolean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testUpdateJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateStringInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateStringInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",\"d\",\"c\"]");
    }

    @Test
    public void testUpdateIntInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateIntInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",64,\"c\"]");
    }

    @Test
    public void testUpdateFloatInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateFloatInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",4.72,\"c\"]");
    }

    @Test
    public void testUpdateBooleanInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateBooleanInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",true,\"c\"]");
    }

    @Test
    public void testUpdateNullInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateNullInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",null,\"c\"]");
    }

    @Test
    public void testUpdateJsonInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateJsonInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",{\"country\":\"SriLanka\"},\"c\"]");
    }

    @Test
    public void testUpdateJsonArrayInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",[1,2,3],\"c\"]");
    }

    @Test(enabled = false)
    public void testGetNestedJsonElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetNestedJsonElement");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Colombo");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "Colombo");
    }

    @Test(enabled = false)
    public void testJsonExprAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonExprAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test()
    public void testSetArrayOutofBoundElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetArrayOutofBoundElement");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[1,2,3,null,null,null,null,8]");
    }

    @Test
    public void testSetToNonArrayWithIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetToNonArrayWithIndex");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
        Assert.assertEquals(returns[1].stringValue(), "foo");
        Assert.assertEquals(returns[2].stringValue(), "true");
    }

    @Test
    public void testGetFromNonArrayWithIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFromNonArrayWithIndex");
        Assert.assertNull(returns[0]);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
    }

    @Test
    public void testSetToNonObjectWithKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetToNonObjectWithKey");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[1,2,3]");
        Assert.assertEquals(returns[1].stringValue(), "foo");
        Assert.assertEquals(returns[2].stringValue(), "true");
    }

    public void testGetFromNonObjectWithKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFromNonObjectWithKey");
        Assert.assertEquals(returns[0], null);
        Assert.assertEquals(returns[1], null);
        Assert.assertEquals(returns[2], null);
    }

    @Test(enabled = false)
    public void testGetStringInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetStringInArray");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "b");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*failed to get element from json: array index out of " +
                    "range: index: 5, size: 3.*")
    public void testGetArrayOutofBoundElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetArrayOutofBoundElement");
    }

    @Test
    public void testGetElementFromPrimitive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetElementFromPrimitive");
        Assert.assertNull(returns[0]);
    }

//    @Test
//    public void testJsonArrayWithVariable() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonArrayWithVariable");
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\",{\"name\":\"supun\"}]");
//    }

    @Test
    public void testUpdateNestedElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateNestedElement");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"details\":{\"fname\":\"Supun\",\"lname\":\"Setunga\"}}");
    }

    @Test
    public void testEmptyStringToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertTrue(json.toString().isEmpty());
    }

    @Test
    public void testJsonStringToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\", \"supun\"}");
    }

    @Test
    public void testStringWithEscapedCharsToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringWithEscapedCharsToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\\\"name\\\", \"supun\"}");
    }

    @Test
    public void testJsonArrayToJsonCasting() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonArrayToJsonCasting");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test
    public void testJsonToJsonArrayCasting() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToJsonArrayCasting");
        Assert.assertTrue(returns[0] instanceof BJSON);

        Assert.assertEquals(returns[0].toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test
    public void testJsonToJsonArrayInvalidCasting() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToJsonArrayInvalidCasting");
        Assert.assertTrue(returns[0] instanceof BStruct);
        String errorMsg = ((BStruct) returns[0]).getStringField(0);
        Assert.assertEquals(errorMsg, "'json[]' cannot be cast to 'json[][][]'");
    }

    @Test
    public void testJsonLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonLength");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error:.*NullReferenceException.*")
    public void testGetFromNull() {
        BRunUtil.invoke(compileResult, "testGetFromNull");
    }

    @Test
    public void testAddToNull() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddToNull");
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }
}
