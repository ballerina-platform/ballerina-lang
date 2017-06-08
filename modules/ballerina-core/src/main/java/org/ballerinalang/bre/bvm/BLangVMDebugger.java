package org.ballerinalang.bre.bvm;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.nonblocking.debugger.BreakPointInfo;
import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JSONUtils;
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
import org.ballerinalang.model.values.StructureType;
import org.ballerinalang.runtime.DefaultBalCallback;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
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
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.VMDebugManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Semaphore;

/**
 * Created by rajith on 6/5/17.
 */
public class BLangVMDebugger extends BLangVM implements Runnable {
    private DebugSessionObserver debugSessionObserver;
    private volatile Semaphore executionSem;
    private boolean mainThread = false;

    public BLangVMDebugger(ProgramFile programFile, Context bContext) {
        super(programFile);
        this.executionSem = new Semaphore(0);
        this.context = bContext;
    }

    public void setMainThread(boolean mainThread) {
        this.mainThread = mainThread;
    }

    public void run() {
        if (mainThread) {
            try {
                executionSem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (debugSessionObserver != null) {
            context.getDebugInfoHolder().setDebugSessionObserver(debugSessionObserver);
        }

        StackFrame currentFrame = context.getControlStackNew().getCurrentFrame();
        this.constPool = currentFrame.packageInfo.getConstPool();
        this.code = currentFrame.packageInfo.getInstructions();

        this.context = context;
        this.controlStack = context.getControlStackNew();
        this.context.setVMBasedExecutor(true);
        this.ip = context.getStartIP();

//        CallableUnitInfo callee = this.controlStack.getRootFrame().callableUnitInfo;

        traceCode(currentFrame.packageInfo);

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

        VMDebugManager.getInstance().setDone(true);
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
                    sf.refRegs[i] = new BDataTable(null, new HashMap<>(0), new ArrayList<>(0));
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
            debugging(ip);
        }
    }

    public void debugging(int cp) {
        if (cp < 0) {
            return;
        }
        String lineNum;
        NodeLocation location;
        LineNumberInfo currentExecLine = controlStack.currentFrame.packageInfo.getLineNumberInfo(cp);

        DebugInfoHolder holder = context.getDebugInfoHolder();
        if (code[cp].getOpcode() == InstructionCodes.CALL && holder.getCurrentCommand() != DebugInfoHolder
                .DebugCommand.STEP_OUT) {
            holder.pushFunctionCallNextIp(cp + 1);
        } else if (code[cp].getOpcode() == InstructionCodes.RET && holder.getCurrentCommand() != DebugInfoHolder
                .DebugCommand.STEP_OUT) {
            holder.popFunctionCallNextIp();
        }
        switch (holder.getCurrentCommand()) {
            case RESUME:
                if (!holder.getCurrentLineStack().isEmpty() && currentExecLine
                        .equals(holder.getCurrentLineStack().peek())) {
                    return;
                }
                lineNum = currentExecLine.getFileName() + ":" + currentExecLine.getLineNumber();
                location = holder.getDebugPoint(lineNum);
                if (location == null) {
                    return;
                }
                if (!holder.getCurrentLineStack().isEmpty()) {
                    holder.getCurrentLineStack().pop();
                    if (!holder.getCurrentLineStack().isEmpty() && currentExecLine
                            .equals(holder.getCurrentLineStack().peek())) {
                        return;
                    }
                }
                holder.setPreviousIp(cp);
                holder.getCurrentLineStack().push(currentExecLine);
                holder.getDebugSessionObserver().notifyHalt(getBreakPointInfo(currentExecLine));
                aquireLock();
                break;
            case STEP_IN:
                if (!holder.getCurrentLineStack().isEmpty() && currentExecLine
                        .equals(holder.getCurrentLineStack().peek())) {
                    return;
                }

                holder.setPreviousIp(cp);
                holder.getCurrentLineStack().push(currentExecLine);
                holder.getDebugSessionObserver().notifyHalt(getBreakPointInfo(currentExecLine));
                aquireLock();
                break;
            case STEP_OVER:
                if (code[holder.getPreviousIp()].getOpcode() == InstructionCodes.CALL) {
                    holder.setNextIp(holder.getPreviousIp() + 1);
                    if (cp == holder.getNextIp()) {
                        holder.setNextIp(-1);
                        holder.setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_IN);
                        return;
                    }
                    holder.setCurrentCommand(DebugInfoHolder.DebugCommand.NEXT_LINE);
                } else {
                    holder.setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_IN);
                }
                break;
            case STEP_OUT:
                if (code[holder.getPreviousIp()].getOpcode() == InstructionCodes.CALL) {
                    holder.setPreviousIp(cp);
                    holder.popFunctionCallNextIp();
                }
                if (holder.peekFunctionCallNextIp() != cp) {
                    return;
                }
                holder.popFunctionCallNextIp();
                holder.setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_IN);
                break;
            case NEXT_LINE:
                if (cp == holder.getNextIp()) {
                    holder.setNextIp(-1);
                    holder.setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_IN);
                    return;
                }
                break;
        }

    }

    private void aquireLock() {
        try {
            executionSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace(); //todo fixme
        }
    }

    public BreakPointInfo getBreakPointInfo(LineNumberInfo current) {

        NodeLocation location = new NodeLocation(current.getPackageInfo().getPkgPath(),
                current.getFileName(), current.getLineNumber());
        BreakPointInfo breakPointInfo = new BreakPointInfo(location);
        for (StackFrame stackFrame : context.getControlStackNew().getStack()) {
//            String pck = (stackFrame.packageInfo.getPkgPath() == null ? "default" : stackFrame
//                    .packageInfo.getPkgPath());
//            String functionName = stackFrame.callableUnitInfo.getName();
//            NodeLocation location = stackFrame.getNodeInfo().getNodeLocation();
//            FrameInfo frameInfo = new FrameInfo(pck, functionName, location.getFileName(), location.getLineNumber());
//            HashMap<SymbolName, AbstractMap.SimpleEntry<Integer, String>> variables = stackFrame.variables;
//            if (null != variables) {
//                for (SymbolName name : variables.keySet()) {
//                    AbstractMap.SimpleEntry<Integer, String> offSet = variables.get(name);
//                    BValue value = null;
//                    switch (offSet.getValue()) {
//                        case "Arg":
//                        case "Local":
//                            value = stackFrame.values[offSet.getKey()];
//                            break;
//                        case "Service":
//                        case "Const":
//                            value = runtimeEnvironment.getStaticMemory().getValue(offSet.getKey());
//                            break;
//                        case "Connector":
//                            BConnector bConnector = (BConnector) stackFrame.values[0];
//                            if (bConnector != null) {
//                                bConnector.getValue(offSet.getKey());
//                            }
//                            break;
//                        default:
//                            value = null;
//                    }
//                    VariableInfo variableInfo = new VariableInfo(name.getName(), offSet.getValue());
//                    variableInfo.setBValue(value);
//                    frameInfo.addVariableInfo(variableInfo);
//                }
//            }
//            breakPointInfo.addFrameInfo(frameInfo);
        }
        return breakPointInfo;
    }

    public void setDebugSessionObserver(DebugSessionObserver debugSessionObserver) {
        this.debugSessionObserver = debugSessionObserver;
        if (context != null) {
            this.context.getDebugInfoHolder().setDebugSessionObserver(debugSessionObserver);
        }
    }

    public void addDebugPoints(List<NodeLocation> breakPoints) {
        if (context != null) {
            this.context.getDebugInfoHolder().addDebugPoints(breakPoints);
        }
    }

    public void resume() {
        context.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.RESUME);
        executionSem.release();
    }

    public void stepIn() {
        context.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_IN);
        executionSem.release();
    }

    public void stepOver() {
        context.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_OVER);
        executionSem.release();
    }

    public void stepOut() {
        context.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.STEP_OUT);
        executionSem.release();
    }

    public void clearDebugPoints() {
        context.getDebugInfoHolder().clearDebugLocations();
    }

    public void addDebugPoint(NodeLocation nodeLocation) {
        context.getDebugInfoHolder().addDebugPoint(nodeLocation);
    }
}
