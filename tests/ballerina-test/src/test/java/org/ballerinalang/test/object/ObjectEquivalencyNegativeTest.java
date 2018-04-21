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
 * Test cases for equivalency of user defined object types with attached functions in ballerina.
 */
public class ObjectEquivalencyNegativeTest {

    @Test(description = "Test equivalence of objects that are in the same package")
    public void testEquivalenceOfObjectsInSamePackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-equivalency-01-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "incompatible types: expected 'person01', found 'person01|error'", 22, 18);
        BAssertUtil.validateError(compileResult, 1,
                "incompatible types: expected 'person02', found 'person02|error'", 46, 18);
        BAssertUtil.validateError(compileResult, 2,
                "incompatible types: expected 'person03', found 'person03|error'", 69, 18);
        BAssertUtil.validateError(compileResult, 3,
                "incompatible types: expected 'person04', found 'person04|error'", 97, 18);
        BAssertUtil.validateError(compileResult, 4,
                "incompatible types: expected 'person05', found 'person05|error'", 128, 18);
        BAssertUtil.validateError(compileResult, 5,
                "incompatible types: expected 'person06', found 'person06|error'", 155, 18);
        BAssertUtil.validateError(compileResult, 6,
                "incompatible types: expected 'person07', found 'person07|error'", 214, 18);
        BAssertUtil.validateError(compileResult, 7,
                "incompatible types: expected 'person08', found 'person08|error'", 280, 18);
        BAssertUtil.validateError(compileResult, 8,
                "incompatible types: expected 'person09', found 'person09|error'", 346, 18);
    }

    @Test(description = "Test equivalence of objects that are in the same package from a third package")
    public void testEquivalenceOfObjectsInSamePackageFromDifferentPackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-equivalency-02-negative.bal");

        BAssertUtil.validateError(compileResult, 0,
                "incompatible types: expected 'org.foo:user', found 'org.foo:userFoo|error'", 11, 23);

    }
}
