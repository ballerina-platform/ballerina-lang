/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.WorkspaceDependencyGraphBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorkspaceResolution {
    private final WorkspaceProject workspaceProject;
    private final DependencyGraph<BuildProject> projectDependencyGraph;
    private final List<Diagnostic> diagnosticList;
    private DefaultDiagnosticResult diagnosticResult;
    private final boolean offline;

    private WorkspaceResolution(WorkspaceProject workspaceProject, ResolutionOptions resolutionOptions) {
        this.workspaceProject = workspaceProject;
        this.offline = resolutionOptions.offline();
        this.projectDependencyGraph = buildDependencyGraph();
        this.diagnosticList = new ArrayList<>();
        this.diagnosticList.addAll(workspaceProject.manifest().diagnostics().allDiagnostics);
    }

    public static WorkspaceResolution from(WorkspaceProject workspaceProject) {
        return new WorkspaceResolution(workspaceProject, ResolutionOptions.builder()
                .setOffline(workspaceProject.buildOptions().offlineBuild())
                .setSticky(workspaceProject.buildOptions().sticky())
                .build());
    }

    public static WorkspaceResolution from(WorkspaceProject workspaceProject, ResolutionOptions resolutionOptions) {
        return new WorkspaceResolution(workspaceProject, resolutionOptions);
    }

    private DependencyGraph<BuildProject> buildDependencyGraph() {
        WorkspaceDependencyGraphBuilder graphBuilder = new WorkspaceDependencyGraphBuilder();
        for (BuildProject project : this.workspaceProject.projects()) {
            graphBuilder.addPackage(project);
            Collection<ResolvedPackageDependency> directDependencies = project.currentPackage()
                    .getResolution(workspaceProject.buildOptions().compilationOptions())
                    .dependencyGraph()
                    .getDirectDependencies(new ResolvedPackageDependency(project.currentPackage(),
                            PackageDependencyScope.DEFAULT));
            addDependencies(project, directDependencies, graphBuilder);
        }
        return graphBuilder.buildGraph();
    }

    private static void addDependencies(BuildProject project, Collection<ResolvedPackageDependency> directDependencies,
                                        WorkspaceDependencyGraphBuilder graphBuilder) {
        graphBuilder.addPackage(project);
        for (ResolvedPackageDependency directDependency : directDependencies) {
            if (directDependency.packageInstance().project().kind() == ProjectKind.BUILD_PROJECT) {
                BuildProject buildProject = (BuildProject) directDependency.packageInstance().project();
                graphBuilder.addDependency(project, buildProject);
                addDependencies(buildProject, directDependency.packageInstance()
                        .getResolution().dependencyGraph().getDirectDependencies(directDependency), graphBuilder);
            }
        }
    }

    public DependencyGraph<BuildProject> dependencyGraph() {
        return projectDependencyGraph;
    }

    public DiagnosticResult diagnosticResult() {
        if (this.diagnosticResult == null) {
            this.diagnosticResult = new DefaultDiagnosticResult(this.diagnosticList);
        }
        return diagnosticResult;
    }
}
