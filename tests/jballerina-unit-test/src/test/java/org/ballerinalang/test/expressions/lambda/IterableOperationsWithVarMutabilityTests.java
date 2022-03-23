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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.utils.BStringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
    private static BString[] values = BStringUtils.getBStringArray(new String[]{"Hello", "World..!", "I", "am",
            "Ballerina.!!!"});

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/lambda/iterable" +
                "/basic-iterable-with-variable-mutability.bal");
    }

    @Test
    public void testInt1() {
        List<Integer> values = Arrays.asList(-5, 2, 4, 5, 7, -8, -3, 2);
        Object returns = BRunUtil.invoke(compileResult, "testInt1");
        Assert.assertEquals(returns, (long) values.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    public void testFloat1() {
        List<Double> values = Arrays.asList(1.1, 2.2, -3.3, 4.4, 5.5);
        double intSum = values.stream().mapToDouble(Double::doubleValue).sum();
        Object returns = BRunUtil.invoke(compileResult, "testFloat1");
        Assert.assertEquals(returns, intSum);
    }

    @Test
    public void testBasicArray1() {
        BArray array = ValueCreator.createArrayValue(values);
        Object returns = BRunUtil.invoke(compileResult, "testBasicArray1", new Object[]{array});
        StringBuilder sb = new StringBuilder();
        Arrays.stream(values).forEach(s -> sb.append(s.getValue().toUpperCase(Locale.getDefault())).append(":").
                append(s.getValue().toLowerCase(Locale.getDefault())).append(" "));
        Assert.assertEquals(returns.toString(), sb.toString().trim());
    }

    @Test
    public void testBasicArray2() {
        BArray array = ValueCreator.createArrayValue(values);
        Object returns = BRunUtil.invoke(compileResult, "testBasicArray2", new Object[]{array});
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(values[i]).append(" ");
        }
        Assert.assertEquals(returns.toString(), sb.toString().trim());
    }

    @Test()
    public void testBasicMap1() {
        Object arr = BRunUtil.invoke(compileResult, "testBasicMap1");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "70");
        Assert.assertEquals(returns.get(1).toString(), "[\"a\",\"e\"]");
    }

    @Test()
    public void testBasicMap2() {
        Object arr = BRunUtil.invoke(compileResult, "testBasicMap2");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "91");
        Assert.assertEquals(returns.get(1).toString(), "start-a : A-b : B-c : C-d : D-e : E-end");
        Assert.assertEquals(returns.get(2).toString(), "{\"a\":\"aA\",\"e\":\"eE\"}");
    }

    @Test
    public void testXML() {
        Object arr = BRunUtil.invoke(compileResult, "xmlTest");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "start-1-2-end");
        Assert.assertEquals(returns.get(1).toString(), "<p:city " +
                "xmlns:p=\"foo\">NY</p:city><q:country xmlns:q=\"bar\">US</q:country>");
    }

    @Test
    public void testRecord() {
        Object arr = BRunUtil.invoke(compileResult, "recordTest");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "16");
        Assert.assertEquals(returns.get(1).toString(), "[\"aa\",\"bb\",\"cc\"]");
    }

    @Test
    public void testIgnoredValue() {
        Object returns = BRunUtil.invoke(compileResult, "testIgnoredValue");
        Assert.assertEquals(returns.toString(), "The start- hello abc :) bye :)  hello cde :) bye :)  " +
                "hello pqr :) bye :) -The end");
    }

    @Test
    public void testInExpression() {
        Object arr = BRunUtil.invoke(compileResult, "testInExpression");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "total count 4");
        Assert.assertEquals(returns.get(1).toString(), "10");
    }

    @Test
    public void testInStatement() {
        Object returns = BRunUtil.invoke(compileResult, "testInStatement");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "16");
    }

    @Test
    public void testWithComplexJson() {
        Object arr = BRunUtil.invoke(compileResult, "testWithComplexJson");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "{\"long_name\":\"1823\",\"short_name\":\"1823\"," +
                "\"types\":[\"street_number\"]}");
        Assert.assertEquals(returns.get(1).toString(), "{\"long_name\":\"Bonpland\",\"short_name\":\"Bonpland\"," +
                "\"types\":[\"street_number\"]}");
    }

    @Test
    public void testWithComplexXML() {
        Object arr = BRunUtil.invoke(compileResult, "testWithComplexXML");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "[0,\"Everyday Italian\"]");
        Assert.assertEquals(returns.get(1).toString(), "[1,\"Harry Potter\"]");
        Assert.assertEquals(returns.get(2).toString(), "[2,\"XQuery Kick Start\"]");
        Assert.assertEquals(returns.get(3).toString(), "[3,\"Learning XML\"]");
    }

    @Test
    public void testWithComplexRecords() {
        Object arr = BRunUtil.invoke(compileResult, "testWithComplexRecords");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "{\"asset\":\"BTC\",\"free\":\"12\",\"locked\":\"8\"}");
        Assert.assertEquals(returns.get(1).toString(), "{\"asset\":\"BTC\",\"free\":\"20\",\"locked\":\"3\"}");
    }

    @Test
    public void multipleIterableOps() {
        Object returns = BRunUtil.invoke(compileResult, "multipleIterableOps");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "[\"USD\",\"USD\",\"EUR\",\"GBP\",\"USD\",\"EUR\",\"GBP\",\"USD\"]");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
