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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Container used to maintain concurrent safety when creating {@code Definitions}.
 *
 * @param <E> type of the definition
 * @since 2201.12.0
 */
public class DefinitionContainer<E extends Definition> {

    private final AtomicReference<E> definition = new AtomicReference<>();

    public boolean isDefinitionReady() {
        return definition.get() != null;
    }

    /**
     * Get the semtype of the definition. Must call {@code isDefinitionReady} before calling this method.
     *
     * @param env {@code Env} in which type is defined at
     * @return recursive semtype representing the type
     */
    public SemType getSemType(Env env) {
        return definition.get().getSemType(env);
    }

    /**
     * Try to set the definition. If the definition is already set, this will not update the definition.
     *
     * @param supplier supplier to get the definition. Calling this should not have side effects
     * @return result of the update
     */
    public DefinitionUpdateResult<E> trySetDefinition(Supplier<E> supplier) {
        if (isDefinitionReady()) {
            return new DefinitionUpdateResult<>(definition.get(), false);
        }
        boolean updated = definition.compareAndSet(null, supplier.get());
        return new DefinitionUpdateResult<>(definition.get(), updated);
    }

    public void clear() {
        this.definition.set(null);
    }

    /**
     * Result of trying to update the definition.
     *
     * @param <E>        Type of the definition
     * @param updated    If update was successful. If this failed you must get the semtype using the {@code getSemType}
     *                   method of the container
     * @param definition If update was successful this will be the new definition. Otherwise, this will be null
     * @since 2201.11.0
     */
    public record DefinitionUpdateResult<E>(E definition, boolean updated) {

    }
}
