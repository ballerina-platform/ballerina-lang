/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.statements.onfail;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains test methods related to the edge cases with on-fail clause.
 *
 * @since 2.0.0
 */

public class OnFailClauseTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/onfail/on-fail-clause.bal");
    }

    @Test(description = "Test on-fail clause edge cases")
    public void testOnFailClause() {
        BRunUtil.invoke(result, "testOnFailEdgeTestcases");
    }

    @Test(description = "Test on-fail clause negative cases - v1")
    public void testOnFailClauseNegativeCaseV1() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/onfail/on-fail-clause-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "undefined symbol 'i'", 22, 55);
    }

    @Test(description = "Test on-fail clause negative cases - v2")
    public void testOnFailClauseNegativeCaseV2() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/onfail/on-fail-clause-negative-v2.bal");

        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 21, 9);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 23, 5);
        BAssertUtil.validateError(negativeResult, i++, "this function must return a result", 32, 1);
        BAssertUtil.validateError(negativeResult, i++, "this function must return a result", 48, 1);
        BAssertUtil.validateError(negativeResult, i++, "this function must return a result", 66, 1);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt' may not have been initialized", 92, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt2' may not have been initialized", 106, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt3' may not have been initialized", 107, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt1' may not have been initialized", 121, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt2' may not have been initialized", 122, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt3' may not have been initialized", 123, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt2' may not have been initialized", 140, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt3' may not have been initialized", 141, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'resultInt3' may not have been initialized", 159, 5);
        BAssertUtil.validateError(negativeResult, i++, "variable 'k' may not have been initialized", 174, 9);
        BAssertUtil.validateError(negativeResult, i++, "variable 'str2' may not have been initialized", 212, 5);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
