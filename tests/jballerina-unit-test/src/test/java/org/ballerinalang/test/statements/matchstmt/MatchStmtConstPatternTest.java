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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the const-pattern.
 *
 * @since 2.0.0
 */
public class MatchStmtConstPatternTest {

    private CompileResult result, resultNegative, resultNegativeSemantics;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/const-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/const-pattern-negative.bal");
        resultNegativeSemantics = BCompileUtil.compile("test-src/statements/matchstmt/const-pattern-negative" +
                "-semantics.bal");
    }

    @DataProvider
    public Object[] constPatternList() {
        return new Object[] {
                "testConstPattern1",
                "testConstPattern2",
                "testConstPattern3",
                "testConstPattern4",
                "testConstPattern5",
                "testConstPattern6",
                "testConstPattern7",
                "testConstPattern8",
                "testConstPattern9",
                "testConstPattern10",
                "testConstPattern11",
                "testConstPattern12",
                "testConstPattern13",
                "testConstPattern14",
                "testConstPattern15",
                "testConstPattern16",
                "testConstPattern17",
                "testConstPattern18",
                "testConstPattern19",
                "testConstPattern20",
                "testConstPatternWithNegativeLiteral",
                "testConstPatternWithPredeclaredPrefix"
        };
    }

    @Test(dataProvider = "constPatternList")
    public void testConstPatterns(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @Test
    public void testMatchStmtInsideForeach() {
        BRunUtil.invoke(result, "testMatchStmtInsideForeach");
    }

    @Test(description = "Test pattern will not be matched")
    public void testConstPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        Assert.assertEquals(resultNegative.getWarnCount(), 28);
        int i = -1;
        String patternNotMatched = "pattern will not be matched";

        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 27, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 28, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 29, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 34, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 36, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 37, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 38, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 44, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 45, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 46, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 52, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 53, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 54, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 56, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 62, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 64, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 65, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 70, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 71, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 72, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 73, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 78, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 79, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 80, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 94, 9);
        BAssertUtil.validateError(resultNegative, ++i, "variable 's' may not have been initialized", 107, 12);
        BAssertUtil.validateError(resultNegative, ++i, "variable 's' may not have been initialized", 125, 12);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 140, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 159, 9);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 170, 5);
        BAssertUtil.validateError(resultNegative, ++i, "this function must return a result", 185, 1);
        BAssertUtil.validateError(resultNegative, ++i, "this function must return a result", 208, 1);
    }

    @Test(description = "Test negative semantics")
    public void testConstPatternNegativeSemantics() {
        Assert.assertEquals(resultNegativeSemantics.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegativeSemantics, 0, "variable 'a' should be declared as constant", 22, 9);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
        resultNegativeSemantics = null;
    }
}
