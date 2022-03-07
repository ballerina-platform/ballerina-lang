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
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Node Transformer to check whether the syntax is valid.
 *
 * @since 2.0.0
 */
public class CodeActionNodeValidator extends NodeTransformer<Boolean> {

    private final Set<Node> visited = new HashSet<>();

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        return node.parent() == null || node.parent().apply(this);
    }

    @Override
    public Boolean transform(VariableDeclarationNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return node.equalsToken().isPresent()
                && !node.equalsToken().get().isMissing()
                && node.typedBindingPattern().apply(this)
                && node.initializer().isPresent()
                && node.initializer().get().apply(this);
    }

    @Override
    public Boolean transform(FunctionCallExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        if (node.functionName().isMissing() || node.openParenToken().isMissing()
                || node.closeParenToken().isMissing()
                || !node.arguments().stream().allMatch(arg -> arg.apply(this))) {
            return false;
        }
        // Check for missing commas
        for (int i = 0; i < node.arguments().separatorSize(); i++) {
            if (node.arguments().getSeparator(i).isMissing()) {
                return false;
            }
        }
        return node.parent().apply(this);
    }
    
    @Override
    public Boolean transform(TypedBindingPatternNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return node.bindingPattern().apply(this)
                && node.typeDescriptor().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(CaptureBindingPatternNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.variableName().isMissing() 
                && node.parent().apply(this);
    }
    
    @Override
    public Boolean transform(CheckExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.checkKeyword().isMissing()
            && node.expression().apply(this);
    }
    
    @Override
    public Boolean transform(TableTypeDescriptorNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.tableKeywordToken().isMissing()
                && node.rowTypeParameterNode().apply(this)
                && node.keyConstraintNode().isPresent() 
                && node.keyConstraintNode().get().apply(this);
    }
    
    @Override
    public Boolean transform(TypeParameterNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.ltToken().isMissing() 
                && !node.gtToken().isMissing() 
                && node.typeNode().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(KeySpecifierNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.keyKeyword().isMissing() 
                && !node.openParenToken().isMissing()
                && !node.closeParenToken().isMissing()
                && node.fieldNames().stream().noneMatch(Node::isMissing);
    }

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.name().isMissing()
            && node.parent().apply(this);
    }

    @Override
    public Boolean transform(AssignmentStatementNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.equalsToken().isMissing()
            && node.expression().apply(this)
            && node.varRef().apply(this);
    }

    @Override
    public Boolean transform(BinaryExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.operator().isMissing()
                && node.lhsExpr().apply(this) 
                && node.rhsExpr().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(LetExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.letKeyword().isMissing() 
                && !node.inKeyword().isMissing()
                && node.letVarDeclarations().stream().allMatch(arg -> arg.apply(this))
                && node.expression().apply(this);
    }

    @Override
    public Boolean transform(LetVariableDeclarationNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.equalsToken().isMissing()
                && node.typedBindingPattern().apply(this)
                && node.expression().apply(this);
    }

    @Override
    public Boolean transform(SpreadFieldNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.ellipsis().isMissing()
                && node.valueExpr().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(MappingConstructorExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.openBrace().isMissing() && !node.closeBrace().isMissing()
                && node.fields().stream().allMatch(arg -> arg.apply(this))
                && node.parent().apply(this); 
    }

    @Override
    public Boolean transform(FieldAccessExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return node.expression().apply(this) 
                && !node.dotToken().isMissing()
                && node.fieldName().apply(this)
                && node.parent().apply(this);
    }
    @Override
    public Boolean transform(PositionalArgumentNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return node.expression().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(BasicLiteralNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.literalToken().isMissing()
            && node.parent().apply(this);
    }

    @Override
    public Boolean transform(NamedArgumentNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.equalsToken().isMissing()
                && node.argumentName().apply(this) 
                && node.expression().apply(this)
                && node.parent().apply(this);
    }
    
    @Override
    public Boolean transform(RestArgumentNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.ellipsis().isMissing()
                && node.expression().apply(this)
                && node.parent().apply(this);
    }

    @Override
    public Boolean transform(ListConstructorExpressionNode node) {
        if (visited.contains(node)) {
            return true;
        }
        visited.add(node);
        return !node.openBracket().isMissing() && !node.closeBracket().isMissing()
                && node.expressions().stream().allMatch(arg -> arg.apply(this))
                && node.parent().apply(this);
    }
    
    public static Boolean validate(NonTerminalNode node) {
        NonTerminalNode validatorNode = node;
        if (node.kind().equals(SyntaxKind.LIST)) {
            validatorNode = node.parent();
        }
        CodeActionNodeValidator nodeValidator = new CodeActionNodeValidator();
        return validatorNode.apply(nodeValidator);
    }
}
