/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.constant;

import io.ballerina.runtime.api.creators.ValueCreator;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for reading constants.
 */
public class SimpleConstantAccessInBalaTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/constant/constant-access.bal");
    }

    @Test
    public void testAccessConstantWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testAccessConstantWithoutType");
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testAccessConstantWithoutTypeAsString() {
        Object returns = BRunUtil.invoke(compileResult, "testAccessConstantWithoutTypeAsString");
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testAccessConstantWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testAccessConstantWithType");
        Assert.assertEquals(returns.toString(), "Colombo");
    }

    @Test
    public void testAccessFiniteType() {
        Object returns = BRunUtil.invoke(compileResult, "testAccessFiniteType");
        Assert.assertEquals(returns.toString(), "A");
    }

    @Test
    public void testReturnFiniteType() {
        Object returns = BRunUtil.invoke(compileResult, "testReturnFiniteType");
        Assert.assertEquals(returns.toString(), "A");
    }

    @Test
    public void testAccessTypeWithContInDef() {
        Object returns = BRunUtil.invoke(compileResult, "testAccessTypeWithContInDef");
        Assert.assertEquals(returns.toString(), "X");
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
        Assert.assertEquals(returns, ValueCreator.createDecimalValue("50.0"));
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
