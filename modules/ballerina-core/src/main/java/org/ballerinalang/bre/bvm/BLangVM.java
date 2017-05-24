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
import org.ballerinalang.util.exceptions.BallerinaException;

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

    public void execFunction(PackageInfo packageInfo, StackFrame[] stackFrames, int fp, int ip) {
        this.constPool = packageInfo.getConstPool().toArray(new ConstantPoolEntry[0]);
        this.code = packageInfo.getInstructionList().toArray(new Instruction[0]);
        this.stackFrames = stackFrames;
        this.fp = fp;
        this.ip = ip;

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
        while (ip >= 0 && ip < code.length && fp >= 0) {

            Instruction instruction = code[ip];
            int opcode = instruction.getOpcode();
            int[] operands = instruction.getOperands();
            ip++;
            StackFrame sf = stackFrames[fp];

            switch (opcode) {
                case InstructionCodes.ICONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.longRegs[i] = ((IntegerCPEntry) constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.FCONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.doubleRegs[i] = ((FloatCPEntry) constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.SCONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.stringRegs[i] = ((StringCPEntry) constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.ICONST_0:
                    i = operands[0];
                    sf.longRegs[i] = 0;
                    break;
                case InstructionCodes.ICONST_1:
                    i = operands[0];
                    sf.longRegs[i] = 1;
                    break;
                case InstructionCodes.ICONST_2:
                    i = operands[0];
                    sf.longRegs[i] = 2;
                    break;
                case InstructionCodes.ICONST_3:
                    i = operands[0];
                    sf.longRegs[i] = 3;
                    break;
                case InstructionCodes.ICONST_4:
                    i = operands[0];
                    sf.longRegs[i] = 4;
                    break;
                case InstructionCodes.ICONST_5:
                    i = operands[0];
                    sf.longRegs[i] = 5;
                    break;
                case InstructionCodes.FCONST_0:
                    i = operands[0];
                    sf.doubleRegs[i] = 0;
                    break;
                case InstructionCodes.FCONST_1:
                    i = operands[0];
                    sf.doubleRegs[i] = 1;
                    break;
                case InstructionCodes.FCONST_2:
                    i = operands[0];
                    sf.doubleRegs[i] = 2;
                    break;
                case InstructionCodes.FCONST_3:
                    i = operands[0];
                    sf.doubleRegs[i] = 3;
                    break;
                case InstructionCodes.FCONST_4:
                    i = operands[0];
                    sf.doubleRegs[i] = 4;
                    break;
                case InstructionCodes.FCONST_5:
                    i = operands[0];
                    sf.doubleRegs[i] = 5;
                    break;
                case InstructionCodes.BCONST_0:
                    i = operands[0];
                    sf.intRegs[i] = 0;
                    break;
                case InstructionCodes.BCONST_1:
                    i = operands[0];
                    sf.intRegs[i] = 1;
                    break;
                case InstructionCodes.ILOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.longRegs[i] = sf.longLocalVars[lvIndex];
                    break;
                case InstructionCodes.SLOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.stringRegs[i] = sf.stringLocalVars[lvIndex];
                    break;
                case InstructionCodes.RLOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.refRegs[i] = sf.refLocalVars[lvIndex];
                    break;
                case InstructionCodes.IALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bIntArray = (BIntArray) sf.refRegs[i];
                    sf.longRegs[k] = bIntArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.FALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bFloatArray = (BFloatArray) sf.refRegs[i];
                    sf.doubleRegs[k] = bFloatArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.SALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bStringArray = (BStringArray) sf.refRegs[i];
                    sf.stringRegs[k] = bStringArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.BALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBooleanArray = (BBooleanArray) sf.refRegs[i];
                    sf.intRegs[k] = bBooleanArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.RALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.refRegs[i];
                    sf.refRegs[k] = bArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.ISTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.longLocalVars[lvIndex] = sf.longRegs[i];
                    break;
                case InstructionCodes.SSTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.stringLocalVars[lvIndex] = sf.stringRegs[i];
                    break;
                case InstructionCodes.RSTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.refLocalVars[lvIndex] = sf.refRegs[i];
                    break;
                case InstructionCodes.IASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bIntArray = (BIntArray) sf.refRegs[i];
                    bIntArray.add(sf.longRegs[j], sf.longRegs[k]);
                    break;
                case InstructionCodes.FASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bFloatArray = (BFloatArray) sf.refRegs[i];
                    bFloatArray.add(sf.longRegs[j], sf.doubleRegs[k]);
                    break;
                case InstructionCodes.SASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bStringArray = (BStringArray) sf.refRegs[i];
                    bStringArray.add(sf.longRegs[j], sf.stringRegs[k]);
                    break;
                case InstructionCodes.BASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBooleanArray = (BBooleanArray) sf.refRegs[i];
                    bBooleanArray.add(sf.longRegs[j], sf.intRegs[k]);
                    break;
                case InstructionCodes.RASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.refRegs[i];
                    bArray.add(sf.longRegs[j], sf.refRegs[k]);
                    break;
                case InstructionCodes.IFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    sf.longRegs[j] = structureType.getIntField(fieldIndex);
                    break;
                case InstructionCodes.FFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    sf.doubleRegs[j] = structureType.getFloatField(fieldIndex);
                    break;
                case InstructionCodes.SFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    sf.stringRegs[j] = structureType.getStringField(fieldIndex);
                    break;
                case InstructionCodes.BFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    sf.intRegs[j] = structureType.getBooleanField(fieldIndex);
                    break;
                case InstructionCodes.RFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    sf.refRegs[j] = structureType.getRefField(fieldIndex);
                    break;
                case InstructionCodes.IFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setIntField(fieldIndex, sf.longRegs[j]);
                    break;
                case InstructionCodes.FFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setFloatField(fieldIndex, sf.doubleRegs[j]);
                    break;
                case InstructionCodes.SFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setStringField(fieldIndex, sf.stringRegs[j]);
                    break;
                case InstructionCodes.BFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setBooleanField(fieldIndex, sf.intRegs[j]);
                    break;
                case InstructionCodes.RFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setRefField(fieldIndex, sf.refRegs[j]);
                    break;

                case InstructionCodes.IADD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.longRegs[k] = sf.longRegs[i] + sf.longRegs[j];
                    break;
                case InstructionCodes.FADD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.doubleRegs[k] = sf.doubleRegs[i] + sf.doubleRegs[j];
                    break;
                case InstructionCodes.SADD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.stringRegs[k] = sf.stringRegs[i] + sf.stringRegs[j];
                    break;
                case InstructionCodes.ISUB:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.longRegs[k] = sf.longRegs[i] - sf.longRegs[j];
                    break;
                case InstructionCodes.FSUB:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.doubleRegs[k] = sf.doubleRegs[i] - sf.doubleRegs[j];
                    break;
                case InstructionCodes.IMUL:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.longRegs[k] = sf.longRegs[i] * sf.longRegs[j];
                    break;
                case InstructionCodes.FMUL:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.doubleRegs[k] = sf.doubleRegs[i] * sf.doubleRegs[j];
                    break;
                case InstructionCodes.IDIV:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    // TODO improve error handling in VM
                    if (sf.longRegs[j] == 0) {
                        throw new BallerinaException(" / by zero");
                    }

                    sf.longRegs[k] = sf.longRegs[i] / sf.longRegs[j];
                    break;
                case InstructionCodes.FDIV:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    // TODO improve error handling in VM
                    if (sf.doubleRegs[j] == 0) {
                        throw new BallerinaException(" / by zero");
                    }

                    sf.doubleRegs[k] = sf.doubleRegs[i] / sf.doubleRegs[j];
                    break;
                case InstructionCodes.IMOD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    // TODO improve error handling in VM
                    if (sf.longRegs[j] == 0) {
                        throw new BallerinaException(" / by zero");
                    }

                    sf.longRegs[k] = sf.longRegs[i] % sf.longRegs[j];
                    break;
                case InstructionCodes.FMOD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    // TODO improve error handling in VM
                    if (sf.doubleRegs[j] == 0) {
                        throw new BallerinaException(" / by zero");
                    }

                    sf.doubleRegs[k] = sf.doubleRegs[i] % sf.doubleRegs[j];
                    break;
                case InstructionCodes.INEG:
                    i = operands[0];
                    j = operands[1];
                    sf.longRegs[j] = -sf.longRegs[i];
                    break;
                case InstructionCodes.FNEG:
                    i = operands[0];
                    j = operands[1];
                    sf.doubleRegs[j] = -sf.doubleRegs[i];
                    break;

                case InstructionCodes.ICMP:
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
                case InstructionCodes.IFEQ:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] == 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.IFNE:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] != 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.IFLT:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] < 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.IFGE:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] >= 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.IFGT:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] > 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.IFLE:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] <= 0) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.GOTO:
                    i = operands[0];
                    ip = i;
                    break;
                case InstructionCodes.CALL:
                    cpIndex = operands[0];
                    FunctionRefCPEntry funcCPEntry = (FunctionRefCPEntry) constPool[cpIndex];
                    FunctionInfo functionInfo = funcCPEntry.getFunctionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(functionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.ACALL:
                    cpIndex = operands[0];
                    ActionRefCPEntry actionRefCPEntry = (ActionRefCPEntry) constPool[cpIndex];
                    ActionInfo actionInfo = actionRefCPEntry.getActionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(actionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.RET:
                    cpIndex = operands[0];
                    FunctionReturnCPEntry funcRetCPEntry = (FunctionReturnCPEntry) constPool[cpIndex];
                    handleReturn(funcRetCPEntry.getRegIndexes());
                    break;
                case InstructionCodes.INEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BIntArray();
                    break;
                case InstructionCodes.FNEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BFloatArray();
                    break;
                case InstructionCodes.SNEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BStringArray();
                    break;
                case InstructionCodes.BNEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BBooleanArray();
                    break;
                case InstructionCodes.RNEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BRefValueArray();
                    break;
                case InstructionCodes.NEWSTRUCT:
                    cpIndex = operands[0];
                    i = operands[1];
                    structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
                    fieldCount = structureRefCPEntry.getStructureTypeInfo().getFieldCount();
                    BStruct bStruct = new BStruct();
                    bStruct.init(fieldCount);
                    sf.refRegs[i] = bStruct;
                    break;
                case InstructionCodes.NEWCONNECTOR:
                    cpIndex = operands[0];
                    i = operands[1];
                    structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
                    fieldCount = structureRefCPEntry.getStructureTypeInfo().getFieldCount();
                    BConnector bConnector = new BConnector();
                    bConnector.init(fieldCount);
                    sf.refRegs[i] = bConnector;
                    break;
                default:
                    throw new UnsupportedOperationException("Opcode " + opcode + " is not supported yet");
            }
        }
    }

    public void invokeCallableUnit(CallableUnitInfo callableUnitInfo, FunctionCallCPEntry funcCallCPEntry) {
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
                    calleeSF.refLocalVars[++refRegIndex] = callerSF.refRegs[argReg];
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
                        callersSF.refRegs[callersRetRegIndex] = currentSF.refRegs[regIndex];
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
