/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test parsing scenarios with ambiguity.
 * 
 * @since 2.0.0
 */
public class AmbiguityResolutionTest extends AbstractMiscTest {

    @Test
    public void testAmbiguousListAtStmtStart() {
        testFile("ambiguity/ambiguity_source_01.bal", "ambiguity/ambiguity_assert_01.json");
    }

    @Test
    public void testAmbiguousListWithAmbiguousMapInside() {
        testFile("ambiguity/ambiguity_source_02.bal", "ambiguity/ambiguity_assert_02.json");
    }

    @Test
    public void testMostlyAmbiguousListOnLHS() {
        testFile("ambiguity/ambiguity_source_03.bal", "ambiguity/ambiguity_assert_03.json");
    }

    @Test
    public void testAmbiguousMapping() {
        testFile("ambiguity/ambiguity_source_04.bal", "ambiguity/ambiguity_assert_04.json");
    }

    @Test
    public void testOpenBracketFollowedByStmtStartOpenBrace() {
        testFile("ambiguity/ambiguity_source_05.bal", "ambiguity/ambiguity_assert_05.json");
    }

    @Test
    public void testAmbiguousMappingAtStmtStart() {
        testFile("ambiguity/ambiguity_source_07.bal", "ambiguity/ambiguity_assert_07.json");
    }

    @Test
    public void testAmbiguousMappingWithQualifiedNameAtStmtStart_1() {
        testFile("ambiguity/ambiguity_source_10.bal", "ambiguity/ambiguity_assert_10.json");
    }

    @Test
    public void testAmbiguousMappingWithQualifiedNameAtStmtStart_2() {
        testFile("ambiguity/ambiguity_source_11.bal", "ambiguity/ambiguity_assert_11.json");
    }

    @Test
    public void testStatementStartWithUnionOfAbogiousNodes() {
        testFile("ambiguity/ambiguity_source_12.bal", "ambiguity/ambiguity_assert_12.json");
    }

    @Test
    public void testStatementStartWithParenthesis() {
        testFile("ambiguity/ambiguity_source_13.bal", "ambiguity/ambiguity_assert_13.json");
    }

    @Test
    public void testStatementStartWithFunctionKeyword() {
        testFile("ambiguity/ambiguity_source_14.bal", "ambiguity/ambiguity_assert_14.json");
    }

    @Test
    public void testStatementStartWithBasicLiteral() {
        testFile("ambiguity/ambiguity_source_15.bal", "ambiguity/ambiguity_assert_15.json");
    }

    @Test
    public void testStatementStartWithImplicitFunParam() {
        testFile("ambiguity/ambiguity_source_17.bal", "ambiguity/ambiguity_assert_17.json");
    }

    @Test
    public void testStmtStartsWithTwoAmbiguousLists() {
        testFile("ambiguity/ambiguity_source_20.bal", "ambiguity/ambiguity_assert_20.json");
    }

    @Test
    public void testStmtStartsBracketedListsWithStringKeys() {
        testFile("ambiguity/ambiguity_source_21.bal", "ambiguity/ambiguity_assert_21.json");
    }

    // Recovery tests

    @Test
    public void testInvalidExprInStmtStartMapping() {
        testFile("ambiguity/ambiguity_source_06.bal", "ambiguity/ambiguity_assert_06.json");
    }

    @Test
    public void testStmtStartMappingRecovery() {
        testFile("ambiguity/ambiguity_source_08.bal", "ambiguity/ambiguity_assert_08.json");
    }

    @Test
    public void testInvalidTokenAfterColonInStmtStartMappingField() {
        testFile("ambiguity/ambiguity_source_09.bal", "ambiguity/ambiguity_assert_09.json");
    }

    @Test
    public void testStatementStartWithFunctionKeywordRecovery() {
        testFile("ambiguity/ambiguity_source_16.bal", "ambiguity/ambiguity_assert_16.json");
    }

    @Test
    public void testStatementStartWithParenthesisRecovery() {
        testFile("ambiguity/ambiguity_source_18.bal", "ambiguity/ambiguity_assert_18.json");
    }
}
