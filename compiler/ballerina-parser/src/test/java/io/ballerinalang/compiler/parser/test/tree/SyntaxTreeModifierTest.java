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

import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TreeModifier;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
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
        ModulePartNode oldRoot = syntaxTree.rootNode();

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
        ModulePartNode oldRoot = syntaxTree.rootNode();

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
        ModulePartNode oldRoot = syntaxTree.rootNode();

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

    @Test
    public void testSeparatedListNodeNonSeperatorModification() {
        SyntaxTree syntaxTree = parseFile("separated_node_list_modify.bal");
        ModulePartNode oldRoot = syntaxTree.rootNode();

        IdentifierModifier identifierModifier = new IdentifierModifier();
        ModulePartNode newRoot = (ModulePartNode) oldRoot.apply(identifierModifier);

        String expectedStr = getFileContentAsString("separated_node_list_modify_assert.bal");
        String actualStr = newRoot.toString();
        Assert.assertEquals(actualStr, expectedStr);
    }

    @Test
    public void testSeparatedListNodeAllNodeModification() {
        SyntaxTree syntaxTree = parseFile("separated_node_list_modify_all_nodes.bal");
        ModulePartNode oldRoot = syntaxTree.rootNode();

        WhiteSpaceMinutiaeRemover whiteSpaceMinutiaeRemover = new WhiteSpaceMinutiaeRemover();
        ModulePartNode newRoot = (ModulePartNode) oldRoot.apply(whiteSpaceMinutiaeRemover);

        String expectedStr = getFileContentAsString("separated_node_list_modify_all_nodes_assert.bal");
        String actualStr = newRoot.toString();
        Assert.assertEquals(actualStr, expectedStr);
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
     * An implementation of {@code TreeModifier} that removes all white space minutiae from all tokens.
     */
    private static class WhiteSpaceMinutiaeRemover extends TreeModifier {

        @Override
        public Token transform(Token token) {
            Predicate<Minutiae> minutiaePredicate = minutiae -> minutiae.kind() == SyntaxKind.WHITESPACE_MINUTIAE;

            MinutiaeList oldLeadingMinutiae = token.leadingMinutiae();
            MinutiaeList oldTrailingMinutiae = token.trailingMinutiae();

            Collection<Minutiae> matchingLeadingMinutiae = getMatchingMinutiae(oldLeadingMinutiae, minutiaePredicate);
            Collection<Minutiae> matchingTrailingMinutiae = getMatchingMinutiae(oldTrailingMinutiae, minutiaePredicate);

            MinutiaeList newLeadingMinutiae = oldLeadingMinutiae.removeAll(matchingLeadingMinutiae);
            MinutiaeList newTrailingMinutiae = oldTrailingMinutiae.removeAll(matchingTrailingMinutiae);

            return token.modify(newLeadingMinutiae, newTrailingMinutiae);
        }

        @Override
        public IdentifierToken transform(IdentifierToken identifierToken) {
            return (IdentifierToken) this.transform((Token) identifierToken);
        }
    }

    private static Collection<Minutiae> getMatchingMinutiae(MinutiaeList leadingMinutiae,
                                                            Predicate<Minutiae> predicate) {
        Collection<Minutiae> c = new ArrayList<>();
        for (int i = 0; i < leadingMinutiae.size(); i++) {
            Minutiae minutiae = leadingMinutiae.get(i);
            if (predicate.test(minutiae)) {
                c.add(minutiae);
            }
        }
        return c;
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
