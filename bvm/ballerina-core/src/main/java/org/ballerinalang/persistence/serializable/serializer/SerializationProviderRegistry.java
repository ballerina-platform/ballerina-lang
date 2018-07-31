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

import java.util.HashMap;

/**
 * Keep track of {@link TypeSerializationProvider} implementations.
 */
public class SerializationProviderRegistry {
    private static final SerializationProviderRegistry INSTANCE = new SerializationProviderRegistry();
    private final HashMap<String, TypeSerializationProvider> providerMap = new HashMap<>();

    private SerializationProviderRegistry() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Singleton instance exists");
        }
    }

    public static SerializationProviderRegistry getInstance() {
        return INSTANCE;
    }

    public TypeSerializationProvider findTypeProvider(String type) {
        TypeSerializationProvider provider = providerMap.get(type);
        if (provider == null) {
            throw new BallerinaException(String.format("No TypeSerializationProvider found for: %s", type));
        }
        return provider;
    }

    public void addTypeProvider(TypeSerializationProvider provider) {
        providerMap.put(provider.getTypeName(), provider);
    }
}
