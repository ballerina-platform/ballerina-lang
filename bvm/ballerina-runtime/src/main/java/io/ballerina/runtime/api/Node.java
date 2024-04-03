/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.api;

import java.util.Map;

/**
 * Represents a Ballerina Node.
 *
 * @since 2201.9.0
 */
public abstract class Node {
    public String nodeId;

    public Node(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Get a detail of the Ballerina node.
     * @param detailKey key of the detail.
     * @return the detail object.
     */
    public abstract Object getDetail(String detailKey);

    /**
     * Get all details of the Ballerina node.
     * @return a map of all details.
     */
    public abstract Map<String, Object> getAllDetails();
}
