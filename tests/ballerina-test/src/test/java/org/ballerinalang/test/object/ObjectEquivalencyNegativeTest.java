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
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined struct types with attached functions in ballerina.
 */
public class ObjectEquivalencyNegativeTest {

    @Test(description = "Test equivalence of structs that are in the same package", enabled = false)
    public void testEquivalenceOfStructsInSamePackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency-01-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "unsafe cast from 'employee01' to 'person01', use multi-return cast expression", 17, 18);
        BAssertUtil.validateError(compileResult, 1,
                "unsafe cast from 'employee02' to 'person02', use multi-return cast expression", 36, 18);
        BAssertUtil.validateError(compileResult, 2,
                "unsafe cast from 'employee03' to 'person03', use multi-return cast expression", 54, 18);
        BAssertUtil.validateError(compileResult, 3,
                "unsafe cast from 'employee04' to 'person04', use multi-return cast expression", 76, 18);
        BAssertUtil.validateError(compileResult, 4,
                "unsafe cast from 'employee05' to 'person05', use multi-return cast expression", 100, 18);
        BAssertUtil.validateError(compileResult, 5,
                "unsafe cast from 'employee06' to 'person06', use multi-return cast expression", 122, 18);
        BAssertUtil.validateError(compileResult, 6,
                "unsafe cast from 'employee07' to 'person07', use multi-return cast expression", 166, 18);
        BAssertUtil.validateError(compileResult, 7,
                "unsafe cast from 'employee08' to 'person08', use multi-return cast expression", 214, 18);
        BAssertUtil.validateError(compileResult, 8,
                "unsafe cast from 'employee09' to 'person09', use multi-return cast expression", 262, 18);
    }

    @Test(description = "Test equivalence of structs that are in the same package from a third package",
            enabled = false)
    public void testEquivalenceOfStructsInSamePackageFromDifferentPackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency-02-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "unsafe cast from 'org.foo.bar:userBar' to 'org.foo:userFoo', " +
                        "use multi-return cast expression", 11, 23);

    }
}
