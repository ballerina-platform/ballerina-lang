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

string[] generatedInitFuncs = [];

function generateMethod(bir:Function func, jvm:ClassWriter cw, bir:Package module, bir:BType? attachedType = ()) {

    // skip code generation, if this is an extern function
    if (isExternFunc(func)) {
        return;
    }

    string currentPackageName = getPackageName(module.org.value, module.name.value);

    BalToJVMIndexMap indexMap = new;
    string funcName = cleanupFunctionName(untaint func.name.value);

    int returnVarRefIndex = -1;

    bir:VariableDcl stranVar = { typeValue: "string", // should be record
                                 name: { value: "srand" },
                                 kind: "ARG" };
    _ = indexMap.getIndex(stranVar);

    // generate method desc
    string desc = getMethodDesc(func.typeValue.paramTypes, func.typeValue.retType);
    int access = ACC_PUBLIC;
    int localVarOffset;
    if !(attachedType is ()) {
        localVarOffset = 1;

        // add the self as the first local var
        // TODO: find a better way
        bir:VariableDcl selfVar = { typeValue: "any",
                                    name: { value: "self" },
                                    kind: "ARG" };
        _ = indexMap.getIndex(selfVar);
    } else {
        localVarOffset = 0;
        access += ACC_STATIC;
    }

    jvm:MethodVisitor mv = cw.visitMethod(access, funcName, desc, (), ());
    InstructionGenerator instGen = new(mv, indexMap, currentPackageName);
    ErrorHandlerGenerator errorGen = new(mv, indexMap);

    mv.visitCode();

    if (isModuleInitFunction(module, func)) {
        // invoke all init functions
        generateInitFunctionInvocation(module, mv);
        generateUserDefinedTypes(mv);

        if (!"".equalsIgnoreCase(currentPackageName)) {
            mv.visitTypeInsn(NEW, typeOwnerClass);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, typeOwnerClass, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitLdcInsn(cleanupPackageName(currentPackageName));
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, io:sprintf("%s", VALUE_CREATOR), "addValueCreator",
                                    io:sprintf("(L%s;L%s;)V", STRING_VALUE, VALUE_CREATOR), false);
        }
    }

    // generate method body
    int k = 1;

    // set channel details to strand.
    // these channel info is required to notify datachannels, when there is a panic
    // we cannot set this during strand creation, because function call do not have this info.
    mv.visitVarInsn(ALOAD, localVarOffset);
    loadChannelDetails(mv, func.workerChannels);
    mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "updateChannelDetails", io:sprintf("([L%s;)V", CHANNEL_DETAILS), false);

    // panic if this strand is cancelled
    checkStrandCancelled(mv, localVarOffset);

    bir:FunctionParam?[] functionParams = [];
    bir:VariableDcl?[] localVars = func.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        var index = indexMap.getIndex(localVar);
        if (localVar.kind != "ARG") {
            bir:BType bType = localVar.typeValue;
            genDefaultValue(mv, bType, index);
        }
        if (localVar is bir:FunctionParam) {
            functionParams[functionParams.length()] =  localVar;
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

    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    jvm:Label resumeLable = labelGen.getLabel(funcName + "resume");
    mv.visitJumpInsn(IFGT, resumeLable);

    jvm:Label varinitLable = labelGen.getLabel(funcName + "varinit");
    mv.visitLabel(varinitLable);

    bir:VariableDcl varDcl = getVariableDcl(localVars[0]);
    returnVarRefIndex = indexMap.getIndex(varDcl);
    bir:BType returnType = func.typeValue.retType;
    genDefaultValue(mv, returnType, returnVarRefIndex);

    // uncomment to test yield
    // mv.visitFieldInsn(GETSTATIC, className, "i", "I");
    // mv.visitInsn(ICONST_1);
    // mv.visitInsn(IADD);
    // mv.visitFieldInsn(PUTSTATIC, className, "i", "I");

    // process basic blocks
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

    TerminatorGenerator termGen = new(mv, indexMap, labelGen, errorGen, module);

    int paramCounter = 0;
    int paramBBCounter = 0;
    while (paramCounter < functionParams.length()) {
        var funcParam = functionParams[paramCounter];
        if (funcParam is bir:FunctionParam && funcParam.hasDefaultExpr) {

            // Load boolean in the next parameter of the related parameter
            var isExistParam = getFunctionParam(functionParams[paramCounter + 1]);
            mv.visitVarInsn(ILOAD, indexMap.getIndex(isExistParam));

            // Gen the if not equal logic
            jvm:Label paramNextLabel = labelGen.getLabel(funcParam.name.value + "next");
            mv.visitJumpInsn(IFNE, paramNextLabel);

            bir:BasicBlock?[] bbArray = func.paramDefaultBBs[paramBBCounter];
            generateBasicBlocks(mv, bbArray, labelGen, errorGen, instGen, termGen, func, returnVarRefIndex,
                                stateVarIndex, localVarOffset, true, module, currentPackageName);
            mv.visitLabel(paramNextLabel);
            paramBBCounter += 1;
        }
        paramCounter += 2;
    }

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

    generateBasicBlocks(mv, basicBlocks, labelGen, errorGen, instGen, termGen, func, returnVarRefIndex, stateVarIndex,
                            localVarOffset, false, module, currentPackageName);

    string frameName = getFrameClassName(currentPackageName, funcName, attachedType);
    mv.visitLabel(resumeLable);
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "frames", "[Ljava/lang/Object;");
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitInsn(ICONST_1);
    mv.visitInsn(ISUB);
    mv.visitInsn(DUP_X1);
    mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, frameName);

    k = localVarOffset;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        var index = indexMap.getIndex(localVar);
        bir:BType bType = localVar.typeValue;
        mv.visitInsn(DUP);

        if (bType is bir:BTypeInt) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "J");
            mv.visitVarInsn(LSTORE, index);
        } else if (bType is bir:BTypeByte) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "I");
            mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BTypeFloat) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "D");
            mv.visitVarInsn(DSTORE, index);
        } else if (bType is bir:BTypeString) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", STRING_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeDecimal) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", DECIMAL_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeBoolean) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), "Z");
            mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BMapType || bType is bir:BRecordType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", MAP_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTableType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", TABLE_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BStreamType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", STREAM_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTupleType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", ARRAY_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", OBJECT_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BErrorType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", ERROR_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BFutureType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", FUTURE_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BInvokableType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", FUNCTION_POINTER));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeDesc) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", TYPEDESC_VALUE));
            mv.visitVarInsn(ASTORE, index);
        }   else if (bType is bir:BTypeNil ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BUnionType ||
                    bType is bir:BJSONType ||
                    bType is bir:BFiniteType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"), 
                    io:sprintf("L%s;", OBJECT));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BXMLType) {
            mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", XML_VALUE));
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


    k = localVarOffset;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        var index = indexMap.getIndex(localVar);
        mv.visitInsn(DUP);

        bir:BType bType = localVar.typeValue;
        if (bType is bir:BTypeInt) {
            mv.visitVarInsn(LLOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "J");
        } else if (bType is bir:BTypeByte) {
            mv.visitVarInsn(ILOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "I");
        } else if (bType is bir:BTypeFloat) {
            mv.visitVarInsn(DLOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "D");
        } else if (bType is bir:BTypeString) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", STRING_VALUE));
        } else if (bType is bir:BTypeDecimal) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", DECIMAL_VALUE));
        } else if (bType is bir:BTypeBoolean) {
            mv.visitVarInsn(ILOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"), "Z");
        } else if (bType is bir:BMapType ||
                    bType is bir:BRecordType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", MAP_VALUE));
        } else if (bType is bir:BTableType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", TABLE_VALUE));
        } else if (bType is bir:BStreamType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", STREAM_VALUE));
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTupleType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", ARRAY_VALUE));
        } else if (bType is bir:BErrorType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", ERROR_VALUE));
        } else if (bType is bir:BFutureType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", FUTURE_VALUE));
        } else if (bType is bir:BTypeDesc) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitTypeInsn(CHECKCAST, TYPEDESC_VALUE);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", TYPEDESC_VALUE));
        } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", OBJECT_VALUE));
        } else if (bType is bir:BInvokableType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", FUNCTION_POINTER));
        } else if (bType is bir:BTypeNil ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BUnionType ||
                    bType is bir:BJSONType ||
                    bType is bir:BFiniteType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", OBJECT));
        } else if (bType is bir:BXMLType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%","_"),
                    io:sprintf("L%s;", XML_VALUE));
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

    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "frames", "[Ljava/lang/Object;");
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitInsn(DUP_X1);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IADD);
    mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "resumeIndex", "I");
    mv.visitVarInsn(ALOAD, frameVarIndex);
    mv.visitInsn(AASTORE);

    termGen.genReturnTerm({pos:{}, kind:"RETURN"}, returnVarRefIndex, func);
    mv.visitMaxs(200, 400);
    mv.visitEnd();
}

function generateBasicBlocks(jvm:MethodVisitor mv, bir:BasicBlock?[] basicBlocks, LabelGenerator labelGen,
            ErrorHandlerGenerator errorGen, InstructionGenerator instGen, TerminatorGenerator termGen,
            bir:Function func, int returnVarRefIndex, int stateVarIndex, int localVarOffset, boolean isArg,
            bir:Package module, string currentPackageName) {
    int j = 0;
    string funcName = cleanupFunctionName(untaint func.name.value);

    // process error entries
    bir:ErrorEntry?[] errorEntries = func.errorEntries;
    bir:ErrorEntry? currentEE = ();
    jvm:Label endLabel = new;
    jvm:Label handlerLabel = new;
    jvm:Label jumpLabel = new;
    int errorEntryCnt = 0;
    if (errorEntries.length() > errorEntryCnt) {
        currentEE = errorEntries[errorEntryCnt];
    }

    while (j < basicBlocks.length()) {
        bir:BasicBlock bb = getBasicBlock(basicBlocks[j]);
        string currentBBName = io:sprintf("%s", bb.id.value);

        // create jvm label
        jvm:Label bbLabel = labelGen.getLabel(funcName + bb.id.value);
        mv.visitLabel(bbLabel);

        // generate instructions
        int m = 0;
        int insCount = bb.instructions.length();
        boolean isTrapped = currentEE is bir:ErrorEntry  && currentEE.trapBB.id.value == currentBBName;
        // Cases will be generate between instructions and terminator of the basic block. So if basic block is
        // trapped we need to generate two try catches as for instructions and terminator.
        if (isTrapped && insCount > 0) {
            endLabel = new;
            handlerLabel = new;
            jumpLabel = new;
            // start try for instructions.
            errorGen.generateTryInsForTrap(<bir:ErrorEntry>currentEE, endLabel, handlerLabel, jumpLabel);
        }
        while (m < insCount) {
            bir:Instruction? inst = bb.instructions[m];
            var pos = inst.pos;
            if (pos is bir:DiagnosticPos) {
                generateDiagnosticPos(pos, mv);
            }
            if (inst is bir:ConstantLoad) {
                instGen.generateConstantLoadIns(inst);
            } else if (inst is bir:Move) {
                if (inst.kind == bir:INS_KIND_TYPE_CAST) {
                    instGen.generateCastIns(inst);
                } else if (inst.kind == bir:INS_KIND_MOVE) {
                    instGen.generateMoveIns(inst);
                } else if (inst.kind == bir:INS_KIND_XML_SEQ_STORE) {
                    instGen.generateXMLStoreIns(inst);
                } else if (inst.kind == bir:INS_KIND_XML_LOAD_ALL) {
                    instGen.generateXMLLoadAllIns(inst);
                } else if (inst.kind == bir:INS_KIND_TYPEOF) {
                    instGen.generateTypeofIns(inst);
                } else if (inst.kind == bir:INS_KIND_NOT) {
                    instGen.generateNotIns(inst);
                } else if (inst.kind == bir:INS_KIND_NEGATE) {
                    instGen.generateNegateIns(inst);
                } else {
                    error err = error("JVM generation is not supported for operation " + io:sprintf("%s", inst));
                    panic err;
                }
            } else if (inst is bir:BinaryOp) {
                instGen.generateBinaryOpIns(inst);
            } else if (inst is bir:NewArray) {
                instGen.generateArrayNewIns(inst);
            } else if (inst is bir:NewMap) {
                instGen.generateMapNewIns(inst);
            } else if (inst is bir:NewTypeDesc) {
                instGen.generateNewTypedescIns(inst);
            } else if (inst is bir:NewTable) {
                instGen.generateTableNewIns(inst);
            } else if (inst is bir:NewStream) {
                instGen.generateStreamNewIns(inst);
            } else if (inst is bir:NewError) {
                instGen.generateNewErrorIns(inst);
            } else if (inst is bir:NewInstance) {
                instGen.generateObjectNewIns(inst, localVarOffset);
            } else if (inst is bir:FieldAccess) {
                if (inst.kind == bir:INS_KIND_MAP_STORE) {
                    instGen.generateMapStoreIns(inst);
                } else if (inst.kind == bir:INS_KIND_MAP_LOAD) {
                    instGen.generateMapLoadIns(inst);
                } else if (inst.kind == bir:INS_KIND_ARRAY_STORE) {
                    instGen.generateArrayStoreIns(inst);
                } else if (inst.kind == bir:INS_KIND_ARRAY_LOAD) {
                    instGen.generateArrayValueLoad(inst);
                } else if (inst.kind == bir:INS_KIND_OBJECT_STORE) {
                    instGen.generateObjectStoreIns(inst);
                } else if (inst.kind == bir:INS_KIND_OBJECT_LOAD) {
                    instGen.generateObjectLoadIns(inst);
                } else if (inst.kind == bir:INS_KIND_XML_ATTRIBUTE_STORE) {
                    instGen.generateXMLAttrStoreIns(inst);
                } else if (inst.kind == bir:INS_KIND_XML_ATTRIBUTE_LOAD) {
                    instGen.generateXMLAttrLoadIns(inst);
                } else if (inst.kind == bir:INS_KIND_XML_LOAD || inst.kind == bir:INS_KIND_XML_SEQ_LOAD) {
                    instGen.generateXMLLoadIns(inst);
                } else {
                    error err = error("JVM generation is not supported for operation " + io:sprintf("%s", inst));
                    panic err;
                }
            } else if (inst is bir:FPLoad) {
                instGen.generateFPLoadIns(inst);
            } else if (inst is bir:TypeTest) {
                 instGen.generateTypeTestIns(inst);
            } else if (inst is bir:NewXMLQName) {
                instGen.generateNewXMLQNameIns(inst);
            } else if (inst is bir:NewStringXMLQName) {
                instGen.generateNewStringXMLQNameIns(inst);
            } else if (inst is bir:NewXMLElement) {
                instGen.generateNewXMLElementIns(inst);
            } else if (inst is bir:NewXMLText) {
                if (inst.kind == bir:INS_KIND_NEW_XML_TEXT) {
                    instGen.generateNewXMLTextIns(inst);
                } else if (inst.kind == bir:INS_KIND_NEW_XML_COMMENT) {
                    instGen.generateNewXMLCommentIns(inst);
                } else {
                    error err = error("JVM generation is not supported for operation " + io:sprintf("%s", inst));
                    panic err;
                }
            } else if (inst is bir:NewXMLPI) {
                instGen.generateNewXMLProcIns(inst);
            } else {
                error err = error("JVM generation is not supported for operation " + io:sprintf("%s", inst));
                panic err;
            }
            m += 1;
        }

        // close the started try block with a catch statement for instructions.
        if (isTrapped && insCount > 0) {
            errorGen.generateCatchInsForTrap(<bir:ErrorEntry>currentEE, endLabel, handlerLabel, jumpLabel);
        }
        jvm:Label bbEndLable = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
        mv.visitLabel(bbEndLable);

        bir:Terminator terminator = bb.terminator;
        if (!isArg) {
            // SIPUSH range is (-32768 to 32767) so if the state index goes beyond that, need to use visitLdcInsn
            mv.visitIntInsn(SIPUSH, j);
            mv.visitVarInsn(ISTORE, stateVarIndex);
        }

        // process terminator
        boolean isTerminatorTrapped = false;
        if (!isArg || (isArg && !(terminator is bir:Return))) {
            if (isTrapped && !(terminator is bir:GOTO)) {
                isTerminatorTrapped = true;
                endLabel = new;
                handlerLabel = new;
                jumpLabel = new;
                // start try for terminator if current block is trapped.
                errorGen.generateTryInsForTrap(<bir:ErrorEntry>currentEE, endLabel, handlerLabel, jumpLabel);
            }
            generateDiagnosticPos(terminator.pos, mv);
            if (isModuleInitFunction(module, func) && terminator is bir:Return) {
                generateAnnotLoad(mv, module.typeDefs, getPackageName(module.org.value, module.name.value));
            }
            termGen.genTerminator(terminator, func, funcName, localVarOffset, returnVarRefIndex);
            if (isTerminatorTrapped) {
                // close the started try block with a catch statement for terminator.
                errorGen.generateCatchInsForTrap(<bir:ErrorEntry>currentEE, endLabel, handlerLabel, jumpLabel);
            }
        }

        // set next error entry after visiting current error entry.
        if (isTrapped) {
            errorEntryCnt = errorEntryCnt + 1;
            if (errorEntries.length() > errorEntryCnt) {
                currentEE = errorEntries[errorEntryCnt];
            }
        }

        var thenBB = terminator["thenBB"];
        if (thenBB is bir:BasicBlock) {
            genYieldCheck(mv, termGen.labelGen, thenBB, funcName, localVarOffset);
        }
        j += 1;
    }
}

function genYieldCheck(jvm:MethodVisitor mv, LabelGenerator labelGen, bir:BasicBlock thenBB, string funcName,
                        int localVarOffset) {
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "yield", "Z");
    jvm:Label yieldLabel = labelGen.getLabel(funcName + "yield");
    mv.visitJumpInsn(IFNE, yieldLabel);

    // goto thenBB
    jvm:Label gotoLabel = labelGen.getLabel(funcName + thenBB.id.value);
    mv.visitJumpInsn(GOTO, gotoLabel);
}

function generateLambdaMethod(bir:AsyncCall|bir:FPLoad ins, jvm:ClassWriter cw, string className, string lambdaName) {
    bir:BType? lhsType;
    string orgName;
    string moduleName;
    string funcName; 
    if (ins is bir:AsyncCall) {
        lhsType = ins.lhsOp.typeValue;
        orgName = ins.pkgID.org;
        moduleName = ins.pkgID.name;
        funcName = ins.name.value;
    } else {
        lhsType = ins.lhsOp.typeValue;
        orgName = ins.pkgID.org;
        moduleName = ins.pkgID.name;
        funcName = ins.name.value;
    }

    string lookupKey = getPackageName(orgName, moduleName) + funcName;
    string jvmClass = lookupFullQualifiedClassName(lookupKey);
    boolean isExternFunction = isBIRFunctionExtern(lookupKey);

    bir:BType returnType = bir:TYPE_NIL;
    if (lhsType is bir:BFutureType) {
        returnType = lhsType.returnType;
    } else if (lhsType is bir:BInvokableType) { 
        returnType = lhsType.retType;
    } else {
        error err = error( "JVM generation is not supported for async return type " +
                                        io:sprintf("%s", lhsType));
        panic err;
    }


    int closureMapsCount = 0;
    if (ins is bir:FPLoad) {
        closureMapsCount = ins.closureMaps.length();
    }
    string closureMapsDesc = getMapValueDesc(closureMapsCount);

    boolean isVoid = returnType is bir:BTypeNil;
    jvm:MethodVisitor mv;
    if (isVoid) {
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, lambdaName,
                                io:sprintf("(%s[L%s;)V", closureMapsDesc, OBJECT), (), ());
    } else {
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, lambdaName,
                                io:sprintf("(%s[L%s;)L%s;", closureMapsDesc, OBJECT, OBJECT), (), ());
    }

    mv.visitCode();
    // load strand as first arg
    // strand and other args are in a object[] param. This param comes after closure maps.
    // hence the closureMapsCount is equal to the array's param index.
    mv.visitVarInsn(ALOAD, closureMapsCount);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, STRAND);

    if (isExternStaticFunctionCall(ins)) {
        jvm:Label blockedOnExternLabel = new;

        mv.visitInsn(DUP);

        mv.visitFieldInsn(GETFIELD, STRAND, "blockedOnExtern", "Z");
        mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, STRAND, "blockedOnExtern", "Z");

        if (!isVoid) {
            mv.visitInsn(DUP);

            mv.visitFieldInsn(GETFIELD, STRAND, "returnValue", "Ljava/lang/Object;");
            mv.visitInsn(ARETURN);   
        } else {
            mv.visitInsn(RETURN);
        }

        mv.visitLabel(blockedOnExternLabel);
    }

    bir:BType?[] paramBTypes = [];
    if (ins is bir:AsyncCall) {
        bir:VarRef?[] paramTypes = ins.args;
        // load and cast param values
        int paramIndex = 1;
        int argIndex = 1;
        foreach var paramType in paramTypes {
            bir:VarRef ref = getVarRef(paramType);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, argIndex);
            mv.visitInsn(AALOAD);
            addUnboxInsn(mv, ref.typeValue);
            paramBTypes[paramIndex -1] = paramType.typeValue;
            paramIndex += 1;

            argIndex += 1;
            if (!isExternFunction) {
                addBooleanTypeToLambdaParamTypes(mv, 0, argIndex);
                paramBTypes[paramIndex -1] = "boolean";
                paramIndex += 1;
            }  
            argIndex += 1;
        }
    } else {
        //load closureMaps
        int i = 0;
        while (i < closureMapsCount) {
            mv.visitVarInsn(ALOAD, i);
            mv.visitInsn(ICONST_1);
            i += 1;
        }

        bir:VariableDcl?[] paramTypes = ins.params;
        // load and cast param values
        int paramIndex = 1;
        int argIndex = 1;
        foreach var paramType in paramTypes {
            bir:VariableDcl dcl = getVariableDcl(paramType);
            mv.visitVarInsn(ALOAD, closureMapsCount);
            mv.visitIntInsn(BIPUSH, argIndex);
            mv.visitInsn(AALOAD);
            addUnboxInsn(mv, dcl.typeValue);
            paramBTypes[paramIndex -1] = dcl.typeValue;
            paramIndex += 1;
            i += 1;
            argIndex += 1;
            
            if (!isExternFunction) {
                addBooleanTypeToLambdaParamTypes(mv, closureMapsCount, argIndex);
                paramBTypes[paramIndex -1] = "boolean";
                paramIndex += 1;
            } 
            argIndex += 1; 
        }
    }

    mv.visitMethodInsn(INVOKESTATIC, jvmClass, funcName, getLambdaMethodDesc(paramBTypes, returnType, closureMapsCount), false);
    
    if (isVoid) {
        mv.visitInsn(RETURN);
    } else {
        addBoxInsn(mv, returnType);
        mv.visitInsn(ARETURN);
    }

    mv.visitMaxs(0,0);
    mv.visitEnd();
}

function addBooleanTypeToLambdaParamTypes(jvm:MethodVisitor mv, int arrayIndex, int paramIndex) {
    mv.visitVarInsn(ALOAD, arrayIndex);
    mv.visitIntInsn(BIPUSH, paramIndex);
    mv.visitInsn(AALOAD);
    addUnboxInsn(mv, "boolean");
}

function genDefaultValue(jvm:MethodVisitor mv, bir:BType bType, int index) {
    if (bType is bir:BTypeInt) {
        mv.visitInsn(LCONST_0);
        mv.visitVarInsn(LSTORE, index);
    } else if (bType is bir:BTypeByte) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (bType is bir:BTypeFloat) {
        mv.visitInsn(DCONST_0);
        mv.visitVarInsn(DSTORE, index);
    } else if (bType is bir:BTypeString) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (bType is bir:BMapType ||
                bType is bir:BArrayType ||
                bType is bir:BTableType ||
                bType is bir:BStreamType ||
                bType is bir:BErrorType ||
                bType is bir:BTypeNil ||
                bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BObjectType ||
                bType is bir:BServiceType ||
                bType is bir:BTypeDecimal ||
                bType is bir:BUnionType ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BFutureType ||
                bType is bir:BJSONType ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeDesc) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
        panic err;
    }
}

function loadDefaultValue(jvm:MethodVisitor mv, bir:BType bType) {
    if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
        mv.visitInsn(LCONST_0);
    } else if (bType is bir:BTypeFloat) {
        mv.visitInsn(DCONST_0);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitInsn(ICONST_0);
    } else if (bType is bir:BTypeString ||
                bType is bir:BMapType ||
                bType is bir:BArrayType ||
                bType is bir:BTableType ||
                bType is bir:BStreamType ||
                bType is bir:BErrorType ||
                bType is bir:BTypeNil ||
                bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BObjectType ||
                bType is bir:BUnionType ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BFutureType ||
                bType is bir:BJSONType ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeDesc) {
        mv.visitInsn(ACONST_NULL);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
        panic err;
    }
}

function getMethodDesc(bir:BType?[] paramTypes, bir:BType? retType, bir:BType? attachedType = ()) returns string {
    string desc = "(Lorg/ballerinalang/jvm/Strand;";

    if (attachedType is bir:BType) {
        desc = desc + getArgTypeSignature(attachedType);
    }

    int i = 0;
    while (i < paramTypes.length()) {
        bir:BType paramType = getType(paramTypes[i]);
        desc = desc + getArgTypeSignature(paramType);
        i += 1;
    }
    string returnType = generateReturnType(retType);
    desc =  desc + returnType;

    return desc;
}

function getLambdaMethodDesc(bir:BType?[] paramTypes, bir:BType? retType, int closureMapsCount) returns string {
    string desc = "(Lorg/ballerinalang/jvm/Strand;";
    int j = 0;
    while (j < closureMapsCount) {
        j += 1;
        desc = desc + "L" + MAP_VALUE + ";" + "Z";
    }

    int i = 0;
    while (i < paramTypes.length()) {
        bir:BType paramType = getType(paramTypes[i]);
        desc = desc + getArgTypeSignature(paramType);
        i += 1;
    }
    string returnType = generateReturnType(retType);
    desc =  desc + returnType;

    return desc;
}

function getArgTypeSignature(bir:BType bType) returns string {
    if (bType is bir:BTypeInt) {
        return "J";
    } else if (bType is bir:BTypeByte) {
        return "I";
    } else if (bType is bir:BTypeFloat) {
        return "D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf("L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        return io:sprintf("L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        return "Z";
    } else if (bType is bir:BTypeNil) {
        return io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BArrayType || bType is bir:BTupleType) {
        return io:sprintf("L%s;", ARRAY_VALUE );
    } else if (bType is bir:BErrorType) {
        return io:sprintf("L%s;", ERROR_VALUE);
    } else if (bType is bir:BTypeAnyData ||
                bType is bir:BUnionType ||
                bType is bir:BJSONType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeAny) {
        return io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        return io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BFutureType) {
        return io:sprintf("L%s;", FUTURE_VALUE);
    } else if (bType is bir:BTableType) {
        return io:sprintf("L%s;", TABLE_VALUE);
    } else if (bType is bir:BStreamType) {
        return io:sprintf("L%s;", STREAM_VALUE);
    } else if (bType is bir:BInvokableType) {
        return io:sprintf("L%s;", FUNCTION_POINTER);
    } else if (bType is bir:BTypeDesc) {
        return io:sprintf("L%s;", TYPEDESC_VALUE);
    } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
        return io:sprintf("L%s;", OBJECT_VALUE);
    } else if (bType is bir:BXMLType) {
        return io:sprintf("L%s;", XML_VALUE);
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
    } else if (bType is bir:BTypeByte) {
        return ")I";
    } else if (bType is bir:BTypeFloat) {
        return ")D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf(")L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        return io:sprintf(")L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        return ")Z";
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
    } else if (bType is bir:BTableType) {
        return io:sprintf(")L%s;", TABLE_VALUE);
    } else if (bType is bir:BStreamType) {
        return io:sprintf(")L%s;", STREAM_VALUE);
    } else if (bType is bir:BFutureType) {
        return io:sprintf(")L%s;", FUTURE_VALUE);
    } else if (bType is bir:BTypeDesc) {
        return io:sprintf(")L%s;", TYPEDESC_VALUE);
    } else if (bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BUnionType ||
                bType is bir:BJSONType ||
                bType is bir:BFiniteType) {
        return io:sprintf(")L%s;", OBJECT);
    } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
        return io:sprintf(")L%s;", OBJECT_VALUE);
    } else if (bType is bir:BInvokableType) {
        return io:sprintf(")L%s;", FUNCTION_POINTER);
    } else if (bType is bir:BXMLType) {
        return io:sprintf(")L%s;", XML_VALUE);
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

function createFunctionPointer(jvm:MethodVisitor mv, string class, string lambdaName, boolean isVoid, int closureMapCount) {
    mv.visitTypeInsn(NEW, FUNCTION_POINTER);
    mv.visitInsn(DUP);
    mv.visitInvokeDynamicInsn(class, lambdaName, isVoid, closureMapCount);

    // load null here for type, since these are fp's created for internal usages.
    mv.visitInsn(ACONST_NULL);

    if (isVoid) {
        mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, "<init>",
                            io:sprintf("(L%s;L%s;)V", CONSUMER, BTYPE), false);
    } else {
        mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, "<init>",
                            io:sprintf("(L%s;L%s;)V", FUNCTION, BTYPE), false);
    }
}

function generateMainMethod(bir:Function? userMainFunc, jvm:ClassWriter cw, bir:Package pkg,  string mainClass,
                            string initClass, boolean serviceEPAvailable) {

    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", (), ());

    BalToJVMIndexMap indexMap = new;
    ErrorHandlerGenerator errorGen = new(mv, indexMap);
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);

    boolean isVoidFunction = userMainFunc is bir:Function && userMainFunc.typeValue.retType is bir:BTypeNil;

    mv.visitTypeInsn(NEW, SCHEDULER);
    mv.visitInsn(DUP);
    mv.visitInsn(ICONST_4);

    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, "<init>", "(IZ)V", false);

if (hasInitFunction(pkg)) {
        string initFuncName = cleanupFunctionName(getModuleInitFuncName(pkg));
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // schedule the init method
        string lambdaName = io:sprintf("$lambda$%s$", initFuncName);

        // create FP value
        createFunctionPointer(mv, initClass, lambdaName, true, 0);

        // no parent strand
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_CONSUMER_METHOD,
            io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, FUTURE_VALUE), false);
        errorGen.printStackTraceFromFutureValue(mv);
        mv.visitInsn(POP);
    }

    if (userMainFunc is bir:Function) {
        mv.visitInsn(DUP);
        string desc = getMethodDesc(userMainFunc.typeValue.paramTypes, userMainFunc.typeValue.retType);
        bir:BType?[] paramTypes = userMainFunc.typeValue.paramTypes;

        mv.visitIntInsn(BIPUSH, paramTypes.length() + 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // first element of the args array will be set by the scheduler
        // load and cast param values
        int paramIndex = 0;
        int paramTypeIndex = 0;
        int argArrayIndex = 0;
        while (paramTypeIndex < paramTypes.length()) {
            var paramType = paramTypes[paramTypeIndex];
            bir:BType pType = getType(paramType);
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, paramIndex + 1);
            // need to catch last iteration, loop count get incremented by 2, due to defaultabal params
            if (userMainFunc.restParamExist && paramTypeIndex + 2 == paramTypes.length()) {
                // load VarArgs array
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, argArrayIndex);
                loadType(mv, pType);
                mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, "createVarArgsArray", 
                    io:sprintf("([L%s;IL%s;)L%s;", STRING_VALUE, ARRAY_TYPE, ARRAY_VALUE), false);
            } else {
                generateParamCast(argArrayIndex, pType, mv);
            }
            mv.visitInsn(AASTORE);
            paramIndex += 1;

            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, paramIndex + 1);
            mv.visitLdcInsn("true");
            mv.visitInsn(AASTORE);
            paramIndex += 1;
            argArrayIndex += 1;
            paramTypeIndex += 2;
        }

        // invoke the user's main method
        string lambdaName = "$lambda$main$";
        createFunctionPointer(mv, initClass, lambdaName, isVoidFunction, 0);

        // no parent strand
        mv.visitInsn(ACONST_NULL);
        //submit to the scheduler
        if (isVoidFunction) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_CONSUMER_METHOD,
                io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, FUTURE_VALUE), false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
                io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, FUTURE_VALUE), false);
            mv.visitInsn(DUP);
        }
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        mv.visitIntInsn(BIPUSH, 100);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitFieldInsn(PUTFIELD, STRAND, "frames", io:sprintf("[L%s;", OBJECT));
        errorGen.printStackTraceFromFutureValue(mv);
        mv.visitInsn(POP);

        // At this point we are done executing all the functions including asyncs
        if (!isVoidFunction) {
            // store future value
            bir:VariableDcl futureVar = { typeValue: "any",
                                    name: { value: "dummy" },
                                    kind: "ARG" };
            int futureVarIndex = indexMap.getIndex(futureVar);
            mv.visitVarInsn(ASTORE, futureVarIndex);
            jvm:Label jumpAfterPrint = new;
            mv.visitVarInsn(ALOAD, futureVarIndex);
            mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "result", io:sprintf("L%s;", OBJECT));

            mv.visitJumpInsn(IFNULL, jumpAfterPrint);

            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, futureVarIndex);
            mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "result", io:sprintf("L%s;", OBJECT));
            bir:BType returnType = userMainFunc.typeValue.retType;
            addUnboxInsn(mv, returnType);
            if (returnType is bir:BTypeInt) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
            } else if (returnType is bir:BTypeByte) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            } else if (returnType is bir:BTypeFloat) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(D)V", false);
            } else if (returnType is bir:BTypeBoolean) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
            } else {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", io:sprintf("(L%s;)V", OBJECT), false);
            }
            mv.visitLabel(jumpAfterPrint);
        }
    }

    scheduleStartMethod(mv, pkg, initClass, serviceEPAvailable, errorGen);
    
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
}

function scheduleStartMethod(jvm:MethodVisitor mv, bir:Package pkg, string initClass, boolean serviceEPAvailable, 
    ErrorHandlerGenerator errorGen) {
    // schedule the start method
    string startFuncName = cleanupFunctionName(getModuleStartFuncName(pkg));
    string startLambdaName = io:sprintf("$lambda$%s$", startFuncName);

    mv.visitIntInsn(BIPUSH, 1);
    mv.visitTypeInsn(ANEWARRAY, OBJECT);

    // create FP value
    createFunctionPointer(mv, initClass, startLambdaName, true, 0);

    // no parent strand
    mv.visitInsn(ACONST_NULL);
    mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_CONSUMER_METHOD,
        io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, FUTURE_VALUE), false);
    
    // need to set immortal=true and start the scheduler again
    if (serviceEPAvailable) {
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, SCHEDULER, "immortal", "Z");
    }
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
    mv.visitIntInsn(BIPUSH, 100);
    mv.visitTypeInsn(ANEWARRAY, OBJECT);
    mv.visitFieldInsn(PUTFIELD, STRAND, "frames", io:sprintf("[L%s;", OBJECT));
    errorGen.printStackTraceFromFutureValue(mv);
    mv.visitInsn(POP);
    
}

# Generate a lambda function to invoke ballerina main.
#
# + userMainFunc - ballerina main function
# + cw - class visitor
# + pkg - package
function generateLambdaForMain(bir:Function userMainFunc, jvm:ClassWriter cw, bir:Package pkg,
                               string mainClass, string initClass) {
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);
    bir:BType returnType = userMainFunc.typeValue.retType;
    boolean isVoidFunc = returnType is bir:BTypeNil;
    
    jvm:MethodVisitor mv;
    if (isVoidFunc) {
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$lambda$main$",
                            io:sprintf("([L%s;)V", OBJECT), (), ());
    } else {
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$lambda$main$",
                            io:sprintf("([L%s;)L%s;", OBJECT, OBJECT), (), ());
    }
    mv.visitCode();

    //load strand as first arg
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, STRAND);

    // load and cast param values
    bir:BType?[] paramTypes = userMainFunc.typeValue.paramTypes;

    int paramIndex = 1;
    foreach var paramType in paramTypes {
        bir:BType pType = getType(paramType);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, paramIndex);
        mv.visitInsn(AALOAD);
        if (userMainFunc.restParamExist && paramTypes.length() == paramIndex + 1) {
            addUnboxInsn(mv, pType);
        } else {
            castFromString(pType, mv);
        }
        paramIndex += 1;
    }

    mv.visitMethodInsn(INVOKESTATIC, mainClass, userMainFunc.name.value, getMethodDesc(paramTypes, returnType), false);
    if (isVoidFunc) {
        mv.visitInsn(RETURN);
    } else {
        addBoxInsn(mv, returnType);
        mv.visitInsn(ARETURN);
    }
    mv.visitMaxs(0,0);
    mv.visitEnd();
}

# Generate a lambda function to invoke ballerina main.
#
# + userMainFunc - ballerina main function
# + cw - class visitor
# + pkg - package
function generateLambdaForPackageInits(jvm:ClassWriter cw, bir:Package pkg,
                               string mainClass, string initClass) {
    //need to generate lambda for package Init as well, if exist
    if (hasInitFunction(pkg)) {
        string initFuncName = cleanupFunctionName(getModuleInitFuncName(pkg));
        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
            io:sprintf("$lambda$%s$", initFuncName),
            io:sprintf("([L%s;)V", OBJECT), (), ());
        mv.visitCode();

         //load strand as first arg
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND);
        mv.visitMethodInsn(INVOKESTATIC, initClass, initFuncName, io:sprintf("(L%s;)V", STRAND), false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();

        // generate another lambda for start function as well
        string startFuncName = cleanupFunctionName(getModuleStartFuncName(pkg));
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, 
            io:sprintf("$lambda$%s$", startFuncName),
            io:sprintf("([L%s;)V", OBJECT), (), ());
        mv.visitCode();

         //load strand as first arg
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND);
        mv.visitMethodInsn(INVOKESTATIC, initClass, startFuncName, io:sprintf("(L%s;)V", STRAND), false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();
    }
}

# Generate cast instruction from String to target type
# 
# + targetType - target type to be casted
# + mv - method visitor
function castFromString(bir:BType targetType, jvm:MethodVisitor mv) {
    mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    if (targetType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "parseLong", io:sprintf("(L%s;)J", STRING_VALUE), false);
    } else if (targetType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "parseInt", io:sprintf("(L%s;)I", STRING_VALUE), false);
    } else if (targetType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "parseDouble", io:sprintf("(L%s;)D", STRING_VALUE), false);
    } else if (targetType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "parseBoolean", io:sprintf("(L%s;)Z", STRING_VALUE), false);
    } else if (targetType is bir:BTypeDecimal) {
        mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", io:sprintf("(L%s;)V", STRING_VALUE), false);
    } else if (targetType is bir:BArrayType) {
        mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
    } else if (targetType is bir:BMapType) {
        mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
    } else if (targetType is bir:BTableType) {
        mv.visitTypeInsn(CHECKCAST, TABLE_VALUE);
    } else if (targetType is bir:BStreamType) {
        mv.visitTypeInsn(CHECKCAST, STREAM_VALUE);
    } else if (targetType is bir:BTypeAny ||
                targetType is bir:BTypeAnyData ||
                targetType is bir:BTypeNil ||
                targetType is bir:BUnionType ||
                targetType is bir:BTypeString) {
        // do nothing
        return;
    } else {
        error err = error("JVM generation is not supported for type " + io:sprintf("%s", targetType));
        panic err;
    }
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
// TODO: remove and use calculateModuleInitFuncName
function getModuleInitFuncName(bir:Package module) returns string {
     return calculateModuleInitFuncName(packageToModuleId(module));
}

function calculateModuleInitFuncName(bir:ModuleID id) returns string {
    return calculateModuleSpecialFuncName(id, "<init>");
}

function calculateModuleSpecialFuncName(bir:ModuleID id, string funcSuffix) returns string {
    string orgName = id.org;
    string moduleName = id.name;
    string versionValue = id.modVersion;

    string funcName;
    if (moduleName.equalsIgnoreCase(".")) {
       funcName = ".." + funcSuffix;
    } else if ("".equalsIgnoreCase(versionValue)) {
       funcName = moduleName + "." + funcSuffix;
    } else {
        funcName = moduleName + ":" + versionValue + "." + funcSuffix;
    }

    if (!orgName.equalsIgnoreCase("$anon")) {
        funcName = orgName  + "/" + funcName;
    }

    return funcName;
}
// TODO: remove and use calculateModuleStartFuncName
function getModuleStartFuncName(bir:Package module) returns string {
    return calculateModuleStartFuncName(packageToModuleId(module));
}

function calculateModuleStartFuncName(bir:ModuleID id) returns string {
    return calculateModuleSpecialFuncName(id, "<start>");
 }

function generateInitFunctionInvocation(bir:Package pkg, jvm:MethodVisitor mv) {
    foreach var mod in pkg.importModules {
        var id = importModuleToModuleId(mod);
        string initFuncName = cleanupFunctionName(calculateModuleInitFuncName(id));
        string startFuncName = cleanupFunctionName(calculateModuleStartFuncName(id));

        // skip the init function invocation is its already generated
        // by some other package
        if (isInitInvoked(initFuncName)) {
            continue;
        }

        string moduleClassName = getModuleLevelClassName(id.org, id.name, MODULE_INIT_CLASS_NAME);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, moduleClassName, initFuncName,
                "(Lorg/ballerinalang/jvm/Strand;)V", false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, moduleClassName, startFuncName,
                "(Lorg/ballerinalang/jvm/Strand;)V", false);

        generatedInitFuncs[generatedInitFuncs.length()] = initFuncName;
    }
}


function generateParamCast(int paramIndex, bir:BType targetType, jvm:MethodVisitor mv) {
    // load BValue array
    mv.visitVarInsn(ALOAD, 0);

    // load value[i]
    mv.visitLdcInsn(paramIndex);
    mv.visitInsn(L2I);
    mv.visitInsn(AALOAD);
}

function generateAnnotLoad(jvm:MethodVisitor mv, bir:TypeDef?[] typeDefs, string pkgName) {
    string typePkgName = ".";
    if (pkgName != "") {
        typePkgName = pkgName;
    }

    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;

        if (bType is bir:BFiniteType || bType is bir:BServiceType) {
            continue;
        }

        loadAnnots(mv, typePkgName, typeDef);
    }
}

function loadAnnots(jvm:MethodVisitor mv, string pkgName, bir:TypeDef typeDef) {
    string pkgClassName = pkgName == "." || pkgName == "" ? MODULE_INIT_CLASS_NAME :
                            lookupGlobalVarClassName(pkgName + ANNOTATION_MAP_NAME);
    mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, io:sprintf("L%s;", MAP_VALUE));
    mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
    loadExternalOrLocalType(mv, typeDef);
    mv.visitMethodInsn(INVOKESTATIC, io:sprintf("%s", ANNOTATION_UTILS), "processAnnotations",
        io:sprintf("(L%s;L%s;)V", MAP_VALUE, BTYPE), false);
}

type BalToJVMIndexMap object {
    private int localVarIndex = 0;
    private map<int> jvmLocalVarIndexMap = {};

    function add(bir:VariableDcl varDcl) {
        string varRefName = self.getVarRefName(varDcl);
        self.jvmLocalVarIndexMap[varRefName] = self.localVarIndex;

        bir:BType bType = varDcl.typeValue;

        if (bType is bir:BTypeInt ||
            bType is bir:BTypeFloat) {
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
        return varDcl.name.value;
    }
};

function generateFrameClasses(bir:Package pkg, map<byte[]> pkgEntries) {
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);

    foreach var func in pkg.functions {
        generateFrameClassForFunction(pkgName, func, pkgEntries);
    }

    foreach var typeDef in pkg.typeDefs {
        bir:Function?[]? attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            foreach var func in attachedFuncs {
                generateFrameClassForFunction(pkgName, func, pkgEntries, attachedType=typeDef.typeValue);
            }
        }
    }
}

function generateFrameClassForFunction (string pkgName, bir:Function? func, map<byte[]> pkgEntries,
                                        bir:BType? attachedType = ()) {
    bir:Function currentFunc = getFunction(untaint func);
    if (isExternFunc(currentFunc)) {
        return;
    }
    string frameClassName = getFrameClassName(pkgName, currentFunc.name.value, attachedType);
    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visitSource(currentFunc.pos.sourceFileName);
    currentClass = untaint frameClassName;
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, frameClassName, (), OBJECT, ());
    generateDefaultConstructor(cw, OBJECT);

    int k = 0;
    bir:VariableDcl?[] localVars = currentFunc.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        bir:BType bType = localVar.typeValue;
        var fieldName = localVar.name.value.replace("%","_");
        generateField(cw, bType, fieldName, false);
        k = k + 1;
    }

    jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, "state", "I");
    fv.visitEnd();

    cw.visitEnd();
    pkgEntries[frameClassName + ".class"] = cw.toByteArray();
}

function getFrameClassName(string pkgName, string funcName, bir:BType? attachedType) returns string {
    string frameClassName = pkgName;
    if (attachedType is bir:BObjectType) {
        frameClassName += cleanupTypeName(attachedType.name.value) + "_";
    } else if (attachedType is bir:BServiceType) {
        frameClassName += cleanupTypeName(attachedType.oType.name.value) + "_";
    } else if (attachedType is bir:BRecordType) {
        frameClassName += cleanupTypeName(attachedType.name.value) + "_";
    }

    return frameClassName + cleanupFunctionName(funcName) + "Frame";
}

# Cleanup type name by replacing '$' with '_'.
function cleanupTypeName(string name) returns string {
    return name.replace("$","_");
}

function cleanupBalExt(string name) returns string {
    return name.replace(BAL_EXTENSION, "");
}

function cleanupPathSeperators(string name) returns string {
   //TODO: should use file_path:getPathSeparator();
   return name.replace(WINDOWS_PATH_SEPERATOR, "-").replace(UNIX_PATH_SEPERATOR, "-");
}

function generateField(jvm:ClassWriter cw, bir:BType bType, string fieldName, boolean isPackage) {
    string typeSig;
    if (bType is bir:BTypeInt) {
        typeSig = "J";
    } else if (bType is bir:BTypeByte) {
        typeSig = "I";
    } else if (bType is bir:BTypeFloat) {
        typeSig = "D";
    } else if (bType is bir:BTypeString) {
        typeSig = io:sprintf("L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        typeSig = io:sprintf("L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        typeSig = "Z";
    } else if (bType is bir:BTypeNil) {
        typeSig = io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BMapType) {
        typeSig = io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BTableType) {
        typeSig = io:sprintf("L%s;", TABLE_VALUE);
    } else if (bType is bir:BStreamType) {
        typeSig = io:sprintf("L%s;", STREAM_VALUE);
    } else if (bType is bir:BRecordType) {
        typeSig = io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BArrayType ||
                bType is bir:BTupleType) {
        typeSig = io:sprintf("L%s;", ARRAY_VALUE);
    } else if (bType is bir:BErrorType) {
        typeSig = io:sprintf("L%s;", ERROR_VALUE);
    } else if (bType is bir:BFutureType) {
        typeSig = io:sprintf("L%s;", FUTURE_VALUE);
    } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
        typeSig = io:sprintf("L%s;", OBJECT_VALUE);
    } else if (bType is bir:BXMLType) {
        typeSig = io:sprintf("L%s;", XML_VALUE);
    } else if (bType is bir:BTypeDesc) {
        typeSig = io:sprintf("L%s;", TYPEDESC_VALUE);
    } else if (bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BUnionType ||
                bType is bir:BJSONType ||
                bType is bir:BFiniteType) {
        typeSig = io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BInvokableType) {
        typeSig = io:sprintf("L%s;", FUNCTION_POINTER);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                    io:sprintf("%s", bType));
        panic err;
    }

    jvm:FieldVisitor fv;
    if (isPackage) {
        fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, fieldName, typeSig);
    } else {
        fv = cw.visitField(ACC_PUBLIC, fieldName, typeSig);
    }
    fv.visitEnd();
}

function generateDefaultConstructor(jvm:ClassWriter cw, string ownerClass) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", (), ());
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, ownerClass, "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
}

function generateDiagnosticPos(bir:DiagnosticPos pos, jvm:MethodVisitor mv) {
    if (pos.sLine != 2147483648) {
        jvm:Label label = new;
        mv.visitLabel(label);
        mv.visitLineNumber(pos.sLine, label);
    }
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

function getFunctionParam(bir:FunctionParam? localVar) returns bir:FunctionParam {
    if (localVar is bir:FunctionParam) {
        return localVar;
    } else {
        error err = error("Invalid function parameter");
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

function isExternFunc(bir:Function func) returns boolean {
    return (func.flags & bir:NATIVE) == bir:NATIVE;
}

function getVarRef(bir:VarRef? varRef) returns bir:VarRef {
    if (varRef is bir:VarRef) {
        return varRef;
    } else {
        error err = error("Invalid variable reference");
        panic err;
    }
}

function getType(bir:BType? bType) returns bir:BType {
    if (bType is bir:BType) {
        return bType;
    } else {
        error err = error("Invalid type");
        panic err;
    }
}

function getMapValueDesc(int count) returns string{
    int i = count;
    string desc = "";
    while(i > 0) {
        desc = desc + "L" + MAP_VALUE + ";";
        i -= 1;
    }

    return desc;
}

function isInitInvoked(string item) returns boolean {
    foreach var listItem in generatedInitFuncs {
        if (listItem.equalsIgnoreCase(item)) {
            return true;
        }
    }

    return false;
}

function getFunctions(bir:Function?[]? functions) returns bir:Function?[] {
    if (functions is bir:Function?[]) {
        return functions;
    } else {
        error err = error(io:sprintf("Invalid functions: %s", functions));
        panic err;
    }
}

function checkStrandCancelled(jvm:MethodVisitor mv, int localVarOffset) {
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, STRAND, "cancel", "Z");
    jvm:Label notCancelledLabel = new;
    mv.visitJumpInsn(IFEQ, notCancelledLabel);
    mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createCancelledFutureError", io:sprintf("()L%s;", ERROR_VALUE), false);
    mv.visitInsn(ATHROW);
    
    mv.visitLabel(notCancelledLabel);
}
