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

import io.ballerina.runtime.api.values.BDecimal;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant test cases.
 */
public class ConstantInTypeDefinitionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/const-in-type-definitions.bal");
    }

    @Test
    public void testTypeConstants() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeConstants");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstWithTypeAssignmentToType() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstWithoutTypeAssignmentToType() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstAndTypeComparison() {
        Object returns = BRunUtil.invoke(compileResult, "testConstAndTypeComparison");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeConstAsParam() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeConstAsParam");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBooleanTypeWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanTypeWithType");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testBooleanTypeWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanTypeWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testIntTypeWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testIntTypeWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testIntTypeWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testIntTypeWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 20L);
    }

    @Test
    public void testByteTypeWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testByteTypeWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatTypeWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatTypeWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testFloatTypeWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatTypeWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 2.0);
    }

    @Test
    public void testDecimalTypeWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalTypeWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BDecimal) returns).floatValue(), 4.0);
    }

    @Test
    public void testStringTypeWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testStringTypeWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testStringTypeWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testStringTypeWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
