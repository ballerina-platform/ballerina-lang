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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
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

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * TestCases for Iterable Operations.
 *
 * @since 0.961.0
 */
public class IterableOperationsTests {

    private CompileResult basic, negative;
    private static BString[] values = BStringUtils.getBStringArray(new String[]{"Hello", "World..!", "I", "am",
            "Ballerina.!!!"});

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
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) returns (boolean)', " +
                "found 'function (int,string) returns (boolean)'", 16, 14);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'map<string>', found " +
                "'map<[string,string]>'", 31, 21);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'any'", 35, 27);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'any'", 38, 22);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'int', found '()'", 46, 9);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected '[other,other]', found 'string[]'",
                48, 18);
        BAssertUtil.validateError(negative, index++,
                                  "invalid list binding pattern: attempted to infer a list type, but found 'other'",
                                  48, 18);
        BAssertUtil.validateError(negative, index++, "invalid operation: type 'string' does not support field access",
                49, 35);
        BAssertUtil.validateError(negative, index++, "too many arguments in call to 'length()'", 55, 9);
        BAssertUtil.validateError(negative, index++, "missing required parameter 'func' in call to 'filter()'", 56, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) returns ()', found 'int'", 58, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected '[string,string,string]', found " +
                "'string'", 63, 15);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) returns ()', found 'function () returns ()'", 64, 15);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 65, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) returns (boolean)', " +
                "found 'function (string) returns ([boolean,int])'", 65, 14);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 66, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) returns " +
                "(boolean)', found 'function (string) returns ()'", 66, 14);
        BAssertUtil.validateError(negative, index++, "variable assignment is required", 67, 5);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) returns (boolean)', " +
                "found 'function (other) returns ()'", 67, 14);
        BAssertUtil.validateError(negative, index++, "unknown type 'person'", 67, 24);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function (ballerina/lang.array:0.0.0:Type) " +
                "returns (boolean)', found 'function (string) returns (other)'", 68, 18);
        BAssertUtil.validateError(negative, index++, "unknown type 'person'", 68, 48);
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
        Object arr = BRunUtil.invoke(basic, "testInt1");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), (long) sum);
        Assert.assertEquals(returns.get(1), (long) values.size());
        Assert.assertEquals(returns.get(2),
                (long) values.stream().mapToInt(Integer::intValue).max().getAsInt());
        Assert.assertEquals(returns.get(3),
                (long) values.stream().mapToInt(Integer::intValue).min().getAsInt());
        Assert.assertEquals(returns.get(4), (long) sum);
    }

    @Test
    public void testInt2() {
        List<Integer> values = Arrays.asList(2, 4, 5, 7, 2);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        Object arr = BRunUtil.invoke(basic, "testInt2");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0), (long) values.size());
        Assert.assertEquals(returns.get(1),
                (long) values.stream().mapToInt(Integer::intValue).max().getAsInt());
        Assert.assertEquals(returns.get(2),
                (long) values.stream().mapToInt(Integer::intValue).min().getAsInt());
        Assert.assertEquals(returns.get(3), (long) sum);
    }

    @Test
    public void testFloat1() {
        List<Double> values = Arrays.asList(1.1, 2.2, -3.3, 4.4, 5.5);
        double intSum = values.stream().mapToDouble(Double::doubleValue).sum();
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        Object arr = BRunUtil.invoke(basic, "testFloat1");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0), intSum);
        Assert.assertEquals(returns.get(1), (long) values.size());
        Assert.assertEquals(returns.get(2),
                values.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
        Assert.assertEquals(returns.get(3),
                values.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
        Assert.assertEquals(returns.get(4), sum);
    }

    @Test
    public void testFloat2() {
        List<Double> values = Arrays.asList(1.1, 2.2, 4.4, 5.5);
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        Object arr = BRunUtil.invoke(basic, "testFloat2");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 4L);
        Assert.assertEquals(returns.get(0), (long) values.size());
        Assert.assertEquals(returns.get(1),
                values.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
        Assert.assertEquals(returns.get(2),
                values.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
        Assert.assertEquals(returns.get(3), sum);
    }

    @Test
    public void testBasicArray1() {
        BArray sarray = ValueCreator.createArrayValue(values);
        Object returns = BRunUtil.invoke(basic, "testBasicArray1", new Object[]{sarray});
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        StringBuilder sb = new StringBuilder();
        Arrays.stream(values).forEach(s -> sb.append(s.getValue().toUpperCase(Locale.getDefault())).append(":").
                append(s.getValue().toLowerCase(Locale.getDefault())).append(" "));
        Assert.assertEquals(returns.toString(), sb.toString().trim());
    }

    @Test
    public void testBasicArray2() {
        BArray sarray = ValueCreator.createArrayValue(values);
        Object returns = BRunUtil.invoke(basic, "testBasicArray2", new Object[]{sarray});
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(i).append(values[i]).append(" ");
        }
        Assert.assertEquals(returns.toString(), sb.toString().trim());
    }

    @Test
    public void testBasicMap1() {
        Object arr = BRunUtil.invoke(basic, "testBasicMap1");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "5");
        Assert.assertEquals(returns.get(1).toString(), "{\"a\":\"a\",\"e\":\"e\"}");
    }

    @Test
    public void testBasicMap2() {
        Object returns = BRunUtil.invoke(basic, "testBasicMap2");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "[\"aA\",\"eE\"]");
    }

    @Test
    public void testXML() {
        Object arr = BRunUtil.invoke(basic, "xmlTest");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "7");
        Assert.assertEquals(returns.get(1).toString(), "3");
        Assert.assertEquals(returns.get(2).toString(),
                "<p:city xmlns:p=\"foo\">NY</p:city><q:country xmlns:q=\"bar\">US</q:country>");
    }

    @Test
    public void testStruct() {
        Object arr = BRunUtil.invoke(basic, "structTest");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "2");
        Assert.assertEquals(returns.get(1).toString(), "[\"bob\",\"tom\",\"sam\"]");
    }

    @Test
    public void testIgnoredValue() {
        Object returns = BRunUtil.invoke(basic, "testIgnoredValue");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "abc pqr");
    }

    @Test
    public void testInExpression() {
        Object arr = BRunUtil.invoke(basic, "testInExpression");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "total count 2");
        Assert.assertEquals(returns.get(1).toString(), "7");
    }

    @Test
    public void testInFunctionInvocation() {
        Object returns = BRunUtil.invoke(basic, "testInFunctionInvocation");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "4");
    }

    @Test
    public void testInStatement() {
        Object returns = BRunUtil.invoke(basic, "testInStatement");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "10");
    }

    @Test
    public void testIterableOutputPrint() {
        Object arr = BRunUtil.invoke(basic, "testIterableOutputPrint");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).getClass(), Long.class);
        Assert.assertEquals(returns.get(1).getClass(), Long.class);
        Assert.assertEquals(returns.get(2).getClass(), Long.class);
        Assert.assertEquals(returns.get(3).getClass(), Long.class);
        long a1 = (long) returns.get(0);
        long a3 = (long) returns.get(1);
        long a4 = (long) returns.get(2);
        long a5 = (long) returns.get(3);
        Assert.assertEquals(a1, 3);
        Assert.assertEquals(a3, -8);
        Assert.assertEquals(a4, 7);
        Assert.assertEquals(a5, 4);
    }

    @Test
    public void testIterableReturnLambda() {
        Object arr = BRunUtil.invoke(basic, "testIterableReturnLambda");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns.get(0) instanceof BFunctionPointer);
        Assert.assertTrue(returns.get(1) instanceof BFunctionPointer);
        Assert.assertTrue(returns.get(2) instanceof BFunctionPointer);
        Assert.assertEquals(getType(returns.get(0)).toString(), "isolated function (int) returns (boolean)");
        Assert.assertEquals(getType(returns.get(1)).toString(), "isolated function (int) returns (boolean)");
        Assert.assertEquals(getType(returns.get(2)).toString(), "isolated function (int) returns (boolean)");
    }

    @AfterClass
    public void tearDown() {
        basic = null;
        negative = null;
    }
}
