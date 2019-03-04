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

            //store
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            //io:println("Const Store Index is :::::::::::", index);
            self.mv.visitVarInsn(LSTORE, index);
        } else if (bType is bir:BTypeString) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);

            //store
            int index = self.getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl);
            //io:println("Const Store Index is :::::::::::", index);
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
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, rhsIndex);
            self.mv.visitVarInsn(ISTORE, lhsLndex);
        } else if (bType is bir:BTypeString) {
            self.mv.visitVarInsn(ALOAD, rhsIndex);
            self.mv.visitVarInsn(ASTORE, lhsLndex);
        } else if (bType is bir:BArrayType) {
            self.mv.visitVarInsn(ALOAD, rhsIndex);
            self.mv.visitVarInsn(ASTORE, lhsLndex);
        } else if (bType is bir:BMapType) {
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
    # + inst - type of the new array
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
    # Generate adding a new value to array
    function generateArrayStoreIns(bir:FieldAccess inst) {
        // TODO: visit(var_ref)
        // TODO: visit(index_expr)
        // TODO: visit(value_expr)
        int varRefIndex = self.getJVMIndexOfVarRef(inst.lhsOp.variableDcl);
        self.mv.visitVarInsn(ALOAD, varRefIndex);
        int keyIndex = self.getJVMIndexOfVarRef(inst.keyOp.variableDcl);
        self.mv.visitVarInsn(LLOAD, keyIndex);
        // int valueIndex = self.getJVMIndexOfVarRef(inst.rhsOp.variableDcl);
        // self.mv.visitVarInsn(LLOAD, valueIndex); //need to fix, this is only for int
        int valueIndex = self.getJVMIndexOfVarRef(inst.rhsOp.variableDcl);
        bir:BType valueType = inst.rhsOp.variableDcl.typeValue;
        self.generateLocalVarLoad(valueType, valueIndex);

        string valueDesc = getTypeDesc(valueType); //pass the value type
        self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "add", io:sprintf("(J%s)V", valueDesc), false);
    }

    function generateArrayValueLoad() {
        // TODO: visit(var_ref)
        // TODO: visit(index_expr)
        bir:BType bType = (); // need to infer from the instruction
        if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getInt", "(J)J", false);
        } else if (bType is bir:BTypeString) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE), false);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getBoolean", "(J)J", false);
        } else if (bType == "byte") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getByte", "(J)B", false);
        } else if (bType == "float") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getFloat", "(J)D", false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), false);
        }
    }

    function generateLocalVarLoad(bir:BType bType, int valueIndex) {
        if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LLOAD, valueIndex);
        } else {
            self.mv.visitVarInsn(ALOAD, valueIndex);
        }
    }

    function generateLocalVarStore(bir:BType bType, int valueIndex) {
        if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LSTORE, valueIndex);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType is bir:BTypeString) {
            self.mv.visitVarInsn(ASTORE, valueIndex);
        } else if (bType is bir:BArrayType) {
            self.mv.visitVarInsn(ASTORE, valueIndex);
        } else if (bType is bir:BMapType) {
            self.mv.visitVarInsn(ASTORE, valueIndex);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
            panic err;
        }
    }
};

function addBoxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    if (bType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else {
        return;
    }
}

function addUnboxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    if (bType is bir:BTypeInt) {
        mv.visitTypeInsn(CHECKCAST, LONG_VALUE);
        mv.visitMethodInsn(INVOKEVIRTUAL, LONG_VALUE, "longValue", "()J", false);
    } else if (bType is bir:BTypeString) {
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    } else if (bType is bir:BMapType) {
        mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
    } else {
        return;
    }
}
