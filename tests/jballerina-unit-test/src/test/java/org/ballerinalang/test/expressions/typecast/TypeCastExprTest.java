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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    @Test
    public void testFloatToInt() {
        Object[] args = {(222222.44444f)};
        Object returns = BRunUtil.invoke(result, "floattoint", args);
        Assert.assertTrue(returns instanceof Long);
        final String expected = "222222";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void floattointWithError() {
        Object[] args = {(222222.44444f)};
        Object returns = BRunUtil.invoke(result, "floattointWithError", args);
        Assert.assertTrue(returns instanceof Long);
        final String expected = "222222";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testIntToFloat() {
        Object[] args = {(55555555)};
        Object returns = BRunUtil.invoke(result, "inttofloat", args);
        Assert.assertTrue(returns instanceof Double);
        double expected = 5.5555555E7;
        Assert.assertEquals((Double) returns, expected, DELTA);
    }

    @Test
    public void testStringToInt() {
        Object[] args = {StringUtils.fromString("100")};
        Object returns = BRunUtil.invoke(result, "stringtoint", args);
        Assert.assertTrue(returns instanceof Long);
        final String expected = "100";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testIncompatibleStringToFloat() {
        Object[] args = {StringUtils.fromString("2222.333f")};
        Object returns = BRunUtil.invoke(result, "stringtofloat", args);
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg = ((BMap) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "'string' value '2222.333f' cannot be converted to 'float'");
    }

    @Test
    public void testStringToJSON() {
        Object[] args = {StringUtils.fromString("{\"name\":\"chanaka\"}")};
        Object returns = BRunUtil.invoke(result, "testStringToJson", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testIntToString() {
        Object[] args = {(111)};
        Object returns = BRunUtil.invoke(result, "inttostring", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testFloatToString() {
        Object[] args = {(111.333f)};
        Object returns = BRunUtil.invoke(result, "floattostring", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns.toString().substring(0, 7), expected);
    }

    @Test
    public void testBooleanToString() {
        Object[] args = {(true)};
        Object returns = BRunUtil.invoke(result, "booleantostring", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testAnyToString() {
        Object returns = BRunUtil.invoke(result, "anyjsontostring");
        Assert.assertTrue(returns instanceof BString);
        final String expected2 = "{\"a\":\"b\"}";
        Assert.assertEquals(returns.toString(), expected2);
    }

    @Test
    public void testBooleanAppendToString() {
        Object[] args = {(true)};
        Object returns = BRunUtil.invoke(result, "booleanappendtostring", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "true-append-true";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testJSONToString() {
        Object returns = BRunUtil.invoke(result, "testJsonToStringCast");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hello");
    }

    @Test
    public void testJSONObjectToStringCast() {
        Object returns = BRunUtil.invoke(result, "testJSONObjectToStringCast");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "{\"foo\":\"bar\"}");
    }

    @Test
    public void testJsonToInt() {
        Object returns = BRunUtil.invoke(result, "testJsonToInt");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test
    public void testJsonToFloat() {
        Object returns = BRunUtil.invoke(result, "testJsonToFloat");
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 7.65);
    }

    @Test
    public void testJsonToBoolean() {
        Object returns = BRunUtil.invoke(result, "testJsonToBoolean");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testStructToStruct() {
        Object returns = BRunUtil.invoke(result, "testStructToStruct");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> student = (BMap<String, Object>) returns;

        Assert.assertEquals(student.get(StringUtils.fromString("name")).toString(), "Supun");

        Assert.assertEquals(student.get(StringUtils.fromString("age")), 25L);

        Object address = student.get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, Object> addressMap = (BMap<String, Object>) address;
        Assert.assertEquals(addressMap.get(StringUtils.fromString("city")).toString(), "Kandy");
        Assert.assertEquals(addressMap.get(StringUtils.fromString("country")).toString(), "SriLanka");

        BArray marksArray = (BArray) student.get(StringUtils.fromString("marks"));
        Assert.assertTrue(marksArray instanceof BArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 24);
        Assert.assertEquals(marksArray.getInt(1), 81);
    }

    @Test
    public void testIncompatibleCast() {
        CompileResult res = BCompileUtil.compile("test-src/expressions/typecast/incompatible-cast-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 5);
        BAssertUtil.validateError(res, 0, "incompatible types: expected 'Person', found 'Student'", 24, 16);
        BAssertUtil.validateError(res, 1, "incompatible types: expected 'float', found 'int'", 33, 16);
        BAssertUtil.validateError(res, 2, "incompatible types: expected 'float', found 'int'", 33, 19);
        BAssertUtil.validateError(res, 3, "incompatible types: expected 'float', found 'int'", 33, 22);
        BAssertUtil.validateError(res, 4, "incompatible types: expected 'float', found 'int'", 37, 18);
    }

    @Test(description = "Test casting a JSON integer to a string")
    public void testJsonIntToString() {
        Object returns = BRunUtil.invoke(result, "testJsonIntToString");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "5");
    }

    @Test
    public void testIncompatibleJsonToInt() {
        Object returns = BRunUtil.invoke(result, "testIncompatibleJsonToInt");
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg = ((BMap) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "'string' value '\"hello\"' cannot be converted to 'int'");
    }

    @Test
    public void testIncompatibleJsonToFloat() {
        Object returns = BRunUtil.invoke(result, "testIncompatibleJsonToFloat");
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg = ((BMap) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "'string' value '\"hello\"' cannot be converted to 'float'");
    }

    @Test(enabled = false) // See https://github.com/ballerina-platform/ballerina-lang/issues/29359
    public void testBooleanInJsonToInt() {
        Object returns = BRunUtil.invoke(result, "testBooleanInJsonToInt");
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg = error.getMessage();
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

    @Test(description = "Test casting nil to a record",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'Student'.*")
    public void testNullStructToStruct() {
        BRunUtil.invoke(result, "testNullStructToStruct");
    }

    @Test(description = "Test casting an int as any type to json")
    public void testAnyIntToJson() {
        Object returns = BRunUtil.invoke(result, "testAnyIntToJson");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 8L);
    }

    @Test(description = "Test casting a string as any type to json")
    public void testAnyStringToJson() {
        Object returns = BRunUtil.invoke(result, "testAnyStringToJson");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Supun");
    }

    @Test(description = "Test casting a boolean as any type to json")
    public void testAnyBooleanToJson() {
        Object returns = BRunUtil.invoke(result, "testAnyBooleanToJson");
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test casting a float as any type to json")
    public void testAnyFloatToJson() {
        BRunUtil.invoke(result, "testAnyFloatToJson");
        Object returns = BRunUtil.invoke(result, "testAnyFloatToJson");
        Assert.assertTrue(returns instanceof Double);
        Assert.assertEquals(returns, 8.73);
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
        Object returns = BRunUtil.invoke(result, "testAnyJsonToJson");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(returns.toString(), "{\"home\":\"SriLanka\"}");
    }

    @Test(description = "Test casting a any to table")
    public void testAnyToTable() {
        BRunUtil.invoke(result, "testAnyToTable");
    }

    @Test(description = "Test casting a null as any type to json")
    public void testAnyNullToJson() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToJson");
        Assert.assertNull(returns);
    }

    @Test(description = "Test casting struct stored as any to struct")
    public void testStructAsAnyToStruct() {
        Object returns = BRunUtil.invoke(result, "testStructAsAnyToStruct");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> student = (BMap<String, Object>) returns;

        Assert.assertEquals(student.get(StringUtils.fromString("name")).toString(), "Supun");

        Assert.assertEquals((student.get(StringUtils.fromString("age"))), 25L);

        Object address = student.get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, Object> addressMap = (BMap<String, Object>) address;
        Assert.assertEquals(addressMap.get(StringUtils.fromString("city")).toString(), "Kandy");
        Assert.assertEquals(addressMap.get(StringUtils.fromString("country")).toString(), "SriLanka");

        BArray marksArray = (BArray) student.get(StringUtils.fromString("marks"));
        Assert.assertTrue(marksArray instanceof BArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 24);
        Assert.assertEquals(marksArray.getInt(1), 81);

        double score = (double) student.get(StringUtils.fromString("score"));
        Assert.assertEquals(score, 0.0);
    }

    @Test(description = "Test casting any to struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map' cannot be cast to 'Person'.*")
    public void testAnyToStruct() {
        BRunUtil.invoke(result, "testAnyToStruct");
    }

    @Test(description = "Test casting a null stored as any to struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'Person'.*")
    public void testAnyNullToStruct() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToStruct");
        Assert.assertNull(returns);
    }

    @Test(description = "Test casting a null stored as any to map",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'map'.*")
    public void testAnyNullToMap() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToMap");
        Assert.assertNull(returns);
    }

    @Test(description = "Test casting a null stored as any to xml",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to " +
                    "'xml<\\(lang\\.xml:Element" + "\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction\\|" +
                    "lang\\.xml:Text\\)>'.*")
    public void testAnyNullToXml() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToXml");
        Assert.assertNull(returns);
    }

    @Test(description = "Test record as any")
    public void testRecordToAny() {
        Object returns = BRunUtil.invoke(result, "testRecordToAny");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> student = (BMap<String, Object>) returns;

        Assert.assertEquals(student.get(StringUtils.fromString("name")).toString(), "Supun");

        Assert.assertEquals(student.get(StringUtils.fromString("age")), 25L);

        Object address = student.get(StringUtils.fromString("address"));
        Assert.assertTrue(address instanceof BMap<?, ?>);
        BMap<String, Object> addressMap = (BMap<String, Object>) address;
        Assert.assertEquals(addressMap.get(StringUtils.fromString("city")).toString(), "Kandy");
        Assert.assertEquals(addressMap.get(StringUtils.fromString("country")).toString(), "SriLanka");

        BArray marksArray = (BArray) student.get(StringUtils.fromString("marks"));
        Assert.assertTrue(marksArray instanceof BArray);
        Assert.assertEquals(marksArray.size(), 2);
        Assert.assertEquals(marksArray.getInt(0), 24);
        Assert.assertEquals(marksArray.getInt(1), 81);

        Assert.assertEquals(student.get(StringUtils.fromString("score")), 0.0);
    }

    @Test(description = "Test map as any")
    public void testMapToAny() {
        Object returns = BRunUtil.invoke(result, "testMapToAny");
        Assert.assertTrue(returns instanceof BMap<?, ?>);
        BMap<String, Object> map = (BMap<String, Object>) returns;
        Assert.assertEquals(map.get(StringUtils.fromString("name")).toString(), "supun");
    }

    @Test(description = "Test casting a struct to another struct in a different package")
    public void testCastToStructInDifferentPkg() {
        CompileResult res = BCompileUtil.compile("test-src/expressions.typecast.foo");
        BRunUtil.invoke(res, "testCastToStructInDifferentPkg");
    }

    @Test
    public void testCompatibleStructForceCasting() {
        Object returns = BRunUtil.invoke(result, "testCompatibleStructForceCasting");
        Assert.assertTrue(returns instanceof BMap);
        BMap<String, Object> structC = (BMap<String, Object>) returns;

        Assert.assertEquals(structC.get(StringUtils.fromString("x")).toString(), "updated-x-valueof-a");

        Assert.assertEquals(structC.get(StringUtils.fromString("y")), 4L);
    }

    @Test
    public void testInCompatibleStructForceCasting() {
        Object returns = BRunUtil.invoke(result, "testInCompatibleStructForceCasting");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "'B' value cannot be converted to 'ATypedesc': " +
                "\n\t\tmissing required field 'y' of type 'int' in record 'A'");
    }

    @Test(description = "Test any to int casting happens without errors, error struct should be null")
    public void testAnyToIntWithoutErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToIntWithoutErrors");

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 6L);
    }

    @Test(description = "Test any to float casting happens without errors, error struct should be null")
    public void testAnyToFloatWithoutErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToFloatWithoutErrors");

        Assert.assertSame(returns.getClass(), Double.class);
        Assert.assertEquals(returns, 6.99);
    }

    @Test(description = "Test any to boolean casting happens without errors, error struct should be null")
    public void testAnyToBooleanWithoutErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToBooleanWithoutErrors");

        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAnyNullToString() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToString");

        // null to string should return string null
        Assert.assertEquals(returns.toString(), "");
    }

    @Test
    public void testAnyToBooleanWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToBooleanWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: 'map' cannot be cast to 'boolean'");
    }

    @Test
    public void testAnyNullToBooleanWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToBooleanWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: '()' cannot be cast to 'boolean'");
    }

    @Test
    public void testAnyToIntWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToIntWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: 'string' cannot be cast to 'int'");
    }

    @Test
    public void testAnyNullToIntWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToIntWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: '()' cannot be cast to 'int'");
    }

    @Test
    public void testAnyToFloatWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToFloatWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: 'string' cannot be cast to 'float'");
    }

    @Test
    public void testAnyNullToFloatWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyNullToFloatWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: '()' cannot be cast to 'float'");
    }

    @Test
    public void testAnyToMapWithErrors() {
        Object returns = BRunUtil.invoke(result, "testAnyToMapWithErrors");

        // check the error
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg =
                ((BMap<String, BString>) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "incompatible types: 'string' cannot be cast to 'map'");
    }

    @Test(description = "Test any boolean to int with errors.")
    public void testAnyBooleanToIntWithErrors() {
        BRunUtil.invoke(result, "testAnyBooleanToIntWithErrors");
    }

    @Test(description = "Test any boolean to float with errors.")
    public void testAnyBooleanToFloatWithErrors() {
        BRunUtil.invoke(result, "testAnyBooleanToFloatWithErrors");
    }

    @Test(description = "Test any boolean to decimal with errors.")
    public void testAnyBooleanToDecimalWithErrors() {
        BRunUtil.invoke(result, "testAnyBooleanToDecimalWithErrors");
    }

    @Test(description = "Test any boolean to string with errors.")
    public void testAnyBooleanToStringWithErrors() {
        BRunUtil.invoke(result, "testAnyBooleanToStringWithErrors");
    }

    @Test(description = "Test any boolean to byte with errors.")
    public void testAnyBooleanToByteWithErrors() {
        BRunUtil.invoke(result, "testAnyBooleanToByteWithErrors");
    }

    @Test(description = "Test any boolean to union with errors.")
    public void testAnyBooleanToUnionWithErrors() {
        BRunUtil.invoke(result, "testAnyBooleanToUnionWithErrors");
    }

    @Test(description = "Test type cast to union type with int subtypes.")
    public void testCastingToIntSubtypesInUnion() {
        BRunUtil.invoke(result, "testCastingToIntSubtypesInUnion");
    }

    @Test(description = "Test erroneous type cast to union type with int subtypes.")
    public void testCastingToIntSubtypesInUnionNegative() {
        BRunUtil.invoke(result, "testCastingToIntSubtypesInUnionNegative");
    }

    @Test
    public void testSameTypeCast() {
        Object returns = BRunUtil.invoke(result, "testSameTypeCast");
        Assert.assertTrue(returns instanceof Long);
        final long expected = 10;
        Assert.assertEquals(returns, expected);
    }

    @Test
    public void testJSONValueCasting() {
        Object returns = BRunUtil.invoke(result, "testJSONValueCasting");
        Assert.assertEquals(returns.toString(), "[\"hello\",4,4.2,true]");
    }

    @Test
    public void testAnonRecordInCast() {
        Object returns = BRunUtil.invoke(result, "testAnonRecordInCast");
        Assert.assertEquals(returns.toString(), "{\"name\":\"Pubudu\"}");
    }

    @Test(dataProvider = "typeCastExprTestFunctions")
    public void testSimpleValueCasting(String function) {
        BRunUtil.invoke(result, function);
    }

    @Test(dataProvider = "typesTestExpressionTestFunctions")
    public void testTypeTestsExpression(String function) {
        BRunUtil.invoke(result, function);
    }

    @Test(dataProvider = "immutableArrayTypesTestFunctions")
    public void testCastOfImmutableArrayTypes(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "typeCastExprTestFunctions")
    public Object[] typeCastExprTestFunctions() {
        return new Object[] {
                "testDecimalToIntCasting",
                "testFloatToDecimalCasting",
                "testDecimalToFloatCasting"
        };
    }

    @DataProvider(name = "typesTestExpressionTestFunctions")
    public Object[][] typesTestExpressionTestFunctions() {
        return new Object[][]{
                {"testIntSubtypeArrayCasting"},
                {"testIntSubtypeArrayCastingWithErrors"},
                {"testCharArrayToStringArray"},
                {"testMapOfCharToMapOfString"},
                {"testFiniteTypeArrayToIntArray"},
                {"testFiniteTypeToStringArray"}
        };
    }

    @DataProvider(name = "immutableArrayTypesTestFunctions")
    public Object[][] immutableArrayTypesTestFunctions() {
        return new Object[][]{
                {"testCastOfReadonlyIntArrayToByteArray"},
                {"testCastOfReadonlyIntArrayToByteArrayNegative"},
                {"testCastOfReadonlyAnyToByteArray"},
                {"testCastOfReadonlyArrayToUnion"},
                {"testCastOfReadonlyUnionArrayToByteArray"},
                {"testCastOfReadonlyRecord"},
                {"testCastOfReadonlyRecordNegative"},
                {"testCastOfReadonlyStringArrayToStringConstantArray"},
                {"testCastOfTwoDimensionalIntArrayToByteArray"}
        };
    }

    @Test
    public void testCastJsonToMapOfAnydata() {
        BRunUtil.invoke(result, "testCastJsonToMapOfAnydata");
    }

    @Test
    public void testCastMapOfJsonToMapOfAnydata() {
        BRunUtil.invoke(result, "testCastMapOfJsonToMapOfAnydata");
    }

    @Test
    public void testInvalidTypeCastMapOfJsonToMapOfBasicType() {
        BRunUtil.invoke(result, "testInvalidTypeCastMapOfJsonToMapOfBasicType");
    }

    @Test
    public void testInvalidTypeCastJsonToMapOfAnydata() {
        BRunUtil.invoke(result, "testInvalidTypeCastJsonToMapOfAnydata");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
