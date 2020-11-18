/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.expressions.lambda;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BFunctionPointer;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * TestCases for Iterable Operations.
 *
 * @since 0.961.0
 */
public class IterableOperationsTests {

    private CompileResult basic, negative;
    private static String[] values = new String[]{"Hello", "World..!", "I", "am", "Ballerina.!!!"};

    @BeforeClass
    public void setup() {
        basic = BCompileUtil.compile("test-src/expressions/lambda/iterable/basic-iterable.bal");
        negative = BCompileUtil.compile("test-src/expressions/lambda/iterable/iterable-negative.bal");
    }

    @Test()
    public void testNegative() {
        Assert.assertEquals(negative.getErrorCount(), 33);
        int index = 0;
        BAssertUtil.validateError(negative, index++, "undefined function 'forEach' in type 'int'", 6, 7);
        BAssertUtil.validateError(negative, index++, "undefined function 'map' in type 'string'", 8, 7);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 14, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error)) returns" +
                " (boolean)', found 'function (int,string) returns (boolean)'", 16, 14);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'map<string>', found " +
                "'map<[string,string]>'", 31, 21);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'any'", 35, 27);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'any'", 38, 22);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'int', found '()'", 46, 9);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected '[other,other]', found 'string[]'",
                48, 18);
        BAssertUtil.validateError(negative, index++,
                                  "invalid tuple binding pattern: attempted to infer a tuple type, but found 'other'",
                                  48, 18);
        BAssertUtil.validateError(negative, index++, "invalid operation: type 'string' does not support field access",
                49, 35);
        BAssertUtil.validateError(negative, index++, "too many arguments in call to 'length()'", 55, 9);
        BAssertUtil.validateError(negative, index++, "missing required parameter 'func' in call to 'filter'()", 56, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error)) " +
                "returns ()', found 'int'", 58, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected '[string,string,string]', found " +
                "'string'", 63, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error))" +
                " returns ()', found 'function () returns ()'", 64, 15);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 65, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error)) returns " +
                "(boolean)', found 'function (string) returns ([boolean,int])'", 65, 14);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 66, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error)) returns " +
                "(boolean)', found 'function (string) returns ()'", 66, 14);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 67, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error)) returns " +
                "(boolean)', found 'function (other) returns ()'", 67, 14);
        BAssertUtil.validateError(negative, index++, "unknown type 'person'", 67, 24);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'function ((any|error)) " +
                "returns (boolean)', found 'function (string) returns (other)'", 68, 18);
        BAssertUtil.validateError(negative, index++, "unknown type 'person'", 68, 47);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'int[]', found 'any[]'", 73, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'int[]', found 'string[]'", 80, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'int[]', found 'string[]'", 89, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'any'", 99, 27);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'map'", 103, 16);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'boolean', found 'int'", 111, 20);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'float', found 'int'", 120, 39);
        BAssertUtil.validateError(negative, index, "incompatible types: expected 'float', found 'int'", 137, 42);
    }

    @Test
    public void testInt1() {
        List<Integer> values = Arrays.asList(-5, 2, 4, 5, 7, -8, -3, 2);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testInt1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), sum);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), values.size());
        Assert.assertEquals(((BInteger) returns[2]).intValue(),
                values.stream().mapToInt(Integer::intValue).max().getAsInt());
        Assert.assertEquals(((BInteger) returns[3]).intValue(),
                values.stream().mapToInt(Integer::intValue).min().getAsInt());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), sum);
    }

    @Test
    public void testInt2() {
        List<Integer> values = Arrays.asList(2, 4, 5, 7, 2);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testInt2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), values.size());
        Assert.assertEquals(((BInteger) returns[1]).intValue(),
                values.stream().mapToInt(Integer::intValue).max().getAsInt());
        Assert.assertEquals(((BInteger) returns[2]).intValue(),
                values.stream().mapToInt(Integer::intValue).min().getAsInt());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), sum);
    }

    @Test
    public void testFloat1() {
        List<Double> values = Arrays.asList(1.1, 2.2, -3.3, 4.4, 5.5);
        double intSum = values.stream().mapToDouble(Double::doubleValue).sum();
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testFloat1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), intSum);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), values.size());
        Assert.assertEquals(((BFloat) returns[2]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), sum);
    }

    @Test
    public void testFloat2() {
        List<Double> values = Arrays.asList(1.1, 2.2, 4.4, 5.5);
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testFloat2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), values.size());
        Assert.assertEquals(((BFloat) returns[1]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
        Assert.assertEquals(((BFloat) returns[2]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), sum);
    }

    @Test
    public void testBasicArray1() {
        BValueArray sarray = new BValueArray(values);
        BValue[] returns = BRunUtil.invoke(basic, "testBasicArray1", new BValue[]{sarray});
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        StringBuilder sb = new StringBuilder();
        Arrays.stream(values).forEach(s -> sb.append(s.toUpperCase(Locale.getDefault())).append(":").
                append(s.toLowerCase(Locale.getDefault())).append(" "));
        Assert.assertEquals(returns[0].stringValue(), sb.toString().trim());
    }

    @Test
    public void testBasicArray2() {
        BValueArray sarray = new BValueArray(values);
        BValue[] returns = BRunUtil.invoke(basic, "testBasicArray2", new BValue[]{sarray});
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(values[i]).append(" ");
        }
        Assert.assertEquals(returns[0].stringValue(), sb.toString().trim());
    }

    @Test
    public void testBasicMap1() {
        BValue[] returns = BRunUtil.invoke(basic, "testBasicMap1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "5");
        Assert.assertEquals(returns[1].stringValue(), "{\"a\":\"a\", \"e\":\"e\"}");
    }

    @Test
    public void testBasicMap2() {
        BValue[] returns = BRunUtil.invoke(basic, "testBasicMap2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[\"aA\", \"eE\"]");
    }

    @Test
    public void testXML() {
        BValue[] returns = BRunUtil.invoke(basic, "xmlTest");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "7");
        Assert.assertEquals(returns[1].stringValue(), "3");
        Assert.assertEquals(returns[2].stringValue(),
                "<p:city xmlns:p=\"foo\">NY</p:city><q:country xmlns:q=\"bar\">US</q:country>");
    }

    @Test
    public void testStruct() {
        BValue[] returns = BRunUtil.invoke(basic, "structTest");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "2");
        Assert.assertEquals(returns[1].stringValue(), "[\"bob\", \"tom\", \"sam\"]");
    }

    @Test
    public void testIgnoredValue() {
        BValue[] returns = BRunUtil.invoke(basic, "testIgnoredValue");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "abc pqr");
    }

    @Test
    public void testInExpression() {
        BValue[] returns = BRunUtil.invoke(basic, "testInExpression");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "total count 2");
        Assert.assertEquals(returns[1].stringValue(), "7");
    }

    @Test
    public void testInFunctionInvocation() {
        BValue[] returns = BRunUtil.invoke(basic, "testInFunctionInvocation");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "4");
    }

    @Test
    public void testInStatement() {
        BValue[] returns = BRunUtil.invoke(basic, "testInStatement");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "10");
    }

    @Test
    public void testIterableOutputPrint() {
        BValue[] returns = BRunUtil.invoke(basic, "testIterableOutputPrint");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(returns[1].getClass(), BInteger.class);
        Assert.assertEquals(returns[2].getClass(), BInteger.class);
        Assert.assertEquals(returns[3].getClass(), BInteger.class);
        BInteger a1 = (BInteger) returns[0];
        BInteger a3 = (BInteger) returns[1];
        BInteger a4 = (BInteger) returns[2];
        BInteger a5 = (BInteger) returns[3];
        Assert.assertEquals(a1.intValue(), 3);
        Assert.assertEquals(a3.intValue(), -8);
        Assert.assertEquals(a4.intValue(), 7);
        Assert.assertEquals(a5.intValue(), 4);
    }

    @Test
    public void testIterableReturnLambda() {
        BValue[] returns = BRunUtil.invoke(basic, "testIterableReturnLambda");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BFunctionPointer);
        Assert.assertTrue(returns[1] instanceof BFunctionPointer);
        Assert.assertTrue(returns[2] instanceof BFunctionPointer);
        Assert.assertEquals(returns[0].getType().toString(), "function (int) returns (boolean)");
        Assert.assertEquals(returns[1].getType().toString(), "function (int) returns (boolean)");
        Assert.assertEquals(returns[2].getType().toString(), "function (int) returns (boolean)");
    }
}
