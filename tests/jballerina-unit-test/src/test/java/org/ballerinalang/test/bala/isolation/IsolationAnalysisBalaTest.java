/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.bala.isolation;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `isolated` objects and functions.
 *
 * @since 2.0.0
 */
public class IsolationAnalysisBalaTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_isolation");
        result = BCompileUtil.compile("test-src/bala/test_bala/isolation/test_isolation.bal");
    }

    @Test
    public void testIsolation() {
        assertEquals(result.getErrorCount(), 0);
        assertEquals(result.getWarnCount(), 0);

        BRunUtil.invoke(result, "testIsolatedFunctionCallInIsolatedFunction");
        BRunUtil.invoke(result, "testNewingIsolatedClassWithImportedIsolatedObjectFields");
    }

    @Test
    public void testIsolationNegative() {
        CompileResult negativeRes =
                BCompileUtil.compile("test-src/bala/test_bala/isolation/test_isolation_negative.bal");
        int index = 0;

        validateError(negativeRes, index++, "invalid invocation of a non-isolated function in an 'isolated' function",
                      20, 13);
        validateError(negativeRes, index++, "invalid invocation of a non-isolated function in an 'isolated' function",
                      23, 13);
        validateError(negativeRes, index++, "invalid non-isolated initialization expression in an 'isolated' function",
                      25, 55);
        validateError(negativeRes, index++, "invalid non-isolated initialization expression in an 'isolated' function",
                      27, 60);
        validateError(negativeRes, index++, "invalid non-private mutable field in an 'isolated' object", 34, 5);
        validateError(negativeRes, index++, "invalid non-private mutable field in an 'isolated' object", 35, 5);
        validateError(negativeRes, index++, "invalid non-private mutable field in an 'isolated' object", 36, 5);
        validateError(negativeRes, index++, "invalid initial value expression: expected an isolated expression", 40,
                      18);
        validateError(negativeRes, index++, "invalid initial value expression: expected an isolated expression", 41,
                      18);
        assertEquals(negativeRes.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
