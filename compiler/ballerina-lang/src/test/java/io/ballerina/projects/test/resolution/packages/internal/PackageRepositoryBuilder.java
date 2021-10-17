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
import io.ballerina.projects.internal.PackageVersionContainer;
import io.ballerina.projects.internal.repositories.AbstractPackageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Reads repository dot files and build test package repository instances.
 *
 * @since 2.0.0
 */
public class PackageRepositoryBuilder {

    private final DefaultPackageRepository centralRepo;
    private final DefaultPackageRepository distRepo;
    private final LocalPackageRepository localRepo;

    public PackageRepositoryBuilder(TestCaseFilePaths filePaths) {
        this.centralRepo = (DefaultPackageRepository) buildInternal(filePaths, RepositoryKind.CENTRAL);
        this.distRepo = (DefaultPackageRepository) buildInternal(filePaths, RepositoryKind.DIST);
        this.localRepo = (LocalPackageRepository) buildLocalRepo(filePaths.localRepoDirPath().orElse(null));
    }

    public AbstractPackageRepository buildCentralRepo() {
        return centralRepo;
    }

    public AbstractPackageRepository buildDistRepo() {
        return distRepo;
    }

    public AbstractPackageRepository buildLocalRepo() {
        return localRepo;
    }

    private PackageRepository buildInternal(TestCaseFilePaths filePaths, RepositoryKind repoKind) {
        Optional<Path> repoDotFilePath = getRepPath(filePaths, repoKind);
        if (repoDotFilePath.isEmpty()) {
            return DefaultPackageRepository.EMPTY_REPO;
        }

        return buildInternal(repoDotFilePath.get(), repoKind);
    }

    private Optional<Path> getRepPath(TestCaseFilePaths filePaths, RepositoryKind repoKind) {
        switch (repoKind) {
            case DIST:
                return filePaths.distRepoPath();
            case CENTRAL:
                return filePaths.centralRepoPath();
            case LOCAL:
                return filePaths.localRepoDirPath();
            default:
                throw new IllegalStateException("Unsupported package repository kind " + repoKind);
        }
    }

    private PackageRepository buildLocalRepo(Path localRepoDirPath) {
        if (localRepoDirPath == null) {
            return DefaultPackageRepository.EMPTY_REPO;
        }

        try {
            return buildLocalRepo(Files.list(localRepoDirPath).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PackageRepository buildLocalRepo(List<Path> localPackagePaths) {
        PackageVersionContainer<PackageDescWrapper> pkgContainer = new PackageVersionContainer<>();
        Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap = new HashMap<>();

        for (Path localPackagePath : localPackagePaths) {
            MutableGraph repoDotGraph = DotGraphUtils.createGraph(localPackagePath);
            PackageDescriptor rootPkgDesc = Utils.getPkgDescFromNode(repoDotGraph.name().toString());
            Set<String> modules = DotGraphUtils.getModuleNames(repoDotGraph, rootPkgDesc);
            DependencyGraph<PackageDescriptor> packageDescGraph =
                    DotGraphUtils.createPackageDescGraph(repoDotGraph);
            pkgContainer.add(rootPkgDesc.org(), rootPkgDesc.name(), rootPkgDesc.version(),
                    new PackageDescWrapper(rootPkgDesc, modules));
            graphMap.put(rootPkgDesc, packageDescGraph);

        }
        return buildInternal(pkgContainer, graphMap, RepositoryKind.LOCAL);
    }

    private PackageRepository buildInternal(Path repoDotFilePath, RepositoryKind repoKind) {
        PackageVersionContainer<PackageDescWrapper> pkgContainer = new PackageVersionContainer<>();
        GraphNodeMarker nodeMarker = new GraphNodeMarker();
        DependencyGraphBuilder<PackageDescriptor> graphBuilder = DependencyGraphBuilder.getBuilder();

        MutableGraph repoDotGraph = DotGraphUtils.createGraph(repoDotFilePath);
        for (MutableGraph packageGraph : repoDotGraph.graphs()) {
            PackageDescriptor rootPkgDesc = Utils.getPkgDescFromNode(packageGraph.name().toString());
            Set<String> modules = DotGraphUtils.getModuleNames(packageGraph, rootPkgDesc);
            pkgContainer.add(rootPkgDesc.org(), rootPkgDesc.name(), rootPkgDesc.version(),
                    new PackageDescWrapper(rootPkgDesc, modules));
            nodeMarker.subGraphFound(rootPkgDesc);

            graphBuilder.add(rootPkgDesc);
            for (Link edge : packageGraph.edges()) {
                PackageDescriptor dependent = Utils.getPkgDescFromNode(edge.from().name().toString());
                nodeMarker.nodeFound(dependent);
                PackageDescriptor dependency = Utils.getPkgDescFromNode(edge.to().name().toString());
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
        } else if (repoKind == RepositoryKind.LOCAL && !unmarkedNodes.isEmpty()) {
            for (PackageDescriptor unmarkedNode : unmarkedNodes) {
                DependencyGraph<PackageDescriptor> dependencyGraph = distRepo.getDependencyGraph(unmarkedNode);
                if (dependencyGraph == null) {
                    dependencyGraph = centralRepo.getDependencyGraph(unmarkedNode);
                }

                if (dependencyGraph == null) {
                    throw new IllegalStateException("Package mentioned in local repo does not have " +
                            "subgraph entries in neither central nor dist repos. package: " +
                            unmarkedNode + " local repo: " + repoDotFilePath);
                } else {
                    graphBuilder.mergeGraph(dependencyGraph);
                }
            }
        }

        Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap = new HashMap<>();
        DependencyGraph<PackageDescriptor> repoGraph = graphBuilder.build();
        for (PackageDescWrapper pkgDescWrapper : pkgContainer.getAll()) {
            DependencyGraphBuilder<PackageDescriptor> builder = DependencyGraphBuilder.getBuilder();
            buildPkgDescGraph(pkgDescWrapper.pkgDesc(), builder, repoGraph);
            graphMap.put(pkgDescWrapper.pkgDesc(), builder.build());
        }

        return buildInternal(pkgContainer, graphMap, repoKind);
    }

    private void buildPkgDescGraph(PackageDescriptor pkgDesc,
                                   DependencyGraphBuilder<PackageDescriptor> graphBuilder,
                                   DependencyGraph<PackageDescriptor> repoGraph) {
        // Can we optimize this by checking whether pkgDesc already exists in the graphBuilder...
        for (PackageDescriptor dependency : repoGraph.getDirectDependencies(pkgDesc)) {
            buildPkgDescGraph(dependency, graphBuilder, repoGraph);
            graphBuilder.addDependency(pkgDesc, dependency);
        }
    }

    private PackageRepository buildInternal(PackageVersionContainer<PackageDescWrapper> pkgContainer,
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
