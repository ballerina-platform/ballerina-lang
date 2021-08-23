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
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.environment.PackageResolver;
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
    private final DependencyManifest dependencyManifest;
    private final PackageResolver packageResolver;
    private final Collection<DependencyNode> directDeps;
    private final DependencyGraph<DependencyNode> expectedGraphSticky;
    private final DependencyGraph<DependencyNode> expectedGraphNoSticky;

    public PackageResolutionTestCase(PackageDescriptor rootPkgDesc,
                                     DependencyManifest dependencyManifest,
                                     PackageResolver packageResolver,
                                     Collection<DependencyNode> directDeps,
                                     DependencyGraph<DependencyNode> expectedGraphSticky,
                                     DependencyGraph<DependencyNode> expectedGraphNoSticky) {
        this.rootPkgDesc = rootPkgDesc;
        this.dependencyManifest = dependencyManifest;
        this.packageResolver = packageResolver;
        this.directDeps = directDeps;
        this.expectedGraphSticky = expectedGraphSticky;
        this.expectedGraphNoSticky = expectedGraphNoSticky;
    }

    public DependencyGraph<DependencyNode> execute(boolean sticky) {
        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPkgDesc, dependencyManifest,
                packageResolver, true, sticky);
        return resolutionEngine.resolveDependencies(directDeps);
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
