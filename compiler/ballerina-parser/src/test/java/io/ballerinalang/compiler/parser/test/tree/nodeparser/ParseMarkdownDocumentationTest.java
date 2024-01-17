/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseMarkdownDocumentation} method.
 *
 * @since 2201.7.0
 */
public class ParseMarkdownDocumentationTest {

    // Valid syntax tests

    @Test
    public void testMarkdownDocumentation1() {
        MarkdownDocumentationNode markdownDoc = NodeParser.parseMarkdownDocumentation("# This is the description");
        Assert.assertEquals(markdownDoc.kind(), SyntaxKind.MARKDOWN_DOCUMENTATION);
        Assert.assertFalse(markdownDoc.hasDiagnostics());
    }

    @Test
    public void testMarkdownDocumentation2() {
        String markdownDocText = "# This is the description\n" +
                "#\n" +
                "# + value - value input parameter\n" +
                "# + return - return a integer value";
        MarkdownDocumentationNode markdownDoc = NodeParser.parseMarkdownDocumentation(markdownDocText);
        Assert.assertEquals(markdownDoc.kind(), SyntaxKind.MARKDOWN_DOCUMENTATION);
        Assert.assertFalse(markdownDoc.hasDiagnostics());
        Assert.assertEquals(markdownDoc.toSourceCode(), markdownDocText);
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        MarkdownDocumentationNode markdownDoc = NodeParser.parseMarkdownDocumentation("");
        Assert.assertEquals(markdownDoc.kind(), SyntaxKind.MARKDOWN_DOCUMENTATION);
        Assert.assertTrue(markdownDoc.hasDiagnostics());

        MarkdownDocumentationLineNode docLine = (MarkdownDocumentationLineNode) markdownDoc.documentationLines().get(0);
        Assert.assertTrue(docLine.hashToken().isMissing());
        Assert.assertTrue(docLine.documentElements().isEmpty());
        Assert.assertEquals(markdownDoc.toString(), " MISSING[#]");
    }

    @Test
    public void testWithAdditionalTokens() {
        String markdownDocText = "# Description\ntype A a;";
        MarkdownDocumentationNode markdownDoc = NodeParser.parseMarkdownDocumentation(markdownDocText);
        Assert.assertEquals(markdownDoc.kind(), SyntaxKind.MARKDOWN_DOCUMENTATION);
        Assert.assertTrue(markdownDoc.hasDiagnostics());

        Assert.assertEquals(markdownDoc.leadingInvalidTokens().size(), 0);
        List<Token> invalidTokens = markdownDoc.trailingInvalidTokens();
        int invalidTokenCount = invalidTokens.size();
        Assert.assertEquals(invalidTokenCount, 4);

        SyntaxKind[] expectedKinds = {SyntaxKind.TYPE_KEYWORD, SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.IDENTIFIER_TOKEN,
                SyntaxKind.SEMICOLON_TOKEN};
        for (int i = 0; i < invalidTokenCount; i++) {
            Assert.assertEquals(invalidTokens.get(i).kind(), expectedKinds[i]);
        }

        Assert.assertEquals(markdownDoc.toSourceCode(), markdownDocText);
        Assert.assertEquals(markdownDoc.toString(), "# Description\n INVALID[type]  INVALID[A]  INVALID[a] INVALID[;]");
    }

    @Test
    public void testWithInvalidInput() {
        String invalidInput = "int a = 10;";
        MarkdownDocumentationNode markdownDoc = NodeParser.parseMarkdownDocumentation(invalidInput);
        Assert.assertEquals(markdownDoc.kind(), SyntaxKind.MARKDOWN_DOCUMENTATION);
        Assert.assertTrue(markdownDoc.hasDiagnostics());

        Assert.assertEquals(markdownDoc.leadingInvalidTokens().size(), 0);
        List<Token> invalidTokens = markdownDoc.trailingInvalidTokens();
        int invalidTokenCount = invalidTokens.size();
        Assert.assertEquals(invalidTokenCount, 5);

        SyntaxKind[] expectedKinds = {SyntaxKind.INT_KEYWORD, SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.EQUAL_TOKEN,
                SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN, SyntaxKind.SEMICOLON_TOKEN};
        for (int i = 0; i < invalidTokenCount; i++) {
            Assert.assertEquals(invalidTokens.get(i).kind(), expectedKinds[i]);
        }

        Assert.assertEquals(markdownDoc.toSourceCode(), invalidInput);
        Assert.assertEquals(markdownDoc.toString(),
                " MISSING[# INVALID[int]  INVALID[a]  INVALID[=]  INVALID[10] INVALID[;]]");
    }
}
