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

function addDefaultableBooleanVarsToSignature(bir:Function? func) {
    bir:Function currentFunc = getFunction(<@untainted> func);
    currentFunc.typeValue = currentFunc.typeValue.clone();
    currentFunc.typeValue.paramTypes = updateParamTypesWithDefaultableBooleanVar(currentFunc.typeValue.paramTypes);
    int index = 0;
    bir:VariableDcl?[] updatedVars = [];
    bir:VariableDcl?[] localVars = currentFunc.localVars;
    int nameIndex = 0;

    foreach (var localVar in localVars) {
        updatedVars[index] = localVar;
        index += 1;
        if (localVar is bir:FunctionParam) {
            // An additional boolean arg is gen for each function parameter.
            string argName = "%syn" + nameIndex;
            nameIndex += 1;
            bir:FunctionParam booleanVar = { kind: bir:VAR_KIND_ARG, name: { value: argName }, typeValue: "boolean", hasDefaultExpr: false };
            updatedVars[index] = booleanVar;
            index += 1;
        }
    }
    currentFunc.localVars = updatedVars;
}

function updateParamTypesWithDefaultableBooleanVar(bir:BType?[] funcParams) returns bir:BType?[] {
    bir:BType?[] paramTypes = [];

    int counter = 0;
    int index = 0;
    // Update the param types to add boolean variables to indicate if the previous variable contains a user given value
    while (counter < funcParams.length()) {
        paramTypes[index] = funcParams[counter];
        paramTypes[index+1] = "boolean";
        index += 2;
        counter += 1;
    }
    return paramTypes;
}

public function rewriteRecordInits(bir:TypeDef?[] typeDefs) {
    foreach (bir:TypeDef? typeDef in typeDefs) {
        bir:BType? recordType = typeDef.typeValue;
        if (recordType is bir:BRecordType) {
            bir:Function?[] attachFuncs = <bir:Function?[]> typeDef.attachedFuncs;
            foreach var func in attachFuncs {
                rewriteRecordInitFunction(<bir:Function> func, recordType);
            }
        }
    }
}

function rewriteRecordInitFunction(bir:Function func, bir:BRecordType recordType) {
    bir:VariableDcl receiver = <bir:VariableDcl> func.receiver;

    // Rename the function name by appending the record name to it.
    // This done to avoid frame class name overlappings.
    func.name.value = cleanupFunctionName(recordType.name.value + func.name.value);

    // change the kind of receiver to 'ARG'
    receiver.kind = bir:VAR_KIND_ARG;

    // Update the name of the reciever. Then any instruction that was refering to the receiver will
    // now refer to the injected parameter. 
    string paramName = "$_" + receiver.name.value;
    receiver.name.value = paramName;

    // Inject an additional parameter to accept the self-record value into the init function
    bir:FunctionParam selfParam = { kind: bir:VAR_KIND_ARG,
                                    name: receiver.name, 
                                    typeValue: receiver.typeValue,
                                    hasDefaultExpr: false,
                                    meta : { name : paramName }
                                  };

    func.typeValue = func.typeValue.clone();
    func.typeValue.paramTypes = [receiver.typeValue];

    bir:VariableDcl?[] localVars = func.localVars;
    bir:VariableDcl?[] updatedLocalVars = [localVars[0], selfParam];
    int index = 1;
    while (index < localVars.length()) {
        updatedLocalVars[index + 1] = localVars[index];
        index += 1;
    }
    func.localVars = updatedLocalVars;
}
