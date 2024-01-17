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
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.NodeDiffBuilder;
import io.ballerina.semver.checker.diff.NodeDiffImpl;
import io.ballerina.semver.checker.diff.NodeListDiffImpl;
import io.ballerina.semver.checker.diff.SemverImpact;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Comparator implementation for Ballerina function parameter lists.
 *
 * @since 2201.2.0
 */
public class ParamListComparator extends NodeListComparator<List<ParameterNode>> {

    public ParamListComparator(List<ParameterNode> newNodes, List<ParameterNode> oldNodes) {
        super(newNodes, oldNodes);
    }

    @Override
    public Optional<? extends NodeListDiffImpl<? extends Node>> computeDiff() {
        NodeListDiffImpl.Builder<ParameterNode> paramDiffs = new NodeListDiffImpl.Builder<>(newNodesList, oldNodesList);

        Map<String, ParameterNode> newParams = newNodesList.stream()
                .collect(Collectors.toMap(this::getParameterName, Function.identity()));
        Map<String, ParameterNode> oldParams = oldNodesList.stream()
                .collect(Collectors.toMap(this::getParameterName, Function.identity()));

        // Todo: write a separate diff extractor to detect parameter order changes.
        DiffExtractor<ParameterNode> paramDiffExtractor = new DiffExtractor<>(newParams, oldParams);

        // Computes and populate diffs for newly added parameters.
        paramDiffExtractor.getAdditions().forEach((paramName, paramNode) -> {
            NodeDiffBuilder paramDiffBuilder = new NodeDiffImpl.Builder<>(paramNode, null);
            switch (paramNode.kind()) {
                case REQUIRED_PARAM:
                    paramDiffBuilder.withMessage("new required parameter '" + paramName + "' is added")
                            .withVersionImpact(SemverImpact.MAJOR);
                    break;
                case DEFAULTABLE_PARAM:
                    paramDiffBuilder.withMessage("new defaultable parameter '" + paramName + "' is added")
                            .withVersionImpact(SemverImpact.MINOR);
                    break;
                case REST_PARAM:
                    paramDiffBuilder.withMessage("new rest parameter '" + paramName + "' is added")
                            .withVersionImpact(SemverImpact.MINOR);
                    break;
                default:
                    paramDiffBuilder.withMessage("new parameter '" + paramName + "' is added")
                            .withVersionImpact(SemverImpact.MAJOR);
            }
            paramDiffBuilder.build().ifPresent(paramDiffs::withChildDiff);
        });

        // Computes and populate diffs for removed parameters.
        paramDiffExtractor.getRemovals().forEach((paramName, paramNode) -> {
            NodeDiffBuilder paramDiffBuilder = new NodeDiffImpl.Builder<>(null, paramNode);
            paramDiffBuilder = paramDiffBuilder.withVersionImpact(SemverImpact.MAJOR);
            switch (paramNode.kind()) {
                case REQUIRED_PARAM:
                    paramDiffBuilder.withMessage("required parameter '" + paramName + "' is removed");
                    break;
                case DEFAULTABLE_PARAM:
                    paramDiffBuilder.withMessage("defaultable parameter '" + paramName + "' is removed");
                    break;
                case REST_PARAM:
                    paramDiffBuilder.withMessage("rest parameter '" + paramName + "' is removed");
                    break;
                default:
                    paramDiffBuilder.withMessage("parameter '" + paramName + "' is removed");
            }
            paramDiffBuilder.build().ifPresent(paramDiffs::withChildDiff);
        });

        // Computes and populate diffs for modified parameters.
        paramDiffExtractor.getCommons().forEach((name, params) -> {
            ParamComparator paramComparator = new ParamComparator(params.getKey(), params.getValue());
            paramComparator.computeDiff().ifPresent(paramDiffs::withChildDiff);
        });

        return paramDiffs.build();
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
