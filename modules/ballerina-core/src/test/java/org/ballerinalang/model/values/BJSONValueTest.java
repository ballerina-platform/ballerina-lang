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
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class BJSONValueTest   {

    private BLangProgram bLangProgram;
    private static final double DELTA = 0.01;
    
    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/values/json-value.bal");
    }
    
    @Test(description = "Test initializing json with a string")
    public void testStringAsJsonVal() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStringAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asText(), "Supun");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Supun");
    }
    
    @Test(description = "Test initializing json with an integer")
    public void testIntAsJsonVal() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testIntAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asInt(), 5);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 5);
    }
    
    @Test(description = "Test initializing json with a float")
    public void testFloatAsJsonVal() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testFloatAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asDouble(), 7.65);

        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 7.65, DELTA);
    }
    
    @Test(description = "Test initializing json with a boolean")
    public void testBooleanAsJsonVal() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testBooleanAsJsonVal");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.value().asBoolean(), true);

        Assert.assertTrue(returns[1] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
    }
    
    @Test(description = "Test initializing json with a null")
    public void testNullAsJsonVal() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullAsJsonVal");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test(description = "Test inline initializing of a json")
    public void testNestedJsonInit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNestedJsonInit");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON person = ((BJSON) returns[0]);
        Assert.assertEquals(person.toString(), "{\"name\":\"aaa\",\"age\":25," +
            "\"parent\":{\"name\":\"bbb\",\"age\":50},\"address\":{\"city\":\"Colombo\",\"country\":\"SriLanka\"}," +
            "\"array\":[1,5,7]}");
    }
    
    @Test
    public void testJsonWithNull() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonWithNull");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"name\":null}");

        Assert.assertEquals(returns[1], null);
    }
    
    @Test
    public void testGetString() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertEquals(returns[1].stringValue(), "Setunga");
    }
    
    @Test
    public void testGetInt() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 43);
    }
    
    @Test
    public void testGetFloat() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.73, DELTA);
    }
    
    @Test
    public void testGetBoolean() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test
    public void testGetJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }
    
    @Test
    public void testGetNonExistingElement() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetNonExistingElement");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test
    public void testAddString() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAddString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }
    
    @Test
    public void testAddInt() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAddInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }
    
    @Test
    public void testAddFloat() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAddFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }
    
    @Test
    public void testAddBoolean() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAddBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }
    
    @Test
    public void testAddJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAddJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }
    
    @Test
    public void testUpdateString() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateString");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"lname\":\"Setunga\"}");
    }
    
    @Test
    public void testUpdateInt() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateInt");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"age\":25}");
    }
    
    @Test
    public void testUpdateFloat() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateFloat");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"score\":4.37}");
    }
    
    @Test
    public void testUpdateBoolean() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateBoolean");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"status\":true}");
    }
    
    @Test
    public void testUpdateJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"fname\":\"Supun\",\"address\":{\"country\":\"SriLanka\"}}");
    }
    
    @Test
    public void testUpdateStringInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateStringInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",\"d\",\"c\"]");
    }
    
    @Test
    public void testUpdateIntInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateIntInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",64,\"c\"]");
    }
    
    @Test
    public void testUpdateFloatInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateFloatInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",4.72,\"c\"]");
    }
    
    @Test
    public void testUpdateBooleanInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateBooleanInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",true,\"c\"]");
    }
    
    @Test
    public void testUpdateNullInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateNullInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",null,\"c\"]");
    }
    
    @Test
    public void testUpdateJsonInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateJsonInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",{\"country\":\"SriLanka\"},\"c\"]");
    }
    
    @Test
    public void testUpdateJsonArrayInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateJsonArrayInArray");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",[1,2,3],\"c\"]");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot get 'fname' from null")
    public void testGetFromNull() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetFromNull");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "[\"a\",[1,2,3],\"c\"]");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot set 'country' of null")
    public void testAddToNull() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAddToNull");
    }
    
    @Test
    public void testGetNestedJsonElement() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetNestedJsonElement");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "Colombo");
        
        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "Colombo");
    }
    
    @Test
    public void testJsonExprAsIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonExprAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "failed to set element to json: array index out of range: index: 7, " +
            "size: 3")
    public void testSetArrayOutofBoundElement() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSetArrayOutofBoundElement");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot set value to '7': expected a 'json-array', but found " +
            "'json-object'")
    public void testSetToNonArrayWithIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSetToNonArrayWithIndex");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot get value from '7': expected a 'json-array', but found " +
            "'json-object'")
    public void testGetFromNonArrayWithIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetFromNonArrayWithIndex");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot set value to 'name': expected a 'json-object', but found " +
            "'json-array'")
    public void testSetToNonObjectWithKey() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSetToNonObjectWithKey");
    }
    
    public void testGetFromNonObjectWithKey() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetFromNonObjectWithKey");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test
    public void testGetStringInArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetStringInArray");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "b");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "failed to get element from json: array index out of range: index: 5, " +
            "size: 3")
    public void testGetArrayOutofBoundElement() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetArrayOutofBoundElement");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot get value from 'fname': expected a 'json-object', but found " +
            "'string'")
    public void testGetStringFromPrimitive() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetStringFromPrimitive");
    }
    
    @Test
    public void testJsonArrayWithVariable() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonArrayWithVariable");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\",{\"name\":\"supun\"}]");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "json-array-with-unsupported-types.bal:3: incompatible types: " +
            "'message' cannot be converted to 'json'")
    public void testJsonArrayWithUnsupportedtypes() {
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/values/json-array-with-unsupported-types.bal");
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "json-init-with-unsupported-types.bal:3: incompatible types: " +
            "'message' cannot be converted to 'json'")
    public void testJsonInitWithUnsupportedtypes() {
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/values/json-init-with-unsupported-types.bal");
    }
    
    @Test
    public void testUpdateNestedElement() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testUpdateNestedElement");
        Assert.assertTrue(returns[0] instanceof BJSON);
        BJSON json = ((BJSON) returns[0]);
        Assert.assertEquals(json.toString(), "{\"details\":{\"fname\":\"Supun\",\"lname\":\"Setunga\"}}");
    }
}
