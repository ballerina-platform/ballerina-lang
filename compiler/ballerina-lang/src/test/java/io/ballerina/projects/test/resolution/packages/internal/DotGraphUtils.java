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

import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableAttributed;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Contains a list of utility methods to work with dot graphs.
 *
 * @since 2.0.0
 */
public class DotGraphUtils {

    private DotGraphUtils() {
    }

    public static MutableGraph createGraph(String content) {
        try {
            return new Parser().read(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MutableGraph createGraph(Path dotFilePath) {
        try (InputStream dot = Files.newInputStream(dotFilePath)) {
            return new Parser().read(dot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DependencyNode getDependencyNode(String name,
                                                   String repo,
                                                   PackageDependencyScope scope,
                                                   String resolutionTypeStr) {
        PackageDescriptor pkgDesc = Utils.getPkgDescFromNode(name, repo);
        DependencyResolutionType resolutionType = DependencyResolutionType.valueOf(
                resolutionTypeStr.toUpperCase(Locale.ENGLISH));
        return new DependencyNode(pkgDesc, scope, resolutionType);
    }

    public static DependencyGraph<PackageDescriptor> createPackageDescGraph(MutableGraph mutableGraph) {
        Map<String, PackageDescriptor> pkgDescMap = new HashMap<>();
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();
        for (MutableNode node : mutableGraph.nodes()) {
            MutableAttributed<MutableNode, ForNode> attrs = node.attrs();

            String repo = null;
            if (attrs.get("repo") != null) {
                repo = Objects.requireNonNull(attrs.get("repo")).toString();
                if (!repo.equals("local")) {
                    throw new IllegalStateException("Unsupported repository: " + repo);
                }
            }

            String name = node.name().toString();
            pkgDescMap.put(name, Utils.getPkgDescFromNode(name, repo));
        }

        for (Link edge : mutableGraph.edges()) {
            PackageDescriptor dependent = pkgDescMap.get(edge.from().name().toString());
            PackageDescriptor dependency = pkgDescMap.get(edge.to().name().toString());
            graphBuilder.addDependency(dependent, dependency);
        }

        return graphBuilder.build();
    }

    public static DependencyGraph<DependencyNode> createDependencyNodeGraph(MutableGraph mutableGraph) {
        Map<String, DependencyNode> dependencyNodeMap = new HashMap<>();
        DependencyGraph.DependencyGraphBuilder<DependencyNode> graphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();

        for (MutableNode node : mutableGraph.nodes()) {
            MutableAttributed<MutableNode, ForNode> attrs = node.attrs();

            String repo = null;
            if (attrs.get("repo") != null) {
                repo = Objects.requireNonNull(attrs.get("repo")).toString();
                if (!repo.equals("local")) {
                    throw new IllegalStateException("Unsupported repository: " + repo);
                }
            }

            PackageDependencyScope scope = Utils.getDependencyScope(attrs.get("scope"));

            String kind;
            if (attrs.get("kind") != null) {
                kind = Objects.requireNonNull(attrs.get("kind")).toString();
            } else {
                kind = "source";
            }

            String name = node.name().toString();
            dependencyNodeMap.put(name, getDependencyNode(name, repo, scope, kind));
        }

        for (Link edge : mutableGraph.edges()) {
            DependencyNode dependent = dependencyNodeMap.get(edge.from().name().toString());
            DependencyNode dependency = dependencyNodeMap.get(edge.to().name().toString());
            graphBuilder.addDependency(dependent, dependency);
        }
        return graphBuilder.build();
    }

    public static Set<String> getModuleNames(MutableGraph packageGraph, PackageDescriptor rootPkgDesc) {
        Optional<MutableNode> rootNodeOptional = packageGraph.nodes().stream()
                .filter(node -> node.name().toString().equals(packageGraph.name().toString()))
                .findAny();

        // Add the default modules;
        Set<String> modules = new HashSet<>();
        modules.add(rootPkgDesc.name().value());
        if (rootNodeOptional.isEmpty()) {
            return modules;
        }

        Object otherModuleAttr = rootNodeOptional.get().attrs().get("other_modules");
        if (otherModuleAttr != null) {
            String[] otherModules = otherModuleAttr.toString().split(",");
            for (String otherModule : otherModules) {
                modules.add(otherModule.trim());
            }
        }

        return modules;
    }
}
