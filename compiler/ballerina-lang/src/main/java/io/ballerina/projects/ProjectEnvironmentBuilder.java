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
import io.ballerina.projects.internal.environment.DefaultProjectEnvironment;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for creating a {@code ProjectEnvironment} during the project loading phase.
 *
 * @since 2.0.0
 */
public class ProjectEnvironmentBuilder {
    private final Environment environment;
    private final Map<Class<?>, Object> services = new HashMap<>();
    private CompilationCacheFactory compilationCacheFactory;

    private ProjectEnvironmentBuilder(Environment environment) {
        this.environment = environment;
    }

    public static ProjectEnvironmentBuilder getBuilder(Environment environment) {
        return new ProjectEnvironmentBuilder(environment);
    }

    public static ProjectEnvironmentBuilder getDefaultBuilder() {
        return new ProjectEnvironmentBuilder(EnvironmentBuilder.buildDefault());
    }

    public ProjectEnvironmentBuilder addCompilationCacheFactory(CompilationCacheFactory compilationCacheFactory) {
        this.compilationCacheFactory = compilationCacheFactory;
        return this;
    }

    public ProjectEnvironment build(Project project) {
        if (compilationCacheFactory != null) {
            services.put(CompilationCache.class, compilationCacheFactory.createCompilationCache(project));
        } else {
            // TODO Set the Project specific cache impl
            // TargetCache for the BuildProject
            // TempCache for the SingleFileProject
            services.put(CompilationCache.class, new DoNothingCompilationCache(project));
        }
        return new DefaultProjectEnvironment(project, environment, services);
    }

    /**
     * No-op compilation cache implementation.
     *
     * @since 2.0.0
     */
    private static class DoNothingCompilationCache extends CompilationCache {

        public DoNothingCompilationCache(Project project) {
            super(project);
        }

        @Override
        public byte[] getBir(ModuleName moduleName) {
            return new byte[0];
        }

        @Override
        public void cacheBir(ModuleName moduleName, byte[] bir) {

        }

        @Override
        public Path getPlatformSpecificLibrary(ModuleName moduleName) {
            return null;
        }
    }
}
