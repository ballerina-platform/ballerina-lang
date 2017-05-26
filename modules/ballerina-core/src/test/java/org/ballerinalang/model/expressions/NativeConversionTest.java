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
package org.ballerinalang.model.expressions;

import com.fasterxml.jackson.databind.JsonNode;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for type conversion.
 */
public class NativeConversionTest {
    private static final double DELTA = 0.01;
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/type/conversion/native-conversion.bal");
    }
    
    @Test
    public void testStructToMap() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructToMap");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<BString, ?> map = (BMap<BString, ?>) returns[0];

        BValue name = map.get(new BString("name"));
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Child");

        BValue age = map.get(new BString("age"));
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue parent = map.get(new BString("parent"));
        Assert.assertTrue(parent instanceof BStruct);
        BStruct parentStruct = (BStruct) parent;
        Assert.assertEquals(((BString) parentStruct.getValue(0)).stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parentStruct.getValue(1)).intValue(), 50);

        BValue address = map.get(new BString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<BString, ?> addressMap = (BMap<BString, ?>) address;
        Assert.assertEquals(addressMap.get(new BString("city")).stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get(new BString("country")).stringValue(), "SriLanka");

        BValue info = map.get(new BString("info"));
        Assert.assertTrue(info instanceof BJSON);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue marks = map.get(new BString("marks"));
        Assert.assertTrue(marks instanceof BArray);
        BArray<BInteger> marksArray = (BArray<BInteger>) marks;
        Assert.assertEquals(marksArray.get(0).intValue(), 67);
        Assert.assertEquals(marksArray.get(1).intValue(), 38);
        Assert.assertEquals(marksArray.get(2).intValue(), 91);
    }

    @Test
    public void testMapToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testMapToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct struct = (BStruct) returns[0];

        BValue name = struct.getValue(0);
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Child");

        BValue age = struct.getValue(1);
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue parent = struct.getValue(2);
        Assert.assertTrue(parent instanceof BStruct);
        BStruct parentStruct = (BStruct) parent;
        Assert.assertEquals(((BString) parentStruct.getValue(0)).stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parentStruct.getValue(1)).intValue(), 50);
        Assert.assertEquals(parentStruct.getValue(2), null);
        Assert.assertEquals(parentStruct.getValue(3), null);
        Assert.assertEquals(parentStruct.getValue(4), null);
        Assert.assertEquals(parentStruct.getValue(5), null);

        BValue info = struct.getValue(3);
        Assert.assertTrue(info instanceof BJSON);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue address = struct.getValue(4);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<BString, ?> addressMap = (BMap<BString, ?>) address;
        Assert.assertEquals(addressMap.get(new BString("city")).stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get(new BString("country")).stringValue(), "SriLanka");

        BValue marks = struct.getValue(5);
        Assert.assertTrue(marks instanceof BArray);
        BArray<BInteger> marksArray = (BArray<BInteger>) marks;
        Assert.assertEquals(marksArray.get(0).intValue(), 87);
        Assert.assertEquals(marksArray.get(1).intValue(), 94);
        Assert.assertEquals(marksArray.get(2).intValue(), 72);
    }
    
    @Test
    public void testJsonToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct struct = (BStruct) returns[0];

        BValue name = struct.getValue(0);
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Child");

        BValue age = struct.getValue(1);
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue parent = struct.getValue(2);
        Assert.assertTrue(parent instanceof BStruct);
        BStruct parentStruct = (BStruct) parent;
        Assert.assertEquals(((BString) parentStruct.getValue(0)).stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parentStruct.getValue(1)).intValue(), 50);
        Assert.assertEquals(parentStruct.getValue(2), null);
        Assert.assertEquals(parentStruct.getValue(3), null);
        Assert.assertEquals(parentStruct.getValue(4), null);
        Assert.assertEquals(parentStruct.getValue(5), null);

        BValue info = struct.getValue(3);
        Assert.assertTrue(info instanceof BJSON);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue address = struct.getValue(4);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<BString, ?> addressMap = (BMap<BString, ?>) address;
        Assert.assertEquals(addressMap.get(new BString("city")).stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get(new BString("country")).stringValue(), "SriLanka");

        BValue marks = struct.getValue(5);
        Assert.assertTrue(marks instanceof BArray);
        BArray<BInteger> marksArray = (BArray<BInteger>) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0).intValue(), 56);
        Assert.assertEquals(marksArray.get(1).intValue(), 79);
    }
    
    @Test
    public void testStructToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        JsonNode child = ((BJSON) returns[0]).value();
        Assert.assertEquals(child.get("name").textValue(), "Child");
        Assert.assertEquals(child.get("age").intValue(), 25);

        JsonNode parent = child.get("parent");
        Assert.assertTrue(parent.isObject());
        Assert.assertEquals(parent.get("name").textValue(), "Parent");
        Assert.assertEquals(parent.get("age").intValue(), 50);
        Assert.assertTrue(parent.get("parent").isNull());
        Assert.assertTrue(parent.get("info").isNull());
        Assert.assertTrue(parent.get("address").isNull());
        Assert.assertTrue(parent.get("marks").isNull());

        JsonNode info = child.get("info");
        Assert.assertTrue(info.isObject());
        Assert.assertEquals(info.get("status").textValue(), "single");

        JsonNode address = child.get("address");
        Assert.assertTrue(info.isObject());
        Assert.assertEquals(address.get("country").textValue(), "SriLanka");
        Assert.assertEquals(address.get("city").textValue(), "Colombo");

        JsonNode marks = child.get("marks");
        Assert.assertTrue(marks.isArray());
        Assert.assertEquals(marks.size(), 3);
        Assert.assertEquals(marks.get(0).intValue(), 87);
        Assert.assertEquals(marks.get(1).intValue(), 94);
        Assert.assertEquals(marks.get(2).intValue(), 72);
    }
    
    @Test(description = "Test converting a struct to a struct", 
            expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "struct-to-struct-conversion.bal:26: incompatible types: 'Person' " +
            "cannot be converted to 'Student'")
    public void testStructToStruct() {
        BTestUtils.parseBalFile("lang/expressions/type/conversion/struct-to-struct-conversion.bal");
    }
    
    @Test(description = "Test converting a map with missing field to a struct", 
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = "cannot cast 'map' to type 'Person: error while mapping 'parent': no" +
            " such field found")
    public void testIncompatibleMapToStruct() {
        BLangFunctions.invoke(bLangProgram, "testIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a map with incompatible inner array to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'map' to type 'Person: error while mapping 'marks': " +
            "incompatible types: expected 'int\\[\\]', found 'float\\[\\]'")
    public void testMapWithIncompatibleArrayToStruct() {
        BLangFunctions.invoke(bLangProgram, "testMapWithIncompatibleArrayToStruct");
    }

    @Test(description = "Test converting a map with incompatible inner struct to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'map' to type 'Employee: error while mapping 'partner':" +
            " incompatible types: expected 'Person', found 'Student'")
    public void testMapWithIncompatibleStructToStruct() {
        BLangFunctions.invoke(bLangProgram, "testMapWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a incompatible JSON to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'Person': error while mapping 'parent': " +
            "no such field found")
    public void testIncompatibleJsonToStruct() {
        BLangFunctions.invoke(bLangProgram, "testIncompatibleJsonToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner map to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'Person': error while mapping " +
            "'address': incompatible types: expected 'json-object', found 'string'")
    public void testJsonWithIncompatibleMapToStruct() {
        BLangFunctions.invoke(bLangProgram, "testJsonWithIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner struct to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'Person': error while mapping 'parent': " +
            "incompatible types: expected 'json-object', found 'string'")
    public void testJsonWithIncompatibleStructToStruct() {
        BLangFunctions.invoke(bLangProgram, "testJsonWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a JSON array to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'Person': incompatible types: expected " +
            "'json-object', found 'json-array'")
    public void testJsonArrayToStruct() {
        BLangFunctions.invoke(bLangProgram, "testJsonArrayToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner type to a struct", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'Person': error while mapping 'age': " +
            "incompatible types: expected 'int', found 'float' in json")
    public void testJsonWithIncompatibleTypeToStruct() {
        BLangFunctions.invoke(bLangProgram, "testJsonWithIncompatibleTypeToStruct");
    }

    @Test(description = "Test converting a struct with inner XML to a JSON", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'Info' to type 'json': error while mapping 'name': " +
            "incompatible types: expected 'json', found 'xml'")
    public void testStructWithXmlToJson() {
        BLangFunctions.invoke(bLangProgram, "testStructWithXmlToJson");
    }

    @Test(description = "Test converting a JSON array to any array")
    public void testJsonToAnyArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToAnyArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BArray<BValue> array = (BArray<BValue>) anyArrayStruct.getValue(0);

        Assert.assertEquals(((BInteger) array.get(0)).intValue(), 4);
        Assert.assertEquals(array.get(1).stringValue(), "Supun");
        Assert.assertEquals(((BFloat) array.get(2)).floatValue(), 5.36);
        Assert.assertEquals(((BBoolean) array.get(3)).booleanValue(), true);
        Assert.assertEquals(((BJSON) array.get(4)).stringValue(), "{\"lname\":\"Setunga\"}");
        Assert.assertEquals(((BJSON) array.get(5)).stringValue(), "[4,3,7]");
        Assert.assertEquals(array.get(6), null);
    }

    @Test(description = "Test converting a JSON array to int array")
    public void testJsonToIntArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToIntArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BArray<BValue> array = (BArray<BValue>) anyArrayStruct.getValue(0);

        Assert.assertEquals(array.getType().getName(), "int[]");
        Assert.assertEquals(((BInteger) array.get(0)).intValue(), 4);
        Assert.assertEquals(((BInteger) array.get(1)).intValue(), 3);
        Assert.assertEquals(((BInteger) array.get(2)).intValue(), 9);
    }

    @Test(description = "Test converting a JSON string array to string array")
    public void testJsonToStringArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToStringArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BArray<BValue> array = (BArray<BValue>) anyArrayStruct.getValue(0);

        Assert.assertEquals(array.getType().getName(), "string[]");
        Assert.assertEquals(((BString) array.get(0)).stringValue(), "a");
        Assert.assertEquals(((BString) array.get(1)).stringValue(), "b");
        Assert.assertEquals(((BString) array.get(2)).stringValue(), "c");
    }

    @Test(description = "Test converting a JSON integer array to string array")
    public void testJsonIntArrayToStringArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonIntArrayToStringArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BArray<BValue> array = (BArray<BValue>) anyArrayStruct.getValue(0);

        Assert.assertEquals(array.getType().getName(), "string[]");
        Assert.assertEquals(((BString) array.get(0)).stringValue(), "4");
        Assert.assertEquals(((BString) array.get(1)).stringValue(), "3");
        Assert.assertEquals(((BString) array.get(2)).stringValue(), "9");
    }

    @Test(description = "Test converting a JSON array to xml array", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'XmlArray': error while mapping 'a': " +
            "incompatible types: expected 'xml', found 'string'")
    public void testJsonToXmlArray() {
        BLangFunctions.invoke(bLangProgram, "testJsonToXmlArray");
    }

    @Test(description = "Test converting a JSON integer array to string array")
    public void testNullJsonToArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullJsonToArray");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a JSON null to string array")
    public void testNullJsonArrayToArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullJsonArrayToArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BArray<BValue> array = (BArray<BValue>) anyArrayStruct.getValue(0);

        Assert.assertEquals(array, null);
    }

    @Test(description = "Test converting a JSON string to string array", 
            expectedExceptions = { BallerinaException.class }, 
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'StringArray': error while mapping 'a': " +
            "incompatible types: expected 'json-array', found 'string'")
    public void testNonArrayJsonToArray() {
        BLangFunctions.invoke(bLangProgram, "testNonArrayJsonToArray");
    }

    @Test(description = "Test converting a null JSON to struct")
    public void testNullJsonToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullJsonToStruct");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a null map to Struct")
    public void testNullMapToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullMapToStruct");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test(description = "Test converting a null Struct to json")
    public void testNullStructToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullStructToJson");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a null Struct to map")
    public void testNullStructToMap() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullStructToMap");
        Assert.assertEquals(returns[0], null);
    }
    
    // transform with errors
    
    @Test
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testIncompatibleJsonToStructWithErrors", 
                new BValue[]{});
        
        // check whether struct is null
        Assert.assertNull(returns[0]);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'json' to type 'Person': error while mapping" +
            " 'parent': incompatible types: expected 'json-object', found 'string'");
    }
}
