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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSingleNamedReturnParam", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

    @Test(description = "Test single named return parameter, zero return arguments")
    public void testSingleNamedReturnParamZeroReturnArgs() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSingleNamedReturnParamZeroReturnArgs", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParam() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testTwoNamedReturnParam", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "test, john");
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParamZeroReturnArgs() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testTwoNamedReturnParamZeroReturnArgs", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "test, john");
    }
}
