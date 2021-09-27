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
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

/**
 * Contains a list of utility methods to work with dot graphs.
 *
 * @since 2.0.0
 */
public class DotGraphUtils {

    private DotGraphUtils() {
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
                                                   String scopeStr,
                                                   String resolutionTypeStr) {
        PackageDescriptor pkgDesc = getPkgDescFromNode(name, repo);
        PackageDependencyScope scope = PackageDependencyScope.valueOf(scopeStr.toUpperCase(Locale.ENGLISH));
        DependencyResolutionType resolutionType = DependencyResolutionType.valueOf(
                resolutionTypeStr.toUpperCase(Locale.ENGLISH));
        return new DependencyNode(pkgDesc, scope, resolutionType);
    }

    public static PackageDescriptor getPkgDescFromNode(String name) {
        return getPkgDescFromNode(name, null);
    }

    public static PackageDescriptor getPkgDescFromNode(String name, String repo) {
        String[] split = name.split("/");
        String[] split1 = split[1].split(":");
        return PackageDescriptor.from(PackageOrg.from(split[0]), PackageName.from(split1[0]),
                PackageVersion.from(split1[1]), repo);
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

            String scope;
            if (attrs.get("scope") != null) {
                scope = Objects.requireNonNull(attrs.get("scope")).toString();
            } else {
                scope = "default";
            }

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

    public static MutableGraph getDotGraph(DependencyGraph<DependencyNode> actualGraph,
                                           OutputStream outputStream) throws IOException {
        MutableGraph dotGraph = mutGraph("dependency-graph").setDirected(true);
        Collection<DependencyNode> dependencyNodes = actualGraph.getNodes();
        for (DependencyNode dependencyNode : dependencyNodes) {
            Collection<DependencyNode> directDependencies = actualGraph.getDirectDependencies(dependencyNode);
            if (directDependencies.isEmpty()) {
                continue;
            }
            MutableNode rootNode = mutNode(dependencyNode.pkgDesc().toString());
            for (DependencyNode directDependency : directDependencies) {
                rootNode = rootNode.addLink(mutNode(directDependency.pkgDesc().toString()));
            }
            dotGraph = dotGraph.add(rootNode);
        }

        return dotGraph;
    }
}
