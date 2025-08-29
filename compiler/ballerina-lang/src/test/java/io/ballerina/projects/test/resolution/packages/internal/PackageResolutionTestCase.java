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
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.BlendedManifest;
import io.ballerina.projects.internal.ModuleResolver;
import io.ballerina.projects.internal.ResolutionEngine;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

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
    private final Collection<ModuleLoadRequest> moduleLoadRequests;
    private final DependencyGraph<DependencyNode> expectedGraphHard;
    private final DependencyGraph<DependencyNode> expectedGraphMedium;
    private final DependencyGraph<DependencyNode> expectedGraphSoft;

    public PackageResolutionTestCase(PackageDescriptor rootPkgDesc,
                                     BlendedManifest blendedManifest,
                                     PackageResolver packageResolver,
                                     ModuleResolver moduleResolver,
                                     Collection<ModuleLoadRequest> moduleLoadRequests,
                                     DependencyGraph<DependencyNode> expectedGraphHard,
                                     DependencyGraph<DependencyNode> expectedGraphMedium,
                                     DependencyGraph<DependencyNode> expectedGraphSoft) {
        this.rootPkgDesc = rootPkgDesc;
        this.blendedManifest = blendedManifest;
        this.packageResolver = packageResolver;
        this.moduleResolver = moduleResolver;
        this.moduleLoadRequests = moduleLoadRequests;
        this.expectedGraphHard = expectedGraphHard;
        this.expectedGraphMedium = expectedGraphMedium;
        this.expectedGraphSoft = expectedGraphSoft;
    }

    public DependencyGraph<DependencyNode> execute(boolean sticky) {
        PackageLockingMode packageLockingMode = sticky ?
                PackageLockingMode.HARD : PackageLockingMode.SOFT;
        return execute(packageLockingMode);
    }

    public DependencyGraph<DependencyNode> execute(PackageLockingMode lockingMode) {
        ResolutionOptions options = ResolutionOptions.builder().setOffline(true)
                .setPackageLockingMode(lockingMode).build();
        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPkgDesc, blendedManifest,
                packageResolver, moduleResolver, options);
        return resolutionEngine.resolveDependencies(moduleLoadRequests);
    }

    public DependencyGraph<DependencyNode> getExpectedGraph(boolean sticky) {
        PackageLockingMode packageLockingMode = sticky ?
                PackageLockingMode.HARD : PackageLockingMode.SOFT;
        return getExpectedGraph(packageLockingMode);
    }

    public DependencyGraph<DependencyNode> getExpectedGraph(PackageLockingMode lockingMode) {
        if (lockingMode.equals(PackageLockingMode.HARD)) {
            if (expectedGraphHard == null) {
                throw new IllegalStateException(Constants.EXP_GRAPH_HARD_FILE_NAME +
                        " file cannot be found in the test case");
            } else {
                return expectedGraphHard;
            }
        } else if (lockingMode.equals(PackageLockingMode.MEDIUM)) {
            if (expectedGraphMedium == null) {
                throw new IllegalStateException(Constants.EXP_GRAPH_MEDIUM_FILE_NAME +
                        " file cannot be found in the test case");
            } else {
                return expectedGraphMedium;
            }
        } else {
            if (expectedGraphSoft == null) {
                throw new IllegalStateException(Constants.EXP_GRAPH_SOFT_FILE_NAME +
                        " file cannot be found in the test case");
            } else {
                return expectedGraphSoft;
            }
        }
    }
}
