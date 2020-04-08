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
import io.ballerinalang.compiler.syntax.tree.Identifier;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.SyntaxTreeModifier;
import io.ballerinalang.compiler.syntax.tree.VariableDeclaration;
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

        VariableDeclModifier variableDeclModifier = new VariableDeclModifier();
        ModulePart oldRoot = syntaxTree.getModulePart();
        ModulePart newRoot = (ModulePart) oldRoot.apply(variableDeclModifier);

        FunctionDefinitionNode oldFuncNode = (FunctionDefinitionNode) oldRoot.members().get(0);
        VariableDeclaration oldStmt = (VariableDeclaration) oldFuncNode.functionBody().statements().get(0);

        FunctionDefinitionNode newFuncNode = (FunctionDefinitionNode) newRoot.members().get(0);
        VariableDeclaration newStmt = (VariableDeclaration) newFuncNode.functionBody().statements().get(0);

        Assert.assertNotEquals(newFuncNode, oldFuncNode);
        Assert.assertNotEquals(newStmt, oldStmt);
        Assert.assertEquals(newStmt.variableName().getText(), oldStmt.variableName().getText() + "new");
        Assert.assertEquals(newStmt.spanWithMinutiae().width(), oldStmt.spanWithMinutiae().width() + 2);
    }

    /**
     * An implementation of {@code SyntaxTreeModifier}.
     */
    private static class VariableDeclModifier extends SyntaxTreeModifier {

        @Override
        public Node transform(VariableDeclaration varDeclStmt) {
            String oldVarName = varDeclStmt.variableName().getText();
            Identifier newVarName = NodeFactory.createIdentifier(oldVarName + "new");
            return NodeFactory.createVariableDeclaration(varDeclStmt.finalKeyword(), varDeclStmt.typeName(),
                    newVarName, varDeclStmt.equalsToken(), varDeclStmt.initializer(), varDeclStmt.semicolonToken());
        }
    }
}
