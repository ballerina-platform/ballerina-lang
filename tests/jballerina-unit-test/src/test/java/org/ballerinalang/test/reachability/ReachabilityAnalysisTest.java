/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.reachability;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateHint;

/**
 * Test cases related to reachability analysis.
 *
 * @since 2.0.0
 */
public class ReachabilityAnalysisTest {
    private static final String UNREACHABLE_CODE = "unreachable code";
    private static final String UNREACHABLE_STMT_BLOCK = "unreachable statement block";
    private static final String INVALID_ERROR_UNREACHABLE_INVOCATION =
            "invalid 'error:unreachable()' function invocation in reachable code";
    private static final String UNNECESSARY_CONDITION =
            "unnecessary condition: expression will always evaluate to 'true'";
    private static final String MUST_RETURN_A_RESULT = "this function must return a result";

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/reachability-analysis/reachability_analysis.bal");
    }

    @Test(dataProvider = "reachabilityTests")
    public void testReachability(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "reachabilityTests")
    public Object[] reachabilityTests() {
        return new Object[]{
                "testReachableCodeWithWhile1",
                "testReachableCodeWithIf1",
                "testReachableCodeWithIf2",
                "testReachableCodeWithIf3",
                "testReachableCodeWithIfElse1",
                "testReachableCodeWithIfElse2",
                "testReachableCodeWithIfElse3",
                "testReachableCodeWithIfElse4",
                "testReachableCodeWithNestedIfElse1",
                "testReachableCodeWithIfElseAndConditionalExpr",
                "testReachableCodeWithIfElseAndConditionalExpr2",
                "testReachableCodeWithWhile1",
                "testReachableCodeWithWhile2",
                "testReachableCodeWithWhile3",
                "testReachableCodeWithWhile4"
        };
    }

    @Test
    public void testIsolatedFunctionsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/reachability-analysis/unreachability_test.bal");
        int i = 0;
        validateError(result, i++, UNREACHABLE_CODE, 18, 17);
        validateError(result, i++, UNREACHABLE_CODE, 26, 17);
        validateError(result, i++, UNREACHABLE_CODE, 36, 14);
        validateError(result, i++, UNREACHABLE_CODE, 44, 14);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 55, 9);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 57, 9);
        validateError(result, i++, UNREACHABLE_CODE, 64, 14);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 78, 9);
        validateError(result, i++, UNREACHABLE_CODE, 79, 12);
        validateError(result, i++, UNREACHABLE_CODE, 92, 5);
        validateError(result, i++, UNREACHABLE_CODE, 101, 9);
        validateHint(result, i++, UNNECESSARY_CONDITION, 122, 15);
        validateError(result, i++, UNREACHABLE_CODE, 124, 12);
        validateHint(result, i++, UNNECESSARY_CONDITION, 138, 15);
        validateError(result, i++, UNREACHABLE_CODE, 140, 22);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 155, 9);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 167, 5);
        validateError(result, i++, MUST_RETURN_A_RESULT, 169, 1);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 178, 5);
        validateError(result, i++, MUST_RETURN_A_RESULT, 180, 1);
        validateError(result, i++, UNREACHABLE_CODE, 184, 17);
        validateError(result, i++, UNREACHABLE_CODE, 187, 16);
        validateError(result, i++, UNREACHABLE_CODE, 194, 17);
        validateError(result, i++, UNREACHABLE_CODE, 197, 16);
        validateHint(result, i++, UNNECESSARY_CONDITION, 212, 12);
        validateError(result, i++, UNREACHABLE_CODE, 216, 9);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 218, 5);
        validateError(result, i++, MUST_RETURN_A_RESULT, 221, 1);
        validateHint(result, i++, UNNECESSARY_CONDITION, 225, 11);
        validateError(result, i++, UNREACHABLE_CODE, 229, 22);
        validateError(result, i++, UNREACHABLE_CODE, 238, 13);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 242, 5);
        validateError(result, i++, MUST_RETURN_A_RESULT, 245, 1);
        validateError(result, i++, UNREACHABLE_CODE, 249, 17);
        validateError(result, i++, INVALID_ERROR_UNREACHABLE_INVOCATION, 254, 5);
        validateError(result, i++, MUST_RETURN_A_RESULT, 257, 1);
        Assert.assertEquals(result.getErrorCount(), i - 4);
        Assert.assertEquals(result.getHintCount(), 4);
    }
}
