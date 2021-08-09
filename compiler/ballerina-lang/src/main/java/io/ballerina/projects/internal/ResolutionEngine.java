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
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;

/**
 * Responsible for creating the dependency graph with automatic version updates.
 *
 * @since 2.0.0
 */
public class ResolutionEngine {
    private final NewPackageDependencyGraphBuilder graphBuilder;
//    private final Project rootProject;
    private final PackageDescriptor rootPkgDesc;
//    private final boolean offline;
//    private final boolean sticky;

    public ResolutionEngine(Project rootProject,
                            PackageDescriptor rootPkgDesc,
                            boolean offline, // TODO Can we combine these two options into buildOptions
                            boolean sticky) {
//        this.rootProject = rootProject;
        this.rootPkgDesc = rootPkgDesc;
//        this.offline = offline;
//        this.sticky = sticky;
        this.graphBuilder = new NewPackageDependencyGraphBuilder(rootPkgDesc);
    }

    public void addDirectDependency(PackageDescriptor dependency,
                                    PackageDependencyScope scope,
                                    DependencyResolutionType resolutionType) {
        graphBuilder.addDependency(rootPkgDesc, dependency, scope, resolutionType);
    }

    public DependencyGraph<PackageDescriptor> resolveDependencies() {
        return null;
    }
}
