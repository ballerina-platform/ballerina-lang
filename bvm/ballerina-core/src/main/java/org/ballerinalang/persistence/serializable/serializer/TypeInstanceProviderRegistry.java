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
package org.ballerinalang.persistence.serializable.serializer;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Keep track of {@link TypeInstanceProvider} implementations.
 */
class TypeInstanceProviderRegistry {
    private static final TypeInstanceProviderRegistry INSTANCE = new TypeInstanceProviderRegistry();
    private final HashMap<String, TypeInstanceProvider> providerMap = new HashMap<>();

    private TypeInstanceProviderRegistry() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Singleton instance exists");
        }
    }

    public static TypeInstanceProviderRegistry getInstance() {
        return INSTANCE;
    }

    TypeInstanceProvider findInstanceProvider(String type) {
        TypeInstanceProvider provider = providerMap.get(type);
        if (provider != null) {
            return provider;
        }

        if (type.endsWith("[]")) {
            ArrayInstanceProvider arrayInstanceProvider = tryCreateArrayInstanceProvider(type);
            if (arrayInstanceProvider != null) {
                addTypeProvider(arrayInstanceProvider);
                return arrayInstanceProvider;
            }
        }

        provider = generateProvider(type);
        if (provider != null) {
            addTypeProvider(provider);
            return provider;
        }
        throw new BallerinaException(String.format(
                "Can not find or create type instance provider for: %s", type));
    }

    private ArrayInstanceProvider tryCreateArrayInstanceProvider(String type) {
        return new ArrayInstanceProvider(type.substring(0, type.length() - 2));
    }

    private TypeInstanceProvider generateProvider(String type) {
        if (isClassLoadable(type)) {
            return new TypeInstanceProviderFactory().createProvider(type);
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

    public void addTypeProvider(TypeInstanceProvider provider) {
        providerMap.put(provider.getTypeName(), provider);
    }

    /**
     * Provide instance of arrays of given type.
     */
    static class ArrayInstanceProvider implements TypeInstanceProvider {
        final String type;
        final TypeInstanceProvider typeProvider;

        ArrayInstanceProvider(String type) {
            this.type = type;
            typeProvider = INSTANCE.findInstanceProvider(type);
        }

        @Override
        public String getTypeName() {
            return type + "[]";
        }

        @Override
        public Object newInstance() {
            Class typeClass = typeProvider.getTypeClass();
            return Array.newInstance(typeClass, 0);
        }

        @Override
        public Class getTypeClass() {
            return newInstance().getClass();
        }
    }
}
