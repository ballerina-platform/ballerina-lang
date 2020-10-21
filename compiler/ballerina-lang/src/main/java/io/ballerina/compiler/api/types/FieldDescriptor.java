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

import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Qualifier;

import java.util.Optional;

/**
 * Represents a field with a name and type.
 *
 * @since 2.0.0
 */
public interface FieldDescriptor {

    /**
     * Get the field name.
     *
     * @return {@link String} name of the field
     */
    String name();

    /**
     * Whether optional field or not.
     *
     * @return {@link Boolean} optional status
     */
    boolean isOptional();

    /**
     * Whether the field has a default.
     *
     * @return {@link Boolean} optional status
     */
    boolean hasDefaultValue();

    /**
     * Get the type descriptor of the field.
     *
     * @return {@link BallerinaTypeDescriptor} of the field
     */
    BallerinaTypeDescriptor typeDescriptor();

    /**
     * Get the documentation attachment.
     *
     * @return {@link Optional} doc attachment of the field
     */
    Optional<Documentation> documentation();

    /**
     * Get the accessibility modifier if available.
     *
     * @return {@link Optional} accessibility modifier
     */
    Optional<Qualifier> qualifier();

    /**
     * Get the signature of the field.
     *
     * @return {@link String} signature
     */
    String signature();
}
