/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package io.ballerina.compiler.api.symbols;

import java.util.List;
import java.util.Optional;

/**
 * Represents a function type descriptor.
 *
 * @since 2.0.0
 */
public interface FunctionTypeSymbol extends TypeSymbol {

    /**
     * Get the required parameters.
     *
     * @return {@link List} of required parameters
     * @deprecated This method will be removed in a later release. Use `params()` instead.
     */
    @Deprecated(forRemoval = true)
    List<ParameterSymbol> parameters();

    /**
     * For regular function types, this will return a list of the non-rest parameters. If this is the `function` type
     * descriptor, this will return empty.
     *
     * @return A {@link List} of required parameters or empty
     */
    Optional<List<ParameterSymbol>> params();

    /**
     * Get the rest parameter.
     *
     * @return {@link Optional} rest parameter
     */
    Optional<ParameterSymbol> restParam();

    /**
     * Get the return type.
     *
     * @return {@link Optional} return type
     */
    Optional<TypeSymbol> returnTypeDescriptor();

    /**
     * Retrieves an instance which captures the info regarding the annotations attached to the return type descriptor.
     * Returns empty if there aren't any annotations attached to the return type descriptor.
     *
     * @return An {@link Annotatable} instance representing the annotations attached to the return type
     */
    Optional<Annotatable> returnTypeAnnotations();
}
