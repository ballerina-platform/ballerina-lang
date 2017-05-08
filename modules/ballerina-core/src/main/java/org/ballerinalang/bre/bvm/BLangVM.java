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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.StructureType;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionReturnCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;

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
    }

    public void execMain() {
        PackageInfo mainPkgInfo = programFile.getPackageInfo(".");
        FunctionInfo mainFuncInfo = mainPkgInfo.getFunctionInfo("main");
        StackFrame stackFrame = new StackFrame(mainFuncInfo, ip, new int[0]);

        // TODO Improve this
        this.constPool = mainPkgInfo.getConstPool().toArray(new ConstantPoolEntry[0]);
        this.code = mainPkgInfo.getInstructionList().toArray(new Instruction[0]);
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
        int fieldIndex;

        int[] fieldCount;

        BIntArray bIntArray;
        BFloatArray bFloatArray;
        BStringArray bStringArray;
        BBooleanArray bBooleanArray;
        BRefValueArray bArray;
        StructureType structureType;

        StructureRefCPEntry structureRefCPEntry;
        FunctionCallCPEntry funcCallCPEntry;

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
                case InstructionCodes.rload:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.bValueRegs[i] = sf.bValueLocalVars[lvIndex];
                    break;
                case InstructionCodes.iaload:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bIntArray = (BIntArray) sf.bValueRegs[i];
                    sf.longRegs[k] = bIntArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.faload:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bFloatArray = (BFloatArray) sf.bValueRegs[i];
                    sf.doubleRegs[k] = bFloatArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.saload:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bStringArray = (BStringArray) sf.bValueRegs[i];
                    sf.stringRegs[k] = bStringArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.baload:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBooleanArray = (BBooleanArray) sf.bValueRegs[i];
                    sf.intRegs[k] = bBooleanArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.raload:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.bValueRegs[i];
                    sf.bValueRegs[k] = bArray.get(sf.longRegs[j]);
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
                case InstructionCodes.rstore:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.bValueLocalVars[lvIndex] = sf.bValueRegs[i];
                    break;
                case InstructionCodes.iastore:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bIntArray = (BIntArray) sf.bValueRegs[i];
                    bIntArray.add(sf.longRegs[j], sf.longRegs[k]);
                    break;
                case InstructionCodes.fastore:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bFloatArray = (BFloatArray) sf.bValueRegs[i];
                    bFloatArray.add(sf.longRegs[j], sf.doubleRegs[k]);
                    break;
                case InstructionCodes.sastore:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bStringArray = (BStringArray) sf.bValueRegs[i];
                    bStringArray.add(sf.longRegs[j], sf.stringRegs[k]);
                    break;
                case InstructionCodes.bastore:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBooleanArray = (BBooleanArray) sf.bValueRegs[i];
                    bBooleanArray.add(sf.longRegs[j], sf.intRegs[k]);
                    break;
                case InstructionCodes.rastore:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.bValueRegs[i];
                    bArray.add(sf.longRegs[j], sf.bValueRegs[k]);
                    break;
                case InstructionCodes.ifieldload:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    sf.longRegs[j] = structureType.getIntField(fieldIndex);
                    break;
                case InstructionCodes.ffieldload:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    sf.doubleRegs[j] = structureType.getFloatField(fieldIndex);
                    break;
                case InstructionCodes.sfieldload:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    sf.stringRegs[j] = structureType.getStringField(fieldIndex);
                    break;
                case InstructionCodes.bfieldload:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    sf.intRegs[j] = structureType.getBooleanField(fieldIndex);
                    break;
                case InstructionCodes.rfieldload:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    sf.bValueRegs[j] = structureType.getRefField(fieldIndex);
                    break;
                case InstructionCodes.ifieldstore:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    structureType.setIntField(fieldIndex, sf.longRegs[j]);
                    break;
                case InstructionCodes.ffieldstore:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    structureType.setFloatField(fieldIndex, sf.doubleRegs[j]);
                    break;
                case InstructionCodes.sfieldstore:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    structureType.setStringField(fieldIndex, sf.stringRegs[j]);
                    break;
                case InstructionCodes.bfieldstore:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    structureType.setBooleanField(fieldIndex, sf.intRegs[j]);
                    break;
                case InstructionCodes.rfieldstore:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.bValueRegs[i];
                    structureType.setRefField(fieldIndex, sf.bValueRegs[j]);
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
                    FunctionRefCPEntry funcCPEntry = (FunctionRefCPEntry) constPool[cpIndex];
                    FunctionInfo functionInfo = funcCPEntry.getFunctionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(functionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.acall:
                    cpIndex = operands[0];
                    ActionRefCPEntry actionRefCPEntry = (ActionRefCPEntry) constPool[cpIndex];
                    ActionInfo actionInfo = actionRefCPEntry.getActionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(actionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.ret:
                    cpIndex = operands[0];
                    FunctionReturnCPEntry funcRetCPEntry = (FunctionReturnCPEntry) constPool[cpIndex];
                    handleReturn(funcRetCPEntry.getRegIndexes());
                    break;
                case InstructionCodes.inewarray:
                    i = operands[0];
                    sf.bValueRegs[i] = new BIntArray();
                    break;
                case InstructionCodes.fnewarray:
                    i = operands[0];
                    sf.bValueRegs[i] = new BFloatArray();
                    break;
                case InstructionCodes.snewarray:
                    i = operands[0];
                    sf.bValueRegs[i] = new BStringArray();
                    break;
                case InstructionCodes.bnewarray:
                    i = operands[0];
                    sf.bValueRegs[i] = new BBooleanArray();
                    break;
                case InstructionCodes.rnewarray:
                    i = operands[0];
                    sf.bValueRegs[i] = new BRefValueArray();
                    break;
                case InstructionCodes.newstruct:
                    cpIndex = operands[0];
                    i = operands[1];
                    structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
                    fieldCount = structureRefCPEntry.getStructureTypeInfo().getFieldCount();
                    BStruct bStruct = new BStruct();
                    bStruct.init(fieldCount);
                    sf.bValueRegs[i] = bStruct;
                    break;
                case InstructionCodes.newconnector:
                    cpIndex = operands[0];
                    i = operands[1];
                    structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
                    fieldCount = structureRefCPEntry.getStructureTypeInfo().getFieldCount();
                    BConnector bConnector = new BConnector();
                    bConnector.init(fieldCount);
                    sf.bValueRegs[i] = bConnector;
                    break;
                default:
                    throw new UnsupportedOperationException("Opcode " + opcode + " is not supported yet");
            }
        }
    }

    private void invokeCallableUnit(CallableUnitInfo callableUnitInfo, FunctionCallCPEntry funcCallCPEntry) {
        int[] argRegs = funcCallCPEntry.getArgRegs();
        BType[] paramTypes = callableUnitInfo.getParamTypes();
        StackFrame callerSF = stackFrames[fp];

        StackFrame calleeSF = new StackFrame(callableUnitInfo, ip, funcCallCPEntry.getRetRegs());
        stackFrames[++fp] = calleeSF;

        // Copy arg values from the current StackFrame to the new StackFrame
        copyArgValues(callerSF, calleeSF, argRegs, paramTypes);

        // TODO Improve following two lines
        this.constPool = calleeSF.packageInfo.getConstPool().toArray(new ConstantPoolEntry[0]);
        this.code = calleeSF.packageInfo.getInstructionList().toArray(new Instruction[0]);
        ip = callableUnitInfo.getCodeAttributeInfo().getCodeAddrs();
    }

    private void copyArgValues(StackFrame callerSF, StackFrame calleeSF, int[] argRegs, BType[] paramTypes) {
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;

        for (int i = 0; i < argRegs.length; i++) {
            BType paramType = paramTypes[i];
            int argReg = argRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    calleeSF.longLocalVars[++longRegIndex] = callerSF.longRegs[argReg];
                    break;
                case TypeTags.FLOAT_TAG:
                    calleeSF.doubleLocalVars[++doubleRegIndex] = callerSF.doubleRegs[argReg];
                    break;
                case TypeTags.STRING_TAG:
                    calleeSF.stringLocalVars[++stringRegIndex] = callerSF.stringRegs[argReg];
                    break;
                case TypeTags.BOOLEAN_TAG:
                    calleeSF.intLocalVars[++booleanRegIndex] = callerSF.intRegs[argReg];
                    break;
                default:
                    calleeSF.bValueLocalVars[++refRegIndex] = callerSF.bValueRegs[argReg];
            }
        }
    }

    private void handleReturn(int[] regIndexes) {
        StackFrame currentSF = stackFrames[fp];
        if (fp != 0) {

            StackFrame callersSF = stackFrames[fp - 1];
            BType[] retTypes = currentSF.callableUnitInfo.getRetParamTypes();

            for (int i = 0; i < regIndexes.length; i++) {
                int regIndex = regIndexes[i];
                int callersRetRegIndex = currentSF.retRegIndexes[i];
                BType retType = retTypes[i];
                switch (retType.getTag()) {
                    case TypeTags.INT_TAG:
                        callersSF.longRegs[callersRetRegIndex] = currentSF.longRegs[regIndex];
                        break;
                    case TypeTags.FLOAT_TAG:
                        callersSF.doubleRegs[callersRetRegIndex] = currentSF.doubleRegs[regIndex];
                        break;
                    case TypeTags.STRING_TAG:
                        callersSF.stringRegs[callersRetRegIndex] = currentSF.stringRegs[regIndex];
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        callersSF.intRegs[callersRetRegIndex] = currentSF.intRegs[regIndex];
                        break;
                    default:
                        callersSF.bValueRegs[callersRetRegIndex] = currentSF.bValueRegs[regIndex];
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
