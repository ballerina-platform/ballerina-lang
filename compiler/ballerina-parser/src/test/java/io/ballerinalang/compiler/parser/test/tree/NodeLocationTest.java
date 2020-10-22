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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains cases to test the functionality of the {@code NodeLocation} class.
 *
 * @since 2.0.0
 */
public class NodeLocationTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testLocationOfModulePart() {
        String sourceFileName = "node_location_test_01.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(10, 1);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(modulePartNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testLocationOfSingleImportNode() {
        String sourceFileName = "node_location_test_02.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        ImportDeclarationNode importDeclNode = modulePartNode.imports().get(0);

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 20);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(importDeclNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testLocationOfModuleNodeWithNewLinesAtTheEnd() {
        String sourceFileName = "node_location_test_06.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(2, 0);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(modulePartNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testLocationOfEmptyFile() {
        String sourceFileName = "node_location_test_07.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        TextDocument textDocument = syntaxTree.textDocument();
        LinePosition linePosition = textDocument.linePositionFrom(0);
        Assert.assertEquals(0, linePosition.line());
        Assert.assertEquals(0, linePosition.offset());

        String inputSrc = "\r\n";
        TextDocument textDocument1 = TextDocuments.from(inputSrc);
        SyntaxTree syntaxTree1 = SyntaxTree.from(textDocument1);
        LinePosition linePosition1 = syntaxTree1.textDocument().linePositionFrom(1);
        Assert.assertEquals(0, linePosition1.line());
        Assert.assertEquals(1, linePosition1.offset());

        LinePosition linePosition2 = syntaxTree1.textDocument().linePositionFrom(2);
        Assert.assertEquals(1, linePosition2.line());
        Assert.assertEquals(0, linePosition2.offset());
    }

    @Test
    public void testLocationWithCRLF() {
        String inputSrc =
                "import ballerina/io;\r\n" +   // line 0
                        "import ballerina/http;\r\n" + // line 1
                        "import ballerina/log;\r\n" + // line 2
                        "import sammj/adder;\r\n" +  // line 3
                        "\r\n" +  // line 4
                        "\r\n" + // line 5
                        "public function main(string[] args) returns error? {\r\n" + // line 6
                        "    int a = args[0];\r\n" + // line 7
                        "    int b = args[1];\r\n" + // line 8
                        "    check adder:add(a,b);\r\n" + // line 9
                        "}\r\n"; // line 10 and line 11
        TextDocument textDocument = TextDocuments.from(inputSrc);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        // This text position maps to the last character of line 2
        LinePosition linePosition1 = syntaxTree.textDocument().linePositionFrom(68);
        Assert.assertEquals(2, linePosition1.line());
        Assert.assertEquals(22, linePosition1.offset());

        // This text position maps to the last character of line 3
        LinePosition linePosition2 = syntaxTree.textDocument().linePositionFrom(69);
        Assert.assertEquals(3, linePosition2.line());
        Assert.assertEquals(0, linePosition2.offset());
    }

    @Test
    public void testLocationWithLF() {
        String inputSrc =
                "import ballerina/io;\n" +   // line 0
                        "import ballerina/http;\n" + // line 1
                        "import ballerina/log;\n" + // line 2
                        "import sammj/adder;\n" +  // line 3
                        "\n" +  // line 4
                        "\n" + // line 5
                        "public function main(string[] args) returns error? {\n" + // line 6
                        "    int a = args[0];\n" + // line 7
                        "    int b = args[1];\n" + // line 8
                        "    check adder:add(a,b);\n" + // line 9
                        "}\n"; // line 10 and line 11
        TextDocument textDocument = TextDocuments.from(inputSrc);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        // This text position maps to the last character of line 2
        LinePosition linePosition1 = syntaxTree.textDocument().linePositionFrom(65);
        Assert.assertEquals(2, linePosition1.line());
        Assert.assertEquals(21, linePosition1.offset());

        // This text position maps to the last character of line 3
        LinePosition linePosition2 = syntaxTree.textDocument().linePositionFrom(66);
        Assert.assertEquals(3, linePosition2.line());
        Assert.assertEquals(0, linePosition2.offset());
    }

    @Test
    public void testLocationOfMultipleImportNodes() {
        String sourceFileName = "node_location_test_03.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        ImportDeclarationNode importDeclarationNode = modulePartNode.imports().get(3);

        LinePosition expectedStartPos = LinePosition.from(3, 0);
        LinePosition expectedEndPos = LinePosition.from(3, 29);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(importDeclarationNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testLocationOfFunctionDefNode() {
        String sourceFileName = "node_location_test_04.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

        LinePosition expectedStartPos = LinePosition.from(5, 0);
        LinePosition expectedEndPos = LinePosition.from(9, 1);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(functionDefNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testConvertingLinePositionToOffset() {
        SyntaxTree syntaxTree = parseFile("node_location_test_04.bal");
        TextDocument textDocument = syntaxTree.textDocument();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

        LinePosition startLinePos = LinePosition.from(5, 0);
        LinePosition endLinePos = LinePosition.from(9, 1);
        int expectedStartOffset = textDocument.textPositionFrom(startLinePos);
        int expectedEndOffset = textDocument.textPositionFrom(endLinePos);

        Assert.assertEquals(functionDefNode.textRange().startOffset(), expectedStartOffset);
        Assert.assertEquals(functionDefNode.textRange().endOffset(), expectedEndOffset);
    }

    @Test(enabled = false)
    public void testConvertingOffsetToLinePosition() {
        SyntaxTree syntaxTree = parseFile("node_location_test_04.bal");
        TextDocument textDocument = syntaxTree.textDocument();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

        int startOffset = 87;
        int endOffset = 209;
        LineRange lineRange = functionDefNode.location().lineRange();
        LinePosition expectedStartLinePos = lineRange.startLine();
        LinePosition expectedEndLinePos = lineRange.endLine();

        Assert.assertEquals(textDocument.linePositionFrom(startOffset), expectedStartLinePos);
        Assert.assertEquals(textDocument.linePositionFrom(endOffset), expectedEndLinePos);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testNodeLocationOfADetachedNode() {
        SyntaxTree syntaxTree = parseFile("node_location_test_05.bal");
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        ImportDeclarationNode importDeclNode = modulePartNode.imports().get(3);
        Assert.assertNotNull(importDeclNode.lineRange());

        // Now create detached node
        IdentifierToken identifierToken = NodeFactory.createIdentifierToken("myorg");
        ImportOrgNameNode importOrgNameNode = importDeclNode.orgName().get();
        ImportOrgNameNode newImportOrgNameNode = importOrgNameNode.modify()
                .withOrgName(identifierToken)
                .apply();
        ImportDeclarationNode detachedImportDeclNode = importDeclNode.modify()
                .withOrgName(newImportOrgNameNode)
                .apply();

        Assert.assertNotNull(detachedImportDeclNode.lineRange());
    }
}
