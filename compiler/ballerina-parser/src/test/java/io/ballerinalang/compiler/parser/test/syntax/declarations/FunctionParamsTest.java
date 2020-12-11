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
 * Test parsing function definition parameters.
 */
public class FunctionParamsTest extends AbstractDeclarationTest {

    // Valid syntax tests

    @Test
    public void testFuncDefParams() {
        test("func-definition/func_params_source_01.bal", "func-definition/func_params_assert_01.json");
    }

    @Test
    public void testUserdefinedTypeOfParams() {
        test("func-definition/func_params_source_05.bal", "func-definition/func_params_assert_05.json");
    }

    @Test
    public void testParamsQualifiedNamedTyped() {
        test("func-definition/func_params_source_08.bal", "func-definition/func_params_assert_08.json");
    }
    
    // Recovery tests
    @Test
    public void testRequiredParamAfterDefaultableParam() {
        test("func-definition/func_params_source_02.bal", "func-definition/func_params_assert_02.json");
    }

    @Test
    public void testMissingCommaInParams() {
        test("func-definition/func_params_source_03.bal", "func-definition/func_params_assert_03.json");
    }

    @Test
    public void testIncompleteParams() {
        testFile("func-definition/func_params_source_04.bal", "func-definition/func_params_assert_04.json");
    }

    @Test
    public void testIncompleteBinarExprInDefaultableParams() {
        test("func-definition/func_params_source_06.bal", "func-definition/func_params_assert_06.json");
    }

    @Test
    public void testMoreParamsAfterRestParam() {
        test("func-definition/func_params_source_07.bal", "func-definition/func_params_assert_07.json");
    }

    @Test
    public void testIncludedRecordParam() {
        test("func-definition/func_params_source_09.bal", "func-definition/func_params_assert_09.json");
    }

    @Test
    public void testIncludedRecordParamWithDefaultValues() {
        test("func-definition/func_params_source_10.bal", "func-definition/func_params_assert_10.json");
    }

    @Test
    public void testIncludedRecordParamWithAdditionalTokenAfterAsterisk() {
        test("func-definition/func_params_source_11.bal", "func-definition/func_params_assert_11.json");
    }

    @Test
    public void testIncludedRecordParamWithAdditionalTokenBeforeAsterisk() {
        test("func-definition/func_params_source_12.bal", "func-definition/func_params_assert_12.json");
    }

    @Test
    public void testIncludedRecordParamWithMissingIdentifier() {
        test("func-definition/func_params_source_13.bal", "func-definition/func_params_assert_13.json");
    }

    @Test
    public void testIncludedRecordParamWithMissingIdentifierAndTypedesc() {
        test("func-definition/func_params_source_14.bal", "func-definition/func_params_assert_14.json");
    }
}
