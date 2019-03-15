import ballerina/io;
type InstructionGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    string currentPackageName;

    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, string currentPackageName) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.currentPackageName = currentPackageName;
    }

    function generateConstantLoadIns(bir:ConstantLoad loadIns) {
        bir:BType bType = loadIns.typeValue;

        if (bType is bir:BTypeInt) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeFloat) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeString) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeBoolean) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeNil) {
            self.mv.visitInsn(ACONST_NULL);
        } else {
            error err = error( "JVM generation is not supported for type : " + io:sprintf("%s", bType));
            panic err;
        }

        self.generateVarStore(loadIns.lhsOp.variableDcl);
    }

    function generateMoveIns(bir:Move moveIns) {
        self.generateVarLoad(moveIns.rhsOp.variableDcl);
        self.generateVarStore(moveIns.lhsOp.variableDcl);
    }


    function generateBinaryOpIns(bir:BinaryOp binaryIns) {
        if (binaryIns.kind == bir:BINARY_LESS_THAN) {
            self.generateLessThanIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_ADD) {
            self.generateAddIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_EQUAL) {
            self.generateEqualIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_SUB) {
            self.generateSubIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_DIV) {
            self.generateDivIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_MUL) {
            self.generateMulIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_AND) {
            self.generateAndIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_OR) {
            self.generateOrIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_LESS_EQUAL) {
            self.generateLessEqualIns(binaryIns);
        } else {
            error err = error("JVM generation is not supported for type : " + io:sprintf("%s", binaryIns.kind));
            panic err;
        }
    }

    function generateBinaryRhsAndLhsLoad(bir:BinaryOp binaryIns) {
        self.generateVarLoad(binaryIns.rhsOp1.variableDcl);
        self.generateVarLoad(binaryIns.rhsOp2.variableDcl);
    }

    function generateLessThanIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFLT, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateLessEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFLE, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFNE, label1);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateAddIns(bir:BinaryOp binaryIns) {
        //io:println("ADD Ins " + io:sprintf("%s", binaryIns));

        bir:BType bType = binaryIns.lhsOp.typeValue;

        if (bType is bir:BTypeInt) {
            self.generateBinaryRhsAndLhsLoad(binaryIns);

            self.mv.visitInsn(LADD);
            self.generateVarStore(binaryIns.lhsOp.variableDcl);
        } else if (bType is bir:BTypeString) {
            self.generateVarLoad(binaryIns.rhsOp1.variableDcl);
            self.generateVarLoad(binaryIns.rhsOp2.variableDcl);
            self.mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                                         "(Ljava/lang/String;)Ljava/lang/String;", false);
            self.generateVarStore(binaryIns.lhsOp.variableDcl);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
    }

    function generateSubIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        self.mv.visitInsn(LSUB);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateDivIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        self.mv.visitInsn(LDIV);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateMulIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        self.mv.visitInsn(LMUL);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateAndIns(bir:BinaryOp binaryIns) {
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ICONST_1
        // ISTORE

        //io:println("AND ins : " + io:sprintf("%s", binaryIns));

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.generateVarLoad(binaryIns.rhsOp1.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPNE, label1);

        self.generateVarLoad(binaryIns.rhsOp2.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPNE, label1);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);

        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateOrIns(bir:BinaryOp binaryIns) {
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ICONST_1
        // ISTORE

        //io:println("OR ins : " + io:sprintf("%s", binaryIns));

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.generateVarLoad(binaryIns.rhsOp1.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPEQ, label1);

        self.generateVarLoad(binaryIns.rhsOp2.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPEQ, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);

        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }

    function generateMapNewIns(bir:NewMap mapNewIns) {
        self.mv.visitTypeInsn(NEW, MAP_VALUE);
        self.mv.visitInsn(DUP);
        loadType(self.mv, mapNewIns.typeValue);
        self.mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
        self.generateVarStore(mapNewIns.lhsOp.variableDcl);
    }

    function generateMapStoreIns(bir:FieldAccess mapStoreIns) {
        // visit map_ref
        self.generateVarLoad(mapStoreIns.lhsOp.variableDcl);

        // visit key_expr
        self.generateVarLoad(mapStoreIns.keyOp.variableDcl);

        // visit value_expr
        bir:BType valueType = mapStoreIns.rhsOp.variableDcl.typeValue;
        self.generateVarLoad(mapStoreIns.rhsOp.variableDcl);
        addBoxInsn(self.mv, valueType);

        self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "put",
                io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), false);

        // emit a pop, since we are not using the return value from the map.put()
        self.mv.visitInsn(POP);
    }

    function generateMapLoadIns(bir:FieldAccess mapStoreIns) {
        // visit map_ref
        self.generateVarLoad(mapStoreIns.rhsOp.variableDcl);

        addUnboxInsn(self.mv, mapStoreIns.rhsOp.variableDcl.typeValue);

        // visit key_expr
        self.generateVarLoad(mapStoreIns.keyOp.variableDcl);

        self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "get",
                io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), false);

        // store in the target reg
        bir:BType targetType = mapStoreIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType);
        self.generateVarStore(mapStoreIns.lhsOp.variableDcl);
    }

    # Generate a new instance of an array value
    # 
    # + inst - the new array instruction
    function generateArrayNewIns(bir:NewArray inst) {
        self.mv.visitTypeInsn(NEW, ARRAY_VALUE);
        self.mv.visitInsn(DUP);
        bir:BType arrayType = inst.typeValue;
        if (arrayType is bir:BArrayType) {
            loadType(self.mv, arrayType.eType);
            self.generateVarLoad(inst.sizeOp.variableDcl);
            self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE, "<init>", io:sprintf("(L%s;J)V", BTYPE), false);
        } else if (arrayType is bir:BTupleType) {
            loadType(self.mv, arrayType);
            self.generateVarLoad(inst.sizeOp.variableDcl);
            self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE, "<init>", io:sprintf("(L%s;J)V", BTYPE), false);
        }
        self.generateVarStore(inst.lhsOp.variableDcl);
    }

    # Generate adding a new value to an array
    # 
    # + inst - array store instruction
    function generateArrayStoreIns(bir:FieldAccess inst) {
        self.generateVarLoad(inst.lhsOp.variableDcl);
        self.generateVarLoad(inst.keyOp.variableDcl);
        bir:BType valueType = inst.rhsOp.variableDcl.typeValue;
        self.generateVarLoad(inst.rhsOp.variableDcl);

        string valueDesc = getTypeDesc(valueType);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "add", io:sprintf("(J%s)V", valueDesc), false);
    }

    # Generating loading a new value from an array to the top of the stack
    # 
    # + inst - field access instruction
    function generateArrayValueLoad(bir:FieldAccess inst) {
        self.generateVarLoad(inst.rhsOp.variableDcl);
        self.generateVarLoad(inst.keyOp.variableDcl);
        bir:BType bType = inst.lhsOp.variableDcl.typeValue;
        if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getInt", "(J)J", false);
        } else if (bType is bir:BTypeString) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE),
                                        false);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getBoolean", "(J)J", false);
        } else if (bType == "byte") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getByte", "(J)B", false);
        } else if (bType == "float") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getFloat", "(J)D", false);
        } else if (bType is bir:BRecordType) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), false);
            self.mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), false);
        }

        self.generateVarStore(inst.lhsOp.variableDcl);
    }

    function generateNewErrorIns(bir:NewError newErrorIns) {
        self.mv.visitTypeInsn(NEW, ERROR_VALUE);
        self.mv.visitInsn(DUP);
        self.generateVarLoad(newErrorIns.reasonOp.variableDcl);
        self.generateVarLoad(newErrorIns.detailsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, "<init>",
                           io:sprintf("(L%s;L%s;)V", STRING_VALUE, REF_VALUE), false);
        self.generateVarStore(newErrorIns.lhsOp.variableDcl);
    }

    function generateVarLoad(bir:VariableDcl varDcl) {
        bir:BType bType = varDcl.typeValue;

        if (varDcl.kind == "GLOBAL") {
            string varName = varDcl.name.value;
            string className = lookupFullQualifiedClassName(self.currentPackageName + varName);

            if (bType is bir:BTypeInt) {
                self.mv.visitFieldInsn(GETSTATIC, className, varName, "J");
            } else if (bType is bir:BMapType) {
                self.mv.visitFieldInsn(GETSTATIC, className, varName, io:sprintf("L%s;", MAP_VALUE));
            } else {
                error err = error("JVM generation is not supported for type " +io:sprintf("%s", bType));
                panic err;
            }
        } else {
            int valueIndex = self.getJVMIndexOfVarRef(varDcl);
            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LLOAD, valueIndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DLOAD, valueIndex);
            } else if (bType is bir:BTypeBoolean || bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ILOAD, valueIndex);
            } else if (bType is bir:BArrayType ||
                          bType is bir:BTypeString ||
                          bType is bir:BMapType ||
                          bType is bir:BTypeAny ||
                          bType is bir:BTypeAnyData ||
                          bType is bir:BTypeNil ||
                          bType is bir:BUnionType ||
                          bType is bir:BTupleType ||
                          bType is bir:BRecordType ||
                          bType is bir:BErrorType) {
                self.mv.visitVarInsn(ALOAD, valueIndex);
            } else {
                error err = error( "JVM generation is not supported for type " +io:sprintf("%s", bType));
                panic err;
            }
        }
    }

    function generateVarStore(bir:VariableDcl varDcl) {
        bir:BType bType = varDcl.typeValue;

        if (varDcl.kind == "GLOBAL") {
            string varName = varDcl.name.value;
            string className = lookupFullQualifiedClassName(self.currentPackageName + varName);

            if (bType is bir:BTypeInt) {
                self.mv.visitFieldInsn(PUTSTATIC, className, varName, "J");
            } else if (bType is bir:BMapType) {
                self.mv.visitFieldInsn(PUTSTATIC, className, varName, io:sprintf("L%s;", MAP_VALUE));
            } else {
                error err = error("JVM generation is not supported for type " +io:sprintf("%s", bType));
                panic err;
            }
        } else {
            int valueIndex = self.getJVMIndexOfVarRef(varDcl);
            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LSTORE, valueIndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DSTORE, valueIndex);
            } else if (bType is bir:BTypeBoolean || bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ISTORE, valueIndex);
            } else if (bType is bir:BArrayType ||
                            bType is bir:BTypeString ||
                            bType is bir:BMapType ||
                            bType is bir:BTypeAny ||
                            bType is bir:BTypeAnyData ||
                            bType is bir:BTypeNil ||
                            bType is bir:BUnionType ||
                            bType is bir:BTupleType ||
                            bType is bir:BRecordType ||
                            bType is bir:BErrorType) {
                self.mv.visitVarInsn(ASTORE, valueIndex);
            } else {
                error err = error("JVM generation is not supported for type " +io:sprintf("%s", bType));
                panic err;
            }
        }
    }

    function generateCastIns(bir:TypeCast typeCastIns) {
        // load source value
        self.generateVarLoad(typeCastIns.rhsOp.variableDcl);
        generateCast(self.mv, typeCastIns.rhsOp.typeValue, typeCastIns.lhsOp.typeValue);
        self.generateVarStore(typeCastIns.lhsOp.variableDcl);
    }

    function generateTypeTestIns(bir:TypeTest typeTestIns) {
        // load source value
        self.generateVarLoad(typeTestIns.rhsOp.variableDcl);

        // load targetType
        loadType(self.mv, typeTestIns.typeValue);

        self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                io:sprintf("(L%s;L%s;)Z", OBJECT, BTYPE, OBJECT), false);
        self.generateVarStore(typeTestIns.lhsOp.variableDcl);
    }
};

function addBoxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    generateCast(mv, bType, "any");
}

function addUnboxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    generateCast(mv, "any", bType);
}
