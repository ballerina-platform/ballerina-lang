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
    private final DependencyGraph<DependencyNode> expectedGraphSticky;
    private final DependencyGraph<DependencyNode> expectedGraphNoSticky;

    public PackageResolutionTestCase(PackageDescriptor rootPkgDesc,
                                     BlendedManifest blendedManifest,
                                     PackageResolver packageResolver,
                                     ModuleResolver moduleResolver,
                                     Collection<ModuleLoadRequest> moduleLoadRequests,
                                     DependencyGraph<DependencyNode> expectedGraphSticky,
                                     DependencyGraph<DependencyNode> expectedGraphNoSticky) {
        this.rootPkgDesc = rootPkgDesc;
        this.blendedManifest = blendedManifest;
        this.packageResolver = packageResolver;
        this.moduleResolver = moduleResolver;
        this.moduleLoadRequests = moduleLoadRequests;
        this.expectedGraphSticky = expectedGraphSticky;
        this.expectedGraphNoSticky = expectedGraphNoSticky;
    }

    public DependencyGraph<DependencyNode> execute(boolean sticky) {
        ResolutionOptions options = ResolutionOptions.builder().setOffline(true).setSticky(sticky).build();
        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPkgDesc, blendedManifest,
                packageResolver, moduleResolver, options);
        return resolutionEngine.resolveDependencies(moduleLoadRequests);
    }

    public DependencyGraph<DependencyNode> getExpectedGraph(boolean sticky) {
        if (sticky) {
            if (expectedGraphSticky == null) {
                throw new IllegalStateException(Constants.EXP_GRAPH_STICKY_FILE_NAME +
                        " file cannot be found in the test case");
            } else {
                return expectedGraphSticky;
            }
        } else {
            if (expectedGraphNoSticky == null) {
                throw new IllegalStateException(Constants.EXP_GRAPH_NO_STICKY_FILE_NAME +
                        " file cannot be found in the test case");
            } else {
                return expectedGraphNoSticky;
            }
        }
    }
}
