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

package io.ballerina.compiler.api.symbols.resourcepath;

import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;

import java.util.List;
import java.util.Optional;

/**
 * Represents a resource path made up from a combination of path segment names, path params and path rest params.
 *
 * @since 2.0.0
 */
public interface PathSegmentList extends ResourcePath {

    /**
     * Returns a list of the path parameters specified in this resource path.
     *
     * @return A list of path params
     */
    List<PathParameterSymbol> pathParameters();

    /**
     * Returns the path rest param if the user has specified one. If not, returns empty.
     *
     * @return The path rest param if one is specified
     */
    Optional<PathParameterSymbol> pathRestParameter();

    /**
     * Returns a list of the path segments which make up this resource path. The order in this list is the same order of
     * the segments in the source file. If there is a path rest param, it is the last one in the list.
     *
     * @return The path segments as a list
     */
    List<PathSegment> list();
}
