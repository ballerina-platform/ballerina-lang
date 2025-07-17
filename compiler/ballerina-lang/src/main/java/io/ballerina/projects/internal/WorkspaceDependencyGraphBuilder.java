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
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorkspaceDependencyGraphBuilder {
    private final Map<Project, Set<Project>> depGraph = new HashMap<>();
    private final DependencyGraph.DependencyGraphBuilder<Project> rawGraphBuilder;

    public WorkspaceDependencyGraphBuilder() {
        this.rawGraphBuilder = DependencyGraph.DependencyGraphBuilder.getBuilder(null);
    }

    public DependencyGraph<Project> buildGraph() {
        DependencyGraph.DependencyGraphBuilder<Project> graphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder(null);
        for (Map.Entry<Project, Set<Project>> entry : depGraph.entrySet()) {
            graphBuilder.addDependencies(entry.getKey(), entry.getValue());
        }

        return graphBuilder.build();
    }

    public void addPackage(Project project) {
        if (!depGraph.containsKey(project)) {
            depGraph.put(project, new HashSet<>());
        }
    }

    public void addDependency(Project dependent, Project dependency) {
        if (!depGraph.containsKey(dependent)) {
            throw new IllegalStateException("Dependent package does not exist in the graph: " + dependent);
        }
        depGraph.get(dependent).add(dependency);
        // Add to raw graph for troubleshooting
        rawGraphBuilder.addDependency(dependent, dependency);
    }
}
