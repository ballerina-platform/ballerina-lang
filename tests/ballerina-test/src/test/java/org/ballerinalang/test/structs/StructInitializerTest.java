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
package org.ballerinalang.test.structs;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for struct initializer feature.
 */
@Test(groups = {"broken"})
public class StructInitializerTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src/structs", "init");
    }

    @Test(description = "Test struct initializers that are in the same package")
    public void testStructInitializerInSamePackage1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructInitializerInSamePackage1");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "Peter");
    }

    @Test(description = "Test struct initializers that are in different packages")
    public void testStructInitializerInAnotherPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructInitializerInAnotherPackage");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "Peter");
    }

    @Test(description = "Test struct initializer order, 1) default values, 2) initializer, 3) literal ")
    public void testStructInitializerOrder() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructInitializerOrder");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
        Assert.assertEquals(returns[1].stringValue(), "AB");
    }

    @Test(description = "Test negative structs initializers scenarios")
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile(this, "test-src/structs", "init.negative");

        Assert.assertEquals(2, result.getErrorCount());
        BAssertUtil.validateError(result, 0,
                "explicit invocation of 'init.negative:person' struct initializer is not allowed",
                24, 5);
        BAssertUtil.validateError(result, 1,
                "attempt to create a struct with a non-public initializer ", 28, 21);

    }
}

