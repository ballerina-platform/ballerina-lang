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
package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains a set of test cases to verify the
 * behaviour of the match statement in Ballerina.
 *
 * @since 0.966.0
 */
public class MatchStatementTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/match_stmt_basic.bal");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStatementBasics1() {
        BValue[] returns = BRunUtil.invoke(result, "testMatchStatementBasics1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "boolean value received: true",
                "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of match statement")
    public void testMatchStmtNegative1() {
        CompileResult compile = BCompileUtil.compile("test-src/statements/matchstmt/match_stmt_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 4);

        BAssertUtil.validateError(compile, 0, "A matching pattern cannot be guaranteed for types '[boolean]'", 9, 31);
        BAssertUtil.validateError(compile, 1, "this function must return a result", 5, 1);
        BAssertUtil.validateError(compile, 2, "unreachable pattern: preceding patterns are " +
                "too general or the pattern ordering is not correct", 18, 9);
        BAssertUtil.validateError(compile, 3, "unreachable pattern: preceding patterns are " +
                "too general or the pattern ordering is not correct", 21, 9);

    }
}
