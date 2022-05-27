/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.comparator;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.Optional;

/**
 * Comparator implementation for Ballerina function parameters.
 *
 * @since 2201.2.0
 */
public class ParamComparator extends NodeComparator<ParameterNode> {

    ParamComparator(ParameterNode newNode, ParameterNode oldNode) {
        super(newNode, oldNode);
    }

    @Override
    public Optional<? extends Diff> computeDiff() {
        NodeDiffBuilder paramDiffs = new NodeDiffImpl.Builder<>(newNode, oldNode);
        compareParamKind(newNode, oldNode).ifPresent(paramDiffs::withChildDiff);
        compareParamType(newNode, oldNode).ifPresent(paramDiffs::withChildDiff);
        compareParamValue(newNode, oldNode).ifPresent(paramDiffs::withChildDiff);
        compareParamAnnotations(newNode, oldNode).ifPresent(paramDiffs::withChildDiff);

        return paramDiffs.build();
    }

    private Optional<NodeDiffImpl<Node>> compareParamAnnotations(ParameterNode newNode, ParameterNode oldNode) {
        // Todo: implement parameter annotation comparison
        return Optional.empty();
    }

    private Optional<? extends Diff> compareParamValue(ParameterNode newParam, ParameterNode oldParam) {
        if (newParam.kind() != SyntaxKind.DEFAULTABLE_PARAM || oldParam.kind() != SyntaxKind.DEFAULTABLE_PARAM) {
            return Optional.empty();
        }

        Node newExpr = ((DefaultableParameterNode) newParam).expression();
        Node oldExpr = ((DefaultableParameterNode) oldParam).expression();
        if (newExpr.toSourceCode().trim().equals(oldExpr.toSourceCode().trim())) {
            return Optional.empty();
        }

        NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(newParam, oldParam);
        diffBuilder.withMessage(String.format("default value of parameter '%s' is changed from '%s' to '%s'",
                ((DefaultableParameterNode) newParam).paramName(), oldExpr.toSourceCode().trim(),
                newExpr.toSourceCode().trim()));

        if (newExpr instanceof BasicLiteralNode && oldExpr instanceof BasicLiteralNode) {
            // Todo: should the version impact be major or minor?
            diffBuilder.withVersionImpact(SemverImpact.MINOR);
        } else {
            // version impact can be ambiguous for the expressions that are not basic literals(i.e. variable references)
            diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
        }
        return diffBuilder.build();
    }

    private Optional<? extends Diff> compareParamType(ParameterNode newParam, ParameterNode oldParam) {
        Node newType = getParamType(newParam);
        Node oldType = getParamType(oldParam);

        if (newType == null || oldType == null) {
            return Optional.empty();
        } else if (!newType.toSourceCode().trim().equals(oldType.toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiffBuilder diffBuilder = new NodeDiffImpl.Builder<>(newParam, oldParam);
            diffBuilder = diffBuilder.withVersionImpact(SemverImpact.AMBIGUOUS);
            diffBuilder.withMessage(String.format("type of parameter '%s' is changed from '%s' to '%s'",
                    getParamName(newParam), oldType.toSourceCode().trim(), newType.toSourceCode().trim()));
            return diffBuilder.build();
        }

        return Optional.empty();
    }

    private Optional<? extends Diff> compareParamKind(ParameterNode newParam, ParameterNode oldParam) {
        if (newNode.kind() == oldNode.kind()) {
            return Optional.empty();
        }

        NodeDiffBuilder paramDiffBuilder = new NodeDiffImpl.Builder<>(newParam, oldParam);
        paramDiffBuilder = paramDiffBuilder.withMessage(String.format("kind of parameter '%s' is changed from '%s' to" +
                " '%s'", getParamName(newParam), oldParam.kind(), newParam.kind()));
        if (newParam.kind() == SyntaxKind.REQUIRED_PARAM && oldParam.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            paramDiffBuilder.withVersionImpact(SemverImpact.MAJOR);
        } else if (newParam.kind() == SyntaxKind.DEFAULTABLE_PARAM && oldParam.kind() == SyntaxKind.REQUIRED_PARAM) {
            paramDiffBuilder.withVersionImpact(SemverImpact.MINOR);
        } else if (newParam.kind() == SyntaxKind.DEFAULTABLE_PARAM && oldParam.kind() == SyntaxKind.REST_PARAM) {
            paramDiffBuilder.withVersionImpact(SemverImpact.MAJOR);
        } else if (newParam.kind() == SyntaxKind.REST_PARAM && oldParam.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            paramDiffBuilder.withVersionImpact(SemverImpact.MAJOR);
        } else if (newParam.kind() == SyntaxKind.REQUIRED_PARAM && oldParam.kind() == SyntaxKind.REST_PARAM) {
            paramDiffBuilder.withVersionImpact(SemverImpact.MAJOR);
        } else if (newParam.kind() == SyntaxKind.REST_PARAM && oldParam.kind() == SyntaxKind.REQUIRED_PARAM) {
            paramDiffBuilder.withVersionImpact(SemverImpact.MAJOR);
        }

        return paramDiffBuilder.build();
    }

    private Node getParamType(ParameterNode paramNode) {
        if (paramNode.kind() == SyntaxKind.REQUIRED_PARAM) {
            return ((RequiredParameterNode) paramNode).typeName();
        } else if (paramNode.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            return ((DefaultableParameterNode) paramNode).typeName();
        } else if (paramNode.kind() == SyntaxKind.REST_PARAM) {
            return ((RestParameterNode) paramNode).typeName();
        } else {
            return null;
        }
    }

    private String getParamName(ParameterNode paramNode) {
        Node paramName = null;
        if (paramNode.kind() == SyntaxKind.REQUIRED_PARAM) {
            paramName = ((RequiredParameterNode) paramNode).paramName().orElse(null);
        } else if (paramNode.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            paramName = ((DefaultableParameterNode) paramNode).paramName().orElse(null);
        } else if (paramNode.kind() == SyntaxKind.REST_PARAM) {
            paramName = ((RestParameterNode) paramNode).paramName().orElse(null);
        }

        return paramName != null ? paramName.toSourceCode().trim() : "unknown";
    }
}
