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
package org.ballerinalang.test.object;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for object initializer feature.
 */
public class ObjectInitializerTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src/object", "init");
    }

    @Test(description = "Test object initializers that are in the same package")
    public void testStructInitializerInSamePackage1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitializerInSamePackage1");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "Peter");
    }

    @Test(description = "Test object initializers that are in different packages")
    public void testStructInitializerInAnotherPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitializerInAnotherPackage");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "Peter");
    }

    @Test(description = "Test object initializer order, 1) default values, 2) initializer, 3) literal ")
    public void testStructInitializerOrder() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInitializerOrder");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
        //TODO: enable below assertion once https://github.com/ballerina-platform/ballerina-lang/issues/6849 is fixed.
//        Assert.assertEquals(returns[1].stringValue(), "AB");
    }

    @Test(description = "Test negative object initializers scenarios")
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile(this, "test-src/object", "init.negative");
        //TODO: enable below assertion once https://github.com/ballerina-platform/ballerina-lang/issues/6852 is fixed.
//        Assert.assertEquals(result.getErrorCount(), 2);
//        BAssertUtil.validateError(result, 1,
//                "unknown type 'student'", 6, 5);

    }
}

