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
        BAssertUtil.validateError(negative, 0, "undefined function 'int.foreach'", 6, 5);
        BAssertUtil.validateError(negative, 1, "undefined function 'string.map'", 8, 5);
        BAssertUtil.validateError(negative, 2, "not enough variables are defined for iterable type '(int,string)[]', " +
                "require '2' variables", 15, 15);
        BAssertUtil.validateError(negative, 3, "undefined function 'count'", 13, 5);
        BAssertUtil.validateError(negative, 4, "function invocation on type '(string,string)[]' is not supported",
                20, 21);
        BAssertUtil.validateError(negative, 5, "incompatible types: expected 'string[]', found '(string,string)[]'",
                28, 24);
        BAssertUtil.validateError(negative, 6, "incompatible types: expected 'map', found '(any)[]'", 32, 22);
        BAssertUtil.validateError(negative, 7, "cannot assign return value of 'filter' operation here, use reduce " +
                "operation", 36, 22);
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
        int intSum = values.stream().mapToInt(Double::intValue).sum();
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        BValue[] returns = BRunUtil.invoke(basic, "testFloat1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), intSum);
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
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[\"A\", \"E\"]");
    }

    @Test
    public void testBasicMap2() {
        BValue[] returns = BRunUtil.invoke(basic, "testBasicMap2");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[\"aA\", \"eE\"]");
    }
}
