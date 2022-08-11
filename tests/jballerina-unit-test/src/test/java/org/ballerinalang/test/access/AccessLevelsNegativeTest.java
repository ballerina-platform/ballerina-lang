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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Negative test cases for checking rules on private types/fields access in ballerina.
 */
public class AccessLevelsNegativeTest {

    @Test(description = "Test private field access")
    public void testPrivateAccessLevel() {
        CompileResult compileResult = BCompileUtil.compile("test-src/access/private_access_negative.bal");

        String expectedWarningMsg = "attempt to expose non-public symbol ";
        int i = 0;
        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 11, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'ChildFoo'", 23, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 24, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 25, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'ChildFoo'", 28, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Bar'", 44, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 44, 55);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 44, 76);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Bar'", 49, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 49, 55);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 49, 76);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 54, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 59, 26);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 59, 48);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 76, 45);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 77, 45);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 126, 6);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 127, 6);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'BarRecord'", 128, 6);
        validateWarning(compileResult, i++, expectedWarningMsg + "'ChildFoo'", 133, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'ChildRecord'", 134, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 135, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 136, 5);
        validateWarning(compileResult, i++, expectedWarningMsg + "'BarRecord'", 154, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 154, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 154, 1);
        validateWarning(compileResult, i++, expectedWarningMsg + "'BarRecord'", 163, 33);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 163, 33);
        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 163, 33);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'Baz'", 165, 37);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'Foo'", 166, 37);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'BarRecord'", 167, 37);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'FooTypeObj'", 191, 1);
//        validateWarning(compileResult, i++, expectedWarningMsg + "'BarTypeRecord'", 193, 1);
        Assert.assertEquals(compileResult.getWarnCount(), i);
    }

    @Test
    public void testPrivateTypeAccessNegative() {
        CompileResult pkgB = BCompileUtil.compile("test-src/access/pvtAccessTest");
        validateError(pkgB, 0, "attempt to refer to non-accessible symbol 'Bar'", 22, 2);
        validateError(pkgB, 1, "unknown type 'Bar'", 22, 2);
    }
}
