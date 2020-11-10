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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class visits each node bottom-up manner until it finds a scoped node such as functionCall, methodCall etc.
 *
 * @since 2.0.0
 */
public class InvocationPositionFinder extends NodeVisitor {
    private final Range range;
    private LinePosition position;
    private NonTerminalNode node;
    private static final Map<Class<?>, Method> SCOPED_NODE_TO_VISIT_METHOD = Arrays.stream(
            InvocationPositionFinder.class.getDeclaredMethods())
            .filter(s -> "visit".equals(s.getName()) && s.getParameterTypes().length > 0)
            .collect(Collectors.toMap(k -> k.getParameterTypes()[0], v -> v));

    public InvocationPositionFinder(Range range) {
        // Mark range without semi-colon
        Position end = new Position(range.getEnd().getLine(), range.getEnd().getCharacter() - 1);
        this.range = new Range(range.getStart(), end);
    }

    public Optional<NonTerminalNode> getNode() {
        return Optional.ofNullable(node);
    }

    public Optional<LinePosition> getPosition() {
        return Optional.ofNullable(position);
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        this.node = functionCallExpressionNode;
        this.position = getNameRefPosition(functionCallExpressionNode.functionName());
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        this.node = methodCallExpressionNode;
        this.position = getNameRefPosition(methodCallExpressionNode.methodName());
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        this.node = remoteMethodCallActionNode;
        this.position = getNameRefPosition(remoteMethodCallActionNode.methodName());
    }

    @Override
    public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
        this.node = fieldAccessExpressionNode;
        this.position = getNameRefPosition(fieldAccessExpressionNode.fieldName());
    }

    private LinePosition getNameRefPosition(NameReferenceNode nameRef) {
        if (nameRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qualifiedNameRef = (QualifiedNameReferenceNode) nameRef;
            return qualifiedNameRef.colon().lineRange().endLine();
        } else if (nameRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode simpleNameRef = (SimpleNameReferenceNode) nameRef;
            return simpleNameRef.name().lineRange().startLine();
        }
        return null;
    }

    @Override
    public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
        this.node = implicitNewExpressionNode;
        this.position = implicitNewExpressionNode.newKeyword().lineRange().startLine();
    }

    @Override
    public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
        this.node = explicitNewExpressionNode;
        this.position = explicitNewExpressionNode.typeDescriptor().lineRange().startLine();
    }

    public void visit(Node node) {
        if (node == null) {
            return;
        }

        // If it is a supported node, visit it
        Method visitMethod = SCOPED_NODE_TO_VISIT_METHOD.get(node.getClass());
        if (visitMethod != null) {
            try {
                visitMethod.invoke(this, node);
            } catch (IllegalAccessException | InvocationTargetException e) {
                // ignore;
            }
        }

        // Visit from bottom-up in tree until we find a supported node or loose the range
        boolean isRangeWithinNode = CommonUtil.isWithinLineRange(this.range.getStart(), node.lineRange()) &&
                CommonUtil.isWithinLineRange(this.range.getEnd(), node.lineRange());
        if (!isRangeWithinNode) {
            visit(node.parent());
        }
    }
}
