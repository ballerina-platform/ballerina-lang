/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler;

/**
 * Ballerina Language server context.
 *
 * @since 0.970.0
 */
public abstract class LSContext {
    private LSOperation operation;

    protected LSContext(LSOperation operation) {
        this.operation = operation;
    }

    /**
     * Add new Context property.
     * @param key   Property Key
     * @param value Property value
     * @param <V>   Key Type
     */
    public abstract <V> void put(Key<V> key, V value);

    /**
     * Get property by Key.
     * @param key   Property Key
     * @param <V>   Key Type
     * @return {@link Object}   Property
     */
    public abstract <V> V get(Key<V> key);

    /**
     * Returns operation name.
     *
     * @return  operation name
     */
    LSOperation getOperation() {
        return this.operation;
    }

    /**
     * @param <K> Property Key
     * @since 0.95.5
     */
    public static class Key<K> {
    }
}
