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
    int lambdaIndex = 0;
    bir:Package module;


    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen, 
        bir:Package module) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.labelGen = labelGen;
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
                         jvm:Label endLabel, jvm:Label handlerLabel, jvm:Label jumpLabel) {
        //io:println("Call Ins : " + io:sprintf("%s", callIns));
        string methodName = callIns.name.value;

        string orgName = callIns.pkgID.org;
        string moduleName = callIns.pkgID.name;
        string jvmClass = lookupFullQualifiedClassName(getPackageName(orgName, moduleName) + methodName);

        string methodDesc = "(Lorg/ballerinalang/jvm/Strand;";

        self.mv.visitVarInsn(ALOAD, 0);
        foreach var arg in callIns.args {

            int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(arg.variableDcl));

            bir:BType bType = arg.typeValue;

            if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
                self.mv.visitVarInsn(LLOAD, argIndex);
                methodDesc = methodDesc + "J";
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DLOAD, argIndex);
                methodDesc = methodDesc + "D";
            } else if (bType is bir:BTypeString) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + "Ljava/lang/String;";
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ILOAD, argIndex);
                methodDesc = methodDesc + "Z";
            } else if (bType is bir:BArrayType ||
                        bType is bir:BTupleType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", ARRAY_VALUE);
            } else if (bType is bir:BRecordType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", MAP_VALUE);
            } else if (bType is bir:BMapType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", MAP_VALUE);
            } else if (bType is bir:BObjectType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", OBJECT_VALUE);
            } else if (bType is bir:BFutureType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", FUTURE_VALUE);
            } else if (bType is bir:BErrorType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", ERROR_VALUE);
            } else if (bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType ||
                        bType is bir:BJSONType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", OBJECT);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                                    io:sprintf("%s", arg.typeValue));
                panic err;
            }
        }


        bir:BType? returnType = callIns.lhsOp.typeValue;
        string returnTypeDesc = generateReturnType(returnType);
        methodDesc = methodDesc + returnTypeDesc;

        // call method
        self.mv.visitMethodInsn(INVOKESTATIC, jvmClass, methodName, methodDesc, false);

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
            self.generateCatchIns(currentEE, endLabel, handlerLabel, jumpLabel);
        }
        
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
        jvm:Label yieldLabel = self.labelGen.getLabel(funcName + "yield");
        self.mv.visitJumpInsn(IFNE, yieldLabel);

        // goto thenBB
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + callIns.thenBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genPanicIns(bir:Panic panicTerm) {
        int errorIndex = self.getJVMIndexOfVarRef(panicTerm.errorOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, errorIndex);
        self.mv.visitInsn(ATHROW);
    }

    function generateTryIns(bir:ErrorEntry currentEE, jvm:Label endLabel, jvm:Label handlerLabel,
                            jvm:Label jumpLabel) {
        jvm:Label startLabel = new;
        self.mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, ERROR_VALUE);
        jvm:Label temp = new;
        self.mv.visitLabel(temp);
        int lhsIndex = self.getJVMIndexOfVarRef(<bir:VariableDcl>currentEE.errorOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, lhsIndex);
        self.mv.visitJumpInsn(IFNONNULL, jumpLabel);
        self.mv.visitLabel(startLabel);
    }

    function generateCatchIns(bir:ErrorEntry currentEE, jvm:Label endLabel, jvm:Label handlerLabel,
                              jvm:Label jumpLabel) {
        int lhsIndex = self.getJVMIndexOfVarRef(<bir:VariableDcl>currentEE.errorOp.variableDcl);
        self.mv.visitLabel(endLabel);
        self.mv.visitJumpInsn(GOTO, jumpLabel);
        self.mv.visitLabel(handlerLabel);
        self.mv.visitVarInsn(ASTORE, lhsIndex);
        self.mv.visitLabel(jumpLabel);
    }
    
    function genAsyncCallTerm(bir:AsyncCall callIns, string funcName) {

        //create a object array of args
        self.mv.visitIntInsn(BIPUSH, callIns.args.length() + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
        self.mv.visitInsn(DUP);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitInsn(AASTORE);
        
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
        self.mv.visitInvokeDynamicInsn(methodClass, lambdaName);
        lambdas[lambdaName] = (callIns, methodClass);
        self.lambdaIndex += 1;
        
        self.mv.visitMethodInsn(INVOKESTATIC, SCHEDULER, "schedule", 
            io:sprintf("([L%s;Ljava/util/function/Function;)L%s;", OBJECT, FUTURE_VALUE), false);

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

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }
};
