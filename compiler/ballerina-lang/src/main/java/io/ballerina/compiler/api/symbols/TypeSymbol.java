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

import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;

/**
 * Represents a ballerina type definition.
 *
 * @since 2.0.0
 */
public interface TypeSymbol extends Symbol, Qualifiable, Deprecatable {

    /**
     * Get the module qualified name.
     *
     * @return {@link String} name
     */
    String moduleQualifiedName();

    /**
     * Type descriptor of the definition.
     *
     * @return {@link BallerinaTypeDescriptor} attached
     */
    BallerinaTypeDescriptor typeDescriptor();

    /**
     * Checks whether the type is a readonly type.
     *
     * @return True if the type is readonly
     */
    boolean readonly();
}
