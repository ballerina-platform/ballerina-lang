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

type TerminatorGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    LabelGenerator labelGen;
    ErrorHandlerGenerator errorGen;
    int lambdaIndex = 0;
    bir:Package module;


    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen, 
                            ErrorHandlerGenerator errorGen, bir:Package module) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.labelGen = labelGen;
        self.errorGen = errorGen;
        self.module = module;
    }

    function genGoToTerm(bir:GOTO gotoIns, string funcName) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genReturnTerm(bir:Return returnIns, int returnVarRefIndex, bir:Function func) {
        bir:BType bType = func.typeValue.retType;
        if (bType is bir:BTypeNil) {
            self.mv.visitInsn(RETURN);
        } else if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            self.mv.visitVarInsn(LLOAD, returnVarRefIndex);
            self.mv.visitInsn(LRETURN);
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
                bType is bir:BTypeAnyData ||
                bType is bir:BObjectType ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BJSONType ||
                bType is bir:BFutureType ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
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
                            io:sprintf("%s", func.typeValue.retType));
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

        int opIndex = self.getJVMIndexOfVarRef(branchIns.op.variableDcl);
        self.mv.visitVarInsn(ILOAD, opIndex);

        jvm:Label trueBBLabel = self.labelGen.getLabel(funcName + trueBBId);
        self.mv.visitJumpInsn(IFGT, trueBBLabel);

        jvm:Label falseBBLabel = self.labelGen.getLabel(funcName + falseBBId);
        self.mv.visitJumpInsn(GOTO, falseBBLabel);
    }

    function genCallTerm(bir:Call callIns, string funcName, boolean isInTryBlock, bir:ErrorEntry? currentEE, 
                         jvm:Label endLabel, jvm:Label handlerLabel, jvm:Label jumpLabel, int localVarOffset) {
        //io:println("Call Ins : " + io:sprintf("%s", callIns));
        string orgName = callIns.pkgID.org;
        string moduleName = callIns.pkgID.name;
        if (callIns.isVirtual) {
            self.genVirtualCall(callIns, orgName, moduleName, localVarOffset);
        } else {
            self.genStaticCall(callIns, orgName, moduleName, localVarOffset);
        }

        // store return
        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp.variableDcl;

        if (lhsOpVarDcl is bir:VariableDcl) {
            int lhsLndex = self.getJVMIndexOfVarRef(lhsOpVarDcl);
            bir:BType? bType = callIns.lhsOp.typeValue;

            if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
                self.mv.visitVarInsn(LSTORE, lhsLndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DSTORE, lhsLndex);
            } else if (bType is bir:BTypeString) {
                self.mv.visitVarInsn(ASTORE, lhsLndex);
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ISTORE, lhsLndex);
            } else if (bType is bir:BArrayType ||
                        bType is bir:BMapType ||
                        bType is bir:BErrorType ||
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BObjectType ||
                        bType is bir:BUnionType ||
                        bType is bir:BRecordType || 
                        bType is bir:BTupleType ||
                        bType is bir:BFutureType ||
                        bType is bir:BJSONType ||
                        bType is bir:BXMLType ||
                        bType is bir:BInvokableType ||
                        bType is bir:BFiniteType ||
                        bType is bir:BTypeDesc) {
                self.mv.visitVarInsn(ASTORE, lhsLndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                            io:sprintf("%s", callIns.lhsOp.typeValue));
                panic err;
            }

        }
        
        // handle trapped function calls.
        if (isInTryBlock &&  currentEE is bir:ErrorEntry) {
            self.errorGen.generateCatchInsForTrap(currentEE, endLabel, handlerLabel, jumpLabel);
        }
        
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
        jvm:Label yieldLabel = self.labelGen.getLabel(funcName + "yield");
        self.mv.visitJumpInsn(IFNE, yieldLabel);

        // goto thenBB
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + callIns.thenBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genStaticCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        string methodName = callIns.name.value;
        string methodDesc = "(Lorg/ballerinalang/jvm/Strand;";

        // load strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);

        int argsCount = callIns.args.length();
        int i = 0;
        while (i < argsCount) {
            bir:VarRef? arg = callIns.args[i];
            methodDesc += self.visitArg(arg);
            i += 1;
        }

        bir:BType? returnType = callIns.lhsOp.typeValue;
        string returnTypeDesc = generateReturnType(returnType);
        methodDesc = methodDesc + returnTypeDesc;
        string jvmClass = lookupFullQualifiedClassName(getPackageName(orgName, moduleName) + methodName);
        self.mv.visitMethodInsn(INVOKESTATIC, jvmClass, methodName, methodDesc, false);
    }

    function genVirtualCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        bir:VariableDcl selfArg = getVariableDcl(callIns.args[0].variableDcl);
        int argIndex = self.getJVMIndexOfVarRef(selfArg);

        // load self
        self.mv.visitVarInsn(ALOAD, argIndex);
        self.mv.visitTypeInsn(CHECKCAST, OBJECT_VALUE);

        // load the strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);

        // load the function name as the second argument
        self.mv.visitLdcInsn(callIns.name.value);

        // create an Object[] for the rest params
        int argsCount = callIns.args.length() - 1;
        self.mv.visitLdcInsn(argsCount);
        self.mv.visitInsn(L2I);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int i = 0;
        while (i < argsCount) {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(i);
            self.mv.visitInsn(L2I);

            // i + 1 is used since we skip the first argument (self)
            bir:VarRef? arg = callIns.args[i + 1];
            _ = self.visitArg(arg);

            // Add the to the rest params array
            addBoxInsn(self.mv, arg.typeValue);
            self.mv.visitInsn(AASTORE);
            i += 1;
        }

        // call method
        string methodDesc = io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);

        bir:BType? returnType = callIns.lhsOp.typeValue;
        if (returnType is ()) {
            self.mv.visitInsn(POP);
        } else {
            addUnboxInsn(self.mv, returnType);
        }
    }

    function visitArg(bir:VarRef? arg) returns string {
        bir:VarRef argRef = getVarRef(arg);
        bir:BType bType = argRef.typeValue;
        int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(argRef.variableDcl));
        if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            self.mv.visitVarInsn(LLOAD, argIndex);
            return "J";
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DLOAD, argIndex);
            return "D";
        } else if (bType is bir:BTypeString) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return "Ljava/lang/String;";
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, argIndex);
            return "Z";
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTupleType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", ARRAY_VALUE);
        } else if (bType is bir:BRecordType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", MAP_VALUE);
        } else if (bType is bir:BMapType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", MAP_VALUE);
        } else if (bType is bir:BObjectType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", OBJECT_VALUE);
        } else if (bType is bir:BFutureType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", FUTURE_VALUE);
        } else if (bType is bir:BTypeDesc) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            self.mv.visitTypeInsn(CHECKCAST, TYPEDESC_VALUE);
            return io:sprintf("L%s;", TYPEDESC_VALUE);
        } else if (bType is bir:BErrorType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", ERROR_VALUE);
        } else if (bType is bir:BInvokableType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            if (bType.retType is bir:BTypeNil) {
                return io:sprintf("L%s;", CONSUMER);
            } else {
                return io:sprintf("L%s;", FUNCTION);
            }   
        } else if (bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BTypeNil ||
                    bType is bir:BUnionType ||
                    bType is bir:BJSONType ||
                    bType is bir:BXMLType ||
                    bType is bir:BFiniteType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", OBJECT);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                                io:sprintf("%s", argRef.typeValue));
            panic err;
        }
    }

    function genAsyncCallTerm(bir:AsyncCall callIns, string funcName) {

        // Load the scheduler from strand
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));

        //create an object array of args
        self.mv.visitIntInsn(BIPUSH, callIns.args.length() + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
        
        int paramIndex = 1;
        foreach var arg in callIns.args {
            bir:VarRef argRef = getVarRef(arg);
            self.mv.visitInsn(DUP);
            self.mv.visitIntInsn(BIPUSH, paramIndex);

            int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(argRef.variableDcl));
            bir:BType bType = argRef.typeValue;

            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LLOAD, argIndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DLOAD, argIndex);
            } else if (bType is bir:BTypeString) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BArrayType ||
                        bType is bir:BTupleType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BRecordType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BMapType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BObjectType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BErrorType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType ||
                        bType is bir:BJSONType ||
                        bType is bir:BXMLType ||
                        bType is bir:BInvokableType ||
                        bType is bir:BFiniteType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                                    io:sprintf("%s", argRef.typeValue));
                panic err;
            }
            addBoxInsn(self.mv, bType);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }

        string lambdaName = "$" + funcName + "$lambda$" + self.lambdaIndex + "$";
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        string methodClass = lookupFullQualifiedClassName(currentPackageName + funcName);
        bir:BType? futureType = callIns.lhsOp.typeValue;
        bir:BType returnType = bir:TYPE_NIL;
        if (futureType is bir:BFutureType) {
            returnType = futureType.returnType;
        }
        boolean isVoid = returnType is bir:BTypeNil;
        self.mv.visitInvokeDynamicInsn(methodClass, lambdaName, isVoid, 0);
        lambdas[lambdaName] = (callIns, methodClass);
        self.lambdaIndex += 1;
        
        self.submitToScheduler(callIns.lhsOp);

        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
        jvm:Label yieldLabel = self.labelGen.getLabel(funcName + "yield");
        self.mv.visitJumpInsn(IFNE, yieldLabel);

        // goto thenBB
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + callIns.thenBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function generateWaitIns(bir:Wait waitInst, string funcName) {
        // TODO : need to fix to support multiple waits
        bir:VarRef? futureVal = waitInst.exprList[0];
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        if (futureVal is bir:VarRef) {
            generateVarLoad(self.mv, futureVal.variableDcl, currentPackageName, 
                self.getJVMIndexOfVarRef(futureVal.variableDcl));
        }

        self.mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "isDone", "Z");
        jvm:Label label = new;
        self.mv.visitJumpInsn(IFNE, label);
        jvm:Label label2 = new;
        self.mv.visitLabel(label2);
        // strand.blocked = true
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitInsn(ICONST_1);
        self.mv.visitFieldInsn(PUTFIELD, STRAND, "blocked", "Z");

        // strand.blockedOn = future.strand
        self.mv.visitVarInsn(ALOAD, 0);
        if (futureVal is bir:VarRef) {
            bir:VariableDcl? varDecl = futureVal.variableDcl;
            if (varDecl is bir:VariableDcl) {
                generateVarLoad(self.mv, varDecl, currentPackageName, 
                    self.getJVMIndexOfVarRef(varDecl));
            }  
        }
        self.mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        self.mv.visitFieldInsn(PUTFIELD, STRAND, "blockedOn", io:sprintf("L%s;", STRAND));

        // strand.yield = true
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitInsn(ICONST_1);
        self.mv.visitFieldInsn(PUTFIELD, STRAND, "yield", "Z");
        jvm:Label yieldLabel = self.labelGen.getLabel(funcName + "yield");
        self.mv.visitJumpInsn(GOTO, yieldLabel);
        self.mv.visitLabel(label);

        // future.result = lhs
        if (futureVal is bir:VarRef) {
            bir:VariableDcl? varDecl = futureVal.variableDcl;
            if (varDecl is bir:VariableDcl) {
                generateVarLoad(self.mv, varDecl, currentPackageName, 
                    self.getJVMIndexOfVarRef(varDecl));
            }  
        }
        self.mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "result", io:sprintf("L%s;", OBJECT));
        addUnboxInsn(self.mv, waitInst.lhsOp.typeValue);
        generateVarStore(self.mv, waitInst.lhsOp.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(waitInst.lhsOp.variableDcl));
    }

    function genFPCallIns(bir:FPCall fpCall, string funcName) {
        if (fpCall.isAsync) {
            // Load the scheduler from strand
            self.mv.visitVarInsn(ALOAD, 0);
            self.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));    
        } else {
            // load function ref, going to directly call the fp
            int fpIndex = self.getJVMIndexOfVarRef(getVariableDcl(fpCall.fp.variableDcl));
            self.mv.visitVarInsn(ALOAD, fpIndex);
        }
        
        // create an object array of args
        self.mv.visitIntInsn(BIPUSH, fpCall.args.length() + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
        
        // load strand
        self.mv.visitInsn(DUP);
        self.mv.visitIntInsn(BIPUSH, 0);
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitInsn(AASTORE);

        // load args
        int paramIndex = 1;
        foreach var arg in fpCall.args {
            self.mv.visitInsn(DUP);
            self.mv.visitIntInsn(BIPUSH, paramIndex);
            
            int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(arg.variableDcl));
            bir:BType? bType = arg.typeValue;

            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LLOAD, argIndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DLOAD, argIndex);
            } else if (bType is bir:BTypeString) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BArrayType ||
                        bType is bir:BTupleType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BRecordType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BMapType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BObjectType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BErrorType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                                    io:sprintf("%s", arg.typeValue));
                panic err;
            }
            addBoxInsn(self.mv, bType);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }

        // if async, we submit this to sceduler (worker scenario)
        if (fpCall.isAsync) {
            // load function ref now
            int fpIndex = self.getJVMIndexOfVarRef(getVariableDcl(fpCall.fp.variableDcl));
            self.mv.visitVarInsn(ALOAD, fpIndex);
            self.submitToScheduler(fpCall.lhsOp);           
        } else if (fpCall.lhsOp is ()) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, CONSUMER, "accept", io:sprintf("(L%s;)V", OBJECT), true);
        } else {
            self.mv.visitMethodInsn(INVOKEINTERFACE, FUNCTION, "apply", io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), true);
            // store reult
            int lhsIndex = self.getJVMIndexOfVarRef(getVariableDcl(fpCall.lhsOp.variableDcl));
            bir:BType? lhsType = fpCall.lhsOp.typeValue;
            if (lhsType is bir:BType) {
                addUnboxInsn(self.mv, lhsType);
            }
            string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
            bir:VariableDcl? lhsVar = fpCall.lhsOp.variableDcl;
            if (lhsVar is bir:VariableDcl) {
                generateVarStore(self.mv, lhsVar, currentPackageName, lhsIndex);
            }
        }

        self.genYieldCheck(fpCall.thenBB, funcName);   
    }

    function genWrkSendIns(bir:WrkSend ins, string funcName) {
        self.mv.visitVarInsn(ALOAD, 0);
        if (!ins.isSameStrand) {
            self.mv.visitFieldInsn(GETFIELD, STRAND, "parent", io:sprintf("L%s;", STRAND));
        }
        self.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", io:sprintf("L%s;", WD_CHANNELS));
        self.mv.visitLdcInsn(ins.channelName.value);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", io:sprintf("(L%s;)L%s;", 
            STRING_VALUE, WORKER_DATA_CHANNEL), false);
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        generateVarLoad(self.mv, ins.dataOp.variableDcl, currentPackageName, self.getJVMIndexOfVarRef(ins.dataOp.variableDcl));
        addBoxInsn(self.mv, ins.dataOp.typeValue);
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "sendData", io:sprintf("(L%s;L%s;)V", OBJECT, STRAND), false); 
    }

    function genWrkReceiveIns(bir:WrkReceive ins, string funcName) {
        self.mv.visitVarInsn(ALOAD, 0);
        if (!ins.isSameStrand) {
            self.mv.visitFieldInsn(GETFIELD, STRAND, "parent", io:sprintf("L%s;", STRAND));
        }     
        self.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", io:sprintf("L%s;", WD_CHANNELS));
        self.mv.visitLdcInsn(ins.channelName.value);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", io:sprintf("(L%s;)L%s;", 
            STRING_VALUE, WORKER_DATA_CHANNEL), false);
 
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "tryTakeData", io:sprintf("(L%s;)L%s;", STRAND, OBJECT), false);
        
        // a dummy var to temporaly store worker result
        bir:VariableDcl tempVar = { typeValue: "any",
                                 name: { value: "wrkMsg" },
                                 kind: "ARG" };
        int wrkResultIndex = self.getJVMIndexOfVarRef(tempVar);
        self.mv.visitVarInsn(ASTORE, wrkResultIndex);
        
        jvm:Label l5 = self.labelGen.getLabel("l55");
        self.mv.visitLabel(l5);
        self.mv.visitVarInsn(ALOAD, wrkResultIndex);
        jvm:Label l6 = self.labelGen.getLabel("l66");
        self.mv.visitJumpInsn(IFNULL, l6);
        jvm:Label l7 = self.labelGen.getLabel("l77");
        self.mv.visitLabel(l7);
        self.mv.visitVarInsn(ALOAD, wrkResultIndex);
        addUnboxInsn(self.mv, ins.lhsOp.typeValue);
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        bir:VariableDcl? lhsVar = ins.lhsOp.variableDcl;
        generateVarStore(self.mv, ins.lhsOp.variableDcl, currentPackageName, self.getJVMIndexOfVarRef(ins.lhsOp.variableDcl));

        self.mv.visitLabel(l6);
        self.genYieldCheck(ins.thenBB, funcName);
    }
        
    function submitToScheduler(bir:VarRef? lhsOp) {
        bir:BType? futureType = lhsOp.typeValue;
        boolean isVoid = false;
        if (futureType is bir:BFutureType) {
            isVoid = futureType.returnType is bir:BTypeNil;
        }
        // load strand
        self.mv.visitVarInsn(ALOAD, 0);
        if (isVoid) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "schedule", 
                io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, CONSUMER, STRAND, FUTURE_VALUE), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "schedule", 
                io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION, STRAND, FUTURE_VALUE), false);
        }

        // store return
        if (lhsOp is bir:VarRef) {
            bir:VariableDcl? lhsOpVarDcl = lhsOp.variableDcl;
            // store the returned strand as the future
            self.mv.visitVarInsn(ASTORE, self.getJVMIndexOfVarRef(getVariableDcl(lhsOpVarDcl)));
        }
    }

    function genYieldCheck(bir:BasicBlock thenBB, string funcName) {
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
        jvm:Label yieldLabel = self.labelGen.getLabel(funcName + "yield");
        self.mv.visitJumpInsn(IFNE, yieldLabel);

        // goto thenBB
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + thenBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }
};

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
