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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the mapping-match-pattern.
 *
 * @since Swan Lake
 */
public class MatchStmtMappingMatchPatternTest {

    private CompileResult result, resultRestPattern;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/mapping-match-pattern.bal");
        resultRestPattern = BCompileUtil.compile("test-src/statements/matchstmt/mapping-match-pattern-with-rest-match" +
                "-pattern.bal");
    }

    @Test
    public void testMappingMatchPattern1() {
        BRunUtil.invoke(result, "testMappingMatchPattern1");
    }

    @Test
    public void testMappingMatchPattern2() {
        BRunUtil.invoke(result, "testMappingMatchPattern2");
    }

    @Test
    public void testMappingMatchPattern3() {
        BRunUtil.invoke(result, "testMappingMatchPattern3");
    }

    @Test
    public void testMappingMatchPattern4() {
        BRunUtil.invoke(result, "testMappingMatchPattern4");
    }

    @Test
    public void testMappingMatchPattern5() {
        BRunUtil.invoke(result, "testMappingMatchPattern5");
    }

    @Test
    public void testMappingMatchPattern6() {
        BRunUtil.invoke(result, "testMappingMatchPattern6");
    }

    @Test
    public void testMappingMatchPattern7() {
        BRunUtil.invoke(result, "testMappingMatchPattern7");
    }

    @Test
    public void testMappingMatchPattern8() {
        BRunUtil.invoke(result, "testMappingMatchPattern8");
    }

    @Test
    public void testMappingMatchPattern9() {
        BRunUtil.invoke(result, "testMappingMatchPattern9");
    }

    @Test
    public void testMappingMatchPattern10() {
        BRunUtil.invoke(result, "testMappingMatchPattern10");
    }

    @Test
    public void testMappingMatchPattern11() {
        BRunUtil.invoke(result, "testMappingMatchPattern11");
    }

    @Test
    public void testMappingMatchPattern12() {
        BRunUtil.invoke(result, "testMappingMatchPattern12");
    }

    @Test
    public void testMappingMatchPattern13() {
        BRunUtil.invoke(result, "testMappingMatchPattern13");
    }

    @Test
    public void testMappingMatchPattern14() {
        BRunUtil.invoke(result, "testMappingMatchPattern14");
    }

    @Test
    public void testMappingMatchPattern15() {
        BRunUtil.invoke(result, "testMappingMatchPattern15");
    }

    @Test
    public void testMappingMatchPattern16() {
        BRunUtil.invoke(result, "testMappingMatchPattern16");
    }

    @Test
    public void testMappingMatchPattern17() {
        BRunUtil.invoke(result, "testMappingMatchPattern17");
    }

    @Test
    public void testMappingMatchPattern18() {
        BRunUtil.invoke(result, "testMappingMatchPattern18");
    }

    @Test
    public void testMappingMatchPattern19() {
        BRunUtil.invoke(result, "testMappingMatchPattern19");
    }

    @Test
    public void testMappingMatchPattern20() {
        BRunUtil.invoke(result, "testMappingMatchPattern20");
    }

    @Test
    public void testMappingMatchPattern21() {
        BRunUtil.invoke(result, "testMappingMatchPattern21");
    }

    @Test
    public void testMappingMatchPattern22() {
        BRunUtil.invoke(result, "testMappingMatchPattern22");
    }

    @Test
    public void testMappingMatchPattern23() {
        BRunUtil.invoke(result, "testMappingMatchPattern23");
    }

    @Test
    public void testMappingMatchPattern24() {
        BRunUtil.invoke(result, "testMappingMatchPattern24");
    }

    @Test
    public void testMappingMatchPattern25() {
        BRunUtil.invoke(result, "testMappingMatchPattern25");
    }

    @Test
    public void testMappingMatchPatternWithWildCard() {
        BRunUtil.invoke(result, "testMappingMatchPatternWithWildCard");
    }

    @Test
    public void testMappingMatchPattern26() {
        BRunUtil.invoke(result, "testMappingMatchPattern26");
    }

    @Test
    public void testMappingBindingToRecordWithDefaultValue() {
        BRunUtil.invoke(result, "testMappingBindingToRecordWithDefaultValue");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern1() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern1");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern2() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern2");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern3() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern3");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern4() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern4");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern5() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern5");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern6() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern6");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern7() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern7");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern8() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPattern8");
    }

    @Test
    public void testMappingMatchPatternWithMapAndAnydataIntersection() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPatternWithMapAndAnydataIntersection");
    }

    @Test
    public void testMappingMatchPatternWithRestPattern11() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPatternWithRestPattern11");
    }

    @Test
    public void testMappingMatchPatternWithClosedRecord() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPatternWithClosedRecord");
    }

    @Test
    public void testMappingMatchPatternWithClosedRecordUnion() {
        BRunUtil.invoke(resultRestPattern, "testMappingMatchPatternWithClosedRecordUnion");
    }

    @Test
    public void testRestMappingAtRuntime() {
        BRunUtil.invoke(resultRestPattern, "testRestMappingAtRuntime");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultRestPattern = null;
    }
}
