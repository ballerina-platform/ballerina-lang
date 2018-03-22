/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.ballerinalang.test.expressions.elvis;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for elvis expressions.
 *
 * @since @since 0.970.0
 */
public class ElvisExpressionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/elvis/elvis-expr.bal");
    }

    @Test
    public void testElvisValueTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisValueTypePositive");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 120);
    }

    @Test
    public void testElvisValueTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisValueTypeNegative");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 111);
    }

    @Test
    public void testElvisValueTypeNested() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisValueTypeNested");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 3000);
    }

    @Test
    public void testElvisRefTypePositive() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypePositive");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 2300);
    }

    @Test
    public void testElvisRefTypeNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypeNegative");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 111);
    }

}
