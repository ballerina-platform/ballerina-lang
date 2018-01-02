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
package org.ballerinalang.util.codegen;

import java.util.Arrays;

/**
 * {@code Instruction} represents a bytecode instruction in Ballerina.
 *
 * @since 0.87
 */
public class Instruction {

    int opcode;
    int[] operands;

    Instruction(int opcode, int... operands) {
        this.opcode = opcode;
        this.operands = operands;
    }

    public int getOpcode() {
        return opcode;
    }

    public int[] getOperands() {
        return operands;
    }

    public void setOperand(int index, int value) {
        operands[index] = value;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "opcode=" + opcode +
                ", operands=" + Arrays.toString(operands) +
                '}';
    }

    /**
     * {@code InstructionCALL} represents the CALL instruction in Ballerina bytecode.
     * <p>
     * The CALL instruction performs a function invocation in BVM.
     *
     * @since 0.95.6
     */
    public static class InstructionCALL extends Instruction {

        public FunctionInfo functionInfo;
        public int[] argRegs;
        public int[] retRegs;

        InstructionCALL(int opcode, FunctionInfo functionInfo,
                        int[] argRegs, int[] retRegs) {
            super(opcode);
            this.functionInfo = functionInfo;
            this.argRegs = argRegs;
            this.retRegs = retRegs;
        }
    }

    /**
     * {@code InstructionACALL} represents the ACALL instruction in Ballerina bytecode.
     * <p>
     * The ACALL instruction performs an action invocation in BVM.
     *
     * @since 0.95.6
     */
    public static class InstructionACALL extends Instruction {

        public String actionName;
        public int[] argRegs;
        public int[] retRegs;

        InstructionACALL(int opcode, String actionName, int[] argRegs, int[] retRegs) {
            super(opcode);
            this.actionName = actionName;
            this.argRegs = argRegs;
            this.retRegs = retRegs;
        }
    }

    /**
     * {@code InstructionTCALL} represents the TCALL instruction in Ballerina bytecode.
     * <p>
     * The TCALL instruction performs an transformer invocation in BVM.
     *
     * @since 0.95.6
     */
    public static class InstructionTCALL extends Instruction {

        public TransformerInfo transformerInfo;
        public int[] argRegs;
        public int[] retRegs;

        InstructionTCALL(int opcode, TransformerInfo transformerInfo,
                        int[] argRegs, int[] retRegs) {
            super(opcode);
            this.transformerInfo = transformerInfo;
            this.argRegs = argRegs;
            this.retRegs = retRegs;
        }
    }
}
