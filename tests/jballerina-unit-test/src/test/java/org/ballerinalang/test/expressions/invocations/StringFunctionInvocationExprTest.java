/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.expressions.invocations;

import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * String function invocation test.
 *
 * @since 0.990.3
 */
public class StringFunctionInvocationExprTest {

    private CompileResult compileResult;
    private CompileResult compileResultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/invocations/string-function-invocation-expr.bal");
        compileResultNegative = BCompileUtil.compile("test-src/expressions/invocations/" +
                "string-function-invocation-negative.bal");
    }

    @Test
    public void testStringFunctionInvocation() {
        BValue[] values = BRunUtil.invoke(compileResult, "testStringFunctionInvocation");
        Assert.assertEquals(values.length, 1);
        Assert.assertTrue(values[0] instanceof BString);
        Assert.assertEquals(values[0].stringValue(), "BALLERINA");
    }

    @Test
    public void testStringFunctionInvocationInArgument() {
        BValue[] values = BRunUtil.invoke(compileResult, "testStringFunctionInvocationInArgument");
        Assert.assertEquals(values.length, 1);
        Assert.assertTrue(values[0] instanceof BString);
        Assert.assertEquals(values[0].stringValue(), "Hello BALLERINA");
    }

    @Test
    public void testStringFunctionInvocationNegative() {
        Assert.assertEquals(compileResultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(compileResultNegative, 0, "undefined function 'randomFunction' in type 'string'", 2
                , 27);
    }
}
