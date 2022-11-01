/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import io.ballerina.identifier.Utils;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JIConstructorCall;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JIMethodCall;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTerminator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JavaMethodCall;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ENV;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ERROR_REASONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_EXTENSION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BIG_DECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_EXCEPTION_HELPER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLOCKED_ON_EXTERN_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DEFAULT_STRAND_DISPATCHER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_LOCK_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.IS_BLOCKED_ON_EXTERN_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAKE_CONCAT_WITH_CONSTANTS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_FUNCTION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_LOCAL_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_TRANSACTIONAL_FUNCTION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_TRANSACTIONAL_LOCAL_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_OF_HEADING_WITH_SEMICOLON;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_POLICY_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_VALUE_ANY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_CONCAT_FACTORY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WD_CHANNELS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_DATA_CHANNEL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addJUnboxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANNOTATION_GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BAL_ENV_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BOBJECT_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_LOCK_FROM_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_LOCK_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RUNTIME_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RUNTIME_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_WD_CHANNELS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_WORKER_DATA_CHANNEL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_CHANNEL_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_DESCRIPTOR_FOR_STRING_CONCAT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_FLUSH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_WAIT_ANY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_WAIT_MULTIPLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_WORKER_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_BAL_ENV;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_DECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INT_TO_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INT_VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.IS_CONCURRENT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOCK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_PUT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PANIC_IF_UNLOCK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SCHEDULE_LOCAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SCHEDULE_TRANSACTIONAL_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SEND_DATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SYNC_SEND_DATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TRY_TAKE_DATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VALUE_OF_DECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.WAIT_RESULT;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.genVarArg;

/**
 * BIR terminator instruction generator class to keep track of method visitor and index map.
 *
 * @since 1.2.0
 */
public class JvmTerminatorGen {

    private MethodVisitor mv;
    private BIRVarToJVMIndexMap indexMap;
    private LabelGenerator labelGen;
    private JvmErrorGen errorGen;
    private String currentPackageName;
    private String moduleInitClass;
    private JvmPackageGen jvmPackageGen;
    private JvmInstructionGen jvmInstructionGen;
    private PackageCache packageCache;
    private SymbolTable symbolTable;
    private Unifier unifier;
    private JvmTypeGen jvmTypeGen;
    private JvmCastGen jvmCastGen;
    private AsyncDataCollector asyncDataCollector;

    public JvmTerminatorGen(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, LabelGenerator labelGen,
                            JvmErrorGen errorGen, PackageID packageID, JvmInstructionGen jvmInstructionGen,
                            JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen,
                            JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector) {

        this.mv = mv;
        this.indexMap = indexMap;
        this.labelGen = labelGen;
        this.errorGen = errorGen;
        this.jvmPackageGen = jvmPackageGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmCastGen = jvmCastGen;
        this.packageCache = jvmPackageGen.packageCache;
        this.jvmInstructionGen = jvmInstructionGen;
        this.symbolTable = jvmPackageGen.symbolTable;
        this.currentPackageName = JvmCodeGenUtil.getPackageName(packageID);
        this.moduleInitClass = JvmCodeGenUtil.getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME);
        this.unifier = new Unifier();
        this.asyncDataCollector = asyncDataCollector;
    }

    private static void genYieldCheckForLock(MethodVisitor mv, LabelGenerator labelGen, String funcName,
                                             int localVarOffset, int yieldLocationVarIndex, int yieldStatusVarIndex,
                                             String fullyQualifiedFuncName, Location terminatorPos) {

        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "isYielded", "()Z", false);
        JvmCodeGenUtil.generateSetYieldedStatus(mv, labelGen, funcName, yieldLocationVarIndex,
                terminatorPos, fullyQualifiedFuncName, "WAITING FOR LOCK", yieldStatusVarIndex);
    }

    private void loadDefaultValue(MethodVisitor mv, BType type) {
        BType bType = JvmCodeGenUtil.getReferredType(type);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitInsn(LCONST_0);
            return;
        } else if (TypeTags.isStringTypeTag(bType.tag) || TypeTags.isXMLTypeTag(bType.tag)) {
            mv.visitInsn(ACONST_NULL);
            return;
        }

        switch (bType.tag) {
            case TypeTags.FLOAT:
                mv.visitInsn(DCONST_0);
                break;
            case TypeTags.BYTE:
            case TypeTags.BOOLEAN:
                mv.visitInsn(ICONST_0);
                break;
            case TypeTags.MAP:
            case TypeTags.ARRAY:
            case TypeTags.ERROR:
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.OBJECT:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.RECORD:
            case TypeTags.TUPLE:
            case TypeTags.FUTURE:
            case TypeTags.JSON:
            case TypeTags.INVOKABLE:
            case TypeTags.FINITE:
            case TypeTags.HANDLE:
            case TypeTags.TYPEDESC:
            case TypeTags.READONLY:
            case TypeTags.STREAM:
                mv.visitInsn(ACONST_NULL);
                break;
            case TypeTags.DECIMAL:
                mv.visitTypeInsn(NEW, DECIMAL_VALUE);
                mv.visitInsn(DUP);
                mv.visitInsn(DCONST_0);
                mv.visitMethodInsn(INVOKESTATIC, BIG_DECIMAL, VALUE_OF_METHOD, VALUE_OF_DECIMAL,
                        false);
                mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, JVM_INIT_METHOD, INIT_DECIMAL,
                        false);
                break;
            case JTypeTags.JTYPE:
                loadDefaultJValue(mv, (JType) bType);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         bType);
        }
    }

    private void loadDefaultJValue(MethodVisitor mv, JType jType) {
        switch (jType.jTag) {
            case JTypeTags.JBYTE:
            case JTypeTags.JBOOLEAN:
            case JTypeTags.JINT:
            case JTypeTags.JSHORT:
            case JTypeTags.JCHAR:
                mv.visitInsn(ICONST_0);
                break;
            case JTypeTags.JLONG:
                mv.visitInsn(LCONST_0);
                break;
            case JTypeTags.JFLOAT:
                mv.visitInsn(FCONST_0);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitInsn(DCONST_0);
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                mv.visitInsn(ACONST_NULL);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         jType);
        }
    }

    public void genTerminator(BIRTerminator terminator, String moduleClassName, BIRNode.BIRFunction func,
                              String funcName, int localVarOffset, int returnVarRefIndex, BType attachedType,
                              int yieldLocationVarIndex, int yieldStatusVarIndex, int invocationVarIndex,
                              String fullyQualifiedFuncName) {

        switch (terminator.kind) {
            case LOCK:
                this.genLockTerm((BIRTerminator.Lock) terminator, funcName, localVarOffset, yieldLocationVarIndex,
                        terminator.pos, fullyQualifiedFuncName, yieldStatusVarIndex);
                return;
            case UNLOCK:
                this.genUnlockTerm((BIRTerminator.Unlock) terminator, funcName);
                return;
            case GOTO:
                this.genGoToTerm((BIRTerminator.GOTO) terminator, funcName);
                return;
            case CALL:
                this.genCallTerm((BIRTerminator.Call) terminator, localVarOffset);
                return;
            case ASYNC_CALL:
                this.genAsyncCallTerm((BIRTerminator.AsyncCall) terminator, localVarOffset,
                                      moduleClassName, attachedType, funcName);
                return;
            case BRANCH:
                this.genBranchTerm((BIRTerminator.Branch) terminator, funcName);
                return;
            case RETURN:
                this.genReturnTerm(returnVarRefIndex, func, invocationVarIndex);
                return;
            case PANIC:
                this.errorGen.genPanic((BIRTerminator.Panic) terminator);
                return;
            case WAIT:
                this.generateWaitIns((BIRTerminator.Wait) terminator, localVarOffset);
                return;
            case WAIT_ALL:
                this.genWaitAllIns((BIRTerminator.WaitAll) terminator, localVarOffset);
                return;
            case FP_CALL:
                this.genFPCallIns((BIRTerminator.FPCall) terminator, moduleClassName, attachedType,
                                  funcName, localVarOffset, invocationVarIndex);
                return;
            case WK_SEND:
                this.genWorkerSendIns((BIRTerminator.WorkerSend) terminator, localVarOffset, invocationVarIndex);
                return;
            case WK_RECEIVE:
                this.genWorkerReceiveIns((BIRTerminator.WorkerReceive) terminator, localVarOffset, invocationVarIndex);
                return;
            case FLUSH:
                this.genFlushIns((BIRTerminator.Flush) terminator, localVarOffset, invocationVarIndex);
                return;
            case PLATFORM:
                this.genPlatformIns((JTerminator) terminator, attachedType, localVarOffset);
                return;
        }
        throw new BLangCompilerException("JVM generation is not supported for terminator instruction " +
                terminator);

    }

    private void genGoToTerm(BIRTerminator.GOTO gotoIns, String funcName) {

        Label gotoLabel = this.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void genLockTerm(BIRTerminator.Lock lockIns, String funcName, int localVarOffset, int yieldLocationVarIndex,
                             Location terminatorPos, String fullyQualifiedFuncName, int yieldStatusVarIndex) {

        Label gotoLabel = this.labelGen.getLabel(funcName + lockIns.lockedBB.id.value);
        String lockStore = "L" + LOCK_STORE + ";";
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        String lockName = GLOBAL_LOCK_NAME + lockIns.lockId;
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
        this.mv.visitLdcInsn(lockName);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", GET_LOCK_FROM_MAP, false);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "lock", LOCK, false);
        this.mv.visitInsn(POP);
        genYieldCheckForLock(this.mv, this.labelGen, funcName, localVarOffset, yieldLocationVarIndex,
                yieldStatusVarIndex, fullyQualifiedFuncName, terminatorPos);
        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void genUnlockTerm(BIRTerminator.Unlock unlockIns, String funcName) {

        Label gotoLabel = this.labelGen.getLabel(funcName + unlockIns.unlockBB.id.value);

        // unlocked in the same order https://yarchive.net/comp/linux/lock_ordering.html
        String lockStore = "L" + LOCK_STORE + ";";
        String lockName = GLOBAL_LOCK_NAME + unlockIns.relatedLock.lockId;
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
        this.mv.visitLdcInsn(lockName);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", GET_LOCK_MAP, false);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "unlock", "()V", false);

        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void handleErrorRetInUnion(int returnVarRefIndex, List<BIRNode.ChannelDetails> channels, BUnionType bType,
                                       int invocationVarIndex) {

        if (channels.size() == 0) {
            return;
        }

        boolean errorIncluded = false;
        for (BType member : bType.getMemberTypes()) {
            member = JvmCodeGenUtil.getReferredType(member);
            if (member.tag == TypeTags.ERROR) {
                errorIncluded = true;
                break;
            }
        }

        if (errorIncluded) {
            this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            this.mv.visitVarInsn(ALOAD, 0);
            JvmCodeGenUtil.loadChannelDetails(this.mv, channels, invocationVarIndex);
            this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "handleWorkerError",
                                    HANDLE_WORKER_ERROR, false);
        }
    }

    private void notifyChannels(List<BIRNode.ChannelDetails> channels, int retIndex, int invocationVarIndex) {

        if (channels.size() == 0) {
            return;
        }

        this.mv.visitVarInsn(ALOAD, 0);
        JvmCodeGenUtil.loadChannelDetails(this.mv, channels, invocationVarIndex);
        this.mv.visitVarInsn(ALOAD, retIndex);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleChannelError", HANDLE_CHANNEL_ERROR, false);
    }

    private void genBranchTerm(BIRTerminator.Branch branchIns, String funcName) {

        String trueBBId = branchIns.trueBB.id.value;
        String falseBBId = branchIns.falseBB.id.value;

        this.loadVar(branchIns.op.variableDcl);

        Label trueBBLabel = this.labelGen.getLabel(funcName + trueBBId);
        this.mv.visitJumpInsn(IFGT, trueBBLabel);

        Label falseBBLabel = this.labelGen.getLabel(funcName + falseBBId);
        this.mv.visitJumpInsn(GOTO, falseBBLabel);
    }

    private void genCallTerm(BIRTerminator.Call callIns, int localVarOffset) {
        // invoke the function
        this.genCall(callIns, callIns.calleePkg, localVarOffset);

        // store return
        this.storeReturnFromCallIns(callIns.lhsOp != null ? callIns.lhsOp.variableDcl : null);
    }

    private void genPlatformIns(JTerminator terminator, BType attachedType, int localVarOffset) {
        switch (terminator.jTermKind) {
            case J_METHOD_CALL:
                this.genJCallTerm((JavaMethodCall) terminator, attachedType, localVarOffset);
                return;
            case JI_METHOD_CALL:
                this.genJICallTerm((JIMethodCall) terminator, localVarOffset);
                return;
            case JI_CONSTRUCTOR_CALL:
                this.genJIConstructorTerm((JIConstructorCall) terminator, localVarOffset);
        }
    }

    private void genJCallTerm(JavaMethodCall callIns, BType attachedType, int localVarOffset) {
        // Load function parameters of the target Java method to the stack..
        Label blockedOnExternLabel = new Label();
        Label notBlockedOnExternLabel = new Label();

        genHandlingBlockedOnExternal(localVarOffset, blockedOnExternLabel);

        if (callIns.lhsOp != null && callIns.lhsOp.variableDcl != null) {
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue",
                                   "Ljava/lang/Object;");
            jvmCastGen.addUnboxInsn(this.mv, callIns.lhsOp.variableDcl.type); // store return
            this.storeToVar(callIns.lhsOp.variableDcl);
        }

        this.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

        this.mv.visitLabel(blockedOnExternLabel);

        int argIndex = 0;
        if (attachedType == null) {
            this.mv.visitVarInsn(ALOAD, localVarOffset);
        } else {
            // Below codes are not needed (as normal external funcs doesn't support attached invocations)
            // check whether function params already include the self
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            BIRNode.BIRVariableDcl selfArg = callIns.args.get(0).variableDcl;
            this.loadVar(selfArg);
            this.mv.visitTypeInsn(CHECKCAST, B_OBJECT);
            argIndex += 1;
        }

        int argsCount = callIns.args.size();
        while (argIndex < argsCount) {
            BIROperand arg = callIns.args.get(argIndex);
            this.loadVar(arg.variableDcl);
            argIndex += 1;
        }

        String jClassName = callIns.jClassName;
        this.mv.visitMethodInsn(INVOKESTATIC, jClassName, callIns.name, callIns.jMethodVMSig, false);

        if (callIns.lhsOp != null && callIns.lhsOp.variableDcl != null) {
            this.storeToVar(callIns.lhsOp.variableDcl);
        }

        this.mv.visitLabel(notBlockedOnExternLabel);
    }

    private void genJICallTerm(JIMethodCall callIns, int localVarOffset) {
        // Load function parameters of the target Java method to the stack..
        Label blockedOnExternLabel = new Label();
        Label notBlockedOnExternLabel = new Label();

        genHandlingBlockedOnExternal(localVarOffset, blockedOnExternLabel);
        if (callIns.lhsOp != null) {
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue",
                                   "Ljava/lang/Object;");
            // store return
            BIROperand lhsOpVarDcl = callIns.lhsOp;
            addJUnboxInsn(this.mv, ((JType) lhsOpVarDcl.variableDcl.type));
            this.storeToVar(lhsOpVarDcl.variableDcl);
        }

        this.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

        this.mv.visitLabel(blockedOnExternLabel);
        boolean isInterface = callIns.invocationType == INVOKEINTERFACE;

        int argIndex = 0;
        if (callIns.invocationType == INVOKEVIRTUAL || isInterface) {
            // check whether function params already include the self
            BIRNode.BIRVariableDcl selfArg = callIns.args.get(0).variableDcl;
            this.loadVar(selfArg);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD, RETURN_OBJECT,
                    false);
            this.mv.visitTypeInsn(CHECKCAST, callIns.jClassName);

            Label ifNonNullLabel = this.labelGen.getLabel("receiver_null_check");
            this.mv.visitLabel(ifNonNullLabel);
            this.mv.visitInsn(DUP);

            Label elseBlockLabel = this.labelGen.getLabel("receiver_null_check_else");
            this.mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
            Label thenBlockLabel = this.labelGen.getLabel("receiver_null_check_then");
            this.mv.visitLabel(thenBlockLabel);
            this.mv.visitFieldInsn(GETSTATIC, BAL_ERROR_REASONS, "JAVA_NULL_REFERENCE_ERROR",
                    GET_STRING);
            this.mv.visitFieldInsn(GETSTATIC, RUNTIME_ERRORS, "JAVA_NULL_REFERENCE",
                    GET_RUNTIME_ERROR);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitTypeInsn(ANEWARRAY, OBJECT);
            this.mv.visitMethodInsn(INVOKESTATIC, BLANG_EXCEPTION_HELPER, "getRuntimeException",
                    GET_RUNTIME_EXCEPTION, false);
            this.mv.visitInsn(ATHROW);
            this.mv.visitLabel(elseBlockLabel);
            argIndex += 1;
        }

        String jMethodVMSig = callIns.jMethodVMSig;
        boolean hasBalEnvParam = jMethodVMSig.startsWith(BAL_ENV_PARAM);

        if (hasBalEnvParam) {
            mv.visitTypeInsn(NEW, BAL_ENV);
            mv.visitInsn(DUP);
            this.mv.visitVarInsn(ALOAD, localVarOffset); // load the strand
            // load the current Module
            mv.visitFieldInsn(GETSTATIC, this.moduleInitClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
            mv.visitMethodInsn(INVOKESPECIAL, BAL_ENV, JVM_INIT_METHOD,
                               INIT_BAL_ENV, false);
        }

        int argsCount = callIns.varArgExist ? callIns.args.size() - 1 : callIns.args.size();
        while (argIndex < argsCount) {
            BIROperand arg = callIns.args.get(argIndex);
            this.loadVar(arg.variableDcl);
            argIndex += 1;
        }
        if (callIns.varArgExist) {
            BIROperand arg = callIns.args.get(argIndex);
            int localVarIndex = this.indexMap.addIfNotExists(arg.variableDcl.name.value, arg.variableDcl.type);
            genVarArg(this.mv, this.indexMap, arg.variableDcl.type, callIns.varArgType, localVarIndex, 
                      symbolTable, jvmCastGen);
        }

        String jClassName = callIns.jClassName;
        String jMethodName = callIns.name;
        this.mv.visitMethodInsn(callIns.invocationType, jClassName, jMethodName, jMethodVMSig, isInterface);

        boolean isVoidMethod = jMethodVMSig.endsWith(")V");
        if (callIns.lhsOp != null && callIns.lhsOp.variableDcl != null) {
            if (hasBalEnvParam && isVoidMethod) {
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue", "Ljava/lang/Object;");

                Label doNotStoreReturn = new Label();
                mv.visitJumpInsn(IFNULL, doNotStoreReturn);

                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue", "Ljava/lang/Object;");
                BIROperand lhsOpVarDcl = callIns.lhsOp;
                addJUnboxInsn(this.mv, ((JType) lhsOpVarDcl.variableDcl.type));
                this.storeToVar(lhsOpVarDcl.variableDcl);

                mv.visitLabel(doNotStoreReturn);
            } else {
                this.storeToVar(callIns.lhsOp.variableDcl);
            }
        }

        this.mv.visitLabel(notBlockedOnExternLabel);
    }

    private void genJIConstructorTerm(JIConstructorCall callIns, int localVarOffset) {
        // Load function parameters of the target Java method to the stack..
        Label blockedOnExternLabel = new Label();
        Label notBlockedOnExternLabel = new Label();

        genHandlingBlockedOnExternal(localVarOffset, blockedOnExternLabel);
        if (callIns.lhsOp.variableDcl != null) {
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue", GET_OBJECT);
            jvmCastGen.addUnboxInsn(this.mv, callIns.lhsOp.variableDcl.type);
            // store return
            BIRNode.BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;
            this.storeToVar(lhsOpVarDcl);
        }

        this.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

        this.mv.visitLabel(blockedOnExternLabel);

        this.mv.visitTypeInsn(NEW, callIns.jClassName);
        this.mv.visitInsn(DUP);

        int argIndex = 0;

        int argsCount = callIns.args.size();
        while (argIndex < argsCount) {
            BIROperand arg = callIns.args.get(argIndex);
            this.loadVar(arg.variableDcl);
            argIndex += 1;
        }

        String jClassName = callIns.jClassName;
        String jMethodName = callIns.name;
        String jMethodVMSig = callIns.jMethodVMSig;
        this.mv.visitMethodInsn(INVOKESPECIAL, jClassName, jMethodName, jMethodVMSig, false);

        BIRNode.BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;

        if (lhsOpVarDcl != null) {
            this.storeToVar(lhsOpVarDcl);
        }

        this.mv.visitLabel(notBlockedOnExternLabel);
    }

    private void genHandlingBlockedOnExternal(int localVarOffset, Label blockedOnExternLabel) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, IS_BLOCKED_ON_EXTERN_FIELD, "()Z", false);
        this.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitInsn(ICONST_0);
        this.mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, BLOCKED_ON_EXTERN_FIELD, "Z");

        // Throw error if strand has panic
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, PANIC_FIELD, GET_BERROR);
        Label panicLabel = new Label();
        mv.visitJumpInsn(IFNULL, panicLabel);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, PANIC_FIELD, GET_BERROR);
        mv.visitInsn(DUP);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, PANIC_FIELD, GET_BERROR);
        mv.visitInsn(ATHROW);
        mv.visitLabel(panicLabel);
    }

    private void storeReturnFromCallIns(BIRNode.BIRVariableDcl lhsOpVarDcl) {

        if (lhsOpVarDcl != null) {
            this.storeToVar(lhsOpVarDcl);
        } else {
            this.mv.visitInsn(POP);
        }
    }


    private void genCall(BIRTerminator.Call callIns, PackageID packageID, int localVarOffset) {

        if (!callIns.isVirtual) {
            this.genFuncCall(callIns, packageID, localVarOffset);
            return;
        }

        BIRNode.BIRVariableDcl selfArg = callIns.args.get(0).variableDcl;
        if (JvmCodeGenUtil.getReferredType(selfArg.type).tag == TypeTags.OBJECT) {
            this.genVirtualCall(callIns, JvmCodeGenUtil.isBallerinaBuiltinModule(
                    packageID.orgName.getValue(), packageID.name.getValue()), localVarOffset);
        } else {
            // then this is a function attached to a built-in type
            this.genBuiltinTypeAttachedFuncCall(callIns, packageID, localVarOffset);
        }
    }

    private void genFuncCall(BIRTerminator.Call callIns, PackageID packageID, int localVarOffset) {
        String methodName = callIns.name.value;
        this.genStaticCall(callIns, packageID, localVarOffset, methodName, methodName);
    }

    private void genBuiltinTypeAttachedFuncCall(BIRTerminator.Call callIns, PackageID packageID, int localVarOffset) {

        String methodLookupName = callIns.name.value;
        int optionalIndex = methodLookupName.indexOf(".");
        int index = optionalIndex != -1 ? optionalIndex + 1 : 0;
        String methodName = methodLookupName.substring(index);
        this.genStaticCall(callIns, packageID, localVarOffset, methodName, methodLookupName);
    }

    private void genStaticCall(BIRTerminator.Call callIns, PackageID packageID, int localVarOffset,
                               String methodName, String methodLookupName) {
        // load strand
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        String encodedMethodName = Utils.encodeFunctionIdentifier(methodLookupName);
        String packageName = JvmCodeGenUtil.getPackageName(callIns.calleePkg);


        int argsCount = callIns.args.size();
        int i = 0;
        while (i < argsCount) {
            BIROperand arg = callIns.args.get(i);
            this.loadVar(arg.variableDcl);
            i += 1;
        }
        BIRFunctionWrapper functionWrapper = jvmPackageGen.lookupBIRFunctionWrapper(packageName + encodedMethodName);
        if (functionWrapper == null) {
            // If the callee function from different module, we need to use decoded function name as lookup key.
            functionWrapper = jvmPackageGen.lookupBIRFunctionWrapper(packageName + Utils
                    .decodeIdentifier(methodLookupName));
        }
        String methodDesc;
        String jvmClass;
        if (functionWrapper != null) {
            jvmClass = functionWrapper.fullQualifiedClassName;
            methodDesc = functionWrapper.jvmMethodDescription;
        } else {
            BPackageSymbol symbol = packageCache.getSymbol(
                    packageID.orgName.getValue() + "/" + packageID.name.getValue());
            Name decodedMethodName = new Name(Utils.decodeIdentifier(methodName));
            BInvokableSymbol funcSymbol = (BInvokableSymbol) symbol.scope.lookup(decodedMethodName).symbol;
            BInvokableType type = (BInvokableType) funcSymbol.type;
            ArrayList<BType> params = new ArrayList<>(type.paramTypes);
            if (type.restType != null) {
                params.add(type.restType);
            }
            String balFileName = funcSymbol.source;


            if (balFileName == null || !balFileName.endsWith(BAL_EXTENSION)) {
                balFileName = MODULE_INIT_CLASS_NAME;
            }

            jvmClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                                                              JvmCodeGenUtil.cleanupPathSeparators(balFileName));
            //TODO: add receiver:  BType attachedType = type.r != null ? receiver.type : null;
            BType retType = unifier.build(type.retType);
            methodDesc = JvmCodeGenUtil.getMethodDesc(params, retType);
        }
        this.mv.visitMethodInsn(INVOKESTATIC, jvmClass, encodedMethodName, methodDesc, false);
    }

    private void genVirtualCall(BIRTerminator.Call callIns, boolean isBuiltInModule, int localVarOffset) {
        // load self
        BIRNode.BIRVariableDcl selfArg = callIns.args.get(0).variableDcl;
        this.loadVar(selfArg);
        this.mv.visitTypeInsn(CHECKCAST, B_OBJECT);

        // load the strand
        this.mv.visitVarInsn(ALOAD, localVarOffset);

        // load the function name as the second argument
        this.mv.visitLdcInsn(JvmCodeGenUtil.rewriteVirtualCallTypeName(callIns.name.value));

        // create an Object[] for the rest params
        int argsCount = callIns.args.size() - 1;
        this.mv.visitLdcInsn((long) (argsCount));
        this.mv.visitInsn(L2I);
        this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int i = 0;
        int j = 0;
        while (i < argsCount) {
            this.mv.visitInsn(DUP);
            this.mv.visitLdcInsn((long) j);
            this.mv.visitInsn(L2I);
            j += 1;
            // i + 1 is used since we skip the first argument (self)
            BIROperand arg = callIns.args.get(i + 1);
            this.loadVar(arg.variableDcl);

            // Add the to the rest params array
            jvmCastGen.addBoxInsn(this.mv, arg.variableDcl.type);
            this.mv.visitInsn(AASTORE);
            i += 1;
        }

        // call method
        String methodDesc = BOBJECT_CALL;
        this.mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "call", methodDesc, true);

        BType returnType = callIns.lhsOp.variableDcl.type;
        jvmCastGen.addUnboxInsn(this.mv, returnType);
    }

    private void genAsyncCallTerm(BIRTerminator.AsyncCall callIns, int localVarOffset, String moduleClassName,
                                  BType attachedType, String parentFunction) {

        PackageID calleePkgId = callIns.calleePkg;

        String orgName = calleePkgId.orgName.value;
        String moduleName = calleePkgId.name.value;

        // Check if already locked before submitting to scheduler.
        String lockStore = "L" + LOCK_STORE + ";";
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
        this.mv.visitLdcInsn(GLOBAL_LOCK_NAME);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "panicIfInLock", PANIC_IF_UNLOCK, false);

        // Load the scheduler from strand
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", GET_SCHEDULER);

        // create an Object[] for the rest params
        int argsCount = callIns.args.size();
        //create an object array of args
        this.mv.visitLdcInsn((long) (argsCount + 1));
        this.mv.visitInsn(L2I);
        this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int paramIndex = 1;
        for (BIROperand arg : callIns.args) {
            this.mv.visitInsn(DUP);
            this.mv.visitLdcInsn((long) paramIndex);
            this.mv.visitInsn(L2I);

            this.loadVar(arg.variableDcl);
            // Add the to the rest params array
            jvmCastGen.addBoxInsn(this.mv, arg.variableDcl.type);
            this.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }
        String funcName = Utils.encodeFunctionIdentifier(callIns.name.value);
        String lambdaName = "$" + funcName + "$lambda$_" + asyncDataCollector.getLambdaIndex() + "$";

        JvmCodeGenUtil.createFunctionPointer(this.mv, asyncDataCollector.getEnclosingClass(), lambdaName);
        asyncDataCollector.add(lambdaName, callIns);
        asyncDataCollector.incrementLambdaIndex();

        boolean concurrent = false;
        String strandName = null;
        // check for concurrent annotation
        if (callIns.annotAttachments.size() > 0) {
            for (BIRNode.BIRAnnotationAttachment annotationAttachment : callIns.annotAttachments) {
                if (annotationAttachment == null ||
                        !STRAND.equals(annotationAttachment.annotTagRef.value) ||
                        !JvmCodeGenUtil.isBuiltInPackage(annotationAttachment.annotPkgId)) {
                    continue;
                }

                Object strandAnnot = ((BIRNode.BIRConstAnnotationAttachment) annotationAttachment).annotValue.value;
                if (strandAnnot instanceof Map) {
                    Map<String, BIRNode.ConstValue> recordValue = (Map<String, BIRNode.ConstValue>) strandAnnot;
                    if (recordValue.containsKey(STRAND_THREAD)) {
                        if (STRAND_VALUE_ANY.equals(recordValue.get(STRAND_THREAD).value)) {
                            concurrent = true;
                        }
                    }

                    if (recordValue.containsKey(STRAND_NAME)) {
                        strandName = recordValue.get(STRAND_NAME).value.toString();

                    }

                    if (recordValue.containsKey(STRAND_POLICY_NAME)) {
                        if (!DEFAULT_STRAND_DISPATCHER.equals(recordValue.get(STRAND_POLICY_NAME).value)) {
                            throw new BLangCompilerException("Unsupported policy. Only 'DEFAULT' policy is " +
                                    "supported by jBallerina runtime.");
                        }
                    }
                }
                break;
            }
        }
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        loadFpReturnType(callIns.lhsOp);
        String workerName =  strandName;
        if (workerName == null) {
            if (callIns.lhsOp.variableDcl.metaVarName != null) {
                this.mv.visitLdcInsn(callIns.lhsOp.variableDcl.metaVarName);
            } else {
                mv.visitInsn(ACONST_NULL);
            }
        } else {
            this.mv.visitLdcInsn(workerName);
        }

        this.submitToScheduler(callIns.lhsOp, moduleClassName, attachedType, parentFunction, concurrent,
                callIns.transactional);
    }

    private void generateWaitIns(BIRTerminator.Wait waitInst, int localVarOffset) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitTypeInsn(NEW, ARRAY_LIST);
        this.mv.visitInsn(DUP);
        this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, "()V", false);

        int i = 0;
        while (i < waitInst.exprList.size()) {
            this.mv.visitInsn(DUP);
            BIROperand futureVal = waitInst.exprList.get(i);
            if (futureVal != null) {
                this.loadVar(futureVal.variableDcl);
            }
            this.mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", ANY_TO_JBOOLEAN, true);
            this.mv.visitInsn(POP);
            i += 1;
        }

        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleWaitAny", HANDLE_WAIT_ANY, false);
        BIRNode.BIRVariableDcl tempVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("waitResult"),
                                                                    VarScope.FUNCTION, VarKind.ARG);
        int resultIndex = this.getJVMIndexOfVarRef(tempVar);
        this.mv.visitVarInsn(ASTORE, resultIndex);

        // assign result if result available
        Label afterIf = new Label();
        this.mv.visitVarInsn(ALOAD, resultIndex);
        this.mv.visitFieldInsn(GETFIELD, WAIT_RESULT, "done", "Z");
        this.mv.visitJumpInsn(IFEQ, afterIf);
        Label withinIf = new Label();
        this.mv.visitLabel(withinIf);
        this.mv.visitVarInsn(ALOAD, resultIndex);
        this.mv.visitFieldInsn(GETFIELD, WAIT_RESULT, "result",
                               GET_OBJECT);
        jvmCastGen.addUnboxInsn(this.mv, waitInst.lhsOp.variableDcl.type);
        this.storeToVar(waitInst.lhsOp.variableDcl);
        this.mv.visitLabel(afterIf);
    }

    private void genWaitAllIns(BIRTerminator.WaitAll waitAll, int localVarOffset) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitTypeInsn(NEW, HASH_MAP);
        this.mv.visitInsn(DUP);
        this.mv.visitMethodInsn(INVOKESPECIAL, HASH_MAP, JVM_INIT_METHOD, "()V", false);
        int i = 0;
        while (i < waitAll.keys.size()) {
            this.mv.visitInsn(DUP);
            this.mv.visitLdcInsn(waitAll.keys.get(i));
            BIROperand futureRef = waitAll.valueExprs.get(i);
            if (futureRef != null) {
                this.loadVar(futureRef.variableDcl);
            }
            this.mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", MAP_PUT, true);
            this.mv.visitInsn(POP);
            i += 1;
        }

        this.loadVar(waitAll.lhsOp.variableDcl);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleWaitMultiple", HANDLE_WAIT_MULTIPLE,
                                false);
    }

    private void genFPCallIns(BIRTerminator.FPCall fpCall, String moduleClassName, BType attachedType,
                              String funcName, int localVarOffset, int invocationVarIndex) {

        if (fpCall.isAsync) {
            // Check if already locked before submitting to scheduler.
            String lockStore = "L" + LOCK_STORE + ";";
            String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
            this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
            this.mv.visitLdcInsn(GLOBAL_LOCK_NAME);
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "panicIfInLock", PANIC_IF_UNLOCK, false);

            // Load the scheduler from strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", GET_SCHEDULER);
        } else {
            // load function ref, going to directly call the fp
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_POINTER, "getFunction", GET_FUNCTION, false);
        }

        boolean workerDerivative = fpCall.workerDerivative;
        int argCount = fpCall.args.size() + 1;
        if (workerDerivative) {
            argCount++;
        }

        // create an object array of args
        this.mv.visitIntInsn(BIPUSH, argCount);
        this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // load strand
        this.mv.visitInsn(DUP);
        // 0th index
        this.mv.visitIntInsn(BIPUSH, 0);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitInsn(AASTORE);

        int paramIndex = 1;
        if (workerDerivative) {
            this.mv.visitInsn(DUP);
            this.mv.visitIntInsn(BIPUSH, paramIndex++);
            this.mv.visitVarInsn(ILOAD, invocationVarIndex);
            this.mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, VALUE_OF_METHOD, INT_VALUE_OF_METHOD, false);
            this.mv.visitInsn(AASTORE);
        }

        // load args
        for (BIROperand arg : fpCall.args) {
            this.mv.visitInsn(DUP);
            this.mv.visitIntInsn(BIPUSH, paramIndex);
            this.loadVar(arg.variableDcl);
            BType bType = arg.variableDcl.type;
            jvmCastGen.addBoxInsn(this.mv, bType);
            this.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }

        // if async, we submit this to scheduler (worker scenario)

        if (fpCall.isAsync) {
            String workerName = fpCall.lhsOp.variableDcl.metaVarName;

            // load function ref now
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "isConcurrent", IS_CONCURRENT, false);
            Label notConcurrent = new Label();
            this.mv.visitJumpInsn(IFEQ, notConcurrent);
            Label concurrent = new Label();
            this.mv.visitLabel(concurrent);
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            loadFpReturnType(fpCall.lhsOp);
            this.loadVar(fpCall.fp.variableDcl);
            if (workerName == null) {
                this.mv.visitInsn(ACONST_NULL);
            } else {
                this.mv.visitLdcInsn(workerName);
            }
            this.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "getStrandName", ANNOTATION_GET_STRAND,
                                    false);
            this.submitToScheduler(fpCall.lhsOp, moduleClassName, attachedType, funcName, true, fpCall.transactional);
            Label afterSubmit = new Label();
            this.mv.visitJumpInsn(GOTO, afterSubmit);
            this.mv.visitLabel(notConcurrent);
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            loadFpReturnType(fpCall.lhsOp);
            this.loadVar(fpCall.fp.variableDcl);
            if (workerName == null) {
                this.mv.visitInsn(ACONST_NULL);
            } else {
                this.mv.visitLdcInsn(workerName);
            }
            this.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "getStrandName", ANNOTATION_GET_STRAND,
                                    false);
            this.submitToScheduler(fpCall.lhsOp, moduleClassName, attachedType, funcName, false, fpCall.transactional);
            this.mv.visitLabel(afterSubmit);
        } else {
            this.mv.visitMethodInsn(INVOKEINTERFACE, FUNCTION, "apply",
                    PASS_OBJECT_RETURN_OBJECT, true);
            // store result
            BType lhsType = fpCall.lhsOp.variableDcl.type;
            if (lhsType != null) {
                jvmCastGen.addUnboxInsn(this.mv, lhsType);
            }

            BIRNode.BIRVariableDcl lhsVar = fpCall.lhsOp.variableDcl;
            if (lhsVar != null) {
                this.storeToVar(lhsVar);
            } else {
                this.mv.visitInsn(POP);
            }
        }
    }

    private void genWorkerSendIns(BIRTerminator.WorkerSend ins, int localVarOffset, int invocationVarIndex) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "parent", GET_STRAND);
        }
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "wdChannels", GET_WD_CHANNELS);
        this.mv.visitVarInsn(ILOAD, invocationVarIndex);
        this.mv.visitInvokeDynamicInsn(MAKE_CONCAT_WITH_CONSTANTS, INT_TO_STRING,
                new Handle(H_INVOKESTATIC, STRING_CONCAT_FACTORY, MAKE_CONCAT_WITH_CONSTANTS,
                        HANDLE_DESCRIPTOR_FOR_STRING_CONCAT, false),
                ins.channel.value + START_OF_HEADING_WITH_SEMICOLON);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", GET_WORKER_DATA_CHANNEL, false);
        this.loadVar(ins.data.variableDcl);
        jvmCastGen.addBoxInsn(this.mv, ins.data.variableDcl.type);
        this.mv.visitVarInsn(ALOAD, localVarOffset);

        if (!ins.isSync) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "sendData", SEND_DATA, false);
        } else {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "syncSendData",
                                    SYNC_SEND_DATA, false);
            BIROperand lhsOp = ins.lhsOp;
            if (lhsOp != null) {
                this.storeToVar(lhsOp.variableDcl);
            }
        }
    }

    private void genWorkerReceiveIns(BIRTerminator.WorkerReceive ins, int localVarOffset, int invocationVarIndex) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "parent", GET_STRAND);
        }
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "wdChannels", GET_WD_CHANNELS);
        this.mv.visitVarInsn(ILOAD, invocationVarIndex);
        this.mv.visitInvokeDynamicInsn(MAKE_CONCAT_WITH_CONSTANTS, INT_TO_STRING,
                new Handle(H_INVOKESTATIC, STRING_CONCAT_FACTORY, MAKE_CONCAT_WITH_CONSTANTS,
                        HANDLE_DESCRIPTOR_FOR_STRING_CONCAT, false),
                ins.workerName.value + START_OF_HEADING_WITH_SEMICOLON);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", GET_WORKER_DATA_CHANNEL, false);

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "tryTakeData", TRY_TAKE_DATA, false);

        BIRNode.BIRVariableDcl tempVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("wrkMsg"),
                VarScope.FUNCTION, VarKind.ARG);
        int wrkResultIndex = this.getJVMIndexOfVarRef(tempVar);
        this.mv.visitVarInsn(ASTORE, wrkResultIndex);

        Label jumpAfterReceive = new Label();
        this.mv.visitVarInsn(ALOAD, wrkResultIndex);
        this.mv.visitJumpInsn(IFNULL, jumpAfterReceive);

        Label withinReceiveSuccess = new Label();
        this.mv.visitLabel(withinReceiveSuccess);
        this.mv.visitVarInsn(ALOAD, wrkResultIndex);
        jvmCastGen.addUnboxInsn(this.mv, ins.lhsOp.variableDcl.type);
        this.storeToVar(ins.lhsOp.variableDcl);

        this.mv.visitLabel(jumpAfterReceive);
    }

    private void genFlushIns(BIRTerminator.Flush ins, int localVarOffset, int invocationVarIndex) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        JvmCodeGenUtil.loadChannelDetails(this.mv, Arrays.asList(ins.channels), invocationVarIndex);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleFlush",
                                HANDLE_FLUSH, false);
        this.storeToVar(ins.lhsOp.variableDcl);
    }

    private void submitToScheduler(BIROperand lhsOp, String moduleClassName, BType attachedType,
                                   String parentFunction, boolean concurrent, boolean transactional) {

        String metaDataVarName;
        ScheduleFunctionInfo strandMetaData;
        if (attachedType != null) {
            metaDataVarName = getStrandMetadataVarName(attachedType.tsymbol.name.value,
                                                                         parentFunction);
            strandMetaData = new ScheduleFunctionInfo(attachedType.tsymbol.name.value, parentFunction);
        } else {
            metaDataVarName = JvmCodeGenUtil.getStrandMetadataVarName(parentFunction);
            strandMetaData = new ScheduleFunctionInfo(parentFunction);

        }
        asyncDataCollector.getStrandMetadata().putIfAbsent(metaDataVarName, strandMetaData);
        this.mv.visitFieldInsn(GETSTATIC, moduleClassName, metaDataVarName, GET_STRAND_METADATA);
        if (transactional && concurrent) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_TRANSACTIONAL_FUNCTION_METHOD,
                    SCHEDULE_TRANSACTIONAL_FUNCTION, false);
        } else if (transactional) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_TRANSACTIONAL_LOCAL_METHOD,
                    SCHEDULE_TRANSACTIONAL_FUNCTION, false);
        } else if (concurrent) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
                               SCHEDULE_TRANSACTIONAL_FUNCTION, false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_LOCAL_METHOD,
                    SCHEDULE_LOCAL, false);
        }
        // store return
        if (lhsOp.variableDcl != null) {
            BIRNode.BIRVariableDcl lhsOpVarDcl = lhsOp.variableDcl;
            // store the returned strand as the future
            this.storeToVar(lhsOpVarDcl);
        }
    }

    static String getStrandMetadataVarName(String typeName, String parentFunction) {
        return STRAND_METADATA_VAR_PREFIX + typeName + "$" + parentFunction +
                "$";
    }

    private void loadFpReturnType(BIROperand lhsOp) {

        BType futureType = lhsOp.variableDcl.type;
        BType returnType = symbolTable.anyType;
        if (futureType.tag == TypeTags.FUTURE) {
            returnType = ((BFutureType) futureType).constraint;
        }
        // load strand
        jvmTypeGen.loadType(this.mv, returnType);
    }

    private int getJVMIndexOfVarRef(BIRNode.BIRVariableDcl varDcl) {
        return this.indexMap.addIfNotExists(varDcl.name.value, varDcl.type);
    }

    private void loadVar(BIRNode.BIRVariableDcl varDcl) {

        jvmInstructionGen.generateVarLoad(this.mv, varDcl, this.getJVMIndexOfVarRef(varDcl));
    }

    private void storeToVar(BIRNode.BIRVariableDcl varDcl) {

        jvmInstructionGen.generateVarStore(this.mv, varDcl, this.getJVMIndexOfVarRef(varDcl));
    }

    public void genReturnTerm(int returnVarRefIndex, BIRNode.BIRFunction func, int invocationVarIndex) {
        BType bType = unifier.build(func.type.retType);
        generateReturnTermFromType(returnVarRefIndex, bType, func, invocationVarIndex);
    }

    private void generateReturnTermFromType(int returnVarRefIndex, BType bType, BIRNode.BIRFunction func,
                                            int invocationVarIndex) {
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitVarInsn(LLOAD, returnVarRefIndex);
            this.mv.visitInsn(LRETURN);
            return;
        } else if (TypeTags.isStringTypeTag(bType.tag) || TypeTags.isXMLTypeTag(bType.tag)
                || TypeTags.REGEXP == bType.tag) {
            this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            this.mv.visitInsn(ARETURN);
            return;
        }

        switch (bType.tag) {
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.MAP:
            case TypeTags.ARRAY:
            case TypeTags.ANY:
            case TypeTags.INTERSECTION:
            case TypeTags.STREAM:
            case TypeTags.TABLE:
            case TypeTags.ANYDATA:
            case TypeTags.OBJECT:
            case TypeTags.DECIMAL:
            case TypeTags.RECORD:
            case TypeTags.TUPLE:
            case TypeTags.JSON:
            case TypeTags.FUTURE:
            case TypeTags.INVOKABLE:
            case TypeTags.HANDLE:
            case TypeTags.FINITE:
            case TypeTags.TYPEDESC:
            case TypeTags.READONLY:
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
                break;
            case TypeTags.BYTE:
            case TypeTags.BOOLEAN:
                this.mv.visitVarInsn(ILOAD, returnVarRefIndex);
                this.mv.visitInsn(IRETURN);
                break;
            case TypeTags.FLOAT:
                this.mv.visitVarInsn(DLOAD, returnVarRefIndex);
                this.mv.visitInsn(DRETURN);
                break;
            case TypeTags.UNION:
                this.handleErrorRetInUnion(returnVarRefIndex, Arrays.asList(func.workerChannels),
                        (BUnionType) bType, invocationVarIndex);
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
                break;
            case TypeTags.ERROR:
                this.notifyChannels(Arrays.asList(func.workerChannels), returnVarRefIndex, invocationVarIndex);
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
                break;
            case TypeTags.TYPEREFDESC:
                generateReturnTermFromType(returnVarRefIndex, JvmCodeGenUtil.getReferredType(bType), func,
                        invocationVarIndex);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        func.type.retType);
        }
    }

    public LabelGenerator getLabelGenerator() {
        return this.labelGen;
    }
}
