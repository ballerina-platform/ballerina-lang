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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionReturnCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;

/**
 * @since 0.87
 */
public class BLangVM {

    public static final int DEFAULT_CONTROL_STACK_SIZE = 2000;

    private ProgramFile programFile;
    private ConstantPoolEntry[] constPool;

    // Instruction pointer;
    private int ip = 0;
    private Instruction[] code;

    // Stack frame pointer;
    private int fp = -1;

    private StackFrame[] stackFrames = new StackFrame[DEFAULT_CONTROL_STACK_SIZE];

    public BLangVM(ProgramFile programFile) {
        this.programFile = programFile;
        this.constPool = programFile.getConstPool().toArray(new ConstantPoolEntry[0]);
        this.code = programFile.getInstructionList().toArray(new Instruction[0]);
    }

    public void execMain() {
        FunctionInfo mainFuncInfo = programFile.getFuncInfoList().get(0);
        StackFrame stackFrame = new StackFrame(mainFuncInfo, ip, new int[0]);
        stackFrames[++fp] = stackFrame;

//        traceCode();

        ip = mainFuncInfo.getCodeAttributeInfo().getCodeAddrs();


        exec();
    }

    /**
     * Act as a virtual CPU
     */
    public void exec() {
        int i;
        int j;
        int k;
        int lvIndex;
        int cpIndex;

//        BIntegerArray bIntArray;

        // TODO use HALT Instruction in the while condition
        while (ip < code.length && fp >= 0) {

            Instruction instruction = code[ip];
            int opcode = instruction.getOpcode();
            int[] operands = instruction.getOperands();
            ip++;
            StackFrame sf = stackFrames[fp];

            switch (opcode) {
                case InstructionCodes.iconst:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.longRegs[i] = ((IntegerCPEntry) constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.fconst:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.doubleRegs[i] = ((FloatCPEntry) constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.sconst:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.stringRegs[i] = ((StringCPEntry) constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.iconst_0:
                    i = operands[0];
                    sf.longRegs[i] = 0;
                    break;
                case InstructionCodes.iconst_1:
                    i = operands[0];
                    sf.longRegs[i] = 1;
                    break;
                case InstructionCodes.iconst_2:
                    i = operands[0];
                    sf.longRegs[i] = 2;
                    break;
                case InstructionCodes.iconst_3:
                    i = operands[0];
                    sf.longRegs[i] = 3;
                    break;
                case InstructionCodes.iconst_4:
                    i = operands[0];
                    sf.longRegs[i] = 4;
                    break;
                case InstructionCodes.iconst_5:
                    i = operands[0];
                    sf.longRegs[i] = 5;
                    break;
                case InstructionCodes.fconst_0:
                    i = operands[0];
                    sf.doubleRegs[i] = 0;
                    break;
                case InstructionCodes.fconst_1:
                    i = operands[0];
                    sf.doubleRegs[i] = 1;
                    break;
                case InstructionCodes.fconst_2:
                    i = operands[0];
                    sf.doubleRegs[i] = 2;
                    break;
                case InstructionCodes.fconst_3:
                    i = operands[0];
                    sf.doubleRegs[i] = 3;
                    break;
                case InstructionCodes.fconst_4:
                    i = operands[0];
                    sf.doubleRegs[i] = 4;
                    break;
                case InstructionCodes.fconst_5:
                    i = operands[0];
                    sf.doubleRegs[i] = 5;
                    break;
                case InstructionCodes.bconst_0:
                    i = operands[0];
                    sf.intRegs[i] = 0;
                    break;
                case InstructionCodes.bconst_1:
                    i = operands[0];
                    sf.intRegs[i] = 1;
                    break;
                case InstructionCodes.iload:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.longRegs[i] = sf.longLocalVars[lvIndex];
                    break;
                case InstructionCodes.sload:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.stringRegs[i] = sf.stringLocalVars[lvIndex];
                    break;
                case InstructionCodes.iaload:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
//                    bIntArray = (BIntegerArray) sf.bValueLocalVars[i];
//                    sf.longRegs[k] = bIntArray.get(j);
                    break;
                case InstructionCodes.istore:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.longLocalVars[lvIndex] = sf.longRegs[i];
                    break;
                case InstructionCodes.sstore:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.stringLocalVars[lvIndex] = sf.stringRegs[i];
                    break;
                case InstructionCodes.iastore:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
//                    bIntArray = (BIntegerArray) sf.bValueLocalVars[i];
//                    bIntArray.add(j, sf.longRegs[k]);
                    break;
                case InstructionCodes.iadd:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.longRegs[k] = sf.longRegs[i] + sf.longRegs[j];
                    break;
                case InstructionCodes.fadd:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.doubleRegs[k] = sf.doubleRegs[i] + sf.doubleRegs[j];
                    break;
                case InstructionCodes.sadd:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.stringRegs[k] = sf.stringRegs[i] + sf.stringRegs[j];
                    break;
                case InstructionCodes.imul:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.longRegs[k] = sf.longRegs[i] * sf.longRegs[j];
                    break;
                case InstructionCodes.isub:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.longRegs[k] = sf.longRegs[i] - sf.longRegs[j];
                    break;
                case InstructionCodes.icmp:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    if (sf.longRegs[i] == sf.longRegs[j]) {
                        sf.intRegs[k] = 0;
                    } else if (sf.longRegs[i] > sf.longRegs[j]) {
                        sf.intRegs[k] = 1;
                    } else {
                        sf.intRegs[k] = -1;
                    }
                    break;
                case InstructionCodes.ifeq:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] == 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.ifne:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] != 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.iflt:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] < 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.ifge:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] >= 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.ifgt:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] > 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.ifle:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] <= 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.goto_:
                    i = operands[0];
                    ip = i;
                    break;
                case InstructionCodes.call:
                    cpIndex = operands[0];
                    FunctionCPEntry funcCPEntry = (FunctionCPEntry) constPool[cpIndex];
                    FunctionInfo functionInfo = programFile.getFunctionInfo(
                            new FunctionInfo(funcCPEntry.getPackageCPIndex(), cpIndex, -1));

                    cpIndex = operands[1];
                    FunctionCallCPEntry funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];

                    invokeFunc(functionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.ret:
                    cpIndex = operands[0];
                    FunctionReturnCPEntry funcRetCPEntry = (FunctionReturnCPEntry) constPool[cpIndex];
                    handleReturn(funcRetCPEntry.getRegIndexes());
                    break;
                case InstructionCodes.inewarray:
                    i = operands[0];
//                    sf.bValueRegs[i] = new BIntegerArray();
                    break;
                default:
                    throw new UnsupportedOperationException("Opcode " + opcode + " not supported yet");

            }
        }
    }

    private void invokeFunc(FunctionInfo functionInfo, FunctionCallCPEntry funcCallCPEntry) {
        int[] argRegs = funcCallCPEntry.getArgRegs();
        String[] paramTypeSigs = functionInfo.getParamTypeSigs();
        StackFrame callerSF = stackFrames[fp];

        StackFrame calleeSF = new StackFrame(functionInfo, ip, funcCallCPEntry.getRetRegs());
        stackFrames[++fp] = calleeSF;

        // Copy arg values from the current StackFrame to the new StackFrame
        copyArgValues(callerSF, calleeSF, argRegs, paramTypeSigs);

        ip = functionInfo.getCodeAttributeInfo().getCodeAddrs();
    }

    private void copyArgValues(StackFrame callerSF, StackFrame calleeSF, int[] argRegs, String[] paramTypeSigs) {
        int longRegIndex = -1;

        for (int i = 0; i < argRegs.length; i++) {
            String typeSig = paramTypeSigs[i];
            int argReg = argRegs[i];
            switch (typeSig) {
                case "I":
                    calleeSF.longLocalVars[++longRegIndex] = callerSF.longRegs[argReg];
                    break;
                case "F":
                    calleeSF.doubleLocalVars[++longRegIndex] = callerSF.doubleRegs[argReg];
                    break;
                case "S":
                    calleeSF.stringLocalVars[++longRegIndex] = callerSF.stringRegs[argReg];
                    break;
            }
        }
    }

    private void handleReturn(int[] regIndexes) {
        StackFrame currentSF = stackFrames[fp];
        if (fp != 0) {

            StackFrame callersSF = stackFrames[fp - 1];
            String[] retTypeSigs = currentSF.functionInfo.getRetParamTypeSigs();

            for (int i = 0; i < regIndexes.length; i++) {
                int regIndex = regIndexes[i];
                int callersRetRegIndex = currentSF.retRegIndexes[i];
                String typeSig = retTypeSigs[i];
                switch (typeSig) {
                    case "I":
                        callersSF.longRegs[callersRetRegIndex] = currentSF.longRegs[regIndex];
                        break;
                    case "F":
                        callersSF.doubleRegs[callersRetRegIndex] = currentSF.doubleRegs[regIndex];
                        break;
                    case "S":
                        callersSF.stringRegs[callersRetRegIndex] = currentSF.stringRegs[regIndex];
                        break;
                }
            }
        }

        ip = currentSF.retAddrs;
        stackFrames[fp] = null;
        fp--;
    }

    private String getOperandsLine(int[] operands) {
        if (operands.length == 0) {
            return "";
        }

        if (operands.length == 1) {
            return "" + operands[0];
        }

        StringBuilder sb = new StringBuilder();
        sb.append(operands[0]);
        for (int i = 1; i < operands.length; i++) {
            sb.append(" ");
            sb.append(operands[i]);
        }
        return sb.toString();
    }
}
