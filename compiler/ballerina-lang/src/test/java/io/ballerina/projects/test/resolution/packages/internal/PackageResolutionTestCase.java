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
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.UpdatePolicy;
import io.ballerina.projects.internal.BlendedManifest;
import io.ballerina.projects.internal.ModuleResolver;
import io.ballerina.projects.internal.ResolutionEngine;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.internal.index.Index;

import java.util.Collection;

/**
 * Represents a single package resolution test case.
 *
 * @since 2.0.0
 */
public class PackageResolutionTestCase {

    private final PackageDescriptor rootPkgDesc;
    private final BlendedManifest blendedManifest;
    private final PackageResolver packageResolver;
    private final ModuleResolver moduleResolver;
    private final Index index;
    private final boolean hasDependencyManifest;
    private final boolean distributionChange;
    private final boolean lessThan24HrsAfterBuild;
    private final Collection<ModuleLoadRequest> moduleLoadRequests;
    private final DependencyGraph<DependencyNode> expectedGraphSoft;
    private final DependencyGraph<DependencyNode> expectedGraphMedium;
    private final DependencyGraph<DependencyNode> expectedGraphHard;
    private final DependencyGraph<DependencyNode> expectedGraphLocked;

    public PackageResolutionTestCase(PackageDescriptor rootPkgDesc,
                                     BlendedManifest blendedManifest,
                                     PackageResolver packageResolver,
                                     ModuleResolver moduleResolver,
                                     Index index,
                                     boolean hasDependencyManifest,
                                     boolean distributionChange,
                                     boolean lessThan24HrsAfterBuild,
                                     Collection<ModuleLoadRequest> moduleLoadRequests,
                                     DependencyGraph<DependencyNode> expectedGraphSoft,
                                     DependencyGraph<DependencyNode> expectedGraphMedium,
                                     DependencyGraph<DependencyNode> expectedGraphHard,
                                     DependencyGraph<DependencyNode> expectedGraphLocked) {
        this.rootPkgDesc = rootPkgDesc;
        this.blendedManifest = blendedManifest;
        this.packageResolver = packageResolver;
        this.moduleResolver = moduleResolver;
        this.index = index;
        this.hasDependencyManifest = hasDependencyManifest;
        this.distributionChange = distributionChange;
        this.lessThan24HrsAfterBuild = lessThan24HrsAfterBuild;
        this.moduleLoadRequests = moduleLoadRequests;
        this.expectedGraphSoft = expectedGraphSoft;
        this.expectedGraphMedium = expectedGraphMedium;
        this.expectedGraphHard = expectedGraphHard;
        this.expectedGraphLocked = expectedGraphLocked;
    }

    public DependencyGraph<DependencyNode> execute(UpdatePolicy policy) {
        ResolutionOptions options = ResolutionOptions.builder().setOffline(true).setUpdatePolicy(policy).build();
        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPkgDesc, blendedManifest,
                packageResolver, moduleResolver, options, index, hasDependencyManifest, distributionChange,
                lessThan24HrsAfterBuild, true);
        return resolutionEngine.resolveDependencies(moduleLoadRequests);
    }

    public DependencyGraph<DependencyNode> getExpectedGraph(UpdatePolicy policy) {
        DependencyGraph<DependencyNode> graph = switch (policy) {
            case SOFT -> expectedGraphSoft;
            case MEDIUM -> expectedGraphMedium;
            case HARD -> expectedGraphHard;
            case LOCKED -> expectedGraphLocked;
        };
        if (graph == null) {
            throw new IllegalStateException(
                    "Expected graph for " + policy + " policy cannot be found in the test case");
        }
        return graph;
    }
}
