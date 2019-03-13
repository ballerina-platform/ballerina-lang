function generateMethod(bir:Function func, jvm:ClassWriter cw, string className) {
    int lambdaIndex = 0;
    BalToJVMIndexMap indexMap = new;
    string funcName = untaint func.name.value;
    int returnVarRefIndex = -1;

    // generate method desc
    string desc = getMethodDesc(func.typeValue.paramTypes, func.typeValue.retType);
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, funcName, desc, null, null);
    mv.visitCode();

    // generate method body
    int k = 1;
    boolean isVoidFunc = false;
    if (func.typeValue.retType is bir:BTypeNil) {
        isVoidFunc = true;
        k = 0;
    }
    bir:VariableDcl stranVar = { typeValue: "string", // should be record
                                 name: { value: "srand" },
                                 kind: "ARG" };
    _ = indexMap.getIndex(stranVar);

    bir:VariableDcl[] localVars = func.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = localVars[k];
        var index = indexMap.getIndex(localVar);
        if(localVar.kind != "ARG"){
            bir:BType bType = localVar.typeValue;
            genDefaultValue(mv, bType, index);
        }
        k += 1;
    }
    bir:VariableDcl stateVar = { typeValue: "string", //should  be javaInt
                                 name: { value: "state" },
                                 kind: "TEMP" };
    var stateVarIndex = indexMap.getIndex(stateVar);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, stateVarIndex);

    LabelGenerator labelGen = new();

    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    jvm:Label resumeLable = labelGen.getLabel(funcName + "resume");
    mv.visitJumpInsn(IFGT, resumeLable);

    jvm:Label varinitLable = labelGen.getLabel(funcName + "varinit");
    mv.visitLabel(varinitLable);

    if (!isVoidFunc) {
        returnVarRefIndex = indexMap.getIndex(localVars[0]);
        bir:BType returnType = func.typeValue.retType;
        genDefaultValue(mv, returnType, returnVarRefIndex);
    }

    // uncomment to test yield
    // mv.visitFieldInsn(GETSTATIC, className, "i", "I");
    // mv.visitInsn(ICONST_1);
    // mv.visitInsn(IADD);
    // mv.visitFieldInsn(PUTSTATIC, className, "i", "I");

    // process basic blocks
    int j = 0;
    bir:BasicBlock[] basicBlocks = func.basicBlocks;

    jvm:Label[] lables = [];
    int[] states = [];

    int i = 0;
    while (i < basicBlocks.length()) {
        bir:BasicBlock bb = basicBlocks[i];
        if(i == 0){
            lables[i] = labelGen.getLabel(funcName + bb.id.value);
        } else {
            lables[i] = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
        }
        states[i] = i;
        i = i + 1;
    }

    TerminatorGenerator termGen = new(mv, indexMap, labelGen);

    // uncomment to test yield
    // mv.visitFieldInsn(GETSTATIC, className, "i", "I");
    // mv.visitIntInsn(BIPUSH, 100);
    // jvm:Label l0 = labelGen.getLabel(funcName + "l0");
    // mv.visitJumpInsn(IF_ICMPNE, l0);
    // mv.visitVarInsn(ALOAD, 0);
    // mv.visitInsn(ICONST_1);
    // mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
    // termGen.genReturnTerm({kind:"RETURN"}, returnVarRefIndex, func);
    // mv.visitLabel(l0);

    mv.visitVarInsn(ILOAD, stateVarIndex);
    jvm:Label yieldLable = labelGen.getLabel(funcName + "yield");
    mv.visitLookupSwitchInsn(yieldLable, states, lables);



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
            } else if (inst is bir:NewArray) {
                instGen.generateArrayNewIns(inst);
            } else if (inst is bir:NewMap) {
                instGen.generateMapNewIns(inst);
            } else if (inst is bir:NewError) {
                instGen.generateNewErrorIns(inst);
            } else if (inst is bir:FieldAccess) {
                if (inst.kind == "MAP_STORE") {
                    instGen.generateMapStoreIns(inst);
                } else if (inst.kind == "MAP_LOAD") {
                    instGen.generateMapLoadIns(inst);
                } else if (inst.kind == "ARRAY_STORE") {
                    instGen.generateArrayStoreIns(inst);
                } else if (inst.kind == "ARRAY_LOAD") {
                    instGen.generateArrayValueLoad(inst);
                }
            } else {
                error err = error( "JVM generation is not supported for operation " + io:sprintf("%s", inst));
                panic err;
            }
            m += 1;
        }

        jvm:Label bbEndLable = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
        mv.visitLabel(bbEndLable);

        mv.visitIntInsn(BIPUSH, j);
        mv.visitVarInsn(ISTORE, stateVarIndex);

        // process terminator
        bir:Terminator terminator = bb.terminator;
        if (terminator is bir:GOTO) {
            termGen.genGoToTerm(terminator, funcName);
        } else if (terminator is bir:Call) {
            termGen.genCallTerm(terminator, funcName, className);
        } else if (terminator is bir:Branch) {
            termGen.genBranchTerm(terminator, funcName);
        } else if (terminator is bir:Return) {
            termGen.genReturnTerm(terminator, returnVarRefIndex, func);
        }
        j += 1;
    }

    var frameName = funcName + "Frame";
    mv.visitLabel(resumeLable);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "frames", "[Ljava/lang/Object;");
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitInsn(ICONST_1);
    mv.visitInsn(ISUB);
    mv.visitInsn(DUP_X1);
    mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, frameName);

    k = 0;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = localVars[k];
        var index = indexMap.getIndex(localVar);
        bir:BType bType = localVar.typeValue;
        mv.visitInsn(DUP);
        if (bType is bir:BTypeInt) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "J");
            mv.visitVarInsn(LSTORE, index);
        } else if (bType is bir:BTypeFloat) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "D");
            mv.visitVarInsn(DSTORE, index);
        } else if (bType is bir:BTypeString) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", STRING_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeBoolean) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "Z");
            mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BTypeByte) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "B");
            mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BMapType || bType is bir:BRecordType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", MAP_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BArrayType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", ARRAY_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BObjectType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", OBJECT_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BErrorType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", ERROR_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeNil ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BUnionType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", OBJECT));
            mv.visitVarInsn(ASTORE, index);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
            panic err;
        }
        k = k + 1;
    }
    mv.visitFieldInsn(GETFIELD, frameName, "state", "I");
    mv.visitVarInsn(ISTORE, stateVarIndex);
    mv.visitJumpInsn(GOTO, varinitLable);


    mv.visitLabel(yieldLable);
    mv.visitTypeInsn(NEW, frameName);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, frameName, "<init>", "()V", false);


    k = 0;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = localVars[k];
        var index = indexMap.getIndex(localVar);
        mv.visitInsn(DUP);

        bir:BType bType = localVar.typeValue;

        if (bType is bir:BTypeInt) {
            mv.visitVarInsn(LLOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "J");
        } else if (bType is bir:BTypeFloat) {
            mv.visitVarInsn(DLOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "D");
        } else if (bType is bir:BTypeString) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", STRING_VALUE));
        } else if (bType is bir:BTypeBoolean) {
            mv.visitVarInsn(ILOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "Z");
        } else if (bType is bir:BTypeByte) {
            mv.visitVarInsn(ILOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "B");
        } else if (bType is bir:BMapType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", MAP_VALUE));
        } else if (bType is bir:BArrayType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", ARRAY_VALUE));
        } else if (bType is bir:BErrorType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", ERROR_VALUE));
        } else if (bType is bir:BObjectType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", OBJECT_VALUE));
        } else if (bType is bir:BTypeNil ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BUnionType ||
                    bType is bir:BObjectType ||
                    bType is bir:BRecordType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", OBJECT));
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
            panic err;
        }

        k = k + 1;
    }

    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, stateVarIndex);
    mv.visitFieldInsn(PUTFIELD, frameName, "state", "I");


    bir:VariableDcl frameVar = { typeValue: "string", // should be record or something
                                 name: { value: "frame" },
                                 kind: "TEMP" };
    var frameVarIndex = indexMap.getIndex(frameVar);
    mv.visitVarInsn(ASTORE, frameVarIndex);

    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "frames", "[Ljava/lang/Object;");
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitInsn(DUP_X1);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IADD);
    mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitVarInsn(ALOAD, frameVarIndex);
    mv.visitInsn(AASTORE);


    termGen.genReturnTerm({kind:"RETURN"}, returnVarRefIndex, func);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
}

function generateLambdaMethod(bir:Call callIns, jvm:ClassWriter cw, string className, string lambdaName) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PRIVATE + ACC_SYNTHETIC, "lambda$stamp$0", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);

    bir:BType[] paramTypes = callIns.args;

    //load strand as first arg
    mv.visitVarInsn(ALOAD, 1);
    mv.visitIntInsn(BIPUSH, 0);
    mv.visitInsn(AALOAD);

    // load and cast param values
    int paramIndex = 1;
    foreach var paramType in paramTypes {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitIntInsn(BIPUSH, paramIndex);
        mv.visitInsn(AALOAD);
        checkCast(paramType, mv);
        paramIndex += 1;
    }

    mv.visitMethodInsn(INVOKEVIRTUAL, className, callIns.name.value, getMethodDesc(paramTypes, callIns.lhsOp.typeValue), false);
    generateObjectCast(callIns.lhsOp.typeValue, mv);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(paramTypes.length() + 2, 2);
    mv.visitEnd();
}

function genDefaultValue(jvm:MethodVisitor mv, bir:BType bType, int index) {
    if (bType is bir:BTypeInt) {
        mv.visitInsn(LCONST_0);
        mv.visitVarInsn(LSTORE, index);
    } else if (bType is bir:BTypeFloat) {
        mv.visitInsn(DCONST_0);
        mv.visitVarInsn(DSTORE, index);
    } else if (bType is bir:BTypeString) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (bType is bir:BTypeByte) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (bType is bir:BMapType ||
                bType is bir:BArrayType ||
                bType is bir:BErrorType ||
                bType is bir:BTypeNil ||
                bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BObjectType ||
                bType is bir:BUnionType ||
                bType is bir:BRecordType) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
        panic err;
    }
}

function getMethodDesc(bir:BType[] paramTypes, bir:BType retType) returns string {
    string desc = "(Lorg/ballerinalang/jvm/Strand;";
    int i = 0;
    while (i < paramTypes.length()) {
        desc = desc + getTypeDesc(paramTypes[i]);
        i += 1;
    }
    string returnType = generateReturnType(retType);
    desc =  desc + returnType;

    return desc;
}

function getTypeDesc(bir:BType bType) returns string {
    if (bType is bir:BTypeInt) {
        return "J";
    } else if (bType is bir:BTypeFloat) {
        return "D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf("L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        return "Z";
    } else if (bType is bir:BTypeByte) {
        return "B";
    } else if (bType is bir:BTypeNil) {
        return io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        return io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BArrayType) {
        return io:sprintf("L%s;", ARRAY_VALUE);
    } else if (bType is bir:BErrorType) {
        return io:sprintf("L%s;", ERROR_VALUE);
    } else if (bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BUnionType) {
        return io:sprintf("L%s;", OBJECT);
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function generateReturnType(bir:BType? bType) returns string {
    if (bType is ()) {
        return ")V";
    } else if (bType is bir:BTypeInt) {
        return ")J";
    } else if (bType is bir:BTypeFloat) {
        return ")D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf(")L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        return ")Z";
    } else if (bType is bir:BTypeByte) {
        return ")B";
    } else if (bType is bir:BTypeNil) {
        return ")V";
    } else if (bType is bir:BArrayType) {
        return io:sprintf(")L%s;", ARRAY_VALUE);
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        return io:sprintf(")L%s;", MAP_VALUE);
    } else if (bType is bir:BErrorType) {
        return io:sprintf(")L%s;", ERROR_VALUE);
    } else if (bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BUnionType) {
        return io:sprintf(")L%s;", OBJECT);
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

    string desc = getMethodDesc(userMainFunc.typeValue.paramTypes, userMainFunc.typeValue.retType);
    bir:BType[] paramTypes = userMainFunc.typeValue.paramTypes;

    mv.visitTypeInsn(NEW, "org/ballerinalang/jvm/Strand");
    mv.visitInsn(DUP);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "org/ballerinalang/jvm/Strand", "<init>", "()V", false);
    mv.visitIntInsn(BIPUSH, 100);
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "frames", "[Ljava/lang/Object;");

    // load and cast param values
    int paramIndex = 0;
    foreach var paramType in paramTypes {
        generateCast(paramIndex, paramType, mv);
        paramIndex += 1;
    }

    // invoke the user's main method
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);
    string mainClass = lookupFullQualifiedClassName(pkgName + userMainFunc.name.value);

    mv.visitMethodInsn(INVOKESTATIC, mainClass, "main", desc, false);

    if (!isVoidFunction) {
        bir:BType returnType = userMainFunc.typeValue.retType;
        if (returnType is bir:BTypeInt) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
        } else if (returnType is bir:BTypeFloat) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(D)V", false);
        } else if (returnType is bir:BTypeBoolean) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
        } else if (returnType is bir:BTypeByte) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
        }
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(paramTypes.length() + 5, 10);
    mv.visitEnd();
}
# Generate cast instruction from Object to target type
# 
# + targetType - target type to be casted
# + mv - method visitor
function checkCast(bir:BType targetType, jvm:MethodVisitor mv) {  
    if (targetType is bir:BTypeInt) {
        mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
    } else if (targetType is bir:BTypeFloat) {
        mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "parseDouble", "()D", false);
    } else if (targetType is bir:BTypeString) {
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    } else if (targetType is bir:BTypeBoolean) {
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "parseBoolean", "()Z", false);
    } else if (targetType is bir:BTypeByte) {
        mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
        mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "parseByte", "()B", false);
    } else if (targetType is bir:BArrayType) {
        mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
    } else if (targetType is bir:BMapType) {
        mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
    } else if (targetType is bir:BTypeAny ||
                targetType is bir:BTypeAnyData ||
                targetType is bir:BTypeNil ||
                targetType is bir:BUnionType) {
        // do nothing
        return;
    } else {
        error err = error("JVM generation is not supported for type " + io:sprintf("%s", targetType));
        panic err;
    }
}

# Cast a given type to object
# 
# + targetType - target type to be casted
# + mv - method visitor
function generateObjectCast(bir:BType targetType, jvm:MethodVisitor mv) {  
    if (targetType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
    } else if (targetType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", "(D)Ljava/lang/Double;", false);
    } else if (targetType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", "(Z)Ljava/lang/Boolean;", false);
    } else if (targetType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "valueOf", "(B)Ljava/lang/Byte;", false);
    } else if (targetType is bir:BTypeAny ||
                targetType is bir:BTypeAnyData ||
                targetType is bir:BTypeNil ||
                targetType is bir:BUnionType ||
                targetType is bir:BTypeString ||
                targetType is bir:BArrayType ||
                targetType is bir:BMapType) {
        // do nothing
        return;
    } else {
        error err = error("JVM generation is not supported for type " + io:sprintf("%s", targetType));
        panic err;
    }
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
    } else if (targetType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "parseDouble", "(Ljava/lang/String;)D", false);
    } else if (targetType is bir:BTypeString) {
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    } else if (targetType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "parseBoolean", "(Ljava/lang/String;)Z", false);
    } else if (targetType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "parseByte", "(Ljava/lang/String;)B", false);
    } else if (targetType is bir:BArrayType) {
        mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
    } else if (targetType is bir:BMapType) {
        mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
    } else if (targetType is bir:BTypeAny ||
                targetType is bir:BTypeAnyData ||
                targetType is bir:BTypeNil ||
                targetType is bir:BUnionType) {
        // do nothing
        return;
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

        if (bType is bir:BTypeInt || bType is bir:BTypeFloat) {
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

function generateFrameClasses(bir:Package pkg, map<byte[]> pkgEntries) {
    foreach var func in pkg.functions {
        var currentFunc = untaint func;
        var frameName = currentFunc.name.value + "Frame";
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, frameName, null, OBJECT_VALUE, null);
        generateDefaultConstructor(cw);

        int k = 0;
        bir:VariableDcl[] localVars = func.localVars;
        while (k < localVars.length()) {
            bir:VariableDcl localVar = localVars[k];
            bir:BType bType = localVar.typeValue;
            var fieldName = localVar.name.value.replace("%","_");
            if (bType is bir:BTypeInt) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, "J");
                fv.visitEnd();
            } else if (bType is bir:BTypeFloat) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, "D");
                fv.visitEnd();
            } else if (bType is bir:BTypeString) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", STRING_VALUE));
                fv.visitEnd();
            } else if (bType is bir:BTypeBoolean) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, "Z");
                fv.visitEnd();
            } else if (bType is bir:BTypeByte) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, "B");
                fv.visitEnd();
            } else if (bType is bir:BTypeNil) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", OBJECT));
                fv.visitEnd();
            } else if (bType is bir:BMapType) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", MAP_VALUE));
                fv.visitEnd();
            } else if (bType is bir:BRecordType) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", MAP_VALUE));
                fv.visitEnd();
            } else if (bType is bir:BArrayType) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", ARRAY_VALUE));
                fv.visitEnd();
            } else if (bType is bir:BErrorType) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", ERROR_VALUE));
                fv.visitEnd();
            } else if (bType is bir:BObjectType) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", OBJECT_VALUE));
                fv.visitEnd();
            } else if (bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BUnionType) {
                jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, io:sprintf("L%s;", OBJECT));
                fv.visitEnd();
            } else {
                error err = error( "JVM generation is not supported for type " +
                                            io:sprintf("%s", bType));
                panic err;
            }

            k = k + 1;
        }

        jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, "state", "I");
        fv.visitEnd();

        cw.visitEnd();
        pkgEntries[frameName + ".class"] = cw.toByteArray();
    }
}

function generateDefaultConstructor(jvm:ClassWriter cw) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, OBJECT, "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
}
