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

/**
 * Represents a resource path of a resource method.
 *
 * @since 2.0.0
 */
public interface ResourcePath {

    /**
     * Returns the type of the resource path represented by this instance.
     *
     * @return The type of resource path of the current instance
     */
    Kind kind();

    /**
     * Returns a string representation of the resource path represented by this instance.
     *
     * @return The string representation of the resource path
     */
    String signature();

    /**
     * Represents the different kinds of resource paths.
     *
     * @since 2.0.0
     */
    enum Kind {
        /**
         * Represents a resource path which consists only of a dot (".").
         */
        DOT_RESOURCE_PATH,
        /**
         * Represents a resource path which consists of a combination of different kinds of path segments: names, path
         * params and path rest params.
         */
        PATH_SEGMENT_LIST,
        /**
         * Represents a resource path with just a path rest param.
         */
        PATH_REST_PARAM
    }
}
