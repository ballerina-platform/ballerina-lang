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
 * Test parsing byte array literal.
 */
public class ByteArrayLiteralTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testBase16Literal() {
        test("base16 ``", "byte-array-literal/base16_literal_assert_01.json");
        test("base16 `5A`", "byte-array-literal/base16_literal_assert_02.json");
        test("base16 ` 5 A `", "byte-array-literal/base16_literal_assert_03.json");
        test("base16 ` 5A B C3 4 `", "byte-array-literal/base16_literal_assert_04.json");
    }

    @Test
    public void testBase64Literal() {
        test("base64 ``", "byte-array-literal/base64_literal_assert_01.json");
        test("base64 `az09`", "byte-array-literal/base64_literal_assert_02.json");
        test("base64 `az092B+/`", "byte-array-literal/base64_literal_assert_03.json");
        test("base64 ` a z 0 92 B+ / `", "byte-array-literal/base64_literal_assert_04.json");
        test("base64 `Awszlq2rtys=`", "byte-array-literal/base64_literal_assert_05.json");
        test("base64 `Awszlq2rty==`", "byte-array-literal/base64_literal_assert_06.json");
        test("base64 ` Aws zl q 2rt y= =`", "byte-array-literal/base64_literal_assert_07.json");
    }

    // Recovery tests
}
