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
package org.ballerinalang.model.values;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class BJSONValueTest {

    private ProgramFile programFile;
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/values/json-value.bal");
    }

    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStringAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asText(), "Supun");
    }

    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIntAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asInt(), 5);
    }

    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testFloatAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asDouble(), 7.65);
    }

    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testBooleanAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asBoolean(), true);
    }

    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullAsJsonVal");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedJsonInit");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.toString(), "{\"name\":\"aaa\",\"age\":25," +
                "\"parent\":{\"name\":\"bbb\",\"age\":50},\"address\":{\"city\":\"Colombo\"," +
                "\"country\":\"SriLanka\"}," +
                "\"array\":[1,5,7]}");
    }

    @Test
    public void testJsonWithNull() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonWithNull");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\":null}");

        Assert.assertEquals(returns[1], null);
    }

    @Test
    public void testGetString() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertEquals(returns[1].stringValue(), "Setunga");
    }

    @Test
    public void testGetInt() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 43);
    }

    @Test
    public void testGetFloat() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.73, DELTA);
    }

    @Test
    public void testGetBoolean() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test
    public void testGetJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }

    @Test
    public void testGetNonExistingElement() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetNonExistingElement");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testAddString() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAddString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testAddInt() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAddInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testAddFloat() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAddFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testAddBoolean() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAddBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testAddJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testAddJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateString() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }

    @Test
    public void testUpdateInt() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }

    @Test
    public void testUpdateFloat() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }

    @Test
    public void testUpdateBoolean() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }

    @Test
    public void testUpdateJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }

    @Test
    public void testUpdateStringInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateStringInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",\"d\",\"c\"]");
    }

    @Test
    public void testUpdateIntInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateIntInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",64,\"c\"]");
    }

    @Test
    public void testUpdateFloatInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateFloatInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",4.72,\"c\"]");
    }

    @Test
    public void testUpdateBooleanInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateBooleanInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",true,\"c\"]");
    }

    @Test
    public void testUpdateNullInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateNullInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",null,\"c\"]");
    }

    @Test
    public void testUpdateJsonInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateJsonInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",{\"country\":\"SriLanka\"},\"c\"]");
    }

    @Test
    public void testUpdateJsonArrayInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",[1,2,3],\"c\"]");
    }

    @Test
    public void testGetNestedJsonElement() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetNestedJsonElement");
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
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonExprAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*failed to set element to json: array index out of range: index: 7, " +
                    "size: 3.*")
    public void testSetArrayOutofBoundElement() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetArrayOutofBoundElement");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot set value to '7': expected a 'json-array', but found " +
                    "'json-object'.*")
    public void testSetToNonArrayWithIndex() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetToNonArrayWithIndex");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot get value from '7': expected a 'json-array', but found " +
                    "'json-object'.*")
    public void testGetFromNonArrayWithIndex() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetFromNonArrayWithIndex");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot set value to 'name': expected a 'json-object', but found " +
                    "'json-array'.*")
    public void testSetToNonObjectWithKey() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetToNonObjectWithKey");
    }

    public void testGetFromNonObjectWithKey() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetFromNonObjectWithKey");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testGetStringInArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetStringInArray");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "b");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*failed to get element from json: array index out of " +
                    "range: index: 5, size: 3.*")
    public void testGetArrayOutofBoundElement() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetArrayOutofBoundElement");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot get value from 'fname': expected a 'json-object', but found " +
                    "'string'.*")
    public void testGetStringFromPrimitive() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetStringFromPrimitive");
    }

//    @Test
//    public void testJsonArrayWithVariable() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonArrayWithVariable");
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\",{\"name\":\"supun\"}]");
//    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "json-array-with-unsupported-types.bal:3: incompatible types: " +
                    "'message' cannot be converted to 'json'")
    public void testJsonArrayWithUnsupportedtypes() {
        BTestUtils.parseBalFile("lang/values/json-array-with-unsupported-types.bal");
    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "json-init-with-unsupported-types.bal:3: incompatible types: " +
                    "'message' cannot be converted to 'json'")
    public void testJsonInitWithUnsupportedtypes() {
        BTestUtils.parseBalFile("lang/values/json-init-with-unsupported-types.bal");
    }

    @Test
    public void testUpdateNestedElement() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateNestedElement");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"details\":{\"fname\":\"Supun\",\"lname\":\"Setunga\"}}");
    }

    @Test
    public void testEmptyStringToJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testEmptyStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertTrue(json.toString().isEmpty());
    }

    @Test
    public void testJsonStringToJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\", \"supun\"}");
    }

    @Test
    public void testStringWithEscapedCharsToJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStringWithEscapedCharsToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\\\"name\\\", \"supun\"}");
    }
}
