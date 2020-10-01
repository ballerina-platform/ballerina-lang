/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.typecast;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Class to test type cast expressions.
 *
 * @since 0.994.0
 */
public class TypeCastExpressionsTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/type_cast_expr.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/typecast/type_cast_expr_negative.bal");
    }

    @Test(dataProvider = "positiveTests")
    public void testCastPositive(String functionName) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected assertion to succeed and return the " +
                "original value");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string' cannot be cast to '\\(\\)'.*")
    public void testNilCastNegative() {
        BRunUtil.invoke(result, "testNilCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'string'.*")
    public void testNilValueCastAsSimpleBasicTypeNegative() {
        BRunUtil.invoke(result, "testNilValueCastAsSimpleBasicTypeNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\(\\)' cannot be cast to 'map<string>'.*")
    public void testNilValueCastAsStructuredTypeNegative() {
        BRunUtil.invoke(result, "testNilValueCastAsStructuredTypeNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: '\\(string\\|int\\|\\(\\)\\)\\[2\\]' cannot be cast to 'string\\[2\\]'.*")
    public void testArrayCastNegative() {
        BRunUtil.invoke(result, "testArrayCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: '\\[string,int\\|string,float\\]' cannot be cast" +
                    " to '\\[string,int,float\\]'.*")
    public void testTupleCastNegative() {
        BRunUtil.invoke(result, "testTupleCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'lang.xml:Text' cannot be cast to 'json'.*")
    public void testJsonCastNegative() {
        BRunUtil.invoke(result, "testJsonCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map' cannot be cast to 'map<string>'.*")
    public void testMapCastNegative() {
        BRunUtil.invoke(result, "testMapCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'Employee' cannot be cast to 'Lead'.*")
    public void testRecordCastNegative() {
        BRunUtil.invoke(result, "testRecordCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'table<TableEmployee>' cannot be cast to " +
                    "'table<TableEmployeeTwo>'.*", enabled = false)
    public void testTableCastNegative() {
        BRunUtil.invoke(result, "testTableCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string' cannot be cast to 'xml\\" +
                    "<lang\\.xml:Element" + "\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction\\|" +
                    "lang\\.xml:Text\\>'.*")
    public void testXmlCastNegative() {
        BRunUtil.invoke(result, "testXmlCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'error' cannot be cast to 'MyErrorTwo'.*")
    public void testErrorCastNegative() {
        BRunUtil.invoke(result, "testErrorCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
        expectedExceptionsMessageRegExp = ".*'function \\(string,int\\) returns \\(string\\)' cannot be cast to " +
                "'function \\(string\\) returns \\(string\\)'.*")
    public void testFunctionCastNegative() {
        BRunUtil.invoke(result, "testFunctionCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: incompatible types: expected 'function \\(string\\) " +
                    "returns \\(string\\)', found 'function \\(string,int\\) returns \\(string\\)'.*", enabled = false)
    public void testFutureCastNegative() {
        BRunUtil.invoke(result, "testFutureCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'EmployeeObject' cannot be cast to 'LeadObject'.*")
    public void testObjectCastNegative() {
        BRunUtil.invoke(result, "testObjectCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'typedesc' cannot be cast to 'int'.*")
    public void testTypedescCastNegative() {
        BRunUtil.invoke(result, "testTypedescCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'map<string>' cannot be cast to 'map<int>'.*")
    public void testMapElementCastNegative() {
        BRunUtil.invoke(result, "testMapElementCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string' cannot be cast to 'int'.*")
    public void testListElementCastNegative() {
        BRunUtil.invoke(result, "testListElementCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: 'int' cannot be cast to 'string\\|boolean'.*")
    public void testDirectlyUnmatchedUnionToUnionCastNegativeOne() {
        BRunUtil.invoke(result, "testDirectlyUnmatchedUnionToUnionCastNegative_1");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: 'string' cannot be cast to 'Lead\\|int'.*")
    public void testDirectlyUnmatchedUnionToUnionCastNegativeTwo() {
        BRunUtil.invoke(result, "testDirectlyUnmatchedUnionToUnionCastNegative_2");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string' cannot be cast to 'int'.*")
    public void testStringAsInvalidBasicType() {
        BRunUtil.invoke(result, "testStringAsInvalidBasicType");
    }

    @Test
    public void testCastPanicWithCheckTrap() {
        BValue[] returns = BRunUtil.invoke(result, "testCastPanicWithCheckTrap");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "incompatible types: 'function (string,int) returns (string)' cannot be cast to " +
                                    "'function (string) returns (string)'");
    }

    @Test(dataProvider = "stringAsStringTests")
    public void testStringAsString(String functionName, String s) {
        BValue[] returns = BRunUtil.invoke(result, functionName, new BValue[]{new BString(s)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "expected strings to be the same");
    }

    @Test
    public void testBooleanAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue(), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue(), "invalid boolean representation as " +
                "boolean");
    }

    @Test
    public void testBooleanInUnionAsBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanInUnionAsBoolean", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue(), "invalid boolean representation as " +
                "boolean");
    }

    @Test
    public void testTypeCastOnRecordLiterals() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeCastOnRecordLiterals");
        Assert.assertEquals(returns[0].stringValue(), "Server mode configuration");
        Assert.assertEquals(returns[1].stringValue(), "Embedded mode configuration");
        Assert.assertEquals(returns[2].stringValue(), "In-memory mode configuration");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'int' cannot be cast to 'string'.*")
    public void testFiniteTypeToValueTypeCastNegative() {
        BRunUtil.invoke(result, "testFiniteTypeToValueTypeCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'int' cannot be cast to 'string\\|xml\\" +
            "<lang\\.xml:Element" + "\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction\\|" +
                    "lang\\.xml:Text\\>'.*")
    public void testFiniteTypeToRefTypeCastNegative() {
        BRunUtil.invoke(result, "testFiniteTypeToRefTypeCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'int' cannot be cast to 'FooBarOne'.*")
    public void testValueTypeToFiniteTypeCastNegative() {
        BRunUtil.invoke(result, "testValueTypeToFiniteTypeCastNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'string' cannot be cast to 'FooOneTrue'.*")
    public void testFiniteTypeToFiniteTypeCastNegative() {
        BRunUtil.invoke(result, "testFiniteTypeToFiniteTypeCastNegative");
    }

    @Test
    public void testCastNegatives() {
        Assert.assertEquals(resultNegative.getErrorCount(), 3);
        int errIndex = 0;
        validateError(resultNegative, errIndex++, "incompatible types: 'Def' cannot be cast to 'Abc'", 19, 15);
        validateError(resultNegative, errIndex++, "incompatible types: 'boolean' cannot be cast to '(int|foo)'",
                30, 16);
        validateError(resultNegative, errIndex, "incompatible types: '(int|foo)' cannot be cast to 'xml'", 35, 13);
    }

    @DataProvider
    public Object[][] positiveTests() {
        return new Object[][]{
                {"testNilCastPositive"},
                {"testStringCastPositive"},
                {"testIntCastPositive"},
                {"testFloatCastPositive"},
                {"testDecimalCastPositive"},
                {"testBooleanCastPositive"},
                {"testArrayCastPositive"},
                {"testTupleCastPositive"},
                {"testJsonCastPositive"},
                {"testMapCastPositive"},
                {"testRecordCastPositive"},
                {"testXmlCastPositive"},
                {"testErrorCastPositive"},
                {"testFunctionCastPositive"},
                {"testFutureCastPositive"},
                {"testObjectCastPositive"},
                {"testTypedescCastPositive"},
                {"testMapElementCastPositive"},
                {"testListElementCastPositive"},
                {"testOutOfOrderUnionConstraintCastPositive"},
                {"testCastToNumericType"},
                {"testBroaderObjectCast"},
                {"testCastOnPotentialConversion"},
                {"testSimpleTypeToUnionCastPositive"},
                {"testDirectlyUnmatchedUnionToUnionCastPositive"},
                {"testFiniteTypeToValueTypeCastPositive"},
                {"testFiniteTypeToRefTypeCastPositive"},
                {"testValueTypeToFiniteTypeCastPositive"},
                {"testFiniteTypeToFiniteTypeCastPositive"}
        };
    }

    @DataProvider
    public Object[][] stringAsStringTests() {
        String[] asStringTestFunctions = new String[]{"testStringAsString", "testStringInUnionAsString"};
        String[] stringValues = new String[]{"a", "", "Hello, from Ballerina!"};
        List<Object[]> result = new ArrayList<>();
        Arrays.stream(asStringTestFunctions)
                .forEach(func -> Arrays.stream(stringValues)
                        .forEach(arg -> result.add(new Object[]{func, arg})));
        return result.toArray(new Object[result.size()][]);
    }

    @Test
    public void testUntaintedWithoutType() {
        BValue[] returns = BRunUtil.invoke(result, "testContexuallyExpectedType");
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertEquals(((BMap) returns[0]).get("name").stringValue(), "Em Zee");
        Assert.assertEquals(((BMap) returns[0]).get("id").stringValue(), "1100");
    }

    @Test
    public void testUntaintedWithoutType2() {
        BValue[] returns = BRunUtil.invoke(result, "testContexuallyExpectedTypeRecContext");
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertEquals(((BMap) returns[0]).get("name").stringValue(), "Em Zee");
        Assert.assertEquals(((BMap) returns[0]).get("id").stringValue(), "1100");
    }

}
