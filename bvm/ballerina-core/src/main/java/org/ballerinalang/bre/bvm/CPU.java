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
import org.ballerinalang.channels.ChannelManager;
import org.ballerinalang.channels.ChannelRegistry;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BAttachedFunction;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BFutureType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.Flags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.ListUtils;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BCollection;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BFuture;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BIntRange;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.model.values.BXMLQName;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.TransactionStatus;
import org.ballerinalang.util.codegen.ErrorTableEntry;
import org.ballerinalang.util.codegen.ForkjoinInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.Instruction.InstructionCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionCHNReceive;
import org.ballerinalang.util.codegen.Instruction.InstructionCHNSend;
import org.ballerinalang.util.codegen.Instruction.InstructionFORKJOIN;
import org.ballerinalang.util.codegen.Instruction.InstructionIteratorNext;
import org.ballerinalang.util.codegen.Instruction.InstructionLock;
import org.ballerinalang.util.codegen.Instruction.InstructionVCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionWRKSendReceive;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.ObjectTypeInfo;
import org.ballerinalang.util.codegen.StructFieldInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.codegen.TypeDefInfo;
import org.ballerinalang.util.codegen.WorkerDataChannelInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.BlobCPEntry;
import org.ballerinalang.util.codegen.cpentries.ByteCPEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.program.CompensationTable;
import org.ballerinalang.util.transactions.LocalTransactionInfo;
import org.ballerinalang.util.transactions.TransactionConstants;
import org.ballerinalang.util.transactions.TransactionResourceManager;
import org.ballerinalang.util.transactions.TransactionUtils;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.ballerinalang.runtime.Constants.STATE_ID;
import static org.ballerinalang.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.util.BLangConstants.BBYTE_MIN_VALUE;
import static org.ballerinalang.util.BLangConstants.STRING_NULL_VALUE;

/**
 * This class executes Ballerina instruction codes by acting as a CPU.
 *
 * @since 0.965.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CPU {

    public static void traceCode(Instruction[] code) {
        PrintStream printStream = System.out;
        for (int i = 0; i < code.length; i++) {
            printStream.println(i + ": " + code[i].toString());
        }
    }

    private static WorkerExecutionContext handleHalt(WorkerExecutionContext ctx) {
        BLangScheduler.workerDone(ctx);
        return ctx.respCtx.signal(new WorkerSignal(ctx, SignalType.HALT, ctx.workerResult));
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

    private static void tryExec(WorkerExecutionContext ctx) {
        BLangScheduler.workerRunning(ctx);

        int i, j, k, l;
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
                    case InstructionCodes.DCONST:
                        cpIndex = operands[0];
                        i = operands[1];
                        String decimalVal = ((UTF8CPEntry) ctx.constPool[cpIndex]).getValue();
                        sf.refRegs[i] = new BDecimal(new BigDecimal(decimalVal, MathContext.DECIMAL128));
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
                    case InstructionCodes.BICONST:
                        cpIndex = operands[0];
                        i = operands[1];
                        sf.intRegs[i] = ((ByteCPEntry) ctx.constPool[cpIndex]).getValue();
                        break;
                    case InstructionCodes.BACONST:
                        cpIndex = operands[0];
                        i = operands[1];
                        sf.refRegs[i] = new BByteArray(((BlobCPEntry) ctx.constPool[cpIndex]).getValue());
                        break;

                    case InstructionCodes.IMOVE:
                    case InstructionCodes.FMOVE:
                    case InstructionCodes.SMOVE:
                    case InstructionCodes.BMOVE:
                    case InstructionCodes.RMOVE:
                    case InstructionCodes.IALOAD:
                    case InstructionCodes.BIALOAD:
                    case InstructionCodes.FALOAD:
                    case InstructionCodes.SALOAD:
                    case InstructionCodes.BALOAD:
                    case InstructionCodes.RALOAD:
                    case InstructionCodes.JSONALOAD:
                    case InstructionCodes.IGLOAD:
                    case InstructionCodes.FGLOAD:
                    case InstructionCodes.SGLOAD:
                    case InstructionCodes.BGLOAD:
                    case InstructionCodes.RGLOAD:
                    case InstructionCodes.MAPLOAD:
                    case InstructionCodes.JSONLOAD:
                        execLoadOpcodes(ctx, sf, opcode, operands);
                        break;

                    case InstructionCodes.IASTORE:
                    case InstructionCodes.BIASTORE:
                    case InstructionCodes.FASTORE:
                    case InstructionCodes.SASTORE:
                    case InstructionCodes.BASTORE:
                    case InstructionCodes.RASTORE:
                    case InstructionCodes.JSONASTORE:
                    case InstructionCodes.IGSTORE:
                    case InstructionCodes.FGSTORE:
                    case InstructionCodes.SGSTORE:
                    case InstructionCodes.BGSTORE:
                    case InstructionCodes.RGSTORE:
                    case InstructionCodes.MAPSTORE:
                    case InstructionCodes.JSONSTORE:
                        execStoreOpcodes(ctx, sf, opcode, operands);
                        break;

                    case InstructionCodes.IADD:
                    case InstructionCodes.FADD:
                    case InstructionCodes.SADD:
                    case InstructionCodes.DADD:
                    case InstructionCodes.XMLADD:
                    case InstructionCodes.ISUB:
                    case InstructionCodes.FSUB:
                    case InstructionCodes.DSUB:
                    case InstructionCodes.IMUL:
                    case InstructionCodes.FMUL:
                    case InstructionCodes.DMUL:
                    case InstructionCodes.IDIV:
                    case InstructionCodes.FDIV:
                    case InstructionCodes.DDIV:
                    case InstructionCodes.IMOD:
                    case InstructionCodes.FMOD:
                    case InstructionCodes.DMOD:
                    case InstructionCodes.INEG:
                    case InstructionCodes.FNEG:
                    case InstructionCodes.DNEG:
                    case InstructionCodes.BNOT:
                    case InstructionCodes.IEQ:
                    case InstructionCodes.FEQ:
                    case InstructionCodes.SEQ:
                    case InstructionCodes.BEQ:
                    case InstructionCodes.DEQ:
                    case InstructionCodes.REQ:
                    case InstructionCodes.REF_EQ:
                    case InstructionCodes.TEQ:
                    case InstructionCodes.INE:
                    case InstructionCodes.FNE:
                    case InstructionCodes.SNE:
                    case InstructionCodes.BNE:
                    case InstructionCodes.DNE:
                    case InstructionCodes.RNE:
                    case InstructionCodes.REF_NEQ:
                    case InstructionCodes.TNE:
                    case InstructionCodes.IAND:
                    case InstructionCodes.BIAND:
                    case InstructionCodes.IOR:
                    case InstructionCodes.BIOR:
                    case InstructionCodes.IXOR:
                    case InstructionCodes.BIXOR:
                    case InstructionCodes.BILSHIFT:
                    case InstructionCodes.BIRSHIFT:
                    case InstructionCodes.IRSHIFT:
                    case InstructionCodes.ILSHIFT:
                    case InstructionCodes.IURSHIFT:
                    case InstructionCodes.TYPE_TEST:
                    case InstructionCodes.IS_LIKE:
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
                    case InstructionCodes.HALT:
                        ctx = handleHalt(ctx);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.IGT:
                    case InstructionCodes.FGT:
                    case InstructionCodes.DGT:
                    case InstructionCodes.IGE:
                    case InstructionCodes.FGE:
                    case InstructionCodes.DGE:
                    case InstructionCodes.ILT:
                    case InstructionCodes.FLT:
                    case InstructionCodes.DLT:
                    case InstructionCodes.ILE:
                    case InstructionCodes.FLE:
                    case InstructionCodes.DLE:
                    case InstructionCodes.REQ_NULL:
                    case InstructionCodes.RNE_NULL:
                    case InstructionCodes.BR_TRUE:
                    case InstructionCodes.BR_FALSE:
                    case InstructionCodes.GOTO:
                        execCmpAndBranchOpcodes(ctx, sf, opcode, operands);
                        break;
                    case InstructionCodes.INT_RANGE:
                        execIntegerRangeOpcodes(sf, operands);
                        break;
                    case InstructionCodes.TR_RETRY:
                        i = operands[0];
                        j = operands[1];
                        k = operands[2];
                        retryTransaction(ctx, i, j, k);
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
                    case InstructionCodes.TR_BEGIN:
                        i = operands[0];
                        j = operands[1];
                        k = operands[2];
                        l = operands[3];
                        beginTransaction(ctx, i, j, k, l);
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
                    case InstructionCodes.CHNRECEIVE:
                        InstructionCHNReceive chnReceiveIns = (InstructionCHNReceive) instruction;
                        if (!handleCHNReceive(ctx, chnReceiveIns.channelName, chnReceiveIns.receiverType,
                                chnReceiveIns.receiverReg, chnReceiveIns.keyType, chnReceiveIns.keyReg)) {
                            return;
                        }
                        break;
                    case InstructionCodes.CHNSEND:
                        InstructionCHNSend chnSendIns = (InstructionCHNSend) instruction;
                        handleCHNSend(ctx, chnSendIns.channelName, chnSendIns.dataType,
                                chnSendIns.dataReg, chnSendIns.keyType, chnSendIns.keyReg);
                        break;
                    case InstructionCodes.FORKJOIN:
                        InstructionFORKJOIN forkJoinIns = (InstructionFORKJOIN) instruction;
                        ctx = invokeForkJoin(ctx, forkJoinIns);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.PANIC:
                        i = operands[0];
                        if (i >= 0) {
                            BError error = (BError) sf.refRegs[i];
                            if (error == null) {
                                handleNullRefError(ctx);
                                break;
                            }
                            BLangVMErrors.attachStackFrame(error, ctx);
                            ctx.setError(error);
                        }
                        handleError(ctx);
                        break;
                    case InstructionCodes.ERROR:
                        createNewError(operands, ctx, sf);
                        break;
                    case InstructionCodes.REASON:
                    case InstructionCodes.DETAIL:
                        handleErrorBuiltinMethods(opcode, operands, sf);
                        break;
                    case InstructionCodes.IS_FROZEN:
                    case InstructionCodes.FREEZE:
                        handleFreezeBuiltinMethods(ctx, opcode, operands, sf);
                        break;
                    case InstructionCodes.STAMP:
                        handleStampBuildInMethod(ctx, operands, sf);
                        break;
                    case InstructionCodes.FPCALL:
                        i = operands[0];
                        if (sf.refRegs[i] == null) {
                            handleNullRefError(ctx);
                            break;
                        }
                        cpIndex = operands[1];
                        funcCallCPEntry = (FunctionCallCPEntry) ctx.constPool[cpIndex];
                        functionInfo = ((BFunctionPointer) sf.refRegs[i]).value();
                        ctx = invokeCallable(ctx, (BFunctionPointer) sf.refRegs[i], funcCallCPEntry, functionInfo, sf);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.FPLOAD:
                        i = operands[0];
                        j = operands[1];
                        k = operands[2];
                        funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[i];
                        typeEntry = (TypeRefCPEntry) ctx.constPool[k];
                        BFunctionPointer functionPointer = new BFunctionPointer(funcRefCPEntry.getFunctionInfo(),
                                typeEntry.getType());
                        sf.refRegs[j] = functionPointer;
                        findAndAddAdditionalVarRegIndexes(ctx, operands, functionPointer);
                        break;
                    case InstructionCodes.VFPLOAD:
                        i = operands[0];
                        j = operands[1];
                        k = operands[2];
                        int m = operands[5];
                        funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[i];
                        typeEntry = (TypeRefCPEntry) ctx.constPool[k];

                        BMap<String, BValue> structVal = (BMap<String, BValue>) ctx.workerLocal.refRegs[m];
                        if (structVal == null) {
                            handleNullRefError(ctx);
                            break;
                        }

                        StructureTypeInfo structInfo = (ObjectTypeInfo) ((BStructureType)
                                structVal.getType()).getTypeInfo();
                        FunctionInfo attachedFuncInfo = structInfo.funcInfoEntries
                                .get(funcRefCPEntry.getFunctionInfo().getName());

                        BFunctionPointer fPointer = new BFunctionPointer(attachedFuncInfo, typeEntry.getType());
                        sf.refRegs[j] = fPointer;
                        findAndAddAdditionalVarRegIndexes(ctx, operands, fPointer);
                        break;

                    case InstructionCodes.CLONE:
                        createClone(ctx, operands, sf);
                        break;

                    case InstructionCodes.I2ANY:
                    case InstructionCodes.BI2ANY:
                    case InstructionCodes.F2ANY:
                    case InstructionCodes.S2ANY:
                    case InstructionCodes.B2ANY:
                    case InstructionCodes.ANY2I:
                    case InstructionCodes.ANY2BI:
                    case InstructionCodes.ANY2F:
                    case InstructionCodes.ANY2S:
                    case InstructionCodes.ANY2B:
                    case InstructionCodes.ANY2D:
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
                    case InstructionCodes.CHECKCAST:
                    case InstructionCodes.IS_ASSIGNABLE:
                    case InstructionCodes.O2JSON:
                        execTypeCastOpcodes(ctx, sf, opcode, operands);
                        break;

                    case InstructionCodes.I2F:
                    case InstructionCodes.I2S:
                    case InstructionCodes.I2B:
                    case InstructionCodes.I2D:
                    case InstructionCodes.I2BI:
                    case InstructionCodes.BI2I:
                    case InstructionCodes.F2I:
                    case InstructionCodes.F2S:
                    case InstructionCodes.F2B:
                    case InstructionCodes.F2D:
                    case InstructionCodes.S2I:
                    case InstructionCodes.S2F:
                    case InstructionCodes.S2B:
                    case InstructionCodes.S2D:
                    case InstructionCodes.B2I:
                    case InstructionCodes.B2F:
                    case InstructionCodes.B2S:
                    case InstructionCodes.B2D:
                    case InstructionCodes.D2I:
                    case InstructionCodes.D2F:
                    case InstructionCodes.D2S:
                    case InstructionCodes.D2B:
                    case InstructionCodes.DT2XML:
                    case InstructionCodes.DT2JSON:
                    case InstructionCodes.T2MAP:
                    case InstructionCodes.T2JSON:
                    case InstructionCodes.MAP2JSON:
                    case InstructionCodes.JSON2MAP:
                    case InstructionCodes.MAP2T:
                    case InstructionCodes.JSON2T:
                    case InstructionCodes.XMLATTRS2MAP:
                    case InstructionCodes.XML2S:
                    case InstructionCodes.ANY2SCONV:
                        execTypeConversionOpcodes(ctx, sf, opcode, operands);
                        break;

                    case InstructionCodes.INEWARRAY:
                        i = operands[0];
                        j = operands[2];
                        sf.refRegs[i] = new BIntArray((int) sf.longRegs[j]);
                        break;
                    case InstructionCodes.BINEWARRAY:
                        i = operands[0];
                        j = operands[2];
                        sf.refRegs[i] = new BByteArray((int) sf.longRegs[j]);
                        break;
                    case InstructionCodes.FNEWARRAY:
                        i = operands[0];
                        j = operands[2];
                        sf.refRegs[i] = new BFloatArray((int) sf.longRegs[j]);
                        break;
                    case InstructionCodes.SNEWARRAY:
                        i = operands[0];
                        j = operands[2];
                        sf.refRegs[i] = new BStringArray((int) sf.longRegs[j]);
                        break;
                    case InstructionCodes.BNEWARRAY:
                        i = operands[0];
                        j = operands[2];
                        sf.refRegs[i] = new BBooleanArray((int) sf.longRegs[j]);
                        break;
                    case InstructionCodes.RNEWARRAY:
                        i = operands[0];
                        cpIndex = operands[1];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        sf.refRegs[i] = new BRefValueArray(typeRefCPEntry.getType());
                        break;
                    case InstructionCodes.NEWSTRUCT:
                        createNewStruct(ctx, operands, sf);
                        break;
                    case InstructionCodes.NEWMAP:
                        i = operands[0];
                        cpIndex = operands[1];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        sf.refRegs[i] = new BMap<String, BRefType>(typeRefCPEntry.getType());
                        break;
                    case InstructionCodes.NEWTABLE:
                        i = operands[0];
                        cpIndex = operands[1];
                        j = operands[2];
                        k = operands[3];
                        l = operands[4];
                        typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
                        BStringArray indexColumns = (BStringArray) sf.refRegs[j];
                        BStringArray keyColumns = (BStringArray) sf.refRegs[k];
                        BRefValueArray dataRows = (BRefValueArray) sf.refRegs[l];
                        sf.refRegs[i] = new BTable(typeRefCPEntry.getType(), indexColumns, keyColumns, dataRows);
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
                    case InstructionCodes.DRET:
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
                        if (!handleVariableLock(ctx, instructionLock.types,
                                instructionLock.pkgRefs, instructionLock.varRegs)) {
                            return;
                        }
                        break;
                    case InstructionCodes.UNLOCK:
                        InstructionLock instructionUnLock = (InstructionLock) instruction;
                        handleVariableUnlock(ctx, instructionUnLock.types,
                                instructionUnLock.pkgRefs, instructionUnLock.varRegs);
                        break;
                    case InstructionCodes.AWAIT:
                        ctx = execAwait(ctx, operands);
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.SCOPE_END:
                        Instruction.InstructionScopeEnd scopeEnd = (Instruction.InstructionScopeEnd) instruction;
                        i = operands[0];
                        k = operands[2];
                        funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[i];
                        typeEntry = (TypeRefCPEntry) ctx.constPool[k];
                        BFunctionPointer fp = new BFunctionPointer(funcRefCPEntry.getFunctionInfo(),
                                typeEntry.getType());
                        findAndAddAdditionalVarRegIndexes(ctx, operands, fp);
                        addToCompensationTable(scopeEnd, ctx, fp);
                        break;
                    case InstructionCodes.COMPENSATE:
                        Instruction.InstructionCompensate compIn = (Instruction.InstructionCompensate) instruction;
                        CompensationTable table = (CompensationTable) ctx.globalProps.get(Constants.COMPENSATION_TABLE);
                        int index = --table.index;
                        if (index >= 0 && (table.compensations.get(index).scope.equals(compIn
                                .scopeName) || compIn.childScopes.contains(table.compensations.get(index).scope))) {
                            CompensationTable.CompensationEntry entry = table.compensations.get(index);

                            int[] retRegsIndexes = {compIn.retRegIndex};
                            ctx = invokeCompensate(ctx, entry.fPointer, entry.functionInfo, sf, retRegsIndexes);
                        }
                        if (ctx == null) {
                            return;
                        }
                        break;
                    case InstructionCodes.LOOP_COMPENSATE:
                        i = operands[0];
                        CompensationTable compTable = (CompensationTable) ctx.globalProps
                                .get(Constants.COMPENSATION_TABLE);
                        if (compTable.index == 0) {
                            compTable.index = compTable.compensations.size();
                        } else {
                            ctx.ip = i;
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (HandleErrorException e) {
                throw e;
            } catch (Throwable e) {
                ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                handleError(ctx);
            }
        }
    }

    private static void createClone(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        BRefType<?> refRegVal = sf.refRegs[i];

        if (refRegVal == null) {
            return;
        }

        if (!checkIsLikeType(refRegVal, BTypes.typeAnydata)) {
            sf.refRegs[j] = BLangVMErrors.createError(ctx, BLangExceptionHelper
                    .getErrorMessage(RuntimeErrors.UNSUPPORTED_CLONE_OPERATION, refRegVal, refRegVal.getType()));
            return;
        }
        sf.refRegs[j] = (BRefType<?>) refRegVal.copy(new HashMap<>());
    }

    private static void createNewError(int[] operands, WorkerExecutionContext ctx, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];
        int l = operands[3];
        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[i];
        sf.refRegs[l] = new BError(typeRefCPEntry.getType(), sf.stringRegs[j], sf.refRegs[k]);
    }

    private static void handleErrorBuiltinMethods(int opcode, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];
        BError error = (BError) sf.refRegs[i];
        switch (opcode) {
            case InstructionCodes.REASON:
                sf.stringRegs[j] = error.getReason();
                break;
            case InstructionCodes.DETAIL:
                sf.refRegs[j] = error.getDetails();
                break;
        }
    }

    private static void handleFreezeBuiltinMethods(WorkerExecutionContext ctx, int opcode, int[] operands,
                                                   WorkerData sf) {
        int i = operands[0];
        int j = operands[1];
        BRefType value = sf.refRegs[i];
        switch (opcode) {
            case InstructionCodes.FREEZE:
                if (value == null) {
                    // assuming we reach here because the value is nil (()), the frozen value would also be nil.
                    sf.refRegs[j] = null;
                    break;
                }

                FreezeStatus freezeStatus = new FreezeStatus(FreezeStatus.State.MID_FREEZE);
                try {
                    value.attemptFreeze(freezeStatus);

                    // if freeze is successful, set the status as frozen and the value itself as the return value
                    freezeStatus.setFrozen();
                    sf.refRegs[j] = value;
                } catch (BLangFreezeException e) {
                    // if freeze is unsuccessful due to an invalid value, set the frozen status of the value and its
                    // constituents to false, and return an error
                    freezeStatus.setUnfrozen();
                    sf.refRegs[j] = BLangVMErrors.createError(ctx, e.getMessage());
                } catch (BallerinaException e) {
                    // if freeze is unsuccessful due to concurrent freeze attempts, set the frozen status of the value
                    // and its constituents to false, and panic
                    freezeStatus.setUnfrozen();
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.IS_FROZEN:
                sf.intRegs[j] = (value == null || value.isFrozen())  ? 1 : 0;
                break;
        }
    }

    private static void handleStampBuildInMethod(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {

        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BRefType<?> valueToBeStamped = sf.refRegs[i];
        BType stampType = ((TypeRefCPEntry) ctx.constPool[j]).getType();
        BType targetType;

        if (stampType.getTag() == TypeTags.UNION_TAG) {
            List<BType> memberTypes = new ArrayList<>(((BUnionType) stampType).getMemberTypes());
            targetType = new BUnionType(memberTypes);

            Predicate<BType> errorPredicate = e -> e.getTag() == TypeTags.ERROR_TAG;
            ((BUnionType) targetType).getMemberTypes().removeIf(errorPredicate);

            if (((BUnionType) targetType).getMemberTypes().size() == 1) {
                targetType = ((BUnionType) stampType).getMemberTypes().get(0);
            }
        } else {
            targetType = stampType;
        }

        if (!checkIsLikeType(valueToBeStamped, targetType)) {
            BError error = BLangVMErrors.createError(ctx,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_STAMP_OPERATION,
                            valueToBeStamped.getType(), targetType));
            sf.refRegs[k] = error;
            return;
        }

        try {
            if (valueToBeStamped != null) {
                valueToBeStamped.stamp(targetType);
            }
            sf.refRegs[k] = valueToBeStamped;
        } catch (BallerinaException e) {
            BError error = BLangVMErrors.createError(ctx,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_STAMP_OPERATION,
                            valueToBeStamped.getType(), targetType));
            sf.refRegs[k] = error;
        }
    }

    /**
     * Handle sending a message to a channel. If there is a worker already waiting to accept this message, it is
     * resumed.
     * @param ctx Current worker context
     * @param channelName Name os the channel to get the message
     * @param dataType Type od the message
     * @param dataReg Registry location of the message
     * @param keyType Type of message key
     * @param keyReg message key registry index
     */
    private static void handleCHNSend(WorkerExecutionContext ctx, String channelName, BType dataType, int dataReg,
            BType keyType, int keyReg) {
        BRefType keyVal = null;
        if (keyType != null) {
            keyVal = extractValue(ctx.workerLocal, keyType, keyReg);
        }
        BRefType dataVal = extractValue(ctx.workerLocal, dataType, dataReg);
        ChannelRegistry.PendingContext pendingCtx = ChannelManager.channelSenderAction(channelName, keyVal, dataVal,
                keyType, dataType);
        if (pendingCtx != null) {
            //inject the value to the ctx
            copyArgValueForWorkerReceive(pendingCtx.context.workerLocal, pendingCtx.regIndex, dataType, dataVal);
            if (pendingCtx.context.interruptible) {
                String stateId = (String) pendingCtx.context.globalProps.get(STATE_ID);
                PersistenceStore.persistState(new State(pendingCtx.context, stateId, pendingCtx.context.ip + 1));
            }
            BLangScheduler.resume(pendingCtx.context);
        }
        if (ctx.interruptible) {
            String stateId = (String) ctx.globalProps.get(STATE_ID);
            PersistenceStore.persistState(new State(ctx, stateId, ctx.ip + 1));
        }
    }

    /**
     * Handles message receiving using a channel.
     * If the expected message is already available, it is assigned to the receiver reg and returns true.
     * @param ctx Current worker context
     * @param channelName Name os the channel to get the message
     * @param receiverType Type of the expected message
     * @param receiverReg Registry index of the receiving message
     * @param keyType Type of message key
     * @param keyIndex message key registry index
     * @return true if a matching value is available
     */
    private static boolean handleCHNReceive(WorkerExecutionContext ctx, String channelName, BType receiverType,
            int receiverReg, BType keyType, int keyIndex) {
        BValue keyVal = null;
        if (keyType != null) {
            keyVal = extractValue(ctx.workerLocal, keyType, keyIndex);
        }
        BValue value = ChannelManager.channelReceiverAction(channelName, keyVal, keyType, ctx, receiverReg,
                receiverType);
        if (value != null) {
            copyArgValueForWorkerReceive(ctx.workerLocal, receiverReg, receiverType, (BRefType) value);
            if (ctx.interruptible) {
                String stateId = (String) ctx.globalProps.get(STATE_ID);
                PersistenceStore.persistState(new State(ctx, stateId, ctx.ip + 1));
            }
            return true;
        }

        return false;
    }

    private static WorkerExecutionContext invokeCallable(WorkerExecutionContext ctx, BFunctionPointer fp,
                                                         FunctionCallCPEntry funcCallCPEntry,
                                                         FunctionInfo functionInfo, WorkerData sf) {
        List<BClosure> closureVars = fp.getClosureVars();
        int[] argRegs = funcCallCPEntry.getArgRegs();
        if (closureVars.isEmpty()) {
            return BLangFunctions.invokeCallable(functionInfo, ctx, argRegs, funcCallCPEntry.getRetRegs(), false);
        }

        int[] newArgRegs = new int[argRegs.length + closureVars.size()];
        System.arraycopy(argRegs, 0, newArgRegs, closureVars.size(), argRegs.length);
        int argRegIndex = 0;

        int longIndex = expandLongRegs(sf, fp);
        int doubleIndex = expandDoubleRegs(sf, fp);
        int intIndex = expandIntRegs(sf, fp);
        int stringIndex = expandStringRegs(sf, fp);
        int refIndex = expandRefRegs(sf, fp);

        for (BClosure closure : closureVars) {
            switch (closure.getType().getTag()) {
                case TypeTags.INT_TAG: {
                    sf.longRegs[longIndex] = ((BInteger) closure.value()).intValue();
                    newArgRegs[argRegIndex++] = longIndex++;
                    break;
                }
                case TypeTags.BYTE_TAG: {
                    sf.intRegs[intIndex] = ((BByte) closure.value()).byteValue();
                    newArgRegs[argRegIndex++] = intIndex++;
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
                default:
                    sf.refRegs[refIndex] = ((BRefType<?>) closure.value());
                    newArgRegs[argRegIndex++] = refIndex++;
            }
        }

        return BLangFunctions.invokeCallable(functionInfo, ctx, newArgRegs, funcCallCPEntry.getRetRegs(), false);
    }

    private static WorkerExecutionContext invokeCompensate(WorkerExecutionContext ctx, BFunctionPointer fp,
                                                           FunctionInfo functionInfo, WorkerData sf, int[] retRegs) {
        List<BClosure> closureVars = fp.getClosureVars();
        if (closureVars.isEmpty()) {
            //compensate functions has no args apart from closure vars
            return BLangFunctions.invokeCallable(functionInfo, ctx, new int[0], retRegs, false);
        }

        int[] newArgRegs = new int[closureVars.size()];
        int argRegIndex = 0;

        int longIndex = expandLongRegs(sf, fp);
        int doubleIndex = expandDoubleRegs(sf, fp);
        int intIndex = expandIntRegs(sf, fp);
        int stringIndex = expandStringRegs(sf, fp);
        int refIndex = expandRefRegs(sf, fp);

        for (BClosure closure : closureVars) {
            switch (closure.getType().getTag()) {
                case TypeTags.INT_TAG: {
                    sf.longRegs[longIndex] = ((BInteger) closure.value()).intValue();
                    newArgRegs[argRegIndex++] = longIndex++;
                    break;
                }
                case TypeTags.BYTE_TAG: {
                    sf.intRegs[intIndex] = ((BByte) closure.value()).byteValue();
                    newArgRegs[argRegIndex++] = intIndex++;
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
                default:
                    sf.refRegs[refIndex] = ((BRefType<?>) closure.value());
                    newArgRegs[argRegIndex++] = refIndex++;
            }
        }

        return BLangFunctions.invokeCallable(functionInfo, ctx, newArgRegs, retRegs, false);
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
        if (fp.getAdditionalIndexCount(BTypes.typeBoolean.getTag()) > 0  ||
                fp.getAdditionalIndexCount(BTypes.typeByte.getTag()) > 0) {
            if (sf.intRegs == null) {
                sf.intRegs = new int[0];
            }
            int[] newIntRegs = new int[sf.intRegs.length + fp.getAdditionalIndexCount(BTypes.typeBoolean.getTag()) +
                    fp.getAdditionalIndexCount(BTypes.typeByte.getTag())];
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
            doubleIndex = sf.doubleRegs.length;
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

    private static int expandRefRegs(WorkerData sf, BFunctionPointer fp) {
        int refIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeAny.getTag()) > 0) {
            if (sf.refRegs == null) {
                sf.refRegs = new BRefType[0];
            }
            BRefType<?>[] newRefRegs = new BRefType[sf.refRegs.length +
                    fp.getAdditionalIndexCount(BTypes.typeAny.getTag())];
            System.arraycopy(sf.refRegs, 0, newRefRegs, 0, sf.refRegs.length);
            refIndex = sf.refRegs.length;
            sf.refRegs = newRefRegs;
        }
        return refIndex;
    }

    private static void findAndAddAdditionalVarRegIndexes(WorkerExecutionContext ctx, int[] operands,
                                                          BFunctionPointer fp) {

        int h = operands[3];

        //if '0', then there are no additional indexes needs to be processed
        if (h == 0) {
            return;
        }

        //or else, this is a closure related scenario
        for (int i = 0; i < h; i++) {
            int operandIndex = i + 4;
            int type = operands[operandIndex];
            int index = operands[++operandIndex];
            switch (type) {
                case TypeTags.INT_TAG: {
                    fp.addClosureVar(new BClosure(new BInteger(ctx.workerLocal.longRegs[index]), BTypes.typeInt),
                            TypeTags.INT_TAG);
                    break;
                }
                case TypeTags.BYTE_TAG: {
                    fp.addClosureVar(new BClosure(new BByte((byte) ctx.workerLocal.intRegs[index]), BTypes.typeByte),
                            TypeTags.BYTE_TAG);
                    break;
                }
                case TypeTags.FLOAT_TAG: {
                    fp.addClosureVar(new BClosure(new BFloat(ctx.workerLocal.doubleRegs[index]), BTypes.typeFloat),
                            TypeTags.FLOAT_TAG);
                    break;
                }
                case TypeTags.DECIMAL_TAG: {
                    fp.addClosureVar(new BClosure(ctx.workerLocal.refRegs[index], BTypes.typeDecimal),
                            TypeTags.DECIMAL_TAG);
                    break;
                }
                case TypeTags.BOOLEAN_TAG: {
                    fp.addClosureVar(new BClosure(new BBoolean(ctx.workerLocal.intRegs[index] == 1),
                                    BTypes.typeBoolean), TypeTags.BOOLEAN_TAG);
                    break;
                }
                case TypeTags.STRING_TAG: {
                    fp.addClosureVar(new BClosure(new BString(ctx.workerLocal.stringRegs[index]), BTypes.typeString),
                            TypeTags.STRING_TAG);
                    break;
                }
                default:
                    fp.addClosureVar(new BClosure(ctx.workerLocal.refRegs[index], BTypes.typeAny), TypeTags.ANY_TAG);
            }
            i++;
        }
    }

    private static void execCmpAndBranchOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode,
                                                int[] operands) {
        int i;
        int j;
        int k;
        BigDecimal lhsValue;
        BigDecimal rhsValue;

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
            case InstructionCodes.DGT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.intRegs[k] = lhsValue.compareTo(rhsValue) > 0 ? 1 : 0;
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
            case InstructionCodes.DGE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.intRegs[k] = lhsValue.compareTo(rhsValue) >= 0 ? 1 : 0;
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
            case InstructionCodes.DLT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.intRegs[k] = lhsValue.compareTo(rhsValue) < 0 ? 1 : 0;
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
            case InstructionCodes.DLE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.intRegs[k] = lhsValue.compareTo(rhsValue) <= 0 ? 1 : 0;
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

    private static void execIntegerRangeOpcodes(WorkerData sf, int[] operands) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];
        sf.refRegs[k] = new BIntArray(LongStream.rangeClosed(sf.longRegs[i], sf.longRegs[j]).toArray());
    }

    private static void execLoadOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int pkgIndex;
        int lvIndex; // Index of the local variable

        BIntArray bIntArray;
        BByteArray bByteArray;
        BFloatArray bFloatArray;
        BStringArray bStringArray;
        BBooleanArray bBooleanArray;
        BMap<String, BRefType> bMap;

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
            case InstructionCodes.RMOVE:
                lvIndex = operands[0];
                i = operands[1];
                sf.refRegs[i] = sf.refRegs[lvIndex];
                break;
            case InstructionCodes.IALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bIntArray = Optional.of((BIntArray) sf.refRegs[i]).get();
                try {
                    sf.longRegs[k] = bIntArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BIALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bByteArray = Optional.of((BByteArray) sf.refRegs[i]).get();
                try {
                    sf.intRegs[k] = bByteArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.FALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bFloatArray = Optional.of((BFloatArray) sf.refRegs[i]).get();
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
                bStringArray = Optional.of((BStringArray) sf.refRegs[i]).get();
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
                bBooleanArray = Optional.of((BBooleanArray) sf.refRegs[i]).get();
                try {
                    sf.intRegs[k] = bBooleanArray.get(sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.RALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                BNewArray bNewArray = Optional.of((BNewArray) sf.refRegs[i]).get();
                try {
                    sf.refRegs[k] = ListUtils.execListGetOperation(bNewArray, sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    sf.refRegs[k] = JSONUtils.getArrayElement(sf.refRegs[i], sf.longRegs[j]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.IGLOAD:
                // package index
                pkgIndex = operands[0];
                // package level variable index
                i = operands[1];
                // Stack registry index
                j = operands[2];
                sf.longRegs[j] = ctx.programFile.globalMemArea.getIntField(pkgIndex, i);
                break;
            case InstructionCodes.FGLOAD:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                sf.doubleRegs[j] = ctx.programFile.globalMemArea.getFloatField(pkgIndex, i);
                break;
            case InstructionCodes.SGLOAD:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                sf.stringRegs[j] = ctx.programFile.globalMemArea.getStringField(pkgIndex, i);
                break;
            case InstructionCodes.BGLOAD:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                sf.intRegs[j] = ctx.programFile.globalMemArea.getBooleanField(pkgIndex, i);
                break;
            case InstructionCodes.RGLOAD:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                sf.refRegs[j] = ctx.programFile.globalMemArea.getRefField(pkgIndex, i);
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

                IntegerCPEntry exceptCPEntry = (IntegerCPEntry) ctx.constPool[operands[3]];
                boolean except = exceptCPEntry.getValue() == 1;
                sf.refRegs[k] = bMap.get(sf.stringRegs[j], except);
                break;

            case InstructionCodes.JSONLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.refRegs[k] = JSONUtils.getElement(sf.refRegs[i], sf.stringRegs[j]);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execStoreOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int pkgIndex;

        BIntArray bIntArray;
        BByteArray bByteArray;
        BFloatArray bFloatArray;
        BStringArray bStringArray;
        BBooleanArray bBooleanArray;
        BMap<String, BRefType> bMap;

        switch (opcode) {
            case InstructionCodes.IASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bIntArray = Optional.of((BIntArray) sf.refRegs[i]).get();
                try {
                    bIntArray.add(sf.longRegs[j], sf.longRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BIASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bByteArray = Optional.of((BByteArray) sf.refRegs[i]).get();
                try {
                    bByteArray.add(sf.longRegs[j], (byte) sf.intRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.FASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bFloatArray = Optional.of((BFloatArray) sf.refRegs[i]).get();
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
                bStringArray = Optional.of((BStringArray) sf.refRegs[i]).get();
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
                bBooleanArray = Optional.of((BBooleanArray) sf.refRegs[i]).get();
                try {
                    bBooleanArray.add(sf.longRegs[j], sf.intRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.RASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                BNewArray list = Optional.of((BNewArray) sf.refRegs[i]).get();
                long index = sf.longRegs[j];
                BRefType refReg = sf.refRegs[k];
                BType elementType = (list.getType().getTag() == TypeTags.ARRAY_TAG)
                        ? ((BArrayType) list.getType()).getElementType()
                        : ((BTupleType) list.getType()).getTupleTypes().get((int) index);
                if (!checkCast(refReg, elementType)) {
                    ctx.setError(BLangVMErrors.createError(ctx,
                            BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_TYPE,
                                    elementType, (refReg != null) ? refReg.getType() : BTypes.typeNull)));
                    handleError(ctx);
                    break;
                }

                try {
                    ListUtils.execListAddOperation(list, index, refReg);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, "Failed to add element: " + e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    JSONUtils.setArrayElement(sf.refRegs[i], sf.longRegs[j], sf.refRegs[k]);
                } catch (Exception e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.IGSTORE:
                pkgIndex = operands[0];
                // Stack reg index
                i = operands[1];
                // Global var index
                j = operands[2];
                ctx.programFile.globalMemArea.setIntField(pkgIndex, j, sf.longRegs[i]);
                break;
            case InstructionCodes.FGSTORE:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                ctx.programFile.globalMemArea.setFloatField(pkgIndex, j, sf.doubleRegs[i]);
                break;
            case InstructionCodes.SGSTORE:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                ctx.programFile.globalMemArea.setStringField(pkgIndex, j, sf.stringRegs[i]);
                break;
            case InstructionCodes.BGSTORE:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                ctx.programFile.globalMemArea.setBooleanField(pkgIndex, j, sf.intRegs[i]);
                break;
            case InstructionCodes.RGSTORE:
                pkgIndex = operands[0];
                i = operands[1];
                j = operands[2];
                ctx.programFile.globalMemArea.setRefField(pkgIndex, j, sf.refRegs[i]);
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

                BRefType<?> value = sf.refRegs[k];
                BType mapType = bMap.getType();
                BType expType = null;

                switch (mapType.getTag()) {
                    case TypeTags.MAP_TAG:
                        if (isValidMapInsertion(mapType, value)) {
                            insertToMap(ctx, bMap, sf.stringRegs[j], value);
                        } else {
                            expType = ((BMapType) mapType).getConstrainedType();
                        }
                        break;
                    case TypeTags.RECORD_TYPE_TAG:
                    case TypeTags.OBJECT_TYPE_TAG:
                        BStructureType structureType = (BStructureType) mapType;
                        BField targetField = structureType.getFields().get(sf.stringRegs[j]);
                        BType targetFieldType;

                        if (structureType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                            targetFieldType = targetField == null ? ((BRecordType) structureType).restFieldType :
                                                                    targetField.fieldType;
                        } else {
                            targetFieldType = targetField.getFieldType();
                        }

                        if (checkIsType(value, targetFieldType)) {
                            insertToMap(ctx, bMap, sf.stringRegs[j], value);
                        } else {
                            expType = targetFieldType;
                        }
                        break;
                }

                if (expType != null) {
                    ctx.setError(BLangVMErrors.createError(ctx, BLangExceptionHelper
                            .getErrorMessage(RuntimeErrors.INVALID_MAP_INSERTION, expType, value.getType())));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONSTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                try {
                    JSONUtils.setElement(sf.refRegs[i], sf.stringRegs[j], sf.refRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, "Failed to set element to JSON: " + e.getMessage()));
                    handleError(ctx);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execBinaryOpCodes(WorkerExecutionContext ctx, WorkerData sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        BigDecimal lhsValue;
        BigDecimal rhsValue;

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
            case InstructionCodes.DADD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.refRegs[k] = new BDecimal(lhsValue.add(rhsValue, MathContext.DECIMAL128));
                break;
            case InstructionCodes.XMLADD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                BXML lhsXMLVal = (BXML) sf.refRegs[i];
                BXML rhsXMLVal = (BXML) sf.refRegs[j];

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
            case InstructionCodes.DSUB:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.refRegs[k] = new BDecimal(lhsValue.subtract(rhsValue, MathContext.DECIMAL128));
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
            case InstructionCodes.DMUL:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.refRegs[k] = new BDecimal(lhsValue.multiply(rhsValue, MathContext.DECIMAL128));
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
                sf.doubleRegs[k] = sf.doubleRegs[i] / sf.doubleRegs[j];
                break;
            case InstructionCodes.DDIV:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                if (rhsValue.compareTo(BigDecimal.ZERO) == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, " / by zero"));
                    handleError(ctx);
                    break;
                }

                sf.refRegs[k] = new BDecimal(lhsValue.divide(rhsValue, MathContext.DECIMAL128));
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
            case InstructionCodes.DMOD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                if (rhsValue.compareTo(BigDecimal.ZERO) == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, " / by zero"));
                    handleError(ctx);
                    break;
                }

                sf.refRegs[k] = new BDecimal(lhsValue.remainder(rhsValue, MathContext.DECIMAL128));
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
            case InstructionCodes.DNEG:
                i = operands[0];
                j = operands[1];
                BigDecimal value = ((BDecimal) sf.refRegs[i]).decimalValue();
                sf.refRegs[j] = new BDecimal(value.negate());
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
            case InstructionCodes.DEQ:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.intRegs[k] = lhsValue.compareTo(rhsValue) == 0 ? 1 : 0;
                break;
            case InstructionCodes.REQ:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.refRegs[i] == null) {
                    sf.intRegs[k] = sf.refRegs[j] == null ? 1 : 0;
                } else {
                    sf.intRegs[k] = isEqual(sf.refRegs[i], sf.refRegs[j]) ? 1 : 0;
                }
                break;
            case InstructionCodes.REF_EQ:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = isReferenceEqual(sf.refRegs[i], sf.refRegs[j]) ? 1 : 0;
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
            case InstructionCodes.DNE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = ((BDecimal) sf.refRegs[i]).decimalValue();
                rhsValue = ((BDecimal) sf.refRegs[j]).decimalValue();
                sf.intRegs[k] = lhsValue.compareTo(rhsValue) != 0 ? 1 : 0;
                break;
            case InstructionCodes.RNE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.refRegs[i] == null) {
                    sf.intRegs[k] = (sf.refRegs[j] != null) ? 1 : 0;
                } else {
                    sf.intRegs[k] = (!isEqual(sf.refRegs[i], sf.refRegs[j])) ? 1 : 0;
                }
                break;
            case InstructionCodes.REF_NEQ:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = isReferenceInequal(sf.refRegs[i], sf.refRegs[j]) ? 1 : 0;
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
            case InstructionCodes.BIAND:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = sf.intRegs[i] & sf.intRegs[j];
                break;
            case InstructionCodes.BIOR:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = sf.intRegs[i] | sf.intRegs[j];
                break;
            case InstructionCodes.BIXOR:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = sf.intRegs[i] ^ sf.intRegs[j];
                break;
            case InstructionCodes.IAND:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.longRegs[k] = sf.longRegs[i] & sf.longRegs[j];
                break;
            case InstructionCodes.IOR:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.longRegs[k] = sf.longRegs[i] | sf.longRegs[j];
                break;
            case InstructionCodes.IXOR:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.longRegs[k] = sf.longRegs[i] ^ sf.longRegs[j];
                break;
            case InstructionCodes.BILSHIFT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = (byte) (sf.intRegs[i] << sf.longRegs[j]);
                break;
            case InstructionCodes.BIRSHIFT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.intRegs[k] = (byte) (sf.intRegs[i] >>> sf.longRegs[j]);
                break;
            case InstructionCodes.IRSHIFT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.longRegs[k] = sf.longRegs[i] >> sf.longRegs[j];
                break;
            case InstructionCodes.ILSHIFT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.longRegs[k] = sf.longRegs[i] << sf.longRegs[j];
                break;
            case InstructionCodes.IURSHIFT:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.longRegs[k] = sf.longRegs[i] >>> sf.longRegs[j];
                break;
            case InstructionCodes.TYPE_TEST:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[j];
                sf.intRegs[k] = checkIsType(sf.refRegs[i], typeRefCPEntry.getType()) ? 1 : 0;
                break;
            case InstructionCodes.IS_LIKE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[j];
                sf.intRegs[k] = checkIsLikeType(sf.refRegs[i], typeRefCPEntry.getType()) ? 1 : 0;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

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

                xmlVal = Optional.of((BXML) sf.refRegs[i]).get();
                xmlQName = Optional.of((BXMLQName) sf.refRegs[j]).get();
                xmlVal.setAttribute(xmlQName.getLocalName(), xmlQName.getUri(), xmlQName.getPrefix(),
                        sf.stringRegs[k]);
                break;
            case InstructionCodes.XMLATTRLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                xmlVal = Optional.of((BXML) sf.refRegs[i]).get();
                xmlQName = Optional.of((BXMLQName) sf.refRegs[j]).get();
                sf.stringRegs[k] = xmlVal.getAttribute(xmlQName.getLocalName(), xmlQName.getUri(),
                        xmlQName.getPrefix());
                break;
            case InstructionCodes.XML2XMLATTRS:
                i = operands[0];
                j = operands[1];
                xmlVal = (BXML) sf.refRegs[i];
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

                xmlVal = Optional.of((BXML) sf.refRegs[i]).get();
                long index = sf.longRegs[j];
                sf.refRegs[k] = xmlVal.getItem(index);
                break;
            case InstructionCodes.XMLLOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                xmlVal = Optional.of((BXML) sf.refRegs[i]).get();
                String qname = sf.stringRegs[j];
                sf.refRegs[k] = xmlVal.children(qname);
                break;
            case InstructionCodes.XMLLOADALL:
                i = operands[0];
                j = operands[1];

                xmlVal = Optional.of((BXML) sf.refRegs[i]).get();
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
            case InstructionCodes.BI2ANY:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BByte((byte) sf.intRegs[i]);
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
            case InstructionCodes.ANY2I:
                i = operands[0];
                j = operands[1];
                sf.longRegs[j] = ((BValueType) sf.refRegs[i]).intValue();
                break;
            case InstructionCodes.ANY2BI:
                i = operands[0];
                j = operands[1];
                sf.intRegs[j] = ((BValueType) sf.refRegs[i]).byteValue();
                break;
            case InstructionCodes.ANY2F:
                i = operands[0];
                j = operands[1];
                sf.doubleRegs[j] = ((BValueType) sf.refRegs[i]).floatValue();
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
            case InstructionCodes.ANY2D:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BDecimal(((BValueType) sf.refRegs[i]).decimalValue());
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
                        sf.refRegs[j] = bRefTypeValue;
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
                if (checkCast(bRefTypeValue, typeRefCPEntry.getType())) {
                    sf.intRegs[j] = 1;
                } else {
                    sf.intRegs[j] = 0;
                }
                break;
            case InstructionCodes.ARRAY2JSON:
                convertArrayToJSON(ctx, operands, sf);
                break;
            case InstructionCodes.JSON2ARRAY:
                convertJSONToArray(ctx, operands, sf);
                break;
            case InstructionCodes.O2JSON:
                i = operands[0];
                cpIndex = operands[1];
                j = operands[2];
                BJSONType targetType = (BJSONType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();
                bRefTypeValue = sf.refRegs[i];
                sf.refRegs[j] = JSONUtils.convertUnionTypeToJSON(bRefTypeValue, targetType);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execTypeConversionOpcodes(WorkerExecutionContext ctx, WorkerData sf, int opcode,
                                                  int[] operands) {
        int i;
        int j;
        BRefType bRefType;
        String str;

        switch (opcode) {
            case InstructionCodes.I2F:
                i = operands[0];
                j = operands[1];
                sf.doubleRegs[j] = sf.longRegs[i];
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
            case InstructionCodes.I2D:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BDecimal(new BigDecimal(sf.longRegs[i], MathContext.DECIMAL128));
                break;
            case InstructionCodes.I2BI:
                i = operands[0];
                j = operands[1];
                if (isByteLiteral(sf.longRegs[i])) {
                    sf.refRegs[j] = new BByte((byte) sf.longRegs[i]);
                } else {
                    handleTypeConversionError(ctx, sf, j, TypeConstants.INT_TNAME, TypeConstants.BYTE_TNAME);
                }
                break;
            case InstructionCodes.BI2I:
                i = operands[0];
                j = operands[1];
                sf.longRegs[j] = Byte.toUnsignedInt((byte) sf.intRegs[i]);
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
            case InstructionCodes.F2D:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = new BDecimal(new BigDecimal(sf.doubleRegs[i], MathContext.DECIMAL128));
                break;
            case InstructionCodes.S2I:
                i = operands[0];
                j = operands[1];

                str = sf.stringRegs[i];
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
            case InstructionCodes.S2D:
                i = operands[0];
                j = operands[1];

                str = sf.stringRegs[i];
                try {
                    sf.refRegs[j] = new BDecimal(new BigDecimal(str, MathContext.DECIMAL128));
                } catch (NumberFormatException e) {
                    handleTypeConversionError(ctx, sf, j, TypeConstants.STRING_TNAME, TypeConstants.DECIMAL_TNAME);
                }
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
            case InstructionCodes.B2D:
                i = operands[0];
                j = operands[1];
                sf.refRegs[j] = sf.intRegs[i] == 1 ? new BDecimal(BigDecimal.ONE) : new BDecimal(BigDecimal.ZERO);
                break;
            case InstructionCodes.D2I:
                i = operands[0];
                j = operands[1];
                sf.longRegs[j] = ((BDecimal) sf.refRegs[i]).intValue();
                break;
            case InstructionCodes.D2F:
                i = operands[0];
                j = operands[1];
                sf.doubleRegs[j] = ((BDecimal) sf.refRegs[i]).floatValue();
                break;
            case InstructionCodes.D2S:
                i = operands[0];
                j = operands[1];
                sf.stringRegs[j] = sf.refRegs[i].stringValue();
                break;
            case InstructionCodes.D2B:
                i = operands[0];
                j = operands[1];
                sf.intRegs[j] = ((BDecimal) sf.refRegs[i]).decimalValue().compareTo(BigDecimal.ZERO) != 0 ? 1 : 0;
                break;
            case InstructionCodes.DT2XML:
                i = operands[0];
                j = operands[1];

                bRefType = sf.refRegs[i];
                if (bRefType == null) {
                    handleTypeConversionError(ctx, sf, j, BTypes.typeNull, BTypes.typeXML);
                    break;
                }

                try {
                    sf.refRegs[j] = XMLUtils.tableToXML((BTable) bRefType);
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
                sf.refRegs[j] = ((BXMLAttributes) sf.refRegs[i]).value();
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

    public static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    private static void execIteratorOperation(WorkerExecutionContext ctx, WorkerData sf, Instruction instruction) {
        int i, j;
        BValue collection;
        BIterator iterator;
        InstructionIteratorNext nextInstruction;
        switch (instruction.getOpcode()) {
            case InstructionCodes.ITR_NEW:
                i = instruction.getOperands()[0];   // collection
                j = instruction.getOperands()[1];   // iterator variable (ref) index.

                collection = sf.refRegs[i];
                if (collection == null) {
                    handleNullRefError(ctx);
                    return;
                } else if (!(collection instanceof BCollection)) {
                    // Value is a value-type JSON.
                    sf.refRegs[j] = new BIterator() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public BValue[] getNext(int arity) {
                            return null;
                        }
                    };
                    break;
                }

                sf.refRegs[j] = ((BCollection) collection).newIterator();
                break;
            case InstructionCodes.ITR_HAS_NEXT:
                i = instruction.getOperands()[0];   // iterator
                j = instruction.getOperands()[1];   // boolean variable index to store has next result
                iterator = (BIterator) sf.refRegs[i];
                sf.intRegs[j] = Optional.of(iterator).get().hasNext() ? 1 : 0;
                break;
            case InstructionCodes.ITR_NEXT:
                nextInstruction = (InstructionIteratorNext) instruction;
                iterator = (BIterator) sf.refRegs[nextInstruction.iteratorIndex];
                BValue[] values = Optional.of(iterator).get().getNext(nextInstruction.arity);
                copyValuesToRegistries(nextInstruction.typeTags, nextInstruction.retRegs, values, sf);
                break;
        }
    }

    private static void copyValuesToRegistries(int[] typeTags, int[] targetReg, BValue[] values, WorkerData sf) {
        for (int i = 0; i < typeTags.length; i++) {
            BValue source = values[i];
            int target = targetReg[i];
            switch (typeTags[i]) {
                case TypeTags.INT_TAG:
                    sf.longRegs[target] = ((BInteger) source).intValue();
                    break;
                case TypeTags.BYTE_TAG:
                    sf.intRegs[target] = ((BByte) source).byteValue();
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

                sf.refRegs[i] = XMLUtils.createXMLComment(sf.stringRegs[j]);
                break;
            case InstructionCodes.NEWXMLTEXT:
                i = operands[0];
                j = operands[1];

                sf.refRegs[i] = XMLUtils.createXMLText(sf.stringRegs[j]);
                break;
            case InstructionCodes.NEWXMLPI:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                sf.refRegs[i] = XMLUtils.createXMLProcessingInstruction(sf.stringRegs[j], sf.stringRegs[k]);
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

    private static boolean handleVariableLock(WorkerExecutionContext ctx, BType[] types,
                                              int[] pkgRegs, int[] varRegs) {
        boolean lockAcquired = true;
        for (int i = 0; i < varRegs.length && lockAcquired; i++) {
            BType paramType = types[i];
            int pkgIndex = pkgRegs[i];
            int regIndex = varRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    lockAcquired = ctx.programFile.globalMemArea.lockIntField(ctx, pkgIndex, regIndex);
                    break;
                case TypeTags.BYTE_TAG:
                    lockAcquired = ctx.programFile.globalMemArea.lockBooleanField(ctx, pkgIndex, regIndex);
                    break;
                case TypeTags.FLOAT_TAG:
                    lockAcquired = ctx.programFile.globalMemArea.lockFloatField(ctx, pkgIndex, regIndex);
                    break;
                case TypeTags.STRING_TAG:
                    lockAcquired = ctx.programFile.globalMemArea.lockStringField(ctx, pkgIndex, regIndex);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    lockAcquired = ctx.programFile.globalMemArea.lockBooleanField(ctx, pkgIndex, regIndex);
                    break;
                default:
                    lockAcquired = ctx.programFile.globalMemArea.lockRefField(ctx, pkgIndex, regIndex);
            }
        }
        return lockAcquired;
    }

    private static void handleVariableUnlock(WorkerExecutionContext ctx, BType[] types,
                                             int[] pkgRegs, int[] varRegs) {
        for (int i = varRegs.length - 1; i > -1; i--) {
            BType paramType = types[i];
            int pkgIndex = pkgRegs[i];
            int regIndex = varRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    ctx.programFile.globalMemArea.unlockIntField(pkgIndex, regIndex);
                    break;
                case TypeTags.BYTE_TAG:
                    ctx.programFile.globalMemArea.unlockBooleanField(pkgIndex, regIndex);
                    break;
                case TypeTags.FLOAT_TAG:
                    ctx.programFile.globalMemArea.unlockFloatField(pkgIndex, regIndex);
                    break;
                case TypeTags.STRING_TAG:
                    ctx.programFile.globalMemArea.unlockStringField(pkgIndex, regIndex);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    ctx.programFile.globalMemArea.unlockBooleanField(pkgIndex, regIndex);
                    break;
                default:
                    ctx.programFile.globalMemArea.unlockRefField(pkgIndex, regIndex);
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
         multiple instructions.
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
        BError errorVal = BLangVMErrors.createTypeCastError(ctx, sourceType, targetType);
        sf.refRegs[errorRegIndex] = errorVal;
    }

    private static void handleTypeConversionError(WorkerExecutionContext ctx, WorkerData sf, int errorRegIndex,
                                                  BType sourceType, BType targetType) {
        handleTypeConversionError(ctx, sf, errorRegIndex, sourceType.toString(), targetType.toString());
    }

    private static void handleTypeConversionError(WorkerExecutionContext ctx, WorkerData sf, int errorRegIndex,
                                                  String sourceTypeName, String targetTypeName) {
        String errorMsg = "'" + sourceTypeName + "' cannot be converted to '" + targetTypeName + "'";
        handleTypeConversionError(ctx, sf, errorRegIndex, errorMsg);
    }

    private static void handleTypeConversionError(WorkerExecutionContext ctx, WorkerData sf,
                                                  int errorRegIndex, String errorMessage) {
        BError errorVal = BLangVMErrors.createTypeConversionError(ctx, errorMessage);
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
        StructureTypeInfo structInfo = (StructureTypeInfo) ((TypeDefInfo) structureRefCPEntry
                .getStructureTypeInfo()).typeInfo;
        sf.refRegs[i] = new BMap<>(structInfo.getType());
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
            BFunctionPointer fpCommitted = new BFunctionPointer(funcRefCPEntry.getFunctionInfo());
            TransactionResourceManager.getInstance().registerCommittedFunction(transactionBlockId, fpCommitted);
        }

        //Register aborted function handler if exists.
        if (abortedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) ctx.constPool[abortedFuncIndex];
            BFunctionPointer fpAborted = new BFunctionPointer(funcRefCPEntry.getFunctionInfo());
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
                BMap<String, BValue> txDataStruct = (BMap<String, BValue>) returns[0];
                globalTransactionId = txDataStruct.get(TransactionConstants.TRANSACTION_ID).stringValue();
                protocol = txDataStruct.get(TransactionConstants.CORDINATION_TYPE).stringValue();
                url = txDataStruct.get(TransactionConstants.REGISTER_AT_URL).stringValue();
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
                String errorMsg = ctx.getError().reason;
                if (BLangVMErrors.TRANSACTION_ERROR.equals(errorMsg)) {
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
        BMap<String, BValue> structVal = (BMap<String, BValue>) ctx.workerLocal.refRegs[receiver];

        // TODO use ObjectTypeInfo once record init function is removed
        StructureTypeInfo structInfo = (StructureTypeInfo) ((BStructureType) structVal.getType()).getTypeInfo();
        FunctionInfo attachedFuncInfo = structInfo.funcInfoEntries.get(virtualFuncInfo.getName());
        return BLangFunctions.invokeCallable(attachedFuncInfo, ctx, argRegs, retRegs, false, flags);
    }

    private static void handleWorkerSend(WorkerExecutionContext ctx, WorkerDataChannelInfo workerDataChannelInfo,
                                         BType type, int reg) {
        BRefType val = extractValue(ctx.workerLocal, type, reg);
        WorkerDataChannel dataChannel = getWorkerChannel(ctx, workerDataChannelInfo.getChannelName());
        dataChannel.putData(val);
    }

    private static WorkerDataChannel getWorkerChannel(WorkerExecutionContext ctx, String name) {
        return ctx.respCtx.getWorkerDataChannel(name);
    }

    private static BRefType extractValue(WorkerData data, BType type, int reg) {
        BRefType result;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                result = new BInteger(data.longRegs[reg]);
                break;
            case TypeTags.BYTE_TAG:
                result = new BByte((byte) data.intRegs[reg]);
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

    private static boolean handleWorkerReceive(WorkerExecutionContext ctx, WorkerDataChannelInfo workerDataChannelInfo,
                                               BType type, int reg) {
        WorkerDataChannel.WorkerResult passedInValue = getWorkerChannel(
                ctx, workerDataChannelInfo.getChannelName()).tryTakeData(ctx);
        if (passedInValue != null) {
            WorkerData currentFrame = ctx.workerLocal;
            copyArgValueForWorkerReceive(currentFrame, reg, type, passedInValue.value);
            return true;
        } else {
            return false;
        }
    }

    public static void copyArgValueForWorkerReceive(WorkerData currentSF, int regIndex, BType paramType,
                                                     BRefType passedInValue) {
        switch (paramType.getTag()) {
            case TypeTags.INT_TAG:
                currentSF.longRegs[regIndex] = ((BInteger) passedInValue).intValue();
                break;
            case TypeTags.BYTE_TAG:
                currentSF.intRegs[regIndex] = ((BByte) passedInValue).byteValue();
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
            default:
                currentSF.refRegs[regIndex] = passedInValue;
        }
    }

    private static WorkerExecutionContext handleReturn(WorkerExecutionContext ctx) {
        BLangScheduler.workerDone(ctx);
        return ctx.respCtx.signal(new WorkerSignal(ctx, SignalType.RETURN, ctx.workerResult));
    }

    private static boolean checkFiniteTypeAssignable(BValue bRefTypeValue, BType lhsType) {
        BFiniteType fType = (BFiniteType) lhsType;
        if (bRefTypeValue == null) {
            // we should not reach here
            return false;
        } else {
            Iterator<BValue> valueSpaceItr = fType.valueSpace.iterator();
            while (valueSpaceItr.hasNext()) {
                BValue valueSpaceItem = valueSpaceItr.next();
                if (valueSpaceItem.getType().getTag() == bRefTypeValue.getType().getTag()) {
                    if (valueSpaceItem.equals(bRefTypeValue)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkUnionCast(BValue rhsValue, BType lhsType, List<TypePair> unresolvedTypes) {
        BUnionType unionType = (BUnionType) lhsType;
        for (BType memberType : unionType.getMemberTypes()) {
            if (checkCast(rhsValue, memberType, unresolvedTypes)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkCast(BValue rhsValue, BType lhsType) {
        return checkCast(rhsValue, lhsType, new ArrayList<>());
    }

    private static boolean checkCast(BValue rhsValue, BType lhsType, List<TypePair> unresolvedTypes) {
        BType rhsType = rhsValue == null ? BTypes.typeNull : rhsValue.getType();
        if (isSameOrAnyType(rhsType, lhsType)) {
            return true;
        }

        if (rhsType.getTag() == TypeTags.INT_TAG && lhsType.getTag() == TypeTags.BYTE_TAG) {
            return isByteLiteral(((BInteger) rhsValue).intValue());
        }

        if (lhsType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionCast(rhsValue, lhsType, unresolvedTypes);
        }

        if (getElementType(rhsType).getTag() == TypeTags.JSON_TAG) {
            return checkJSONCast(rhsValue, rhsType, lhsType, unresolvedTypes);
        }

        if (lhsType.getTag() == TypeTags.ARRAY_TAG && rhsValue instanceof BNewArray) {
            BType sourceType = rhsValue.getType();
            if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
                if (((BArrayType) sourceType).getState() == BArrayState.CLOSED_SEALED
                        && ((BArrayType) lhsType).getState() == BArrayState.CLOSED_SEALED
                        && ((BArrayType) sourceType).getSize() != ((BArrayType) lhsType).getSize()) {
                    return false;
                }
                sourceType = ((BArrayType) rhsValue.getType()).getElementType();
            }
            return checkArrayCast(sourceType, ((BArrayType) lhsType).getElementType(), unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.TUPLE_TAG && lhsType.getTag() == TypeTags.TUPLE_TAG) {
            return checkTupleCast(rhsValue, lhsType, unresolvedTypes);
        }

        if (lhsType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return checkFiniteTypeAssignable(rhsValue, lhsType);
        }

        return checkCastByType(rhsType, lhsType, unresolvedTypes);
    }

    /**
     * This method is for use as the first check in checking for cast/assignability for two types.
     * Checks whether the source type is the same as the target type or if the target type is any type, and if true
     * the return value would be true.
     *
     * @param rhsType   the source type - the type (of the value) being cast/assigned
     * @param lhsType   the target type against which cast/assignability is checked
     * @return          true if the lhsType is any or is the same as rhsType
     */
    private static boolean isSameOrAnyType(BType rhsType, BType lhsType) {
        return lhsType.getTag() == TypeTags.ANY_TAG || rhsType.equals(lhsType);
    }

    private static boolean checkCastByType(BType rhsType, BType lhsType, List<TypePair> unresolvedTypes) {
        if (rhsType.getTag() == TypeTags.INT_TAG &&
                (lhsType.getTag() == TypeTags.FLOAT_TAG || lhsType.getTag() == TypeTags.DECIMAL_TAG)) {
            return true;
        } else if (rhsType.getTag() == TypeTags.FLOAT_TAG && lhsType.getTag() == TypeTags.DECIMAL_TAG) {
            return true;
        } else if (rhsType.getTag() == TypeTags.BYTE_TAG && lhsType.getTag() == TypeTags.INT_TAG) {
            return true;
        }

        if (lhsType.getTag() == TypeTags.JSON_TAG) {
            switch (rhsType.getTag()) {
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.NULL_TAG:
                case TypeTags.JSON_TAG:
                    return true;
                case TypeTags.ARRAY_TAG:
                    if (((BJSONType) lhsType).getConstrainedType() != null) {
                        return false;
                    }
                    return checkCastByType(((BArrayType) rhsType).getElementType(), lhsType, unresolvedTypes);
                default:
                    return false;
            }
        }

        if (rhsType.getTag() == TypeTags.OBJECT_TYPE_TAG && lhsType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            return checkObjectEquivalency((BStructureType) rhsType, (BStructureType) lhsType, unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.RECORD_TYPE_TAG && lhsType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            return checkRecordEquivalency((BRecordType) lhsType, (BRecordType) rhsType, unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.MAP_TAG && lhsType.getTag() == TypeTags.MAP_TAG) {
            return checkMapCast(rhsType, lhsType, unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.TABLE_TAG && lhsType.getTag() == TypeTags.TABLE_TAG) {
            return true;
        }

        if (rhsType.getTag() == TypeTags.STREAM_TAG && lhsType.getTag() == TypeTags.STREAM_TAG) {
            return isAssignable(((BStreamType) rhsType).getConstrainedType(),
                    ((BStreamType) lhsType).getConstrainedType(), unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.FUNCTION_POINTER_TAG && lhsType.getTag() == TypeTags.FUNCTION_POINTER_TAG) {
            return checkFunctionCast(rhsType, (BFunctionType) lhsType);
        }

        if (lhsType.getTag() == TypeTags.ANYDATA_TAG && isAnydata(rhsType)) {
            return true;
        }

        return false;
    }

    private static boolean checkMapCast(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        BMapType sourceMapType = (BMapType) sourceType;
        BMapType targetMapType = (BMapType) targetType;

        if (sourceMapType.equals(targetMapType)) {
            return true;
        }

        if (targetMapType.getConstrainedType().getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        if (sourceMapType.getConstrainedType().getTag() == TypeTags.OBJECT_TYPE_TAG &&
                targetMapType.getConstrainedType().getTag() == TypeTags.OBJECT_TYPE_TAG) {
            return checkObjectEquivalency((BStructureType) sourceMapType.getConstrainedType(),
                    (BStructureType) targetMapType.getConstrainedType(), unresolvedTypes);
        }

        if (sourceMapType.getConstrainedType().getTag() == TypeTags.RECORD_TYPE_TAG &&
                targetMapType.getConstrainedType().getTag() == TypeTags.RECORD_TYPE_TAG) {
            return checkRecordEquivalency((BRecordType) targetMapType.getConstrainedType(),
                                          (BRecordType) sourceMapType.getConstrainedType(), unresolvedTypes);
        }

        return false;
    }

    private static boolean checkArrayCast(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType sourceArrayType = (BArrayType) sourceType;
            BArrayType targetArrayType = (BArrayType) targetType;
            if (targetArrayType.getDimensions() > sourceArrayType.getDimensions()) {
                return false;
            }

            return checkArrayCast(sourceArrayType.getElementType(), targetArrayType.getElementType(), unresolvedTypes);
        } else if (targetType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionAssignable(sourceType, targetType, unresolvedTypes);
        }

        if (targetType.getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        return sourceType.equals(targetType);
    }

    private static boolean checkTupleCast(BValue sourceValue, BType targetType, List<TypePair> unresolvedTypes) {
        BRefValueArray source = (BRefValueArray) sourceValue;
        BTupleType target = (BTupleType) targetType;
        List<BType> targetTupleTypes = target.getTupleTypes();
        if (source.size() != targetTupleTypes.size()) {
            return false;
        }
        for (int i = 0; i < source.size(); i++) {
            if (!checkCast(source.getBValue(i), targetTupleTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAnydata(BType type) {
        if (type.getTag() <= TypeTags.ANYDATA_TAG) {
            return true;
        }

        switch (type.getTag()) {
            case TypeTags.MAP_TAG:
                return isAnydata(((BMapType) type).getConstrainedType());
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) type;
                List<BType> fieldTypes = recordType.getFields().values().stream()
                        .map(BField::getFieldType)
                        .collect(Collectors.toList());
                return isAnydata(fieldTypes) && (recordType.sealed || isAnydata(recordType.restFieldType));
            case TypeTags.UNION_TAG:
                return isAnydata(((BUnionType) type).getMemberTypes());
            case TypeTags.TUPLE_TAG:
                return isAnydata(((BTupleType) type).getTupleTypes());
            case TypeTags.ARRAY_TAG:
                return isAnydata(((BArrayType) type).getElementType());
            case TypeTags.FINITE_TYPE_TAG:
                Set<BType> valSpaceTypes = ((BFiniteType) type).valueSpace.stream()
                                                                        .map(BValue::getType)
                                                                        .collect(Collectors.toSet());
                return isAnydata(valSpaceTypes);
            default:
                return false;
        }
    }

    private static boolean isAnydata(Collection<BType> types) {
        return types.stream().allMatch(CPU::isAnydata);
    }

    private static BType getElementType(BType type) {
        if (type.getTag() != TypeTags.ARRAY_TAG) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    public static boolean checkStructEquivalency(BStructureType rhsType, BStructureType lhsType) {
        return checkObjectEquivalency(rhsType, lhsType, new ArrayList<>());
    }

    private static boolean checkObjectEquivalency(BStructureType rhsType, BStructureType lhsType,
                                                 List<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(rhsType, lhsType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

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

        if (lhsType.getFields().size() > rhsType.getFields().size() ||
                lhsAttachedFunctionCount > rhsType.getAttachedFunctions().length) {
            return false;
        }

        return !Flags.isFlagOn(lhsType.flags, Flags.PUBLIC) &&
                rhsType.getPackagePath().equals(lhsType.getPackagePath()) ?
                checkPrivateObjectsEquivalency(lhsType, rhsType, unresolvedTypes) :
                checkPublicObjectsEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    public static boolean checkRecordEquivalency(BRecordType lhsType, BRecordType rhsType,
                                                 List<TypePair> unresolvedTypes) {
        // Both records should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Flags.isFlagOn(lhsType.flags ^ rhsType.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both records are private, they should be in the same package.
        if (!Flags.isFlagOn(lhsType.flags, Flags.PUBLIC) &&
                !rhsType.getPackagePath().equals(lhsType.getPackagePath())) {
            return false;
        }

        // If only one is a closed record, the records aren't equivalent
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        // The rest field types should match if they are open records
        if ((!lhsType.sealed && !rhsType.sealed) &&
                !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    private static boolean checkPrivateObjectsEquivalency(BStructureType lhsType, BStructureType rhsType,
                                                           List<TypePair> unresolvedTypes) {
        Map<String, BField> rhsFields = rhsType.getFields();
        for (Map.Entry<String, BField> lhsFieldEntry : lhsType.getFields().entrySet()) {
            BField rhsField = rhsFields.get(lhsFieldEntry.getKey());
            if (rhsField == null || !isSameType(rhsField.fieldType, lhsFieldEntry.getValue().fieldType)) {
                return false;
            }
        }

        BAttachedFunction[] lhsFuncs = lhsType.getAttachedFunctions();
        BAttachedFunction[] rhsFuncs = rhsType.getAttachedFunctions();
        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsType.initializer || lhsFunc == lhsType.defaultsValuesInitFunc) {
                continue;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkPublicObjectsEquivalency(BStructureType lhsType, BStructureType rhsType,
                                                           List<TypePair> unresolvedTypes) {
        // Check the whether there is any private fields in RHS type
        if (rhsType.getFields().values().stream().anyMatch(field -> !Flags.isFlagOn(field.flags, Flags.PUBLIC))) {
            return false;
        }

        Map<String, BField> rhsFields = rhsType.getFields();
        for (Map.Entry<String, BField> lhsFieldEntry : lhsType.getFields().entrySet()) {
            BField rhsField = rhsFields.get(lhsFieldEntry.getKey());
            if (rhsField == null || !Flags.isFlagOn(lhsFieldEntry.getValue().flags, Flags.PUBLIC) ||
                    !isSameType(rhsField.fieldType, lhsFieldEntry.getValue().fieldType)) {
                return false;
            }
        }

        BAttachedFunction[] lhsFuncs = lhsType.getAttachedFunctions();
        BAttachedFunction[] rhsFuncs = rhsType.getAttachedFunctions();
        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsType.initializer || lhsFunc == lhsType.defaultsValuesInitFunc) {
                continue;
            }

            if (!Flags.isFlagOn(lhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null || !Flags.isFlagOn(rhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }
        }

        // Check for private attached function in RHS type
        for (BAttachedFunction rhsFunc : rhsFuncs) {
            if (!Flags.isFlagOn(rhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkFieldEquivalency(BRecordType lhsType, BRecordType rhsType,
                                                 List<TypePair> unresolvedTypes) {
        Map<String, BField> rhsFields = rhsType.getFields();

        for (Map.Entry<String, BField> lhsFieldEntry : lhsType.getFields().entrySet()) {
            BField rhsField = rhsFields.get(lhsFieldEntry.getKey());

            if (rhsField == null || !isAssignable(rhsField.fieldType, lhsFieldEntry.getValue().fieldType,
                                                  unresolvedTypes)) {
                return false;
            }

            rhsFields.remove(lhsFieldEntry.getKey());
        }

        return rhsFields.entrySet().stream().allMatch(
                fieldEntry -> isAssignable(fieldEntry.getValue().getFieldType(), lhsType.restFieldType,
                                           unresolvedTypes));
    }

    private static boolean checkFunctionTypeEqualityForObjectType(BFunctionType source, BFunctionType target,
                                                                  List<TypePair> unresolvedTypes) {
        if (source.paramTypes.length != target.paramTypes.length ||
                source.retParamTypes.length != target.retParamTypes.length) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.length; i++) {
            if (!isAssignable(source.paramTypes[i], target.paramTypes[i], unresolvedTypes)) {
                return false;
            }
        }

        if (source.retParamTypes == null && target.retParamTypes == null) {
            return true;
        } else if (source.retParamTypes == null || target.retParamTypes == null) {
            return false;
        }

        for (int i = 0; i < source.retParamTypes.length; i++) {
            if (!isAssignable(source.retParamTypes[i], target.retParamTypes[i], unresolvedTypes)) {
                return false;
            }
        }

        return true;
    }

    private static BAttachedFunction getMatchingInvokableType(BAttachedFunction[] rhsFuncs, BAttachedFunction lhsFunc,
                                                              List<TypePair> unresolvedTypes) {
        return Arrays.stream(rhsFuncs)
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> checkFunctionTypeEqualityForObjectType(rhsFunc.type, lhsFunc.type, unresolvedTypes))
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

    private static boolean checkJSONEquivalency(BValue json, BJSONType sourceType, BJSONType targetType,
                                                List<TypePair> unresolvedTypes) {
        BRecordType sourceConstrainedType = (BRecordType) sourceType.getConstrainedType();
        BRecordType targetConstrainedType = (BRecordType) targetType.getConstrainedType();

        // Casting to an unconstrained JSON
        if (targetConstrainedType == null) {
            return true;
        }

        // Casting from constrained JSON to constrained JSON
        if (sourceConstrainedType != null) {
            if (sourceConstrainedType.equals(targetConstrainedType)) {
                return true;
            }

            return checkRecordEquivalency(targetConstrainedType, sourceConstrainedType, unresolvedTypes);
        }

        // Casting from unconstrained JSON to constrained JSON
        if (json == null) {
            return true;
        }

        // Return false if the JSON is not a json-object
        if (json.getType().getTag() != TypeTags.JSON_TAG) {
            return false;
        }

        BMap<String, BValue> jsonObject = (BMap<String, BValue>) json;
        Map<String, BField> tFields = targetConstrainedType.getFields();
        for (Map.Entry<String, BField> tFieldEntry : tFields.entrySet()) {
            String fieldName = tFieldEntry.getKey();
            if (!jsonObject.hasKey(fieldName)) {
                return false;
            }

            BValue fieldVal = jsonObject.get(fieldName);
            if (!checkJSONCast(fieldVal, fieldVal.getType(), tFieldEntry.getValue().getFieldType(), unresolvedTypes)) {
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
     * @param unresolvedTypes Unresolved types
     * @return Runtime compatibility for casting
     */
    private static boolean checkJSONCast(BValue json, BType sourceType, BType targetType,
                                         List<TypePair> unresolvedTypes) {
        switch (targetType.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
                return json != null && json.getType().getTag() == targetType.getTag();
            case TypeTags.DECIMAL_TAG:
                return json != null && json.getType().getTag() == TypeTags.FLOAT_TAG;
            case TypeTags.ARRAY_TAG:
                if (json == null || json.getType().getTag() != TypeTags.ARRAY_TAG) {
                    return false;
                }
                BArrayType arrayType = (BArrayType) targetType;
                BRefValueArray array = (BRefValueArray) json;
                for (int i = 0; i < array.size(); i++) {
                    // get the element type of source and json, and recursively check for json casting.
                    BType sourceElementType = sourceType.getTag() == TypeTags.ARRAY_TAG
                            ? ((BArrayType) sourceType).getElementType() : sourceType;
                    if (!checkJSONCast(array.get(i), sourceElementType, arrayType.getElementType(), unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.JSON_TAG:
                // If JSON is unconstrained, any JSON value is compatible
                if (((BJSONType) targetType).getConstrainedType() == null &&
                        getElementType(sourceType).getTag() == TypeTags.JSON_TAG) {
                    return true;
                } else if (sourceType.getTag() != TypeTags.JSON_TAG) {
                    // If JSON is constrained, only JSON objects are compatible
                    return false;
                }
                return checkJSONEquivalency(json, (BJSONType) sourceType, (BJSONType) targetType, unresolvedTypes);
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
                return true;
            default:
                return false;
        }
    }

    private static void convertStructToMap(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int j = operands[1];

        // TODO: do validation for type?
        BMap newMap = new BMap(BTypes.typeMap);
        ((BMap) sf.refRegs[i]).getMap().forEach((key, value)
                -> newMap.put(key, value == null ? null : ((BValue) value).copy(new HashMap<>())));
        sf.refRegs[j] = newMap;
    }

    private static void convertStructToJSON(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BJSONType targetType = (BJSONType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();

        BMap<String, BValue> bStruct = (BMap<String, BValue>) sf.refRegs[i];
        try {
            sf.refRegs[j] = JSONUtils.convertMapToJSON(bStruct, targetType);
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
        try {
            sf.refRegs[j] = JSONUtils.convertArrayToJSON(bArray);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + bArray.getType() + "' to type '" + BTypes.typeJSON + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static void convertJSONToArray(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BArrayType targetType = (BArrayType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();

        BRefType<?> json = sf.refRegs[i];
        if (json == null) {
            handleTypeConversionError(ctx, sf, j, BTypes.typeNull, targetType);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.convertJSON(json, targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + json.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static void convertMapToJSON(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BJSONType targetType = (BJSONType) ((TypeRefCPEntry) ctx.constPool[cpIndex]).getType();

        BMap<String, ?> bMap = (BMap<String, ?>) sf.refRegs[i];
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

        BRefType<?> json = sf.refRegs[i];
        if (json == null) {
            handleTypeConversionError(ctx, sf, j, BTypes.typeNull, targetType);
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.jsonToBMap(json, targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + json.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static void convertMapToStruct(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
        BStructureType structType = (BStructureType) typeRefCPEntry.getType();
        BMap<String, BValue> bMap = (BMap<String, BValue>) sf.refRegs[i];

        try {
            sf.refRegs[j] = convertMapToStruct(ctx, bMap, structType);
        } catch (BallerinaException e) {
            sf.refRegs[j] = null;
            String errorMsg = "cannot convert '" + bMap.getType() + "' to type '" + structType + ": " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static BMap<String, BValue> convertMapToStruct(WorkerExecutionContext ctx,
                                                           BMap<String, BValue> bMap, BStructureType structType) {
        BMap<String, BValue> bStruct = new BMap<>(structType);
        StructureTypeInfo structInfo = (StructureTypeInfo) structType.getTypeInfo();

        for (StructFieldInfo fieldInfo : structInfo.getFieldInfoEntries()) {
            String key = fieldInfo.getName();
            BType fieldType = fieldInfo.getFieldType();

            boolean containsField = bMap.hasKey(key);
            if (!containsField) {
                DefaultValueAttributeInfo defaultValAttrInfo = (DefaultValueAttributeInfo)
                        getAttributeInfo(fieldInfo, AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE);
                if (defaultValAttrInfo != null) {
                    switch (fieldType.getTag()) {
                        case TypeTags.INT_TAG:
                            bStruct.put(key, new BInteger(defaultValAttrInfo.getDefaultValue().getIntValue()));
                            continue;
                        case TypeTags.BYTE_TAG:
                            bStruct.put(key, new BByte(defaultValAttrInfo.getDefaultValue().getByteValue()));
                            continue;
                        case TypeTags.FLOAT_TAG:
                            bStruct.put(key, new BFloat(defaultValAttrInfo.getDefaultValue().getFloatValue()));
                            continue;
                        case TypeTags.DECIMAL_TAG:
                            bStruct.put(key, new BDecimal(defaultValAttrInfo.getDefaultValue().getDecimalValue()));
                            continue;
                        case TypeTags.STRING_TAG:
                            bStruct.put(key, new BString(defaultValAttrInfo.getDefaultValue().getStringValue()));
                            continue;
                        case TypeTags.BOOLEAN_TAG:
                            bStruct.put(key, new BBoolean(defaultValAttrInfo.getDefaultValue().getBooleanValue()));
                            continue;
                    }
                }
                bStruct.put(key, fieldType.getZeroValue());
                continue;
            }

            BValue mapVal = bMap.get(key);
            if (mapVal == null && BTypes.isValueType(fieldType)) {
                throw BLangExceptionHelper.getRuntimeException(
                        RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key, fieldType, null);
            }

            if (mapVal != null && mapVal.getType().getTag() == TypeTags.MAP_TAG) {
                bStruct.put(key, convertMap(ctx, (BMap<String, BValue>) mapVal, fieldType, key));
                continue;
            }

            if (!checkCast(mapVal, fieldType, new ArrayList<TypePair>())) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING,
                                                               key, fieldType,
                                                               mapVal == null ? null : mapVal.getType());
            }
            bStruct.put(key, mapVal);
        }
        return bStruct;
    }

    private static BValue convertMap(WorkerExecutionContext ctx, BMap<String, BValue> mapValue, BType targetType,
                                     String key) {
        switch (targetType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                return convertMapToStruct(ctx, mapValue, (BStructureType) targetType);
            case TypeTags.JSON_TAG:
                return JSONUtils.convertMapToJSON(mapValue, (BJSONType) targetType);
            case TypeTags.UNION_TAG:
                for (BType memType : ((BUnionType) targetType).getMemberTypes()) {
                    try {
                        return convertMap(ctx, mapValue, memType, key);
                    } catch (BallerinaException e) {
                        // ignore conversion exception if thrown when the expected type is a union
                        // type, to allow attempting conversion for other types
                    }
                }
                break;
            default:
                if (checkCast(mapValue, targetType, new ArrayList<TypePair>())) {
                    return mapValue;
                }
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key,
                                                       targetType, mapValue.getType());
    }

    private static void convertJSONToStruct(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
        BRefType<?> bjson = sf.refRegs[i];
        if (bjson == null) {
            handleTypeConversionError(ctx, sf, j, bjson != null ? bjson.getType() : BTypes.typeNull,
                    typeRefCPEntry.getType());
            return;
        }

        try {
            sf.refRegs[j] = JSONUtils.convertJSONToStruct(bjson, (BStructureType) typeRefCPEntry.getType());
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
        // TODO: Fix me
        int ip = ctx.ip;
        ip--;
        ErrorTableEntry match = ErrorTableEntry.getMatch(ctx.callableUnitInfo.getPackageInfo(), ip);
        if (match != null) {
            ctx.ip = match.ipTarget;
            ctx.workerLocal.refRegs[match.regIndex] = ctx.getError();
            ctx.setError(null);
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

    private static void calculateLength(WorkerExecutionContext ctx, int[] operands, WorkerData sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) ctx.constPool[cpIndex];
        int typeTag = typeRefCPEntry.getType().getTag();
        if (typeTag == TypeTags.STRING_TAG) {
            sf.longRegs[j] = sf.stringRegs[i].length();
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
        } else if (typeTag == TypeTags.TABLE_TAG) {
            BTable bTable = (BTable) entity;
            int tableLength = bTable.length();
            sf.longRegs[j] = tableLength;
            return;
        } else if (entity instanceof BMap) {
            sf.longRegs[j] = ((BMap) entity).size();
            return;
        } else if (entity instanceof BNewArray) {
            sf.longRegs[j] = ((BNewArray) entity).size();
            return;
        }

        sf.longRegs[j] = -1;
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

    private static void insertToMap(WorkerExecutionContext ctx, BMap bMap, String fieldName, BValue value) {
        try {
            bMap.put(fieldName, value);
        } catch (BLangFreezeException e) {
            // we would only reach here for record or map, not for object
            String errMessage = "";
            switch (bMap.getType().getTag()) {
                case TypeTags.RECORD_TYPE_TAG:
                    errMessage = "Invalid update of record field: ";
                    break;
                case TypeTags.MAP_TAG:
                    errMessage = "Invalid map insertion: ";
                    break;
            }
            ctx.setError(BLangVMErrors.createError(ctx, errMessage + e.getMessage()));
            handleError(ctx);
        }
    }

    private static boolean isValidMapInsertion(BType mapType, BValue value) {
        if (value == null) {
            return true;
        }

        BType constraintType = ((BMapType) mapType).getConstrainedType();
        if (constraintType == BTypes.typeAny || constraintType.equals(value.getType())) {
            return true;
        }

        return checkCast(value, constraintType, new ArrayList<>());
    }

    public static boolean isAssignable(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (isSameOrAnyType(sourceType, targetType)) {
            return true;
        }

        if (targetType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionAssignable(sourceType, targetType, unresolvedTypes);
        }

        // TODO: 6/26/18 complete impl. for JSON assignable
        if (targetType.getTag() == TypeTags.JSON_TAG && sourceType.getTag() == TypeTags.JSON_TAG) {
            return true;
        }

        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            if (((BArrayType) sourceType).getState() == BArrayState.CLOSED_SEALED
                    && ((BArrayType) targetType).getState() == BArrayState.CLOSED_SEALED
                    && ((BArrayType) sourceType).getSize() != ((BArrayType) targetType).getSize()) {
                return false;
            }
            return checkArrayCast(((BArrayType) sourceType).getElementType(),
                                  ((BArrayType) targetType).getElementType(), unresolvedTypes);
        }

        if (sourceType.getTag() == TypeTags.TUPLE_TAG && targetType.getTag() == TypeTags.TUPLE_TAG) {
            return checkTupleAssignable(sourceType, targetType, unresolvedTypes);
        }

        return checkCastByType(sourceType, targetType, unresolvedTypes);
    }

    private static boolean checkUnionAssignable(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.UNION_TAG) {
            for (BType sourceMemberType : ((BUnionType) sourceType).getMemberTypes()) {
                if (!checkUnionAssignable(sourceMemberType, targetType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        } else {
            BUnionType targetUnionType = (BUnionType) targetType;
            for (BType memberType : targetUnionType.getMemberTypes()) {
                if (isAssignable(sourceType, memberType, unresolvedTypes)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean checkTupleAssignable(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        List<BType> targetTupleTypes = ((BTupleType) targetType).getTupleTypes();
        List<BType> sourceTupleTypes = ((BTupleType) sourceType).getTupleTypes();

        if (sourceTupleTypes.size() != targetTupleTypes.size()) {
            return false;
        }
        for (int i = 0; i < sourceTupleTypes.size(); i++) {
            if (!isAssignable(sourceTupleTypes.get(i), targetTupleTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFunctionCast(BType sourceType, BFunctionType targetType) {
        if (sourceType.getTag() != TypeTags.FUNCTION_POINTER_TAG) {
            return false;
        }

        BFunctionType source = (BFunctionType) sourceType;
        if (source.paramTypes.length != targetType.paramTypes.length ||
                source.retParamTypes.length != targetType.retParamTypes.length) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.length; i++) {
            if (!isSameType(source.paramTypes[i], targetType.paramTypes[i])) {
                return false;
            }
        }

        for (int i = 0; i < source.retParamTypes.length; i++) {
            if (!isSameType(source.retParamTypes[i], targetType.retParamTypes[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add the corresponding compensation function pointer of the given scope, to the compensations table. A copy of
     * current worker data of the args also added to the table.
     * @param scopeEnd current scope instruction
     * @param ctx current WorkerExecutionContext
     */
    private static void addToCompensationTable(Instruction.InstructionScopeEnd scopeEnd, WorkerExecutionContext ctx,
            BFunctionPointer fp) {
        CompensationTable compensationTable = (CompensationTable) ctx.globalProps.get(Constants.COMPENSATION_TABLE);
        CompensationTable.CompensationEntry entry = compensationTable.getNewEntry();
        entry.functionInfo = scopeEnd.function;
        entry.scope = scopeEnd.scopeName;
        entry.fPointer = fp;
        compensationTable.compensations.add(entry);
        compensationTable.index++;
    }

    public static boolean checkIsLikeType(BValue sourceValue, BType targetType) {
        BType sourceType = sourceValue == null ? BTypes.typeNull : sourceValue.getType();
        if (checkIsType(sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        switch (targetType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsLikeRecordType(sourceValue, (BRecordType) targetType);
            case TypeTags.JSON_TAG:
                return checkIsLikeJSONType(sourceValue, (BJSONType) targetType);
            case TypeTags.MAP_TAG:
                return checkIsLikeMapType(sourceValue, (BMapType) targetType);
            case TypeTags.ARRAY_TAG:
                return checkIsLikeArrayType(sourceValue, (BArrayType) targetType);
            case TypeTags.TUPLE_TAG:
                return checkIsLikeTupleType(sourceValue, (BTupleType) targetType);
            case TypeTags.ANYDATA_TAG:
                return checkIsLikeAnydataType(sourceValue, targetType);
            case TypeTags.FINITE_TYPE_TAG:
                return checkFiniteTypeAssignable(sourceValue, targetType);
            case TypeTags.UNION_TAG:
                return ((BUnionType) targetType).getMemberTypes().stream()
                        .anyMatch(type -> checkIsLikeType(sourceValue, type));
            default:
                return false;
        }
    }

    private static boolean checkIsLikeAnydataType(BValue sourceValue, BType targetType) {
        switch (sourceValue.getType().getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.MAP_TAG:
                return ((BMap) sourceValue).getMap().entrySet().stream()
                        .allMatch(value -> checkIsLikeType((BValue) value, targetType));
            case TypeTags.ARRAY_TAG:
                BNewArray arr = (BNewArray) sourceValue;
                switch (arr.getType().getTag()) {
                    case TypeTags.INT_TAG:
                    case TypeTags.FLOAT_TAG:
                    case TypeTags.DECIMAL_TAG:
                    case TypeTags.STRING_TAG:
                    case TypeTags.BOOLEAN_TAG:
                    case TypeTags.BYTE_TAG:
                        return true;
                    default:
                        return Arrays.stream(((BRefValueArray) sourceValue).getValues())
                                .allMatch(value -> checkIsLikeType(value, targetType));
                }
            case TypeTags.TUPLE_TAG:
                return Arrays.stream(((BRefValueArray) sourceValue).getValues())
                        .allMatch(value -> checkIsLikeType(value, targetType));
            case TypeTags.ANYDATA_TAG:
                return true;
            case TypeTags.FINITE_TYPE_TAG:
            case TypeTags.UNION_TAG:
                return checkIsLikeType(sourceValue, targetType);
            default:
                return false;
        }
    }

    private static boolean checkIsLikeTupleType(BValue sourceValue, BTupleType targetType) {
        if (!(sourceValue instanceof BRefValueArray)) {
            return false;
        }

        BRefValueArray source = (BRefValueArray) sourceValue;
        if (source.size() != targetType.getTupleTypes().size()) {
            return false;
        }

        return IntStream.range(0, (int) source.size())
                .allMatch(i -> checkIsLikeType(source.get(i), targetType.getTupleTypes().get(i)));
    }

    private static boolean checkIsLikeArrayType(BValue sourceValue, BArrayType targetType) {
        if (!(sourceValue instanceof BRefValueArray)) {
            return false;
        }

        BType arrayElementType = targetType.getElementType();
        BRefType<?>[] arrayValues = ((BRefValueArray) sourceValue).getValues();
        for (int i = 0; i < ((BRefValueArray) sourceValue).size(); i++) {
            if (!checkIsLikeType(arrayValues[i], arrayElementType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeMapType(BValue sourceValue, BMapType targetType) {
        if (!(sourceValue instanceof BMap)) {
            return false;
        }

        for (Object mapEntry : ((BMap) sourceValue).values()) {
            if (!checkIsLikeType((BValue) mapEntry, targetType.getConstrainedType())) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeJSONType(BValue sourceValue, BJSONType targetType) {
        if (targetType.getConstrainedType() != null) {
            return checkIsLikeType(sourceValue, targetType.getConstrainedType());
        } else if (sourceValue.getType().getTag() == TypeTags.ARRAY_TAG) {
            BRefType<?>[] arrayValues = ((BRefValueArray) sourceValue).getValues();
            for (int i = 0; i < ((BRefValueArray) sourceValue).size(); i++) {
                if (!checkIsLikeType(arrayValues[i], targetType)) {
                    return false;
                }
            }
        } else if (sourceValue.getType().getTag() == TypeTags.MAP_TAG) {
            for (BValue value : ((BMap) sourceValue).values()) {
                if (!checkIsLikeType(value, targetType)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIsLikeRecordType(BValue sourceValue, BRecordType targetType) {
        if (!(sourceValue instanceof BMap)) {
            return false;
        }

        Map<String, BType> targetTypeField = new HashMap<>();
        BType restFieldType = targetType.restFieldType;

        for (BField field : targetType.getFields().values()) {
            targetTypeField.put(field.getFieldName(), field.fieldType);
        }

        for (Map.Entry targetTypeEntry : targetTypeField.entrySet()) {
            String fieldName = targetTypeEntry.getKey().toString();

            if (!(((BMap) sourceValue).getMap().containsKey(fieldName)) &&
                    !(Flags.isFlagOn(targetType.getFields().get(fieldName).flags, Flags.OPTIONAL))) {
                return false;
            }
        }

        for (Object object : ((BMap) sourceValue).getMap().entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();

            if (targetTypeField.containsKey(fieldName)) {
                if (!checkIsLikeType(((BValue) valueEntry.getValue()), targetTypeField.get(fieldName))) {
                    return false;
                }
            } else {
                if (!targetType.sealed) {
                    if (!checkIsLikeType(((BValue) valueEntry.getValue()), restFieldType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIsType(BValue sourceVal, BType targetType) {
        if (isMutable(sourceVal)) {
            BType sourceType = sourceVal == null ? BTypes.typeNull : sourceVal.getType();
            return checkIsType(sourceType, targetType, new ArrayList<>());
        }

        return checkIsLikeType(sourceVal, targetType);
    }

    private static boolean checkIsType(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        // First check whether both types are the same.
        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.XML_TAG:
                return sourceType.getTag() == targetType.getTag();
            case TypeTags.MAP_TAG:
                return checkIsMapType(sourceType, (BMapType) targetType, unresolvedTypes);
            case TypeTags.JSON_TAG:
                return checkIsJSONType(sourceType, (BJSONType) targetType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsRecordType(sourceType, (BRecordType) targetType, unresolvedTypes);
            case TypeTags.FUNCTION_POINTER_TAG:
                return checkFunctionCast(sourceType, (BFunctionType) targetType);
            case TypeTags.ARRAY_TAG:
                return checkIsArrayType(sourceType, (BArrayType) targetType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                return checkIsTupleType(sourceType, (BTupleType) targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return ((BUnionType) targetType).getMemberTypes().stream()
                        .anyMatch(type -> checkIsType(sourceType, type, unresolvedTypes));
            case TypeTags.TABLE_TAG:
                return checkIsTableType(sourceType, (BTableType) targetType, unresolvedTypes);
            case TypeTags.ANY_TAG:
                return true;
            case TypeTags.ANYDATA_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
                return isAssignable(sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return checkIsFiniteType(sourceType, (BFiniteType) targetType, unresolvedTypes);
            case TypeTags.FUTURE_TAG:
                return checkIsFutureType(sourceType, (BFutureType) targetType, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean checkIsMapType(BType sourceType, BMapType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.MAP_TAG) {
            return false;
        }
        return checkContraints(((BMapType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkIsJSONType(BType sourceType, BJSONType targetType,
                                         List<TypePair> unresolvedTypes) {
        // If the target is an constrained JSON, then value also should be of
        // constrained JSON type. And the constraints should satisfy 'is type'
        // relationship.
        if (targetType.getConstrainedType() != null) {
            if (sourceType.getTag() != TypeTags.JSON_TAG) {
                return false;
            }

            BType constraintType = ((BJSONType) sourceType).getConstrainedType();
            if (constraintType == null) {
                return false;
            }

            return checkIsType(constraintType, targetType.getConstrainedType(), unresolvedTypes);
        }

        switch (sourceType.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
                return true;
            case TypeTags.JSON_TAG:
                // JSON should be unconstrained
                return targetType.getConstrainedType() == null;
            case TypeTags.ARRAY_TAG:
                // Element type of the array should be 'is type' JSON
                return checkIsType(((BArrayType) sourceType).getElementType(), targetType, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean checkIsRecordType(BType sourceType, BRecordType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.RECORD_TYPE_TAG) {
            return false;
        }

        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        // Unsealed records are not equivalent to sealed records. But vice-versa is allowed.
        BRecordType sourceRecordType = (BRecordType) sourceType;
        if (targetType.sealed && !sourceRecordType.sealed) {
            return false;
        }

        if (targetType.getFields().size() > sourceRecordType.getFields().size()) {
            return false;
        }

        // If both are sealed (one is sealed means other is also sealed) check the rest field type
        if (!sourceRecordType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, BField> sourceFields = sourceRecordType.getFields();

        return targetType.getFields().values().stream().noneMatch(targetField -> {
            BField sourceField = sourceFields.get(targetField.fieldName);
            return sourceField == null || !checkIsType(sourceField.fieldType, targetField.fieldType, unresolvedTypes);
        });
    }

    private static boolean checkIsTableType(BType sourceType, BTableType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TABLE_TAG) {
            return false;
        }
        return checkContraints(((BTableType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkIsArrayType(BType sourceType, BArrayType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.ARRAY_TAG) {
            return false;
        }

        BArrayType sourceArrayType = (BArrayType) sourceType;
        if (sourceArrayType.getState() != targetType.getState() || sourceArrayType.getSize() != targetType.getSize()) {
            return false;
        }
        return checkIsType(sourceArrayType.getElementType(), targetType.getElementType(), unresolvedTypes);
    }

    private static boolean checkIsTupleType(BType sourceType, BTupleType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TUPLE_TAG) {
            return false;
        }

        List<BType> sourceTypes = ((BTupleType) sourceType).getTupleTypes();
        List<BType> targetTypes = targetType.getTupleTypes();
        if (sourceTypes.size() != targetTypes.size()) {
            return false;
        }

        for (int i = 0; i < sourceTypes.size(); i++) {
            if (!checkIsType(sourceTypes.get(i), targetTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsFiniteType(BType sourceType, BFiniteType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        BFiniteType sourceFiniteType = (BFiniteType) sourceType;
        if (sourceFiniteType.valueSpace.size() != targetType.valueSpace.size()) {
            return false;
        }

        return sourceFiniteType.valueSpace.stream().allMatch(value -> targetType.valueSpace.contains(value));
    }

    private static boolean checkIsFutureType(BType sourceType, BFutureType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.FUTURE_TAG) {
            return false;
        }
        return checkContraints(((BFutureType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkContraints(BType sourceConstraint, BType targetConstraint,
                                           List<TypePair> unresolvedTypes) {
        if (sourceConstraint == null) {
            sourceConstraint = BTypes.typeAny;
        }

        if (targetConstraint == null) {
            targetConstraint = BTypes.typeAny;
        }

        return checkIsType(sourceConstraint, targetConstraint, unresolvedTypes);
    }

    private static boolean isMutable(BValue value) {
        if (value == null) {
            return false;
        }

        // All the value types are immutable
        if (value.getType().getTag() < TypeTags.JSON_TAG || value.getType().getTag() == TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        return !value.isFrozen();
    }

    /**
     * Deep value equality check for anydata.
     *
     * @param lhsValue  The value on the left hand side
     * @param rhsValue  The value on the right hand side
     * @return True if values are equal, else false.
     */
    private static boolean isEqual(BValue lhsValue, BValue rhsValue) {
        if (lhsValue == rhsValue) {
            return true;
        }

        if (null == lhsValue || null == rhsValue) {
            return false;
        }

        int lhsValTypeTag = lhsValue.getType().getTag();
        int rhsValTypeTag = rhsValue.getType().getTag();

        switch (lhsValTypeTag) {
            case TypeTags.STRING_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
                return lhsValue.equals(rhsValue);
            case TypeTags.INT_TAG:
                if (rhsValTypeTag == TypeTags.BYTE_TAG) {
                    return ((BInteger) lhsValue).intValue() == (((BByte) rhsValue).intValue());
                }
                return lhsValue.equals(rhsValue);
            case TypeTags.BYTE_TAG:
                if (rhsValTypeTag == TypeTags.INT_TAG) {
                    return ((BByte) lhsValue).intValue() == (((BInteger) rhsValue).intValue());
                }
                return lhsValue.equals(rhsValue);
            case TypeTags.XML_TAG:
                return XMLUtils.isEqual((BXML) lhsValue, (BXML) rhsValue);
            case TypeTags.TABLE_TAG:
                // TODO: 10/8/18
                break;
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return isMappingType(rhsValTypeTag) && isEqual((BMap) lhsValue, (BMap) rhsValue);
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                return isListType(rhsValTypeTag) && isEqual((BNewArray) lhsValue, (BNewArray) rhsValue);
        }
        return false;
    }

    private static boolean isListType(int typeTag) {
        return typeTag == TypeTags.ARRAY_TAG || typeTag == TypeTags.TUPLE_TAG;
    }

    private static boolean isMappingType(int typeTag) {
        return typeTag == TypeTags.MAP_TAG || typeTag == TypeTags.RECORD_TYPE_TAG || typeTag == TypeTags.JSON_TAG;
    }

    /**
     * Deep equality check for an array/tuple.
     *
     * @param lhsList   The array/tuple on the left hand side
     * @param rhsList   The array/tuple on the right hand side
     * @return True if the array/tuple values are equal, else false.
     */
    private static boolean isEqual(BNewArray lhsList, BNewArray rhsList) {
        if (lhsList.size() != rhsList.size()) {
            return false;
        }

        for (int i = 0; i < lhsList.size(); i++) {
            if (!isEqual(lhsList.getBValue(i), rhsList.getBValue(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deep equality check for a map.
     *
     * @param lhsMap    Map on the left hand side
     * @param rhsMap    Map on the right hand side
     * @return True if the map values are equal, else false.
     */
    private static boolean isEqual(BMap lhsMap, BMap rhsMap) {
        if (lhsMap.size() != rhsMap.size()) {
            return false;
        }

        if (!lhsMap.getMap().keySet().containsAll(rhsMap.getMap().keySet())) {
            return false;
        }

        Iterator<Map.Entry<String, BValue>> mapIterator = lhsMap.getMap().entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, BValue> lhsMapEntry = mapIterator.next();
            if (!isEqual(lhsMapEntry.getValue(), rhsMap.get(lhsMapEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Maintains the frozen status of a freezable {@link BValue}.
     *
     * @since 0.985.0
     */
    public static class FreezeStatus {
        /**
         * Representation of the current state of a freeze attempt.
         */
        public enum State {
            FROZEN, MID_FREEZE, UNFROZEN;
        }

        private State currentState;

        public FreezeStatus(State state) {
            this.currentState = state;
        }

        private void setFrozen() {
            this.currentState = State.FROZEN;
        }

        private void setUnfrozen() {
            this.currentState = State.UNFROZEN;
        }

        public State getState() {
            return currentState;
        }

        public boolean isFrozen() {
            return currentState == State.FROZEN;
        }
    }

    /**
     * Reference equality check for values. If both the values are simple basic types, returns the same
     * result as {@link CPU#isEqual(BValue, BValue)}
     *
     * @param lhsValue  The value on the left hand side
     * @param rhsValue  The value on the right hand side
     * @return True if values are reference equal or in the case of simple basic types if the values are equal,
     * else false.
     */
    private static boolean isReferenceEqual(BValue lhsValue, BValue rhsValue) {
        if (lhsValue == rhsValue) {
            return true;
        }

        // if one is null, the other also needs to be null to be true
        if (lhsValue == null || rhsValue == null) {
            return false;
        }

        if (isSimpleBasicType(lhsValue.getType()) && isSimpleBasicType(rhsValue.getType())) {
            return isEqual(lhsValue, rhsValue);
        }

        return false;
    }

    /**
     * Reference inequality check for values. If both the values are simple basic types, returns the same
     * result as the negation of {@link CPU#isEqual(BValue, BValue)}
     *
     * @param lhsValue  The value on the left hand side
     * @param rhsValue  The value on the right hand side
     * @return True if values are not reference equal or in the case of simple basic types if the values are not equal,
     * else false.
     */
    private static boolean isReferenceInequal(BValue lhsValue, BValue rhsValue) {
        if (lhsValue == null || rhsValue == null) {
            return lhsValue != rhsValue;
        }

        if (isSimpleBasicType(lhsValue.getType()) && isSimpleBasicType(rhsValue.getType())) {
            return !isEqual(lhsValue, rhsValue);
        }

        return lhsValue != rhsValue;
    }

    private static boolean isSimpleBasicType(BType type) {
        return type.getTag() < TypeTags.JSON_TAG;
    }

    /**
     * Type vector of size two, to hold the source and the target types.
     *
     * @since 0.982.0
     */
    private static class TypePair {
        BType sourceType;
        BType targetType;

        public TypePair(BType sourceType, BType targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypePair)) {
                return false;
            }

            TypePair other = (TypePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }
    }
}
