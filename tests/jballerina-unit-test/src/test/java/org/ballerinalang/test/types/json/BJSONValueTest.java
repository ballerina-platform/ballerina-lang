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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BNewArray;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

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
    public void testJsonWithIncompatibleTypesNegative() {
        int i = 0;

        // testJsonArrayWithUnsupportedtypes
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'json', found 'table<DummyType>'", 7, 30);

        // testJsonInitWithUnsupportedtypes
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'json', found 'table<DummyType>'", 13, 42);

        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'map<json>', found 'Person'", 21, 20);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'map<json>', found 'AnotherPerson'", 22, 19);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'map<json>', found 'PersonWithTypedesc'", 23, 25);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testJSONWithExpressionKey() {
        CompileResult result = BCompileUtil.compile("test-src/types/jsontype/json-literal-with-expr-key.bal");
        BValue[] returns = BRunUtil.invoke(result, "testJSONWithExpressionKey");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"a\":\"Lion\", \"key1\":\"Cat\", \"key2\":\"Dog\"}");
    }

    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
    }

    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 7.65);
    }

    @Test(description = "Test initializing json with a byte")
    public void testByteAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BByte);
        Assert.assertEquals(((BByte) returns[0]).byteValue(), 5);
    }

    @Test(description = "Test initializing json with a decimal")
    public void testDecimalAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal("7.65", MathContext.DECIMAL128));
    }

    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullAsJsonVal");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedJsonInit");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"aaa\", \"age\":25, " +
                "\"parent\":{\"name\":\"bbb\", \"age\":50}, \"address\":{\"city\":\"Colombo\", " +
                "\"country\":\"SriLanka\"}, " +
                "\"array\":[1, 5, 7]}");
    }

    @Test
    public void testJsonWithNull() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonWithNull");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":null}");
        Assert.assertNull(returns[1]);
    }

    @Test
    public void testGetString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertEquals(returns[1].stringValue(), "Setunga");
    }

    @Test
    public void testGetInt() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 43);
    }

    @Test
    public void testGetFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.73, DELTA);
    }

    @Test
    public void testGetDecimal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDecimal");
        Assert.assertTrue(returns[0] instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal("9.5", MathContext.DECIMAL128));
    }

    @Test
    public void testGetBoolean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testGetJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetJson");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"city\":\"Colombo\", \"country\":\"SriLanka\"}");
    }

    @Test
    public void testGetNonExistingElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetNonExistingElement");
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "Key 'name' not found in JSON mapping");
    }

    @Test
    public void testAddString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddString");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"lname\":\"Setunga\"}");
    }

    @Test
    public void testAddInt() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddInt");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"age\":25}");
    }

    @Test
    public void testAddFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddFloat");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"score\":4.37}");
    }

    @Test
    public void testAddDecimal() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddDecimal");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"score\":4.37}");
    }

    @Test
    public void testAddBoolean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddBoolean");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"status\":true}");
    }

    @Test
    public void testAddJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddJson");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateString");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"lname\":\"Setunga\"}");
    }

    @Test
    public void testUpdateInt() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateInt");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"age\":25}");
    }

    @Test
    public void testUpdateFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateFloat");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"score\":4.37}");
    }

    @Test
    public void testUpdateBoolean() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateBoolean");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"status\":true}");
    }

    @Test
    public void testUpdateJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateJson");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"fname\":\"Supun\", \"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateStringInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateStringInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", \"d\", \"c\"]");
    }

    @Test
    public void testUpdateIntInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateIntInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", 64, \"c\"]");
    }

    @Test
    public void testUpdateFloatInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateFloatInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", 4.72, \"c\"]");
    }

    @Test
    public void testUpdateDecimalInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateDecimalInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", 4.72, \"c\"]");
    }

    @Test
    public void testUpdateBooleanInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateBooleanInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", true, \"c\"]");
    }

    @Test
    public void testUpdateNullInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateNullInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", null, \"c\"]");
    }

    @Test
    public void testUpdateJsonInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateJsonInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", {\"country\":\"SriLanka\"}, \"c\"]");
    }

    @Test
    public void testUpdateJsonArrayInArray() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\", [1, 2, 3], \"c\"]");
    }

    @Test
    public void testGetNestedJsonElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetNestedJsonElement");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test()
    public void testSetArrayOutofBoundElement() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSetArrayOutofBoundElement");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 2, 3, null, null, null, null, 8]");
    }

    @Test
    public void testGetStringInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetStringInArray");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "b");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*IndexOutOfRange \\{\"message\":\"array index out of range: index: 5, " +
                  "size: 3.*")
    public void testGetArrayOutofBoundElement() {
        BRunUtil.invoke(compileResult, "testGetArrayOutofBoundElement");
    }

    @Test
    public void testGetElementFromPrimitive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetElementFromPrimitive");
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "JSON value is not a mapping");
    }
    
    @Test
    public void testUpdateNestedElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateNestedElement");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"details\":{\"fname\":\"Supun\", \"lname\":\"Setunga\"}}");
    }

    @Test
    public void testEmptyStringToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyStringToJson");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[0].stringValue().isEmpty());
    }

    @Test
    public void testJsonStringToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonStringToJson");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\", \"supun\"}");
    }

    @Test
    public void testStringWithEscapedCharsToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringWithEscapedCharsToJson");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{\\\"name\\\", \"supun\"}");
    }

    @Test
    public void testJsonArrayToJsonCasting() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testJsonArrayToJsonCasting");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[[1, 2, 3], [3, 4, 5], [7, 8, 9]]");
    }

    @Test
    public void testJsonToJsonArrayCasting() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToJsonArrayCasting");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[[1, 2, 3], [3, 4, 5], [7, 8, 9]]");
        Assert.assertTrue(
                returns[1].stringValue().contains("incompatible types: 'json[]' cannot be cast to 'json[][]'"));
        Assert.assertEquals(returns[2].stringValue(), "[[1, 2, 3], [3, 4, 5], [7, 8, 9]]");
    }

    @Test
    public void testJsonToJsonArrayInvalidCasting() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToJsonArrayInvalidCasting");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'json[]' value cannot be converted to 'json[][][]'");
    }

    @Test
    public void testJsonLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonLength");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testGetFromNull() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFromNull");
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "JSON value is not a mapping");
    }

    @Test
    public void testAddToNull() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddToNull");
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"Supun\", \"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testJsonIntToFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonIntToFloat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'int'.*")
    public void testNullJsonToInt() {
        BRunUtil.invoke(compileResult, "testNullJsonToInt");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'float'.*")
    public void testNullJsonToFloat() {
        BRunUtil.invoke(compileResult, "testNullJsonToFloat");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'string'.*")
    public void testNullJsonToString() {
        BRunUtil.invoke(compileResult, "testNullJsonToString");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'boolean'.*")
    public void testNullJsonToBoolean() {
        BRunUtil.invoke(compileResult, "testNullJsonToBoolean");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.typedesc\\}ConversionError " +
                    "\\{\"message\":\"cannot convert '\\(\\)' to type 'map<json>'.*")
    public void testNullJsonToMap() {
        BRunUtil.invoke(compileResult, "testNullJsonToMap");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'int\\[\\]'.*")
    public void testNullJsonToArray() {
        BRunUtil.invoke(compileResult, "testNullJsonToArray");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: 'json\\[\\]' cannot be cast to 'map<json>\\[\\]'.*")
    public void testMapJsonToJsonArray() {
        BRunUtil.invoke(compileResult, "testMapJsonToJsonArray");
    }

    @Test
    public void testIntArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntArrayToJsonAssignment");
        Assert.assertTrue(returns[0] instanceof BNewArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 5, 9, 4]");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 5);
    }

    @Test
    public void testFloatArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatArrayToJsonAssignment");
        Assert.assertTrue(returns[0] instanceof BNewArray);
        Assert.assertEquals(returns[0].stringValue(), "[1.3, 5.4, 9.4, 4.5]");
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 5.4);
    }

    @Test
    public void testDecimalArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalArrayToJsonAssignment");
        Assert.assertTrue(returns[0] instanceof BNewArray);
        Assert.assertEquals(returns[0].stringValue(), "[1.3, 1.234, 4.1, 4.540000000000000035527136788005009]");
        Assert.assertTrue(returns[1] instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns[1]).decimalValue(), new BigDecimal("1.234", MathContext.DECIMAL128));
    }

    @Test
    public void testStringArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringArrayToJsonAssignment");
        Assert.assertTrue(returns[0] instanceof BNewArray);
        Assert.assertEquals(returns[0].stringValue(), "[\"apple\", \"orange\", \"grape\"]");
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "orange");
    }

    @Test
    public void testBooleanArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanArrayToJsonAssignment");
        Assert.assertTrue(returns[0] instanceof BNewArray);
        Assert.assertEquals(returns[0].stringValue(), "[true, true, false, true]");
        Assert.assertTrue(returns[1] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }
}
