/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.taintchecking;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the "untaint" unary expressions.
 *
 * @since 0.965.0
 */
public class UntaintAndTaintAnnotationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/taintchecking/expressions/untaint-with-other-constructs.bal");
    }

    // Test the behaviour of "untaint" expression in taint analyzer logic.

    @Test
    public void testUntaint() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/expressions/untaint.bal");
    }

    @Test
    public void testTaint() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/expressions/taint.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'sensitiveInput'", 19, 20);
    }

    @Test
    public void testUntaintVariable() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/expressions/untaint-variable-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 5, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 8, 20);
    }

    // Tests to confirm that "untaint" expression does not introduce any side effects to expected runtime behaviour.

    @Test
    public void testUntaintWithAddOperator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithAddOperatorInReturn");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithAddOperatorWithVariable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithAddOperatorInReturn");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithFunctionParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithFunctionParam");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithFunctionReturn() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithFunctionReturn");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithReceiver() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithReceiver");
        Assert.assertEquals(returns[0].stringValue(), "input1input2input3input4");
    }

    @Test
    public void untaintWithLengthOf() {
        BValue[] returns = BRunUtil.invoke(compileResult, "untaintWithLengthOf");
        Assert.assertEquals(returns[0].stringValue(), "24");
    }
}
