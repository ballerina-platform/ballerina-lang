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
package org.wso2.ballerinalang.programfile;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * {@code Instruction} represents an bytecode instruction in Ballerina.
 *
 * @since 0.87
 */
@Deprecated
public class Instruction {

    public int opcode;

    public Operand[] ops;

    Instruction(int opcode) {
        this.opcode = opcode;
        this.ops = new Operand[0];
    }

    Instruction(int opcode, Operand... operands) {
        this.opcode = opcode;
        this.ops = operands;
    }

    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        Arrays.stream(ops).forEach(i -> sj.add(String.valueOf(i.value)));
        return Mnemonics.getMnem(opcode) + " " + sj.toString();
    }

    /**
     * @since 0.95.7
     */
    public static class Operand {
        public int value = -1;

        public Operand(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * @since 0.95.7
     */
    public static class RegIndex extends Operand {
        public int typeTag;
        public boolean isLHSIndex;
        public boolean isVarIndex;

        public RegIndex(int value, int typeTag) {
            super(value);
            this.typeTag = typeTag;
        }

        public RegIndex(int value, int typeTag, boolean isLHSIndex) {
            super(value);
            this.typeTag = typeTag;
            this.isLHSIndex = isLHSIndex;
        }
    }
}
