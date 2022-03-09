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
package org.ballerinalang.test.statements.returnstmt;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for named return statement.
 * return;
 */
public class NamedReturnParameterTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/returnstmt/named-return-positive.bal");
    }

    @Test(description = "Test single named return parameter")
    public void testSingleNamedReturnParam() {
        Object[] args = {(10), StringUtils.fromString("test")};
        Object returns = BRunUtil.invoke(compileResult, "testSingleNamedReturnParam", args);

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 12L);
    }

    @Test(description = "Test single named return parameter, zero return arguments")
    public void testSingleNamedReturnParamZeroReturnArgs() {
        Object[] args = {(10), StringUtils.fromString("test")};
        Object returns = BRunUtil.invoke(compileResult, "testSingleNamedReturnParamZeroReturnArgs", args);

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 12L);
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParam() {
        Object[] args = {(10), StringUtils.fromString("test")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testTwoNamedReturnParam", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "test, john");
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParamZeroReturnArgs() {
        Object[] args = {(10), StringUtils.fromString("test")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testTwoNamedReturnParamZeroReturnArgs", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);

        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "test, john");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
