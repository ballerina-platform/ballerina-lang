/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;
import io.ballerinalang.compiler.text.TextLine;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains methods to test external syntax tree traversal API.
 *
 * @since 1.3.0
 */
public class TreeTraversalAPITest extends AbstractSyntaxTreeAPITest {

    @Test(enabled = false)
    public void testFindTokenFromPosition() {
        String sourceFilePath = "find_token_test_1.bal";
        String sourceText = getFileContentAsString(sourceFilePath);
        SyntaxTree syntaxTree = parseFile(sourceFilePath);
        ModulePartNode modulePart = syntaxTree.rootNode();

        // Get the expected lexemes from the TextDocument itself
        // You will get text lines with normalized newline characters
        TextDocument textDocument = TextDocuments.from(sourceText);
        TextLine textLine = textDocument.line(5);

        String expectedLexeme = textLine.text().substring(40, 41); // This should be the char 'g' in '....(f + g));'
        Token actualToken = modulePart.findToken(115);
        Assert.assertEquals(actualToken.toString(), expectedLexeme);

        expectedLexeme = textLine.text().substring(36, 38); // This should be the chars 'f ' in '....(f + g));'
        actualToken = modulePart.findToken(111);
        Assert.assertEquals(actualToken.toString(), expectedLexeme);
    }

    @Test(enabled = false)
    public void testGetParentOfToken() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token token = modulePart.findToken(115);

        Node parent = token.parent();
        Assert.assertEquals(parent.kind(), SyntaxKind.BINARY_EXPRESSION);

        parent = token.parent().parent().parent().parent().parent().parent().parent();
        Assert.assertEquals(parent.kind(), SyntaxKind.LOCAL_VAR_DECL);
    }

    @Test
    public void testGetParentOfFunctionDef() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token funcToken = modulePart.findToken(50);

        Node funcDef = funcToken.parent();
        Assert.assertEquals(funcDef.kind(), SyntaxKind.FUNCTION_DEFINITION);
        Assert.assertEquals(funcDef.parent().kind(), SyntaxKind.MODULE_PART);
    }

    @Test
    public void testGenChildrenOfFunctionDef() {
        SyntaxTree syntaxTree = parseFile("find_token_test_1.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();
        Token funcToken = modulePart.findToken(50);
        NonTerminalNode funcDef = funcToken.parent();

        List<SyntaxKind> actualChildNodeKindList = new ArrayList<>();
        List<SyntaxKind> expectedChildNodeKindList = Arrays.asList(
                SyntaxKind.METADATA,
                SyntaxKind.PUBLIC_KEYWORD,
                SyntaxKind.FUNCTION_KEYWORD,
                SyntaxKind.IDENTIFIER_TOKEN,
                SyntaxKind.FUNCTION_SIGNATURE,
                SyntaxKind.FUNCTION_BODY_BLOCK);

        for (Node child : funcDef.children()) {
            if (child.kind() == SyntaxKind.NONE) {
                continue;
            }
            actualChildNodeKindList.add(child.kind());
        }

        Assert.assertEquals(actualChildNodeKindList, expectedChildNodeKindList);
    }
}
