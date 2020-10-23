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

import ballerina/bir;
import ballerina/io;
import ballerina/jvm;
import ballerina/stringutils;
import ballerina/java;

string[] generatedInitFuncs = [];
int nextId = -1;
int nextVarId = -1;

bir:BAttachedFunction errorRecInitFunc = {name:{value:"$$<init>"}, funcType:{retType:"()"}, flags:0};
bir:BRecordType detailRec = {name:{value:"detail"},
                                sealed:false,
                                restFieldType:"()",
                                initFunction:errorRecInitFunc,
                                typeFlags: (TYPE_FLAG_ANYDATA | TYPE_FLAG_PURETYPE)
                            };

bir:BErrorType errType = {name:{value:"error"},
                                moduleId:{org:BALLERINA,
                                name:BUILT_IN_PACKAGE_NAME},
                                reasonType:bir:TYPE_STRING,
                                detailType:detailRec};

bir:BUnionType errUnion = { members:["()", errType],
                            typeFlags: (TYPE_FLAG_NILABLE | TYPE_FLAG_PURETYPE)
                          };

function generateMethod(bir:Function birFunc,
                            jvm:ClassWriter cw,
                            bir:Package birModule,
                            bir:BType? attachedType = (),
                            boolean isService = false,
                            string serviceName = "") {
    if (isExternFunc(birFunc)) {
        genJMethodForBExternalFunc(birFunc, cw, birModule, attachedType = attachedType);
    } else {
        genJMethodForBFunc(birFunc, cw, birModule, isService, serviceName, attachedType = attachedType);
    }
}

function genJMethodForBFunc(bir:Function func,
                           jvm:ClassWriter cw,
                           bir:Package module,
                           boolean isService,
                           string serviceName,
                           bir:BType? attachedType = ()) {
    string currentPackageName = getPackageName(module.org.value, module.name.value);
    BalToJVMIndexMap indexMap = new;
    string funcName = cleanupFunctionName(<@untainted> func.name.value);
    boolean useBString = isBStringFunc(funcName);
    int returnVarRefIndex = -1;

    bir:VariableDcl stranVar = { typeValue: "string", // should be record
                                 name: { value: "srand" },
                                 kind: "ARG" };
    _ = indexMap.getIndex(stranVar);

    // generate method desc
    string desc = getMethodDesc(func.typeValue.paramTypes, <bir:BType?> func.typeValue?.retType, useBString = useBString);
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
    InstructionGenerator instGen = new(mv, indexMap, module);
    ErrorHandlerGenerator errorGen = new(mv, indexMap, currentPackageName);
    LabelGenerator labelGen = new();

    mv.visitCode();

    jvm:Label? tryStart = ();
    boolean isObserved = false;
    boolean isWorker = (func.flags & bir:WORKER) == bir:WORKER;
    boolean isRemote = (func.flags & bir:REMOTE) == bir:REMOTE;
    if ((isService || isRemote || isWorker) && funcName != "init" && funcName != "$init$") {
        // create try catch block to start and stop observability.
        isObserved = true;
        tryStart = labelGen.getLabel("try-start");
        mv.visitLabel(<jvm:Label>tryStart);
    }

    jvm:Label methodStartLabel = new;
    mv.visitLabel(methodStartLabel);

    // generate method body
    int k = 1;

    // set channel details to strand.
    // these channel info is required to notify datachannels, when there is a panic
    // we cannot set this during strand creation, because function call do not have this info.
    if (func.workerChannels.length() > 0) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        loadChannelDetails(mv, func.workerChannels);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "updateChannelDetails", io:sprintf("([L%s;)V", CHANNEL_DETAILS), false);
    }

    // panic if this strand is cancelled
    checkStrandCancelled(mv, localVarOffset);

    bir:VariableDcl?[] localVars = func.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        var index = indexMap.getIndex(localVar);
        if (localVar.kind != "ARG") {
            bir:BType bType = localVar.typeValue;
            genDefaultValue(mv, bType, index);
        }
        k += 1;
    }

    bir:VariableDcl varDcl = getVariableDcl(localVars[0]);
    returnVarRefIndex = indexMap.getIndex(varDcl);
    bir:BType returnType = <bir:BType> func.typeValue?.retType;
    genDefaultValue(mv, returnType, returnVarRefIndex);

    bir:VariableDcl stateVar = { typeValue: "string", //should  be javaInt
                                 name: { value: "state" },
                                 kind: "TEMP" };
    var stateVarIndex = indexMap.getIndex(stateVar);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, stateVarIndex);

    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "resumeIndex", "I");
    jvm:Label resumeLable = labelGen.getLabel(funcName + "resume");
    mv.visitJumpInsn(IFGT, resumeLable);

    jvm:Label varinitLable = labelGen.getLabel(funcName + "varinit");
    mv.visitLabel(varinitLable);

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
    int caseIndex = 0;
    while (i < basicBlocks.length()) {
        bir:BasicBlock bb = getBasicBlock(basicBlocks[i]);
        if(i == 0){
            lables[caseIndex] = labelGen.getLabel(funcName + bb.id.value);
            states[caseIndex] = caseIndex;
            caseIndex += 1;
        }
        lables[caseIndex] = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
        states[caseIndex] = caseIndex;
        caseIndex += 1;
        i = i + 1;
    }

    TerminatorGenerator termGen = new(mv, indexMap, labelGen, errorGen, module);

    // uncomment to test yield
    // mv.visitFieldInsn(GETSTATIC, className, "i", "I");
    // mv.visitIntInsn(BIPUSH, 100);
    // jvm:Label l0 = labelGen.getLabel(funcName + "l0");
    // mv.visitJumpInsn(IF_ICMPNE, l0);
    // mv.visitVarInsn(ALOAD, 0);
    // mv.visitInsn(ICONST_1);
    // mv.visitFieldInsn(PUTFIELD, "io/ballerina/runtime/scheduling/Strand", "yield", "Z");
    // termGen.genReturnTerm({kind:"RETURN"}, returnVarRefIndex, func);
    // mv.visitLabel(l0);

    mv.visitVarInsn(ILOAD, stateVarIndex);
    jvm:Label yieldLable = labelGen.getLabel(funcName + "yield");
    mv.visitLookupSwitchInsn(yieldLable, states, lables);

    generateBasicBlocks(mv, basicBlocks, labelGen, errorGen, instGen, termGen, func, returnVarRefIndex, stateVarIndex,
                            localVarOffset, false, module, currentPackageName, attachedType, isObserved, isService, serviceName, useBString = useBString);

    string frameName = getFrameClassName(currentPackageName, funcName, attachedType);
    mv.visitLabel(resumeLable);
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "frames", "[Ljava/lang/Object;");
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "resumeIndex", "I");
    mv.visitInsn(ICONST_1);
    mv.visitInsn(ISUB);
    mv.visitInsn(DUP_X1);
    mv.visitFieldInsn(PUTFIELD, "io/ballerina/runtime/scheduling/Strand", "resumeIndex", "I");
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, frameName);

    geerateFrameClassFieldLoad(localVars, mv, indexMap, frameName, useBString);
    mv.visitFieldInsn(GETFIELD, frameName, "state", "I");
    mv.visitVarInsn(ISTORE, stateVarIndex);
    mv.visitJumpInsn(GOTO, varinitLable);


    mv.visitLabel(yieldLable);
    mv.visitTypeInsn(NEW, frameName);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, frameName, "<init>", "()V", false);


    geerateFrameClassFieldUpdate(localVars, mv, indexMap, frameName, useBString);

    mv.visitInsn(DUP);
    mv.visitVarInsn(ILOAD, stateVarIndex);
    mv.visitFieldInsn(PUTFIELD, frameName, "state", "I");


    bir:VariableDcl frameVar = { typeValue: "string", // should be record or something
                                 name: { value: "frame" },
                                 kind: "TEMP" };
    var frameVarIndex = indexMap.getIndex(frameVar);
    mv.visitVarInsn(ASTORE, frameVarIndex);

    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "frames", "[Ljava/lang/Object;");
    mv.visitVarInsn(ALOAD, localVarOffset);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, "io/ballerina/runtime/scheduling/Strand", "resumeIndex", "I");
    mv.visitInsn(DUP_X1);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IADD);
    mv.visitFieldInsn(PUTFIELD, "io/ballerina/runtime/scheduling/Strand", "resumeIndex", "I");
    mv.visitVarInsn(ALOAD, frameVarIndex);
    mv.visitInsn(AASTORE);

    jvm:Label methodEndLabel = new;
    // generate the try catch finally to stop observing if an error occurs.
    if (isObserved) {
        jvm:Label tryEnd = labelGen.getLabel("try-end");
        jvm:Label tryCatch = labelGen.getLabel("try-handler");
        // visitTryCatchBlock visited at the end since order of the error table matters.
        mv.visitTryCatchBlock(<jvm:Label>tryStart, tryEnd, tryCatch, ERROR_VALUE);
        jvm:Label tryFinally = labelGen.getLabel("try-finally");
        mv.visitTryCatchBlock(<jvm:Label>tryStart, tryEnd, tryFinally, ());
        jvm:Label tryCatchFinally = labelGen.getLabel("try-catch-finally");
        mv.visitTryCatchBlock(tryCatch, tryCatchFinally, tryFinally, ());

        bir:VariableDcl catchVarDcl = { typeValue: "any", name: { value: "$_catch_$" } };
        int catchVarIndex = indexMap.getIndex(catchVarDcl);
        bir:VariableDcl throwableVarDcl = { typeValue: "any", name: { value: "$_throwable_$" } };
        int throwableVarIndex = indexMap.getIndex(throwableVarDcl);

        // Try-To-Finally
        mv.visitLabel(tryEnd);
        // emitStopObservationInvocation(mv, localVarOffset);
        jvm:Label tryBlock1 = labelGen.getLabel("try-block-1");
        mv.visitLabel(tryBlock1);
        mv.visitJumpInsn(GOTO, methodEndLabel);

        // Catch Block
        mv.visitLabel(tryCatch);
        mv.visitVarInsn(ASTORE, catchVarIndex);
        jvm:Label tryBlock2 = labelGen.getLabel("try-block-2");
        mv.visitLabel(tryBlock2);
        emitReportErrorInvocation(mv, localVarOffset, catchVarIndex);
        mv.visitLabel(tryCatchFinally);
        emitStopObservationInvocation(mv, localVarOffset);
        jvm:Label tryBlock3 = labelGen.getLabel("try-block-3");
        mv.visitLabel(tryBlock3);
        // re-throw caught error value
        mv.visitVarInsn(ALOAD, catchVarIndex);
        mv.visitInsn(ATHROW);

        // Finally Block
        mv.visitLabel(tryFinally);
        mv.visitVarInsn(ASTORE, throwableVarIndex);
        emitStopObservationInvocation(mv, localVarOffset);
        jvm:Label tryBlock4 = labelGen.getLabel("try-block-4");
        mv.visitLabel(tryBlock4);
        mv.visitVarInsn(ALOAD, throwableVarIndex);
        mv.visitInsn(ATHROW);
    }
    mv.visitLabel(methodEndLabel);
    termGen.genReturnTerm({pos:{}, kind:"RETURN"}, returnVarRefIndex, func);

    // Create Local Variable Table
    k = localVarOffset;
    // Add strand variable to LVT
    mv.visitLocalVariable("__strand", io:sprintf("L%s;", STRAND), methodStartLabel, methodEndLabel, localVarOffset);
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        jvm:Label startLabel = methodStartLabel;
        jvm:Label endLabel = methodEndLabel;
        var tmpBoolParam = localVar.typeValue is bir:BTypeBoolean && localVar.name.value.startsWith("%syn");
        if (!tmpBoolParam && (localVar.kind is bir:LocalVarKind || localVar.kind is bir:ArgVarKind)) {
            // local vars have visible range information
            if (localVar.kind is bir:LocalVarKind) {
                string startBBID = localVar.meta.startBBID;
                string endBBID = localVar.meta.endBBID;
                int insOffset = localVar.meta.insOffset;
                if (startBBID != "") {
                    startLabel = labelGen.getLabel(funcName + startBBID + "ins" + insOffset.toString());
                }
                if (endBBID != "") {
                    endLabel = labelGen.getLabel(funcName + endBBID + "beforeTerm");
                }
            }
            string metaVarName = localVar.meta.name;
            if (metaVarName != "" &&
                      // filter out compiler added vars
                      !((metaVarName.startsWith("$") && metaVarName.endsWith("$"))
                        || (metaVarName.startsWith("$$") && metaVarName.endsWith("$$"))
                        || metaVarName.startsWith("_$$_"))) {
                mv.visitLocalVariable(metaVarName, getJVMTypeSign(localVar.typeValue),
                                startLabel, endLabel, indexMap.getIndex(localVar));
            }
        }
        k = k + 1;
    }

    mv.visitMaxs(0, 0);
    mv.visitEnd();
}

function geerateFrameClassFieldLoad(bir:VariableDcl?[] localVars, jvm:MethodVisitor mv,
                                    BalToJVMIndexMap indexMap, string frameName, boolean useBString) {
    int k = 0;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        var index = indexMap.getIndex(localVar);
        bir:BType bType = localVar.typeValue;
        mv.visitInsn(DUP);

        if (bType is bir:BTypeInt) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "J");
            mv.visitVarInsn(LSTORE, index);
        } else if (bType is bir:BTypeByte) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
            mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BTypeFloat) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "D");
            mv.visitVarInsn(DSTORE, index);
        } else if (bType is bir:BTypeString) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", useBString ? B_STRING_VALUE : STRING_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeDecimal) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", DECIMAL_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeBoolean) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "Z");
            mv.visitVarInsn(ISTORE, index);
        } else if (bType is bir:BMapType || bType is bir:BRecordType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", MAP_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTableType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", TABLE_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BStreamType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", STREAM_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTupleType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", ARRAY_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", OBJECT_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BErrorType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", ERROR_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BFutureType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", FUTURE_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BInvokableType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", FUNCTION_POINTER));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeDesc) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", TYPEDESC_VALUE));
            mv.visitVarInsn(ASTORE, index);
        }   else if (bType is bir:BTypeNil ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BUnionType ||
                    bType is bir:BJSONType ||
                    bType is bir:BFiniteType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", OBJECT));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BXMLType) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", XML_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is bir:BTypeHandle) {
            mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", HANDLE_VALUE));
            mv.visitVarInsn(ASTORE, index);
        } else if (bType is jvm:JType) {
            generateFrameClassJFieldLoad(localVar, mv, index, frameName);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
            panic err;
        }
        k = k + 1;
    }

}

function generateFrameClassJFieldLoad(bir:VariableDcl localVar, jvm:MethodVisitor mv,
                                    int index, string frameName) {
    jvm:JType jType = <jvm:JType>localVar.typeValue;

    if (jType is jvm:JByte) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JChar) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JShort) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JInt) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JLong) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "J");
        mv.visitVarInsn(LSTORE, index);
    } else if (jType is jvm:JFloat) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "F");
        mv.visitVarInsn(FSTORE, index);
    } else if (jType is jvm:JDouble) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "D");
        mv.visitVarInsn(DSTORE, index);
    } else if (jType is jvm:JBoolean) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "Z");
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JArrayType ||
                jType is jvm:JRefType) {
        mv.visitFieldInsn(GETFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), getJTypeSignature(jType));
        mv.visitVarInsn(ASTORE, index);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                    io:sprintf("%s", jType));
        panic err;
    }

}

function geerateFrameClassFieldUpdate(bir:VariableDcl?[] localVars, jvm:MethodVisitor mv,
                                      BalToJVMIndexMap indexMap, string frameName, boolean useBString) {
    int k = 0;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        var index = indexMap.getIndex(localVar);
        mv.visitInsn(DUP);

        bir:BType bType = localVar.typeValue;
        if (bType is bir:BTypeInt) {
            mv.visitVarInsn(LLOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "J");
        } else if (bType is bir:BTypeByte) {
            mv.visitVarInsn(ILOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
        } else if (bType is bir:BTypeFloat) {
            mv.visitVarInsn(DLOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "D");
        } else if (bType is bir:BTypeString) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", useBString ? B_STRING_VALUE : STRING_VALUE));
        } else if (bType is bir:BTypeDecimal) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", DECIMAL_VALUE));
        } else if (bType is bir:BTypeBoolean) {
            mv.visitVarInsn(ILOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "Z");
        } else if (bType is bir:BMapType ||
                    bType is bir:BRecordType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", MAP_VALUE));
        } else if (bType is bir:BTableType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", TABLE_VALUE));
        } else if (bType is bir:BStreamType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", STREAM_VALUE));
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTupleType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", ARRAY_VALUE));
        } else if (bType is bir:BErrorType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", ERROR_VALUE));
        } else if (bType is bir:BFutureType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", FUTURE_VALUE));
        } else if (bType is bir:BTypeDesc) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitTypeInsn(CHECKCAST, TYPEDESC_VALUE);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", TYPEDESC_VALUE));
        } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", OBJECT_VALUE));
        } else if (bType is bir:BInvokableType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", FUNCTION_POINTER));
        } else if (bType is bir:BTypeNil ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BUnionType ||
                    bType is bir:BJSONType ||
                    bType is bir:BFiniteType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", OBJECT));
        } else if (bType is bir:BXMLType) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                    io:sprintf("L%s;", XML_VALUE));
        } else if (bType is bir:BTypeHandle) {
            mv.visitVarInsn(ALOAD, index);
            mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"),
                     io:sprintf("L%s;", HANDLE_VALUE));
        } else if (bType is jvm:JType) {
            generateFrameClassJFieldUpdate(localVar, mv, index, frameName);
        } else {
            error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
            panic err;
        }
        k = k + 1;
    }
}

function generateFrameClassJFieldUpdate(bir:VariableDcl localVar, jvm:MethodVisitor mv,
                                      int index, string frameName) {
    bir:BType jType = <jvm:JType>localVar.typeValue;
    if (jType is jvm:JByte) {
        mv.visitVarInsn(ILOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "B");
    } else if (jType is jvm:JChar) {
        mv.visitVarInsn(ILOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "C");
    } else if (jType is jvm:JShort) {
        mv.visitVarInsn(ILOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "S");
    } else if (jType is jvm:JInt) {
        mv.visitVarInsn(ILOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "I");
    } else if (jType is jvm:JLong) {
        mv.visitVarInsn(LLOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "J");
    } else if (jType is jvm:JFloat) {
        mv.visitVarInsn(FLOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "F");
    } else if (jType is jvm:JDouble) {
        mv.visitVarInsn(DLOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "D");
    } else if (jType is jvm:JBoolean) {
        mv.visitVarInsn(ILOAD, index);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), "Z");
    } else if (jType is jvm:JArrayType ||
                jType is jvm:JRefType) {
        string classSig = getJTypeSignature(jType);
        string className = getSignatureForJType(jType);
        mv.visitVarInsn(ALOAD, index);
        mv.visitTypeInsn(CHECKCAST, className);
        mv.visitFieldInsn(PUTFIELD, frameName, stringutils:replace(localVar.name.value, "%","_"), classSig);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                    io:sprintf("%s", jType));
        panic err;
    }
}

function getJVMTypeSign(bir:BType bType) returns string {
    string jvmType = "";
    if (bType is bir:BTypeInt) {
        jvmType = "J";
    } else if (bType is bir:BTypeByte) {
        jvmType = "I";
    } else if (bType is bir:BTypeFloat) {
        jvmType = "D";
    } else if (bType is bir:BTypeBoolean) {
        jvmType = "Z";
    } else if (bType is bir:BTypeString) {
        jvmType = io:sprintf("L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        jvmType = io:sprintf("L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        jvmType = io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BTableType) {
        jvmType = io:sprintf("L%s;", TABLE_VALUE);
    } else if (bType is bir:BStreamType) {
        jvmType = io:sprintf("L%s;", STREAM_VALUE);
    } else if (bType is bir:BArrayType ||
                bType is bir:BTupleType) {
        jvmType = io:sprintf("L%s;", ARRAY_VALUE);
    } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
        jvmType = io:sprintf("L%s;", OBJECT_VALUE);
    } else if (bType is bir:BErrorType) {
        jvmType = io:sprintf("L%s;", ERROR_VALUE);
    } else if (bType is bir:BFutureType) {
        jvmType = io:sprintf("L%s;", FUTURE_VALUE);
    } else if (bType is bir:BInvokableType) {
        jvmType = io:sprintf("L%s;", FUNCTION_POINTER);
    } else if (bType is bir:BTypeHandle) {
        jvmType = io:sprintf("L%s;", HANDLE_VALUE);
    } else if (bType is bir:BTypeDesc) {
        jvmType = io:sprintf("L%s;", TYPEDESC_VALUE);
    }   else if (bType is bir:BTypeNil
            || bType is bir:BTypeAny
            || bType is bir:BTypeAnyData
            || bType is bir:BUnionType
            || bType is bir:BJSONType
            || bType is bir:BFiniteType) {
        jvmType = io:sprintf("L%s;", OBJECT);
    } else if (bType is jvm:JType) {
        jvmType = getJTypeSignature(bType);
    } else if (bType is bir:BXMLType) {
        jvmType = io:sprintf("L%s;", XML_VALUE);
    }
    return jvmType;
}

function generateBasicBlocks(jvm:MethodVisitor mv, bir:BasicBlock?[] basicBlocks, LabelGenerator labelGen,
            ErrorHandlerGenerator errorGen, InstructionGenerator instGen, TerminatorGenerator termGen,
            bir:Function func, int returnVarRefIndex, int stateVarIndex, int localVarOffset, boolean isArg,
            bir:Package module, string currentPackageName, bir:BType? attachedType, boolean isObserved = false,
            boolean isService = false, string serviceName = "", boolean useBString = false) {
    int j = 0;
    string funcName = cleanupFunctionName(<@untainted> func.name.value);

    int caseIndex = 0;

    while (j < basicBlocks.length()) {
        bir:BasicBlock bb = getBasicBlock(basicBlocks[j]);
        string currentBBName = io:sprintf("%s", bb.id.value);

        // create jvm label
        jvm:Label bbLabel = labelGen.getLabel(funcName + bb.id.value);
        mv.visitLabel(bbLabel);
        if (j == 0 && !isArg) {
            // SIPUSH range is (-32768 to 32767) so if the state index goes beyond that, need to use visitLdcInsn
            mv.visitIntInsn(SIPUSH, caseIndex);
            mv.visitVarInsn(ISTORE, stateVarIndex);
            caseIndex += 1;
        }

        string serviceOrConnectorName = serviceName;
        if (isObserved && j == 0) {
            string observationStartMethod = isService ? "startResourceObservation" : "startCallableObservation";
            if !isService && attachedType is bir:BObjectType {
                // add module org and module name to remote spans.
                serviceOrConnectorName = getFullQualifiedRemoteFunctionName(
                                attachedType.moduleId.org, attachedType.moduleId.name, serviceName);
            }
            emitStartObservationInvocation(mv, localVarOffset, serviceOrConnectorName, funcName, observationStartMethod);
        }

        // generate instructions
        int m = 0;
        int insCount = bb.instructions.length();

        int insKind;
        while (m < insCount) {
            jvm:Label insLabel = labelGen.getLabel(funcName + bb.id.value + "ins" + m.toString());
            mv.visitLabel(insLabel);
            bir:Instruction? inst = bb.instructions[m];
            if (inst is ()) {
                continue;
            } else {
                insKind = inst.kind;
                generateDiagnosticPos(inst.pos, mv);
            }

            if (insKind <= bir:BINARY_BITWISE_UNSIGNED_RIGHT_SHIFT) {
                instGen.generateBinaryOpIns(<bir:BinaryOp> inst);
            } else if (insKind <= bir:INS_KIND_TYPE_CAST) {
                if (insKind == bir:INS_KIND_MOVE) {
                    instGen.generateMoveIns(<bir:Move> inst);
                } else if (insKind == bir:INS_KIND_CONST_LOAD) {
                    instGen.generateConstantLoadIns(<bir:ConstantLoad> inst, useBString);
                } else if (insKind == bir:INS_KIND_NEW_MAP) {
                    instGen.generateMapNewIns(<bir:NewMap> inst, localVarOffset);
                } else if (insKind == bir:INS_KIND_NEW_INST) {
                    instGen.generateObjectNewIns(<bir:NewInstance> inst, localVarOffset);
                } else if (insKind == bir:INS_KIND_MAP_STORE) {
                    instGen.generateMapStoreIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_NEW_ARRAY) {
                    instGen.generateArrayNewIns(<bir:NewArray> inst, useBString);
                } else if (insKind == bir:INS_KIND_ARRAY_STORE) {
                    instGen.generateArrayStoreIns(<bir:FieldAccess> inst, useBString);
                } else if (insKind == bir:INS_KIND_MAP_LOAD) {
                    instGen.generateMapLoadIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_ARRAY_LOAD) {
                    instGen.generateArrayValueLoad(<bir:FieldAccess> inst, useBString);
                } else if (insKind == bir:INS_KIND_NEW_ERROR) {
                    instGen.generateNewErrorIns(<bir:NewError> inst);
                } else {
                    instGen.generateCastIns(<bir:TypeCast> inst, useBString);
                }
            } else if (insKind <= bir:INS_KIND_NEW_STRING_XML_QNAME) {
                if (insKind == bir:INS_KIND_IS_LIKE) {
                    instGen.generateIsLikeIns(<bir:IsLike> inst);
                } else if (insKind == bir:INS_KIND_TYPE_TEST) {
                    instGen.generateTypeTestIns(<bir:TypeTest> inst);
                } else if (insKind == bir:INS_KIND_OBJECT_STORE) {
                    instGen.generateObjectStoreIns(<bir:FieldAccess> inst, useBString);
                } else if (insKind == bir:INS_KIND_OBJECT_LOAD) {
                    instGen.generateObjectLoadIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_NEW_XML_ELEMENT) {
                    instGen.generateNewXMLElementIns(<bir:NewXMLElement> inst);
                } else if (insKind == bir:INS_KIND_NEW_XML_TEXT) {
                    instGen.generateNewXMLTextIns(<bir:NewXMLText> inst);
                } else if (insKind == bir:INS_KIND_NEW_XML_COMMENT) {
                    instGen.generateNewXMLCommentIns(<bir:NewXMLComment> inst);
                } else if (insKind == bir:INS_KIND_NEW_XML_PI) {
                    instGen.generateNewXMLProcIns(<bir:NewXMLPI> inst);
                } else if (insKind == bir:INS_KIND_NEW_XML_QNAME) {
                    instGen.generateNewXMLQNameIns(<bir:NewXMLQName> inst);
                } else {
                    instGen.generateNewStringXMLQNameIns(<bir:NewStringXMLQName> inst);
                } 
            } else if (insKind <= bir:INS_KIND_NEW_TABLE) {
                if (insKind == bir:INS_KIND_XML_SEQ_STORE) {
                    instGen.generateXMLStoreIns(<bir:XMLAccess> inst);
                } else if (insKind == bir:INS_KIND_XML_SEQ_LOAD) {
                    instGen.generateXMLLoadIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_XML_LOAD) {
                    instGen.generateXMLLoadIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_XML_LOAD_ALL) {
                    instGen.generateXMLLoadAllIns(<bir:XMLAccess> inst);
                } else if (insKind == bir:INS_KIND_XML_ATTRIBUTE_STORE) {
                    instGen.generateXMLAttrStoreIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_XML_ATTRIBUTE_LOAD) {
                    instGen.generateXMLAttrLoadIns(<bir:FieldAccess> inst);
                } else if (insKind == bir:INS_KIND_FP_LOAD) {
                    instGen.generateFPLoadIns(<bir:FPLoad> inst);
                } else if (insKind == bir:INS_KIND_STRING_LOAD) {
                    instGen.generateStringLoadIns(<bir:FieldAccess> inst);
                } else {
                    instGen.generateTableNewIns(<bir:NewTable> inst);
                }
            } else if (insKind <= bir:INS_KIND_NEGATE) {
                if (insKind == bir:INS_KIND_TYPEOF) {
                    instGen.generateTypeofIns(<bir:UnaryOp> inst);
                } else if (insKind == bir:INS_KIND_NOT) {
                    instGen.generateNotIns(<bir:UnaryOp> inst);
                } else if (insKind == bir:INS_KIND_NEW_TYPEDESC) {
                    instGen.generateNewTypedescIns(<bir:NewTypeDesc> inst);
                } else {
                    instGen.generateNegateIns(<bir:UnaryOp> inst);
                } 
            } else if (insKind == bir:INS_KIND_PLATFORM) {
                instGen.generatePlatformIns(<JInstruction>inst);
            } else {
                error err = error("JVM generation is not supported for operation " + io:sprintf("%s", inst));
                panic err;
            }
            m += 1;
        }

        jvm:Label bbEndLable = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
        mv.visitLabel(bbEndLable);

        bir:Terminator terminator = bb.terminator;
        if (!isArg) {
            // SIPUSH range is (-32768 to 32767) so if the state index goes beyond that, need to use visitLdcInsn
            mv.visitIntInsn(SIPUSH, caseIndex);
            mv.visitVarInsn(ISTORE, stateVarIndex);
            caseIndex += 1;
        }

        // process terminator
        boolean isTerminatorTrapped = false;
        if (!isArg || (isArg && !(terminator is bir:Return))) {
            generateDiagnosticPos(terminator.pos, mv);
            if (isModuleInitFunction(module, func) && terminator is bir:Return) {
                generateAnnotLoad(mv, module.typeDefs, getPackageName(module.org.value, module.name.value));
            }
            termGen.genTerminator(terminator, func, funcName, localVarOffset, returnVarRefIndex, attachedType, isObserved);
        }

        errorGen.generateTryCatch(func, funcName, bb, instGen, termGen, labelGen);

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
    mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isYielded", "()Z", false);
    jvm:Label yieldLabel = labelGen.getLabel(funcName + "yield");
    mv.visitJumpInsn(IFNE, yieldLabel);

    // goto thenBB
    jvm:Label gotoLabel = labelGen.getLabel(funcName + thenBB.id.value);
    mv.visitJumpInsn(GOTO, gotoLabel);
}

function generateLambdaMethod(bir:AsyncCall|bir:FPLoad ins, jvm:ClassWriter cw, string lambdaName) {
    bir:BType? lhsType;
    string orgName;
    string moduleName;
    string funcName;
    int paramIndex = 1;
    boolean isVirtual = ins is bir:AsyncCall &&  ins.isVirtual;
    if (ins is bir:AsyncCall) {
        lhsType = ins.lhsOp?.typeValue;
        orgName = ins.pkgID.org;
        moduleName = ins.pkgID.name;
        funcName = ins.name.value;
    } else {
        lhsType = ins.lhsOp.typeValue;
        orgName = ins.pkgID.org;
        moduleName = ins.pkgID.name;
        funcName = ins.name.value;
    }

    boolean isExternFunction =  isExternStaticFunctionCall(ins);
    boolean isBuiltinModule = isBallerinaBuiltinModule(orgName, moduleName);

    bir:BType returnType = bir:TYPE_NIL;
    if (lhsType is bir:BFutureType) {
        returnType = lhsType.returnType;
    } else if (ins is bir:FPLoad) {
        returnType = ins.retType;
        if (returnType is bir:BInvokableType) {
            returnType = <bir:BType> returnType?.retType;
        }
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

    jvm:MethodVisitor mv;
    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, cleanupFunctionName(lambdaName),
                            io:sprintf("(%s[L%s;)L%s;", closureMapsDesc, OBJECT, OBJECT), (), ());

    mv.visitCode();
    // load strand as first arg
    // strand and other args are in a object[] param. This param comes after closure maps.
    // hence the closureMapsCount is equal to the array's param index.
    mv.visitVarInsn(ALOAD, closureMapsCount);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, STRAND);

    if (isExternFunction) {
        jvm:Label blockedOnExternLabel = new;

        mv.visitInsn(DUP);

        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isBlockedOnExtern", "()Z", false);
        mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, STRAND, "blockedOnExtern", "Z");

        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND, "returnValue", "Ljava/lang/Object;");
        mv.visitInsn(ARETURN);

        mv.visitLabel(blockedOnExternLabel);
    }
    bir:BType?[] paramBTypes = [];

    if (ins is bir:AsyncCall) {
        bir:VarRef?[] paramTypes = ins.args;
        if (isVirtual) {
            genLoadDataForObjectAttachedLambdas(ins, mv, closureMapsCount, paramTypes , isBuiltinModule);
            int paramTypeIndex = 1;
            paramIndex = 2;
            while ( paramTypeIndex < paramTypes.length()) {
                generateObjectArgs(mv, paramIndex);
                paramTypeIndex += 1;
                paramIndex += 1;
                if (!isBuiltinModule) {
                    generateObjectArgs(mv, paramIndex);
                    paramIndex += 1;
                }
            }
        } else {
            // load and cast param values
            int argIndex = 1;
            foreach var paramType in paramTypes {
                bir:VarRef ref = getVarRef(paramType);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, argIndex);
                mv.visitInsn(AALOAD);
                addUnboxInsn(mv, ref.typeValue);
                paramBTypes[paramIndex -1] = paramType?.typeValue;
                paramIndex += 1;

                argIndex += 1;
                if (!isBuiltinModule) {
                    addBooleanTypeToLambdaParamTypes(mv, 0, argIndex);
                    paramBTypes[paramIndex -1] = "boolean";
                    paramIndex += 1;
                }
                argIndex += 1;
            }
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

            if (!isBuiltinModule) {
                addBooleanTypeToLambdaParamTypes(mv, closureMapsCount, argIndex);
                paramBTypes[paramIndex -1] = "boolean";
                paramIndex += 1;
            }
            argIndex += 1;
        }
    }

    if (isVirtual) {
        string methodDesc = io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
        mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);
    } else {
        string methodDesc = getLambdaMethodDesc(paramBTypes, returnType, closureMapsCount);
        string jvmClass = lookupFullQualifiedClassName(getPackageName(orgName, moduleName) + funcName);
        mv.visitMethodInsn(INVOKESTATIC, jvmClass, funcName, methodDesc, false);
    }

    if (!isVirtual) {
        addBoxInsn(mv, returnType);
    }
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();
}

function genLoadDataForObjectAttachedLambdas(bir:AsyncCall ins, jvm:MethodVisitor mv, int closureMapsCount,
                    bir:VarRef?[] paramTypes , boolean isBuiltinModule) {
    mv.visitInsn(POP);
    mv.visitVarInsn(ALOAD, closureMapsCount);
    mv.visitInsn(ICONST_1);
    bir:VarRef ref = getVarRef(ins.args[0]);
    mv.visitInsn(AALOAD);
    addUnboxInsn(mv, ref.typeValue);
    mv.visitVarInsn(ALOAD, closureMapsCount);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, STRAND);

    mv.visitLdcInsn(cleanupObjectTypeName(ins.name.value));
    int objectArrayLength = paramTypes.length() - 1;
    if (!isBuiltinModule) {
        mv.visitIntInsn(BIPUSH, objectArrayLength * 2);
    } else {
        mv.visitIntInsn(BIPUSH, objectArrayLength);
    }
    mv.visitTypeInsn(ANEWARRAY, OBJECT);
}

function generateObjectArgs(jvm:MethodVisitor mv, int paramIndex) {
    mv.visitInsn(DUP);
    mv.visitIntInsn(BIPUSH, paramIndex - 2);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitIntInsn(BIPUSH, paramIndex + 1);
    mv.visitInsn(AALOAD);
    mv.visitInsn(AASTORE);
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
                bType is bir:BTypeHandle ||
                bType is bir:BTypeDesc) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else if (bType is jvm:JType) {
        genJDefaultValue(mv, bType, index);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
        panic err;
    }
}

function genJDefaultValue(jvm:MethodVisitor mv, jvm:JType jType, int index) {
    if (jType is jvm:JByte) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JChar) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JShort) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JInt) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JLong) {
        mv.visitInsn(LCONST_0);
        mv.visitVarInsn(LSTORE, index);
    } else if (jType is jvm:JFloat) {
        mv.visitInsn(FCONST_0);
        mv.visitVarInsn(FSTORE, index);
    } else if (jType is jvm:JDouble) {
        mv.visitInsn(DCONST_0);
        mv.visitVarInsn(DSTORE, index);
    } else if (jType is jvm:JBoolean) {
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, index);
    } else if (jType is jvm:JArrayType ||
                jType is jvm:JRefType) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, index);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", jType));
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
                bType is bir:BTypeHandle ||
                bType is bir:BTypeDesc) {
        mv.visitInsn(ACONST_NULL);
    } else if (bType is jvm:JType) {
	    loadDefaultJValue(mv, bType);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", bType));
        panic err;
    }
}

function loadDefaultJValue(jvm:MethodVisitor mv, jvm:JType jType) {
    if (jType is jvm:JByte) {
        mv.visitInsn(ICONST_0);
    } else if (jType is jvm:JChar) {
        mv.visitInsn(ICONST_0);
    } else if (jType is jvm:JShort) {
        mv.visitInsn(ICONST_0);
    } else if (jType is jvm:JInt) {
        mv.visitInsn(ICONST_0);
    } else if (jType is jvm:JLong) {
        mv.visitInsn(LCONST_0);
    } else if (jType is jvm:JFloat) {
        mv.visitInsn(FCONST_0);
    } else if (jType is jvm:JDouble) {
        mv.visitInsn(DCONST_0);
    } else if (jType is jvm:JBoolean) {
        mv.visitInsn(ICONST_0);
    } else if (jType is jvm:JArrayType ||
                jType is jvm:JRefType) {
        mv.visitInsn(ACONST_NULL);
    } else {
        error err = error( "JVM generation is not supported for type " +
                                        io:sprintf("%s", jType));
        panic err;
    }
}

function getMethodDesc(bir:BType?[] paramTypes, bir:BType? retType, bir:BType? attachedType = (),
                        boolean isExtern = false, boolean useBString = false) returns string {
    string desc = "(Lio/ballerina/runtime/scheduling/Strand;";

    if (attachedType is bir:BType) {
        desc = desc + getArgTypeSignature(attachedType, useBString);
    }

    int i = 0;
    while (i < paramTypes.length()) {
        bir:BType paramType = getType(paramTypes[i]);
        desc = desc + getArgTypeSignature(paramType, useBString);
        i += 1;
    }
    string returnType = generateReturnType(retType, isExtern, useBString);
    desc =  desc + returnType;

    return desc;
}

function getLambdaMethodDesc(bir:BType?[] paramTypes, bir:BType? retType, int closureMapsCount) returns string {
    string desc = "(Lio/ballerina/runtime/scheduling/Strand;";
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

function getArgTypeSignature(bir:BType bType, boolean useBString = false) returns string {
    if (bType is bir:BTypeInt) {
        return "J";
    } else if (bType is bir:BTypeByte) {
        return "I";
    } else if (bType is bir:BTypeFloat) {
        return "D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf("L%s;", useBString ? B_STRING_VALUE : STRING_VALUE);
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
    } else if (bType is bir:BTypeHandle) {
        return io:sprintf("L%s;", HANDLE_VALUE);
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function generateReturnType(bir:BType? bType, boolean isExtern = false, boolean useBString = false) returns string {
    if (bType is ()|bir:BTypeNil) {
        if (isExtern) {
            return ")V";
        }
        return io:sprintf(")L%s;", OBJECT);
    } else if (bType is bir:BTypeInt) {
        return ")J";
    } else if (bType is bir:BTypeByte) {
        return ")I";
    } else if (bType is bir:BTypeFloat) {
        return ")D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf(")L%s;", useBString ? B_STRING_VALUE : STRING_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        return io:sprintf(")L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        return ")Z";
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
    } else if (bType is bir:BTypeHandle) {
        return io:sprintf(")L%s;", HANDLE_VALUE);
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function getMainFunc(bir:Function?[] funcs) returns bir:Function? {
    bir:Function? userMainFunc = ();
    foreach var func in funcs {
        if (func is bir:Function && func.name.value == "main") {
            userMainFunc = <@untainted> func;
            break;
        }
    }

    return userMainFunc;
}

function createFunctionPointer(jvm:MethodVisitor mv, string class, string lambdaName, int closureMapCount) {
    mv.visitTypeInsn(NEW, FUNCTION_POINTER);
    mv.visitInsn(DUP);
    mv.visitInvokeDynamicInsn(class, cleanupFunctionName(lambdaName), closureMapCount);

    // load null here for type, since these are fp's created for internal usages.
    mv.visitInsn(ACONST_NULL);

    mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, "<init>",
                        io:sprintf("(L%s;L%s;)V", FUNCTION, BTYPE), false);
}

function generateMainMethod(bir:Function? userMainFunc, jvm:ClassWriter cw, bir:Package pkg,  string mainClass,
                            string initClass, boolean serviceEPAvailable) {

    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", (), ());

    // check for java compatibility
    generateJavaCompatibilityCheck(mv);

    // set system properties
    initConfigurations(mv);
    // start all listeners
    startListeners(mv, serviceEPAvailable);

    // register a shutdown hook to call package stop() method.
    registerShutdownListener(mv, initClass);

    BalToJVMIndexMap indexMap = new;
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);
    ErrorHandlerGenerator errorGen = new(mv, indexMap, pkgName);

    // add main string[] args param first
    bir:VariableDcl argsVar = { typeValue: "any",
                                    name: { value: "argsdummy" },
                                    kind: "ARG" };
    _ = indexMap.getIndex(argsVar);

    boolean isVoidFunction = userMainFunc is bir:Function && userMainFunc.typeValue?.retType is bir:BTypeNil;

    mv.visitTypeInsn(NEW, SCHEDULER);
    mv.visitInsn(DUP);
    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, "<init>", "(Z)V", false);
    bir:VariableDcl schedulerVar = { typeValue: "any",
                                    name: { value: "schedulerdummy" },
                                    kind: "ARG" };
    int schedulerVarIndex = indexMap.getIndex(schedulerVar);
    mv.visitVarInsn(ASTORE, schedulerVarIndex);

    if (hasInitFunction(pkg)) {
        string initFuncName = MODULE_INIT;
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // schedule the init method
        string lambdaName = io:sprintf("$lambda$%s$", initFuncName);

        // create FP value
        createFunctionPointer(mv, initClass, lambdaName, 0);

        // no parent strand
        mv.visitInsn(ACONST_NULL);
        bir:BType anyType = "any";
        loadType(mv, anyType);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
            io:sprintf("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE), false);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        mv.visitIntInsn(BIPUSH, 100);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitFieldInsn(PUTFIELD, STRAND, "frames", io:sprintf("[L%s;", OBJECT));
        errorGen.printStackTraceFromFutureValue(mv, indexMap);

        bir:VariableDcl futureVar = { typeValue: "any",
                                    name: { value: "initdummy" },
                                    kind: "ARG" };
        int futureVarIndex = indexMap.getIndex(futureVar);
        mv.visitVarInsn(ASTORE, futureVarIndex);
        mv.visitVarInsn(ALOAD, futureVarIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "result", io:sprintf("L%s;", OBJECT));

        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_RETURNED_ERROR_METHOD, io:sprintf("(L%s;)V", OBJECT), false);
    }

    if (userMainFunc is bir:Function) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        loadCLIArgsForMain(mv, userMainFunc.params, userMainFunc.restParamExist, userMainFunc.annotAttachments);

        // invoke the user's main method
        string lambdaName = "$lambda$main$";
        createFunctionPointer(mv, initClass, lambdaName, 0);

        // no parent strand
        mv.visitInsn(ACONST_NULL);

        //submit to the scheduler
        loadType(mv, userMainFunc.typeValue?.retType);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
            io:sprintf("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE), false);
        mv.visitInsn(DUP);

        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        mv.visitIntInsn(BIPUSH, 100);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitFieldInsn(PUTFIELD, STRAND, "frames", io:sprintf("[L%s;", OBJECT));
        errorGen.printStackTraceFromFutureValue(mv, indexMap);

        // At this point we are done executing all the functions including asyncs
        if (!isVoidFunction) {
            // store future value
            bir:VariableDcl futureVar = { typeValue: "any",
                                    name: { value: "dummy" },
                                    kind: "ARG" };
            int futureVarIndex = indexMap.getIndex(futureVar);
            mv.visitVarInsn(ASTORE, futureVarIndex);
            mv.visitVarInsn(ALOAD, futureVarIndex);
            mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "result", io:sprintf("L%s;", OBJECT));

            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_RETURNED_ERROR_METHOD, io:sprintf("(L%s;)V", OBJECT), false);
        }
    }

    if (hasInitFunction(pkg)) {
        scheduleStartMethod(mv, pkg, initClass, serviceEPAvailable, errorGen, indexMap, schedulerVarIndex);
    }

    // stop all listeners
    stopListeners(mv, serviceEPAvailable);
    if (!serviceEPAvailable) {
        mv.visitMethodInsn(INVOKESTATIC, JAVA_RUNTIME, "getRuntime", io:sprintf("()L%s;", JAVA_RUNTIME), false);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_RUNTIME, "exit", "(I)V", false);
    }
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
}

function initConfigurations(jvm:MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS,
                "initConfigurations", io:sprintf("([L%s;)[L%s;", STRING_VALUE, STRING_VALUE), false);
    mv.visitVarInsn(ASTORE, 0);
}

function startListeners(jvm:MethodVisitor mv, boolean isServiceEPAvailable) {
    mv.visitLdcInsn(isServiceEPAvailable);
    mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "startListeners", "(Z)V", false);
}

function stopListeners(jvm:MethodVisitor mv, boolean isServiceEPAvailable) {
    mv.visitLdcInsn(isServiceEPAvailable);
    mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "stopListeners", "(Z)V", false);
}

function registerShutdownListener(jvm:MethodVisitor mv, string initClass) {
    string shutdownClassName = initClass + "$SignalListener";
    mv.visitMethodInsn(INVOKESTATIC, JAVA_RUNTIME, "getRuntime", io:sprintf("()L%s;", JAVA_RUNTIME), false);
    mv.visitTypeInsn(NEW, shutdownClassName);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, shutdownClassName, "<init>", "()V", false);
    mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_RUNTIME, "addShutdownHook", io:sprintf("(L%s;)V", JAVA_THREAD), false);
}

function scheduleStartMethod(jvm:MethodVisitor mv, bir:Package pkg, string initClass, boolean serviceEPAvailable,
    ErrorHandlerGenerator errorGen, BalToJVMIndexMap indexMap, int schedulerVarIndex) {

    mv.visitVarInsn(ALOAD, schedulerVarIndex);
    // schedule the start method
    string startFuncName = MODULE_START;
    string startLambdaName = io:sprintf("$lambda$%s$", startFuncName);

    mv.visitIntInsn(BIPUSH, 1);
    mv.visitTypeInsn(ANEWARRAY, OBJECT);

    // create FP value
    createFunctionPointer(mv, initClass, startLambdaName, 0);

    // no parent strand
    mv.visitInsn(ACONST_NULL);
    bir:BType anyType = "any";
    loadType(mv, anyType);
    mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
        io:sprintf("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE), false);


    mv.visitInsn(DUP);
    mv.visitInsn(DUP);
    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
    mv.visitIntInsn(BIPUSH, 100);
    mv.visitTypeInsn(ANEWARRAY, OBJECT);
    mv.visitFieldInsn(PUTFIELD, STRAND, "frames", io:sprintf("[L%s;", OBJECT));
    errorGen.printStackTraceFromFutureValue(mv, indexMap);

    bir:VariableDcl futureVar = { typeValue: "any",
                                name: { value: "startdummy" },
                                kind: "ARG" };
    int futureVarIndex = indexMap.getIndex(futureVar);
    mv.visitVarInsn(ASTORE, futureVarIndex);
    mv.visitVarInsn(ALOAD, futureVarIndex);
    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "result", io:sprintf("L%s;", OBJECT));

    mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_RETURNED_ERROR_METHOD, io:sprintf("(L%s;)V", OBJECT), false);
    // need to set immortal=true and start the scheduler again
    if (serviceEPAvailable) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, SCHEDULER, "immortal", "Z");

        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);
    }
}

# Generate a lambda function to invoke ballerina main.
#
# + userMainFunc - ballerina main function
# + cw - class visitor
# + pkg - package
function generateLambdaForMain(bir:Function userMainFunc, jvm:ClassWriter cw, bir:Package pkg,
                               string mainClass, string initClass) {
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);
    bir:BType returnType = <bir:BType> userMainFunc.typeValue?.retType;

    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$lambda$main$",
                                            io:sprintf("([L%s;)L%s;", OBJECT, OBJECT), (), ());
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
        addUnboxInsn(mv, pType);
        paramIndex += 1;
    }

    mv.visitMethodInsn(INVOKESTATIC, mainClass, userMainFunc.name.value, getMethodDesc(paramTypes, returnType), false);
    addBoxInsn(mv, returnType);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();
}

function loadCLIArgsForMain(jvm:MethodVisitor mv, bir:FunctionParam?[] params, boolean hasRestParam,
    bir:AnnotationAttachment?[] annotAttachments) {

    // get defaultable arg names from function annotation
    string[] defaultableNames = [];
    int defaultableIndex = 0;
    foreach var attachment in annotAttachments {
        if (attachment is bir:AnnotationAttachment && attachment.annotTagRef.value == DEFAULTABLE_ARGS_ANOT_NAME) {
            var annotRecValue = <bir:AnnotationRecordValue>attachment.annotValues[0];
            var annotFieldMap = annotRecValue.annotValueMap;
            var annotArrayValue = <bir:AnnotationArrayValue>annotFieldMap[DEFAULTABLE_ARGS_ANOT_FIELD];
            foreach var entryOptional in annotArrayValue.annotValueArray {
                var argValue = <bir:AnnotationLiteralValue>entryOptional;
                defaultableNames[defaultableIndex] = <string>argValue.literalValue;
                defaultableIndex += 1;
            }
            break;
        }
    }
    // create function info array
    mv.visitIntInsn(BIPUSH, params.length());
    mv.visitTypeInsn(ANEWARRAY, io:sprintf("%s$ParamInfo", RUNTIME_UTILS));
    int index = 0;
    defaultableIndex = 0;
    foreach var param in params {
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, index);
        index += 1;
        mv.visitTypeInsn(NEW, io:sprintf("%s$ParamInfo", RUNTIME_UTILS));
        mv.visitInsn(DUP);
        if (param is bir:FunctionParam) {
            if (param.hasDefaultExpr) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }
            mv.visitLdcInsn(defaultableNames[defaultableIndex]);
            defaultableIndex += 1;
            // var varIndex = indexMap.getIndex(param);
            loadType(mv, param.typeValue);
        }
        mv.visitMethodInsn(INVOKESPECIAL, io:sprintf("%s$ParamInfo", RUNTIME_UTILS), "<init>",
            io:sprintf("(ZL%s;L%s;)V", STRING_VALUE, BTYPE), false);
        mv.visitInsn(AASTORE);
    }

     // load string[] that got parsed into to java main
    mv.visitVarInsn(ALOAD, 0);
    if (hasRestParam) {
        mv.visitInsn(ICONST_1);
    } else {
        mv.visitInsn(ICONST_0);
    }

     // invoke ArgumentParser.extractEntryFuncArgs()
    mv.visitMethodInsn(INVOKESTATIC, ARGUMENT_PARSER, "extractEntryFuncArgs",
            io:sprintf("([L%s$ParamInfo;[L%s;Z)[L%s;", RUNTIME_UTILS, STRING_VALUE, OBJECT), false);
}

# Generate a lambda function to invoke ballerina main.
#
# + cw - class visitor
# + pkg - package
function generateLambdaForPackageInits(jvm:ClassWriter cw, bir:Package pkg,
                               string mainClass, string initClass, bir:ModuleID[] depMods) {
    //need to generate lambda for package Init as well, if exist
    if (hasInitFunction(pkg)) {
        string initFuncName = MODULE_INIT;
        generateLambdaForModuleFunction(cw, initFuncName, initClass, voidReturn=false);

        // generate another lambda for start function as well
        string startFuncName = MODULE_START;
        generateLambdaForModuleFunction(cw, startFuncName, initClass, voidReturn=false);

        string stopFuncName = "<stop>";
        bir:ModuleID currentModId = packageToModuleId(pkg);
        string fullFuncName = calculateModuleSpecialFuncName(currentModId, stopFuncName);

        generateLambdaForDepModStopFunc(cw, cleanupFunctionName(fullFuncName), initClass);

        foreach var id in depMods {
            fullFuncName = calculateModuleSpecialFuncName(id, stopFuncName);
            string lookupKey = getPackageName(id.org, id.name) + fullFuncName;

            string jvmClass = lookupFullQualifiedClassName(lookupKey);

            generateLambdaForDepModStopFunc(cw, cleanupFunctionName(fullFuncName), jvmClass);
        }
    }
}

function generateLambdaForModuleFunction(jvm:ClassWriter cw, string funcName, string initClass,
                                         boolean voidReturn = true) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
                                            io:sprintf("$lambda$%s$", funcName),
                                            io:sprintf("([L%s;)L%s;", OBJECT, OBJECT), (), ());
    mv.visitCode();

    //load strand as first arg
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, STRAND);

    mv.visitMethodInsn(INVOKESTATIC, initClass, funcName, io:sprintf("(L%s;)L%s;", STRAND, OBJECT), false);
    addBoxInsn(mv, errUnion);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();
}

function generateLambdaForDepModStopFunc(jvm:ClassWriter cw, string funcName, string initClass) {
    jvm:MethodVisitor mv;
    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
                        io:sprintf("$lambda$%s", funcName),
                        io:sprintf("([L%s;)L%s;", OBJECT, OBJECT), (), ());
    mv.visitCode();

    //load strand as first arg
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, STRAND);

    mv.visitMethodInsn(INVOKESTATIC, initClass, funcName, io:sprintf("(L%s;)L%s;", STRAND, OBJECT), false);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();
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
    if (moduleName == ".") {
       funcName = ".." + funcSuffix;
    } else if (versionValue == "") {
       funcName = moduleName + "." + funcSuffix;
    } else {
        funcName = moduleName + ":" + versionValue + "." + funcSuffix;
    }

    if (!stringutils:equalsIgnoreCase(orgName, "$anon")) {
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

function getModuleStopFuncName(bir:Package module) returns string {
    return calculateModuleSpecialFuncName(packageToModuleId(module), "<stop>");
}

function addInitAndTypeInitInstructions(bir:Package pkg, bir:Function func) {
    bir:BasicBlock?[] basicBlocks = [];
    nextId = -1;
    bir:BasicBlock nextBB = {id: getNextBBId(), instructions: []};
    basicBlocks[basicBlocks.length()] = nextBB;

    bir:ModuleID modID = packageToModuleId(pkg);

    bir:BasicBlock typeOwnerCreateBB = {id: getNextBBId(), instructions: []};
    basicBlocks[basicBlocks.length()] = typeOwnerCreateBB;

    bir:Call createTypesCallTerm = {pos:{}, args:[], kind:bir:TERMINATOR_CALL, lhsOp:(), pkgID:modID,
                        name:{value:CURRENT_MODULE_INIT}, isVirtual:false, thenBB:typeOwnerCreateBB};
    nextBB.terminator = createTypesCallTerm;

    if (func.basicBlocks.length() == 0) {
        bir:Return ret = {pos:{sLine:999}, kind:bir:TERMINATOR_RETURN};
        typeOwnerCreateBB.terminator = ret;
        func.basicBlocks = basicBlocks;
        return;
    }

    bir:GOTO gotoNext = {pos:{}, kind:bir:TERMINATOR_GOTO, targetBB:<bir:BasicBlock>func.basicBlocks[0]};
    typeOwnerCreateBB.terminator = gotoNext;

    foreach var basicBB in func.basicBlocks {
        basicBlocks[basicBlocks.length()] = basicBB;
    }
    func.basicBlocks = basicBlocks;
}

function enrichPkgWithInitializers(map<JavaClass> jvmClassMap, string typeOwnerClass,
                                        bir:Package pkg, bir:ModuleID[] depModArray) {
    JavaClass javaClass = <JavaClass>jvmClassMap[typeOwnerClass];
    bir:Function initFunc = generateDepModInit(depModArray, pkg, MODULE_INIT, "<init>");
    javaClass.functions[javaClass.functions.length()] = initFunc;
    pkg.functions[pkg.functions.length()] = initFunc;

    bir:Function startFunc = generateDepModInit(depModArray, pkg, MODULE_START, "<start>");
    javaClass.functions[javaClass.functions.length()] = startFunc;
    pkg.functions[pkg.functions.length()] = startFunc;

}

function generateDepModInit(bir:ModuleID[] imprtMods, bir:Package pkg, string funcName,
                                string initName) returns bir:Function {
    nextId = -1;
    nextVarId = -1;

    bir:VariableDcl retVar = {name: {value:"%ret"}, typeValue: errUnion};
    bir:VarRef retVarRef = {variableDcl:retVar, typeValue:errUnion};

    bir:Function modInitFunc = {pos:{}, basicBlocks:[], localVars:[retVar],
                            name:{value:funcName}, typeValue:{retType:errUnion},
                            workerChannels:[], receiver:(), restParamExist:false};
    _ = addAndGetNextBasicBlock(modInitFunc);

    bir:VariableDcl boolVal = addAndGetNextVar(modInitFunc, bir:TYPE_BOOLEAN);
    bir:VarRef boolRef = {variableDcl:boolVal, typeValue:bir:TYPE_BOOLEAN};

    foreach var id in imprtMods {
        string initFuncName = calculateModuleSpecialFuncName(id, initName);
        _ = addCheckedInvocation(modInitFunc, id, initFuncName, retVarRef, boolRef);
    }

    bir:ModuleID currentModId = packageToModuleId(pkg);
    string currentInitFuncName = calculateModuleSpecialFuncName(currentModId, initName);
    bir:BasicBlock lastBB = addCheckedInvocation(modInitFunc, currentModId, currentInitFuncName, retVarRef, boolRef);

    bir:Return ret = {pos:{}, kind:bir:TERMINATOR_RETURN};
    lastBB.terminator = ret;

    return modInitFunc;
}

function getNextBBId() returns bir:Name {
    string bbIdPrefix = "genBB";
    nextId += 1;
    return {value:bbIdPrefix + nextId.toString()};
}

function getNextVarId() returns bir:Name {
    string varIdPrefix = "%";
    nextVarId += 1;
    return {value:varIdPrefix + nextVarId.toString()};
}

function addCheckedInvocation(bir:Function func, bir:ModuleID modId, string initFuncName,
                                    bir:VarRef retVar, bir:VarRef boolRef) returns bir:BasicBlock {
    bir:BasicBlock lastBB = <bir:BasicBlock>func.basicBlocks[func.basicBlocks.length() - 1];
    bir:BasicBlock nextBB = addAndGetNextBasicBlock(func);
    // TODO remove once lang.annotation is fixed
    if (modId.org == BALLERINA && modId.name == BUILT_IN_PACKAGE_NAME) {
        bir:Call initCallTerm = {pos:{}, args:[], kind:bir:TERMINATOR_CALL, lhsOp:(), pkgID:modId,
                            name:{value:initFuncName}, isVirtual:false, thenBB:nextBB};
        lastBB.terminator = initCallTerm;
        return nextBB;
    }
    bir:Call initCallTerm = {pos:{}, args:[], kind:bir:TERMINATOR_CALL, lhsOp:retVar, pkgID:modId,
                        name:{value:initFuncName}, isVirtual:false, thenBB:nextBB};
    lastBB.terminator = initCallTerm;

    bir:TypeTest typeTest = {pos:{}, kind:bir:INS_KIND_TYPE_TEST,
                                lhsOp:boolRef, rhsOp:retVar, typeValue:errType};
    nextBB.instructions[nextBB.instructions.length()] = typeTest;

    bir:BasicBlock trueBB = addAndGetNextBasicBlock(func);

    bir:BasicBlock retBB = addAndGetNextBasicBlock(func);

    bir:Return ret = {pos:{}, kind:bir:TERMINATOR_RETURN};
    retBB.terminator = ret;

    bir:GOTO gotoRet = {pos:{}, kind:bir:TERMINATOR_GOTO, targetBB:retBB};
    trueBB.terminator = gotoRet;

    bir:BasicBlock falseBB = addAndGetNextBasicBlock(func);
    bir:Branch branch = {pos:{}, falseBB:falseBB, kind:bir:TERMINATOR_BRANCH, op:boolRef, trueBB:trueBB};
    nextBB.terminator = branch;
    return falseBB;
}

function addAndGetNextBasicBlock(bir:Function func) returns bir:BasicBlock {
    bir:BasicBlock nextbb = {id: getNextBBId(), instructions: []};
    func.basicBlocks[func.basicBlocks.length()] = nextbb;
    return nextbb;
}

function addAndGetNextVar(bir:Function func, bir:BType typeVal) returns bir:VariableDcl {
    bir:VariableDcl nextLocalVar = {name: getNextVarId(), typeValue: typeVal};
    func.localVars[func.localVars.length()] = nextLocalVar;
    return nextLocalVar;
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
        } else if (bType is jvm:JLong || bType is jvm:JDouble) {
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
    foreach var func in pkg.functions {
        generateFrameClassForFunction(pkg, func, pkgEntries);
    }

    foreach var typeDef in pkg.typeDefs {
        bir:Function?[]? attachedFuncs = typeDef?.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            bir:BType? attachedType;
            if (typeDef?.typeValue is bir:BRecordType) {
                // Only attach function of records is the record init. That should be
                // generated as a static function.
                attachedType = ();
            } else {
                attachedType = typeDef?.typeValue;
            }
            foreach var func in attachedFuncs {
                generateFrameClassForFunction(pkg, func, pkgEntries, attachedType=attachedType);
            }
        }
    }
}

function generateFrameClassForFunction (bir:Package pkg, bir:Function? func, map<byte[]> pkgEntries,
                                        bir:BType? attachedType = ()) {
    string pkgName = getPackageName(pkg.org.value, pkg.name.value);
    bir:Function currentFunc = getFunction(<@untainted> func);
    string frameClassName = getFrameClassName(pkgName, currentFunc.name.value, attachedType);
    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visitSource(currentFunc.pos.sourceFileName);
    currentClass = <@untainted> frameClassName;
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, frameClassName, (), OBJECT, ());
    generateDefaultConstructor(cw, OBJECT);

    int k = 0;
    bir:VariableDcl?[] localVars = currentFunc.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        bir:BType bType = localVar.typeValue;
        var fieldName = stringutils:replace(localVar.name.value, "%","_");
        generateField(cw, bType, fieldName, false);
        k = k + 1;
    }

    jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC, "state", "I");
    fv.visitEnd();

    cw.visitEnd();

    // panic if there are errors in the frame class. These cannot be logged, since
    // frame classes are internal implementation details.
    pkgEntries[frameClassName + ".class"] = checkpanic cw.toByteArray();
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
# + return - cleaned name
function cleanupTypeName(string name) returns string {
    return stringutils:replace(name, "$","_");
}

function cleanupBalExt(string name) returns string {
    return stringutils:replace(name, BAL_EXTENSION, "");
}

function cleanupPathSeperators(string name) returns string {
   //TODO: should use file_path:getPathSeparator();
   return stringutils:replace(name, WINDOWS_PATH_SEPERATOR, JAVA_PACKAGE_SEPERATOR);
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
        typeSig = io:sprintf("L%s;", BSTRING_VALUE);
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
    } else if (bType is bir:BTypeHandle) {
        typeSig = io:sprintf("L%s;", HANDLE_VALUE);
    } else if (bType is jvm:JType) {
        typeSig = getJTypeSignature(bType);
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
    return stringutils:replaceAll(functionName, "[\\.:/<>]", "_");
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
    if (varRef is ()) {
        error err = error("Invalid variable reference");
        panic err;
    } else {
        return varRef;
    }
}

function getType(bir:BType? bType) returns bir:BType {
    if (bType is ()) {
        error err = error("Invalid type");
        panic err;
    } else {
        return bType;
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
        if (stringutils:equalsIgnoreCase(listItem, item)) {
            return true;
        }
    }

    return false;
}

function getFunctions(bir:Function?[]? functions) returns bir:Function?[] {
    if (functions is ()) {
        error err = error(io:sprintf("Invalid functions: %s", functions));
        panic err;
    } else {
        return functions;
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

function stringArrayContains(string[] array, string item) returns boolean {
    foreach var listItem in array {
        if (stringutils:equalsIgnoreCase(listItem, item)) {
            return true;
        }
    }
    return false;
}

function logCompileError(error compileError, bir:Package|bir:TypeDef|bir:Function src, bir:Package currentModule) {
    string reason = compileError.reason();
    map<anydata|error> detail = compileError.detail();
    error err;
    bir:DiagnosticPos pos;
    string name;
    if (reason == ERROR_REASON_METHOD_TOO_LARGE) {
        name = <string> detail.get("name");
        bir:Function? func = findBIRFunction(src, name);
        if (func is ()) {
            panic compileError;
        } else {
            err = error(io:sprintf("method is too large: '%s'", func.name.value));
            pos = func.pos;
        }
    } else if (reason == ERROR_REASON_CLASS_TOO_LARGE) {
        name = <string> detail.get("name");
        err = error(io:sprintf("file is too large: '%s'", name));
        pos = {};
    } else {
        panic compileError;
    }

    dlogger.logError(<@untainted> err, <@untainted> pos, <@untainted> currentModule);
}

function findBIRFunction(bir:Package|bir:TypeDef|bir:Function src, string name) returns bir:Function? {
    if (src is bir:Function) {
        return src;
    } else if (src is bir:Package) {
        foreach var func in src.functions {
            if (func is bir:Function && cleanupFunctionName(func.name.value) == name) {
                return func;
            }
        }
    } else {
        bir:Function?[]? attachedFuncs = src.attachedFuncs;
        if (attachedFuncs is bir:Function?[]) {
            foreach var func in attachedFuncs {
                if (func is bir:Function && cleanupFunctionName(func.name.value) == name) {
                    return func;
                }
            }
        }
    }

    return ();
}

function generateModuleInitializer(jvm:ClassWriter cw, bir:Package module) {
    string orgName = module.org.value;
    string moduleName = module.name.value;
    string versionValue = module.versionValue.value;
    string pkgName = getPackageName(orgName, moduleName);

    // Using object return type since this is similar to a ballerina function without a return.
    // A ballerina function with no returns is equivalent to a function with nil-return.
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CURRENT_MODULE_INIT,
                                          io:sprintf("(L%s;)L%s;", STRAND, OBJECT), (), ());
    mv.visitCode();

    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, "$createTypes", "()V", false);
    mv.visitTypeInsn(NEW, typeOwnerClass);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, typeOwnerClass, "<init>", "()V", false);
    mv.visitVarInsn(ASTORE, 1);
    mv.visitLdcInsn(orgName);
    mv.visitLdcInsn(moduleName);
    mv.visitLdcInsn(versionValue);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, io:sprintf("%s", VALUE_CREATOR), "addValueCreator",
                       io:sprintf("(L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE, VALUE_CREATOR),
                       false);

    // Add a nil-return
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();

    //Adding this java method to the function map because this is getting called from a bir instruction.
    bir:Function func = {pos:{}, basicBlocks:[], localVars:[],
                            name:{value:CURRENT_MODULE_INIT}, typeValue:{retType:"()"},
                            workerChannels:[], receiver:(), restParamExist:false};
    birFunctionMap[pkgName + CURRENT_MODULE_INIT] = getFunctionWrapper(func, orgName, moduleName,
                                                                    versionValue, typeOwnerClass);
}

function generateExecutionStopMethod(jvm:ClassWriter cw, string initClass, bir:Package module, bir:ModuleID[] imprtMods) {
    string orgName = module.org.value;
    string moduleName = module.name.value;
    string versionValue = module.versionValue.value;
    string pkgName = getPackageName(orgName, moduleName);
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, MODULE_STOP, "()V", (), ());
    mv.visitCode();

    BalToJVMIndexMap indexMap = new;
    ErrorHandlerGenerator errorGen = new(mv, indexMap, pkgName);

    bir:VariableDcl argsVar = { typeValue: "any",
                                    name: { value: "schedulerVar" },
                                    kind: "ARG" };
    int schedulerIndex = indexMap.getIndex(argsVar);
    bir:VariableDcl futureVar = { typeValue: "any",
                                    name: { value: "futureVar" },
                                    kind: "ARG" };
    int futureIndex = indexMap.getIndex(futureVar);

    mv.visitTypeInsn(NEW, SCHEDULER);
    mv.visitInsn(DUP);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, "<init>", "(IZ)V", false);

    mv.visitVarInsn(ASTORE, schedulerIndex);


    string stopFuncName = "<stop>";

    bir:ModuleID currentModId = packageToModuleId(module);
    string fullFuncName = calculateModuleSpecialFuncName(currentModId, stopFuncName);

    scheduleStopMethod(mv, initClass, cleanupFunctionName(fullFuncName), errorGen, indexMap, schedulerIndex, futureIndex);

    int i = imprtMods.length() - 1;
    while i >= 0 {
        bir:ModuleID id = imprtMods[i];
        i -= 1;
        fullFuncName = calculateModuleSpecialFuncName(id, stopFuncName);

        scheduleStopMethod(mv, initClass, cleanupFunctionName(fullFuncName), errorGen, indexMap, schedulerIndex, futureIndex);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();

    //Adding this java method to the function map because this is getting called from a bir instruction.
    bir:Function func = {pos:{}, basicBlocks:[], localVars:[],
                            name:{value:MODULE_STOP}, typeValue:{retType:"()"},
                            workerChannels:[], receiver:(), restParamExist:false};
    birFunctionMap[pkgName + MODULE_STOP] = getFunctionWrapper(func, orgName, moduleName,
                                                                    versionValue, typeOwnerClass);
}

function scheduleStopMethod(jvm:MethodVisitor mv, string initClass, string stopFuncName,
                            ErrorHandlerGenerator errorGen, BalToJVMIndexMap indexMap, int schedulerIndex,
                            int futureIndex) {
    string lambdaFuncName = "$lambda$" + stopFuncName;
    // Create a schedular. A new schedular is used here, to make the stop function to not to
    // depend/wait on whatever is being running on the background. eg: a busy loop in the main.

    mv.visitVarInsn(ALOAD, schedulerIndex);

    mv.visitIntInsn(BIPUSH, 1);
    mv.visitTypeInsn(ANEWARRAY, OBJECT);

    // create FP value
    createFunctionPointer(mv, initClass, lambdaFuncName, 0);

    // no parent strand
    mv.visitInsn(ACONST_NULL);

    loadType(mv, bir:TYPE_NIL);
    mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
        io:sprintf("([L%s;L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, BTYPE, FUTURE_VALUE), false);

    mv.visitVarInsn(ASTORE, futureIndex);

    mv.visitVarInsn(ALOAD, futureIndex);

    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
    mv.visitIntInsn(BIPUSH, 100);
    mv.visitTypeInsn(ANEWARRAY, OBJECT);
    mv.visitFieldInsn(PUTFIELD, STRAND, "frames", io:sprintf("[L%s;", OBJECT));

    mv.visitVarInsn(ALOAD, futureIndex);
    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
    mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));
    mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);

    mv.visitVarInsn(ALOAD, futureIndex);
    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, io:sprintf("L%s;", THROWABLE));

    // handle any runtime errors
    jvm:Label labelIf = new;
    mv.visitJumpInsn(IFNULL, labelIf);

    mv.visitVarInsn(ALOAD, futureIndex);
    mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, io:sprintf("L%s;", THROWABLE));
    mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, io:sprintf("(L%s;)V", THROWABLE),
                        false);
    mv.visitLabel(labelIf);
}

function generateJavaCompatibilityCheck(jvm:MethodVisitor mv) {
    mv.visitLdcInsn(getJavaVersion());
    mv.visitMethodInsn(INVOKESTATIC, COMPATIBILITY_CHECKER, "verifyJavaCompatibility", 
                        io:sprintf("(L%s;)V", STRING_VALUE), false);
}

function getJavaVersion() returns string {
    handle versionProperty = java:fromString("java.version");
    string? javaVersion = java:toString(getProperty(versionProperty));
    if (javaVersion is string) {
        return javaVersion;
    } else {
        return "";
    }
}

function isBStringFunc(string funcName) returns boolean {
    return funcName.endsWith("$bstring");
}

function nameOfBStringFunc(string nonBStringFuncName) returns string {
    return nonBStringFuncName + "$bstring";
}

function conditionalBStringName(string nonBStringName, boolean useBString) returns string {
    if(useBString) {
        return nameOfBStringFunc(nonBStringName);
    }
    return nonBStringName;
}

function nameOfNonBStringFunc(string funcName) returns string {
    if(isBStringFunc(funcName)) {
        return funcName.substring(0, funcName.length() - 8);
    }
    return funcName;
}

function getProperty(handle propertyName) returns handle = @java:Method {
    'class: "java.lang.System",
    name: "getProperty"
} external;
