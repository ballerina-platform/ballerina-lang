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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
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
}
