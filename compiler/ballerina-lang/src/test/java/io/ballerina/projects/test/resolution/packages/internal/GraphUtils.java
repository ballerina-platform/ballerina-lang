/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test.resolution.packages.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains a list of utility methods to work with Ballerina dependency graphs.
 *
 * @since 2.0.0
 */
public class GraphUtils {

    private GraphUtils() {
    }

    private static final Comparator<DependencyNode> pkgDescComparator =
            Comparator.comparing(depNode -> depNode.pkgDesc().toString());

    public static DependencyGraph<PackageDescriptor> createPkgDescGraph(DependencyGraph<DependencyNode> depNodeGraph) {
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();
        for (DependencyNode rootNode : depNodeGraph.getNodes()) {
            Collection<DependencyNode> directDependencies = depNodeGraph.getDirectDependencies(rootNode);
            for (DependencyNode directDependency : directDependencies) {
                graphBuilder.addDependency(rootNode.pkgDesc(), directDependency.pkgDesc());
            }
        }
        return graphBuilder.build();
    }

    public static GraphComparisonResult compareGraph(DependencyGraph<DependencyNode> actualGraph,
                                                     DependencyGraph<DependencyNode> expectedGraph) {

        List<DependencyNode> actualGraphNodes = getSortedCollection(actualGraph.getNodes());
        List<DependencyNode> expectedGraphNodes = getSortedCollection(expectedGraph.getNodes());
        if (actualGraphNodes.size() != expectedGraphNodes.size()) {
            return reportGraphsWithDifferentNumberOfNodes(actualGraphNodes, expectedGraphNodes);
        }

        if (!actualGraphNodes.equals(expectedGraphNodes)) {
            return reportGraphsWithDifferentNumberOfNodes(actualGraphNodes, expectedGraphNodes);
        }

        for (DependencyNode actualGraphNode : actualGraphNodes) {
            List<DependencyNode> actualGraphDeps = getSortedCollection(
                    actualGraph.getDirectDependencies(actualGraphNode));
            List<DependencyNode> expectedGraphDeps = getSortedCollection(
                    expectedGraph.getDirectDependencies(actualGraphNode));
            if (!actualGraphDeps.equals(expectedGraphDeps)) {
                GraphComparisonResult compResult = new GraphComparisonResult(false);
                compResult.addDiagnostic("Package " + actualGraphNode.toString() +
                        "'s dependencies have changed in the actual graph");
                compResult.addDiagnostic("Actual:   " + actualGraphNode.toString() + " -> " + actualGraphDeps);
                compResult.addDiagnostic("Expected: " + actualGraphNode.toString() + " -> " + expectedGraphDeps);
                return compResult;
            }
        }

        return GraphComparisonResult.IDENTICAL_GRAPH_COMP_RESULT;
    }

    private static List<DependencyNode> getSortedCollection(Collection<DependencyNode> nodes) {
        return nodes.stream().sorted(pkgDescComparator).collect(Collectors.toList());
    }

    private static GraphComparisonResult reportGraphsWithDifferentNumberOfNodes(
            List<DependencyNode> actualGraphNodes,
            List<DependencyNode> expectedGraphNodes) {
        GraphComparisonResult comparisonResult = new GraphComparisonResult(false);
        comparisonResult.addDiagnostic("Actual and expected graphs are not equal");
        List<DependencyNode> expectedDiff = new ArrayList<>(expectedGraphNodes);
        expectedDiff.removeAll(actualGraphNodes);
        if (!expectedDiff.isEmpty()) {
            comparisonResult.addDiagnostic("Actual graph does not contain some packages: " + expectedDiff);
        }

        List<DependencyNode> actualDiff = new ArrayList<>(actualGraphNodes);
        actualDiff.removeAll(expectedGraphNodes);
        if (!actualDiff.isEmpty()) {
            comparisonResult.addDiagnostic("Actual graph contains additional packages: " + actualDiff);
        }

        comparisonResult.addDiagnostic("Actual (" + actualGraphNodes.size() + "):   " + actualGraphNodes);
        comparisonResult.addDiagnostic("Expected (" + expectedGraphNodes.size() + "): " + expectedGraphNodes);
        return comparisonResult;
    }

}
