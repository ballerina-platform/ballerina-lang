/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.symbols;

/**
 * Represents a field of an object type descriptor.
 *
 * @since 2.0.0
 */
public interface ObjectFieldSymbol extends Symbol, Annotatable, Deprecatable, Documentable, Qualifiable {

    /**
     * Get the type descriptor of the field.
     *
     * @return {@link TypeSymbol} of the field
     */
    TypeSymbol typeDescriptor();

    /**
     * Get the signature of the field.
     *
     * @return {@link String} signature
     */
    String signature();
}
