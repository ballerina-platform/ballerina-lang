/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.uri.parser;

import java.util.List;
import java.util.Map;

/**
 * Represent what kind of path segment is exists in the node.
 */
public interface PathSegment {

    /**
     * Expend the path segment.
     *
     * @param variables variables to expand.
     * @return a string of expansion.
     */
    String expand(Map<String, String> variables);

    /**
     * match the length of the URI fragment.
     *
     * @param childNodesList Child node list of the current node.
     * @param uriFragment URI fragment which should be matched.
     * @param variables Variable map which should be filled in.
     * @return the length of the match with the URI fragment given.
     */
    int match(List<? extends Node> childNodesList, String uriFragment, Map<String, String> variables);

    /**
     * Get the token of the path segment.
     *
     * @return Token of the path segment.
     */
    String getToken();

    /**
     * Get the first character in the path segment.
     *
     * @return the first character in the path segment.
     */
    char getFirstCharacter();

}
