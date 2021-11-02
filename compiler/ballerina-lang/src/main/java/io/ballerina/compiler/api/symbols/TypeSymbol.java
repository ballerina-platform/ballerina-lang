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
 * Represents a Ballerina Type Descriptor.
 *
 * @since 2.0.0
 */
public interface TypeSymbol extends Symbol {

    /**
     * Get the Type Kind.
     *
     * @return {@link TypeDescKind} represented by the model
     */
    TypeDescKind typeKind();

    /**
     * Get the signature of the type descriptor.
     *
     * @return {@link String} signature.
     */
    String signature();

    /**
     * List of lang library functions that can be called using a method call expression.
     *
     * @return {@link List} of lang library functions of the type
     */
    List<FunctionSymbol> langLibMethods();

    /**
     * Checks whether a value of this type can be assigned to a variable of the specified type.
     *
     * @param targetType The type with which compatibility is checked
     * @return Returns true if this type is assignable to the specified type
     * @deprecated This method will be replaced by subtypeOf() method in a later version. This is just a rename in the
     * method for aligning the method names with the language semantics. Switching to subtypeOf() will not break
     * anything.
     */
    @Deprecated
    boolean assignableTo(TypeSymbol targetType);

    /**
     * Checks whether this type is a subtype of the specified type. This is evaluated as per the language semantics
     * defined in the spec.
     *
     * @param targetType The type with which compatibility is checked
     * @return Returns true if this type is a subtype of the specified type
     */
    boolean subtypeOf(TypeSymbol targetType);
}
