type TerminatorGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    LabelGenerator labelGen;
    int lambdaIndex = 0;
    bir:Package module;


    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen, bir:Package module) {
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
        } else if (bType is bir:BTypeInt) {
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
        } else if (bType is bir:BTypeByte) {
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
                bType is bir:BTupleType) {
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

    function genCallTerm(bir:Call callIns, string funcName) {
        //io:println("Call Ins : " + io:sprintf("%s", callIns));
        string methodName = callIns.name.value;

        string orgName = callIns.pkgID.org;
        string moduleName = callIns.pkgID.name;
        string jvmClass = lookupFullQualifiedClassName(getPackageName(orgName, moduleName) + methodName);

        string methodDesc = "(Lorg/ballerinalang/jvm/Strand;";

        self.mv.visitVarInsn(ALOAD, 0);
        foreach var arg in callIns.args {

            int argIndex = self.getJVMIndexOfVarRef(arg.variableDcl);

            bir:BType bType = arg.typeValue;

            if (bType is bir:BTypeInt) {
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
            } else if (bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ILOAD, argIndex);
                methodDesc = methodDesc + "I";
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
            } else if (bType is bir:BErrorType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", ERROR_VALUE);
            } else if (bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType) {
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

            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LSTORE, lhsLndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DSTORE, lhsLndex);
            } else if (bType is bir:BTypeString) {
                self.mv.visitVarInsn(ASTORE, lhsLndex);
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ISTORE, lhsLndex);
            } else if (bType is bir:BTypeByte) {
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
                        bType is bir:BTupleType) {
                self.mv.visitVarInsn(ASTORE, lhsLndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                            io:sprintf("%s", callIns.lhsOp.typeValue));
                panic err;
            }

        }

        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
        jvm:Label yieldLabel = self.labelGen.getLabel(funcName + "yield");
        self.mv.visitJumpInsn(IFNE, yieldLabel);

        // goto thenBB
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + callIns.thenBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genAsyncCallTerm(bir:Call callIns, string funcName) {

        //create a object array of args
        self.mv.visitVarInsn(BIPUSH, callIns.args.length() + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
        self.mv.visitInsn(DUP);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitInsn(AASTORE);
        
        int paramIndex = 1;
        foreach var arg in callIns.args {
            self.mv.visitInsn(DUP);
            self.mv.visitVarInsn(BIPUSH, paramIndex);
            
            int argIndex = self.getJVMIndexOfVarRef(arg.variableDcl);
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
        }

        string lambdaName = "$" + funcName + "$lambda$" + self.lambdaIndex + "$";
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        string methodClass = lookupFullQualifiedClassName(currentPackageName + funcName);
        self.mv.visitInvokeDynamicInsn(methodClass, lambdaName);
        lambdas[lambdaName] = (callIns, methodClass);
        self.lambdaIndex += 1;
        
        self.mv.visitMethodInsn(INVOKESTATIC, "org/ballerinalang/jvm/Scheduler", "schedule", 
            "([Ljava/lang/Object;Ljava/util/function/Function;)Lorg/ballerinalang/jvm/Strand;", false);

        //TODO: revisit when bir is there for futures.
        self.mv.visitInsn(POP); //pops the returned strand for now
        self.mv.visitVarInsn(BIPUSH,100); // for now always retun a int
        // store return
        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp.variableDcl;
        // store the returned strand as the future
        // self.mv.visitVarsInsn(ASTORE, self.getJVMIndexOfVarRef(lhsOpVarDcl));

        if (lhsOpVarDcl is bir:VariableDcl) {
            int lhsLndex = self.getJVMIndexOfVarRef(lhsOpVarDcl);
            bir:BType? bType = callIns.lhsOp.typeValue;

            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LSTORE, lhsLndex);
            } else {
                    error err = error( "JVM generation in ASYNC is not supported for type " +
                                            io:sprintf("%s", callIns.lhsOp.typeValue));
                panic err;
            }
        }

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
