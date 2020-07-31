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
import ballerina/io;

type JMethodFunctionWrapper record {|
    * BIRFunctionWrapper;
    jvm:Method jMethod;
|};

type JFieldFunctionWrapper record {|
    * BIRFunctionWrapper;
    jvm:Field jField;
|};

// Java specific terminator kinds
public const JTERM_CALL = 1;

public const JTERM_NEW = 2;

public type JTermKind JTERM_CALL | JTERM_NEW;

// Java specific terminator definitions
public type JTerminator record {|
    bir:DiagnosticPos pos;
    bir:TerminatorKind kind = bir:TERMINATOR_PLATFORM;
    JTermKind jKind;
    anydata...;
|};

public type JIMethodCall record  {|
    bir:DiagnosticPos pos;
    bir:VarRef?[] args;
    boolean varArgExist;
    jvm:JType? varArgType;
    bir:TerminatorKind kind = bir:TERMINATOR_PLATFORM;
    bir:VarRef? lhsOp;
    JTermKind jKind = JTERM_CALL;
    string jClassName;
    string jMethodVMSig;
    string name;
    int invocationType;
    bir:BasicBlock thenBB;
|};

public type JIConstructorCall record  {|
    bir:DiagnosticPos pos;
    bir:VarRef?[] args;
    boolean varArgExist;
    jvm:JType? varArgType;
    bir:TerminatorKind kind = bir:TERMINATOR_PLATFORM;
    bir:VarRef? lhsOp;
    JTermKind jKind = JTERM_NEW;
    string jClassName;
    string jMethodVMSig;
    string name;
    bir:BasicBlock thenBB;
|};

// Java specific instruction kinds
public const JCAST = 1;

public const JNEW = 2;

public type JInsKind JCAST | JNEW;

// Java specific instruction definitions
public type JInstruction record  {|
    bir:DiagnosticPos pos;
    bir:InstructionKind kind = bir:INS_KIND_PLATFORM;
    JInsKind jKind;
    anydata...;
|};

public type JCast record {|
    bir:DiagnosticPos pos;
    bir:InstructionKind kind = bir:INS_KIND_PLATFORM;
    JInsKind jKind = JCAST;
    bir:VarRef lhsOp;
    bir:VarRef rhsOp;
    bir:BType targetType;
|};

public type JErrorEntry record {|
    bir:BasicBlock trapBB;
    bir:BasicBlock endBB;
    bir:VarRef errorOp;
    bir:BasicBlock targetBB;
    CatchIns[] catchIns;
|};

public type CatchIns record {|
    string errorClass;
    bir:Return term;
|};

type JInteropFunctionWrapper JMethodFunctionWrapper | JFieldFunctionWrapper;

function createJInteropFunctionWrapper(jvm:InteropValidator interopValidator,
                                       jvm:InteropValidationRequest jInteropValidationReq,
                                       bir:Function birFunc,
                                       string orgName,
                                       string moduleName,
                                       string versionValue,
                                       string  birModuleClassName) returns JInteropFunctionWrapper | error  {

    addDefaultableBooleanVarsToSignature(birFunc);
    // Update the function wrapper only for Java interop functions
    BIRFunctionWrapper birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName,
                                                versionValue, birModuleClassName);
    if (jInteropValidationReq is jvm:MethodValidationRequest) {
        jInteropValidationReq.restParamExist = birFunc.restParamExist;
        return createJMethodWrapper(interopValidator, jInteropValidationReq, birFuncWrapper);
    } else {
        return createJFieldWrapper(interopValidator, jInteropValidationReq, birFuncWrapper);
    }
}

function createJMethodWrapper(jvm:InteropValidator interopValidator, jvm:MethodValidationRequest jMethodValidationReq,
                              BIRFunctionWrapper birFuncWrapper) returns JMethodFunctionWrapper | error {
    var jMethod = check interopValidator.validateAndGetJMethod(jMethodValidationReq);

    return  {
        orgName : birFuncWrapper.orgName,
        moduleName : birFuncWrapper.moduleName,
        versionValue : birFuncWrapper.versionValue,
        func : birFuncWrapper.func,
        fullQualifiedClassName : birFuncWrapper.fullQualifiedClassName,
        jvmMethodDescription : birFuncWrapper.jvmMethodDescription,
        jMethod: <jvm:Method>jMethod
    };
}

function createJFieldWrapper(jvm:InteropValidator interopValidator, jvm:FieldValidationRequest jFieldValidationReq,
                             BIRFunctionWrapper birFuncWrapper) returns JFieldFunctionWrapper | error  {
    var jField = check interopValidator.validateAndGetJField(jFieldValidationReq);

    return  {
        orgName : birFuncWrapper.orgName,
        moduleName : birFuncWrapper.moduleName,
        versionValue : birFuncWrapper.versionValue,
        func : birFuncWrapper.func,
        fullQualifiedClassName : birFuncWrapper.fullQualifiedClassName,
        jvmMethodDescription : birFuncWrapper.jvmMethodDescription,
        jField: <jvm:Field>jField
    };
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
    InstructionGenerator instGen = new(mv, indexMap, birModule);
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
                            -1, strandParamIndex, true, birModule, currentPackageName, (), false);
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
        mv.visitFieldInsn(GETSTATIC, BAL_ERROR_REASONS, "JAVA_NULL_REFERENCE_ERROR", "L" + STRING_VALUE + ";");
        mv.visitFieldInsn(GETSTATIC, RUNTIME_ERRORS, "JAVA_NULL_REFERENCE", "L" + RUNTIME_ERRORS + ";");
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitMethodInsn(INVOKESTATIC, BLANG_EXCEPTION_HELPER, "getRuntimeException",
            "(L" + STRING_VALUE + ";L" + RUNTIME_ERRORS + ";[L" + OBJECT + ";)L" + ERROR_VALUE + ";", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(elseBlockLabel);
    }

    // Load java method parameters
    birFuncParamIndex = jField.isStatic ? 0: 2;
    int jMethodParamIndex = 0;
    if (birFuncParamIndex < birFuncParams.length()) {
        var birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        int paramLocalVarIndex = indexMap.getIndex(birFuncParam);
        loadMethodParamToStackInInteropFunction(mv, birFuncParam, jFieldType, currentPackageName, paramLocalVarIndex,
                                                indexMap, false);
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
    bir:BType retType = <bir:BType>birFunc.typeValue["retType"];
    bir:VariableDcl retVarDcl = { typeValue: <bir:BType>retType, name: { value: "$_ret_var_$" }, kind: "LOCAL" };
    int returnVarRefIndex = indexMap.getIndex(retVarDcl);

    if retType is bir:BTypeNil {
        mv.visitInsn(ACONST_NULL);
    } else if retType is bir:BTypeHandle {
        // Here the corresponding Java method parameter type is 'jvm:RefType'. This has been verified before
        bir:VariableDcl retJObjectVarDcl = { typeValue: "any", name: { value: "$_ret_jobject_var_$" }, kind: "LOCAL" };
        int returnJObjectVarRefIndex = indexMap.getIndex(retJObjectVarDcl);
        mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
        mv.visitTypeInsn(NEW, HANDLE_VALUE);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
        mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, "<init>", "(Ljava/lang/Object;)V", false);
    } else {
        // bType is a value-type
        if(jFieldType is jvm:JPrimitiveType) {
            performWideningPrimitiveConversion(mv, <BValueType>retType, jFieldType);
        } else {
            addUnboxInsn(mv, retType);
        }
    }

    generateVarStore(mv, retVarDcl, currentPackageName, returnVarRefIndex);

    jvm:Label retLabel = labelGen.getLabel("return_lable");
    mv.visitLabel(retLabel);
    mv.visitLineNumber(birFunc.pos.sLine, retLabel);
    termGen.genReturnTerm({pos:{}, kind:"RETURN"}, returnVarRefIndex, birFunc);
    mv.visitMaxs(200, 400);
    mv.visitEnd();
}

function desugarInteropFuncs(bir:Package module, JMethodFunctionWrapper extFuncWrapper,
                                    bir:Function birFunc) {
    // resetting the variable generation index
    bir:BType retType = <bir:BType>birFunc.typeValue["retType"];
    jvm:Method jMethod = extFuncWrapper.jMethod;
    jvm:MethodType jMethodType = jMethod.mType;
    jvm:JType[] jMethodParamTypes = jMethodType.paramTypes;
    jvm:JType jMethodRetType = jMethodType.retType;


    nextId = -1;
    nextVarId = -1;
    string bbPrefix = "wrapperGen";

    bir:BasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = bbPrefix);
    bir:BasicBlock retBB = {id: getNextDesugarBBId(bbPrefix), instructions: []};

    bir:VarRef?[] args = [];

    bir:VariableDcl? receiver = birFunc.receiver;

    bir:FunctionParam?[] birFuncParams = birFunc.params;
    int birFuncParamIndex = 0;
    // Load receiver which is the 0th parameter in the birFunc
    if jMethod.kind is jvm:METHOD && !jMethod.isStatic {
        bir:FunctionParam birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        bir:BType bPType = birFuncParam.typeValue;
        bir:VarRef argRef = {variableDcl:birFuncParam, typeValue:bPType};
        args[args.length()] = argRef;
        birFuncParamIndex = 1;
    }

    jvm:JType? varArgType = ();
    int jMethodParamIndex = 0;
    int paramCount = birFuncParams.length();
    while (birFuncParamIndex < paramCount) {
        bir:FunctionParam birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        boolean isVarArg = (birFuncParamIndex == (paramCount - 1)) && birFunc.restParamExist;
        bir:BType bPType = birFuncParam.typeValue;
        jvm:JType jPType = jMethodParamTypes[jMethodParamIndex];
        bir:VarRef argRef = {variableDcl:birFuncParam, typeValue:bPType};
        // we generate cast operations for unmatching B to J types
        if (!isVarArg && !isMatchingBAndJType(bPType, jPType)) {
            string varName = "$_param_jobject_var" + birFuncParamIndex.toString() + "_$";
            bir:VariableDcl paramVarDcl = { typeValue:jPType, name: { value: varName }, kind: "LOCAL" };
            birFunc.localVars[birFunc.localVars.length()] = paramVarDcl;
            bir:VarRef paramVarRef = {typeValue:jPType, variableDcl:paramVarDcl};
            JCast jToBCast = {pos:birFunc.pos, lhsOp:paramVarRef, rhsOp:argRef, targetType:jPType};
            argRef = paramVarRef;
            beginBB.instructions[beginBB.instructions.length()] = jToBCast;
        }
        // for var args, we have two options
        // 1 - desugar java array creation here,
        // 2 - keep the var arg type in the intstruction and do the array creation in instruction gen
        // we are going with the option two for the time being, hence keeping var arg type in the instructions
        // (drawback with option 2 is, function frame may not have proper variables)
        if (isVarArg) {
            varArgType = jPType;
        }
        args[args.length()] = argRef;
        birFuncParamIndex += 1;
        jMethodParamIndex += 1;
    }

    int invocationType = INVOKESTATIC;
    if jMethod.kind is jvm:METHOD && !jMethod.isStatic {
        if jMethod.isInterface {
            invocationType = INVOKEINTERFACE;
        } else {
            invocationType = INVOKEVIRTUAL;
        }
    } else if jMethod.kind is jvm:METHOD && jMethod.isStatic {
    	// nothing to do - remove later
    } else {
        invocationType = INVOKESPECIAL;
    }

    bir:VarRef? jRetVarRef = ();

    bir:BasicBlock thenBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = bbPrefix);
    bir:GOTO gotoRet = {pos:{}, kind:bir:TERMINATOR_GOTO, targetBB:retBB};
    thenBB.terminator = gotoRet;

    if (!(retType is bir:BTypeNil)) {
        bir:VarRef retRef = {variableDcl:getVariableDcl(birFunc.localVars[0]), typeValue:retType};
        if (!(jMethodRetType is jvm:JVoid)) {
            bir:VariableDcl retJObjectVarDcl = { typeValue:jMethodRetType, name: { value: "$_ret_jobject_var_$" }, kind: "LOCAL" };
            birFunc.localVars[birFunc.localVars.length()] = retJObjectVarDcl;
            bir:VarRef castVarRef = {typeValue:jMethodRetType, variableDcl:retJObjectVarDcl};
            jRetVarRef = castVarRef;
            JCast jToBCast = {pos:birFunc.pos, lhsOp:retRef, rhsOp:castVarRef, targetType:retType};
            thenBB.instructions[thenBB.instructions.length()] = jToBCast;
        }

        bir:BasicBlock catchBB = {id: getNextDesugarBBId(bbPrefix), instructions: []};
        JErrorEntry ee = { trapBB:beginBB, endBB:thenBB, errorOp:retRef, targetBB:catchBB, catchIns:[] };
        foreach var exception in extFuncWrapper.jMethod.throws {
            bir:Return exceptionRet = {pos:{}, kind:bir:TERMINATOR_RETURN};
            CatchIns catchIns = { error'class:exception, term:exceptionRet };
            ee.catchIns[ee.catchIns.length()] = catchIns;
        }

        birFunc.errorEntries[birFunc.errorEntries.length()] = ee;
    }

    string jMethodName = birFunc.name.value;
    // We may be able to use the same instruction rather than two, check later
    if jMethod.kind is jvm:CONSTRUCTOR {
        JIConstructorCall jCall = {pos:birFunc.pos, args:args, varArgExist:birFunc.restParamExist, varArgType:varArgType,
	                        kind:bir:TERMINATOR_PLATFORM, lhsOp:jRetVarRef, jClassName:jMethod.class, name:jMethod.name,
				jMethodVMSig:jMethod.sig, thenBB:thenBB};
        beginBB.terminator = jCall;
    } else {
        JIMethodCall jCall = {pos:birFunc.pos, args:args, varArgExist:birFunc.restParamExist, varArgType:varArgType,
				kind:bir:TERMINATOR_PLATFORM, lhsOp:jRetVarRef, jClassName:jMethod.class, name:jMethod.name,
				jMethodVMSig:jMethod.sig, invocationType:invocationType, thenBB:thenBB};
        beginBB.terminator = jCall;
    }

    // Adding the returnBB to the end of BB list
    birFunc.basicBlocks[birFunc.basicBlocks.length()] = retBB;

    bir:Return ret = {pos:birFunc.pos, kind:bir:TERMINATOR_RETURN};
    retBB.terminator = ret;

    //json|error j = json.constructFrom(birFunc);
    //if (j is json) {
    //	io:println(j.toJsonString());
    //} else {
    //	io:println(j);
    //}
}

function isMatchingBAndJType(bir:BType sourceTypes, jvm:JType targetType) returns boolean {
    if ((sourceTypes is bir:BTypeInt && targetType is jvm:JLong) ||
              (sourceTypes is bir:BTypeFloat && targetType is jvm:JDouble) ||
              (sourceTypes is bir:BTypeBoolean && targetType is jvm:JBoolean)) {
        return true;
    }
    return false;
}

type BValueType bir:BTypeInt | bir:BTypeFloat | bir:BTypeBoolean | bir:BTypeByte | bir:BTypeNil;

// These conversions are already validate beforehand, therefore I am just emitting type conversion instructions here.
// We can improve following logic with a type lattice.
function performWideningPrimitiveConversion(jvm:MethodVisitor mv, BValueType bType, jvm:JPrimitiveType jType){
    if bType is bir:BTypeInt && jType is jvm:JLong {
        return; // NOP
    } else if bType is bir:BTypeFloat && jType is jvm:JDouble {
        return; // NOP
    } else if bType is bir:BTypeInt {
        mv.visitInsn(I2L);
    } else if bType is bir:BTypeFloat {
        if jType is jvm:JLong {
            mv.visitInsn(L2D);
        } else if jType is jvm:JFloat {
            mv.visitInsn(F2D);
        } else {
            mv.visitInsn(I2D);
        }
    }
}

function loadMethodParamToStackInInteropFunction(jvm:MethodVisitor mv,
                                                 bir:FunctionParam birFuncParam,
                                                 jvm:JType jMethodParamType,
                                                 string currentPackageName,
                                                 int localVarIndex,
                                                 BalToJVMIndexMap indexMap,
                                                 boolean isVarArg) {
    bir:BType bFuncParamType = birFuncParam.typeValue;
    if (isVarArg) {
        genVarArg(mv, indexMap, bFuncParamType, jMethodParamType, localVarIndex);
    } else {
        // Load the parameter value to the stack
        generateVarLoad(mv, birFuncParam, currentPackageName, localVarIndex);
        generateBToJCheckCast(mv, bFuncParamType, <jvm:JType>jMethodParamType);
    }
}

function getJTypeSignature(jvm:JType jType) returns string {
    if (jType is jvm:JRefType) {
        return "L" + jType.typeValue + ";";
    } else if (jType is jvm:JArrayType) {
        jvm:JType eType = jType.elementType;
        return "[" + getJTypeSignature(eType);
    } else {
        if (jType is jvm:JByte) {
            return "B";
        } else if (jType is jvm:JChar) {
            return "C";
        } else if (jType is jvm:JShort) {
            return "S";
        } else if (jType is jvm:JInt) {
            return "I";
        } else if (jType is jvm:JLong) {
            return "J";
        } else if (jType is jvm:JFloat) {
            return "F";
        } else if (jType is jvm:JDouble) {
            return "D";
        } else if (jType is jvm:JBoolean ) {
            return "Z";
        } else {
            error e = error(io:sprintf("invalid element type: %s", jType));
            panic e;
        }
    }
}

function getSignatureForJType(jvm:JRefType|jvm:JArrayType jType) returns string {
    if (jType is jvm:JRefType) {
        return jType.typeValue;
    } else {
        jvm:JType eType = jType.elementType;
        string sig = "[";
        while (eType is jvm:JArrayType) {
            eType = eType.elementType;
            sig += "[";
        }

        if (eType is jvm:JRefType) {
            return sig + "L" + getSignatureForJType(eType) + ";";
        } else if (eType is jvm:JByte) {
            return sig + "B";
        } else if (eType is jvm:JChar) {
            return sig + "C";
        } else if (eType is jvm:JShort) {
            return sig + "S";
        } else if (eType is jvm:JInt) {
            return sig + "I";
        } else if (eType is jvm:JLong) {
            return sig + "J";
        } else if (eType is jvm:JFloat) {
            return sig + "F";
        } else if (eType is jvm:JDouble) {
            return sig + "D";
        } else if (eType is jvm:JBoolean ) {
            return sig + "Z";
        } else {
            error e = error(io:sprintf("invalid element type: %s", eType));
            panic e;
        }
    }
}

function genVarArg(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, bir:BType bType, jvm:JType jvmType,
                   int varArgIndex) {
    jvm:JType jElementType;
    bir:BType bElementType;
    if (jvmType is jvm:JArrayType && bType is bir:BArrayType) {
        jElementType = jvmType.elementType;
        bElementType = bType.eType;
    } else {
        error e = error(io:sprintf("invalid type for var-arg: %s", jvmType));
        panic e;
    }

    bir:VariableDcl varArgsLen = { typeValue: bir:TYPE_INT,
                                   name: { value: "$varArgsLen" },
                                   kind: bir:VAR_KIND_TEMP };
    bir:VariableDcl index = { typeValue: bir:TYPE_INT,
                              name: { value: "$index" },
                              kind: bir:VAR_KIND_TEMP };
    bir:VariableDcl valueArray = { typeValue: bir:TYPE_ANY,
                                   name: { value: "$valueArray" },
                                   kind: bir:VAR_KIND_TEMP };

    int varArgsLenVarIndex = indexMap.getIndex(varArgsLen);
    int indexVarIndex = indexMap.getIndex(index);
    int valueArrayIndex = indexMap.getIndex(valueArray);

    // get the number of var args provided
    mv.visitVarInsn(ALOAD, varArgIndex);
    mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "size", "()I", true);
    mv.visitInsn(DUP);  // duplicate array size - needed for array new
    mv.visitVarInsn(ISTORE, varArgsLenVarIndex);

    // create an array to hold the results. i.e: jvm values
    genArrayNew(mv, jElementType);
    mv.visitVarInsn(ASTORE, valueArrayIndex);

    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, indexVarIndex);
    jvm:Label l1 = new jvm:Label();
    jvm:Label l2 = new jvm:Label();
    mv.visitLabel(l1);

    // if index >= varArgsLen, then jump to end
    mv.visitVarInsn(ILOAD, indexVarIndex);
    mv.visitVarInsn(ILOAD, varArgsLenVarIndex);
    mv.visitJumpInsn(IF_ICMPGE, l2);

    // `valueArray` and `index` to stack, for lhs of assignment
    mv.visitVarInsn(ALOAD, valueArrayIndex);
    mv.visitVarInsn(ILOAD, indexVarIndex);

    // load `varArg[index]`
    mv.visitVarInsn(ALOAD, varArgIndex);
    mv.visitVarInsn(ILOAD, indexVarIndex);
    mv.visitInsn(I2L);

    if (bElementType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
    } else if (bElementType is bir:BTypeString) {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE), true);
    } else if (bElementType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
    } else if (bElementType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
    } else if (bElementType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
    } else if (bElementType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), true);
        mv.visitTypeInsn(CHECKCAST, HANDLE_VALUE);
    } else {
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), true);
    }

    // unwrap from handleValue
    generateBToJCheckCast(mv, bElementType, <jvm:JType>jElementType);

    // valueArray[index] = varArg[index]
    genArrayStore(mv, jElementType);

    // // increment index, and go to the condition again
    mv.visitIincInsn(indexVarIndex, 1);
    mv.visitJumpInsn(GOTO, l1);

    mv.visitLabel(l2);
    mv.visitVarInsn(ALOAD, valueArrayIndex);
}

function genArrayStore(jvm:MethodVisitor mv, jvm:JType jType) {
    int code;
    if jType is jvm:JInt {
        code = IASTORE;
    } else if jType is jvm:JLong {
        code = LASTORE;
    } else if jType is jvm:JDouble {
        code = DASTORE;
    } else if jType is jvm:JByte || jType is jvm:JBoolean {
        code = BASTORE;
    } else if jType is jvm:JShort {
        code = SASTORE;
    } else if jType is jvm:JChar {
        code = CASTORE;
    } else if jType is jvm:JFloat {
        code = FASTORE;
    } else {
        code = AASTORE;
    }

    mv.visitInsn(code);
}

function genArrayNew(jvm:MethodVisitor mv, jvm:JType elementType) {
    if elementType is jvm:JInt {
        mv.visitIntInsn(NEWARRAY, T_INT);
    } else if elementType is jvm:JLong {
        mv.visitIntInsn(NEWARRAY, T_LONG);
    } else if elementType is jvm:JDouble {
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
    } else if elementType is jvm:JByte || elementType is jvm:JBoolean {
        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
    } else if elementType is jvm:JShort {
        mv.visitIntInsn(NEWARRAY, T_SHORT);
    } else if elementType is jvm:JChar {
        mv.visitIntInsn(NEWARRAY, T_CHAR);
    } else if elementType is jvm:JFloat {
        mv.visitIntInsn(NEWARRAY, T_FLOAT);
    } else if elementType is jvm:JRefType | jvm:JArrayType {
        mv.visitTypeInsn(ANEWARRAY, getSignatureForJType(elementType));
    } else {
        error e = error(io:sprintf("invalid type for var-arg: %s", elementType));
        panic e;
    }
}
