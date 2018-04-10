/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.elements;

/**
 * @since 0.94
 */
public enum Flag {
    /**
     * Indicates Flagged node is a public node.
     */
    PUBLIC,
    /**
     * Indicates Flagged node is a native construct.
     */
    NATIVE,
    /**
     * Indicates Flagged node is a final node.
     */
    FINAL,
    /**
     * Indicates Flagged node is a invokable node attached to a {@link org.ballerinalang.model.types.Type}.
     */
    ATTACHED,
    /**
     * Indicates Flagged node is a Lambda.
     */
    LAMBDA,
    /**
     * Indicates Flagged Node executes in parallel workers.
     */
    PARALLEL,
    /**
     * Indicates Flagged Node is a connector node.
     * TODO need to fix this in a better way.
     * this is needed for composer to identify whether user defined type is a connector or not.
     */
    CONNECTOR,
    /**
     * Indicates Flagged node is deprecated.
     */
    DEPRECATED,
    /**
     * Indicates Flagged node is a read only node.
     */
    READONLY,
}
