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

import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.ObjectFieldNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that stores the formatting metrics for a particular node.
 */
class NodeFormattingMetrics {

    private Node node;
    private NonTerminalNode parent;
    private NonTerminalNode grandParent;

    NodeFormattingMetrics(Node node) {

        this.node = node;
        this.parent = node.parent();
        if (parent != null) {
            this.grandParent = parent.parent();
        } else {
            this.grandParent = null;
        }
        setNodeMetrics();
    }

    private void setNodeMetrics() {
        if (node instanceof BuiltinSimpleNameReferenceNode) {
            handleBuiltinSimpleNameReferenceNode();
        }
    }

    boolean handleBuiltinSimpleNameReferenceNode() {
        boolean addSpaces = true;
        ArrayList<SyntaxKind> parentNodes = new ArrayList<>(
                Arrays.asList(
                        SyntaxKind.FUNCTION_CALL,
                        SyntaxKind.TYPE_TEST_EXPRESSION,
                        SyntaxKind.TYPE_PARAMETER,
                        SyntaxKind.TYPE_CAST_PARAM,
                        SyntaxKind.UNION_TYPE_DESC,
                        SyntaxKind.XML_TYPE_DESC));
        if (parent != null && (parentNodes.contains(parent.kind()) || grandParent != null &&
                ((parent.kind().equals(SyntaxKind.TYPED_BINDING_PATTERN) &&
                        grandParent.kind().equals(SyntaxKind.FOREACH_STATEMENT)) ||
                (parent.kind().equals(SyntaxKind.UNION_TYPE_DESC) &&
                        grandParent.kind().equals(SyntaxKind.PARAMETERIZED_TYPE_DESC)) ||
                (parent.kind().equals(SyntaxKind.OBJECT_FIELD) &&
                        ((ObjectFieldNode) parent).visibilityQualifier().isPresent()) ||
                (grandParent.kind().equals(SyntaxKind.LOCAL_VAR_DECL) &&
                        grandParent.children().get(1).equals(parent)) ||
                grandParent.kind().equals(SyntaxKind.FROM_CLAUSE) ||
                grandParent.kind().equals(SyntaxKind.PARAMETERIZED_TYPE_DESC)))) {
            addSpaces = false;
        }
        if (parent != null && grandParent != null && (parent.kind().equals(SyntaxKind.UNION_TYPE_DESC) &&
                !grandParent.kind().equals(SyntaxKind.TYPE_CAST_PARAM) && parent.children().get(0).equals(node))) {
            addSpaces = true;
        }
        return addSpaces;
    }

    boolean handleSimpleNameReferenceNode() {
        boolean addSpaces = false;
        ArrayList<SyntaxKind> parentNodes = new ArrayList<>(
                Arrays.asList(
                        SyntaxKind.TYPED_BINDING_PATTERN,
                        SyntaxKind.FIELD_ACCESS,
                        SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE,
                        SyntaxKind.OPTIONAL_TYPE_DESC,
                        SyntaxKind.ARRAY_TYPE_DESC));
        if (parent != null && ((parentNodes.contains(parent.kind())) || grandParent != null &&
                ((parent.kind().equals(SyntaxKind.INDEXED_EXPRESSION) &&
                        grandParent.kind().equals(SyntaxKind.ASSIGNMENT_STATEMENT)) ||
                ((parent.kind().equals(SyntaxKind.ASYNC_SEND_ACTION) ||
                parent.kind().equals(SyntaxKind.ASSIGNMENT_STATEMENT)) &&
                        parent.children().get(0).equals(node))))) {
            addSpaces = true;
        }
        if (parent != null && grandParent != null &&
                (grandParent.kind().equals(SyntaxKind.OBJECT_FIELD) &&
                        ((ObjectFieldNode) grandParent).visibilityQualifier().isPresent())) {
            addSpaces = false;
        }
        return addSpaces;
    }

    boolean handleQualifiedNameReferenceNode() {
        boolean addSpaces = false;
        if (parent != null && parent.kind().equals(SyntaxKind.TYPED_BINDING_PATTERN)) {
            addSpaces = true;
        }
        return addSpaces;
    }
}
