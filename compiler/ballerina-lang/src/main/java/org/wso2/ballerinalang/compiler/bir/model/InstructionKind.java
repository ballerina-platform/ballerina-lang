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
    ASYNC_CALL((byte) 5),

    // Non-terminating instructions
    MOVE((byte) 20),
    CONST_LOAD((byte) 21),
    NEW_STRUCTURE((byte) 22),
    MAP_STORE((byte) 23),
    MAP_LOAD((byte) 24),
    NEW_ARRAY((byte) 25),
    ARRAY_STORE((byte) 26),
    ARRAY_LOAD((byte) 27),
    NEW_ERROR((byte) 28),
    TYPE_CAST((byte) 29),
    IS_LIKE((byte) 30),
    TYPE_TEST((byte) 31),
    NEW_INSTANCE((byte) 32),
    OBJECT_STORE((byte) 33),
    OBJECT_LOAD((byte) 34),
    PANIC((byte) 35),
    NEW_XML_ELEMENT((byte) 36),
    NEW_XML_TEXT((byte) 37),
    NEW_XML_COMMENT((byte) 38),
    NEW_XML_PI((byte) 39),
    NEW_XML_SEQ((byte) 40),
    NEW_XML_QNAME((byte) 41),
    XML_SEQ_STORE((byte) 42),
    XML_SEQ_LOAD((byte) 43),

    // Binary expression related instructions.
    ADD((byte) 50),
    SUB((byte) 51),
    MUL((byte) 52),
    DIV((byte) 53),
    MOD((byte) 54),
    EQUAL((byte) 55),
    NOT_EQUAL((byte) 56),
    GREATER_THAN((byte) 57),
    GREATER_EQUAL((byte) 58),
    LESS_THAN((byte) 59),
    LESS_EQUAL((byte) 60);

    byte value;

    InstructionKind(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
