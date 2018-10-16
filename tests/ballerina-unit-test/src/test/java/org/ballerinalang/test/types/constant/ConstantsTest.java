/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.constant;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Compile time constant tests.
 */
public class ConstantsTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/constants.bal");
    }

    @Test
    public void getName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getName");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void getAge() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getAge");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void getId() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getId");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 123);
    }

    @Test
    public void concatConstants() {
        BValue[] returns = BRunUtil.invoke(compileResult, "concatConstants");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals((returns[0]).stringValue(), "Hello Ballerina");
    }

    @Test
    public void typeTest() {
        BValue[] returns = BRunUtil.invoke(compileResult, "typeTest");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test
    public void testConstantsWithoutTypes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstantsWithoutTypes");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testConstantsWithTypes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstantsWithTypes");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testMixedWithLiteral() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMixedWithLiteral");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }

    @Test
    public void testMixedWithObject() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMixedWithObject");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void checkTypeAsParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "checkTypeAsParam");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }
}
