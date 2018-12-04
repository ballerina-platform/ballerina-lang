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
package org.ballerinalang.test.access;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for checking rules on private types/fields access in ballerina.
 */
public class AccessLevelsNegativeTest {

    @Test(description = "Test private field access")
    public void testPrivateAccessLevel() {
        CompileResult compileResult = BCompileUtil.compile("test-src/access/private_access_negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 25);
        String expectedErrMsg = "attempt to expose non-public symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg + "'Foo'", 13, 1);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg + "'Bar'", 15, 1);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg + "'Baz'", 17, 1);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg + "'Baz'", 21, 14);
        BAssertUtil.validateError(compileResult, 4, expectedErrMsg + "'Foo'", 22, 14);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg + "'ChildFoo'", 29, 5);
        BAssertUtil.validateError(compileResult, 6, expectedErrMsg + "'Foo'", 30, 5);
        BAssertUtil.validateError(compileResult, 7, expectedErrMsg + "'Baz'", 31, 5);
        BAssertUtil.validateError(compileResult, 8, expectedErrMsg + "'Foo'", 50, 77);
        BAssertUtil.validateError(compileResult, 9, expectedErrMsg + "'Foo'", 55, 78);
        BAssertUtil.validateError(compileResult, 10, expectedErrMsg + "'Baz'", 65, 48);
        BAssertUtil.validateError(compileResult, 11, expectedErrMsg + "'Baz'", 82, 45);
        BAssertUtil.validateError(compileResult, 12, expectedErrMsg + "'Foo'", 83, 45);
        BAssertUtil.validateError(compileResult, 13, expectedErrMsg + "'Baz'", 132, 6);
        BAssertUtil.validateError(compileResult, 14, expectedErrMsg + "'Foo'", 133, 6);
        BAssertUtil.validateError(compileResult, 15, expectedErrMsg + "'BarRecord'", 134, 6);
        BAssertUtil.validateError(compileResult, 16, expectedErrMsg + "'ChildFoo'", 139, 5);
        BAssertUtil.validateError(compileResult, 17, expectedErrMsg + "'ChildRecord'", 140, 5);
        BAssertUtil.validateError(compileResult, 18, expectedErrMsg + "'Foo'", 141, 5);
        BAssertUtil.validateError(compileResult, 19, expectedErrMsg + "'Baz'", 142, 5);
        BAssertUtil.validateError(compileResult, 20, expectedErrMsg + "'Baz'", 171, 37);
        BAssertUtil.validateError(compileResult, 21, expectedErrMsg + "'Foo'", 172, 37);
        BAssertUtil.validateError(compileResult, 22, expectedErrMsg + "'BarRecord'", 173, 37);
        BAssertUtil.validateError(compileResult, 23, expectedErrMsg + "'FooTypeObj'", 197, 1);
        BAssertUtil.validateError(compileResult, 24, expectedErrMsg + "'BarTypeRecord'", 199, 1);
    }
}
