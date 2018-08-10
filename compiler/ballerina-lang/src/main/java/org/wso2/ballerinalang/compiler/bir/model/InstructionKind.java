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
 * The kind of instruction.
 *
 * @since 0.980.0
 */
public enum InstructionKind {
    // Terminating instructions
    GOTO((byte) 1),
    CALL((byte) 2),
    BRANCH((byte) 3),
    RETURN((byte) 4),

    // Non-terminating instructions
    MOVE((byte) 5),
    CONST_LOAD((byte) 6),

    // Binary expression related instructions.
    ADD((byte) 7),
    SUB((byte) 8),
    MUL((byte) 9),
    DIV((byte) 10),
    MOD((byte) 11),
    EQUAL((byte) 12),
    NOT_EQUAL((byte) 13),
    GREATER_THAN((byte) 14),
    GREATER_EQUAL((byte) 15),
    LESS_THAN((byte) 16),
    LESS_EQUAL((byte) 17);

    byte value;

    InstructionKind(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
