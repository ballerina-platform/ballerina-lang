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

import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.function.Predicate;

/**
 * This class contains cases to test the {@code TreeModifier} functionality.
 *
 * @since 1.3.0
 */
public class SyntaxTreeModifierTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testVarDeclStmtModification() {
        SyntaxTree syntaxTree = parseFile("variable_decl_stmt_modify.bal");
        ModulePartNode oldRoot = syntaxTree.modulePart();

        VariableDeclModifier variableDeclModifier = new VariableDeclModifier();
        ModulePartNode newRoot = (ModulePartNode) oldRoot.apply(variableDeclModifier);

        FunctionDefinitionNode oldFuncNode = (FunctionDefinitionNode) oldRoot.members().get(0);
        FunctionBodyBlockNode oldFuncBody = (FunctionBodyBlockNode) oldFuncNode.functionBody();
        VariableDeclarationNode oldStmt = (VariableDeclarationNode) oldFuncBody.statements().get(0);
        Token oldVarName = ((CaptureBindingPatternNode) oldStmt.typedBindingPattern().bindingPattern()).variableName();

        FunctionDefinitionNode newFuncNode = (FunctionDefinitionNode) newRoot.members().get(0);
        FunctionBodyBlockNode newFuncBody = (FunctionBodyBlockNode) newFuncNode.functionBody();
        VariableDeclarationNode newStmt = (VariableDeclarationNode) newFuncBody.statements().get(0);
        Token newVarName = ((CaptureBindingPatternNode) newStmt.typedBindingPattern().bindingPattern()).variableName();

        Assert.assertNotEquals(newFuncNode, oldFuncNode);
        Assert.assertNotEquals(newStmt, oldStmt);
        Assert.assertEquals(newVarName.text(), oldVarName.text() + "new");
        Assert.assertEquals(newStmt.textRangeWithMinutiae().length(), oldStmt.textRangeWithMinutiae().length() + 2);
    }

    @Test
    public void testRenameIdentifierWithoutTrivia() {
        SyntaxTree syntaxTree = parseFile("variable_decl_stmt_modify.bal");
        ModulePartNode oldRoot = syntaxTree.modulePart();

        IdentifierModifier identifierModifier = new IdentifierModifier();
        ModulePartNode newRoot = (ModulePartNode) oldRoot.apply(identifierModifier);

        FunctionDefinitionNode oldFuncNode = (FunctionDefinitionNode) oldRoot.members().get(0);
        String oldFuncName = oldFuncNode.functionName().text();

        FunctionDefinitionNode newFuncNode = (FunctionDefinitionNode) newRoot.members().get(0);
        String newFuncName = newFuncNode.functionName().text();

        Assert.assertEquals(newFuncName, oldFuncName + "_new");
    }

    @Test
    public void testBinaryExprModification() {
        // There are no '-' or '/' tokens in the old tree
        // There are two '+' tokens and two '*' tokens in the old tree
        SyntaxTree syntaxTree = parseFile("binary_expression_modify.bal");
        ModulePartNode oldRoot = syntaxTree.modulePart();

        // // There are no '+' or '*' tokens here in the new tree
        BinaryExpressionModifier binaryExprModifier = new BinaryExpressionModifier();
        ModulePartNode newRoot = binaryExprModifier.transform(oldRoot);

        Predicate<SyntaxKind> plusOrAsteriskTokenPredicate =
                syntaxKind -> SyntaxKind.PLUS_TOKEN == syntaxKind || SyntaxKind.ASTERISK_TOKEN == syntaxKind;
        Predicate<SyntaxKind> minusOrSlashTokenPredicate =
                syntaxKind -> SyntaxKind.MINUS_TOKEN == syntaxKind || SyntaxKind.SLASH_TOKEN == syntaxKind;

        TokenCounter plusOrAsteriskCounter = new TokenCounter(plusOrAsteriskTokenPredicate);
        TokenCounter minusOrSlashCounter = new TokenCounter(minusOrSlashTokenPredicate);

        Assert.assertEquals(plusOrAsteriskCounter.transform(newRoot), new Integer(0));
        Assert.assertEquals(minusOrSlashCounter.transform(newRoot), new Integer(4));
    }

    /**
     * An implementation of {@code TreeModifier} that modify all variable declaration statements.
     */
    private static class VariableDeclModifier extends TreeModifier {

        @Override
        public VariableDeclarationNode transform(VariableDeclarationNode varDeclStmt) {
            TypedBindingPatternNode typedBindingPattern = varDeclStmt.typedBindingPattern();
            Token varNameToken = ((CaptureBindingPatternNode) typedBindingPattern.bindingPattern()).variableName();
            // Create a new identifier that does not inherit minutiae from the old one.
            IdentifierToken newVarName = NodeFactory.createIdentifierToken(varNameToken.text() + "new");
            CaptureBindingPatternNode newCaptureBP = NodeFactory.createCaptureBindingPatternNode(newVarName);
            TypedBindingPatternNode newTypedBP =
                    NodeFactory.createTypedBindingPatternNode(typedBindingPattern.typeDescriptor(), newCaptureBP);
            return NodeFactory.createVariableDeclarationNode(varDeclStmt.annotations(),
                    varDeclStmt.finalKeyword().orElse(null), newTypedBP, varDeclStmt.equalsToken().orElse(null),
                    varDeclStmt.initializer().orElse(null), varDeclStmt.semicolonToken());
        }
    }

    /**
     * An implementation of {@code TreeModifier} that rename all identifiers in the tree.
     */
    private static class IdentifierModifier extends TreeModifier {

        @Override
        public IdentifierToken transform(IdentifierToken identifier) {
            return identifier.modify(identifier.text() + "_new");
        }
    }

    /**
     * An implementation of {@code TreeModifier} that perform random changes.
     * Transform + to -.
     * Transform * to /.
     */
    private static class BinaryExpressionModifier extends TreeModifier {

        @Override
        public BinaryExpressionNode transform(BinaryExpressionNode binaryExprNode) {
            Node newLHSExpr = modifyNode(binaryExprNode.lhsExpr());
            Node newRHSExpr = modifyNode(binaryExprNode.rhsExpr());

            Token newOperator;
            Token oldOperator = binaryExprNode.operator();
            switch (oldOperator.kind()) {
                case PLUS_TOKEN:
                    newOperator = NodeFactory.createToken(SyntaxKind.MINUS_TOKEN, oldOperator.leadingMinutiae(),
                            oldOperator.trailingMinutiae());
                    break;
                case ASTERISK_TOKEN:
                    newOperator = NodeFactory.createToken(SyntaxKind.SLASH_TOKEN, oldOperator.leadingMinutiae(),
                            oldOperator.trailingMinutiae());
                    break;
                default:
                    newOperator = oldOperator;
            }

            return binaryExprNode.modify().withOperator(newOperator).withLhsExpr(newLHSExpr).withRhsExpr(newRHSExpr)
                    .apply();
        }
    }

    /**
     * An implementation of {@code NodeTransformer} that counts the number of token that matches a given predicate.
     */
    private static class TokenCounter extends NodeTransformer<Integer> {
        private final Predicate<SyntaxKind> predicate;

        public TokenCounter(Predicate<SyntaxKind> predicate) {
            this.predicate = predicate;
        }

        @Override
        public Integer transform(Token token) {
            SyntaxKind syntaxKind = token.kind();
            return predicate.test(syntaxKind) ? 1 : 0;
        }

        @Override
        protected Integer transformSyntaxNode(Node node) {
            if (node instanceof Token) {
                return node.apply(this);
            }

            int tokenCount = 0;
            NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
            for (Node child : nonTerminalNode.children()) {
                tokenCount += child.apply(this);
            }
            return tokenCount;
        }
    }
}
