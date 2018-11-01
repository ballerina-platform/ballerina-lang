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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for reading constants.
 */
public class ConstantTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        result = BCompileUtil.compile("test-src/balo/test_balo/constant/constant.bal");
    }

    @Test
    public void testAccessConstantWithoutType() {
        BValue[] returns = BRunUtil.invoke(result, "testAccessConstantWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testAccessConstantWithType() {
        BValue[] returns = BRunUtil.invoke(result, "testAccessConstantWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
    }

    @Test
    public void testAccessFiniteType() {
        BValue[] returns = BRunUtil.invoke(result, "testAccessFiniteType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "A");
    }

    // Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11148
    @Test(enabled = false)
    public void testReturnFiniteType() {
        BValue[] returns = BRunUtil.invoke(result, "testReturnFiniteType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "A");
    }
}
