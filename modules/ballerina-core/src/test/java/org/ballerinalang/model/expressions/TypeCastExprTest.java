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
 * Test Cases for type casting.
 */
public class TypeCastExprTest {
    private static final double DELTA = 0.01;
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/type/cast/type-casting.bal");
    }

//    @Test
//    public void testXMLToJSON() {
//        BValue[] args = {new BXML("<name>chanaka</name>")};
//        BValue[] returns = Functions.invoke(bLangProgram, "xmltojson", args);
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        final String expected = "{\"name\":\"chanaka\"}";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }

//    @Test
//    public void testJSONToXML() {
//        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
//        BValue[] returns = Functions.invoke(bLangProgram, "jsontoxml", args);
//        Assert.assertTrue(returns[0] instanceof BXML);
//        final String expected = "<name>chanaka</name>";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }

    @Test
    public void testFloatToInt() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "floattoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToFloat() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "inttofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 5.5555555E7;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test
    public void testStringToInt() {
        BValue[] args = {new BString("100")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "stringtoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "100";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToFloat() {
        BValue[] args = {new BString("2222.333f")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "stringtofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 2222.333;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test
    public void testStringToJSON() {
        BValue[] args = {new BString("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStringToJson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

//    @Test
//    public void testStringToXML() {
//        BValue[] args = {new BString("<name>chanaka</name>")};
//        BValue[] returns = Functions.invoke(bLangProgram, "stringtoxml", args);
//        Assert.assertTrue(returns[0] instanceof BXML);
//        final String expected = "<name>chanaka</name>";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }

    @Test
    public void testIntToString() {
        BValue[] args = {new BInteger(111)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "inttostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToString() {
        BValue[] args = {new BFloat(111.333f)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "floattostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns[0].stringValue().substring(0, 7), expected);
    }

    @Test
    public void testBooleanToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "booleantostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testBooleanAppendToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "booleanappendtostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true-append-true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

//    @Test
//    public void testXMLToString() {
//        BValue[] args = {new BXML("<name>chanaka</name>")};
//        BValue[] returns = Functions.invoke(bLangProgram, "xmltostring", args);
//        Assert.assertTrue(returns[0] instanceof BString);
//        final String expected = "<name>chanaka</name>";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }
//
    @Test
    public void testJSONToString() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToString", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJsonToInt() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }
    
    @Test
    public void testJsonToFloat() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 7.65);
    }
    
    @Test
    public void testJsonToBoolean() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonToBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }
    
    @Test
    public void testIntArrayToLongArray() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "intarrtofloatarr");
        Assert.assertTrue(returns[0] instanceof BArray);
        BArray result = (BArray) returns[0];
        Assert.assertTrue(result.get(0) instanceof BFloat);
        Assert.assertEquals(((BFloat) result.get(0)).floatValue(), 999.0, DELTA);
        Assert.assertTrue(result.get(1) instanceof BFloat);
        Assert.assertEquals(((BFloat) result.get(1)).floatValue(), 95.0, DELTA);
        Assert.assertTrue(result.get(2) instanceof BFloat);
        Assert.assertEquals(((BFloat) result.get(2)).floatValue(), 889.0, DELTA);
    }
    
//    @Test
//    public void testSimpleJsonToMap() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSimpleJsonToMap");
//        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
//        BMap map = (BMap) returns[0];
//        
//        BValue value1 = map.get(new BString("fname"));
//        Assert.assertTrue(value1 instanceof BString);
//        Assert.assertEquals(value1.stringValue(), "Supun");
//        
//        BValue value2 = map.get(new BString("lname"));
//        Assert.assertTrue(value2 instanceof BString);
//        Assert.assertEquals(value2.stringValue(), "Setunga");
//    }
    
//    @Test
//    public void testComplexJsonToMap() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testComplexJsonToMap");
//        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
//        BMap map = (BMap) returns[0];
//
//        BValue value1 = map.get(new BString("name"));
//        Assert.assertTrue(value1 instanceof BString);
//        Assert.assertEquals(value1.stringValue(), "Supun");
//
//        BValue value2 = map.get(new BString("age"));
//        Assert.assertTrue(value2 instanceof BInteger);
//        Assert.assertEquals(((BInteger) value2).intValue(), 25);
//
//        BValue value3 = map.get(new BString("gpa"));
//        Assert.assertTrue(value3 instanceof BFloat);
//        Assert.assertEquals(((BFloat) value3).floatValue(), 2.81);
//
//        BValue value4 = map.get(new BString("status"));
//        Assert.assertTrue(value4 instanceof BBoolean);
//        Assert.assertEquals(((BBoolean) value4).booleanValue(), true);
//
//        BValue value5 = map.get(new BString("info"));
//        Assert.assertEquals(value5, null);
//
//        BValue value6 = map.get(new BString("address"));
//        Assert.assertTrue(value6 instanceof BJSON);
//        Assert.assertEquals(value6.stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
//
//        BValue value7 = map.get(new BString("marks"));
//        Assert.assertTrue(value7 instanceof BJSON);
//        Assert.assertEquals(value7.stringValue(), "[1,5,7]");
//    }
    
//    @Test
//    public void testSimpleMapToJson() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSimpleMapToJson");
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        JsonNode jsonNode = ((BJSON) returns[0]).value();
//        Assert.assertEquals(jsonNode.get("fname").textValue(), "Supun");
//        Assert.assertEquals(jsonNode.get("lname").textValue(), "Setunga");
//    }
    
//    @Test
//    public void testComplexMapToJson() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testComplexMapToJson");
//        Assert.assertTrue(returns[0] instanceof BJSON);
//        JsonNode jsonNode = ((BJSON) returns[0]).value();
//        Assert.assertEquals(jsonNode.get("name").textValue(), "Supun");
//        Assert.assertEquals(jsonNode.get("age").intValue(), 25);
//        Assert.assertEquals(jsonNode.get("status").booleanValue(), true);
//        Assert.assertTrue(jsonNode.get("info").isNull());
//        Assert.assertEquals(jsonNode.get("intArray").toString(), "[7,8,9]");
//        
//        JsonNode addressNode = jsonNode.get("address");
//        Assert.assertEquals(addressNode.get("country").textValue(), "USA");
//        Assert.assertEquals(addressNode.get("city").textValue(), "CA");
//    }
    
    @Test
    public void testStructToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct student = (BStruct) returns[0];
        
        BValue name = student.getValue(0);
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Supun");

        BValue age = student.getValue(1);
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue address = student.getValue(2);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<BString, ?> addressMap = (BMap<BString, ?>) address;
        Assert.assertEquals(addressMap.get(new BString("city")).stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get(new BString("country")).stringValue(), "SriLanka");
        
        BValue marks = student.getValue(3);
        Assert.assertTrue(marks instanceof BArray);
        BArray<BInteger> marksArray = (BArray<BInteger>) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0).intValue(), 24);
        Assert.assertEquals(marksArray.get(1).intValue(), 81);
    }
    
    @Test(description = "Test casting a struct to an incompatible struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "incompatible-struct-cast.bal:24: incompatible types: 'Student' " +
            "cannot be assigned to 'Person'")
    public void testIncompatibleStructToStructCast() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/incompatible-struct-cast.bal");
    }
    
    @Test(description = "Test casting a JSON integer to a string")
    public void testJsonIntToString() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonIntToString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }
    
    @Test(description = "Test casting an incomatible JSON to integer",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'int': incompatible types: expected 'int'," +
            " found 'string' in json")
    public void testIncompatibleJsonToInt() {
        BLangFunctions.invoke(bLangProgram, "testIncompatibleJsonToInt");
    }
    
    @Test(description = "Test casting an incomatible JSON to float",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'float': incompatible types: expected " +
            "'float', found 'string' in json")
    public void testIncompatibleJsonToFloat() {
        BLangFunctions.invoke(bLangProgram, "testIncompatibleJsonToFloat");
    }
    
    @Test(description = "Test casting an incomatible JSON to boolean",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'boolean': incompatible types: expected " +
            "'boolean', found 'string' in json")
    public void testIncompatibleJsonToBoolean() {
        BLangFunctions.invoke(bLangProgram, "testIncompatibleJsonToBoolean");
    }
    
    @Test(description = "Test casting a boolean in JSON to int",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'int': incompatible types: expected " +
            "'int', found 'boolean' in json")
    public void testBooleanInJsonToInt() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testBooleanInJsonToInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }
    
    @Test(description = "Test casting an integer in JSON to float",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'json' to type 'float': incompatible types: expected " +
            "'float', found 'int' in json")
    public void testIntInJsonToFloat() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testIntInJsonToFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 7.0);
    }
    
    @Test(description = "Test casting a null JSON to string",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'null' value to type 'string'")
    public void testNullJsonToString() {
        BLangFunctions.invoke(bLangProgram, "testNullJsonToString");
    }
    
    @Test(description = "Test casting a null JSON to int",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'null' value to type 'int'")
    public void testNullJsonToInt() {
        BLangFunctions.invoke(bLangProgram, "testNullJsonToInt");
    }
    
    @Test(description = "Test casting a null JSON to float",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'null' value to type 'float'")
    public void testNullJsonToFloat() {
        BLangFunctions.invoke(bLangProgram, "testNullJsonToFloat");
    }
    
    @Test(description = "Test casting a null JSON to boolean",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'null' value to type 'boolean'")
    public void testNullJsonToBoolean() {
        BLangFunctions.invoke(bLangProgram, "testNullJsonToBoolean");
    }
    
    @Test(description = "Test casting a null Struct to Struct")
    public void testNullStructToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testNullStructToStruct");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test(description = "Test casting an int as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'int' to type 'json'")
    public void testAnyIntToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyIntToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().intValue(), 8);
    }
    
    @Test(description = "Test casting a string as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'string' to type 'json'")
    public void testAnyStringToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyStringToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().textValue(), "Supun");
    }
    
    @Test(description = "Test casting a boolean as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'boolean' to type 'json'")
    public void testAnyBooleanToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyBooleanToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().booleanValue(), true);
    }
    
    @Test(description = "Test casting a float as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'float' to type 'json'")
    public void testAnyFloatToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyFloatToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().floatValue(), 8.73F);
    }
    
    @Test(description = "Test casting a map as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'map' to type 'json'")
    public void testAnyMapToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyMapToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().toString(), "{\"name\":\"supun\"}");
    }
    
    @Test(description = "Test casting a struct as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'Address' to type 'json'")
    public void testAnyStructToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyStructToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().toString(), "{\"city\":\"CA\",\"country\":\"\"}");
    }
    
    @Test(description = "Test casting a json as any type to json")
    public void testAnyJsonToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyJsonToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().toString(), "{\"home\":\"SriLanka\"}");
    }
    
    @Test(description = "Test casting a null as any type to json")
    public void testAnyNullToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToJson");
        Assert.assertEquals(returns[0], null);
    }
    
    @Test(description = "Test casting an array as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'any\\[\\]' to type 'json'")
    public void testAnyArrayToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyArrayToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().toString(), "[8,4,6]");
    }
    
    @Test(description = "Test casting a xml as any type to json",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'xml' to type 'json'")
    public void testAnyXmlToJson() {
        BLangFunctions.invoke(bLangProgram, "testAnyXmlToJson");
    }
    
    @Test(description = "Test casting a struct to map",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "struct-to-map.bal:22: incompatible types: 'Person' cannot be cast " +
            "to 'map'")
    public void testStructToMap() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/struct-to-map.bal");
    }

    @Test(description = "Test casting a map to struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "map-to-struct.bal:36: incompatible types: 'map' cannot be cast to " +
            "'Person'")
    public void testMapToStruct() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/map-to-struct.bal");
    }
    
    @Test(description = "Test casting a json to map",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "json-to-map.bal:9: incompatible types: 'json' cannot be cast to 'map'")
    public void testJsonToMap() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/json-to-map.bal");
    }
    
    @Test(description = "Test casting a json to struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "json-to-struct.bal:34: incompatible types: 'json' cannot be cast to" +
            " 'Person'")
    public void testJsonToStruct() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/json-to-struct.bal");
    }
    
    @Test(description = "Test casting struct stored as any to struct")
    public void testStructAsAnyToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructAsAnyToStruct");
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct student = (BStruct) returns[0];
        
        BValue name = student.getValue(0);
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Supun");

        BValue age = student.getValue(1);
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue address = student.getValue(2);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<BString, ?> addressMap = (BMap<BString, ?>) address;
        Assert.assertEquals(addressMap.get(new BString("city")).stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get(new BString("country")).stringValue(), "SriLanka");
        
        BValue marks = student.getValue(3);
        Assert.assertTrue(marks instanceof BArray);
        BArray<BInteger> marksArray = (BArray<BInteger>) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0).intValue(), 24);
        Assert.assertEquals(marksArray.get(1).intValue(), 81);
        
        BValue score = student.getValue(7);
        Assert.assertTrue(score instanceof BFloat);
        Assert.assertEquals(((BFloat) score).floatValue(), 0.0);
    }
    
    @Test(description = "Test casting any to struct",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'map' to type 'Person'")
    public void testAnyToStruct() {
        BLangFunctions.invoke(bLangProgram, "testAnyToStruct");
    }
    
    @Test(description = "Test casting a null stored as any to struct")
    public void testAnyNullToStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToStruct");
        Assert.assertNull(returns[0]);
    }
    
    @Test(description = "Test casting a null stored as any to map")
    public void testAnyNullToMap() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToMap");
        Assert.assertNull(returns[0]);
    }
    
    @Test(description = "Test casting a null stored as any to xml")
    public void testAnyNullToXml() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToXml");
        Assert.assertNull(returns[0]);
    }
    
    @Test(description = "Test explicit casting struct to any")
    public void testStructToAnyExplicit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructToAnyExplicit");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct student = (BStruct) returns[0];
        
        BValue name = student.getValue(0);
        Assert.assertTrue(name instanceof BString);
        Assert.assertEquals(name.stringValue(), "Supun");

        BValue age = student.getValue(1);
        Assert.assertTrue(age instanceof BInteger);
        Assert.assertEquals(((BInteger) age).intValue(), 25);

        BValue address = student.getValue(2);
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<BString, ?> addressMap = (BMap<BString, ?>) address;
        Assert.assertEquals(addressMap.get(new BString("city")).stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get(new BString("country")).stringValue(), "SriLanka");
        
        BValue marks = student.getValue(3);
        Assert.assertTrue(marks instanceof BArray);
        BArray<BInteger> marksArray = (BArray<BInteger>) marks;
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.get(0).intValue(), 24);
        Assert.assertEquals(marksArray.get(1).intValue(), 81);
        
        BValue score = student.getValue(7);
        Assert.assertTrue(score instanceof BFloat);
        Assert.assertEquals(((BFloat) score).floatValue(), 0.0);
    }
    
    @Test(description = "Test explicit casting struct to any")
    public void testMapToAnyExplicit() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testMapToAnyExplicit");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<BString, ?> map = (BMap<BString, ?>) returns[0];
        Assert.assertEquals(map.get(new BString("name")).stringValue(), "supun");
    }
    
    @Test(description = "Test casting a struct to another struct in a different package")
    public void testCastToStructInDifferentPkg() {
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/expressions/type/cast/foo");
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCastToStructInDifferentPkg");
    }
    
    // Casting with errors returned

    @Test
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCompatibleStructForceCasting", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct structC = (BStruct) returns[0];
        
        BValue xVal = structC.getValue(0);
        Assert.assertTrue(xVal instanceof BString);
        Assert.assertEquals(xVal.stringValue(), "updated-x-valueof-a");

        BValue yVal = structC.getValue(1);
        Assert.assertTrue(yVal instanceof BInteger);
        Assert.assertEquals(((BInteger) yVal).intValue(), 4);
        
        // check whether error is null
        Assert.assertNull(returns[1]);
    }
    
    @Test
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testInCompatibleStructForceCasting", new BValue[]{});
        
        // check whether struct is null
        Assert.assertNull(returns[0]);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "incompatible types: expected 'A', found 'B'");
        
        BValue sourceType = error.getValue(2);
        Assert.assertTrue(sourceType instanceof BString);
        Assert.assertEquals(sourceType.stringValue(), "B");
        
        BValue targetType = error.getValue(3);
        Assert.assertTrue(targetType instanceof BString);
        Assert.assertEquals(targetType.stringValue(), "A");
    }
    
    @Test(description = "Test returning a mismatching error when casting",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "mismatch-error-in-multi-return-casting.bal:18: incompatible types for" +
            " 'err': expected 'ballerina.lang.errors:CastError', found 'Error'")
    public void testMistmatchErrorInMultiReturnCasting() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/mismatch-error-in-multi-return-casting.bal");
    }
    
    @Test(description = "Test casting with too many returns",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "casting-with-too-many-returns.bal:17: assignment count mismatch: " +
            "3 != 2")
    public void testCastingWithTooManyReturns() {
        BTestUtils.parseBalFile("lang/expressions/type/cast/casting-with-too-many-returns.bal");
    }
    
    
    @Test
    public void testAnyToStringWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyToStringWithErrors", new BValue[]{});
        
        // check whether string is empty
        Assert.assertEquals(returns[0].stringValue(), "");
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'any' with type 'int' to type 'string'");
    }
    
    @Test
    public void testAnyNullToStringWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToStringWithErrors", new BValue[]{});
        
        // check whether string is empty
        Assert.assertEquals(returns[0].stringValue(), "");
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'null' value to type 'string'");
    }
    
    @Test
    public void testAnyToBooleanWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyToBooleanWithErrors", new BValue[]{});
        
        // check whether string is empty
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'any' with type 'int' to type 'boolean'");
    }
    
    @Test
    public void testAnyNullToBooleanWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToBooleanWithErrors", new BValue[]{});
        
        // check whether string is empty
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'null' value to type 'boolean'");
    }
    
    @Test
    public void testAnyToIntWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyToIntWithErrors", new BValue[]{});
        
        // check whether int is zero
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'any' with type 'string' to type 'int'");
    }
    
    @Test
    public void testAnyNullToIntWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToIntWithErrors", new BValue[]{});
        
        // check whether int is zero
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'null' value to type 'int'");
    }
    
    @Test
    public void testAnyToFloatWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyToFloatWithErrors", new BValue[]{});
        
        // check whether float is zero
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'any' with type 'string' to type 'float'");
    }
    
    @Test
    public void testAnyNullToFloatWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyNullToFloatWithErrors", new BValue[]{});
        
        // check whether float is zero
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'null' value to type 'float'");
    }
    
    @Test
    public void testAnyToMapWithErrors() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testAnyToMapWithErrors", new BValue[]{});
        
        // check whether map is null
        Assert.assertNull(returns[0]);
        
        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "cannot cast 'any' with type 'string' to type 'map'");
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
