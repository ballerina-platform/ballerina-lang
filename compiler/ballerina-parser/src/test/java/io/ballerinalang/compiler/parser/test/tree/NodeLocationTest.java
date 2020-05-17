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
import io.ballerinalang.compiler.syntax.tree.NodeLocation;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.LineRange;
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

        LinePosition expectedStartPos = new LinePosition(0, 0);
        LinePosition expectedEndPos = new LinePosition(10, 1);
        assertNodeLocation(modulePartNode.location(), sourceFileName, expectedStartPos, expectedEndPos);
    }

    @Test
    public void testLocationOfSingleImportNode() {
        String sourceFileName = "node_location_test_02.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ImportDeclarationNode importDeclNode = syntaxTree.modulePart().imports().get(0);

        LinePosition expectedStartPos = new LinePosition(0, 0);
        LinePosition expectedEndPos = new LinePosition(0, 20);
        assertNodeLocation(importDeclNode.location(), sourceFileName, expectedStartPos, expectedEndPos);
    }

    @Test
    public void testLocationOfMultipleImportNodes() {
        String sourceFileName = "node_location_test_03.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.modulePart();
        ImportDeclarationNode importDeclarationNode = modulePartNode.imports().get(3);

        LinePosition expectedStartPos = new LinePosition(3, 0);
        LinePosition expectedEndPos = new LinePosition(3, 29);
        assertNodeLocation(importDeclarationNode.location(), sourceFileName, expectedStartPos, expectedEndPos);
    }

    @Test
    public void testLocationOfFunctionDefNode() {
        String sourceFileName = "node_location_test_04.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.modulePart();
        FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

        LinePosition expectedStartPos = new LinePosition(5, 0);
        LinePosition expectedEndPos = new LinePosition(9, 1);
        assertNodeLocation(functionDefNode.location(), sourceFileName, expectedStartPos, expectedEndPos);
    }

    private void assertNodeLocation(NodeLocation nodeLocation,
                                    String sourceFileName,
                                    LinePosition expectedStartPos,
                                    LinePosition expectedEndPos) {
        LineRange lineRange = nodeLocation.lineRange();
        Assert.assertEquals(lineRange.filePath(), sourceFileName);
        Assert.assertEquals(lineRange.startLine(), expectedStartPos);
        Assert.assertEquals(lineRange.endLine(), expectedEndPos);
    }
}
