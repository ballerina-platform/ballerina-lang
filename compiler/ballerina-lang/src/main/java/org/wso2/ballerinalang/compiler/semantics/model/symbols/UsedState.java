/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

/**
 * This enum indicates whether the function will be used in the execution chain or not
 */
public enum UsedState {
    // Indicates a function is connected to the invocation chain of init or main functions
    // These functions are the only ones that is needed for the execution
    USED,

    // Indicates a function is not used
    // These functions can be safely discarded to reduce execution time and final jar size
    UNUSED,

    // Indicates a function which was not analyzed
    // Since it is ambiguous whether the function is used or not, these functions are treated the same way as USED
    UNEXPOLORED
}
