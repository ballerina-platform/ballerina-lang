/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.arrays;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains methods to test the arrays implementation in Ballerina.
 *
 * @since 0.8.0
 */
public class BArrayValueTest {

    private static final double DELTA = 0.01;
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-value.bal");
    }

    @Test(description = "Test lazy arrays creation", expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*array index out of range: index: 0, size: 0.*")
    public void testLazyArrayCreation() {
        BRunUtil.invoke(compileResult, "lazyInitThrowArrayIndexOutOfBound", new Object[0]);
    }

    @Test(description = "Test lazy arrays initializer. Size should be zero")
    public void lazyInitSizeZero() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "lazyInitSizeZero", args);

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 0);
    }

    @Test(description = "Test add value operation on int arrays")
    public void addValueToIntegerArray() {
        Object returns = BRunUtil.invoke(compileResult, "addValueToIntArray");

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 200, "Invalid arrays size.");
        Assert.assertEquals(arrayValue.getInt(0), (-10), "Invalid value returned.");
        Assert.assertEquals(arrayValue.getInt(15), 20, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getInt(99), 2147483647, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getInt(100), -4, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getInt(115), -2147483647, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getInt(199), 6, "Invalid value returned.");
    }

    @Test(description = "Test add value operation on float arrays")
    public void addValueToFloatArray() {
        Object returns = BRunUtil.invoke(compileResult, "addValueToFloatArray");

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 200, "Invalid arrays size.");

        Assert.assertEquals(arrayValue.getFloat(0), -10.0, DELTA, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getFloat(15), 2.5, DELTA, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getFloat(99), 2147483647.1, DELTA,
                "Invalid value returned.");
        Assert.assertEquals(arrayValue.getFloat(100), 4.3, DELTA, "Invalid value returned.");
        Assert.assertEquals(arrayValue.getFloat(115), -2147483647.7, DELTA,
                "Invalid value returned.");
        Assert.assertEquals(arrayValue.getFloat(199), 6.9, DELTA, "Invalid value returned.");
    }

    @Test(description = "test default value of an element in an integer array")
    public void testDefaultValueOfIntArrayElement() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDefaultValueOfIntArrayElement", args);

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertTrue(returns.get(2) instanceof Long);

        Assert.assertEquals(returns.get(0), 0L);
        Assert.assertEquals(returns.get(1), 0L);
        Assert.assertEquals(returns.get(2), 45L);
    }

    @Test(description = "test default value of an element in an json array")
    public void testDefaultValueOfJsonArrayElement() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testDefaultValueOfJsonArrayElement", args);

        Assert.assertNull(returns.get(0));
        Assert.assertNull(returns.get(1));
        Assert.assertTrue(returns.get(2) instanceof BMap);
        Assert.assertEquals(returns.get(2).toString(), "{\"name\":\"supun\"}");
    }

    @Test(description = "test default value of an element in an json array")
    public void testArrayGrowth() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testArrayGrowth", args);

        long value = (long) returns;
        Assert.assertEquals(value, 2390725);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
