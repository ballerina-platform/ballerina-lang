/*
*   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for function pointers with rest params.
 *
 * @since 1.1.0
 */
public class FunctionPointersWithRestArgsTest {

    private CompileResult fpProgram;

    @BeforeClass
    public void setup() {
        fpProgram = BCompileUtil.compile("test-src/expressions/lambda/function-pointers-with-rest-args.bal");
    }

    @Test
    public void testFunctionPointerRest() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRest");
        Assert.assertEquals(returns[0].stringValue(), "[1, 2, 3]");
    }

    @Test
    public void testFunctionPointerRestTyped() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestTyped");
        Assert.assertEquals(returns[0].stringValue(), "[4, 5, 6]");
    }

    @Test
    public void testFunctionPointerAssignmentWithRestParams() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerAssignmentWithRestParams");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 3);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);

        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);

        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[2].stringValue(), "[3, 4]");
    }

    @Test
    public void testFunctionPointerRestParamExpand() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamExpand");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 3);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);

        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 7);

        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[2].stringValue(), "[8, 9, 4]");
    }

    @Test
    public void testFunctionPointerRestParamUnionNarrow() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamUnionNarrow");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[2, 3, 4]");
    }

    @Test
    public void testFunctionPointerRestParamUnionNarrowName() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamUnionNarrowName");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[3, 2, 1]");
    }

    @Test
    public void testFunctionPointerRestParamStructuredType() {
        BValue[] returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamStructuredType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Irshad");
    }
}
