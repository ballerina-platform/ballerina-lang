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

package io.ballerina.compiler.api.types.util;

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;

import java.util.Set;

/**
 * Defines the methods for accessing the information on lang library methods.
 *
 * @since 2.0.0
 */
public interface LangLibMethod {

    /**
     * Get the set of qualifiers added to the method.
     *
     * @return The set of qualifiers for the method
     */
    Set<Qualifier> qualifiers();

    /**
     * Get the name of the method.
     *
     * @return Name of the method
     */
    String name();

    /**
     * Get the signature information of the method.
     *
     * @return The type of the method
     */
    FunctionTypeDescriptor typeDescriptor();
}
