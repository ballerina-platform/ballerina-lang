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
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for user defined struct types with private fields in ballerina.
 */
public class StructWithPrivateFieldsNegativeTest {

    @Test(description = "Test private field access")
    public void testPrivateFieldAccess() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-private-fields-02-negative.bal");

        BAssertUtil.validateError(compileResult, 0, "attempt to refer to non-public symbol 'ssn'", 9, 18);
        BAssertUtil.validateError(compileResult, 1, "undefined field 'ssn' in struct 'org.foo:person'", 9, 18);
    }

    @Test(description = "Test runtime struct equivalence  field access")
    public void testRuntimeStructEqNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/structs/struct-private-fields-01-negative.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeStructEqNegative");

        Assert.assertEquals(returns[0].stringValue(), "'org.foo:user' cannot be cast to 'userB'");
    }

    @Test(description = "Test private struct access in public functions")
    public void testPrivateStructAccess1() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/structs", "private-field1");

        Assert.assertEquals(compileResult.getErrorCount(), 11);
        String expectedErrMsg = "attempt to refer to non-public symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg + "'ChildFoo'", 4, 33);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg + "'ChildFoo'", 4, 33);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg + "'privatePerson'", 8, 9);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg + "'privatePerson'", 8, 13);
        BAssertUtil.validateError(compileResult, 4, expectedErrMsg + "'privatePerson'", 12, 43);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg + "'privatePerson'", 16, 9);
        BAssertUtil.validateError(compileResult, 6, expectedErrMsg + "'privatePerson'", 16, 47);
        BAssertUtil.validateError(compileResult, 7, expectedErrMsg + "'privatePerson'", 16, 13);
        BAssertUtil.validateError(compileResult, 8, expectedErrMsg + "'privatePerson'", 20, 5);
        BAssertUtil.validateError(compileResult, 9, "unknown type 'privatePerson'", 20, 5);
    }

    @Test(description = "Test private struct access in public functions")
    public void testPrivateStructAccess2() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/structs", "private-field2");

        Assert.assertEquals(compileResult.getErrorCount(), 6);
        String expectedErrMsg = "attempt to refer to non-public symbol ";

        BAssertUtil.validateError(compileResult, 1, expectedErrMsg + "'FooFamily'", 5, 13);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg + "'FooFamily'", 10, 13);
        BAssertUtil.validateError(compileResult, 4, expectedErrMsg + "'address'", 15, 13);
    }
}
