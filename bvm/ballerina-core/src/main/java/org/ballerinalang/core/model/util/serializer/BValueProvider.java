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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Keep track of {@link SerializationBValueProvider}s.
 *
 * @since 0.982.0
 */
public class BValueProvider {
    private static final BValueProvider INSTANCE = new BValueProvider();
    private final ConcurrentHashMap<String, SerializationBValueProvider> providerMap = new ConcurrentHashMap<>();

    private BValueProvider() {
    }

    static BValueProvider getInstance() {
        return INSTANCE;
    }

    public SerializationBValueProvider find(String type) {
        return providerMap.get(type);
    }

    public void register(SerializationBValueProvider provider) {
        providerMap.put(provider.typeName(), provider);
    }
}
