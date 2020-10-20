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
package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.symbols.MethodSymbol;

import java.util.List;
import java.util.Optional;

/**
 * Represents an object type descriptor.
 *
 * @since 2.0.0
 */
public interface ObjectTypeDescriptor extends BallerinaTypeDescriptor {
    
    /**
     * Get the object type qualifiers.
     *
     * @return {@link List} of object type qualifiers
     */
    List<TypeQualifier> typeQualifiers();

    /**
     * Get the object fields.
     *
     * @return {@link List} of object fields
     */
    List<FieldDescriptor> fieldDescriptors();

    /**
     * Get the list of methods.
     *
     * @return {@link List} of object methods
     */
    List<MethodSymbol> methods();

    /**
     * Get the init method.
     *
     * @return {@link Optional} init method
     */
    Optional<MethodSymbol> initMethod();

    /**
     * Represents the object type qualifier.
     *
     * @since 2.0.0
     */
    enum TypeQualifier {
        ABSTRACT("abstract"),
        LISTENER("listener"),
        CLIENT("client");

        private String value;

        TypeQualifier(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
