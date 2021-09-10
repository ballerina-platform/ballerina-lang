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

import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyGraph.DependencyGraphBuilder;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.environment.PackageRepository;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Reads repository dot files and build test package repository instances.
 *
 * @since 2.0.0
 */
public class PackageRepositoryBuilder {

    private PackageRepositoryBuilder() {
    }

    public static PackageRepository build(TestCaseFilePaths filePaths, RepositoryKind repoKind) {
        Optional<Path> repoDotFilePath = getRepPath(filePaths, repoKind);
        if (repoDotFilePath.isEmpty()) {
            return DefaultPackageRepository.EMPTY_REPO;
        }

        return buildInternal(repoDotFilePath.get(), repoKind);
    }

    private static Optional<Path> getRepPath(TestCaseFilePaths filePaths, RepositoryKind repoKind) {
        switch (repoKind) {
            case DIST:
                return filePaths.distRepoPath();
            case CENTRAL:
                return filePaths.centralRepoPath();
            case LOCAL:
                return filePaths.localRepoPath();
            default:
                throw new IllegalStateException("Unsupported package repository kind " + repoKind);
        }
    }

    private static PackageRepository buildInternal(Path repoDotFilePath, RepositoryKind repoKind) {
        PackageContainer<PackageDescriptor> pkgContainer = new PackageContainer<>();
        Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap = new HashMap<>();
        GraphNodeMarker nodeMarker = new GraphNodeMarker();
        DependencyGraphBuilder<PackageDescriptor> graphBuilder =
                DependencyGraphBuilder.getBuilder();

        MutableGraph repoDotGraph = DotGraphUtils.createGraph(repoDotFilePath);
        for (MutableGraph packageGraph : repoDotGraph.graphs()) {
            PackageDescriptor rootNode = DotGraphUtils.getPkgDescFromNode(packageGraph.name().toString());
            pkgContainer.add(rootNode.org(), rootNode.name(), rootNode.version(), rootNode);
            nodeMarker.subGraphFound(rootNode);


            graphBuilder.add(rootNode);
            for (Link edge : packageGraph.edges()) {
                PackageDescriptor dependent = DotGraphUtils.getPkgDescFromNode(edge.from().name().toString());
                nodeMarker.nodeFound(dependent);
                PackageDescriptor dependency = DotGraphUtils.getPkgDescFromNode(edge.to().name().toString());
                nodeMarker.nodeFound(dependency);
                graphBuilder.addDependency(dependent, dependency);
            }

        }

        List<PackageDescriptor> unmarkedNodes = nodeMarker.getUnmarkedNodes();
        if (repoKind != RepositoryKind.LOCAL && !unmarkedNodes.isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (PackageDescriptor unmarkedNode : unmarkedNodes) {
                stringJoiner.add(unmarkedNode.toString());
            }
            throw new IllegalStateException("Some packages (" + stringJoiner +
                    ") do no have subgraph entries in " + repoDotFilePath);
        }

        DependencyGraph<PackageDescriptor> repoGraph = graphBuilder.build();
        for (PackageDescriptor pkgDesc : pkgContainer.getAll()) {
            DependencyGraphBuilder<PackageDescriptor> builder = DependencyGraphBuilder.getBuilder();
            buildPkgDescGraph(pkgDesc, builder, repoGraph);
            graphMap.put(pkgDesc, builder.build());
        }

        return buildInternal(pkgContainer, graphMap, repoKind);
    }

    private static void buildPkgDescGraph(PackageDescriptor pkgDesc,
                                          DependencyGraphBuilder<PackageDescriptor> graphBuilder,
                                          DependencyGraph<PackageDescriptor> repoGraph) {
        // Can we optimize this by checking whether pkgDesc already exists in the graphBuilder...
        for (PackageDescriptor dependency : repoGraph.getDirectDependencies(pkgDesc)) {
            buildPkgDescGraph(dependency, graphBuilder, repoGraph);
            graphBuilder.addDependency(pkgDesc, dependency);
        }
    }

    private static PackageRepository buildInternal(PackageContainer<PackageDescriptor> pkgContainer,
                                                   Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap,
                                                   RepositoryKind repoKind) {
        switch (repoKind) {
            case LOCAL:
                return new LocalPackageRepository(pkgContainer, graphMap);
            case CENTRAL:
            case DIST:
                return new DefaultPackageRepository(pkgContainer, graphMap);
            default:
                throw new IllegalStateException("Unsupported package repository kind " + repoKind);
        }
    }

    private static class GraphNodeMarker {
        Map<PackageDescriptor, Boolean> marker = new HashMap<>();

        void subGraphFound(PackageDescriptor pkgDesc) {
            marker.put(pkgDesc, Boolean.TRUE);
        }

        void nodeFound(PackageDescriptor pkgDesc) {
            if (marker.containsKey(pkgDesc) && marker.get(pkgDesc) == Boolean.TRUE) {
                return;
            }

            marker.putIfAbsent(pkgDesc, Boolean.FALSE);
        }

        List<PackageDescriptor> getUnmarkedNodes() {
            return marker.entrySet().stream()
                    .filter(entry -> entry.getValue() == Boolean.FALSE)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
    }
}
