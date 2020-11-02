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

import io.ballerina.projects.environment.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * This class allows API users to build a custom {@code Environment}.
 *
 * @since 2.0.0
 */
public class EnvironmentBuilder {
    private Map<Class<?>, Object> services = new HashMap<>();

    public static Environment buildDefault() {
        return new DefaultEnvironment();
    }

    public static EnvironmentBuilder getInstance() {
        return new EnvironmentBuilder();
    }

    public <T> EnvironmentBuilder addService(Class<T> clazz, T instance) {
        services.put(clazz, instance);
        return this;
    }
}
