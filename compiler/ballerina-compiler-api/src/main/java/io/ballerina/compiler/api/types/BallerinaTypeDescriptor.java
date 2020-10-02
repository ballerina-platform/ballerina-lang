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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.MethodSymbol;

import java.util.List;

/**
 * Represents a Ballerina Type Descriptor.
 *
 * @since 2.0.0
 */
public interface BallerinaTypeDescriptor {

    /**
     * Get the Type Kind.
     *
     * @return {@link TypeDescKind} represented by the model
     */
    TypeDescKind kind();

    /**
     * Get the module ID.
     *
     * @return {@link ModuleID} of the Type
     */
    ModuleID moduleID();

    /**
     * Get the signature of the type descriptor.
     *
     * @return {@link String} signature.
     */
    String signature();

    /**
     * List of members that are visible to a value of this type.
     * 
     * @return {@link List} of visible member symbols
     */
    List<MethodSymbol> builtinMethods();
}
