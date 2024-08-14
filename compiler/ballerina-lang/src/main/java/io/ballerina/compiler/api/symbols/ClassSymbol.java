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

import java.util.Map;
import java.util.Optional;

/**
 * Represents a class symbol.
 *
 * @since 2.0.0
 */
public interface ClassSymbol extends ObjectTypeSymbol, Qualifiable, Deprecatable, Annotatable, Documentable {

    /**
     * Get the symbols of the fields of the class. The mapping is a set of field name and field symbol pairs. The
     * returned map is ordered. The order in which the fields were specified in the source code is preserved when
     * iterating the entries of the map.
     *
     * @return An ordered map containing the symbols of the fields
     */
    @Override
    Map<String, ClassFieldSymbol> fieldDescriptors();

    /**
     * Get the init method.
     *
     * @return {@link Optional} init method
     */
    Optional<MethodSymbol> initMethod();
}
