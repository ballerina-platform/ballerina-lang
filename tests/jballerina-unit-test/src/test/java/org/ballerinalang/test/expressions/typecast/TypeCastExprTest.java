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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for type casting.
 */
public class TypeCastExprTest {
    private static final double DELTA = 0.01;
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/type-casting.bal");
    }

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

    @Test
    public void testFloatToInt() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = BRunUtil.invoke(result, "floattoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void floattointWithError() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = BRunUtil.invoke(result, "floattointWithError", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToFloat() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = BRunUtil.invoke(result, "inttofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 5.5555555E7;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test
    public void testStringToInt() {
        BValue[] args = {new BString("100")};
        BValue[] returns = BRunUtil.invoke(result, "stringtoint", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "100";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringToFloat() {
        BValue[] args = {new BString("2222.333f")};
        BValue[] returns = BRunUtil.invoke(result, "stringtofloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 2222.333;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test
    public void testStringToJSON() {
        BValue[] args = {new BString("{\"name\":\"chanaka\"}")};
        BValue[] returns = BRunUtil.invoke(result, "testStringToJson", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToString() {
        BValue[] args = {new BInteger(111)};
        BValue[] returns = BRunUtil.invoke(result, "inttostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToString() {
        BValue[] args = {new BFloat(111.333f)};
        BValue[] returns = BRunUtil.invoke(result, "floattostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns[0].stringValue().substring(0, 7), expected);
    }

    @Test
    public void testBooleanToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BRunUtil.invoke(result, "booleantostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testAnyToString() {
        BValue[] returns = BRunUtil.invoke(result, "anyjsontostring");
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected2 = "{\"a\":\"b\"}";
        Assert.assertEquals(returns[0].stringValue(), expected2);
    }

    @Test
    public void testBooleanAppendToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BRunUtil.invoke(result, "booleanappendtostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true-append-true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJSONToString() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonToStringCast");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test
    public void testJSONObjectToStringCast() {
        BValue[] returns = BRunUtil.invoke(result, "testJSONObjectToStringCast");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{\"foo\":\"bar\"}");
    }

    @Test
    public void testJsonToInt() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonToInt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testJsonToFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonToFloat");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 7.65);
    }

    @Test
    public void testJsonToBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonToBoolean");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    /*@Test
    public void testSimpleJsonToMap() {
        BValue[] returns = BTestUtils.invoke(result, "testSimpleJsonToMap");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap map = (BMap) returns[0];

        BValue value1 = map.get(new BString("fname"));
        Assert.assertTrue(value1 instanceof BString);
        Assert.assertEquals(value1.stringValue(), "Supun");

        BValue value2 = map.get(new BString("lname"));
        Assert.assertTrue(value2 instanceof BString);
        Assert.assertEquals(value2.stringValue(), "Setunga");
    }*/

    /*@Test
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
    }*/

    /*@Test
    public void testSimpleMapToJson() {
        BValue[] returns = BTestUtils.invoke(result, "testSimpleMapToJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        JsonNode jsonNode = ((BJSON) returns[0]).value();
        Assert.assertEquals(jsonNode.get("fname").textValue(), "Supun");
        Assert.assertEquals(jsonNode.get("lname").textValue(), "Setunga");
    }*/

    /*@Test
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
    }*/

    @Test
    public void testStructToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testStructToStruct");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> student = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(student.get("name").stringValue(), "Supun");

        Assert.assertEquals(((BInteger) student.get("age")).intValue(), 25);

        BValue address = student.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, BValue> addressMap = (BMap<String, BValue>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValueArray marksArray = (BValueArray) student.get("marks");
        Assert.assertTrue(marksArray instanceof BValueArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 24);
        Assert.assertEquals(marksArray.getInt(1), 81);
    }

    @Test
    public void testIncompatibleCast() {
        CompileResult res = BCompileUtil.compile("test-src/expressions/typecast/incompatible-cast-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 6);
        BAssertUtil.validateError(res, 0, "incompatible types: expected 'Person', found 'Student'", 24, 16);
        BAssertUtil.validateError(res, 1, "incompatible types: expected 'float', found 'int'", 33, 16);
        BAssertUtil.validateError(res, 2, "incompatible types: expected 'float', found 'int'", 33, 19);
        BAssertUtil.validateError(res, 3, "incompatible types: expected 'float', found 'int'", 33, 22);
        BAssertUtil.validateError(res, 4, "incompatible types: expected 'float', found 'int'", 37, 18);
        BAssertUtil.validateError(res, 5, "incompatible types: 'any[]' cannot be cast to 'json'", 43, 24);
    }

    @Test(description = "Test casting a JSON integer to a string")
    public void testJsonIntToString() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonIntToString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }

    @Test
    public void testIncompatibleJsonToInt() {
        BValue[] returns = BRunUtil.invoke(result, "testIncompatibleJsonToInt");
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'string' value 'hello' cannot be converted to 'int'");
    }

    @Test
    public void testIncompatibleJsonToFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testIncompatibleJsonToFloat");
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'string' value 'hello' cannot be converted to 'float'");
    }

    public void testBooleanInJsonToInt() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInJsonToInt");
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = error.getReason();
        Assert.assertEquals(errorMsg, "'boolean' cannot be cast to 'int'");
    }

    @Test(description = "Test casting a null JSON to string",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<json>' cannot be cast to 'string'.*")
    public void testNullJsonToString() {
        BRunUtil.invoke(result, "testNullJsonToString");
    }

    @Test(description = "Test casting a null JSON to int",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<json>' cannot be cast to 'int'.*")
    public void testNullJsonToInt() {
        BRunUtil.invoke(result, "testNullJsonToInt");
    }

    @Test(description = "Test casting a null JSON to float",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<json>' cannot be cast to 'float'.*")
    public void testNullJsonToFloat() {
        BRunUtil.invoke(result, "testNullJsonToFloat");
    }

    @Test(description = "Test casting a null JSON to boolean",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<json>' cannot be cast to 'boolean'.*")
    public void testNullJsonToBoolean() {
        BRunUtil.invoke(result, "testNullJsonToBoolean");
    }

//    @Test(description = "Test casting a null Struct to Struct")
//    public void testNullStructToStruct() {
//        BValue[] returns = BRunUtil.invoke(result, "testNullStructToStruct");
//        Assert.assertEquals(returns[0], null);
//    }

    @Test(description = "Test casting an int as any type to json")
    public void testAnyIntToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyIntToJson");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
    }

    @Test(description = "Test casting a string as any type to json")
    public void testAnyStringToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyStringToJson");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
    }

    @Test(description = "Test casting a boolean as any type to json")
    public void testAnyBooleanToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyBooleanToJson");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test casting a float as any type to json")
    public void testAnyFloatToJson() {
        BRunUtil.invoke(result, "testAnyFloatToJson");
        BValue[] returns = BRunUtil.invoke(result, "testAnyFloatToJson");
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 8.73);
    }

    @Test(description = "Test casting a map as any type to json",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*incompatible types: 'map' cannot be cast to 'json'.*")
    public void testAnyMapToJson() {
        BRunUtil.invoke(result, "testAnyMapToJson");
    }

    @Test(description = "Test casting a struct as any type to json",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*incompatible types: 'Address' cannot be cast to 'json'.*")
    public void testAnyStructToJson() {
        BRunUtil.invoke(result, "testAnyStructToJson");
    }

    @Test(description = "Test casting a json as any type to json")
    public void testAnyJsonToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyJsonToJson");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].toString(), "{\"home\":\"SriLanka\"}");
    }

    @Test(description = "Test casting a any to table")
    public void testAnyToTable() {
        BRunUtil.invoke(result, "testAnyToTable");
    }

    @Test(description = "Test casting a null as any type to json")
    public void testAnyNullToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToJson");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test casting struct stored as any to struct")
    public void testStructAsAnyToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testStructAsAnyToStruct");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> student = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(student.get("name").stringValue(), "Supun");

        Assert.assertEquals(((BInteger) student.get("age")).intValue(), 25);

        BValue address = student.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, BValue> addressMap = (BMap<String, BValue>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValueArray marksArray = (BValueArray) student.get("marks");
        Assert.assertTrue(marksArray instanceof BValueArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 24);
        Assert.assertEquals(marksArray.getInt(1), 81);

        double score = ((BFloat) student.get("score")).floatValue();
        Assert.assertEquals(score, 0.0);
    }

    @Test(description = "Test casting any to struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map' cannot be cast to 'Person'.*")
    public void testAnyToStruct() {
        BRunUtil.invoke(result, "testAnyToStruct");
    }

    @Test(description = "Test casting a null stored as any to struct",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'Person'.*")
    public void testAnyNullToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToStruct");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test casting a null stored as any to map",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'map'.*")
    public void testAnyNullToMap() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToMap");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test casting a null stored as any to xml",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to " +
                  "'xml\\<lang\\.xml:Element" + "\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction\\|" +
                  "lang\\.xml:Text\\>'.*")
    public void testAnyNullToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToXml");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test record as any")
    public void testRecordToAny() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordToAny");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> student = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(student.get("name").stringValue(), "Supun");

        Assert.assertEquals(((BInteger) student.get("age")).intValue(), 25);

        BValue address = student.get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, BValue> addressMap = (BMap<String, BValue>) address;
        Assert.assertEquals(addressMap.get("city").stringValue(), "Kandy");
        Assert.assertEquals(addressMap.get("country").stringValue(), "SriLanka");

        BValueArray marksArray = (BValueArray) student.get("marks");
        Assert.assertTrue(marksArray instanceof BValueArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 24);
        Assert.assertEquals(marksArray.getInt(1), 81);

        Assert.assertEquals(((BFloat) student.get("score")).floatValue(), 0.0);
    }

    @Test(description = "Test map as any")
    public void testMapToAny() {
        BValue[] returns = BRunUtil.invoke(result, "testMapToAny");
        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> map = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(map.get("name").stringValue(), "supun");
    }

    @Test(description = "Test casting a struct to another struct in a different package")
    public void testCastToStructInDifferentPkg() {
        CompileResult res = BCompileUtil.compile(this, "test-src/expressions.typecast.foo",
                "expressions.typecast.foo");
        BRunUtil.invoke(res, "testCastToStructInDifferentPkg");
    }

    @Test
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testCompatibleStructForceCasting");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BValue> structC = (BMap<String, BValue>) returns[0];

        Assert.assertEquals(structC.get("x").stringValue(), "updated-x-valueof-a");

        Assert.assertEquals(((BInteger) structC.get("y")).intValue(), 4);
    }

    @Test
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testInCompatibleStructForceCasting");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'B' value cannot be converted to 'A'");
    }

    @Test (description = "Test any to int casting happens without errors, error struct should be null")
    public void testAnyToIntWithoutErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToIntWithoutErrors");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);
    }

    @Test (description = "Test any to float casting happens without errors, error struct should be null")
    public void testAnyToFloatWithoutErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToFloatWithoutErrors");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 6.99);
    }

    @Test (description = "Test any to boolean casting happens without errors, error struct should be null")
    public void testAnyToBooleanWithoutErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToBooleanWithoutErrors");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAnyNullToString() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToString");

        // null to string should return string null
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testAnyToBooleanWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToBooleanWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: 'map' cannot be cast to 'boolean'");
    }

    @Test
    public void testAnyNullToBooleanWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToBooleanWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: '()' cannot be cast to 'boolean'");
    }

    @Test
    public void testAnyToIntWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToIntWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: 'string' cannot be cast to 'int'");
    }

    @Test
    public void testAnyNullToIntWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToIntWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: '()' cannot be cast to 'int'");
    }

    @Test
    public void testAnyToFloatWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToFloatWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: 'string' cannot be cast to 'float'");
    }

    @Test
    public void testAnyNullToFloatWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyNullToFloatWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: '()' cannot be cast to 'float'");
    }

    @Test
    public void testAnyToMapWithErrors() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToMapWithErrors");

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        BError error = (BError) returns[0];
        String errorMsg = ((BMap<String, BString>) error.getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible types: 'string' cannot be cast to 'map'");
    }

    // TODO:
/*    @Test
    public void testErrorInForceCasting() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testErrorInForceCasting");

        // check whether float is zero
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        BValue errorMsg = error.getValue(0);
        Assert.assertTrue(errorMsg instanceof BString);
        Assert.assertEquals(errorMsg.stringValue(), "incompatible types: expected 'A', found 'B'");
    }*/

    @Test
    public void testSameTypeCast() {
        BValue[] returns = BRunUtil.invoke(result, "testSameTypeCast");
        Assert.assertTrue(returns[0] instanceof BInteger);
        final int expected = 10;
        Assert.assertEquals(((BInteger) returns[0]).intValue(), expected);
    }

    @Test
    public void testJSONValueCasting() {
        BValue[] returns = BRunUtil.invoke(result, "testJSONValueCasting");
        Assert.assertEquals(returns[0].stringValue(), "hello");
        Assert.assertEquals(returns[1].stringValue(), "4");
        Assert.assertEquals(returns[2].stringValue(), "4.2");
        Assert.assertEquals(returns[3].stringValue(), "true");
    }

    @Test
    public void testAnonRecordInCast() {
        BValue[] returns = BRunUtil.invoke(result, "testAnonRecordInCast");
        Assert.assertEquals(returns[0].stringValue(), "{name:\"Pubudu\"}");
    }
}
