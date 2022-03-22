/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to cover built in methods related tests on JBallerina.
 *
 * @since 0.995.0
 */
public class BuiltinMethodTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/builtin-methods.bal");
    }

    @Test(description = "Test clone")
    public void testClone() {
        Object result = BRunUtil.invoke(compileResult, "testClone");
        Assert.assertTrue(result instanceof BMap);
        BMap bMap = (BMap) result;
        Assert.assertEquals(bMap.get(StringUtils.fromString("test")).toString(), "sample");
    }

    @Test(description = "Test clone any")
    public void testCloneAny() {
        Object result = BRunUtil.invoke(compileResult, "testCloneAny");
        Assert.assertTrue(result instanceof BMap);
        BMap bMap = (BMap) result;
        Assert.assertEquals(bMap.get(StringUtils.fromString("test")).toString(), "sample");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
