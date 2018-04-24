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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for user defined object types with private fields in ballerina.
 */
public class ObjectWithPrivateFieldsNegativeTest {

    @Test(description = "Test runtime object equivalence  field access")
    public void testRuntimeObjEqNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-private-fields-01-negative.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeObjEqNegative");

        Assert.assertEquals(returns[0].stringValue(), "'org.foo:user' cannot be cast to 'userB'");
    }

    @Test(description = "Test private field access")
    public void testPrivateFieldAccess() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-private-fields-02-negative.bal");

        BAssertUtil.validateError(compileResult, 0, "attempt to refer to non-public symbol 'ssn'", 7, 18);
        BAssertUtil.validateError(compileResult, 1, "undefined field 'ssn' in struct 'org.foo:person'", 7, 18);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess1() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/object", "private-field1");

        Assert.assertEquals(compileResult.getErrorCount(), 15);
        String expectedErrMsg1 = "attempt to refer to non-public symbol ";
        String expectedErrMsg2 = "attempt to expose non-public symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg2 + "'ChildFoo'", 5, 9);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg2 + "'privatePerson'", 33, 45);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg2 + "'privatePerson'", 41, 73);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg2 + "'FooFamily'", 16, 9);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg1 + "'ChildFoo.new'", 4, 32);
        BAssertUtil.validateError(compileResult, 6, expectedErrMsg1 + "'ParentFoo.new'", 4, 24);
        BAssertUtil.validateError(compileResult, 7, expectedErrMsg1 + "'privatePerson'", 8, 9);
        BAssertUtil.validateError(compileResult, 8, expectedErrMsg1 + "'privatePerson'", 8, 13);
        BAssertUtil.validateError(compileResult, 9, expectedErrMsg1 + "'privatePerson.new'", 12, 43);
        BAssertUtil.validateError(compileResult, 10, expectedErrMsg1 + "'privatePerson'", 16, 9);
        BAssertUtil.validateError(compileResult, 11, expectedErrMsg1 + "'privatePerson.new'", 16, 47);
        BAssertUtil.validateError(compileResult, 12, expectedErrMsg1 + "'privatePerson'", 16, 13);
        BAssertUtil.validateError(compileResult, 13, expectedErrMsg1 + "'privatePerson'", 20, 5);
        BAssertUtil.validateError(compileResult, 14, "unknown type 'privatePerson'", 20, 5);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess2() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/object", "private-field2");

        Assert.assertEquals(compileResult.getErrorCount(), 10);
        String expectedErrMsg1 = "attempt to refer to non-public symbol ";
        String expectedErrMsg2 = "attempt to expose non-public symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg2 + "'ChildFoo'", 5, 9);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg2 + "'FooFamily'", 16, 9);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg1 + "'FooFamily'", 5, 13);
        BAssertUtil.validateError(compileResult, 7, expectedErrMsg1 + "'FooFamily'", 10, 13);
        BAssertUtil.validateError(compileResult, 8, expectedErrMsg1 + "'address'", 15, 13);
        BAssertUtil.validateError(compileResult, 9, "undefined field 'address' in struct 'org.foo.baz:FooEmployee'",
                15, 13);
    }
}
