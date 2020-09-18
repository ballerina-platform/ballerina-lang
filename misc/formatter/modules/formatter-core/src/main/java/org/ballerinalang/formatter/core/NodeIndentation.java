/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.formatter.core;

import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.ObjectFieldNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.formatter.core.FormatterUtils.getStartColumn;

/**
 * Class that computes the formatting indentation for a particular node.
 */
class NodeIndentation {

    private NodeIndentation() {

    }

    static int builtinSimpleNameReferenceNode(Node node, FormattingOptions options) {
        NonTerminalNode parent = node.parent();
        NonTerminalNode grandParent = null;
        if (parent != null) {
            grandParent = parent.parent();
        }
        boolean addSpaces = true;
        List<SyntaxKind> parentNodes = Arrays.asList(
                        SyntaxKind.FUNCTION_CALL,
                        SyntaxKind.ON_FAIL_CLAUSE,
                        SyntaxKind.TYPE_TEST_EXPRESSION,
                        SyntaxKind.TYPE_PARAMETER,
                        SyntaxKind.TUPLE_TYPE_DESC,
                        SyntaxKind.TYPE_CAST_PARAM,
                        SyntaxKind.INTERSECTION_TYPE_DESC,
                        SyntaxKind.UNION_TYPE_DESC,
                        SyntaxKind.XML_TYPE_DESC);
        if (parent != null && (parentNodes.contains(parent.kind()) || grandParent != null &&
                ((parent.kind() == (SyntaxKind.TYPED_BINDING_PATTERN) &&
                        grandParent.kind() == (SyntaxKind.FOREACH_STATEMENT)) ||
                (parent.kind() == (SyntaxKind.UNION_TYPE_DESC) &&
                        grandParent.kind() == (SyntaxKind.PARAMETERIZED_TYPE_DESC)) ||
                (parent.kind() == (SyntaxKind.OBJECT_FIELD) &&
                        (((ObjectFieldNode) parent).visibilityQualifier().isPresent() ||
                        ((ObjectFieldNode) parent).finalKeyword().isPresent())) ||
                (grandParent.kind() == (SyntaxKind.LOCAL_VAR_DECL) &&
                        grandParent.children().get(1).equals(parent)) ||
                (parent.kind() == (SyntaxKind.RECORD_FIELD) &&
                        ((RecordFieldNode) parent).readonlyKeyword().isPresent()) ||
                grandParent.kind() == (SyntaxKind.FROM_CLAUSE) ||
                grandParent.kind() == (SyntaxKind.PARAMETERIZED_TYPE_DESC)))) {
            addSpaces = false;
        }
        if (parent != null && grandParent != null && (parent.kind() == (SyntaxKind.UNION_TYPE_DESC) &&
                (grandParent.kind() != SyntaxKind.TYPE_CAST_PARAM) && parent.children().get(0).equals(node))) {
            addSpaces = true;
        }
        return getStartColumn(node, addSpaces, options);
    }

    static int simpleNameReferenceNode(Node node, FormattingOptions options) {
        NonTerminalNode parent = node.parent();
        NonTerminalNode grandParent = null;
        if (parent != null) {
            grandParent = parent.parent();
        }
        boolean addSpaces = false;
        ArrayList<SyntaxKind> parentNodes = new ArrayList<>(
                Arrays.asList(
                        SyntaxKind.TYPED_BINDING_PATTERN,
                        SyntaxKind.FIELD_ACCESS,
                        SyntaxKind.LOCK_STATEMENT,
                        SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE,
                        SyntaxKind.RECORD_FIELD,
                        SyntaxKind.MATCH_CLAUSE,
                        SyntaxKind.OPTIONAL_TYPE_DESC,
                        SyntaxKind.ARRAY_TYPE_DESC));
        if (parent != null && ((parentNodes.contains(parent.kind())) || grandParent != null &&
                ((parent.kind() == (SyntaxKind.INDEXED_EXPRESSION) &&
                        grandParent.kind() == (SyntaxKind.ASSIGNMENT_STATEMENT)) ||
                ((parent.kind() == (SyntaxKind.ASYNC_SEND_ACTION) ||
                parent.kind() == (SyntaxKind.ASSIGNMENT_STATEMENT)) &&
                        parent.children().get(0).equals(node))))) {
            addSpaces = true;
        }
        if (parent != null && parent.kind() == SyntaxKind.METHOD_CALL && grandParent != null &&
                grandParent.kind() == SyntaxKind.CALL_STATEMENT) {
            if (((MethodCallExpressionNode) parent).expression().equals(node)) {
                addSpaces = true;
            } else if (((MethodCallExpressionNode) parent).methodName().equals(node)) {
                addSpaces = false;
            }
        }
        if (parent != null && parent.kind() == SyntaxKind.FUNCTION_CALL) {
            addSpaces = false;
        }
        if (parent != null && grandParent != null && parent.kind() == SyntaxKind.INTERSECTION_TYPE_DESC &&
                grandParent.kind() == SyntaxKind.TYPED_BINDING_PATTERN) {
            addSpaces = true;
        }
        if (parent != null && grandParent != null &&
                (grandParent.kind() == (SyntaxKind.OBJECT_FIELD) &&
                        ((ObjectFieldNode) grandParent).visibilityQualifier().isPresent())) {
            addSpaces = false;
        }
        return getStartColumn(node, addSpaces, options);
    }

    static int qualifiedNameReferenceNode(Node node, FormattingOptions options) {
        NonTerminalNode parent = node.parent();
        boolean addSpaces = false;
        if (parent != null && parent.kind() == (SyntaxKind.TYPED_BINDING_PATTERN)) {
            addSpaces = true;
        }
        if (parent != null && parent.kind() == (SyntaxKind.FUNCTION_CALL) && parent.parent() != null &&
                parent.parent().kind() == (SyntaxKind.CALL_STATEMENT)) {
            addSpaces = true;
        }
        return getStartColumn(node, addSpaces, options);
    }

    static int ifElseStatementNode(Node node, FormattingOptions options) {
        NonTerminalNode parent = node.parent();
        if (parent == null) {
            return 0;
        } else if (parent.kind() == (SyntaxKind.ELSE_BLOCK)) {
            return 1;
        } else {
            boolean addSpaces = false;
            return getStartColumn(node, addSpaces, options);
        }
    }

    static int blockStatementNode(Node node, FormattingOptions options) {
        return getStartColumn(node, false, options);
    }
}
