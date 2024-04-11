/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the readonly violation error consistency between BTuple type and BRecord type.
 *
 * @since 2201.9.0
 */
public class TupleVsArrayReadonlyErrorConsistencyTest {
    private CompileResult resultWithTupleUpdateMethod;
    private CompileResult resultWithoutTupleUpdateMethod;

    @BeforeClass
    public void setup() {
        resultWithTupleUpdateMethod =
                BCompileUtil.compile("test-src/types/readonly/test_tuple_vs_array_readonly_violation_consistency.bal");
        resultWithoutTupleUpdateMethod = BCompileUtil.compile("test-src/types/readonly/" +
                "test_tuple_vs_array_readonly_violation_consistency_without_tuple_Update_method.bal");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'name' in record of type '\\(Employee & readonly\\)'\".*")
    public void testWithTupleUpdateMethod() {
        BRunUtil.invoke(resultWithTupleUpdateMethod, "testFrozenAnyArrayElementUpdate");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'name' in record of type 'Employee & readonly'\".*")
    public void testWithoutTupleUpdateMethod() {
        BRunUtil.invoke(resultWithoutTupleUpdateMethod, "testFrozenAnyArrayElementUpdate");
    }

    @AfterClass
    public void tearDown() {
        resultWithTupleUpdateMethod = null;
        resultWithoutTupleUpdateMethod = null;
    }
}
