import ballerina/io;
type InstructionGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;

    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap) {
        self.mv = mv;
        self.indexMap = indexMap;
    }

    function generateConstantLoadIns(bir:ConstantLoad loadIns) {
        bir:BType bType = loadIns.typeValue;

        if (bType is bir:BTypeInt) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            self.mv.visitVarInsn(LSTORE, index);
        } else if (bType is bir:BTypeFloat) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            self.mv.visitVarInsn(DSTORE, index);
        } else if (bType is bir:BTypeString) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            self.mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeBoolean) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            self.mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BTypeNil) {
            self.mv.visitInsn(ACONST_NULL);
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            self.mv.visitVarInsn(ASTORE, index);
        } else {
            error err = error( "JVM generation is not supported for type : " + io:sprintf("%s", bType));
            panic err;
        }
    }

    function generateMoveIns(bir:Move moveIns) {
        int rhsIndex = self.getJVMIndexOfVarRef(moveIns.rhsOp.variableDcl);
        //io:println("RHS Index is :::::::::::", rhsIndex);
        int lhsLndex = self.getJVMIndexOfVarRef(moveIns.lhsOp.variableDcl);
        //io:println("LHS Index is :::::::::::", lhsLndex);

        bir:BType bType = moveIns.rhsOp.typeValue;
        if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LLOAD, rhsIndex);
            self.mv.visitVarInsn(LSTORE, lhsLndex);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DLOAD, rhsIndex);
            self.mv.visitVarInsn(DSTORE, lhsLndex);
        } else if (bType is bir:BTypeString) {
            self.mv.visitVarInsn(ALOAD, rhsIndex);
            self.mv.visitVarInsn(ASTORE, lhsLndex);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, rhsIndex);
            self.mv.visitVarInsn(ISTORE, lhsLndex);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitVarInsn(ILOAD, rhsIndex);
            self.mv.visitVarInsn(ISTORE, lhsLndex);
        } else if (bType is bir:BArrayType ||
                        bType is bir:BMapType ||
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType) {
            self.mv.visitVarInsn(ALOAD, rhsIndex);
            self.mv.visitVarInsn(ASTORE, lhsLndex);
        } else if (bType is bir:BRecordType) {
            self.mv.visitVarInsn(ALOAD, rhsIndex);
            self.mv.visitVarInsn(ASTORE, lhsLndex);
        } else if (bType is bir:BErrorType) {
            self.mv.visitVarInsn(ALOAD, rhsIndex);
            self.mv.visitVarInsn(ASTORE, lhsLndex);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", moveIns.rhsOp.typeValue));
            panic err;
        }
    }


    function generateBinaryOpIns(bir:BinaryOp binaryIns) {
        if (binaryIns.kind is bir:LESS_THAN) {
            self.generateLessThanIns(binaryIns);
        } else if (binaryIns.kind is bir:ADD) {
            self.generateAddIns(binaryIns);
        } else if (binaryIns.kind is bir:EQUAL) {
            self.generateEqualIns(binaryIns);
        } else if (binaryIns.kind is bir:SUB) {
            self.generateSubIns(binaryIns);
        } else if (binaryIns.kind is bir:DIV) {
            self.generateDivIns(binaryIns);
        } else if (binaryIns.kind is bir:MUL) {
            self.generateMulIns(binaryIns);
        } else if (binaryIns.kind is bir:AND) {
            self.generateAndIns(binaryIns);
        } else if (binaryIns.kind is bir:OR) {
            self.generateOrIns(binaryIns);
        } else if (binaryIns.kind is bir:LESS_EQUAL) {
            self.generateLessEqualIns(binaryIns);
        } else {
            error err = error("JVM generation is not supported for type : " + io:sprintf("%s", binaryIns.kind));
            panic err;
        }
    }

    function generateBinaryRhsAndLhsLoad(bir:BinaryOp binaryIns) {
        int rhsOps1Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
        self.mv.visitVarInsn(LLOAD, rhsOps1Index);

        int rhsOps2Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
        self.mv.visitVarInsn(LLOAD, rhsOps2Index);
    }

    function generateLessThanIns(bir:BinaryOp binaryIns) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFLT, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.mv.visitVarInsn(ISTORE, lhsOpIndex);
    }

    function generateLessEqualIns(bir:BinaryOp binaryIns) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFLE, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.mv.visitVarInsn(ISTORE, lhsOpIndex);
    }

    function generateEqualIns(bir:BinaryOp binaryIns) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFNE, label1);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.mv.visitVarInsn(ISTORE, lhsOpIndex);
    }

    function generateAddIns(bir:BinaryOp binaryIns) {
        //io:println("ADD Ins " + io:sprintf("%s", binaryIns));

        bir:BType bType = binaryIns.lhsOp.typeValue;

        if (bType is bir:BTypeInt) {
            bir:VarRef lhsOp = binaryIns.lhsOp;
            self.generateBinaryRhsAndLhsLoad(binaryIns);
            int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

            self.mv.visitInsn(LADD);
            self.mv.visitVarInsn(LSTORE, lhsOpIndex);
        } else if (bType is bir:BTypeString) {

            int rhsOps1Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
            self.mv.visitVarInsn(ALOAD, rhsOps1Index);

            int rhsOps2Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
            self.mv.visitVarInsn(ALOAD, rhsOps2Index);
            self.mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                                         "(Ljava/lang/String;)Ljava/lang/String;", false);

            bir:VarRef lhsVarRef = binaryIns.lhsOp;
            int lhsIndex = self.getJVMIndexOfVarRef(lhsVarRef.variableDcl);
            self.mv.visitVarInsn(ASTORE, lhsIndex);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
    }

    function generateSubIns(bir:BinaryOp binaryIns) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

        self.mv.visitInsn(LSUB);
        self.mv.visitVarInsn(LSTORE, lhsOpIndex);
    }

    function generateDivIns(bir:BinaryOp binaryIns) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        //io:println("DIV ins : " + io:sprintf("%s", lhsOp));
        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

        self.mv.visitInsn(LDIV);
        self.mv.visitVarInsn(LSTORE, lhsOpIndex);
    }

    function generateMulIns(bir:BinaryOp binaryIns) {
        bir:VarRef lhsOp = binaryIns.lhsOp;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        //io:println("DIV ins : " + io:sprintf("%s", lhsOp));
        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);

        self.mv.visitInsn(LMUL);
        self.mv.visitVarInsn(LSTORE, lhsOpIndex);
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

        bir:VarRef lhsOp = binaryIns.lhsOp;

        //io:println("AND ins : " + io:sprintf("%s", binaryIns));

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        int rhsOps1Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
        self.mv.visitVarInsn(ILOAD, rhsOps1Index);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPNE, label1);

        int rhsOps2Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
        self.mv.visitVarInsn(ILOAD, rhsOps2Index);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPNE, label1);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);

        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);
        self.mv.visitVarInsn(ISTORE, lhsOpIndex);
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

        bir:VarRef lhsOp = binaryIns.lhsOp;

        //io:println("OR ins : " + io:sprintf("%s", binaryIns));

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        int rhsOps1Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl);
        self.mv.visitVarInsn(ILOAD, rhsOps1Index);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPEQ, label1);

        int rhsOps2Index = self.getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl);
        self.mv.visitVarInsn(ILOAD, rhsOps2Index);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPEQ, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);

        int lhsOpIndex = self.getJVMIndexOfVarRef(lhsOp.variableDcl);
        self.mv.visitVarInsn(ISTORE, lhsOpIndex);
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }

    function generateMapNewIns(bir:NewMap mapNewIns) {
        self.mv.visitTypeInsn(NEW, MAP_VALUE);
        self.mv.visitInsn(DUP);
        loadType(self.mv, mapNewIns.typeValue);
        self.mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);

        int lhsOpIndex = self.getJVMIndexOfVarRef(mapNewIns.lhsOp.variableDcl);
        self.mv.visitVarInsn(ASTORE, lhsOpIndex);
    }

    function generateMapStoreIns(bir:FieldAccess mapStoreIns) {
        // visit map_ref
        int mapIndex = self.getJVMIndexOfVarRef(mapStoreIns.lhsOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, mapIndex);

        // visit key_expr
        int keyIndex = self.getJVMIndexOfVarRef(mapStoreIns.keyOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, keyIndex);

        // visit value_expr
        int valueIndex = self.getJVMIndexOfVarRef(mapStoreIns.rhsOp.variableDcl);
        bir:BType valueType = mapStoreIns.rhsOp.variableDcl.typeValue;
        self.generateLocalVarLoad(valueType, valueIndex);
        addBoxInsn(self.mv, valueType);

        self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "put",
                io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), false);

        // emit a pop, since we are not using the return value from the map.put()
        self.mv.visitInsn(POP);
    }

    function generateMapLoadIns(bir:FieldAccess mapStoreIns) {
        // visit map_ref
        int mapIndex = self.getJVMIndexOfVarRef(mapStoreIns.rhsOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, mapIndex);
        addUnboxInsn(self.mv, mapStoreIns.rhsOp.variableDcl.typeValue);

        // visit key_expr
        int keyIndex = self.getJVMIndexOfVarRef(mapStoreIns.keyOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, keyIndex);

        self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "get",
                io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), false);

        // store in the target reg
        bir:BType targetType = mapStoreIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType);
        int targetIndex = self.getJVMIndexOfVarRef(mapStoreIns.lhsOp.variableDcl);
        self.generateLocalVarStore(targetType, targetIndex);
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
            self.mv.visitVarInsn(LLOAD, self.getJVMIndexOfVarRef(inst.sizeOp.variableDcl));
            self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE, "<init>", io:sprintf("(L%s;J)V", BTYPE), false);
        }
        self.mv.visitVarInsn(ASTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
    }

    # Generate adding a new value to an array
    # 
    # + inst - array store instruction
    function generateArrayStoreIns(bir:FieldAccess inst) {
        int varRefIndex = self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, varRefIndex);
        int keyIndex = self.getJVMIndexOfVarRef(inst.keyOp.variableDcl);
        self.mv.visitVarInsn(LLOAD, keyIndex);
        int valueIndex = self.getJVMIndexOfVarRef(inst.rhsOp.variableDcl);
        bir:BType valueType = inst.rhsOp.variableDcl.typeValue;
        self.generateLocalVarLoad(valueType, valueIndex);

        string valueDesc = getTypeDesc(valueType);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "add", io:sprintf("(J%s)V", valueDesc), false);
    }

    # Generating loading a new value from an array to the top of the stack
    # 
    # + inst - field access instruction
    function generateArrayValueLoad(bir:FieldAccess inst) {
        int varRefIndex = self.getJVMIndexOfVarRef(inst.rhsOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, varRefIndex);
        int keyIndex = self.getJVMIndexOfVarRef(inst.keyOp.variableDcl);
        self.mv.visitVarInsn(LLOAD, keyIndex);
        bir:BType bType = inst.lhsOp.variableDcl.typeValue;
        if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getInt", "(J)J", false);
            self.mv.visitVarInsn(LSTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
        } else if (bType is bir:BTypeString) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE),
                                        false);
            self.mv.visitVarInsn(ASTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getBoolean", "(J)J", false);
            self.mv.visitVarInsn(ISTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
        } else if (bType == "byte") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getByte", "(J)B", false);
            self.mv.visitVarInsn(ISTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
        } else if (bType == "float") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getFloat", "(J)D", false);
            self.mv.visitVarInsn(DSTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT_VALUE),
                                        false);
            self.mv.visitVarInsn(ASTORE, self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl));
        }
    }
    
    function generateNewErrorIns(bir:NewError newErrorIns) {
        // create new error value
        self.mv.visitTypeInsn(NEW, ERROR_VALUE);
        self.mv.visitInsn(DUP);
        // visit reason and detail
        int reasonIndex = self.getJVMIndexOfVarRef(newErrorIns.reasonOp.variableDcl);
        int detailsIndex = self.getJVMIndexOfVarRef(newErrorIns.detailsOp.variableDcl);
        int lhsIndex = self.getJVMIndexOfVarRef(newErrorIns.lhsOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, reasonIndex);
        self.mv.visitVarInsn(ALOAD, detailsIndex);
        self.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, "<init>",
                           io:sprintf("(L%s;L%s;)V", STRING_VALUE, REF_VALUE), false);
        self.mv.visitVarInsn(ASTORE, lhsIndex);
    }

    function generateLocalVarLoad(bir:BType bType, int valueIndex) {
        if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LLOAD, valueIndex);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, valueIndex);
        } else {
            self.mv.visitVarInsn(ALOAD, valueIndex);
        }
    }

    function generateLocalVarStore(bir:BType bType, int valueIndex) {
        if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LSTORE, valueIndex);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DSTORE, valueIndex);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType is bir:BArrayType ||
                        bType is bir:BTypeString ||
                        bType is bir:BMapType ||
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType ||
                        bType is bir:BRecordType ||
                        bType is bir:BErrorType) {
            self.mv.visitVarInsn(ASTORE, valueIndex);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
            panic err;
        }
    }

    function generateCastIns(bir:TypeCast typeCastIns) {
        int sourceIndex = self.getJVMIndexOfVarRef(typeCastIns.rhsOp.variableDcl);
        bir:BType sourceType = typeCastIns.rhsOp.variableDcl.typeValue;
        self.generateLocalVarLoad(sourceType, sourceIndex);

        generateCast(self.mv, typeCastIns.rhsOp.typeValue, typeCastIns.lhsOp.typeValue);

        int targetIndex = self.getJVMIndexOfVarRef(typeCastIns.lhsOp.variableDcl);
        bir:BType targetType = typeCastIns.lhsOp.variableDcl.typeValue;
        self.generateLocalVarStore(targetType, targetIndex);
    }
};

function addBoxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    generateCast(mv, bType, "any");
}

function addUnboxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    generateCast(mv, "any", bType);
}
