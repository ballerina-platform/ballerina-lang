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
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for user defined object types with private fields in ballerina.
 */
public class ObjectWithPrivateFieldsNegativeTest {

    @Test(description = "Test runtime object equivalence  field access")
    public void testRuntimeObjEqNegative() {

        CompileResult compileResult = BCompileUtil.compile("test-src/object/RuntimeObjEgNegativeProject");
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeObjEqNegative");

        Assert.assertEquals(returns[0].stringValue(), "{ballerina}TypeCastError {\"message\":\"incompatible types:" +
                " 'pkg.org_foo:user' cannot be cast to 'pkg:userB'\"}");
    }

    @Test(description = "Test private field access")
    public void testPrivateFieldAccess() {

        CompileResult compileResult = BCompileUtil.compile("test-src/object/PrivateFieldsAccessProject");

        BAssertUtil.validateError(compileResult, 0, "attempt to refer to non-accessible symbol 'ssn'", 7, 18);
        BAssertUtil.validateError(compileResult, 1, "undefined field 'ssn' in object 'test/pkg.org_foo:1.0.0:person'",
                7, 20);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/PrivateObjAccess1Project");

        Assert.assertEquals(compileResult.getErrorCount(), 5);
        String expectedErrMsg1 = "attempt to refer to non-accessible symbol ";
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'ParentFoo.init'", 4, 23);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'ChildFoo.init'", 4, 31);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'PrivatePerson.init'", 12, 40);
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'PrivatePerson.init'", 16, 47);
        BAssertUtil.validateError(compileResult, i, expectedErrMsg1 + "'PrivatePerson.init'", 20, 27);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess1SemanticsNegative() {
        CompileResult compileResult
                = BCompileUtil.compile("test-src/object/PrivateObjAccess1SemanticsNegativeProject");
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
        CompileResult compileResult = BCompileUtil.compile("test-src/object/PrivateObjAccess2Project");

        Assert.assertEquals(compileResult.getErrorCount(), 2);
        String expectedErrMsg1 = "attempt to refer to non-accessible symbol ";
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, expectedErrMsg1 + "'address'", 10, 13);
        BAssertUtil.validateError(compileResult, i,
                "undefined field 'address' in object 'test/pkg.org_foo_baz:1.0.0:FooEmployee'", 10, 18);
    }

    @Test(description = "Test private object access in public functions")
    public void testPrivateObjAccess2SemanticsNegative() {
        CompileResult compileResult
                = BCompileUtil.compile("test-src/object/PrivateObjAccess2SemanticsNegativeProject");

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
                "undefined field 'address' in object 'test/pkg.org_foo_baz_sn:1.0.0:FooEmployee'", 10, 18);
    }
}
