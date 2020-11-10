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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "emptyFunction", args);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test function with empty default worker")
    public void testFunctionWithEmptyDefaultWorker() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "funcEmptyDefaultWorker", args);
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testNoReturnFunctions() {
        BValue[] args = {};
        BRunUtil.invoke(result, "test1", args);
        BRunUtil.invoke(result, "test2", args);
        BRunUtil.invoke(result, "test3", args);
    }
}
