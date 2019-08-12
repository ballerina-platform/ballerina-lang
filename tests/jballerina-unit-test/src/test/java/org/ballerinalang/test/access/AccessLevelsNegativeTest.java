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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for checking rules on private types/fields access in ballerina.
 */
public class AccessLevelsNegativeTest {

    @Test(description = "Test private field access")
    public void testPrivateAccessLevel() {
        CompileResult compileResult = BCompileUtil.compile("test-src/access/private_access_negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 22);
        String expectedErrMsg = "attempt to expose non-public symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg + "'Baz'", 15, 14);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg + "'Foo'", 16, 14);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg + "'ChildFoo'", 23, 5);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg + "'Foo'", 24, 5);
        BAssertUtil.validateError(compileResult, 4, expectedErrMsg + "'Baz'", 25, 5);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg + "'Foo'", 44, 76);
        BAssertUtil.validateError(compileResult, 6, expectedErrMsg + "'Foo'", 49, 77);
        BAssertUtil.validateError(compileResult, 7, expectedErrMsg + "'Baz'", 59, 48);
        BAssertUtil.validateError(compileResult, 8, expectedErrMsg + "'Baz'", 76, 45);
        BAssertUtil.validateError(compileResult, 9, expectedErrMsg + "'Foo'", 77, 45);
        BAssertUtil.validateError(compileResult, 10, expectedErrMsg + "'Baz'", 126, 6);
        BAssertUtil.validateError(compileResult, 11, expectedErrMsg + "'Foo'", 127, 6);
        BAssertUtil.validateError(compileResult, 12, expectedErrMsg + "'BarRecord'", 128, 6);
        BAssertUtil.validateError(compileResult, 13, expectedErrMsg + "'ChildFoo'", 133, 5);
        BAssertUtil.validateError(compileResult, 14, expectedErrMsg + "'ChildRecord'", 134, 5);
        BAssertUtil.validateError(compileResult, 15, expectedErrMsg + "'Foo'", 135, 5);
        BAssertUtil.validateError(compileResult, 16, expectedErrMsg + "'Baz'", 136, 5);
        BAssertUtil.validateError(compileResult, 17, expectedErrMsg + "'Baz'", 165, 37);
        BAssertUtil.validateError(compileResult, 18, expectedErrMsg + "'Foo'", 166, 37);
        BAssertUtil.validateError(compileResult, 19, expectedErrMsg + "'BarRecord'", 167, 37);
        BAssertUtil.validateError(compileResult, 20, expectedErrMsg + "'FooTypeObj'", 191, 1);
        BAssertUtil.validateError(compileResult, 21, expectedErrMsg + "'BarTypeRecord'", 193, 1);
    }
}
