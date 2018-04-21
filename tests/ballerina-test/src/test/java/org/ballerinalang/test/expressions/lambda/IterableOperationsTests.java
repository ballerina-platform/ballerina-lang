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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
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
    private static String[] values = new String[] {"Hello", "World..!", "I", "am", "Ballerina.!!!"};

    @BeforeClass
    public void setup() {
        basic = BCompileUtil.compile("test-src/expressions/lambda/iterable/basic-iterable.bal");
        negative = BCompileUtil.compile("test-src/expressions/lambda/iterable/iterable-negative.bal");
    }

    @Test
    public void testNegative() {
        Assert.assertEquals(negative.getErrorCount(), 20);
        BAssertUtil.validateError(negative, 0, "undefined function 'int.foreach'", 6, 5);
        BAssertUtil.validateError(negative, 1, "undefined function 'string.map'", 8, 5);
        BAssertUtil.validateError(negative, 2, "variable assignment is required", 14, 5);
        BAssertUtil.validateError(negative, 3, "iterable lambda function required a single param or a tuple param",
                16, 14);
        BAssertUtil.validateError(negative, 4, "invalid function 'keys' invocation on type '(string,string) " +
                "collection'", 23, 21);
        BAssertUtil.validateError(negative, 5, "incompatible types: expected 'string[]', found '(string,string) " +
                "collection'", 31, 24);
        BAssertUtil.validateError(negative, 6, "incompatible types: expected 'map', found '(any) collection'", 35, 22);
        BAssertUtil.validateError(negative, 7, "cannot assign return value of 'filter' operation here, use a reduce " +
                "operation", 38, 22);
        BAssertUtil.validateError(negative, 8, "incompatible types: expected 'int', found '()'", 46, 19);

        BAssertUtil.validateError(negative, 9, "incompatible types: expected tuple, found '(int,string) " +
                "collection'", 48, 5);
        BAssertUtil.validateError(negative, 10, "no argument required for operation 'count'", 55, 17);
        BAssertUtil.validateError(negative, 11, "single lambda function required here", 56, 5);
        BAssertUtil.validateError(negative, 12, "single lambda function required here", 58, 15);
        BAssertUtil.validateError(negative, 13, "too many variables are defined for iterable type 'string[]'", 63, 15);
        BAssertUtil.validateError(negative, 14, "iterable lambda function required a single param or a tuple param",
                64, 15);
        BAssertUtil.validateError(negative, 15, "too many return arguments are defined for operation 'filter'", 65, 14);
        BAssertUtil.validateError(negative, 16, "not enough return arguments are defined for operation 'filter'", 66,
                14);
        BAssertUtil.validateError(negative, 17, "unknown type 'person'", 67, 15);
        BAssertUtil.validateError(negative, 18, "not enough return arguments are defined for operation 'filter'", 67,
                14);
        BAssertUtil.validateError(negative, 19, "unknown type 'person'", 68, 33);
    }

    @Test
    public void testInt1() {
        List<Integer> values = Arrays.asList(-5, 2, 4, 5, 7, -8, -3, 2);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testInt1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), sum);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), values.size());
        Assert.assertEquals(((BInteger) returns[2]).intValue(),
                values.stream().mapToInt(Integer::intValue).max().getAsInt());
        Assert.assertEquals(((BInteger) returns[3]).intValue(),
                values.stream().mapToInt(Integer::intValue).min().getAsInt());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), sum);
        Assert.assertEquals(((BFloat) returns[5]).floatValue(),
                values.stream().mapToInt(Integer::intValue).average().getAsDouble());
    }

    @Test
    public void testInt2() {
        List<Integer> values = Arrays.asList(2, 4, 5, 7, 2);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testInt2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), values.size());
        Assert.assertEquals(((BInteger) returns[1]).intValue(),
                values.stream().mapToInt(Integer::intValue).max().getAsInt());
        Assert.assertEquals(((BInteger) returns[2]).intValue(),
                values.stream().mapToInt(Integer::intValue).min().getAsInt());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), sum);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(),
                values.stream().mapToInt(Integer::intValue).average().getAsDouble());
    }

    @Test
    public void testFloat1() {
        List<Double> values = Arrays.asList(1.1, 2.2, -3.3, 4.4, 5.5);
        double intSum = values.stream().mapToDouble(Double::doubleValue).sum();
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testFloat1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), intSum);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), values.size());
        Assert.assertEquals(((BFloat) returns[2]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), sum);
        Assert.assertEquals(((BFloat) returns[5]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).average().getAsDouble());
    }

    @Test
    public void testFloat2() {
        List<Double> values = Arrays.asList(1.1, 2.2, 4.4, 5.5);
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testFloat2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), values.size());
        Assert.assertEquals(((BFloat) returns[1]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
        Assert.assertEquals(((BFloat) returns[2]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), sum);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(),
                values.stream().mapToDouble(Double::doubleValue).average().getAsDouble());
    }

    @Test
    public void testBasicArray1() {
        BStringArray sarray = new BStringArray(values);
        BValue[] returns = BRunUtil.invoke(basic, "testBasicArray1", new BValue[] {sarray});
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
        BStringArray sarray = new BStringArray(values);
        BValue[] returns = BRunUtil.invoke(basic, "testBasicArray2", new BValue[] {sarray});
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
        Assert.assertEquals(returns[1].stringValue(), "[\"a\", \"e\"]");
    }

    @Test
    public void testBasicMap2() {
        BValue[] returns = BRunUtil.invoke(basic, "testBasicMap2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[\"aA\", \"eE\"]");
    }

    @Test(enabled = false)
    public void testJSON() {
        BValue[] returns = BRunUtil.invoke(basic, "jsonTest");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "bob10true[{\"subject\":\"maths\",\"marks\":75}," +
                "{\"subject\":\"English\",\"marks\":85}]");
        Assert.assertEquals(returns[1].stringValue(), "[\"bob\"]");
        Assert.assertEquals(returns[2].stringValue(), "4");
        Assert.assertEquals(returns[3].stringValue(), "4");
        Assert.assertEquals(returns[4].stringValue(), "[\"0->{\"subject\":\"maths\",\"marks\":75}\", " +
                "\"1->{\"subject\":\"English\",\"marks\":85}\"]");
    }

    @Test
    public void testXML() {
        BValue[] returns = BRunUtil.invoke(basic, "xmlTest");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "7");
        Assert.assertEquals(returns[1].stringValue(), "3");
        Assert.assertEquals(returns[2].stringValue(), "{\"0\":<p:city xmlns:p=\"foo\" xmlns:q=\"bar\">NY</p:city>, " +
                "\"1\":<q:country xmlns:q=\"bar\" xmlns:p=\"foo\">US</q:country>}");
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
}
