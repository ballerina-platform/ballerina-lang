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
package org.ballerinalang.test.expressions.lambda;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Function pointers and lambda, with optional parameters.
 *
 * @since 0.965
 */
public class FunctionPointersWithOptionalArgsTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/lambda/function-pointers-with-optional-args.bal");
    }

    @Test()
    public void testFunctionPointersWithNamedArgs() {
        CompileResult result =
                BCompileUtil.compile("test-src/expressions/lambda/function-pointers-with-named-args-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'int[]', found 'int'", 2, 19);
        BAssertUtil.validateError(result, 1, "undefined symbol 'funcWithRestArgs'", 6, 69);
    }

    @Test
    public void testFunctionPointerAssignmentWithNamedParams() {
        Object arr = BRunUtil.invoke(result, "testFunctionPointerAssignmentWithNamedParams");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 3);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0), 1L);

        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1), 2L);

        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(2).toString(), "Alex");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
