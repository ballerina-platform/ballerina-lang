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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
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
        compileResult = BTestUtils.compile("test-src/types/jsontype/json-value.bal");
        negativeResult = BTestUtils.compile("test-src/types/jsontype/json-literal-negative.bal");
    }

    @Test
    public void testJsonInitWithUnsupportedtypes() {
        // testJsonArrayWithUnsupportedtypes
        BTestUtils.validateError(negativeResult, 0, "incompatible types: expected 'json', found 'datatable'", 3, 30);
        
        // testJsonInitWithUnsupportedtypes
        BTestUtils.validateError(negativeResult, 1, "incompatible types: expected 'json', found 'datatable'", 9, 39);
    }

    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testStringAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asText(), "Supun");
    }

    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testIntAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asInt(), 5);
    }

    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testFloatAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asDouble(), 7.65);
    }

    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testBooleanAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asBoolean(), true);
    }

    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testNullAsJsonVal");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testNestedJsonInit");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.toString(), "{\"name\":\"aaa\",\"age\":25," +
                "\"parent\":{\"name\":\"bbb\",\"age\":50},\"address\":{\"city\":\"Colombo\"," +
                "\"country\":\"SriLanka\"}," +
                "\"array\":[1,5,7]}");
    }

    @Test
    public void testJsonWithNull() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonWithNull");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\":null}");

        Assert.assertEquals(returns[1], null);
    }

    @Test
    public void testGetString() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertEquals(returns[1].stringValue(), "Setunga");
    }

    @Test
    public void testGetInt() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 43);
    }

    @Test
    public void testGetFloat() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.73, DELTA);
    }

    @Test
    public void testGetBoolean() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test
    public void testGetJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }

    @Test
    public void testGetNonExistingElement() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetNonExistingElement");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testAddString() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testAddString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testAddInt() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testAddInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testAddFloat() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testAddFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testAddBoolean() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testAddBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testAddJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testAddJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateString() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testUpdateInt() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testUpdateFloat() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testUpdateBoolean() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testUpdateJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateStringInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateStringInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",\"d\",\"c\"]");
    }

    @Test
    public void testUpdateIntInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateIntInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",64,\"c\"]");
    }

    @Test
    public void testUpdateFloatInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateFloatInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",4.72,\"c\"]");
    }

    @Test
    public void testUpdateBooleanInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateBooleanInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",true,\"c\"]");
    }

    @Test
    public void testUpdateNullInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateNullInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",null,\"c\"]");
    }

    @Test
    public void testUpdateJsonInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateJsonInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",{\"country\":\"SriLanka\"},\"c\"]");
    }

    @Test
    public void testUpdateJsonArrayInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",[1,2,3],\"c\"]");
    }

    @Test
    public void testGetNestedJsonElement() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetNestedJsonElement");
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
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonExprAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test()
    public void testSetArrayOutofBoundElement() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testSetArrayOutofBoundElement");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[1,2,3,null,null,null,null,8]");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot set value to '7': expected a 'json-array', but found " +
                    "'json-object'.*")
    public void testSetToNonArrayWithIndex() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testSetToNonArrayWithIndex");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot get value from '7': expected a 'json-array', but found " +
                    "'json-object'.*")
    public void testGetFromNonArrayWithIndex() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetFromNonArrayWithIndex");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot set value to 'name': expected a 'json-object', but found " +
                    "'json-array'.*")
    public void testSetToNonObjectWithKey() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testSetToNonObjectWithKey");
    }

    public void testGetFromNonObjectWithKey() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetFromNonObjectWithKey");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testGetStringInArray() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetStringInArray");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "b");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*failed to get element from json: array index out of " +
                    "range: index: 5, size: 3.*")
    public void testGetArrayOutofBoundElement() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetArrayOutofBoundElement");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot get value from 'fname': expected a 'json-object', but found " +
                    "'string'.*")
    public void testGetStringFromPrimitive() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetStringFromPrimitive");
    }

//    @Test
//    public void testJsonArrayWithVariable() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonArrayWithVariable");
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\",{\"name\":\"supun\"}]");
//    }

    @Test
    public void testUpdateNestedElement() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testUpdateNestedElement");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"details\":{\"fname\":\"Supun\",\"lname\":\"Setunga\"}}");
    }

    @Test
    public void testEmptyStringToJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testEmptyStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertTrue(json.toString().isEmpty());
    }

    @Test
    public void testJsonStringToJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\", \"supun\"}");
    }

    @Test
    public void testStringWithEscapedCharsToJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testStringWithEscapedCharsToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\\\"name\\\", \"supun\"}");
    }

    @Test(enabled = false)
    public void testJsonArrayToJsonCasting() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonArrayToJsonCasting");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test(enabled = false)
    public void testJsonToJsonArrayCasting() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonToJsonArrayCasting");
        Assert.assertTrue(returns[0] instanceof BJSON);

        Assert.assertEquals(returns[0].toString(), "[[1,2,3],[3,4,5],[7,8,9]]");
    }

    @Test(enabled = false)
    public void testJsonToJsonArrayInvalidCasting() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonToJsonArrayInvalidCasting");
        Assert.assertEquals(returns[0], null);

        Assert.assertTrue(returns[1] instanceof BStruct);
        String errorMsg = ((BStruct) returns[1]).getStringField(0);
        Assert.assertEquals(errorMsg, "'json' cannot be cast to 'json[][][]'");
    }

    @Test
    public void testJsonLength() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonLength");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
    }
    
    // TODO: fix the following test cases
    /*
        function testJsonArrayToJsonCasting () (json) {
            json[][] j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];
        
            var j2, _ = (json)j1;
            return j2;
        }
        
        function testJsonToJsonArrayCasting () (json[], json[][], TypeCastError) {
            json j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];
        
            var j2, e = (json[])j1;
            var j3, e = (json[][])j1;
        
            return j2, j3, e;
        }
        
        function testJsonToJsonArrayInvalidCasting () (json[][][], TypeCastError) {
            json j1 = [[1, 2, 3], [3, 4, 5], [7, 8, 9]];
        
            var j2, e = (json[][][])j1;
        
            return j2, e;
        }
     */
}
