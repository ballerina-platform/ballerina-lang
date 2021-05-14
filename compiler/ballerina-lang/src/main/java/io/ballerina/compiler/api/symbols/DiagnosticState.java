/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.symbols;

/**
 * This is used to represent the state of a variable symbol. In cases of compiler errors, sometimes a variable symbol's
 * type may be set to compile error. In such cases, this will help to further narrow down the cause for it.
 *
 * @since 2.0.0
 */
public enum DiagnosticState {
    /**
     * Indicates that the variable symbol is semantically valid.
     */
    VALID,
    /**
     * Indicates that the symbol represents a redeclard symbol.
     */
    REDECLARED,
    /**
     * Indicates that the specified type of variable could not be resolved. i.e., specified type is not defined.
     */
    UNKNOWN_TYPE,
    /**
     * Indicates that the compiler failed to determine the type of the variable using the context. This is applicable
     * for variables declared using `var`.
     */
    FAILED_TO_DETERMINE_TYPE
}
