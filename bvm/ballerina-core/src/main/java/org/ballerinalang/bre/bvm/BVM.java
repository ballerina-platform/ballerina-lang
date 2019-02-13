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
import org.ballerinalang.bre.BLangCallableUnitCallback;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.NativeCallContext;
import org.ballerinalang.bre.old.WorkerExecutionContext;
import org.ballerinalang.channels.ChannelManager;
import org.ballerinalang.channels.ChannelRegistry;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BAttachedFunction;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BFutureType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BObjectType;
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
import org.ballerinalang.model.util.DecimalValueKind;
import org.ballerinalang.model.util.Flags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.ListUtils;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BCallableFuture;
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BCollection;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BFuture;
import org.ballerinalang.model.values.BIntRange;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.model.values.BXMLQName;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.Transactions;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ErrorTableEntry;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.Instruction.InstructionCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionIteratorNext;
import org.ballerinalang.util.codegen.Instruction.InstructionLock;
import org.ballerinalang.util.codegen.Instruction.InstructionTrBegin;
import org.ballerinalang.util.codegen.Instruction.InstructionTrEnd;
import org.ballerinalang.util.codegen.Instruction.InstructionTrRetry;
import org.ballerinalang.util.codegen.Instruction.InstructionUnLock;
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
import org.ballerinalang.util.exceptions.BLangMapStoreException;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.TransactionConstants;
import org.ballerinalang.util.transactions.TransactionLocalContext;
import org.ballerinalang.util.transactions.TransactionResourceManager;
import org.ballerinalang.util.transactions.TransactionUtils;
import org.wso2.ballerinalang.compiler.util.BArrayState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.ballerinalang.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.util.BLangConstants.BBYTE_MIN_VALUE;
import static org.ballerinalang.util.BLangConstants.BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX;
import static org.ballerinalang.util.BLangConstants.BINT_MAX_VALUE_DOUBLE_RANGE_MAX;
import static org.ballerinalang.util.BLangConstants.BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN;
import static org.ballerinalang.util.BLangConstants.BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
import static org.ballerinalang.util.BLangConstants.STRING_NULL_VALUE;

/**
 * This class executes Ballerina instruction codes by acting as a VM.
 *
 * @since 0.985.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BVM {

    /**
     * This is to be used to continue the execution of a strand when a non blocking call continues.
     *
     * @param strand to be executed
     */
    static void execute(Strand strand) {
        int i, j, k, l;
        int cpIndex;
        FunctionCallCPEntry funcCallCPEntry;
        FunctionRefCPEntry funcRefCPEntry;
        TypeRefCPEntry typeRefCPEntry;
        FunctionInfo functionInfo;
        InstructionCALL callIns;

        boolean debugEnabled = strand.programFile.getDebugger().isDebugEnabled();

        int callersRetRegIndex;

        StackFrame sf = strand.currentFrame;

        while (sf.ip >= 0) {
            if (strand.aborted) {
                handleFutureTermination(strand);
                return;
            }
            if (debugEnabled && debug(strand)) {
                return;
            }

            Instruction instruction = sf.code[sf.ip];
            int opcode = instruction.getOpcode();
            int[] operands = instruction.getOperands();
            sf.ip++;

            switch (opcode) {
                case InstructionCodes.ICONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.longRegs[i] = ((IntegerCPEntry) sf.constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.FCONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.doubleRegs[i] = ((FloatCPEntry) sf.constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.DCONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    String decimalVal = ((UTF8CPEntry) sf.constPool[cpIndex]).getValue();
                    sf.refRegs[i] = new BDecimal(new BigDecimal(decimalVal, MathContext.DECIMAL128));
                    break;
                case InstructionCodes.SCONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.stringRegs[i] = ((StringCPEntry) sf.constPool[cpIndex]).getValue();
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
                    sf.intRegs[i] = ((ByteCPEntry) sf.constPool[cpIndex]).getValue();
                    break;
                case InstructionCodes.BACONST:
                    cpIndex = operands[0];
                    i = operands[1];
                    sf.refRegs[i] = new BValueArray(((BlobCPEntry) sf.constPool[cpIndex]).getValue());
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
                    execLoadOpcodes(strand, sf, opcode, operands);
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
                    execStoreOpcodes(strand, sf, opcode, operands);
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
                    execBinaryOpCodes(strand, sf, opcode, operands);
                    break;

                case InstructionCodes.LENGTH:
                    calculateLength(strand, operands, sf);
                    break;
                case InstructionCodes.TYPELOAD:
                    cpIndex = operands[0];
                    j = operands[1];
                    TypeRefCPEntry typeEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
                    sf.refRegs[j] = new BTypeDescValue(typeEntry.getType());
                    break;
                case InstructionCodes.HALT:
                    if (strand.fp > 0) {
                        // Stop the observation context before popping the stack frame
                        ObserveUtils.stopCallableObservation(strand);
                        strand.popFrame();
                        break;
                    }
                    sf.ip = -1;
                    strand.respCallback.signal();
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
                    execCmpAndBranchOpcodes(strand, sf, opcode, operands);
                    break;
                case InstructionCodes.INT_RANGE:
                    execIntegerRangeOpcodes(sf, operands);
                    break;
                case InstructionCodes.TR_RETRY:
                    InstructionTrRetry trRetry = (InstructionTrRetry) instruction;
                    retryTransaction(strand, trRetry.blockId, trRetry.abortEndIp, trRetry.trStatusReg);
                    break;
                case InstructionCodes.CALL:
                    callIns = (InstructionCALL) instruction;
                    strand = invokeCallable(strand, callIns.functionInfo,
                            callIns.argRegs, callIns.retRegs[0], callIns.flags);
                    if (strand == null) {
                        return;
                    }
                    break;
                case InstructionCodes.VCALL:
                    InstructionVCALL vcallIns = (InstructionVCALL) instruction;
                    strand = invokeVirtualFunction(strand, sf, vcallIns.receiverRegIndex, vcallIns.functionInfo,
                            vcallIns.argRegs, vcallIns.retRegs[0], vcallIns.flags);
                    if (strand == null) {
                        return;
                    }
                    break;
                case InstructionCodes.TR_BEGIN:
                    InstructionTrBegin trBegin = (InstructionTrBegin) instruction;
                    beginTransaction(strand, trBegin.transactionType, trBegin.blockId, trBegin.retryCountReg,
                            trBegin.committedFuncIndex, trBegin.abortedFuncIndex);
                    break;
                case InstructionCodes.TR_END:
                    InstructionTrEnd trEnd = (InstructionTrEnd) instruction;
                    endTransaction(strand, trEnd.blockId, trEnd.endType, trEnd.statusRegIndex, trEnd.errorRegIndex);
                    break;
                case InstructionCodes.WRKSEND:
                    InstructionWRKSendReceive wrkSendIns = (InstructionWRKSendReceive) instruction;
                    handleWorkerSend(strand, wrkSendIns.dataChannelInfo, wrkSendIns.type,
                            wrkSendIns.reg, wrkSendIns.channelInSameStrand);
                    break;
                case InstructionCodes.WRKRECEIVE:
                    InstructionWRKSendReceive wrkReceiveIns = (InstructionWRKSendReceive) instruction;
                    if (!handleWorkerReceive(strand, wrkReceiveIns.dataChannelInfo, wrkReceiveIns.type,
                            wrkReceiveIns.reg, wrkReceiveIns.channelInSameStrand)) {
                        return;
                    }
                    break;
                case InstructionCodes.CHNRECEIVE:
                    Instruction.InstructionCHNReceive chnReceiveIns =
                            (Instruction.InstructionCHNReceive) instruction;
                    if (!handleCHNReceive(strand, chnReceiveIns.channelName, chnReceiveIns.receiverType,
                            chnReceiveIns.receiverReg, chnReceiveIns.keyType, chnReceiveIns.keyReg)) {
                        return;
                    }
                    break;
                case InstructionCodes.CHNSEND:
                    Instruction.InstructionCHNSend chnSendIns = (Instruction.InstructionCHNSend) instruction;
                    handleCHNSend(strand, chnSendIns.channelName, chnSendIns.dataType, chnSendIns.dataReg,
                            chnSendIns.keyType, chnSendIns.keyReg);
                    break;
                case InstructionCodes.FLUSH:
                    Instruction.InstructionFlush flushIns = (Instruction.InstructionFlush) instruction;
                    if (!WaitCallbackHandler.handleFlush(strand, flushIns.retReg, flushIns.channels)) {
                        return;
                    }
                    break;
                case InstructionCodes.WORKERSYNCSEND:
                    Instruction.InstructionWRKSyncSend syncSendIns = (Instruction.InstructionWRKSyncSend) instruction;
                    if (!handleWorkerSyncSend(strand, syncSendIns.dataChannelInfo, syncSendIns.type, syncSendIns.reg,
                            syncSendIns.retReg, syncSendIns.isSameStrand)) {
                        return;
                    }
                    //worker data channel will resume this upon data retrieval or error
                    break;
                case InstructionCodes.PANIC:
                    i = operands[0];
                    if (i >= 0) {
                        BError error = (BError) sf.refRegs[i];
                        if (error == null) {
                            //TODO do we need this null check?
                            handleNullRefError(strand);
                            break;
                        }
                        strand.setError(error);
                    }
                    handleError(strand);
                    break;
                case InstructionCodes.ERROR:
                    createNewError(operands, strand, sf);
                    break;
                case InstructionCodes.REASON:
                case InstructionCodes.DETAIL:
                    handleErrorBuiltinMethods(opcode, operands, sf);
                    break;
                case InstructionCodes.IS_FROZEN:
                case InstructionCodes.FREEZE:
                    handleFreezeBuiltinMethods(strand, opcode, operands, sf);
                    break;
                case InstructionCodes.STAMP:
                    handleStampBuildInMethod(strand, operands, sf);
                    break;
                case InstructionCodes.CONVERT:
                    handleConvertBuildInMethod(strand, sf, operands);
                    break;
                case InstructionCodes.FPCALL:
                    i = operands[0];
                    if (sf.refRegs[i] == null) {
                        handleNullRefError(strand);
                        break;
                    }
                    cpIndex = operands[1];
                    funcCallCPEntry = (FunctionCallCPEntry) sf.constPool[cpIndex];
                    functionInfo = ((BFunctionPointer) sf.refRegs[i]).value();
                    strand = invokeCallable(strand, (BFunctionPointer) sf.refRegs[i], funcCallCPEntry,
                            functionInfo, sf, funcCallCPEntry.getFlags());
                    if (strand == null) {
                        return;
                    }
                    break;
                case InstructionCodes.FPLOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    funcRefCPEntry = (FunctionRefCPEntry) sf.constPool[i];
                    typeEntry = (TypeRefCPEntry) sf.constPool[k];
                    BFunctionPointer functionPointer = new BFunctionPointer(funcRefCPEntry.getFunctionInfo(),
                            typeEntry.getType());
                    sf.refRegs[j] = functionPointer;
                    findAndAddAdditionalVarRegIndexes(sf, operands, functionPointer);
                    break;
                case InstructionCodes.VFPLOAD:
                    i = operands[0];
                    j = operands[1];
                    k = operands[2];
                    int m = operands[5];
                    funcRefCPEntry = (FunctionRefCPEntry) sf.constPool[i];
                    typeEntry = (TypeRefCPEntry) sf.constPool[k];

                    BMap<String, BValue> structVal = (BMap<String, BValue>) sf.refRegs[m];
                    if (structVal == null) {
                        handleNullRefError(strand);
                        break;
                    }

                    StructureTypeInfo structInfo = (ObjectTypeInfo) ((BStructureType)
                            structVal.getType()).getTypeInfo();
                    FunctionInfo attachedFuncInfo = structInfo.funcInfoEntries
                            .get(funcRefCPEntry.getFunctionInfo().getName());

                    BFunctionPointer fPointer = new BFunctionPointer(attachedFuncInfo, typeEntry.getType());
                    sf.refRegs[j] = fPointer;
                    findAndAddAdditionalVarRegIndexes(sf, operands, fPointer);
                    break;

                case InstructionCodes.CLONE:
                    createClone(strand, operands, sf);
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
                case InstructionCodes.TYPE_ASSERTION:
                    execTypeCastOpcodes(strand, sf, opcode, operands);
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
                    execTypeConversionOpcodes(strand, sf, opcode, operands);
                    break;

                case InstructionCodes.INEWARRAY:
                    i = operands[0];
                    j = operands[2];
                    sf.refRegs[i] = new BValueArray(BTypes.typeInt, (int) sf.longRegs[j]);
                    break;
                case InstructionCodes.BINEWARRAY:
                    i = operands[0];
                    j = operands[2];
                    sf.refRegs[i] = new BValueArray(BTypes.typeByte, (int) sf.longRegs[j]);
                    break;
                case InstructionCodes.FNEWARRAY:
                    i = operands[0];
                    j = operands[2];
                    sf.refRegs[i] = new BValueArray(BTypes.typeFloat, (int) sf.longRegs[j]);
                    break;
                case InstructionCodes.SNEWARRAY:
                    i = operands[0];
                    j = operands[2];
                    sf.refRegs[i] = new BValueArray(BTypes.typeString, (int) sf.longRegs[j]);
                    break;
                case InstructionCodes.BNEWARRAY:
                    i = operands[0];
                    j = operands[2];
                    sf.refRegs[i] = new BValueArray(BTypes.typeBoolean, (int) sf.longRegs[j]);
                    break;
                case InstructionCodes.RNEWARRAY:
                    i = operands[0];
                    cpIndex = operands[1];
                    typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
                    sf.refRegs[i] = new BValueArray(typeRefCPEntry.getType());
                    break;
                case InstructionCodes.NEWSTRUCT:
                    createNewStruct(operands, sf);
                    break;
                case InstructionCodes.NEWMAP:
                    i = operands[0];
                    cpIndex = operands[1];
                    typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
                    sf.refRegs[i] = new BMap<String, BRefType>(typeRefCPEntry.getType());
                    break;
                case InstructionCodes.NEWTABLE:
                    i = operands[0];
                    cpIndex = operands[1];
                    j = operands[2];
                    k = operands[3];
                    l = operands[4];
                    typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
                    BValueArray indexColumns = (BValueArray) sf.refRegs[j];
                    BValueArray keyColumns = (BValueArray) sf.refRegs[k];
                    BValueArray dataRows = (BValueArray) sf.refRegs[l];
                    try {
                        sf.refRegs[i] = new BTable(typeRefCPEntry.getType(), indexColumns, keyColumns, dataRows);
                    } catch (BallerinaException e) {
                        strand.setError(BLangVMErrors.createError(strand, e.getMessage()));
                        handleError(strand);
                    }
                    break;
                case InstructionCodes.NEWSTREAM:
                    i = operands[0];
                    cpIndex = operands[1];
                    typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
                    StringCPEntry name = (StringCPEntry) sf.constPool[operands[2]];
                    BStream stream = new BStream(typeRefCPEntry.getType(), name.getValue());
                    sf.refRegs[i] = stream;
                    break;
                case InstructionCodes.IRET:
                        j = operands[0];
                        if (strand.fp > 0) {
                            StackFrame pf = strand.peekFrame(1);
                            callersRetRegIndex = sf.retReg;
                            pf.longRegs[callersRetRegIndex] = sf.longRegs[j];
                        } else {
                            strand.respCallback.setIntReturn(sf.longRegs[j]);
                        }
                        break;
                    case InstructionCodes.FRET:
                        j = operands[0];
                        if (strand.fp > 0) {
                            StackFrame pf = strand.peekFrame(1);
                            callersRetRegIndex = sf.retReg;
                            pf.doubleRegs[callersRetRegIndex] = sf.doubleRegs[j];
                        } else {
                            strand.respCallback.setFloatReturn(sf.doubleRegs[j]);
                        }
                        break;
                    case InstructionCodes.SRET:
                        j = operands[0];
                        if (strand.fp > 0) {
                            StackFrame pf = strand.peekFrame(1);
                            callersRetRegIndex = sf.retReg;
                            pf.stringRegs[callersRetRegIndex] = sf.stringRegs[j];
                        } else {
                            strand.respCallback.setStringReturn(sf.stringRegs[j]);
                        }
                        break;
                    case InstructionCodes.BRET:
                        j = operands[0];
                        if (strand.fp > 0) {
                            StackFrame pf = strand.peekFrame(1);
                            callersRetRegIndex = sf.retReg;
                            pf.intRegs[callersRetRegIndex] = sf.intRegs[j];
                        } else {
                            strand.respCallback.setBooleanReturn(sf.intRegs[j]);
                        }
                        break;
                    case InstructionCodes.DRET:
                    case InstructionCodes.RRET:
                        j = operands[0];
                        if (strand.fp > 0) {
                            StackFrame pf = strand.peekFrame(1);
                            callersRetRegIndex = sf.retReg;
                            pf.refRegs[callersRetRegIndex] = sf.refRegs[j];
                        } else {
                            strand.respCallback.setRefReturn(sf.refRegs[j]);
                        }
                        if (checkIsType(sf.refRegs[j], BTypes.typeError)) {
                            sf.errorRetReg = j;
                        }
                        break;
                    case InstructionCodes.RET:
                        if (strand.fp > 0) {
                            // Stop the observation context before popping the stack frame
                            ObserveUtils.stopCallableObservation(strand);
                            if (sf.errorRetReg > -1) {
                                //notifying waiting workers
                                sf.handleChannelError(sf.refRegs[sf.errorRetReg], strand.peekFrame(1).wdChannels);
                            }
                            strand.popFrame();
                            break;
                        }
                        if (sf.errorRetReg > -1) {
                            //notifying waiting workers
                            sf.handleChannelError(sf.refRegs[sf.errorRetReg], strand.respCallback.parentChannels);
                        }
                        sf.ip = -1;
                        strand.respCallback.signal();
                        return;
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
                        execXMLOpcodes(strand, sf, opcode, operands);
                        break;
                    case InstructionCodes.ITR_NEW:
                    case InstructionCodes.ITR_NEXT:
                    execIteratorOperation(strand, sf, instruction);
                    break;
                case InstructionCodes.LOCK:
                    InstructionLock instructionLock = (InstructionLock) instruction;
                    if (!handleVariableLock(strand, instructionLock.types, instructionLock.pkgRefs,
                            instructionLock.varRegs, instructionLock.fieldRegs, instructionLock.varCount,
                            instructionLock.uuid)) {
                        return;
                    }
                    break;
                case InstructionCodes.UNLOCK:
                    InstructionUnLock instructionUnLock = (InstructionUnLock) instruction;
                    handleVariableUnlock(strand, instructionUnLock.types, instructionUnLock.pkgRefs,
                            instructionUnLock.varRegs, instructionUnLock.varCount, instructionUnLock.uuid,
                            instructionUnLock.hasFieldVar);
                    break;
                case InstructionCodes.WAIT:
                    if (!execWait(strand, operands)) {
                        return;
                    }
                    break;
                case InstructionCodes.WAITALL:
                    if (!execWaitForAll(strand, operands)) {
                        return;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            sf = strand.currentFrame;
        }
    }

    private static void handleFutureTermination(Strand strand) {
        // Set error to strand and callback
        BError error = BLangVMErrors.createCancelledFutureError(strand);
        strand.setError(error);
        ((SafeStrandCallback) strand.respCallback).setErrorForCancelledFuture(error);
        // Make the ip of current frame to -1
        strand.currentFrame.ip = -1;
        // Panic all stack frames in the strand
        panicStackFrame(strand);
        // Signal transactions for errors
        signalTransactionError(strand, StackFrame.TransactionParticipantType.REMOTE_PARTICIPANT);
        strand.respCallback.signal();
    }

    private static void panicStackFrame(Strand strand) {
        if (strand.fp < 0) {
            return;
        }
        StackFrame poppedFrame = strand.popFrame();
        // Stop observation
        ObserveUtils.stopObservation(poppedFrame.observerContext);
        // Panic channels in the current frame
        poppedFrame.handleChannelPanic(strand.getError(), poppedFrame.wdChannels);
        // Signal transactions for errors
        signalTransactionError(strand, poppedFrame.trxParticipant);
        panicStackFrame(strand);
    }

    private static boolean handleWorkerSyncSend(Strand strand, WorkerDataChannelInfo dataChannelInfo, BType type,
                                                int reg, int retReg, boolean isSameStrand) {
        BRefType val = extractValue(strand.currentFrame, type, reg);
        WorkerDataChannel dataChannel = getWorkerChannel(strand, dataChannelInfo.getChannelName(), isSameStrand);
        return dataChannel.syncSendData(val, strand, retReg);
    }

    private static void createClone(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];

        BRefType<?> refRegVal = sf.refRegs[i];

        if (refRegVal == null) {
            return;
        }

        if (!checkIsLikeType(refRegVal, BTypes.typeAnydata)) {
            sf.refRegs[j] =
                    BLangVMErrors.createError(ctx, BallerinaErrorReasons.CLONE_ERROR,
                                              BLangExceptionHelper.getErrorMessage(
                                                      RuntimeErrors.UNSUPPORTED_CLONE_OPERATION, refRegVal.getType()));
            return;
        }
        sf.refRegs[j] = (BRefType<?>) refRegVal.copy(new HashMap<>());
    }

    private static Strand invokeCallable(Strand strand, CallableUnitInfo callableUnitInfo,
                                       int[] argRegs, int retReg, int flags) {
        //TODO refactor when worker info is removed from compiler
        StackFrame df = new StackFrame(callableUnitInfo.getPackageInfo(), callableUnitInfo,
                callableUnitInfo.getDefaultWorkerInfo().getCodeAttributeInfo(), retReg, flags,
                callableUnitInfo.workerSendInChannels);
        copyArgValues(strand.currentFrame, df, argRegs, callableUnitInfo.getParamTypes());

        if (!FunctionFlags.isAsync(df.invocationFlags)) {
            try {
                strand.pushFrame(df);
            } catch (ArrayIndexOutOfBoundsException e) {
                // Need to decrement the frame pointer count. Otherwise ArrayIndexOutOfBoundsException will
                // be thrown from the popFrame() as well.
                strand.fp--;
                strand.setError(BLangVMErrors.createError(strand, BallerinaErrorReasons.STACK_OVERFLOW_ERROR,
                        "stack overflow"));
                handleError(strand);
                return strand;
            }
            // Start observation after pushing the stack frame
            ObserveUtils.startCallableObservation(strand, df.invocationFlags);
            if (callableUnitInfo.isNative()) {
                return invokeNativeCallable(callableUnitInfo, strand, df, retReg, df.invocationFlags);
            }
            return strand;
        }

        SafeStrandCallback strandCallback = new SafeStrandCallback(callableUnitInfo.getRetParamTypes()[0],
                strand.currentFrame.wdChannels, callableUnitInfo.workerSendInChannels);

        Strand calleeStrand = new Strand(strand.programFile, callableUnitInfo.getName(),
                strand.globalProps, strandCallback);
        calleeStrand.pushFrame(df);
        // Start observation after pushing the stack frame
        ObserveUtils.startCallableObservation(calleeStrand, strand.respCallback.getObserverContext());
        if (callableUnitInfo.isNative()) {
            Context nativeCtx = new NativeCallContext(calleeStrand, callableUnitInfo, df);
            NativeCallableUnit nativeCallable = callableUnitInfo.getNativeCallableUnit();
            if (nativeCallable.isBlocking()) {
                BVMScheduler.scheduleNative(nativeCallable, nativeCtx, null);
            } else {
                BLangCallableUnitCallback callableUnitCallback = new BLangCallableUnitCallback(nativeCtx,
                        calleeStrand, retReg, callableUnitInfo.getRetParamTypes()[0]);
                BVMScheduler.executeNative(nativeCallable, nativeCtx, callableUnitCallback);
            }
        } else {
            BVMScheduler.schedule(calleeStrand);
        }
        strand.currentFrame.refRegs[retReg] = new BCallableFuture(callableUnitInfo.getName(), calleeStrand);
        return strand;
    }

    private static Strand invokeNativeCallable(CallableUnitInfo callableUnitInfo, Strand strand,
                                               StackFrame sf, int retReg, int flags) {
        BType retType = callableUnitInfo.getRetParamTypes()[0];

        Context ctx = new NativeCallContext(strand, callableUnitInfo, sf);
        NativeCallableUnit nativeCallable = callableUnitInfo.getNativeCallableUnit();
        try {
            if (nativeCallable.isBlocking()) {
                nativeCallable.execute(ctx, null);

                if (strand.fp > 0) {
                    // Stop the observation context before popping the stack frame
                    ObserveUtils.stopCallableObservation(strand);
                    if (BVM.checkIsType(ctx.getReturnValue(), BTypes.typeError)) {
                        strand.currentFrame.handleChannelError((BRefType) ctx.getReturnValue(),
                                strand.peekFrame(1).wdChannels);
                    }
                    strand.popFrame();
                    StackFrame retFrame = strand.currentFrame;
                    BLangVMUtils.populateWorkerDataWithValues(retFrame, retReg, ctx.getReturnValue(), retType);
                    return strand;
                }
                if (BVM.checkIsType(ctx.getReturnValue(), BTypes.typeError)) {
                    strand.currentFrame.handleChannelError((BRefType) ctx.getReturnValue(),
                            strand.respCallback.parentChannels);
                }
                strand.respCallback.signal();
                return null;
            }
            CallableUnitCallback callback = new BLangCallableUnitCallback(ctx, strand, retReg, retType);
            nativeCallable.execute(ctx, callback);
            return null;
        } catch (BLangNullReferenceException e) {
            strand.setError(BLangVMErrors.createNullRefException(strand));
        } catch (BallerinaException e) {
            strand.setError(BLangVMErrors.createError(strand, e.getMessage(), e.getDetail()));
        } catch (Throwable e) {
            strand.setError(BLangVMErrors.createError(strand, e.getMessage()));
        }
        // Stop the observation context before popping the stack frame
        ObserveUtils.stopCallableObservation(strand);
        if (strand.fp > 0) {
            strand.currentFrame.handleChannelPanic(strand.getError(), strand.peekFrame(1).wdChannels);
            strand.popFrame();
        } else {
            strand.currentFrame.handleChannelPanic(strand.getError(), strand.respCallback.parentChannels);
            strand.popFrame();
        }
        handleError(strand);
        return strand;
    }

    private static void copyArgValues(StackFrame caller, StackFrame callee, int[] argRegs, BType[] paramTypes) {
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
                    callee.longRegs[++longRegIndex] = caller.longRegs[argReg];
                    break;
                case TypeTags.BYTE_TAG:
                    callee.intRegs[++booleanRegIndex] = caller.intRegs[argReg];
                    break;
                case TypeTags.FLOAT_TAG:
                    callee.doubleRegs[++doubleRegIndex] = caller.doubleRegs[argReg];
                    break;
                case TypeTags.STRING_TAG:
                    callee.stringRegs[++stringRegIndex] = caller.stringRegs[argReg];
                    break;
                case TypeTags.BOOLEAN_TAG:
                    callee.intRegs[++booleanRegIndex] = caller.intRegs[argReg];
                    break;
                default:
                    callee.refRegs[++refRegIndex] = caller.refRegs[argReg];
            }
        }
    }

    private static void createNewError(int[] operands, Strand strand, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];
        int l = operands[3];
        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) sf.constPool[i];
        sf.refRegs[l] = (BRefType<?>) BLangVMErrors
                .createError(strand, true, (BErrorType) typeRefCPEntry.getType(), sf.stringRegs[j],
                        (BMap<String, BValue>) sf.refRegs[k]);
    }

    private static void handleErrorBuiltinMethods(int opcode, int[] operands, StackFrame sf) {
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

    private static void handleFreezeBuiltinMethods(Strand ctx, int opcode, int[] operands,
                                                   StackFrame sf) {
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
                    sf.refRegs[j] = BLangVMErrors.createError(ctx, BallerinaErrorReasons.FREEZE_ERROR, e.getMessage());
                } catch (BallerinaException e) {
                    // if freeze is unsuccessful due to concurrent freeze attempts, set the frozen status of the value
                    // and its constituents to false, and panic
                    freezeStatus.setUnfrozen();
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.IS_FROZEN:
                sf.intRegs[j] = (value == null || value.isFrozen()) ? 1 : 0;
                break;
        }
    }

    private static void handleStampBuildInMethod(Strand ctx, int[] operands, StackFrame sf) {

        int i = operands[0];
        int j = operands[1];
        int k = operands[2];

        BRefType<?> valueToBeStamped = sf.refRegs[i];
        BType stampType = ((TypeRefCPEntry) sf.constPool[j]).getType();
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
            BError error;
            if (valueToBeStamped != null) {
                error = BLangVMErrors.createError(ctx, BallerinaErrorReasons.STAMP_ERROR,
                        BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_STAMP_OPERATION,
                                valueToBeStamped.getType(), targetType));
            } else {
                error = BLangVMErrors.createError(ctx, BallerinaErrorReasons.STAMP_ERROR,
                        BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_STAMP_NULL, targetType));
            }
            sf.refRegs[k] = error;
            return;
        }
        try {
            if (valueToBeStamped != null) {
                valueToBeStamped.stamp(targetType, new ArrayList<>());
            }
            sf.refRegs[k] = valueToBeStamped;
        } catch (BallerinaException e) {
            BError error = BLangVMErrors.createError(ctx, BallerinaErrorReasons.STAMP_ERROR, e.getDetail());
            sf.refRegs[k] = error;
        }
    }

    private static void handleConvertBuildInMethod(Strand strand, StackFrame sf, int[] operands) {

        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
        BRefType bRefTypeValue = sf.refRegs[i];
        if (bRefTypeValue == null) {
            sf.refRegs[j] = null;
            return;
        }
        int targetTag = typeRefCPEntry.getType().getTag();
        try {
            if (BTypes.isValueType(bRefTypeValue.getType())) {
                convertValueTypes(strand, sf, j, typeRefCPEntry, bRefTypeValue, targetTag);
                return;
            }
            if (targetTag == TypeTags.STRING_TAG) {
                sf.refRegs[j] = new BString(bRefTypeValue.toString());
                return;
            }
            handleTypeConversionError(strand, sf, j, bRefTypeValue.getType(), typeRefCPEntry.getType());
        } catch (RuntimeException e) {
            handleTypeConversionError(strand, sf, j, bRefTypeValue.getType(), typeRefCPEntry.getType());
        }
    }

    private static void convertValueTypes(Strand strand, StackFrame sf, int resultRegIndex,
                                          TypeRefCPEntry typeRefCPEntry, BRefType bRefTypeValue, int targetTag) {
        switch (targetTag) {
            case TypeTags.INT_TAG:
                sf.refRegs[resultRegIndex] = new BInteger(((BValueType) bRefTypeValue).intValue());
                break;
            case TypeTags.FLOAT_TAG:
                sf.refRegs[resultRegIndex] = new BFloat(((BValueType) bRefTypeValue).floatValue());
                break;
            case TypeTags.DECIMAL_TAG:
                sf.refRegs[resultRegIndex] = new BDecimal(((BValueType) bRefTypeValue).decimalValue());
                break;
            case TypeTags.STRING_TAG:
                sf.refRegs[resultRegIndex] = new BString(bRefTypeValue.toString());
                break;
            case TypeTags.BOOLEAN_TAG:
                sf.refRegs[resultRegIndex] = new BBoolean(((BValueType) bRefTypeValue).booleanValue());
                break;
            case TypeTags.BYTE_TAG:
                sf.refRegs[resultRegIndex] = new BByte(((BValueType) bRefTypeValue).byteValue());
                break;
            default:
                handleTypeConversionError(strand, sf, resultRegIndex, bRefTypeValue.getType(),
                                          typeRefCPEntry.getType());
        }
    }

    /**
     * Handle sending a message to a channel. If there is a worker already waiting to accept this message, it is
     * resumed.
     *
     * @param ctx         Current worker context
     * @param channelName Name os the channel to get the message
     * @param dataType    Type od the message
     * @param dataReg     Registry location of the message
     * @param keyType     Type of message key
     * @param keyReg      message key registry index
     */
    private static void handleCHNSend(Strand ctx, String channelName, BType dataType, int dataReg, BType keyType,
                                      int keyReg) {
        BRefType keyVal = null;
        if (keyType != null) {
            keyVal = extractValue(ctx.currentFrame, keyType, keyReg);
        }
        BRefType dataVal = extractValue(ctx.currentFrame, dataType, dataReg);
        ChannelRegistry.PendingContext pendingCtx = ChannelManager.channelSenderAction(channelName, keyVal, dataVal,
                keyType, dataType);
        if (pendingCtx != null) {
            //inject the value to the ctx
            copyArgValueForWorkerReceive(pendingCtx.context.currentFrame, pendingCtx.regIndex, dataType, dataVal);
            BVMScheduler.schedule(pendingCtx.context);
        }
    }

    /**
     * Handles message receiving using a channel.
     * If the expected message is already available, it is assigned to the receiver reg and returns true.
     *
     * @param ctx          Current worker context
     * @param channelName  Name os the channel to get the message
     * @param receiverType Type of the expected message
     * @param receiverReg  Registry index of the receiving message
     * @param keyType      Type of message key
     * @param keyIndex     message key registry index
     * @return true if a matching value is available
     */
    private static boolean handleCHNReceive(Strand ctx, String channelName, BType receiverType,
                                            int receiverReg, BType keyType, int keyIndex) {
        BValue keyVal = null;
        if (keyType != null) {
            keyVal = extractValue(ctx.currentFrame, keyType, keyIndex);
        }
        BValue value = ChannelManager.channelReceiverAction(channelName, keyVal, keyType, ctx, receiverReg,
                receiverType);
        if (value != null) {
            copyArgValueForWorkerReceive(ctx.currentFrame, receiverReg, receiverType, (BRefType) value);
            return true;
        }

        return false;
    }

    private static Strand invokeCallable(Strand ctx, BFunctionPointer fp, FunctionCallCPEntry funcCallCPEntry,
                                         FunctionInfo functionInfo, StackFrame sf, int flags) {
        List<BClosure> closureVars = fp.getClosureVars();
        int[] argRegs = funcCallCPEntry.getArgRegs();
        if (closureVars.isEmpty()) {
            return invokeCallable(ctx, functionInfo, argRegs, funcCallCPEntry.getRetRegs()[0], flags);
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

        return invokeCallable(ctx, functionInfo, newArgRegs, funcCallCPEntry.getRetRegs()[0], flags);
    }

    private static int expandLongRegs(StackFrame sf, BFunctionPointer fp) {
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

    private static int expandIntRegs(StackFrame sf, BFunctionPointer fp) {
        int intIndex = 0;
        if (fp.getAdditionalIndexCount(BTypes.typeBoolean.getTag()) > 0 ||
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

    private static int expandDoubleRegs(StackFrame sf, BFunctionPointer fp) {
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

    private static int expandStringRegs(StackFrame sf, BFunctionPointer fp) {
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

    private static int expandRefRegs(StackFrame sf, BFunctionPointer fp) {
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

    private static void findAndAddAdditionalVarRegIndexes(StackFrame sf, int[] operands,
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
                    fp.addClosureVar(new BClosure(new BInteger(sf.longRegs[index]), BTypes.typeInt),
                            TypeTags.INT_TAG);
                    break;
                }
                case TypeTags.BYTE_TAG: {
                    fp.addClosureVar(new BClosure(new BByte((byte) sf.intRegs[index]), BTypes.typeByte),
                            TypeTags.BYTE_TAG);
                    break;
                }
                case TypeTags.FLOAT_TAG: {
                    fp.addClosureVar(new BClosure(new BFloat(sf.doubleRegs[index]), BTypes.typeFloat),
                            TypeTags.FLOAT_TAG);
                    break;
                }
                case TypeTags.DECIMAL_TAG: {
                    fp.addClosureVar(new BClosure(sf.refRegs[index], BTypes.typeDecimal),
                            TypeTags.DECIMAL_TAG);
                    break;
                }
                case TypeTags.BOOLEAN_TAG: {
                    fp.addClosureVar(new BClosure(new BBoolean(sf.intRegs[index] == 1),
                            BTypes.typeBoolean), TypeTags.BOOLEAN_TAG);
                    break;
                }
                case TypeTags.STRING_TAG: {
                    fp.addClosureVar(new BClosure(new BString(sf.stringRegs[index]), BTypes.typeString),
                            TypeTags.STRING_TAG);
                    break;
                }
                default:
                    fp.addClosureVar(new BClosure(sf.refRegs[index], BTypes.typeAny), TypeTags.ANY_TAG);
            }
            i++;
        }
    }

    private static void execCmpAndBranchOpcodes(Strand ctx, StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        BDecimal lhsValue;
        BDecimal rhsValue;

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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.intRegs[k] = checkDecimalGreaterThan(lhsValue, rhsValue) ? 1 : 0;
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.intRegs[k] = checkDecimalGreaterThanOrEqual(lhsValue, rhsValue) ? 1 : 0;
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.intRegs[k] = checkDecimalGreaterThan(rhsValue, lhsValue) ? 1 : 0;
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.intRegs[k] = checkDecimalGreaterThanOrEqual(rhsValue, lhsValue) ? 1 : 0;
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
                    sf.ip = j;
                }
                break;
            case InstructionCodes.BR_FALSE:
                i = operands[0];
                j = operands[1];
                if (sf.intRegs[i] == 0) {
                    sf.ip = j;
                }
                break;
            case InstructionCodes.GOTO:
                i = operands[0];
                sf.ip = i;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execIntegerRangeOpcodes(StackFrame sf, int[] operands) {
        int i = operands[0];
        int j = operands[1];
        int k = operands[2];
        sf.refRegs[k] = new BValueArray(LongStream.rangeClosed(sf.longRegs[i], sf.longRegs[j]).toArray());
    }

    private static void execLoadOpcodes(Strand ctx, StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int pkgIndex;
        int lvIndex; // Index of the local variable

        BValueArray bValueArray;
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
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    sf.longRegs[k] = bValueArray.getInt(sf.longRegs[j]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BIALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    sf.intRegs[k] = bValueArray.getByte(sf.longRegs[j]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.FALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    sf.doubleRegs[k] = bValueArray.getFloat(sf.longRegs[j]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.SALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    sf.stringRegs[k] = bValueArray.getString(sf.longRegs[j]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    sf.intRegs[k] = bValueArray.getBoolean(sf.longRegs[j]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
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
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONALOAD:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    sf.refRegs[k] = JSONUtils.getArrayElement(sf.refRegs[i], sf.longRegs[j]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
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

                IntegerCPEntry exceptCPEntry = (IntegerCPEntry) sf.constPool[operands[3]];
                boolean except = exceptCPEntry.getValue() == 1;
                try {
                    sf.refRegs[k] = bMap.get(sf.stringRegs[j], except);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
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

    private static void execStoreOpcodes(Strand ctx, StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        int pkgIndex;

        BValueArray bValueArray;
        BMap<String, BRefType> bMap;

        switch (opcode) {
            case InstructionCodes.IASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    bValueArray.add(sf.longRegs[j], sf.longRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BIASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    bValueArray.add(sf.longRegs[j], (byte) sf.intRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.FASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    bValueArray.add(sf.longRegs[j], sf.doubleRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.SASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    bValueArray.add(sf.longRegs[j], sf.stringRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.BASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                bValueArray = Optional.of((BValueArray) sf.refRegs[i]).get();
                try {
                    bValueArray.add(sf.longRegs[j], sf.intRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
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
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                            BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_TYPE,
                                    elementType, (refReg != null) ? refReg.getType() : BTypes.typeNull)));
                    handleError(ctx);
                    break;
                }

                try {
                    ListUtils.execListAddOperation(list, index, refReg);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONASTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];

                try {
                    JSONUtils.setArrayElement(sf.refRegs[i], sf.longRegs[j], sf.refRegs[k]);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
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
                try {
                    handleMapStore(ctx, bMap, sf.stringRegs[j], sf.refRegs[k]);
                } catch (BLangMapStoreException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            case InstructionCodes.JSONSTORE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                try {
                    JSONUtils.setElement(sf.refRegs[i], sf.stringRegs[j], sf.refRegs[k]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                } catch (BLangFreezeException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execBinaryOpCodes(Strand ctx, StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int k;
        BDecimal lhsValue;
        BDecimal rhsValue;

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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.refRegs[k] = lhsValue.add(rhsValue);
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.refRegs[k] = lhsValue.subtract(rhsValue);
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.refRegs[k] = lhsValue.multiply(rhsValue);
                break;
            case InstructionCodes.IDIV:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.longRegs[j] == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.DIVISION_BY_ZERO_ERROR,
                                                           " / by zero"));
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.refRegs[k] = lhsValue.divide(rhsValue);
                break;
            case InstructionCodes.IMOD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.longRegs[j] == 0) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.DIVISION_BY_ZERO_ERROR,
                                                           " / by zero"));
                    handleError(ctx);
                    break;
                }

                sf.longRegs[k] = sf.longRegs[i] % sf.longRegs[j];
                break;
            case InstructionCodes.FMOD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                sf.doubleRegs[k] = sf.doubleRegs[i] % sf.doubleRegs[j];
                break;
            case InstructionCodes.DMOD:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.refRegs[k] = lhsValue.remainder(rhsValue);
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.intRegs[k] = isDecimalRealNumber(lhsValue) && isDecimalRealNumber(rhsValue) &&
                        lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) == 0 ? 1 : 0;
                break;
            case InstructionCodes.REQ:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.refRegs[i] == null) {
                    sf.intRegs[k] = sf.refRegs[j] == null ? 1 : 0;
                } else {
                    sf.intRegs[k] = isEqual(sf.refRegs[i], sf.refRegs[j], new ArrayList<>()) ? 1 : 0;
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
                    break; //TODO is this correct?
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
                lhsValue = (BDecimal) sf.refRegs[i];
                rhsValue = (BDecimal) sf.refRegs[j];
                sf.intRegs[k] = !isDecimalRealNumber(lhsValue) || !isDecimalRealNumber(rhsValue) ||
                        lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) != 0 ? 1 : 0;
                break;
            case InstructionCodes.RNE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                if (sf.refRegs[i] == null) {
                    sf.intRegs[k] = (sf.refRegs[j] != null) ? 1 : 0;
                } else {
                    sf.intRegs[k] = (!isEqual(sf.refRegs[i], sf.refRegs[j], new ArrayList<>())) ? 1 : 0;
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
                    break; //TODO is this correct?
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
                TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) sf.constPool[j];
                sf.intRegs[k] = checkIsType(sf.refRegs[i], typeRefCPEntry.getType()) ? 1 : 0;
                break;
            case InstructionCodes.IS_LIKE:
                i = operands[0];
                j = operands[1];
                k = operands[2];
                typeRefCPEntry = (TypeRefCPEntry) sf.constPool[j];
                sf.intRegs[k] = checkIsLikeType(sf.refRegs[i], typeRefCPEntry.getType()) ? 1 : 0;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execXMLOpcodes(Strand ctx, StackFrame sf, int opcode, int[] operands) {
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
                try {
                    xmlVal.setAttribute(xmlQName.getLocalName(), xmlQName.getUri(), xmlQName.getPrefix(),
                            sf.stringRegs[k]);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.XML_OPERATION_ERROR,
                                                           e.getMessage()));
                    handleError(ctx);
                }
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
                try {
                    sf.refRegs[k] = xmlVal.getItem(index);
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.XML_OPERATION_ERROR,
                                                           e.getMessage()));
                    handleError(ctx);
                }
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

    private static void execTypeCastOpcodes(Strand ctx, StackFrame sf, int opcode, int[] operands) {
        int i;
        int j;
        int cpIndex; // Index of the constant pool

        BRefType bRefTypeValue;
        TypeRefCPEntry typeRefCPEntry;

        switch (opcode) {
            case InstructionCodes.TYPE_ASSERTION:
                i = operands[0];
                cpIndex = operands[1];
                j = operands[2];
                typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
                BType expectedType = typeRefCPEntry.getType();

                bRefTypeValue = sf.refRegs[i];

                if (bRefTypeValue == null) {
                    if (expectedType.getTag() == TypeTags.NULL_TAG) {
                        sf.refRegs[j] = null;
                        break;
                    }
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.TYPE_ASSERTION_ERROR,
                                                           BLangExceptionHelper.getErrorMessage(
                                                                   RuntimeErrors.TYPE_ASSERTION_ERROR, expectedType,
                                                                   "()")));
                    handleError(ctx);
                } else if (isSimpleBasicType(expectedType)) {
                    execExplicitlyTypedExpressionOpCode(ctx, sf, expectedType, bRefTypeValue, j);
                } else if (expectedType.equals(bRefTypeValue.getType())) {
                    sf.refRegs[j] = bRefTypeValue;
                } else {
                    ctx.setError(
                            BLangVMErrors.createError(ctx, BallerinaErrorReasons.TYPE_ASSERTION_ERROR,
                                                      BLangExceptionHelper.getErrorMessage(
                                                              RuntimeErrors.TYPE_ASSERTION_ERROR,
                                                                   (expectedType.getTag() == TypeTags.NULL_TAG ?
                                                                            "()" : expectedType),
                                                                   bRefTypeValue.getType())));
                    handleError(ctx);
                }
                break;
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
                typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];

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
                typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
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
                BJSONType targetType = (BJSONType) ((TypeRefCPEntry) sf.constPool[cpIndex]).getType();
                bRefTypeValue = sf.refRegs[i];
                sf.refRegs[j] = JSONUtils.convertUnionTypeToJSON(bRefTypeValue, targetType);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void execExplicitlyTypedExpressionOpCode(Strand ctx, StackFrame sf, BType targetType,
                                                            BRefType bRefTypeValue, int regIndex) {
        BType sourceType = bRefTypeValue.getType();
        int targetTag = targetType.getTag();
        if (!isSimpleBasicType(sourceType) ||
                (sourceType.getTag() == TypeTags.STRING_TAG && targetTag != TypeTags.STRING_TAG)) {
            ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.TYPE_ASSERTION_ERROR,
                                                   "assertion error: expected '" + targetType + "', found '" +
                                                           bRefTypeValue.getType() + "'"));
            handleError(ctx);
            return;
        }

        try {
            switch (targetTag) {
                case TypeTags.STRING_TAG:
                    sf.stringRegs[regIndex] = bRefTypeValue.stringValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    sf.doubleRegs[regIndex] = ((BValueType) bRefTypeValue).floatValue();
                    break;
                case TypeTags.DECIMAL_TAG:
                    sf.refRegs[regIndex] = new BDecimal(((BValueType) bRefTypeValue).decimalValue());
                    break;
                case TypeTags.INT_TAG:
                    sf.longRegs[regIndex] = ((BValueType) bRefTypeValue).intValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    sf.intRegs[regIndex] = ((BValueType) bRefTypeValue).booleanValue() ? 1 : 0;
                    break;
            }
        } catch (BallerinaException e) {
            ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
            handleError(ctx);
        }
    }

    private static void execTypeConversionOpcodes(Strand ctx, StackFrame sf, int opcode,
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
                sf.refRegs[j] = new BDecimal(
                        (new BigDecimal(sf.longRegs[i], MathContext.DECIMAL128)).setScale(1,
                                                                                          BigDecimal.ROUND_HALF_EVEN));
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
                double valueToConvert = sf.doubleRegs[i];
                if (Double.isNaN(valueToConvert) || Double.isInfinite(valueToConvert)) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                                                           "'float' value '" + valueToConvert + "' cannot be " +
                                                                   "converted to 'int'"));
                    handleError(ctx);
                    break;
                }

                if (!isFloatWithinIntRange(valueToConvert)) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.NUMBER_CONVERSION_ERROR, "out " +
                            "of range 'float' value '" + valueToConvert + "' cannot be converted to 'int'"));
                    handleError(ctx);
                    break;
                }

                sf.longRegs[j] = Math.round(valueToConvert);
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
                sf.refRegs[j] = sf.intRegs[i] == 1 ?
                        new BDecimal(BigDecimal.ONE.setScale(1, BigDecimal.ROUND_HALF_EVEN)) :
                        new BDecimal(BigDecimal.ZERO.setScale(1, BigDecimal.ROUND_HALF_EVEN));
                break;
            case InstructionCodes.D2I:
                i = operands[0];
                j = operands[1];
                BDecimal decimal = (BDecimal) sf.refRegs[i];
                if (decimal.valueKind == DecimalValueKind.NOT_A_NUMBER ||
                        !isDecimalWithinIntRange((decimal.decimalValue()))) {
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.NUMBER_CONVERSION_ERROR,
                            "out of range 'decimal' value '" + decimal + "' cannot be converted to 'int'"));
                    handleError(ctx);
                    break;
                }
                sf.longRegs[j] = Math.round(((BDecimal) sf.refRegs[i]).decimalValue().doubleValue());
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
                    sf.refRegs[j] = JSONUtils.toJSON((BTable) bRefType);
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

    public static boolean isFloatWithinIntRange(double doubleValue) {
        return doubleValue < BINT_MAX_VALUE_DOUBLE_RANGE_MAX && doubleValue > BINT_MIN_VALUE_DOUBLE_RANGE_MIN;
    }

    public static boolean isDecimalWithinIntRange(BigDecimal decimalValue) {
        return decimalValue.compareTo(BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX) < 0 &&
                decimalValue.compareTo(BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN) > 0;
    }

    private static boolean isDecimalRealNumber(BDecimal decimalValue) {
        return decimalValue.valueKind == DecimalValueKind.ZERO || decimalValue.valueKind == DecimalValueKind.OTHER;
    }

    private static boolean checkDecimalGreaterThan(BDecimal lhsValue, BDecimal rhsValue) {
        switch (lhsValue.valueKind) {
            case POSITIVE_INFINITY:
                return isDecimalRealNumber(rhsValue) || rhsValue.valueKind == DecimalValueKind.NEGATIVE_INFINITY;
            case ZERO:
            case OTHER:
                return rhsValue.valueKind == DecimalValueKind.NEGATIVE_INFINITY || (isDecimalRealNumber(rhsValue) &&
                        lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) > 0);
            default:
                return false;
        }
    }

    private static boolean checkDecimalGreaterThanOrEqual(BDecimal lhsValue, BDecimal rhsValue) {
        return checkDecimalGreaterThan(lhsValue, rhsValue) ||
                (isDecimalRealNumber(lhsValue) && isDecimalRealNumber(rhsValue) &&
                        lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) == 0);
    }

    private static void execIteratorOperation(Strand ctx, StackFrame sf, Instruction instruction) {
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
                        public BValue getNext() {
                            return null;
                        }
                    };
                    break;
                }

                sf.refRegs[j] = ((BCollection) collection).newIterator();
                break;
            case InstructionCodes.ITR_NEXT:
                nextInstruction = (InstructionIteratorNext) instruction;
                iterator = (BIterator) sf.refRegs[nextInstruction.iteratorIndex];

                try {
                    // Check whether we have a next value.
                    if (!Optional.of(iterator).get().hasNext()) {
                        // If we don't have a next value, that means we have reached the end of the iterable list. So
                        // we set null to the corresponding registry location.
                        sf.refRegs[nextInstruction.retRegs[0]] = null;
                        return;
                    }
                    // Get the next value.
                    BValue value = Optional.of(iterator).get().getNext();
                    // We create a new map and add the value to the map with the key `value`. Then we set this
                    // map to the corresponding registry location.
                    BMap<String, BValue> newMap = new BMap<>(nextInstruction.constraintType);
                    newMap.put("value", value);
                    sf.refRegs[nextInstruction.retRegs[0]] = (BRefType) newMap;
                } catch (BallerinaException e) {
                    ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), e.getDetail()));
                    handleError(ctx);
                }
                break;
        }
    }

    private static void execXMLCreationOpcodes(Strand ctx, StackFrame sf, int opcode,
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
                    ctx.setError(BLangVMErrors.createError(ctx, BallerinaErrorReasons.XML_CREATION_ERROR,
                                                           e.getMessage()));
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

    private static boolean handleVariableLock(Strand strand, BType[] types, int[] pkgRegs, int[] varRegs,
                                              int[] fieldRegs, int varCount, String uuid) {
        boolean lockAcquired = true;
        for (int i = 0; i < varCount && lockAcquired; i++) {
            BType paramType = types[i];
            int pkgIndex = pkgRegs[i];
            int regIndex = varRegs[i];

            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    lockAcquired = strand.programFile.globalMemArea.lockIntField(strand, pkgIndex, regIndex);
                    break;
                case TypeTags.BYTE_TAG:
                    lockAcquired = strand.programFile.globalMemArea.lockBooleanField(strand, pkgIndex, regIndex);
                    break;
                case TypeTags.FLOAT_TAG:
                    lockAcquired = strand.programFile.globalMemArea.lockFloatField(strand, pkgIndex, regIndex);
                    break;
                case TypeTags.STRING_TAG:
                    lockAcquired = strand.programFile.globalMemArea.lockStringField(strand, pkgIndex, regIndex);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    lockAcquired = strand.programFile.globalMemArea.lockBooleanField(strand, pkgIndex, regIndex);
                    break;
                default:
                    lockAcquired = strand.programFile.globalMemArea.lockRefField(strand, pkgIndex, regIndex);
            }
        }

        if (varRegs.length <= varCount) {
            return lockAcquired;
        }

        //lock on field access
        strand.currentFrame.localProps.putIfAbsent(uuid, new Stack<VarLock>());
        Stack lockStack = (Stack) strand.currentFrame.localProps.get(uuid);

        for (int i = 0; (i < varRegs.length - varCount) && lockAcquired; i++) {
            int regIndex = varRegs[varCount + i];
            String field = strand.currentFrame.stringRegs[fieldRegs[i]];
            VarLock lock = ((BMap) strand.currentFrame.refRegs[regIndex]).getFieldLock(field);
            lockAcquired = lock.lock(strand);
            if (lockAcquired) {
                lockStack.push(lock);
            }
        }
        return lockAcquired;
    }

    private static void handleVariableUnlock(Strand strand, BType[] types, int[] pkgRegs, int[] varRegs,
                                             int varCount, String uuid, boolean hasFieldVar) {
        if (hasFieldVar) {
            Stack<VarLock> lockStack = (Stack<VarLock>) strand.currentFrame.localProps.get(uuid);
            while (!lockStack.isEmpty()) {
                lockStack.pop().unlock();
            }
        }

        for (int i = varCount - 1; i > -1; i--) {
            BType paramType = types[i];
            int pkgIndex = pkgRegs[i];
            int regIndex = varRegs[i];
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    strand.programFile.globalMemArea.unlockIntField(pkgIndex, regIndex);
                    break;
                case TypeTags.BYTE_TAG:
                    strand.programFile.globalMemArea.unlockBooleanField(pkgIndex, regIndex);
                    break;
                case TypeTags.FLOAT_TAG:
                    strand.programFile.globalMemArea.unlockFloatField(pkgIndex, regIndex);
                    break;
                case TypeTags.STRING_TAG:
                    strand.programFile.globalMemArea.unlockStringField(pkgIndex, regIndex);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    strand.programFile.globalMemArea.unlockBooleanField(pkgIndex, regIndex);
                    break;
                default:
                    strand.programFile.globalMemArea.unlockRefField(pkgIndex, regIndex);
            }
        }
    }

    /**
     * Method to calculate and detect debug points when the instruction point is given.
     */
    private static boolean debug(Strand ctx) {
        Debugger debugger = ctx.programFile.getDebugger();
        if (!debugger.isClientSessionActive()) {
            return false;
        }
        DebugContext debugContext = ctx.getDebugContext();

        if (debugContext.isStrandPaused()) {
            debugContext.setStrandPaused(false);
            return false;
        }

        if (isIgnorableInstruction(ctx)) {
            return false;
        }
        
        LineNumberInfo currentExecLine = debugger
                .getLineNumber(ctx.currentFrame.callableUnitInfo.getPackageInfo().getPkgPath(), ctx.currentFrame.ip);
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
                debugContext.clearContext();
                break;
            case STEP_IN:
                debugHit(ctx, currentExecLine, debugger);
                return true;
            case STEP_OVER:
                if (debugContext.getFramePointer() < ctx.fp) {
                    return false;
                }
                debugHit(ctx, currentExecLine, debugger);
                return true;
            case STEP_OUT:
                if (debugContext.getFramePointer() > ctx.fp) {
                    debugHit(ctx, currentExecLine, debugger);
                    return true;
                }
                return false;
            default:
                debugger.notifyExit();
                debugger.stopDebugging();
        }
        return false;
    }

    private static boolean isIgnorableInstruction(Strand ctx) {
        int opcode = ctx.currentFrame.code[ctx.currentFrame.ip].getOpcode();
        return opcode == InstructionCodes.GOTO;
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
    private static boolean debugPointCheck(Strand ctx, LineNumberInfo currentExecLine, Debugger debugger) {
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
    private static void debugHit(Strand ctx, LineNumberInfo currentExecLine, Debugger debugger) {
        ctx.getDebugContext().updateContext(currentExecLine, ctx.fp);
        debugger.pauseWorker(ctx);
        debugger.notifyDebugHit(ctx, currentExecLine, ctx.getId());
    }

    private static void handleAnyToRefTypeCast(Strand ctx, StackFrame sf, int[] operands,
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

    private static void handleTypeCastError(Strand ctx, StackFrame sf, int errorRegIndex,
                                            BType sourceType, BType targetType) {
        handleTypeCastError(ctx, sf, errorRegIndex, sourceType.toString(), targetType.toString());
    }

    private static void handleTypeCastError(Strand ctx, StackFrame sf, int errorRegIndex,
                                            String sourceType, String targetType) {
        BError errorVal = BLangVMErrors.createTypeCastError(ctx, sourceType, targetType);
        sf.refRegs[errorRegIndex] = errorVal;
    }

    private static void handleTypeConversionError(Strand ctx, StackFrame sf, int errorRegIndex,
                                                  BType sourceType, BType targetType) {
        handleTypeConversionError(ctx, sf, errorRegIndex, sourceType.toString(), targetType.toString());
    }

    private static void handleTypeConversionError(Strand ctx, StackFrame sf, int errorRegIndex,
                                                  String sourceTypeName, String targetTypeName) {
        String errorMsg = "'" + sourceTypeName + "' cannot be converted to '" + targetTypeName + "'";
        handleTypeConversionError(ctx, sf, errorRegIndex, errorMsg);
    }

    private static void handleTypeConversionError(Strand ctx, StackFrame sf,
                                                  int errorRegIndex, String errorMessage) {
        BError errorVal = BLangVMErrors.createTypeConversionError(ctx, errorMessage);
        sf.refRegs[errorRegIndex] = errorVal;
    }

    private static void createNewIntRange(int[] operands, StackFrame sf) {
        long startValue = sf.longRegs[operands[0]];
        long endValue = sf.longRegs[operands[1]];
        sf.refRegs[operands[2]] = new BIntRange(startValue, endValue);
    }

    private static void createNewStruct(int[] operands, StackFrame sf) {
        int cpIndex = operands[0];
        int i = operands[1];
        StructureRefCPEntry structureRefCPEntry = (StructureRefCPEntry) sf.constPool[cpIndex];
        StructureTypeInfo structInfo = (StructureTypeInfo) ((TypeDefInfo) structureRefCPEntry
                .getStructureTypeInfo()).typeInfo;
        sf.refRegs[i] = new BMap<>(structInfo.getType());
    }

    private static void beginTransaction(Strand strand, int transactionType, int transactionBlockIdIndex,
                                         int retryCountRegIndex, int committedFuncIndex, int abortedFuncIndex) {
        if (transactionType == Transactions.TransactionType.PARTICIPANT.value) {
            beginTransactionLocalParticipant(strand, transactionBlockIdIndex, committedFuncIndex, abortedFuncIndex);
            return;
        } else if (transactionType == Transactions.TransactionType.REMOTE_PARTICIPANT.value) {
            beginRemoteParticipant(strand, transactionBlockIdIndex, committedFuncIndex, abortedFuncIndex);
            return;
        }

        //Transaction is attempted three times by default to improve resiliency
        int retryCount = TransactionConstants.DEFAULT_RETRY_COUNT;
        if (retryCountRegIndex != -1) {
            retryCount = (int) strand.currentFrame.longRegs[retryCountRegIndex];
            if (retryCount < 0) {
                strand.setError(BLangVMErrors
                        .createError(strand, BallerinaErrorReasons.TRANSACTION_ERROR,
                                     BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_RETRY_COUNT)));
                handleError(strand);
                return;
            }
        }

        // If global tx enabled, it is managed via transaction coordinator.
        // Otherwise it is managed locally without any interaction with the transaction coordinator.
        if (strand.getLocalTransactionContext() != null) {
            // starting a transaction within already infected transaction.
            createAndSetDynamicNestedTrxError(strand);
            handleError(strand);
            return;
        }
        String transactionBlockId = getTrxBlockIdFromCP(strand, transactionBlockIdIndex);

        TransactionLocalContext transactionLocalContext = createAndNotifyGlobalTx(strand, transactionBlockId);
        strand.setLocalTransactionContext(transactionLocalContext);
        transactionLocalContext.beginTransactionBlock(transactionBlockId, retryCount);
    }

    private static void beginRemoteParticipant(Strand strand, int transactionBlockIdIndex, int committedFuncIndex,
                                               int abortedFuncIndex) {
        TransactionLocalContext localTransactionContext = strand.getLocalTransactionContext();
        if (localTransactionContext == null) {
            // No transaction available to participate,
            // We have no business here. This is a no-op.
            return;
        }

        // Register committed function handler if exists.
        BFunctionPointer fpCommitted = null;
        if (committedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) strand.currentFrame.constPool[committedFuncIndex];
            fpCommitted = new BFunctionPointer(funcRefCPEntry.getFunctionInfo());
        }

        // Register aborted function handler if exists.
        BFunctionPointer fpAborted = null;
        if (abortedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) strand.currentFrame.constPool[abortedFuncIndex];
            fpAborted = new BFunctionPointer(funcRefCPEntry.getFunctionInfo());
        }
        String transactionBlockId = getTrxBlockIdFromCP(strand, transactionBlockIdIndex);
        localTransactionContext.setResourceParticipant(true);
        String globalTransactionId = localTransactionContext.getGlobalTransactionId();
        localTransactionContext.beginTransactionBlock(transactionBlockId, -1);
        TransactionResourceManager.getInstance()
                .registerParticipation(globalTransactionId, transactionBlockId, fpCommitted, fpAborted, strand);
        strand.currentFrame.trxParticipant = StackFrame.TransactionParticipantType.REMOTE_PARTICIPANT;

    }

    private static void createAndSetDynamicNestedTrxError(Strand strand) {
        BError error = BLangVMErrors.createError(strand, BallerinaErrorReasons.TRANSACTION_ERROR,
                                                 BLangExceptionHelper.getErrorMessage(
                                                         RuntimeErrors.INVALID_DYNAMICALLY_NESTED_TRANSACTION));
        strand.setError(error);
    }

    private static void beginTransactionLocalParticipant(Strand strand, int transactionBlockIdCpIndex,
                                                         int committedFuncIndex, int abortedFuncIndex) {
        TransactionLocalContext transactionLocalContext = strand.getLocalTransactionContext();
        if (transactionLocalContext == null) {
            // No transaction available to participate,
            // We have no business here. This is a no-op.
            return;
        }

        String transactionBlockId = getTrxBlockIdFromCP(strand, transactionBlockIdCpIndex);

        // Register committed function handler if exists.
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();
        BFunctionPointer fpCommitted = null;
        if (committedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) strand.currentFrame.constPool[committedFuncIndex];
            fpCommitted = new BFunctionPointer(funcRefCPEntry.getFunctionInfo());
            transactionResourceManager.registerCommittedFunction(transactionBlockId, fpCommitted);
        }

        // Register aborted function handler if exists.
        BFunctionPointer fpAborted = null;
        if (abortedFuncIndex != -1) {
            FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) strand.currentFrame.constPool[abortedFuncIndex];
            fpAborted = new BFunctionPointer(funcRefCPEntry.getFunctionInfo());
            transactionResourceManager.registerAbortedFunction(transactionBlockId, fpAborted);
        }

        transactionLocalContext.beginTransactionBlock(transactionBlockId, 1);
        transactionResourceManager.registerParticipation(transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId, fpCommitted, fpAborted, strand);
        // this call frame is a transaction participant.
        strand.currentFrame.trxParticipant = StackFrame.TransactionParticipantType.LOCAL_PARTICIPANT;
    }

    private static String getTrxBlockIdFromCP(Strand strand, int index) {
        StringCPEntry stringCPEntry = (StringCPEntry) strand.currentFrame.constPool[index];
        return stringCPEntry.getValue();
    }

    private static TransactionLocalContext createAndNotifyGlobalTx(Strand ctx, String transactionBlockId) {
        BValue[] txResult = TransactionUtils.notifyTransactionBegin(ctx, null, null,
                transactionBlockId, TransactionConstants.DEFAULT_COORDINATION_TYPE);

        BMap<String, BValue> txDataStruct = (BMap<String, BValue>) txResult[0];
        String globalTransactionId = txDataStruct.get(TransactionConstants.TRANSACTION_ID).stringValue();
        String url = txDataStruct.get(TransactionConstants.REGISTER_AT_URL).stringValue();
        String protocol = txDataStruct.get(TransactionConstants.CORDINATION_TYPE).stringValue();

        return TransactionLocalContext.create(globalTransactionId, url, protocol);
    }

    private static TransactionLocalContext createLocalOnlyTransaction() {
        String globalTransactionId = UUID.randomUUID().toString().replaceAll("-", "");
        return TransactionLocalContext.create(globalTransactionId, null, null);
    }

    private static void retryTransaction(Strand strand, int transactionBlockIdCpIndex,
                                         int trAbortEndIp, int trEndStatusReg) {
        strand.currentFrame.intRegs[trEndStatusReg] = 0; // set trend status to normal.
        TransactionLocalContext transactionLocalContext = strand.getLocalTransactionContext();
        transactionLocalContext.getAndClearFailure();

        String transactionBlockId = getTrxBlockIdFromCP(strand, transactionBlockIdCpIndex);
        if (transactionLocalContext.isRetryPossible(strand, transactionBlockId)) {
            if (transactionLocalContext.isRetryAttempt(transactionBlockId)) {
                TransactionLocalContext newLocalTransaction = createAndNotifyGlobalTx(strand, transactionBlockId);
                int allowedRetryCount = transactionLocalContext.getAllowedRetryCount(transactionBlockId);
                newLocalTransaction.beginTransactionBlock(transactionBlockId,
                        allowedRetryCount - 1);
                strand.setLocalTransactionContext(newLocalTransaction);
            }
            strand.getLocalTransactionContext().incrementCurrentRetryCount(transactionBlockId);
            strand.setError(null);
            // todo: communicate re-try intent to coordinator
            // tr_end will communicate the tx ending to coordinator
            return;
        }

        strand.currentFrame.intRegs[trEndStatusReg] = 1;
        strand.currentFrame.ip = trAbortEndIp;
    }

    private static void endTransaction(Strand strand, int transactionBlockIdCpIndex, int endType, int statusRegIndex,
                                       int errorRegIndex) {
        TransactionLocalContext localTxInfo = strand.getLocalTransactionContext();
        String txBlockId = getTrxBlockIdFromCP(strand, transactionBlockIdCpIndex);

        try {
            //In success case no need to do anything as with the transaction end phase it will be committed.
            switch (Transactions.TransactionStatus.getConst(endType)) {
                case BLOCK_END: // 0
                    // set statusReg
                    transactionBlockEnd(strand, txBlockId, statusRegIndex, localTxInfo, errorRegIndex);
                    break;
                case FAILED: // -1
                    transactionFailedEnd(strand, txBlockId, localTxInfo, statusRegIndex);
                    break;
                case ABORTED: // -2
                    transactionAbortedEnd(strand, txBlockId, localTxInfo, statusRegIndex, errorRegIndex);
                    break;
                case END: // 1
                    transactionEndEnd(strand, txBlockId, localTxInfo);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid transaction end endType: " + endType);
            }
        } catch (Throwable e) {
            strand.setError(BLangVMErrors.createError(strand, BallerinaErrorReasons.TRANSACTION_ERROR, e.getMessage()));
            handleError(strand);
        }
    }

    private static void transactionAbortedEnd(Strand strand, String txBlockId, TransactionLocalContext localTxInfo,
                                              int statusRegIndex, int errorRegIndex) {
        // Notify only if, aborted by 'abort' statement.
        if (strand.currentFrame.intRegs[statusRegIndex] == 0) {
            notifyTransactionAbort(strand, txBlockId, localTxInfo);
        }
        setErrorRethrowReg(strand, statusRegIndex, errorRegIndex);
    }

    private static void setErrorRethrowReg(Strand strand, int statusRegIndex, int errorRegIndex) {
        if (strand.getError() != null) {
            BError cause = strand.getError();
            if (cause != null) {
                strand.setError(cause);
                strand.currentFrame.refRegs[errorRegIndex] = cause; // panic on this error.
            }
            // Next 2 instructions re-throw this error.
            strand.currentFrame.intRegs[statusRegIndex] = 1;
            return;
        }
        // Skip rethrow instruction, since there is no error.
        strand.currentFrame.intRegs[statusRegIndex] = 0;
    }

    private static void transactionEndEnd(Strand strand, String transactionBlockId,
                                          TransactionLocalContext transactionLocalContext) {
        boolean isOuterTx = transactionLocalContext.onTransactionEnd(transactionBlockId);
        strand.removeLocalTransactionContext();
    }

    private static void transactionBlockEnd(Strand strand, String transactionBlockId, int statusRegIndex,
                                            TransactionLocalContext transactionLocalContext, int errorRegIndex) {
        // Tx reached end of block, it may or may not successfully finished.
        TransactionLocalContext.TransactionFailure failure = transactionLocalContext.getFailure();

        BError error = null;
        BRefType<?> errorVal = strand.currentFrame.refRegs[errorRegIndex];
        if (errorVal != null && errorVal.getType().getTag() == TypeTags.ERROR_TAG) {
            error = (BError) errorVal;
        }

        if (error != null && strand.getError() == null) {
            strand.setError(error);
            strand.currentFrame.refRegs[errorRegIndex] = null;
        }

        if (failure == null && error == null && strand.getError() == null) {
            // Skip branching to retry block as there is no local failure.
            // Will set this reg if there is failure in global coordinated trx.
            strand.currentFrame.intRegs[statusRegIndex] = 0;

            TransactionUtils.CoordinatorCommit coordinatorStatus =
                    notifyGlobalPrepareAndCommit(strand, transactionBlockId, transactionLocalContext);

            if (!TransactionUtils.CoordinatorCommit.COMMITTED.equals(coordinatorStatus)) {
                // Coordinator returned un-committed status, hence skip committed block, and goto failed block.
                strand.currentFrame.intRegs[statusRegIndex] = 1;
            }
        } else {
            // Tx failed, branch to retry block.
            strand.currentFrame.intRegs[statusRegIndex] = 1;

            // This could be a transaction failure from a native/std library. Or due to an an error/exception.
            // If transaction is not already marked as failed do so now for future references.
            if (failure == null) {
                transactionLocalContext.markFailure();
            }
            boolean notifyCoordinator = transactionLocalContext.onTransactionFailed(strand, transactionBlockId);
            if (notifyCoordinator) {
                notifyTransactionAbort(strand, transactionBlockId, transactionLocalContext);
            }
        }
    }

    private static TransactionUtils.CoordinatorCommit notifyGlobalPrepareAndCommit(
            Strand ctx, String transactionBlockId, TransactionLocalContext transactionLocalContext) {
        return TransactionUtils.notifyTransactionEnd(ctx,
                transactionLocalContext.getGlobalTransactionId(), transactionBlockId);
    }

    private static void notifyTransactionAbort(Strand strand, String transactionBlockId,
                                               TransactionLocalContext transactionLocalContext) {
        TransactionUtils.notifyTransactionAbort(strand, transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId);
        TransactionResourceManager.getInstance()
                .notifyAbort(transactionLocalContext.getGlobalTransactionId(), transactionBlockId, false);
    }

    private static void transactionFailedEnd(Strand strand, String transactionBlockId,
                                             TransactionLocalContext transactionLocalContext,
                                             int runOnRetryBlockRegIndex) {
        // Invoking tr_end with transaction status of FAILED means tx has failed for some reason.
        if (transactionLocalContext.isRetryPossible(strand, transactionBlockId)) {
            strand.currentFrame.intRegs[runOnRetryBlockRegIndex] = 1;
        } else {
            strand.currentFrame.intRegs[runOnRetryBlockRegIndex] = 0;
        }
    }

    private static Strand invokeVirtualFunction(Strand ctx, StackFrame sf, int receiver,
                                                FunctionInfo virtualFuncInfo, int[] argRegs,
                                                int retReg, int flags) {
        BMap<String, BValue> structVal = (BMap<String, BValue>) sf.refRegs[receiver];

        // TODO use ObjectTypeInfo once record init function is removed
        StructureTypeInfo structInfo = (StructureTypeInfo) ((BStructureType) structVal.getType()).getTypeInfo();
        FunctionInfo attachedFuncInfo = structInfo.funcInfoEntries.get(virtualFuncInfo.getName());
        return invokeCallable(ctx, attachedFuncInfo, argRegs, retReg, flags);
    }

    private static void handleWorkerSend(Strand ctx, WorkerDataChannelInfo workerDataChannelInfo,
                                         BType type, int reg, boolean channelInSameStrand) {
        BRefType val = extractValue(ctx.currentFrame, type, reg);
        WorkerDataChannel dataChannel = getWorkerChannel(ctx, workerDataChannelInfo.getChannelName(),
                channelInSameStrand);
        dataChannel.sendData(val);
    }

    private static WorkerDataChannel getWorkerChannel(Strand ctx, String name, boolean channelInSameStrand) {
        if (channelInSameStrand) {
            return ctx.currentFrame.wdChannels.getWorkerDataChannel(name);
        }
        if (ctx.fp > 0) {
            return ctx.peekFrame(1).wdChannels.getWorkerDataChannel(name);
        }
        return ctx.respCallback.parentChannels.getWorkerDataChannel(name);
    }

    private static BRefType extractValue(StackFrame data, BType type, int reg) {
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

    private static boolean handleWorkerReceive(Strand ctx, WorkerDataChannelInfo workerDataChannelInfo,
                                               BType type, int reg, boolean channelInSameStrand) {
        return getWorkerChannel(ctx, workerDataChannelInfo.getChannelName(),
                channelInSameStrand).tryTakeData(ctx, type, reg);
    }

    public static void copyArgValueForWorkerReceive(StackFrame currentSF, int regIndex, BType paramType,
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
            return checkJSONCast(rhsValue, rhsType, lhsType);
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
        return (lhsType.getTag() == TypeTags.ANY_TAG && rhsType.getTag() != TypeTags.ERROR_TAG) || rhsType
                .equals(lhsType);
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
                case TypeTags.MAP_TAG:
                    return checkCastByType(((BMapType) rhsType).getConstrainedType(), lhsType, unresolvedTypes);
                case TypeTags.ARRAY_TAG:
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
        BValueArray source = (BValueArray) sourceValue;
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
        return isAnydata(type, new HashSet<>());
    }

    private static boolean isAnydata(BType type, Set<BType> unresolvedTypes) {
        if (type.getTag() <= TypeTags.ANYDATA_TAG) {
            return true;
        }

        switch (type.getTag()) {
            case TypeTags.MAP_TAG:
                return isAnydata(((BMapType) type).getConstrainedType(), unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                if (unresolvedTypes.contains(type)) {
                    return true;
                }
                unresolvedTypes.add(type);
                BRecordType recordType = (BRecordType) type;
                List<BType> fieldTypes = recordType.getFields().values().stream()
                                                   .map(BField::getFieldType)
                                                   .collect(Collectors.toList());
                return isAnydata(fieldTypes, unresolvedTypes) && (recordType.sealed ||
                        isAnydata(recordType.restFieldType, unresolvedTypes));
            case TypeTags.UNION_TAG:
                return isAnydata(((BUnionType) type).getMemberTypes(), unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                return isAnydata(((BTupleType) type).getTupleTypes(), unresolvedTypes);
            case TypeTags.ARRAY_TAG:
                return isAnydata(((BArrayType) type).getElementType(), unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                Set<BType> valSpaceTypes = ((BFiniteType) type).valueSpace.stream()
                                                                          .map(BValue::getType)
                                                                          .collect(Collectors.toSet());
                return isAnydata(valSpaceTypes, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean isAnydata(Collection<BType> types, Set<BType> unresolvedTypes) {
        return types.stream().allMatch(bType -> isAnydata(bType, unresolvedTypes));
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

        // Cannot assign open records to closed record types
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        // The rest field types should match if they are open records
        if (!rhsType.sealed && !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
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
        Set<String> lhsFieldNames = lhsType.getFields().keySet();

        for (BField lhsField : lhsType.getFields().values()) {
            BField rhsField = rhsFields.get(lhsField.fieldName);

            // If the LHS field is a required one, there has to be a corresponding required field in the RHS record.
            if (!Flags.isFlagOn(lhsField.flags, Flags.OPTIONAL)
                    && (rhsField == null || Flags.isFlagOn(rhsField.flags, Flags.OPTIONAL))) {
                return false;
            }

            if (rhsField == null || !isAssignable(rhsField.fieldType, lhsField.fieldType, unresolvedTypes)) {
                return false;
            }
        }

        if (lhsType.sealed) {
            return lhsFieldNames.containsAll(rhsFields.keySet());
        }

        return rhsFields.values().stream()
                            .filter(field -> !lhsFieldNames.contains(field.fieldName))
                            .allMatch(field -> isAssignable(field.fieldType, lhsType.restFieldType, unresolvedTypes));
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

    /**
     * Check the compatibility of casting a JSON to a target type.
     *
     * @param json       JSON to cast
     * @param sourceType Type of the source JSON
     * @param targetType Target type
     * @return Runtime compatibility for casting
     */
    private static boolean checkJSONCast(BValue json, BType sourceType, BType targetType) {
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
                BValueArray array = (BValueArray) json;
                for (int i = 0; i < array.size(); i++) {
                    // get the element type of source and json, and recursively check for json casting.
                    BType sourceElementType = sourceType.getTag() == TypeTags.ARRAY_TAG
                            ? ((BArrayType) sourceType).getElementType() : sourceType;
                    if (!checkJSONCast(array.getRefValue(i), sourceElementType, arrayType.getElementType())) {
                        return false;
                    }
                }
                return true;
            case TypeTags.JSON_TAG:
                return getElementType(sourceType).getTag() == TypeTags.JSON_TAG;
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
                return true;
            default:
                return false;
        }
    }

    private static void  convertStructToMap(Strand strand, int[] operands, StackFrame sf) {
        int i = operands[0];
        int j = operands[1];

        // TODO: do validation for type?
        BMap newMap = new BMap(BTypes.typeMap);
        ((BMap) sf.refRegs[i]).getMap().forEach((key, value)
                -> newMap.put(key, value == null ? null : ((BValue) value).copy(new HashMap<>())));
        sf.refRegs[j] = newMap;
    }

    private static void convertStructToJSON(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BJSONType targetType = (BJSONType) ((TypeRefCPEntry) sf.constPool[cpIndex]).getType();

        BMap<String, BValue> bStruct = (BMap<String, BValue>) sf.refRegs[i];
        try {
            sf.refRegs[j] = JSONUtils.convertMapToJSON(bStruct, targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + bStruct.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static void convertArrayToJSON(Strand ctx, int[] operands, StackFrame sf) {
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

    private static void convertJSONToArray(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BArrayType targetType = (BArrayType) ((TypeRefCPEntry) sf.constPool[cpIndex]).getType();

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

    private static void convertMapToJSON(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BJSONType targetType = (BJSONType) ((TypeRefCPEntry) sf.constPool[cpIndex]).getType();

        BMap<String, ?> bMap = (BMap<String, ?>) sf.refRegs[i];
        try {
            sf.refRegs[j] = JSONUtils.convertMapToJSON((BMap<String, BValue>) bMap, targetType);
        } catch (Exception e) {
            String errorMsg = "cannot convert '" + bMap.getType() + "' to type '" + targetType + "': " +
                    e.getMessage();
            handleTypeConversionError(ctx, sf, j, errorMsg);
        }
    }

    private static void convertJSONToMap(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];
        BMapType targetType = (BMapType) ((TypeRefCPEntry) sf.constPool[cpIndex]).getType();

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

    private static void  convertMapToStruct(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
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

    private static BMap<String, BValue> convertMapToStruct(Strand ctx,
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

    private static BValue convertMap(Strand ctx, BMap<String, BValue> mapValue, BType targetType,
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

    private static void convertJSONToStruct(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
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

    private static void handleNullRefError(Strand strand) {
        strand.setError(BLangVMErrors.createNullRefException(strand));
        handleError(strand);
    }

    public static void handleError(Strand strand) {
        StackFrame sf = strand.currentFrame;
        // TODO: Fix me
        int ip = sf.ip;
        ip--;
        ErrorTableEntry match = ErrorTableEntry.getMatch(sf.callableUnitInfo.getPackageInfo(), ip);
        if (match != null) {
            sf.ip = match.ipTarget;
            sf.refRegs[match.regIndex] = strand.getError();
            strand.setError(null);
        } else if (strand.fp > 0) {
            // Stop the observation context before popping the stack frame
            ObserveUtils.stopCallableObservation(strand);
            StackFrame popedFrame = strand.popFrame();
            popedFrame.handleChannelPanic(strand.getError(), strand.currentFrame.wdChannels);
            signalTransactionError(strand, popedFrame.trxParticipant);
            handleError(strand);
        } else {
            strand.respCallback.setError(strand.getError());
            strand.currentFrame.handleChannelPanic(strand.getError(), strand.respCallback.parentChannels);
            signalTransactionError(strand, StackFrame.TransactionParticipantType.REMOTE_PARTICIPANT);
            //Below is to return current thread from VM
            sf.ip = -1;
            strand.respCallback.signal();
        }
    }

    private static void signalTransactionError(Strand strand,
                                               StackFrame.TransactionParticipantType transactionParticipant) {
        TransactionLocalContext transactionLocalContext = strand.getLocalTransactionContext();
        if (transactionLocalContext == null) {
            return;
        }
        boolean resourceParticipant = transactionLocalContext.isResourceParticipant();
        if (resourceParticipant && transactionParticipant == StackFrame.TransactionParticipantType.REMOTE_PARTICIPANT) {
            transactionLocalContext.notifyLocalRemoteParticipantFailure();
        } else if (transactionParticipant == StackFrame.TransactionParticipantType.LOCAL_PARTICIPANT) {
            transactionLocalContext.notifyLocalParticipantFailure();
        } else if (strand.aborted) {
            String blockID = transactionLocalContext.getCurrentTransactionBlockId();
            notifyTransactionAbort(strand, blockID, transactionLocalContext);
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

    private static void calculateLength(Strand ctx, int[] operands, StackFrame sf) {
        int i = operands[0];
        int cpIndex = operands[1];
        int j = operands[2];

        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) sf.constPool[cpIndex];
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

    private static boolean execWait(Strand strand, int[] operands) {
        int c = operands[0];
        TypeRefCPEntry typeEntry = (TypeRefCPEntry) strand.currentFrame.constPool[operands[1]];
        BType expType = typeEntry.getType();
        int retValReg = operands[2];

        SafeStrandCallback[] callbacks = new SafeStrandCallback[c];
        for (int i = 0; i < c; i++) {
            int futureReg = operands[i + 3];
            BFuture future = (BFuture) strand.currentFrame.refRegs[futureReg];
            callbacks[i] = (SafeStrandCallback) future.value().respCallback;
        }
        strand.createWaitHandler(c, null);
        return WaitCallbackHandler.handleReturnInWait(strand, expType, retValReg, callbacks);
    }

    private static boolean execWaitForAll(Strand strand, int[] operands) {
        int c = operands[0];
        // TODO: 11/22/18  Remove this from the CodeGen
        TypeRefCPEntry typeEntry = (TypeRefCPEntry) strand.currentFrame.constPool[operands[1]];
        BType expType = typeEntry.getType();
        int retValReg = operands[2];

        List<SafeStrandCallback.WaitMultipleCallback> callbackList = new ArrayList<>();
        for (int i = 0; i < c; i = i + 2) {
            int index = i + 3;
            // Get the key
            int keyRegIndex = operands[index];
            // Get the expression followed
            int futureReg = operands[index + 1];
            BFuture future = (BFuture) strand.currentFrame.refRegs[futureReg];
            callbackList.add(new SafeStrandCallback.WaitMultipleCallback(keyRegIndex,
                                                                         (SafeStrandCallback) future.value().
                                                                                 respCallback));
        }
        strand.createWaitHandler(c, new ArrayList(callbackList.stream()
                                                              .map(SafeStrandCallback.WaitMultipleCallback::
                                                                            getKeyRegIndex)
                                                              .collect(Collectors.toList())));
        return WaitCallbackHandler.handleReturnInWaitMultiple(strand, retValReg, callbackList);
    }
    /**
     * This is used to propagate the results of {@link BVM#handleError(Strand)} to the
     * main CPU instruction loop.
     */
    public static class HandleErrorException extends BallerinaException {

        private static final long serialVersionUID = 1L;

        public WorkerExecutionContext ctx;

        public HandleErrorException(WorkerExecutionContext ctx) {
            this.ctx = ctx;
        }

    }

    private static void handleMapStore(Strand ctx, BMap<String, BRefType> bMap, String fieldName,
                                       BRefType<?> value) {
        BType mapType = bMap.getType();

        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                if (!isValidMapInsertion(mapType, value)) {
                    BType expType = ((BMapType) mapType).getConstrainedType();
                    throw new BLangMapStoreException(BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                                                     BLangExceptionHelper
                                                             .getErrorMessage(RuntimeErrors.INVALID_MAP_INSERTION,
                                                                              expType, value.getType()));
                }
                insertToMap(ctx, bMap, fieldName, value);
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.SERVICE_TAG:
                BObjectType objType = (BObjectType) mapType;
                BField objField = objType.getFields().get(fieldName);
                BType objFieldType = objField.getFieldType();
                if (!checkIsType(value, objFieldType)) {
                    throw new BLangMapStoreException(BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                                                     BLangExceptionHelper.getErrorMessage(
                                                             RuntimeErrors.INVALID_OBJECT_FIELD_ADDITION, fieldName,
                                                             objFieldType, value.getType()));
                }
                insertToMap(ctx, bMap, fieldName, value);
                break;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recType = (BRecordType) mapType;
                BField recField = recType.getFields().get(fieldName);
                BType recFieldType;

                if (recField != null) {
                    // If there is a corresponding field in the record, use it
                    recFieldType = recField.fieldType;
                } else if (recType.restFieldType != null) {
                    // If there isn't a corresponding field, but there is a rest field, use it
                    recFieldType = recType.restFieldType;
                } else {
                    // If both of the above conditions fail, the implication is that this is an attempt to insert a
                    // value to a non-existent field in a closed record.
                    throw new BLangMapStoreException(BallerinaErrorReasons.KEY_NOT_FOUND_ERROR,
                                                     BLangExceptionHelper.getErrorMessage(
                                                             RuntimeErrors.INVALID_RECORD_FIELD_ACCESS, fieldName,
                                                             recType));
                }

                if (!checkIsType(value, recFieldType)) {
                    throw new BLangMapStoreException(BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                                                     BLangExceptionHelper.getErrorMessage(
                                                             RuntimeErrors.INVALID_RECORD_FIELD_ADDITION, fieldName,
                                                             recFieldType, value.getType()));
                }

                insertToMap(ctx, bMap, fieldName, value);
                break;
        }
    }

    private static void insertToMap(Strand ctx, BMap bMap, String fieldName, BValue value) {
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
            ctx.setError(BLangVMErrors.createError(ctx, e.getMessage(), errMessage + e.getDetail()));
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

    private static boolean isDeepStampingRequiredForArray(BType sourceType) {
        BType elementType = ((BArrayType) sourceType).getElementType();

        if (elementType != null) {
            if (BTypes.isValueType(elementType)) {
                return false;
            } else if (elementType instanceof BArrayType) {
                return isDeepStampingRequiredForArray(elementType);
            }
            return true;
        }
        return true;
    }

    private static boolean isDeepStampingRequiredForMap(BType sourceType) {
        BType constrainedType = ((BMapType) sourceType).getConstrainedType();

        if (constrainedType != null) {
            if (BTypes.isValueType(constrainedType)) {
                return false;
            } else if (constrainedType instanceof BMapType) {
                return isDeepStampingRequiredForMap(constrainedType);
            }
            return true;
        }
        return true;
    }

    public static BType resolveMatchingTypeForUnion(BValue value, BType type) {
        if (value instanceof BValueArray && value.getType().getTag() == TypeTags.ARRAY_TAG &&
                !isDeepStampingRequiredForArray(((BValueArray) value).getArrayType())) {
            return ((BValueArray) value).getArrayType();
        }

        if (value instanceof BMap && value.getType().getTag() == TypeTags.MAP_TAG &&
                !isDeepStampingRequiredForMap(value.getType())) {
            return value.getType();
        }

        if (checkIsLikeType(value, BTypes.typeInt)) {
            return BTypes.typeInt;
        }

        if (checkIsLikeType(value, BTypes.typeFloat)) {
            return BTypes.typeFloat;
        }

        if (checkIsLikeType(value, BTypes.typeString)) {
            return BTypes.typeString;
        }

        if (checkIsLikeType(value, BTypes.typeBoolean)) {
            return BTypes.typeBoolean;
        }

        if (checkIsLikeType(value, BTypes.typeByte)) {
            return BTypes.typeByte;
        }

        BType anydataArrayType = new BArrayType(type);
        if (checkIsLikeType(value, anydataArrayType)) {
            return anydataArrayType;
        }

        if (checkIsLikeType(value, BTypes.typeXML)) {
            return BTypes.typeXML;
        }

        BType anydataMapType = new BMapType(type);
        if (checkIsLikeType(value, anydataMapType)) {
            return anydataMapType;
        }

        //not possible
        return null;
    }

    public static boolean checkIsLikeType(BValue sourceValue, BType targetType) {
        return checkIsLikeType(sourceValue, targetType, new ArrayList<>());
    }

    public static boolean checkIsLikeType(BValue sourceValue, BType targetType, List<TypeValuePair> unresolvedValues) {
        BType sourceType = sourceValue == null ? BTypes.typeNull : sourceValue.getType();
        if (checkIsType(sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        switch (targetType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsLikeRecordType(sourceValue, (BRecordType) targetType, unresolvedValues);
            case TypeTags.JSON_TAG:
                return checkIsLikeJSONType(sourceValue, (BJSONType) targetType, unresolvedValues);
            case TypeTags.MAP_TAG:
                return checkIsLikeMapType(sourceValue, (BMapType) targetType, unresolvedValues);
            case TypeTags.ARRAY_TAG:
                return checkIsLikeArrayType(sourceValue, (BArrayType) targetType, unresolvedValues);
            case TypeTags.TUPLE_TAG:
                return checkIsLikeTupleType(sourceValue, (BTupleType) targetType, unresolvedValues);
            case TypeTags.ANYDATA_TAG:
                return checkIsLikeAnydataType(sourceValue, targetType, unresolvedValues);
            case TypeTags.FINITE_TYPE_TAG:
                return checkFiniteTypeAssignable(sourceValue, targetType);
            case TypeTags.UNION_TAG:
                return ((BUnionType) targetType).getMemberTypes().stream()
                        .anyMatch(type -> checkIsLikeType(sourceValue, type, unresolvedValues));
            default:
                return false;
        }
    }

    private static boolean checkIsLikeAnydataType(BValue sourceValue, BType targetType,
                                                  List<TypeValuePair> unresolvedValues) {
        switch (sourceValue.getType().getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.MAP_TAG:
                return ((BMap) sourceValue).getMap().values().stream()
                        .allMatch(value -> checkIsLikeType((BValue) value, targetType, unresolvedValues));
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
                        return Arrays.stream(((BValueArray) sourceValue).getValues())
                                .allMatch(value -> checkIsLikeType(value, targetType, unresolvedValues));
                }
            case TypeTags.TUPLE_TAG:
                return Arrays.stream(((BValueArray) sourceValue).getValues())
                        .allMatch(value -> checkIsLikeType(value, targetType, unresolvedValues));
            case TypeTags.ANYDATA_TAG:
                return true;
            case TypeTags.FINITE_TYPE_TAG:
            case TypeTags.UNION_TAG:
                return checkIsLikeType(sourceValue, targetType, unresolvedValues);
            default:
                return false;
        }
    }

    private static boolean checkIsLikeTupleType(BValue sourceValue, BTupleType targetType,
                                                List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof BValueArray)) {
            return false;
        }

        BValueArray source = (BValueArray) sourceValue;
        if (source.size() != targetType.getTupleTypes().size()) {
            return false;
        }

        if (BTypes.isValueType(source.elementType)) {
            int bound = (int) source.size();
            for (int i = 0; i < bound; i++) {
                if (!checkIsType(source.elementType, targetType.getTupleTypes().get(i), new ArrayList<>())) {
                    return false;
                }
            }
            return true;
        }

        int bound = (int) source.size();
        for (int i = 0; i < bound; i++) {
            if (!checkIsLikeType(source.getRefValue(i), targetType.getTupleTypes().get(i), unresolvedValues)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeArrayType(BValue sourceValue, BArrayType targetType, 
                                                List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof BValueArray)) {
            return false;
        }

        BValueArray source = (BValueArray) sourceValue;
        if (BTypes.isValueType(source.elementType)) {
            return checkIsType(source.elementType, targetType.getElementType(), new ArrayList<>());
        }

        BType arrayElementType = targetType.getElementType();
        BRefType<?>[] arrayValues = source.getValues();
        for (int i = 0; i < ((BValueArray) sourceValue).size(); i++) {
            if (!checkIsLikeType(arrayValues[i], arrayElementType, unresolvedValues)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeMapType(BValue sourceValue, BMapType targetType,
                                              List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof BMap)) {
            return false;
        }

        for (Object mapEntry : ((BMap) sourceValue).values()) {
            if (!checkIsLikeType((BValue) mapEntry, targetType.getConstrainedType(), unresolvedValues)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeJSONType(BValue sourceValue, BJSONType targetType, 
                                               List<TypeValuePair> unresolvedValues) {
        if (sourceValue.getType().getTag() == TypeTags.ARRAY_TAG) {
            BValueArray source = (BValueArray) sourceValue;
            if (BTypes.isValueType(source.elementType)) {
                return checkIsType(source.elementType, targetType, new ArrayList<>());
            }

            BRefType<?>[] arrayValues = source.getValues();
            for (int i = 0; i < ((BValueArray) sourceValue).size(); i++) {
                if (!checkIsLikeType(arrayValues[i], targetType, unresolvedValues)) {
                    return false;
                }
            }
        } else if (sourceValue.getType().getTag() == TypeTags.MAP_TAG) {
            for (BValue value : ((BMap) sourceValue).values()) {
                if (!checkIsLikeType(value, targetType, unresolvedValues)) {
                    return false;
                }
            }
        } else if (sourceValue.getType().getTag() == TypeTags.RECORD_TYPE_TAG) {
            TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
            if (unresolvedValues.contains(typeValuePair)) {
                return true;
            }
            unresolvedValues.add(typeValuePair);
            for (Object object : ((BMap) sourceValue).getMap().values()) {
                if (!checkIsLikeType((BValue) object, targetType, unresolvedValues)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIsLikeRecordType(BValue sourceValue, BRecordType targetType,
                                                 List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof BMap)) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }
        unresolvedValues.add(typeValuePair);
        Map<String, BType> targetTypeField = new HashMap<>();
        BType restFieldType = targetType.restFieldType;

        for (BField field : targetType.getFields().values()) {
            targetTypeField.put(field.getFieldName(), field.fieldType);
        }

        for (Map.Entry targetTypeEntry : targetTypeField.entrySet()) {
            String fieldName = targetTypeEntry.getKey().toString();

            if (!(((BMap) sourceValue).getMap().containsKey(fieldName)) &&
                    !Flags.isFlagOn(targetType.getFields().get(fieldName).flags, Flags.OPTIONAL)) {
                return false;
            }
        }

        for (Object object : ((BMap) sourceValue).getMap().entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();

            if (targetTypeField.containsKey(fieldName)) {
                if (!checkIsLikeType(((BValue) valueEntry.getValue()), targetTypeField.get(fieldName),
                                     unresolvedValues)) {
                    return false;
                }
            } else {
                if (!targetType.sealed) {
                    if (!checkIsLikeType(((BValue) valueEntry.getValue()), restFieldType, unresolvedValues)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkIsType(BValue sourceVal, BType targetType) {
        if (isMutable(sourceVal)) {
            BType sourceType = sourceVal == null ? BTypes.typeNull : sourceVal.getType();
            return checkIsType(sourceType, targetType, new ArrayList<>());
        }

        return checkIsLikeType(sourceVal, targetType, new ArrayList<>());
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
            case TypeTags.SERVICE_TAG:
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
        switch (sourceType.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.JSON_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                // Element type of the array should be 'is type' JSON
                return checkIsType(((BArrayType) sourceType).getElementType(), targetType, unresolvedTypes);
            case TypeTags.MAP_TAG:
                return checkCastByType(((BMapType) sourceType).getConstrainedType(), targetType, unresolvedTypes);
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

        // If both are sealed (one is sealed means other is also sealed) check the rest field type
        if (!sourceRecordType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, BField> sourceFields = sourceRecordType.getFields();
        Set<String> targetFieldNames = targetType.getFields().keySet();

        for (BField targetField : targetType.getFields().values()) {
            BField sourceField = sourceFields.get(targetField.getFieldName());

            // If the LHS field is a required one, there has to be a corresponding required field in the RHS record.
            if (!Flags.isFlagOn(targetField.flags, Flags.OPTIONAL)
                    && (sourceField == null || Flags.isFlagOn(sourceField.flags, Flags.OPTIONAL))) {
                return false;
            }

            if (sourceField == null || !checkIsType(sourceField.fieldType, targetField.fieldType, unresolvedTypes)) {
                return false;
            }
        }

        // If there are fields remaining in the source record, first check if it's a closed record. Closed records
        // should only have the fields specified by its type.
        if (targetType.sealed) {
            return targetFieldNames.containsAll(sourceFields.keySet());
        }

        // If it's an open record, check if they are compatible with the rest field of the target type.
        return sourceFields.values().stream()
                .filter(field -> !targetFieldNames.contains(field.fieldName))
                .allMatch(field -> checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes));
    }

    private static boolean checkIsTableType(BType sourceType, BTableType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TABLE_TAG) {
            return false;
        }
        return checkTableConstraints(((BTableType) sourceType).getConstrainedType(),
                                     targetType.getConstrainedType(), unresolvedTypes);
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

    private static boolean checkTableConstraints(BType sourceConstraint, BType targetConstraint,
                                                 List<TypePair> unresolvedTypes) {
        // handle unconstrained tables returned by actions
        if (sourceConstraint == null) {
            if (targetConstraint.getTag() == TypeTags.RECORD_TYPE_TAG) {
                BRecordType targetConstrRecord = (BRecordType) targetConstraint;
                return !targetConstrRecord.sealed && targetConstrRecord.restFieldType == BTypes.typeAnydata;
            }
            return false;
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
     * @param lhsValue          The value on the left hand side
     * @param rhsValue          The value on the right hand side
     * @param checkedValues     Structured value pairs already compared or being compared
     * @return True if values are equal, else false.
     */
    private static boolean isEqual(BValue lhsValue, BValue rhsValue, List<ValuePair> checkedValues) {
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
                return isMappingType(rhsValTypeTag) && isEqual((BMap) lhsValue, (BMap) rhsValue, checkedValues);
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                return isListType(rhsValTypeTag) && isEqual((BNewArray) lhsValue, (BNewArray) rhsValue, checkedValues);
            case TypeTags.SERVICE_TAG:
                break;
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
     * @param lhsList           The array/tuple on the left hand side
     * @param rhsList           The array/tuple on the right hand side
     * @param checkedValues     Structured value pairs already compared or being compared
     * @return True if the array/tuple values are equal, else false.
     */
    private static boolean isEqual(BNewArray lhsList, BNewArray rhsList, List<ValuePair> checkedValues) {
        ValuePair compValuePair = new ValuePair(lhsList, rhsList);
        if (checkedValues.contains(compValuePair)) {
            return true;
        }
        checkedValues.add(compValuePair);

        if (lhsList.size() != rhsList.size()) {
            return false;
        }

        for (int i = 0; i < lhsList.size(); i++) {
            if (!isEqual(lhsList.getBValue(i), rhsList.getBValue(i), checkedValues)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deep equality check for a map.
     *
     * @param lhsMap            Map on the left hand side
     * @param rhsMap            Map on the right hand side
     * @param checkedValues     Structured value pairs already compared or being compared
     * @return True if the map values are equal, else false.
     */
    private static boolean isEqual(BMap lhsMap, BMap rhsMap, List<ValuePair> checkedValues) {
        ValuePair compValuePair = new ValuePair(lhsMap, rhsMap);
        if (checkedValues.contains(compValuePair)) {
            return true;
        }
        checkedValues.add(compValuePair);

        if (lhsMap.size() != rhsMap.size()) {
            return false;
        }

        if (!lhsMap.getMap().keySet().containsAll(rhsMap.getMap().keySet())) {
            return false;
        }

        Iterator<Map.Entry<String, BValue>> mapIterator = lhsMap.getMap().entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, BValue> lhsMapEntry = mapIterator.next();
            if (!isEqual(lhsMapEntry.getValue(), rhsMap.get(lhsMapEntry.getKey()), checkedValues)) {
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
     * result as {@link BVM#isEqual(BValue, BValue, List)}
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
            return isEqual(lhsValue, rhsValue, Collections.emptyList());
        }

        return false;
    }

    /**
     * Reference inequality check for values. If both the values are simple basic types, returns the same
     * result as the negation of {@link BVM#isEqual(BValue, BValue, List)}
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
            return !isEqual(lhsValue, rhsValue, Collections.emptyList());
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

    /**
     * Unordered BValue vector of size two, to hold two values being compared.
     *
     * @since 0.985.0
     */
    private static class ValuePair {
        List<BValue> valueList = new ArrayList<>(2);

        ValuePair(BValue valueOne, BValue valueTwo) {
            valueList.add(valueOne);
            valueList.add(valueTwo);
        }

        @Override
        public boolean equals(Object otherPair) {
            if (!(otherPair instanceof ValuePair)) {
                return false;
            }

            return ((ValuePair) otherPair).valueList.containsAll(valueList) &&
                    valueList.containsAll(((ValuePair) otherPair).valueList);
        }
    }

    /**
     * Type vector of size two, to hold the source value and the target type.
     *
     * @since 0.990.3
     */
    public static class TypeValuePair {
        BValue sourceValue;
        BType targetType;

        public TypeValuePair(BValue sourceValue, BType targetType) {
            this.sourceValue = sourceValue;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypeValuePair)) {
                return false;
            }
            TypeValuePair other = (TypeValuePair) obj;
            return this.sourceValue.equals(other.sourceValue) && this.targetType.equals(other.targetType);
        }
    }
}
