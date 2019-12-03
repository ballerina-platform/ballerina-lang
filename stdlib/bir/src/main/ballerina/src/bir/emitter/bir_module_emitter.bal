
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

public function emitModule(Package mod) returns string {
    string modStr = "================ Emitting Module ================";
    modStr += emitLBreaks(1);
    modStr += "module";
    modStr += emitSpaces(1);
    modStr += emitName(mod.org) + "/" + emitName(mod.name);
    modStr += emitSpaces(1);
    modStr += "v";
    modStr += emitSpaces(1);
    modStr += emitName(mod.versionValue) + ";";
    modStr += emitLBreaks(2);
    modStr += emitImports(mod.importModules);
    modStr += emitLBreaks(2);
    modStr += emitTypeDefs(mod.typeDefs);
    modStr += emitLBreaks(2);
    modStr += emitFunctions(mod.functions);

    modStr += emitLBreaks(1);
    modStr += "================ Emitting Module ================";
    return modStr;
}	

function emitImports(ImportModule[] impMods) returns string {
    string impStr = "";
    foreach ImportModule mod in impMods {
        impStr += emitImport(mod);
        impStr += emitLBreaks(1);
    }
    return impStr;
}

function emitImport(ImportModule impMod) returns string {
    string impStr = "import ";
    impStr += emitName(impMod.modOrg) + "/";
    impStr += emitName(impMod.modName);
    if !isEmpty(impMod.modVersion) {
        impStr += emitSpaces(1);
        impStr += "v";
        impStr += emitSpaces(1);
        impStr += emitName(impMod.modVersion);
    }
    impStr += ";";
    return impStr;
}

function emitTypeDefs(TypeDef?[] typeDefs) returns string {
    string tDefStr = "";
    foreach TypeDef? tDef in typeDefs {
        if tDef is TypeDef {
            tDefStr += emitTypeDef(tDef);
            tDefStr += emitLBreaks(2);
	}
    }
    return tDefStr;
}

function emitTypeDef(TypeDef tDef) returns string {
    // Adding the type to global type map
    bTypes[tDef.name.value] = tDef.typeValue;

    string tDefStr = "";
    tDefStr += emitFlags(tDef.flags);
    if tDefStr != "" {
        tDefStr += emitSpaces(1);
    }
    tDefStr += emitName(tDef.name);
    tDefStr += emitSpaces(1);
    tDefStr += emitType(tDef.typeValue);
    Function?[]? attachedFuncs = tDef.attachedFuncs;
    if attachedFuncs is Function?[] {
        tDefStr += emitLBreaks(1);
        tDefStr += "{";
        tDefStr += emitLBreaks(1);
        foreach Function? func in attachedFuncs {
            if func is Function {
                tDefStr += emitFunction(func, 1);
                tDefStr += emitLBreaks(1);
            }
        }
        tDefStr += "}";
    }
    tDefStr += ";";
    return tDefStr;
}

////////////////////////////////////// Global var emitting ///////////////////
function emitGlobalVars(GlobalVariableDcl?[] globleVars) returns string {
    string globalVarStr = "";
    foreach GlobalVariableDcl? globalVar in globleVars {
    	if globalVar is GlobalVariableDcl {
            globalVarStr += emitGlobalVar(globalVar);
            globalVarStr += emitLBreaks(1);
	}
    }
    return globalVarStr;
}

function emitGlobalVar(GlobalVariableDcl globalVar) returns string {
    string globalVarStr = "";
    globalVarStr += emitFlags(globalVar.flags);
    globalVarStr += emitName(globalVar.name);
    globalVarStr += emitTypeRef(globalVar.typeValue);
    return globalVarStr;
}

function emitFunctions(Function?[] funcs, int tabs = 0) returns string {
    string funcString = "";
    foreach Function? func in funcs {
    	if func is Function {
            funcString += emitFunction(func, tabs);
            funcString += emitLBreaks(2);
	}
    }
    return funcString;
}	

function emitFunction(Function func, int tabs) returns string {
    string funcString = "";
    funcString += emitTabs(tabs);
    funcString += emitFlags(func.flags);
    if funcString != "" {
        funcString += emitSpaces(1);
    }
    funcString += emitName(func.name);
    funcString += emitSpaces(1);
    funcString += emitType(func.typeValue);
    funcString += emitSpaces(1);
    funcString += "{";
    funcString += emitLBreaks(1);
    funcString += emitLocalVars(func.localVars, tabs + 1);
    funcString += emitLBreaks(1);
    funcString += emitBasicBlocks(func.basicBlocks, tabs + 1);
    funcString += emitLBreaks(1);
    funcString += emitTabs(tabs);
    funcString += "}";
    return funcString;
}	

function emitLocalVars(VariableDcl?[] localVars, int tabs) returns string {
    string varStr = "";
    foreach VariableDcl? lVar in localVars {
    	if lVar is VariableDcl {
            varStr += emitLocalVar(lVar, tabs);
            varStr += emitLBreaks(1);
	}
    }
    return varStr;
}

function emitLocalVar(VariableDcl lVar, int tabs) returns string {
    string lVarStr = "";
    lVarStr += emitTabs(tabs);
    lVarStr += emitName(lVar.name);
    lVarStr += emitSpaces(1);
    lVarStr += emitTypeRef(lVar.typeValue);
    lVarStr += ";";
    return lVarStr;
}

function emitBasicBlocks(BasicBlock?[] basicBlocks, int tabs) returns string {
    string bbStr = "";
    foreach BasicBlock? bb in basicBlocks {
    	if bb is BasicBlock {
            bbStr += emitBasicBlock(bb, tabs);
            bbStr += emitLBreaks(1);
	}
    }
    return bbStr;
}

function emitBasicBlock(BasicBlock basicBlock, int tabs) returns string {
    string bbStr = "";
    bbStr += emitTabs(tabs);
    bbStr += emitName(basicBlock.id);
    bbStr += emitSpaces(1);
    bbStr += "{";
    bbStr += emitLBreaks(1);
    bbStr += emitInstructions(basicBlock.instructions, tabs + 1);
    bbStr += emitTerminator(basicBlock.terminator, tabs + 1);
    bbStr += emitLBreaks(1);
    bbStr += emitTabs(tabs);
    bbStr += "}";
    return bbStr;
}


