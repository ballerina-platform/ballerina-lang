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
package org.ballerinalang.test.balo.constant;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for reading constants.
 */
public class ConstantTests {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        compileResult = BCompileUtil.compile("test-src/balo/test_balo/constant/constant.bal");
    }

    @Test
    public void testAccessConstantWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessConstantWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testAccessConstantWithoutTypeAsString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessConstantWithoutTypeAsString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testAccessConstantWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessConstantWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test
    public void testAccessFiniteType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessFiniteType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "A");
    }

    @Test
    public void testReturnFiniteType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnFiniteType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "A");
    }

    @Test
    public void testAccessTypeWithContInDef() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessTypeWithContInDef");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "C");
    }

    @Test
    public void testConstInMapKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInMapKey");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testConstInMapValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInMapValue");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testConstInJsonKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInJsonKey");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testConstInJsonValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInJsonValue");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testBooleanConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testByteConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 240);
    }

    @Test
    public void testFloatConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testStringConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina is awesome");
    }

    @Test
    public void testBooleanConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testByteConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 240);
    }

    @Test
    public void testFloatConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testStringConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina is awesome");
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
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 240);
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
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
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

    @Test
    public void testFloatAsFiniteType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatAsFiniteType");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.0);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.0);
    }

    @Test
    public void testNilWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNilWithoutType");
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testNilWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNilWithType");
        Assert.assertNull(returns[0]);
    }
}
