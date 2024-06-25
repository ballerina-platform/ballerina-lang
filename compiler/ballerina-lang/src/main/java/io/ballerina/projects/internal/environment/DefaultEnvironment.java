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

import io.ballerina.projects.environment.Environment;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the {@code Environment} of the build project.
 *
 * @since 2.0.0
 */
public class DefaultEnvironment extends Environment {
    private final Map<Class<?>, Object> services;
    public Path buildProjectTargetDir;

    public DefaultEnvironment(Map<Class<?>, Object> services) {
        this.services = services;
    }

    public DefaultEnvironment() {
        this(new HashMap<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) services.get(clazz);
    }

    public <T> void addService(Class<T> clazz, T service) {
        services.put(clazz, service);
    }
}
