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
package org.ballerinalang.test.expressions.typecast;


import com.fasterxml.jackson.databind.JsonNode;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
//import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
//import org.ballerinalang.model.values.BJSON;
//import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.codegen.ProgramFile;
//import org.ballerinalang.util.exceptions.BLangRuntimeException;
//import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for type casting.
 */
public class TypeCastExprTest {
    private static final double DELTA = 0.01;
    private ProgramFile bLangProgram;
    private CompileResult result;


//    @BeforeClass
//    public void setup() {
//        result = BTestUtils.compile("test-src/expressions/typecast/type-casting.bal");
//    }

//    @Test
//    public void testXMLToJSON() {
//        BValue[] args = {new BXML("<name>chanaka</name>")};
//        BValue[] returns = Functions.invoke(bLangProgram, "xmltojson", args);
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        final String expected = "{\"name\":\"chanaka\"}";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }
//
//    @Test
//    public void testJSONToXML() {
//        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
//        BValue[] returns = BTestUtils.invoke(result, "jsontoxml", args);
//        Assert.assertTrue(returns[0] instanceof BXML);
//        final String expected = "<name>chanaka</name>";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }

    @Test(enabled = false)
    public void testFloatToInt() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = BTestUtils.invoke(result, "floattoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(enabled = false)
    public void testIntToFloat() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = BTestUtils.invoke(result, "inttofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 5.5555555E7;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test(enabled = false)
    public void testStringToInt() {
        BValue[] args = {new BString("100")};
        BValue[] returns = BTestUtils.invoke(result, "stringtoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "100";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(enabled = false)
    public void testStringToFloat() {
        BValue[] args = {new BString("2222.333f")};
        BValue[] returns = BTestUtils.invoke(result, "stringtofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 2222.333;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test(enabled = false)
    public void testStringToJSON() {
        BValue[] args = {new BString("{\"name\":\"chanaka\"}")};
        BValue[] returns = BTestUtils.invoke(result, "testStringToJson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(enabled = false)
    public void testStringToXML() {
        BValue[] args = {new BString("<name>chanaka</name>")};
        BValue[] returns = BTestUtils.invoke(result, "stringtoxml", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        final String expected = "<name>chanaka</name>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(enabled = false)
    public void testIntToString() {
        BValue[] args = {new BInteger(111)};
        BValue[] returns = BTestUtils.invoke(result, "inttostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(enabled = false)
    public void testFloatToString() {
        BValue[] args = {new BFloat(111.333f)};
        BValue[] returns = BTestUtils.invoke(result, "floattostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns[0].stringValue().substring(0, 7), expected);
    }

    @Test(enabled = false)
    public void testBooleanToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BTestUtils.invoke(result, "booleantostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }


    @Test(enabled = false)
    public void testBooleanAppendToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BTestUtils.invoke(result, "booleanappendtostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true-append-true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    //precommented
    //    @Test
//    public void testXMLToString() {
//        BValue[] args = {new BXML("<name>chanaka</name>")};
//        BValue[] returns = Functions.invoke(bLangProgram, "xmltostring", args);
//        Assert.assertTrue(returns[0] instanceof BString);
//        final String expected = "<name>chanaka</name>";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }
//
    //------

    @Test(enabled = false)
    public void testJSONToString() {
        BValue[] returns = BTestUtils.invoke(result, "testJsonToStringCast", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test(enabled = false)
    public void testJSONObjectToStringCast() {
        BValue[] returns = BTestUtils.invoke(result, "testJSONObjectToStringCast", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");

        Assert.assertTrue(returns[1] instanceof BStruct);
        Assert.assertEquals(((BStruct) returns[1]).getStringField(0),
                            "'json-object' cannot be converted to 'string'");
    }

    @Test(enabled = false)
    public void testJsonToInt() {
        BValue[] returns = BTestUtils.invoke(result, "testJsonToInt", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(enabled = false)
    public void testJsonToFloat() {
        BValue[] returns = BTestUtils.invoke(result, "testJsonToFloat", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 7.65);
    }

    @Test(enabled = false)
    public void testJsonToBoolean() {
        BValue[] returns = BTestUtils.invoke(result, "testJsonToBoolean", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test(enabled = false)
    public void testIntArrayToLongArray() {
        BValue[] returns = BTestUtils.invoke(result, "intarrtofloatarr", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BFloatArray);
        BFloatArray result = (BFloatArray) returns[0];
        Assert.assertEquals(result.get(0), 999.0, DELTA);
        Assert.assertEquals(result.get(1), 95.0, DELTA);
        Assert.assertEquals(result.get(2), 889.0, DELTA);
    }


    @Test(enabled = false)
    public void testSimpleJsonToMap() {
        BValue[] returns = BTestUtils.invoke(result, "testSimpleJsonToMap",new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap map = (BMap) returns[0];

        BValue value1 = map.get(new BString("fname"));
        Assert.assertTrue(value1 instanceof BString);
        Assert.assertEquals(value1.stringValue(), "Supun");

        BValue value2 = map.get(new BString("lname"));
        Assert.assertTrue(value2 instanceof BString);
        Assert.assertEquals(value2.stringValue(), "Setunga");
    }

    @Test(enabled = false)
    public void testComplexJsonToMap() {
        BValue[] returns = BTestUtils.invoke(result, "testComplexJsonToMap");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap map = (BMap) returns[0];

        BValue value1 = map.get(new BString("name"));
        Assert.assertTrue(value1 instanceof BString);
        Assert.assertEquals(value1.stringValue(), "Supun");

        BValue value2 = map.get(new BString("age"));
        Assert.assertTrue(value2 instanceof BInteger);
        Assert.assertEquals(((BInteger) value2).intValue(), 25);

        BValue value3 = map.get(new BString("gpa"));
        Assert.assertTrue(value3 instanceof BFloat);
        Assert.assertEquals(((BFloat) value3).floatValue(), 2.81);

        BValue value4 = map.get(new BString("status"));
        Assert.assertTrue(value4 instanceof BBoolean);
        Assert.assertEquals(((BBoolean) value4).booleanValue(), true);

        BValue value5 = map.get(new BString("info"));
        Assert.assertEquals(value5, null);

        BValue value6 = map.get(new BString("address"));
        Assert.assertTrue(value6 instanceof BJSON);
        Assert.assertEquals(value6.stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");

        BValue value7 = map.get(new BString("marks"));
        Assert.assertTrue(value7 instanceof BJSON);
        Assert.assertEquals(value7.stringValue(), "[1,5,7]");
    }

    @Test(enabled = false)
    public void testSimpleMapToJson() {
        BValue[] returns = BTestUtils.invoke(result, "testSimpleMapToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        JsonNode jsonNode = ((BJSON) returns[0]).value();
        Assert.assertEquals(jsonNode.get("fname").textValue(), "Supun");
        Assert.assertEquals(jsonNode.get("lname").textValue(), "Setunga");
    }

    @Test(enabled = false)
    public void testComplexMapToJson() {
        BValue[] returns = BTestUtils.invoke(result, "testComplexMapToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        JsonNode jsonNode = ((BJSON) returns[0]).value();
        Assert.assertEquals(jsonNode.get("name").textValue(), "Supun");
        Assert.assertEquals(jsonNode.get("age").intValue(), 25);
        Assert.assertEquals(jsonNode.get("status").booleanValue(), true);
        Assert.assertTrue(jsonNode.get("info").isNull());
        Assert.assertEquals(jsonNode.get("intArray").toString(), "[7,8,9]");

        JsonNode addressNode = jsonNode.get("address");
        Assert.assertEquals(addressNode.get("country").textValue(), "USA");
        Assert.assertEquals(addressNode.get("city").textValue(), "CA");
    }

    @Test(enabled = false)
    public void testStructToStruct() {
        BValue[] returns = BTestUtils.invoke(result, "testStructToStruct", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct student = (BStruct) returns[0];

        Assert.assertEquals(student.getStringField(0), "Supun");

        Assert.assertEquals(student.getIntField(0), 25);

        BValue address = student.getRefField(0);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BIntArray marksArray = (BIntArray) student.getRefField(1);
        Assert.assertTrue(marksArray instanceof BIntArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0), 24);
        Assert.assertEquals(marksArray.get(1), 81);
    }

    @Test(enabled = false, description = "Test casting a struct to an incompatible struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "incompatible-struct-cast.bal:24: incompatible types: 'Student' " +
                    "cannot be assigned to 'Person'")
    public void testIncompatibleStructToStructCast() {
        BTestUtils.compile("test-src/expressions/typecast/incompatible-struct-cast.bal");
    }

    @Test(enabled = false, description = "Test casting a JSON integer to a string")
    public void testJsonIntToString() {
        BValue[] returns = BTestUtils.invoke(result, "testJsonIntToString", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }

    @Test(enabled = false, description = "Test casting an incomatible JSON to integer",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeConversionError, message: " +
                    "'string' cannot be converted to 'int'.*")
    public void testIncompatibleJsonToInt() {
        BTestUtils.invoke(result, "testIncompatibleJsonToInt", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting an incomatible JSON to float",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeConversionError, message: " +
                    "'string' cannot be converted to 'float'.*")
    public void testIncompatibleJsonToFloat() {
        BTestUtils.invoke(result, "testIncompatibleJsonToFloat", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting an incomatible JSON to boolean",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeConversionError, message: " +
                    "'string' cannot be converted to 'boolean'.*")
    public void testIncompatibleJsonToBoolean() {
        BTestUtils.invoke(result, "testIncompatibleJsonToBoolean", new BValue[]{});
    }

//    @Test(enabled = false, description = "Test casting a boolean in JSON to int",
//            expectedExceptions = {BLangRuntimeException.class},
//            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeConversionError,
//            message: 'boolean' " +
//                    "cannot be converted to 'int'.*")
//    public void testBooleanInJsonToInt() {
//        BTestUtils.invoke(result, "testBooleanInJsonToInt", new BValue[]{});
//    }

    @Test(enabled = false, description = "Test casting an integer in JSON to float",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeConversionError, message: 'int' " +
                    "cannot be converted to 'float'.*")
    public void testIntInJsonToFloat() {
        BTestUtils.invoke(result, "testIntInJsonToFloat", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a null JSON to string",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testNullJsonToString() {
        BTestUtils.invoke(result, "testNullJsonToString" , new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a null JSON to int",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testNullJsonToInt() {
        BTestUtils.invoke(result, "testNullJsonToInt", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a null JSON to float",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testNullJsonToFloat() {
        BTestUtils.invoke(result, "testNullJsonToFloat", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a null JSON to boolean",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testNullJsonToBoolean() {
        BTestUtils.invoke(result, "testNullJsonToBoolean", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a null Struct to Struct")
    public void testNullStructToStruct() {
        BValue[] returns = BTestUtils.invoke(result, "testNullStructToStruct", new BValue[]{});
        Assert.assertEquals(returns[0], null);
    }

    @Test(enabled = false, description = "Test casting an int as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: " +
                    "'int' cannot be cast to 'json'.*")
    public void testAnyIntToJson() {
        BTestUtils.invoke(result, "testAnyIntToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a string as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: " +
                    "'string' cannot be cast to 'json'.*")
    public void testAnyStringToJson() {
        BTestUtils.invoke(result, "testAnyStringToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a boolean as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, " +
                    "message: 'boolean' cannot be cast to 'json'.*")
    public void testAnyBooleanToJson() {
        BTestUtils.invoke(result, "testAnyBooleanToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a float as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: " +
                    "'float' cannot be cast to 'json'.*")
    public void testAnyFloatToJson() {
        BTestUtils.invoke(result, "testAnyFloatToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a map as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: 'map' " +
                    "cannot be cast to 'json'.*")
    public void testAnyMapToJson() {
        BTestUtils.invoke(result, "testAnyMapToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a struct as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: " +
                    "'Address' cannot be cast to 'json'.*")
    public void testAnyStructToJson() {
        BTestUtils.invoke(result, "testAnyStructToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a json as any type to json")
    public void testAnyJsonToJson() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyJsonToJson" , new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().toString(), "{\"home\":\"SriLanka\"}");
    }

    @Test(enabled = false, description = "Test casting a null as any type to json")
    public void testAnyNullToJson() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToJson", new BValue[]{});
        Assert.assertEquals(returns[0], null);
    }

    @Test(enabled = false, description = "Test casting an array as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: 'any\\[\\]' " +
                    "cannot be cast to 'json'.*")
    public void testAnyArrayToJson() {
        BTestUtils.invoke(result, "testAnyArrayToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a xml as any type to json",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:TypeCastError, message: 'message' " +
                    "cannot be cast to 'json'.*")
    public void testAnyMessageToJson() {
        BTestUtils.invoke(result, "testAnyMessageToJson", new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a struct to map",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "struct-to-map.bal:22: incompatible types: 'Person' cannot be cast " +
                    "to 'map', try conversion")
    public void testStructToMap() {
        BTestUtils.compile("test-src/expressions/cast/struct-to-map.bal");
    }

    @Test(description = "Test casting a map to struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "map-to-struct.bal:36: incompatible types: 'map' cannot be cast to " +
                    "'Person', try conversion")
    public void testMapToStruct() {
        BTestUtils.compile("test-src/expressions/cast/map-to-struct.bal");
    }

    @Test(enabled = false, description = "Test casting a json to map",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "json-to-map.bal:9: incompatible types: 'json' cannot be cast to 'map'")
    public void testJsonToMap() {
        BTestUtils.compile("test-src/expressions/cast/json-to-map.bal");
    }

    @Test(enabled = false, description = "Test casting a json to struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "json-to-struct.bal:34: incompatible types: 'json' cannot be cast to" +
                    " 'Person', try conversion")
    public void testJsonToStruct() {
        BTestUtils.compile("test-src/expressions/cast/json-to-struct.bal");
    }

    @Test(enabled = false, description = "Test casting a map to json",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "map-to-json-error.bal:7: incompatible types: 'map' cannot " +
                    "be cast to 'json'")
    public void testMapToJsonCastingError() {
        BTestUtils.compile("test-src/expressions/cast/map-to-json-error.bal");
    }

    @Test(enabled = false, description = "Test casting struct stored as any to struct")
    public void testStructAsAnyToStruct() {
        BValue[] returns = BTestUtils.invoke(result, "testStructAsAnyToStruct", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct student = (BStruct) returns[0];

        String name = student.getStringField(0);
        Assert.assertEquals(name, "Supun");

        int age = (int) student.getIntField(0);
        Assert.assertEquals(age, 25);

        BValue address = student.getRefField(0);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, ?> addressMap = (BMap<String, ?>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BIntArray marks = (BIntArray) student.getRefField(1);
        Assert.assertTrue(marks instanceof BIntArray);
        Assert.assertEquals(marks.size(), 2);
        Assert.assertEquals(marks.get(0), 24);
        Assert.assertEquals(marks.get(1), 81);

        double score = student.getFloatField(0);
        Assert.assertEquals(score, 0.0);
    }

    @Test(enabled = false, description = "Test casting any to struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*'map' cannot be cast to 'Person'.*")
    public void testAnyToStruct() {
        BTestUtils.invoke(result, "testAnyToStruct" , new BValue[]{});
    }

    @Test(enabled = false, description = "Test casting a null stored as any to struct")
    public void testAnyNullToStruct() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToStruct", new BValue[]{});
        Assert.assertNull(returns[0]);
    }

    @Test(enabled = false, description = "Test casting a null stored as any to map")
    public void testAnyNullToMap() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToMap", new BValue[]{});
        Assert.assertNull(returns[0]);
    }

    @Test(enabled = false, description = "Test casting a null stored as any to xml")
    public void testAnyNullToXml() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToXml", new BValue[]{});
        Assert.assertNull(returns[0]);
    }

    @Test(enabled = false, description = "Test explicit casting struct to any")
    public void testStructToAnyExplicit() {
        BValue[] returns = BTestUtils.invoke(result, "testStructToAnyExplicit", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct student = (BStruct) returns[0];

        Assert.assertEquals(student.getStringField(0), "Supun");

        Assert.assertEquals(student.getIntField(0), 25);

        BValue address = student.getRefField(0);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, BString> addressMap = (BMap<String, BString>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BIntArray marksArray = (BIntArray) student.getRefField(1);
        Assert.assertTrue(marksArray instanceof BIntArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0), 24);
        Assert.assertEquals(marksArray.get(1), 81);

        Assert.assertEquals(student.getFloatField(0), 0.0);
    }

    @Test(enabled = false, description = "Test explicit casting struct to any")
    public void testMapToAnyExplicit() {
        BValue[] returns = BTestUtils.invoke(result, "testMapToAnyExplicit", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BString> map = (BMap<String, BString>) returns[0];
        Assert.assertEquals(map.get("name").stringValue(), "supun");
    }

    @Test(enabled = false, description = "Test casting a struct to another struct in a different package")
    public void testCastToStructInDifferentPkg() {
        result = BTestUtils.compile("lang/expressions/btype/cast/foo");
        BValue[] returns = BTestUtils.invoke(result, "testCastToStructInDifferentPkg", new BValue[]{});
    }

    @Test(enabled = false)
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BTestUtils.invoke(result, "testCompatibleStructForceCasting", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct structC = (BStruct) returns[0];

        Assert.assertEquals(structC.getStringField(0), "updated-x-valueof-a");

        Assert.assertEquals(structC.getIntField(0), 4);

        // check whether error is null
        Assert.assertNull(returns[1]);
    }

    @Test(enabled = false)
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BTestUtils.invoke(result, "testInCompatibleStructForceCasting", new BValue[]{});

        // check whether struct is null
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'B' cannot be cast to 'A'");

        String sourceType = error.getStringField(1);
        Assert.assertEquals(sourceType, "B");

        String targetType = error.getStringField(2);
        Assert.assertEquals(targetType, "A");
    }

    @Test(enabled = false, description = "Test returning a mismatching error when casting",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "mismatch-error-in-multi-return-casting.bal:18: incompatible types: " +
                    "expected 'ballerina.lang.errors:TypeCastError', found 'Error'")
    public void testMistmatchErrorInMultiReturnCasting() {
        BTestUtils.compile("test-src/expressions/cast/mismatch-error-in-multi-return-casting.bal");
    }

    @Test(enabled = false, description = "Test casting with too many returns",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "casting-with-too-many-returns.bal:17: assignment count mismatch: " +
                    "3 != 2")
    public void testCastingWithTooManyReturns() {
        BTestUtils.compile("test-src/expressions/cast/casting-with-too-many-returns.bal");
    }


    @Test(enabled = false)
    public void testAnyToStringWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToStringWithErrors", new BValue[]{});

        // check whether string is empty
        //TODO : Check with VM string registry maintain empty as null
        Assert.assertEquals(returns[0].stringValue(), "");

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'int' cannot be cast to 'string'");
    }

    @Test (enabled = false, description = "Test any to string casting happens without errors, error struct should be null")
    public void testAnyToStringWithoutErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToStringWithoutErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "value");

        // check the error
        Assert.assertNull(returns[1]);
    }

    @Test (enabled = false, description = "Test any to int casting happens without errors, error struct should be null")
    public void testAnyToIntWithoutErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToIntWithoutErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);

        // check the error
        Assert.assertNull(returns[1]);
    }

    @Test (enabled = false, description = "Test any to float casting happens without errors, error struct should be null")
    public void testAnyToFloatWithoutErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToFloatWithoutErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 6.99);

        // check the error
        Assert.assertNull(returns[1]);
    }

    @Test (enabled = false, description = "Test any to boolean casting happens without errors, error struct should be null")
    public void testAnyToBooleanWithoutErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToBooleanWithoutErrors", new BValue[]{});

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);

        // check the error
        Assert.assertNull(returns[1]);
    }

    @Test(enabled = false)
    public void testAnyNullToStringWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToStringWithErrors", new BValue[]{});

        // check whether string is empty
        //TODO : Check with VM string registry maintain empty as null
        Assert.assertEquals(returns[0].stringValue(), "");

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'string'");
    }

    @Test(enabled = false)
    public void testAnyToBooleanWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToBooleanWithErrors", new BValue[]{});

        // check whether string is empty
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'int' cannot be cast to 'boolean'");
    }

    @Test(enabled = false)
    public void testAnyNullToBooleanWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToBooleanWithErrors", new BValue[]{});

        // check whether string is empty
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'boolean'");
    }

    @Test(enabled = false)
    public void testAnyToIntWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToIntWithErrors", new BValue[]{});

        // check whether int is zero
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'int'");
    }

    @Test(enabled = false)
    public void testAnyNullToIntWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToIntWithErrors", new BValue[]{});

        // check whether int is zero
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'int'");
    }

    @Test(enabled = false)
    public void testAnyToFloatWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToFloatWithErrors", new BValue[]{});

        // check whether float is zero
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'float'");
    }

    @Test(enabled = false)
    public void testAnyNullToFloatWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToFloatWithErrors", new BValue[]{});

        // check whether float is zero
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'float'");
    }

    @Test(enabled = false)
    public void testAnyToMapWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToMapWithErrors", new BValue[]{});

        // check whether map is null
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'map'");
    }

    // TODO:
/*    @Test
    public void testErrorInForceCasting() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testErrorInForceCasting", new BValue[]{});

        // check whether float is zero
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "incompatible types: expected 'A', found 'B'");
    }*/
}
