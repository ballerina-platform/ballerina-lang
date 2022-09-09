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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Test Cases for type conversion.
 */
public class NativeConversionTest {

    private CompileResult compileResult;

    private CompileResult packageResult;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion.bal");
        packageResult = BCompileUtil.compile("test-src/expressions/conversion/test_project");
    }

    @Test
    public void testStructToMap() {

        Object returns = BRunUtil.invoke(compileResult, "testStructToMap");
        Assert.assertTrue(returns instanceof BMap<?, ?>);
        BMap<String, ?> map = (BMap<String, ?>) returns;

        Object name = map.get(StringUtils.fromString("name"));
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.toString(), "Child");

        Object age = map.get(StringUtils.fromString("age"));
        Assert.assertTrue(age instanceof Long);
        Assert.assertEquals(age, 25L);

        Object parent = map.get(StringUtils.fromString("parent"));
        Assert.assertTrue(parent instanceof BMap);
        BMap<String, Object> parentStruct = (BMap<String, Object>) parent;
        Assert.assertEquals(parentStruct.get(StringUtils.fromString("name")).toString(), "Parent");
        Assert.assertEquals(parentStruct.get(StringUtils.fromString("age")), 50L);

        Object address = map.get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get(StringUtils.fromString("city")).toString(), "Colombo");
        Assert.assertEquals(addressMap.get(StringUtils.fromString("country")).toString(), "SriLanka");

        Object info = map.get(StringUtils.fromString("info"));
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.toString(), "{\"status\":\"single\"}");

        Object marks = map.get(StringUtils.fromString("marks"));
        Assert.assertTrue(marks instanceof BArray);
        BArray marksArray = (BArray) marks;
        Assert.assertEquals(marksArray.getRefValue(0), 67L);
        Assert.assertEquals(marksArray.getRefValue(1), 38L);
        Assert.assertEquals(marksArray.getRefValue(2), 91L);
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

        Object returns = BRunUtil.invoke(compileResult, functionName);
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> struct = (BMap<String, Object>) returns;

        String name = struct.get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(name, "Child");

        long age = (long) struct.get(StringUtils.fromString("age"));
        Assert.assertEquals(age, 25);

        Object parent = struct.get(StringUtils.fromString("parent"));
        Assert.assertTrue(parent instanceof BMap);
        BMap<String, Object> parentStruct = (BMap<String, Object>) parent;
        Assert.assertEquals(parentStruct.get(StringUtils.fromString("name")).toString(), "Parent");
        Assert.assertEquals((parentStruct.get(StringUtils.fromString("age"))), 50L);
        Assert.assertNull(parentStruct.get(StringUtils.fromString("parent")));
        Assert.assertNull(parentStruct.get(StringUtils.fromString("info")));
        Assert.assertNull(parentStruct.get(StringUtils.fromString("address")));
        Assert.assertNull(parentStruct.get(StringUtils.fromString("marks")));

        Object info = struct.get(StringUtils.fromString("info"));
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.toString(), "{\"status\":\"single\"}");

        Object address = struct.get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get(StringUtils.fromString("city")).toString(), "Colombo");
        Assert.assertEquals(addressMap.get(StringUtils.fromString("country")).toString(), "SriLanka");

        Object marks = struct.get(StringUtils.fromString("marks"));
        Assert.assertTrue(marks instanceof BArray);
        BArray marksArray = (BArray) marks;
        Assert.assertEquals(marksArray.getInt(0), 87);
        Assert.assertEquals(marksArray.getInt(1), 94);
        Assert.assertEquals(marksArray.getInt(2), 72);
    }

    @Test
    public void testJsonToStruct() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToStruct");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> struct = (BMap<String, Object>) returns;

        String name = struct.get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(name, "Child");

        long age = (long) struct.get(StringUtils.fromString("age"));
        Assert.assertEquals(age, 25);

        Object parent = struct.get(StringUtils.fromString("parent"));
        Assert.assertTrue(parent instanceof BMap);
        BMap<String, Object> parentStruct = (BMap<String, Object>) parent;
        Assert.assertEquals(parentStruct.get(StringUtils.fromString("name")).toString(), "Parent");
        Assert.assertEquals((parentStruct.get(StringUtils.fromString("age"))), 50L);
        Assert.assertNull(parentStruct.get(StringUtils.fromString("parent")));
        Assert.assertNull(parentStruct.get(StringUtils.fromString("info")));
        Assert.assertNull(parentStruct.get(StringUtils.fromString("address")));
        Assert.assertNull(parentStruct.get(StringUtils.fromString("marks")));

        Object info = struct.get(StringUtils.fromString("info"));
        Assert.assertTrue(info instanceof BMap);
        Assert.assertEquals(info.toString(), "{\"status\":\"single\"}");

        Object address = struct.get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get(StringUtils.fromString("city")).toString(), "Colombo");
        Assert.assertEquals(addressMap.get(StringUtils.fromString("country")).toString(), "SriLanka");

        Object marks = struct.get(StringUtils.fromString("marks"));
        Assert.assertTrue(marks instanceof BArray);
        BArray marksArray = (BArray) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 56);
        Assert.assertEquals(marksArray.getInt(1), 79);
    }

    @Test
    public void testStructToJson() {

        Object returns = BRunUtil.invoke(compileResult, "testStructToJson");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> child = ((BMap<String, Object>) returns);
        Assert.assertEquals(child.get(StringUtils.fromString("name")).toString(), "Child");
        Assert.assertEquals((child.get(StringUtils.fromString("age"))), 25L);

        Assert.assertTrue(child.get(StringUtils.fromString("parent")) instanceof BMap);
        BMap<String, Object> parent = (BMap<String, Object>) child.get(StringUtils.fromString("parent"));
        Assert.assertEquals(parent.get(StringUtils.fromString("name")).toString(), "Parent");
        Assert.assertEquals((parent.get(StringUtils.fromString("age"))), 50L);
        Assert.assertNull(parent.get(StringUtils.fromString("parent")));
        Assert.assertNull(parent.get(StringUtils.fromString("info")));
        Assert.assertNull(parent.get(StringUtils.fromString("address")));
        Assert.assertNull(parent.get(StringUtils.fromString("marks")));

        Assert.assertTrue(child.get(StringUtils.fromString("info")) instanceof BMap);
        BMap<String, Object> info = (BMap<String, Object>) child.get(StringUtils.fromString("info"));
        Assert.assertEquals(info.get(StringUtils.fromString("status")).toString(), "single");

        Assert.assertTrue(child.get(StringUtils.fromString("address")) instanceof BMap);
        BMap<String, Object> address = (BMap<String, Object>) child.get(StringUtils.fromString("address"));
        Assert.assertEquals(address.get(StringUtils.fromString("country")).toString(), "SriLanka");
        Assert.assertEquals(address.get(StringUtils.fromString("city")).toString(), "Colombo");

        Assert.assertTrue(child.get(StringUtils.fromString("marks")) instanceof BArray);
        BArray marks = (BArray) child.get(StringUtils.fromString("marks"));
        Assert.assertEquals(marks.size(), 3);
        Assert.assertEquals(marks.getRefValue(0), 87L);
        Assert.assertEquals(marks.getRefValue(1), 94L);
        Assert.assertEquals(marks.getRefValue(2), 72L);
    }

    @Test(description = "Test any type to anydata type at runtime")
    public void testAnyRecordToAnydataMap() {

        Object returns = BRunUtil.invoke(compileResult, "testAnyRecordToAnydataMap");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("name")).toString(), "Waruna");
    }

    @Test(description = "Test converting a map to json")
    public void testMapToJsonConversion() {

        Object result = BRunUtil.invoke(compileResult, "testComplexMapToJson");
        Assert.assertTrue(result instanceof BMap);
        Assert.assertEquals(result.toString(), "{\"name\":\"Supun\",\"age\":25,\"gpa\":2.81,\"status\":true}");
    }

    @Test(description = "Test converting a struct with map of blob to a JSON",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'Info2' value cannot be converted to 'json'.*")
    public void testStructWithIncompatibleTypeToJson() {

        BRunUtil.invoke(compileResult, "testStructWithIncompatibleTypeToJson");
    }

    @Test(description = "Test converting a map to a record, with a map value for a JSON field")
    public void testMapToStructWithMapValueForJsonField() {

        Object returns = BRunUtil.invoke(compileResult, "testMapToStructWithMapValueForJsonField");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> personStruct = (BMap<String, Object>) returns;

        Assert.assertEquals(personStruct.toString(), "{\"name\":\"Child\",\"age\":25,\"parent\":null," +
                "\"info\":{\"status\":\"single\"},\"address\":{\"city\":\"Colombo\",\"country\":\"SriLanka\"}," +
                "\"marks\":[87,94,72],\"a\":\"any value\",\"score\":5.67,\"alive\":true}");
    }

    @Test(description = "Test converting a map with missing field to a struct")
    public void testMapWithMissingOptionalFieldsToStruct() {

        Object returns = BRunUtil.invoke(compileResult, "testMapWithMissingOptionalFieldsToStruct");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> personStruct = (BMap<String, Object>) returns;

        Assert.assertEquals(personStruct.toString(), "{\"name\":\"Child\",\"age\":25,\"parent\":null," +
                "\"address\":{\"city\":\"Colombo\",\"country\":\"SriLanka\"},\"marks\":[87,94,72],\"a\":\"any " +
                "value\",\"score\":5.67,\"alive\":true}");
    }

    @Test(description = "Test converting a map with incompatible inner array to a struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*'map<anydata>' value cannot be converted to 'Person'.*")
    public void testMapWithIncompatibleArrayToStruct() {

        BRunUtil.invoke(compileResult, "testMapWithIncompatibleArrayToStruct");
    }

    @Test(description = "Test converting a map with incompatible inner struct to a struct",
            expectedExceptions = {BLangRuntimeException.class},
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

        Object returns = BRunUtil.invoke(compileResult, "testJsonToStructWithMissingOptionalFields");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"name\":\"Child\",\"age\":25,\"parent\":null," +
                "\"info\":{\"status\":\"single\"},\"address\":{\"city\":\"Colombo\",\"country\":\"SriLanka\"}," +
                "\"marks\":[87,94,72],\"a\":\"any value\",\"score\":5.67,\"alive\":true}");
    }

    @Test(description = "Test converting a incompatible JSON to a struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>'" +
                    " value cannot be converted to 'PersonWithChildren'.*")
    public void testJsonToStructWithMissingRequiredFields() {

        BRunUtil.invoke(compileResult, "testJsonToStructWithMissingRequiredFields");
    }

    @Test(description = "Test converting a JSON with incompatible inner map to a struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>'" +
                    " value cannot be converted to 'Person'.*")
    public void testJsonWithIncompatibleMapToStruct() {

        BRunUtil.invoke(compileResult, "testJsonWithIncompatibleMapToStruct");
    }

    @Test(description = "Test converting a JSON with incompatible inner struct to a struct",
            expectedExceptions = {BLangRuntimeException.class},
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
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'Info' value cannot be converted to 'json'.*")
    public void testStructWithIncompatibleTypeMapToJson() {

        BRunUtil.invoke(compileResult, "testStructWithIncompatibleTypeMapToJson");
    }

    @Test(description = "Test converting a JSON array to any data array")
    public void testJsonToAnyArray() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToAnyArray");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> anyArrayStruct = (BMap<String, Object>) returns;
        BArray array = (BArray) anyArrayStruct.get(StringUtils.fromString("a"));

        Assert.assertEquals(array.getRefValue(0), 4L);
        Assert.assertEquals(array.getRefValue(1).toString(), "Supun");
        Assert.assertEquals(array.getRefValue(2), 5.36);
        Assert.assertTrue((Boolean) array.getRefValue(3));
        Assert.assertEquals(array.getRefValue(4).toString(), "{\"lname\":\"Setunga\"}");
        Assert.assertEquals(array.getRefValue(5).toString(), "[4,3,7]");
        Assert.assertNull(array.getRefValue(6));
    }

    @Test(description = "Test converting a JSON array to int array")
    public void testJsonToIntArray() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToIntArray");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> anyArrayStruct = (BMap<String, Object>) returns;

        BArray array = (BArray) anyArrayStruct.get(StringUtils.fromString("a"));
        Assert.assertEquals(((BArrayType) array.getType()).getElementType().toString(), "int");
        Assert.assertEquals(array.getInt(0), 4);
        Assert.assertEquals(array.getInt(1), 3);
        Assert.assertEquals(array.getInt(2), 9);
    }

    @Test(description = "Test converting a JSON string array to string array")
    public void testJsonToStringArray() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToStringArray");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> anyArrayStruct = (BMap<String, Object>) returns;
        BArray array = (BArray) anyArrayStruct.get(StringUtils.fromString("a"));

        Assert.assertEquals(((BArrayType) array.getType()).getElementType().toString(), "string");
        Assert.assertEquals(array.getString(0), "a");
        Assert.assertEquals(array.getString(1), "b");
        Assert.assertEquals(array.getString(2), "c");
    }

    @Test(description = "Test converting a JSON integer array to string array")
    public void testJsonIntArrayToStringArray() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonIntArrayToStringArray");
        Assert.assertEquals(returns.toString(), "{\"a\":[\"4\",\"3\",\"9\"]}");
    }

    @Test(description = "Test converting a JSON array to xml array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>' " +
                    "value cannot be converted to 'XmlArray'.*")
    public void testJsonToXmlArray() {

        BRunUtil.invoke(compileResult, "testJsonToXmlArray");
    }

    @Test(description = "Test converting a JSON integer array to string array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot convert '\\(\\)' to type 'StringArray'.*")
    public void testNullJsonToArray() {

        Object returns = BRunUtil.invoke(compileResult, "testNullJsonToArray");
        Assert.assertNull(returns);
    }

    @Test(description = "Test converting a JSON null to string array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>'" +
                    " value cannot be converted to 'StringArray'.*")
    public void testNullJsonArrayToArray() {

        BRunUtil.invoke(compileResult, "testNullJsonArrayToArray");
    }

    @Test(description = "Test converting a JSON string to string array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>' value cannot be converted to 'StringArray': \n\t\t.*")
    public void testNonArrayJsonToArray() {

        BRunUtil.invoke(compileResult, "testNonArrayJsonToArray");
    }

    @Test(description = "Test converting a null JSON to struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*cannot convert '\\(\\)' to type 'Person'.*")
    public void testNullJsonToStruct() {

        BRunUtil.invoke(compileResult, "testNullJsonToStruct");
    }

    @Test(description = "Test converting a null Struct to json")
    public void testNullStructToJson() {

        Object returns = BRunUtil.invoke(compileResult, "testNullStructToJson");
        Assert.assertNull(returns);
    }

    @Test
    public void testStructToMapWithRefTypeArray() {

        Object arr = BRunUtil.invoke(compileResult, "testStructToMapWithRefTypeArray");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof BMap<?, ?>);
        BMap<String, ?> map = (BMap<String, ?>) returns.get(0);

        Object name = map.get(StringUtils.fromString("title"));
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.toString(), "The Revenant");

        Object age = map.get(StringUtils.fromString("year"));
        Assert.assertTrue(age instanceof Long);
        Assert.assertEquals(age, 2015L);

        Object marks = map.get(StringUtils.fromString("genre"));
        Assert.assertTrue(marks instanceof BArray);
        BArray genreArray = (BArray) marks;
        Assert.assertEquals(genreArray.getRefValue(0).toString(), "Adventure");
        Assert.assertEquals(genreArray.getRefValue(1).toString(), "Drama");
        Assert.assertEquals(genreArray.getRefValue(2).toString(), "Thriller");

        Object actors = map.get(StringUtils.fromString("actors"));
        Assert.assertTrue(actors instanceof BArray);
        BArray actorsArray = (BArray) actors;
        Assert.assertTrue(actorsArray.getRefValue(0) instanceof BMap);
        Assert.assertEquals(actorsArray.getRefValue(0).toString(),
                "{\"fname\":\"Leonardo\",\"lname\":\"DiCaprio\",\"age\":35}");
    }

    @Test
    public void testStructWithStringArrayToJSON() {

        Object returns = BRunUtil.invoke(compileResult, "testStructWithStringArrayToJSON");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"names\":[\"John\",\"Doe\"]}");
    }

    @Test
    public void testEmptyJSONtoStructWithOptionals() {

        Object returns = BRunUtil.invoke(compileResult, "testEmptyJSONtoStructWithOptionals");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> bValue = (BMap<String, Object>) returns;
        Assert.assertEquals(bValue.size(), 0);
    }

    @Test
    public void structWithComplexMapToJson() {

        Object returns = BRunUtil.invoke(compileResult, "structWithComplexMapToJson");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"foo\":{\"a\":4,\"b\":2.5,\"c\":true,\"d\":\"apple\"," +
                "\"e\":{\"foo\":\"bar\"},\"f\":{\"name\":\"\",\"age\":0},\"g\":[1,8,7],\"h\":null}}");
    }

    @Test
    public void structWithComplexArraysToJson() {

        Object returns = BRunUtil.invoke(compileResult, "structWithComplexArraysToJson");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(),
                "{\"a\":[4,6,9],\"b\":[4.6,7.5],\"c\":[true,true,false],\"d\":[\"apple\",\"orange\"],\"e\":[{},{}]," +
                        "\"f\":[{\"name\":\"\",\"age\":0},{\"name\":\"\",\"age\":0}],\"g\":[{\"foo\":\"bar\"}]}");
    }

    @Test
    public void testJsonToRecordInPackage() {

        Object returns = BRunUtil.invoke(packageResult, "testJsonToRecord");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> struct = (BMap<String, Object>) returns;

        String name = struct.get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(name, "John");

        long age = (long) struct.get(StringUtils.fromString("age"));
        Assert.assertEquals(age, 30);

        Object address = struct.get(StringUtils.fromString("adrs"));
        Assert.assertTrue(address instanceof BMap);
        BMap<String, Object> addressStruct = (BMap<String, Object>) address;
        Assert.assertEquals(addressStruct.get(StringUtils.fromString("street")).toString(), "20 Palm Grove");
        Assert.assertEquals(addressStruct.get(StringUtils.fromString("city")).toString(), "Colombo 03");
        Assert.assertEquals(addressStruct.get(StringUtils.fromString("country")).toString(), "Sri Lanka");
    }

    @Test
    public void testMapToRecordInPackage() {

        Object returns = BRunUtil.invoke(packageResult, "testMapToRecord");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> struct = (BMap<String, Object>) returns;

        String name = struct.get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(name, "John");

        long age = (long) struct.get(StringUtils.fromString("age"));
        Assert.assertEquals(age, 30);

        Object address = struct.get(StringUtils.fromString("adrs"));
        Assert.assertTrue(address instanceof BMap);
        BMap<String, Object> addressStruct = (BMap<String, Object>) address;
        Assert.assertEquals(addressStruct.get(StringUtils.fromString("street")).toString(), "20 Palm Grove");
        Assert.assertEquals(addressStruct.get(StringUtils.fromString("city")).toString(), "Colombo 03");
        Assert.assertEquals(addressStruct.get(StringUtils.fromString("country")).toString(), "Sri Lanka");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonToMapUnconstrained() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToMapUnconstrained");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns;
        Assert.assertEquals(map.toString(),
                "{\"x\":5,\"y\":10,\"z\":3.14,\"o\":{\"a\":\"A\",\"b\":\"B\",\"c\":true}}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonToMapConstrained1() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToMapConstrained1");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns;
        Assert.assertEquals(map.toString(), "{\"x\":\"A\",\"y\":\"B\"}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonToMapConstrained2() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToMapConstrained2");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns;
        Assert.assertEquals(map.toString(), "{\"a\":{\"x\":5,\"y\":10}}");
    }

    @Test(description = "Test converting json to constrained map",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>' " +
                    "value cannot be converted to 'T1Map'.*")
    public void testJsonToMapConstrainedFail() {

        BRunUtil.invoke(compileResult, "testJsonToMapConstrainedFail");
    }

    @Test
    public void testStructArrayConversion1() {

        Object returns = BRunUtil.invoke(compileResult, "testStructArrayConversion1");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> struct = (BMap<String, Object>) returns;
        Assert.assertEquals((struct.get(StringUtils.fromString("y"))), 1L);
    }

    @Test(description = "Test converting incompatible record types")
    public void testStructArrayConversion2() {

        Object returns = BRunUtil.invoke(compileResult, "testStructArrayConversion2");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> struct = (BMap<String, Object>) returns;
        Assert.assertEquals((struct.get(StringUtils.fromString("z"))), 2L);
    }

    @Test
    public void testTupleConversion1() {

        BRunUtil.invoke(compileResult, "testTupleConversion1");
    }

    @Test
    public void testTupleConversion2() {

        Object arr = BRunUtil.invoke(compileResult, "testTupleConversion2");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(getType(returns.get(0)).getTag(), TypeTags.INT);
        Assert.assertEquals(getType(returns.get(1)).getTag(), TypeTags.STRING);
    }

    @Test
    public void testArrayToJson1() {

        Object returns = BRunUtil.invoke(compileResult, "testArrayToJson1");
        Assert.assertEquals(returns.toString(), "[10,15]");
    }

    @Test
    public void testArrayToJson2() {

        Object returns = BRunUtil.invoke(compileResult, "testArrayToJson2");
        Assert.assertEquals(returns.toString(), "[{\"x\":10,\"y\":0},{\"x\":15,\"y\":0}]");
    }

    @Test
    public void testJsonToArray1() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToArray1");
    }

    @Test
    public void testJsonToArray2() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonToArray2");
        Assert.assertEquals(returns.toString(), "[1,2,3]");
    }

    @Test(description = "Test an invalid json to array conversion",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map<json>' value cannot" +
                    " be converted to 'IntArrayType'.*")
    public void testJsonToArrayFail() {

        BRunUtil.invoke(compileResult, "testJsonToArrayFail");
    }

    @Test
    public void testJsonFloatToRecordWithFloat() {

        Object returns = BRunUtil.invoke(compileResult, "testJsonFloatToRecordWithFloat");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"f\":3.0}");
    }

    @Test(description = "Test result is json after converting record to json")
    public void testRecordToJsonWithIsJson() {

        Object returns = BRunUtil.invoke(compileResult, "testRecordToJsonWithIsJson");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test convert any, anydata and json values to string")
    public void testImplicitConversionToString() {

        Object returns = BRunUtil.invoke(compileResult, "testImplicitConversionToString");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, ?> map = (BMap<String, ?>) returns;
        Assert.assertEquals(map.get(StringUtils.fromString("fromFloat")).toString(), "234.45");
        Assert.assertEquals(map.get(StringUtils.fromString("fromString")).toString(), "hello");
        Assert.assertEquals(map.get(StringUtils.fromString("fromInt")).toString(), "200");
        Assert.assertEquals(map.get(StringUtils.fromString("fromDecimal")).toString(), "23.456");
        Assert.assertEquals(map.get(StringUtils.fromString("fromByte")).toString(), "5");
        Assert.assertEquals(map.get(StringUtils.fromString("fromBoolean")).toString(), "true");
    }

    @Test
    public void testConvertWithFuncCall() {

        Object returns = BRunUtil.invoke(compileResult, "testConvertWithFuncCall");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test
    public void testConvertWithFuncReturnUnion() {

        Object returns = BRunUtil.invoke(compileResult, "testConvertWithFuncReturnUnion");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 125L);
    }
}
