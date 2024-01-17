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
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Global variable function with package test.
 */
public class GlobalVarFunctionWithPkgTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/TestGlobaVarProject1");
    }

    @Test(description = "Test accessing global variables defined in other packages")
    public void testAccessingGlobalVar() {
        BArray returns = (BArray) BRunUtil.invoke(result, "getGlobalVars", new Object[0]);
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

    @Test(description = "Test change global var within functions")
    public void testChangeGlobalVarWithinFunction() {
        Object[] args = {(88)};
        Object returns = BRunUtil.invoke(result, "changeGlobalVar", args);

        Assert.assertSame(returns.getClass(), Double.class);

        Assert.assertEquals(returns, 165.0);

        CompileResult resultGlobalVar = BCompileUtil.compile("test-src/statements/variabledef/TestGlobaVarProject1");
        Object returnsChanged = BRunUtil.invoke(resultGlobalVar, "getGlobalFloatVar", new Object[0]);

        Assert.assertSame(returnsChanged.getClass(), Double.class);

        Assert.assertEquals(returnsChanged, 80.0);
    }

    @Test(description = "Test assigning global variable to another global variable in different package")
    public void testAssignGlobalVarToAnotherGlobalVar() {
        Object returns = BRunUtil.invoke(result, "getAssignedGlobalVarFloat", new Object[0]);

        Assert.assertSame(returns.getClass(), Double.class);

        Assert.assertEquals(returns, 88343.0);

    }

    @Test(description = "Test assigning function invocation to global variable")
    public void testAssignFuncInvocationToGlobalVar() {
        Object returns = BRunUtil.invoke(result, "getGlobalVarInt", new Object[0]);

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 8876L);

    }

    @Test(description = "Test retrieving variable from different package when that package is already initialized " +
            "within another package")
    public void testRetrievingVarFromDifferentPkg() {

        CompileResult result = BCompileUtil.compile("test-src/statements/variabledef/TestGlobaVarProject2");
        Object returns = BRunUtil.invoke(result, "getStringInPkg", new Object[0]);

        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "sample value");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
