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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
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
 * Constant test cases.
 */
public class ConstantInTypeDefinitionTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/const-in-type-definitions.bal");
    }

    @Test
    public void testTypeConstants() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeConstants");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "GET");
    }

    @Test
    public void testConstWithTypeAssignmentToType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "GET");
    }

    @Test
    public void testConstWithoutTypeAssignmentToType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "GET");
    }

    @Test
    public void testConstAndTypeComparison() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstAndTypeComparison");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeConstAsParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeConstAsParam");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testBooleanTypeWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanTypeWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testBooleanTypeWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanTypeWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntTypeWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntTypeWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testIntTypeWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntTypeWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
    }

    @Test
    public void testByteTypeWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteTypeWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 240);
    }

    @Test
    public void testFloatTypeWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatTypeWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testFloatTypeWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatTypeWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.0);
    }

    @Test
    public void testDecimalTypeWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalTypeWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BDecimal) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testStringTypeWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringTypeWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina is awesome");
    }

    @Test
    public void testStringTypeWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringTypeWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina rocks");
    }
}
