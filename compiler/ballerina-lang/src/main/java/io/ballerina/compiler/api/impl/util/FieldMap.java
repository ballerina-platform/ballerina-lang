/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.compiler.api.impl.util;

import java.util.LinkedHashMap;

import static io.ballerina.compiler.api.impl.util.SymbolUtils.unescapeUnicode;

/**
 * This is a map used for storing the field symbols of record, object and class symbols. The key is a String and this
 * map will store the field names(keys) in unescaped format. e.g., 'int will be stored as int. However, when querying
 * for keys, this follows the semantics of the language. i.e., both 'int and int will return the same field symbol if
 * such a symbol is present.
 *
 * This map is only meant to be used as a container for fields and as such, is intended to be used wrapped in an
 * unmodifiable map. Therefore, only the put() method is overridden since that's the only modifying operation required
 * for building the fields map (at least so far).
 *
 * @param <K> The key. Supposed to be String
 * @param <V> The field symbol type
 */
public class FieldMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 609051204130116L;

    @Override
    public V get(Object key) {
        validateKeyType(key);
        return super.get(unescapeUnicode((String) key));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        validateKeyType(key);
        return super.getOrDefault(unescapeUnicode((String) key), defaultValue);
    }

    @Override
    public V put(K key, V value) {
        validateKeyType(key);
        return super.put((K) unescapeUnicode((String) key), value);
    }

    @Override
    public boolean containsKey(Object key) {
        validateKeyType(key);
        return super.containsKey(unescapeUnicode((String) key));
    }

    private void validateKeyType(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(
                    "Expected a String arg, but found: " + key.getClass().getCanonicalName());
        }
    }
}
