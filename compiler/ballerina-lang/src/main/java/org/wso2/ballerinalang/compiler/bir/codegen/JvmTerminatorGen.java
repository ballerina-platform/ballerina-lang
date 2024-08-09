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
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.*;
import org.wso2.ballerinalang.compiler.bir.codegen.model.*;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.*;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.*;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.*;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.genVarArg;

/**
 * BIR terminator instruction generator class to keep track of method visitor and index map.
 *
 * @since 1.2.0
 */
public class JvmTerminatorGen {

    private final MethodVisitor mv;
    private final BIRVarToJVMIndexMap indexMap;
    private final LabelGenerator labelGen;
    private final JvmErrorGen errorGen;
    private final String currentPackageName;
    private final String moduleInitClass;
    private final JvmPackageGen jvmPackageGen;
    private final JvmInstructionGen jvmInstructionGen;
    private final PackageCache packageCache;
    private final SymbolTable symbolTable;
    private final Unifier unifier;
    private final JvmTypeGen jvmTypeGen;
    private final JvmCastGen jvmCastGen;
    private final AsyncDataCollector asyncDataCollector;
    private final String strandMetadataClass;

    public JvmTerminatorGen(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, LabelGenerator labelGen,
                            JvmErrorGen errorGen, PackageID packageID, JvmInstructionGen jvmInstructionGen,
                            JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen,
                            JvmCastGen jvmCastGen, JvmConstantsGen jvmConstantsGen,
                            AsyncDataCollector asyncDataCollector) {

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
        this.strandMetadataClass = jvmConstantsGen.getStrandMetadataConstantsClass();
    }

    public void genTerminator(BIRTerminator terminator, BIRNode.BIRFunction func,
                              String funcName, int localVarOffset, int returnVarRefIndex,
                              BType attachedType, int channelMapVarIndex, int sendWorkerChannelNamesVar,
                              int receiveWorkerChannelNamesVar) {
        switch (terminator.kind) {
            case GOTO -> {
                this.genGoToTerm((BIRTerminator.GOTO) terminator, funcName);
                return;
            }
            case RETURN -> {
                this.genReturnTerm(returnVarRefIndex, func, localVarOffset, sendWorkerChannelNamesVar,
                        receiveWorkerChannelNamesVar);
                return;
            }
            case BRANCH -> {
                this.genBranchTerm((BIRTerminator.Branch) terminator, funcName);
                return;
            }
            case CALL -> {
                this.genCallTerm((BIRTerminator.Call) terminator, localVarOffset, funcName);
                return;
            }
            case FP_CALL -> {
                this.genFPCallIns((BIRTerminator.FPCall) terminator, attachedType, funcName, localVarOffset,
                        func.hasWorkers, channelMapVarIndex);
                return;
            }
            case ASYNC_CALL -> {
                this.genAsyncCallTerm((BIRTerminator.AsyncCall) terminator, localVarOffset, attachedType, funcName,
                        func.hasWorkers, channelMapVarIndex);
                return;
            }
            case PLATFORM -> {
                this.genPlatformIns((JTerminator) terminator, attachedType, localVarOffset, func);
                return;
            }
            case PANIC -> {
                this.errorGen.genPanic((BIRTerminator.Panic) terminator);
                return;
            }
            case WAIT -> {
                this.generateWaitIns((BIRTerminator.Wait) terminator, localVarOffset);
                return;
            }
            case WAIT_ALL -> {
                this.genWaitAllIns((BIRTerminator.WaitAll) terminator, localVarOffset);
                return;
            }
            case LOCK -> {
                this.genLockTerm((BIRTerminator.Lock) terminator, funcName);
                return;
            }
            case UNLOCK -> {
                this.genUnlockTerm((BIRTerminator.Unlock) terminator, funcName);
                return;
            }
            case WK_SEND -> {
                this.genWorkerSendIns((BIRTerminator.WorkerSend) terminator, localVarOffset);
                return;
            }
            case WK_RECEIVE -> {
                this.genWorkerReceiveIns((BIRTerminator.WorkerReceive) terminator, localVarOffset);
                return;
            }
            case WK_ALT_RECEIVE -> {
                this.genWorkerAlternateReceiveIns((BIRTerminator.WorkerAlternateReceive) terminator, localVarOffset);
                return;
            }
            case WK_MULTIPLE_RECEIVE -> {
                this.genWorkerMultipleReceiveIns((BIRTerminator.WorkerMultipleReceive) terminator, localVarOffset);
                return;
            }
            case FLUSH -> {
                this.genFlushIns((BIRTerminator.Flush) terminator, localVarOffset);
                return;
            }
        }
        throw new BLangCompilerException("JVM generation is not supported for terminator instruction " +
                terminator);

    }

    private void genGoToTerm(BIRTerminator.GOTO gotoIns, String funcName) {
        Label gotoLabel = this.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    public void genReturnTerm(int returnVarRefIndex, BIRNode.BIRFunction func, int localVarOffset,
                              int sendWorkerChannelNamesVar, int receiveWorkerChannelNamesVar) {
        if ((func.flags & Flags.WORKER) == Flags.WORKER && func.workerChannels.length > 0) {
            mv.visitVarInsn(ALOAD, localVarOffset);
            mv.visitLdcInsn(func.workerName.toString());
            mv.visitVarInsn(ALOAD, sendWorkerChannelNamesVar);
            mv.visitVarInsn(ALOAD, receiveWorkerChannelNamesVar);
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_WORKER_RETURN_METHOD, HANDLE_WORKER_RETURN, false);
        }

        BType bType = unifier.build(func.type.retType);
        generateReturnTermFromType(returnVarRefIndex, bType, func);
    }

    private void genBranchTerm(BIRTerminator.Branch branchIns, String funcName) {
        this.loadVar(branchIns.op.variableDcl);
        Label trueBBLabel = this.labelGen.getLabel(funcName + branchIns.trueBB.id.value);
        this.mv.visitJumpInsn(IFGT, trueBBLabel);
        Label falseBBLabel = this.labelGen.getLabel(funcName + branchIns.falseBB.id.value);
        this.mv.visitJumpInsn(GOTO, falseBBLabel);
    }

    private void genCall(BIRTerminator.Call callIns, PackageID packageID, int localVarOffset) {

        if (!callIns.isVirtual) {
            this.genFuncCall(callIns, packageID, localVarOffset);
            return;
        }

        BIRNode.BIRVariableDcl selfArg = callIns.args.getFirst().variableDcl;
        BType selfArgRefType = JvmCodeGenUtil.getImpliedType(selfArg.type);
        if (selfArgRefType.tag == TypeTags.OBJECT  || selfArgRefType.tag == TypeTags.UNION) {
            this.genVirtualCall(callIns, localVarOffset);
        } else {
            // then this is a function attached to a built-in type
            this.genBuiltinTypeAttachedFuncCall(callIns, packageID, localVarOffset);
        }
    }

    private void genLockTerm(BIRTerminator.Lock lockIns, String funcName) {

        Label gotoLabel = this.labelGen.getLabel(funcName + lockIns.lockedBB.id.value);
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        String lockName = GLOBAL_LOCK_NAME + lockIns.lockId;
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, GET_LOCK_STORE);
        this.mv.visitLdcInsn(lockName);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", GET_LOCK_FROM_MAP, false);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, REENTRANT_LOCK, "lock", VOID_METHOD_DESC, false);
        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void genUnlockTerm(BIRTerminator.Unlock unlockIns, String funcName) {

        Label gotoLabel = this.labelGen.getLabel(funcName + unlockIns.unlockBB.id.value);

        // unlocked in the same order https://yarchive.net/comp/linux/lock_ordering.html
        String lockName = GLOBAL_LOCK_NAME + unlockIns.relatedLock.lockId;
        String initClassName = jvmPackageGen.lookupGlobalVarClassName(this.currentPackageName, LOCK_STORE_VAR_NAME);
        this.mv.visitFieldInsn(GETSTATIC, initClassName, LOCK_STORE_VAR_NAME, GET_LOCK_STORE);
        this.mv.visitLdcInsn(lockName);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", GET_LOCK_FROM_MAP, false);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, REENTRANT_LOCK, "unlock", VOID_METHOD_DESC, false);
        this.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    private void genCallTerm(BIRTerminator.Call callIns, int localVarOffset, String funcName) {
        // invoke the function
        this.genCall(callIns, callIns.calleePkg, localVarOffset);

        // store return
        this.storeReturnFromCallIns(callIns.lhsOp != null ? callIns.lhsOp.variableDcl : null);
        Label thenBBLabel = this.labelGen.getLabel(funcName + callIns.thenBB.id.value);
        this.mv.visitJumpInsn(GOTO, thenBBLabel);
    }

    private void genPlatformIns(JTerminator terminator, BType attachedType, int localVarOffset,
                                BIRNode.BIRFunction func) {
        switch (terminator.jTermKind) {
            case J_METHOD_CALL -> this.genJCallTerm((JavaMethodCall) terminator, attachedType, localVarOffset);
            case JI_METHOD_CALL -> this.genJICallTerm((JIMethodCall) terminator, localVarOffset, func);
            case JI_CONSTRUCTOR_CALL -> this.genJIConstructorTerm((JIConstructorCall) terminator);
            case JI_METHOD_CLI_CALL -> this.genJICLICallTerm((JIMethodCLICall) terminator, localVarOffset);
            default -> throw new BLangCompilerException("JVM generation is not supported for terminator instruction " +
                    terminator);
        }
    }

    private void genJICLICallTerm(JIMethodCLICall terminator, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset + 1);
        this.mv.visitTypeInsn(CHECKCAST, terminator.jClassName);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, terminator.jClassName, terminator.name, terminator.jMethodVMSig, false);
        BIRNode.BIRVariableDcl tempVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("%arrayResult"),
                VarScope.FUNCTION, VarKind.TEMP);
        int resultIndex = this.getJVMIndexOfVarRef(tempVar);
        this.mv.visitVarInsn(ASTORE, resultIndex);
        int nonDefaultArgsCount = terminator.lhsArgs.size() - terminator.defaultFunctionArgs.size();
        int index = 0;
        for (BIROperand lhsArg : terminator.lhsArgs) {
            this.mv.visitVarInsn(ALOAD, resultIndex);
            this.mv.visitIntInsn(BIPUSH, index + 1);
            this.mv.visitInsn(AALOAD);
            if (index < nonDefaultArgsCount) {
                jvmCastGen.addUnboxInsn(this.mv, lhsArg.variableDcl.type);
            } else {
                lhsArg = terminator.defaultFunctionArgs.get(index - nonDefaultArgsCount);
            }
            index++;
            this.storeToVar(lhsArg.variableDcl);
        }
    }

    private void genJCallTerm(JavaMethodCall callIns, BType attachedType, int localVarOffset) {

        int argIndex = 0;
        if (attachedType == null) {
            this.mv.visitVarInsn(ALOAD, localVarOffset);
        } else {
            // Below codes are not needed (as normal external functions doesn't support attached invocations)
            // check whether function params already include the self
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            BIRNode.BIRVariableDcl selfArg = callIns.args.getFirst().variableDcl;
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
    }

    private void genJICallTerm(JIMethodCall callIns, int localVarOffset, BIRNode.BIRFunction func) {
        boolean isInterface = callIns.invocationType == INVOKEINTERFACE;
        int argIndex = 0;
        if (callIns.invocationType == INVOKEVIRTUAL || isInterface) {
            // check whether function params already include the self
            BIRNode.BIRVariableDcl selfArg = callIns.args.getFirst().variableDcl;
            this.loadVar(selfArg);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD, RETURN_OBJECT, false);
            this.mv.visitTypeInsn(CHECKCAST, callIns.jClassName);

            Label ifNonNullLabel = this.labelGen.getLabel("receiver_null_check");
            this.mv.visitLabel(ifNonNullLabel);
            this.mv.visitInsn(DUP);

            Label elseBlockLabel = this.labelGen.getLabel("receiver_null_check_else");
            this.mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
            Label thenBlockLabel = this.labelGen.getLabel("receiver_null_check_then");
            this.mv.visitLabel(thenBlockLabel);
            this.mv.visitFieldInsn(GETSTATIC, ERROR_REASONS, "JAVA_NULL_REFERENCE_ERROR",
                    GET_STRING);
            this.mv.visitFieldInsn(GETSTATIC, ERROR_CODES, "JAVA_NULL_REFERENCE",
                    GET_RUNTIME_ERROR);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitTypeInsn(ANEWARRAY, OBJECT);
            this.mv.visitMethodInsn(INVOKESTATIC, ERROR_HELPER, "getRuntimeException",
                    GET_RUNTIME_EXCEPTION, false);
            this.mv.visitInsn(ATHROW);
            this.mv.visitLabel(elseBlockLabel);
            argIndex += 1;
        }

        String jMethodVMSig = callIns.jMethodVMSig;
        boolean hasBalEnvParam = jMethodVMSig.startsWith(BAL_ENV_PARAM);

        if (hasBalEnvParam) {
            mv.visitTypeInsn(NEW, BAL_ENV_CLASS);
            mv.visitInsn(DUP);
            this.mv.visitVarInsn(ALOAD, localVarOffset); // load the strand
            // load the current Module
            mv.visitFieldInsn(GETSTATIC, this.moduleInitClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
            // load function name
            mv.visitLdcInsn(func.name.getValue());
            this.jvmTypeGen.loadFunctionPathParameters(mv, (BInvokableTypeSymbol) func.type.tsymbol);
            mv.visitMethodInsn(INVOKESPECIAL, BAL_ENV_CLASS, JVM_INIT_METHOD, INIT_BAL_ENV_WITH_FUNC_NAME, false);
        }

        if (callIns.receiver != null) {
            this.loadVar(callIns.receiver.variableDcl);
        }

        List<BIROperand> resourcePathArgs = callIns.resourcePathArgs;
        if (resourcePathArgs != null && !resourcePathArgs.isEmpty()) {
            genResourcePathArgs(resourcePathArgs);
        }
        List<BIROperand> functionArgs = callIns.functionArgs;
        if (functionArgs != null && !functionArgs.isEmpty()) {
            genBundledFunctionArgs(functionArgs);
        }
        if (callIns.isInternal) {
            this.mv.visitVarInsn(ALOAD, localVarOffset); // load the strand
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
        if (callIns.lhsOp != null && callIns.lhsOp.variableDcl != null) {
            this.storeToVar(callIns.lhsOp.variableDcl);
        }
    }

    private void genJIConstructorTerm(JIConstructorCall callIns) {
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
    }

    private void storeReturnFromCallIns(BIRNode.BIRVariableDcl lhsOpVarDcl) {
        if (lhsOpVarDcl != null) {
            this.storeToVar(lhsOpVarDcl);
        } else {
            this.mv.visitInsn(POP);
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
            jvmClass = functionWrapper.fullQualifiedClassName();
            methodDesc = functionWrapper.jvmMethodDescription();
        } else {
            BPackageSymbol symbol = packageCache.getSymbol(
                    packageID.orgName.getValue() + "/" + packageID.name.getValue());
            Name decodedMethodName = new Name(Utils.decodeIdentifier(methodName));
            BInvokableSymbol funcSymbol = (BInvokableSymbol) symbol.scope.lookup(decodedMethodName).symbol;
            if (funcSymbol == null && JvmCodeGenUtil.isModuleInitializerMethod(decodedMethodName.value)) {
                // moduleInit() and moduleStart() functions are not present in the BIR cache because they are generated
                // in CodeGen phase. Therefore, they are not found inside the packageSymbol scope.
                jvmClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                        JvmCodeGenUtil.cleanupPathSeparators(MODULE_INIT_CLASS_NAME));
                this.mv.visitMethodInsn(INVOKESTATIC, jvmClass, encodedMethodName, MODULE_INITIALIZER, false);
                return;
            }
            assert funcSymbol != null;
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

    private void genVirtualCall(BIRTerminator.Call callIns, int localVarOffset) {
        // load self
        BIRNode.BIRVariableDcl selfArg = callIns.args.getFirst().variableDcl;
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

            // Add to the rest params array
            jvmCastGen.addBoxInsn(this.mv, arg.variableDcl.type);
            this.mv.visitInsn(AASTORE);
            i += 1;
        }

        // call method
        this.mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "call", BOBJECT_CALL, true);
        BType returnType = callIns.lhsOp.variableDcl.type;
        jvmCastGen.addUnboxInsn(this.mv, returnType);
    }

    private void genAsyncCallTerm(BIRTerminator.AsyncCall callIns, int localVarOffset, BType attachedType,
                                  String parentFunction, boolean hasWorkers, int channelMapVarIndex) {
        // Load the scheduler from strand
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", GET_SCHEDULER);

        LambdaFunction lambdaFunction = asyncDataCollector.addAndGetLambda(callIns.name.value, callIns, true);
        JvmCodeGenUtil.createFunctionPointer(this.mv, lambdaFunction.enclosingClass, lambdaFunction.lambdaName);

        boolean isIsolated = false;
        String strandName = null;
        // check for isIsolated annotation
        if (!callIns.annotAttachments.isEmpty()) {
            for (BIRNode.BIRAnnotationAttachment annotationAttachment : callIns.annotAttachments) {
                if (annotationAttachment == null ||
                        !STRAND.equals(annotationAttachment.annotTagRef.value) ||
                        !JvmCodeGenUtil.isBuiltInPackage(annotationAttachment.annotPkgId)) {
                    continue;
                }

                Object strandAnnot = ((BIRNode.BIRConstAnnotationAttachment) annotationAttachment).annotValue.value;
                if (strandAnnot instanceof Map<?, ?> recordValue) {
                    if (recordValue.containsKey(STRAND_THREAD)) {
                        BIRNode.ConstValue constValue = (BIRNode.ConstValue) recordValue.get(STRAND_THREAD);
                        if (STRAND_VALUE_ANY.equals(constValue.value)) {
                            isIsolated = true;
                        }
                    }

                    if (recordValue.containsKey(STRAND_NAME)) {
                        BIRNode.ConstValue constValue = (BIRNode.ConstValue) recordValue.get(STRAND_THREAD);
                        strandName = constValue.toString();

                    }

                    if (recordValue.containsKey(STRAND_POLICY_NAME)) {
                        BIRNode.ConstValue constValue = (BIRNode.ConstValue) recordValue.get(STRAND_THREAD);
                        if (!DEFAULT_STRAND_DISPATCHER.equals(constValue.value)) {
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
        this.mv.visitLdcInsn(Objects.requireNonNullElseGet(workerName,
                () -> Objects.requireNonNullElse(callIns.lhsOp.variableDcl.metaVarName, DEFAULT_STRAND_NAME)));
        this.submitToScheduler(callIns.lhsOp, attachedType, parentFunction, isIsolated, callIns, hasWorkers,
                channelMapVarIndex);
    }

    private void genAsyncCallArgs(BIRTerminator.AsyncCall callIns) {
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
            // Add to the rest params array
            jvmCastGen.addBoxInsn(this.mv, arg.variableDcl.type);
            this.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }
    }

    private void generateWaitIns(BIRTerminator.Wait waitInst, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        if (waitInst.exprList.size() ==  1) {
            BIROperand futureVal = waitInst.exprList.getFirst();
            this.loadVar(futureVal.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, ASYNC_UTILS, "handleWait", HANDLE_WAIT, false);
        } else {
            this.mv.visitTypeInsn(NEW, ARRAY_LIST);
            this.mv.visitInsn(DUP);
            this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, VOID_METHOD_DESC, false);

            int i = 0;
            while (i < waitInst.exprList.size()) {
                this.mv.visitInsn(DUP);
                BIROperand futureVal = waitInst.exprList.get(i);
                this.loadVar(futureVal.variableDcl);
                this.mv.visitMethodInsn(INVOKEINTERFACE, LIST, ADD_METHOD, ANY_TO_JBOOLEAN, true);
                this.mv.visitInsn(POP);
                i += 1;
            }
            this.mv.visitMethodInsn(INVOKESTATIC, ASYNC_UTILS, "handleWaitAny", HANDLE_WAIT_ANY, false);
        }
        // assign result
        jvmCastGen.addUnboxInsn(this.mv, waitInst.lhsOp.variableDcl.type);
        this.storeToVar(waitInst.lhsOp.variableDcl);
    }

    private void genWaitAllIns(BIRTerminator.WaitAll waitAll, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitTypeInsn(NEW, HASH_MAP);
        this.mv.visitInsn(DUP);
        this.mv.visitMethodInsn(INVOKESPECIAL, HASH_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
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
        this.mv.visitMethodInsn(INVOKESTATIC, ASYNC_UTILS, "handleWaitMultiple", HANDLE_WAIT_MULTIPLE, false);
    }

    private void genFPCallIns(BIRTerminator.FPCall fpCall, BType attachedType, String funcName, int localVarOffset,
                              boolean hasWorkers, int channelMapVarIndex) {

        if (fpCall.isAsync) {
            // Load the scheduler from strand
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            this.mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", GET_SCHEDULER);
        } else {
            // load function ref, going to directly call the fp
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitFieldInsn(GETFIELD, FUNCTION_POINTER, "function", GET_FUNCTION);
        }

        // if async, we submit this to scheduler (worker scenario)
        if (fpCall.isAsync) {
            String workerName = fpCall.lhsOp.variableDcl.metaVarName;
            boolean isIsolated = false;
            // check for isIsolated annotation
            for (BIRNode.BIRAnnotationAttachment annotationAttachment : fpCall.annotAttachments) {
                Object strandAnnot = ((BIRNode.BIRConstAnnotationAttachment) annotationAttachment).annotValue.value;
                if (strandAnnot instanceof Map<?, ?> recordValue) {
                    if (recordValue.containsKey(STRAND_THREAD)) {
                        BIRNode.ConstValue constValue = (BIRNode.ConstValue) recordValue.get(STRAND_THREAD);
                        if (STRAND_VALUE_ANY.equals(constValue.value)) {
                            isIsolated = true;
                        }
                    }
                }
                break;
            }
            // load function ref now
            this.loadVar(fpCall.fp.variableDcl);
            this.mv.visitVarInsn(ALOAD, localVarOffset);
            loadFpReturnType(fpCall.lhsOp);
            if (workerName == null) {
                throw new RuntimeException("worker name null");
            } else {
                this.mv.visitLdcInsn(workerName);
            }
            this.submitToScheduler(fpCall.lhsOp, attachedType, funcName, isIsolated, fpCall, localVarOffset,
                    hasWorkers, channelMapVarIndex);
        } else {
            this.genFpCallArgs(fpCall, localVarOffset);
            this.mv.visitMethodInsn(INVOKEINTERFACE, FUNCTION, "apply", PASS_OBJECT_RETURN_OBJECT, true);
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

    private void genFpCallArgs(BIRTerminator.FPCall fpCall, int localVarOffset) {
        int argCount = fpCall.args.size() + 1;

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
    }

    private void genWorkerSendIns(BIRTerminator.WorkerSend ins, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitLdcInsn(ins.channel.value);
        this.loadVar(ins.data.variableDcl);
        this.jvmCastGen.addBoxInsn(this.mv, ins.data.variableDcl.type);
        if (ins.isSync) {
            this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "syncSend", SEND_DATA, false);
        } else {
            this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "asyncSend", SEND_DATA, false);
        }
    }

    private void genWorkerAlternateReceiveIns(BIRTerminator.WorkerAlternateReceive ins, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        int channelSize = ins.channels.size();
        this.mv.visitIntInsn(BIPUSH, channelSize);
        this.mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
        int count  = 0;
        for (String channel : ins.channels) {
            this.mv.visitInsn(DUP);
            this.mv.visitIntInsn(BIPUSH, count++);
            this.mv.visitLdcInsn(channel);
            this.mv.visitInsn(AASTORE);
        }
        this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "alternateReceive", ALT_RECEIVE_CALL, false);
        generateReceiveResultStore(ins.lhsOp);
    }


    private void genWorkerMultipleReceiveIns(BIRTerminator.WorkerMultipleReceive ins, int localVarOffset) {

        BIRNode.BIRVariableDcl mapVar = new BIRNode.BIRVariableDcl(symbolTable.anyType,
                new Name(WORKER_CHANNEL_NAMES_MAP), VarScope.FUNCTION, VarKind.LOCAL);
        int mapVarIndex = this.getJVMIndexOfVarRef(mapVar);
        this.mv.visitTypeInsn(NEW, HASH_MAP);
        this.mv.visitInsn(DUP);
        this.mv.visitMethodInsn(INVOKESPECIAL, HASH_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        this.mv.visitVarInsn(ASTORE, mapVarIndex);

        for (BIRTerminator.WorkerMultipleReceive.ReceiveField receiveField : ins.receiveFields) {
            this.mv.visitVarInsn(ALOAD, mapVarIndex);
            this.mv.visitLdcInsn(receiveField.key());
            this.mv.visitLdcInsn(receiveField.workerReceive());
            this.mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", MAP_PUT, true);
            this.mv.visitInsn(POP);
        }

        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitVarInsn(ALOAD, mapVarIndex);
        jvmTypeGen.loadType(this.mv, ins.targetType);
        this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "multipleReceive", MULTIPLE_RECEIVE_CALL, false);
        generateReceiveResultStore(ins.lhsOp);
    }

    private void genWorkerReceiveIns(BIRTerminator.WorkerReceive ins, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        this.mv.visitLdcInsn(ins.workerName.value);
        this.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "receive", RECEIVE_DATA, false);
        generateReceiveResultStore(ins.lhsOp);
    }

    private void generateReceiveResultStore(BIROperand ins) {
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
        jvmCastGen.addUnboxInsn(this.mv, ins.variableDcl.type);
        this.storeToVar(ins.variableDcl);
        this.mv.visitLabel(jumpAfterReceive);
    }

    private void genFlushIns(BIRTerminator.Flush ins, int localVarOffset) {
        this.mv.visitVarInsn(ALOAD, localVarOffset);
        int channelSize = ins.channels.length;
        this.mv.visitIntInsn(BIPUSH, channelSize);
        this.mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
        int count  = 0;
        for (BIRNode.ChannelDetails channelDetails : ins.channels) {
            this.mv.visitInsn(DUP);
            this.mv.visitIntInsn(BIPUSH, count++);
            this.mv.visitLdcInsn(channelDetails.name);
            this.mv.visitInsn(AASTORE);
        }
        this.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_UTILS, "flush", HANDLE_FLUSH, false);
        this.storeToVar(ins.lhsOp.variableDcl);
    }

    private void submitToScheduler(BIROperand lhsOp, BType attachedType, String parentFunction, boolean isIsolated,
                                   BIRTerminator.AsyncCall callIns, boolean hasWorkers, int channelMapVarIndex) {
        this.genStrandMetadataForCall(attachedType, parentFunction);
        this.genWorkerChannelMapForCall(hasWorkers, channelMapVarIndex);
        this.genAsyncCallArgs(callIns);
        this.genStartFunctionCall(lhsOp, isIsolated);
    }

    private void submitToScheduler(BIROperand lhsOp, BType attachedType, String parentFunction, boolean isIsolated,
                                   BIRTerminator.FPCall fpCall, int localVarOffset,
                                   boolean hasWorkers, int channelMapVarIndex) {
        this.genStrandMetadataForCall(attachedType, parentFunction);
        this.genWorkerChannelMapForCall(hasWorkers, channelMapVarIndex);
        this.genFpCallArgs(fpCall, localVarOffset);
        this.genStartFunctionCall(lhsOp, isIsolated);
    }

    private void genStartFunctionCall(BIROperand lhsOp, boolean isIsolated) {
        if (isIsolated) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, START_ISOLATED_WORKER, SCHEDULE_CALL, false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, START_NON_ISOLATED_WORKER, SCHEDULE_CALL, false);
        }
        // store return
        if (lhsOp.variableDcl != null) {
            BIRNode.BIRVariableDcl lhsOpVarDcl = lhsOp.variableDcl;
            // store the returned strand as the future
            this.storeToVar(lhsOpVarDcl);
        }
    }

    private void genStrandMetadataForCall(BType attachedType, String parentFunction) {
        String metaDataVarName;
        if (attachedType != null) {
            metaDataVarName = setAndGetStrandMetadataVarName(attachedType.tsymbol.name.value, parentFunction,
                    asyncDataCollector);
        } else {
            metaDataVarName = JvmCodeGenUtil.setAndGetStrandMetadataVarName(parentFunction, asyncDataCollector);
        }
        this.mv.visitFieldInsn(GETSTATIC, this.strandMetadataClass, metaDataVarName, GET_STRAND_METADATA);
    }

    private void genWorkerChannelMapForCall(boolean hasWorkers, int channelMapVarIndex) {
        if (hasWorkers) {
            mv.visitVarInsn(ALOAD, channelMapVarIndex);
        } else {
            mv.visitInsn(ACONST_NULL);
        }
    }


    private String setAndGetStrandMetadataVarName(String typeName, String parentFunction,
                                         AsyncDataCollector asyncDataCollector) {
        String metaDataVarName = STRAND_METADATA_VAR_PREFIX + typeName + "$" + parentFunction + "$";
        asyncDataCollector.getStrandMetadata().putIfAbsent(metaDataVarName,
                new ScheduleFunctionInfo(typeName, parentFunction));
        return metaDataVarName;
    }

    private void loadFpReturnType(BIROperand lhsOp) {
        BType futureType = JvmCodeGenUtil.getImpliedType(lhsOp.variableDcl.type);
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

    private void genResourcePathArgs(List<BIROperand> pathArgs) {
        int pathVarArrayIndex = this.indexMap.addIfNotExists("$pathVarArray", symbolTable.anyType);
        int bundledArrayIndex = this.indexMap.addIfNotExists("$pathArrayArgs", symbolTable.anyType);
        genBundledArgs(pathArgs, pathVarArrayIndex, bundledArrayIndex, TYPE_ANYDATA_ARRAY, true);
    }

    private void genBundledFunctionArgs(List<BIROperand> args) {
        int functionArgArrayIndex = this.indexMap.addIfNotExists("$functionArgArray", symbolTable.anyType);
        int bundledArrayIndex = this.indexMap.addIfNotExists("$functionArrayArgs", symbolTable.anyType);
        genBundledArgs(args, functionArgArrayIndex, bundledArrayIndex, TYPE_ANY_ARRAY, false);
    }

    private void genBundledArgs(List<BIROperand> args, int argsArrayIndex, int bundledArrayIndex, String fieldName,
                                boolean isFromPathArgs) {
        mv.visitLdcInsn((long) args.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitVarInsn(ASTORE, argsArrayIndex);

        int i = 0;
        for (BIROperand arg : args) {
            mv.visitVarInsn(ALOAD, argsArrayIndex);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            this.loadVar(arg.variableDcl);
            if (isFromPathArgs) {
                // Add CheckCast instruction for path args.
                jvmCastGen.generateCheckCastToAnyData(mv, arg.variableDcl.type);
            } else {
                // Add Box instruction if the type is a value type.
                jvmCastGen.addBoxInsn(mv, arg.variableDcl.type);
            }
            mv.visitInsn(AASTORE);
            i++;
        }
        mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, argsArrayIndex);
        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, fieldName, LOAD_ARRAY_TYPE);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, JVM_INIT_METHOD, INIT_ANYDATA_ARRAY, false);
        mv.visitVarInsn(ASTORE, bundledArrayIndex);
        mv.visitVarInsn(ALOAD, bundledArrayIndex);
    }

    private void generateReturnTermFromType(int returnVarRefIndex, BType bType, BIRNode.BIRFunction func) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
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
            case TypeTags.NIL, TypeTags.NEVER, TypeTags.MAP, TypeTags.ARRAY, TypeTags.ANY, TypeTags.STREAM,
                 TypeTags.TABLE, TypeTags.ANYDATA, TypeTags.OBJECT, TypeTags.DECIMAL, TypeTags.RECORD,
                 TypeTags.TUPLE, TypeTags.JSON, TypeTags.FUTURE, TypeTags.INVOKABLE, TypeTags.HANDLE,
                 TypeTags.FINITE, TypeTags.TYPEDESC, TypeTags.READONLY, TypeTags.UNION, TypeTags.ERROR -> {
                this.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                this.mv.visitInsn(ARETURN);
            }
            case TypeTags.BYTE, TypeTags.BOOLEAN -> {
                this.mv.visitVarInsn(ILOAD, returnVarRefIndex);
                this.mv.visitInsn(IRETURN);
            }
            case TypeTags.FLOAT -> {
                this.mv.visitVarInsn(DLOAD, returnVarRefIndex);
                this.mv.visitInsn(DRETURN);
            }
            default -> throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    func.type.retType);
        }
    }

}
