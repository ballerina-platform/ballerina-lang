/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.model;

/**
 * The kind of a variable reference in BIR.
 *
 * @since 0.980.0
 */
public enum VarKind {
    /**
     * User-defined local variable.
     */
    LOCAL((byte) 1),

    /**
     * Function argument.
     */
    ARG((byte) 2),

    /**
     * Temporary variable used to store sub-expression results.
     */
    TEMP((byte) 3),

    /**
     * Special variable which holds the return value of a function.
     */
    RETURN((byte) 4),

    /**
     * User-defined global variable.
     */
    GLOBAL((byte) 5),

    /**
     * self referencing variable.
     */
    SELF((byte) 6),

    /**
     * Variable that holds constants.
     */
    CONSTANT((byte) 7),

    /**
     * Variable generated at desugar.
     */
    SYNTHETIC((byte) 8);

    byte value;

    VarKind(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
