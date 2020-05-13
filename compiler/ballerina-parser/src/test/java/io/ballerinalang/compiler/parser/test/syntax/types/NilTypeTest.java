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
 * Test parsing nil type.
 */
public class NilTypeTest extends AbstractTypesTest {

    //Valid source test
    @Test
    public void testValidNilTypeDeclaration () {
        test("() a;", "nil-type/nil_type_assert_01.json");
    }

    //Recovery source test

    @Test
    public void testInValidNilTypeWithoutIdentifier () {
        test("() ;", "nil-type/nil_type_assert_02.json");
    }

    @Test
    public void testInValidNilTypeWithoutCloseParentheses () {
        test("( a;", "nil-type/nil_type_assert_03.json");
    }

    @Test
    public void testInValidNilTypeWithExtraTokensInsideParentheses () {
        test("(t 4) a;", "nil-type/nil_type_assert_04.json");
    }

    @Test
    public void testInValidNilTypeWithExtraCloseParentheses () {
        test("()) a;", "nil-type/nil_type_assert_05.json");
    }

    @Test
    public void testInValidNilTypeWithMultipleExtraOpenParentheses () {
        test("(() a;", "nil-type/nil_type_assert_06.json");
    }
}
