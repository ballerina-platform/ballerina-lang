/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.types.semtype;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class ConcurrentLazyContainer<E> implements Supplier<E> {

    private Supplier<E> initializer;
    private final AtomicReference<E> value = new AtomicReference<>();

    ConcurrentLazyContainer(Supplier<E> initializer) {
        this.initializer = initializer;
    }

    @Override
    public E get() {
        E result = value.get();
        if (result == null) {
            result = initializer.get();
            if (!value.compareAndSet(null, result)) {
                result = value.get();
            }
            initializer = null;
        }
        return result;
    }
}
