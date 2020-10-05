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

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Test Cases for type conversion.
 */
public class NativeConversionTest {

    private CompileResult compileResult;

    private CompileResult packageResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion.bal");
        packageResult = BCompileUtil.compile(this, "test-src/expressions/conversion/TestProj", "a.b");
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
        Assert.assertTrue(parent instanceof BMap);
        BMap<String, BValue> parentStruct = (BMap<String, BValue>) parent;
        Assert.assertEquals(parentStruct.get("name").stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parentStruct.get("age")).intValue(), 50);

        BValue address = map.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValue info = map.get("info");
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue marks = map.get("marks");
        Assert.assertTrue(marks instanceof BValueArray);
        BValueArray marksArray = (BValueArray) marks;
        Assert.assertEquals(marksArray.getInt(0), 67);
        Assert.assertEquals(marksArray.getInt(1), 38);
        Assert.assertEquals(marksArray.getInt(2), 91);
    }

    @Test
    public void testMapToStruct() {
        testMapToStruct(compileResult, "testMapToStruct");
    }

    @Test
    public void testNestedMapToNestedStruct() {
        testMapToStruct(compileResult, "testNestedMapToNestedStruct");
    }

    private void testMapToStruct(CompileResult compileResult, String functionName) {
        BValue[] returns = BRunUtil.invoke(compileResult, functionName);
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> struct = (BMap<String, BValue>) returns[0];

        String name = struct.get("name").stringValue();
        Assert.assertEquals(name, "Child");

        long age = ((BInteger) struct.get("age")).intValue();
        Assert.assertEquals(age, 25);

        BValue parent = struct.get("parent");
        Assert.assertTrue(parent instanceof BMap);
        BMap<String, BValue> parentStruct = (BMap<String, BValue>) parent;
        Assert.assertEquals(parentStruct.get("name").stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parentStruct.get("age")).intValue(), 50);
        Assert.assertNull(parentStruct.get("parent"));
        Assert.assertNull(parentStruct.get("info"));
        Assert.assertNull(parentStruct.get("address"));
        Assert.assertNull(parentStruct.get("marks"));

        BValue info = struct.get("info");
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue address = struct.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValue marks = struct.get("marks");
        Assert.assertTrue(marks instanceof BValueArray);
        BValueArray marksArray = (BValueArray) marks;
        Assert.assertEquals(marksArray.getInt(0), 87);
        Assert.assertEquals(marksArray.getInt(1), 94);
        Assert.assertEquals(marksArray.getInt(2), 72);
    }

    @Test
    public void testJsonToStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStruct");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> struct = (BMap<String, BValue>) returns[0];

        String name = struct.get("name").stringValue();
        Assert.assertEquals(name, "Child");

        long age = ((BInteger) struct.get("age")).intValue();
        Assert.assertEquals(age, 25);

        BValue parent = struct.get("parent");
        Assert.assertTrue(parent instanceof BMap);
        BMap<String, BValue> parentStruct = (BMap<String, BValue>) parent;
        Assert.assertEquals(parentStruct.get("name").stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parentStruct.get("age")).intValue(), 50);
        Assert.assertNull(parentStruct.get("parent"));
        Assert.assertNull(parentStruct.get("info"));
        Assert.assertNull(parentStruct.get("address"));
        Assert.assertNull(parentStruct.get("marks"));

        BValue info = struct.get("info");
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.stringValue(), "{\"status\":\"single\"}");

        BValue address = struct.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Colombo");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValue marks = struct.get("marks");
        Assert.assertTrue(marks instanceof BValueArray);
        BValueArray marksArray = (BValueArray) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 56);
        Assert.assertEquals(marksArray.getInt(1), 79);
    }

    @Test
    public void testStructToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructToJson");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> child = ((BMap<String, BValue>) returns[0]);
        Assert.assertEquals(child.get("name").stringValue(), "Child");
        Assert.assertEquals(((BInteger) child.get("age")).intValue(), 25);

        Assert.assertTrue(child.get("parent") instanceof BMap);
        BMap<String, BValue> parent = (BMap<String, BValue>) child.get("parent");
        Assert.assertEquals(parent.get("name").stringValue(), "Parent");
        Assert.assertEquals(((BInteger) parent.get("age")).intValue(), 50);
        Assert.assertNull(parent.get("parent"));
        Assert.assertNull(parent.get("info"));
        Assert.assertNull(parent.get("address"));
        Assert.assertNull(parent.get("marks"));

        Assert.assertTrue(child.get("info") instanceof BMap);
        BMap<String, BValue> info = (BMap<String, BValue>) child.get("info");
        Assert.assertEquals(info.get("status").stringValue(), "single");

        Assert.assertTrue(child.get("address") instanceof BMap);
        BMap<String, BValue> address = (BMap<String, BValue>) child.get("address");
        Assert.assertEquals(address.get("country").stringValue(), "SriLanka");
        Assert.assertEquals(address.get("city").stringValue(), "Colombo");


        Assert.assertTrue(child.get("marks") instanceof BValueArray);
        BValueArray marks = (BValueArray) child.get("marks");
        Assert.assertEquals(marks.size(), 3);
        Assert.assertEquals(((BInteger) marks.getRefValue(0)).intValue(), 87);
        Assert.assertEquals(((BInteger) marks.getRefValue(1)).intValue(), 94);
        Assert.assertEquals(((BInteger) marks.getRefValue(2)).intValue(), 72);
    }
    
    @Test(description = "Test any type to anydata type at runtime")
    public void testAnyRecordToAnydataMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyRecordToAnydataMap");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(((BMap) returns[0]).get("name").stringValue(), "Waruna");
    }

    @Test(description = "Test converting a map to json")
    public void testMapToJsonConversion() {
        BValue[] result = BRunUtil.invoke(compileResult, "testComplexMapToJson");
        Assert.assertTrue(result[0] instanceof BMap);
        Assert.assertEquals(result[0].toString(), "{\"name\":\"Supun\", \"age\":25, \"gpa\":2.81, \"status\":true}");
    }

    @Test(description = "Test converting a struct with map of blob to a JSON",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'Info2' value cannot be converted to 'json'.*")
    public void testStructWithIncompatibleTypeToJson() {
        BRunUtil.invoke(compileResult, "testStructWithIncompatibleTypeToJson");
    }

    @Test(description = "Test converting a map to a record, with a map value for a JSON field")
    public void testMapToStructWithMapValueForJsonField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapToStructWithMapValueForJsonField");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> personStruct = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(personStruct.stringValue(), "{name:\"Child\", age:25, parent:(), " +
                "info:{\"status\":\"single\"}, address:{\"city\":\"Colombo\", \"country\":\"SriLanka\"}, " +
                "marks:[87, 94, 72], a:\"any value\", score:5.67, alive:true, children:()}");
    }

    @Test(description = "Test converting a map with missing field to a struct")
    public void testMapWithMissingOptionalFieldsToStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapWithMissingOptionalFieldsToStruct");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> personStruct = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(personStruct.stringValue(), "{name:\"Child\", age:25, parent:(), " +
                "address:{\"city\":\"Colombo\", \"country\":\"SriLanka\"}, marks:[87, 94, 72], " +
                "a:\"any value\", score:5.67, alive:true}");
    }

    @Test(description = "Test converting a map with incompatible inner array to a struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp =
                  ".*'map<anydata>' value cannot be converted to 'Person'.*")
    public void testMapWithIncompatibleArrayToStruct() {
        BRunUtil.invoke(compileResult, "testMapWithIncompatibleArrayToStruct");
    }

    @Test(description = "Test converting a map with incompatible inner struct to a struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'map<anydata>' " +
                  "value cannot be converted to 'Employee'.*")
    public void testMapWithIncompatibleStructToStruct() {
        BRunUtil.invoke(compileResult, "testMapWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a incompatible JSON to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*'map<json>' value cannot be converted to 'Person'.*")
    public void testIncompatibleJsonToStruct() {
        BRunUtil.invoke(compileResult, "testIncompatibleJsonToStruct");
    }

    @Test(description = "Test converting a JSON with missing optional fields to a struct")
    public void testJsonToStructWithMissingOptionalFields() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStructWithMissingOptionalFields");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{name:\"Child\", age:25, parent:(), " +
                "info:{\"status\":\"single\"}, address:{\"city\":\"Colombo\", \"country\":\"SriLanka\"}, " +
                "marks:[87, 94, 72], a:\"any value\", score:5.67, alive:true}");
    }

    @Test(description = "Test converting a incompatible JSON to a struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'map<json>'" +
                  " value cannot be converted to 'PersonWithChildren'.*")
    public void testJsonToStructWithMissingRequiredFields() {
        BRunUtil.invoke(compileResult, "testJsonToStructWithMissingRequiredFields");
    }

    @Test(description = "Test converting a JSON with incompatible inner map to a struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'map<json>'" +
                  " value cannot be converted to 'Person'.*")
    public void testJsonWithIncompatibleMapToStruct() {
        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner struct to a struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'map<json>' " +
                  "value cannot be converted to 'Person'.*")
    public void testJsonWithIncompatibleStructToStruct() {
        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleStructToStruct");
    }

    @Test(description = "Test converting a JSON array to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*'json\\[\\]' value cannot be converted to 'Person'.*")
    public void testJsonArrayToStruct() {
        BRunUtil.invoke(compileResult, "testJsonArrayToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner type to a struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*'map<json>' value cannot be converted to 'Person'.*")
    public void testJsonWithIncompatibleTypeToStruct() {
        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleTypeToStruct");
    }

    @Test(description = "Test converting a struct with map of blob to a JSON",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'Info' value cannot be converted to 'json'.*")
    public void testStructWithIncompatibleTypeMapToJson() {
        BRunUtil.invoke(compileResult, "testStructWithIncompatibleTypeMapToJson");
    }

    @Test(description = "Test converting a JSON array to any data array")
    public void testJsonToAnyArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToAnyArray");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> anyArrayStruct = (BMap<String, BValue>) returns[0];
        BValueArray array = (BValueArray) anyArrayStruct.get("a");

        Assert.assertEquals(((BInteger) array.getRefValue(0)).intValue(), 4);
        Assert.assertEquals(array.getRefValue(1).stringValue(), "Supun");
        Assert.assertEquals(((BFloat) array.getRefValue(2)).floatValue(), 5.36);
        Assert.assertTrue(((BBoolean) array.getRefValue(3)).booleanValue());
        Assert.assertEquals(array.getRefValue(4).stringValue(), "{\"lname\":\"Setunga\"}");
        Assert.assertEquals(array.getRefValue(5).stringValue(), "[4, 3, 7]");
        Assert.assertNull(array.getRefValue(6));
    }

    @Test(description = "Test converting a JSON array to int array")
    public void testJsonToIntArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToIntArray");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> anyArrayStruct = (BMap<String, BValue>) returns[0];

        BValueArray array = (BValueArray) anyArrayStruct.get("a");
        Assert.assertEquals(((BArrayType) array.getType()).getElementType().toString(), "int");
        Assert.assertEquals(array.getInt(0), 4);
        Assert.assertEquals(array.getInt(1), 3);
        Assert.assertEquals(array.getInt(2), 9);
    }

    @Test(description = "Test converting a JSON string array to string array")
    public void testJsonToStringArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStringArray");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> anyArrayStruct = (BMap<String, BValue>) returns[0];
        BValueArray array = (BValueArray) anyArrayStruct.get("a");

        Assert.assertEquals(((BArrayType) array.getType()).getElementType().toString(), "string");
        Assert.assertEquals(array.getString(0), "a");
        Assert.assertEquals(array.getString(1), "b");
        Assert.assertEquals(array.getString(2), "c");
    }

    @Test(description = "Test converting a JSON integer array to string array")
    public void testJsonIntArrayToStringArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonIntArrayToStringArray");
        Assert.assertEquals(returns[0].toString(), "{\"a\":[\"4\", \"3\", \"9\"]}");
    }

    @Test(description = "Test converting a JSON array to xml array",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*'map<json>' " +
                  "value cannot be converted to 'XmlArray'.*")
    public void testJsonToXmlArray() {
        BRunUtil.invoke(compileResult, "testJsonToXmlArray");
    }

    @Test(description = "Test converting a JSON integer array to string array",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*cannot convert '\\(\\)' to type 'StringArray'.*")
    public void testNullJsonToArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullJsonToArray");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test converting a JSON null to string array",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'map<json>'" +
                  " value cannot be converted to 'StringArray'.*")
    public void testNullJsonArrayToArray() {
        BRunUtil.invoke(compileResult, "testNullJsonArrayToArray");
    }

    @Test(description = "Test converting a JSON string to string array",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*'map<json>' value cannot be converted to 'StringArray'.*")
    public void testNonArrayJsonToArray() {
        BRunUtil.invoke(compileResult, "testNonArrayJsonToArray");
    }

    @Test(description = "Test converting a null JSON to struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*cannot convert '\\(\\)' to type 'Person'.*")
    public void testNullJsonToStruct() {
        BRunUtil.invoke(compileResult, "testNullJsonToStruct");
    }

    @Test(description = "Test converting a null Struct to json")
    public void testNullStructToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullStructToJson");
        Assert.assertNull(returns[0]);
    }

    @Test
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
        Assert.assertTrue(marks instanceof BValueArray);
        BValueArray genreArray = (BValueArray) marks;
        Assert.assertEquals(genreArray.getString(0), "Adventure");
        Assert.assertEquals(genreArray.getString(1), "Drama");
        Assert.assertEquals(genreArray.getString(2), "Thriller");

        BValue actors = map.get("actors");
        Assert.assertTrue(actors instanceof BValueArray);
        BValueArray actorsArray = (BValueArray) actors;
        Assert.assertTrue(actorsArray.getRefValue(0) instanceof BMap);
        Assert.assertEquals(actorsArray.getRefValue(0).stringValue(),
                "{\"fname\":\"Leonardo\", \"lname\":\"DiCaprio\", \"age\":35}");
    }

    @Test
    public void testStructWithStringArrayToJSON() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructWithStringArrayToJSON");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"names\":[\"John\", \"Doe\"]}");
    }

    @Test
    public void testEmptyJSONtoStructWithOptionals() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyJSONtoStructWithOptionals");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> bValue = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(bValue.size(), 0);
    }

    @Test
    public void structWithComplexMapToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "structWithComplexMapToJson");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"foo\":{\"a\":4, \"b\":2.5, \"c\":true, \"d\":\"apple\", " +
                "\"e\":{\"foo\":\"bar\"}, \"f\":{\"name\":\"\", \"age\":0}, \"g\":[1, 8, 7], \"h\":null}}");
    }

    @Test
    public void structWithComplexArraysToJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "structWithComplexArraysToJson");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"a\":[4, 6, 9], \"b\":[4.6, 7.5], " +
                "\"c\":[true, true, false], \"d\":[\"apple\", \"orange\"], \"e\":[{}, {}], " +
                "\"f\":[{\"name\":\"\", \"age\":0}, {\"name\":\"\", \"age\":0}], \"g\":[{\"foo\":\"bar\"}]}");
    }

    @Test
    public void testJsonToRecordInPackage() {
        BValue[] returns = BRunUtil.invoke(packageResult, "testJsonToRecord");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> struct = (BMap<String, BValue>) returns[0];

        String name = struct.get("name").stringValue();
        Assert.assertEquals(name, "John");

        long age = ((BInteger) struct.get("age")).intValue();
        Assert.assertEquals(age, 30);

        BValue address = struct.get("adrs");
        Assert.assertTrue(address instanceof BMap);
        BMap<String, BValue> addressStruct = (BMap<String, BValue>) address;
        Assert.assertEquals(addressStruct.get("street").stringValue(), "20 Palm Grove");
        Assert.assertEquals(addressStruct.get("city").stringValue(), "Colombo 03");
        Assert.assertEquals(addressStruct.get("country").stringValue(), "Sri Lanka");
    }

    @Test
    public void testMapToRecordInPackage() {
        BValue[] returns = BRunUtil.invoke(packageResult, "testMapToRecord");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> struct = (BMap<String, BValue>) returns[0];

        String name = struct.get("name").stringValue();
        Assert.assertEquals(name, "John");

        long age = ((BInteger) struct.get("age")).intValue();
        Assert.assertEquals(age, 30);

        BValue address = struct.get("adrs");
        Assert.assertTrue(address instanceof BMap);
        BMap<String, BValue> addressStruct = (BMap<String, BValue>) address;
        Assert.assertEquals(addressStruct.get("street").stringValue(), "20 Palm Grove");
        Assert.assertEquals(addressStruct.get("city").stringValue(), "Colombo 03");
        Assert.assertEquals(addressStruct.get("country").stringValue(), "Sri Lanka");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonToMapUnconstrained() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToMapUnconstrained");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns[0];
        Assert.assertEquals(map.stringValue(),
                "{\"x\":5, \"y\":10, \"z\":3.14, \"o\":{\"a\":\"A\", \"b\":\"B\", \"c\":true}}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonToMapConstrained1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToMapConstrained1");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns[0];
        Assert.assertEquals(map.stringValue(), "{\"x\":\"A\", \"y\":\"B\"}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonToMapConstrained2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToMapConstrained2");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns[0];
        Assert.assertEquals(map.stringValue(), "{\"a\":{x:5, y:10}}");
    }

    @Test(description = "Test converting json to constrained map",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*'map<json>' " +
                    "value cannot be converted to 'map<T1>'.*")
    public void testJsonToMapConstrainedFail() {
        BRunUtil.invoke(compileResult, "testJsonToMapConstrainedFail");
    }

    @Test
    public void testStructArrayConversion1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructArrayConversion1");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> struct = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(((BInteger) struct.get("y")).intValue(), 1);
    }

    @Test(description = "Test converting incompatible record types")
    public void testStructArrayConversion2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructArrayConversion2");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> struct = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(((BInteger) struct.get("z")).intValue(), 2);
    }

//    @Test(description = "Test performing an invalid object to record conversion",
//            expectedExceptions = { BLangRuntimeException.class },
//            expectedExceptionsMessageRegExp = ".*'T3' cannot be cast to 'O2'.*")
//    public void testObjectRecordConversionFail() {
//        BRunUtil.invoke(compileResult, "testObjectRecordConversionFail");
//    }

    @Test
    public void testTupleConversion1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleConversion1");
        Assert.assertEquals(returns.length, 2);
    }

    @Test
    public void testTupleConversion2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleConversion2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.INT);
        Assert.assertEquals(returns[1].getType().getTag(), TypeTags.STRING);
    }

    @Test
    public void testArrayToJson1() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testArrayToJson1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[10, 15]");
    }

    @Test
    public void testArrayToJson2() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testArrayToJson2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[{\"x\":10, \"y\":0}, {\"x\":15, \"y\":0}]");
    }

    @Test
    public void testJsonToArray1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToArray1");
        Assert.assertEquals(returns.length, 1);
    }

    @Test
    public void testJsonToArray2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToArray2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[1, 2, 3]");
    }

    @Test(description = "Test an invalid json to array conversion",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*'map<json>' value cannot" +
                    " be converted to 'int\\[\\]'.*")
    public void testJsonToArrayFail() {
        BRunUtil.invoke(compileResult, "testJsonToArrayFail");
    }

    @Test
    public void testJsonFloatToRecordWithFloat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonFloatToRecordWithFloat");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{f:3.0}");
    }

    @Test(description = "Test result is json after converting record to json")
    public void testRecordToJsonWithIsJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRecordToJsonWithIsJson");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test convert any, anydata and json values to string")
    public void testImplicitConversionToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testImplicitConversionToString");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns[0];
        Assert.assertEquals(map.get("fromFloat").stringValue(), "234.45");
        Assert.assertEquals(map.get("fromString").stringValue(), "hello");
        Assert.assertEquals(map.get("fromInt").stringValue(), "200");
        Assert.assertEquals(map.get("fromDecimal").stringValue(), "23.456");
        Assert.assertEquals(map.get("fromByte").stringValue(), "5");
        Assert.assertEquals(map.get("fromBoolean").stringValue(), "true");
    }

    @Test
    public void testConvertWithFuncCall() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConvertWithFuncCall");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testConvertWithFuncReturnUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConvertWithFuncReturnUnion");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 125);
    }
}
