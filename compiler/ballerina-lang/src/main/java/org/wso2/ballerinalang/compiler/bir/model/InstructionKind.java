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
    NEW_STRUCTURE((byte) 7),
    MAP_STORE((byte) 8),
    MAP_LOAD((byte) 9),
    NEW_ARRAY((byte) 10),
    ARRAY_STORE((byte) 11),
    ARRAY_LOAD((byte) 12),

    // Binary expression related instructions.
    ADD((byte) 20),
    SUB((byte) 21),
    MUL((byte) 22),
    DIV((byte) 23),
    MOD((byte) 24),
    EQUAL((byte) 25),
    NOT_EQUAL((byte) 26),
    GREATER_THAN((byte) 27),
    GREATER_EQUAL((byte) 28),
    LESS_THAN((byte) 29),
    LESS_EQUAL((byte) 30);

    byte value;

    InstructionKind(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
