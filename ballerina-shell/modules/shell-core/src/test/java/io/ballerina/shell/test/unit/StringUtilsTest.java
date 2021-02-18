/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.test.unit;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link StringUtils}.
 *
 * @since 2.0.0
 */
public class StringUtilsTest {
    @Test
    public void testQuoted() {
        Assert.assertEquals(StringUtils.quoted("a"), "'a");
        Assert.assertEquals(StringUtils.quoted("variable"), "'variable");
        Assert.assertEquals(StringUtils.quoted("'a"), "'a");
        Assert.assertEquals(StringUtils.quoted("'variable"), "'variable");
    }

    @Test
    public void testShortenedString() {
        Assert.assertEquals(StringUtils.shortenedString(""), "");
        Assert.assertEquals(StringUtils.shortenedString("abc".repeat(2)), "abcabc");
        Assert.assertEquals(StringUtils.shortenedString("abc".repeat(10)), "abcabcabcabcabcabcabcabcabcabc");
        Assert.assertEquals(StringUtils.shortenedString("abc".repeat(100)),
                "abcabcabcabcabcabcabcabcabcabcabcabcabc...abcabcabcabcabcabcabcabcabcabcabcabcabc");
    }

    @Test
    public void testHighlightDiagnostic() {
        testHighlightDiagnostic("int i = 100", "" +
                "error: missing semicolon token\n" +
                "\tint i = 100\n" +
                "\t           ^");
        testHighlightDiagnostic("int i\\-\\like\\-hyphens = 100", "" +
                "error: invalid escape sequence '\\l'\n" +
                "\tint i\\-\\like\\-hyphens = 100\n" +
                "\t    ^---------------^");
        testHighlightDiagnostic("\\l", "" +
                "error: invalid escape sequence '\\l'\n" +
                "\t\\l\n" +
                "\t^^");
    }

    private void testHighlightDiagnostic(String code, String expectedError) {
        TextDocument textDocument = TextDocuments.from(code);
        SyntaxTree tree = SyntaxTree.from(textDocument);
        Diagnostic diagnostic = tree.diagnostics().iterator().next();
        String error = StringUtils.highlightDiagnostic(textDocument, diagnostic);
        error = error.replace("\r\n", "\n");
        Assert.assertEquals(error, expectedError);
    }
}
