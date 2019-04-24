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
                bType is bir:BErrorType ||
                bType is bir:BObjectType ||
                bType is bir:BUnionType ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BJSONType ||
                bType is bir:BFutureType) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", func.typeValue.retType));
            panic err;
        }
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
                        bType is bir:BJSONType) {
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
        bir:BType bType = arg.typeValue;
        int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(arg.variableDcl));
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
        } else if (bType is bir:BErrorType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", ERROR_VALUE);
        } else if (bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BTypeNil ||
                    bType is bir:BUnionType ||
                    bType is bir:BJSONType) {
            self.mv.visitVarInsn(ALOAD, argIndex);
            return io:sprintf("L%s;", OBJECT);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                                io:sprintf("%s", arg.typeValue));
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
            self.mv.visitInsn(DUP);
            self.mv.visitIntInsn(BIPUSH, paramIndex);
            
            int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(arg.variableDcl));
            bir:BType bType = arg.typeValue;

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
            generateObjectCast(bType, self.mv);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;
        }

        string lambdaName = "$" + funcName + "$lambda$" + self.lambdaIndex + "$";
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        string methodClass = lookupFullQualifiedClassName(currentPackageName + funcName);
        bir:BType futureType = callIns.lhsOp.typeValue;
        bir:BType returnType = ();
        if (futureType is bir:BFutureType) {
            returnType = futureType.returnType;
        }
        boolean isVoid = returnType is bir:BTypeNil;
        self.mv.visitInvokeDynamicInsn(methodClass, lambdaName, isVoid);
        lambdas[lambdaName] = (callIns, methodClass);
        self.lambdaIndex += 1;
        
        if (isVoid) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "schedule", 
             io:sprintf("([L%s;L%s;)L%s;", OBJECT, CONSUMER, FUTURE_VALUE), false);
        } else {
             self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "schedule", 
            io:sprintf("([L%s;L%s;)L%s;", OBJECT, FUNCTION, FUTURE_VALUE), false);
        }

        // store return
        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp.variableDcl;
        // store the returned strand as the future
        self.mv.visitVarInsn(ASTORE, self.getJVMIndexOfVarRef(getVariableDcl(lhsOpVarDcl)));

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
        checkCastFromObject(waitInst.lhsOp.typeValue, self.mv);
        generateVarStore(self.mv, waitInst.lhsOp.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(waitInst.lhsOp.variableDcl));
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }
};
