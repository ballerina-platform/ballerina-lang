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
import java.util.Map;
import java.util.Optional;

/**
 * Represents a record type descriptor.
 *
 * @since 2.0.0
 */
public interface RecordTypeSymbol extends TypeSymbol {

    /**
     * Get the symbols of the fields of the record type. The mapping is a set of field name and field symbol pairs. The
     * returned map is ordered. The order in which the fields were specified in the source code is preserved when
     * iterating the entries of the map.
     *
     * @return An ordered map containing the symbols of the fields
     */
    Map<String, RecordFieldSymbol> fieldDescriptors();

    /**
     * Get the rest type descriptor.
     *
     * @return {@link Optional} rest type descriptor
     */
    Optional<TypeSymbol> restTypeDescriptor();

    /**
     * Gets a list of included types. An included type is always a named record type.
     *
     * @return The list of included record types
     */
    List<TypeSymbol> typeInclusions();
}
