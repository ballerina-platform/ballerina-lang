/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

/**
 * This enum indicates whether the {@link BIRNode} will be used in the execution chain or not.
 *
 * @since 2201.10.0
 */
public enum UsedState {
    /**
     * Indicates a {@link BIRNode} is connected to the invocation chain of one of the root BIRNodes.
     * These BIRNodes are the only ones that is needed for the execution.
     */
    USED,

    /**
     * Indicates a {@link BIRNode} is not used.
     * These BIRNodes can be safely removed to reduce execution time and final jar size.
     */
    UNUSED,

    /**
     * Indicates a {@link BIRNode} was not analyzed.
     * These BIRNodes are treated the same way as {@link UsedState#USED}.
     */
    UNEXPLORED
}
