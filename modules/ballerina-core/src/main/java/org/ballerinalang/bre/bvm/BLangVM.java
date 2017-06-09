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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.bre.BallerinaTransactionManager;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBlobArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.StructureType;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.runtime.DefaultBalCallback;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.services.DefaultServerConnectorErrorHandler;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ErrorTableEntry;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.codegen.Mnemonics;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.TypeCPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerDataChannelRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerInvokeCPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerReplyCPEntry;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * @since 0.87
 */
public class BLangVM {

    private static final Logger logger = LoggerFactory.getLogger(BLangVM.class);
    private Context context;
    private ControlStackNew controlStack;
    private ProgramFile programFile;
    private ConstantPoolEntry[] constPool;

    // Instruction pointer;
    private int ip = 0;
    private Instruction[] code;

    private StructureType globalMemBlock;

    public BLangVM(ProgramFile programFile) {
        this.programFile = programFile;
        this.globalMemBlock = programFile.getGlobalMemoryBlock();
    }

    // TODO Remove
    private void traceCode(PackageInfo packageInfo) {
        PrintStream printStream = System.out;
        for (int i = 0; i < code.length; i++) {
            printStream.println(i + ": " + Mnemonics.getMnem(code[i].getOpcode()) + " " +
                    getOperandsLine(code[i].getOperands()));
        }

//        printStream.println("ErrorTable\n\t\tfrom\tto\ttarget\terror");
//        packageInfo.getErrorTableEntriesList().forEach(error -> printStream.println(error.toString()));
//
//        printStream.println("LineNumberTable\n\tline\t\tip");
//        packageInfo.getLineNumberInfoList().forEach(line -> printStream.println(line.toString()));
    }

    public void run(Context context) {
        StackFrame currentFrame = context.getControlStackNew().getCurrentFrame();
        this.constPool = currentFrame.packageInfo.getConstPool();
        this.code = currentFrame.packageInfo.getInstructions();

        this.context = context;
        this.controlStack = context.getControlStackNew();
        this.context.setVMBasedExecutor(true);
        this.ip = context.getStartIP();

//        traceCode(null);

        if (context.getError() != null) {
            handleError();
        } else if (context.actionInfo != null) {
            // // TODO : Temporary to solution make non-blocking working.
            BType[] retTypes = context.actionInfo.getRetParamTypes();
            StackFrame calleeSF = controlStack.popFrame();
            this.constPool = controlStack.currentFrame.packageInfo.getConstPool();
            this.code = controlStack.currentFrame.packageInfo.getInstructions();
            handleReturnFromNativeCallableUnit(controlStack.currentFrame, context.funcCallCPEntry.getRetRegs(),
                    calleeSF.returnValues, retTypes);

            // TODO Remove
            prepareStructureTypeFromNativeAction(context.nativeArgValues);
            context.nativeArgValues = null;
            context.funcCallCPEntry = null;
            context.actionInfo = null;
        }
        exec();
    }

    /**
     * Act as a virtual CPU
     */
    private void exec() {
        int i;
        int j;
        int k;
        int lvIndex; // Index of the local variable
        int cpIndex; // Index of the constant pool
        int fieldIndex;

        int[] fieldCount;

        BIntArray bIntArray;
        BFloatArray bFloatArray;
        BStringArray bStringArray;
        BBooleanArray bBooleanArray;
        BBlobArray bBlobArray;
        BRefValueArray bArray;
        StructureType structureType;
        BMap<String, BRefType> bMap;
        BRefType bRefType;

        StructureRefCPEntry structureRefCPEntry;
        FunctionCallCPEntry funcCallCPEntry;
        FunctionRefCPEntry funcRefCPEntry;
        TypeCPEntry typeCPEntry;
        ActionRefCPEntry actionRefCPEntry;
        StringCPEntry stringCPEntry;

        FunctionInfo functionInfo;
        ActionInfo actionInfo;
        StructureTypeInfo structureTypeInfo;
        WorkerDataChannelRefCPEntry workerRefCPEntry;
        WorkerInvokeCPEntry workerInvokeCPEntry;
        WorkerReplyCPEntry workerReplyCPEntry;
        WorkerDataChannel workerDataChannel;

        StackFrame currentSF, callersSF;
        int callersRetRegIndex;

        // TODO use HALT Instruction in the while condition
        while (ip >= 0 && ip < code.length && controlStack.fp >= 0) {

            Instruction instruction = code[ip];
            int opcode = instruction.getOpcode();
            int[] operands = instruction.getOperands();
            ip++;
            StackFrame sf = controlStack.getCurrentFrame();

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
                case InstructionCodes.RCONST_NULL:
                    i = operands[0];
                    sf.refRegs[i] = null;
                    break;

                case InstructionCodes.ILOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.longRegs[i] = sf.longLocalVars[lvIndex];
                    break;
                case InstructionCodes.FLOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.doubleRegs[i] = sf.doubleLocalVars[lvIndex];
                    break;
                case InstructionCodes.SLOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.stringRegs[i] = sf.stringLocalVars[lvIndex];
                    break;
                case InstructionCodes.BLOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.intRegs[i] = sf.intLocalVars[lvIndex];
                    break;
                case InstructionCodes.LLOAD:
                    lvIndex = operands[0];
                    i = operands[1];
                    sf.byteRegs[i] = sf.byteLocalVars[lvIndex];
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
                case InstructionCodes.LALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBlobArray = (BBlobArray) sf.refRegs[i];
                    sf.byteRegs[k] = bBlobArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.RALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.refRegs[i];
                    sf.refRegs[k] = bArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.JSONALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    // TODO Proper error handling
                    sf.refRegs[k] = JSONUtils.getArrayElement((BJSON) sf.refRegs[i], sf.longRegs[j]);
                    break;
                case InstructionCodes.IGLOAD:
                    // Global variable index
                    i = operands[0];
                    // Stack registry index
                    j = operands[1];
                    sf.longRegs[j] = globalMemBlock.getIntField(i);
                    break;
                case InstructionCodes.FGLOAD:
                    i = operands[0];
                    j = operands[1];
                    sf.doubleRegs[j] = globalMemBlock.getFloatField(i);
                    break;
                case InstructionCodes.SGLOAD:
                    i = operands[0];
                    j = operands[1];
                    sf.stringRegs[j] = globalMemBlock.getStringField(i);
                    break;
                case InstructionCodes.BGLOAD:
                    i = operands[0];
                    j = operands[1];
                    sf.intRegs[j] = globalMemBlock.getBooleanField(i);
                    break;
                case InstructionCodes.LGLOAD:
                    i = operands[0];
                    j = operands[1];
                    sf.byteRegs[j] = globalMemBlock.getBlobField(i);
                    break;
                case InstructionCodes.RGLOAD:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = globalMemBlock.getRefField(i);
                    break;

                case InstructionCodes.ISTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.longLocalVars[lvIndex] = sf.longRegs[i];
                    break;
                case InstructionCodes.FSTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.doubleLocalVars[lvIndex] = sf.doubleRegs[i];
                    break;
                case InstructionCodes.SSTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.stringLocalVars[lvIndex] = sf.stringRegs[i];
                    break;
                case InstructionCodes.BSTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.intLocalVars[lvIndex] = sf.intRegs[i];
                    break;
                case InstructionCodes.LSTORE:
                    i = operands[0];
                    lvIndex = operands[1];
                    sf.byteLocalVars[lvIndex] = sf.byteRegs[i];
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
                case InstructionCodes.LASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBlobArray = (BBlobArray) sf.refRegs[i];
                    bBlobArray.add(sf.longRegs[j], sf.byteRegs[k]);
                    break;
                case InstructionCodes.RASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.refRegs[i];
                    bArray.add(sf.longRegs[j], sf.refRegs[k]);
                    break;
                case InstructionCodes.JSONASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    // TODO Proper error handling
                    JSONUtils.setArrayElement((BJSON) sf.refRegs[i], sf.longRegs[j], (BJSON) sf.refRegs[k]);
                    break;
                case InstructionCodes.IGSTORE:
                    // Stack reg index
                    i = operands[0];
                    // Global var index
                    j = operands[1];
                    globalMemBlock.setIntField(j, sf.longRegs[i]);
                    break;
                case InstructionCodes.FGSTORE:
                    i = operands[0];
                    j = operands[1];
                    globalMemBlock.setFloatField(j, sf.doubleRegs[i]);
                    break;
                case InstructionCodes.SGSTORE:
                    i = operands[0];
                    j = operands[1];
                    globalMemBlock.setStringField(j, sf.stringRegs[i]);
                    break;
                case InstructionCodes.BGSTORE:
                    i = operands[0];
                    j = operands[1];
                    globalMemBlock.setBooleanField(j, sf.intRegs[i]);
                    break;
                case InstructionCodes.LGSTORE:
                    i = operands[0];
                    j = operands[1];
                    globalMemBlock.setBlobField(j, sf.byteRegs[i]);
                    break;
                case InstructionCodes.RGSTORE:
                    i = operands[0];
                    j = operands[1];
                    globalMemBlock.setRefField(j, sf.refRegs[i]);
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
                case InstructionCodes.LFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    sf.byteRegs[j] = structureType.getBlobField(fieldIndex);
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
                case InstructionCodes.LFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setBlobField(fieldIndex, sf.byteRegs[j]);
                    break;
                case InstructionCodes.RFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    structureType.setRefField(fieldIndex, sf.refRegs[j]);
                    break;

                case InstructionCodes.MAPLOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bMap = (BMap<String, BRefType>) sf.refRegs[i];
                    sf.refRegs[k] = bMap.get(sf.stringRegs[j]);
                    break;
                case InstructionCodes.MAPSTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bMap = (BMap<String, BRefType>) sf.refRegs[i];
                    bMap.put(sf.stringRegs[j], sf.refRegs[k]);
                    break;

                case InstructionCodes.JSONLOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    // TODO Proper error handling
                    sf.refRegs[k] = JSONUtils.getElement((BJSON) sf.refRegs[i], sf.stringRegs[j]);
                    break;
                case InstructionCodes.JSONSTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    // TODO Proper error handling
                    JSONUtils.setElement((BJSON) sf.refRegs[i], sf.stringRegs[j], (BJSON) sf.refRegs[k]);
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
                case InstructionCodes.XMLADD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    // Here it is assumed that a refType addition can only be a xml-concat.
                    sf.refRegs[k] = XMLUtils.concatenate((BXML) sf.refRegs[i], (BXML) sf.refRegs[j]);
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

                    if (sf.longRegs[j] == 0) {
                        context.setError(BLangVMErrors.createError(context, ip, " / by zero"));
                        handleError();
                        break;
                    }

                    sf.longRegs[k] = sf.longRegs[i] / sf.longRegs[j];
                    break;
                case InstructionCodes.FDIV:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    if (sf.doubleRegs[j] == 0) {
                        context.setError(BLangVMErrors.createError(context, ip, " / by zero"));
                        handleError();
                        break;
                    }

                    sf.doubleRegs[k] = sf.doubleRegs[i] / sf.doubleRegs[j];
                    break;
                case InstructionCodes.IMOD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    if (sf.longRegs[j] == 0) {
                        context.setError(BLangVMErrors.createError(context, ip, " / by zero"));
                        handleError();
                        break;
                    }

                    sf.longRegs[k] = sf.longRegs[i] % sf.longRegs[j];
                    break;
                case InstructionCodes.FMOD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    if (sf.doubleRegs[j] == 0) {
                        context.setError(BLangVMErrors.createError(context, ip, " / by zero"));
                        handleError();
                        break;
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
                case InstructionCodes.BNOT:
                    i = operands[0];
                    j = operands[1];
                    sf.intRegs[j] = sf.intRegs[i] == 0 ? 1 : 0;
                    break;

                case InstructionCodes.IEQ:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.longRegs[i] == sf.longRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.FEQ:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.doubleRegs[i] == sf.doubleRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.SEQ:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.stringRegs[i].equals(sf.stringRegs[j]) ? 1 : 0;
                    break;
                case InstructionCodes.BEQ:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.intRegs[i] == sf.intRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.REQ:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.refRegs[i] == sf.refRegs[j] ? 1 : 0;
                    break;

                case InstructionCodes.INE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.longRegs[i] != sf.longRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.FNE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.doubleRegs[i] != sf.doubleRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.SNE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = !(sf.stringRegs[i].equals(sf.stringRegs[j])) ? 1 : 0;
                    break;
                case InstructionCodes.BNE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.intRegs[i] != sf.intRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.RNE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.refRegs[i] != sf.refRegs[j] ? 1 : 0;
                    break;

                case InstructionCodes.IGT:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.longRegs[i] > sf.longRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.FGT:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.doubleRegs[i] > sf.doubleRegs[j] ? 1 : 0;
                    break;

                case InstructionCodes.IGE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.longRegs[i] >= sf.longRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.FGE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.doubleRegs[i] >= sf.doubleRegs[j] ? 1 : 0;
                    break;

                case InstructionCodes.ILT:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.longRegs[i] < sf.longRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.FLT:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.doubleRegs[i] < sf.doubleRegs[j] ? 1 : 0;
                    break;

                case InstructionCodes.ILE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.longRegs[i] <= sf.longRegs[j] ? 1 : 0;
                    break;
                case InstructionCodes.FLE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    sf.intRegs[k] = sf.doubleRegs[i] <= sf.doubleRegs[j] ? 1 : 0;
                    break;

                case InstructionCodes.REQ_NULL:
                    i = operands[0];
                    j = operands[1];
                    if (sf.refRegs[i] == null) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.RNE_NULL:
                    i = operands[0];
                    j = operands[1];
                    if (sf.refRegs[i] != null) {
                        ip = j;
                    }
                    break;

                case InstructionCodes.BR_TRUE:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] == 1) {
                        ip = j;
                    }
                    break;
                case InstructionCodes.BR_FALSE:
                    i = operands[0];
                    j = operands[1];
                    if (sf.intRegs[i] == 0) {
                        ip = j;
                    }
                    break;

                case InstructionCodes.GOTO:
                    i = operands[0];
                    ip = i;
                    break;
                case InstructionCodes.CALL:
                    cpIndex = operands[0];
                    funcRefCPEntry = (FunctionRefCPEntry) constPool[cpIndex];
                    functionInfo = funcRefCPEntry.getFunctionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(functionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.TRBGN: {
                    BallerinaTransactionManager ballerinaTransactionManager = context.getBallerinaTransactionManager();
                    if (ballerinaTransactionManager == null) {
                        ballerinaTransactionManager = new BallerinaTransactionManager();
                        context.setBallerinaTransactionManager(ballerinaTransactionManager);
                    }
                    ballerinaTransactionManager.beginTransactionBlock();
                }
                    break;
                case InstructionCodes.TREND: {
                    i = operands[0];
                    BallerinaTransactionManager ballerinaTransactionManager = context.getBallerinaTransactionManager();
                    if (ballerinaTransactionManager != null) {
                        if (i == 0) {
                            ballerinaTransactionManager.commitTransactionBlock();
                        } else {
                            ballerinaTransactionManager.setTransactionError(true);
                            ballerinaTransactionManager.rollbackTransactionBlock();
                        }
                        ballerinaTransactionManager.endTransactionBlock();
                        if (ballerinaTransactionManager.isOuterTransaction()) {
                            context.setBallerinaTransactionManager(null);
                        }
                    }
                }
                    break;
                case InstructionCodes.WRKINVOKE:
                    cpIndex = operands[0];
                    workerRefCPEntry = (WorkerDataChannelRefCPEntry) constPool[cpIndex];
                    workerDataChannel = workerRefCPEntry.getWorkerDataChannel();

                    cpIndex = operands[1];
                    workerInvokeCPEntry = (WorkerInvokeCPEntry) constPool[cpIndex];
                    invokeWorker(workerDataChannel, workerInvokeCPEntry);
                    break;
                case InstructionCodes.WRKREPLY:
                    cpIndex = operands[0];
                    workerRefCPEntry = (WorkerDataChannelRefCPEntry) constPool[cpIndex];
                    workerDataChannel = workerRefCPEntry.getWorkerDataChannel();

                    cpIndex = operands[1];
                    workerReplyCPEntry = (WorkerReplyCPEntry) constPool[cpIndex];
                    replyWorker(workerDataChannel, workerReplyCPEntry);
                    break;
                case InstructionCodes.NCALL:
                    cpIndex = operands[0];
                    funcRefCPEntry = (FunctionRefCPEntry) constPool[cpIndex];
                    functionInfo = funcRefCPEntry.getFunctionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeNativeFunction(functionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.ACALL:
                    cpIndex = operands[0];
                    actionRefCPEntry = (ActionRefCPEntry) constPool[cpIndex];
                    actionInfo = actionRefCPEntry.getActionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(actionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.NACALL:
                    cpIndex = operands[0];
                    actionRefCPEntry = (ActionRefCPEntry) constPool[cpIndex];
                    actionInfo = actionRefCPEntry.getActionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeNativeAction(actionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.THROW:
                    i = operands[0];
                    if (i >= 0) {
                        BStruct error = (BStruct) sf.refRegs[i];
                        BLangVMErrors.setStackTrace(context, ip, error);
                        context.setError(error);
                    }
                    handleError();
                    break;
                case InstructionCodes.ERRSTORE:
                    i = operands[0];
                    sf.refLocalVars[i] = context.getError();
                    // clear error.
                    context.setError(null);
                    break;
                case InstructionCodes.I2F:
                    i = operands[0];
                    j = operands[1];
                    sf.doubleRegs[j] = (double) sf.longRegs[i];
                    break;
                case InstructionCodes.I2S:
                    i = operands[0];
                    j = operands[1];
                    sf.stringRegs[j] = Long.toString(sf.longRegs[i]);
                    break;
                case InstructionCodes.I2B:
                    i = operands[0];
                    j = operands[1];
                    sf.intRegs[j] = sf.longRegs[i] != 0 ? 1 : 0;
                    break;
                case InstructionCodes.I2ANY:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BInteger(sf.longRegs[i]);
                    break;
                case InstructionCodes.I2JSON:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BJSON(Long.toString(sf.longRegs[i]));
                    break;
                case InstructionCodes.F2I:
                    i = operands[0];
                    j = operands[1];
                    sf.longRegs[j] = (long) sf.doubleRegs[i];
                    break;
                case InstructionCodes.F2S:
                    i = operands[0];
                    j = operands[1];
                    sf.stringRegs[j] = Double.toString(sf.doubleRegs[i]);
                    break;
                case InstructionCodes.F2B:
                    i = operands[0];
                    j = operands[1];
                    sf.intRegs[j] = sf.doubleRegs[i] != 0.0 ? 1 : 0;
                    break;
                case InstructionCodes.F2ANY:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BFloat(sf.doubleRegs[i]);
                    break;
                case InstructionCodes.F2JSON:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BJSON(Double.toString(sf.doubleRegs[i]));
                    break;
                case InstructionCodes.S2I:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    try {
                        sf.longRegs[j] = Long.parseLong(sf.stringRegs[i]);
                    } catch (NumberFormatException e) {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.S2F:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    try {
                        sf.doubleRegs[j] = Double.parseDouble(sf.stringRegs[i]);
                    } catch (NumberFormatException e) {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.S2B:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];

                    try {
                        sf.intRegs[j] = Boolean.parseBoolean(sf.stringRegs[i]) ? 1 : 0;
                    } catch (NumberFormatException e) {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.S2ANY:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BString(sf.stringRegs[i]);
                    break;
                case InstructionCodes.S2JSON:
                    i = operands[0];
                    j = operands[1];
                    String jsonStr = StringEscapeUtils.escapeJson(sf.stringRegs[i]);
                    sf.refRegs[j] = new BJSON("\"" + jsonStr + "\"");
                    break;
                case InstructionCodes.B2I:
                    i = operands[0];
                    j = operands[1];
                    sf.longRegs[j] = sf.intRegs[i];
                    break;
                case InstructionCodes.B2F:
                    i = operands[0];
                    j = operands[1];
                    sf.doubleRegs[j] = sf.intRegs[i];
                    break;
                case InstructionCodes.B2S:
                    i = operands[0];
                    j = operands[1];
                    sf.stringRegs[j] = sf.intRegs[i] == 1 ? "true" : "false";
                    break;
                case InstructionCodes.B2ANY:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BBoolean(sf.intRegs[i] == 1);
                    break;
                case InstructionCodes.B2JSON:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BJSON(sf.intRegs[i] == 1 ? "true" : "false");
                    break;
                case InstructionCodes.L2ANY:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BBlob(sf.byteRegs[i]);
                    break;
                case InstructionCodes.JSON2I:
                    convertJSONToInt(operands, sf);
                    break;
                case InstructionCodes.JSON2F:
                    convertJSONToFloat(operands, sf);
                    break;
                case InstructionCodes.JSON2S:
                    convertJSONToString(operands, sf);
                    break;
                case InstructionCodes.JSON2B:
                    convertJSONToBoolean(operands, sf);
                    break;

                case InstructionCodes.ANY2I:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeInt) {
                        sf.longRegs[j] = ((BInteger) bRefType).intValue();
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2F:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeFloat) {
                        sf.doubleRegs[j] = ((BFloat) bRefType).floatValue();
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2S:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeString) {
                        sf.stringRegs[j] = bRefType.stringValue();
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2B:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeBoolean) {
                        sf.intRegs[j] = ((BBoolean) bRefType).booleanValue() ? 1 : 0;
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2L:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeBlob) {
                        sf.byteRegs[j] = ((BBlob) bRefType).blobValue();
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2JSON:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeJSON) {
                        sf.refRegs[j] = bRefType;
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2T:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() instanceof BStructType) {
                        sf.refRegs[j] = bRefType;
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.ANY2MAP:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bRefType = sf.refRegs[i];

                    if (bRefType.getType() == BTypes.typeMap) {
                        sf.refRegs[j] = bRefType;
                    } else {
                        // TODO
                        throw new BallerinaException("incompatible types");
                    }
                    break;
                case InstructionCodes.NULL2JSON:
                    i = operands[0];
                    j = operands[1];
                    sf.refRegs[j] = new BJSON("null");
                    break;
                case InstructionCodes.CHECKCAST:
                    i = operands[0];
                    cpIndex = operands[1];
                    j = operands[2];
                    typeCPEntry = (TypeCPEntry) constPool[cpIndex];

                    // TODO NULL Check  and Array casting
                    if (checkCast(sf.refRegs[i].getType(), typeCPEntry.getType())) {
                        sf.refRegs[j] = sf.refRegs[i];
                    } else {
                        throw new BallerinaException("Incompatible types: " +
                                sf.refRegs[i].getType() + " and " + typeCPEntry.getType());
                        // TODO Handle cast errors
                    }
                    break;
                case InstructionCodes.DT2XML:
                    i = operands[0];
                    j = operands[1];
                    bRefType = sf.refRegs[i];
                    sf.refRegs[j] = XMLUtils.datatableToXML((BDataTable) bRefType);
                    break;
                case InstructionCodes.DT2JSON:
                    i = operands[0];
                    j = operands[1];
                    bRefType = sf.refRegs[i];
                    sf.refRegs[j] = JSONUtils.toJSON((BDataTable) bRefType);
                    break;
                case InstructionCodes.INEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BIntArray();
                    break;
                case InstructionCodes.ARRAYLEN:
                    i = operands[0];
                    j = operands[1];
                    if (sf.refRegs[i] == null) {
                        //TODO improve error message to be more informative
                        throw new BallerinaException("array is null.");
                    }
                    BNewArray array = (BNewArray) sf.refRegs[i];
                    sf.longRegs[j] = array.size();
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
                    cpIndex = operands[1];
                    typeCPEntry = (TypeCPEntry) constPool[cpIndex];
                    sf.refRegs[i] = new BRefValueArray(typeCPEntry.getType());
                    break;
                case InstructionCodes.LNEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BBlobArray();
                    break;
                case InstructionCodes.JSONNEWARRAY:
                    i = operands[0];
                    j = operands[1];
                    // This is a temporary solution to create n-valued JSON array
                    StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
                    for (int index = 0; index < sf.longRegs[j]; index++) {
                        stringJoiner.add("0");
                    }
                    sf.refRegs[i] = new BJSON(stringJoiner.toString());
                    break;

                case InstructionCodes.NEWSTRUCT:
                    cpIndex = operands[0];
                    i = operands[1];
                    structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
                    structureTypeInfo = structureRefCPEntry.getStructureTypeInfo();
                    fieldCount = structureTypeInfo.getFieldCount();
                    BStruct bStruct = new BStruct(structureTypeInfo.getType());
                    bStruct.setFieldTypes(structureTypeInfo.getFieldTypes());
                    bStruct.init(fieldCount);
                    sf.refRegs[i] = bStruct;
                    break;
                case InstructionCodes.NEWCONNECTOR:
                    cpIndex = operands[0];
                    i = operands[1];
                    structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
                    structureTypeInfo = structureRefCPEntry.getStructureTypeInfo();
                    fieldCount = structureTypeInfo.getFieldCount();
                    BConnector bConnector = new BConnector(structureTypeInfo.getType());
                    bConnector.setFieldTypes(structureTypeInfo.getFieldTypes());
                    bConnector.init(fieldCount);
                    sf.refRegs[i] = bConnector;
                    break;
                case InstructionCodes.NEWMAP:
                    i = operands[0];
                    sf.refRegs[i] = new BMap<String, BRefType>();
                    break;
                case InstructionCodes.NEWJSON:
                    i = operands[0];
                    sf.refRegs[i] = new BJSON("{}");
                    break;
                case InstructionCodes.NEWMESSAGE:
                    i = operands[0];
                    sf.refRegs[i] = new BMessage();
                    break;
                case InstructionCodes.NEWDATATABLE:
                    i = operands[0];
                    sf.refRegs[i] = new BDataTable(null, new ArrayList<>(0));
                    break;
                case InstructionCodes.REP:
                    i = operands[0];
                    BMessage message = null;
                    if (i >= 0) {
                        message = (BMessage) sf.refRegs[i];
                    }
                    context.setError(null);
                    if (context.getBalCallback() != null &&
                            ((DefaultBalCallback) context.getBalCallback()).getParentCallback() != null) {
                        context.getBalCallback().done(message != null ? message.value() : null);
                    }
                    ip = -1;
                    break;
                case InstructionCodes.IRET:
                    i = operands[0];
                    j = operands[1];
                    currentSF = controlStack.getCurrentFrame();
                    callersSF = controlStack.getStack()[controlStack.fp - 1];
                    callersRetRegIndex = currentSF.retRegIndexes[i];
                    callersSF.longRegs[callersRetRegIndex] = currentSF.longRegs[j];
                    break;
                case InstructionCodes.FRET:
                    i = operands[0];
                    j = operands[1];
                    currentSF = controlStack.getCurrentFrame();
                    callersSF = controlStack.getStack()[controlStack.fp - 1];
                    callersRetRegIndex = currentSF.retRegIndexes[i];
                    callersSF.doubleRegs[callersRetRegIndex] = currentSF.doubleRegs[j];
                    break;
                case InstructionCodes.SRET:
                    i = operands[0];
                    j = operands[1];
                    currentSF = controlStack.getCurrentFrame();
                    callersSF = controlStack.getStack()[controlStack.fp - 1];
                    callersRetRegIndex = currentSF.retRegIndexes[i];
                    callersSF.stringRegs[callersRetRegIndex] = currentSF.stringRegs[j];
                    break;
                case InstructionCodes.BRET:
                    i = operands[0];
                    j = operands[1];
                    currentSF = controlStack.getCurrentFrame();
                    callersSF = controlStack.getStack()[controlStack.fp - 1];
                    callersRetRegIndex = currentSF.retRegIndexes[i];
                    callersSF.intRegs[callersRetRegIndex] = currentSF.intRegs[j];
                    break;
                case InstructionCodes.LRET:
                    i = operands[0];
                    j = operands[1];
                    currentSF = controlStack.getCurrentFrame();
                    callersSF = controlStack.getStack()[controlStack.fp - 1];
                    callersRetRegIndex = currentSF.retRegIndexes[i];
                    callersSF.byteRegs[callersRetRegIndex] = currentSF.byteRegs[j];
                    break;
                case InstructionCodes.RRET:
                    i = operands[0];
                    j = operands[1];
                    currentSF = controlStack.getCurrentFrame();
                    callersSF = controlStack.getStack()[controlStack.fp - 1];
                    callersRetRegIndex = currentSF.retRegIndexes[i];
                    callersSF.refRegs[callersRetRegIndex] = currentSF.refRegs[j];
                    break;
                case InstructionCodes.RET:
                    handleReturn();
                    break;
                default:
                    throw new UnsupportedOperationException("Opcode " + opcode + " is not supported yet");
            }
        }
    }

    public void invokeCallableUnit(CallableUnitInfo callableUnitInfo, FunctionCallCPEntry funcCallCPEntry) {
        int[] argRegs = funcCallCPEntry.getArgRegs();
        BType[] paramTypes = callableUnitInfo.getParamTypes();
        StackFrame callerSF = controlStack.getCurrentFrame();

        WorkerInfo defaultWorkerInfo = callableUnitInfo.getDefaultWorkerInfo();
        StackFrame calleeSF = new StackFrame(callableUnitInfo, defaultWorkerInfo, ip, funcCallCPEntry.getRetRegs());
        controlStack.pushFrame(calleeSF);

        // Copy arg values from the current StackFrame to the new StackFrame
        copyArgValues(callerSF, calleeSF, argRegs, paramTypes);

        // TODO Improve following two lines
        this.constPool = calleeSF.packageInfo.getConstPool();
        this.code = calleeSF.packageInfo.getInstructions();
        ip = defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs();

        // Invoke other workers
        BLangVMWorkers.invoke(programFile, callableUnitInfo, callerSF, argRegs);

    }

    public void invokeWorker(WorkerDataChannel workerDataChannel, WorkerInvokeCPEntry workerInvokeCPEntry) {
        StackFrame currentFrame = controlStack.getCurrentFrame();

        // Extract the outgoing expressions
        BValue[] arguments = new BValue[workerInvokeCPEntry.getbTypes().length];
        copyArgValuesForWorkerInvoke(currentFrame, workerInvokeCPEntry.getArgRegs(),
                workerInvokeCPEntry.getbTypes(), arguments);

        //populateArgumentValuesForWorker(expressions, arguments);
        if (workerDataChannel != null) {
            workerDataChannel.putData(arguments);
        }

//        else {
//            BArray<BValue> bArray = new BArray<>(BValue.class);
//            for (int j = 0; j < arguments.length; j++) {
//                BValue returnVal = arguments[j];
//                bArray.add(j, returnVal);
//            }
//            controlStack.setReturnValue(0, bArray);
//        }
    }

    public void replyWorker(WorkerDataChannel workerDataChannel, WorkerReplyCPEntry workerReplyCPEntry) {

        BValue[] passedInValues = (BValue[]) workerDataChannel.takeData();
        StackFrame currentFrame = controlStack.getCurrentFrame();
        //currentFrame.returnValues = passedInValues;
        copyArgValuesForWorkerReply(currentFrame, workerReplyCPEntry.getArgRegs(),
                workerReplyCPEntry.getTypes(), passedInValues);
//        for (int i = 0; i < localVars.length; i++) {
//            Expression lExpr = localVars[i];
//            BValue rValue = passedInValues[i];
//            if (lExpr instanceof VariableRefExpr) {
//                assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
//            } else if (lExpr instanceof ArrayMapAccessExpr) {
//                assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
//            } else if (lExpr instanceof FieldAccessExpr) {
//                assignValueToFieldAccessExpr(rValue, (FieldAccessExpr) lExpr);
//            }
//        }
//        int[] argRegs = funcCallCPEntry.getArgRegs();
//        BType[] paramTypes = callableUnitInfo.getParamTypes();
//        StackFrame callerSF = controlStack.getCurrentFrame();
//
//        StackFrame calleeSF = new StackFrame(callableUnitInfo, ip, funcCallCPEntry.getRetRegs());
//        controlStack.pushFrame(calleeSF);
//
//        // Copy arg values from the current StackFrame to the new StackFrame
//        copyArgValues(callerSF, calleeSF, argRegs, paramTypes);
//
//        // TODO Improve following two lines
//        this.constPool = calleeSF.packageInfo.getConstPool().toArray(new ConstantPoolEntry[0]);
//        this.code = calleeSF.packageInfo.getInstructionList().toArray(new Instruction[0]);
//        ip = callableUnitInfo.getCodeAttributeInfo().getCodeAddrs();
    }

    public static void copyArgValuesForWorkerInvoke(StackFrame callerSF, int[] argRegs, BType[] paramTypes,
                                                    BValue[] arguments) {
        for (int i = 0; i < argRegs.length; i++) {
            BType paramType = paramTypes[i];
            int argReg = argRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    arguments[i] = new BInteger(callerSF.longRegs[argReg]);
                    break;
                case TypeTags.FLOAT_TAG:
                    arguments[i] = new BFloat(callerSF.doubleRegs[argReg]);
                    break;
                case TypeTags.STRING_TAG:
                    arguments[i] = new BString(callerSF.stringRegs[argReg]);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    boolean temp = (callerSF.intRegs[argReg]) > 0 ? true : false;
                    arguments[i] = new BBoolean(temp);
                    break;
                default:
                    arguments[i] = callerSF.refRegs[argReg];
            }
        }
    }

    public static void copyArgValuesForWorkerReply(StackFrame currentSF, int[] argRegs, BType[] paramTypes,
                                                   BValue[] passedInValues) {
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;

        for (int i = 0; i < argRegs.length; i++) {
            BType paramType = paramTypes[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    currentSF.getLongRegs()[++longRegIndex] = ((BInteger) passedInValues[i]).intValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    currentSF.getDoubleRegs()[++doubleRegIndex] = ((BFloat) passedInValues[i]).floatValue();
                    break;
                case TypeTags.STRING_TAG:
                    currentSF.getStringRegs()[++stringRegIndex] = ((BString) passedInValues[i]).stringValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    currentSF.getIntRegs()[++booleanRegIndex] = (((BBoolean) passedInValues[i]).booleanValue()) ? 1 : 0;
                    break;
                default:
                    currentSF.getRefRegs()[++refRegIndex] = (BRefType) passedInValues[i];
            }
        }
    }


    public static void copyArgValues(StackFrame callerSF, StackFrame calleeSF, int[] argRegs, BType[] paramTypes) {
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        int blobRegIndex = -1;

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
                case TypeTags.BLOB_TAG:
                    calleeSF.byteLocalVars[++blobRegIndex] = callerSF.byteRegs[argReg];
                    break;
                default:
                    calleeSF.refLocalVars[++refRegIndex] = callerSF.refRegs[argReg];
            }
        }
    }

    private void handleReturn() {
        StackFrame currentSF = controlStack.popFrame();
        context.setError(null);
        if (controlStack.fp >= 0) {
            StackFrame callersSF = controlStack.currentFrame;
            // TODO Improve
            this.constPool = callersSF.packageInfo.getConstPool();
            this.code = callersSF.packageInfo.getInstructions();
        }
        ip = currentSF.retAddrs;
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

    private void invokeNativeFunction(FunctionInfo functionInfo, FunctionCallCPEntry funcCallCPEntry) {
        StackFrame callerSF = controlStack.currentFrame;
        BValue[] nativeArgValues = populateNativeArgs(callerSF, funcCallCPEntry.getArgRegs(),
                functionInfo.getParamTypes());

        // TODO Remove
        prepareStructureTypeForNativeAction(nativeArgValues);

        BType[] retTypes = functionInfo.getRetParamTypes();
        BValue[] returnValues = new BValue[retTypes.length];
        StackFrame caleeSF = new StackFrame(functionInfo, nativeArgValues, returnValues);
        controlStack.pushFrame(caleeSF);

        // Invoke Native function;
        AbstractNativeFunction nativeFunction = functionInfo.getNativeFunction();
        try {
            nativeFunction.executeNative(context);
        } catch (Throwable e) {
            context.setError(BLangVMErrors.createError(this.context, ip, e.getMessage()));
            controlStack.popFrame();
            handleError();
            return;
        }
        // Copy return values to the callers stack
        controlStack.popFrame();
        handleReturnFromNativeCallableUnit(callerSF, funcCallCPEntry.getRetRegs(), returnValues, retTypes);

        // TODO Remove
        prepareStructureTypeFromNativeAction(nativeArgValues);
    }

    private void invokeNativeAction(ActionInfo actionInfo, FunctionCallCPEntry funcCallCPEntry) {
        StackFrame callerSF = controlStack.currentFrame;
        // TODO : Remove after non blocking action usage
        BValue[] nativeArgValues = populateNativeArgs(callerSF, funcCallCPEntry.getArgRegs(),
                actionInfo.getParamTypes());

        // TODO : Remove once we handle this properly for return values
        BType[] retTypes = actionInfo.getRetParamTypes();
        BValue[] returnValues = new BValue[retTypes.length];

        StackFrame caleeSF = new StackFrame(actionInfo, actionInfo.getDefaultWorkerInfo(), 0, null, returnValues);
        copyArgValues(callerSF, caleeSF, funcCallCPEntry.getArgRegs(),
                    actionInfo.getParamTypes());


        controlStack.pushFrame(caleeSF);

        AbstractNativeAction nativeAction = actionInfo.getNativeAction();
        try {
            if (!context.initFunction && !context.isInTransaction() && nativeAction.isNonBlockingAction()) {
                // Enable non-blocking.
                context.setStartIP(ip);
                // TODO : Temporary solution to make non-blocking working.
                if (caleeSF.packageInfo == null) {
                    caleeSF.packageInfo = actionInfo.getPackageInfo();
                }
                context.programFile = programFile;
                context.nativeArgValues = nativeArgValues;
                context.funcCallCPEntry = funcCallCPEntry;
                context.actionInfo = actionInfo;
                BalConnectorCallback connectorCallback = new BalConnectorCallback(context);
                connectorCallback.setNativeAction(nativeAction);
                nativeAction.execute(context, connectorCallback);
                ip = -1;
                return;
                // release thread.
            } else {
                nativeAction.execute(context);
                // Copy return values to the callers stack
                controlStack.popFrame();
                handleReturnFromNativeCallableUnit(callerSF, funcCallCPEntry.getRetRegs(), returnValues, retTypes);

            }
        } catch (Throwable e) {
            context.setError(BLangVMErrors.createError(this.context, ip, e.getMessage()));
            controlStack.popFrame();
            handleError();
            return;
        }
    }

    public static BValue[] populateNativeArgs(StackFrame callerSF, int[] argRegs, BType[] paramTypes) {
        BValue[] nativeArgValues = new BValue[paramTypes.length];
        for (int i = 0; i < argRegs.length; i++) {
            BType paramType = paramTypes[i];
            int argReg = argRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    nativeArgValues[i] = new BInteger(callerSF.longRegs[argReg]);
                    break;
                case TypeTags.FLOAT_TAG:
                    nativeArgValues[i] = new BFloat(callerSF.doubleRegs[argReg]);
                    break;
                case TypeTags.STRING_TAG:
                    nativeArgValues[i] = new BString(callerSF.stringRegs[argReg]);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    nativeArgValues[i] = new BBoolean(callerSF.intRegs[argReg] == 1);
                    break;
                case TypeTags.BLOB_TAG:
                    nativeArgValues[i] = new BBlob(callerSF.byteRegs[argReg]);
                    break;
                default:
                    nativeArgValues[i] = callerSF.refRegs[argReg];
            }
        }
        return nativeArgValues;
    }

    public static void handleReturnFromNativeCallableUnit(StackFrame callerSF, int[] returnRegIndexes,
                                                          BValue[] returnValues, BType[] retTypes) {
        for (int i = 0; i < returnValues.length; i++) {
            int callersRetRegIndex = returnRegIndexes[i];
            BType retType = retTypes[i];
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    callerSF.longRegs[callersRetRegIndex] = ((BInteger) returnValues[i]).intValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    callerSF.doubleRegs[callersRetRegIndex] = ((BFloat) returnValues[i]).floatValue();
                    break;
                case TypeTags.STRING_TAG:
                    callerSF.stringRegs[callersRetRegIndex] = returnValues[i].stringValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    callerSF.intRegs[callersRetRegIndex] = ((BBoolean) returnValues[i]).booleanValue() ? 1 : 0;
                    break;
                case TypeTags.BLOB_TAG:
                    callerSF.byteRegs[callersRetRegIndex] = ((BBlob) returnValues[i]).blobValue();
                    break;
                default:
                    callerSF.refRegs[callersRetRegIndex] = (BRefType) returnValues[i];
            }
        }
    }

    // TODO Remove this once all the native actions are refactored
    private void prepareStructureTypeForNativeAction(BValue[] bValues) {
        for (BValue bValue : bValues) {
            if (bValue instanceof StructureType) {
                prepareStructureTypeForNativeAction((StructureType) bValue);
            }
        }
    }

    private void prepareStructureTypeForNativeAction(StructureType structureType) {
        BType[] fieldTypes = structureType.getFieldTypes();
        BValue[] memoryBlock = new BValue[fieldTypes.length];

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;

        for (int i = 0; i < fieldTypes.length; i++) {
            BType paramType = fieldTypes[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    memoryBlock[i] = new BInteger(structureType.getIntField(++longRegIndex));
                    break;
                case TypeTags.FLOAT_TAG:
                    memoryBlock[i] = new BFloat(structureType.getFloatField(++doubleRegIndex));
                    break;
                case TypeTags.STRING_TAG:
                    memoryBlock[i] = new BString(structureType.getStringField(++stringRegIndex));
                    break;
                case TypeTags.BOOLEAN_TAG:
                    memoryBlock[i] = new BBoolean(structureType.getBooleanField(++booleanRegIndex) == 1);
                    break;
                case TypeTags.BLOB_TAG:
                    memoryBlock[i] = new BBlob(structureType.getBlobField(++blobRegIndex));
                    break;
                default:
                    memoryBlock[i] = structureType.getRefField(++refRegIndex);
            }
        }

        structureType.setMemoryBlock(memoryBlock);
    }

    // TODO Remove this once all the native actions are refactored
    public static void prepareStructureTypeFromNativeAction(BValue[] bValues) {
        for (BValue bValue : bValues) {
            if (bValue instanceof StructureType) {
                prepareStructureTypeFromNativeAction((StructureType) bValue);
            }
        }
    }

    public static void prepareStructureTypeFromNativeAction(StructureType structureType) {
        BType[] fieldTypes = structureType.getFieldTypes();
        BValue[] memoryBlock = structureType.getMemoryBlock();
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;

        for (int i = 0; i < fieldTypes.length; i++) {
            BType paramType = fieldTypes[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    structureType.setIntField(++longRegIndex, ((BInteger) memoryBlock[i]).intValue());
                    break;
                case TypeTags.FLOAT_TAG:
                    structureType.setFloatField(++doubleRegIndex, ((BFloat) memoryBlock[i]).floatValue());
                    break;
                case TypeTags.STRING_TAG:
                    structureType.setStringField(++stringRegIndex, memoryBlock[i].stringValue());
                    break;
                case TypeTags.BOOLEAN_TAG:
                    structureType.setBooleanField(++booleanRegIndex,
                            ((BBoolean) memoryBlock[i]).booleanValue() ? 1 : 0);
                    break;
                case TypeTags.BLOB_TAG:
                    structureType.setBlobField(++blobRegIndex, ((BBlob) memoryBlock[i]).blobValue());
                    break;
                default:
                    structureType.setRefField(++refRegIndex, (BRefType) memoryBlock[i]);
            }
        }
    }

    private boolean checkCast(BType sourceType, BType targetType) {
        if (sourceType.equals(targetType)) {
            return true;
        }

        if (sourceType.getTag() == TypeTags.STRUCT_TAG && targetType.getTag() == TypeTags.STRUCT_TAG) {
            return checkStructEquivalency((BStructType) sourceType, (BStructType) targetType);

        }

        if (targetType.getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        // Array casting
        if (targetType.getTag() == TypeTags.ARRAY_TAG || sourceType.getTag() == TypeTags.ARRAY_TAG) {
            return checkArrayCast(sourceType, targetType);
        }

        return true;
    }

    private boolean checkArrayCast(BType sourceType, BType targetType) {
        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType sourceArrayType = (BArrayType) sourceType;
            BArrayType targetArrayType = (BArrayType) targetType;
            if (targetArrayType.getDimensions() > sourceArrayType.getDimensions()) {
                return false;
            }

            return checkArrayCast(sourceArrayType.getElementType(), targetArrayType.getElementType());
        } else if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            return targetType.getTag() == TypeTags.ANY_TAG;
        }

        return sourceType.equals(targetType);
    }

    public static boolean checkStructEquivalency(BStructType sourceType, BStructType targetType) {
        // Struct Type equivalency
        BStructType.StructField[] sFields = sourceType.getStructFields();
        BStructType.StructField[] tFields = targetType.getStructFields();

        if (tFields.length > sFields.length) {
            return false;
        }

        for (int i = 0; i < tFields.length; i++) {
            if (tFields[i].getFieldType() == sFields[i].getFieldType() &&
                    tFields[i].getFieldName().equals(sFields[i].getFieldName())) {
                continue;
            }
            return false;
        }

        return true;
    }

    // TODO Refactor these methods and move them to a proper util class
    private static void convertJSONToInt(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        // TODO  Check for NULL
//        if (bjson == null) {
//            String errorMsg =
//                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeInt);
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
//        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeInt, e.getMessage());
            throw new BallerinaException(errorMsg);
            // TODO
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
        }

        if (jsonNode.isInt() || jsonNode.isLong()) {
            sf.longRegs[j] = jsonNode.longValue();
            return;
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                BTypes.typeInt, JSONUtils.getTypeName(jsonNode));
    }

    private static void convertJSONToFloat(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        // TODO  Check for NULL
//        if (bjson == null) {
//            String errorMsg =
//                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
// BTypes.typeFloat);
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
//        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeFloat, e.getMessage());
            throw new BallerinaException(errorMsg);
            // TODO
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
        }

        if (jsonNode.isFloat() || jsonNode.isDouble()) {
            sf.doubleRegs[j] = jsonNode.doubleValue();
            return;
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                BTypes.typeFloat, JSONUtils.getTypeName(jsonNode));
    }

    private static void convertJSONToString(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        // TODO  Check for NULL
//        if (bjson == null) {
//            String errorMsg =
//                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
// BTypes.typeString);
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
//        }

        try {
            sf.stringRegs[j] = jsonValue.stringValue();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeString, e.getMessage());
            throw new BallerinaException(errorMsg);
            // TODO
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
        }
    }

    private static void convertJSONToBoolean(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        // TODO  Check for NULL
//        if (bjson == null) {
//            String errorMsg =
//                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
// BTypes.typeBoolean);
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
//        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeBoolean, e.getMessage());
            throw new BallerinaException(errorMsg);
            // TODO
//            return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
        }

        if (jsonNode.isBoolean()) {
            sf.intRegs[j] = jsonNode.booleanValue() ? 1 : 0;
            return;
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                BTypes.typeFloat, JSONUtils.getTypeName(jsonNode));
    }

    private void handleError() {
        int currentIP = ip - 1;
        StackFrame currentFrame = controlStack.getCurrentFrame();
        ErrorTableEntry match = null;
        while (controlStack.fp >= 0) {
            match = ErrorTableEntry.getMatch(currentFrame.packageInfo, currentIP, context.getError());
            if (match != null) {
                break;
            }
            controlStack.popFrame();
            if (controlStack.getCurrentFrame() == null) {
                break;
            }
            currentIP = currentFrame.retAddrs - 1;
            currentFrame = controlStack.getCurrentFrame();
        }
        if (controlStack.getCurrentFrame() == null) {
            // root level error handling.
            ip = -1;
            PrintStream err = System.err;
            err.println(BLangVMErrors.getPrintableStackTrace(context.getError()));
            if (context.getServiceInfo() != null) {
                // Invoke ServiceConnector error handler.
                Object protocol = context.getCarbonMessage().getProperty("PROTOCOL");
                Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                        BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);
                try {
                    optionalErrorHandler
                            .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                            .handleError(new BallerinaException(BLangVMErrors.getErrorMsg(context.getError
                                            ())),
                                    context.getCarbonMessage(), context.getBalCallback());
                } catch (Exception e) {
                    logger.error("cannot handle error using the error handler for: " + protocol, e);
                }
            }
            return;
        }
        // match should be not null at this point.
        if (match != null) {
            PackageInfo packageInfo = currentFrame.packageInfo;
            this.constPool = packageInfo.getConstPool();
            this.code = packageInfo.getInstructions();
            ip = match.getIpTarget();
            return;
        }
        ip = -1;
        logger.error("fatal error. incorrect error table entry.");
    }
}
