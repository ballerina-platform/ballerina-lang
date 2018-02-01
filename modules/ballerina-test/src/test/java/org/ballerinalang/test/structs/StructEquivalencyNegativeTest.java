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
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined struct types with attached functions in ballerina.
 */
public class StructEquivalencyNegativeTest {

    @Test(description = "Test equivalence of structs that are in the same package")
    public void testEquivalenceOfStructsInSamePackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency-01-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "unsafe cast from 'user' to 'person', use multi-return cast expression", 88, 16);
        BAssertUtil.validateError(compileResult, 1,
                "unsafe cast from 'employee' to 'person', use multi-return cast expression", 97, 16);
    }

    @Test(description = "Test equivalence of structs that are in the same package from a third package")
    public void testEquivalenceOfStructsInSamePackageFromDifferentPackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency-02-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "unsafe cast from 'org.foo.bar:userBar' to 'org.foo:userFoo', " +
                        "use multi-return cast expression", 11, 23);

    }
}
