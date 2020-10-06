/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.symbols;

import io.ballerina.compiler.api.types.FieldDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * Represent Class Symbol.
 *
 * @since 2.0.0
 */
public interface ClassSymbol extends Symbol {

    /**
     * Get the Class's qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    List<Qualifier> qualifiers();

    /**
     * Get the class's functions.
     *
     * @return {@link List} of functions
     */
    List<FunctionSymbol> functions();

    /**
     * Get the class fields.
     * 
     * @return {@link List} of defined fields
     */
    List<FieldDescriptor> fields();

    /**
     * Get the type reference.
     * 
     * @return {@link Optional} type reference of the class
     */
    Optional<TypeSymbol> typeReference();
}
