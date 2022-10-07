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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Class to test type cast expressions.
 *
 * @since 0.994.0
 */
public class TypeCastExpressionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/type_cast_expr.bal");
    }

    @Test(dataProvider = "positiveTests")
    public void testCastPositive(String functionName) {
        Object returns = BRunUtil.invoke(result, functionName, new Object[0]);
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns, "expected assertion to succeed and return the " +
                "original value");
    }

    @Test(dataProvider = "stringAsStringTests")
    public void testStringAsString(String functionName, String s) {
        Object returns = BRunUtil.invoke(result, functionName, new Object[]{StringUtils.fromString(s)});
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns, "expected strings to be the same");
    }

    @Test
    public void testBooleanAsBoolean() {
        Object arr = BRunUtil.invoke(result, "testBooleanAsBoolean", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns.get(0), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns.get(1), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns.get(2).getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns.get(2), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns.get(3).getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns.get(3), "invalid boolean representation as " +
                "boolean");
    }

    @Test
    public void testBooleanInUnionAsBoolean() {
        Object arr = BRunUtil.invoke(result, "testBooleanInUnionAsBoolean", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns.get(0), "invalid boolean representation as " +
                "boolean");
        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        Assert.assertFalse((Boolean) returns.get(1), "invalid boolean representation as " +
                "boolean");
    }

    @Test
    public void testTypeCastOnRecordLiterals() {
        Object arr = BRunUtil.invoke(result, "testTypeCastOnRecordLiterals");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0).toString(), "Server mode configuration");
        Assert.assertEquals(returns.get(1).toString(), "Embedded mode configuration");
        Assert.assertEquals(returns.get(2).toString(), "In-memory mode configuration");
    }

    @Test
    public void testCastNegatives() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/typecast/type_cast_expr_negative.bal");

        int errIndex = 0;
        validateError(resultNegative, errIndex++, "incompatible types: 'Def' cannot be cast to 'Abc'", 19, 15);
        validateError(resultNegative, errIndex++, "incompatible types: 'boolean' cannot be cast to 'FooInt'",
                30, 16);
        validateError(resultNegative, errIndex++, "incompatible types: 'FooInt' cannot be cast to 'xml'", 35, 13);
        validateError(resultNegative, errIndex++, "incompatible types: '(int|error)' cannot be cast to 'int'", 67, 13);
        validateError(resultNegative, errIndex++, "incompatible types: '(json|error)' cannot be cast to 'string'", 68
                , 13);
        validateError(resultNegative, errIndex++, "incompatible types: '(json|error)' cannot be cast to 'string'", 69,
                13);
        validateError(resultNegative, errIndex++, "incompatible types: '(string[]|int)' cannot be cast to 'byte[]'",
                78, 32);
        validateError(resultNegative, errIndex++, "incompatible mapping constructor expression for type '(record {| " +
                        "byte[] a; anydata...; |}|record {| string a; anydata...; |})'", 79, 47);
        validateError(resultNegative, errIndex++, "incompatible types: '(string[]|int)' cannot be cast to 'byte[]'",
                79, 51);
        validateError(resultNegative, errIndex++, "incompatible mapping constructor expression for type '(record {| " +
                "string[] a; anydata...; |}|record {| string a; anydata...; |})'", 82, 49);
        validateError(resultNegative, errIndex++, "incompatible types: expected 'Obj', found 'object { int i; }'", 96,
                      15);
        // https://github.com/ballerina-platform/ballerina-lang/issues/38104
        validateError(resultNegative, errIndex++, "incompatible types: expected 'Obj', found 'object { int i; }'", 96,
                      15);
        validateError(resultNegative, errIndex++, "incompatible types: expected 'int', found 'string'", 110, 52);
        validateError(resultNegative, errIndex++, "missing non-defaultable required record field 'empCount'", 119, 46);
        validateError(resultNegative, errIndex++, "missing required parameter 'j' in call to 'new()'", 123, 15);
        validateError(resultNegative, errIndex++, "missing error detail arg for error detail field 'code'", 129, 23);
        Assert.assertEquals(resultNegative.getErrorCount(), errIndex);
    }

    @DataProvider
    public Object[] positiveTests() {
        return new Object[]{
                "testNilCastPositive",
                "testStringCastPositive",
                "testIntCastPositive",
                "testFloatCastPositive",
                "testDecimalCastPositive",
                "testBooleanCastPositive",
                "testArrayCastPositive",
                "testTupleCastPositive",
                "testJsonCastPositive",
                "testMapCastPositive",
                "testRecordCastPositive",
                "testXmlCastPositive",
                "testErrorCastPositive",
                "testFunctionCastPositive",
                "testFutureCastPositive",
                "testObjectCastPositive",
                "testTypedescCastPositive",
                "testMapElementCastPositive",
                "testListElementCastPositive",
                "testOutOfOrderUnionConstraintCastPositive",
                "testCastToNumericType",
                "testBroaderObjectCast",
                "testCastOnPotentialConversion",
                "testSimpleTypeToUnionCastPositive",
                "testDirectlyUnmatchedUnionToUnionCastPositive",
                "testFiniteTypeToValueTypeCastPositive",
                "testFiniteTypeToRefTypeCastPositive",
                "testValueTypeToFiniteTypeCastPositive",
                "testFiniteTypeToFiniteTypeCastPositive",
                "testRecordCastWithSpecialChars",
                "testObjectCastWithSpecialChars",
                "testTableCastWithSpecialChars"
        };
    }

    @DataProvider
    public Object[] futureCastTests() {
        return new String[] {
                "testFutureWithoutFutureConstraintCastPositive",
                "testFutureCastNegative",
                "testFutureOfFutureValueCastNegative"
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
        Object returns = BRunUtil.invoke(result, "testContexuallyExpectedType");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("name")).toString(), "Em Zee");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("id")).toString(), "1100");
    }

    @Test
    public void testUntaintedWithoutType2() {
        Object returns = BRunUtil.invoke(result, "testContexuallyExpectedTypeRecContext");
        Assert.assertTrue(returns instanceof BMap);
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("name")).toString(), "Em Zee");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("id")).toString(), "1100");
    }

    @DataProvider
    public Object[] mappingToRecordTests() {
        return new Object[]{
                "testImmutableJsonMappingToExclusiveRecordPositive",
                "testImmutableJsonMappingToInclusiveRecordPositive"
        };
    }

    @Test(dataProvider = "mappingToRecordTests")
    public void testJsonMappingToRecordPositive(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @Test(dataProvider = "futureCastTests")
    public void testFutureCast(String function) {
        BRunUtil.invoke(result, function);
    }

    @Test(dataProvider = "negativeCastTests")
    public void testNegativeCast(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider
    public Object[] negativeCastTests() {
        return new String[]{
                "testNilValueCastAsSimpleBasicTypeNegative", "testNilValueCastAsStructuredTypeNegative",
                "testArrayCastNegative", "testTupleCastNegative", "testJsonCastNegative", "testMapCastNegative",
                "testRecordCastNegative", "testTableCastNegative", "testXmlCastNegative", "testErrorCastNegative",
                "testFunctionCastNegative", "testObjectCastNegative", "testTypedescCastNegative",
                "testMapElementCastNegative", "testListElementCastNegative",
                "testDirectlyUnmatchedUnionToUnionCastNegative_1", "testDirectlyUnmatchedUnionToUnionCastNegative_2",
                "testMutableJsonMappingToExclusiveRecordNegative", "testTypeCastInConstructorMemberWithUnionCET",
                "testStringAsInvalidBasicType", "testCastPanicWithCheckTrap", "testFiniteTypeToValueTypeCastNegative",
                "testFiniteTypeToRefTypeCastNegative", "testValueTypeToFiniteTypeCastNegative",
                "testFiniteTypeToFiniteTypeCastNegative", "testCastOfFiniteTypeWithIntersectingBuiltInSubType",
                "testFiniteTypeArrayNegative"
        };
    }

    @Test(dataProvider = "typeCastWithConstructorTests")
    public void testTypeCastWithConstructors(String testFuncName) {
        BRunUtil.invoke(result, testFuncName);
    }

    @DataProvider
    public Object[] typeCastWithConstructorTests() {
        return new Object[]{
                "testTypeCastWithObjectConstructorExpr",
                "testTypeCastWithRawTemplateExpr",
                "testTypeCastWithTableConstructorExpr",
                "testTypeCastWithNewExpr",
                "testTypeCastWithErrorConstructorExpr"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
