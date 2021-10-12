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
public class MatchGuardCodeAnalysisNegativeTest {

    @Test
    public void testMatchGuardCodeAnalysisNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/matchstmt/match_guard_code_analysis_negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, getInvalidRestFieldKeyError("y"), 25, 35);
        BAssertUtil.validateError(result, i++, getInvalidRestFieldKeyError("z"), 29, 29);
        BAssertUtil.validateError(result, i++, getInvalidRestFieldKeyError("y"), 29, 41);
        BAssertUtil.validateWarning(result, i++, "pattern will not be matched", 40, 9);
        BAssertUtil.validateError(result, i++, "incompatible types: 'record {| int x; anydata...; |}' will not be " +
                "matched to 'record {| string x; int i; anydata...; |}'", 40, 20);
        BAssertUtil.validateWarning(result, i++, "pattern will not be matched", 42, 9);
        BAssertUtil.validateError(result, i++, "incompatible types: 'record {| int x; anydata...; |}' will not be " +
                "matched to 'record {| string x; int i; anydata...; |}'", 42, 30);
        Assert.assertEquals(result.getWarnCount(), 2);
        Assert.assertEquals(result.getErrorCount(), i - 2);
    }

    private String getInvalidRestFieldKeyError(String key) {
        return "invalid key '" + key + "': identifiers cannot be used as rest field keys, expected a string literal " +
                "or an expression";
    }
}
