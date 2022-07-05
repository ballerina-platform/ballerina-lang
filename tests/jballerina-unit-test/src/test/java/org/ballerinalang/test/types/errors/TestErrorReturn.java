/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.errors;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for validating returning an error.
 */
public class TestErrorReturn {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/errors/valid-ignore.bal");
    }

    @Test(description = "Testing test1 method")
    public void testValidateIgnoreReturn1() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "a");
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1), 2.0);
    }

    @Test(description = "Testing test2 method")
    public void testValidateIgnoreReturn2() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "a");
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1).toString(), "b");
    }

    @Test(description = "Testing test3 method")
    public void testValidateIgnoreReturn3() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 1.0);
    }

    @Test(description = "validate ignored error struct type.")
    public void testValidateErrorReturn() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/errors/error-return.bal");
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testReturnError", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns.get(0));
        Assert.assertEquals(returns.get(0).toString(), "FOO:10.5");
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(1).toString(), "QUX:ERROR");
        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(2).toString(), "BAZ:0.0");
        Assert.assertNotNull(returns.get(3));
        Assert.assertEquals(returns.get(3).toString(), "BAR:11.5");

        Object returns1 = BRunUtil.invoke(compileResult, "testReturnAndThrowError", args);
        Assert.assertNotNull(returns1);
        Assert.assertEquals(returns1.toString(), "invalid name");
    }

    @Test(description = "test case for validating when all variables are ignored. ")
    public void testValidateIgnoreAll() {
        Object[] args = {};
        BRunUtil.invoke(compileResult, "test4", args);
        BRunUtil.invoke(compileResult, "test5", args);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
