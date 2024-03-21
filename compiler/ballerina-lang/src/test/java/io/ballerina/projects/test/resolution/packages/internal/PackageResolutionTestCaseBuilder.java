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
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.BlendedManifest;
import io.ballerina.projects.internal.ModuleResolver;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.internal.environment.DefaultPackageResolver;
import io.ballerina.projects.internal.repositories.AbstractPackageRepository;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds package resolution test cases.
 *
 * @since 2.0.0
 */
public class PackageResolutionTestCaseBuilder {

    private PackageResolutionTestCaseBuilder() {
    }

    public static PackageResolutionTestCase build(TestCaseFilePaths filePaths, boolean sticky) {
        // Create PackageResolver
        DotGraphBasedPackageResolver packageResolver = buildPackageResolver(filePaths);

        // Create module load requests
        Collection<ModuleLoadRequest> moduleLoadRequests = getModuleLoadRequests(filePaths.appPath());

        // Root Package Descriptor
        PackageDescWrapper rootPkgDescWrapper = getRootPkgDescWrapper(filePaths.appPath());
        PackageDescriptor rootPkgDes = rootPkgDescWrapper.pkgDesc();

        // Create dependencyManifest
        DependencyManifest dependencyManifest = getDependencyManifest(
                filePaths.dependenciesTomlPath().orElse(null));

        // Create packageManifest
        PackageManifest packageManifest = getPackageManifest(
                filePaths.ballerinaTomlPath().orElse(null), rootPkgDes);

        // Create expected dependency graph with sticky
        DependencyGraph<DependencyNode> expectedGraphSticky = getPkgDescGraph(
                filePaths.expectedGraphStickyPath().orElse(null));

        // Create expected dependency graph with no sticky
        DependencyGraph<DependencyNode> expectedGraphNoSticky = getPkgDescGraph(
                filePaths.expectedGraphNoStickyPath().orElse(null));

        BlendedManifest blendedManifest = BlendedManifest.from(dependencyManifest,
                packageManifest, packageResolver.localRepo(), new HashMap<>(), false);
        ModuleResolver moduleResolver = new ModuleResolver(rootPkgDes,
                getModulesInRootPackage(rootPkgDescWrapper, rootPkgDes),
                blendedManifest, packageResolver, ResolutionOptions.builder().setSticky(sticky).build());
        return new PackageResolutionTestCase(rootPkgDes, blendedManifest,
                packageResolver, moduleResolver, moduleLoadRequests,
                expectedGraphSticky, expectedGraphNoSticky);
    }

    private static List<ModuleName> getModulesInRootPackage(PackageDescWrapper rootPkgDescWrapper,
                                                            PackageDescriptor rootPkgDes) {
        return rootPkgDescWrapper.modules().stream()
                .map(modNameStr -> Utils.getModuleName(rootPkgDes.name(), modNameStr))
                .collect(Collectors.toList());
    }

    private static DependencyGraph<DependencyNode> getPkgDescGraph(Path dotFilePath) {
        if (dotFilePath == null) {
            return null;
        }

        MutableGraph mutableGraph = DotGraphUtils.createGraph(dotFilePath);
        return DotGraphUtils.createDependencyNodeGraph(mutableGraph);
    }

    private static DotGraphBasedPackageResolver buildPackageResolver(TestCaseFilePaths filePaths) {
        PackageRepositoryBuilder repoBuilder = new PackageRepositoryBuilder(filePaths);
        AbstractPackageRepository centralRepo = repoBuilder.buildCentralRepo();
        AbstractPackageRepository distRepo = repoBuilder.buildDistRepo();
        AbstractPackageRepository localRepo = repoBuilder.buildLocalRepo();

        // Package cache is not needed for now.
        return new DotGraphBasedPackageResolver(distRepo, centralRepo, localRepo, null);
    }

    private static Collection<ModuleLoadRequest> getModuleLoadRequests(Path appDotFilePath) {
        List<ModuleLoadRequest> moduleLoadRequests = new ArrayList<>();

        MutableGraph graph = DotGraphUtils.createGraph(appDotFilePath);
        String rootPkgName = graph.name().toString();
        for (MutableNode node : graph.nodes()) {
            if (rootPkgName.equals(node.name().toString())) {
                continue;
            }
            MutableAttributed<MutableNode, ForNode> attrs = node.attrs();
            PackageDependencyScope scope = Utils.getDependencyScope(attrs.get("scope"));
            DependencyResolutionType resolutionType = DependencyResolutionType.SOURCE;
            moduleLoadRequests.add(Utils.getModuleLoadRequest(node.name().value(), scope, resolutionType));
        }
        return moduleLoadRequests;
    }

    private static DependencyManifest getDependencyManifest(Path dependenciesTomlPath) {
        if (dependenciesTomlPath == null) {
            return DependencyManifest.from("2.0.0", null, Collections.emptyList(), Collections.emptyList());
        }

        List<DependencyManifest.Package> recordedDeps = new ArrayList<>();
        MutableGraph graph = DotGraphUtils.createGraph(dependenciesTomlPath);
        for (MutableNode node : graph.nodes()) {
            MutableAttributed<MutableNode, ForNode> attrs = node.attrs();
            PackageDependencyScope scope = Utils.getDependencyScope(attrs.get("scope"));

            boolean isTransitive = false;
            if (attrs.get("transitive") != null) {
                isTransitive = Boolean.parseBoolean((Objects.requireNonNull(attrs.get("transitive"))).toString());
            }

            PackageDescriptor pkgDesc = Utils.getPkgDescFromNode(node.name().value(), null);
            recordedDeps.add(new DependencyManifest.Package(pkgDesc.name(), pkgDesc.org(), pkgDesc.version(),
                    scope.getValue(), isTransitive, Collections.emptyList(), Collections.emptyList()));
        }
        return DependencyManifest.from("2.0.0", null, recordedDeps, Collections.emptyList());
    }

    private static PackageManifest getPackageManifest(Path balTomlPath, PackageDescriptor rootPkgDesc) {
        if (balTomlPath == null) {
            return PackageManifest.from(rootPkgDesc, null, null, Collections.emptyMap(),
                    Collections.emptyList());
        }

        List<PackageManifest.Dependency> dependencies = new ArrayList<>();
        MutableGraph graph = DotGraphUtils.createGraph(balTomlPath);
        for (MutableNode node : graph.nodes()) {
            MutableAttributed<MutableNode, ForNode> attrs = node.attrs();
            PackageDescriptor pkgDesc = Utils.getPkgDescFromNode(node.name().value(), null);

            String repo = null;
            if (attrs.get("repo") != null) {
                repo = Objects.requireNonNull(attrs.get("repo")).toString();
                if (!repo.equals("local")) {
                    throw new IllegalStateException("Unsupported repository name: " + repo);
                }
            }

            dependencies.add(new PackageManifest.Dependency(pkgDesc.name(),
                    pkgDesc.org(), pkgDesc.version(), repo, new NullLocation()));
        }

        return PackageManifest.from(rootPkgDesc, null, null, Collections.emptyMap(), dependencies);
    }

    private static PackageDescWrapper getRootPkgDescWrapper(Path appDotFilePath) {
        MutableGraph graph = DotGraphUtils.createGraph(appDotFilePath);
        PackageDescriptor pkgDesc = Utils.getPkgDescFromNode(graph.name().toString());
        Set<String> moduleNames = DotGraphUtils.getModuleNames(graph, pkgDesc);
        return new PackageDescWrapper(pkgDesc, moduleNames);
    }

    static class DotGraphBasedPackageResolver extends DefaultPackageResolver {
        private final AbstractPackageRepository localRepo;

        public DotGraphBasedPackageResolver(AbstractPackageRepository distributionRepo,
                                            AbstractPackageRepository centralRepo,
                                            AbstractPackageRepository localRepo,
                                            PackageCache packageCache) {
            super(distributionRepo, centralRepo, localRepo, packageCache);
            this.localRepo = localRepo;
        }

        public AbstractPackageRepository localRepo() {
            return localRepo;
        }
    }

    private static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            return null;
        }

        @Override
        public TextRange textRange() {
            return null;
        }
    }
}
