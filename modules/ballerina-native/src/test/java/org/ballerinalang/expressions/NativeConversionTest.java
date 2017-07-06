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
package org.ballerinalang.expressions;

import com.fasterxml.jackson.databind.JsonNode;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for type conversion.
 */
public class NativeConversionTest {
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/type/conversion/native-conversion.bal");
    }

    @Test
    public void testStructToMap() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStructToMap");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, ?> map = (BMap<String, ?>) returns[0];

        BValue name = map.get("name");
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Child");

        BValue age = map.get("age");
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue parent = map.get("parent");
        Assert.assertTrue(parent instanceof BStruct);
        BStruct parentStruct = (BStruct) parent;
        Assert.assertEquals(parentStruct.getStringField(0), "Parent");
        Assert.assertEquals(parentStruct.getIntField(0), 50);

        BValue address = map.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValue info = map.get("info");
        Assert.assertTrue(info instanceof BJSON);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue marks = map.get("marks");
        Assert.assertTrue(marks instanceof BIntArray);
        BIntArray marksArray = (BIntArray) marks;
        Assert.assertEquals(marksArray.get(0), 67);
        Assert.assertEquals(marksArray.get(1), 38);
        Assert.assertEquals(marksArray.get(2), 91);
    }

    @Test
    public void testMapToStruct() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testMapToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct struct = (BStruct) returns[0];

        String name = struct.getStringField(0);
        Assert.assertEquals(name, "Child");

        long age = struct.getIntField(0);
        Assert.assertEquals(age, 25);

        BValue parent = struct.getRefField(0);
        Assert.assertTrue(parent instanceof BStruct);
        BStruct parentStruct = (BStruct) parent;
        Assert.assertEquals(parentStruct.getStringField(0), "Parent");
        Assert.assertEquals(parentStruct.getIntField(0), 50);
        Assert.assertEquals(parentStruct.getRefField(0), null);
        Assert.assertEquals(parentStruct.getRefField(1), null);
        Assert.assertEquals(parentStruct.getRefField(2), null);
        Assert.assertEquals(parentStruct.getRefField(3), null);

        BValue info = struct.getRefField(1);
        Assert.assertTrue(info instanceof BJSON);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue address = struct.getRefField(2);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValue marks = struct.getRefField(3);
        Assert.assertTrue(marks instanceof BIntArray);
        BIntArray marksArray = (BIntArray) marks;
        Assert.assertEquals(marksArray.get(0), 87);
        Assert.assertEquals(marksArray.get(1), 94);
        Assert.assertEquals(marksArray.get(2), 72);
    }

    @Test
    public void testJsonToStruct() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct struct = (BStruct) returns[0];

        String name = struct.getStringField(0);
        Assert.assertEquals(name, "Child");

        long age = struct.getIntField(0);
        Assert.assertEquals(age, 25);

        BValue parent = struct.getRefField(0);
        Assert.assertTrue(parent instanceof BStruct);
        BStruct parentStruct = (BStruct) parent;
        Assert.assertEquals(parentStruct.getStringField(0), "Parent");
        Assert.assertEquals(parentStruct.getIntField(0), 50);
        Assert.assertEquals(parentStruct.getRefField(0), null);
        Assert.assertEquals(parentStruct.getRefField(1), null);
        Assert.assertEquals(parentStruct.getRefField(2), null);
        Assert.assertEquals(parentStruct.getRefField(3), null);

        BValue info = struct.getRefField(1);
        Assert.assertTrue(info instanceof BJSON);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue address = struct.getRefField(2);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValue marks = struct.getRefField(3);
        Assert.assertTrue(marks instanceof BIntArray);
        BIntArray marksArray = (BIntArray) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0), 56);
        Assert.assertEquals(marksArray.get(1), 79);
    }

    @Test
    public void testStructToJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStructToJson");
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
            "cannot be converted to 'Student', try casting")
    public void testStructToStruct() {
        BTestUtils.getProgramFile("lang/expressions/type/conversion/struct-to-struct-conversion.bal");
    }

    @Test(description = "Test converting a map to json",
            expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "map-to-json-conversion-error.bal:7: incompatible types: 'map' " +
                    "cannot be converted to 'json'")
    public void testMapToJsonConversionError() {
        BTestUtils.getProgramFile("lang/expressions/type/conversion/map-to-json-conversion-error.bal");
    }
    
    @Test(description = "Test converting a map with missing field to a struct", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'map' to type 'Person: error while " +
                    "mapping 'parent': no such field found.*")
    public void testIncompatibleMapToStruct() {
        BLangFunctions.invokeNew(programFile, "testIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a map with incompatible inner array to a struct", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'map' to type 'Person: error while mapping 'marks': " +
            "incompatible types: expected 'int\\[\\]', found 'float\\[\\]'.*")
    public void testMapWithIncompatibleArrayToStruct() {
        BLangFunctions.invokeNew(programFile, "testMapWithIncompatibleArrayToStruct");
    }

    // TODO With the latest changes introduced to BLangVM, this test does not return an error
    // TODO Because Student struct is compatible with Person struct.
//    @Test(description = "Test converting a map with incompatible inner struct to a struct",
//            expectedExceptions = { BLangRuntimeException.class },
//            expectedExceptionsMessageRegExp = ".*cannot cast 'map' to type 'Employee:
// error while mapping 'partner':" +
//            " incompatible types: expected 'Person', found 'Student'.*")
    public void testMapWithIncompatibleStructToStruct() {
        BLangFunctions.invokeNew(programFile, "testMapWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a incompatible JSON to a struct", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while " +
                    "mapping 'parent': no such field found.*")
    public void testIncompatibleJsonToStruct() {
        BLangFunctions.invokeNew(programFile, "testIncompatibleJsonToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner map to a struct", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while mapping " +
            "'address': incompatible types: expected 'json-object', found 'string'.*")
    public void testJsonWithIncompatibleMapToStruct() {
        BLangFunctions.invokeNew(programFile, "testJsonWithIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner struct to a struct", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while " +
                    "mapping 'parent': incompatible types: expected 'json-object', found 'string'.*")
    public void testJsonWithIncompatibleStructToStruct() {
        BLangFunctions.invokeNew(programFile, "testJsonWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a JSON array to a struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': incompatible " +
                    "types: expected 'json-object', found 'json-array'.*")
    public void testJsonArrayToStruct() {
        BLangFunctions.invokeNew(programFile, "testJsonArrayToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner type to a struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while mapping 'age': " +
                    "incompatible types: expected 'int', found 'float' in json.*")
    public void testJsonWithIncompatibleTypeToStruct() {
        BLangFunctions.invokeNew(programFile, "testJsonWithIncompatibleTypeToStruct");
    }

    @Test(description = "Test converting a struct with inner XML to a JSON",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot convert 'Info' to type 'json': error while mapping 'msg': " +
                    "incompatible types: expected 'json', found 'message'.*")
    public void testStructWithMessageToJson() {
        BLangFunctions.invokeNew(programFile, "testStructWithMessageToJson");
    }

    @Test(description = "Test converting a JSON array to any array")
    public void testJsonToAnyArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonToAnyArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BRefValueArray array = (BRefValueArray) anyArrayStruct.getRefField(0);

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
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonToIntArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BIntArray array = (BIntArray) anyArrayStruct.getRefField(0);

        Assert.assertEquals(array.getType().toString(), "int[]");
        Assert.assertEquals(array.get(0), 4);
        Assert.assertEquals(array.get(1), 3);
        Assert.assertEquals(array.get(2), 9);
    }

    @Test(description = "Test converting a JSON string array to string array")
    public void testJsonToStringArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonToStringArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BStringArray array = (BStringArray) anyArrayStruct.getRefField(0);

        Assert.assertEquals(array.getType().toString(), "string[]");
        Assert.assertEquals(array.get(0), "a");
        Assert.assertEquals(array.get(1), "b");
        Assert.assertEquals(array.get(2), "c");
    }

    @Test(description = "Test converting a JSON integer array to string array")
    public void testJsonIntArrayToStringArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testJsonIntArrayToStringArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BStringArray array = (BStringArray) anyArrayStruct.getRefField(0);

        Assert.assertEquals(array.getType().toString(), "string[]");
        Assert.assertEquals(array.get(0), "4");
        Assert.assertEquals(array.get(1), "3");
        Assert.assertEquals(array.get(2), "9");
    }

    @Test(description = "Test converting a JSON array to xml array", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'XmlArray': error while mapping 'a': " +
            "incompatible types: expected 'xml', found 'string'.*")
    public void testJsonToXmlArray() {
        BLangFunctions.invokeNew(programFile, "testJsonToXmlArray");
    }

    @Test(description = "Test converting a JSON integer array to string array")
    public void testNullJsonToArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullJsonToArray");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a JSON null to string array")
    public void testNullJsonArrayToArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullJsonArrayToArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BStringArray array = (BStringArray) anyArrayStruct.getRefField(0);

        Assert.assertEquals(array, null);
    }

    @Test(description = "Test converting a JSON string to string array", 
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'StringArray': error while " +
                    "mapping 'a': incompatible types: expected 'json-array', found 'string'.*")
    public void testNonArrayJsonToArray() {
        BLangFunctions.invokeNew(programFile, "testNonArrayJsonToArray");
    }

    @Test(description = "Test converting a null JSON to struct")
    public void testNullJsonToStruct() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullJsonToStruct");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a null map to Struct")
    public void testNullMapToStruct() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullMapToStruct");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test(description = "Test converting a null Struct to json")
    public void testNullStructToJson() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullStructToJson");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a null Struct to map")
    public void testNullStructToMap() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNullStructToMap");
        Assert.assertEquals(returns[0], null);
    }
    
    // transform with errors
    
    @Test
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIncompatibleJsonToStructWithErrors",
                new BValue[]{});
        
        // check whether struct is null
        Assert.assertNull(returns[0]);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'Person': error while mapping" +
            " 'parent': incompatible types: expected 'json-object', found 'string'");
    }
}
