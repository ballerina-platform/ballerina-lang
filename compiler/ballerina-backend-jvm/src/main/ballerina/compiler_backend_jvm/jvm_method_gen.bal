function generateMethod(bir:Function func, jvm:ClassWriter cw) {
    BalToJVMIndexMap indexMap = new;
    string funcName = untaint func.name.value;

    int returnVarRefIndex = -1;

    // generate method desc
    string desc = getMethodDesc(func);

    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, funcName, desc, null, null);

    mv.visitCode();

    // generate method body
    int k = 1;
    boolean isVoidFunc = false;
    if (func.typeValue.retType is bir:BTypeNil) {
        isVoidFunc = true;
        k = 0;
    }

    bir:VariableDcl[] localVars = func.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = localVars[k];
        _ = indexMap.getIndex(localVar);
        k += 1;
    }

    if (!isVoidFunc) {
        returnVarRefIndex = indexMap.getIndex(localVars[0]);
    }

    // process basic blocks
    int j = 0;
    bir:BasicBlock[] basicBlocks = func.basicBlocks;
    LabelGenerator labelGen = new();

    while (j < basicBlocks.length()) {
        bir:BasicBlock bb = basicBlocks[j];
        //io:println("Basic Block Is : ", bb.id.value);
        string currentBBName = io:sprintf("%s", bb.id.value);

        // create jvm label
        jvm:Label bbLabel = labelGen.getLabel(funcName + bb.id.value);
        mv.visitLabel(bbLabel);

        // generate instructions
        int m = 0;
        while (m < bb.instructions.length()) {
            bir:Instruction inst = bb.instructions[m];
            InstructionGenerator instGen = new(mv, indexMap);
            if (inst is bir:ConstantLoad) {
                instGen.generateConstantLoadIns(inst);
            } else if (inst is bir:Move) {
                instGen.generateMoveIns(inst);
            } else if (inst is bir:BinaryOp) {
                instGen.generateBinaryOpIns(inst);
            } else {
                error err = error( "JVM generation is not supported for operation " + io:sprintf("%s", inst));
                panic err;
            }
            m += 1;
        }

        // process terminator
        bir:Terminator terminator = bb.terminator;
        TerminatorGenerator termGen = new(mv, indexMap, labelGen);
        if (terminator is bir:GOTO) {
            termGen.genGoToTerm(terminator, funcName);
        } else if (terminator is bir:Call) {
            termGen.genCallTerm(terminator, funcName);
        } else if (terminator is bir:Branch) {
            termGen.genBranchTerm(terminator, funcName);
        } else if (terminator is bir:Return) {
            termGen.genReturnTerm(terminator, returnVarRefIndex, func);
        }
        j += 1;
    }

    mv.visitMaxs(100, 400);
    mv.visitEnd();
}

function getMethodDesc(bir:Function func) returns string {
    string desc = "(";
    int i = 0;
    while (i < func.argsCount) {
        desc = desc + getMethodArgDesc(func.typeValue.paramTypes[i]);
        i += 1;
    }
    string returnType = generateReturnType(func.typeValue.retType);
    desc =  desc + returnType;

    return desc;
}

function getMethodArgDesc(bir:BType bType) returns string {
    if (bType is bir:BTypeInt) {
        return "J";
    } else if (bType is bir:BTypeString) {
        return "Ljava/lang/String;";
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function generateReturnType(bir:BType? bType) returns string {
    if (bType is bir:BTypeNil) {
        return ")V";
    } else if (bType is bir:BTypeInt) {
        return ")J";
    } else if (bType is bir:BTypeString) {
        return ")Ljava/lang/String;";
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function getMainFunc(bir:Function[] funcs) returns bir:Function? {
    bir:Function? userMainFunc = ();
    foreach var func in funcs {
        if (func.name.value == "main") {
            userMainFunc = untaint func;
            break;
        }
    }

    return userMainFunc;
}

function generateMainMethod(bir:Function userMainFunc, jvm:ClassWriter cw, bir:Package pkg) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

    // todo : generate the global var init class and other crt0 loading
    generateUserDefinedTypes(mv, pkg.typeDefs);

    boolean isVoidFunction = userMainFunc.typeValue.retType is bir:BTypeNil;

    if (!isVoidFunction) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    }

    string desc = getMethodDesc(userMainFunc);
    bir:BType[] paramTypes = userMainFunc.typeValue.paramTypes;

    // load and cast param values
    int paramIndex = 0;
    foreach var paramType in paramTypes {
        generateCast(paramIndex, paramType, mv);
        paramIndex += 1;
    }

    // invoke the user's main method
    mv.visitMethodInsn(INVOKESTATIC, invokedClassName, "main", desc, false);

    if (!isVoidFunction) {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(paramTypes.length() + 5, 10);
    mv.visitEnd();
}

function generateCast(int paramIndex, bir:BType targetType, jvm:MethodVisitor mv) {
    // load BValue array
    mv.visitVarInsn(ALOAD, 0);

    // load value[i]
    mv.visitLdcInsn(paramIndex);
    mv.visitInsn(L2I);
    mv.visitInsn(AALOAD);

    if (targetType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "parseLong", "(Ljava/lang/String;)J", false);
    } else {
        error err = error("JVM generation is not supported for type " + io:sprintf("%s", targetType));
        panic err;
    }
}


type BalToJVMIndexMap object {
    private int localVarIndex = 0;
    private map<int> jvmLocalVarIndexMap = {};

    function add(bir:VariableDcl varDcl) {
        string varRefName = self.getVarRefName(varDcl);
        self.jvmLocalVarIndexMap[varRefName] = self.localVarIndex;

        bir:BType bType = varDcl.typeValue;

        if (bType is bir:BTypeInt) {
            self.localVarIndex = self.localVarIndex + 2;
        } else {
            self.localVarIndex = self.localVarIndex + 1;
        }
    }

    function getIndex(bir:VariableDcl varDcl) returns int {
        string varRefName = self.getVarRefName(varDcl);
        if (!(self.jvmLocalVarIndexMap.hasKey(varRefName))) {
            self.add(varDcl);
        }

        return self.jvmLocalVarIndexMap[varRefName] ?: -1;
    }

    function getVarRefName(bir:VariableDcl varDcl) returns string {
        return io:sprintf("%s", varDcl);
    }
};
