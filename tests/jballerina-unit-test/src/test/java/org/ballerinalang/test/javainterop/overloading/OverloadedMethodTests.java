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
package org.ballerinalang.test.javainterop.overloading;

import org.ballerinalang.model.values.BHandleValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for java interop overloaded method invocations.
 *
 * @since 1.0.0
 */
public class OverloadedMethodTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/overloading/overloaded_constructor_test.bal");
    }

    @Test(description = "Test invoking a java constructor that accepts nothing")
    public void testOverloadedConstructorsWithOneParam() {
        BValue[] returns = BRunUtil.invoke(result, "testOverloadedConstructorsWithOneParam");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), "string buffer value");
        Assert.assertEquals(((BHandleValue) returns[1]).getValue(), "string builder value");
    }
}
