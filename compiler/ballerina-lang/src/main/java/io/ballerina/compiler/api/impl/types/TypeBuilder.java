/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;

/**
 * Represents the set of type builder interfaces which are used to generate complex types in a single semantic context.
 *
 * @since 2.0.0
 */
public abstract class TypeBuilder {

    public XML XML_TYPE;

    /**
     * Represents the methods required to build the XML type symbol of an XML type descriptor.
     */
    public interface XML {

        /**
         * Sets the type parameter which could be optional when building an XML type. This type parameter shall
         * either be null, or a subtype of the XML type.
         *
         * @param typeParam     The optional type parameter
         * @return The {@link XML} instance with the type parameter being set
        */
        XML withTypeParam(TypeSymbol typeParam);

        /**
         * Builds the XML type descriptor with a semantically valid type parameter.
         *
         * @return The built {@link XMLTypeSymbol}
         */
        XMLTypeSymbol build();
    }
}
