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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for elvis expressions.
 *
 * @since 0.964.1
 */
public class ElvisExpressionTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/elvis/elvis-expr.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/elvis/elvis-expr-negative.bal");
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

    @Test
    public void testElvisRefTypeNested() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypeNested");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 4000);
    }

    @Test
    public void testElvisRefTypeCaseTwo() {
        BValue[] results = BRunUtil.invoke(compileResult, "testElvisRefTypeCaseTwo");
        Assert.assertEquals(results.length, 1);
        Assert.assertTrue(results[0] instanceof BString);
        Assert.assertEquals(results[0].stringValue(), "kevin");
    }

    @Test
    public void testElvisOperatorNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'int', found 'int|null'", 5, 14);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'int', found 'string'", 12, 14);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'int', found 'string'", 19, 9);
    }

}
