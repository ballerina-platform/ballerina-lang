/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases to verify code analysis for match guards via compilation errors logged in code analysis.
 *
 * @since 2.0.0
 */
public class MatchGuardAnalysisTest {

    private static final String NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR =
            "cannot call a non-isolated function/method in a match guard when the type of the action/expression " +
                    "being matched is not a subtype of 'readonly'";
    private static final String NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR =
            "cannot call a function/method in a match guard with an argument of a type that is not a subtype of " +
                    "'readonly'";

    @Test
    public void testMatchGuardTypeCheckingNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/matchstmt/match_guard_type_checking_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found '()'", 25, 25);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'int'", 37, 14);
        BAssertUtil.validateError(result, i++, "operator '||' not defined for 'boolean' and 'int'", 39, 14);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'boolean', found 'int'", 41, 14);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testMatchGuardCodeAnalysisNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/matchstmt/match_guard_code_analysis_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, getInvalidRestFieldKeyError("y"), 25, 35);
        BAssertUtil.validateError(result, i++, getInvalidRestFieldKeyError("z"), 29, 29);
        BAssertUtil.validateError(result, i++, getInvalidRestFieldKeyError("y"), 29, 41);
        BAssertUtil.validateWarning(result, i++, "pattern will not be matched", 40, 9);
        BAssertUtil.validateError(result, i++, "incompatible types: 'Foo' will not be " +
                "matched to 'record {| string x; int i; anydata...; |}'", 40, 20);
        BAssertUtil.validateWarning(result, i++, "pattern will not be matched", 42, 9);
        BAssertUtil.validateError(result, i++, "incompatible types: 'Foo' will not be " +
                "matched to 'record {| string x; int i; anydata...; |}'", 42, 30);
        BAssertUtil.validateError(result, i++, "incompatible types: 'function () returns ()' will not be " +
                "matched to 'isolated function'", 51, 19);
        BAssertUtil.validateError(result, i++, "incompatible types: 'function (int,string) returns ()' " +
                "will not be matched to 'isolated function'", 60, 19);
        BAssertUtil.validateError(result, i++, "incompatible types: 'function (int,string) returns (int)' " +
                "will not be matched to 'isolated function'", 69, 19);
        BAssertUtil.validateError(result, i++, "incompatible types: 'function () returns (int)' will not be " +
                "matched to 'isolated function'", 78, 19);
        Assert.assertEquals(result.getWarnCount(), 2);
        Assert.assertEquals(result.getErrorCount(), i - 2);
    }

    private String getInvalidRestFieldKeyError(String key) {
        return "invalid key '" + key + "': identifiers cannot be used as rest field keys, expected a string literal " +
                "or an expression";
    }

    @Test
    public void testMatchGuardFunctionAndMethodCallAnalysis() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/matchstmt/match_guard_function_method_call_mutability_analysis.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testMatchGuardFunctionAndMethodCallNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/matchstmt/match_guard_function_method_call_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 49, 22);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 51, 22);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 51, 36);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 62, 34);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 62, 39);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 64, 27);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 66, 33);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 68, 37);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 70, 25);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 72, 25);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 134, 36);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 136, 22);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 152, 37);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 152, 42);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 154, 30);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 156, 36);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 158, 40);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 160, 19);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 162, 19);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 162, 60);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 219, 35);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 228, 29);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 228, 37);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 230, 23);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 237, 33);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 237, 38);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 237, 44);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 239, 20);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 239, 21);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 239, 29);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 241, 20);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 241, 21);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 241, 29);
        BAssertUtil.validateError(result, i++, NON_READ_ONLY_ARG_IN_MATCH_GUARD_CALL_ERROR, 243, 35);
        BAssertUtil.validateError(result, i++, NON_ISOLATED_CALL_IN_MATCH_GUARD_ERROR, 243, 35);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
