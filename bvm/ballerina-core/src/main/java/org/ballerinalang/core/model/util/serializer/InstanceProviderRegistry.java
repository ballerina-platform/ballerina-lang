/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.util.HashMap;

/**
 * Keep track of {@link TypeInstanceProvider} implementations.
 *
 * @since 0.982.0
 */
public class InstanceProviderRegistry {
    private static final InstanceProviderRegistry INSTANCE = new InstanceProviderRegistry();
    private final HashMap<String, TypeInstanceProvider> providerMap = new HashMap<>();

    private InstanceProviderRegistry() {
    }

    static InstanceProviderRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Find {@link TypeInstanceProvider} implementation for given type.
     *
     * @param type To find TypeInstanceProvider for.
     * @return TypeInstanceProvider for specified type.
     */
    TypeInstanceProvider findInstanceProvider(String type) {
        TypeInstanceProvider provider = providerMap.get(type);
        if (provider != null) {
            return provider;
        }

        TypeInstanceProvider instanceProvider = generateProvider(type);
        if (instanceProvider != null) {
            add(instanceProvider);
            return instanceProvider;
        }
        throw new BallerinaException(String.format("Can not find or create type instance provider for: %s", type));
    }

    private TypeInstanceProvider generateProvider(String type) {
        if (isClassLoadable(type)) {
            return new TypeInstanceProviderFactory().from(type);
        }
        return null;
    }

    private boolean isClassLoadable(String type) {
        // try to load the class and see.
        try {
            Class<?> clazz = Class.forName(type, false, this.getClass().getClassLoader());
            return clazz != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void add(TypeInstanceProvider provider) {
        providerMap.put(provider.getTypeName(), provider);
    }
}
