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
import guru.nidi.graphviz.model.MutableAttributed;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.internal.environment.DefaultPackageResolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Builds package resolution test cases.
 *
 * @since 2.0.0
 */
public class PackageResolutionTestCaseBuilder {

    private PackageResolutionTestCaseBuilder() {
    }

    public static PackageResolutionTestCase build(TestCaseFilePaths filePaths) {
        // Create PackageResolver
        PackageResolver packageResolver = buildPackageResolver(filePaths);

        // Create Direct dependencies
        Collection<DependencyNode> directDeps = getDirectDependencies(filePaths.appPath());

        // Create dependencyManifest
        DependencyManifest dependencyManifest = DependencyManifest.from("2.0.0",
                Collections.emptyList());

        // Root Package Descriptor
        PackageDescriptor rootPkgDesc = getRootPackageDesc(filePaths.appPath());

        DependencyGraph<DependencyNode> expectedGraphSticky = getPkgDescGraph(
                filePaths.expectedGraphStickyPath().orElse(null));
        DependencyGraph<DependencyNode> expectedGraphNoSticky = getPkgDescGraph(
                filePaths.expectedGraphNoStickyPath().orElse(null));

        return new PackageResolutionTestCase(rootPkgDesc, dependencyManifest, packageResolver,
                directDeps, expectedGraphSticky, expectedGraphNoSticky);
    }

    private static DependencyGraph<DependencyNode> getPkgDescGraph(Path dotFilePath) {
        if (dotFilePath == null) {
            return null;
        }

        MutableGraph mutableGraph = DotGraphUtils.createGraph(dotFilePath);
        return DotGraphUtils.createDependencyNodeGraph(mutableGraph);
    }

    private static PackageResolver buildPackageResolver(TestCaseFilePaths filePaths) {
        PackageRepository centralRepo = PackageRepositoryBuilder.build(filePaths,
                RepositoryKind.CENTRAL);
        PackageRepository distRepo = PackageRepositoryBuilder.build(filePaths,
                RepositoryKind.DIST);
        PackageRepository localRepo = PackageRepositoryBuilder.build(filePaths,
                RepositoryKind.LOCAL);

        // Package cache is not needed for now.
        return new DefaultPackageResolver(distRepo, centralRepo, localRepo, null);

    }

    private static Collection<DependencyNode> getDirectDependencies(Path appDotFilePath) {
        List<DependencyNode> directDeps = new ArrayList<>();

        MutableGraph graph = DotGraphUtils.createGraph(appDotFilePath);
        for (MutableNode node : graph.nodes()) {
            MutableAttributed<MutableNode, ForNode> attrs = node.attrs();

            String repo = null;
            if (attrs.get("repo") != null) {
                repo = Objects.requireNonNull(attrs.get("repo")).toString();
                if (!repo.equals("local")) {
                    throw new IllegalStateException("Unsupported repository name: " + repo);
                }
            }

            PackageDependencyScope scope;
            if (attrs.get("scope") != null) {
                String scopeStr = Objects.requireNonNull(attrs.get("scope")).toString();
                if (!scopeStr.equals("test_only")) {
                    throw new IllegalStateException("Unsupported scope: " + scopeStr);
                }
                scope = PackageDependencyScope.TEST_ONLY;
            } else {
                scope = PackageDependencyScope.DEFAULT;
            }

            DependencyResolutionType resolutionType = DependencyResolutionType.SOURCE;
            PackageDescriptor pkgDesc = DotGraphUtils.getPkgDescFromNode(node.name().value(), repo);
            directDeps.add(new DependencyNode(pkgDesc, scope, resolutionType));
        }
        return directDeps;
    }

    private static PackageDescriptor getRootPackageDesc(Path appDotFilePath) {
        MutableGraph graph = DotGraphUtils.createGraph(appDotFilePath);
        return DotGraphUtils.getPkgDescFromNode(graph.name().toString());
    }
}
