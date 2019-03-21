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

function generateMethod(bir:Function func, jvm:ClassWriter cw, bir:Package module) {

    string currentPackageName = getPackageName(module.org.value, module.name.value);

    BalToJVMIndexMap indexMap = new;
    string funcName = cleanupFunctionName(untaint func.name.value);

    int returnVarRefIndex = -1;

    // generate method desc
    string desc = getMethodDesc(func);
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, funcName, desc, (), ());
    mv.visitCode();

    if (isModuleInitFunction(module, func)) {
        // invoke all init functions
        generateInitFunctionInvocation(module, mv);
        generateUserDefinedTypes(mv, module.typeDefs);
    }

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

    bir:VariableDcl?[] localVars = func.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
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
        bir:VariableDcl varDcl = getVariableDcl(localVars[0]);
        returnVarRefIndex = indexMap.getIndex(varDcl);
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
    bir:BasicBlock?[] basicBlocks = func.basicBlocks;

    jvm:Label[] lables = [];
    int[] states = [];

    int i = 0;
    while (i < basicBlocks.length()) {
        bir:BasicBlock bb = getBasicBlock(basicBlocks[i]);
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
        bir:BasicBlock bb = getBasicBlock(basicBlocks[j]);
        //io:println("Basic Block Is : ", bb.id.value);
        string currentBBName = io:sprintf("%s", bb.id.value);

        // create jvm label
        jvm:Label bbLabel = labelGen.getLabel(funcName + bb.id.value);
        mv.visitLabel(bbLabel);

        // generate instructions
        int m = 0;
        while (m < bb.instructions.length()) {
            bir:Instruction? inst = bb.instructions[m];
            InstructionGenerator instGen = new(mv, indexMap, currentPackageName);
            if (inst is bir:ConstantLoad) {
                instGen.generateConstantLoadIns(inst);
            } else if (inst is bir:Move) {
                if (inst.kind == "TYPE_CAST") {
                    instGen.generateCastIns(inst);
                } else {
                    instGen.generateMoveIns(inst);
                }
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
            } else if (inst is bir:TypeTest) {
                instGen.generateTypeTestIns(inst);
            } else {
                error err = error("JVM generation is not supported for operation " + io:sprintf("%s", inst));
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
            termGen.genCallTerm(terminator, funcName);
        } else if (terminator is bir:Branch) {
            termGen.genBranchTerm(terminator, funcName);
        } else if (terminator is bir:Return) {
            termGen.genReturnTerm(terminator, returnVarRefIndex, func);
        }
        j += 1;
    }

    var frameName = currentPackageName + funcName + "Frame";
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
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
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
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTupleType) {
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
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
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
        } else if (bType is bir:BMapType ||
                    bType is bir:BRecordType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", MAP_VALUE));
        } else if (bType is bir:BArrayType || 
                    bType is bir:BTupleType) {
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
                    bType is bir:BUnionType) {
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
                bType is bir:BRecordType ||
                bType is bir:BTupleType) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
        panic err;
    }
}

function getMethodDesc(bir:Function func) returns string {
    string desc = "(Lorg/ballerinalang/jvm/Strand;";
    int i = 0;
    while (i < func.argsCount) {
        desc = desc + getArgTypeSignature(func.typeValue.paramTypes[i]);
        i += 1;
    }
    string returnType = generateReturnType(func.typeValue.retType);
    desc =  desc + returnType;

    return desc;
}

function getArgTypeSignature(bir:BType bType) returns string {
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
    } else if (bType is bir:BArrayType || bType is bir:BTupleType) {
        return io:sprintf("L%s;", ARRAY_VALUE );
    } else if (bType is bir:BErrorType) {
        return io:sprintf("L%s;", ERROR_VALUE);
    } else if (bType is bir:BTypeAny || bType is bir:BTypeAnyData || bType is bir:BUnionType) {
        return io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        return io:sprintf("L%s;", MAP_VALUE);
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
    } else if (bType is bir:BArrayType ||
                bType is bir:BTupleType) {
        return io:sprintf(")L%s;", ARRAY_VALUE);
    } else if (bType is bir:BMapType || 
                bType is bir:BRecordType) {
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

function getMainFunc(bir:Function?[] funcs) returns bir:Function? {
    bir:Function? userMainFunc = ();
    foreach var func in funcs {
        if (func is bir:Function && func.name.value == "main") {
            userMainFunc = untaint func;
            break;
        }
    }

    return userMainFunc;
}

function generateMainMethod(bir:Function userMainFunc, jvm:ClassWriter cw, bir:Package pkg) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", (), ());

    string pkgName = getPackageName(pkg.org.value, pkg.name.value);
    string mainClass = lookupFullQualifiedClassName(pkgName + userMainFunc.name.value);

    if (hasInitFunction(pkg)) {
        string initFuncName = cleanupFunctionName(getModuleInitFuncName(pkg));
        mv.visitTypeInsn(NEW, "org/ballerinalang/jvm/Strand");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "org/ballerinalang/jvm/Strand", "<init>", "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, mainClass, initFuncName, 
                "(Lorg/ballerinalang/jvm/Strand;)V", false);
    }

    boolean isVoidFunction = userMainFunc.typeValue.retType is bir:BTypeNil;

    if (!isVoidFunction) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    }

    string desc = getMethodDesc(userMainFunc);
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
        generateParamCast(paramIndex, paramType, mv);
        paramIndex += 1;
    }

    // invoke the user's main method
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

function hasInitFunction(bir:Package pkg) returns boolean {
    foreach var func in pkg.functions {
        if (func is bir:Function && isModuleInitFunction(pkg, func)) {
            return true;
        }
    }
    return false;
}

function isModuleInitFunction(bir:Package module, bir:Function func) returns boolean {
    string moduleInit = getModuleInitFuncName(module);
    return func.name.value == moduleInit;
}

function getModuleInitFuncName(bir:Package module) returns string {
    string orgName = module.org.value;
    string moduleName = module.name.value;
    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        return orgName  + "/" + moduleName + ":" + module.versionValue.value + ".<init>";
    } else {
        return "..<init>";
    }
}

function generateInitFunctionInvocation(bir:Package pkg, jvm:MethodVisitor mv) {
    foreach var mod in pkg.importModules {
        bir:Package importedPkg = lookupModule(mod, currentBIRContext);
        if (hasInitFunction(importedPkg)) {
            string initFuncName = cleanupFunctionName(getModuleInitFuncName(importedPkg));
            string moduleClassName = getModuleLevelClassName(importedPkg.org.value, importedPkg.name.value,
                                                                importedPkg.name.value);
            mv.visitTypeInsn(NEW, "org/ballerinalang/jvm/Strand");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "org/ballerinalang/jvm/Strand", "<init>", "()V", false);
            mv.visitMethodInsn(INVOKESTATIC, moduleClassName, initFuncName,
                    "(Lorg/ballerinalang/jvm/Strand;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
        }
        generateInitFunctionInvocation(importedPkg, mv);
    }
}

function generateParamCast(int paramIndex, bir:BType targetType, jvm:MethodVisitor mv) {
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
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);

    foreach var func in pkg.functions {
        var currentFunc = getFunction(untaint func);
        var frameClassName = pkgName + cleanupFunctionName(currentFunc.name.value) + "Frame";
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, frameClassName, (), OBJECT_VALUE, ());
        generateDefaultConstructor(cw);

        int k = 0;
        bir:VariableDcl?[] localVars = currentFunc.localVars;
        while (k < localVars.length()) {
            bir:VariableDcl localVar = getVariableDcl(localVars[k]);
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
            } else if (bType is bir:BArrayType ||
                        bType is bir:BTupleType) {
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
        pkgEntries[frameClassName + ".class"] = cw.toByteArray();
    }
}

function generateDefaultConstructor(jvm:ClassWriter cw) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", (), ());
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, OBJECT, "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
}

function cleanupFunctionName(string functionName) returns string {
    return functionName.replaceAll("[\\.:/<>]", "_");
}

function getVariableDcl(bir:VariableDcl? localVar) returns bir:VariableDcl {
    if (localVar is bir:VariableDcl) {
        return localVar;
    } else {
        error err = error("Invalid variable declarion");
        panic err;
    }
}

function getBasicBlock(bir:BasicBlock? bb) returns bir:BasicBlock {
    if (bb is bir:BasicBlock) {
        return bb;
    } else {
        error err = error("Invalid basic block");
        panic err;
    }
}

function getFunction(bir:Function? bfunction) returns bir:Function {
    if (bfunction is bir:Function) {
        return bfunction;
    } else {
        error err = error("Invalid function");
        panic err;
    }
}

function getTypeDef(bir:TypeDef? typeDef) returns bir:TypeDef {
    if (typeDef is bir:TypeDef) {
        return typeDef;
    } else {
        error err = error("Invalid type definition");
        panic err;
    }
}

function getObjectField(bir:BObjectField? objectField) returns bir:BObjectField {
    if (objectField is bir:BObjectField) {
        return objectField;
    } else {
        error err = error("Invalid object field");
        panic err;
    }
}

function getRecordField(bir:BRecordField? recordField) returns bir:BRecordField {
    if (recordField is bir:BRecordField) {
        return recordField;
    } else {
        error err = error("Invalid record field");
        panic err;
    }
}
