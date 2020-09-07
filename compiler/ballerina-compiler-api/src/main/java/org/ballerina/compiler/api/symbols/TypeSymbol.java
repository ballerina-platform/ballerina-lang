/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.compiler.api.symbols;

import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * Represents a ballerina type definition.
 *
 * @since 1.3.0
 */
public interface TypeSymbol extends Symbol {

    /**
     * Get the module qualified name.
     * 
     * @return {@link String} name
     */
    String moduleQualifiedName();

    /**
     * List of qualifiers attached to the type definition.
     * 
     * @return {@link List} of qualifiers
     */
    List<Qualifier> qualifiers();

    /**
     * Type descriptor of the definition.
     * 
     * @return {@link BallerinaTypeDescriptor} attached
     */
    Optional<BallerinaTypeDescriptor> typeDescriptor();
}
