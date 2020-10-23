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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the error-match-pattern.
 *
 * @since Swan Lake
 */
@Test(groups = { "disableOnOldParser" })
public class MatchStmtErrorMatchPatternTest {
    private CompileResult result, restPatternResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/error-match-pattern.bal");
        restPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/error-match-pattern-with-rest-match" +
                "-pattern.bal");
    }

    @Test
    public void testErrorMatchPattern1() {
        BRunUtil.invoke(result, "testErrorMatchPattern1");
    }

    @Test
    public void testErrorMatchPattern2() {
        BRunUtil.invoke(result, "testErrorMatchPattern2");
    }

    @Test
    public void testErrorMatchPattern3() {
        BRunUtil.invoke(result, "testErrorMatchPattern3");
    }

    @Test
    public void testErrorMatchPattern4() {
        BRunUtil.invoke(result, "testErrorMatchPattern4");
    }

    @Test
    public void testErrorMatchPattern5() {
        BRunUtil.invoke(result, "testErrorMatchPattern5");
    }

    @Test
    public void testErrorMatchPattern6() {
        BRunUtil.invoke(result, "testErrorMatchPattern6");
    }

    @Test
    public void testErrorMatchPattern7() {
        BRunUtil.invoke(result, "testErrorMatchPattern7");
    }

    @Test
    public void testErrorMatchPattern8() {
        BRunUtil.invoke(result, "testErrorMatchPattern8");
    }

    @Test
    public void testErrorMatchPatternWithRestPattern1() {
        BRunUtil.invoke(restPatternResult, "testErrorMatchPattern1");
    }

    @Test
    public void testErrorMatchPatternWithRestPattern2() {
        BRunUtil.invoke(restPatternResult, "testErrorMatchPattern2");
    }

    @Test
    public void testErrorMatchPatternWithRestPattern3() {
        BRunUtil.invoke(restPatternResult, "testErrorMatchPattern3");
    }
}
