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
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyGraph.DependencyGraphBuilder;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.util.List;
import java.util.StringJoiner;

/**
 * Contains utilities that serialize {@code DependencyGraph} instances to DOT graph format.
 *
 * @since 2.0.0
 */
public class DotGraphs {

    private DotGraphs() {
    }

    public static String serializeResolvedPackageDependencyGraph(
            DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
        return serializeDependencyNodeGraph(toDependencyNodeGraph(dependencyGraph));
    }

    public static String serializeDependencyNodeGraph(DependencyGraph<DependencyNode> dependencyGraph) {
        String firstLine = getFirstLine(dependencyGraph);
        String lastLine = "}";
        StringJoiner edges = new StringJoiner("\n\t", "\t", "\n\n");
        StringJoiner nodes = new StringJoiner("\n\t", "\t", "\n");

        // First getting the sorted list to produce the ordered list of edges
        List<DependencyNode> sortedList = dependencyGraph.toTopologicallySortedList();
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            DependencyNode depNode = sortedList.get(i);
            for (DependencyNode directDep : dependencyGraph.getDirectDependencies(depNode)) {
                edges.add(getEdgeLine(depNode, directDep));
            }
        }

        for (DependencyNode depNode : dependencyGraph.getNodes()) {
            if (depNode.scope() == PackageDependencyScope.TEST_ONLY) {
                nodes.add(getTestOnlyNodeLine(depNode));
            } else if (depNode.pkgDesc().repository().isPresent()) {
                nodes.add(getLocalRepoNodeLine(depNode));
            }

            if (!sortedList.contains(depNode)) {
                for (DependencyNode directDep : dependencyGraph.getDirectDependencies(depNode)) {
                    edges.add(getEdgeLine(depNode, directDep));
                }
            }
        }

        return firstLine + edges.toString() + nodes.toString() + lastLine;
    }

    private static String getFirstLine(DependencyGraph<DependencyNode> dependencyGraph) {
        String graphName;
        DependencyNode graphRoot = dependencyGraph.getRoot();
        if (graphRoot != null) {
            graphName = graphRoot.pkgDesc().toString();
        } else {
            graphName = "app";
        }

        return "digraph \"" + graphName + "\" {\n";
    }

    private static String getEdgeLine(DependencyNode fromNode, DependencyNode toNode) {
        return "\"" + fromNode.pkgDesc().toString() + "\" -> \"" + toNode.pkgDesc().toString() + "\"";
    }

    private static String getTestOnlyNodeLine(DependencyNode node) {
        String attr;
        if (node.pkgDesc().repository().isPresent()) {
            attr = " [scope= \"" + node.scope().getValue() + "\", repo = \"local\"]";
        } else {
            attr = " [scope= \"" + node.scope().getValue() + "\"]";
        }
        return getNodeLine(node.pkgDesc(), attr);
    }

    private static String getLocalRepoNodeLine(DependencyNode node) {
        return getNodeLine(node.pkgDesc(), " [repo = \"local\"]");
    }

    private static String getNodeLine(PackageDescriptor pkgDesc, String attr) {
        return "\"" + pkgDesc + "\"" + attr;
    }

    private static DependencyGraph<DependencyNode> toDependencyNodeGraph(
            DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
        DependencyNode rootNode = toDependencyNode(dependencyGraph.getRoot());
        DependencyGraphBuilder<DependencyNode> graphBuilder = DependencyGraphBuilder.getBuilder(rootNode);

        for (ResolvedPackageDependency resPkgNode : dependencyGraph.getNodes()) {
            DependencyNode depNode = toDependencyNode(resPkgNode);
            for (ResolvedPackageDependency resPkgDirectDep : dependencyGraph.getDirectDependencies(resPkgNode)) {
                DependencyNode depNodeDirectDep = toDependencyNode(resPkgDirectDep);
                graphBuilder.addDependency(depNode, depNodeDirectDep);
            }
        }

        return graphBuilder.build();
    }

    private static DependencyNode toDependencyNode(ResolvedPackageDependency resPkgDep) {
        return new DependencyNode(resPkgDep.packageInstance().descriptor(),
                resPkgDep.scope(), resPkgDep.dependencyResolvedType());
    }
}
