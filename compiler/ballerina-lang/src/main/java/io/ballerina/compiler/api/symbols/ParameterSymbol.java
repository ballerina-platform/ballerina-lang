/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.symbols;

import java.util.Optional;

/**
 * Represents a parameter with a name and type.
 *
 * @since 2.0.0
 */
public interface ParameterSymbol extends Symbol, Annotatable, Qualifiable {

    /**
     * Get the parameter name.
     *
     * @return {@link Optional} name of the field
     */
    @Override
    Optional<String> getName();

    /**
     * Get the type descriptor of the field.
     *
     * @return {@link TypeSymbol} of the field
     */
    TypeSymbol typeDescriptor();

    /**
     * Get a string representation of the signature of the parameter.
     *
     * @return {@link String} signature
     */
    String signature();

    /**
     * Get the kind of the parameter.
     *
     * @return {@link ParameterKind} of the param
     */
    ParameterKind paramKind();
}
