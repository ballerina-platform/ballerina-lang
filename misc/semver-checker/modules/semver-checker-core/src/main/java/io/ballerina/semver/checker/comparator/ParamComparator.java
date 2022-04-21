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
import io.ballerina.semver.checker.diff.CompatibilityLevel;
import io.ballerina.semver.checker.diff.DiffType;
import io.ballerina.semver.checker.diff.NodeDiff;

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
    public Optional<NodeDiff<Node>> computeDiff() {
        NodeDiff<Node> paramDiffs = new NodeDiff<>(newNode, oldNode);
        if (newNode.kind() != oldNode.kind()) {
            compareParamKind(newNode, oldNode).ifPresent(paramDiffs::addChildDiff);
        }
        compareParamType(newNode, oldNode).ifPresent(paramDiffs::addChildDiff);
        compareParamValue(newNode, oldNode).ifPresent(paramDiffs::addChildDiff);
        compareParamAnnotations(newNode, oldNode).ifPresent(paramDiffs::addChildDiff);

        if (!paramDiffs.getChildDiffs().isEmpty()) {
            paramDiffs.computeCompatibilityLevel();
            return Optional.of(paramDiffs);
        }

        return Optional.empty();
    }

    private Optional<NodeDiff<Node>> compareParamAnnotations(ParameterNode newNode, ParameterNode oldNode) {
        // Todo: implement parameter annotation comparison
        return Optional.empty();
    }

    private Optional<NodeDiff<Node>> compareParamValue(ParameterNode newParam, ParameterNode oldParam) {
        if (newParam.kind() != SyntaxKind.DEFAULTABLE_PARAM || oldParam.kind() != SyntaxKind.DEFAULTABLE_PARAM) {
            return Optional.empty();
        }

        Node newExpr = ((DefaultableParameterNode) newParam).expression();
        Node oldExpr = ((DefaultableParameterNode) oldParam).expression();
        if (newExpr.toSourceCode().trim().equals(oldExpr.toSourceCode().trim())) {
            return Optional.empty();
        }

        NodeDiff<Node> diff = new NodeDiff<>(newParam, oldParam);
        diff.setMessage(String.format("default value of the parameter: '%s' is changed from: '%s' to: '%s'",
                ((DefaultableParameterNode) newParam).paramName(), oldExpr.toSourceCode().trim(),
                newExpr.toSourceCode().trim()));

        if (newExpr instanceof BasicLiteralNode && oldExpr instanceof BasicLiteralNode) {
            // Todo: should the compatibility level be major or minor?
            diff.setCompatibilityLevel(CompatibilityLevel.MINOR);
        } else {
            // semver compatibility level can be ambiguous for the expressions that are not basic literals.
            // (i.e. variable references)
            diff.setCompatibilityLevel(CompatibilityLevel.AMBIGUOUS);
        }
        return Optional.of(diff);
    }

    private Optional<NodeDiff<Node>> compareParamType(ParameterNode newParam, ParameterNode oldParam) {
        Node newType = extractParamType(newParam);
        Node oldType = extractParamType(oldParam);

        if (newType == null) {
            addErrorDiagnostic("unsupported param type: " + newParam.kind() + " detected");
        } else if (oldType == null) {
            addErrorDiagnostic("unsupported param type: " + newParam.kind() + " detected");
        } else if (!newType.toSourceCode().trim().equals(oldType.toSourceCode().trim())) {
            // Todo: improve type changes validation using semantic APIs
            NodeDiff<Node> diff = new NodeDiff<>(newParam, oldNode, CompatibilityLevel.AMBIGUOUS);
            diff.setMessage(String.format("parameter type changed from: '%s' to: %s", oldType.toSourceCode(),
                    newType.toSourceCode()));
            return Optional.of(diff);
        }

        return Optional.empty();
    }

    private Optional<NodeDiff<Node>> compareParamKind(ParameterNode newParam, ParameterNode oldParam) {
        NodeDiff<Node> paramDiff = new NodeDiff<>(newParam, oldParam);
        paramDiff.setMessage("parameter kind is changed from '" + newParam.kind() + "' to '" + oldParam.kind());
        if (newParam.kind() == SyntaxKind.REQUIRED_PARAM && oldParam.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
        } else if (newParam.kind() == SyntaxKind.DEFAULTABLE_PARAM && oldParam.kind() == SyntaxKind.REQUIRED_PARAM) {
            paramDiff.setCompatibilityLevel(CompatibilityLevel.MINOR);
        } else if (newParam.kind() == SyntaxKind.DEFAULTABLE_PARAM && oldParam.kind() == SyntaxKind.REST_PARAM) {
            paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
        } else if (newParam.kind() == SyntaxKind.REST_PARAM && oldParam.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
            paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
        } else if (newParam.kind() == SyntaxKind.REQUIRED_PARAM && oldParam.kind() == SyntaxKind.REST_PARAM) {
            paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
        } else if (newParam.kind() == SyntaxKind.REST_PARAM && oldParam.kind() == SyntaxKind.REQUIRED_PARAM) {
            paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
        }

        return Optional.of(paramDiff);
    }

    private Node extractParamType(ParameterNode paramNode) {
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
}
