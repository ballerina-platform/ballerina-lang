// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the const-pattern.
 *
 * @since Swan Lake
 */
@Test(groups = { "disableOnOldParser" })
public class MatchStatementSyntaxErrorsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/match-stmt-syntax-errors.bal");
    }

    @Test
    public void testSyntaxErrors() {
        Assert.assertEquals(result.getErrorCount(), 11);

        int i = -1;
        BAssertUtil.validateError(result, ++i, "undefined symbol 'v'", 5, 9);
        BAssertUtil.validateError(result, ++i, "missing open brace token", 6, 1);
        BAssertUtil.validateError(result, ++i, "missing right double arrow token", 6, 1);
        BAssertUtil.validateError(result, ++i, "missing close brace token", 8, 1);
        BAssertUtil.validateError(result, ++i, "match statement should have one or more match clauses", 12, 1);
        BAssertUtil.validateError(result, ++i, "missing open brace token", 12, 1);
        BAssertUtil.validateError(result, ++i, "missing close brace token", 13, 1);
        BAssertUtil.validateError(result, ++i, "match statement should have one or more match clauses", 17, 1);
        BAssertUtil.validateError(result, ++i, "missing identifier", 17, 1);
        BAssertUtil.validateError(result, ++i, "missing open brace token", 17, 1);
        BAssertUtil.validateError(result, ++i, "missing close brace token", 18, 1);
    }
}
