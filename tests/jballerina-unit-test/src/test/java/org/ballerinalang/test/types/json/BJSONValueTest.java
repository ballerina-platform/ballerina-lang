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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int[]|error)', found 'json'", 29, 21);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int|error)', found '(json|Error)'", 33, 19);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(MyJsonDiff|xml)', found '(json|xml)'", 73, 24);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testJSONWithExpressionKey() {
        CompileResult result = BCompileUtil.compile("test-src/types/jsontype/json-literal-with-expr-key.bal");
        Object returns = BRunUtil.invoke(result, "testJSONWithExpressionKey");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"a\":\"Lion\",\"key1\":\"Cat\",\"key2\":\"Dog\"}");
    }

    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testStringAsJsonVal");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Supun");
    }

    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testIntAsJsonVal");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatAsJsonVal");
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 7.65);
    }

    @Test(description = "Test initializing json with a byte")
    public void testByteAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testByteAsJsonVal");
        Assert.assertTrue(returns instanceof Integer);
        Assert.assertEquals(returns, 5);
    }

    @Test(description = "Test initializing json with a decimal")
    public void testDecimalAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalAsJsonVal");
        Assert.assertTrue(returns instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns).value(), new BigDecimal("7.65", MathContext.DECIMAL128));
    }

    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanAsJsonVal");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        Object returns = BRunUtil.invoke(compileResult, "testNullAsJsonVal");
        Assert.assertNull(returns);
    }

    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        Object returns = BRunUtil.invoke(compileResult, "testNestedJsonInit");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"name\":\"aaa\",\"age\":25," +
                "\"parent\":{\"name\":\"bbb\",\"age\":50},\"address\":{\"city\":\"Colombo\"," +
                "\"country\":\"SriLanka\"},\"array\":[1,5,7]}");
    }

    @Test
    public void testJsonWithNull() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testJsonWithNull");
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertEquals(returns.get(0).toString(), "{\"name\":null}");
        Assert.assertNull(returns.get(1));
    }

    @Test
    public void testGetString() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testGetString");
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Supun");
        Assert.assertEquals(returns.get(1).toString(), "Setunga");
    }

    @Test
    public void testGetInt() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testGetInt");
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(0), 25L);
        Assert.assertEquals(returns.get(1), 43L);
    }

    @Test
    public void testGetFloat() {
        Object returns = BRunUtil.invoke(compileResult, "testGetFloat");
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals((Double) returns, 9.73, DELTA);
    }

    @Test
    public void testGetDecimal() {
        Object returns = BRunUtil.invoke(compileResult, "testGetDecimal");
        Assert.assertTrue(returns instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns).value(), new BigDecimal("9.5", MathContext.DECIMAL128));
    }

    @Test
    public void testGetBoolean() {
        Object returns = BRunUtil.invoke(compileResult, "testGetBoolean");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testGetJson() {
        Object returns = BRunUtil.invoke(compileResult, "testGetJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }

    @Test
    public void testGetNonExistingElement() {
        Object returns = BRunUtil.invoke(compileResult, "testGetNonExistingElement");
        String errorMsg = ((BMap<String, BString>) ((BError) returns).getDetails()).get(
                StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "key 'name' not found in JSON mapping");
    }

    @Test
    public void testAddString() {
        Object returns = BRunUtil.invoke(compileResult, "testAddString");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testAddInt() {
        Object returns = BRunUtil.invoke(compileResult, "testAddInt");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testAddFloat() {
        Object returns = BRunUtil.invoke(compileResult, "testAddFloat");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testAddDecimal() {
        Object returns = BRunUtil.invoke(compileResult, "testAddDecimal");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testAddBoolean() {
        Object returns = BRunUtil.invoke(compileResult, "testAddBoolean");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testAddJson() {
        Object returns = BRunUtil.invoke(compileResult, "testAddJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateString() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateString");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testUpdateInt() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateInt");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testUpdateFloat() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateFloat");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testUpdateBoolean() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateBoolean");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testUpdateJson() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateStringInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateStringInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",\"d\",\"c\"]");
    }

    @Test
    public void testUpdateIntInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateIntInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",64,\"c\"]");
    }

    @Test
    public void testUpdateFloatInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateFloatInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",4.72,\"c\"]");
    }

    @Test
    public void testUpdateDecimalInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateDecimalInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",4.72,\"c\"]");
    }

    @Test
    public void testUpdateBooleanInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateBooleanInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",true,\"c\"]");
    }

    @Test
    public void testUpdateNullInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateNullInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",null,\"c\"]");
    }

    @Test
    public void testUpdateJsonInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateJsonInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",{\"country\":\"SriLanka\"},\"c\"]");
    }

    @Test
    public void testUpdateJsonArrayInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",[1,2,3],\"c\"]");
    }

    @Test
    public void testGetNestedJsonElement() {
        Object returns = BRunUtil.invoke(compileResult, "testGetNestedJsonElement");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Colombo");
    }

    @Test()
    public void testSetArrayOutofBoundElement() {
        Object returns = BRunUtil.invoke(compileResult, "testSetArrayOutofBoundElement");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[1,2,3,null,null,null,null,8]");
    }

    @Test
    public void testGetStringInArray() {
        Object returns = BRunUtil.invoke(compileResult, "testGetStringInArray");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "b");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*IndexOutOfRange \\{\"message\":\"array index out of range: index: 5, " +
                            "size: 3.*")
    public void testGetArrayOutofBoundElement() {
        BRunUtil.invoke(compileResult, "testGetArrayOutofBoundElement");
    }

    @Test
    public void testGetElementFromPrimitive() {
        Object returns = BRunUtil.invoke(compileResult, "testGetElementFromPrimitive");
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "JSON value is not a mapping");
    }

    @Test
    public void testUpdateNestedElement() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateNestedElement");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"details\":{\"fname\":\"Supun\",\"lname\":\"Setunga\"}}");
    }

    @Test
    public void testEmptyStringToJson() {
        Object returns = BRunUtil.invoke(compileResult, "testEmptyStringToJson");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertTrue((Boolean) returns.toString().isEmpty());
    }

    @Test
    public void testJsonStringToJson() {
        Object returns = BRunUtil.invoke(compileResult, "testJsonStringToJson");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "{\"name\", \"supun\"}");
    }

    @Test
    public void testStringWithEscapedCharsToJson() {
        Object returns = BRunUtil.invoke(compileResult, "testStringWithEscapedCharsToJson");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "{\\\"name\\\", \"supun\"}");
    }

    @Test
    public void testJsonArrayToJsonCasting() {
        Object returns = BRunUtil.invoke(compileResult, "testJsonArrayToJsonCasting");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test
    public void testJsonToJsonArrayCasting() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testJsonToJsonArrayCasting");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
        Assert.assertTrue(
                returns.get(1).toString().contains("incompatible types: 'json[]' cannot be cast to 'json[][]'"));
        Assert.assertEquals(returns.get(2).toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test
    public void testJsonToJsonArrayInvalidCasting() {
        Object returns = BRunUtil.invoke(compileResult, "testJsonToJsonArrayInvalidCasting");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "'json[]' value cannot be converted to 'Json3DArray': " +
                "\n\t\tarray element '[0][0]' should be of type 'json[]', found '1'" +
                "\n\t\tarray element '[0][1]' should be of type 'json[]', found '2'" +
                "\n\t\tarray element '[0][2]' should be of type 'json[]', found '3'" +
                "\n\t\tarray element '[1][0]' should be of type 'json[]', found '3'" +
                "\n\t\tarray element '[1][1]' should be of type 'json[]', found '4'" +
                "\n\t\tarray element '[1][2]' should be of type 'json[]', found '5'" +
                "\n\t\tarray element '[2][0]' should be of type 'json[]', found '7'" +
                "\n\t\tarray element '[2][1]' should be of type 'json[]', found '8'" +
                "\n\t\tarray element '[2][2]' should be of type 'json[]', found '9'");
    }

    @Test
    public void testJsonLength() {
        Object returns = BRunUtil.invoke(compileResult, "testJsonLength");

        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 3L);
    }

    @Test
    public void testGetFromNull() {
        Object returns = BRunUtil.invoke(compileResult, "testGetFromNull");
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "JSON value is not a mapping");
    }

    @Test
    public void testAddToNull() {
        Object returns = BRunUtil.invoke(compileResult, "testAddToNull");
        Assert.assertEquals(returns.toString(), "{\"name\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testJsonIntToFloat() {
        Object returns = BRunUtil.invoke(compileResult, "testJsonIntToFloat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'int'.*")
    public void testNullJsonToInt() {
        BRunUtil.invoke(compileResult, "testNullJsonToInt");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'float'.*")
    public void testNullJsonToFloat() {
        BRunUtil.invoke(compileResult, "testNullJsonToFloat");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'string'.*")
    public void testNullJsonToString() {
        BRunUtil.invoke(compileResult, "testNullJsonToString");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'boolean'.*")
    public void testNullJsonToBoolean() {
        BRunUtil.invoke(compileResult, "testNullJsonToBoolean");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.value\\}ConversionError " +
                    "\\{\"message\":\"cannot convert '\\(\\)' to type 'JsonMap'.*")
    public void testNullJsonToMap() {
        BRunUtil.invoke(compileResult, "testNullJsonToMap");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: '\\(\\)' cannot be cast to 'int\\[\\]'.*")
    public void testNullJsonToArray() {
        BRunUtil.invoke(compileResult, "testNullJsonToArray");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"" +
                    "incompatible types: 'json\\[\\]' cannot be cast to 'map<json>\\[\\]'.*")
    public void testMapJsonToJsonArray() {
        BRunUtil.invoke(compileResult, "testMapJsonToJsonArray");
    }

    @Test
    public void testJsonMapAccess() {
        BRunUtil.invoke(compileResult, "testJsonMapAccess");
    }

    @Test
    public void testIntArrayToJsonAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testIntArrayToJsonAssignment");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[1,5,9,4]");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 5L);
    }

    @Test
    public void testFloatArrayToJsonAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testFloatArrayToJsonAssignment");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[1.3,5.4,9.4,4.5]");
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 5.4);
    }

    @Test
    public void testDecimalArrayToJsonAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDecimalArrayToJsonAssignment");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[1.3,1.234,4.1,4.54]");
        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        Assert.assertEquals(((BDecimal) returns.get(1)).value(), new BigDecimal("1.234", MathContext.DECIMAL128));
    }

    @Test
    public void testStringArrayToJsonAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStringArrayToJsonAssignment");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[\"apple\",\"orange\",\"grape\"]");
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "orange");
    }

    @Test
    public void testBooleanArrayToJsonAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testBooleanArrayToJsonAssignment");
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[true,true,false,true]");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test
    public void testJsonLaxErrorLifting() {
        BRunUtil.invoke(compileResult, "testJsonLaxErrorLifting");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeResult = null;
    }
}
