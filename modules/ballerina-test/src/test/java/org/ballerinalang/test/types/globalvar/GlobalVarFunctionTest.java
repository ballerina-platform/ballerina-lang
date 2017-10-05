/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.globalvar;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Global variable function test.
 */
public class GlobalVarFunctionTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/types/globalvar/global-var-function.bal");
    }

    @Test(description = "Test Defining global variables", enabled = false)
    public void testDefiningGlobalVar() {
        BValue[] args = new BValue[0];
        BValue[] returns = BTestUtils.invoke(result, "getGlobalVars", args);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 800);
        Assert.assertEquals(((BString) returns[1]).stringValue(), "value");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 99.34323);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 88343);
    }

    @Test(description = "Test access global variable within function", enabled = false)
    public void testAccessGlobalVarWithinFunctions() {
        BValue[] returns = BTestUtils.invoke(result, "accessGlobalVar");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 89143);
    }

    @Test(description = "Test change global var within functions", enabled = false)
    public void testChangeGlobalVarWithinFunction() {
        BValue[] args = {new BInteger(88)};
        BValue[] returns = BTestUtils.invoke(result, "changeGlobalVar", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 165.0);


        CompileResult resultGlobalVar = BTestUtils
                .compile("test-src/types/globalvar/global-var-function.bal");

        BValue[] returnsChanged = BTestUtils.invoke(resultGlobalVar, "getGlobalFloatVar");

        Assert.assertEquals(returnsChanged.length, 1);
        Assert.assertSame(returnsChanged[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returnsChanged[0]).floatValue(), 80.0);
    }

    @Test(description = "Test assigning global variable to another global variable", enabled = false)
    public void testAssignGlobalVarToAnotherGlobalVar() {
        BValue[] returns = BTestUtils.invoke(result, "getGlobalVarFloat1");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 99.34323);
    }

    @Test(description = "Test assigning global var within a function", enabled = false)
    public void testInitializingGlobalVarWithinFunction() {
        BValue[] returns = BTestUtils.invoke(result, "initializeGlobalVarSeparately");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BJSON.class);
        Assert.assertSame(returns[1].getClass(), BFloat.class);

        Assert.assertEquals(((BJSON) returns[0]).stringValue(), "{\"name\":\"James\",\"age\":30}");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 3432.3423);
    }
}
