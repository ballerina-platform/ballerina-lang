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

import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class contains cases to test the {@code SyntaxTreeModifier} functionality.
 *
 * @since 1.3.0
 */
public class SyntaxTreeModifierTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testVarDeclStmtModification() {
        SyntaxTree syntaxTree = parseFile("variable_decl_stmt_modify.bal");
        ModulePartNode oldRoot = syntaxTree.getModulePart();

        VariableDeclModifier variableDeclModifier = new VariableDeclModifier();
        ModulePartNode newRoot = (ModulePartNode) oldRoot.apply(variableDeclModifier);

        FunctionDefinitionNode oldFuncNode = (FunctionDefinitionNode) oldRoot.members().get(0);
        FunctionBodyBlockNode oldFuncBody = (FunctionBodyBlockNode) oldFuncNode.functionBody();
        VariableDeclarationNode oldStmt = (VariableDeclarationNode) oldFuncBody.statements().get(0);

        FunctionDefinitionNode newFuncNode = (FunctionDefinitionNode) newRoot.members().get(0);
        FunctionBodyBlockNode newFuncBody = (FunctionBodyBlockNode) newFuncNode.functionBody();
        VariableDeclarationNode newStmt = (VariableDeclarationNode) newFuncBody.statements().get(0);

        Assert.assertNotEquals(newFuncNode, oldFuncNode);
        Assert.assertNotEquals(newStmt, oldStmt);
        Assert.assertEquals(newStmt.variableName().text(), oldStmt.variableName().text() + "new");
        Assert.assertEquals(newStmt.spanWithMinutiae().width(), oldStmt.spanWithMinutiae().width() + 2);
    }

    @Test
    public void testRenameIdentifierWithoutTrivia() {
        SyntaxTree syntaxTree = parseFile("variable_decl_stmt_modify.bal");
        ModulePartNode oldRoot = syntaxTree.getModulePart();

        IdentifierModifier identifierModifier = new IdentifierModifier();
        ModulePartNode newRoot = (ModulePartNode) oldRoot.apply(identifierModifier);

        FunctionDefinitionNode oldFuncNode = (FunctionDefinitionNode) oldRoot.members().get(0);
        String oldFuncName = oldFuncNode.functionName().text();

        FunctionDefinitionNode newFuncNode = (FunctionDefinitionNode) newRoot.members().get(0);
        String newFuncName = newFuncNode.functionName().text();

        Assert.assertEquals(newFuncName, oldFuncName + "_new");
    }

    /**
     * An implementation of {@code SyntaxTreeModifier} that modify all variable declaration statements.
     */
    private static class VariableDeclModifier extends TreeModifier {

        @Override
        public Node transform(VariableDeclarationNode varDeclStmt) {
            String oldVarName = varDeclStmt.variableName().text();
            IdentifierToken newVarName = NodeFactory.createIdentifierToken(oldVarName + "new");
            return NodeFactory.createVariableDeclarationNode(varDeclStmt.annotations(),
                    varDeclStmt.finalKeyword().orElse(null), varDeclStmt.typeName(), newVarName,
                    varDeclStmt.equalsToken().orElse(null), varDeclStmt.initializer().orElse(null),
                    varDeclStmt.semicolonToken());
        }
    }

    /**
     * An implementation of {@code SyntaxTreeModifier} that rename all identifiers in the tree.
     */
    private static class IdentifierModifier extends TreeModifier {

        @Override
        public Node transform(IdentifierToken identifier) {
            return NodeFactory.createIdentifierToken(identifier.text() + "_new");
        }
    }
}
