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
package io.ballerinalang.compiler.parser.test.syntax.statements;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test parsing do-statements.
 */
public class DoStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testEmptyDo() {
        testFile("do-stmt/do_stmt_source_01.bal", "do-stmt/do_stmt_assert_01.json");
    }

    @Test
    public void testDoWithBody() {
        testFile("do-stmt/do_stmt_source_02.bal", "do-stmt/do_stmt_assert_02.json");
    }

    @Test
    public void testDoWithOnFailClause() {
        testFile("do-stmt/do_stmt_source_03.bal", "do-stmt/do_stmt_assert_03.json");
    }

    @Test
    public void testDoWithOnFailClauseWithoutVariable() {
        testFile("do-stmt/do_stmt_source_09.bal", "do-stmt/do_stmt_assert_09.json");
    }

    @Test
    public void testDoWithOnFailClauseHavingErrorBPWithVar() {
        testFile("do-stmt/do_stmt_source_10.bal", "do-stmt/do_stmt_assert_10.json");
    }

    @Test
    public void testDoWithOnFailClauseHavingErrorBPWithType() {
        testFile("do-stmt/do_stmt_source_11.bal", "do-stmt/do_stmt_assert_11.json");
    }

    @Test
    public void testDoWithOnFailClauseHavingErrorBPWithUserDefinedError() {
        testFile("do-stmt/do_stmt_source_12.bal", "do-stmt/do_stmt_assert_12.json");

    }

    @Test
    public void testDoWithOnFailClauseHavingErrorBPWithUserDefinedErrorWithVar() {
        testFile("do-stmt/do_stmt_source_13.bal", "do-stmt/do_stmt_assert_13.json");
    }

    @DataProvider(name = "onFailClauseOtherBPTestDataProvider")
    public Object[][] onFailClauseOtherBPTestDataProvider() {
        return new Object[][]{
                {"do-stmt/do_stmt_source_18.bal", "do-stmt/do_stmt_assert_18.json"},
                {"do-stmt/do_stmt_source_19.bal", "do-stmt/do_stmt_assert_19.json"},
                {"do-stmt/do_stmt_source_20.bal", "do-stmt/do_stmt_assert_20.json"},
                {"do-stmt/do_stmt_source_21.bal", "do-stmt/do_stmt_assert_21.json"}
        };
    }

    @Test(dataProvider = "onFailClauseOtherBPTestDataProvider")
    public void testDoOnFailClauseWithOtherBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "onFailClauseErrorBPWithFieldBPTestDataProvider")
    public Object[][] onFailClauseErrorBPWithFieldBPTestDataProvider() {
        return new Object[][]{
                {"do-stmt/do_stmt_source_22.bal", "do-stmt/do_stmt_assert_22.json"},
                {"do-stmt/do_stmt_source_23.bal", "do-stmt/do_stmt_assert_23.json"},
                {"do-stmt/do_stmt_source_24.bal", "do-stmt/do_stmt_assert_24.json"}
        };
    }

    @Test(dataProvider = "onFailClauseErrorBPWithFieldBPTestDataProvider")
    public void testDoOnFailClausHavingErrorBPWithFieldBP(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @DataProvider(name = "onFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public Object[][] onFailClauseErrorBPWithOtherTypeDescTestDataProvider() {
        return new Object[][]{
                {"do-stmt/do_stmt_source_25.bal", "do-stmt/do_stmt_assert_25.json"},
                {"do-stmt/do_stmt_source_26.bal", "do-stmt/do_stmt_assert_26.json"},
                {"do-stmt/do_stmt_source_27.bal", "do-stmt/do_stmt_assert_27.json"}
        };
    }

    @Test(dataProvider = "onFailClauseErrorBPWithOtherTypeDescTestDataProvider")
    public void testDoOnFailClausHavingErrorBPWithOtherTypeDesc(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    // Recovery test

    @Test
    public void testDoWithMissingOpenCloseBraces() {
        testFile("do-stmt/do_stmt_source_04.bal", "do-stmt/do_stmt_assert_04.json");
        testFile("do-stmt/do_stmt_source_05.bal", "do-stmt/do_stmt_assert_05.json");
    }

    @DataProvider(name = "onFailClauseRecoveryTestDataProvider")
    public Object[][] onFailClauseRecoveryTestDataProvider() {
        return new Object[][]{
                {"do-stmt/do_stmt_source_06.bal", "do-stmt/do_stmt_assert_06.json"},
                {"do-stmt/do_stmt_source_14.bal", "do-stmt/do_stmt_assert_14.json"},
                {"do-stmt/do_stmt_source_15.bal", "do-stmt/do_stmt_assert_15.json"},
                {"do-stmt/do_stmt_source_16.bal", "do-stmt/do_stmt_assert_16.json"},
                {"do-stmt/do_stmt_source_17.bal", "do-stmt/do_stmt_assert_17.json"},
                {"do-stmt/do_stmt_source_28.bal", "do-stmt/do_stmt_assert_28.json"}
        };
    }

    @Test(dataProvider = "onFailClauseRecoveryTestDataProvider")
    public void testDoOnFailClauseRecovery(String sourceFile, String assertFile) {
        testFile(sourceFile, assertFile);
    }

    @Test
    public void testDoWithMissingDoKeyword() {
        testFile("do-stmt/do_stmt_source_07.bal", "do-stmt/do_stmt_assert_07.json");
        testFile("do-stmt/do_stmt_source_08.bal", "do-stmt/do_stmt_assert_08.json");
    }
}
