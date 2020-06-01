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
package io.ballerinalang.compiler.parser.test.syntax.types;

import org.testng.annotations.Test;

/**
 * Test parsing function type descriptors.
 * 
 * @since 1.3.0
 */
public class FunctionTypeTest extends AbstractTypesTest {

    @Test
    public void testFuncTypeWithNoReturns() {
        testFile("func-type/func_type_source_01.bal", "func-type/func_type_assert_01.json");
    }

    @Test
    public void testFuncTypeWithReturns() {
        testFile("func-type/func_type_source_02.bal", "func-type/func_type_assert_02.json");
    }

    @Test
    public void testFuncTypeUsagesInTypeDescContexts() {
        testFile("func-type/func_type_source_03.bal", "func-type/func_type_assert_03.json");
    }

    @Test
    public void testFuncTypeWithoutReturnsInParam() {
        testFile("func-type/func_type_source_04.bal", "func-type/func_type_assert_04.json");
    }

    // Recovery test

    @Test
    public void testRecoveryInFuncTypeWithNoReturns() {
        testFile("func-type/func_type_source_05.bal", "func-type/func_type_assert_05.json");
    }

    @Test
    public void testRecoveryInFuncTypeWithReturns() {
        testFile("func-type/func_type_source_06.bal", "func-type/func_type_assert_06.json");
    }

    @Test
    public void testRecoveryInFuncTypeUsagesInTypeDescContexts() {
        testFile("func-type/func_type_source_07.bal", "func-type/func_type_assert_07.json");
    }
}
