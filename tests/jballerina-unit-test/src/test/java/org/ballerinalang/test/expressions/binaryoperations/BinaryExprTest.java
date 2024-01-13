/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.binaryoperations;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of binary expressions.
 */
public class BinaryExprTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/binary-expr.bal");
    }

    @Test(description = "Test binary logical expression")
    public void testBinaryExpr() {
        // stone + value
        boolean stone = true;
        boolean value = true;
        Object[] args = { (stone), (value) };
        Object returns = BRunUtil.invoke(result, "makeChild", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean actual = (boolean) returns;
        boolean expected = true;
        Assert.assertEquals(actual, expected);

        // stone + !value
        stone = true;
        value = false;
        args = new Object[] { (stone), (value) };
        returns = BRunUtil.invoke(result, "makeChild", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        actual = (boolean) returns;
        expected = false;
        Assert.assertEquals(actual, expected);

        // !stone + value
        stone = false;
        value = true;
        args = new Object[] { (stone), (value) };
        returns = BRunUtil.invoke(result, "makeChild", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        actual = (boolean) returns;
        expected = false;
        Assert.assertEquals(actual, expected);

        // !stone + !value
        stone = false;
        value = false;
        args = new Object[] { (stone), (value) };
        returns = BRunUtil.invoke(result, "makeChild", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        actual = (boolean) returns;
        expected = false;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test multiple binary AND expressions")
    public void multiBinaryAndExpressionTest() {
        boolean one = true;
        boolean two = false;
        boolean three = true;

        boolean expectedResult = one & two & three;

        Object[] args = { (one), (two), (three) };
        Object returns = BRunUtil.invoke(result, "multiBinaryANDExpression", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean actualResult = (boolean) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multiple binary OR expressions")
    public void multiBinaryORExpressionTest() {
        boolean one = true;
        boolean two = false;
        boolean three = true;

        boolean expectedResult = one || two || three;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "multiBinaryORExpression", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean actualResult = (boolean) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multiple binary expressions")
    public void multiBinaryExpressionTest() {
        boolean one = true;
        boolean two = false;
        boolean three = true;

        boolean expectedResult = (!one || (two && three)) || (!three || (one && two));

        Object[] args = { (one), (two), (three) };
        Object returns = BRunUtil.invoke(result, "multiBinaryExpression", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean actualResult = (boolean) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test bitwise and")
    public void bitwiseAndTest() {
        long a = 5;
        long b = 6;
        int c = 7;
        int d = 8;
        Object[] args = {(a), (b), (c), (d)};
        Object arr = BRunUtil.invoke(result, "bitwiseAnd", args);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0), a & b);
        Assert.assertEquals(((Byte) returns.get(1)).longValue(), a & c);
        Assert.assertEquals(((Byte) returns.get(2)).intValue(), c & d);
        Assert.assertEquals(((Byte) returns.get(3)).longValue(), b & d);
    }

    @Test(description = "Test binary expression with query", dataProvider = "binaryExpressionWithQueryDataProvider")
    public void binaryExpressionWithQuery(String fnName) {
        BRunUtil.invoke(result, fnName, new Object[]{});
    }

    @DataProvider(name = "binaryExpressionWithQueryDataProvider")
    public Object[] binaryExpressionWithQueryData() {
        return new Object[] {
                "binaryAndWithQuery",
                "binaryOrWithQuery"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
