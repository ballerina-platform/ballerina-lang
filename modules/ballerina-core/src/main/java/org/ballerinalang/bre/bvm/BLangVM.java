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
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BArray;
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
import org.ballerinalang.runtime.worker.WorkerCallback;
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
import org.ballerinalang.util.codegen.cpentries.ForkJoinCPEntry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class executes Ballerina instruction codes.
 * 
 * @since 0.88
 */
public class BLangVM {

    private static final Logger logger = LoggerFactory.getLogger(BLangVM.class);
    private Context context;
    private ControlStackNew controlStack;
    private ProgramFile programFile;
    private ConstantPoolEntry[] constPool;
    private boolean isForkJoinTimedOut;
    private CallableUnitInfo currentCallableUnitInfo;

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
    }

    public void run(Context context) {
        StackFrame currentFrame = context.getControlStackNew().getCurrentFrame();
        this.constPool = currentFrame.packageInfo.getConstPool();
        this.code = currentFrame.packageInfo.getInstructions();

        this.context = context;
        this.controlStack = context.getControlStackNew();
        this.context.setVMBasedExecutor(true);
        this.ip = context.getStartIP();
        currentCallableUnitInfo = currentFrame.getCallableUnitInfo();

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
            //prepareStructureTypeFromNativeAction(context.nativeArgValues);
            context.nativeArgValues = null;
            context.funcCallCPEntry = null;
            context.actionInfo = null;
        }

        // TODO Try catch Throwable
        exec();
    }

    public void execWorker(Context context, int startIP, int endIP) {
        context.setStartIP(startIP);
        run(context);
    }

    /**
     * Act as a virtual CPU.
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
        BJSON jsonVal;
        BStruct errorVal;

        FunctionCallCPEntry funcCallCPEntry;
        FunctionRefCPEntry funcRefCPEntry;
        TypeCPEntry typeCPEntry;
        ActionRefCPEntry actionRefCPEntry;

        FunctionInfo functionInfo;
        ActionInfo actionInfo;
        WorkerDataChannelRefCPEntry workerRefCPEntry;
        WorkerInvokeCPEntry workerInvokeCPEntry;
        WorkerReplyCPEntry workerReplyCPEntry;
        WorkerDataChannel workerDataChannel;
        ForkJoinCPEntry forkJoinCPEntry;

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
                    if (bIntArray == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.longRegs[k] = bIntArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.FALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bFloatArray = (BFloatArray) sf.refRegs[i];
                    if (bFloatArray == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.doubleRegs[k] = bFloatArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.SALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bStringArray = (BStringArray) sf.refRegs[i];
                    if (bStringArray == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.stringRegs[k] = bStringArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.BALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBooleanArray = (BBooleanArray) sf.refRegs[i];
                    if (bBooleanArray == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.intRegs[k] = bBooleanArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.LALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBlobArray = (BBlobArray) sf.refRegs[i];
                    if (bBlobArray == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.byteRegs[k] = bBlobArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.RALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.refRegs[i];
                    if (bArray == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.refRegs[k] = bArray.get(sf.longRegs[j]);
                    break;
                case InstructionCodes.JSONALOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    jsonVal = (BJSON) sf.refRegs[i];
                    if (jsonVal == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.refRegs[k] = JSONUtils.getArrayElement(jsonVal, sf.longRegs[j]);
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
                    if (bIntArray == null) {
                        handleNullRefError();
                        break;
                    }

                    bIntArray.add(sf.longRegs[j], sf.longRegs[k]);
                    break;
                case InstructionCodes.FASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bFloatArray = (BFloatArray) sf.refRegs[i];
                    if (bFloatArray == null) {
                        handleNullRefError();
                        break;
                    }

                    bFloatArray.add(sf.longRegs[j], sf.doubleRegs[k]);
                    break;
                case InstructionCodes.SASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bStringArray = (BStringArray) sf.refRegs[i];
                    if (bStringArray == null) {
                        handleNullRefError();
                        break;
                    }

                    bStringArray.add(sf.longRegs[j], sf.stringRegs[k]);
                    break;
                case InstructionCodes.BASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBooleanArray = (BBooleanArray) sf.refRegs[i];
                    if (bBooleanArray == null) {
                        handleNullRefError();
                        break;
                    }

                    bBooleanArray.add(sf.longRegs[j], sf.intRegs[k]);
                    break;
                case InstructionCodes.LASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bBlobArray = (BBlobArray) sf.refRegs[i];
                    if (bBlobArray == null) {
                        handleNullRefError();
                        break;
                    }

                    bBlobArray.add(sf.longRegs[j], sf.byteRegs[k]);
                    break;
                case InstructionCodes.RASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bArray = (BRefValueArray) sf.refRegs[i];
                    if (bArray == null) {
                        handleNullRefError();
                        break;
                    }

                    bArray.add(sf.longRegs[j], sf.refRegs[k]);
                    break;
                case InstructionCodes.JSONASTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    jsonVal = (BJSON) sf.refRegs[i];
                    if (jsonVal == null) {
                        handleNullRefError();
                        break;
                    }

                    JSONUtils.setArrayElement(jsonVal, sf.longRegs[j], (BJSON) sf.refRegs[k]);
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
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.longRegs[j] = structureType.getIntField(fieldIndex);
                    break;
                case InstructionCodes.FFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.doubleRegs[j] = structureType.getFloatField(fieldIndex);
                    break;
                case InstructionCodes.SFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.stringRegs[j] = structureType.getStringField(fieldIndex);
                    break;
                case InstructionCodes.BFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.intRegs[j] = structureType.getBooleanField(fieldIndex);
                    break;
                case InstructionCodes.LFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.byteRegs[j] = structureType.getBlobField(fieldIndex);
                    break;
                case InstructionCodes.RFIELDLOAD:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.refRegs[j] = structureType.getRefField(fieldIndex);
                    break;
                case InstructionCodes.IFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    structureType.setIntField(fieldIndex, sf.longRegs[j]);
                    break;
                case InstructionCodes.FFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    structureType.setFloatField(fieldIndex, sf.doubleRegs[j]);
                    break;
                case InstructionCodes.SFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    structureType.setStringField(fieldIndex, sf.stringRegs[j]);
                    break;
                case InstructionCodes.BFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    structureType.setBooleanField(fieldIndex, sf.intRegs[j]);
                    break;
                case InstructionCodes.LFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    structureType.setBlobField(fieldIndex, sf.byteRegs[j]);
                    break;
                case InstructionCodes.RFIELDSTORE:
                    i = operands[0];
                    fieldIndex = operands[1];
                    j = operands[2];
                    structureType = (StructureType) sf.refRegs[i];
                    if (structureType == null) {
                        handleNullRefError();
                        break;
                    }

                    structureType.setRefField(fieldIndex, sf.refRegs[j]);
                    break;

                case InstructionCodes.MAPLOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bMap = (BMap<String, BRefType>) sf.refRegs[i];
                    if (bMap == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.refRegs[k] = bMap.get(sf.stringRegs[j]);
                    break;
                case InstructionCodes.MAPSTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    bMap = (BMap<String, BRefType>) sf.refRegs[i];
                    if (bMap == null) {
                        handleNullRefError();
                        break;
                    }

                    bMap.put(sf.stringRegs[j], sf.refRegs[k]);
                    break;

                case InstructionCodes.JSONLOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    jsonVal = (BJSON) sf.refRegs[i];
                    if (jsonVal == null) {
                        handleNullRefError();
                        break;
                    }

                    sf.refRegs[k] = JSONUtils.getElement(jsonVal, sf.stringRegs[j]);
                    break;
                case InstructionCodes.JSONSTORE:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    jsonVal = (BJSON) sf.refRegs[i];
                    if (jsonVal == null) {
                        handleNullRefError();
                        break;
                    }

                    JSONUtils.setElement(jsonVal, sf.stringRegs[j], (BJSON) sf.refRegs[k]);
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
                    BXML lhsXMLVal = (BXML) sf.refRegs[i];
                    BXML rhsXMLVal = (BXML) sf.refRegs[j];
                    if (lhsXMLVal == null || rhsXMLVal == null) {
                        handleNullRefError();
                        break;
                    }

                    // Here it is assumed that a refType addition can only be a xml-concat.
                    sf.refRegs[k] = XMLUtils.concatenate(lhsXMLVal, rhsXMLVal);
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
                case InstructionCodes.HALT:
                    ip = -1;
                    break;
                case InstructionCodes.CALL:
                    cpIndex = operands[0];
                    funcRefCPEntry = (FunctionRefCPEntry) constPool[cpIndex];
                    functionInfo = funcRefCPEntry.getFunctionInfo();

                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) constPool[cpIndex];
                    invokeCallableUnit(functionInfo, funcCallCPEntry);
                    break;
                case InstructionCodes.TRBGN:
                    beginTransaction();
                    break;
                case InstructionCodes.TREND:
                    endTransaction(operands);
                    break;
                case InstructionCodes.WRKINVOKE:
                    cpIndex = operands[0];
                    workerRefCPEntry = (WorkerDataChannelRefCPEntry) constPool[cpIndex];
                    workerDataChannel = workerRefCPEntry.getWorkerDataChannel();
                    BType[] types = workerRefCPEntry.getTypes();

                    cpIndex = operands[1];
                    workerInvokeCPEntry = (WorkerInvokeCPEntry) constPool[cpIndex];
                    invokeWorker(workerDataChannel, workerInvokeCPEntry, types);
                    break;
                case InstructionCodes.WRKREPLY:
                    cpIndex = operands[0];
                    workerRefCPEntry = (WorkerDataChannelRefCPEntry) constPool[cpIndex];
                    workerDataChannel = workerRefCPEntry.getWorkerDataChannel();
                    types = workerRefCPEntry.getTypes();

                    cpIndex = operands[1];
                    workerReplyCPEntry = (WorkerReplyCPEntry) constPool[cpIndex];
                    replyWorker(workerDataChannel, workerReplyCPEntry, types);
                    break;
                case InstructionCodes.FORKJOIN:
                    cpIndex = operands[0];
                    forkJoinCPEntry = (ForkJoinCPEntry) constPool[cpIndex];
                    invokeForkJoin(forkJoinCPEntry);
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
                        if (error == null) {
                            handleNullRefError();
                            break;
                        }

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

                case InstructionCodes.I2ANY:
                case InstructionCodes.F2ANY:
                case InstructionCodes.S2ANY:
                case InstructionCodes.B2ANY:
                case InstructionCodes.L2ANY:
                case InstructionCodes.ANY2I:
                case InstructionCodes.ANY2F:
                case InstructionCodes.ANY2S:
                case InstructionCodes.ANY2B:
                case InstructionCodes.ANY2L:
                case InstructionCodes.ANY2JSON:
                case InstructionCodes.ANY2XML:
                case InstructionCodes.ANY2MAP:
                case InstructionCodes.ANY2MSG:
                case InstructionCodes.ANY2T:
                case InstructionCodes.ANY2C:
                case InstructionCodes.NULL2JSON:
                case InstructionCodes.CHECKCAST:
                    execTypeCastOpcodes(sf, opcode, operands);
                    break;

                case InstructionCodes.I2F:
                case InstructionCodes.I2S:
                case InstructionCodes.I2B:
                case InstructionCodes.I2JSON:
                case InstructionCodes.F2I:
                case InstructionCodes.F2S:
                case InstructionCodes.F2B:
                case InstructionCodes.F2JSON:
                case InstructionCodes.S2I:
                case InstructionCodes.S2F:
                case InstructionCodes.S2B:
                case InstructionCodes.S2JSON:
                case InstructionCodes.B2I:
                case InstructionCodes.B2F:
                case InstructionCodes.B2S:
                case InstructionCodes.B2JSON:
                case InstructionCodes.JSON2I:
                case InstructionCodes.JSON2F:
                case InstructionCodes.JSON2S:
                case InstructionCodes.JSON2B:
                case InstructionCodes.DT2XML:
                case InstructionCodes.DT2JSON:
                    execTypeConversionOpcodes(sf, opcode, operands);
                    break;

                case InstructionCodes.INEWARRAY:
                    i = operands[0];
                    sf.refRegs[i] = new BIntArray();
                    break;
                case InstructionCodes.ARRAYLEN:
                    i = operands[0];
                    j = operands[1];

                    BNewArray array = (BNewArray) sf.refRegs[i];
                    if (array == null) {
                        handleNullRefError();
                        break;
                    }
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
                    createNewStruct(operands, sf);
                    break;
                case InstructionCodes.NEWCONNECTOR:
                    createNewConnector(operands, sf);
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
                    handleReply(operands, sf);
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

    private void execTypeCastOpcodes(StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int cpIndex; // Index of the constant pool

        BRefType bRefType;
        TypeCPEntry typeCPEntry;

        switch (opcode) {
            case InstructionCodes.I2ANY:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BInteger(sf.longRegs[i]);
                break;
            case InstructionCodes.F2ANY:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BFloat(sf.doubleRegs[i]);
                break;
            case InstructionCodes.S2ANY:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BString(sf.stringRegs[i]);
                break;
            case InstructionCodes.B2ANY:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BBoolean(sf.intRegs[i] == 1);
                break;
            case InstructionCodes.L2ANY:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BBlob(sf.byteRegs[i]);
                break;
            case InstructionCodes.ANY2I:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleTypeCastError(sf, k, BTypes.typeNull, BTypes.typeInt);
                } else if (bRefType.getType() == BTypes.typeInt) {
                    sf.longRegs[j] = ((BInteger) bRefType).intValue();
                } else {
                    handleTypeCastError(sf, k, bRefType.getType(), BTypes.typeInt);
                }
                break;
            case InstructionCodes.ANY2F:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleTypeCastError(sf, k, BTypes.typeNull, BTypes.typeFloat);
                } else if (bRefType.getType() == BTypes.typeFloat) {
                    sf.doubleRegs[j] = ((BFloat) bRefType).floatValue();
                } else {
                    handleTypeCastError(sf, k, bRefType.getType(), BTypes.typeFloat);
                }
                break;
            case InstructionCodes.ANY2S:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleTypeCastError(sf, k, BTypes.typeNull, BTypes.typeString);
                } else if (bRefType.getType() == BTypes.typeString) {
                    sf.stringRegs[j] = bRefType.stringValue();
                } else {
                    handleTypeCastError(sf, k, bRefType.getType(), BTypes.typeString);
                }
                break;
            case InstructionCodes.ANY2B:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleTypeCastError(sf, k, BTypes.typeNull, BTypes.typeBoolean);
                } else if (bRefType.getType() == BTypes.typeBoolean) {
                    sf.intRegs[j] = ((BBoolean) bRefType).booleanValue() ? 1 : 0;
                } else {
                    handleTypeCastError(sf, k, bRefType.getType(), BTypes.typeBoolean);
                }
                break;
            case InstructionCodes.ANY2L:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleTypeCastError(sf, k, BTypes.typeNull, BTypes.typeBlob);
                } else if (bRefType.getType() == BTypes.typeBlob) {
                    sf.byteRegs[j] = ((BBlob) bRefType).blobValue();
                } else {
                    handleTypeCastError(sf, k, bRefType.getType(), BTypes.typeBlob);
                }
                break;
            case InstructionCodes.ANY2JSON:
                handleAnyToRefTypeCast(sf, operands, BTypes.typeJSON);
                break;
            case InstructionCodes.ANY2XML:
                handleAnyToRefTypeCast(sf, operands, BTypes.typeXML);
                break;
            case InstructionCodes.ANY2MAP:
                handleAnyToRefTypeCast(sf, operands, BTypes.typeMap);
                break;
            case InstructionCodes.ANY2MSG:
                handleAnyToRefTypeCast(sf, operands, BTypes.typeMessage);
                break;
            case InstructionCodes.ANY2DT:
                handleAnyToRefTypeCast(sf, operands, BTypes.typeDatatable);
                break;
            case InstructionCodes.ANY2T:
            case InstructionCodes.ANY2C:
            case InstructionCodes.CHECKCAST:
                i = operands[0];
                cpIndex = operands[1];
                j = operands[2];
                k = operands[3];
                typeCPEntry = (TypeCPEntry) constPool[cpIndex];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    sf.refRegs[j] = null;
                } else if (checkCast(bRefType.getType(), typeCPEntry.getType())) {
                    sf.refRegs[j] = sf.refRegs[i];
                } else {
                    handleTypeCastError(sf, k, bRefType.getType(), typeCPEntry.getType());
                }
                break;
            case InstructionCodes.NULL2JSON:
                j = operands[1];
                sf.refRegs[j] = new BJSON("null");
                break;
            default:
                throw new UnsupportedOperationException("Opcode " + opcode + " is not supported yet");
        }
    }

    private void execTypeConversionOpcodes(StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        BRefType bRefType;

        switch (opcode) {
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
                    handleTypeConversionError(sf, k, TypeConstants.STRING_TNAME, TypeConstants.INT_TNAME);
                }
                break;
            case InstructionCodes.S2F:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    sf.doubleRegs[j] = Double.parseDouble(sf.stringRegs[i]);
                } catch (NumberFormatException e) {
                    handleTypeConversionError(sf, k, TypeConstants.STRING_TNAME, TypeConstants.FLOAT_TNAME);
                }
                break;
            case InstructionCodes.S2B:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    sf.intRegs[j] = Boolean.parseBoolean(sf.stringRegs[i]) ? 1 : 0;
                } catch (NumberFormatException e) {
                    handleTypeConversionError(sf, k, TypeConstants.STRING_TNAME, TypeConstants.BOOLEAN_TNAME);
                }
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
            case InstructionCodes.B2JSON:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BJSON(sf.intRegs[i] == 1 ? "true" : "false");
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
            case InstructionCodes.DT2XML:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleNullRefError();
                    break;
                }

                sf.refRegs[j] = XMLUtils.datatableToXML((BDataTable) bRefType, context.isInTransaction());
                break;
            case InstructionCodes.DT2JSON:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleNullRefError();
                    break;
                }

                sf.refRegs[j] = JSONUtils.toJSON((BDataTable) bRefType, context.isInTransaction());
                break;
            default:
                throw new UnsupportedOperationException("Opcode " + opcode + " is not supported yet");
        }
    }

    private void handleAnyToRefTypeCast(StackFrame sf, int[] operands, BType targetType) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BRefType bRefType = sf.refRegs[i];
        if (bRefType == null) {
            sf.refRegs[j] = null;
        } else if (bRefType.getType() == targetType) {
            sf.refRegs[j] = bRefType;
        } else {
            handleTypeCastError(sf, k, bRefType.getType(), targetType);
        }
    }

    private void handleTypeCastError(StackFrame sf, int errorRegIndex, BType sourceType, BType targetType) {
        BStruct errorVal;
        errorVal = BLangVMErrors.createTypeCastError(context, ip, sourceType, targetType);
        if (errorRegIndex == -1) {
            context.setError(errorVal);
            handleError();
            return;
        }

        sf.refRegs[errorRegIndex] = errorVal;
    }

    private void handleTypeConversionError(StackFrame sf, int errorRegIndex,
                                           String sourceTypeName, String targetTypeName) {
        BStruct errorVal;
        errorVal = BLangVMErrors.createTypeConversionError(context, ip, sourceTypeName, targetTypeName);
        if (errorRegIndex == -1) {
            context.setError(errorVal);
            handleError();
            return;
        }

        sf.refRegs[errorRegIndex] = errorVal;
    }

    private void handleReply(int[] operands, StackFrame sf) {
        int i;
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
    }

    private void createNewConnector(int[] operands, StackFrame sf) {
        int cpIndex;
        int i;
        StructureRefCPEntry structureRefCPEntry;
        StructureTypeInfo structureTypeInfo;
        int[] fieldCount;
        cpIndex = operands[0];
        i = operands[1];
        structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
        structureTypeInfo = structureRefCPEntry.getStructureTypeInfo();
        fieldCount = structureTypeInfo.getFieldCount();
        BConnector bConnector = new BConnector(structureTypeInfo.getType());
        bConnector.setFieldTypes(structureTypeInfo.getFieldTypes());
        bConnector.init(fieldCount);
        sf.refRegs[i] = bConnector;
    }

    private void createNewStruct(int[] operands, StackFrame sf) {
        int cpIndex;
        int i;
        StructureRefCPEntry structureRefCPEntry;
        StructureTypeInfo structureTypeInfo;
        int[] fieldCount;
        cpIndex = operands[0];
        i = operands[1];
        structureRefCPEntry = (StructureRefCPEntry) constPool[cpIndex];
        structureTypeInfo = structureRefCPEntry.getStructureTypeInfo();
        fieldCount = structureTypeInfo.getFieldCount();
        BStruct bStruct = new BStruct(structureTypeInfo.getType());
        bStruct.setFieldTypes(structureTypeInfo.getFieldTypes());
        bStruct.init(fieldCount);
        sf.refRegs[i] = bStruct;
    }

    private void endTransaction(int[] operands) {
        int i;
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

    private void beginTransaction() {
        BallerinaTransactionManager ballerinaTransactionManager = context.getBallerinaTransactionManager();
        if (ballerinaTransactionManager == null) {
            ballerinaTransactionManager = new BallerinaTransactionManager();
            context.setBallerinaTransactionManager(ballerinaTransactionManager);
        }
        ballerinaTransactionManager.beginTransactionBlock();
    }

    public void invokeCallableUnit(CallableUnitInfo callableUnitInfo, FunctionCallCPEntry funcCallCPEntry) {
        currentCallableUnitInfo = callableUnitInfo;
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

    public void invokeWorker(WorkerDataChannel workerDataChannel, WorkerInvokeCPEntry workerInvokeCPEntry,
                             BType[] types) {
        StackFrame currentFrame = controlStack.getCurrentFrame();

        // Extract the outgoing expressions
        BValue[] arguments = new BValue[workerInvokeCPEntry.getbTypes().length];
        copyArgValuesForWorkerInvoke(currentFrame, workerInvokeCPEntry.getArgRegs(),
                types, arguments);

        //populateArgumentValuesForWorker(expressions, arguments);
        if (workerDataChannel != null) {
            workerDataChannel.putData(arguments);
            workerDataChannel.setTypes(types);
        } else {
            BArray<BValue> bArray = new BArray<>(BValue.class);
            for (int j = 0; j < arguments.length; j++) {
                BValue returnVal = arguments[j];
                bArray.add(j, returnVal);
            }
            controlStack.getCurrentFrame().returnValues[0] = bArray;
        }
    }

    public void invokeForkJoin(ForkJoinCPEntry forkJoinCPEntry) {
        ForkJoinStmt forkJoinStmt = forkJoinCPEntry.getForkJoinStmt();
        List<BLangVMWorkers.WorkerExecutor> workerRunnerList = new ArrayList<>();
        List<WorkerResult> resultMsgs = new ArrayList<>();
        //Map<String, BRefValueArray> resultInvokeAll = new HashMap<>();
        //BRefValueArray resultInvokeAny = null;
        long timeout = 60; // Default timeout value is 60 seconds
        if (forkJoinCPEntry.isTimeoutAvailable()) {
            timeout = controlStack.getCurrentFrame().getLongRegs()[0];
        }

        Worker[] workers = forkJoinStmt.getWorkers();
        Map<String, BLangVMWorkers.WorkerExecutor> triggeredWorkers = new HashMap<>();
        for (Worker worker : workers) {
            Context workerContext = new Context();
            WorkerCallback workerCallback = new WorkerCallback(workerContext);
            workerContext.setBalCallback(workerCallback);

            StackFrame callerSF = controlStack.getCurrentFrame();
            int[] argRegs = forkJoinCPEntry.getArgRegs();

            ControlStackNew controlStack = workerContext.getControlStackNew();
            StackFrame calleeSF = new StackFrame(currentCallableUnitInfo,
                    forkJoinCPEntry.getWorkerInfo(worker.getName()), -1, new int[1]);
            controlStack.pushFrame(calleeSF);

            BLangVM.copyValuesForForkJoin(callerSF, calleeSF, argRegs);


            // Copy arg values from the current StackFrame to the new StackFrame
            // TODO fix this. Move the copyArgValues method to another util function
            // BLangVM.copyArgValues(callerSF, calleeSF, argRegs, paramTypes);

            BLangVM bLangVM = new BLangVM(programFile);
            //ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
            BLangVMWorkers.WorkerExecutor workerRunner = new BLangVMWorkers.WorkerExecutor(bLangVM,
                    workerContext, forkJoinCPEntry.getWorkerInfo(worker.getName()));
            workerRunnerList.add(workerRunner);
            triggeredWorkers.put(worker.getName(), workerRunner);
        }

        if (forkJoinStmt.getJoin().getJoinType().equalsIgnoreCase("any")) {
            String[] joinWorkerNames = forkJoinStmt.getJoin().getJoinWorkers();
            if (joinWorkerNames.length == 0) {
                // If there are no workers specified, wait for any of all the workers
                resultMsgs.add(invokeAnyWorker(workerRunnerList, timeout));
                //resultMsgs.add(res);
            } else {
                List<BLangVMWorkers.WorkerExecutor> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                resultMsgs.add(invokeAnyWorker(workerRunnersSpecified, timeout));
                //resultMsgs.add(res);
            }
        } else {
            String[] joinWorkerNames = forkJoinStmt.getJoin().getJoinWorkers();
            if (joinWorkerNames.length == 0) {
                // If there are no workers specified, wait for all of all the workers
                resultMsgs.addAll(invokeAllWorkers(workerRunnerList, timeout));
            } else {
                List<BLangVMWorkers.WorkerExecutor> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                resultMsgs.addAll(invokeAllWorkers(workerRunnersSpecified, timeout));
            }
        }

        if (isForkJoinTimedOut) {
            ip = forkJoinStmt.getTimeout().getIp();
            // Execute the timeout block

            int offsetTimeout = ((StackVarLocation) forkJoinStmt.getTimeout().getTimeoutResult().getMemoryLocation()).
                    getStackFrameOffset();
            BMap<String, BRefValueArray> mbMap = new BMap<>();
            for (WorkerResult workerResult : resultMsgs) {
                mbMap.put(workerResult.getWorkerName(), workerResult.getResult());
            }
            controlStack.getCurrentFrame().getRefLocalVars()[offsetTimeout] = mbMap;

            isForkJoinTimedOut = false;

        } else {
            ip = forkJoinStmt.getJoin().getIp();
            // Assign values to join block message arrays
            int offsetJoin = ((StackVarLocation) forkJoinStmt.getJoin().getJoinResult().getMemoryLocation()).
                    getStackFrameOffset();
            BMap<String, BRefValueArray> mbMap = new BMap<>();
            for (WorkerResult workerResult : resultMsgs) {
                mbMap.put(workerResult.getWorkerName(), workerResult.getResult());
            }
            controlStack.getCurrentFrame().getRefLocalVars()[offsetJoin] = mbMap;
        }
    }

    private WorkerResult invokeAnyWorker(List<BLangVMWorkers.WorkerExecutor> workerRunnerList, long timeout) {
        ExecutorService anyExecutor = Executors.newWorkStealingPool();
        WorkerResult result;
        try {
            result = anyExecutor.invokeAny(workerRunnerList, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } catch (TimeoutException e) {
            isForkJoinTimedOut = true;
            return null;
        }
        return result;
    }

    private List<WorkerResult> invokeAllWorkers(List<BLangVMWorkers.WorkerExecutor> workerRunnerList,
                                                long timeout) {
        ExecutorService allExecutor = Executors.newWorkStealingPool();
        List<WorkerResult> result = new ArrayList<>();
        try {
            allExecutor.invokeAll(workerRunnerList, timeout, TimeUnit.SECONDS).stream().map(bMessageFuture -> {
                try {
                    return bMessageFuture.get();
                } catch (CancellationException e) {
                    // This means task has been timedout and cancelled by system.
                    isForkJoinTimedOut = true;
                    return null;
                } catch (Exception e) {
                    return null;
                }

            }).forEach((WorkerResult b) -> {
                result.add(b);
            });
        } catch (InterruptedException e) {
            return result;
        }
        return result;
    }

    public void replyWorker(WorkerDataChannel workerDataChannel, WorkerReplyCPEntry workerReplyCPEntry, BType[] types) {

        BValue[] passedInValues = (BValue[]) workerDataChannel.takeData();
        StackFrame currentFrame = controlStack.getCurrentFrame();
        copyArgValuesForWorkerReply(currentFrame, workerReplyCPEntry.getArgRegs(),
                types, passedInValues);
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

    public static void copyValuesForForkJoin(StackFrame callerSF, StackFrame calleeSF, int[] argRegs) {
        int longLocalVals = argRegs[0];
        int doubleLocalVals = argRegs[1];
        int stringLocalVals = argRegs[2];
        int booleanLocalVals = argRegs[3];
        int blobLocalVals = argRegs[4];
        int refLocalVals = argRegs[5];

        for (int i = 0; i <= longLocalVals; i++) {
            calleeSF.getLongLocalVars()[i] = callerSF.getLongLocalVars()[i];
        }

        for (int i = 0; i <= doubleLocalVals; i++) {
            calleeSF.getDoubleLocalVars()[i] = callerSF.getDoubleLocalVars()[i];
        }

        for (int i = 0; i <= stringLocalVals; i++) {
            calleeSF.getStringLocalVars()[i] = callerSF.getStringLocalVars()[i];
        }

        for (int i = 0; i <= booleanLocalVals; i++) {
            calleeSF.getIntLocalVars()[i] = callerSF.getIntLocalVars()[i];
        }

        for (int i = 0; i <= refLocalVals; i++) {
            calleeSF.getRefLocalVars()[i] = callerSF.getRefLocalVars()[i];
        }

        for (int i = 0; i <= blobLocalVals; i++) {
            calleeSF.getByteLocalVars()[i] = callerSF.getByteLocalVars()[i];
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

        // TODO : Remove once we handle this properly for return values
        BType[] retTypes = functionInfo.getRetParamTypes();
        BValue[] returnValues = new BValue[retTypes.length];

        StackFrame caleeSF = new StackFrame(functionInfo, functionInfo.getDefaultWorkerInfo(), 0, null, returnValues);
        copyArgValues(callerSF, caleeSF, funcCallCPEntry.getArgRegs(),
                functionInfo.getParamTypes());

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
    }

    private void invokeNativeAction(ActionInfo actionInfo, FunctionCallCPEntry funcCallCPEntry) {
        StackFrame callerSF = controlStack.currentFrame;

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
                    if (returnValues[i] == null) {
                        // TODO Null reference error properly
                        throw new BallerinaException("returned null reference");
                    }
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

    private void convertJSONToInt(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError();
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeInt, e.getMessage());
            context.setError(BLangVMErrors.createError(context, ip, errorMsg));
            handleError();
            return;
        }

        if (jsonNode.isInt() || jsonNode.isLong()) {
            sf.longRegs[j] = jsonNode.longValue();
            return;
        }

        handleTypeConversionError(sf, k, JSONUtils.getTypeName(jsonNode), TypeConstants.INT_TNAME);
    }

    private void convertJSONToFloat(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError();
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeFloat, e.getMessage());
            context.setError(BLangVMErrors.createError(context, ip, errorMsg));
            handleError();
            return;
        }

        if (jsonNode.isFloat() || jsonNode.isDouble()) {
            sf.doubleRegs[j] = jsonNode.doubleValue();
            return;
        }

        handleTypeConversionError(sf, k, JSONUtils.getTypeName(jsonNode), TypeConstants.FLOAT_TNAME);
    }

    private void convertJSONToString(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError();
            return;
        }

        try {
            sf.stringRegs[j] = jsonValue.stringValue();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeString, e.getMessage());
            context.setError(BLangVMErrors.createError(context, ip, errorMsg));
            handleError();
        }
    }

    private void convertJSONToBoolean(int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError();
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeBoolean, e.getMessage());
            context.setError(BLangVMErrors.createError(context, ip, errorMsg));
            handleError();
            return;
        }

        if (jsonNode.isBoolean()) {
            sf.intRegs[j] = jsonNode.booleanValue() ? 1 : 0;
            return;
        }

        handleTypeConversionError(sf, k, JSONUtils.getTypeName(jsonNode), TypeConstants.BOOLEAN_TNAME);
    }

    private void handleNullRefError() {
        context.setError(BLangVMErrors.createNullRefError(context, ip));
        handleError();
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
            if (context.getServiceInfo() != null) {
                // Invoke ServiceConnector error handler.
                Object protocol = context.getCarbonMessage().getProperty("PROTOCOL");
                Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                        BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);
                try {
                    optionalErrorHandler
                            .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                            .handleError(new BallerinaException(
                                            BLangVMErrors.getErrorMessage(context.getError())),
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
