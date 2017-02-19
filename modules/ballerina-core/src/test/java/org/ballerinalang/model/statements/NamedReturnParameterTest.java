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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for named return statement.
 * return;
 */
public class NamedReturnParameterTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/statements/returnstmt/named-return-positive.bal");
    }

    @Test(description = "Test single named return parameter")
    public void testSingleNamedReturnParam() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSingleNamedReturnParam", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(12, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test single named return parameter with zero return stmt arguments")
    public void testSingleNamedReturnParamDefaultValue() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSingleNamedReturnParamDefaultValue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(0, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test single named return parameter, zero return arguments")
    public void testSingleNamedReturnParamZeroReturnArgs() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSingleNamedReturnParamZeroReturnArgs", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(12, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test single named return parameter, zero return arguments and default values")
    public void testSingleNamedReturnParamDefaultValueZeroReturnArgs() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram,
                "testSingleNamedReturnParamDefaultValueZeroReturnArgs", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(0, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParam() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testTwoNamedReturnParam", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(12, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("test, john", returns[1].stringValue());
    }

    @Test(description = "Test two named return parameters with default valuse")
    public void testTwoNamedReturnParamDefaultValue() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testTwoNamedReturnParamDefaultValue", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(0, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("", returns[1].stringValue());
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParamZeroReturnArgs() {
        BValue[] args = {new BInteger(10), new BString("test")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testTwoNamedReturnParamZeroReturnArgs", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(12, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("test, john", returns[1].stringValue());
    }

    @Test(description = "Test two named return parameters")
    public void testTwoNamedReturnParamZeroReturnArgsDefaultValue() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram,
                "testTwoNamedReturnParamZeroReturnArgsDefaultValue", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(0, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("", returns[1].stringValue());
    }
}
