/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.parser;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.annotations.Test;

/**
 * Syntax Errors test class for ballerina filers.
 * This class test error handling for violations of grammar.
 */
public class InvalidSyntaxParserTest {

    /**
     * Test missing expected syntax.
     */

    @Test
    public void testParseSemicolonMissingSerivce() {
        CompileResult result = BTestUtils.compile("test-src/parser/semicolon-missing-service-negative.bal");
        BTestUtils.validateError(result, 0, "mismatched input 'reply'. expecting ';'", 10, 7);
    }

    @Test
    public void testParseSemicolonMissingMainFunc() {
        CompileResult result = BTestUtils.compile("test-src/parser/semicolon-missing-func-negative.bal");
        BTestUtils.validateError(result, 0, "mismatched input 'reply'. expecting ';'", 5, 2);
    }

    /**
     * Test invalid identifier. i.e: {@link org.antlr.v4.runtime.NoViableAltException}
     */

    @Test
    public void testParseIdentifierMissingSerivce() {
        CompileResult result = BTestUtils.compile("test-src/parser/identifier-missing-service-negative.bal");
        BTestUtils.validateError(result, 0, "invalid token ';'", 10, 10);
    }

    @Test
    public void testParseIdentifierMissingMainFunc() {
        CompileResult result = BTestUtils.compile("test-src/parser/identifier-missing-func-negative.bal");
        BTestUtils.validateError(result, 0, "invalid token ';'", 3, 5);
    }

    @Test
    public void testReservedWordVariable() {
        CompileResult result = BTestUtils.compile("test-src/parser/reserved-word-variable-negative.bal");
        BTestUtils.validateError(result, 0, "invalid token 'resource'", 3, 9);
    }

    /**
     * Test unwanted token.
     */

    @Test
    public void testServiceWithoutResourceName() {
        CompileResult result = BTestUtils.compile("test-src/parser/service-without-resource-name-negative.bal");
        BTestUtils.validateError(result, 0, "mismatched input '{'. expecting Identifier", 6, 12);
        BTestUtils.validateError(result, 1, "mismatched input ';'. expecting {'[', Identifier}", 8, 14);
    }

    @Test
    public void testParseMainFuncWithoutName() {
        CompileResult result = BTestUtils.compile("test-src/parser/func-without-name-negative.bal");
        BTestUtils.validateError(result, 0, "invalid token '{'", 1, 10);
        BTestUtils.validateError(result, 1, "mismatched input ';'. expecting {'[', Identifier}", 3, 9);
    }

    /**
     * Test mismatched input. i.e. {@link org.antlr.v4.runtime.InputMismatchException}
     */

    @Test
    public void testServiceWithoutResourceParams() {
        CompileResult result = BTestUtils.compile("test-src/parser/service-without-resource-params-negative.bal");
        BTestUtils.validateError(result, 0, "mismatched input '{'. expecting '('", 6, 18);
        BTestUtils.validateError(result, 1, "mismatched input ';'. expecting {'[', Identifier}", 8, 14);
    }

    @Test
    public void testParseMainFuncWithoutParams() {
        CompileResult result = BTestUtils.compile("test-src/parser/func-without-params-negative.bal");
        BTestUtils.validateError(result, 0, "mismatched input '{'. expecting '('", 1, 15);
    }

    @Test
    public void testResourceWithEmptyReply() {
        CompileResult result = BTestUtils.compile("test-src/parser/resource-with-empty-reply-negative.bal");
        BTestUtils.validateError(result, 0, "invalid token 'reply'", 9, 5);
    }

}
