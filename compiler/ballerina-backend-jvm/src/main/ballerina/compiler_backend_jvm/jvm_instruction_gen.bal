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

    // TODO following functions are not yet used
    function generateMapNewIns() {
        self.mv.visitTypeInsn(NEW, MAP_VALUE);
        self.mv.visitInsn(DUP);
        self.mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE, "<init>", "()V", false);
    }

    function generateMapStoreIns() {
        // TODO: visit(var_ref)
        // TODO: visit(key_expr)
        // TODO: visit(value_expr)
        self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "put",
                io:sprintf("(L%s;L%s;)L%s;", OBJECT_VALUE, OBJECT_VALUE, OBJECT_VALUE), false);

        // emit a pop, since we are not using the return value from the map.put()
        self.mv.visitInsn(POP);
    }

    # Generate a new instance of an array value
    # 
    # + arrayType - type of the new array
    function generateArrayNewIns(bir:VarRef arrayType) {
        self.mv.visitTypeInsn(NEW, Array_VALUE);
        self.mv.visitInsn(DUP);
        self.mv.visitVarInsn(ALOAD, self.getJVMIndexOfVarRef(arrayType.variableDcl));
        self.mv.visitMethodInsn(INVOKESPECIAL, Array_VALUE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
    }
    # Generate adding a new value to array
    function generateArrayStoreIns() {
        // TODO: visit(var_ref)
        // TODO: visit(index_expr)
        // TODO: visit(value_expr)
        string valueDesc = getMethodArgDesc("int"); //pass the value type
        self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "add", io:sprintf("(J%s;)V", valueDesc), false);
    }

    function generateArrayValueLoad() {
        // TODO: visit(var_ref)
        // TODO: visit(index_expr)
        bir:BType bType = (); // need to infer from the instruction
        if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "getInt", "(J)J", false);
        } else if (bType is bir:BTypeString) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE), false);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "getBoolean", "(J)J", false);
        } else if (bType == "byte") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "getByte", "(J)B", false);
        } else if (bType == "float") {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "getFloat", "(J)D", false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, Array_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT_VALUE), false);
        }
    }

    # Generate code to load an instance of the given type
    # to the top of the stack.
    #
    # + bType - type to load
    function loadType(bir:BType bType) {
        string typeFieldName = "";
        if (bType is bir:BTypeInt) {
            typeFieldName = "typeInt";
        } else if (bType is bir:BTypeString) {
            typeFieldName = "typeString";
        } else if (bType is bir:BTypeBoolean) {
            typeFieldName = "typeBoolean";
        } else if (bType is bir:BTypeNil) {
            typeFieldName = "typeNull";
        } else if (bType is bir:BArrayType) {
            self.loadArrayType(bType);
            return;
        } else if (bType is bir:BUnionType) {
            self.loadUnionType(bType);
            return;
        } else {
            error err = error("JVM generation is not supported for type " + io:sprintf("%s", bType));
            panic err;
        }

        self.mv.visitFieldInsn(GETSTATIC, BTYPES, typeFieldName, io:sprintf("L%s;", BTYPE));
    }

    # Generate code to load an instance of the given array type
    # to the top of the stack.
    #
    # + bType - array type to load
    function loadArrayType(bir:BArrayType bType) {
        // Create an new array type
        self.mv.visitTypeInsn(NEW, ARRAY_TYPE);
        self.mv.visitInsn(DUP);

        // Load the element type
        self.loadType(bType.eType);

        // invoke the constructor
        self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
    }

    # Generate code to load an instance of the given union type
    # to the top of the stack.
    #
    # + bType - union type to load
    function loadUnionType(bir:BUnionType bType) {
        // Create the union type
        self.mv.visitTypeInsn(NEW, UNION_TYPE);
        self.mv.visitInsn(DUP);

        // Create the members array
        bir:BType[] memberTypes = bType.members;
        self.mv.visitLdcInsn(memberTypes.length());
        self.mv.visitInsn(L2I);
        self.mv.visitTypeInsn(ANEWARRAY, BTYPE);
        int i = 0;
        foreach var memberType in memberTypes {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(i);
            self.mv.visitInsn(L2I);

            // Load the member type
            self.loadType(memberType);

            // Add the member to the array
            self.mv.visitInsn(AASTORE);
            i += 1;
        }

        // initialize the union type using the members array
        self.mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE, "<init>", io:sprintf("([L%s;)V", BTYPE), false);
        return;
    }
};
