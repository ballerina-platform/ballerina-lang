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

    @Test(description = "Test private field access")
    public void testCompileTimeStructEq() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-private-fields-02-negative.bal");

        BAssertUtil.validateError(compileResult, 2,
                "unsafe cast from 'personFoo' to 'org.foo:person', use multi-return cast expression", 22, 20);
    }

    @Test(description = "Test runtime struct equivalence  field access")
    public void testRuntimeTimeStructEqNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/structs/struct-private-fields-01-negative.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeTimeStructEqNegative");

        Assert.assertEquals(returns[0].stringValue(), "'org.foo:user' cannot be cast to 'userB'");
    }
}
