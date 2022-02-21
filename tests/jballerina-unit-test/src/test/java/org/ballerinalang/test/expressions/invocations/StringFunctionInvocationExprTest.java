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

import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object values = BRunUtil.invoke(compileResult, "testStringFunctionInvocation");
        Assert.assertTrue(values instanceof BString);
        Assert.assertEquals(values.toString(), "BALLERINA");
    }

    @Test
    public void testStringFunctionInvocationInArgument() {
        Object values = BRunUtil.invoke(compileResult, "testStringFunctionInvocationInArgument");
        Assert.assertTrue(values instanceof BString);
        Assert.assertEquals(values.toString(), "Hello BALLERINA");
    }

    @Test
    public void testStringFunctionInvocationNegative() {
        Assert.assertEquals(compileResultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(compileResultNegative, 0, "undefined function 'randomFunction' in type 'string'", 2
                , 27);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        compileResultNegative = null;
    }
}
