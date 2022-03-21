/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for functions.
 */
public class FunctionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/invocations/function-stmt.bal");
    }

    @Test(description = "Test empty function scenario")
    public void testEmptyFunction() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "emptyFunction", args);
        Assert.assertNull(returns);
    }

    @Test(description = "Test function with empty default worker")
    public void testFunctionWithEmptyDefaultWorker() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "funcEmptyDefaultWorker", args);
        Assert.assertNull(returns);
    }

    @Test
    public void testNoReturnFunctions() {
        Object[] args = {};
        BRunUtil.invoke(result, "test1", args);
        BRunUtil.invoke(result, "test2", args);
        BRunUtil.invoke(result, "test3", args);
    }

    @Test(description = "Test frame yield depth overflow", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}StackOverflow \\{\"message\":\"stack overflow\"}")
    public void testRecursiveFunctionWhereStackOverflows() {
        BRunUtil.invoke(result, "testRecursiveFunctionWhereStackOverflows");
    }

    @Test
    public void testRecursiveFunctionWhichYields() {
        BRunUtil.invoke(result, "testRecursiveFunctionWhichYields");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
