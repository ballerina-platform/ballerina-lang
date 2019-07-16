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
