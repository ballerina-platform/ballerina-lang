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

import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.semver.checker.diff.CompatibilityLevel;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.DiffType;
import io.ballerina.semver.checker.diff.NodeDiff;
import io.ballerina.semver.checker.diff.NodeListDiff;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParamListComparator extends NodeListComparator<List<ParameterNode>> {

    public ParamListComparator(List<ParameterNode> newNodes, List<ParameterNode> oldNodes) {
        super(newNodes, oldNodes);
    }

    @Override
    public Optional<NodeListDiff<ParameterNode>> computeDiff() {
        NodeListDiff<ParameterNode> paramDiffs = new NodeListDiff<>(newNodesList, oldNodesList);

        Map<String, ParameterNode> newParams = newNodesList.stream()
                .collect(Collectors.toMap(this::getParameterName, Function.identity()));
        Map<String, ParameterNode> oldParams = oldNodesList.stream()
                .collect(Collectors.toMap(this::getParameterName, Function.identity()));

        // Todo: write a separate diff extractor to detect changes on parameter order as well.
        DiffExtractor<ParameterNode> paramDiffExtractor = new DiffExtractor<>(newParams, oldParams);

        // Computes and populate diffs for newly added parameters.
        paramDiffExtractor.getAdditions().forEach((paramName, paramNode) -> {
            NodeDiff<Node> paramDiff = new NodeDiff<>(paramNode, null, DiffType.NEW, CompatibilityLevel.UNKNOWN);
            switch (paramNode.kind()) {
                case REQUIRED_PARAM:
                    paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
                    paramDiff.setMessage("new required parameter added");
                    return;
                case DEFAULTABLE_PARAM:
                    paramDiff.setCompatibilityLevel(CompatibilityLevel.MINOR);
                    paramDiff.setMessage("new defaultable parameter added");
                    return;
                case REST_PARAM:
                    paramDiff.setCompatibilityLevel(CompatibilityLevel.MINOR);
                    paramDiff.setMessage("new rest parameter added");
                    return;
                default:
                    paramDiff.setCompatibilityLevel(CompatibilityLevel.MAJOR);
                    paramDiff.setMessage("new unknown parameter added");
            }
            paramDiffs.addChildDiff(paramDiff);
        });

        // Computes and populate diffs for removed parameters.
        paramDiffExtractor.getRemovals().forEach((paramName, paramNode) -> {
            NodeDiff<Node> paramDiff = new NodeDiff<>(null, paramNode, DiffType.REMOVED, CompatibilityLevel.MAJOR);
            switch (paramNode.kind()) {
                case REQUIRED_PARAM:
                    paramDiff.setMessage("required parameter removed");
                    return;
                case DEFAULTABLE_PARAM:
                    paramDiff.setMessage("defaultable parameter removed");
                    return;
                case REST_PARAM:
                    paramDiff.setMessage("rest parameter removed");
                    return;
                default:
                    paramDiff.setMessage("unknown parameter removed");
            }
            paramDiffs.addChildDiff(paramDiff);
        });

        // Computes and populate diffs for modified parameters.
        paramDiffExtractor.getCommons().forEach((name, params) -> {
            ParamComparator paramComparator = new ParamComparator(params.getKey(), params.getValue());
            paramComparator.computeDiff().ifPresent(paramDiffs::addChildDiff);
        });

        return Optional.of(paramDiffs);
    }

    private String getParameterName(Node paramNode) {
        switch (paramNode.kind()) {
            case REQUIRED_PARAM:
                return ((RequiredParameterNode) paramNode).paramName().orElseThrow().text();
            case DEFAULTABLE_PARAM:
                return ((DefaultableParameterNode) paramNode).paramName().orElseThrow().text();
            case REST_PARAM:
                return ((RestParameterNode) paramNode).paramName().orElseThrow().text();
            default:
                return ""; // Todo
        }
    }
}
