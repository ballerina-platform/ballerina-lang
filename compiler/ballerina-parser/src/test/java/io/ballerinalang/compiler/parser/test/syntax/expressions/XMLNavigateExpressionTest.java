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
 * Test parsing xml navigate expression.
 */
public class XMLNavigateExpressionTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testSimpleXMLFilter() {
        test("a.<*>", "xml-navigate-expr/xml_filter_expr_assert_01.json");
        test("a.<b:c>", "xml-navigate-expr/xml_filter_expr_assert_02.json");
        test("a.<b:*>", "xml-navigate-expr/xml_filter_expr_assert_03.json");
    }

    @Test
    public void testXMLFilterWithMoreThanOneNamePattern() {
        test("a.<*|*>", "xml-navigate-expr/xml_filter_expr_assert_04.json");
        test("a.<b:c|d:*|*>", "xml-navigate-expr/xml_filter_expr_assert_05.json");
    }

    @Test
    public void testXMLFilter() {
        test("x + a.<*|*>.<b:c|*|d:*>.<*> + y", "xml-navigate-expr/xml_filter_expr_assert_06.json");
    }

    @Test
    public void testSimpleXMLStep() {
        test("a/*", "xml-navigate-expr/xml_step_expr_assert_01.json");
        test("a/<b:c|d:*|*>", "xml-navigate-expr/xml_step_expr_assert_02.json");
        test("a/**/<b:c|d:*|*>", "xml-navigate-expr/xml_step_expr_assert_03.json");
    }

    @Test
    public void testXMLStepWithOneStepExtend() {
        test("a /* .<b:c|*|d:*>", "xml-navigate-expr/xml_step_expr_assert_04.json");
        test("a /* [b]", "xml-navigate-expr/xml_step_expr_assert_05.json");
        test("a /* .b(5, a, age = 18, ...subjects)", "xml-navigate-expr/xml_step_expr_assert_06.json");
        test("a /<*> .<b:c|*|d:*>", "xml-navigate-expr/xml_step_expr_assert_07.json");
        test("a /<*> [b]", "xml-navigate-expr/xml_step_expr_assert_08.json");
        test("a /<*> .b(5, a, age = 18, ...subjects)", "xml-navigate-expr/xml_step_expr_assert_09.json");
        test("a /**/<*> .<b:c|*|d:*>", "xml-navigate-expr/xml_step_expr_assert_10.json");
        test("a /**/<*> [b]", "xml-navigate-expr/xml_step_expr_assert_11.json");
        test("a /**/<*> .b(5, a, age = 18, ...subjects)", "xml-navigate-expr/xml_step_expr_assert_12.json");
    }

    @Test
    public void testSimpleXMLStepWithMoreThanOneStepExtend() {
        test("a /* .<b:c|*|d:*> [e] .b(5, a, age = 18, ...subjects)", "xml-navigate-expr/xml_step_expr_assert_13.json");
        test("a /<b:c> [d>e] [f] .<*>", "xml-navigate-expr/xml_step_expr_assert_14.json");
        test("a /**/<*> .foo(5, a, age = 18, ...subjects) .<b:c> .<d:*>",
                "xml-navigate-expr/xml_step_expr_assert_15.json");
    }

    // Recovery tests

    @Test
    public void testXMLNavigateWithMissingNamePattern() {
        test("a .<>", "xml-navigate-expr/xml_filter_expr_assert_07.json");
        test("a /<>", "xml-navigate-expr/xml_step_expr_assert_16.json");
        test("a /**/<>", "xml-navigate-expr/xml_step_expr_assert_17.json");
    }

    @Test
    public void testXMLNavigateWithMissingGtToken() {
        test("a .<*", "xml-navigate-expr/xml_filter_expr_assert_08.json");
        test("a .<b:c", "xml-navigate-expr/xml_filter_expr_assert_09.json");
        test("a .<b:*", "xml-navigate-expr/xml_filter_expr_assert_10.json");
        test("a /<*", "xml-navigate-expr/xml_step_expr_assert_18.json");
        test("a /**/<*", "xml-navigate-expr/xml_step_expr_assert_19.json");
    }

    @Test
    public void testXMLNavigateWithMissingPipeTokens() {
        test("a .<* * *>", "xml-navigate-expr/xml_filter_expr_assert_11.json");
        test("a .<b c:d *>", "xml-navigate-expr/xml_filter_expr_assert_12.json");
        test("a /<* * *>", "xml-navigate-expr/xml_step_expr_assert_20.json");
        test("a /**/<* * *>", "xml-navigate-expr/xml_step_expr_assert_21.json");
    }

    @Test
    public void testXMLNavigateWithMissingAtomicNamePattern() {
        test("a .< | >", "xml-navigate-expr/xml_filter_expr_assert_13.json");
        test("a .< :b | :c >", "xml-navigate-expr/xml_filter_expr_assert_14.json");
        test("a /< | >", "xml-navigate-expr/xml_step_expr_assert_22.json");
        test("a /**/< | >", "xml-navigate-expr/xml_step_expr_assert_23.json");
    }

    @Test
    public void testXMLNavigateWithExtraTokenInAtomicNamePattern() {
        test("a .< key * >", "xml-navigate-expr/xml_filter_expr_assert_15.json");
        test("a /< * | key b:c >", "xml-navigate-expr/xml_step_expr_assert_24.json");
        test("a /**/< b key :c >", "xml-navigate-expr/xml_step_expr_assert_25.json");
    }
}
