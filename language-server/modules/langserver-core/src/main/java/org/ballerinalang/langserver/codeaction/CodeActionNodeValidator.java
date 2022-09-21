/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Node Transformer to check whether the syntax is valid.
 *
 * @since 2201.0.3
 */
public class CodeActionNodeValidator extends NodeTransformer<Boolean> {

    private final Set<Node> visitedNodes = new HashSet<>();

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        return node.parent() == null || node.parent().apply(this);
    }

    private Boolean isVisited(Node node) {
        if (visitedNodes.contains(node)) {
            return true;
        }
        visitedNodes.add(node);
        return false;
    }

    @Override
    public Boolean transform(VariableDeclarationNode node) {
        return isVisited(node) || node.typedBindingPattern().apply(this)
                // checks for both variables with & without the initializer
                && (node.equalsToken().isEmpty() || !node.equalsToken().get().isMissing()
                && node.initializer().isPresent()
                && node.initializer().get().apply(this));
    }

    @Override
    public Boolean transform(FunctionCallExpressionNode node) {
        if (isVisited(node)) {
            return true;
        }
        // Check for missing commas
        for (int i = 0; i < node.arguments().separatorSize(); i++) {
            if (node.arguments().getSeparator(i).isMissing()) {
                return false;
            }
        }
        return !node.functionName().isMissing() &&
                !node.openParenToken().isMissing() &&
                !node.closeParenToken().isMissing() &&
                node.arguments().stream().allMatch(arg -> arg.apply(this))
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(TypedBindingPatternNode node) {
        return isVisited(node) || node.bindingPattern().apply(this)
                && node.typeDescriptor().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(CaptureBindingPatternNode node) {
        return isVisited(node) || !node.variableName().isMissing()
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(CheckExpressionNode node) {
        return isVisited(node) || !node.checkKeyword().isMissing()
                && node.expression().apply(this);
    }

    @Override
    public Boolean transform(TableTypeDescriptorNode node) {
        return isVisited(node) || !node.tableKeywordToken().isMissing()
                && node.rowTypeParameterNode().apply(this)
                // checks both when the key constraint is available and not
                && (node.keyConstraintNode().isEmpty() || node.keyConstraintNode().get().apply(this));
    }

    @Override
    public Boolean transform(TypeParameterNode node) {
        return isVisited(node) || !node.ltToken().isMissing()
                && !node.gtToken().isMissing()
                && node.typeNode().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(KeySpecifierNode node) {
        return isVisited(node) || !node.keyKeyword().isMissing()
                && !node.openParenToken().isMissing()
                && !node.closeParenToken().isMissing()
                && node.fieldNames().stream().noneMatch(Node::isMissing);
    }

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        return isVisited(node) || !node.name().isMissing()
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(AssignmentStatementNode node) {
        return isVisited(node) || !node.equalsToken().isMissing()
                && node.expression().apply(this)
                && node.varRef().apply(this);
    }

    @Override
    public Boolean transform(BinaryExpressionNode node) {
        return isVisited(node) || !node.operator().isMissing()
                && node.lhsExpr().apply(this)
                && node.rhsExpr().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(LetExpressionNode node) {
        return isVisited(node) || !node.letKeyword().isMissing()
                && !node.inKeyword().isMissing()
                && node.letVarDeclarations().stream().allMatch(arg -> arg.apply(this))
                && node.expression().apply(this);
    }

    @Override
    public Boolean transform(LetVariableDeclarationNode node) {
        return isVisited(node) || !node.equalsToken().isMissing()
                && node.typedBindingPattern().apply(this)
                && node.expression().apply(this);
    }

    @Override
    public Boolean transform(SpreadFieldNode node) {
        return isVisited(node) || !node.ellipsis().isMissing()
                && node.valueExpr().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(MappingConstructorExpressionNode node) {
        return isVisited(node) || !node.openBrace().isMissing()
                && !node.closeBrace().isMissing()
                && node.fields().stream().allMatch(arg -> arg.apply(this))
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(FieldAccessExpressionNode node) {
        return isVisited(node) || node.expression().apply(this)
                && !node.dotToken().isMissing()
                && node.fieldName().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(PositionalArgumentNode node) {
        return isVisited(node) || node.expression().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(BasicLiteralNode node) {
        return isVisited(node) || !node.literalToken().isMissing()
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(NamedArgumentNode node) {
        return isVisited(node) || !node.equalsToken().isMissing()
                && node.argumentName().apply(this)
                && node.expression().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(RestArgumentNode node) {
        return isVisited(node) || !node.ellipsis().isMissing()
                && node.expression().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(ListConstructorExpressionNode node) {
        return isVisited(node) || !node.openBracket().isMissing()
                && !node.closeBracket().isMissing()
                && node.expressions().stream().allMatch(arg -> arg.apply(this))
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(SpecificFieldNode node) {
        return isVisited(node) ||
                (node.colon().isPresent() && !node.fieldName().toSourceCode().isEmpty()
                        && (node.valueExpr().isEmpty() || node.valueExpr().get().apply(this))
                        && node.parent().apply(this));
    }

    @Override
    public Boolean transform(RequiredParameterNode node) {
        if (node.paramName().isEmpty()) {
            return false;
        }
        Token paramName = node.paramName().get();
        return isVisited(node) || node.typeName().apply(this)
                && paramName.leadingInvalidTokens().isEmpty()
                && paramName.trailingInvalidTokens().isEmpty();
    }

    @Override
    public Boolean transform(MapTypeDescriptorNode node) {
        return isVisited(node) || !node.mapKeywordToken().isMissing()
                && !node.mapTypeParamsNode().isMissing()
                && !node.mapTypeParamsNode().gtToken().isMissing()
                && !node.mapTypeParamsNode().ltToken().isMissing();
    }

    @Override
    public Boolean transform(TypeTestExpressionNode node) {
        return isVisited(node) || !node.isKeyword().isMissing()
                && node.typeDescriptor().apply(this)
                && node.expression().apply(this);
    }

    @Override
    public Boolean transform(FunctionDefinitionNode node) {
        return isVisited(node) || !node.functionKeyword().isMissing()
                && !node.functionName().isMissing()
                && node.leadingInvalidTokens().isEmpty()
                && node.trailingInvalidTokens().isEmpty()
                && node.functionSignature().apply(this)
                && node.functionBody().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(FunctionSignatureNode node) {
        return isVisited(node) || !node.openParenToken().isMissing()
                && !node.closeParenToken().isMissing()
                && node.leadingInvalidTokens().isEmpty()
                && node.trailingInvalidTokens().isEmpty()
                && (node.returnTypeDesc().isEmpty() || node.returnTypeDesc().get().apply(this))
                && node.parameters().stream().allMatch(parameterNode -> parameterNode.apply(this));
    }

    @Override
    public Boolean transform(FunctionBodyBlockNode node) {
        return isVisited(node) || !node.isMissing()
                && !node.openBraceToken().isMissing()
                && !node.closeBraceToken().isMissing()
                && node.leadingInvalidTokens().isEmpty()
                && node.trailingInvalidTokens().isEmpty()
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(ReturnTypeDescriptorNode node) {
        return isVisited(node) || !node.returnsKeyword().isMissing()
                && node.type().apply(this);
    }

    @Override
    public Boolean transform(ExpressionFunctionBodyNode node) {
        return isVisited(node) || !node.rightDoubleArrow().isMissing()
                && !node.expression().isMissing();
    }

    @Override
    public Boolean transform(ClassDefinitionNode node) {
        return isVisited(node) || !node.classKeyword().isMissing()
                && !node.className().isMissing()
                && !node.openBrace().isMissing()
                && !node.closeBrace().isMissing()
                && node.leadingInvalidTokens().isEmpty()
                && node.trailingInvalidTokens().isEmpty()
                && node.members().stream().allMatch(member -> member.apply(this));
    }

    @Override
    public Boolean transform(TableConstructorExpressionNode node) {
        return isVisited(node) || !node.tableKeyword().isMissing()
                && !node.openBracket().isMissing()
                && !node.closeBracket().isMissing()
                && (node.keySpecifier().isEmpty() || node.keySpecifier().get().apply(this))
                && node.rows().stream().allMatch(row -> row.apply(this))
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(TemplateExpressionNode node) {
        return isVisited(node) || (!node.startBacktick().isMissing()
                && !node.endBacktick().isMissing());
    }
    
    @Override
    public Boolean transform(QualifiedNameReferenceNode node) {
        /*
            We need to suggest import module code action when,
            ex: "io<cursor>:" hence we have special cased typed binding pattern.
         */
        return isVisited(node) || !node.colon().isMissing() && !node.modulePrefix().isMissing()
                && node.parent() != null
                && (node.parent().kind() == SyntaxKind.TYPED_BINDING_PATTERN || node.parent().apply(this));
    }

    /**
     * Checks whether the syntax is valid.
     *
     * @param node Node at cursor position
     * @return {@link Boolean} True if syntactically correct, false otherwise
     */
    public static boolean validate(Node node) {
        Node validatorNode = node;
        if (node.kind().equals(SyntaxKind.LIST)) {
            validatorNode = node.parent();
        }
        CodeActionNodeValidator nodeValidator = new CodeActionNodeValidator();
        return Optional.ofNullable(validatorNode.apply(nodeValidator)).orElse(true);
    }
}
