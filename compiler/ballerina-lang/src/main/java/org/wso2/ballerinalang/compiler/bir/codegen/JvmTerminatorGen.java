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

import io.ballerina.runtime.IdentifierUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
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
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFNE;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ENV;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ERROR_REASONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_EXTENSION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_EXCEPTION_HELPER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLOCKED_ON_EXTERN_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BUILT_IN_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DEFAULT_STRAND_DISPATCHER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_LOCK_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.IS_BLOCKED_ON_EXTERN_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REF_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_FUNCTION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_LOCAL_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_POLICY_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_VALUE_ANY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WD_CHANNELS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_DATA_CHANNEL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addJUnboxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
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
    private JvmPackageGen jvmPackageGen;
    private JvmInstructionGen jvmInstructionGen;
    private PackageCache packageCache;
    private SymbolTable symbolTable;
    private ResolvedTypeBuilder typeBuilder;

    public JvmTerminatorGen(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, LabelGenerator labelGen,
                            JvmErrorGen errorGen, BIRNode.BIRPackage module, JvmInstructionGen jvmInstructionGen,
                            JvmPackageGen jvmPackageGen) {

        this.mv = mv;
        this.indexMap = indexMap;
        this.labelGen = labelGen;
        this.errorGen = errorGen;
        this.jvmPackageGen = jvmPackageGen;
        this.packageCache = jvmPackageGen.packageCache;
        this.jvmInstructionGen = jvmInstructionGen;
        this.symbolTable = jvmPackageGen.symbolTable;
        this.currentPackageName = JvmCodeGenUtil.getPackageName(module);
        this.typeBuilder = new ResolvedTypeBuilder();
    }

    private static void genYieldCheckForLock(MethodVisitor mv, LabelGenerator labelGen, String funcName,
                                             int localVarOffset) {

        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "isYielded", "()Z", false);
        Label yieldLabel = labelGen.getLabel(funcName + "yield");
        mv.visitJumpInsn(IFNE, yieldLabel);
    }

    private void loadDefaultValue(MethodVisitor mv, BType bType) {
        if (TypeTags.isIntegerTypeTag(bType.tag) || bType.tag == TypeTags.BYTE) {
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
                mv.visitInsn(ACONST_NULL);
                break;
            case JTypeTags.JTYPE:
                loadDefaultJValue(mv, (JType) bType);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         String.format("%s", bType));
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
                                                         String.format("%s", jType));
        }
    }

    public void genTerminator(BIRTerminator terminator, String moduleClassName, BIRNode.BIRFunction func,
                       String funcName, int localVarOffset, int returnVarRefIndex,
                       BType attachedType, AsyncDataCollector asyncDataCollector) {

        switch (terminator.kind) {
            case LOCK:
                this.genLockTerm((BIRTerminator.Lock) terminator, funcName, localVarOffset);
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
                this.genAsyncCallTerm((BIRTerminator.AsyncCall) terminator, localVarOffset, moduleClassName,
                                      attachedType, funcName, asyncDataCollector);
                return;
            case BRANCH:
                this.genBranchTerm((BIRTerminator.Branch) terminator, funcName);
                return;
            case RETURN:
                this.genReturnTerm(returnVarRefIndex, func);
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
                this.genFPCallIns((BIRTerminator.FPCall) terminator, moduleClassName, attachedType, funcName,
                                  asyncDataCollector, localVarOffset);
                return;
            case WK_SEND:
                this.genWorkerSendIns((BIRTerminator.WorkerSend) terminator, localVarOffset);
                return;
            case WK_RECEIVE:
                this.genWorkerReceiveIns((BIRTerminator.WorkerReceive) terminator, localVarOffset);
                return;
            case FLUSH:
                this.genFlushIns((BIRTerminator.Flush) terminator, localVarOffset);
                return;
            case PLATFORM:
                if (terminator instanceof JavaMethodCall) {
                    this.genJCallTerm((JavaMethodCall) terminator, attachedType, localVarOffset);
                    return;
                } else if (terminator instanceof JIMethodCall) {
                    this.genJICallTerm((JIMethodCall) terminator, localVarOffset);
                    return;
                } else if (terminator instanceof JIConstructorCall) {
                    this.genJIConstructorTerm((JIConstructorCall) terminator,
                            localVarOffset);
                    return;
                }
        }
        throw new BLangCompilerException("JVM generation is not supported for terminator instruction " +
                String.format("%s", terminator));

    }

    private void genGoToTerm(BIRTerminator.GOTO gotoIns, String funcName) {

        Label gotoLabel = this.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void genLockTerm(BIRTerminator.Lock lockIns, String funcName, int localVarOffset) {

        Label gotoLabel = this.labelGen.getLabel(funcName + lockIns.lockedBB.id.value);
        String lockStore = "L" + LOCK_STORE + ";";
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        String lockName = GLOBAL_LOCK_NAME + lockIns.lockId;
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
        this.mv.visitLdcInsn(lockName);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap",
                String.format("(L%s;)L%s;", STRING_VALUE, LOCK_VALUE), false);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "lock", String.format("(L%s;)Z", STRAND_CLASS), false);
        this.mv.visitInsn(POP);
        genYieldCheckForLock(this.mv, this.labelGen, funcName, localVarOffset);
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
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", String.format("(L%s;)L%s;",
                STRING_VALUE, LOCK_VALUE), false);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "unlock", "()V", false);

        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void handleErrorRetInUnion(int returnVarRefIndex, List<BIRNode.ChannelDetails> channels, BUnionType bType) {

        if (channels.size() == 0) {
            return;
        }

        boolean errorIncluded = false;
        for (BType member : bType.getMemberTypes()) {
            if (member.tag == TypeTags.ERROR) {
                errorIncluded = true;
                break;
            }
        }

        if (errorIncluded) {
            this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            this.mv.visitVarInsn(ALOAD, 0);
            JvmCodeGenUtil.loadChannelDetails(this.mv, channels);
            this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "handleWorkerError",
                                    String.format("(L%s;L%s;[L%s;)V", REF_VALUE, STRAND_CLASS, CHANNEL_DETAILS), false);
        }
    }

    private void notifyChannels(List<BIRNode.ChannelDetails> channels, int retIndex) {

        if (channels.size() == 0) {
            return;
        }

        this.mv.visitVarInsn(ALOAD, 0);
        JvmCodeGenUtil.loadChannelDetails(this.mv, channels);
        this.mv.visitVarInsn(ALOAD, retIndex);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleChannelError", String.format("([L%s;L%s;)V",
                                                                                                 CHANNEL_DETAILS,
                                                                                                 ERROR_VALUE), false);
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

        PackageID calleePkgId = callIns.calleePkg;

        String orgName = calleePkgId.orgName.value;
        String moduleName = calleePkgId.name.value;
        String version = calleePkgId.version.value;

        // invoke the function
        this.genCall(callIns, orgName, moduleName, version, localVarOffset);

        // store return
        this.storeReturnFromCallIns(callIns.lhsOp != null ? callIns.lhsOp.variableDcl : null);
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
            JvmCastGen.addUnboxInsn(this.mv, callIns.lhsOp.variableDcl.type); // store return
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
            this.visitArg(arg);
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
            this.mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD, String.format("()L%s;", OBJECT),
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
                    String.format("L%s;", STRING_VALUE));
            this.mv.visitFieldInsn(GETSTATIC, RUNTIME_ERRORS, "JAVA_NULL_REFERENCE",
                    String.format("L%s;", RUNTIME_ERRORS));
            this.mv.visitInsn(ICONST_0);
            this.mv.visitTypeInsn(ANEWARRAY, OBJECT);
            this.mv.visitMethodInsn(INVOKESTATIC, BLANG_EXCEPTION_HELPER, "getRuntimeException",
                    String.format("(L%s;L%s;[L%s;)L%s;", STRING_VALUE, RUNTIME_ERRORS, OBJECT, ERROR_VALUE), false);
            this.mv.visitInsn(ATHROW);
            this.mv.visitLabel(elseBlockLabel);
            argIndex += 1;
        }

        String jMethodVMSig = callIns.jMethodVMSig;
        boolean hasBalEnvParam = jMethodVMSig.startsWith(String.format("(L%s;", BAL_ENV));

        if (hasBalEnvParam) {
            mv.visitTypeInsn(NEW, BAL_ENV);
            mv.visitInsn(DUP);
            this.mv.visitVarInsn(ALOAD, localVarOffset); // load the strand
            mv.visitMethodInsn(INVOKESPECIAL, BAL_ENV, JVM_INIT_METHOD, String.format("(L%s;)V", STRAND_CLASS),
                               false);
        }

        int argsCount = callIns.varArgExist ? callIns.args.size() - 1 : callIns.args.size();
        while (argIndex < argsCount) {
            BIROperand arg = callIns.args.get(argIndex);
            this.visitArg(arg);
            argIndex += 1;
        }
        if (callIns.varArgExist) {
            BIROperand arg = callIns.args.get(argIndex);
            int localVarIndex = this.indexMap.addToMapIfNotFoundAndGetIndex(arg.variableDcl);
            genVarArg(this.mv, this.indexMap, arg.variableDcl.type, callIns.varArgType, localVarIndex, symbolTable);
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
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue", String.format("L%s;", OBJECT));
            JvmCastGen.addUnboxInsn(this.mv, callIns.lhsOp.variableDcl.type);
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
            this.visitArg(arg);
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
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, PANIC_FIELD, String.format("L%s;", BERROR));
        Label panicLabel = new Label();
        mv.visitJumpInsn(IFNULL, panicLabel);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, PANIC_FIELD, String.format("L%s;", BERROR));
        mv.visitInsn(DUP);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, PANIC_FIELD, String.format("L%s;", BERROR));
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


    private void genCall(BIRTerminator.Call callIns, String orgName, String moduleName,
                             String version, int localVarOffset) {

        if (!callIns.isVirtual) {
            this.genFuncCall(callIns, orgName, moduleName, version, localVarOffset);
            return;
        }

        BIRNode.BIRVariableDcl selfArg = callIns.args.get(0).variableDcl;
        if (selfArg.type.tag == TypeTags.OBJECT) {
            this.genVirtualCall(callIns, orgName, moduleName, localVarOffset);
        } else {
            // then this is a function attached to a built-in type
            this.genBuiltinTypeAttachedFuncCall(callIns, orgName, moduleName, version, localVarOffset);
        }
    }

    private void genFuncCall(BIRTerminator.Call callIns, String orgName, String moduleName, String version,
                             int localVarOffset) {

        String methodName = IdentifierUtils.encodeIdentifier(callIns.name.value);
        this.genStaticCall(callIns, orgName, moduleName, version, localVarOffset, methodName, methodName);
    }

    private void genBuiltinTypeAttachedFuncCall(BIRTerminator.Call callIns, String orgName,
                                                String moduleName,  String version, int localVarOffset) {

        String methodLookupName = callIns.name.value;
        int optionalIndex = methodLookupName.indexOf(".");
        int index = optionalIndex != -1 ? optionalIndex + 1 : 0;
        String methodName = methodLookupName.substring(index);
        this.genStaticCall(callIns, orgName, moduleName, version, localVarOffset, methodName, methodLookupName);
    }

    private void genStaticCall(BIRTerminator.Call callIns, String orgName, String moduleName,
                               String version, int localVarOffset,
                               String methodName, String methodLookupName) {
        // load strand
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        String lookupKey = JvmCodeGenUtil.getPackageName(orgName, moduleName, version) + methodLookupName;

        int argsCount = callIns.args.size();
        int i = 0;
        while (i < argsCount) {
            BIROperand arg = callIns.args.get(i);
            boolean userProvidedArg = this.visitArg(arg);
            this.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            i += 1;
        }
        String cleanMethodName = JvmCodeGenUtil.cleanupFunctionName(methodName);
        BIRFunctionWrapper functionWrapper = jvmPackageGen.lookupBIRFunctionWrapper(lookupKey);
        String methodDesc;
        String jvmClass;
        if (functionWrapper != null) {
            jvmClass = functionWrapper.fullQualifiedClassName;
            methodDesc = functionWrapper.jvmMethodDescription;
        } else {
            BPackageSymbol symbol = packageCache.getSymbol(orgName + "/" + moduleName);
            BInvokableSymbol funcSymbol = (BInvokableSymbol) symbol.scope.lookup(new Name(methodName)).symbol;
            BInvokableType type = (BInvokableType) funcSymbol.type;
            ArrayList<BType> params = new ArrayList<>(type.paramTypes);
            if (type.restType != null) {
                params.add(type.restType);
            }
            for (int j = params.size() - 1; j >= 0; j--) {
                params.add(j + 1, symbolTable.booleanType);
            }
            String balFileName = funcSymbol.source;


            if (balFileName == null || !balFileName.endsWith(BAL_EXTENSION)) {
                balFileName = MODULE_INIT_CLASS_NAME;
            }

            jvmClass = JvmCodeGenUtil.getModuleLevelClassName(orgName, moduleName, version,
                                                              JvmCodeGenUtil.cleanupPathSeparators(balFileName));
            //TODO: add receiver:  BType attachedType = type.r != null ? receiver.type : null;
            BType retType = typeBuilder.build(type.retType);
            methodDesc = JvmCodeGenUtil.getMethodDesc(params, retType);
        }
        this.mv.visitMethodInsn(INVOKESTATIC, jvmClass, cleanMethodName, methodDesc, false);
    }

    private void genVirtualCall(BIRTerminator.Call callIns, String orgName, String moduleName, int localVarOffset) {
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
        // arg count doubled and 'isExist' boolean variables added for each arg.
        this.mv.visitLdcInsn((long) (argsCount * 2));
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
            boolean userProvidedArg = this.visitArg(arg);

            // Add the to the rest params array
            JvmCastGen.addBoxInsn(this.mv, arg.variableDcl.type);
            this.mv.visitInsn(AASTORE);

            this.mv.visitInsn(DUP);
            this.mv.visitLdcInsn((long) j);
            this.mv.visitInsn(L2I);
            j += 1;

            this.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            JvmCastGen.addBoxInsn(this.mv, symbolTable.booleanType);
            this.mv.visitInsn(AASTORE);

            i += 1;
        }

        // call method
        String methodDesc = String.format("(L%s;L%s;[L%s;)L%s;", STRAND_CLASS, STRING_VALUE, OBJECT, OBJECT);
        this.mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "call", methodDesc, true);

        BType returnType = callIns.lhsOp.variableDcl.type;
        JvmCastGen.addUnboxInsn(this.mv, returnType);
    }

    private void loadBooleanArgToIndicateUserProvidedArg(String orgName, String moduleName, boolean userProvided) {

        if (JvmCodeGenUtil.isBallerinaBuiltinModule(orgName, moduleName)) {
            return;
        }
        // Extra boolean is not gen for extern functions for now until the wrapper function is implemented.
        // We need to refactor this method. I am not sure whether userProvided flag make sense
        if (userProvided) {
            this.mv.visitInsn(ICONST_1);
        } else {
            this.mv.visitInsn(ICONST_0);
        }
    }

    private boolean visitArg(BIROperand arg) {
        BIRNode.BIRVariableDcl varDcl = arg.variableDcl;
        if (varDcl.name.value.startsWith("_")) {
            loadDefaultValue(this.mv, varDcl.type);
            return false;
        }

        this.loadVar(varDcl);
        return true;
    }

    private void genAsyncCallTerm(BIRTerminator.AsyncCall callIns, int localVarOffset, String moduleClassName,
                                  BType attachedType, String parentFunction, AsyncDataCollector asyncDataCollector) {

        PackageID calleePkgId = callIns.calleePkg;

        String orgName = calleePkgId.orgName.value;
        String moduleName = calleePkgId.name.value;

        // Check if already locked before submitting to scheduler.
        String lockStore = "L" + LOCK_STORE + ";";
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
        this.mv.visitLdcInsn(GLOBAL_LOCK_NAME);
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "panicIfInLock",
                                String.format("(L%s;L%s;)V", STRING_VALUE, STRAND_CLASS), false);

        // Load the scheduler from strand
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", String.format("L%s;", SCHEDULER));

        // create an Object[] for the rest params
        int argsCount = callIns.args.size();
        //create an object array of args
        this.mv.visitLdcInsn((long) (argsCount * 2 + 1));
        this.mv.visitInsn(L2I);
        this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int paramIndex = 1;
        for (BIROperand arg : callIns.args) {
            this.mv.visitInsn(DUP);
            this.mv.visitLdcInsn((long) paramIndex);
            this.mv.visitInsn(L2I);

            boolean userProvidedArg = this.visitArg(arg);
            // Add the to the rest params array
            JvmCastGen.addBoxInsn(this.mv, arg.variableDcl.type);
            this.mv.visitInsn(AASTORE);
            paramIndex += 1;

            this.mv.visitInsn(DUP);
            this.mv.visitLdcInsn((long) paramIndex);
            this.mv.visitInsn(L2I);

            this.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            JvmCastGen.addBoxInsn(this.mv, symbolTable.booleanType);
            this.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }
        String funcName = IdentifierUtils.encodeIdentifier(callIns.name.value);
        String lambdaName = "$" + funcName + "$lambda$" + asyncDataCollector.getLambdaIndex() + "$";

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
                        !BALLERINA.equals(annotationAttachment.packageID.orgName.value) ||
                        !BUILT_IN_PACKAGE_NAME.equals(annotationAttachment.packageID.name.value)) {
                    continue;
                }

                if (annotationAttachment.annotValues.size() == 0) {
                    break;
                }

                BIRNode.BIRAnnotationValue strandAnnot = annotationAttachment.annotValues.get(0);
                if (strandAnnot instanceof BIRNode.BIRAnnotationRecordValue) {
                    BIRNode.BIRAnnotationRecordValue recordValue = (BIRNode.BIRAnnotationRecordValue) strandAnnot;
                    if (recordValue.annotValueEntryMap.containsKey(STRAND_THREAD)) {
                        BIRNode.BIRAnnotationValue mapVal = recordValue.annotValueEntryMap.get(STRAND_THREAD);
                        if (mapVal instanceof BIRNode.BIRAnnotationLiteralValue &&
                                STRAND_VALUE_ANY.equals(((BIRNode.BIRAnnotationLiteralValue) mapVal).value)) {
                            concurrent = true;
                        }
                    }

                    if (recordValue.annotValueEntryMap.containsKey(STRAND_NAME)) {
                        strandName = ((BIRNode.BIRAnnotationLiteralValue) recordValue.
                                annotValueEntryMap.get(STRAND_NAME)).value.toString();

                    }

                    if (recordValue.annotValueEntryMap.containsKey(STRAND_POLICY_NAME)) {
                        BIRNode.BIRAnnotationValue mapVal = recordValue.annotValueEntryMap.get(STRAND_POLICY_NAME);
                        if (mapVal instanceof BIRNode.BIRAnnotationLiteralValue &&
                                !DEFAULT_STRAND_DISPATCHER.equals(((BIRNode.BIRAnnotationLiteralValue) mapVal).value)) {
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

        this.submitToScheduler(callIns.lhsOp, moduleClassName, attachedType, parentFunction, asyncDataCollector,
                               concurrent);
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
            this.mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", String.format("(L%s;)Z", OBJECT), true);
            this.mv.visitInsn(POP);
            i += 1;
        }

        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleWaitAny",
                                String.format("(L%s;)L%s$WaitResult;", LIST, STRAND_CLASS), false);
        BIRNode.BIRVariableDcl tempVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("waitResult"),
                                                                    VarScope.FUNCTION, VarKind.ARG);
        int resultIndex = this.getJVMIndexOfVarRef(tempVar);
        this.mv.visitVarInsn(ASTORE, resultIndex);

        // assign result if result available
        Label afterIf = new Label();
        this.mv.visitVarInsn(ALOAD, resultIndex);
        this.mv.visitFieldInsn(GETFIELD, String.format("%s$WaitResult", STRAND_CLASS), "done", "Z");
        this.mv.visitJumpInsn(IFEQ, afterIf);
        Label withinIf = new Label();
        this.mv.visitLabel(withinIf);
        this.mv.visitVarInsn(ALOAD, resultIndex);
        this.mv.visitFieldInsn(GETFIELD, String.format("%s$WaitResult", STRAND_CLASS), "result",
                               String.format("L%s;", OBJECT));
        JvmCastGen.addUnboxInsn(this.mv, waitInst.lhsOp.variableDcl.type);
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
            this.mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", String.format("(L%s;L%s;)L%s;",
                                                                               OBJECT, OBJECT, OBJECT), true);
            this.mv.visitInsn(POP);
            i += 1;
        }

        this.loadVar(waitAll.lhsOp.variableDcl);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleWaitMultiple", String.format("(L%s;L%s;)V",
                                                                                                 MAP, MAP_VALUE),
                                false);
    }

    private void genFPCallIns(BIRTerminator.FPCall fpCall, String moduleClassName, BType attachedType, String funcName,
                              AsyncDataCollector asyncDataCollector, int localVarOffset) {

        if (fpCall.isAsync) {
            // Check if already locked before submitting to scheduler.
            String lockStore = "L" + LOCK_STORE + ";";
            String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
            this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, lockStore);
            this.mv.visitLdcInsn(GLOBAL_LOCK_NAME);
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "panicIfInLock",
                                    String.format("(L%s;L%s;)V", STRING_VALUE, STRAND_CLASS), false);

            // Load the scheduler from strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", String.format("L%s;", SCHEDULER));
        } else {
            // load function ref, going to directly call the fp
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_POINTER, "getFunction",
                    String.format("()L%s;", FUNCTION), false);
        }

        // create an object array of args
        this.mv.visitIntInsn(BIPUSH, fpCall.args.size() * 2 + 1);
        this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // load strand
        this.mv.visitInsn(DUP);

        // 0th index
        this.mv.visitIntInsn(BIPUSH, 0);

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitInsn(AASTORE);

        // load args
        int paramIndex = 1;
        for (BIROperand arg : fpCall.args) {
            this.mv.visitInsn(DUP);
            this.mv.visitIntInsn(BIPUSH, paramIndex);
            this.loadVar(arg.variableDcl);
            BType bType = arg.variableDcl.type;
            JvmCastGen.addBoxInsn(this.mv, bType);
            this.mv.visitInsn(AASTORE);
            paramIndex += 1;

            this.loadTrueValueAsArg(paramIndex);
            paramIndex += 1;
        }

        // if async, we submit this to sceduler (worker scenario)

        if (fpCall.isAsync) {
            String workerName = fpCall.lhsOp.variableDcl.metaVarName;

            // load function ref now
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "isConcurrent", String.format("(L%s;)Z",
                    FUNCTION_POINTER), false);
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
            this.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "getStrandName",
                                    String.format("(L%s;L%s;)L%s;", FUNCTION_POINTER, STRING_VALUE, STRING_VALUE),
                                    false);
            this.submitToScheduler(fpCall.lhsOp, moduleClassName, attachedType, funcName, asyncDataCollector, true);
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
            this.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "getStrandName",
                                    String.format("(L%s;L%s;)L%s;", FUNCTION_POINTER, STRING_VALUE, STRING_VALUE),
                                    false);
            this.submitToScheduler(fpCall.lhsOp, moduleClassName, attachedType, funcName, asyncDataCollector, false);
            this.mv.visitLabel(afterSubmit);
        } else {
            this.mv.visitMethodInsn(INVOKEINTERFACE, FUNCTION, "apply",
                    String.format("(L%s;)L%s;", OBJECT, OBJECT), true);
            // store result
            BType lhsType = fpCall.lhsOp.variableDcl.type;
            if (lhsType != null) {
                JvmCastGen.addUnboxInsn(this.mv, lhsType);
            }

            BIRNode.BIRVariableDcl lhsVar = fpCall.lhsOp.variableDcl;
            if (lhsVar != null) {
                this.storeToVar(lhsVar);
            } else {
                this.mv.visitInsn(POP);
            }
        }
    }

    private void loadTrueValueAsArg(int paramIndex) {

        this.mv.visitInsn(DUP);
        this.mv.visitIntInsn(BIPUSH, paramIndex);
        this.mv.visitInsn(ICONST_1);
        JvmCastGen.addBoxInsn(this.mv, symbolTable.booleanType);
        this.mv.visitInsn(AASTORE);
    }

    private void genWorkerSendIns(BIRTerminator.WorkerSend ins, int localVarOffset) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "parent", String.format("L%s;", STRAND_CLASS));
        }
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "wdChannels", String.format("L%s;", WD_CHANNELS));
        this.mv.visitLdcInsn(ins.channel.value);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", String.format("(L%s;)L%s;",
                STRING_VALUE, WORKER_DATA_CHANNEL), false);
        this.loadVar(ins.data.variableDcl);
        JvmCastGen.addBoxInsn(this.mv, ins.data.variableDcl.type);
        this.mv.visitVarInsn(ALOAD, localVarOffset);

        if (!ins.isSync) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "sendData",
                                    String.format("(L%s;L%s;)V", OBJECT, STRAND_CLASS), false);
        } else {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "syncSendData",
                                    String.format("(L%s;L%s;)L%s;", OBJECT, STRAND_CLASS, OBJECT), false);
            BIROperand lhsOp = ins.lhsOp;
            if (lhsOp != null) {
                this.storeToVar(lhsOp.variableDcl);
            }
        }
    }

    private void genWorkerReceiveIns(BIRTerminator.WorkerReceive ins, int localVarOffset) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "parent", String.format("L%s;", STRAND_CLASS));
        }
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "wdChannels", String.format("L%s;", WD_CHANNELS));
        this.mv.visitLdcInsn(ins.workerName.value);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", String.format("(L%s;)L%s;",
                STRING_VALUE, WORKER_DATA_CHANNEL), false);

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "tryTakeData", String.format("(L%s;)L%s;",
                                                                                                 STRAND_CLASS, OBJECT),
                                false);

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
        JvmCastGen.addUnboxInsn(this.mv, ins.lhsOp.variableDcl.type);
        this.storeToVar(ins.lhsOp.variableDcl);

        this.mv.visitLabel(jumpAfterReceive);
    }

    private void genFlushIns(BIRTerminator.Flush ins, int localVarOffset) {

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        JvmCodeGenUtil.loadChannelDetails(this.mv, Arrays.asList(ins.channels));
        this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "handleFlush",
                                String.format("([L%s;)L%s;", CHANNEL_DETAILS, ERROR_VALUE), false);
        this.storeToVar(ins.lhsOp.variableDcl);
    }

    private void submitToScheduler(BIROperand lhsOp, String moduleClassName, BType attachedType, String parentFunction,
                                   AsyncDataCollector asyncDataCollector, boolean concurrent) {

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
        this.mv.visitFieldInsn(GETSTATIC, moduleClassName, metaDataVarName, String.format("L%s;", STRAND_METADATA));
        if (concurrent) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
                               String.format("([L%s;L%s;L%s;L%s;L%s;L%s;)L%s;", OBJECT, B_FUNCTION_POINTER,
                                             STRAND_CLASS, TYPE, STRING_VALUE, STRAND_METADATA, FUTURE_VALUE), false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_LOCAL_METHOD,
                               String.format("([L%s;L%s;L%s;L%s;L%s;L%s;)L%s;", OBJECT, B_FUNCTION_POINTER,
                                             STRAND_CLASS, TYPE, STRING_VALUE, STRAND_METADATA, FUTURE_VALUE), false);
        }
        // store return
        if (lhsOp.variableDcl != null) {
            BIRNode.BIRVariableDcl lhsOpVarDcl = lhsOp.variableDcl;
            // store the returned strand as the future
            this.storeToVar(lhsOpVarDcl);
        }
    }

    static String getStrandMetadataVarName(String typeName, String parentFunction) {
        return STRAND_METADATA_VAR_PREFIX + JvmCodeGenUtil.cleanupReadOnlyTypeName(typeName) + "$" + parentFunction +
                "$";
    }

    private void loadFpReturnType(BIROperand lhsOp) {

        BType futureType = lhsOp.variableDcl.type;
        BType returnType = symbolTable.anyType;
        if (futureType.tag == TypeTags.FUTURE) {
            returnType = ((BFutureType) futureType).constraint;
        }
        // load strand
        loadType(this.mv, returnType);
    }

    private int getJVMIndexOfVarRef(BIRNode.BIRVariableDcl varDcl) {

        return this.indexMap.addToMapIfNotFoundAndGetIndex(varDcl);
    }

    private void loadVar(BIRNode.BIRVariableDcl varDcl) {

        jvmInstructionGen.generateVarLoad(this.mv, varDcl, this.getJVMIndexOfVarRef(varDcl));
    }

    private void storeToVar(BIRNode.BIRVariableDcl varDcl) {

        jvmInstructionGen.generateVarStore(this.mv, varDcl, this.getJVMIndexOfVarRef(varDcl));
    }

    public void genReturnTerm(int returnVarRefIndex, BIRNode.BIRFunction func) {

        BType bType = typeBuilder.build(func.type.retType);

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitVarInsn(LLOAD, returnVarRefIndex);
            this.mv.visitInsn(LRETURN);
            return;
        } else if (TypeTags.isStringTypeTag(bType.tag) || TypeTags.isXMLTypeTag(bType.tag)) {
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
                        (BUnionType) bType);
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
                break;
            case TypeTags.ERROR:
                this.notifyChannels(Arrays.asList(func.workerChannels), returnVarRefIndex);
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        String.format("%s", func.type.retType));
        }
    }

    public LabelGenerator getLabelGenerator() {
        return this.labelGen;
    }
}
