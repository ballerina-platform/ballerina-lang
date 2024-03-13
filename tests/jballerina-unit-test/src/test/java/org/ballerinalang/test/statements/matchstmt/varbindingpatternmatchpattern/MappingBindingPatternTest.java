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
import org.testng.annotations.DataProvider;
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

    @Test(dataProvider = "functionsToTestMappingBindingPattern")
    public void testMappingBindingPattern(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] functionsToTestMappingBindingPattern() {
        return new Object[]{
                "testMappingBindingPattern1",
                "testMappingBindingPattern2",
                "testMappingBindingPattern3",
                "testMappingBindingPattern4",
                "testMappingBindingPattern5",
                "testMappingBindingPattern6",
                "testMappingBindingPattern7",
                "testMappingBindingPattern8",
                "testMappingBindingPattern9",
                "testMappingBindingPattern10",
                "testMappingBindingPattern11",
                "testMappingBindingPattern12",
                "testMappingBindingPattern13",
                "testMappingBindingPattern14",
                "testMappingBindingPattern15",
                "testMappingBindingPattern16",
                "testMappingBindingPattern17",
                "testMappingBindingToRecordWithDefaultValue"
        };
    }

    @Test(dataProvider = "functionsToTestMappingBindingPatternWithRest")
    public void testMappingBindingPatternWithRest(String functionName) {
        BRunUtil.invoke(restMatchPatternResult, functionName);
    }

    @DataProvider
    public Object[] functionsToTestMappingBindingPatternWithRest() {
        return new Object[]{
                "testMappingBindingPatternWithRest1",
                "testMappingBindingPatternWithRest2",
                "testMappingBindingPatternWithRest3",
                "testMappingBindingPatternWithRest4",
                "testMappingBindingPatternWithRest5",
                "testMappingBindingPatternWithRest6",
                "testMappingBindingPatternWithRest7",
                "testMappingBindingPatternWithRest8",
                "testRestMappingAtRuntime",
                "testRestRecordPattern",
                "testReachableMappingBinding",
                "testRestBindingPatternWithRecords"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
        restMatchPatternResult = null;
    }
}
