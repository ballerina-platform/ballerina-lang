type TerminatorGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    LabelGenerator labelGen;

    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.labelGen = labelGen;
    }

    function genGoToTerm(bir:GOTO gotoIns, string funcName) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genReturnTerm(bir:Return returnIns, int returnVarRefIndex, bir:Function func) {
        if (func.typeValue.retType is bir:BTypeNil) {
            self.mv.visitInsn(RETURN);
        } else {
            bir:BType bType = func.typeValue.retType;
            if (bType is bir:BTypeInt) {
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
            } else if (bType is bir:BArrayType) {
                self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                self.mv.visitInsn(ARETURN);
            } else if (bType is bir:BMapType) {
                self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                self.mv.visitInsn(ARETURN);
            } else if (bType is bir:BTypeAny || bType is bir:BTypeNil) {
                self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
                self.mv.visitInsn(ARETURN);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                io:sprintf("%s", func.typeValue.retType));
                panic err;
            }
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
        string methodDesc = "(Lorg/ballerina/jvm/Strand;";

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
            } else if (bType is bir:BArrayType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", ARRAY_VALUE);
            } else if (bType is bir:BMapType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
                methodDesc = methodDesc + io:sprintf("L%s;", MAP_VALUE);
            } else if (bType is bir:BTypeAny || bType is bir:BTypeNil) {
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
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeNil) {
                self.mv.visitVarInsn(ASTORE, lhsLndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                            io:sprintf("%s", callIns.lhsOp.typeValue));
                panic err;
            }

        }

        self.mv.visitVarInsn(ALOAD, 0);
        self.mv.visitFieldInsn(GETFIELD, "org/ballerina/jvm/Strand", "yield", "Z");
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
