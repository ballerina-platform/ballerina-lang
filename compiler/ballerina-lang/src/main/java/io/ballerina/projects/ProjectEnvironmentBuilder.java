/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.environment.DefaultEnvironment;
import io.ballerina.projects.internal.environment.DefaultProjectEnvironment;
import io.ballerina.projects.internal.environment.ProjectDependencyCache;
import io.ballerina.projects.internal.environment.ProjectDependencyCacheContext;
import io.ballerina.projects.repos.BuildProjectCompilationCache;
import io.ballerina.projects.repos.TempDirCompilationCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for creating a {@code ProjectEnvironment} during the project loading phase.
 *
 * @since 2.0.0
 */
public class ProjectEnvironmentBuilder {
    private Environment environment;
    private final Map<Class<?>, Object> services = new HashMap<>();
    private CompilationCacheFactory compilationCacheFactory;
    private boolean projectDependencyCacheWritable;
    private ProjectDependencyCache projectDependencyCache;
    private boolean defaultEnvironment;

    private ProjectEnvironmentBuilder(Environment environment) {
        this.environment = environment;
    }

    public static ProjectEnvironmentBuilder getBuilder(Environment environment) {
        return new ProjectEnvironmentBuilder(environment);
    }

    public static ProjectEnvironmentBuilder getDefaultBuilder() {
        ProjectEnvironmentBuilder builder = new ProjectEnvironmentBuilder(null);
        builder.defaultEnvironment = true;
        return builder;
    }

    public ProjectEnvironmentBuilder addCompilationCacheFactory(CompilationCacheFactory compilationCacheFactory) {
        this.compilationCacheFactory = compilationCacheFactory;
        return this;
    }

    /** Enables writes to the project target dependency cache. */
    public ProjectEnvironmentBuilder enableProjectDependencyCacheWrites() {
        this.projectDependencyCacheWritable = true;
        return this;
    }

    public boolean isProjectDependencyCacheWritable() {
        return projectDependencyCacheWritable;
    }

    /**
     * Uses the given project dependency cache for the project being built, regardless of the project kind.
     * Used when a dependency is compiled separately on behalf of a project, e.g. in a dependency
     * compilation subprocess, so that its artifacts go to the project target instead of the home cache.
     *
     * @param projectDependencyCache project dependency cache
     * @return this builder
     */
    public ProjectEnvironmentBuilder setProjectDependencyCache(ProjectDependencyCache projectDependencyCache) {
        this.projectDependencyCache = projectDependencyCache;
        return this;
    }

    public ProjectEnvironment build(Project project) {
        boolean isBuildProject = project.kind() == ProjectKind.BUILD_PROJECT
                || project.kind() == ProjectKind.WORKSPACE_PROJECT;
        ProjectDependencyCache dependencyCache = this.projectDependencyCache;
        if (dependencyCache == null && isBuildProject) {
            dependencyCache = new ProjectDependencyCache(project.targetDir(), projectDependencyCacheWritable);
        }
        if (environment == null && defaultEnvironment) {
            if (dependencyCache != null) {
                ProjectDependencyCacheContext.set(dependencyCache);
            }
            try {
                environment = EnvironmentBuilder.buildDefault();
            } finally {
                ProjectDependencyCacheContext.remove();
            }
        }
        if (dependencyCache != null && environment instanceof DefaultEnvironment defaultEnvironment
                && defaultEnvironment.getService(ProjectDependencyCache.class) == null) {
            defaultEnvironment.addService(ProjectDependencyCache.class, dependencyCache);
        }
        CompilationCache compilationCache;
        if (compilationCacheFactory != null) {
            compilationCache = compilationCacheFactory.createCompilationCache(project);
        } else {
            compilationCache = switch (project.kind()) {
                case BUILD_PROJECT, WORKSPACE_PROJECT -> BuildProjectCompilationCache.from(project);
                case SINGLE_FILE_PROJECT -> TempDirCompilationCache.from(project);
                case BALA_PROJECT ->
                        throw new IllegalStateException("BALAProject should always be created with a CompilationCache");
            };
        }
        services.put(CompilationCache.class, compilationCache);
        return new DefaultProjectEnvironment(project, environment, services);
    }
}
