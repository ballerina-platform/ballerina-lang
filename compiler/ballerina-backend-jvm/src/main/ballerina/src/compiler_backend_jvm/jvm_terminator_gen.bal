// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/bir;
import ballerina/io;
import ballerina/jvm;
import ballerina/stringutils;

type TerminatorGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    LabelGenerator labelGen;
    ErrorHandlerGenerator errorGen;
    bir:Package module;
    string currentPackageName;

    function init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen,
                            ErrorHandlerGenerator errorGen, bir:Package module) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.labelGen = labelGen;
        self.errorGen = errorGen;
        self.module = module;
        self.currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
    }

    function genTerminator(bir:Terminator terminator, bir:Function func, string funcName,
                           int localVarOffset, int returnVarRefIndex, bir:BType? attachedType, boolean isObserved = false) {
        if (terminator is bir:Lock) {
            self.genLockTerm(terminator, funcName, localVarOffset);
        } else if (terminator is bir:Unlock) {
            self.genUnlockTerm(terminator, funcName, attachedType);
        } else if (terminator is bir:GOTO) {
            self.genGoToTerm(terminator, funcName);
        } else if (terminator is bir:Call) {
            self.genCallTerm(terminator, funcName, localVarOffset);
        } else if (terminator is bir:AsyncCall) {
            self.genAsyncCallTerm(terminator, localVarOffset);
        } else if (terminator is bir:Branch) {
            self.genBranchTerm(terminator, funcName);
        } else if (terminator is bir:Return) {
            self.genReturnTerm(terminator, returnVarRefIndex, func, isObserved, localVarOffset);
        } else if (terminator is bir:Panic) {
            self.errorGen.genPanic(terminator);
        } else if (terminator is bir:Wait) {
            self.generateWaitIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:WaitAll) {
            self.genWaitAllIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:FPCall) {
            self.genFPCallIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:WorkerSend) {
            self.genWorkerSendIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:WorkerReceive) {
            self.genWorkerReceiveIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:Flush) {
            self.genFlushIns(terminator, funcName, localVarOffset);
        } else if (terminator is JavaMethodCall) {
            self.genJCallTerm(terminator, funcName, attachedType, localVarOffset);
        } else if (terminator is JIMethodCall) {
            self.genJICallTerm(terminator, funcName, attachedType, localVarOffset);
        } else if (terminator is JIConstructorCall) {
            self.genJIConstructorTerm(terminator, funcName, attachedType, localVarOffset);
        } else {
            error err = error( "JVM generation is not supported for terminator instruction " +
                io:sprintf("%s", terminator));
            panic err;
        }
    }

    function genGoToTerm(bir:GOTO gotoIns, string funcName) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genLockTerm(bir:Lock lockIns, string funcName, int localVarOffset) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + lockIns.lockBB.id.value);
        string lockStore = "L" + LOCK_STORE + ";";
        string initClassName = lookupGlobalVarClassName(self.currentPackageName + "LOCK_STORE");
        self.mv.visitFieldInsn(GETSTATIC, initClassName, "LOCK_STORE", lockStore);
        self.mv.visitLdcInsn("global");
        self.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", io:sprintf("(L%s;)L%s;", STRING_VALUE,
            LOCK_VALUE), false);
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "lock", io:sprintf("(L%s;)Z", STRAND), false);
        self.mv.visitInsn(POP);
        genYieldCheckForLock(self.mv, self.labelGen, funcName, localVarOffset);

        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genUnlockTerm(bir:Unlock unlockIns, string funcName, bir:BType? attachedType) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + unlockIns.unlockBB.id.value);

        // unlocked in the same order https://yarchive.net/comp/linux/lock_ordering.html
        string lockStore = "L" + LOCK_STORE + ";";
        string initClassName = lookupGlobalVarClassName(self.currentPackageName + "LOCK_STORE");
        self.mv.visitFieldInsn(GETSTATIC, initClassName, "LOCK_STORE", lockStore);
        self.mv.visitLdcInsn("global");
        self.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_STORE, "getLockFromMap", io:sprintf("(L%s;)L%s;", STRING_VALUE,
            LOCK_VALUE), false);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, LOCK_VALUE, "unlock", "()V", false);

        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    int kl = 0;
    function genReturnTerm(bir:Return returnIns, int returnVarRefIndex, bir:Function func,
                            boolean isObserved = false, int localVarOffset = -1) {
        if (isObserved) {
            emitStopObservationInvocation(self.mv, localVarOffset);
        }
        bir:BType bType = <bir:BType> func.typeValue?.retType;
        if (bType is bir:BTypeNil) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LLOAD, returnVarRefIndex);
            self.mv.visitInsn(LRETURN);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitVarInsn(ILOAD, returnVarRefIndex);
            self.mv.visitInsn(IRETURN);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DLOAD, returnVarRefIndex);
            self.mv.visitInsn(DRETURN);
        } else if (bType is bir:BTypeString) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, returnVarRefIndex);
            self.mv.visitInsn(IRETURN);
        } else if (bType is bir:BMapType ||
                bType is bir:BArrayType ||
                bType is bir:BTypeAny ||
                bType is bir:BTableType ||
                bType is bir:BStreamType ||
                bType is bir:BTypeAnyData ||
                bType is bir:BObjectType ||
                bType is bir:BServiceType ||
                bType is bir:BTypeDecimal ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BJSONType ||
                bType is bir:BFutureType ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
                bType is bir:BTypeHandle ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeDesc) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BUnionType) {
            self.handleErrorRetInUnion(returnVarRefIndex, func.workerChannels, bType);
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BErrorType) {
            self.notifyChannels(func.workerChannels, returnVarRefIndex);
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", func.typeValue?.retType));
            panic err;
        }
    }

    function handleErrorRetInUnion(int returnVarRefIndex, bir:ChannelDetail[] channels, bir:BUnionType bType) {
        if (channels.length() == 0) {
            return;
        }

        boolean errorIncluded = false;
        foreach var member in bType.members {
            if (member is bir:BErrorType) {
                errorIncluded = true;
                break;
            }
        }

        if (errorIncluded) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitVarInsn(ALOAD, 0);
            loadChannelDetails(self.mv, channels);
            self.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "handleWorkerError",
                io:sprintf("(L%s;L%s;[L%s;)V", REF_VALUE, STRAND, CHANNEL_DETAILS), false);
        }
    }

    function notifyChannels(bir:ChannelDetail[] channels, int retIndex) {
        if (channels.length() == 0) {
            return;
        }

        self.mv.visitVarInsn(ALOAD, 0);
        loadChannelDetails(self.mv, channels);
        self.mv.visitVarInsn(ALOAD, retIndex);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleChannelError", io:sprintf("([L%s;L%s;)V",
            CHANNEL_DETAILS, ERROR_VALUE), false);
    }

    function genBranchTerm(bir:Branch branchIns, string funcName) {
        string trueBBId = branchIns.trueBB.id.value;
        string falseBBId = branchIns.falseBB.id.value;

        self.loadVar(branchIns.op.variableDcl);

        jvm:Label trueBBLabel = self.labelGen.getLabel(funcName + trueBBId);
        self.mv.visitJumpInsn(IFGT, trueBBLabel);

        jvm:Label falseBBLabel = self.labelGen.getLabel(funcName + falseBBId);
        self.mv.visitJumpInsn(GOTO, falseBBLabel);
    }

    function genCallTerm(bir:Call callIns, string funcName, int localVarOffset) {
        string orgName = callIns.pkgID.org;
        string moduleName = callIns.pkgID.name;
        var callInsCopy = callIns.clone();
        if(isBStringFunc(funcName)) {
            callInsCopy.name.value =  nameOfBStringFunc(callIns.name.value);
        }
        // invoke the function
        self.genCall(callInsCopy, orgName, moduleName, localVarOffset);

        // store return
        self.storeReturnFromCallIns(callIns.lhsOp?.variableDcl);
    }

    function genJCallTerm(JavaMethodCall callIns, string funcName, bir:BType? attachedType, int localVarOffset) {
        // Load function parameters of the target Java method to the stack..
        jvm:Label blockedOnExternLabel = new;
        jvm:Label notBlockedOnExternLabel = new;

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
        self.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitInsn(ICONST_0);
        self.mv.visitFieldInsn(PUTFIELD, "io/ballerina/runtime/scheduling/Strand", "blockedOnExtern", "Z");

        if (callIns.lhsOp?.variableDcl is bir:VariableDcl) {
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "returnValue", "Ljava/lang/Object;");
            addUnboxInsn(self.mv, callIns.lhsOp?.typeValue); // store return
            bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp?.variableDcl;

            if (lhsOpVarDcl is bir:VariableDcl) {
                self.storeToVar(lhsOpVarDcl);
            }
        }

        self.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

        self.mv.visitLabel(blockedOnExternLabel);

        int argIndex = 0;
        if attachedType is () {
            self.mv.visitVarInsn(ALOAD, localVarOffset);
        } else {
            // Below codes are not needed (as normal external funcs doesn't support attached invocations)
            // check whether function params already include the self
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            bir:VariableDcl selfArg = getVariableDcl(callIns.args[0]?.variableDcl);
            self.loadVar(selfArg);
            self.mv.visitTypeInsn(CHECKCAST, OBJECT_VALUE);
            argIndex += 1;
        }

        int argsCount = callIns.args.length();
        while (argIndex < argsCount) {
            bir:VarRef? arg = callIns.args[argIndex];
            _ = self.visitArg(arg);
            argIndex += 1;
        }

        string jClassName = callIns.jClassName;
        string jMethodName = callIns.name;
        string jMethodVMSig = isBStringFunc(funcName) ? callIns.jMethodVMSigBString : callIns.jMethodVMSig;
        self.mv.visitMethodInsn(INVOKESTATIC, jClassName, jMethodName, jMethodVMSig, false);

        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp?.variableDcl;

        if (lhsOpVarDcl is bir:VariableDcl) {
            self.storeToVar(lhsOpVarDcl);
        }

        self.mv.visitLabel(notBlockedOnExternLabel);
    }

    function genJICallTerm(JIMethodCall callIns, string funcName, bir:BType? attachedType, int localVarOffset) {
        // Load function parameters of the target Java method to the stack..
        jvm:Label blockedOnExternLabel = new;
        jvm:Label notBlockedOnExternLabel = new;

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
        self.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitInsn(ICONST_0);
        self.mv.visitFieldInsn(PUTFIELD, "io/ballerina/runtime/scheduling/Strand", "blockedOnExtern", "Z");

        if (callIns.lhsOp?.variableDcl is bir:VariableDcl) {
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "returnValue", "Ljava/lang/Object;");
            addJUnboxInsn(self.mv, callIns.lhsOp?.typeValue);
            // store return
            bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp?.variableDcl;

            if (lhsOpVarDcl is bir:VariableDcl) {
                self.storeToVar(lhsOpVarDcl);
            }
        }

        self.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

        self.mv.visitLabel(blockedOnExternLabel);
        boolean isInterface = callIns.invocationType == INVOKEINTERFACE;

        int argIndex = 0;
        if (callIns.invocationType == INVOKEVIRTUAL || isInterface) {
            // check whether function params already include the self
            bir:VariableDcl selfArg = getVariableDcl(callIns.args[0]?.variableDcl);
            self.loadVar(selfArg);
            self.mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
            self.mv.visitTypeInsn(CHECKCAST, callIns.jClassName);

            jvm:Label ifNonNullLabel = self.labelGen.getLabel("receiver_null_check");
            self.mv.visitLabel(ifNonNullLabel);
            self.mv.visitInsn(DUP);

            jvm:Label elseBlockLabel = self.labelGen.getLabel("receiver_null_check_else");
            self.mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
            jvm:Label thenBlockLabel = self.labelGen.getLabel("receiver_null_check_then");
            self.mv.visitLabel(thenBlockLabel);
            self.mv.visitFieldInsn(GETSTATIC, BAL_ERROR_REASONS, "JAVA_NULL_REFERENCE_ERROR", "L" + STRING_VALUE + ";");
            self.mv.visitFieldInsn(GETSTATIC, RUNTIME_ERRORS, "JAVA_NULL_REFERENCE", "L" + RUNTIME_ERRORS + ";");
            self.mv.visitInsn(ICONST_0);
            self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
            self.mv.visitMethodInsn(INVOKESTATIC, BLANG_EXCEPTION_HELPER, "getRuntimeException",
                "(L" + STRING_VALUE + ";L" + RUNTIME_ERRORS + ";[L" + OBJECT + ";)L" + ERROR_VALUE + ";", false);
            self.mv.visitInsn(ATHROW);
            self.mv.visitLabel(elseBlockLabel);
            argIndex += 1;
        }

        int argsCount = callIns.varArgExist ? callIns.args.length() - 1 : callIns.args.length();
        while (argIndex < argsCount) {
            bir:VarRef? arg = callIns.args[argIndex];
            _ = self.visitArg(arg);
            argIndex += 1;
        }
        if (callIns.varArgExist) {
            bir:VarRef arg = <bir:VarRef>callIns.args[argIndex];
            int localVarIndex = self.indexMap.getIndex(arg.variableDcl);
            genVarArg(self.mv, self.indexMap, arg.typeValue, <jvm:JType>callIns.varArgType, localVarIndex);
        }

        string jClassName = callIns.jClassName;
        string jMethodName = callIns.name;
        string jMethodVMSig = callIns.jMethodVMSig;
        self.mv.visitMethodInsn(callIns.invocationType, jClassName, jMethodName, jMethodVMSig, isInterface);

        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp?.variableDcl;

        if (lhsOpVarDcl is bir:VariableDcl) {
            self.storeToVar(lhsOpVarDcl);
        }

        self.mv.visitLabel(notBlockedOnExternLabel);
    }

    function genJIConstructorTerm(JIConstructorCall callIns, string funcName, bir:BType? attachedType, int localVarOffset) {
        // Load function parameters of the target Java method to the stack..
        jvm:Label blockedOnExternLabel = new;
        jvm:Label notBlockedOnExternLabel = new;

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
        self.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitInsn(ICONST_0);
        self.mv.visitFieldInsn(PUTFIELD, "io/ballerina/runtime/scheduling/Strand", "blockedOnExtern", "Z");

        if (callIns.lhsOp?.variableDcl is bir:VariableDcl) {
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "returnValue", "Ljava/lang/Object;");
            addUnboxInsn(self.mv, callIns.lhsOp?.typeValue);
            // store return
            bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp?.variableDcl;

            if (lhsOpVarDcl is bir:VariableDcl) {
                self.storeToVar(lhsOpVarDcl);
            }
        }

        self.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

        self.mv.visitLabel(blockedOnExternLabel);

        self.mv.visitTypeInsn(NEW, callIns.jClassName);
        self.mv.visitInsn(DUP);

        int argIndex = 0;

        int argsCount = callIns.args.length();
        while (argIndex < argsCount) {
            bir:VarRef? arg = callIns.args[argIndex];
            _ = self.visitArg(arg);
            argIndex += 1;
        }

        string jClassName = callIns.jClassName;
        string jMethodName = callIns.name;
        string jMethodVMSig = callIns.jMethodVMSig;
        self.mv.visitMethodInsn(INVOKESPECIAL, jClassName, jMethodName, jMethodVMSig, false);

        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp?.variableDcl;

        if (lhsOpVarDcl is bir:VariableDcl) {
            self.storeToVar(lhsOpVarDcl);
        }

        self.mv.visitLabel(notBlockedOnExternLabel);
    }

    private function cleanupVariableDecl(bir:VariableDcl? varDecl) returns bir:VariableDcl {
        if(varDecl is bir:VariableDcl) {
            return varDecl;
        }
        error err = error("Invalid variable declaration");
        panic err;
    }

    private function cleanupLocalLock(bir:LocalLocks? localLock) returns bir:LocalLocks {
        if(localLock is bir:LocalLocks) {
            return localLock;
        }
        error err = error("Invalid lock variable detail");
        panic err;
    }

    private function storeReturnFromCallIns(bir:VariableDcl? lhsOpVarDcl) {
        if (lhsOpVarDcl is bir:VariableDcl) {
            self.storeToVar(lhsOpVarDcl);
        } else {
            self.mv.visitInsn(POP);
        }
    }

    private function genCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        if (!callIns.isVirtual) {
            self.genFuncCall(callIns, orgName, moduleName, localVarOffset);
            return;
        }

        bir:VariableDcl selfArg = getVariableDcl(callIns.args[0]?.variableDcl);
        if (selfArg.typeValue is bir:BObjectType || selfArg.typeValue is bir:BServiceType) {
            self.genVirtualCall(callIns, orgName, moduleName, localVarOffset);
        } else {
            // then this is a function attached to a built-in type
            self.genBuiltinTypeAttachedFuncCall(callIns, orgName, moduleName, localVarOffset);
        }
    }

    private function genFuncCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        string methodName = callIns.name.value;
        self.genStaticCall(callIns, orgName, moduleName, localVarOffset, methodName, methodName);
    }

    private function genBuiltinTypeAttachedFuncCall(bir:Call callIns, string orgName, string moduleName,
                                                    int localVarOffset) {
        string methodLookupName = callIns.name.value;
        int? optionalIndex = methodLookupName.indexOf(".");
        int index = optionalIndex is int ? optionalIndex + 1 : 0;
        string methodName = methodLookupName.substring(index, methodLookupName.length());
        self.genStaticCall(callIns, orgName, moduleName, localVarOffset, methodName, methodLookupName);
    }

    private function genStaticCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset,
                                   string methodName, string methodLookupName) {
        // load strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        string lookupKey = nameOfNonBStringFunc(getPackageName(orgName, moduleName) + methodLookupName);

        int argsCount = callIns.args.length();
        int i = 0;
        while (i < argsCount) {
            bir:VarRef? arg = callIns.args[i];
            boolean userProvidedArg = self.visitArg(arg);
            self.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            i += 1;
        }

        string jvmClass = lookupFullQualifiedClassName(lookupKey);
        string cleanMethodName = cleanupFunctionName(methodName);
        boolean useBString = IS_BSTRING && orgName == "ballerina" &&
                             (moduleName == "lang.string" || moduleName == "lang.error" || moduleName == "lang.value" 
                             || moduleName == "lang.map") && !cleanMethodName.endsWith("_");
        if (useBString) {
            cleanMethodName = nameOfBStringFunc(cleanMethodName);
        }
        string methodDesc = lookupJavaMethodDescription(lookupKey, useBString);
        self.mv.visitMethodInsn(INVOKESTATIC, jvmClass, cleanMethodName, methodDesc, false);
    }

    private function genVirtualCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        // load self
        bir:VariableDcl selfArg = getVariableDcl(callIns.args[0]?.variableDcl);
        self.loadVar(selfArg);
        self.mv.visitTypeInsn(CHECKCAST, OBJECT_VALUE);

        // load the strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);

        // load the function name as the second argument
        self.mv.visitLdcInsn(cleanupObjectTypeName(callIns.name.value));

        // create an Object[] for the rest params
        int argsCount = callIns.args.length() - 1;
        // arg count doubled and 'isExist' boolean variables added for each arg.
        self.mv.visitLdcInsn(argsCount * 2);
        self.mv.visitInsn(L2I);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int i = 0;
        int j = 0;
        while (i < argsCount) {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(j);
            self.mv.visitInsn(L2I);
            j += 1;
            // i + 1 is used since we skip the first argument (self)
            bir:VarRef? arg = callIns.args[i + 1];
            boolean userProvidedArg = self.visitArg(arg);

            // Add the to the rest params array
            addBoxInsn(self.mv, arg?.typeValue);
            self.mv.visitInsn(AASTORE);

            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(j);
            self.mv.visitInsn(L2I);
            j += 1;

            self.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            addBoxInsn(self.mv, "boolean");
            self.mv.visitInsn(AASTORE);

            i += 1;
        }

        // call method
        string methodDesc = io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);

        bir:BType? returnType = callIns.lhsOp?.typeValue;
        addUnboxInsn(self.mv, returnType);
    }

    function loadBooleanArgToIndicateUserProvidedArg(string orgName, string moduleName, boolean userProvided) {
        if isBallerinaBuiltinModule(orgName, moduleName) {
            return;
        }
         // Extra boolean is not gen for extern functions for now until the wrapper function is implemented.
        // We need to refactor this method. I am not sure whether userProvided flag make sense
        if (userProvided) {
            self.mv.visitInsn(ICONST_1);
        } else {
            self.mv.visitInsn(ICONST_0);
        }
    }

    function visitArg(bir:VarRef? arg) returns boolean {
        bir:VarRef argRef = getVarRef(arg);
        if (argRef.variableDcl.name.value.startsWith("_")) {
            loadDefaultValue(self.mv, getVarRef(arg).typeValue);
            return false;
        }

        bir:VariableDcl varDcl = getVariableDcl(argRef.variableDcl);
        self.loadVar(varDcl);
        return true;
    }

    function genAsyncCallTerm(bir:AsyncCall callIns, int localVarOffset) {
        string orgName = callIns.pkgID.org;
        string moduleName = callIns.pkgID.name;
        // Load the scheduler from strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));

        // create an Object[] for the rest params
        int argsCount = callIns.args.length();
        //create an object array of args
        self.mv.visitLdcInsn(argsCount * 2 + 1);
        self.mv.visitInsn(L2I);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int paramIndex = 1;
        foreach var arg in callIns.args {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(paramIndex);
            self.mv.visitInsn(L2I);

            boolean userProvidedArg = self.visitArg(arg);
            // Add the to the rest params array
            addBoxInsn(self.mv, arg?.typeValue);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;

            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(paramIndex);
            self.mv.visitInsn(L2I);

            self.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            addBoxInsn(self.mv, "boolean");
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }
        string funcName = callIns.name.value;
        string lambdaName = "$" + funcName + "$lambda$" + lambdaIndex.toString() + "$";
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);

        bir:BType? futureType = callIns.lhsOp?.typeValue;
        bir:BType returnType = bir:TYPE_NIL;
        if (futureType is bir:BFutureType) {
            returnType = futureType.returnType;
        }

        createFunctionPointer(self.mv, currentClass, lambdaName, 0);
        lambdas[lambdaName] = callIns;
        lambdaIndex += 1;

        boolean concurrent = false;
        // check for concurrent annotation
        if (callIns.annotAttachments.length() > 0 ) {
            foreach var v in callIns.annotAttachments {                
                if (v is bir:AnnotationAttachment && 
                    v.annotTagRef.value == "strand" && 
                    v.moduleId.org == "ballerina" && 
                    v.moduleId.name == "lang.annotations") {
                        if (v.annotValues.length() > 0) {
                            bir:AnnotationValue val = <bir:AnnotationValue> v.annotValues[0];
                            if (val is bir:AnnotationRecordValue) {
                                if (val.annotValueMap.hasKey("thread") && 
                                    val.annotValueMap.get("thread")["literalValue"] == "any") {
                                        concurrent = true;
                                }

                                if (val.annotValueMap.hasKey("name") &&
                                    val.annotValueMap.get("name")["literalValue"] != "DEFAULT") {
                                        panic error("Unsupported policy. Only 'DEFAULT' policy is supported by jballerina runtime.");
                                }
                            }
                        }
                    break;
                } 
            }
        }

        self.submitToScheduler(callIns.lhsOp, localVarOffset, concurrent);
    }

    function generateWaitIns(bir:Wait waitInst, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitTypeInsn(NEW, ARRAY_LIST);
        self.mv.visitInsn(DUP);
        self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);

        int i = 0;
        while (i < waitInst.exprList.length()) {
            self.mv.visitInsn(DUP);
            bir:VarRef? futureVal = waitInst.exprList[i];
            if (futureVal is bir:VarRef) {
                self.loadVar(futureVal.variableDcl);
            }
            self.mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", io:sprintf("(L%s;)Z", OBJECT), true);
            self.mv.visitInsn(POP);
            i += 1;
        }

        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleWaitAny", io:sprintf("(L%s;)L%s$WaitResult;", LIST, STRAND), false);
        bir:VariableDcl tempVar = { typeValue: "any",
                                 name: { value: "waitResult" },
                                 kind: "ARG" };
        int resultIndex = self.getJVMIndexOfVarRef(tempVar);
        self.mv.visitVarInsn(ASTORE, resultIndex);

        // assign result if result available
        jvm:Label afterIf = new;
        self.mv.visitVarInsn(ALOAD, resultIndex);
        self.mv.visitFieldInsn(GETFIELD, io:sprintf("%s$WaitResult", STRAND), "done", "Z");
        self.mv.visitJumpInsn(IFEQ, afterIf);
        jvm:Label withinIf = new;
        self.mv.visitLabel(withinIf);
        self.mv.visitVarInsn(ALOAD, resultIndex);
        self.mv.visitFieldInsn(GETFIELD, io:sprintf("%s$WaitResult", STRAND), "result", io:sprintf("L%s;", OBJECT));
        addUnboxInsn(self.mv, waitInst.lhsOp.typeValue);
        self.storeToVar(waitInst.lhsOp.variableDcl);
        self.mv.visitLabel(afterIf);
    }

    function genWaitAllIns(bir:WaitAll waitAll, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitTypeInsn(NEW, "java/util/HashMap");
        self.mv.visitInsn(DUP);
        self.mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
        int i = 0;
        while (i < waitAll.keys.length()) {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(waitAll.keys[i]);
            bir:VarRef? futureRef = waitAll.futures[i];
            if (futureRef is bir:VarRef) {
                self.loadVar(futureRef.variableDcl);
            }
            self.mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), true);
            self.mv.visitInsn(POP);
            i += 1;
        }

        self.loadVar(waitAll.lhsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleWaitMultiple", io:sprintf("(L%s;L%s;)V", MAP, MAP_VALUE), false);
    }

    function genFPCallIns(bir:FPCall fpCall, string funcName, int localVarOffset) {
        if (fpCall.isAsync) {
            // Load the scheduler from strand
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));
        } else {
            // load function ref, going to directly call the fp
            self.loadVar(fpCall.fp.variableDcl);
        }

        // create an object array of args
        self.mv.visitIntInsn(BIPUSH, fpCall.args.length() * 2 + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // load strand
        self.mv.visitInsn(DUP);

        // 0th index
        self.mv.visitIntInsn(BIPUSH, 0);

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitInsn(AASTORE);

        // load args
        int paramIndex = 1;
        foreach var arg in fpCall.args {
            self.mv.visitInsn(DUP);
            self.mv.visitIntInsn(BIPUSH, paramIndex);
            self.loadVar(getVariableDcl(arg?.variableDcl));
            bir:BType? bType = arg?.typeValue;
            addBoxInsn(self.mv, bType);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;

            self.loadTrueValueAsArg(paramIndex);
            paramIndex += 1;
        }

        // if async, we submit this to sceduler (worker scenario)
        bir:BType returnType = fpCall.fp.typeValue;

        if (fpCall.isAsync) {
            // load function ref now
            self.loadVar(fpCall.fp.variableDcl);
            self.mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "isConcurrent", io:sprintf("(L%s;)Z", FUNCTION_POINTER), false);
            jvm:Label notConcurrent = new;
            self.mv.visitJumpInsn(IFEQ, notConcurrent);
            jvm:Label concurrent = new;
            self.mv.visitLabel(concurrent);
            self.loadVar(fpCall.fp.variableDcl);
            self.submitToScheduler(fpCall.lhsOp, localVarOffset, true);
            jvm:Label afterSubmit = new;
            self.mv.visitJumpInsn(GOTO, afterSubmit);
            self.mv.visitLabel(notConcurrent);
            self.loadVar(fpCall.fp.variableDcl);
            self.submitToScheduler(fpCall.lhsOp, localVarOffset, false);
            self.mv.visitLabel(afterSubmit);

        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_POINTER, "call", io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), false);
            // store reult
            bir:BType? lhsType = fpCall.lhsOp?.typeValue;
            if (lhsType is bir:BType) {
                addUnboxInsn(self.mv, lhsType);
            }

            bir:VariableDcl? lhsVar = fpCall.lhsOp?.variableDcl;
            if (lhsVar is bir:VariableDcl) {
                self.storeToVar(lhsVar);
            } else {
                self.mv.visitInsn(POP);
            }
        }
    }

    function loadTrueValueAsArg(int paramIndex) {
        self.mv.visitInsn(DUP);
        self.mv.visitIntInsn(BIPUSH, paramIndex);
        self.mv.visitInsn(ICONST_1);
        addBoxInsn(self.mv, "boolean");
        self.mv.visitInsn(AASTORE);
    }

    function genWorkerSendIns(bir:WorkerSend ins, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            self.mv.visitFieldInsn(GETFIELD, STRAND, "parent", io:sprintf("L%s;", STRAND));
        }
        self.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", io:sprintf("L%s;", WD_CHANNELS));
        self.mv.visitLdcInsn(ins.channelName.value);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", io:sprintf("(L%s;)L%s;",
            STRING_VALUE, WORKER_DATA_CHANNEL), false);
        self.loadVar(ins.dataOp.variableDcl);
        addBoxInsn(self.mv, ins.dataOp.typeValue);
        self.mv.visitVarInsn(ALOAD, localVarOffset);

        if (!ins.isSync) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "sendData", io:sprintf("(L%s;L%s;)V", OBJECT,
                STRAND), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "syncSendData", io:sprintf("(L%s;L%s;)L%s;",
                OBJECT, STRAND, OBJECT), false);
            bir:VarRef? lhsOp = ins.lhsOp;
            if (lhsOp is bir:VarRef) {
                self.storeToVar(lhsOp.variableDcl);
            }
        }
    }

    function genWorkerReceiveIns(bir:WorkerReceive ins, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            self.mv.visitFieldInsn(GETFIELD, STRAND, "parent", io:sprintf("L%s;", STRAND));
        }
        self.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", io:sprintf("L%s;", WD_CHANNELS));
        self.mv.visitLdcInsn(ins.channelName.value);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", io:sprintf("(L%s;)L%s;",
            STRING_VALUE, WORKER_DATA_CHANNEL), false);

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "tryTakeData", io:sprintf("(L%s;)L%s;", STRAND, OBJECT), false);

        bir:VariableDcl tempVar = { typeValue: "any",
                                 name: { value: "wrkMsg" },
                                 kind: "ARG" };
        int wrkResultIndex = self.getJVMIndexOfVarRef(tempVar);
        self.mv.visitVarInsn(ASTORE, wrkResultIndex);

        jvm:Label jumpAfterReceive = new;
        self.mv.visitVarInsn(ALOAD, wrkResultIndex);
        self.mv.visitJumpInsn(IFNULL, jumpAfterReceive);

        jvm:Label withinReceiveSuccess = new;
        self.mv.visitLabel(withinReceiveSuccess);
        self.mv.visitVarInsn(ALOAD, wrkResultIndex);
        addUnboxInsn(self.mv, ins.lhsOp.typeValue);
        self.storeToVar(ins.lhsOp.variableDcl);

        self.mv.visitLabel(jumpAfterReceive);
    }

    function genFlushIns(bir:Flush ins, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        loadChannelDetails(self.mv, ins.workerChannels);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleFlush",
                io:sprintf("([L%s;)L%s;", CHANNEL_DETAILS, ERROR_VALUE), false);
        self.storeToVar(ins.lhsOp.variableDcl);
    }

    function submitToScheduler(bir:VarRef? lhsOp, int localVarOffset, boolean concurrent) {
        bir:BType? futureType = lhsOp?.typeValue;
        bir:BType returnType = "any";
        if (futureType is bir:BFutureType) {
            returnType = futureType.returnType;
        }

        // load strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        loadType(self.mv, returnType);
        if (concurrent) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
                io:sprintf("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_LOCAL_METHOD,
                io:sprintf("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE), false);
        }

        // store return
        if (lhsOp is bir:VarRef) {
            bir:VariableDcl? lhsOpVarDcl = lhsOp.variableDcl;
            // store the returned strand as the future
            self.storeToVar(getVariableDcl(lhsOpVarDcl));
        }
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }

    private function loadVar(bir:VariableDcl varDcl) {
        generateVarLoad(self.mv, varDcl, self.currentPackageName, self.getJVMIndexOfVarRef(varDcl));
    }

    private function storeToVar(bir:VariableDcl varDcl) {
        generateVarStore(self.mv, varDcl, self.currentPackageName, self.getJVMIndexOfVarRef(varDcl));
    }
};

function genYieldCheckForLock(jvm:MethodVisitor mv, LabelGenerator labelGen, string funcName,
                        int localVarOffset) {
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isYielded", "()Z", false);
    jvm:Label yieldLabel = labelGen.getLabel(funcName + "yield");
    mv.visitJumpInsn(IFNE, yieldLabel);
}

function loadChannelDetails(jvm:MethodVisitor mv, bir:ChannelDetail[] channels) {
    mv.visitIntInsn(BIPUSH, channels.length());
    mv.visitTypeInsn(ANEWARRAY, CHANNEL_DETAILS);
    int index = 0;
    foreach bir:ChannelDetail ch in channels {
        // generating array[i] = new ChannelDetails(name, onSameStrand, isSend);
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, index);
        index += 1;

        mv.visitTypeInsn(NEW, CHANNEL_DETAILS);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(ch.name.value);

        if (ch.onSameStrand) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        if (ch.isSend) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        mv.visitMethodInsn(INVOKESPECIAL, CHANNEL_DETAILS, "<init>", io:sprintf("(L%s;ZZ)V", STRING_VALUE),
            false);
        mv.visitInsn(AASTORE);
    }
}


function cleanupObjectTypeName(string typeName) returns string {
    int index = stringutils:lastIndexOf(typeName, ".");
    if (index > 0) {
        return typeName.substring(index + 1, typeName.length());
    } else {
        return typeName;
    }
}

function isExternStaticFunctionCall(bir:Call|bir:AsyncCall|bir:FPLoad callIns) returns boolean {
    string methodName;
    string orgName;
    string moduleName;

    if (callIns is bir:Call) {
        if (callIns.isVirtual) {
            return false;
        }
        methodName = callIns.name.value;
        orgName = callIns.pkgID.org;
        moduleName = callIns.pkgID.name;
    } else if (callIns is bir:AsyncCall) {
        methodName = callIns.name.value;
        orgName = callIns.pkgID.org;
        moduleName = callIns.pkgID.name;
    } else {
        methodName = callIns.name.value;
        orgName = callIns.pkgID.org;
        moduleName = callIns.pkgID.name;
    }

    string key = getPackageName(orgName, moduleName) + methodName;

    if (birFunctionMap.hasKey(key)) {
        BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap[key]);
        return isExternFunc(functionWrapper.func);
    }

    return false;
}
