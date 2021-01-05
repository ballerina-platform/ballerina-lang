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

/**
 * Represents an object type descriptor.
 *
 * @since 2.0.0
 */
public interface ObjectTypeSymbol extends TypeSymbol, Qualifiable {

    /**
     * Get the object fields.
     *
     * @return {@link List} of object fields
     */
    List<FieldSymbol> fieldDescriptors();

    /**
     * Get the list of methods.
     *
     * @return {@link List} of object methods
     */
    List<MethodSymbol> methods();

    /**
     * Gets a list of included types. An included type is always a subtype of object.
     *
     * @return The list of included object types
     */
    List<TypeSymbol> typeInclusions();
}
