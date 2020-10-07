/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
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
 * @since 0.995.0
 */
public class IterableOperationsWithVarMutabilityTests {

    private CompileResult compileResult;
    private static String[] values = new String[]{"Hello", "World..!", "I", "am", "Ballerina.!!!"};

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/lambda/iterable" +
                "/basic-iterable-with-variable-mutability.bal");
    }

    @Test
    public void testInt1() {
        List<Integer> values = Arrays.asList(-5, 2, 4, 5, 7, -8, -3, 2);
        BValue[] returns = BRunUtil.invoke(compileResult, "testInt1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), values.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    public void testFloat1() {
        List<Double> values = Arrays.asList(1.1, 2.2, -3.3, 4.4, 5.5);
        double intSum = values.stream().mapToDouble(Double::doubleValue).sum();
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloat1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), intSum);
    }

    @Test
    public void testBasicArray1() {
        BValueArray array = new BValueArray(values);
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicArray1", new BValue[]{array});
        Assert.assertEquals(returns.length, 1);
        StringBuilder sb = new StringBuilder();
        Arrays.stream(values).forEach(s -> sb.append(s.toUpperCase(Locale.getDefault())).append(":").
                append(s.toLowerCase(Locale.getDefault())).append(" "));
        Assert.assertEquals(returns[0].stringValue(), sb.toString().trim());
    }

    @Test
    public void testBasicArray2() {
        BValueArray array = new BValueArray(values);
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicArray2", new BValue[]{array});
        Assert.assertEquals(returns.length, 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(values[i]).append(" ");
        }
        Assert.assertEquals(returns[0].stringValue(), sb.toString().trim());
    }

    @Test()
    public void testBasicMap1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicMap1");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "70");
        Assert.assertEquals(returns[1].stringValue(), "[\"a\", \"e\"]");
    }

    @Test()
    public void testBasicMap2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicMap2");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "91");
        Assert.assertEquals(returns[1].stringValue(), "start-a : A-b : B-c : C-d : D-e : E-end");
        Assert.assertEquals(returns[2].stringValue(), "{\"a\":\"aA\", \"e\":\"eE\"}");
    }

    @Test
    public void testXML() {
        BValue[] returns = BRunUtil.invoke(compileResult, "xmlTest");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "start-1-2-end");
        Assert.assertEquals(returns[1].stringValue(), "<p:city " +
                "xmlns:p=\"foo\">NY</p:city><q:country xmlns:q=\"bar\">US</q:country>");
    }

    @Test
    public void testRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "recordTest");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "16");
        Assert.assertEquals(returns[1].stringValue(), "[\"aa\", \"bb\", \"cc\"]");
    }

    @Test
    public void testIgnoredValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIgnoredValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "The start- hello abc :) bye :)  hello cde :) bye :)  " +
                "hello pqr :) bye :) -The end");
    }

    @Test
    public void testInExpression() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInExpression");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "total count 4");
        Assert.assertEquals(returns[1].stringValue(), "10");
    }

    @Test
    public void testInStatement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInStatement");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "16");
    }

    @Test
    public void testWithComplexJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithComplexJson");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "{\"long_name\":\"1823\", \"short_name\":\"1823\", " +
                "\"types\":[\"street_number\"]}");
        Assert.assertEquals(returns[1].stringValue(), "{\"long_name\":\"Bonpland\", \"short_name\":\"Bonpland\", " +
                "\"types\":[\"street_number\"]}");
    }

    @Test
    public void testWithComplexXML() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithComplexXML");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "[0, \"Everyday Italian\"]");
        Assert.assertEquals(returns[1].stringValue(), "[1, \"Harry Potter\"]");
        Assert.assertEquals(returns[2].stringValue(), "[2, \"XQuery Kick Start\"]");
        Assert.assertEquals(returns[3].stringValue(), "[3, \"Learning XML\"]");
    }

    @Test
    public void testWithComplexRecords() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testWithComplexRecords");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "{asset:\"BTC\", free:\"12\", locked:\"8\"}");
        Assert.assertEquals(returns[1].stringValue(), "{asset:\"BTC\", free:\"20\", locked:\"3\"}");
    }

    @Test
    public void multipleIterableOps() {
        BValue[] returns = BRunUtil.invoke(compileResult, "multipleIterableOps");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[\"USD\", \"USD\", \"EUR\", \"GBP\", \"USD\", \"EUR\", " +
                "\"GBP\", \"USD\"]");
    }
}
