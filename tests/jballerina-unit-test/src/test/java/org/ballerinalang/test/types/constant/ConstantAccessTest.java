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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant access test cases.
 */
public class ConstantAccessTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/AccessProject");
    }

    @Test(description = "Test accessing constant from other packages")
    public void testAccessingConstantFromOtherPkg() {
        Object returns = BRunUtil.invoke(compileResult, "accessConstantFromOtherPkg");

        Assert.assertSame(returns.getClass(), Double.class);
        Assert.assertEquals(returns, 342342.234);
    }

    @Test(description = "Test accessing public constant from other packages")
    public void accessPublicConstantFromOtherPackage() {
        Object returns = BRunUtil.invoke(compileResult, "accessPublicConstantFromOtherPackage");

        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test(description = "Test accessing public constant type from other packages")
    public void accessPublicConstantTypeFromOtherPackage() {
        Object returns = BRunUtil.invoke(compileResult, "accessPublicConstantTypeFromOtherPackage");

        Assert.assertEquals(returns.toString(), "A");
    }

    @Test(description = "Test assigning constant from other package to global variable")
    public void testAssigningConstFromOtherPkgToGlobalVar() {
        Object returns = BRunUtil.invoke(compileResult, "assignConstFromOtherPkgToGlobalVar");

        Assert.assertSame(returns.getClass(), Double.class);
        Assert.assertEquals(returns, 342342.234);
    }

    @Test(description = "Test negative constant values")
    public void testNegativeConstantValues() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getNegativeConstants");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        Assert.assertSame(returns.get(2).getClass(), Double.class);
        Assert.assertSame(returns.get(3).getClass(), Double.class);
        Assert.assertEquals(returns.get(0), -342L);
        Assert.assertEquals(returns.get(1), -88L);
        Assert.assertEquals(returns.get(2), -88.2);
        Assert.assertEquals(returns.get(3), -3343.88);
    }

    @Test(description = "Test assigning float to int in constants")
    public void floatIntConversion() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "floatIntConversion");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertSame(returns.get(0).getClass(), Double.class);
        Assert.assertEquals(returns.get(0), 4.0);

        Assert.assertSame(returns.get(1).getClass(), Double.class);
        Assert.assertEquals(returns.get(1), 6.0);

        Assert.assertSame(returns.get(2).getClass(), Double.class);
        Assert.assertEquals(returns.get(2), 10.0);
    }

    @Test
    public void testTypeAssignment() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeAssignment");

        Assert.assertEquals(returns.toString(), "A");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
