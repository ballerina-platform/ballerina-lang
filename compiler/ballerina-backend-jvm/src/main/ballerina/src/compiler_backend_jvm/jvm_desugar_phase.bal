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

function addDefaultableBooleanVarsToSignature(bir:Function? func) {
    bir:Function currentFunc = getFunction(<@untainted> func);
    currentFunc.typeValue = currentFunc.typeValue.clone();
    currentFunc.typeValue.paramTypes = updateParamTypesWithDefaultableBooleanVar(currentFunc.typeValue.paramTypes,
    currentFunc.typeValue?.restType);
    int index = 0;
    bir:VariableDcl?[] updatedVars = [];
    bir:VariableDcl?[] localVars = currentFunc.localVars;
    int nameIndex = 0;

    foreach (var localVar in localVars) {
        updatedVars[index] = localVar;
        index += 1;
        if (localVar is bir:FunctionParam) {
            // An additional boolean arg is gen for each function parameter.
            string argName = "%syn" + nameIndex.toString();
            nameIndex += 1;
            bir:FunctionParam booleanVar = { kind: bir:VAR_KIND_ARG, name: { value: argName }, typeValue: "boolean", hasDefaultExpr: false };
            updatedVars[index] = booleanVar;
            index += 1;
        }
    }
    currentFunc.localVars = updatedVars;
}

function enrichWithDefaultableParamInits(bir:Function currentFunc) {

    int k = 1;
    bir:FunctionParam?[] functionParams = [];
    bir:VariableDcl?[] localVars = currentFunc.localVars;
    while (k < localVars.length()) {
        bir:VariableDcl localVar = getVariableDcl(localVars[k]);
        if (localVar is bir:FunctionParam) {
            functionParams[functionParams.length()] =  localVar;
        }
        k += 1;
    }

    nextId = -1;
    nextVarId = -1;

    bir:BasicBlock?[] basicBlocks = [];

    bir:BasicBlock nextBB = insertAndGetNextBasicBlock(basicBlocks);

    int paramCounter = 0;
    int paramBBCounter = 0;
    while (paramCounter < functionParams.length()) {
        var funcParam = functionParams[paramCounter];
        if (funcParam is bir:FunctionParam && funcParam.hasDefaultExpr) {
            int boolParam = paramCounter + 1;
            bir:FunctionParam funcBooleanParam = getFunctionParam(functionParams[boolParam]);
            bir:VarRef boolRef = {variableDcl:funcBooleanParam, typeValue:bir:TYPE_BOOLEAN};
            bir:UnaryOp notOp = {pos:{}, kind:bir:INS_KIND_NOT, lhsOp:boolRef, rhsOp:boolRef};
            nextBB.instructions[nextBB.instructions.length()] = notOp;
            bir:BasicBlock?[] bbArray = currentFunc.paramDefaultBBs[paramBBCounter];
            bir:BasicBlock trueBB = getBasicBlock(bbArray[0]);
            foreach var defaultBB in bbArray {
                basicBlocks[basicBlocks.length()] = getBasicBlock(defaultBB);
            }
            bir:BasicBlock falseBB = insertAndGetNextBasicBlock(basicBlocks);
            bir:Branch branch = {pos:{}, falseBB:falseBB, kind:bir:TERMINATOR_BRANCH, op:boolRef, trueBB:trueBB};
            nextBB.terminator = branch;

            bir:BasicBlock lastBB = getBasicBlock(bbArray[bbArray.length() - 1]);
            bir:GOTO gotoRet = {pos:{}, kind:bir:TERMINATOR_GOTO, targetBB:falseBB};
            lastBB.terminator = gotoRet;

            nextBB = falseBB;

            paramBBCounter += 1;
        }
        paramCounter += 2;
    }

    if (basicBlocks.length() == 1) {
    	// this means only one block added, if there are default vars, there must be more than one block
    	return;
    }
    if(currentFunc.basicBlocks.length() == 0) {
    	currentFunc.basicBlocks = basicBlocks;
        return;
    }

    int pl = currentFunc.basicBlocks.length();
    bir:BasicBlock firstBB = getBasicBlock(currentFunc.basicBlocks[0]);

    bir:GOTO gotoRet = {pos:{}, kind:bir:TERMINATOR_GOTO, targetBB:firstBB};
    nextBB.terminator = gotoRet;
    foreach var bb in currentFunc.basicBlocks {
     	basicBlocks[basicBlocks.length()] = bb;
    }

    int nl = basicBlocks.length();
    currentFunc.basicBlocks = basicBlocks;
}

function insertAndGetNextBasicBlock(bir:BasicBlock?[] basicBlocks, string prefix = "desugaredBB") returns bir:BasicBlock {
    bir:BasicBlock nextbb = {id: getNextDesugarBBId(prefix), instructions: []};
    basicBlocks[basicBlocks.length()] = nextbb;
    return nextbb;
}

function getNextDesugarBBId(string prefix) returns bir:Name {
    string bbIdPrefix = prefix;
    nextId += 1;
    return {value:bbIdPrefix + nextId.toString()};
}

function updateParamTypesWithDefaultableBooleanVar(bir:BType?[] funcParams, bir:BType? restType) returns bir:BType?[] {
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
    if (!(restType is ())) {
        paramTypes[index] = restType;
        paramTypes[index+1] = "boolean";
    }
    return paramTypes;
}

public function rewriteRecordInits(bir:TypeDef?[] typeDefs) {
    foreach (bir:TypeDef? typeDef in typeDefs) {
        bir:BType? recordType = typeDef?.typeValue;
        if (recordType is bir:BRecordType) {
            bir:Function?[] attachFuncs = <bir:Function?[]> typeDef?.attachedFuncs;
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
