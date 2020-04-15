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

        String expectedErrMsg = "attempt to expose non-public symbol ";
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 11, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'ChildFoo'", 23, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 24, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 25, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'ChildFoo'", 28, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Bar'", 44, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 44, 55);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 44, 76);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Bar'", 49, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 49, 55);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 49, 76);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 54, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 59, 26);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 59, 48);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 76, 45);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 77, 45);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 126, 6);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 127, 6);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'BarRecord'", 128, 6);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'ChildFoo'", 133, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'ChildRecord'", 134, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 135, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 136, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'BarRecord'", 154, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 154, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 154, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'BarRecord'", 163, 33);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 163, 33);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 163, 33);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Baz'", 165, 37);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'Foo'", 166, 37);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'BarRecord'", 167, 37);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'FooTypeObj'", 191, 1);
//        BAssertUtil.validateError(compileResult, i++, expectedErrMsg + "'BarTypeRecord'", 193, 1);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

    @Test
    public void testPrivateTypeAccessNegative() {
        CompileResult pkgB = BCompileUtil.compile(this, "test-src/access/pvtAccessTest", "B");
        BAssertUtil.validateError(pkgB, 0, "attempt to refer to non-accessible symbol 'Bar'", 22, 2);
        BAssertUtil.validateError(pkgB, 1, "unknown type 'Bar'", 22, 2);
    }
}
