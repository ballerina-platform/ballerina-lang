/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.commons.LanguageServerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Language server context holding the common utility instances of an associated language server instance.
 *
 * @since 2.0.0
 */
public class LanguageServerContextImpl implements LanguageServerContext {
    private final Map<LanguageServerContext.Key<?>, Object> props = new HashMap<>();
    private final Map<Class<?>, Object> objects = new HashMap<>();

    public LanguageServerContextImpl() {
    }

    @Override
    public <V> void put(LanguageServerContext.Key<V> key, V value) {
        props.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(LanguageServerContext.Key<V> key) {
        return (V) props.get(key);
    }

    @Override
    public <V> void put(Class<V> clazz, V value) {
        objects.put(clazz, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(Class<V> clazz) {
        return (V) objects.get(clazz);
    }
}
