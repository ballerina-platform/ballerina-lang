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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.codegen.cpentries.ForkJoinCPEntry;

import java.util.Arrays;
import java.util.StringJoiner;

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

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        Arrays.stream(operands).forEach(i -> sj.add(String.valueOf(i)));
        return Mnemonics.getMnem(opcode) + " " + sj.toString();
    }

    /**
     * {@code InstructionCALL} represents the CALL instruction in Ballerina bytecode.
     * <p>
     * The CALL instruction performs a function invocation in BVM.
     *
     * @since 0.95.6
     */
    public static class InstructionCALL extends Instruction {

        public int funcRefCPIndex;
        public FunctionInfo functionInfo;
        public int flags;
        public int[] argRegs;
        public int[] retRegs;

        InstructionCALL(int opcode, int funcRefCPIndex, FunctionInfo functionInfo, int flags,
                        int[] argRegs, int[] retRegs) {
            super(opcode);
            this.funcRefCPIndex = funcRefCPIndex;
            this.functionInfo = functionInfo;
            this.flags = flags;
            this.argRegs = argRegs;
            this.retRegs = retRegs;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(funcRefCPIndex));
            sj.add(String.valueOf(argRegs.length));
            Arrays.stream(argRegs).forEach(i -> sj.add(String.valueOf(i)));
            sj.add(String.valueOf(retRegs.length));
            Arrays.stream(retRegs).forEach(i -> sj.add(String.valueOf(i)));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code InstructionVCALL} represents the VCALL instruction in Ballerina bytecode.
     * <p>
     * The VCALL instruction performs a virtual function invocation in BVM.
     *
     * @since 0.95.6
     */
    public static class InstructionVCALL extends InstructionCALL {
        public int receiverRegIndex;

        InstructionVCALL(int opcode, int receiverRegIndex, int funcRefCPIndex,
                        FunctionInfo functionInfo, int flags, int[] argRegs, int[] retRegs) {
            super(opcode, funcRefCPIndex, functionInfo, flags, argRegs, retRegs);
            this.receiverRegIndex = receiverRegIndex;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(receiverRegIndex));
            sj.add(String.valueOf(funcRefCPIndex));
            sj.add(String.valueOf(argRegs.length));
            Arrays.stream(argRegs).forEach(i -> sj.add(String.valueOf(i)));
            sj.add(String.valueOf(retRegs.length));
            Arrays.stream(retRegs).forEach(i -> sj.add(String.valueOf(i)));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
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

        public int actionRefCPIndex;
        public String actionName;
        public int flags;
        public int[] argRegs;
        public int[] retRegs;

        InstructionACALL(int opcode, int actionRefCPIndex, String actionName, int flags, 
                int[] argRegs, int[] retRegs) {
            super(opcode);
            this.actionRefCPIndex = actionRefCPIndex;
            this.actionName = actionName;
            this.flags = flags;
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

        public int transformerRefCPIndex;
        public TransformerInfo transformerInfo;
        public int flags;
        public int[] argRegs;
        public int[] retRegs;

        InstructionTCALL(int opcode, int transformerRefCPIndex, TransformerInfo transformerInfo, int flags,
                         int[] argRegs, int[] retRegs) {
            super(opcode);
            this.transformerRefCPIndex = transformerRefCPIndex;
            this.transformerInfo = transformerInfo;
            this.flags = flags;
            this.argRegs = argRegs;
            this.retRegs = retRegs;
        }
    }

    /**
     * {@code InstructionWRKSendReceive} represents the worker send/receive operation in Ballerina.
     *
     * @since 0.95.6
     */
    public static class InstructionWRKSendReceive extends Instruction {

        public int channelRefCPIndex;
        public WorkerDataChannelInfo dataChannelInfo;
        public int sigCPIndex;
        public BType type;
        public int reg;

        InstructionWRKSendReceive(int opcode, int channelRefCPIndex, WorkerDataChannelInfo dataChannelInfo,
                                  int sigCPIndex, BType type, int reg) {
            super(opcode);
            this.channelRefCPIndex = channelRefCPIndex;
            this.dataChannelInfo = dataChannelInfo;
            this.sigCPIndex = sigCPIndex;
            this.type = type;
            this.reg = reg;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(channelRefCPIndex));
            sj.add(String.valueOf(sigCPIndex));
            sj.add(String.valueOf(reg));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code InstructionFORKJOIN} represents forkjoin statement in in Ballerina.
     *
     * @since 0.95.6
     */
    public static class InstructionFORKJOIN extends Instruction {

        public int forkJoinCPIndex;
        public ForkJoinCPEntry forkJoinCPEntry;
        public int timeoutRegIndex;
        public int joinVarRegIndex;
        public int joinBlockAddr;
        public int timeoutVarRegIndex;
        public int timeoutBlockAddr;

        public InstructionFORKJOIN(int opcode, int forkJoinCPIndex, ForkJoinCPEntry forkJoinCPEntry,
                                   int timeoutRegIndex, int joinVarRegIndex, int joinBlockAddr,
                                   int timeoutVarRegIndex, int timeoutBlockAddr) {
            super(opcode);
            this.forkJoinCPIndex = forkJoinCPIndex;
            this.forkJoinCPEntry = forkJoinCPEntry;
            this.timeoutRegIndex = timeoutRegIndex;
            this.joinVarRegIndex = joinVarRegIndex;
            this.joinBlockAddr = joinBlockAddr;
            this.timeoutVarRegIndex = timeoutVarRegIndex;
            this.timeoutBlockAddr = timeoutBlockAddr;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(forkJoinCPIndex));
            sj.add(String.valueOf(timeoutRegIndex));
            sj.add(String.valueOf(joinVarRegIndex));
            sj.add(String.valueOf(joinBlockAddr));
            sj.add(String.valueOf(timeoutVarRegIndex));
            sj.add(String.valueOf(timeoutBlockAddr));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code {@link InstructionIteratorNext}} represents the ITR_NEXT instruction in Ballerina bytecode.
     *
     * @since 0.96.0
     */
    public static class InstructionIteratorNext extends Instruction {

        public int iteratorIndex, arity;
        public int[] typeTags, retRegs;

        InstructionIteratorNext(int opcode, int iteratorIndex, int arity, int[] typeTags, int[] retRegs) {
            super(opcode);
            this.iteratorIndex = iteratorIndex;
            this.arity = arity;
            this.typeTags = typeTags;
            this.retRegs = retRegs;
        }
    }

    /**
     * {@code {@link InstructionLock}} represents the LOCK/UNLOCK instruction in Ballerina bytecode.
     *
     * @since 0.961.0
     */
    public static class InstructionLock extends Instruction {

        public BType[] types;
        public int[] varRegs;

        InstructionLock(int opcode, BType[] types, int[] varRegs) {
            super(opcode);
            this.types = types;
            this.varRegs = varRegs;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            for (int i = 0; i < varRegs.length; i++) {
                sj.add(types[i].toString());
                sj.add(String.valueOf(varRegs[i]));
            }
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }
}
