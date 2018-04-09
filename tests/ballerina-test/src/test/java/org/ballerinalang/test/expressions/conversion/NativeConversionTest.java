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
package org.ballerinalang.test.expressions.conversion;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonNode;
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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for type conversion.
 */
@Test
public class NativeConversionTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;
    private CompileResult packageResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion-negative.bal");
        packageResult = BCompileUtil.compile(this, "test-src/expressions/conversion/", "a.b");
    }

    @Test
    public void testStructToMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToMap");
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

    @Test(enabled = false)
    public void testMapToStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapToStruct");
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStruct");
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        JsonNode child = ((BJSON) returns[0]).value();
        Assert.assertEquals(child.get("name").stringValue(), "Child");
        Assert.assertEquals(child.get("age").longValue(), 25);

        JsonNode parent = child.get("parent");
        Assert.assertTrue(parent.isObject());
        Assert.assertEquals(parent.get("name").stringValue(), "Parent");
        Assert.assertEquals(parent.get("age").longValue(), 50);
        Assert.assertTrue(parent.get("parent").isNull());
        Assert.assertTrue(parent.get("info").isNull());
        Assert.assertTrue(parent.get("address").isNull());
        Assert.assertTrue(parent.get("marks").isNull());

        JsonNode info = child.get("info");
        Assert.assertTrue(info.isObject());
        Assert.assertEquals(info.get("status").stringValue(), "single");

        JsonNode address = child.get("address");
        Assert.assertTrue(info.isObject());
        Assert.assertEquals(address.get("country").stringValue(), "SriLanka");
        Assert.assertEquals(address.get("city").stringValue(), "Colombo");

        JsonNode marks = child.get("marks");
        Assert.assertTrue(marks.isArray());
        Assert.assertEquals(marks.size(), 3);
        Assert.assertEquals(marks.get(0).longValue(), 87);
        Assert.assertEquals(marks.get(1).longValue(), 94);
        Assert.assertEquals(marks.get(2).longValue(), 72);
    }
    
    @Test
    public void testStructToJsonConstrained1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToJsonConstrained1");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }
    
    @Test
    public void testStructToJsonConstrained2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToJsonConstrained2");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot convert 'Person2' to type 'json<Person3>'.*")
    public void testStructToJsonConstrainedNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToJsonConstrainedNegative");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test(description = "Test converting a map to json")
    public void testMapToJsonConversionError() {
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: 'map' cannot be convert to 'json'", 36, 15);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'map' to type 'Person: error while mapping 'info': " +
                  "incompatible types: expected 'json', found 'map'.*")
    public void testIncompatibleMapToStruct() {
        BRunUtil.invoke(compileResult, "testIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a map with missing field to a struct", enabled = false)
    public void testMapWithMissingFieldsToStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapWithMissingFieldsToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct personStruct = (BStruct) returns[0];

        Assert.assertEquals(personStruct.stringValue(), "{name:\"Child\", age:25, parent:null, info:null, " +
                "address:{\"city\":\"Colombo\", \"country\":\"SriLanka\"}, marks:[87, 94, 72], a:null, score:0.0, " +
                "alive:false, children:null}");
    }

    @Test(description = "Test converting a map with incompatible inner array to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'map' to type 'Person: error while mapping 'marks': " +
                  "incompatible types: expected 'int\\[\\]', found 'float\\[\\]'.*", enabled = false)
    //TODO Enable test
    public void testMapWithIncompatibleArrayToStruct() {
        BRunUtil.invoke(compileResult, "testMapWithIncompatibleArrayToStruct");
    }

    @Test(description = "Test converting a map with incompatible inner struct to a struct",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'map' to type 'Employee: error while mapping" +
                    " 'partner': incompatible types: expected 'Person', found 'Student'.*")
    public void testMapWithIncompatibleStructToStruct() {
        BRunUtil.invoke(compileResult, "testMapWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a incompatible JSON to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while " +
                  "mapping 'age': incompatible types: expected 'int', found 'string' in json.*")
    public void testIncompatibleJsonToStruct() {
        BRunUtil.invoke(compileResult, "testIncompatibleJsonToStruct");
    }

    @Test(description = "Test converting a incompatible JSON to a struct")
    public void testJsonToStructWithMissingFields() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStructWithMissingFields");
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(returns[0].stringValue(), "{name:\"Child\", age:25, parent:null, " +
                "info:{\"status\":\"single\"}, address:{\"city\":\"Colombo\", \"country\":\"SriLanka\"}, " +
                "marks:[87, 94, 72], a:null, score:0.0, alive:false, children:null}");
    }

    @Test(description = "Test converting a JSON with incompatible inner map to a struct",
          expectedExceptions = {BLangRuntimeException.class}/*,
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while mapping " +
                  "'address': incompatible types: expected 'json-object', found 'string'.*"*/) 
    //TODO fix the expectedExceptionsMessageRegExp 
    public void testJsonWithIncompatibleMapToStruct() {
        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner struct to a struct",
          expectedExceptions = {BLangRuntimeException.class}/*,
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while " +
                  "mapping 'parent': incompatible types: expected 'json-object', found 'string'.*"*/)
    //TODO fix the expectedExceptionsMessageRegExp
    public void testJsonWithIncompatibleStructToStruct() {
        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a JSON array to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': incompatible " +
                  "types: expected 'json-object', found 'json-array'.*")
    public void testJsonArrayToStruct() {
        BRunUtil.invoke(compileResult, "testJsonArrayToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner type to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'Person': error while mapping 'age': " +
                  "incompatible types: expected 'int', found 'float' in json.*")
    public void testJsonWithIncompatibleTypeToStruct() {
        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleTypeToStruct");
    }

    @Test(description = "Test converting a struct with map of blob to a JSON",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert 'Info' to type 'json': error while mapping 'bar': " +
                    "incompatible types: expected 'json', found 'blob'.*")
    public void testStructWithIncompatibleTypeMapToJson() {
        BRunUtil.invoke(compileResult, "testStructWithIncompatibleTypeMapToJson");
    }

    // TODO: certain types can be validated during the compile time. Validate those and throw semantic errors
    @Test(description = "Test converting a struct with map of blob to a JSON", enabled = false)
    public void testStructWithIncompatibleTypeToJson() {
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: 'Info' cannot be convert to 'json'", 48, 10);
    }

    @Test(description = "Test converting a JSON array to any array")
    public void testJsonToAnyArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToAnyArray");
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToIntArray");
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStringArray");
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonIntArrayToStringArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BStringArray array = (BStringArray) anyArrayStruct.getRefField(0);

        Assert.assertEquals(array.getType().toString(), "string[]");
        Assert.assertEquals(array.get(0), "4");
        Assert.assertEquals(array.get(1), "3");
        Assert.assertEquals(array.get(2), "9");
    }

    @Test(description = "Test converting a JSON array to xml array",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'XmlArray': error while mapping 'a': " +
                  "incompatible types: expected 'xml', found 'string'.*")
    public void testJsonToXmlArray() {
        BRunUtil.invoke(compileResult, "testJsonToXmlArray");
    }

    @Test(description = "Test converting a JSON integer array to string array",
            expectedExceptions = { BLangRuntimeException.class }/*,
            expectedExceptionsMessageRegExp = "error: ballerina.runtime:NullReferenceException.*"*/, enabled = false)
    //TODO fix expectedExceptionsMessageRegExp
    public void testNullJsonToArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullJsonToArray");
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test converting a JSON null to string array", enabled = false)
    public void testNullJsonArrayToArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullJsonArrayToArray");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct anyArrayStruct = (BStruct) returns[0];
        BStringArray array = (BStringArray) anyArrayStruct.getRefField(0);

        Assert.assertEquals(array, null);
    }

    @Test(description = "Test converting a JSON string to string array",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*cannot convert 'json' to type 'StringArray': error while " +
                  "mapping 'a': incompatible types: expected 'json-array', found 'string'.*")
    public void testNonArrayJsonToArray() {
        BRunUtil.invoke(compileResult, "testNonArrayJsonToArray");
    }

    @Test(description = "Test converting a null JSON to struct", expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: ballerina.runtime:NullReferenceException.*", enabled = false)
    public void testNullJsonToStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullJsonToStruct");
    }

    @Test(description = "Test converting a null map to Struct", expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: ballerina.runtime:NullReferenceException.*", enabled = false)
    public void testNullMapToStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullMapToStruct");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test converting a null Struct to json", expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: ballerina.runtime:NullReferenceException.*", enabled = false)
    public void testNullStructToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullStructToJson");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test converting a null Struct to map", expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: ballerina.runtime:NullReferenceException.*", enabled = false)
    public void testNullStructToMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullStructToMap");
        Assert.assertNull(returns[0]);
        Assert.assertNotNull(returns[1]);
    }

    // transform with errors

    @Test
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIncompatibleJsonToStructWithErrors",
                                             new BValue[]{});

        // check the error
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct error = (BStruct) returns[0];
        String errorMsg = error.getStringField(0);
        //TODO fix the error message checking; 
        //Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'Person': error while mapping" +
        //        " 'parent': incompatible types: expected 'json-object', found 'string'");
    }

    // Todo - Fix casting issue
    @Test(enabled = false)
    public void testStructToMapWithRefTypeArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToMapWithRefTypeArray");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, ?> map = (BMap<String, ?>) returns[0];

        BValue name = map.get("title");
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "The Revenant");

        BValue age = map.get("year");
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 2015);

        BValue marks = map.get("genre");
        Assert.assertTrue(marks instanceof BStringArray);
        BStringArray genreArray = (BStringArray) marks;
        Assert.assertEquals(genreArray.get(0), "Adventure");
        Assert.assertEquals(genreArray.get(1), "Drama");
        Assert.assertEquals(genreArray.get(2), "Thriller");

        BValue actors = map.get("actors");
        Assert.assertTrue(actors instanceof BRefValueArray);
        BRefValueArray actorsArray = (BRefValueArray) actors;
        Assert.assertTrue(actorsArray.get(0) instanceof BStruct);
        Assert.assertEquals(actorsArray.get(0).stringValue(), "{fname:\"Leonardo\", lname:\"DiCaprio\", age:35}");
    }

    @Test
    public void testStructWithStringArrayToJSON() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructWithStringArrayToJSON");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"names\":[\"John\",\"Doe\"]}");
    }

    @Test(enabled = false)
    public void testEmptyJSONtoStructWithDefaults() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyJSONtoStructWithDefaults");
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "string value");
        Assert.assertEquals(((BStruct) returns[0]).getIntField(0), 45);
        Assert.assertEquals(((BStruct) returns[0]).getFloatField(0), 5.3);
        Assert.assertEquals(((BStruct) returns[0]).getBooleanField(0), 1);
        Assert.assertEquals(((BStruct) returns[0]).getRefField(0), null);
        Assert.assertEquals(((BStruct) returns[0]).getBlobField(0).length, 0);
    }

    @Test//(enabled = false)
    public void testEmptyJSONtoStructWithoutDefaults() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyJSONtoStructWithoutDefaults");

        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "");
        Assert.assertEquals(((BStruct) returns[0]).getIntField(0), 0);
        Assert.assertEquals(((BStruct) returns[0]).getFloatField(0), 0.0);
        Assert.assertEquals(((BStruct) returns[0]).getBooleanField(0), 0);
        Assert.assertEquals(((BStruct) returns[0]).getRefField(0), null);
        Assert.assertEquals(((BStruct) returns[0]).getBlobField(0).length, 0);
    }

    @Test
    public void testEmptyMaptoStructWithDefaults() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyMaptoStructWithDefaults");
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(returns[0].stringValue(), "{s:\"string value\", a:45, f:5.3, b:true, j:null, blb:null}");
    }

    @Test
    public void testEmptyMaptoStructWithoutDefaults() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyMaptoStructWithoutDefaults");
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(returns[0].stringValue(), "{s:\"\", a:0, f:0.0, b:false, j:null, blb:null}");
    }

    @Test
    public void testSameTypeConversion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSameTypeConversion");
        Assert.assertTrue(returns[0] instanceof BInteger);
        int expected = 10;
        Assert.assertEquals(((BInteger) returns[0]).intValue(), expected);
    }
    
    @Test(enabled = false)
    public void testErrorOnConversions() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorOnConversions");
        Assert.assertNull(returns[0]);
        Assert.assertNull(returns[1]);
        Assert.assertNull(returns[2]);
    }

    @Test
    public void structWithComplexMapToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "structWithComplexMapToJson");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"foo\":{\"a\":4,\"b\":2.5,\"c\":true,\"d\":\"apple\"," +
                "\"e\":{\"foo\":\"bar\"},\"f\":{\"name\":\"\",\"age\":0},\"g\":[1,8,7],\"h\":null}}");
    }

    @Test
    public void structWithComplexArraysToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "structWithComplexArraysToJson");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"a\":[4,6,9],\"b\":[4.6,7.5],\"c\":[true,true,false]," +
                "\"d\":[\"apple\",\"orange\"],\"e\":[{},{}],\"f\":[{\"name\":\"\",\"age\":0}," +
                "{\"name\":\"\",\"age\":0}],\"g\":[{\"foo\":\"bar\"}]}");
    }

    @Test
    public void testJsonToStructInPackage() {
        BValue[] returns = BRunUtil.invoke(packageResult, "testJsonToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct struct = (BStruct) returns[0];

        String name = struct.getStringField(0);
        Assert.assertEquals(name, "John");

        long age = struct.getIntField(0);
        Assert.assertEquals(age, 30);

        BValue address = struct.getRefField(0);
        Assert.assertTrue(address instanceof BStruct);
        BStruct addressStruct = (BStruct) address;
        Assert.assertEquals(addressStruct.getStringField(0), "20 Palm Grove");
        Assert.assertEquals(addressStruct.getStringField(1), "Colombo 03");
        Assert.assertEquals(addressStruct.getStringField(2), "Sri Lanka");
    }
}
