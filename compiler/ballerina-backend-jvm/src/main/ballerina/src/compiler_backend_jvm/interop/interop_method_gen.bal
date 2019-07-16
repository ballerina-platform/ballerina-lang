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
import ballerina/jvm;

type JMethodFunctionWrapper record {|
    * BIRFunctionWrapper;
    jvm:Method jMethod;
|};

type JFieldFunctionWrapper record {|
    * BIRFunctionWrapper;
    jvm:Field jField;
|};

type JInteropFunctionWrapper JMethodFunctionWrapper | JFieldFunctionWrapper;

function createJInteropFunctionWrapper(jvm:InteropValidationRequest jInteropValidationReq,
                                       bir:Function birFunc,
                                       string orgName,
                                       string moduleName,
                                       string versionValue,
                                       string  birModuleClassName) returns JInteropFunctionWrapper  {

    addDefaultableBooleanVarsToSignature(birFunc);
    // Update the function wrapper only for Java interop functions
    BIRFunctionWrapper birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName,
                                                versionValue, birModuleClassName);
    if jInteropValidationReq is jvm:MethodValidationRequest {
        return createJMethodWrapper(jInteropValidationReq, birFuncWrapper);
    } else {
        return createJFieldWrapper(jInteropValidationReq, birFuncWrapper);
    }
}

function createJMethodWrapper(jvm:MethodValidationRequest jMethodValidationReq,
                              BIRFunctionWrapper birFuncWrapper) returns JMethodFunctionWrapper  {
    var jMethodOrError = jvm:validateAndGetJMethod(jMethodValidationReq);
    if (jMethodOrError is error) {
        panic jMethodOrError;
    }

    return  {
        orgName : birFuncWrapper.orgName,
        moduleName : birFuncWrapper.moduleName,
        versionValue : birFuncWrapper.versionValue,
        func : birFuncWrapper.func,
        fullQualifiedClassName : birFuncWrapper.fullQualifiedClassName,
        jvmMethodDescription : birFuncWrapper.jvmMethodDescription,
        jMethod: <jvm:Method>jMethodOrError
    };
}

function createJFieldWrapper(jvm:FieldValidationRequest jFieldValidationReq,
                             BIRFunctionWrapper birFuncWrapper) returns JFieldFunctionWrapper  {
    var jFieldOrError = jvm:validateAndGetJField(jFieldValidationReq);
    if (jFieldOrError is error) {
        panic jFieldOrError;
    }

    return  {
        orgName : birFuncWrapper.orgName,
        moduleName : birFuncWrapper.moduleName,
        versionValue : birFuncWrapper.versionValue,
        func : birFuncWrapper.func,
        fullQualifiedClassName : birFuncWrapper.fullQualifiedClassName,
        jvmMethodDescription : birFuncWrapper.jvmMethodDescription,
        jField: <jvm:Field>jFieldOrError
    };
}

function genJMethodForBExternalFuncInterop(JInteropFunctionWrapper extFuncWrapper,
                                           jvm:ClassWriter cw,
                                           bir:Package birModule){
    if  extFuncWrapper is JMethodFunctionWrapper {
        genJMethodForInteropMethod(extFuncWrapper, cw, birModule);
    } else {
        genJFieldForInteropField(extFuncWrapper, cw, birModule);
    }
}

function genJFieldForInteropField(JFieldFunctionWrapper jFieldFuncWrapper,
                                  jvm:ClassWriter cw,
                                  bir:Package birModule){
    var currentPackageName = getPackageName(birModule.org.value, birModule.name.value);

    // Create a local variable for the strand
    BalToJVMIndexMap indexMap = new;
    bir:VariableDcl strandVarDcl = { typeValue: "string", name: { value: "$_strand_$" }, kind: "ARG" };
    int strandParamIndex = indexMap.getIndex(strandVarDcl);

    // Generate method desc
    bir:Function birFunc = jFieldFuncWrapper.func;
    string desc = getMethodDesc(birFunc.typeValue.paramTypes, birFunc.typeValue["retType"]);
    int access = ACC_PUBLIC + ACC_STATIC;

    jvm:MethodVisitor mv = cw.visitMethod(access, birFunc.name.value, desc, (), ());
    InstructionGenerator instGen = new(mv, indexMap, currentPackageName);
    ErrorHandlerGenerator errorGen = new(mv, indexMap, currentPackageName);
    LabelGenerator labelGen = new();
    TerminatorGenerator termGen = new(mv, indexMap, labelGen, errorGen, birModule);
    mv.visitCode();

    jvm:Label paramLoadLabel = labelGen.getLabel("param_load");
    mv.visitLabel(paramLoadLabel);
    mv.visitLineNumber(birFunc.pos.sLine, paramLoadLabel);

    // birFunc.localVars contains all the function parameters as well as added boolean parameters to indicate the
    //  availability of default values.
    // The following line cast localvars to function params. This is guaranteed not to fail.
    // Get a JVM method local variable index for the parameter
    bir:FunctionParam?[] birFuncParams = [];
    foreach var birLocalVarOptional in birFunc.localVars {
        if (birLocalVarOptional is bir:FunctionParam) {
            birFuncParams[birFuncParams.length()] =  birLocalVarOptional;
            _ = indexMap.getIndex(<bir:FunctionParam>birLocalVarOptional);
        }
    }

    // Generate if blocks to check and set default values to parameters
    int birFuncParamIndex = 0;
    int paramDefaultsBBIndex = 0;
    foreach var birFuncParamOptional in birFuncParams {
        var birFuncParam = <bir:FunctionParam>birFuncParamOptional;
        // Skip boolean function parameters to indicate the existence of default values
        if (birFuncParamIndex % 2 !== 0 || !birFuncParam.hasDefaultExpr) {
            // Skip the loop if:
            //  1) This birFuncParamIndex had an odd value: indicates a generated boolean parameter
            //  2) This function param doesn't have a default value
            birFuncParamIndex += 1;
            continue;
        }

        // The following boolean parameter indicates the existence of a default value
        var isDefaultValueExist = <bir:FunctionParam>birFuncParams[birFuncParamIndex + 1];
        mv.visitVarInsn(ILOAD, indexMap.getIndex(isDefaultValueExist));

        // Gen the if not equal logic
        jvm:Label paramNextLabel = labelGen.getLabel(birFuncParam.name.value + "next");
        mv.visitJumpInsn(IFNE, paramNextLabel);

        bir:BasicBlock?[] basicBlocks = birFunc.paramDefaultBBs[paramDefaultsBBIndex];
        generateBasicBlocks(mv, basicBlocks, labelGen, errorGen, instGen, termGen, birFunc, -1,
                            -1, strandParamIndex, true, birModule, currentPackageName, ());
        mv.visitLabel(paramNextLabel);

        birFuncParamIndex += 1;
        paramDefaultsBBIndex += 1;
    }

    jvm:Field jField = jFieldFuncWrapper.jField;
    jvm:JType jFieldType = jField.fType;

    // Load receiver which is the 0th parameter in the birFunc
    if !jField.isStatic {
        var receiverLocalVarIndex = indexMap.getIndex(<bir:FunctionParam>birFuncParams[0]);
        mv.visitVarInsn(ALOAD, receiverLocalVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, jField.class);

        jvm:Label ifNonNullLabel = labelGen.getLabel("receiver_null_check");
        mv.visitLabel(ifNonNullLabel);
        mv.visitInsn(DUP);

        jvm:Label elseBlockLabel = labelGen.getLabel("receiver_null_check_else");
        mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
        jvm:Label thenBlockLabel = labelGen.getLabel("receiver_null_check_then");
        mv.visitLabel(thenBlockLabel);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("instance is null");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(elseBlockLabel);
    }

    // Load java method parameters
    birFuncParamIndex = jField.isStatic ? 0: 2;
    int jMethodParamIndex = 0;
    if (birFuncParamIndex < birFuncParams.length()) {
        var birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        int paramLocalVarIndex = indexMap.getIndex(birFuncParam);
        loadMethodParamToStackInInteropFunction(mv, birFuncParam, jFieldType, currentPackageName, paramLocalVarIndex);
    }

    if jField.isStatic {
        if jField.method is jvm:ACCESS {
            mv.visitFieldInsn(GETSTATIC, jField.class, jField.name, jField.sig);
        } else {
            mv.visitFieldInsn(PUTSTATIC, jField.class, jField.name, jField.sig);
        }
    } else {
        if jField.method is jvm:ACCESS {
            mv.visitFieldInsn(GETFIELD, jField.class, jField.name, jField.sig);
        } else {
            mv.visitFieldInsn(PUTFIELD, jField.class, jField.name, jField.sig);
        }
    }

    // Handle return type
    int returnVarRefIndex = -1;
    bir:BType retType = <bir:BType>birFunc.typeValue["retType"];
    if retType is bir:BTypeNil {
    } else {
        bir:VariableDcl retVarDcl = { typeValue: <bir:BType>retType, name: { value: "$_ret_var_$" }, kind: "LOCAL" };
        returnVarRefIndex = indexMap.getIndex(retVarDcl);
        if retType is bir:BTypeHandle {
            // Here the corresponding Java method parameter type is 'jvm:RefType'. This has been verified before
            bir:VariableDcl retJObjectVarDcl = { typeValue: "any", name: { value: "$_ret_jobject_var_$" }, kind: "LOCAL" };
            int returnJObjectVarRefIndex = indexMap.getIndex(retJObjectVarDcl);
            mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
            mv.visitTypeInsn(NEW, HANDLE_VALUE);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
            mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, "<init>", "(Ljava/lang/Object;)V", false);
        } else {
            performWideningPrimitiveConversion(mv, <BValueType>retType, <jvm:PrimitiveType>jFieldType);
        }
        generateVarStore(mv, retVarDcl, currentPackageName, returnVarRefIndex);
    }

    jvm:Label retLabel = labelGen.getLabel("return_lable");
    mv.visitLabel(retLabel);
    mv.visitLineNumber(birFunc.pos.sLine, retLabel);
    termGen.genReturnTerm({pos:{}, kind:"RETURN"}, returnVarRefIndex, birFunc);
    mv.visitMaxs(200, 400);
    mv.visitEnd();
}

function genJMethodForInteropMethod(JMethodFunctionWrapper extFuncWrapper,
                                    jvm:ClassWriter cw,
                                    bir:Package birModule){
    var currentPackageName = getPackageName(birModule.org.value, birModule.name.value);

    // Create a local variable for the strand
    BalToJVMIndexMap indexMap = new;
    bir:VariableDcl strandVarDcl = { typeValue: "string", name: { value: "$_strand_$" }, kind: "ARG" };
    int strandParamIndex = indexMap.getIndex(strandVarDcl);

    // Generate method desc
    bir:Function birFunc = extFuncWrapper.func;
    string desc = getMethodDesc(birFunc.typeValue.paramTypes, birFunc.typeValue["retType"]);
    int access = ACC_PUBLIC + ACC_STATIC;

    jvm:MethodVisitor mv = cw.visitMethod(access, birFunc.name.value, desc, (), ());
    InstructionGenerator instGen = new(mv, indexMap, currentPackageName);
    ErrorHandlerGenerator errorGen = new(mv, indexMap, currentPackageName);
    LabelGenerator labelGen = new();
    TerminatorGenerator termGen = new(mv, indexMap, labelGen, errorGen, birModule);
    mv.visitCode();

    jvm:Label paramLoadLabel = labelGen.getLabel("param_load");
    mv.visitLabel(paramLoadLabel);
    mv.visitLineNumber(birFunc.pos.sLine, paramLoadLabel);

    // birFunc.localVars contains all the function parameters as well as added boolean parameters to indicate the
    //  availability of default values.
    // The following line cast localvars to function params. This is guaranteed not to fail.
    // Get a JVM method local variable index for the parameter
    bir:FunctionParam?[] birFuncParams = [];
    foreach var birLocalVarOptional in birFunc.localVars {
        if (birLocalVarOptional is bir:FunctionParam) {
            birFuncParams[birFuncParams.length()] =  birLocalVarOptional;
            _ = indexMap.getIndex(<bir:FunctionParam>birLocalVarOptional);
        }
    }

    // Generate if blocks to check and set default values to parameters
    int birFuncParamIndex = 0;
    int paramDefaultsBBIndex = 0;
    foreach var birFuncParamOptional in birFuncParams {
        var birFuncParam = <bir:FunctionParam>birFuncParamOptional;
        // Skip boolean function parameters to indicate the existence of default values
        if (birFuncParamIndex % 2 !== 0 || !birFuncParam.hasDefaultExpr) {
            // Skip the loop if:
            //  1) This birFuncParamIndex had an odd value: indicates a generated boolean parameter
            //  2) This function param doesn't have a default value
            birFuncParamIndex += 1;
            continue;
        }

        // The following boolean parameter indicates the existence of a default value
        var isDefaultValueExist = <bir:FunctionParam>birFuncParams[birFuncParamIndex + 1];
        mv.visitVarInsn(ILOAD, indexMap.getIndex(isDefaultValueExist));

        // Gen the if not equal logic
        jvm:Label paramNextLabel = labelGen.getLabel(birFuncParam.name.value + "next");
        mv.visitJumpInsn(IFNE, paramNextLabel);

        bir:BasicBlock?[] basicBlocks = birFunc.paramDefaultBBs[paramDefaultsBBIndex];
        generateBasicBlocks(mv, basicBlocks, labelGen, errorGen, instGen, termGen, birFunc, -1,
                            -1, strandParamIndex, true, birModule, currentPackageName, ());
        mv.visitLabel(paramNextLabel);

        birFuncParamIndex += 1;
        paramDefaultsBBIndex += 1;
    }

    jvm:Method jMethod = extFuncWrapper.jMethod;
    jvm:MethodType jMethodType = jMethod.mType;
    jvm:JType[] jMethodParamTypes = jMethodType.paramTypes;
    jvm:JType jMethodRetType = jMethodType.retType;

    // Load receiver which is the 0th parameter in the birFunc
    if jMethod.kind is jvm:INSTANCE {
        //var receiverParam = <bir:FunctionParam>birFuncParams[0];
        var receiverLocalVarIndex = indexMap.getIndex(<bir:FunctionParam>birFuncParams[0]);
        //var receiverJType = <jvm:RefType> jMethodParamTypes[0];
        mv.visitVarInsn(ALOAD, receiverLocalVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, jMethod.class);

        jvm:Label ifNonNullLabel = labelGen.getLabel("receiver_null_check");
        mv.visitLabel(ifNonNullLabel);
        mv.visitInsn(DUP);

        jvm:Label elseBlockLabel = labelGen.getLabel("receiver_null_check_else");
        mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
        jvm:Label thenBlockLabel = labelGen.getLabel("receiver_null_check_then");
        mv.visitLabel(thenBlockLabel);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("instance is null");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(elseBlockLabel);
    } else if jMethod.kind is jvm:CONSTRUCTOR {
        mv.visitTypeInsn(NEW, jMethod.class);
        mv.visitInsn(DUP);
    }

    // Load java method parameters
    birFuncParamIndex = jMethod.kind is jvm:INSTANCE ? 2: 0;
    int jMethodParamIndex = 0;
    while (birFuncParamIndex < birFuncParams.length()) {
        var birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        int paramLocalVarIndex = indexMap.getIndex(birFuncParam);
        loadMethodParamToStackInInteropFunction(mv, birFuncParam,
                                    jMethodParamTypes[jMethodParamIndex], currentPackageName, paramLocalVarIndex);
        birFuncParamIndex += 2;
        jMethodParamIndex += 1;
    }

    if jMethod.kind is jvm:INSTANCE {
        if jMethod.isInterface {
            mv.visitMethodInsn(INVOKEINTERFACE, jMethod.class, jMethod.name, jMethod.sig, true);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, jMethod.class, jMethod.name, jMethod.sig, false);
        }
    } else if jMethod.kind is jvm:STATIC {
        mv.visitMethodInsn(INVOKESTATIC, jMethod.class, jMethod.name, jMethod.sig, false);
    } else {
        // jMethod.kind is jvm:CONSTRUCTOR
        mv.visitMethodInsn(INVOKESPECIAL, jMethod.class, jMethod.name, jMethod.sig, false);

    }

    // Handle return type
    int returnVarRefIndex = -1;
    bir:BType retType = <bir:BType>birFunc.typeValue["retType"];
    if retType is bir:BTypeNil {
    } else {
        bir:VariableDcl retVarDcl = { typeValue: <bir:BType>retType, name: { value: "$_ret_var_$" }, kind: "LOCAL" };
        returnVarRefIndex = indexMap.getIndex(retVarDcl);
        if retType is bir:BTypeHandle {
            // Here the corresponding Java method parameter type is 'jvm:RefType'. This has been verified before
            bir:VariableDcl retJObjectVarDcl = { typeValue: "any", name: { value: "$_ret_jobject_var_$" }, kind: "LOCAL" };
            int returnJObjectVarRefIndex = indexMap.getIndex(retJObjectVarDcl);
            mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
            mv.visitTypeInsn(NEW, HANDLE_VALUE);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
            mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, "<init>", "(Ljava/lang/Object;)V", false);
        } else {
            performWideningPrimitiveConversion(mv, <BValueType>retType, <jvm:PrimitiveType>jMethodRetType);
        }
        generateVarStore(mv, retVarDcl, currentPackageName, returnVarRefIndex);
    }

    jvm:Label retLabel = labelGen.getLabel("return_lable");
    mv.visitLabel(retLabel);
    mv.visitLineNumber(birFunc.pos.sLine, retLabel);
    termGen.genReturnTerm({pos:{}, kind:"RETURN"}, returnVarRefIndex, birFunc);
    mv.visitMaxs(200, 400);
    mv.visitEnd();
}

type BValueType bir:BTypeInt | bir:BTypeFloat | bir:BTypeBoolean | bir:BTypeByte;

// These conversions are already validate beforehand, therefore I am just emitting type conversion instructions here.
// We can improve following logic with a type lattice.
function performWideningPrimitiveConversion(jvm:MethodVisitor mv, BValueType bType, jvm:PrimitiveType jType){
    if bType is bir:BTypeInt && jType is jvm:Long {
        return; // NOP
    } else if bType is bir:BTypeFloat && jType is jvm:Double {
        return; // NOP
    } else if bType is bir:BTypeInt {
        mv.visitInsn(I2L);
    } else if bType is bir:BTypeFloat {
        if jType is jvm:Long {
            mv.visitInsn(L2D);
        } else if jType is jvm:Float {
            mv.visitInsn(F2D);
        } else {
            mv.visitInsn(I2D);
        }
    }
}

// We can improve following logic with a type lattice.
function performNarrowingPrimitiveConversion(jvm:MethodVisitor mv, BValueType bType, jvm:PrimitiveType jType){
    if bType is bir:BTypeInt && jType is jvm:Long {
        return; // NOP
    } else if bType is bir:BTypeFloat && jType is jvm:Double {
        return; // NOP
    } else if bType is bir:BTypeInt {
        // Only possible jvm types are Byte, Short, Char and Int
        mv.visitInsn(L2I);
        if jType is jvm:Byte {
            mv.visitInsn(I2B);
        } else if jType is jvm:Short {
            mv.visitInsn(I2S);
        } else if jType is jvm:Char {
            mv.visitInsn(I2C);
        }
    } else if bType is bir:BTypeFloat {
        // Only possible jvm types are Byte, Short, Char, Int, Long and Float
        if jType is jvm:Byte {
            mv.visitInsn(D2I);
            mv.visitInsn(I2B);
        } else if jType is jvm:Short {
            mv.visitInsn(D2I);
            mv.visitInsn(I2S);
        } else if jType is jvm:Char {
            mv.visitInsn(D2I);
            mv.visitInsn(I2C);
        } else if jType is jvm:Int {
            mv.visitInsn(D2I);
        } else if jType is jvm:Long {
            mv.visitInsn(D2L);
        } else if jType is jvm:Float {
            mv.visitInsn(D2F);
        }
    }
}

function loadMethodParamToStackInInteropFunction(jvm:MethodVisitor mv,
                                                 bir:FunctionParam birFuncParam,
                                                 jvm:JType jMethodParamType,
                                                 string currentPackageName,
                                                 int localVarIndex) {
    // Load the parameter value to the stack
    generateVarLoad(mv, birFuncParam, currentPackageName, localVarIndex);
    bir:BType bFuncParamType = birFuncParam.typeValue;
    if bFuncParamType is bir:BTypeHandle && jMethodParamType is jvm:RefType {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, jMethodParamType.typeName);
    } else {
        performNarrowingPrimitiveConversion(mv, <BValueType>bFuncParamType, <jvm:PrimitiveType>jMethodParamType);
    }
}
