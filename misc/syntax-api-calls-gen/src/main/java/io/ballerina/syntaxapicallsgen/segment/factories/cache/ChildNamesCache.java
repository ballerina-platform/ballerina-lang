/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.segment.factories.cache;

import io.ballerina.syntaxapicallsgen.config.SyntaxApiCallsGenConfig;

import java.util.List;
import java.util.Map;


/**
 * Helper class to load the method param name map.
 * Required because NonTerminalNode does not expose all of its child names.
 * Load each method call with its required parameter names.
 *
 * @since 2.0.0
 */
public class ChildNamesCache {
    private final Map<String, List<String>> cache;

    private ChildNamesCache(Map<String, List<String>> cache) {
        this.cache = cache;
    }

    /**
     * Create the cache from the file defined in the config.
     *
     * @param config Configuration object with Json location.
     * @return Created cache.
     */
    public static ChildNamesCache fromConfig(SyntaxApiCallsGenConfig config) {
        return new ChildNamesCache(config.readChildNamesJson());
    }

    /**
     * Get the parameter entry names of the given node type.
     *
     * @param nodeType Node type to find the child names of.
     * @return Child name(s) of the node type.
     */
    public List<String> getChildNames(String nodeType) {
        return cache.get(nodeType);
    }
}
