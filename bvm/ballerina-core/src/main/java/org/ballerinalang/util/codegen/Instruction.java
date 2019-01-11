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
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;

import java.util.ArrayList;
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
            sj.add(String.valueOf(flags));
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
        public boolean channelInSameStrand;

        InstructionWRKSendReceive(int opcode, int channelRefCPIndex, WorkerDataChannelInfo dataChannelInfo,
                                  int sigCPIndex, BType type, int reg, boolean channelInSameStrand) {
            super(opcode);
            this.channelRefCPIndex = channelRefCPIndex;
            this.dataChannelInfo = dataChannelInfo;
            this.sigCPIndex = sigCPIndex;
            this.type = type;
            this.reg = reg;
            this.channelInSameStrand = channelInSameStrand;
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
     * {@code InstructionCHNReceive} represents the channel receive operation in Ballerina.
     *
     * @since 0.982.0
     */
    public static class InstructionCHNReceive extends Instruction {

        public String channelName;
        public BType receiverType;
        public int receiverReg;
        public BType keyType;
        public int keyReg;

        public InstructionCHNReceive(int opcode, String channelName, BType receiverType,
                int receiverReg, BType keyType, int keyReg) {
            super(opcode);
            this.channelName = channelName;
            this.receiverType = receiverType;
            this.receiverReg = receiverReg;
            this.keyType = keyType;
            this.keyReg = keyReg;
        }
    }

    /**
     * {@code InstructionCHNSend} represents the channel send operation in Ballerina.
     *
     * @since 0.982.0
     */
    public static class InstructionCHNSend extends Instruction {

        public String channelName;
        public BType dataType;
        public int dataReg;
        public BType keyType;
        public int keyReg;

        public InstructionCHNSend(int opcode, String channelName, BType dataType,
                int dataReg, BType keyType, int keyReg) {
            super(opcode);
            this.channelName = channelName;
            this.dataType = dataType;
            this.dataReg = dataReg;
            this.keyType = keyType;
            this.keyReg = keyReg;
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
        public BType constraintType;

        InstructionIteratorNext(int opcode, int iteratorIndex, int arity, int[] typeTags, int[] retRegs,
                                BType constraintType) {
            super(opcode);
            this.iteratorIndex = iteratorIndex;
            this.arity = arity;
            this.typeTags = typeTags;
            this.retRegs = retRegs;
            this.constraintType = constraintType;
        }
    }

    /**
     * {@code {@link InstructionLock}} represents the LOCK instruction in Ballerina bytecode.
     *
     * @since 0.961.0
     */
    public static class InstructionLock extends Instruction {

        public BType[] types;
        public int[] pkgRefs;
        public int[] varRegs;
        public int[] fieldRegs;
        public int varCount;
        public String uuid;

        InstructionLock(int opcode, BType[] types, int[] pkgRefs, int[] varRegs, int[] fieldNames, int varCount,
                        String uuid) {
            super(opcode);
            this.types = types;
            this.pkgRefs = pkgRefs;
            this.varRegs = varRegs;
            this.fieldRegs = fieldNames;
            this.varCount = varCount;
            this.uuid = uuid;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            for (int i = 0; i < varRegs.length; i++) {
                sj.add(types[i].toString());
                sj.add(String.valueOf(pkgRefs[i]));
                sj.add(String.valueOf(varRegs[i]));
            }
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code {@link InstructionUnLock}} represents the UNLOCK instruction in Ballerina bytecode.
     *
     * @since 0.985.0
     */
    public static class InstructionUnLock extends Instruction {

        public BType[] types;
        public int[] pkgRefs;
        public int[] varRegs;
        public int varCount;
        public String uuid;
        public boolean hasFieldVar;

        InstructionUnLock(int opcode, BType[] types, int[] pkgRefs, int[] varRegs, int varCount, String uuid,
                          boolean hasFieldsVar) {
            super(opcode);
            this.types = types;
            this.pkgRefs = pkgRefs;
            this.varRegs = varRegs;
            this.varCount = varCount;
            this.uuid = uuid;
            this.hasFieldVar = hasFieldsVar;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            for (int i = 0; i < varRegs.length; i++) {
                sj.add(types[i].toString());
                sj.add(String.valueOf(pkgRefs[i]));
                sj.add(String.valueOf(varRegs[i]));
            }
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code {@link InstructionCompensate}} represents the COMPENSATE instruction in Ballerina bytecode.
     */
    public static class InstructionCompensate extends Instruction {
        public String scopeName;
        public ArrayList<String> childScopes = new ArrayList<>();
        public int retRegIndex;

        InstructionCompensate(int opcode, String scopeName, ArrayList<String> childScopes, int retRegIndex) {
            super(opcode);
            this.scopeName = scopeName;
            this.childScopes = childScopes;
            this.retRegIndex = retRegIndex;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(scopeName));
            for (String child : childScopes) {
                sj.add(child);
            }
            return Mnemonics.getMnem(opcode) + " " + sj.toString() + " " + retRegIndex;
        }
    }

    /**
     * {@code {@link InstructionWRKSyncSend}} represents synchronous worker send in Ballerina bytecode.
     */
    public static class InstructionWRKSyncSend extends Instruction {
        public int channelRefCPIndex;
        public WorkerDataChannelInfo dataChannelInfo;
        public int sigCPIndex;
        public BType type;
        public int reg;
        public int retReg;
        public boolean isSameStrand;

        InstructionWRKSyncSend(int opcode, int channelRefCPIndex, WorkerDataChannelInfo dataChannelInfo,
                               int sigCPIndex, BType type, int reg, int retReg, boolean isSameStrand) {
            super(opcode);
            this.channelRefCPIndex = channelRefCPIndex;
            this.dataChannelInfo = dataChannelInfo;
            this.sigCPIndex = sigCPIndex;
            this.type = type;
            this.reg = reg;
            this.retReg = retReg;
            this.isSameStrand = isSameStrand;
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
     * {@code {@link InstructionFlush}} represents worker flush in Ballerina bytecode.
     */
    public static class InstructionFlush extends Instruction {

        public int retReg;
        public String[] channels;

        InstructionFlush(int opcode, int retReg, String[] channels) {

            super(opcode);
            this.retReg = retReg;
            this.channels = channels;
        }

        @Override
        public String toString() {

            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(retReg));
            for (int i = 0; i < channels.length; i++) {
                sj.add(channels[i]);
            }
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code {@link InstructionScopeEnd}} represents end of a SCOPE block in Ballerina bytecode.
     */
    public static class InstructionScopeEnd extends Instruction {

        public FunctionInfo function;
        public ArrayList<String> childScopes;
        public String scopeName;
        public FunctionRefCPEntry functionCP;

        InstructionScopeEnd(int opcode, FunctionInfo function, ArrayList<String> childScopes,
                String scopeName, FunctionRefCPEntry funcCP) {
            super(opcode);
            this.function = function;
            this.childScopes = childScopes;
            this.scopeName = scopeName;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(scopeName));
            for (String child : childScopes) {
                sj.add(child);
            }
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code InstructionTrBegin} represents the TR_BEGIN instruction in Ballerina bytecode.
     * <p>
     * The TR_BEGIN instruction performs transaction initialisation and transaction participant initialisation.
     *
     * @since 0.990.0
     */
    public static class InstructionTrBegin extends Instruction {
        public final int transactionType;
        public final int blockId;
        public final int retryCountReg;
        public final int committedFuncIndex;
        public final int abortedFuncIndex;

        InstructionTrBegin(int opcode, int transactionType, int blockId, int retryCountReg,
                           int committedFuncIndex, int abortedFuncIndex) {
            super(opcode);

            this.transactionType = transactionType;
            this.blockId = blockId;
            this.retryCountReg = retryCountReg;
            this.committedFuncIndex = committedFuncIndex;
            this.abortedFuncIndex = abortedFuncIndex;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(transactionType));
            sj.add(String.valueOf(blockId));
            sj.add(String.valueOf(retryCountReg));
            sj.add(String.valueOf(committedFuncIndex));
            sj.add(String.valueOf(abortedFuncIndex));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code InstructionEnd} represents the TR_END instruction in Ballerina bytecode.
     * <p>
     * The TR_END instruction performs transaction ending in multiple stages of the transaction.
     *
     * @since 0.990.0
     */
    public static class InstructionTrEnd extends Instruction {
        public final int endType;
        public final int blockId;
        public final int statusRegIndex;
        public final int errorRegIndex;

        InstructionTrEnd(int opcode, int blockId, int endType,
                           int statusRegIndex, int errorRegIndex) {
            super(opcode);

            this.blockId = blockId;
            this.endType = endType;
            this.statusRegIndex = statusRegIndex;
            this.errorRegIndex = errorRegIndex;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(endType));
            sj.add(String.valueOf(blockId));
            sj.add(String.valueOf(statusRegIndex));
            sj.add(String.valueOf(errorRegIndex));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }

    /**
     * {@code InstructionRetry} represents the TR_RETRY instruction in Ballerina bytecode.
     * <p>
     * The TR_RETRY instruction performs transaction retry.
     *
     * @since 0.990.0
     */
    public static class InstructionTrRetry extends Instruction {
        public final int blockId;
        public final int abortEndIp;
        public final int trStatusReg;

        InstructionTrRetry(int opcode, int blockId,
                           int abortEndIp, int trStatusReg) {
            super(opcode);

            this.blockId = blockId;
            this.abortEndIp = abortEndIp;
            this.trStatusReg = trStatusReg;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(" ");
            sj.add(String.valueOf(blockId));
            sj.add(String.valueOf(abortEndIp));
            sj.add(String.valueOf(trStatusReg));
            return Mnemonics.getMnem(opcode) + " " + sj.toString();
        }
    }
}
