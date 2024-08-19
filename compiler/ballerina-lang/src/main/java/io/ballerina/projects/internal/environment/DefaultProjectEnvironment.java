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
package io.ballerina.projects.internal.environment;

import io.ballerina.projects.Project;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.ProjectEnvironment;

import java.util.Map;

/**
 * Represents the per build project environment context.
 *
 * @since 2.0.0
 */
public class DefaultProjectEnvironment extends ProjectEnvironment {

    private final Map<Class<?>, Object> services;
    private final Environment environment;

    public DefaultProjectEnvironment(Project project, Environment environment, Map<Class<?>, Object> services) {
        this.environment = environment;
        this.services = services;

        WritablePackageCache globalPackageCache = (WritablePackageCache) environment.getService(PackageCache.class);
        ProjectPackageCache projectPackageCache = new ProjectPackageCache(project, globalPackageCache);
        services.put(PackageCache.class, projectPackageCache);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        Object service = services.get(clazz);
        if (service == null) {
            service = environment.getService(clazz);
        }

        return (T) service;
    }

    @Override
    public Environment environment() {
        return environment;
    }
}
