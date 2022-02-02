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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

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
        Object result = JvmRunUtil.invoke(compileResult, "testIntWithoutArgs");
        Assert.assertEquals(result, 7L);
    }

    @Test
    public void testInt2() {
        Object result = JvmRunUtil.invoke(compileResult, "testIntWithArgs", new Object[]{5});
        Assert.assertEquals(result, 10L);
    }

    @Test
    public void testString1() {
        Object result = JvmRunUtil.invoke(compileResult, "testStringWithoutArgs");
        Assert.assertEquals(result.toString(), "Hello");
    }

    @Test
    public void testString2() {
        Object result = JvmRunUtil.invoke(compileResult, "testStringWithArgs", new Object[]{StringUtils.fromString(
                "World")});
        Assert.assertEquals(result.toString(), "HelloWorld");
    }

    @Test
    public void testArray() {
        Object result = JvmRunUtil.invoke(compileResult, "testArray", new Object[]{StringUtils.fromString("World")});
        Assert.assertEquals(result.toString(), "3");
    }

    @Test
    public void getGlobalVar() {
        Object returns = JvmRunUtil.invoke(compileResult, "getGlobalVar");
        Assert.assertEquals(returns, 7L);
    }

    @Test
    public void testInt() {
        Object result = JvmRunUtil.invoke(compileResult, "testByteWithoutArgs");
        Assert.assertEquals(result, 7L);
    }

    @Test(description = "Test byte value assignment")
    public void testByteValue() {
        Object returns = JvmRunUtil.invoke(compileResult, "testByteValue");
        Assert.assertSame(returns.getClass(), Integer.class);
        int byteValue = (int) returns;
        Assert.assertEquals(byteValue, 34, "Invalid byte value returned.");
    }

    @Test(description = "Test byte value space")
    public void testByteValueSpace() {
        Object returns = JvmRunUtil.invoke(compileResult, "testByteValueSpace");
        int byteValue = (int) returns;
        Assert.assertEquals(byteValue, 234, "Invalid byte value returned.");
    }

    @Test(description = "Test byte default value")
    public void testByteDefaultValue() {
        Object returns = JvmRunUtil.invoke(compileResult, "testByteDefaultValue");

        Assert.assertSame(returns.getClass(), Integer.class);
        int byteValue = (int) returns;
        Assert.assertEquals(byteValue, 0, "Invalid byte value returned.");
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
        int input = 34;
        Object[] args = {(input)};
        Object returns = JvmRunUtil.invoke(compileResult, functionName, args);

        Assert.assertSame(returns.getClass(), Integer.class);
        int byteValue = (int) returns;
        Assert.assertEquals(byteValue, input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer cast")
    public void testByteToIntCast() {
        int input = 12;
        Object[] args = {(input)};
        Object returns = JvmRunUtil.invoke(compileResult, "testByteToIntCast", args);

        Assert.assertSame(returns.getClass(), Long.class);
        long intValue = (long) returns;
        Assert.assertEquals(intValue, input, "Invalid integer value returned.");
    }

    @Test(description = "Test integer to byte cast")
    public void testIntToByteCast() {
        int input = 123;
        Object[] args = {(input)};
        Object returns = JvmRunUtil.invoke(compileResult, "testIntToByteExplicitCast", args);

        Assert.assertSame(returns.getClass(), Integer.class);
        int bByte = (int) returns;
        Assert.assertEquals(bByte, input, "Invalid byte value returned.");
    }

    @Test(description = "Test integer to byte explicit cast")
    public void testIntToByteExplicitCast() {
        int input = 123;
        Object[] args = {(input)};
        Object returns = JvmRunUtil.invoke(compileResult, "testIntToByteCast", args);

        Assert.assertSame(returns.getClass(), Integer.class);
        int bByte = (int) returns;
        Assert.assertEquals(bByte, input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte array value")
    public void testByteArrayValue() {
        Object[] args = {};
        Object returns = JvmRunUtil.invoke(compileResult, "testByteArray", args);

        Assert.assertTrue(returns instanceof BArray);
    }

    @Test(description = "Test byte array assignment")
    public void testByteArrayAssignment() {
        byte input1 = 2;
        byte input2 = 56;
        byte input3 = 89;
        byte input4 = 23;

        BArray bByteArrayIn = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_BYTE));
        bByteArrayIn.add(0, input1);
        bByteArrayIn.add(1, input2);
        bByteArrayIn.add(2, input3);
        bByteArrayIn.add(3, input4);
        Object[] args = {bByteArrayIn};

        Object returns = JvmRunUtil.invoke(compileResult, "testByteArrayAssignment", args);

        Assert.assertTrue(returns instanceof BArray);
        BArray bByteArrayOut = (BArray) returns;

        Assert.assertEquals(bByteArrayOut.getByte(0), input1);
        Assert.assertEquals(bByteArrayOut.getByte(1), input2);
        Assert.assertEquals(bByteArrayOut.getByte(2), input3);
        Assert.assertEquals(bByteArrayOut.getByte(3), input4);
    }

    @Test
    public void testTuple() {
        // Todo: revisit when tuple access and var type supported
        Object result = JvmRunUtil.invoke(compileResult, "tupleTest");
        Assert.assertEquals(result.toString(), "10");
    }

    @Test
    public void testRecords() {
        Object result = JvmRunUtil.invoke(compileResult, "recordsTest");
        Assert.assertEquals(result.toString(), "JBallerina");
    }

    @Test
    public void testUnions() {
        Object result = JvmRunUtil.invoke(compileResult, "unionTest");
        Assert.assertEquals(result.toString(), "10.5");
    }

    @Test
    public void testAny() {
        Object result = JvmRunUtil.invoke(compileResult, "anyTest");
        Assert.assertEquals(result.toString(), "{\"name\":\"Jbal\",\"physics\":75,\"chemistry\":89}");
    }

    @Test
    public void testAnyData() {
        Object result = JvmRunUtil.invoke(compileResult, "anyDataTest");
        Assert.assertEquals(result.toString(), "1000");
    }

    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        Object returns = JvmRunUtil.invoke(compileResult, "testStringAsJsonVal");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Supun");
    }

    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        Object returns = JvmRunUtil.invoke(compileResult, "testIntAsJsonVal");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        Object returns = JvmRunUtil.invoke(compileResult, "testFloatAsJsonVal");
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 7.65);
    }

    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBooleanAsJsonVal");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        Object returns = JvmRunUtil.invoke(compileResult, "testNullAsJsonVal");
        Assert.assertNull(returns);
    }

    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        Object returns = JvmRunUtil.invoke(compileResult, "testNestedJsonInit");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(),
                "{\"name\":\"aaa\",\"age\":25," +
                        "\"parent\":{\"name\":\"bbb\",\"age\":50},\"address\":{\"city\":\"Colombo\"," +
                        "\"country\":\"SriLanka\"}," + "\"array\":[1,5,7]}");
    }

    @Test
    public void testJsonWithNull() {
        Object val = JvmRunUtil.invoke(compileResult, "testJsonWithNull");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertEquals(returns.get(0).toString(), "{\"name\":null}");
        Assert.assertNull(returns.get(1));
    }

    @Test
    public void testGetString() {
        Object val = JvmRunUtil.invoke(compileResult, "testGetString");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Supun");
        Assert.assertEquals(returns.get(1).toString(), "Setunga");
    }

    @Test
    public void testGetInt() {
        Object val = JvmRunUtil.invoke(compileResult, "testGetInt");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(0), 25L);
        Assert.assertEquals(returns.get(1), 43L);
    }

    @Test
    public void testGetFloat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetFloat");
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals((double) returns, 9.73, 0.01);
    }

    @Test
    public void testGetBoolean() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetBoolean");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testGetJson() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }

    @Test
    public void testGetNonExistingElement() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetNonExistingElement");
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BError) returns).getMessage(), "{ballerina/lang.map}KeyNotFound");

    }

    @Test
    public void testAddString() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAddString");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testAddInt() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAddInt");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testAddFloat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAddFloat");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testAddBoolean() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAddBoolean");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testAddJson() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAddJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateString() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateString");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testUpdateInt() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateInt");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testUpdateFloat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateFloat");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testUpdateBoolean() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateBoolean");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testUpdateJson() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateStringInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateStringInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",\"d\",\"c\"]");
    }

    @Test
    public void testUpdateIntInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateIntInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",64,\"c\"]");
    }

    @Test
    public void testUpdateFloatInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateFloatInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",4.72,\"c\"]");
    }

    @Test
    public void testUpdateBooleanInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateBooleanInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",true,\"c\"]");
    }

    @Test
    public void testUpdateNullInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateNullInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",null,\"c\"]");
    }

    @Test
    public void testUpdateJsonInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateJsonInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",{\"country\":\"SriLanka\"},\"c\"]");
    }

    @Test
    public void testUpdateJsonArrayInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[\"a\",[1,2,3],\"c\"]");
    }

    @Test
    public void testGetNestedJsonElement() {
        Object val = JvmRunUtil.invoke(compileResult, "testGetNestedJsonElement");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0)instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Colombo");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Colombo");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "Colombo");

        Assert.assertTrue(returns.get(3) instanceof BString);
        Assert.assertEquals(returns.get(3).toString(), "Colombo");
    }

    @Test
    public void testJsonExprAsIndex() {
        Object returns = JvmRunUtil.invoke(compileResult, "testJsonExprAsIndex");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Colombo");
    }

    @Test()
    public void testSetArrayOutofBoundElement() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSetArrayOutofBoundElement");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[1,2,3,null,null,null,null,8]");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: 'map<json>' " +
                    "cannot be cast to 'json\\[\\]'.*")
    public void testSetToNonArrayWithIndex() {
        Object val = JvmRunUtil.invoke(compileResult, "testSetToNonArrayWithIndex");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertTrue(returns.get(2) instanceof Boolean);
        Assert.assertEquals(returns.get(0).toString(), "{\"name\":\"supun\"}");
        Assert.assertEquals(returns.get(1).toString(), "foo");
        Assert.assertEquals(returns.get(2).toString(), "true");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<json>' cannot be cast to 'json\\[\\]'.*")
    public void testGetFromNonArrayWithIndex() {
        Object val = JvmRunUtil.invoke(compileResult, "testGetFromNonArrayWithIndex");
        BArray returns = (BArray) val;
        Assert.assertNull(returns.get(0));
        Assert.assertNull(returns.get(1));
        Assert.assertNull(returns.get(2));
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: 'json\\[\\]' " +
                    "cannot be cast to 'map<json>.*")
    public void testSetToNonObjectWithKey() {
        Object val = JvmRunUtil.invoke(compileResult, "testSetToNonObjectWithKey");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertTrue(returns.get(2) instanceof Boolean);
        Assert.assertEquals(returns.get(0).toString(), "[1,2,3]");
        Assert.assertEquals(returns.get(1).toString(), "foo");
        Assert.assertEquals(returns.get(2).toString(), "true");
    }

    @Test
    public void testGetFromNonObjectWithKey() {
        Object val = JvmRunUtil.invoke(compileResult, "testGetFromNonObjectWithKey");
        BArray returns = (BArray) val;
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(((BError) returns.get(0)).getDetails().toString(),
                "{\"message\":\"JSON value is not a mapping\"}");
        Assert.assertEquals(((BError) returns.get(1)).getDetails().toString(),
                "{\"message\":\"JSON value is not a mapping\"}");
        Assert.assertEquals(((BError) returns.get(2)).getDetails().toString(),
                "{\"message\":\"JSON value is not a mapping\"}");
    }

    @Test
    public void testGetStringInArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetStringInArray");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "b");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*IndexOutOfRange \\{\"message\":\"array index out of range: " +
                    "index: 5, size: 3.*")
    public void testGetArrayOutofBoundElement() {
        JvmRunUtil.invoke(compileResult, "testGetArrayOutofBoundElement");
    }

    @Test
    public void testGetElementFromPrimitive() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetElementFromPrimitive");
        Assert.assertEquals(((BError) returns).getDetails().toString(),
                "{\"message\":\"JSON value is not a mapping\"}");
    }

    @Test
    public void testUpdateNestedElement() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUpdateNestedElement");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"details\":{\"fname\":\"Supun\",\"lname\":\"Setunga\"}}");
    }

    @Test
    public void testJsonArrayToJsonCasting() {
        Object returns = JvmRunUtil.invoke(compileResult, "testJsonArrayToJsonCasting");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}JSONOperationError \\{\"message\":\"JSON value " +
                    "is not " +
                    "a mapping\"\\}\n" +
                    "\tat types:testGetFromNull\\(types.bal:588\\)")
    public void testGetFromNull() {
        JvmRunUtil.invoke(compileResult, "testGetFromNull");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)'" +
                    " cannot be cast to 'map<json>'.*")
    public void testAddToNull() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAddToNull");
        Assert.assertEquals(returns.toString(), "{\"name\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testJsonIntToFloat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testJsonIntToFloat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'int'.*")
    public void testNullJsonToInt() {
        JvmRunUtil.invoke(compileResult, "testNullJsonToInt");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'float'.*")
    public void testNullJsonToFloat() {
        JvmRunUtil.invoke(compileResult, "testNullJsonToFloat");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'string'.*")
    public void testNullJsonToString() {
        JvmRunUtil.invoke(compileResult, "testNullJsonToString");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'boolean'.*")
    public void testNullJsonToBoolean() {
        JvmRunUtil.invoke(compileResult, "testNullJsonToBoolean");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'int\\[\\]'.*")
    public void testNullJsonToArray() {
        JvmRunUtil.invoke(compileResult, "testNullJsonToArray");
    }

    @Test
    public void testIntArrayToJsonAssignment() {
        Object val = JvmRunUtil.invoke(compileResult, "testIntArrayToJsonAssignment");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[1,5,9,4]");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 5L);
    }

    @Test
    public void testFloatArrayToJsonAssignment() {
        Object val = JvmRunUtil.invoke(compileResult, "testFloatArrayToJsonAssignment");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[1.3,5.4,9.4,4.5]");
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 5.4);
    }

    @Test
    public void testStringArrayToJsonAssignment() {
        Object val = JvmRunUtil.invoke(compileResult, "testStringArrayToJsonAssignment");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[\"apple\",\"orange\",\"grape\"]");
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "orange");
    }

    @Test
    public void testBooleanArrayToJsonAssignment() {
        Object val = JvmRunUtil.invoke(compileResult, "testBooleanArrayToJsonAssignment");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertEquals(returns.get(0).toString(), "[true,true,false,true]");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test
    public void testFutures() {
        Object result = JvmRunUtil.invoke(compileResult, "futuresTest");
        Assert.assertTrue(result.toString().equals("0") || result.toString().equals("100") ||
                result.toString().equals("200"));
    }

    @Test
    public void testBasicStructAsObject() {
        Object val = JvmRunUtil.invoke(objectsResult, "testSimpleObjectAsStruct");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "sample name");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test
    public void testBasicStructAsObjectWithJustNew() {
        Object val = JvmRunUtil.invoke(objectsResult, "testSimpleObjectAsStructWithNew");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "sample name");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test
    public void testUserInitFunction() {
        Object val = JvmRunUtil.invoke(objectsResult, "testUserInitFunction");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 4);

        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Long.class);
        Assert.assertTrue(returns.get(3) instanceof BString);

        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "sample name");
        Assert.assertEquals(returns.get(2), 50L);
        Assert.assertEquals(returns.get(3).toString(), "february");
    }

    @Test
    public void testWait() {
        Object result = JvmRunUtil.invoke(compileResult, "waitTest");
        Assert.assertEquals(result.toString(), "wait");
    }

    @Test
    public void testSelfReferencingRecord() {
        Object result = JvmRunUtil.invoke(compileResult, "testSelfReferencingRecord");
        Assert.assertEquals(result.toString(), "{\"a\":2,\"f\":{\"a\":1,\"f\":null}}");
    }

    @Test
    public void testSelfReferencingObject() {
        Object result = JvmRunUtil.invoke(objectsResult, "testSelfReferencingObject");
        Assert.assertEquals(result.toString(), "{a:3, f:null}");
    }

    @Test
    public void testNewTable() {
        JvmRunUtil.invoke(compileResult, "tableFunc");
    }

    @Test
    public void testDecimalWithoutArgs() {
        Object result = JvmRunUtil.invoke(compileResult, "testDecimalWithoutArgs");
        Assert.assertEquals(((BDecimal) result).intValue(), 7);
    }

    @Test
    public void testDecimalWithArgs() {
        Object result = JvmRunUtil.invoke(compileResult, "testDecimalWithArgs",
                new Object[]{ValueCreator.createDecimalValue(BigDecimal.valueOf(5))});
        Assert.assertEquals(((BDecimal) result).intValue(), 10);
    }

    @Test
    public void testObjectWithSameNameAsFileName() {
        Object result = JvmRunUtil.invoke(objectsResult, "testObjectWithSameNameAsFileName");
        Assert.assertEquals(result.toString(), "works!");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string\\[\\]' cannot be cast to " +
                    "'\\[float\\]\\[\\]'.*")
    public void testTupleArrayTypeToString() {
        JvmRunUtil.invoke(compileResult, "testTupleArrayTypeToString");
    }

    @Test
    public void testTypeDescValuePrint() {
        JvmRunUtil.invoke(compileResult, "testTypeDescValuePrint");
    }

    @Test
    public void testEnumFlagAndMembers() {
        Object result = JvmRunUtil.invokeAndGetJVMResult(compileResult, "testEnumFlagAndMembers");
        Type type = TypeUtils.getType(result);
        Assert.assertEquals(type.getTag(), TypeTags.TUPLE_TAG);

        TupleValueImpl tupleValue = (TupleValueImpl) result;
        TypedescValueImpl typedescValue = (TypedescValueImpl) tupleValue.getRefValue(0);
        UnionType returnType = (UnionType) ((FunctionType) typedescValue.getDescribingType()).getReturnType();
        List<Type> originalMemberTypes = returnType.getOriginalMemberTypes();
        Assert.assertEquals(originalMemberTypes.size(), 2);
        Type memType1 = originalMemberTypes.get(0);
        Assert.assertEquals(memType1.getTag(), TypeTags.UNION_TAG);
        UnionType memUnionType = (UnionType) memType1;
        Assert.assertTrue(SymbolFlags.isFlagOn(memUnionType.getFlags(), SymbolFlags.ENUM));
        Assert.assertEquals(memUnionType.getMemberTypes().size(), 2);
        Type memType2 = originalMemberTypes.get(1);
        Assert.assertEquals(memType2.getTag(), TypeTags.NULL_TAG);

        typedescValue = (TypedescValueImpl) tupleValue.getRefValue(1);
        returnType = (UnionType) ((FunctionType) typedescValue.getDescribingType()).getReturnType();
        originalMemberTypes = returnType.getOriginalMemberTypes();
        Assert.assertEquals(originalMemberTypes.size(), 2);
        memType1 = originalMemberTypes.get(0);
        Assert.assertEquals(memType1.getTag(), TypeTags.UNION_TAG);
        memUnionType = (UnionType) memType1;
        Assert.assertFalse(SymbolFlags.isFlagOn(memUnionType.getFlags(), SymbolFlags.ENUM));
        Assert.assertEquals(memUnionType.getMemberTypes().size(), 2);
        memType2 = originalMemberTypes.get(1);
        Assert.assertEquals(memType2.getTag(), TypeTags.NULL_TAG);
    }

    @Test
    public void testName() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/TypesProject");
        Object returnedValue = JvmRunUtil.invokeAndGetJVMResult(result, "testName");
        Type type = TypeUtils.getType(returnedValue);
        Assert.assertEquals(type.getTag(), TypeTags.TUPLE_TAG);

        TupleValueImpl tupleValue = (TupleValueImpl) returnedValue;
        assertName(getReturnType(tupleValue.getRefValue(0)), "MyEnum", "testorg/types:1:MyEnum");
        assertName(getReturnType(tupleValue.getRefValue(1)), "", "");
        assertName(getReturnType(tupleValue.getRefValue(2)), "MyTuple", "testorg/types:1:MyTuple");
        assertName(getReturnType(tupleValue.getRefValue(3)), "", "");
    }

    private Type getReturnType(Object typedesc) {
        return ((FunctionType) ((BTypedesc) typedesc).getDescribingType()).getReturnType();
    }

    private void assertName(Type type, String expectedName, String expectedQualifiedName) {
        Assert.assertEquals(type.getName(), expectedName);
        Assert.assertEquals(type.getQualifiedName(), expectedQualifiedName);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        objectsResult = null;
    }
}
