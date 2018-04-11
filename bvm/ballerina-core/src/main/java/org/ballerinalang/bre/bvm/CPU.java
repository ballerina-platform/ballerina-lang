/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.Flags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBlobArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BCollection;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BFuture;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BIntRange;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.model.values.BXMLQName;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.model.values.StructureType;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.TransactionStatus;
import org.ballerinalang.util.codegen.AttachedFunctionInfo;
import org.ballerinalang.util.codegen.ErrorTableEntry;
import org.ballerinalang.util.codegen.ForkjoinInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.Instruction.InstructionCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionFORKJOIN;
import org.ballerinalang.util.codegen.Instruction.InstructionIteratorNext;
import org.ballerinalang.util.codegen.Instruction.InstructionLock;
import org.ballerinalang.util.codegen.Instruction.InstructionTCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionVCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionWRKSendReceive;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.StructFieldInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.codegen.WorkerDataChannelInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;
import org.ballerinalang.util.transactions.TransactionConstants;
import org.ballerinalang.util.transactions.TransactionResourceManager;
import org.ballerinalang.util.transactions.TransactionUtils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import static org.ballerinalang.util.BLangConstants.STRING_NULL_VALUE;

/**
 * This class executes Ballerina instruction codes by acting as a CPU.
 *
 * @since 0.965.0
 */
public class CPU {

    public static void traceCode(Instruction[] code) {
        PrintStream printStream = System.out;
        for (int i = 0; i < code.length; i++) {
            printStream.println(i + ": " + code[i].toString());
        }
    }

    private static WorkerExecutionContext handleHalt(WorkerExecutionContext ctx) {
        BLangScheduler.workerDone(ctx);
        return ctx.respCtx.signal(new WorkerSignal(ctx, SignalType.HALT, null));
    }

    public static void exec(WorkerExecutionContext ctx) {
        while (ctx != null && !ctx.isRootContext()) {
            try {
                tryExec(ctx);
                break;
            } catch (HandleErrorException e) {
                ctx = e.ctx;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void tryExec(WorkerExecutionContext ctx) {
        BLangScheduler.workerRunning(ctx);

        int i;
        int j;
        int cpIndex;
        FunctionCallCPEntry funcCallCPEntry;
        FunctionRefCPEntry funcRefCPEntry;
        TypeRefCPEntry typeRefCPEntry;
        FunctionInfo functionInfo;
        InstructionCALL callIns;

        boolean debugEnabled = ctx.programFile.getDebugger().isDebugEnabled();

        WorkerData currentSF, callersSF;
        int callersRetRegIndex;

        while (ctx.ip >= 0) {
            try {
                if (ctx.stop) {
                    BLangScheduler.workerDone(ctx);
                    return;
                }
                if (debugEnabled && debug(ctx)) {
                    return;
                }
    
                Instruction instruction = ctx.code[ctx.ip];
                int opcode = instruction.getOpcode();
                int[] operands = instruction.getOperands();
                ctx.ip++;
                WorkerData sf = ctx.workerLocal;
                switch (opcode) {
                    case InstructionCodes.ICONST:
                        cpIndex = operands[0];
                        i = operands[1];
                        sf.longRegs[i] = ((IntegerCPEntry) ctx.constPool[cpIndex]).getValue();
                        break;
                    case InstructionCodes.FCONST:
                        cpIndex = operands[0];
                        i = operands[1];
                        sf.doubleRegs[i] = ((FloatCPEntry) ctx.constPool[cpIndex]).getValue();
                        break;
                    case InstructionCodes.SCONST:
                        cpIndex = operands[0];
                        i = operands[1];
                        sf.stringRegs[i] = ((StringCPEntry) ctx.constPool[cpIndex]).getValue();
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
    
                    case InstructionCodes.IMOVE:
                    case InstructionCodes.FMOVE:
                    case InstructionCodes.SMOVE:
                    case InstructionCodes.BMOVE:
                    case InstructionCodes.LMOVE:
                    case InstructionCodes.RMOVE:
                    case InstructionCodes.IALOAD:
                    case InstructionCodes.FALOAD:
                    case InstructionCodes.SALOAD:
                    case InstructionCodes.BALOAD:
                    case InstructionCodes.LALOAD:
                    case InstructionCodes.RALOAD:
                    case InstructionCodes.JSONALOAD:
                    case InstructionCodes.IGLOAD:
                    case InstructionCodes.FGLOAD:
                    case InstructionCodes.SGLOAD:
                    case InstructionCodes.BGLOAD:
                    case InstructionCodes.LGLOAD:
                    case InstructionCodes.RGLOAD:
                    case InstructionCodes.IFIELDLOAD:
                    case InstructionCodes.FFIELDLOAD:
                    case InstructionCodes.SFIELDLOAD:
                    case InstructionCodes.BFIELDLOAD:
                    case InstructionCodes.LFIELDLOAD:
                    case InstructionCodes.RFIELDLOAD:
                    case InstructionCodes.MAPLOAD:
                    case InstructionCodes.JSONLOAD:
                        execLoadOpcodes(ctx, sf, opcode, operands);
                        break;
    
                    case InstructionCodes.IASTORE:
                    case InstructionCodes.FASTORE:
                    case InstructionCodes.SASTORE:
                    case InstructionCodes.BASTORE:
                    case InstructionCodes.LASTORE:
                    case InstructionCodes.RASTORE:
                    case InstructionCodes.JSONASTORE:
                    case InstructionCodes.IGSTORE:
                    case InstructionCodes.FGSTORE:
                    case InstructionCodes.SGSTORE:
                    case InstructionCodes.BGSTORE:
                    case InstructionCodes.LGSTORE:
                    case InstructionCodes.RGSTORE:
                    case InstructionCodes.IFIELDSTORE:
                    case InstructionCodes.FFIELDSTORE:
                    case InstructionCodes.SFIELDSTORE:
                    case InstructionCodes.BFIELDSTORE:
                    case InstructionCodes.LFIELDSTORE:
                    case InstructionCodes.RFIELDSTORE:
                    case InstructionCodes.MAPSTORE:
                    case InstructionCodes.JSONSTORE:
                        execStoreOpcodes(ctx, sf, opcode, operands);
                        break;
    
                    case InstructionCodes.IADD:
                    case InstructionCodes.FADD:
                    case InstructionCodes.SADD:
                    case InstructionCodes.XMLADD:
                    case InstructionCodes.ISUB:
                    case InstructionCodes.FSUB:
                    case InstructionCodes.IMUL:
                    case InstructionCodes.FMUL:
                    case InstructionCodes.IDIV:
                    case InstructionCodes.FDIV:
                    case InstructionCodes.IMOD:
                    case InstructionCodes.FMOD:
                    case InstructionCodes.INEG:
                    case InstructionCodes.FNEG:
                    case InstructionCodes.BNOT:
                    case InstructionCodes.IEQ:
                    case InstructionCodes.FEQ:
                    case InstructionCodes.SEQ:
                    case InstructionCodes.BEQ:
                    case InstructionCodes.REQ:
                    case InstructionCodes.TEQ:
                    case InstructionCodes.INE:
                    case InstructionCodes.FNE:
                    case InstructionCodes.SNE:
                    case InstructionCodes.BNE:
                    case InstructionCodes.RNE:
                    case InstructionCodes.TNE:
                        execBinaryOpCodes(ctx, sf, opcode, operands);
                        break;
    
                    case InstructionCodes.LENGTHOF:
                        calculateLength(ctx, operands, sf);
                        break;
                    case InstructionCodes.TYPELOAD:
                        cpIndex = operands[0];
                        j = operands[1];
                        TypeRefCPEntry typeEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        sf.refRegs[j] = new BTypeDescValue(typeEntry.getType());
                        break;
                    case InstructionCodes.TYPEOF:
                        i = operands[0];
                        j = operands[1];
                        if (sf.refRegs[i] == null) {
                            sf.refRegs[j] = new BTypeDescValue(BTypes.typeNull);
                            break;
                        }
                        sf.refRegs[j] = new BTypeDescValue(sf.refRegs[i].getType());
                        break;
                    case InstructionCodes.HALT:
                        ctx = handleHalt(ctx);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.IGT:
                    case InstructionCodes.FGT:
                    case InstructionCodes.IGE:
                    case InstructionCodes.FGE:
                    case InstructionCodes.ILT:
                    case InstructionCodes.FLT:
                    case InstructionCodes.ILE:
                    case InstructionCodes.FLE:
                    case InstructionCodes.REQ_NULL:
                    case InstructionCodes.RNE_NULL:
                    case InstructionCodes.BR_TRUE:
                    case InstructionCodes.BR_FALSE:
                    case InstructionCodes.GOTO:
                    case InstructionCodes.SEQ_NULL:
                    case InstructionCodes.SNE_NULL:
                        execCmpAndBranchOpcodes(ctx, sf, opcode, operands);
                        break;
    
                    case InstructionCodes.TR_RETRY:
                        i = operands[0];
                        j = operands[1];
                        int l = operands[2];
                        retryTransaction(ctx, i, j, l);
                        break;
                    case InstructionCodes.CALL:
                        callIns = (InstructionCALL) instruction;
                        ctx = BLangFunctions.invokeCallable(callIns.functionInfo, ctx, callIns.argRegs,
                                callIns.retRegs, false, callIns.flags);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.VCALL:
                        InstructionVCALL vcallIns = (InstructionVCALL) instruction;
                        ctx = invokeVirtualFunction(ctx, vcallIns.receiverRegIndex, vcallIns.functionInfo,
                                vcallIns.argRegs, vcallIns.retRegs, vcallIns.flags);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.TCALL:
                        InstructionTCALL tcallIns = (InstructionTCALL) instruction;
                        ctx = BLangFunctions.invokeCallable(tcallIns.transformerInfo, ctx, tcallIns.argRegs,
                                tcallIns.retRegs, false, tcallIns.flags);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.TR_BEGIN:
                        i = operands[0];
                        j = operands[1];
                        int k = operands[2];
                        int h = operands[3];
                        beginTransaction(ctx, i, j, k, h);
                        break;
                    case InstructionCodes.TR_END:
                        i = operands[0];
                        j = operands[1];
                        endTransaction(ctx, i, j);
                        break;
                    case InstructionCodes.WRKSEND:
                        InstructionWRKSendReceive wrkSendIns = (InstructionWRKSendReceive) instruction;
                        handleWorkerSend(ctx, wrkSendIns.dataChannelInfo, wrkSendIns.type, wrkSendIns.reg);
                        break;
                    case InstructionCodes.WRKRECEIVE:
                        InstructionWRKSendReceive wrkReceiveIns = (InstructionWRKSendReceive) instruction;
                        if (!handleWorkerReceive(ctx, wrkReceiveIns.dataChannelInfo, wrkReceiveIns.type,
                                wrkReceiveIns.reg)) {
                            return;
                        }
                        break;
                    case InstructionCodes.FORKJOIN:
                        InstructionFORKJOIN forkJoinIns = (InstructionFORKJOIN) instruction;
                        ctx = invokeForkJoin(ctx, forkJoinIns);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.THROW:
                        i = operands[0];
                        if (i >= 0) {
                            BStruct error = (BStruct) sf.refRegs[i];
                            if (error == null) {
                                handleNullRefError(ctx);
                                break;
                            }
    
                            BLangVMErrors.attachStackFrame(error, ctx);
                            ctx.setError(error);
                        }
                        handleError(ctx);
                        break;
                    case InstructionCodes.ERRSTORE:
                        i = operands[0];
                        sf.refRegs[i] = ctx.getError();
                        // clear error
                        ctx.setError(null);
                        break;
                    case InstructionCodes.FPCALL:
                        i = operands[0];
                        if (sf.refRegs[i] == null) {
                            handleNullRefError(ctx);
                            break;
                        }
                        cpIndex = operands[1];
                        funcCallCPEntry = (FunctionCallCPEntry) ctx.constPool[cpIndex];
                        funcRefCPEntry = ((BFunctionPointer) sf.refRegs[i]).value();
                        functionInfo = funcRefCPEntry.getFunctionInfo();
                        ctx = invokeCallable(ctx, (BFunctionPointer) sf.refRegs[i], funcCallCPEntry, functionInfo, sf);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.FPLOAD:
                        i = operands[0];
                        j = operands[1];
                        funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[i];
                        BFunctionPointer functionPointer = new BFunctionPointer(funcRefCPEntry);
                        sf.refRegs[j] = functionPointer;
                        findAndAddClosureVarRegIndexes(ctx, operands, functionPointer);
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
                    case InstructionCodes.ARRAY2JSON:
                    case InstructionCodes.JSON2ARRAY:
                    case InstructionCodes.ANY2JSON:
                    case InstructionCodes.ANY2XML:
                    case InstructionCodes.ANY2MAP:
                    case InstructionCodes.ANY2TYPE:
                    case InstructionCodes.ANY2E:
                    case InstructionCodes.ANY2T:
                    case InstructionCodes.ANY2C:
                    case InstructionCodes.ANY2DT:
                    case InstructionCodes.NULL2JSON:
                    case InstructionCodes.CHECKCAST:
                    case InstructionCodes.B2JSON:
                    case InstructionCodes.JSON2I:
                    case InstructionCodes.JSON2F:
                    case InstructionCodes.JSON2S:
                    case InstructionCodes.JSON2B:
                    case InstructionCodes.NULL2S:
                    case InstructionCodes.IS_ASSIGNABLE:
                    case InstructionCodes.CHECK_CONVERSION:
                        execTypeCastOpcodes(ctx, sf, opcode, operands);
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
                    case InstructionCodes.DT2XML:
                    case InstructionCodes.DT2JSON:
                    case InstructionCodes.T2MAP:
                    case InstructionCodes.T2JSON:
                    case InstructionCodes.MAP2JSON:
                    case InstructionCodes.JSON2MAP:
                    case InstructionCodes.MAP2T:
                    case InstructionCodes.JSON2T:
                    case InstructionCodes.XMLATTRS2MAP:
                    case InstructionCodes.S2XML:
                    case InstructionCodes.S2JSONX:
                    case InstructionCodes.XML2S:
                    case InstructionCodes.ANY2SCONV:
                        execTypeConversionOpcodes(ctx, sf, opcode, operands);
                        break;
    
                    case InstructionCodes.INEWARRAY:
                        i = operands[0];
                        sf.refRegs[i] = new BIntArray();
                        break;
                    case InstructionCodes.ARRAYLEN:
                        i = operands[0];
                        j = operands[1];
    
                        BValue value = sf.refRegs[i];
    
                        if (value == null) {
                            handleNullRefError(ctx);
                            break;
                        }
    
                        if (value.getType().getTag() == TypeTags.JSON_TAG) {
                            sf.longRegs[j] = ((BJSON) value).value().size();
                            break;
                        }
    
                        sf.longRegs[j] = ((BNewArray) value).size();
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
                    case InstructionCodes.LNEWARRAY:
                        i = operands[0];
                        sf.refRegs[i] = new BBlobArray();
                        break;
                    case InstructionCodes.RNEWARRAY:
                        i = operands[0];
                        cpIndex = operands[1];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        sf.refRegs[i] = new BRefValueArray(typeRefCPEntry.getType());
                        break;
                    case InstructionCodes.JSONNEWARRAY:
                        i = operands[0];
                        j = operands[1];
                        // This is a temporary solution to create n-valued JSON array
                        StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
                        for (int index = 0; index < sf.longRegs[j]; index++) {
                            stringJoiner.add(null);
                        }
                        sf.refRegs[i] = new BJSON(stringJoiner.toString());
                        break;
    
                    case InstructionCodes.NEWSTRUCT:
                        createNewStruct(ctx, operands, sf);
                        break;
                    case InstructionCodes.NEWMAP:
                        i = operands[0];
                        cpIndex = operands[1];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        BMapType mapType = (BMapType) typeRefCPEntry.getType();
                        sf.refRegs[i] = new BMap<String, BRefType>(mapType);
                        break;
                    case InstructionCodes.NEWJSON:
                        i = operands[0];
                        cpIndex = operands[1];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        sf.refRegs[i] = new BJSON("{}", typeRefCPEntry.getType());
                        break;
                    case InstructionCodes.NEWTABLE:
                        i = operands[0];
                        cpIndex = operands[1];
                        j = operands[2];
                        BStruct configStruct = (BStruct) sf.refRegs[j];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        sf.refRegs[i] = new BTable(typeRefCPEntry.getType(), configStruct);
                        break;
                    case InstructionCodes.NEWSTREAM:
                        i = operands[0];
                        cpIndex = operands[1];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        StringCPEntry name = (StringCPEntry) ctx.constPool[operands[2]];
                        BStream stream = new BStream(typeRefCPEntry.getType(), name.getValue());
                        sf.refRegs[i] = stream;
                        break;
                    case InstructionCodes.NEW_INT_RANGE:
                        createNewIntRange(operands, sf);
                        break;
                    case InstructionCodes.IRET:
                        i = operands[0];
                        j = operands[1];
                        currentSF = ctx.workerLocal;
                        callersSF = ctx.workerResult;
                        callersRetRegIndex = ctx.retRegIndexes[i];
                        callersSF.longRegs[callersRetRegIndex] = currentSF.longRegs[j];
                        break;
                    case InstructionCodes.FRET:
                        i = operands[0];
                        j = operands[1];
                        currentSF = ctx.workerLocal;
                        callersSF = ctx.workerResult;
                        callersRetRegIndex = ctx.retRegIndexes[i];
                        callersSF.doubleRegs[callersRetRegIndex] = currentSF.doubleRegs[j];
                        break;
                    case InstructionCodes.SRET:
                        i = operands[0];
                        j = operands[1];
                        currentSF = ctx.workerLocal;
                        callersSF = ctx.workerResult;
                        callersRetRegIndex = ctx.retRegIndexes[i];
                        callersSF.stringRegs[callersRetRegIndex] = currentSF.stringRegs[j];
                        break;
                    case InstructionCodes.BRET:
                        i = operands[0];
                        j = operands[1];
                        currentSF = ctx.workerLocal;
                        callersSF = ctx.workerResult;
                        callersRetRegIndex = ctx.retRegIndexes[i];
                        callersSF.intRegs[callersRetRegIndex] = currentSF.intRegs[j];
                        break;
                    case InstructionCodes.LRET:
                        i = operands[0];
                        j = operands[1];
                        currentSF = ctx.workerLocal;
                        callersSF = ctx.workerResult;
                        callersRetRegIndex = ctx.retRegIndexes[i];
                        callersSF.byteRegs[callersRetRegIndex] = currentSF.byteRegs[j];
                        break;
                    case InstructionCodes.RRET:
                        i = operands[0];
                        j = operands[1];
                        currentSF = ctx.workerLocal;
                        callersSF = ctx.workerResult;
                        callersRetRegIndex = ctx.retRegIndexes[i];
                        callersSF.refRegs[callersRetRegIndex] = currentSF.refRegs[j];
                        break;
                    case InstructionCodes.RET:
                        ctx = handleReturn(ctx);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.XMLATTRSTORE:
                    case InstructionCodes.XMLATTRLOAD:
                    case InstructionCodes.XML2XMLATTRS:
                    case InstructionCodes.S2QNAME:
                    case InstructionCodes.NEWQNAME:
                    case InstructionCodes.NEWXMLELEMENT:
                    case InstructionCodes.NEWXMLCOMMENT:
                    case InstructionCodes.NEWXMLTEXT:
                    case InstructionCodes.NEWXMLPI:
                    case InstructionCodes.XMLSEQSTORE:
                    case InstructionCodes.XMLSEQLOAD:
                    case InstructionCodes.XMLLOAD:
                    case InstructionCodes.XMLLOADALL:
                    case InstructionCodes.NEWXMLSEQ:
                        execXMLOpcodes(ctx, sf, opcode, operands);
                        break;
                    case InstructionCodes.ITR_NEW:
                    case InstructionCodes.ITR_NEXT:
                    case InstructionCodes.ITR_HAS_NEXT:
                        execIteratorOperation(ctx, sf, instruction);
                        break;
                    case InstructionCodes.LOCK:
                        InstructionLock instructionLock = (InstructionLock) instruction;
                        if (!handleVariableLock(ctx, instructionLock.types, instructionLock.varRegs)) {
                            return;
                        }
                        break;
                    case InstructionCodes.UNLOCK:
                        InstructionLock instructionUnLock = (InstructionLock) instruction;
                        handleVariableUnlock(ctx, instructionUnLock.types, instructionUnLock.varRegs);
                        break;
                    case InstructionCodes.AWAIT:
                        ctx = execAwait(ctx, operands);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (HandleErrorException e) {
                throw e;
            } catch (Throwable e) {
                BLangVMUtils.log("fatal error: " + e.getMessage());
                ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                handleError(ctx);
            }
        }
    }

    private static WorkerExecutionContext invokeCallable(WorkerExecutionContext ctx, BFunctionPointer fp,
                                                         FunctionCallCPEntry funcCallCPEntry,
                                                         FunctionInfo functionInfo, WorkerData sf) {
        List<BClosure> closureVars = fp.getClosureVars();
        int[] argRegs = funcCallCPEntry.getArgRegs();
        if (closureVars.isEmpty()) {
            argRegs = expandArgRegs(argRegs, functionInfo.getParamTypes());
            return BLangFunctions.invokeCallable(functionInfo, ctx, argRegs, funcCallCPEntry.getRetRegs(), false);
        }

        int[] newArgRegs = new int[argRegs.length + closureVars.size()];
        System.arraycopy(argRegs, 0, newArgRegs, closureVars.size(), argRegs.length);
        int argRegIndex = 0;

        int longIndex = expandLongRegs(sf, fp);
        int doubleIndex = expandDoubleRegs(sf, fp);
        int intIndex = expandIntRegs(sf, fp);
        int stringIndex = expandStringRegs(sf, fp);
        int byteIndex = expandByteRegs(sf, fp);
        int refIndex = expandRefRegs(sf, fp);

        for (BClosure closure : closureVars) {
            switch (closure.getType().getTag()) {
                case TypeTags.INT_TAG: {
                    sf.longRegs[longIndex] = ((BInteger) closure.value()).intValue();
                    newArgRegs[argRegIndex++] = longIndex++;
                    break;
                }
                case TypeTags.FLOAT_TAG: {
                    sf.doubleRegs[doubleIndex] = ((BFloat) closure.value()).floatValue();
                    newArgRegs[argRegIndex++] = doubleIndex++;
                    break;
                }
                case TypeTags.BOOLEAN_TAG: {
                    sf.intRegs[intIndex] = ((BBoolean) closure.value()).booleanValue() ? 1 : 0;
                    newArgRegs[argRegIndex++] = intIndex++;
                    break;
                }
                case TypeTags.STRING_TAG: {
                    sf.stringRegs[stringIndex] = (closure.value()).stringValue();
                    newArgRegs[argRegIndex++] = stringIndex++;
                    break;
                }
                case TypeTags.BLOB_TAG: {
                    sf.byteRegs[byteIndex] = ((BBlob) closure.value()).blobValue();
                    newArgRegs[argRegIndex++] = byteIndex++;
                    break;
                }
                default:
                    sf.refRegs[refIndex] = ((BRefType) closure.value());
                    newArgRegs[argRegIndex++] = refIndex++;
            }
        }

        return BLangFunctions.invokeCallable(functionInfo, ctx, newArgRegs, funcCallCPEntry.getRetRegs(), false);
    }

    private static int[] expandArgRegs(int[] argRegs, BType[] paramTypes) {
        if (paramTypes.length == 0 || paramTypes.length == argRegs.length ||
                TypeTags.STRUCT_TAG != paramTypes[0].getTag()) {
            return argRegs;
        }
        int[] expandedArgs = new int[paramTypes.length];
        // self object/struct param is always at the 0'th index
        expandedArgs[0] = 0;
        System.arraycopy(argRegs, 0, expandedArgs, 1, argRegs.length);
        return expandedArgs;
    }

    private static int expandLongRegs(WorkerData sf, BFunctionPointer fp) {
        int longIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeInt.getTag()) > 0) {
            if (sf.longRegs == null) {
                sf.longRegs = new long[0];
            }
            long[] newLongRegs = new long[sf.longRegs.length + fp.getAdditionalIndexCount(BTypes.typeInt.getTag())];
            System.arraycopy(sf.longRegs, 0, newLongRegs, 0, sf.longRegs.length);
            longIndex = sf.longRegs.length;
            sf.longRegs = newLongRegs;
        }
        return longIndex;
    }

    private static int expandIntRegs(WorkerData sf, BFunctionPointer fp) {
        int intIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeBoolean.getTag()) > 0) {
            if (sf.intRegs == null) {
                sf.intRegs = new int[0];
            }
            int[] newIntRegs = new int[sf.intRegs.length + fp.getAdditionalIndexCount(BTypes.typeBoolean.getTag())];
            System.arraycopy(sf.intRegs, 0, newIntRegs, 0, sf.intRegs.length);
            intIndex = sf.intRegs.length;
            sf.intRegs = newIntRegs;
        }
        return intIndex;
    }

    private static int expandDoubleRegs(WorkerData sf, BFunctionPointer fp) {
        int doubleIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeFloat.getTag()) > 0) {
            if (sf.doubleRegs == null) {
                sf.doubleRegs = new double[0];
            }
            double[] newDoubleRegs = new double[sf.doubleRegs.length +
                    fp.getAdditionalIndexCount(BTypes.typeFloat.getTag())];
            System.arraycopy(sf.doubleRegs, 0, newDoubleRegs, 0, sf.doubleRegs.length);
            doubleIndex = sf.intRegs.length;
            sf.doubleRegs = newDoubleRegs;
        }
        return doubleIndex;
    }

    private static int expandStringRegs(WorkerData sf, BFunctionPointer fp) {
        int stringIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeString.getTag()) > 0) {
            if (sf.stringRegs == null) {
                sf.stringRegs = new String[0];
            }
            String[] newStringRegs = new String[sf.stringRegs.length +
                    fp.getAdditionalIndexCount(BTypes.typeString.getTag())];
            System.arraycopy(sf.stringRegs, 0, newStringRegs, 0, sf.stringRegs.length);
            stringIndex = sf.stringRegs.length;
            sf.stringRegs = newStringRegs;
        }
        return stringIndex;
    }

    private static int expandByteRegs(WorkerData sf, BFunctionPointer fp) {
        int byteIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeBlob.getTag()) > 0) {
            if (sf.byteRegs == null) {
                sf.byteRegs = new byte[0][];
            }
            byte[][] newByteRegs = new byte[sf.byteRegs.length +
                    fp.getAdditionalIndexCount(BTypes.typeBlob.getTag())][];
            System.arraycopy(sf.byteRegs, 0, newByteRegs, 0, sf.byteRegs.length);
            byteIndex = sf.byteRegs.length;
            sf.byteRegs = newByteRegs;
        }
        return byteIndex;
    }

    private static int expandRefRegs(WorkerData sf, BFunctionPointer fp) {
        int refIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeAny.getTag()) > 0) {
            if (sf.refRegs == null) {
                sf.refRegs = new BRefType[0];
            }
            BRefType[] newRefRegs = new BRefType[sf.refRegs.length +
                    fp.getAdditionalIndexCount(BTypes.typeAny.getTag())];
            System.arraycopy(sf.refRegs, 0, newRefRegs, 0, sf.refRegs.length);
            refIndex = sf.refRegs.length;
            sf.refRegs = newRefRegs;
        }
        return refIndex;
    }

    private static void findAndAddClosureVarRegIndexes(WorkerExecutionContext ctx, int[] operands,
                                                       BFunctionPointer fp) {

        int h = operands[2];

        if (h == 0) {
            return;
        }

        for (int i = 0; i < h; i++) {
            int operandIndex = (i * 2) + 3;
            int type = operands[operandIndex];
            int index = operands[++operandIndex];
            switch (type) {
                case TypeTags.INT_TAG: {
                    fp.addClosureVar(new BClosure(new BInteger(ctx.workerLocal.longRegs[index])), TypeTags.INT_TAG);
                    break;
                }
                case TypeTags.FLOAT_TAG: {
                    fp.addClosureVar(new BClosure(new BFloat(ctx.workerLocal.doubleRegs[index])), TypeTags.FLOAT_TAG);
                    break;
                }
                case TypeTags.BOOLEAN_TAG: {
                    fp.addClosureVar(new BClosure(new BBoolean(ctx.workerLocal.intRegs[index] == 1)),
                            TypeTags.BOOLEAN_TAG);
                    break;
                }
                case TypeTags.STRING_TAG: {
                    fp.addClosureVar(new BClosure(new BString(ctx.workerLocal.stringRegs[index])), TypeTags.STRING_TAG);
                    break;
                }
                case TypeTags.BLOB_TAG:
                    fp.addClosureVar(new BClosure(new BBlob(ctx.workerLocal.byteRegs[index])), TypeTags.BLOB_TAG);
                    break;
                default:
                    fp.addClosureVar(new BClosure(ctx.workerLocal.refRegs[index]), TypeTags.ANY_TAG);
            }
        }
    }

    private static void execCmpAndBranchOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode,
                                                int[] operands) {
        int i;
        int j;
        int k;
        switch (opcode) {
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
                    sf.intRegs[j] = 1;
                } else {
                    sf.intRegs[j] = 0;
                }
                break;
            case InstructionCodes.RNE_NULL:
                i = operands[0];
                j = operands[1];
                if (sf.refRegs[i] != null) {
                    sf.intRegs[j] = 1;
                } else {
                    sf.intRegs[j] = 0;
                }
                break;
            case InstructionCodes.SEQ_NULL:
                i = operands[0];
                j = operands[1];
                if (sf.stringRegs[i] == null) {
                    sf.intRegs[j] = 1;
                } else {
                    sf.intRegs[j] = 0;
                }
                break;
            case InstructionCodes.SNE_NULL:
                i = operands[0];
                j = operands[1];
                if (sf.stringRegs[i] != null) {
                    sf.intRegs[j] = 1;
                } else {
                    sf.intRegs[j] = 0;
                }
                break;
            case InstructionCodes.BR_TRUE:
                i = operands[0];
                j = operands[1];
                if (sf.intRegs[i] == 1) {
                    ctx.ip = j;
                }
                break;
            case InstructionCodes.BR_FALSE:
                i = operands[0];
                j = operands[1];
                if (sf.intRegs[i] == 0) {
                    ctx.ip = j;
                }
                break;
            case InstructionCodes.GOTO:
                i = operands[0];
                ctx.ip = i;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void execLoadOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int lvIndex; // Index of the local variable
        int fieldIndex;

        BIntArray bIntArray;
        BFloatArray bFloatArray;
        BStringArray bStringArray;
        BBooleanArray bBooleanArray;
        BBlobArray bBlobArray;
        BRefValueArray bArray;
        StructureType structureType;
        BMap<String, BRefType> bMap;
        BJSON jsonVal;
        switch (opcode) {
            case InstructionCodes.IMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.longRegs[i] = sf.longRegs[lvIndex];
                break;
            case InstructionCodes.FMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.doubleRegs[i] = sf.doubleRegs[lvIndex];
                break;
            case InstructionCodes.SMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.stringRegs[i] = sf.stringRegs[lvIndex];
                break;
            case InstructionCodes.BMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.intRegs[i] = sf.intRegs[lvIndex];
                break;
            case InstructionCodes.LMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.byteRegs[i] = sf.byteRegs[lvIndex];
                break;
            case InstructionCodes.RMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.refRegs[i] = sf.refRegs[lvIndex];
                break;
            case InstructionCodes.IALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bIntArray = (BIntArray) sf.refRegs[i];
                if (bIntArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.longRegs[k] = bIntArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.FALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bFloatArray = (BFloatArray) sf.refRegs[i];
                if (bFloatArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.doubleRegs[k] = bFloatArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.SALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bStringArray = (BStringArray) sf.refRegs[i];
                if (bStringArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.stringRegs[k] = bStringArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bBooleanArray = (BBooleanArray) sf.refRegs[i];
                if (bBooleanArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.intRegs[k] = bBooleanArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.LALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bBlobArray = (BBlobArray) sf.refRegs[i];
                if (bBlobArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.byteRegs[k] = bBlobArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.RALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bArray = (BRefValueArray) sf.refRegs[i];
                if (bArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.refRegs[k] = bArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                jsonVal = (BJSON) sf.refRegs[i];
                if (jsonVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.refRegs[k] = JSONUtils.getArrayElement(jsonVal, sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.IGLOAD:
                // Global variable index
                i = operands[0];
                // Stack registry index
                j = operands[1];
                sf.longRegs[j] = ctx.programFile.getGlobalMemoryBlock().getIntField(i);
                break;
            case InstructionCodes.FGLOAD:
                i = operands[0];
                j = operands[1];
                sf.doubleRegs[j] = ctx.programFile.getGlobalMemoryBlock().getFloatField(i);
                break;
            case InstructionCodes.SGLOAD:
                i = operands[0];
                j = operands[1];
                sf.stringRegs[j] = ctx.programFile.getGlobalMemoryBlock().getStringField(i);
                break;
            case InstructionCodes.BGLOAD:
                i = operands[0];
                j = operands[1];
                sf.intRegs[j] = ctx.programFile.getGlobalMemoryBlock().getBooleanField(i);
                break;
            case InstructionCodes.LGLOAD:
                i = operands[0];
                j = operands[1];
                sf.byteRegs[j] = ctx.programFile.getGlobalMemoryBlock().getBlobField(i);
                break;
            case InstructionCodes.RGLOAD:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = ctx.programFile.getGlobalMemoryBlock().getRefField(i);
                break;

            case InstructionCodes.IFIELDLOAD:
                i = operands[0];
                fieldIndex = operands[1];
                j = operands[2];
                structureType = (StructureType) sf.refRegs[i];
                if (structureType == null) {
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
                    break;
                }

                sf.refRegs[j] = structureType.getRefField(fieldIndex);
                break;

            case InstructionCodes.MAPLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bMap = (BMap<String, BRefType>) sf.refRegs[i];
                if (bMap == null) {
                    handleNullRefError(ctx);
                    break;
                }

                sf.refRegs[k] = bMap.get(sf.stringRegs[j]);
                break;

            case InstructionCodes.JSONLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                jsonVal = (BJSON) sf.refRegs[i];
                if (jsonVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                sf.refRegs[k] = JSONUtils.getElement(jsonVal, sf.stringRegs[j]);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void execStoreOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int fieldIndex;

        BIntArray bIntArray;
        BFloatArray bFloatArray;
        BStringArray bStringArray;
        BBooleanArray bBooleanArray;
        BBlobArray bBlobArray;
        BRefValueArray bArray;
        StructureType structureType;
        BMap<String, BRefType> bMap;
        BJSON jsonVal;
        switch (opcode) {
            case InstructionCodes.IASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bIntArray = (BIntArray) sf.refRegs[i];
                if (bIntArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    bIntArray.add(sf.longRegs[j], sf.longRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.FASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bFloatArray = (BFloatArray) sf.refRegs[i];
                if (bFloatArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    bFloatArray.add(sf.longRegs[j], sf.doubleRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.SASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bStringArray = (BStringArray) sf.refRegs[i];
                if (bStringArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    bStringArray.add(sf.longRegs[j], sf.stringRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bBooleanArray = (BBooleanArray) sf.refRegs[i];
                if (bBooleanArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    bBooleanArray.add(sf.longRegs[j], sf.intRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.LASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bBlobArray = (BBlobArray) sf.refRegs[i];
                if (bBlobArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    bBlobArray.add(sf.longRegs[j], sf.byteRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.RASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bArray = (BRefValueArray) sf.refRegs[i];
                if (bArray == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    bArray.add(sf.longRegs[j], sf.refRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                jsonVal = (BJSON) sf.refRegs[i];
                if (jsonVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    JSONUtils.setArrayElement(jsonVal, sf.longRegs[j], (BJSON) sf.refRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.IGSTORE:
                // Stack reg index
                i = operands[0];
                // Global var index
                j = operands[1];
                ctx.programFile.getGlobalMemoryBlock().setIntField(j, sf.longRegs[i]);
                break;
            case InstructionCodes.FGSTORE:
                i = operands[0];
                j = operands[1];
                ctx.programFile.getGlobalMemoryBlock().setFloatField(j, sf.doubleRegs[i]);
                break;
            case InstructionCodes.SGSTORE:
                i = operands[0];
                j = operands[1];
                ctx.programFile.getGlobalMemoryBlock().setStringField(j, sf.stringRegs[i]);
                break;
            case InstructionCodes.BGSTORE:
                i = operands[0];
                j = operands[1];
                ctx.programFile.getGlobalMemoryBlock().setBooleanField(j, sf.intRegs[i]);
                break;
            case InstructionCodes.LGSTORE:
                i = operands[0];
                j = operands[1];
                ctx.programFile.getGlobalMemoryBlock().setBlobField(j, sf.byteRegs[i]);
                break;
            case InstructionCodes.RGSTORE:
                i = operands[0];
                j = operands[1];
                ctx.programFile.getGlobalMemoryBlock().setRefField(j, sf.refRegs[i]);
                break;

            case InstructionCodes.IFIELDSTORE:
                i = operands[0];
                fieldIndex = operands[1];
                j = operands[2];
                structureType = (StructureType) sf.refRegs[i];
                if (structureType == null) {
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
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
                    handleNullRefError(ctx);
                    break;
                }

                structureType.setRefField(fieldIndex, sf.refRegs[j]);
                break;
            case InstructionCodes.MAPSTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bMap = (BMap<String, BRefType>) sf.refRegs[i];
                if (bMap == null) {
                    handleNullRefError(ctx);
                    break;
                }

                BMapType mapType = (BMapType) bMap.getType();
                if (sf.refRegs[k] == null) {
                    bMap.put(sf.stringRegs[j], sf.refRegs[k]);
                } else if (mapType.getConstrainedType() == BTypes.typeAny ||
                        mapType.getConstrainedType().equals(sf.refRegs[k].getType())) {
                    bMap.put(sf.stringRegs[j], sf.refRegs[k]);
                } else if (sf.refRegs[k].getType().getTag() == TypeTags.STRUCT_TAG
                        && mapType.getConstrainedType().getTag() == TypeTags.STRUCT_TAG
                        && checkStructEquivalency((BStructType) sf.refRegs[k].getType(),
                        (BStructType) mapType.getConstrainedType())) {
                    bMap.put(sf.stringRegs[j], sf.refRegs[k]);
                } else {
                    ctx.setError(BLangVMErrors.createError(ctx,
                            BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_MAP_INSERTION,
                                    mapType.getConstrainedType(), sf.refRegs[k].getType())));
                    handleError(ctx);
                    break;
                }
                break;
            case InstructionCodes.JSONSTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                jsonVal = (BJSON) sf.refRegs[i];
                if (jsonVal == null) {
                    handleNullRefError(ctx);
                    break;
                }
                JSONUtils.setElement(jsonVal, sf.stringRegs[j], (BJSON) sf.refRegs[k]);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void execBinaryOpCodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        switch (opcode) {
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
                    handleNullRefError(ctx);
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
                    ctx.setError(BLangVMErrors.createError(ctx, " / by zero"));
                    handleError(ctx);
                    break;
                }

                sf.longRegs[k] = sf.longRegs[i] / sf.longRegs[j];
                break;
            case InstructionCodes.FDIV:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.doubleRegs[j] == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, " / by zero"));
                    handleError(ctx);
                    break;
                }

                sf.doubleRegs[k] = sf.doubleRegs[i] / sf.doubleRegs[j];
                break;
            case InstructionCodes.IMOD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.longRegs[j] == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, " / by zero"));
                    handleError(ctx);
                    break;
                }

                sf.longRegs[k] = sf.longRegs[i] % sf.longRegs[j];
                break;
            case InstructionCodes.FMOD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.doubleRegs[j] == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, " / by zero"));
                    handleError(ctx);
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
                sf.intRegs[k] = StringUtils.isEqual(sf.stringRegs[i], sf.stringRegs[j]) ? 1 : 0;
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
            case InstructionCodes.TEQ:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.refRegs[i] == null || sf.refRegs[j] == null) {
                    handleNullRefError(ctx);
                }
                sf.intRegs[k] = sf.refRegs[i].equals(sf.refRegs[j]) ? 1 : 0;
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
                sf.intRegs[k] = !StringUtils.isEqual(sf.stringRegs[i], sf.stringRegs[j]) ? 1 : 0;
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
            case InstructionCodes.TNE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.refRegs[i] == null || sf.refRegs[j] == null) {
                    handleNullRefError(ctx);
                }
                sf.intRegs[k] = (!sf.refRegs[i].equals(sf.refRegs[j])) ? 1 : 0;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void execXMLOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int localNameIndex;
        int uriIndex;
        int prefixIndex;

        BXML<?> xmlVal;
        BXMLQName xmlQName;

        switch (opcode) {
            case InstructionCodes.XMLATTRSTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                xmlVal = (BXML) sf.refRegs[i];
                if (xmlVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                xmlQName = (BXMLQName) sf.refRegs[j];
                if (xmlQName == null) {
                    handleNullRefError(ctx);
                    break;
                }

                xmlVal.setAttribute(xmlQName.getLocalName(), xmlQName.getUri(), xmlQName.getPrefix(),
                        sf.stringRegs[k]);
                break;
            case InstructionCodes.XMLATTRLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                xmlVal = (BXML) sf.refRegs[i];
                if (xmlVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                xmlQName = (BXMLQName) sf.refRegs[j];
                if (xmlQName == null) {
                    handleNullRefError(ctx);
                    break;
                }

                sf.stringRegs[k] = xmlVal.getAttribute(xmlQName.getLocalName(), xmlQName.getUri(),
                        xmlQName.getPrefix());
                break;
            case InstructionCodes.XML2XMLATTRS:
                i = operands[0];
                j = operands[1];

                xmlVal = (BXML) sf.refRegs[i];
                if (xmlVal == null) {
                    sf.refRegs[j] = null;
                    break;
                }

                sf.refRegs[j] = new BXMLAttributes(xmlVal);
                break;
            case InstructionCodes.S2QNAME:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                String qNameStr = sf.stringRegs[i];
                int parenEndIndex = qNameStr.indexOf('}');

                if (qNameStr.startsWith("{") && parenEndIndex > 0) {
                    sf.stringRegs[j] = qNameStr.substring(parenEndIndex + 1, qNameStr.length());
                    sf.stringRegs[k] = qNameStr.substring(1, parenEndIndex);
                } else {
                    sf.stringRegs[j] = qNameStr;
                    sf.stringRegs[k] = STRING_NULL_VALUE;
                }

                break;
            case InstructionCodes.NEWQNAME:
                localNameIndex = operands[0];
                uriIndex = operands[1];
                prefixIndex = operands[2];
                i = operands[3];

                String localname = sf.stringRegs[localNameIndex];
                localname = StringEscapeUtils.escapeXml11(localname);

                String prefix = sf.stringRegs[prefixIndex];
                prefix = StringEscapeUtils.escapeXml11(prefix);

                sf.refRegs[i] = new BXMLQName(localname, sf.stringRegs[uriIndex], prefix);
                break;
            case InstructionCodes.XMLSEQLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                xmlVal = (BXML) sf.refRegs[i];
                if (xmlVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                long index = sf.longRegs[j];
                sf.refRegs[k] = xmlVal.getItem(index);
                break;
            case InstructionCodes.XMLLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                xmlVal = (BXML<?>) sf.refRegs[i];
                if (xmlVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                String qname = sf.stringRegs[j];
                sf.refRegs[k] = xmlVal.children(qname);
                break;
            case InstructionCodes.XMLLOADALL:
                i = operands[0];
                j = operands[1];

                xmlVal = (BXML<?>) sf.refRegs[i];
                if (xmlVal == null) {
                    handleNullRefError(ctx);
                    break;
                }

                sf.refRegs[j] = xmlVal.children();
                break;
            case InstructionCodes.NEWXMLELEMENT:
            case InstructionCodes.NEWXMLCOMMENT:
            case InstructionCodes.NEWXMLTEXT:
            case InstructionCodes.NEWXMLPI:
            case InstructionCodes.XMLSEQSTORE:
            case InstructionCodes.NEWXMLSEQ:
                execXMLCreationOpcodes(ctx, sf, opcode, operands);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void execTypeCastOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int cpIndex; // Index of the constant pool

        BRefType bRefTypeValue;
        TypeRefCPEntry typeRefCPEntry;

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
                sf.longRegs[j] = ((BInteger) sf.refRegs[i]).intValue();
                break;
            case InstructionCodes.ANY2F:
                i = operands[0];
                j = operands[1];
                sf.doubleRegs[j] = ((BFloat) sf.refRegs[i]).floatValue();
                break;
            case InstructionCodes.ANY2S:
                i = operands[0];
                j = operands[1];
                sf.stringRegs[j] = sf.refRegs[i].stringValue();
                break;
            case InstructionCodes.ANY2B:
                i = operands[0];
                j = operands[1];
                sf.intRegs[j] = ((BBoolean) sf.refRegs[i]).booleanValue() ? 1 : 0;
                break;
            case InstructionCodes.ANY2L:
                i = operands[0];
                j = operands[1];
                sf.byteRegs[j] = ((BBlob) sf.refRegs[i]).blobValue();
                break;
            case InstructionCodes.ANY2JSON:
                handleAnyToRefTypeCast(ctx, sf, operands, BTypes.typeJSON);
                break;
            case InstructionCodes.ANY2XML:
                handleAnyToRefTypeCast(ctx, sf, operands, BTypes.typeXML);
                break;
            case InstructionCodes.ANY2MAP:
                handleAnyToRefTypeCast(ctx, sf, operands, BTypes.typeMap);
                break;
            case InstructionCodes.ANY2TYPE:
                handleAnyToRefTypeCast(ctx, sf, operands, BTypes.typeDesc);
                break;
            case InstructionCodes.ANY2DT:
                handleAnyToRefTypeCast(ctx, sf, operands, BTypes.typeTable);
                break;
            case InstructionCodes.ANY2STM:
                handleAnyToRefTypeCast(ctx, sf, operands, BTypes.typeStream);
                break;
            case InstructionCodes.ANY2E:
            case InstructionCodes.ANY2T:
            case InstructionCodes.ANY2C:
            case InstructionCodes.CHECKCAST:
                i = operands[0];
                cpIndex = operands[1];
                j = operands[2];
                typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];

                bRefTypeValue = sf.refRegs[i];

                if (checkCast(bRefTypeValue, typeRefCPEntry.getType())) {
                    sf.refRegs[j] = sf.refRegs[i];
                } else {
                    handleTypeCastError(ctx, sf, j, bRefTypeValue != null ? bRefTypeValue.getType() : BTypes.typeNull,
                            typeRefCPEntry.getType());
                }
                break;
            case InstructionCodes.IS_ASSIGNABLE:
                i = operands[0];
                cpIndex = operands[1];
                j = operands[2];
                typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                bRefTypeValue = sf.refRegs[i];
                if (isAssignable(bRefTypeValue, typeRefCPEntry.getType())) {
                    sf.intRegs[j] = 1;
                } else {
                    sf.intRegs[j] = 0;
                }
                break;
            case InstructionCodes.NULL2JSON:
                j = operands[1];
                sf.refRegs[j] = new BJSON("null");
                break;
            case InstructionCodes.B2JSON:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BJSON(sf.intRegs[i] == 1 ? "true" : "false");
                break;
            case InstructionCodes.JSON2I:
                castJSONToInt(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2F:
                castJSONToFloat(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2S:
                castJSONToString(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2B:
                castJSONToBoolean(ctx, operands, sf);
                break;
            case InstructionCodes.NULL2S:
                j = operands[1];
                sf.stringRegs[j] = null;
                break;
            case InstructionCodes.ARRAY2JSON:
                convertArrayToJSON(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2ARRAY:
                break;
            case InstructionCodes.CHECK_CONVERSION:
                i = operands[0];
                j = operands[1];
                bRefTypeValue = sf.refRegs[i];
                sf.refRegs[j] = JSONUtils.convertUnionTypeToJSON(bRefTypeValue);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void execTypeConversionOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode,
                                                  int[] operands) {
        int i;
        int j;
        int k;
        BRefType bRefType;
        String str;

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

                str = sf.stringRegs[i];
                if (str == null) {
                    handleTypeConversionError(ctx, sf, j, null, TypeConstants.INT_TNAME);
                    break;
                }

                try {
                    sf.refRegs[j] = new BInteger(Long.parseLong(str));
                } catch (NumberFormatException e) {
                    handleTypeConversionError(ctx, sf, j, TypeConstants.STRING_TNAME, TypeConstants.INT_TNAME);
                }
                break;
            case InstructionCodes.S2F:
                i = operands[0];
                j = operands[1];

                str = sf.stringRegs[i];
                if (str == null) {
                    handleTypeConversionError(ctx, sf, j, null, TypeConstants.FLOAT_TNAME);
                    break;
                }

                try {
                    sf.refRegs[j] = new BFloat(Double.parseDouble(str));
                } catch (NumberFormatException e) {
                    handleTypeConversionError(ctx, sf, j, TypeConstants.STRING_TNAME, TypeConstants.FLOAT_TNAME);
                }
                break;
            case InstructionCodes.S2B:
                i = operands[0];
                j = operands[1];
                sf.intRegs[j] = Boolean.parseBoolean(sf.stringRegs[i]) ? 1 : 0;
                break;
            case InstructionCodes.S2JSON:
                i = operands[0];
                j = operands[1];
                str = StringEscapeUtils.escapeJson(sf.stringRegs[i]);
                sf.refRegs[j] = str == null ? null : new BJSON("\"" + str + "\"");
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
            case InstructionCodes.DT2XML:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.refRegs[j] = XMLUtils.tableToXML((BTable) bRefType, ctx.isInTransaction());
                } catch (Exception e) {
                    sf.refRegs[j] = null;
                    handleTypeConversionError(ctx, sf, j, TypeConstants.TABLE_TNAME, TypeConstants.XML_TNAME);
                }
                break;
            case InstructionCodes.DT2JSON:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleNullRefError(ctx);
                    break;
                }

                try {
                    sf.refRegs[j] = JSONUtils.toJSON((BTable) bRefType, ctx.isInTransaction());
                } catch (Exception e) {
                    handleTypeConversionError(ctx, sf, j, TypeConstants.TABLE_TNAME, TypeConstants.XML_TNAME);
                }
                break;
            case InstructionCodes.T2MAP:
                convertStructToMap(ctx, operands, sf);
                break;
            case InstructionCodes.T2JSON:
                convertStructToJSON(ctx, operands, sf);
                break;
            case InstructionCodes.MAP2JSON:
                convertMapToJSON(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2MAP:
                convertJSONToMap(ctx, operands, sf);
                break;
            case InstructionCodes.MAP2T:
                convertMapToStruct(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2T:
                convertJSONToStruct(ctx, operands, sf);
                break;
            case InstructionCodes.XMLATTRS2MAP:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    sf.refRegs[j] = null;
                    break;
                }

                sf.refRegs[j] = ((BXMLAttributes) sf.refRegs[i]).value();
                break;
            case InstructionCodes.S2XML:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                str = sf.stringRegs[i];
                if (str == null) {
                    sf.refRegs[j] = null;
                    sf.refRegs[k] = null;
                    break;
                }

                try {
                    sf.refRegs[j] = XMLUtils.parse(str);
                    sf.refRegs[k] = null;
                } catch (BallerinaException e) {
                    sf.refRegs[j] = null;
                    handleTypeConversionError(ctx, sf, k, e.getMessage());
                }
                break;
            case InstructionCodes.S2JSONX:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                str = sf.stringRegs[i];

                try {
                    sf.refRegs[j] = str == null ? null : new BJSON(str);
                    sf.refRegs[k] = null;
                } catch (BallerinaException e) {
                    sf.refRegs[j] = null;
                    handleTypeConversionError(ctx, sf, k, e.getMessage());
                }
                break;
            case InstructionCodes.XML2S:
                i = operands[0];
                j = operands[1];
                sf.stringRegs[j] = sf.refRegs[i].stringValue();
                break;
            case InstructionCodes.ANY2SCONV:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    sf.stringRegs[j] = STRING_NULL_VALUE;
                } else {
                    sf.stringRegs[j] = bRefType.stringValue();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execIteratorOperation(WorkerExecutionContext ctx, WorkerData sf, Instruction instruction) {
        int i, j;
        BCollection collection;
        BIterator iterator;
        InstructionIteratorNext nextInstruction;
        switch (instruction.getOpcode()) {
            case InstructionCodes.ITR_NEW:
                i = instruction.getOperands()[0];   // collection
                j = instruction.getOperands()[1];   // iterator variable (ref) index.
                collection = (BCollection) sf.refRegs[i];
                if (collection == null) {
                    handleNullRefError(ctx);
                    return;
                }
                sf.refRegs[j] = collection.newIterator();
                break;
            case InstructionCodes.ITR_HAS_NEXT:
                i = instruction.getOperands()[0];   // iterator
                j = instruction.getOperands()[1];   // boolean variable index to store has next result
                iterator = (BIterator) sf.refRegs[i];
                if (iterator == null) {
                    sf.intRegs[j] = 0;
                    return;
                }
                sf.intRegs[j] = iterator.hasNext() ? 1 : 0;
                break;
            case InstructionCodes.ITR_NEXT:
                nextInstruction = (InstructionIteratorNext) instruction;
                iterator = (BIterator) sf.refRegs[nextInstruction.iteratorIndex];
                if (iterator == null) {
                    return;
                }
                BValue[] values = iterator.getNext(nextInstruction.arity);
                copyValuesToRegistries(nextInstruction.typeTags, nextInstruction.retRegs, values, sf);
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    private static void copyValuesToRegistries(int[] typeTags, int[] targetReg, BValue[] values, WorkerData sf) {
        for (int i = 0; i < typeTags.length; i++) {
            BValue source = values[i];
            int target = targetReg[i];
            switch (typeTags[i]) {
                case TypeTags.INT_TAG:
                    sf.longRegs[target] = ((BInteger) source).intValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    sf.doubleRegs[target] = ((BFloat) source).floatValue();
                    break;
                case TypeTags.STRING_TAG:
                    sf.stringRegs[target] = source.stringValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    sf.intRegs[target] = ((BBoolean) source).booleanValue() ? 1 : 0;
                    break;
                case TypeTags.BLOB_TAG:
                    sf.byteRegs[target] = ((BBlob) source).blobValue();
                    break;
                default:
                    sf.refRegs[target] = (BRefType) source;
            }
        }
    }

    private static void execXMLCreationOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode,
                                               int[] operands) {
        int i;
        int j;
        int k;
        int l;
        BXML<?> xmlVal;

        switch (opcode) {
            case InstructionCodes.NEWXMLELEMENT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                l = operands[3];

                BXMLQName startTagName = (BXMLQName) sf.refRegs[j];
                BXMLQName endTagName = (BXMLQName) sf.refRegs[k];

                try {
                    sf.refRegs[i] = XMLUtils.createXMLElement(startTagName, endTagName, sf.stringRegs[l]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.NEWXMLCOMMENT:
                i = operands[0];
                j = operands[1];

                try {
                    sf.refRegs[i] = XMLUtils.createXMLComment(sf.stringRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.NEWXMLTEXT:
                i = operands[0];
                j = operands[1];

                try {
                    sf.refRegs[i] = XMLUtils.createXMLText(sf.stringRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.NEWXMLPI:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    sf.refRegs[i] = XMLUtils.createXMLProcessingInstruction(sf.stringRegs[j], sf.stringRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.XMLSEQSTORE:
                i = operands[0];
                j = operands[1];

                xmlVal = (BXML<?>) sf.refRegs[i];
                BXML<?> child = (BXML<?>) sf.refRegs[j];
                xmlVal.addChildren(child);
                break;
            case InstructionCodes.NEWXMLSEQ:
                i = operands[0];
                sf.refRegs[i] = new BXMLSequence();
                break;
        }
    }

    private static boolean handleVariableLock(WorkerExecutionContext ctx, BType[] types, int[] varRegs) {
        boolean lockAcquired = true;
        for (int i = 0; i < varRegs.length && lockAcquired; i++) {
            BType paramType = types[i];
            int regIndex = varRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    lockAcquired = ctx.programFile.getGlobalMemoryBlock().lockIntField(ctx, regIndex);
                    break;
                case TypeTags.FLOAT_TAG:
                    lockAcquired = ctx.programFile.getGlobalMemoryBlock().lockFloatField(ctx, regIndex);
                    break;
                case TypeTags.STRING_TAG:
                    lockAcquired = ctx.programFile.getGlobalMemoryBlock().lockStringField(ctx, regIndex);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    lockAcquired = ctx.programFile.getGlobalMemoryBlock().lockBooleanField(ctx, regIndex);
                    break;
                case TypeTags.BLOB_TAG:
                    lockAcquired = ctx.programFile.getGlobalMemoryBlock().lockBlobField(ctx, regIndex);
                    break;
                default:
                    lockAcquired = ctx.programFile.getGlobalMemoryBlock().lockRefField(ctx, regIndex);
            }
        }
        return lockAcquired;
    }

    private static void handleVariableUnlock(WorkerExecutionContext ctx, BType[] types, int[] varRegs) {
        for (int i = varRegs.length - 1; i > -1; i--) {
            BType paramType = types[i];
            int regIndex = varRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    ctx.programFile.getGlobalMemoryBlock().unlockIntField(regIndex);
                    break;
                case TypeTags.FLOAT_TAG:
                    ctx.programFile.getGlobalMemoryBlock().unlockFloatField(regIndex);
                    break;
                case TypeTags.STRING_TAG:
                    ctx.programFile.getGlobalMemoryBlock().unlockStringField(regIndex);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    ctx.programFile.getGlobalMemoryBlock().unlockBooleanField(regIndex);
                    break;
                case TypeTags.BLOB_TAG:
                    ctx.programFile.getGlobalMemoryBlock().unlockBlobField(regIndex);
                    break;
                default:
                    ctx.programFile.getGlobalMemoryBlock().unlockRefField(regIndex);
            }
        }
    }

    /**
     * Method to calculate and detect debug points when the instruction point is given.
     */
    private static boolean debug(WorkerExecutionContext ctx) {
        Debugger debugger = ctx.programFile.getDebugger();
        if (!debugger.isClientSessionActive()) {
            return false;
        }
        DebugContext debugContext = ctx.getDebugContext();

        if (debugContext.isWorkerPaused()) {
            debugContext.setWorkerPaused(false);
            return false;
        }

        LineNumberInfo currentExecLine = debugger
                .getLineNumber(ctx.callableUnitInfo.getPackageInfo().getPkgPath(), ctx.ip);
        /*
         Below if check stops hitting the same debug line again and again in case that single line has
         multctx.iple instructions.
         */
        if (currentExecLine.equals(debugContext.getLastLine())) {
            return false;
        }
        if (debugPointCheck(ctx, currentExecLine, debugger)) {
            return true;
        }

        switch (debugContext.getCurrentCommand()) {
            case RESUME:
                /*
                 In case of a for loop, need to clear the last hit line, so that, same line can get hit again.
                 */
                debugContext.clearLastDebugLine();
                break;
            case STEP_IN:
            case STEP_OVER:
                debugHit(ctx, currentExecLine, debugger);
                return true;
            case STEP_OUT:
                break;
            default:
                debugger.notifyExit();
                debugger.stopDebugging();
        }
        return false;
    }

    /**
     * Helper method to check whether given point is a debug point or not.
     * If it's a debug point, then notify the debugger.
     *
     * @param ctx             Current ctx.
     * @param currentExecLine Current execution line.
     * @param debugger        Debugger object.
     * @return Boolean true if it's a debug point, false otherwise.
     */
    private static boolean debugPointCheck(WorkerExecutionContext ctx, LineNumberInfo currentExecLine,
                                           Debugger debugger) {
        if (!currentExecLine.isDebugPoint()) {
            return false;
        }
        debugHit(ctx, currentExecLine, debugger);
        return true;
    }

    /**
     * Helper method to set required details when a debug point hits.
     * And also to notify the debugger.
     *
     * @param ctx             Current ctx.
     * @param currentExecLine Current execution line.
     * @param debugger        Debugger object.
     */
    private static void debugHit(WorkerExecutionContext ctx, LineNumberInfo currentExecLine, Debugger debugger) {
        ctx.getDebugContext().setLastLine(currentExecLine);
        debugger.pauseWorker(ctx);
        debugger.notifyDebugHit(ctx, currentExecLine, ctx.getDebugContext().getWorkerId());
    }

    @SuppressWarnings("rawtypes")
    private static void handleAnyToRefTypeCast(WorkerExecutionContext ctx, WorkerData sf, int[] operands,
                                               BType targetType) {
        int i = operands[0];
        int j = operands[1];

        BRefType bRefType = sf.refRegs[i];
        if (bRefType == null) {
            sf.refRegs[j] = null;
        } else if (bRefType.getType() == targetType) {
            sf.refRegs[j] = bRefType;
        } else {
            handleTypeCastError(ctx, sf, j, bRefType.getType(), targetType);
        }
    }

    private static void handleTypeCastError(WorkerExecutionContext ctx, WorkerData sf, int errorRegIndex,
                                            BType sourceType, BType targetType) {
        handleTypeCastError(ctx, sf, errorRegIndex, sourceType.toString(), targetType.toString());
    }

    private static void handleTypeCastError(WorkerExecutionContext ctx, WorkerData sf, int errorRegIndex,
                                            String sourceType, String targetType) {
        BStruct errorVal;
        errorVal = BLangVMErrors.createTypeCastError(ctx, sourceType, targetType);
        sf.refRegs[errorRegIndex] = errorVal;
    }

    private static void handleTypeConversionError(WorkerExecutionContext ctx, WorkerData sf, int errorRegIndex,
                                                  String sourceTypeName, String targetTypeName) {
        String errorMsg = "'" + sourceTypeName + "' cannot be converted to '" + targetTypeName + "'";
        handleTypeConversionError(ctx, sf, errorRegIndex, errorMsg);
    }

    private static void handleTypeConversionError(WorkerExecutionContext ctx, WorkerData sf,
                                                  int errorRegIndex, String errorMessage) {
        BStruct errorVal;
        errorVal = BLangVMErrors.createTypeConversionError(ctx, errorMessage);
        if (errorRegIndex == -1) {
            ctx.setError(errorVal);
            handleError(ctx);
            return;
        }

        sf.refRegs[errorRegIndex] = errorVal;
    }

    private static void createNewIntRange(int[] operands, WorkerData sf) {
        long startValue = sf.longRegs[operands[0]];
        long endValue = sf.longRegs[operands[1]];
        sf.refRegs[operands[2]] = new BIntRange(startValue, endValue);
    }

    private static void createNewStruct(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int cpIndex = operands[0];
        int i = operands[1];
        StructureRefCPEntry structureRefCPEntry = (StructureRefCPEntry) ctx.constPool[cpIndex];
        StructInfo structInfo = (StructInfo) structureRefCPEntry.getStructureTypeInfo();
        sf.refRegs[i] = new BStruct(structInfo.getType());
    }

    private static void beginTransaction(WorkerExecutionContext ctx, int transactionBlockId, int retryCountRegIndex,
                                         int committedFuncIndex, int abortedFuncIndex) {
        //If global tx enabled, it is managed via transaction coordinator. Otherwise it is managed locally without
        //any interaction with the transaction coordinator.
        boolean isGlobalTransactionEnabled = ctx.getGlobalTransactionEnabled();

        //Transaction is attempted three times by default to improve resiliency
        int retryCount = TransactionConstants.DEFAULT_RETRY_COUNT;
        if (retryCountRegIndex != -1) {
            retryCount = (int) ctx.workerLocal.longRegs[retryCountRegIndex];
            if (retryCount < 0) {
                ctx.setError(BLangVMErrors
                        .createError(ctx, BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_RETRY_COUNT)));
                handleError(ctx);
                return;
            }
        }

        //Register committed function handler if exists.
        if (committedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[committedFuncIndex];
            BFunctionPointer fpCommitted = new BFunctionPointer(funcRefCPEntry);
            TransactionResourceManager.getInstance().registerCommittedFunction(transactionBlockId, fpCommitted);
        }

        //Register aborted function handler if exists.
        if (abortedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[abortedFuncIndex];
            BFunctionPointer fpAborted = new BFunctionPointer(funcRefCPEntry);
            TransactionResourceManager.getInstance().registerAbortedFunction(transactionBlockId, fpAborted);
        }

        LocalTransactionInfo localTransactionInfo = ctx.getLocalTransactionInfo();
        if (localTransactionInfo == null) {
            String globalTransactionId;
            String protocol = null;
            String url = null;
            if (isGlobalTransactionEnabled) {
                BValue[] returns = TransactionUtils.notifyTransactionBegin(ctx, null, null, transactionBlockId,
                        TransactionConstants.DEFAULT_COORDINATION_TYPE);
                BStruct txDataStruct = (BStruct) returns[0];
                globalTransactionId = txDataStruct.getStringField(1);
                protocol = txDataStruct.getStringField(2);
                url = txDataStruct.getStringField(3);
            } else {
                globalTransactionId = UUID.randomUUID().toString().replaceAll("-", "");
            }
            localTransactionInfo = new LocalTransactionInfo(globalTransactionId, url, protocol);
            ctx.setLocalTransactionInfo(localTransactionInfo);
        } else {
            if (isGlobalTransactionEnabled) {
                TransactionUtils.notifyTransactionBegin(ctx, localTransactionInfo.getGlobalTransactionId(),
                        localTransactionInfo.getURL(), transactionBlockId, localTransactionInfo.getProtocol());
            }
        }
        localTransactionInfo.beginTransactionBlock(transactionBlockId, retryCount);
    }

    private static void retryTransaction(WorkerExecutionContext ctx, int transactionBlockId, int startOfAbortIP,
                                         int startOfNoThrowEndIP) {
        LocalTransactionInfo localTransactionInfo = ctx.getLocalTransactionInfo();
        if (!localTransactionInfo.isRetryPossible(ctx, transactionBlockId)) {
            if (ctx.getError() == null) {
                ctx.ip = startOfNoThrowEndIP;
            } else {
                if (BLangVMErrors.TRANSACTION_ERROR.equals(ctx.getError().getStringField(0))) {
                    ctx.ip = startOfNoThrowEndIP;
                } else {
                    ctx.ip = startOfAbortIP;
                }
            }
        }
        localTransactionInfo.incrementCurrentRetryCount(transactionBlockId);
    }

    private static void endTransaction(WorkerExecutionContext ctx, int transactionBlockId, int status) {
        LocalTransactionInfo localTransactionInfo = ctx.getLocalTransactionInfo();
        boolean isGlobalTransactionEnabled = ctx.getGlobalTransactionEnabled();
        boolean notifyCoordinator;
        try {
            //In success case no need to do anything as with the transaction end phase it will be committed.
            if (status == TransactionStatus.FAILED.value()) {
                notifyCoordinator = localTransactionInfo.onTransactionFailed(ctx, transactionBlockId);
                if (notifyCoordinator) {
                    if (isGlobalTransactionEnabled) {
                        TransactionUtils.notifyTransactionAbort(ctx, localTransactionInfo.getGlobalTransactionId(),
                                transactionBlockId);
                    } else {
                        TransactionResourceManager.getInstance()
                                .notifyAbort(localTransactionInfo.getGlobalTransactionId(), transactionBlockId, false);
                    }
                }
            } else if (status == TransactionStatus.ABORTED.value()) {
                if (isGlobalTransactionEnabled) {
                    TransactionUtils.notifyTransactionAbort(ctx, localTransactionInfo.getGlobalTransactionId(),
                            transactionBlockId);
                } else {
                    TransactionResourceManager.getInstance()
                            .notifyAbort(localTransactionInfo.getGlobalTransactionId(), transactionBlockId, false);
                }
            } else if (status == TransactionStatus.SUCCESS.value()) {
                //We dont' need to notify the coordinator in this case. If it does not receive abort from the tx
                //it will commit at the end message
                if (!isGlobalTransactionEnabled) {
                    TransactionResourceManager.getInstance()
                            .prepare(localTransactionInfo.getGlobalTransactionId(), transactionBlockId);
                    TransactionResourceManager.getInstance()
                            .notifyCommit(localTransactionInfo.getGlobalTransactionId(), transactionBlockId);
                }
            } else if (status == TransactionStatus.END.value()) { //status = 1 Transaction end
                boolean isOuterTx = localTransactionInfo.onTransactionEnd(transactionBlockId);
                if (isGlobalTransactionEnabled) {
                    TransactionUtils.notifyTransactionEnd(ctx, localTransactionInfo.getGlobalTransactionId(),
                            transactionBlockId);
                }
                if (isOuterTx) {
                    BLangVMUtils.removeTransactionInfo(ctx);
                }
            }
        } catch (Throwable e) {
            ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
            handleError(ctx);
        }
    }

    private static WorkerExecutionContext invokeVirtualFunction(WorkerExecutionContext ctx, int receiver,
                                                                FunctionInfo virtualFuncInfo, int[] argRegs,
                                                                int[] retRegs, int flags) {
        BStruct structVal = (BStruct) ctx.workerLocal.refRegs[receiver];
        if (structVal == null) {
            ctx.setError(BLangVMErrors.createNullRefException(ctx));
            handleError(ctx);
            return null;
        }

        StructInfo structInfo = structVal.getType().structInfo;
        AttachedFunctionInfo attachedFuncInfo = structInfo.funcInfoEntries.get(virtualFuncInfo.getName());
        FunctionInfo concreteFuncInfo = attachedFuncInfo.functionInfo;
        return BLangFunctions.invokeCallable(concreteFuncInfo, ctx, argRegs, retRegs, false, flags);
    }

    @SuppressWarnings("rawtypes")
    private static void handleWorkerSend(WorkerExecutionContext ctx, WorkerDataChannelInfo workerDataChannelInfo,
                                         BType type, int reg) {
        BRefType val = extractValue(ctx.workerLocal, type, reg);
        WorkerDataChannel dataChannel = getWorkerChannel(ctx, workerDataChannelInfo.getChannelName());
        dataChannel.putData(val);
    }

    private static WorkerDataChannel getWorkerChannel(WorkerExecutionContext ctx, String name) {
        return ctx.respCtx.getWorkerDataChannel(name);
    }

    @SuppressWarnings("rawtypes")
    private static BRefType extractValue(WorkerData data, BType type, int reg) {
        BRefType result;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                result = new BInteger(data.longRegs[reg]);
                break;
            case TypeTags.FLOAT_TAG:
                result = new BFloat(data.doubleRegs[reg]);
                break;
            case TypeTags.STRING_TAG:
                result = new BString(data.stringRegs[reg]);
                break;
            case TypeTags.BOOLEAN_TAG:
                result = new BBoolean(data.intRegs[reg] > 0);
                break;
            case TypeTags.BLOB_TAG:
                result = new BBlob(data.byteRegs[reg]);
                break;
            default:
                result = data.refRegs[reg];
        }
        return result;
    }

    private static WorkerExecutionContext invokeForkJoin(WorkerExecutionContext ctx, InstructionFORKJOIN forkJoinIns) {
        ForkjoinInfo forkjoinInfo = forkJoinIns.forkJoinCPEntry.getForkjoinInfo();
        return BLangFunctions.invokeForkJoin(ctx, forkjoinInfo, forkJoinIns.joinBlockAddr, forkJoinIns.joinVarRegIndex,
                forkJoinIns.timeoutRegIndex, forkJoinIns.timeoutBlockAddr, forkJoinIns.timeoutVarRegIndex);
    }

    @SuppressWarnings("rawtypes")
    private static boolean handleWorkerReceive(WorkerExecutionContext ctx, WorkerDataChannelInfo workerDataChannelInfo,
                                               BType type, int reg) {
        BRefType passedInValue = getWorkerChannel(ctx, workerDataChannelInfo.getChannelName()).tryTakeData(ctx);
        if (passedInValue != null) {
            WorkerData currentFrame = ctx.workerLocal;
            copyArgValueForWorkerReceive(currentFrame, reg, type, passedInValue);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    public static void copyArgValueForWorkerReceive(WorkerData currentSF, int regIndex, BType paramType,
                                                     BRefType passedInValue) {
        switch (paramType.getTag()) {
            case TypeTags.INT_TAG:
                currentSF.longRegs[regIndex] = ((BInteger) passedInValue).intValue();
                break;
            case TypeTags.FLOAT_TAG:
                currentSF.doubleRegs[regIndex] = ((BFloat) passedInValue).floatValue();
                break;
            case TypeTags.STRING_TAG:
                currentSF.stringRegs[regIndex] = (passedInValue).stringValue();
                break;
            case TypeTags.BOOLEAN_TAG:
                currentSF.intRegs[regIndex] = (((BBoolean) passedInValue).booleanValue()) ? 1 : 0;
                break;
            case TypeTags.BLOB_TAG:
                currentSF.byteRegs[regIndex] = ((BBlob) passedInValue).blobValue();
                break;
            default:
                currentSF.refRegs[regIndex] = (BRefType) passedInValue;
        }
    }

    public static void copyValues(WorkerExecutionContext ctx, WorkerData parent, WorkerData workerSF) {
        CodeAttributeInfo codeInfo = ctx.parent.callableUnitInfo.getDefaultWorkerInfo().getCodeAttributeInfo();
        System.arraycopy(parent.longRegs, 0, workerSF.longRegs, 0, codeInfo.getMaxLongLocalVars());
        System.arraycopy(parent.doubleRegs, 0, workerSF.doubleRegs, 0, codeInfo.getMaxDoubleLocalVars());
        System.arraycopy(parent.intRegs, 0, workerSF.intRegs, 0, codeInfo.getMaxIntLocalVars());
        System.arraycopy(parent.stringRegs, 0, workerSF.stringRegs, 0, codeInfo.getMaxStringLocalVars());
        System.arraycopy(parent.byteRegs, 0, workerSF.byteRegs, 0, codeInfo.getMaxByteLocalVars());
        System.arraycopy(parent.refRegs, 0, workerSF.refRegs, 0, codeInfo.getMaxRefLocalVars());
    }


    public static void copyArgValues(WorkerData callerSF, WorkerData calleeSF, int[] argRegs, BType[] paramTypes) {
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
                    calleeSF.longRegs[++longRegIndex] = callerSF.longRegs[argReg];
                    break;
                case TypeTags.FLOAT_TAG:
                    calleeSF.doubleRegs[++doubleRegIndex] = callerSF.doubleRegs[argReg];
                    break;
                case TypeTags.STRING_TAG:
                    calleeSF.stringRegs[++stringRegIndex] = callerSF.stringRegs[argReg];
                    break;
                case TypeTags.BOOLEAN_TAG:
                    calleeSF.intRegs[++booleanRegIndex] = callerSF.intRegs[argReg];
                    break;
                case TypeTags.BLOB_TAG:
                    calleeSF.byteRegs[++blobRegIndex] = callerSF.byteRegs[argReg];
                    break;
                default:
                    calleeSF.refRegs[++refRegIndex] = callerSF.refRegs[argReg];
            }
        }
    }

    private static WorkerExecutionContext handleReturn(WorkerExecutionContext ctx) {
        BLangScheduler.workerDone(ctx);
        return ctx.respCtx.signal(new WorkerSignal(ctx, SignalType.RETURN, ctx.workerResult));
    }

    public static boolean isAssignable(BValue rhsValue, BType lhsType) {
        if (rhsValue == null) {
            return false;
        }
        
        if (lhsType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionCast(rhsValue, lhsType);
        }

        BType rhsType = rhsValue.getType();
        if (rhsType.equals(lhsType)) {
            return true;
        } else if (rhsType.getTag() == TypeTags.INT_TAG &&
                (lhsType.getTag() == TypeTags.JSON_TAG || lhsType.getTag() == TypeTags.FLOAT_TAG)) {
            return true;
        } else if (rhsType.getTag() == TypeTags.FLOAT_TAG && lhsType.getTag() == TypeTags.JSON_TAG) {
            return true;
        } else if (rhsType.getTag() == TypeTags.STRING_TAG && lhsType.getTag() == TypeTags.JSON_TAG) {
            return true;
        } else if (rhsType.getTag() == TypeTags.BOOLEAN_TAG && lhsType.getTag() == TypeTags.JSON_TAG) {
            return true;
        }

        // if lhs type is JSON
        if (getElementType(lhsType).getTag() == TypeTags.JSON_TAG &&
                getElementType(rhsType).getTag() == TypeTags.JSON_TAG) {
            return checkJSONCast(((BJSON) rhsValue).value(), rhsType, lhsType);
        }

        if (rhsType.getTag() == TypeTags.STRUCT_TAG && lhsType.getTag() == TypeTags.STRUCT_TAG) {
            return checkStructEquivalency((BStructType) rhsType, (BStructType) lhsType);
        }

        if (lhsType.getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        // Array casting
        if (lhsType.getTag() == TypeTags.ARRAY_TAG || rhsType.getTag() == TypeTags.ARRAY_TAG) {
            return checkArrayCast(rhsType, lhsType);
        }

        // Check MAP casting
        if (rhsType.getTag() == TypeTags.MAP_TAG && lhsType.getTag() == TypeTags.MAP_TAG) {
            return checkMapCast(rhsType, lhsType);
        }

        // This doesn't compare constraints as there is a requirement to be able to return raw table type and assign
        // it to a constrained table reference.
        if (rhsType.getTag() == TypeTags.TABLE_TAG && lhsType.getTag() == TypeTags.TABLE_TAG) {
            return true;
        }

        if (rhsType.getTag() == TypeTags.STREAM_TAG && lhsType.getTag() == TypeTags.STREAM_TAG) {
            return true;
        }

        if (rhsType.getTag() == TypeTags.FUNCTION_POINTER_TAG &&
                lhsType.getTag() == TypeTags.FUNCTION_POINTER_TAG) {
            return true;
        }
        
        if (rhsType.getTag() == TypeTags.TUPLE_TAG && lhsType.getTag() == TypeTags.TUPLE_TAG) {
            return checkTupleCast(rhsValue, lhsType);
        }
        
        return false;
    }
    
    private static boolean checkUnionCast(BValue rhsValue, BType lhsType) {
        BUnionType unionType = (BUnionType) lhsType;
        for (BType memberType : unionType.getMemberTypes()) {
            if (checkCast(rhsValue, memberType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkCast(BValue rhsValue, BType lhsType) {
        // Check union types
        if (lhsType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionCast(rhsValue, lhsType);
        }
        
        BType rhsType = BTypes.typeNull;
        if (rhsValue != null) {
            rhsType = rhsValue.getType();
        }

        if (rhsType.equals(lhsType)) {
            return true;
        }

        if (rhsType.getTag() == TypeTags.STRUCT_TAG && lhsType.getTag() == TypeTags.STRUCT_TAG) {
            return checkStructEquivalency((BStructType) rhsType, (BStructType) lhsType);
        }

        if (lhsType.getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        // Check JSON casting
        if (getElementType(rhsType).getTag() == TypeTags.JSON_TAG) {
            return checkJSONCast(((BJSON) rhsValue).value(), rhsType, lhsType);
        }

        // Array casting
        if (lhsType.getTag() == TypeTags.ARRAY_TAG || rhsType.getTag() == TypeTags.ARRAY_TAG) {
            return checkArrayCast(rhsType, lhsType);
        }

        // Check MAP casting
        if (rhsType.getTag() == TypeTags.MAP_TAG && lhsType.getTag() == TypeTags.MAP_TAG) {
            return checkMapCast(rhsType, lhsType);
        }
        
        // Check tuple casting
        if (rhsType.getTag() == TypeTags.TUPLE_TAG && lhsType.getTag() == TypeTags.TUPLE_TAG) {
            return checkTupleCast(rhsValue, lhsType);
        } 

        return false;
    }

    private static boolean checkMapCast(BType sourceType, BType targetType) {

        BMapType sourceMapType = (BMapType) sourceType;
        BMapType targetMapType = (BMapType) targetType;

        if (sourceMapType.equals(targetMapType)) {
            return true;
        }

        if (sourceMapType.getConstrainedType().getTag() == TypeTags.STRUCT_TAG
                && targetMapType.getConstrainedType().getTag() == TypeTags.STRUCT_TAG) {
            return checkStructEquivalency((BStructType) sourceMapType.getConstrainedType(),
                    (BStructType) targetMapType.getConstrainedType());
        }

        return false;
    }

    private static boolean checkArrayCast(BType sourceType, BType targetType) {
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
    
    private static boolean checkTupleCast(BValue sourceValue, BType targetType) {
        BRefValueArray source = (BRefValueArray) sourceValue;
        BTupleType target = (BTupleType) targetType;
        List<BType> targetTupleTypes = target.getTupleTypes();
        if (source.size() != targetTupleTypes.size()) {
            return false;
        }
        for (int i = 0; i < source.size(); i++) {
            if (!checkCast(source.getBValue(i), targetTupleTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static BType getElementType(BType type) {
        if (type.getTag() != TypeTags.ARRAY_TAG) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    public static boolean checkStructEquivalency(BStructType rhsType, BStructType lhsType) {
        // Both structs should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Flags.isFlagOn(lhsType.flags ^ rhsType.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both structs are private, they should be in the same package.
        if (!Flags.isFlagOn(lhsType.flags, Flags.PUBLIC) &&
                !rhsType.getPackagePath().equals(lhsType.getPackagePath())) {
            return false;
        }

        // Adjust the number of the attached functions of the lhs struct based on
        //  the availability of the initializer function.
        int lhsAttachedFunctionCount = lhsType.initializer != null ?
                lhsType.getAttachedFunctions().length - 1 :
                lhsType.getAttachedFunctions().length;

        if (lhsType.getStructFields().length > rhsType.getStructFields().length ||
                lhsAttachedFunctionCount > rhsType.getAttachedFunctions().length) {
            return false;
        }

        return !Flags.isFlagOn(lhsType.flags, Flags.PUBLIC) &&
                rhsType.getPackagePath().equals(lhsType.getPackagePath()) ?
                checkEquivalencyOfTwoPrivateStructs(lhsType, rhsType) :
                checkEquivalencyOfPublicStructs(lhsType, rhsType);
    }

    private static boolean checkEquivalencyOfTwoPrivateStructs(BStructType lhsType, BStructType rhsType) {
        for (int fieldCounter = 0; fieldCounter < lhsType.getStructFields().length; fieldCounter++) {
            BStructType.StructField lhsField = lhsType.getStructFields()[fieldCounter];
            BStructType.StructField rhsField = rhsType.getStructFields()[fieldCounter];
            if (lhsField.fieldName.equals(rhsField.fieldName) &&
                    isSameType(rhsField.fieldType, lhsField.fieldType)) {
                continue;
            }
            return false;
        }

        BStructType.AttachedFunction[] lhsFuncs = lhsType.getAttachedFunctions();
        BStructType.AttachedFunction[] rhsFuncs = rhsType.getAttachedFunctions();
        for (BStructType.AttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsType.initializer || lhsFunc == lhsType.defaultsValuesInitFunc) {
                continue;
            }

            BStructType.AttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc);
            if (rhsFunc == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkEquivalencyOfPublicStructs(BStructType lhsType, BStructType rhsType) {
        int fieldCounter = 0;
        for (; fieldCounter < lhsType.getStructFields().length; fieldCounter++) {
            // Return false if either field is private
            BStructType.StructField lhsField = lhsType.getStructFields()[fieldCounter];
            BStructType.StructField rhsField = rhsType.getStructFields()[fieldCounter];
            if (!Flags.isFlagOn(lhsField.flags, Flags.PUBLIC) ||
                    !Flags.isFlagOn(rhsField.flags, Flags.PUBLIC)) {
                return false;
            }

            if (lhsField.fieldName.equals(rhsField.fieldName) &&
                    isSameType(rhsField.fieldType, lhsField.fieldType)) {
                continue;
            }
            return false;
        }

        // Check the rest of the fields in RHS type
        for (; fieldCounter < rhsType.getStructFields().length; fieldCounter++) {
            if (!Flags.isFlagOn(rhsType.getStructFields()[fieldCounter].flags, Flags.PUBLIC)) {
                return false;
            }
        }

        BStructType.AttachedFunction[] lhsFuncs = lhsType.getAttachedFunctions();
        BStructType.AttachedFunction[] rhsFuncs = rhsType.getAttachedFunctions();
        for (BStructType.AttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsType.initializer || lhsFunc == lhsType.defaultsValuesInitFunc) {
                continue;
            }

            if (!Flags.isFlagOn(lhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }

            BStructType.AttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc);
            if (rhsFunc == null || !Flags.isFlagOn(rhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkFunctionTypeEquality(BFunctionType source, BFunctionType target) {
        if (source.paramTypes.length != target.paramTypes.length ||
                source.retParamTypes.length != target.retParamTypes.length) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.length; i++) {
            if (!isSameType(source.paramTypes[i], target.paramTypes[i])) {
                return false;
            }
        }

        for (int i = 0; i < source.retParamTypes.length; i++) {
            if (!isSameType(source.retParamTypes[i], target.retParamTypes[i])) {
                return false;
            }
        }

        return true;
    }

    private static BStructType.AttachedFunction getMatchingInvokableType(BStructType.AttachedFunction[] rhsFuncs,
                                                                         BStructType.AttachedFunction lhsFunc) {
        return Arrays.stream(rhsFuncs)
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> checkFunctionTypeEquality(lhsFunc.type, rhsFunc.type))
                .findFirst()
                .orElse(null);
    }


    private static boolean isSameType(BType rhsType, BType lhsType) {
        // First check whether both references points to the same object.
        if (rhsType == lhsType || rhsType.equals(lhsType)) {
            return true;
        }

        if (rhsType.getTag() == lhsType.getTag() && rhsType.getTag() == TypeTags.ARRAY_TAG) {
            return checkArrayEquivalent(rhsType, lhsType);
        }

        // TODO Support function types, json/map constrained types etc.
        if (rhsType.getTag() == TypeTags.MAP_TAG && lhsType.getTag() == TypeTags.MAP_TAG) {
            return lhsType.equals(rhsType);
        }

        return false;
    }

    private static boolean checkArrayEquivalent(BType actualType, BType expType) {
        if (expType.getTag() == TypeTags.ARRAY_TAG && actualType.getTag() == TypeTags.ARRAY_TAG) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) expType;
            BArrayType rhsArrayType = (BArrayType) actualType;
            return checkArrayEquivalent(lhrArrayType.getElementType(), rhsArrayType.getElementType());
        }
        // Now one or both types are not array types and they have to be equal
        if (expType == actualType) {
            return true;
        }
        return false;
    }

    private static void castJSONToInt(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError(ctx);
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeInt, e.getMessage());
            ctx.setError(BLangVMErrors.createError(ctx, errorMsg));
            handleError(ctx);
            return;
        }

        if (jsonNode.isLong()) {
            sf.longRegs[j] = jsonNode.longValue();
            return;
        }

        sf.longRegs[j] = 0;
//        handleTypeCastError(ctx, sf, j, JSONUtils.getTypeName(jsonNode), TypeConstants.INT_TNAME);
    }

    private static void castJSONToFloat(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError(ctx);
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeFloat, e.getMessage());
            ctx.setError(BLangVMErrors.createError(ctx, errorMsg));
            handleError(ctx);
            return;
        }

        if (jsonNode.isDouble()) {
            sf.doubleRegs[j] = jsonNode.doubleValue();
            return;
        }

        sf.doubleRegs[j] = 0;
//        handleTypeCastError(ctx, sf, j, JSONUtils.getTypeName(jsonNode), TypeConstants.FLOAT_TNAME);
    }

    private static void castJSONToString(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError(ctx);
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            sf.stringRegs[j] = BLangConstants.STRING_EMPTY_VALUE;
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeString, e.getMessage());
            ctx.setError(BLangVMErrors.createError(ctx, errorMsg));
            handleError(ctx);
            return;
        }

        if (jsonNode.isString()) {
            sf.stringRegs[j] = jsonNode.stringValue();
            return;
        }

        sf.stringRegs[j] = STRING_NULL_VALUE;
//        handleTypeCastError(ctx, sf, j, JSONUtils.getTypeName(jsonNode), TypeConstants.STRING_TNAME);
    }

    private static void castJSONToBoolean(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BJSON jsonValue = (BJSON) sf.refRegs[i];
        if (jsonValue == null) {
            handleNullRefError(ctx);
            return;
        }

        JsonNode jsonNode;
        try {
            jsonNode = jsonValue.value();
        } catch (BallerinaException e) {
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeBoolean, e.getMessage());
            ctx.setError(BLangVMErrors.createError(ctx, errorMsg));
            handleError(ctx);
            return;
        }

        if (jsonNode.isBoolean()) {
            sf.intRegs[j] = jsonNode.booleanValue() ? 1 : 0;
            return;
        }

        // Reset the value in the case of an error;
        sf.intRegs[j] = 0;
//        handleTypeCastError(ctx, sf, j, JSONUtils.getTypeName(jsonNode), TypeConstants.BOOLEAN_TNAME);
    }

    private static boolean checkJSONEquivalency(JsonNode json, BJSONType sourceType, BJSONType targetType) {
        BStructType sourceConstrainedType = (BStructType) sourceType.getConstrainedType();
        BStructType targetConstrainedType = (BStructType) targetType.getConstrainedType();

        // Casting to an unconstrained JSON
        if (targetConstrainedType == null) {
            // ideally we should't reach here. This is checked from typeChecker
            return true;
        }

        // Casting from constrained JSON to constrained JSON
        if (sourceConstrainedType != null) {
            if (sourceConstrainedType.equals(targetConstrainedType)) {
                return true;
            }

            return checkStructEquivalency(sourceConstrainedType, targetConstrainedType);
        }

        // Casting from unconstrained JSON to constrained JSON
        BStructType.StructField[] tFields = targetConstrainedType.getStructFields();
        for (int i = 0; i < tFields.length; i++) {
            String fieldName = tFields[i].getFieldName();
            if (!json.has(fieldName)) {
                return false;
            }

            if (!checkJSONCast(json.get(fieldName), sourceType, tFields[i].getFieldType())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check the compatibility of casting a JSON to a target type.
     *
     * @param json       JSON to cast
     * @param sourceType Type of the source JSON
     * @param targetType Target type
     * @return Runtime compatibility for casting
     */
    private static boolean checkJSONCast(JsonNode json, BType sourceType, BType targetType) {
        switch (targetType.getTag()) {
            case TypeTags.STRING_TAG:
                return json.isString();
            case TypeTags.INT_TAG:
                return json.isLong();
            case TypeTags.FLOAT_TAG:
                return json.isDouble();
            case TypeTags.ARRAY_TAG:
                if (!json.isArray()) {
                    return false;
                }
                BArrayType arrayType = (BArrayType) targetType;
                for (int i = 0; i < json.size(); i++) {
                    // get the element type of source and json, and recursively check for json casting.
                    BType sourceElementType = sourceType.getTag() == TypeTags.ARRAY_TAG
                            ? ((BArrayType) sourceType).getElementType() : sourceType;
                    if (!checkJSONCast(json.get(i), sourceElementType, arrayType.getElementType())) {
                        return false;
                    }
                }
                return true;
            case TypeTags.JSON_TAG:
                // If target type is not constrained, any JSON is assignable.
                if (((BJSONType) targetType).getConstrainedType() == null) {
                    return true;
                }

                if (sourceType.getTag() != TypeTags.JSON_TAG) {
                    return false;
                }
                return checkJSONEquivalency(json, (BJSONType) sourceType, (BJSONType) targetType);
            default:
                return false;
        }
    }

    private static void convertStructToMap(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BStruct bStruct = (BStruct) sf.refRegs[i];
        if (bStruct == null) {
            handleNullRefError(ctx);
            return;
        }

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;

        BStructType.StructField[] structFields = (bStruct.getType()).getStructFields();
        BMap<String, BValue> map = BTypes.typeMap.getEmptyValue();
        for (BStructType.StructField structField : structFields) {
            String key = structField.getFieldName();
            BType fieldType = structField.getFieldType();
            switch (fieldType.getTag()) {
                case TypeTags.INT_TAG:
                    map.put(key, new BInteger(bStruct.getIntField(++longRegIndex)));
                    break;
                case TypeTags.FLOAT_TAG:
                    map.put(key, new BFloat(bStruct.getFloatField(++doubleRegIndex)));
                    break;
                case TypeTags.STRING_TAG:
                    map.put(key, new BString(bStruct.getStringField(++stringRegIndex)));
                    break;
                case TypeTags.BOOLEAN_TAG:
                    map.put(key, new BBoolean(bStruct.getBooleanField(++booleanRegIndex) == 1));
                    break;
                case TypeTags.BLOB_TAG:
                    map.put(key, new BBlob(bStruct.getBlobField(++blobRegIndex)));
                    break;
                default:
                    BValue value = bStruct.getRefField(++refRegIndex);
                    map.put(key, value == null ? null : value.copy());
            }
        }

        sf.refRegs[j] = map;
    }

    private static void convertStructToJSON(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BJSONType targetType = (BJSONType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();

        BStruct bStruct = (BStruct) sf.refRegs[i];
        if (bStruct == null) {
            handleNullRefError(ctx);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.convertStructToJSON(bStruct, targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + bStruct.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }
    
    private static void convertArrayToJSON(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BNewArray bArray = (BNewArray) sf.refRegs[i];
        if (bArray == null) {
            handleNullRefError(ctx);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.convertArrayToJSON(bArray);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + bArray.getType() + "' to type '" + BTypes.typeJSON + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void convertMapToJSON(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BJSONType targetType = (BJSONType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();

        BMap<String, ?> bMap = (BMap<String, ?>) sf.refRegs[i];
        if (bMap == null) {
            handleNullRefError(ctx);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.convertMapToJSON((BMap<String, BValue>) bMap, targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + bMap.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }
    
    private static void convertJSONToMap(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BMapType targetType = (BMapType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();

        BJSON json = (BJSON) sf.refRegs[i];
        if (json == null) {
            handleNullRefError(ctx);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.jsonNodeToBMap(json.value(), targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + json.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void convertMapToStruct(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
        BMap<String, BValue> bMap = (BMap<String, BValue>) sf.refRegs[i];
        if (bMap == null) {
            handleNullRefError(ctx);
            return;
        }

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;
        BStructType structType = (BStructType) typeRefCPEntry.getType();
        BStruct bStruct = new BStruct(structType);
        StructInfo structInfo = ctx.callableUnitInfo.getPackageInfo().getStructInfo(structType.getName());

        Set<String> keys = bMap.keySet();
        for (StructFieldInfo fieldInfo : structInfo.getFieldInfoEntries()) {
            String key = fieldInfo.getName();
            BType fieldType = fieldInfo.getFieldType();
            BValue mapVal = null;
            try {
                boolean containsField = keys.contains(key);
                DefaultValueAttributeInfo defaultValAttrInfo = null;
                if (containsField) {
                    mapVal = bMap.get(key);
                    if (mapVal == null && BTypes.isValueType(fieldType)) {
                        throw BLangExceptionHelper.getRuntimeException(
                                RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key, fieldType, null);
                    }

                    if (mapVal != null && !checkCast(mapVal, fieldType)) {
                        throw BLangExceptionHelper.getRuntimeException(
                                RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key, fieldType, mapVal.getType());
                    }
                } else {
                    defaultValAttrInfo = (DefaultValueAttributeInfo) getAttributeInfo(fieldInfo,
                            AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE);
                }

                switch (fieldType.getTag()) {
                    case TypeTags.INT_TAG:
                        longRegIndex++;
                        if (containsField) {
                            bStruct.setIntField(longRegIndex, ((BInteger) mapVal).intValue());
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setIntField(longRegIndex, defaultValAttrInfo.getDefaultValue().getIntValue());
                        }
                        break;
                    case TypeTags.FLOAT_TAG:
                        doubleRegIndex++;
                        if (containsField) {
                            bStruct.setFloatField(doubleRegIndex, ((BFloat) mapVal).floatValue());
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setFloatField(doubleRegIndex, defaultValAttrInfo.getDefaultValue().getFloatValue());
                        }
                        break;
                    case TypeTags.STRING_TAG:
                        stringRegIndex++;
                        if (containsField) {
                            bStruct.setStringField(stringRegIndex, ((BString) mapVal).stringValue());
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setStringField(stringRegIndex,
                                    defaultValAttrInfo.getDefaultValue().getStringValue());
                        }
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        booleanRegIndex++;
                        if (containsField) {
                            bStruct.setBooleanField(booleanRegIndex, ((BBoolean) mapVal).booleanValue() ? 1 : 0);
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setBooleanField(booleanRegIndex,
                                    defaultValAttrInfo.getDefaultValue().getBooleanValue() ? 1 : 0);
                        }
                        break;
                    case TypeTags.BLOB_TAG:
                        blobRegIndex++;
                        if (containsField && mapVal != null) {
                            bStruct.setBlobField(blobRegIndex, ((BBlob) mapVal).blobValue());
                        }
                        break;
                    default:
                        bStruct.setRefField(++refRegIndex, (BRefType) mapVal);
                }
            } catch (BallerinaException e) {
                sf.refRegs[j] = null;
                String errorMsg = "cannot convert '" + bMap.getType() + "' to type '" + structType + ": " +
                        e.getMessage();
                handleTypeConversionError(ctx, sf, j, errorMsg);
                return;
            }
        }

        sf.refRegs[j] = bStruct;
    }

    private static void convertJSONToStruct(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
        BJSON bjson = (BJSON) sf.refRegs[i];
        if (bjson == null) {
            handleNullRefError(ctx);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.convertJSONToStruct(bjson, (BStructType) typeRefCPEntry.getType());
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + TypeConstants.JSON_TNAME + "' to type '" +
                    typeRefCPEntry.getType() + "': " + e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static void handleNullRefError(WorkerExecutionContext ctx) {
        ctx.setError(BLangVMErrors.createNullRefException(ctx));
        handleError(ctx);
    }

    public static void handleError(WorkerExecutionContext ctx) {
        int ip = ctx.ip;
        ip--;
        ErrorTableEntry match = ErrorTableEntry.getMatch(ctx.callableUnitInfo.getPackageInfo(), ip,
                ctx.getError());
        if (match != null) {
            ctx.ip = match.getIpTarget();
        } else {
            BLangScheduler.workerExcepted(ctx);
            throw new HandleErrorException(
                    ctx.respCtx.signal(new WorkerSignal(ctx, SignalType.ERROR, ctx.workerResult)));
        }
    }

    private static AttributeInfo getAttributeInfo(AttributeInfoPool attrInfoPool, AttributeInfo.Kind attrInfoKind) {
        for (AttributeInfo attributeInfo : attrInfoPool.getAttributeInfoEntries()) {
            if (attributeInfo.getKind() == attrInfoKind) {
                return attributeInfo;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static void calculateLength(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
        int typeTag = typeRefCPEntry.getType().getTag();
        if (typeTag == TypeTags.STRING_TAG) {
            String value = sf.stringRegs[i];
            if (value == null) {
                handleNullRefError(ctx);
            } else {
                sf.longRegs[j] = value.length();
            }
            return;
        } else if (typeTag == TypeTags.BLOB_TAG) {
            // Here it is assumed null is not supported for blob type
            sf.longRegs[j] = sf.byteRegs[i].length;
            return;
        }

        BValue entity = sf.refRegs[i];
        if (entity == null) {
            handleNullRefError(ctx);
            return;
        }

        if (typeTag == TypeTags.XML_TAG) {
            sf.longRegs[j] = ((BXML) entity).length();
            return;
        } else if (entity instanceof BJSON) {
            if (JSONUtils.isJSONArray((BJSON) entity)) {
                sf.longRegs[j] = JSONUtils.getJSONArrayLength((BJSON) sf.refRegs[i]);
            } else {
                sf.longRegs[j] = -1;
            }
            return;
        } else if (typeTag == TypeTags.MAP_TAG) {
            sf.longRegs[j] = ((BMap) entity).size();
            return;
        }

        BNewArray newArray = (BNewArray) entity;
        sf.longRegs[j] = newArray.size();
        return;
    }

    private static WorkerExecutionContext execAwait(WorkerExecutionContext ctx, int[] operands) {
        int futureReg = operands[0];
        int retValReg = operands[1];
        BFuture future = (BFuture) ctx.workerLocal.refRegs[futureReg];
        WorkerResponseContext respCtx = future.value();
        if (retValReg != -1) {
            return respCtx.joinTargetContextInfo(ctx, new int[]{retValReg});
        } else {
            return respCtx.joinTargetContextInfo(ctx, new int[0]);
        }
    }

    /**
     * This is used to propagate the results of {@link CPU#handleError(WorkerExecutionContext)} to the
     * main CPU instruction loop.
     */
    public static class HandleErrorException extends BallerinaException {

        private static final long serialVersionUID = 1L;

        public WorkerExecutionContext ctx;

        public HandleErrorException(WorkerExecutionContext ctx) {
            this.ctx = ctx;
        }

    }

}
