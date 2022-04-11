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
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
        return serializeDependencyNodeGraph(dependencyGraph, getGraphName(dependencyGraph));
    }

    public static String serializeDependencyNodeGraph(DependencyGraph<DependencyNode> dependencyGraph,
                                                      String graphName) {

        PackageVersionContainer<DependencyNode> pkgContainer = new PackageVersionContainer<>();
        for (DependencyNode depNode : dependencyGraph.getNodes()) {
            PackageDescriptor pkgDesc = depNode.pkgDesc();
            if (pkgDesc.isLangLibPackage()) {
                continue;
            }
            pkgContainer.add(pkgDesc.org(), pkgDesc.name(), pkgDesc.version(), depNode);
        }

        String firstLine = getFirstLine(graphName);
        String lastLine = "}";
        StringJoiner edges = new StringJoiner("\n\t", "\n\t// Edges\n\t", "\n");
        StringJoiner nodes = new StringJoiner("\n\t", "\t", "\n");

        // First getting the sorted list to produce the ordered list of edges
        PackageContainer<DependencyNode> visitedNodes = new PackageContainer<>();
        List<DependencyNode> sortedList = dependencyGraph.toTopologicallySortedList();
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            DependencyNode depNode = sortedList.get(i);
            if (depNode.pkgDesc().isLangLibPackage()) {
                continue;
            }

            for (DependencyNode directDep : dependencyGraph.getDirectDependencies(depNode)) {
                if (directDep.pkgDesc().isLangLibPackage()) {
                    continue;
                }
                edges.add(getEdgeLine(depNode, directDep));
            }

            if (visitedNodes.contains(depNode.pkgDesc().org(), depNode.pkgDesc().name())) {
                continue;
            }
            visitedNodes.add(depNode.pkgDesc().org(), depNode.pkgDesc().name(), depNode);
            nodes.add(getNodeLine(depNode, pkgContainer));
        }

        return firstLine + nodes.toString() + edges.toString() + lastLine;
    }

    private static String getGraphName(DependencyGraph<DependencyNode> dependencyGraph) {
        DependencyNode graphRoot = dependencyGraph.getRoot();
        if (graphRoot != null) {
            return graphRoot.pkgDesc().toString();
        } else {
            return "app";
        }
    }

    private static String getFirstLine(String graphName) {
        return "digraph \"" + graphName + "\" {\n\tnode [shape=record]\n";
    }

    private static String getEdgeLine(DependencyNode fromNode, DependencyNode toNode) {
        return getNodeLabelInEdge(fromNode.pkgDesc()) + " -> " + getNodeLabelInEdge(toNode.pkgDesc()) + ";";
    }

    private static String getNodeLabelInEdge(PackageDescriptor pkgDesc) {
        return getNodeLabel(pkgDesc) + ":\"" + pkgDesc.version() + "\"";
    }

    private static String getNodeLabel(PackageDescriptor pkgDesc) {
        return "\"" + pkgDesc.org() + "/" + pkgDesc.name() + "\"";
    }

    private static String getNodeLine(DependencyNode depNode,
                                      PackageVersionContainer<DependencyNode> pkgContainer) {
        StringJoiner attrs = new StringJoiner(",", getNodeLabel(depNode.pkgDesc()) + " [", "];");
        String labelAttr = getNodeLabelAttr(depNode, pkgContainer);
        if (depNode.scope() == PackageDependencyScope.TEST_ONLY) {
            attrs.add(getTestOnlyNodeLine(depNode));
        } else if (depNode.pkgDesc().repository().isPresent()) {
            attrs.add(getLocalRepoNodeLine());
        }
        attrs.add(labelAttr);
        return attrs.toString();
    }

    private static String getNodeLabelAttr(DependencyNode depNode,
                                           PackageVersionContainer<DependencyNode> pkgContainer) {
        PackageDescriptor pkgDesc = depNode.pkgDesc();
        Collection<DependencyNode> unSortedDepNodes = pkgContainer.get(pkgDesc.org(), pkgDesc.name());
        List<DependencyNode> sortedDepNodes = sortVersions(unSortedDepNodes);
        StringJoiner labelAttr = new StringJoiner(" | ", "label=\"", "\"");
        for (DependencyNode sortedDepNode : sortedDepNodes) {
            labelAttr.add("<" + sortedDepNode.pkgDesc().version() + "> " + sortedDepNode.pkgDesc());
        }
        return labelAttr.toString();
    }

    private static List<DependencyNode> sortVersions(Collection<DependencyNode> depNodes) {
        List<DependencyNode> sortedList = new ArrayList<>(depNodes);
        sortedList.sort(packageVersionComparator);
        return sortedList;
    }

    private static final Comparator<DependencyNode> packageVersionComparator =
            (depNode1, depNode2) -> {
                PackageVersion latest;
                PackageVersion v1 = depNode1.pkgDesc().version();
                PackageVersion v2 = depNode2.pkgDesc().version();
                SemanticVersion semVer1 = v1.value();
                SemanticVersion semVer2 = v2.value();
                boolean isV1PreReleaseVersion = semVer1.isPreReleaseVersion();
                boolean isV2PreReleaseVersion = semVer2.isPreReleaseVersion();
                if (isV1PreReleaseVersion ^ isV2PreReleaseVersion) {
                    // Only one version is a pre-release version
                    // Return the version which is not a pre-release version
                    latest = isV1PreReleaseVersion ? v2 : v1;
                } else {
                    // Both versions are pre-release versions or both are not pre-release versions
                    // Find the the latest version
                    latest = semVer1.greaterThanOrEqualTo(semVer2) ? v1 : v2;
                }

                if (v1 == latest) {
                    return 1;
                }
                return -1;
            };

    private static String getTestOnlyNodeLine(DependencyNode node) {
        if (node.pkgDesc().repository().isPresent()) {
            return "scope=\"" + node.scope().getValue() + "\", repo=\"local\"";
        } else {
            return "scope=\"" + node.scope().getValue() + "\"";
        }
    }

    private static String getLocalRepoNodeLine() {
        return "repo=\"local\"";
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
