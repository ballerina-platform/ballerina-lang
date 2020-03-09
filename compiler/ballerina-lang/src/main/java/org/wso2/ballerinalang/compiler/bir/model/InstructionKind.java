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
    WAIT((byte) 6),
    FP_CALL((byte) 7),
    WK_RECEIVE((byte) 8),
    WK_SEND((byte) 9),
    FLUSH((byte) 10),
    LOCK((byte) 11),
    FIELD_LOCK((byte) 12),
    UNLOCK((byte) 13),
    WAIT_ALL((byte) 14),

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
    FP_LOAD((byte) 36),
    STRING_LOAD((byte) 37),
    NEW_XML_ELEMENT((byte) 38),
    NEW_XML_TEXT((byte) 39),
    NEW_XML_COMMENT((byte) 40),
    NEW_XML_PI((byte) 41),
    NEW_XML_SEQ((byte) 42),
    NEW_XML_QNAME((byte) 43),
    NEW_STRING_XML_QNAME((byte) 44),
    XML_SEQ_STORE((byte) 45),
    XML_SEQ_LOAD((byte) 46),
    XML_LOAD((byte) 47),
    XML_LOAD_ALL((byte) 48),
    XML_ATTRIBUTE_LOAD((byte) 49),
    XML_ATTRIBUTE_STORE((byte) 50),
    NEW_TABLE((byte) 51),
    NEW_TYPEDESC((byte) 52),
    NEW_STREAM((byte) 53),

    // Binary expression related instructions.
    ADD((byte) 61),
    SUB((byte) 62),
    MUL((byte) 63),
    DIV((byte) 64),
    MOD((byte) 65),
    EQUAL((byte) 66),
    NOT_EQUAL((byte) 67),
    GREATER_THAN((byte) 68),
    GREATER_EQUAL((byte) 69),
    LESS_THAN((byte) 70),
    LESS_EQUAL((byte) 71),
    AND((byte) 72),
    OR((byte) 73),
    REF_EQUAL((byte) 74),
    REF_NOT_EQUAL((byte) 75),
    CLOSED_RANGE((byte) 76),
    HALF_OPEN_RANGE((byte) 77),
    ANNOT_ACCESS((byte) 78),

    // Unary expression related instructions.
    TYPEOF((byte) 80),
    NOT((byte) 81),
    NEGATE((byte) 82),
    BITWISE_AND((byte) 83),
    BITWISE_OR((byte) 84),
    BITWISE_XOR((byte) 85),
    BITWISE_LEFT_SHIFT((byte) 86),
    BITWISE_RIGHT_SHIFT((byte) 87),
    BITWISE_UNSIGNED_RIGHT_SHIFT((byte) 88),

    PLATFORM((byte) 128);

    byte value;

    InstructionKind(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
