/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DUP;
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
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ERROR_REASONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_EXCEPTION_HELPER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REF_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WD_CHANNELS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_DATA_CHANNEL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen.ErrorHandlerGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.IS_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addBoxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addJUnboxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addUnboxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarLoad;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarStore;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmLabelGen.LabelGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupFunctionName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.createFunctionPointer;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.isBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.loadDefaultValue;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nameOfBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nameOfNonBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmObservabilityGen.emitStopObservationInvocation;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.BIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.birFunctionMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.computeLockNameFromString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.currentClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getBIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lambdaIndex;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lambdas;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupFullQualifiedClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupGlobalVarClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupJavaMethodDescription;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.JavaMethodCall;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.isBallerinaBuiltinModule;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JIConstructorCall;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JIMethodCall;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.genVarArg;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.ASYNC_CALL;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.CALL;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.FP_LOAD;

/**
 * BIR termination instructions to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmTerminatorGen {

    private static void genYieldCheckForLock(MethodVisitor mv, LabelGenerator labelGen, String funcName,
                                             int localVarOffset) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isYielded", "()Z", false);
        Label yieldLabel = labelGen.getLabel(funcName + "yield");
        mv.visitJumpInsn(IFNE, yieldLabel);
    }

    static void loadChannelDetails(MethodVisitor mv, List<BIRNode.ChannelDetails> channels) {

        mv.visitIntInsn(BIPUSH, channels.size());
        mv.visitTypeInsn(ANEWARRAY, CHANNEL_DETAILS);
        int index = 0;
        for (BIRNode.ChannelDetails ch : channels) {
            // generating array[i] = new ChannelDetails(name, onSameStrand, isSend);
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, index);
            index += 1;

            mv.visitTypeInsn(NEW, CHANNEL_DETAILS);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(ch.name);

            if (ch.channelInSameStrand) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }

            if (ch.send) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }

            mv.visitMethodInsn(INVOKESPECIAL, CHANNEL_DETAILS, "<init>", String.format("(L%s;ZZ)V", STRING_VALUE),
                    false);
            mv.visitInsn(AASTORE);
        }
    }

    static String cleanupObjectTypeName(String typeName) {

        int index = typeName.lastIndexOf(".");
        if (index > 0) {
            return typeName.substring(index + 1);
        } else {
            return typeName;
        }
    }

    static boolean isExternStaticFunctionCall(BIRInstruction callIns) {

        String methodName;
        String orgName;
        String moduleName;

        InstructionKind kind = callIns.getKind();

        PackageID packageID;

        if (kind == CALL) {
            BIRTerminator.Call call = (BIRTerminator.Call) callIns;
            if (call.isVirtual) {
                return false;
            }
            methodName = call.name.value;
            packageID = call.calleePkg;
        } else if (kind == ASYNC_CALL) {
            BIRTerminator.AsyncCall asyncCall = (BIRTerminator.AsyncCall) callIns;
            methodName = asyncCall.name.value;
            packageID = asyncCall.calleePkg;
        } else if (kind == FP_LOAD) {
            BIRNonTerminator.FPLoad fpLoad = (BIRNonTerminator.FPLoad) callIns;
            methodName = fpLoad.funcName.value;
            packageID = fpLoad.pkgId;
        } else {
            throw new BLangCompilerException("JVM static function call generation is not supported for instruction " +
                    String.format("%s", callIns));
        }

        orgName = packageID.orgName.value;
        moduleName = packageID.name.value;

        String key = getPackageName(orgName, moduleName) + methodName;

        if (birFunctionMap.containsKey(key)) {
            BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap.get(key));
            return isExternFunc(functionWrapper.func);
        }

        return false;
    }

    public static class TerminatorGenerator {

        MethodVisitor mv;
        BalToJVMIndexMap indexMap;
        LabelGenerator labelGen;
        ErrorHandlerGenerator errorGen;
        BIRPackage module;
        String currentPackageName;
        int kl = 0;

        public TerminatorGenerator(MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen,
                                   ErrorHandlerGenerator errorGen, BIRPackage module) {

            this.mv = mv;
            this.indexMap = indexMap;
            this.labelGen = labelGen;
            this.errorGen = errorGen;
            this.module = module;
            this.currentPackageName = getPackageName(this.module.org.value, this.module.name.value);
        }

        void genTerminator(BIRTerminator terminator, BIRFunction func, String funcName,
                           int localVarOffset, int returnVarRefIndex, @Nilable BType attachedType,
                           boolean isObserved /* = false */) {

            switch (terminator.kind) {
                case LOCK:
                    this.genLockTerm((BIRTerminator.Lock) terminator, funcName, localVarOffset);
                    return;
                case FIELD_LOCK:
                    this.genFieldLockTerm((BIRTerminator.FieldLock) terminator, funcName, localVarOffset, attachedType);
                    return;
                case UNLOCK:
                    this.genUnlockTerm((BIRTerminator.Unlock) terminator, funcName, attachedType);
                    return;
                case GOTO:
                    this.genGoToTerm((BIRTerminator.GOTO) terminator, funcName);
                    return;
                case CALL:
                    this.genCallTerm((BIRTerminator.Call) terminator, funcName, localVarOffset);
                    return;
                case ASYNC_CALL:
                    this.genAsyncCallTerm((BIRTerminator.AsyncCall) terminator, localVarOffset);
                    return;
                case BRANCH:
                    this.genBranchTerm((BIRTerminator.Branch) terminator, funcName);
                    return;
                case RETURN:
                    this.genReturnTerm((BIRTerminator.Return) terminator, returnVarRefIndex, func, isObserved,
                            localVarOffset);
                    return;
                case PANIC:
                    this.errorGen.genPanic((BIRTerminator.Panic) terminator);
                    return;
                case WAIT:
                    this.generateWaitIns((BIRTerminator.Wait) terminator, funcName, localVarOffset);
                    return;
                case WAIT_ALL:
                    this.genWaitAllIns((BIRTerminator.WaitAll) terminator, funcName, localVarOffset);
                    return;
                case FP_CALL:
                    this.genFPCallIns((BIRTerminator.FPCall) terminator, funcName, localVarOffset);
                    return;
                case WK_SEND:
                    this.genWorkerSendIns((BIRTerminator.WorkerSend) terminator, funcName, localVarOffset);
                    return;
                case WK_RECEIVE:
                    this.genWorkerReceiveIns((BIRTerminator.WorkerReceive) terminator, funcName, localVarOffset);
                    return;
                case FLUSH:
                    this.genFlushIns((BIRTerminator.Flush) terminator, funcName, localVarOffset);
                    return;
                case PLATFORM:
                    if (terminator instanceof JavaMethodCall) {
                        this.genJCallTerm((JavaMethodCall) terminator, funcName, attachedType, localVarOffset);
                        return;
                    } else if (terminator instanceof JIMethodCall) {
                        this.genJICallTerm((JIMethodCall) terminator, funcName, attachedType, localVarOffset);
                        return;
                    } else if (terminator instanceof JIConstructorCall) {
                        this.genJIConstructorTerm((JIConstructorCall) terminator, funcName, attachedType, localVarOffset);
                        return;
                    }
            }
            throw new BLangCompilerException("JVM generation is not supported for terminator instruction " +
                    String.format("%s", terminator));

        }

        void genGoToTerm(BIRTerminator.GOTO gotoIns, String funcName) {

            Label gotoLabel = this.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
            this.mv.visitJumpInsn(GOTO, gotoLabel);
        }

        void genLockTerm(BIRTerminator.Lock lockIns, String funcName, int localVarOffset) {

            Label gotoLabel = this.labelGen.getLabel(funcName + lockIns.lockedBB.id.value);
            String lockClass = "L" + LOCK_VALUE + ";";
            String varClassName = lookupGlobalVarClassName(this.currentPackageName + lockIns.globalVar.name.value);
            String lockName = computeLockNameFromString(lockIns.globalVar.name.value);
            this.mv.visitFieldInsn(GETSTATIC, varClassName, lockName, lockClass);
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "lock", String.format("(L%s;)Z", STRAND), false);
            this.mv.visitInsn(POP);
            genYieldCheckForLock(this.mv, this.labelGen, funcName, localVarOffset);
            this.mv.visitJumpInsn(GOTO, gotoLabel);
        }

        void genFieldLockTerm(BIRTerminator.FieldLock lockIns, String funcName, int localVarOffset,
                              @Nilable BType attachedType) {

            Label gotoLabel = this.labelGen.getLabel(funcName + lockIns.lockedBB.id.value);
            String lockClass = "L" + LOCK_VALUE + ";";
            String lockName = computeLockNameFromString(lockIns.field);
            this.loadVar(lockIns.localVar.variableDcl);

            if (attachedType.tag == TypeTags.OBJECT) {
                String className = getTypeValueClassName(this.module, attachedType.name.getValue());
                this.mv.visitFieldInsn(GETFIELD, className, lockName, lockClass);
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "lock", String.format("(L%s;)Z", STRAND), false);
                this.mv.visitInsn(POP);
                genYieldCheckForLock(this.mv, this.labelGen, funcName, localVarOffset);
                this.mv.visitJumpInsn(GOTO, gotoLabel);
            } else {
                throw new BLangCompilerException("JVM field lock generation is not supported for type " +
                        String.format("%s", attachedType));
            }
        }

        void genUnlockTerm(BIRTerminator.Unlock unlockIns, String funcName, @Nilable BType attachedType) {

            Label gotoLabel = this.labelGen.getLabel(funcName + unlockIns.unlockBB.id.value);

            String lockClass = "L" + LOCK_VALUE + ";";
            // unlocked in the same order https://yarchive.net/comp/linux/lock_ordering.html
            for (BIRNode.BIRGlobalVariableDcl globalVariable : unlockIns.globalVars) {
                BIRVariableDcl globleVar = this.cleanupVariableDecl(globalVariable);
                String varClassName = lookupGlobalVarClassName(this.currentPackageName + globleVar.name.value);
                String lockName = computeLockNameFromString(globleVar.name.value);
                this.mv.visitFieldInsn(GETSTATIC, varClassName, lockName, lockClass);
                this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "unlock", "()V", false);
            }

            for (Map.Entry<BIROperand, Set<String>> localLockDetails : unlockIns.fieldLocks.entrySet()) {
                if (localLockDetails != null) {
                    if (attachedType.tag == TypeTags.OBJECT) {
                        String className = getTypeValueClassName(this.module, attachedType.name.getValue());
                        for (String fieldName : localLockDetails.getValue()) {
                            String lockName = computeLockNameFromString(fieldName);
                            this.loadVar(localLockDetails.getKey().variableDcl);
                            this.mv.visitFieldInsn(GETFIELD, className, lockName, lockClass);
                            this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "unlock", "()V", false);
                        }
                    } else {
                        throw new BLangCompilerException("JVM field unlock generation is not supported for type " +
                                String.format("%s", attachedType));
                    }
                }
            }

            this.mv.visitJumpInsn(GOTO, gotoLabel);
        }

        void handleErrorRetInUnion(int returnVarRefIndex, List<BIRNode.ChannelDetails> channels, BUnionType bType) {

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
                loadChannelDetails(this.mv, channels);
                this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "handleWorkerError",
                        String.format("(L%s;L%s;[L%s;)V", REF_VALUE, STRAND, CHANNEL_DETAILS), false);
            }
        }

        void notifyChannels(List<BIRNode.ChannelDetails> channels, int retIndex) {

            if (channels.size() == 0) {
                return;
            }

            this.mv.visitVarInsn(ALOAD, 0);
            loadChannelDetails(this.mv, channels);
            this.mv.visitVarInsn(ALOAD, retIndex);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleChannelError", String.format("([L%s;L%s;)V",
                    CHANNEL_DETAILS, ERROR_VALUE), false);
        }

        void genBranchTerm(BIRTerminator.Branch branchIns, String funcName) {

            String trueBBId = branchIns.trueBB.id.value;
            String falseBBId = branchIns.falseBB.id.value;

            this.loadVar(branchIns.op.variableDcl);

            Label trueBBLabel = this.labelGen.getLabel(funcName + trueBBId);
            this.mv.visitJumpInsn(IFGT, trueBBLabel);

            Label falseBBLabel = this.labelGen.getLabel(funcName + falseBBId);
            this.mv.visitJumpInsn(GOTO, falseBBLabel);
        }

        void genCallTerm(BIRTerminator.Call callIns, String funcName, int localVarOffset) {

            PackageID calleePkgId = callIns.calleePkg;

            String orgName = calleePkgId.orgName.value;
            String moduleName = calleePkgId.name.value;
            BIRTerminator.Call callInsCopy = new BIRTerminator.Call(callIns.pos, callIns.kind, callIns.isVirtual,
                    calleePkgId, callIns.name, callIns.args, callIns.lhsOp, callIns.thenBB);
            if (isBStringFunc(funcName)) {
                callInsCopy.name.value = nameOfBStringFunc(callIns.name.value);
            }
            // invoke the function
            this.genCall(callInsCopy, orgName, moduleName, localVarOffset);

            // store return
            if (callIns.lhsOp != null) {
                this.storeReturnFromCallIns(callIns.lhsOp.variableDcl);
            }
        }

        void genJCallTerm(JavaMethodCall callIns, String funcName, @Nilable BType attachedType, int localVarOffset) {
            // Load function parameters of the target Java method to the stack..
            Label blockedOnExternLabel = new Label();
            Label notBlockedOnExternLabel = new Label();

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
            this.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/scheduling/Strand", "blockedOnExtern", "Z");

            if (callIns.lhsOp != null && callIns.lhsOp.variableDcl != null) {
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/scheduling/Strand", "returnValue",
                        "Ljava/lang/Object;");
                @Nilable BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;
                if (lhsOpVarDcl != null) {
                    addUnboxInsn(this.mv, callIns.lhsOp.variableDcl.type, false); // store return
                    this.storeToVar(lhsOpVarDcl);
                }
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
                BIRVariableDcl selfArg = getVariableDcl(callIns.args.get(0).variableDcl);
                this.loadVar(selfArg);
                this.mv.visitTypeInsn(CHECKCAST, OBJECT_VALUE);
                argIndex += 1;
            }

            int argsCount = callIns.args.size();
            while (argIndex < argsCount) {
                @Nilable BIROperand arg = callIns.args.get(argIndex);
                this.visitArg(arg);
                argIndex += 1;
            }

            String jClassName = callIns.jClassName;
            String jMethodName = callIns.name;
            String jMethodVMSig = isBStringFunc(funcName) ? callIns.jMethodVMSigBString : callIns.jMethodVMSig;
            this.mv.visitMethodInsn(INVOKESTATIC, jClassName, jMethodName, jMethodVMSig, false);

            @Nilable BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;

            if (lhsOpVarDcl != null) {
                this.storeToVar(lhsOpVarDcl);
            }

            this.mv.visitLabel(notBlockedOnExternLabel);
        }

        void genJICallTerm(JIMethodCall callIns, String funcName, @Nilable BType attachedType, int localVarOffset) {
            // Load function parameters of the target Java method to the stack..
            Label blockedOnExternLabel = new Label();
            Label notBlockedOnExternLabel = new Label();

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
            this.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/scheduling/Strand", "blockedOnExtern", "Z");

            if (callIns.lhsOp != null) {
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/scheduling/Strand", "returnValue",
                        "Ljava/lang/Object;");
                // store return
                @Nilable BIROperand lhsOpVarDcl = callIns.lhsOp;
                addJUnboxInsn(this.mv, lhsOpVarDcl.variableDcl.type);
                this.storeToVar(lhsOpVarDcl.variableDcl);
            }

            this.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

            this.mv.visitLabel(blockedOnExternLabel);
            boolean isInterface = callIns.invocationType == INVOKEINTERFACE;

            int argIndex = 0;
            if (callIns.invocationType == INVOKEVIRTUAL || isInterface) {
                // check whether function params already include the self
                BIRVariableDcl selfArg = getVariableDcl(callIns.args.get(0).variableDcl);
                this.loadVar(selfArg);
                this.mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
                this.mv.visitTypeInsn(CHECKCAST, callIns.jClassName);

                Label ifNonNullLabel = this.labelGen.getLabel("receiver_null_check");
                this.mv.visitLabel(ifNonNullLabel);
                this.mv.visitInsn(DUP);

                Label elseBlockLabel = this.labelGen.getLabel("receiver_null_check_else");
                this.mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
                Label thenBlockLabel = this.labelGen.getLabel("receiver_null_check_then");
                this.mv.visitLabel(thenBlockLabel);
                this.mv.visitFieldInsn(GETSTATIC, BAL_ERROR_REASONS, "JAVA_NULL_REFERENCE_ERROR",
                        "L" + STRING_VALUE + ";");
                this.mv.visitFieldInsn(GETSTATIC, RUNTIME_ERRORS, "JAVA_NULL_REFERENCE", "L" + RUNTIME_ERRORS + ";");
                this.mv.visitInsn(ICONST_0);
                this.mv.visitTypeInsn(ANEWARRAY, OBJECT);
                this.mv.visitMethodInsn(INVOKESTATIC, BLANG_EXCEPTION_HELPER, "getRuntimeException",
                        "(L" + STRING_VALUE + ";L" + RUNTIME_ERRORS + ";[L" + OBJECT + ";)L" + ERROR_VALUE + ";", false);
                this.mv.visitInsn(ATHROW);
                this.mv.visitLabel(elseBlockLabel);
                argIndex += 1;
            }

            int argsCount = callIns.varArgExist ? callIns.args.size() - 1 : callIns.args.size();
            while (argIndex < argsCount) {
                @Nilable BIROperand arg = callIns.args.get(argIndex);
                this.visitArg(arg);
                argIndex += 1;
            }
            if (callIns.varArgExist) {
                BIROperand arg = callIns.args.get(argIndex);
                int localVarIndex = this.indexMap.getIndex(arg.variableDcl);
                genVarArg(this.mv, this.indexMap, arg.variableDcl.type, (JType) callIns.varArgType, localVarIndex);
            }

            String jClassName = callIns.jClassName;
            String jMethodName = callIns.name;
            String jMethodVMSig = callIns.jMethodVMSig;
            this.mv.visitMethodInsn(callIns.invocationType, jClassName, jMethodName, jMethodVMSig, isInterface);

            @Nilable BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;

            if (lhsOpVarDcl != null) {
                this.storeToVar(lhsOpVarDcl);
            }

            this.mv.visitLabel(notBlockedOnExternLabel);
        }

        void genJIConstructorTerm(JIConstructorCall callIns, String funcName, @Nilable BType attachedType,
                                  int localVarOffset) {
            // Load function parameters of the target Java method to the stack..
            Label blockedOnExternLabel = new Label();
            Label notBlockedOnExternLabel = new Label();

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
            this.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/scheduling/Strand", "blockedOnExtern", "Z");

            if (callIns.lhsOp.variableDcl != null) {
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/scheduling/Strand", "returnValue",
                        "Ljava/lang/Object;");
                addUnboxInsn(this.mv, callIns.lhsOp.variableDcl.type, false);
                // store return
                @Nilable BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;
                this.storeToVar(lhsOpVarDcl);
            }

            this.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

            this.mv.visitLabel(blockedOnExternLabel);

            this.mv.visitTypeInsn(NEW, callIns.jClassName);
            this.mv.visitInsn(DUP);

            int argIndex = 0;

            int argsCount = callIns.args.size();
            while (argIndex < argsCount) {
                @Nilable BIROperand arg = callIns.args.get(argIndex);
                this.visitArg(arg);
                argIndex += 1;
            }

            String jClassName = callIns.jClassName;
            String jMethodName = callIns.name;
            String jMethodVMSig = callIns.jMethodVMSig;
            this.mv.visitMethodInsn(INVOKESPECIAL, jClassName, jMethodName, jMethodVMSig, false);

            @Nilable BIRVariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl;

            if (lhsOpVarDcl != null) {
                this.storeToVar(lhsOpVarDcl);
            }

            this.mv.visitLabel(notBlockedOnExternLabel);
        }

        private BIRVariableDcl cleanupVariableDecl(@Nilable BIRVariableDcl varDecl) {

            if (varDecl != null) {
                return varDecl;
            }
            throw new BLangCompilerException("Invalid variable declaration");
        }

        private void storeReturnFromCallIns(@Nilable BIRVariableDcl lhsOpVarDcl) {

            if (lhsOpVarDcl != null) {
                this.storeToVar(lhsOpVarDcl);
            } else {
                this.mv.visitInsn(POP);
            }
        }

        private void genCall(BIRTerminator.Call callIns, String orgName, String moduleName, int localVarOffset) {

            if (!callIns.isVirtual) {
                this.genFuncCall(callIns, orgName, moduleName, localVarOffset);
                return;
            }

            BIRVariableDcl selfArg = getVariableDcl(callIns.args.get(0).variableDcl);
            if (selfArg.type.tag == TypeTags.OBJECT) {
                this.genVirtualCall(callIns, orgName, moduleName, localVarOffset);
            } else {
                // then this is a function attached to a built-in type
                this.genBuiltinTypeAttachedFuncCall(callIns, orgName, moduleName, localVarOffset);
            }
        }

        private void genFuncCall(BIRTerminator.Call callIns, String orgName, String moduleName, int localVarOffset) {

            String methodName = callIns.name.value;
            this.genStaticCall(callIns, orgName, moduleName, localVarOffset, methodName, methodName);
        }

        private void genBuiltinTypeAttachedFuncCall(BIRTerminator.Call callIns, String orgName, String moduleName,
                                                    int localVarOffset) {

            String methodLookupName = callIns.name.value;
            @Nilable int optionalIndex = methodLookupName.indexOf(".");
            int index = optionalIndex != -1 ? optionalIndex + 1 : 0;
            String methodName = methodLookupName.substring(index);
            this.genStaticCall(callIns, orgName, moduleName, localVarOffset, methodName, methodLookupName);
        }

        private void genStaticCall(BIRTerminator.Call callIns, String orgName, String moduleName, int localVarOffset,
                                   String methodName, String methodLookupName) {
            // load strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            String lookupKey = nameOfNonBStringFunc(getPackageName(orgName, moduleName) + methodLookupName);

            int argsCount = callIns.args.size();
            int i = 0;
            while (i < argsCount) {
                @Nilable BIROperand arg = callIns.args.get(i);
                boolean userProvidedArg = this.visitArg(arg);
                this.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
                i += 1;
            }

            String jvmClass = lookupFullQualifiedClassName(lookupKey);
            String cleanMethodName = cleanupFunctionName(methodName);
            boolean useBString = IS_BSTRING && orgName.equals("ballerina") &&
                    moduleName.equals("lang.string") && !cleanMethodName.endsWith("_");
            if (useBString) {
                cleanMethodName = nameOfBStringFunc(cleanMethodName);
            }
            String methodDesc = lookupJavaMethodDescription(lookupKey, useBString);
            this.mv.visitMethodInsn(INVOKESTATIC, jvmClass, cleanMethodName, methodDesc, false);
        }

        private void genVirtualCall(BIRTerminator.Call callIns, String orgName, String moduleName, int localVarOffset) {
            // load self
            BIRVariableDcl selfArg = getVariableDcl(callIns.args.get(0).variableDcl);
            this.loadVar(selfArg);
            this.mv.visitTypeInsn(CHECKCAST, OBJECT_VALUE);

            // load the strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);

            // load the function name as the second argument
            this.mv.visitLdcInsn(cleanupObjectTypeName(callIns.name.value));

            // create an Object[] for the rest params
            int argsCount = callIns.args.size() - 1;
            // arg count doubled and 'isExist' boolean variables added for each arg.
            this.mv.visitLdcInsn(argsCount * 2);
            this.mv.visitInsn(L2I);
            this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

            int i = 0;
            int j = 0;
            while (i < argsCount) {
                this.mv.visitInsn(DUP);
                this.mv.visitLdcInsn(j);
                this.mv.visitInsn(L2I);
                j += 1;
                // i + 1 is used since we skip the first argument (self)
                @Nilable BIROperand arg = callIns.args.get(i + 1);
                boolean userProvidedArg = this.visitArg(arg);

                // Add the to the rest params array
                addBoxInsn(this.mv, arg.variableDcl.type);
                this.mv.visitInsn(AASTORE);

                this.mv.visitInsn(DUP);
                this.mv.visitLdcInsn(j);
                this.mv.visitInsn(L2I);
                j += 1;

                this.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
                addBoxInsn(this.mv, new BType(TypeTags.BOOLEAN, null));
                this.mv.visitInsn(AASTORE);

                i += 1;
            }

            // call method
            String methodDesc = String.format("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
            this.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);

            @Nilable BType returnType = callIns.lhsOp.variableDcl.type;
            addUnboxInsn(this.mv, returnType, false);
        }

        void loadBooleanArgToIndicateUserProvidedArg(String orgName, String moduleName, boolean userProvided) {

            if (isBallerinaBuiltinModule(orgName, moduleName)) {
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

        boolean visitArg(BIROperand arg) {

            BIRVariableDcl varDcl = getVariableDcl(arg.variableDcl);
            if (varDcl.name.value.startsWith("_")) {
                loadDefaultValue(this.mv, varDcl.type);
                return false;
            }

            this.loadVar(varDcl);
            return true;
        }

        void genAsyncCallTerm(BIRTerminator.AsyncCall callIns, int localVarOffset) {

            PackageID calleePkgId = callIns.calleePkg;

            String orgName = calleePkgId.orgName.value;
            String moduleName = calleePkgId.name.value;
            // Load the scheduler from strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", String.format("L%s;", SCHEDULER));

            // create an Object[] for the rest params
            int argsCount = callIns.args.size();
            //create an object array of args
            this.mv.visitLdcInsn(argsCount * 2 + 1);
            this.mv.visitInsn(L2I);
            this.mv.visitTypeInsn(ANEWARRAY, OBJECT);

            int paramIndex = 1;
            for (BIROperand arg : callIns.args) {
                this.mv.visitInsn(DUP);
                this.mv.visitLdcInsn(paramIndex);
                this.mv.visitInsn(L2I);

                boolean userProvidedArg = this.visitArg(arg);
                // Add the to the rest params array
                addBoxInsn(this.mv, arg.variableDcl.type);
                this.mv.visitInsn(AASTORE);
                paramIndex += 1;

                this.mv.visitInsn(DUP);
                this.mv.visitLdcInsn(paramIndex);
                this.mv.visitInsn(L2I);

                this.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
                addBoxInsn(this.mv, new BType(TypeTags.BOOLEAN, null));
                this.mv.visitInsn(AASTORE);
                paramIndex += 1;
            }
            String funcName = callIns.name.value;
            String lambdaName = "$" + funcName + "$lambda$" + lambdaIndex + "$";
            String currentPackageName = getPackageName(this.module.org.value, this.module.name.value);

            @Nilable BType futureType = callIns.lhsOp.variableDcl.type;
            BType returnType = symbolTable.nilType;
            if (futureType.tag == TypeTags.FUTURE) {
                returnType = ((BFutureType) futureType).constraint;
            }

            createFunctionPointer(this.mv, currentClass, lambdaName, 0);
            lambdas.put(lambdaName, callIns);
            lambdaIndex += 1;

            this.submitToScheduler(callIns.lhsOp, localVarOffset);
        }

        void generateWaitIns(BIRTerminator.Wait waitInst, String funcName, int localVarOffset) {

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitTypeInsn(NEW, ARRAY_LIST);
            this.mv.visitInsn(DUP);
            this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);

            int i = 0;
            while (i < waitInst.exprList.size()) {
                this.mv.visitInsn(DUP);
                @Nilable BIROperand futureVal = waitInst.exprList.get(i);
                if (futureVal != null) {
                    this.loadVar(futureVal.variableDcl);
                }
                this.mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", String.format("(L%s;)Z", OBJECT), true);
                this.mv.visitInsn(POP);
                i += 1;
            }

            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleWaitAny",
                    String.format("(L%s;)L%s$WaitResult;", LIST, STRAND), false);
            BIRVariableDcl tempVar = new BIRVariableDcl(symbolTable.anyType, new Name("waitResult"), VarScope.FUNCTION,
                    VarKind.ARG);
            int resultIndex = this.getJVMIndexOfVarRef(tempVar);
            this.mv.visitVarInsn(ASTORE, resultIndex);

            // assign result if result available
            Label afterIf = new Label();
            this.mv.visitVarInsn(ALOAD, resultIndex);
            this.mv.visitFieldInsn(GETFIELD, String.format("%s$WaitResult", STRAND), "done", "Z");
            this.mv.visitJumpInsn(IFEQ, afterIf);
            Label withinIf = new Label();
            this.mv.visitLabel(withinIf);
            this.mv.visitVarInsn(ALOAD, resultIndex);
            this.mv.visitFieldInsn(GETFIELD, String.format("%s$WaitResult", STRAND), "result",
                    String.format("L%s;", OBJECT));
            addUnboxInsn(this.mv, waitInst.lhsOp.variableDcl.type, false);
            this.storeToVar(waitInst.lhsOp.variableDcl);
            this.mv.visitLabel(afterIf);
        }

        void genWaitAllIns(BIRTerminator.WaitAll waitAll, String funcName, int localVarOffset) {

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitTypeInsn(NEW, "java/util/HashMap");
            this.mv.visitInsn(DUP);
            this.mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            int i = 0;
            while (i < waitAll.keys.size()) {
                this.mv.visitInsn(DUP);
                this.mv.visitLdcInsn(waitAll.keys.get(i));
                @Nilable BIROperand futureRef = waitAll.valueExprs.get(i);
                if (futureRef != null) {
                    this.loadVar(futureRef.variableDcl);
                }
                this.mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", String.format("(L%s;L%s;)L%s;",
                        OBJECT, OBJECT, OBJECT), true);
                this.mv.visitInsn(POP);
                i += 1;
            }

            this.loadVar(waitAll.lhsOp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleWaitMultiple", String.format("(L%s;L%s;)V",
                    MAP, MAP_VALUE), false);
        }

        void genFPCallIns(BIRTerminator.FPCall fpCall, String funcName, int localVarOffset) {

            if (fpCall.isAsync) {
                // Load the scheduler from strand
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", String.format("L%s;", SCHEDULER));
            } else {
                // load function ref, going to directly call the fp
                this.loadVar(fpCall.fp.variableDcl);
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
                this.loadVar(getVariableDcl(arg.variableDcl));
                @Nilable BType bType = arg.variableDcl.type;
                addBoxInsn(this.mv, bType);
                this.mv.visitInsn(AASTORE);
                paramIndex += 1;

                this.loadTrueValueAsArg(paramIndex);
                paramIndex += 1;
            }

            // if async, we submit this to sceduler (worker scenario)
            BType returnType = fpCall.fp.variableDcl.type;

            if (fpCall.isAsync) {
                // load function ref now
                this.loadVar(fpCall.fp.variableDcl);
                this.submitToScheduler(fpCall.lhsOp, localVarOffset);
            } else {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_POINTER, "call",
                        String.format("(L%s;)L%s;", OBJECT, OBJECT), false);
                // store reult
                @Nilable BType lhsType = fpCall.lhsOp.variableDcl.type;
                if (lhsType != null) {
                    addUnboxInsn(this.mv, lhsType, false);
                }

                @Nilable BIRVariableDcl lhsVar = fpCall.lhsOp.variableDcl;
                if (lhsVar != null) {
                    this.storeToVar(lhsVar);
                } else {
                    this.mv.visitInsn(POP);
                }
            }
        }

        void loadTrueValueAsArg(int paramIndex) {

            this.mv.visitInsn(DUP);
            this.mv.visitIntInsn(BIPUSH, paramIndex);
            this.mv.visitInsn(ICONST_1);
            addBoxInsn(this.mv, new BType(TypeTags.BOOLEAN, null));
            this.mv.visitInsn(AASTORE);
        }

        void genWorkerSendIns(BIRTerminator.WorkerSend ins, String funcName, int localVarOffset) {

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            if (!ins.isSameStrand) {
                this.mv.visitFieldInsn(GETFIELD, STRAND, "parent", String.format("L%s;", STRAND));
            }
            this.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", String.format("L%s;", WD_CHANNELS));
            this.mv.visitLdcInsn(ins.channel.value);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", String.format("(L%s;)L%s;",
                    STRING_VALUE, WORKER_DATA_CHANNEL), false);
            this.loadVar(ins.data.variableDcl);
            addBoxInsn(this.mv, ins.data.variableDcl.type);
            this.mv.visitVarInsn(ALOAD, localVarOffset);

            if (!ins.isSync) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "sendData", String.format("(L%s;L%s;)V",
                        OBJECT, STRAND), false);
            } else {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "syncSendData",
                        String.format("(L%s;L%s;)L%s;", OBJECT, STRAND, OBJECT), false);
                @Nilable BIROperand lhsOp = ins.lhsOp;
                if (lhsOp != null) {
                    this.storeToVar(lhsOp.variableDcl);
                }
            }
        }

        void genWorkerReceiveIns(BIRTerminator.WorkerReceive ins, String funcName, int localVarOffset) {

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            if (!ins.isSameStrand) {
                this.mv.visitFieldInsn(GETFIELD, STRAND, "parent", String.format("L%s;", STRAND));
            }
            this.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", String.format("L%s;", WD_CHANNELS));
            this.mv.visitLdcInsn(ins.workerName.value);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", String.format("(L%s;)L%s;",
                    STRING_VALUE, WORKER_DATA_CHANNEL), false);

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "tryTakeData", String.format("(L%s;)L%s;",
                    STRAND, OBJECT), false);

            BIRVariableDcl tempVar = new BIRVariableDcl(symbolTable.anyType, new Name("wrkMsg"), VarScope.FUNCTION,
                    VarKind.ARG);
            int wrkResultIndex = this.getJVMIndexOfVarRef(tempVar);
            this.mv.visitVarInsn(ASTORE, wrkResultIndex);

            Label jumpAfterReceive = new Label();
            this.mv.visitVarInsn(ALOAD, wrkResultIndex);
            this.mv.visitJumpInsn(IFNULL, jumpAfterReceive);

            Label withinReceiveSuccess = new Label();
            this.mv.visitLabel(withinReceiveSuccess);
            this.mv.visitVarInsn(ALOAD, wrkResultIndex);
            addUnboxInsn(this.mv, ins.lhsOp.variableDcl.type, false);
            this.storeToVar(ins.lhsOp.variableDcl);

            this.mv.visitLabel(jumpAfterReceive);
        }

        void genFlushIns(BIRTerminator.Flush ins, String funcName, int localVarOffset) {

            this.mv.visitVarInsn(ALOAD, localVarOffset);
            loadChannelDetails(this.mv, Arrays.asList(ins.channels));
            this.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleFlush",
                    String.format("([L%s;)L%s;", CHANNEL_DETAILS, ERROR_VALUE), false);
            this.storeToVar(ins.lhsOp.variableDcl);
        }

        void submitToScheduler(@Nilable BIROperand lhsOp, int localVarOffset) {

            @Nilable BType futureType = lhsOp.variableDcl.type;
            BType returnType = symbolTable.anyType;
            if (futureType.tag == TypeTags.FUTURE) {
                returnType = ((BFutureType) futureType).constraint;
            }

            // load strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            loadType(this.mv, returnType);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "scheduleFunction",
                    String.format("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE),
                    false);

            // store return
            if (lhsOp.variableDcl != null) {
                @Nilable BIRVariableDcl lhsOpVarDcl = lhsOp.variableDcl;
                // store the returned strand as the future
                this.storeToVar(getVariableDcl(lhsOpVarDcl));
            }
        }

        int getJVMIndexOfVarRef(BIRVariableDcl varDcl) {

            return this.indexMap.getIndex(varDcl);
        }

        private void loadVar(BIRVariableDcl varDcl) {

            generateVarLoad(this.mv, varDcl, this.currentPackageName, this.getJVMIndexOfVarRef(varDcl));
        }

        private void storeToVar(BIRVariableDcl varDcl) {

            generateVarStore(this.mv, varDcl, this.currentPackageName, this.getJVMIndexOfVarRef(varDcl));
        }

        public void genReturnTerm(BIRTerminator.Return returnIns, int returnVarRefIndex, BIRFunction func,
                                  boolean isObserved /* = false */, int localVarOffset /* = -1 */) {

            if (isObserved) {
                emitStopObservationInvocation(this.mv, localVarOffset);
            }
            BType bType = func.type.retType;
            if (bType.tag == TypeTags.NIL) {
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
            } else if (bType.tag == TypeTags.INT) {
                this.mv.visitVarInsn(LLOAD, returnVarRefIndex);
                this.mv.visitInsn(LRETURN);
            } else if (bType.tag == TypeTags.BYTE) {
                this.mv.visitVarInsn(ILOAD, returnVarRefIndex);
                this.mv.visitInsn(IRETURN);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitVarInsn(DLOAD, returnVarRefIndex);
                this.mv.visitInsn(DRETURN);
            } else if (bType.tag == TypeTags.STRING) {
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
            } else if (bType.tag == TypeTags.BOOLEAN) {
                this.mv.visitVarInsn(ILOAD, returnVarRefIndex);
                this.mv.visitInsn(IRETURN);
            } else if (bType.tag == TypeTags.MAP ||
                    bType.tag == TypeTags.ARRAY ||
                    bType.tag == TypeTags.ANY ||
                    bType.tag == TypeTags.TABLE ||
                    bType.tag == TypeTags.ANYDATA ||
                    bType.tag == TypeTags.OBJECT ||
                    bType.tag == TypeTags.DECIMAL ||
                    bType.tag == TypeTags.RECORD ||
                    bType.tag == TypeTags.TUPLE ||
                    bType.tag == TypeTags.JSON ||
                    bType.tag == TypeTags.FUTURE ||
                    bType.tag == TypeTags.XML ||
                    bType.tag == TypeTags.INVOKABLE ||
                    bType.tag == TypeTags.HANDLE ||
                    bType.tag == TypeTags.FINITE ||
                    bType.tag == TypeTags.TYPEDESC) {
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
            } else if (bType.tag == TypeTags.UNION) {
                this.handleErrorRetInUnion(returnVarRefIndex, Arrays.asList(func.workerChannels), (BUnionType) bType);
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
            } else if (bType.tag == TypeTags.ERROR) {
                this.notifyChannels(Arrays.asList(func.workerChannels), returnVarRefIndex);
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
            } else {
                throw new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", func.type.retType));
            }
        }
    }
}
