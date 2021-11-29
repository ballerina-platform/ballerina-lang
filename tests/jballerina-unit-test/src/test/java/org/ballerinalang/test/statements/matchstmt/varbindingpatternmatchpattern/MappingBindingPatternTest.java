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
package org.ballerinalang.test.statements.matchstmt.varbindingpatternmatchpattern;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the var binding-pattern mapping-binding-pattern.
 *
 * @since 2.0.0
 */
@Test
public class MappingBindingPatternTest {
    private CompileResult result, restMatchPatternResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping_binding_pattern.bal");
        restMatchPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping_binding_pattern_with_rest_binding_pattern.bal");
    }

    @Test
    public void testMappingBindingPattern1() {
        BRunUtil.invoke(result, "testMappingBindingPattern1");
    }

    @Test
    public void testMappingBindingPattern2() {
        BRunUtil.invoke(result, "testMappingBindingPattern2");
    }

    @Test
    public void testMappingBindingPattern3() {
        BRunUtil.invoke(result, "testMappingBindingPattern3");
    }

    @Test
    public void testMappingBindingPattern4() {
        BRunUtil.invoke(result, "testMappingBindingPattern4");
    }

    @Test
    public void testMappingBindingPattern5() {
        BRunUtil.invoke(result, "testMappingBindingPattern5");
    }

    @Test
    public void testMappingBindingPattern6() {
        BRunUtil.invoke(result, "testMappingBindingPattern6");
    }

    @Test
    public void testMappingBindingPattern7() {
        BRunUtil.invoke(result, "testMappingBindingPattern7");
    }

    @Test
    public void testMappingBindingPattern8() {
        BRunUtil.invoke(result, "testMappingBindingPattern8");
    }

    @Test
    public void testMappingBindingPattern9() {
        BRunUtil.invoke(result, "testMappingBindingPattern9");
    }

    @Test
    public void testMappingBindingPattern10() {
        BRunUtil.invoke(result, "testMappingBindingPattern10");
    }

    @Test
    public void testMappingBindingPattern11() {
        BRunUtil.invoke(result, "testMappingBindingPattern11");
    }

    @Test
    public void testMappingBindingPattern12() {
        BRunUtil.invoke(result, "testMappingBindingPattern12");
    }

    @Test
    public void testMappingBindingPattern13() {
        BRunUtil.invoke(result, "testMappingBindingPattern13");
    }

    @Test
    public void testMappingBindingPattern14() {
        BRunUtil.invoke(result, "testMappingBindingPattern14");
    }

    @Test
    public void testMappingBindingPattern15() {
        BRunUtil.invoke(result, "testMappingBindingPattern15");
    }

    @Test
    public void testMappingBindingPattern16() {
        BRunUtil.invoke(result, "testMappingBindingPattern16");
    }

    @Test
    public void testMappingBindingPattern17() {
        BRunUtil.invoke(result, "testMappingBindingPattern17");
    }

    @Test
    public void testMappingBindingToRecordWithDefaultValue() {
        BRunUtil.invoke(result, "testMappingBindingToRecordWithDefaultValue");
    }

    @Test
    public void testMappingBindingPatternWithRest1() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest1");
    }

    @Test
    public void testMappingBindingPatternWithRest2() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest2");
    }

    @Test
    public void testMappingBindingPatternWithRest3() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest3");
    }

    @Test
    public void testMappingBindingPatternWithRest4() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest4");
    }

    @Test
    public void testMappingBindingPatternWithRest5() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest5");
    }

    @Test
    public void testMappingBindingPatternWithRest6() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest6");
    }

    @Test
    public void testMappingBindingPatternWithRest7() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest7");
    }

    @Test
    public void testMappingBindingPatternWithRest8() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest8");
    }

    @Test
    public void testRestMappingAtRuntime() {
        BRunUtil.invoke(restMatchPatternResult, "testRestMappingAtRuntime");
    }

    @Test
    public void testRestRecordPattern() {
        BRunUtil.invoke(restMatchPatternResult, "testRestRecordPattern");
    }

    @Test
    public void testReachableMappingBinding() {
        BRunUtil.invoke(restMatchPatternResult, "testReachableMappingBinding");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        restMatchPatternResult = null;
    }
}
