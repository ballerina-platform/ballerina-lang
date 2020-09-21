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
     * Indicates Flagged node is a public node.
     */
    PRIVATE,
    /**
     * Indicates Flagged node is a remote function.
     */
    REMOTE,
    /**
     * Indicates Flagged node is a transactional function.
     */
    TRANSACTIONAL,
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
     * Indicates Flagged node is a worker lambda.
     */
    WORKER,
    /**
     * Indicates Flagged Node executes in parallel workers.
     */
    PARALLEL,
    /**
     * Indicates Flagged Node is a listener node.
     */
    LISTENER,
    /**
     * Indicates Flagged node is a read only node.
     */
    READONLY,
    /**
     * Indicates Flagged node is a final node.
     */
    FUNCTION_FINAL,
    /**
     * Indicates Flagged node is a interface node.
     */
    INTERFACE,
    /**
     * Indicates Flagged record field is a required field.
     */
    REQUIRED,
    /**
     * Temporary indicator for records.
     */
    RECORD,
    /**
     * Indicator for ANONYMOUS types.
     */
    ANONYMOUS,
    /**
     * Indicates that the flagged node is an optional field.
     */
    OPTIONAL,
    /**
     * Indicates flagged node is a testable node.
     */
    TESTABLE,
    /**
     * Indicates Flagged node is a client node.
     */
    CLIENT,
    /**
     * Indicates Flagged node is a resource node.
     */
    RESOURCE,
    /**
     * Indicates Flagged node is an isolated node.
     */
    ISOLATED,
    /**
     * Indicates Flagged node is a service node.
     */
    SERVICE,
    /**
     * Indicates flagged node is a constant node.
     */
    CONSTANT,
    /**
     * Indicates flagged node is a paramType.
     */
    TYPE_PARAM,
    /**
     * Indicates flagged node is a lang library function.
     */
    LANG_LIB,
    /**
     * Indicates flagged node is a worker inside fork stmt.
     */
    FORKED,
    /**
     * Indicates flagged node is a distinct type.
     */
    DISTINCT,
    /**
     * Indicates flagged node is a class.
     */
    CLASS
}
