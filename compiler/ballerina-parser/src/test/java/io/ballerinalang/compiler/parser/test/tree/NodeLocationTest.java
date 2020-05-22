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

import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.LineRange;
import io.ballerinalang.compiler.text.TextDocument;
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
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(10, 1);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(modulePartNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testLocationOfSingleImportNode() {
        String sourceFileName = "node_location_test_02.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ImportDeclarationNode importDeclNode = syntaxTree.modulePart().imports().get(0);

        LinePosition expectedStartPos = LinePosition.from(0, 0);
        LinePosition expectedEndPos = LinePosition.from(0, 20);
        LineRange expectedLineRange = LineRange.from(sourceFileName, expectedStartPos, expectedEndPos);
        assertLineRange(importDeclNode.location().lineRange(), expectedLineRange);
    }

    @Test
    public void testLocationOfMultipleImportNodes() {
        String sourceFileName = "node_location_test_03.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.modulePart();
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
        ModulePartNode modulePartNode = syntaxTree.modulePart();
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
        ModulePartNode modulePartNode = syntaxTree.modulePart();
        FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

        LinePosition startLinePos = LinePosition.from(5, 0);
        LinePosition endLinePos = LinePosition.from(9, 1);
        int expectedStartOffset = textDocument.textPositionFrom(startLinePos);
        int expectedEndOffset = textDocument.textPositionFrom(endLinePos);

        Assert.assertEquals(functionDefNode.textRange().startOffset(), expectedStartOffset);
        Assert.assertEquals(functionDefNode.textRange().endOffset(), expectedEndOffset);
    }

    @Test
    public void testConvertingOffsetToLinePosition() {
        SyntaxTree syntaxTree = parseFile("node_location_test_04.bal");
        TextDocument textDocument = syntaxTree.textDocument();
        ModulePartNode modulePartNode = syntaxTree.modulePart();
        FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

        int startOffset = 87;
        int endOffset = 209;
        LineRange lineRange = functionDefNode.location().lineRange();
        LinePosition expectedStartLinePos = lineRange.startLine();
        LinePosition expectedEndLinePos = lineRange.endLine();

        Assert.assertEquals(textDocument.linePositionFrom(startOffset), expectedStartLinePos);
        Assert.assertEquals(textDocument.linePositionFrom(endOffset), expectedEndLinePos);
    }
}
