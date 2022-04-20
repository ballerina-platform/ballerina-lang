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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
        Object val = BRunUtil.invoke(result, "testIntVarargs_1");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1), 6L);
        Assert.assertEquals(returns.get(2), 0L);
    }

    @Test
    public void testIntVarargs_2() {
        Object returns = BRunUtil.invoke(result, "testIntVarargs_2");
        Assert.assertEquals(returns, 5L);
    }

    @Test
    public void testIntVarargs_3() {
        Object val = BRunUtil.invoke(result, "testIntVarargs_3");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0), 9L);
        Assert.assertEquals(returns.get(1), 6L);
    }

    @Test
    public void testIntVarargs_4() {
        Object returns = BRunUtil.invoke(result, "testIntVarargs_4");
        Assert.assertEquals(returns, 8L);
    }

    @Test
    public void testLongVarargs() {
        Object val = BRunUtil.invoke(result, "testLongVarargs");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1), 6L);
        Assert.assertEquals(returns.get(2), 0L);
    }

    @Test
    public void testGetSumOfIntArrays() {
        Object returns = BRunUtil.invoke(result, "testGetSumOfIntArrays");
        Assert.assertEquals(returns, 10L);
    }

    @Test
    public void testGetSumOfIntArraysWithAnnot() {
        Object returns = BRunUtil.invoke(result, "testGetSumOfIntArraysWithAnnot");
        Assert.assertEquals(returns, 10L);
    }

    @Test
    public void testJavaListVarargs() {
        Object returns = BRunUtil.invoke(result, "testJavaListVarargs");
        Assert.assertEquals(((HandleValue) returns).getValue().toString(),
                "[apples, arranges, grapes, pineapple, mangoes]");
    }

    @Test
    public void testPrimitiveVarargsWithGenerics() {
        Object returns = BRunUtil.invoke(result, "testPrimitiveVarargsWithGenerics");
        Assert.assertEquals(((HandleValue) returns).getValue().toString(), "[3, 6, 9]");
    }

    @Test
    public void testPasingValueTypeToJavaObject() {
        Object returns = BRunUtil.invoke(result, "testPasingValueTypeToJavaObject");
        Assert.assertEquals(returns, 4L);
    }

    @Test
    public void testJavaGenericReturnType() {
        Object val = BRunUtil.invoke(result, "testJavaGenericReturnType");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 8L);
        Assert.assertEquals(returns.get(1), 3.25);
        Assert.assertEquals(((HandleValue) returns.get(2)).getValue(), "apples");
    }

    @Test
    public void testRefTypeVarArg() {
        Object val = BRunUtil.invoke(result, "testRefTypeVarArg");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "[7, 2, 8]");
        Assert.assertEquals(returns.get(1).toString(), "[error(\"error one\"), error(\"error two\")]");
    }

    @Test
    public void testIntArrayTypeVararg() {
        Object returns = BRunUtil.invoke(result, "testIntArrayTypeVararg");
        Assert.assertEquals(returns.toString(), "[[7,2], [8]]");
    }

    @Test
    public void testRefArrayTypeVararg() {
        Object returns = BRunUtil.invoke(result, "testRefArrayTypeVararg");
        Assert.assertEquals(returns.toString(), "[[error(\"error one\")], [error(\"error two\")]]");
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
            sj.add(value.toString());
        }
        return sj.toString();
    }
}
