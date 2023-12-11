/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.symbols.resourcepath.util;

import io.ballerina.compiler.api.symbols.Symbol;

/**
 * Represents a segment of a resource segment path list.
 *
 * @since 2.0.0
 */
public interface PathSegment extends Symbol {

    /**
     * Returns the type of the path segment.
     *
     * @return The type of the path segment represented by the instance
     */
    Kind pathSegmentKind();

    /**
     * Returns a string representation of the path segment.
     *
     * @return A string representation of the path segment
     */
    String signature();

    /**
     * Represents the types of path segments available.
     *
     * @since 2.0.0
     */
    enum Kind {
        /**
         * Represents a segment in the resource path which is just a identifier.
         */
        NAMED_SEGMENT,
        /**
         * Represents a path parameter segment.
         */
        PATH_PARAMETER,
        /**
         * Represents a path rest parameter segment.
         */
        PATH_REST_PARAMETER
    }
}
