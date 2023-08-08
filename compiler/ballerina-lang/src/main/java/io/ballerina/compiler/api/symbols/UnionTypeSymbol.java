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
 * Represents an union type descriptor.
 *
 * @since 2.0.0
 */
public interface UnionTypeSymbol extends TypeSymbol {

    /**
     * Gets the exact member types the user specified in the source code when writing the union.
     *
     * @return {@link List} of user specified member types
     */
    List<TypeSymbol> userSpecifiedMemberTypes();

    /**
     * Gets the expanded set of member types. If there are any unions in the members, members of those unions are taken
     * recursively.
     *
     * @return {@link List} of expanded member types
     */
    List<TypeSymbol> memberTypeDescriptors();

    /**
     * Check whether the union type is an enum.
     *
     * @return {@code true} if the union type is an enum, {@code false} otherwise
     */
    boolean isEnum();

    /**
     * Get the enum symbol if the union type is an enum.
     *
     * @return Optional of {@link EnumSymbol} enum symbol
     */
    Optional<EnumSymbol> getEnumSymbol();
}
