/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.types.constant;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant access test cases.
 */
public class ConstantAccessTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src/types/constant/access", "main");
    }

    @Test(description = "Test accessing constant from other packages")
    public void testAccessingConstantFromOtherPkg() {
        BValue[] returns = BRunUtil.invoke(compileResult, "accessConstantFromOtherPkg");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 342342.234);
    }

    @Test(description = "Test accessing public constant from other packages")
    public void accessPublicConstantFromOtherPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "accessPublicConstantFromOtherPackage");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test(description = "Test accessing public constant type from other packages")
    public void accessPublicConstantTypeFromOtherPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "accessPublicConstantTypeFromOtherPackage");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "A");
    }

    @Test(description = "Test assigning constant from other package to global variable")
    public void testAssigningConstFromOtherPkgToGlobalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "assignConstFromOtherPkgToGlobalVar");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 342342.234);
    }

    @Test(description = "Test negative constant values")
    public void testNegativeConstantValues() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getNegativeConstants");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BFloat.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -342);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -88);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), -88.2);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), -3343.88);
    }

    @Test(description = "Test assigning float to int in constants")
    public void floatIntConversion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "floatIntConversion");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);

        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.0);

        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 10.0);
    }

    @Test
    public void testTypeAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeAssignment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "A");
    }
}
