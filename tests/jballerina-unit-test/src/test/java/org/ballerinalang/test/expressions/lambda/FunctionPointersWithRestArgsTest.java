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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRest");
        Assert.assertEquals(returns.toString(), "[1,2,3]");
    }

    @Test
    public void testFunctionPointerRestTyped() {
        Object returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestTyped");
        Assert.assertEquals(returns.toString(), "[4,5,6]");
    }

    @Test
    public void testFunctionPointerAssignmentWithRestParams() {
        Object arr = BRunUtil.invoke(fpProgram, "testFunctionPointerAssignmentWithRestParams");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 3);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0), 1L);

        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1), 2L);

        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(2).toString(), "[3,4]");
    }

    @Test
    public void testFunctionPointerRestParamExpand() {
        Object arr = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamExpand");
        BArray returns = (BArray) arr;
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.size(), 3);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0), 6L);

        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1), 7L);

        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(2).toString(), "[8,9,4]");
    }

    @Test
    public void testFunctionPointerRestParamUnionNarrow() {
        Object returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamUnionNarrow");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "[2,3,4]");
    }

    @Test
    public void testFunctionPointerRestParamUnionNarrowName() {
        Object returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamUnionNarrowName");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "[3,2,1]");
    }

    @Test
    public void testFunctionPointerRestParamStructuredType() {
        Object returns = BRunUtil.invoke(fpProgram, "testFunctionPointerRestParamStructuredType");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Irshad");
    }

    @AfterClass
    public void tearDown() {
        fpProgram = null;
    }
}
