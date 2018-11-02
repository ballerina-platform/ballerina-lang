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

    // Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11148
    @Test(enabled = false)
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
}
