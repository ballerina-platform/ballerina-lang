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
 * Test cases for equivalency of user defined struct types in ballerina.
 */
public class StructEquivalencyNegativeTest {

    @Test(description = "Test equivalence of structs that are in the same package")
    public void testEquivalenceOfStructsInSamePackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency-01-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "incompatible types: expected 'person01', found 'person01|error'", 17, 18);
        BAssertUtil.validateError(compileResult, 1,
                "incompatible types: expected 'person02', found 'person02|error'", 36, 18);
        BAssertUtil.validateError(compileResult, 2,
                "incompatible types: expected 'person03', found 'person03|error'", 54, 18);
        BAssertUtil.validateError(compileResult, 3,
                "incompatible types: expected 'person06', found 'person06|error'", 76, 18);
    }

    @Test(description = "Test equivalence of structs that are in the same package from a third package")
    public void testEquivalenceOfStructsInSamePackageFromDifferentPackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency-02-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "incompatible types: expected 'org.foo:user', found 'org.foo:userFoo|error'", 11, 23);

    }
}
