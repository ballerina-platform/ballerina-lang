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

import io.ballerinalang.compiler.syntax.tree.FunctionDefinition;
import io.ballerinalang.compiler.syntax.tree.Identifier;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;
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
        ModulePart oldRoot = syntaxTree.getModulePart();

        VariableDeclModifier variableDeclModifier = new VariableDeclModifier();
        ModulePart newRoot = (ModulePart) oldRoot.apply(variableDeclModifier);

        FunctionDefinition oldFuncNode = (FunctionDefinition) oldRoot.members().get(0);
        VariableDeclaration oldStmt = (VariableDeclaration) oldFuncNode.functionBody().statements().get(0);

        FunctionDefinition newFuncNode = (FunctionDefinition) newRoot.members().get(0);
        VariableDeclaration newStmt = (VariableDeclaration) newFuncNode.functionBody().statements().get(0);

        Assert.assertNotEquals(newFuncNode, oldFuncNode);
        Assert.assertNotEquals(newStmt, oldStmt);
        Assert.assertEquals(newStmt.variableName().text(), oldStmt.variableName().text() + "new");
        Assert.assertEquals(newStmt.spanWithMinutiae().width(), oldStmt.spanWithMinutiae().width() + 2);
    }

    @Test
    public void testRenameIdentifierWithoutTrivia() {
        SyntaxTree syntaxTree = parseFile("variable_decl_stmt_modify.bal");
        ModulePart oldRoot = syntaxTree.getModulePart();

        IdentifierModifier identifierModifier = new IdentifierModifier();
        ModulePart newRoot = (ModulePart) oldRoot.apply(identifierModifier);

        FunctionDefinition oldFuncNode = (FunctionDefinition) oldRoot.members().get(0);
        String oldFuncName = oldFuncNode.functionName().text();

        FunctionDefinition newFuncNode = (FunctionDefinition) newRoot.members().get(0);
        String newFuncName = newFuncNode.functionName().text();

        Assert.assertEquals(newFuncName, oldFuncName + "_new");
    }

    /**
     * An implementation of {@code SyntaxTreeModifier} that modify all variable declaration statements.
     */
    private static class VariableDeclModifier extends TreeModifier {

        @Override
        public Node transform(VariableDeclaration varDeclStmt) {
            String oldVarName = varDeclStmt.variableName().text();
            Identifier newVarName = NodeFactory.createIdentifier(oldVarName + "new");
            return NodeFactory.createVariableDeclaration(varDeclStmt.finalKeyword(), varDeclStmt.typeName(),
                    newVarName, varDeclStmt.equalsToken(), varDeclStmt.initializer(), varDeclStmt.semicolonToken());
        }
    }

    /**
     * An implementation of {@code SyntaxTreeModifier} that rename all identifiers in the tree.
     */
    private static class IdentifierModifier extends TreeModifier {

        @Override
        public Node transform(Identifier identifier) {
            return NodeFactory.createIdentifier(identifier.text() + "_new");
        }
    }
}
