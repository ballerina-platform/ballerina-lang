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
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
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
public class CreateFunctionNodeValidator extends NodeTransformer<Boolean> {

    Node node;
    private Set<Node> visited = new HashSet<>();

    public CreateFunctionNodeValidator(Node node) {
        this.node = node;
    }

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        if (node.parent() == null) {
            return true;
        }
        return node.parent().apply(this);
    }

    @Override
    public Boolean transform(VariableDeclarationNode node) {
        if (!visited.contains(node)) {
            visited.add(node);

            if (node.equalsToken().get().isMissing()) {
                return false;
            }
            return node.typedBindingPattern().apply(this)
                    && node.initializer().get().apply(this);
        }
        return true;
    }
    
    @Override
    public Boolean transform(TypedBindingPatternNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            return node.bindingPattern().apply(this) 
                    && node.typeDescriptor().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(CaptureBindingPatternNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            return !node.variableName().isMissing();
        }
        return true;
    }
    
    @Override
    public Boolean transform(CheckExpressionNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.checkKeyword().isMissing()) {
                return false;
            }
            if (visited.contains(node.expression())) {
                return node.parent().apply(this);
            } else {
                return node.expression().apply(this);
            }
        }
        return true;
    }
    
    @Override
    public Boolean transform(TableTypeDescriptorNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.tableKeywordToken().isMissing()) {
                return false;
            }
            return node.rowTypeParameterNode().apply(this)
                    && node.keyConstraintNode().get().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(SpreadFieldNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.ellipsis().isMissing()) {
                return false;
            }
            if (visited.contains(node.valueExpr())) {
                return node.parent().apply(this);
            } else {
                return node.valueExpr().apply(this);
            }
        }
        return true;
    }

    @Override
    public Boolean transform(TypeParameterNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.ltToken().isMissing() || node.gtToken().isMissing()) {
                return false;
            }
            return node.typeNode().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(KeySpecifierNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.keyKeyword().isMissing() || node.openParenToken().isMissing()
                    || node.closeParenToken().isMissing()) {
                return false;
            }
            return node.fieldNames().stream().allMatch(arg -> arg.apply(this));
        }
        return true;
    }

    @Override
    public Boolean transform(FunctionCallExpressionNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.functionName().isMissing() || node.openParenToken().isMissing() 
                    || node.closeParenToken().isMissing() 
                    || !node.arguments().stream().allMatch(arg -> arg.apply(this))) {
                return false;
            }
            // Check for missing commas
            for (int i = 0 ; i < node.arguments().separatorSize() ; i++) {
                if (node.arguments().getSeparator(i).isMissing()) {
                    return false;
                }
            }
            return node.parent().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.name().isMissing()) {
                return false;
            }
            return node.parent().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(AssignmentStatementNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.equalsToken().isMissing() || node.expression().isMissing()) {
                return false;
            }
            return node.varRef().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(BinaryExpressionNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            
            if (node.operator().isMissing()) {
                return false;
            }
            return node.lhsExpr().apply(this) && node.rhsExpr().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(LetExpressionNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.letKeyword().isMissing() || node.inKeyword().isMissing()
                || !node.letVarDeclarations().stream().allMatch(arg -> arg.apply(this))) {
                return false;
            }
            return node.expression().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(LetVariableDeclarationNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.equalsToken().isMissing()) {
                return false;
            }
            return node.typedBindingPattern().apply(this)
                    && node.expression().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(PositionalArgumentNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (visited.contains(node.expression())) {
                return node.parent().apply(this);
            }
            return node.expression().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(BasicLiteralNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.literalToken().isMissing()) {
                return false;
            }
            return node.parent().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(NamedArgumentNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.equalsToken().isMissing()) {
                return false;
            }
            return node.argumentName().apply(this) && node.expression().apply(this);
        }
        return true;
    }
    
    @Override
    public Boolean transform(RestArgumentNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            if (node.ellipsis().isMissing()) {
                return false;
            }
            return node.expression().apply(this);
        }
        return true;
    }

    @Override
    public Boolean transform(ListConstructorExpressionNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            return !node.openBracket().isMissing() && !node.closeBracket().isMissing()
                    && node.expressions().stream().allMatch(arg -> arg.apply(this));
        }
        return true;
    }
    
    public Boolean validate(NonTerminalNode node) {
        NonTerminalNode validatorNode = node;
        if (node.kind().equals(SyntaxKind.LIST)) {
            validatorNode = node.parent();
        }
        CreateFunctionNodeValidator nodeValidator = new CreateFunctionNodeValidator(validatorNode);
        return node.apply(nodeValidator);
    }
    
}
