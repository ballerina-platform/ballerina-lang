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
package io.ballerina.projects.env;

import io.ballerina.projects.Project;
import io.ballerina.projects.environment.EnvironmentContext;
import io.ballerina.projects.environment.GlobalPackageCache;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import io.ballerina.projects.repos.DistributionPackageCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the per build project environment context.
 *
 * @since 2.0.0
 */
public class BuildProjectEnvContext extends ProjectEnvironmentContext {

    private Map<Class<?>, Object> services = new HashMap<>();

    public static BuildProjectEnvContext from(Project project, BuildEnvContext buildEnvContext) {
        return new BuildProjectEnvContext(project, buildEnvContext);
    }

    private BuildProjectEnvContext(Project project, BuildEnvContext buildEnvContext) {
        services.put(EnvironmentContext.class, buildEnvContext);
        GlobalPackageCache globalPackageCache = buildEnvContext.getService(GlobalPackageCache.class);
        DistributionPackageCache distCache = buildEnvContext.getService(DistributionPackageCache.class);
        services.put(PackageResolver.class, new DefaultPackageResolver(project, distCache, globalPackageCache));
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) services.get(clazz);
    }
}
