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
package io.ballerinalang.compiler.parser.test.syntax.declarations;

import org.testng.annotations.Test;

/**
 * Test parsing function definitions.
 */
public class FunctionDefinitionTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testSimpleFuncDef() {
        test("func-definition/func_def_source_01.bal", "func-definition/func_def_assert_01.json");
    }

    @Test
    public void testSimpleFuncDefWithReturns() {
        test("func-definition/func_def_source_02.bal", "func-definition/func_def_assert_02.json");
    }

    @Test
    public void testExternalFuncDef() {
        test("func-definition/func_def_source_08.bal", "func-definition/func_def_assert_08.json");
    }

    @Test
    public void testFunctionWithIsolatedQualifier() {
        test("func-definition/isolated_func_def_01.bal", "func-definition/isolated_func_def_assert_01.json");
        test("func-definition/isolated_func_def_02.bal", "func-definition/isolated_func_def_assert_02.json");
        test("func-definition/isolated_func_def_03.bal", "func-definition/isolated_func_def_assert_03.json");
        test("func-definition/isolated_func_def_04.bal", "func-definition/isolated_func_def_assert_04.json");
        test("func-definition/isolated_func_def_05.bal", "func-definition/isolated_func_def_assert_05.json");
        test("func-definition/isolated_func_def_06.bal", "func-definition/isolated_func_def_assert_06.json");
    }

    // Recovery tests

    @Test
    public void testFuncDefWithExtraFuncName() {
        test("func-definition/func_def_source_03.bal", "func-definition/func_def_assert_03.json");
    }

    @Test
    public void testFuncDefWithExtraFuncName2() {
        test("func-definition/func_def_source_18.bal", "func-definition/func_def_assert_18.json");
    }

    @Test
    public void testFuncDefWithMisplacedCloseBrace() {
        test("func-definition/func_def_source_04.bal", "func-definition/func_def_assert_04.json");
    }

    @Test
    public void testFuncDefWithoutModifier() {
        test("func-definition/func_def_source_05.bal", "func-definition/func_def_assert_05.json");
    }

    @Test
    public void testFuncDefWithMultipleReturnTypes() {
        test("func-definition/func_def_source_06.bal", "func-definition/func_def_assert_06.json");
    }

    @Test
    public void testFuncDefWithMultipleExtraTokens() {
        test("func-definition/func_def_source_07.bal", "func-definition/func_def_assert_07.json");
    }

    @Test
    public void testExternalFuncDefWithParenthesis() {
        testFile("func-definition/func_def_source_09.bal", "func-definition/func_def_assert_09.json");
    }

    @Test
    public void testExternalFuncDefWithMisingEqualAndSemicolon() {
        test("func-definition/func_def_source_10.bal", "func-definition/func_def_assert_10.json");
    }

    @Test
    public void testExternalFuncDefWithMisingReturnType() {
        test("func-definition/func_def_source_11.bal", "func-definition/func_def_assert_11.json");
    }

    @Test
    public void testFuncDefWithExtraParenthesis() {
        test("func-definition/func_def_source_12.bal", "func-definition/func_def_assert_12.json");
    }

    @Test
    public void testFuncDefWithMissingCloseBrace() {
        test("func-definition/func_def_source_13.bal", "func-definition/func_def_assert_13.json");
    }

    @Test
    public void testFuncDefWithMisplacedFuncName() {
        test("func-definition/func_def_source_14.bal", "func-definition/func_def_assert_14.json");
    }

    @Test
    public void testFuncDefWithMissingOpenBraceAndStatements() {
        test("func-definition/func_def_source_15.bal", "func-definition/func_def_assert_15.json");
    }

    @Test
    public void testFuncDefWithOnlyFunctionKeyword() {
        testFile("func-definition/func_def_source_16.bal", "func-definition/func_def_assert_16.json");
    }

    @Test
    public void testFuncDefWithMisingOpenParenAndOpenBrace() {
        test("func-definition/func_def_source_17.bal", "func-definition/func_def_assert_17.json");
    }

    @Test
    public void testFuncDefWithExtraIncompleteTokenAtEnd() {
        testFile("func-definition/func_def_source_19.bal", "func-definition/func_def_assert_19.json");
    }

    @Test
    public void testIncompleteFunctionBodyStatement() {
        testFile("func-definition/func_def_source_20.bal", "func-definition/func_def_assert_20.json");
    }
    
    @Test
    public void testOnlyPublicKeyword() {
        testFile("func-definition/func_def_source_21.bal", "func-definition/func_def_assert_21.json");
    }
    
    @Test
    public void testOnlyFunctionKeyword() {
        testFile("func-definition/func_def_source_22.bal", "func-definition/func_def_assert_22.json");
    }
    
    @Test
    public void testIncompleteExternalFunctionBodyRecovery() {
        testFile("func-definition/func_def_source_23.bal", "func-definition/func_def_assert_23.json");
    }

    @Test
    public void testFunctionWithInvalidExpressionStatement() {
        testFile("func-definition/func_def_source_24.bal", "func-definition/func_def_assert_24.json");
    }

    @Test
    public void testMissingFunctionNameWithQualifiers() {
        testFile("func-definition/func_def_source_26.bal", "func-definition/func_def_assert_26.json");
    }

    @Test
    public void testMissingTokensWithQualifiers() {
        testFile("func-definition/func_def_source_27.bal", "func-definition/func_def_assert_27.json");
    }

    @Test
    public void testTopLevelFunctionKeywordRecovery() {
        testFile("func-definition/func_def_source_28.bal", "func-definition/func_def_assert_28.json");
    }

    @Test
    public void testRecoveryWithinFuncDefWithOtherTopLvlConstructs() {
        testFile("func-definition/func_def_source_29.bal", "func-definition/func_def_assert_29.json");
        testFile("func-definition/func_def_source_30.bal", "func-definition/func_def_assert_30.json");
    }

    @Test
    public void testReturnTypeDescRecoveryInFunDef() {
        testFile("func-definition/func_def_source_31.bal", "func-definition/func_def_assert_31.json");
    }

    @Test
    public void testErrorsBeforeFunDef() {
        testFile("func-definition/func_def_source_32.bal", "func-definition/func_def_assert_32.json");
    }

    @Test
    public void testRecoveryWhenTypingExternal() {
        testFile("func-definition/func_def_source_33.bal", "func-definition/func_def_assert_33.json");
    }
}
