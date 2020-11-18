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
import org.ballerinalang.test.util.BRunUtil;
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
public class MatchStmtConstPatternTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/const-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/const-pattern-negative.bal");
    }

    @Test
    public void testConstPattern1() {
        BRunUtil.invoke(result, "testConstPattern1");
    }

    @Test
    public void testConstPattern2() {
        BRunUtil.invoke(result, "testConstPattern2");
    }

    @Test
    public void testConstPattern3() {
        BRunUtil.invoke(result, "testConstPattern3");
    }

    @Test
    public void testConstPattern4() {
        BRunUtil.invoke(result, "testConstPattern4");
    }

    @Test
    public void testConstPattern5() {
        BRunUtil.invoke(result, "testConstPattern5");
    }

    @Test
    public void testConstPattern6() {
        BRunUtil.invoke(result, "testConstPattern6");
    }

    @Test
    public void testConstPattern7() {
        BRunUtil.invoke(result, "testConstPattern7");
    }

    @Test
    public void testConstPattern8() {
        BRunUtil.invoke(result, "testConstPattern8");
    }

    @Test
    public void testConstPattern9() {
        BRunUtil.invoke(result, "testConstPattern9");
    }

    @Test
    public void testConstPattern10() {
        BRunUtil.invoke(result, "testConstPattern10");
    }

    @Test
    public void testConstPattern11() {
        BRunUtil.invoke(result, "testConstPattern11");
    }

    @Test
    public void testConstPattern12() {
        BRunUtil.invoke(result, "testConstPattern12");
    }

    @Test
    public void testConstPattern13() {
        BRunUtil.invoke(result, "testConstPattern13");
    }

    @Test(description = "Test pattern will not be matched")
    public void testConstPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 28);
        int i = -1;
        String patternNotMatched = "pattern will not be matched";

        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 27, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 28, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 29, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 34, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 36, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 37, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 38, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 43, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 44, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 45, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 46, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 52, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 53, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 54, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 56, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 62, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 64, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 65, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 70, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 71, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 72, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 73, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 78, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 79, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 80, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 94, 9);
        BAssertUtil.validateError(resultNegative, ++i, "variable 's' may not have been initialized", 107, 12);
        BAssertUtil.validateError(resultNegative, ++i, "variable 's' may not have been initialized", 125, 12);
    }
}
