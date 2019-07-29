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

type OldStyleExternalFunctionWrapper record {|
    * BIRFunctionWrapper;
    string jClassName;
    bir:BType?[] jMethodPramTypes;
    string jMethodVMSig;
|};

type ExternalFunctionWrapper JInteropFunctionWrapper | OldStyleExternalFunctionWrapper;

function genJMethodForBExternalFunc(bir:Function birFunc,
                                      jvm:ClassWriter cw,
                                      bir:Package birModule,
                                      bir:BType? attachedType = ()) {
    var extFuncWrapper = getExternalFunctionWrapper(birModule, birFunc, attachedType = attachedType);

    if extFuncWrapper is OldStyleExternalFunctionWrapper {
        genJMethodForBExternalFuncOldStyle(extFuncWrapper, cw, birModule, attachedType = attachedType);
    } else {
        genJMethodForBExternalFuncInterop(extFuncWrapper, cw, birModule);
    }
}

function genJMethodForBExternalFuncOldStyle(OldStyleExternalFunctionWrapper extFuncWrapper,
                                            jvm:ClassWriter cw,
                                            bir:Package birModule,
                                            bir:BType? attachedType = ()) {

    var currentPackageName = getPackageName(birModule.org.value, birModule.name.value);

    // Create a local variable for the strand
    BalToJVMIndexMap indexMap = new;
    bir:VariableDcl strandVarDcl = { typeValue: "string", name: { value: "$_strand_$" }, kind: "ARG" };
    int strandParamIndex = indexMap.getIndex(strandVarDcl);

    // generate method desc
    bir:Function birFunc = extFuncWrapper.func;
    string desc = getMethodDesc(birFunc.typeValue.paramTypes, birFunc.typeValue["retType"]);
    int access = ACC_PUBLIC;
    string selfParamName = "$_self_$";
    int selfParamIndex = -1;
    if attachedType is () {
        access += ACC_STATIC;
    } else {
        bir:VariableDcl selfVar = { typeValue: attachedType, name: { value: "$_self_$" }, kind: "ARG" };
        selfParamIndex = indexMap.getIndex(selfVar);
    }

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
                            -1, strandParamIndex, true, birModule, currentPackageName, attachedType);
        mv.visitLabel(paramNextLabel);

        birFuncParamIndex += 1;
        paramDefaultsBBIndex += 1;
    }

    // Load function parameters of the target Java method to the stack..
    if attachedType is () {
        mv.visitVarInsn(ALOAD, strandParamIndex);
    } else {
        // check whether function params already include the self
        mv.visitVarInsn(ALOAD, selfParamIndex);
        mv.visitVarInsn(ALOAD, strandParamIndex);
    }

    birFuncParamIndex = 0;
    while (birFuncParamIndex < birFuncParams.length()) {
        var birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        int paramLocalVarIndex = indexMap.getIndex(birFuncParam);
        generateVarLoad(mv, birFuncParam, currentPackageName, paramLocalVarIndex);
        birFuncParamIndex += 2;
    }

    string jMethodName = birFunc.name.value;
    mv.visitMethodInsn(INVOKESTATIC, extFuncWrapper.jClassName, jMethodName, extFuncWrapper.jMethodVMSig, false);

    // Handle return type
    int returnVarRefIndex = -1;
    bir:BType retType = <bir:BType>birFunc.typeValue["retType"];
    if retType is bir:BTypeNil {
    } else {
        bir:VariableDcl retVarDcl = { typeValue: <bir:BType>retType, name: { value: "$_ret_var_$" }, kind: "LOCAL" };
        returnVarRefIndex = indexMap.getIndex(retVarDcl);
        generateVarStore(mv, retVarDcl, currentPackageName, returnVarRefIndex);
    }

    jvm:Label retLabel = labelGen.getLabel("return_lable");
    mv.visitLabel(retLabel);
    mv.visitLineNumber(birFunc.pos.sLine, retLabel);
    termGen.genReturnTerm({pos:{}, kind:"RETURN"}, returnVarRefIndex, birFunc);
    mv.visitMaxs(200, 400);
    mv.visitEnd();
}

function getExternalFunctionWrapper(bir:Package birModule, bir:Function birFunc,
                                    bir:BType? attachedType = ())
                                    returns ExternalFunctionWrapper {

    string lookupKey;
    var currentPackageName = getPackageName(birModule.org.value, birModule.name.value);
    string birFuncName = birFunc.name.value;

    if attachedType is () {
        lookupKey = currentPackageName + birFuncName;
    } else if attachedType is bir:BObjectType  {
        lookupKey = currentPackageName + attachedType.name.value + "." + birFuncName;
    } else {
        panic error(io:sprintf("Java method generation for the receiver type %s is not supported: ", attachedType));
    }

    var birFuncWrapper = birFunctionMap[lookupKey];
    if (birFuncWrapper is ExternalFunctionWrapper) {
        return birFuncWrapper;
    } else {
        panic error("cannot find function definition for : " + lookupKey);
    }
}

function createExternalFunctionWrapper(bir:Function birFunc, string orgName ,string moduleName,
                                       string versionValue,  string  birModuleClassName) returns BIRFunctionWrapper {
    BIRFunctionWrapper birFuncWrapper;
    jvm:InteropValidationRequest? jInteropValidationReq = getInteropAnnotValue(birFunc);
    if (jInteropValidationReq is ()) {
        // This is a old-style external Java interop function
        string pkgName = getPackageName(orgName, moduleName);
        var jClassName = lookupExternClassName(cleanupPackageName(pkgName), birFunc.name.value);
        if (jClassName is string) {
            if isBallerinaBuiltinModule(orgName, moduleName) {
                birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName, versionValue, jClassName);
            } else {
                birFuncWrapper = createOldStyleExternalFunctionWrapper(birFunc, orgName, moduleName, versionValue,
                                            birModuleClassName, jClassName);
            }
        } else {
            error err = error("cannot find full qualified class name for extern function : " + pkgName +
                                                birFunc.name.value);
            panic err;
        }
    } else {
        birFuncWrapper = createJInteropFunctionWrapper(jInteropValidationReq, birFunc, orgName, moduleName,
                                versionValue, birModuleClassName);
    }

    return birFuncWrapper;
}

function createOldStyleExternalFunctionWrapper(bir:Function birFunc, string orgName,
                                    string moduleName, string versionValue, string birModuleClassName,
                                    string jClassName) returns OldStyleExternalFunctionWrapper {

    bir:BType?[] jMethodPramTypes = birFunc.typeValue.paramTypes.clone();
    addDefaultableBooleanVarsToSignature(birFunc);
    bir:BInvokableType functionTypeDesc = birFunc.typeValue;

    bir:VariableDcl? receiver = birFunc.receiver;
    bir:BType? attachedType = receiver is bir:VariableDcl ? receiver.typeValue : ();
    string jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, <bir:BType?> functionTypeDesc?.retType,
                                                attachedType = attachedType);
    string jMethodVMSig = getMethodDesc(jMethodPramTypes, <bir:BType?> functionTypeDesc?.retType,
                                        attachedType = attachedType);

    return {
        orgName : orgName,
        moduleName : moduleName,
        versionValue : versionValue,
        func : birFunc,
        fullQualifiedClassName : birModuleClassName,
        jvmMethodDescription : jvmMethodDescription,
        jClassName: jClassName,
        jMethodPramTypes: jMethodPramTypes,
        jMethodVMSig: jMethodVMSig
    };
}

function isBallerinaBuiltinModule(string orgName, string moduleName) returns boolean {
    return orgName == "ballerina" && moduleName == "builtin";
}
