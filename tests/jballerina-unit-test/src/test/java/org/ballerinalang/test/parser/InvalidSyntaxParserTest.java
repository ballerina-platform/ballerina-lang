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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Syntax Errors test class for ballerina filers.
 * This class test error handling for violations of grammar.
 */
public class InvalidSyntaxParserTest {

    /**
     * Test missing expected syntax.
     */

    @Test
    public void testParseSemicolonMissingService() {
        CompileResult result = BCompileUtil.compile("test-src/parser/semicolon-missing-service-negative.bal");
        BAssertUtil.validateError(result, 0, "invalid token 'return'", 10, 7);
    }

    @Test
    public void testParseSemicolonMissingMainFunc() {
        CompileResult result = BCompileUtil.compile("test-src/parser/semicolon-missing-func-negative.bal");
        BAssertUtil.validateError(result, 0, "invalid token 'return'", 5, 2);
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
        BAssertUtil.validateError(result, 0,
                                  "mismatched input '('. expecting {'public', 'private', 'resource', 'function', " +
                                          "'remote', 'transactional', '}', '@', DocumentationLineStart}", 6, 3);
        BAssertUtil.validateError(result, 1, "mismatched input 'caller'. expecting {')', '[', '?', '&', '|'}", 6, 16);
        BAssertUtil.validateError(result, 2, "mismatched input ','. expecting ';'", 6, 22);
        BAssertUtil.validateError(result, 3, "mismatched input ')'. expecting ';'", 6, 44);
    }

    @Test
    public void testParseMainFuncWithoutName() {
        CompileResult result = BCompileUtil.compile("test-src/parser/func-without-name-negative.bal");
        BAssertUtil.validateError(result, 0, "mismatched input '{'. expecting {'[', '?', '&', '|', Identifier}", 1, 30);
        BAssertUtil.validateError(result, 1, "mismatched input ';'. expecting {'[', '?', '&', '|', Identifier}", 3, 6);
    }

    /**
     * Test mismatched input. i.e. {@link org.antlr.v4.runtime.InputMismatchException}
     */

    @Test
    public void testServiceWithoutResourceParams() {
        CompileResult result = BCompileUtil.compile("test-src/parser/service-without-resource-params-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "extraneous input '{'", 5, 26);
        BAssertUtil.validateError(result, index++, "mismatched input ':'. expecting ';'", 6, 16);
        BAssertUtil.validateError(result, index++, "mismatched input '\"GET\"'. expecting {'service', 'function', " +
                "'object', 'record', 'abstract', 'client', 'distinct', 'int', 'byte', 'float', 'decimal', 'boolean', " +
                "'string', 'error', 'map', 'json', 'xml', 'table', 'stream', 'any', 'typedesc', 'future', 'anydata', " +
                "'handle', 'readonly', 'never', '(', '[', Identifier}", 6, 18);
        BAssertUtil.validateError(result, index++, "mismatched input ':'. expecting ';'", 7, 13);
        BAssertUtil.validateError(result, index++, "invalid token '{'", 9, 29);
        BAssertUtil.validateError(result, index++, "mismatched input '{'. expecting '('", 9, 29);
        BAssertUtil.validateError(result, index++, "extraneous input '}'", 12, 1);
        assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testParseMainFuncWithoutParams() {
        CompileResult result = BCompileUtil.compile("test-src/parser/func-without-params-negative.bal");
        BAssertUtil.validateError(result, 0, "mismatched input '{'. expecting '('", 1, 15);
    }

    @Test
    public void testObjectAttachedFunctionWithInvalidSyntax() {
        CompileResult result = BCompileUtil.compile("test-src/parser/object-attached-func-with-invalid-syntax.bal");
        BAssertUtil.validateError(result, 0, "mismatched input '.'. expecting '('", 6, 17);
        BAssertUtil.validateError(result, 1, "mismatched input '('. expecting {'[', '?', '&', '|', Identifier}", 6, 21);
        BAssertUtil.validateError(result, 2, "extraneous input 'a'", 6, 26);
        BAssertUtil.validateError(result, 3, "mismatched input 'returns'. expecting {'[', '?', '&', '|', Identifier}",
                6, 29);
        BAssertUtil.validateError(result, 4, "extraneous input '}'", 8, 1);
    }

//    @Test
//    public void testResourceWithReply() {
//        CompileResult result = BCompileUtil.compile("test-src/parser/resource-with-reply-negative.bal");
//        BAssertUtil.validateError(result, 0, "undefined symbol 'reply'", 6, 5);
//    }

    // token recognition.

    public void testTokenRecognition() {
        CompileResult result = BCompileUtil
                .compile("test-src/parser/resource-with-empty-reply-negative.bal");
        BAssertUtil.validateError(result, 0, "token recognition error at: '\\'", 3, 5);
        BAssertUtil.validateError(result, 0, "token recognition error at: '*'", 4, 15);
    }

//    @Test
//    public void testListenerDeclarationWithDefinedDifferentType() {
//        CompileResult result = BCompileUtil.compile("test-src/parser/listener_declaration_type_reuse_negative.bal");
//        BAssertUtil.validateError(result, 0, "invalid assignment: 'listener' declaration is final", 22, 5);
//      BAssertUtil.validateError(result, 1, "incompatible types: expected 'ballerina/http:1.0.0:MockListener', found" +
//                " 'int'", 22, 9);
//        BAssertUtil.validateError(result, 2, "incompatible types: expected 'int', found " +
//                "'ballerina/http:1.0.0:MockListener'", 23, 9);
//        BAssertUtil.validateError(result, 3, "incompatible types: expected 'lang.object:Listener', found 'int'", 26,
//                                  14);
//        BAssertUtil.validateError(result, 4, "incompatible types: expected 'lang.object:Listener', found 'Person'",
//                                  29, 24);
//        BAssertUtil.validateError(result, 5, "incompatible types: expected 'lang.object:Listener', found 'other'", 31
//                , 23);
//    }
}
