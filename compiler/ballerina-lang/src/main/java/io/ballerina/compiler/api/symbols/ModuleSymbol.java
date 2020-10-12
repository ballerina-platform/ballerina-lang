/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.symbols;

import java.util.List;

/**
 * Represents a ballerina module.
 *
 * @since 2.0.0
 */
public interface ModuleSymbol extends Symbol {

    /**
     * Get the public functions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    List<FunctionSymbol> functions();

    /**
     * Get the public type definitions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    List<TypeSymbol> typeDefinitions();

    /**
     * Get the public constants defined within the module.
     *
     * @return {@link List} of type definitions
     */
    List<ConstantSymbol> constants();

    /**
     * Get the listeners in the Module.
     *
     * @return {@link List} of listeners
     */
    List<TypeSymbol> listeners();

    /**
     * Get the module service definitions.
     * 
     * @return {@link List} of services defined at the module level
     */
    List<ServiceSymbol> services();
    
    /**
     * Get all public the symbols within the module.
     *
     * @return {@link List} of type definitions
     */
    List<Symbol> allSymbols();

    // TODO: add the remaining symbols. i.e: services, xmlns-decl
}
