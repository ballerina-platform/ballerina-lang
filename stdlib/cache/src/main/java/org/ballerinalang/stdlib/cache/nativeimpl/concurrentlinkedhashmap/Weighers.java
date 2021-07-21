/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.cache.nativeimpl.concurrentlinkedhashmap;

/**
 * A common set of {@link Weigher} implementations.
 *
 */
final class Weighers {

    private Weighers() {}

    /**
     * A weigher where a value has a weight of <tt>1</tt>. A map bounded with this weigher
     * will evict when the number of key-value pairs exceeds the capacity.
     *
     * @return A weigher where a value takes one unit of capacity.
     */
    @SuppressWarnings("unchecked")
    public static <V> Weigher<V> singleton() {
        return (Weigher<V>) SingletonWeigher.INSTANCE;
    }

    private enum SingletonWeigher implements Weigher<Object> {
        INSTANCE;

        public int weightOf(Object value) {
            return 1;
        }
    }
}
