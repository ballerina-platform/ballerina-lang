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

type OldStyleExternalFunctionWrapper record {|
    * BIRFunctionWrapper;
    string jClassName;
    bir:BType?[] jMethodPramTypes;
    string jMethodVMSig;
|};

public type JavaMethodCall record  {|
    bir:DiagnosticPos pos;
    bir:VarRef?[] args;
    bir:TerminatorKind kind;
    bir:VarRef? lhsOp;
    string jClassName;
    string jMethodVMSig;
    string name;
    bir:BasicBlock thenBB;
|};

type ExternalFunctionWrapper JInteropFunctionWrapper | OldStyleExternalFunctionWrapper;

function genJMethodForBExternalFunc(bir:Function birFunc,
                                      jvm:ClassWriter cw,
                                      bir:Package birModule,
                                      bir:BType? attachedType = ()) {
    var extFuncWrapper = getExternalFunctionWrapper(birModule, birFunc, attachedType = attachedType);

    if extFuncWrapper is OldStyleExternalFunctionWrapper {
        genJMethodForBFunc(birFunc, cw, birModule, false, "", attachedType = attachedType);
    } else {
        genJMethodForBExternalFuncInterop(extFuncWrapper, cw, birModule);
    }
}

function injectDefaultParamInits(bir:Package module) {
    
    // filter out functions.
    bir:Function?[] functions = module.functions;
    if (functions.length() > 0) {
        int funcSize = functions.length();
        int count  = 3;

        // Generate classes for other functions.
        while (count < funcSize) {
            bir:Function birFunc = <bir:Function>functions[count];
            count = count + 1;
            var extFuncWrapper = lookupBIRFunctionWrapper(module, birFunc, attachedType = ());
            if extFuncWrapper is OldStyleExternalFunctionWrapper {
                desugarOldExternFuncs(module, extFuncWrapper, birFunc);
                enrichWithDefaultableParamInits(birFunc);
            } else if (!(extFuncWrapper is JMethodFunctionWrapper) && !(extFuncWrapper is JFieldFunctionWrapper)) {
                enrichWithDefaultableParamInits(birFunc);
            }
        }
    }

}


function desugarOldExternFuncs(bir:Package module, OldStyleExternalFunctionWrapper extFuncWrapper,
                                    bir:Function birFunc) {
    bir:BType retType = <bir:BType>birFunc.typeValue["retType"];

    bir:VarRef? retRef = ();
    if (!(retType is bir:BTypeNil)) {
        retRef = {variableDcl:getVariableDcl(birFunc.localVars[0]), typeValue:retType};
    }

    nextId = -1;
    nextVarId = -1;

    bir:BasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = "wrapperGen");
    bir:BasicBlock retBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = "wrapperGen");

    bir:VarRef?[] args = [];

    bir:VariableDcl? receiver = birFunc.receiver;
    if (!(receiver is ())) {
        bir:VarRef argRef = {variableDcl:receiver, typeValue:receiver.typeValue};
        args[args.length()] = argRef;
    }

    bir:FunctionParam?[] birFuncParams = birFunc.params;

    int birFuncParamIndex = 0;
    while (birFuncParamIndex < birFuncParams.length()) {
        bir:FunctionParam birFuncParam = <bir:FunctionParam>birFuncParams[birFuncParamIndex];
        bir:VarRef argRef = {variableDcl:birFuncParam, typeValue:birFuncParam.typeValue};
        args[args.length()] = argRef;
        birFuncParamIndex += 1;
    }

    string jMethodName = birFunc.name.value;
    JavaMethodCall jCall = {pos:birFunc.pos, args:args, kind:bir:TERMINATOR_PLATFORM, lhsOp:retRef,
                                jClassName:extFuncWrapper.jClassName, name:jMethodName, jMethodVMSig:extFuncWrapper.jMethodVMSig, thenBB:retBB};
    beginBB.terminator = jCall;

    bir:Return ret = {pos:birFunc.pos, kind:bir:TERMINATOR_RETURN};
    retBB.terminator = ret;
}

function getExternalFunctionWrapper(bir:Package birModule, bir:Function birFunc,
                                    bir:BType? attachedType = ())
                                    returns ExternalFunctionWrapper {

    var birFuncWrapper = lookupBIRFunctionWrapper(birModule, birFunc, attachedType = attachedType);
    if (birFuncWrapper is ExternalFunctionWrapper) {
        return birFuncWrapper;
    } else {
        panic error("cannot find function definition for : " + birFunc.name.value);// TODO improve
    }
}

function lookupBIRFunctionWrapper(bir:Package birModule, bir:Function birFunc,
                                    bir:BType? attachedType = ()) returns BIRFunctionWrapper {
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
    if (birFuncWrapper is BIRFunctionWrapper) {
        return birFuncWrapper;
    } else {
        panic error("cannot find function definition for : " + lookupKey);
    }
}

function createExternalFunctionWrapper(bir:Function birFunc, string orgName ,string moduleName, string versionValue,
                                        string  birModuleClassName) returns BIRFunctionWrapper | error {
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
        birFuncWrapper = check createJInteropFunctionWrapper(jInteropValidationReq, birFunc, orgName, moduleName,
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
                                        attachedType = attachedType, isExtern = true);

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
