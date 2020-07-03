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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for user defined object types with private fields in ballerina.
 */
public class ObjectWithPrivateFieldsNegativeTest {

    @Test(description = "Test runtime object equivalence  field access")
    public void testRuntimeObjEqNegative() {

        CompileResult compileResult = BCompileUtil.compile("test-src/object/ObjectProject",
                "object-private-fields-01-negative");
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeObjEqNegative");

        Assert.assertEquals(returns[0].stringValue(), "{ballerina}TypeCastError {\"message\":\"incompatible types:" +
                " 'org.foo:user' cannot be cast to 'object-private-fields-01-negative:userB'\"}");
    }

    @Test(description = "Test private field access")
    public void testPrivateFieldAccess() {

        CompileResult compileResult = BCompileUtil.compile("test-src/object/ObjectProject",
                "object-private-fields-02-negative");

        BAssertUtil.validateError(compileResult, 0, "attempt to refer to non-accessible symbol 'ssn'", 7, 18);
        BAssertUtil.validateError(compileResult, 1, "undefined field 'ssn' in object 'testorg/org.foo:1.0.0:person'",
                7, 19);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess1() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/object/ObjectProject", "private-field1");

        Assert.assertEquals(compileResult.getErrorCount(), 6);
        String expectedErrMsg1 = "attempt to refer to non-accessible symbol ";
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'ParentFoo.init'", 4, 24);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'ChildFoo.init'", 4, 32);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'ChildFoo.init'", 4, 32);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'PrivatePerson.init'", 12, 43);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'PrivatePerson.init'", 16, 47);
        BAssertUtil.validateError(compileResult, i, expectedErrMsg1 + "'PrivatePerson.init'", 20, 27);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess1SemanticsNegative() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/object/ObjectProject", "private-field1.sn");
        Assert.assertEquals(compileResult.getErrorCount(), 8);
        String expectedErrMsg1 = "attempt to refer to non-accessible symbol ";
        String expectedErrMsg2 = "attempt to expose non-public symbol ";
        int i = 0;
        // First error is in a different package
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'ChildFoo'", 5, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 34, 45);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 38, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 42, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 42, 73);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'FooFamily'", 16, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'PrivatePerson'", 20, 5);
        BAssertUtil.validateError(compileResult, i, "unknown type 'PrivatePerson'", 20, 5);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess2() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/object/ObjectProject", "private-field2");

        Assert.assertEquals(compileResult.getErrorCount(), 2);
        String expectedErrMsg1 = "attempt to refer to non-accessible symbol ";
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'address'", 10, 13);
        BAssertUtil.validateError(compileResult, i,
                "undefined field 'address' in object 'testorg/org.foo.baz:1.0.0:FooEmployee'", 10, 17);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess2SemanticsNegative() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/object/ObjectProject", "private-field2.sn");

        Assert.assertEquals(compileResult.getErrorCount(), 8);
        String expectedErrMsg1 = "attempt to refer to non-accessible symbol ";
        String expectedErrMsg2 = "attempt to expose non-public symbol ";
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'ChildFoo'", 5, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 34, 45);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 38, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 42, 1);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'PrivatePerson'", 42, 73);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg2 + "'FooFamily'", 16, 5);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'address'", 10, 13);
        BAssertUtil.validateError(compileResult, i,
                "undefined field 'address' in object 'testorg/org.foo.baz.sn:1.0.0:FooEmployee'", 10, 17);
    }
}
