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
public class TypeInstanceProviderRegistry {
    private static final TypeInstanceProviderRegistry INSTANCE = new TypeInstanceProviderRegistry();
    private final HashMap<String, TypeInstanceProvider> providerMap = new HashMap<>();
    private final HashMap<String, String> typeNameMap = new HashMap<>();

    private TypeInstanceProviderRegistry() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Singleton instance exists");
        }
    }

    public static TypeInstanceProviderRegistry getInstance() {
        return INSTANCE;
    }

    public TypeInstanceProvider findTypeProvider(String type) {
        TypeInstanceProvider provider = providerMap.get(type);
        if (provider == null) {
            provider = generateProvider(type);
            if (provider != null) {
                addTypeProvider(provider);
            }
        }
        if (type.endsWith("[]")) {
            return new ArrayInstanceProvider(type.substring(0, type.length() - 2));
        }
        if (provider == null) {
            throw new BallerinaException(String.format("No TypeInstanceProvider found for: %s", type));
        }
        return provider;
    }

    private TypeInstanceProvider generateProvider(String type) {
        String className = null;
        if (isClassLoadable(type)) {
            className = type;
        }
        if (className == null) {
            className = typeNameMap.get(type);
        }
        if (className == null) {
            return null;
        }
        return new TypeInstanceProviderFactory().createProvider(className);
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

    public void addTypeNameMapping(String typeName, String fullQClassName) {
        this.typeNameMap.put(typeName, fullQClassName);
    }

    public void clearTypeNameMappings() {
        typeNameMap.clear();
    }


    /**
     * Provide instance of arrays of given type.
     */
    public static class ArrayInstanceProvider implements TypeInstanceProvider {
        final String type;
        private TypeInstanceProvider typeProvider;

        public ArrayInstanceProvider(String type) {
            this.type = type;
            typeProvider = INSTANCE.findTypeProvider(type);

        }

        @Override
        public String getTypeName() {
            return type + "[]";
        }

        @Override
        public Object newInstance() {
            Class typeClass = typeProvider.getTypeClass();
            Object array = Array.newInstance(typeClass, 0);
            return array;
        }

        @Override
        public Class getTypeClass() {
            return newInstance().getClass();
        }
    }
}
