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
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing anonymous function expression.
 * 
 * @since 1.3.0
 */
public class AnonymousFuncExpressionTest extends AbstractExpressionsTest {

    // Valid source tests

    @Test
    public void testExplicitFuncWithEmptyBlockBody() {
        testFile("anon-func/anon_func_source_01.bal", "anon-func/anon_func_assert_01.json");
    }

    @Test
    public void testExplicitFuncWithExpressionBody() {
        testFile("anon-func/anon_func_source_02.bal", "anon-func/anon_func_assert_02.json");
    }

    @Test
    public void testOpPrecedenceForExpressionBody() {
        testFile("anon-func/anon_func_source_04.bal", "anon-func/anon_func_assert_04.json");
    }

    @Test
    public void testOpPrecedenceForSubsequentAnonFuncOperators() {
        testFile("anon-func/anon_func_source_11.bal", "anon-func/anon_func_assert_11.json");
    }

    @Test
    public void testSimpleImplicitAnonFunc() {
        testFile("anon-func/anon_func_source_07.bal", "anon-func/anon_func_assert_07.json");
    }

    @Test
    public void testImplicitAnonFuncWithInferedParamList() {
        testFile("anon-func/anon_func_source_09.bal", "anon-func/anon_func_assert_09.json");
    }

    // Recovery test

    @Test
    public void testErrorsInExplicitFuncWithEmptyBlockBody() {
        testFile("anon-func/anon_func_source_03.bal", "anon-func/anon_func_assert_03.json");
    }

    @Test
    public void testMissingExprInExpressionFuncBody() {
        testFile("anon-func/anon_func_source_05.bal", "anon-func/anon_func_assert_05.json");
    }

    @Test
    public void testMissingFuncBody() {
        testFile("anon-func/anon_func_source_06.bal", "anon-func/anon_func_assert_06.json");
    }

    @Test
    public void testSimpleImplicitAnonFuncRecovery() {
        testFile("anon-func/anon_func_source_08.bal", "anon-func/anon_func_assert_08.json");
    }

    @Test
    public void testRecoveryInImplicitAnonFuncWithInferedParamList() {
        testFile("anon-func/anon_func_source_10.bal", "anon-func/anon_func_assert_10.json");
    }
}
