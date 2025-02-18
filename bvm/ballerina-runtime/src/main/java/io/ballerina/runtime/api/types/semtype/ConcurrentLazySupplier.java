/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.api.types.semtype;

import java.util.function.Supplier;

/**
 * A thread-safe single lazy supplier that initialize the value only once.
 *
 * @param <E> type of the value
 * @since 2201.12.0
 */
public class ConcurrentLazySupplier<E> implements Supplier<E> {

    private Supplier<E> initializer;
    private volatile E value = null;

    public ConcurrentLazySupplier(Supplier<E> initializer) {
        this.initializer = initializer;
    }

    @Override
    public E get() {
        E result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    result = initializer.get();
                    assert result != null;
                    value = result;
                    initializer = null;
                }
            }
        }
        return result;
    }
}
