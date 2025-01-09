/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;

/**
 * Represent a type definition which will act as a layer of indirection between {@code Env} and the type descriptor.
 *
 * @since 2201.11.0
 */
public abstract class Definition {

    private DefinitionContainer<? extends Definition> container;

    /**
     * Get the {@code SemType} of this definition in the given environment.
     *
     * @param env type environment
     */
    public abstract SemType getSemType(Env env);

    /**
     * Register the container as the holder of this definition. Used to maintain concurrency invariants.
     *
     * @param container holder of the definition
     * @see io.ballerina.runtime.internal.types.semtype.DefinitionContainer
     */
    public void registerContainer(DefinitionContainer<? extends Definition> container) {
        this.container = container;
    }

    protected void notifyContainer() {
        if (container != null) {
            container.definitionUpdated();
        }
    }
}
