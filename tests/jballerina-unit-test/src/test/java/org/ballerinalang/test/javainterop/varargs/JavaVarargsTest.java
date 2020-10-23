/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.javainterop.varargs;

import io.ballerina.runtime.values.ArrayValue;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Test cases for java interop with functions with varargs.
 *
 * @since 1.0.0
 */
public class JavaVarargsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/varargs/java_varargs_tests.bal");
    }

    @Test
    public void testIntVarargs_1() {
        BValue[] returns = BRunUtil.invoke(result, "testIntVarargs_1");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 6);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
    }

    @Test
    public void testIntVarargs_2() {
        BValue[] returns = BRunUtil.invoke(result, "testIntVarargs_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testIntVarargs_3() {
        BValue[] returns = BRunUtil.invoke(result, "testIntVarargs_3");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 9);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 6);
    }

    @Test
    public void testIntVarargs_4() {
        BValue[] returns = BRunUtil.invoke(result, "testIntVarargs_4");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
    }

    @Test
    public void testLongVarargs() {
        BValue[] returns = BRunUtil.invoke(result, "testLongVarargs");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 6);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
    }

    @Test
    public void testGetSumOfIntArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testGetSumOfIntArrays");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testGetSumOfIntArraysWithAnnot() {
        BValue[] returns = BRunUtil.invoke(result, "testGetSumOfIntArraysWithAnnot");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testJavaListVarargs() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaListVarargs");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue().toString(),
                "[apples, arranges, grapes, pineapple, mangoes]");
    }

    @Test
    public void testPrimitiveVarargsWithGenerics() {
        BValue[] returns = BRunUtil.invoke(result, "testPrimitiveVarargsWithGenerics");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue().toString(), "[3, 6, 9]");
    }

    @Test
    public void testPasingValueTypeToJavaObject() {
        BValue[] returns = BRunUtil.invoke(result, "testPasingValueTypeToJavaObject");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testJavaGenericReturnType() {
        BValue[] returns = BRunUtil.invoke(result, "testJavaGenericReturnType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 3.25);
        Assert.assertEquals(((BHandleValue) returns[2]).getValue(), "apples");
    }

    @Test (enabled = false)
    public void testRefTypeVarArg() {
        BValue[] returns = BRunUtil.invoke(result, "testRefTypeVarArg");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[7, 2, 8]");
        Assert.assertEquals(returns[1].stringValue(), "[error error one, error error two]");
    }

    @Test (enabled = false)
    public void testIntArrayTypeVararg() {
        BValue[] returns = BRunUtil.invoke(result, "testIntArrayTypeVararg");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[[7 2], [8]]");
    }

    @Test (enabled = false)
    public void testRefArrayTypeVararg() {
        BValue[] returns = BRunUtil.invoke(result, "testRefArrayTypeVararg");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[[error error one], [error error two]]");
    }

    // Java methods for interop

    public static int getSum(int... vals) {
        return Arrays.stream(vals).reduce(0, Integer::sum);
    }

    public static int getMax(int a, int b, int... vals) {
        return Integer.max(Integer.max(a, b), Arrays.stream(vals).reduce(0, Integer::max));
    }

    public static long getLongSum(long... vals) {
        return Arrays.stream(vals).reduce(0, Long::sum);
    }

    public static int[] getIntArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return array;
    }

    public static int getSumOfIntArrays(int[]... vals) {
        int sum = 0;
        for (int[] val : vals) {
            sum += Arrays.stream(val).reduce(0, Integer::sum);
        }
        return sum;
    }

    public static List<String> getList(String... vals) {
        return Arrays.asList(vals);
    }

    public static List<String> merge(List<String>... lists) {
        List<String> mergedList = new ArrayList<String>();
        for (List<String> list : lists) {
            mergedList.addAll(list);
        }
        return mergedList;
    }

    public static int toShort(Object vals) {
        return ((Number) vals).shortValue();
    }

    public static <T> T getGenericValue(T value) {
        return value;
    }

    public static String getRefVararg(Object... values) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (Object value : values) {
            sj.add(value.toString());
        }
        return sj.toString();
    }

    public static String getArrayTypeVararg(ArrayValue... values) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (ArrayValue value : values) {
            sj.add("[" + value.toString() + "]");
        }
        return sj.toString();
    }
}
