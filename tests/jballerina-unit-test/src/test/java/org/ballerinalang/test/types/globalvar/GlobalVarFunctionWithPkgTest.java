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

import org.ballerinalang.core.model.values.BFloat;
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
 * Global variable function with package test.
 */
public class GlobalVarFunctionWithPkgTest {

    CompileResult result;
    
    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(this, "test-src/statements/variabledef/TestProj", "globalvar.pkg.main");
    }

    @Test(description = "Test accessing global variables defined in other packages")
    public void testAccessingGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVars", new BValue[0]);
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

    @Test(description = "Test change global var within functions")
    public void testChangeGlobalVarWithinFunction() {
        BValue[] args = {new BInteger(88)};
        BValue[] returns = BRunUtil.invoke(result, "changeGlobalVar", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 165.0);

        CompileResult resultGlobalVar = BCompileUtil.compile(this, "test-src/statements/variabledef/TestProj",
                "globalvar.pkg.main");
        BValue[] returnsChanged = BRunUtil.invoke(resultGlobalVar, "getGlobalFloatVar", new BValue[0]);

        Assert.assertEquals(returnsChanged.length, 1);
        Assert.assertSame(returnsChanged[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returnsChanged[0]).floatValue(), 80.0);
    }

    @Test(description = "Test assigning global variable to another global variable in different package")
    public void testAssignGlobalVarToAnotherGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "getAssignedGlobalVarFloat", new BValue[0]);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 88343.0);

    }

    @Test(description = "Test assigning function invocation to global variable")
    public void testAssignFuncInvocationToGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarInt", new BValue[0]);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8876);

    }

    @Test(description = "Test retrieving variable from different package when that package is already initialized " +
            "within another package")
    public void testRetrievingVarFromDifferentPkg() {

        CompileResult result = BCompileUtil.compile(this, "test-src/statements/variabledef/TestProj",
                "globalvar.pkg.abc");
        BValue[] returns = BRunUtil.invoke(result, "getStringInPkg", new BValue[0]);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        Assert.assertEquals(((BString) returns[0]).stringValue(), "sample value");
    }
}
