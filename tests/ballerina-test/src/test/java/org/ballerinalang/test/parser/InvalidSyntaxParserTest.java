/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
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
        CompileResult result = BCompileUtil.compile("test-src/parser/semicolon-missing-service-negative.bal");
        BAssertUtil.validateError(result, 0, "missing token ';' before 'return'", 10, 7);
    }

    @Test
    public void testParseSemicolonMissingMainFunc() {
        CompileResult result = BCompileUtil.compile("test-src/parser/semicolon-missing-func-negative.bal");
        BAssertUtil.validateError(result, 0, "missing token ';' before 'return'", 5, 2);
    }

    /**
     * Test invalid identifier. i.e: {@link org.antlr.v4.runtime.NoViableAltException}
     */

    @Test
    public void testReservedWordVariable() {
        CompileResult result = BCompileUtil.compile("test-src/parser/reserved-word-variable-negative.bal");
        BAssertUtil.validateError(result, 0, "invalid token 'resource'", 3, 9);
    }

    /**
     * Test unwanted token.
     */

    @Test
    public void testServiceWithoutResourceName() {
        CompileResult result = BCompileUtil.compile("test-src/parser/service-without-resource-name-negative.bal");
        BAssertUtil.validateError(result, 0, "invalid token 'endpoint'", 6, 6);
        BAssertUtil.validateError(result, 3, "extraneous input ';'", 8, 7);
    }

    @Test
    public void testParseMainFuncWithoutName() {
        CompileResult result = BCompileUtil.compile("test-src/parser/func-without-name-negative.bal");
        BAssertUtil.validateError(result, 0, "mismatched input '{'. expecting {'[', '?', '|', Identifier}", 1, 30);
        BAssertUtil.validateError(result, 1, "mismatched input ';'. expecting {'[', '?', '|', Identifier}", 3, 6);
    }

    /**
     * Test mismatched input. i.e. {@link org.antlr.v4.runtime.InputMismatchException}
     */

    @Test
    public void testServiceWithoutResourceParams() {
        CompileResult result = BCompileUtil.compile("test-src/parser/service-without-resource-params-negative.bal");
        BAssertUtil.validateError(result, 0, "missing token '(' before '{'", 9, 11);
        BAssertUtil.validateError(result, 1, "extraneous input 'return'", 11, 9);
    }

    @Test
    public void testParseMainFuncWithoutParams() {
        CompileResult result = BCompileUtil.compile("test-src/parser/func-without-params-negative.bal");
        BAssertUtil.validateError(result, 0, "invalid token '{'", 1, 15);
    }

    @Test
    public void testResourceWithReply() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resource-with-reply-negative.bal");
        BAssertUtil.validateError(result, 0, "undefined symbol 'reply'", 6, 5);
    }

    // token recognition.

    public void testTokenRecognition() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resource-with-empty-reply-negative.bal");
        BAssertUtil.validateError(result, 0, "token recognition error at: '\\'", 3, 5);
        BAssertUtil.validateError(result, 0, "token recognition error at: '*'", 4, 15);
    }
}
