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

    @Test
    public void testDefaultableParamWithInferredTypedescDefault() {
        test("func-definition/func_params_source_18.bal", "func-definition/func_params_assert_18.json");
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
    public void testIncompleteExprInDefaultableParams() {
        test("func-definition/func_params_source_06.bal", "func-definition/func_params_assert_06.json");
        testFile("func-definition/func_params_source_25.bal", "func-definition/func_params_assert_25.json");
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

    @Test
    public void testAsteriskWithKeyWord() {
        test("func-definition/func_params_source_15.bal", "func-definition/func_params_assert_15.json");
    }

    @Test
    public void testIncludedRecordParamWithExtraKeyWord() {
        test("func-definition/func_params_source_16.bal", "func-definition/func_params_assert_16.json");
    }

    @Test
    public void testIncludedRecordParamWithAnnotationAndExtraKeyWord() {
        test("func-definition/func_params_source_17.bal", "func-definition/func_params_assert_17.json");
    }

    @Test
    public void testDefaultableParamWithInferredTypedescDefaultMissingStartLT() {
        test("func-definition/func_params_source_19.bal", "func-definition/func_params_assert_19.json");
    }

    @Test
    public void testDefaultableParamWithInferredTypedescDefaultMissingEndGT() {
        test("func-definition/func_params_source_20.bal", "func-definition/func_params_assert_20.json");
    }

    @Test
    public void testDefaultableParamWithIncompleteCastWithAnnotation() {
        test("func-definition/func_params_source_21.bal", "func-definition/func_params_assert_21.json");
    }

    @Test
    public void testDefaultableParamWithInvalidToken() {
        test("func-definition/func_params_source_22.bal", "func-definition/func_params_assert_22.json");
        test("func-definition/func_params_source_23.bal", "func-definition/func_params_assert_23.json");
    }

    @Test
    public void testMissingEqualsTokenWithDefaultableParamWithInferredTypedescDefault() {
        test("func-definition/func_params_source_24.bal", "func-definition/func_params_assert_24.json");
    }

    @Test
    public void testIncludedRecordParamAsRestParam() {
        test("func-definition/func_params_source_26.bal", "func-definition/func_params_assert_26.json");
    }

    @Test
    public void testIncludedRecordParamAsRestParam2() {
        test("func-definition/func_params_source_27.bal", "func-definition/func_params_assert_27.json");
    }

    @Test
    public void testIncludedRecordParamAsRestParamInMethod() {
        test("func-definition/func_params_source_28.bal", "func-definition/func_params_assert_28.json");
    }

    @Test
    public void testIncludedRecordParamAsRestParamInAnonFuncExpr() {
        test("func-definition/func_params_source_29.bal", "func-definition/func_params_assert_29.json");
    }
}
