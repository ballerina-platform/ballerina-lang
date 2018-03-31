/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Language server context for the Workspace service.
 * @since v0.964.0
 */
public class WorkspaceServiceContext implements LanguageServerContext {
    private Map<LanguageServerContext.Key<?>, Object> props = new HashMap<>();

    @Override
    public <V> void put(LanguageServerContext.Key<V> key, V value) {
        props.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(LanguageServerContext.Key<V> key) {
        return (V) props.get(key);
    }
}
