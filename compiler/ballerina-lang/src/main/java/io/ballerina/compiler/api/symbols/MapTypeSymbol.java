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

import java.util.Optional;

/**
 * Represents a map type descriptor.
 *
 * @since 2.0.0
 */
public interface MapTypeSymbol extends TypeSymbol {

    /**
     * Get the element type descriptor.
     *
     * @return {@link TypeSymbol} of the members
     * @deprecated This method will be removed in a later release. Use `typeParam()` instead.
     */
    @Deprecated(forRemoval = true, since = "slalpha5")
    Optional<TypeSymbol> typeParameter();

    /**
     * Gets the type of the values in the fields of the mapping.
     *
     * @return The type of the members
     */
    TypeSymbol typeParam();
}
