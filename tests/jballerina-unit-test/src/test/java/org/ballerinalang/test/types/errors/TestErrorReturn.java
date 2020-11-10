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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 2.0);
    }

    @Test(description = "Testing test2 method")
    public void testValidateIgnoreReturn2() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "b");
    }

    @Test(description = "Testing test3 method")
    public void testValidateIgnoreReturn3() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0);
    }


    @Test(description = "validate ignored error struct type.")
    public void testValidateErrorReturn() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/errors/error-return.bal");
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnError", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "FOO:10.5");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "QUX:ERROR");
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[2].stringValue(), "BAZ:0.0");
        Assert.assertNotNull(returns[3]);
        Assert.assertEquals(returns[3].stringValue(), "BAR:11.5");

        returns = BRunUtil.invoke(compileResult, "testReturnAndThrowError", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "invalid name");
    }

    @Test(description = "test case for validating when all variables are ignored. ")
    public void testValidateIgnoreAll() {
        BValue[] args = {};
        BRunUtil.invoke(compileResult, "test4", args);
        BRunUtil.invoke(compileResult, "test5", args);
    }

}
