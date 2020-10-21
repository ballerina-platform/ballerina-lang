/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.test.jvm;

import org.ballerinalang.core.model.types.BTypes;
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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

/**
 * Test cases to cover some basic types related tests on JBallerina.
 *
 * @since 0.955.0
 */
public class TypesTest {

    private CompileResult compileResult;
    private CompileResult objectsResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/types.bal");
        objectsResult = BCompileUtil.compile("test-src/jvm/objects.bal");
    }

    @Test
    public void testInt1() {
        BValue[] result = BRunUtil.invoke(compileResult, "testIntWithoutArgs", new BValue[] {});
        Assert.assertEquals(((BInteger) result[0]).intValue(), 7);
    }

    @Test
    public void testInt2() {
        BValue[] result = BRunUtil.invoke(compileResult, "testIntWithArgs", new BValue[] { new BInteger(5) });
        Assert.assertEquals(((BInteger) result[0]).intValue(), 10);
    }

    @Test
    public void testString1() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStringWithoutArgs", new BValue[] {});
        Assert.assertEquals((result[0]).stringValue(), "Hello");
    }

    @Test
    public void testString2() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStringWithArgs", new BValue[] { new BString("World") });
        Assert.assertEquals((result[0]).stringValue(), "HelloWorld");
    }

    @Test
    public void testArray() {
        BValue[] result = BRunUtil.invoke(compileResult, "testArray", new BValue[] { new BString("World") });
        Assert.assertEquals((result[0]).stringValue(), "3");
    }

    @Test
    public void getGlobalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getGlobalVar");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }

    @Test
    public void testInt() {
        BValue[] result = BRunUtil.invoke(compileResult, "testByteWithoutArgs");
        Assert.assertEquals(((BInteger) result[0]).intValue(), 7);
    }

    @Test(description = "Test byte value assignment")
    public void testByteValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteValue", new BValue[] {});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 34, "Invalid byte value returned.");
    }

    @Test(description = "Test byte value space")
    public void testByteValueSpace() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteValueSpace", new BValue[] {});
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 234, "Invalid byte value returned.");
    }

    @Test(description = "Test byte default value")
    public void testByteDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteDefaultValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 0, "Invalid byte value returned.");
    }

    @Test(description = "Test byte function parameter")
    public void testByteParameter() {
        invokeByteInputFunction("testByteParam");
    }

    @Test(description = "Test global byte value assignment")
    public void testGlobalByte() {
        invokeByteInputFunction("testGlobalByte");
    }

    private void invokeByteInputFunction(String functionName) {
        long input = 34;
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer cast")
    public void testByteToIntCast() {
        long input = 12;
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), input, "Invalid integer value returned.");
    }

    @Test(description = "Test integer to byte cast")
    public void testIntToByteCast() {
        int input = 123;
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntToByteExplicitCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test integer to byte explicit cast")
    public void testIntToByteExplicitCast() {
        int input = 123;
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntToByteCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte array value")
    public void testByteArrayValue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
    }

    @Test(description = "Test byte array assignment")
    public void testByteArrayAssignment() {
        byte input1 = 2;
        byte input2 = 56;
        byte input3 = 89;
        byte input4 = 23;

        BValueArray bByteArrayIn = new BValueArray(BTypes.typeByte);
        bByteArrayIn.add(0, input1);
        bByteArrayIn.add(1, input2);
        bByteArrayIn.add(2, input3);
        bByteArrayIn.add(3, input4);
        BValue[] args = { bByteArrayIn };

        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayAssignment", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray bByteArrayOut = (BValueArray) returns[0];

        Assert.assertEquals(bByteArrayOut.getByte(0), input1);
        Assert.assertEquals(bByteArrayOut.getByte(1), input2);
        Assert.assertEquals(bByteArrayOut.getByte(2), input3);
        Assert.assertEquals(bByteArrayOut.getByte(3), input4);
    }

    @Test
    public void testTuple() {
        // Todo: revisit when tuple access and var type supported
        BValue[] result = BRunUtil.invoke(compileResult, "tupleTest");
        Assert.assertEquals((result[0]).stringValue(), "10");
    }

    @Test
    public void testRecords() {
        BValue[] result = BRunUtil.invoke(compileResult, "recordsTest");
        Assert.assertEquals((result[0]).stringValue(), "JBallerina");
    }

    @Test
    public void testUnions() {
        BValue[] result = BRunUtil.invoke(compileResult, "unionTest");
        Assert.assertEquals((result[0]).stringValue(), "10.5");
    }

    @Test
    public void testAny() {
        BValue[] result = BRunUtil.invoke(compileResult, "anyTest");
        Assert.assertEquals((result[0]).stringValue(), "{name:\"Jbal\", physics:75, chemistry:89}");
    }

    @Test
    public void testAnyData() {
        BValue[] result = BRunUtil.invoke(compileResult, "anyDataTest");
        Assert.assertEquals((result[0]).stringValue(), "1000");
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
        Assert.assertEquals(returns[0].stringValue(),
                "{\"name\":\"aaa\", \"age\":25, " +
                        "\"parent\":{\"name\":\"bbb\", \"age\":50}, \"address\":{\"city\":\"Colombo\", " +
                        "\"country\":\"SriLanka\"}, " + "\"array\":[1, 5, 7]}");
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
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.73, 0.01);
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
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BError) returns[0]).getReason(), "{ballerina/lang.map}KeyNotFound");

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

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Colombo");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "Colombo");
    }

    @Test
    public void testJsonExprAsIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonExprAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test()
    public void testSetArrayOutofBoundElement() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSetArrayOutofBoundElement");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[1, 2, 3, null, null, null, null, 8]");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: 'map<json>' " +
                    "cannot be cast to 'json\\[\\]'.*")
    public void testSetToNonArrayWithIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetToNonArrayWithIndex");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertTrue(returns[2] instanceof BBoolean);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
        Assert.assertEquals(returns[1].stringValue(), "foo");
        Assert.assertEquals(returns[2].stringValue(), "true");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<json>' cannot be cast to 'json\\[\\]'.*")
    public void testGetFromNonArrayWithIndex() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFromNonArrayWithIndex");
        Assert.assertNull(returns[0]);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: 'json\\[\\]' " +
                    "cannot be cast to 'map<json>.*")
    public void testSetToNonObjectWithKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetToNonObjectWithKey");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertTrue(returns[2] instanceof BBoolean);
        Assert.assertEquals(returns[0].stringValue(), "[1, 2, 3]");
        Assert.assertEquals(returns[1].stringValue(), "foo");
        Assert.assertEquals(returns[2].stringValue(), "true");
    }

    @Test
    public void testGetFromNonObjectWithKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetFromNonObjectWithKey");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(),
                            "{\"message\":\"JSON value is not a mapping\"}");
        Assert.assertEquals(((BError) returns[1]).getDetails().stringValue(),
                            "{\"message\":\"JSON value is not a mapping\"}");
        Assert.assertEquals(((BError) returns[2]).getDetails().stringValue(),
                            "{\"message\":\"JSON value is not a mapping\"}");
    }

    @Test
    public void testGetStringInArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetStringInArray");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "b");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*IndexOutOfRange \\{\"message\":\"array index out of range: " +
                                            "index: 5, size: 3.*")
    public void testGetArrayOutofBoundElement() {
        BRunUtil.invoke(compileResult, "testGetArrayOutofBoundElement");
    }

    @Test
    public void testGetElementFromPrimitive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetElementFromPrimitive");
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(),
                            "{\"message\":\"JSON value is not a mapping\"}");
    }

    @Test
    public void testUpdateNestedElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateNestedElement");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"details\":{\"fname\":\"Supun\", \"lname\":\"Setunga\"}}");
    }

    @Test
    public void testJsonArrayToJsonCasting() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testJsonArrayToJsonCasting");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[[1, 2, 3], [3, 4, 5], [7, 8, 9]]");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*incompatible types: 'error' cannot be cast to 'string'.*")
    public void testGetFromNull() {
        BRunUtil.invoke(compileResult, "testGetFromNull");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)'" +
                                              " cannot be cast to 'map<json>'.*")
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
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'int'.*")
    public void testNullJsonToInt() {
        BRunUtil.invoke(compileResult, "testNullJsonToInt");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'float'.*")
    public void testNullJsonToFloat() {
        BRunUtil.invoke(compileResult, "testNullJsonToFloat");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'string'.*")
    public void testNullJsonToString() {
        BRunUtil.invoke(compileResult, "testNullJsonToString");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'boolean'.*")
    public void testNullJsonToBoolean() {
        BRunUtil.invoke(compileResult, "testNullJsonToBoolean");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'int\\[\\]'.*")
    public void testNullJsonToArray() {
        BRunUtil.invoke(compileResult, "testNullJsonToArray");
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

    @Test
    public void testFutures() {
        BValue[] result = BRunUtil.invoke(compileResult, "futuresTest");
        Assert.assertTrue(result[0].stringValue().equals("0") || result[0].stringValue().equals("100") ||
                result[0].stringValue().equals("200"));
    }

    @Test
    public void testBasicStructAsObject() {
        BValue[] returns = BRunUtil.invoke(objectsResult, "testSimpleObjectAsStruct");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test
    public void testBasicStructAsObjectWithJustNew() {
        BValue[] returns = BRunUtil.invoke(objectsResult, "testSimpleObjectAsStructWithNew");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test
    public void testUserInitFunction() {
        BValue[] returns = BRunUtil.invoke(objectsResult, "testUserInitFunction");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test
    public void testWait() {
        BValue[] result = BRunUtil.invoke(compileResult, "waitTest");
        Assert.assertEquals(result[0].stringValue(), "wait");
    }

    @Test
    public void testWaitOnSame() {
        BValue[] result = BRunUtil.invoke(compileResult, "waitOnSame");
        Assert.assertEquals(result[0].stringValue(), "wait1");
        Assert.assertEquals(result[1].stringValue(), "wait2");
        Assert.assertEquals(result[2].stringValue(), "00112233");
    }

    @Test
    public void testSelfReferencingRecord() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSelfReferencingRecord");
        Assert.assertEquals((result[0]).stringValue(), "{a:2, f:{a:1, f:()}}");
    }

    @Test
    public void testSelfReferencingObject() {
        BValue[] result = BRunUtil.invoke(objectsResult, "testSelfReferencingObject");
        Assert.assertEquals((result[0]).stringValue(), "{a:3, f:()}");
    }

    @Test
    public void testNewTable() {
        BRunUtil.invoke(compileResult, "tableFunc");
    }

    @Test
    public void testDecimalWithoutArgs() {
        BValue[] result = BRunUtil.invoke(compileResult, "testDecimalWithoutArgs", new BValue[] {});
        Assert.assertEquals(((BDecimal) result[0]).intValue(), 7);
    }

    @Test
    public void testDecimalWithArgs() {
        BValue[] result = BRunUtil.invoke(compileResult, "testDecimalWithArgs",
                                          new BValue[] {new BDecimal(BigDecimal.valueOf(5))});
        Assert.assertEquals(((BDecimal) result[0]).intValue(), 10);
    }

    @Test
    public void testObjectWithSameNameAsFileName() {
        BValue[] result = BRunUtil.invoke(objectsResult, "testObjectWithSameNameAsFileName");
        Assert.assertEquals((result[0]).stringValue(), "works!");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string\\[\\]' cannot be cast to " +
                    "'\\[float\\]\\[\\]'.*")
    public void testTupleArrayTypeToString() {
        BRunUtil.invoke(compileResult, "testTupleArrayTypeToString");
    }

    @Test
    public void testTypeDescValuePrint() {
        PrintStream tempOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        BRunUtil.invoke(compileResult, "testTypeDescValuePrint");
        Assert.assertEquals(new String(baos.toByteArray()), "typedesc map<int|string>");
        System.setOut(tempOut);
    }
}
