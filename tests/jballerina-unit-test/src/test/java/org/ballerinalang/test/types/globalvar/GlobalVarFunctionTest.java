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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Global variable function test.
 */
public class GlobalVarFunctionTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/global-var-function.bal");
    }

    @Test(description = "Test Defining global variables")
    public void testDefiningGlobalVar() {
        Object[] args = new Object[0];
        BArray returns = (BArray) BRunUtil.invoke(result, "getGlobalVars", args);
        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertSame(returns.get(2).getClass(), Double.class);
        Assert.assertSame(returns.get(3).getClass(), Long.class);
        Assert.assertEquals(returns.get(0), 800L);
        Assert.assertEquals(returns.get(1).toString(), "value");
        Assert.assertEquals(returns.get(2), 99.34323);
        Assert.assertEquals(returns.get(3), 88343L);
    }

    @Test(description = "Test access global variable within function")
    public void testAccessGlobalVarWithinFunctions() {
        Object returns = BRunUtil.invoke(result, "accessGlobalVar");

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 89143L);
    }

    @Test(description = "Test change global var within functions")
    public void testChangeGlobalVarWithinFunction() {
        Object[] args = {(88)};
        Object returns = BRunUtil.invoke(result, "changeGlobalVar", args);

        Assert.assertSame(returns.getClass(), Double.class);

        Assert.assertEquals(returns, 165.0);

        CompileResult resultGlobalVar = BCompileUtil
                .compile("test-src/statements/variabledef/global-var-function.bal");

        Object returnsChanged = BRunUtil.invoke(resultGlobalVar, "getGlobalFloatVar");

        Assert.assertSame(returnsChanged.getClass(), Double.class);

        Assert.assertEquals(returnsChanged, 80.0);
    }

    @Test(description = "Test assigning global variable to another global variable")
    public void testAssignGlobalVarToAnotherGlobalVar() {
        Object returns = BRunUtil.invoke(result, "getGlobalVarFloat1");

        Assert.assertSame(returns.getClass(), Double.class);

        Assert.assertEquals(returns, 99.34323);
    }

    @Test(description = "Test assigning global var within a function")
    public void testInitializingGlobalVarWithinFunction() {
        BArray returns = (BArray) BRunUtil.invoke(result, "initializeGlobalVarSeparately");

        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertSame(returns.get(1).getClass(), Double.class);

        Assert.assertEquals(returns.get(0).toString(), "{\"name\":\"James\",\"age\":30}");
        Assert.assertEquals(returns.get(1), 3432.3423);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
