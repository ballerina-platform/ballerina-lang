/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.emit;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitBasicBlockRef;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitFlags;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitLBreaks;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitName;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitSpaces;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitTabs;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitVarRef;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.isEmpty;
import static org.wso2.ballerinalang.compiler.bir.emit.InstructionEmitter.emitInstructions;
import static org.wso2.ballerinalang.compiler.bir.emit.InstructionEmitter.emitTerminator;
import static org.wso2.ballerinalang.compiler.bir.emit.TypeEmitter.emitType;
import static org.wso2.ballerinalang.compiler.bir.emit.TypeEmitter.emitTypeRef;

/**
 * BIR module emitter which prints the BIR model to console.
 *
 * @since 1.2.0
 */
public class BIREmitter {

    private static final CompilerContext.Key<BIREmitter> BIR_EMITTER = new CompilerContext.Key<>();
    private static final PrintStream console = System.out;

    public static BIREmitter getInstance(CompilerContext context) {

        BIREmitter birEmitter = context.get(BIR_EMITTER);
        if (birEmitter == null) {
            birEmitter = new BIREmitter(context);
        }

        return birEmitter;
    }

    private BIREmitter(CompilerContext context) {

        context.put(BIR_EMITTER, this);
    }

    public void emit(BIRNode.BIRPackage birPackage) {

        console.println(emitModule(birPackage));
    }

    private String emitModule(BIRNode.BIRPackage mod) {

        String modStr = "================ Emitting Module ================";
        modStr += emitLBreaks(1);
        modStr += "module";
        modStr += emitSpaces(1);
        modStr += emitName(mod.org) + "/" + emitName(mod.name);
        modStr += emitSpaces(1);
        modStr += "v";
        modStr += emitSpaces(1);
        modStr += emitName(mod.version) + ";";
        modStr += emitLBreaks(2);
        modStr += emitImports(mod.importModules);
        modStr += emitLBreaks(2);
        modStr += emitTypeDefs(mod.typeDefs);
        modStr += emitLBreaks(2);
        modStr += emitGlobalVars(mod.globalVars);
        modStr += emitLBreaks(2);
        modStr += emitFunctions(mod.functions, 0);

        modStr += emitLBreaks(1);
        modStr += "================ Emitting Module ================";
        return modStr;
    }

    private String emitImports(List<BIRNode.BIRImportModule> impMods) {

        StringBuilder impStr = new StringBuilder();
        for (BIRNode.BIRImportModule mod : impMods) {
            impStr.append(emitImport(mod));
            impStr.append(emitLBreaks(1));
        }
        return impStr.toString();
    }

    private String emitImport(BIRNode.BIRImportModule impMod) {

        String impStr = "import ";
        impStr += emitName(impMod.org) + "/";
        impStr += emitName(impMod.name);
        if (!isEmpty(impMod.version)) {
            impStr += emitSpaces(1);
            impStr += "v";
            impStr += emitSpaces(1);
            impStr += emitName(impMod.version);
        }
        impStr += ";";
        return impStr;
    }

    private String emitTypeDefs(List<BIRNode.BIRTypeDefinition> typeDefs) {

        StringBuilder tDefStr = new StringBuilder();
        for (BIRNode.BIRTypeDefinition tDef : typeDefs) {
            tDefStr.append(emitTypeDef(tDef));
            tDefStr.append(emitLBreaks(2));
        }
        return tDefStr.toString();
    }

    private String emitTypeDef(BIRNode.BIRTypeDefinition tDef) {
        // Adding the type to global type map
        TypeEmitter.B_TYPES.put(tDef.name.value, tDef.type);

        StringBuilder tDefStr = new StringBuilder();
        tDefStr.append(emitFlags(tDef.flags));
        if (!tDefStr.toString().equals("")) {
            tDefStr.append(emitSpaces(1));
        }
        tDefStr.append(emitName(tDef.name));
        tDefStr.append(emitSpaces(1));
        tDefStr.append(emitType(tDef.type, 0));
        List<BIRNode.BIRFunction> attachedFuncs = tDef.attachedFuncs;
        if (attachedFuncs != null) {
            tDefStr.append(emitLBreaks(1));
            tDefStr.append("{");
            tDefStr.append(emitLBreaks(1));
            for (BIRNode.BIRFunction func : attachedFuncs) {
                tDefStr.append(emitFunction(func, 1));
                tDefStr.append(emitLBreaks(1));
            }
            tDefStr.append("}");
        }
        tDefStr.append(";");
        return tDefStr.toString();
    }

    ////////////////////////////////////// Global var emitting ///////////////////
    private String emitGlobalVars(List<BIRNode.BIRGlobalVariableDcl> globleVars) {

        StringBuilder globalVarStr = new StringBuilder();
        for (BIRNode.BIRGlobalVariableDcl globalVar : globleVars) {
            globalVarStr.append(emitGlobalVar(globalVar));
            globalVarStr.append(emitLBreaks(1));
        }
        return globalVarStr.toString();
    }

    private String emitGlobalVar(BIRNode.BIRGlobalVariableDcl globalVar) {

        String globalVarStr = "";
        globalVarStr += emitFlags(globalVar.flags);
        if (!globalVarStr.equals("")) {
            globalVarStr += emitSpaces(1);
        }
        globalVarStr += emitName(globalVar.name);
        globalVarStr += emitSpaces(1);
        globalVarStr += emitTypeRef(globalVar.type, 0);
        globalVarStr += ";";
        return globalVarStr;
    }

    private String emitFunctions(List<BIRNode.BIRFunction> funcs, int tabs) {
        StringBuilder funcString = new StringBuilder();
        for (BIRNode.BIRFunction func : funcs) {
            funcString.append(emitFunction(func, tabs));
            funcString.append(emitLBreaks(2));
        }
        return funcString.toString();
    }

    public String emitFunction(BIRNode.BIRFunction func, int tabs) {
        String funcString = "";
        funcString += emitTabs(tabs);
        funcString += emitFlags(func.flags);
        if (!funcString.equals("")) {
            funcString += emitSpaces(1);
        }
        funcString += emitName(func.name);
        funcString += emitSpaces(1);
        funcString += emitType(func.type, 0);
        funcString += emitSpaces(1);
        funcString += "{";
        funcString += emitLBreaks(1);
        funcString += emitLocalVar(func.returnVariable, tabs + 1);
        funcString += emitLBreaks(1);
        funcString += emitArguments(func.parameters, tabs + 1);
        funcString += emitLocalVars(func.localVars, tabs + 1);
        funcString += emitLBreaks(1);
        funcString += emitBasicBlocks(func.basicBlocks, tabs + 1);
        funcString += emitLBreaks(1);
        funcString += emitErrorEntries(func.errorTable, tabs + 1);
        funcString += emitLBreaks(1);
        funcString += emitTabs(tabs);
        funcString += "}";
        return funcString;
    }

    private String emitLocalVars(List<BIRNode.BIRVariableDcl> localVars, int tabs) {

        StringBuilder varStr = new StringBuilder();
        for (BIRNode.BIRVariableDcl lVar : localVars) {
            varStr.append(emitLocalVar(lVar, tabs));
            varStr.append(emitLBreaks(1));
        }
        return varStr.toString();
    }

    private String emitLocalVar(BIRNode.BIRVariableDcl lVar, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitName(lVar.name);
        str += "(";
        str += lVar.kind.toString();
        str += ")";
        str += emitSpaces(1);
        str += emitTypeRef(lVar.type, 0);
        str += ";";
        return str;
    }

    private String emitArguments(Map<BIRNode.BIRFunctionParameter, List<BIRNode.BIRBasicBlock>> localVars, int tabs) {

        StringBuilder varStr = new StringBuilder();
        for (BIRNode.BIRFunctionParameter parameter : localVars.keySet()) {
            varStr.append(emitLocalVar(parameter, tabs));
            varStr.append(emitLBreaks(1));
        }
        return varStr.toString();
    }

    private String emitBasicBlocks(List<BIRNode.BIRBasicBlock> basicBlocks, int tabs) {

        StringBuilder bbStr = new StringBuilder();
        for (BIRNode.BIRBasicBlock bb : basicBlocks) {
            bbStr.append(emitBasicBlock(bb, tabs));
            bbStr.append(emitLBreaks(1));
        }
        return bbStr.toString();
    }

    private String emitBasicBlock(BIRNode.BIRBasicBlock basicBlock, int tabs) {

        String bbStr = "";
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

//////////////////// emit error table entries ////////////
// TODO improve this to be able to plug custom error emitters as there may be
// TODO cnt - custom error entries @platform specific code gen sides
//
//   -------------------------------
//   | trapBB       | errorOp      |
//   -------------------------------
//   | bb1          | op1          |

    private String emitErrorEntries(List<BIRNode.BIRErrorEntry> errorEntries, int tabs) {

        StringBuilder str = new StringBuilder();
        if (errorEntries.size() == 0) {
            return str.toString();
        }
        str.append(emitTabs(tabs));
        str.append("---------------------------------------------");
        str.append(emitLBreaks(1));
        str.append(emitTabs(tabs));
        str.append("|");
        str.append(emitSpaces(1));
        str.append("trapBB");
        str.append(emitSpaces(7));
        str.append("|");
        str.append(emitSpaces(1));
        str.append("endBB");
        str.append(emitSpaces(8));
        str.append("|");
        str.append(emitSpaces(1));
        str.append("errorOp");
        str.append(emitSpaces(6));
        str.append("|");
        str.append(emitLBreaks(1));
        str.append(emitTabs(tabs));
        str.append("---------------------------------------------");
        str.append(emitLBreaks(1));
        for (BIRNode.BIRErrorEntry err : errorEntries) {
            str.append(emitErrorEntry(err, tabs));
            str.append(emitLBreaks(1));
        }
        str.append(emitTabs(tabs));
        str.append("---------------------------------------------");
        return str.toString();
    }

    private String emitErrorEntry(BIRNode.BIRErrorEntry err, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += "|";
        str += emitSpaces(1);
        String bbRef = emitBasicBlockRef(err.trapBB);
        int bbSpaces = calculateSpaces(bbRef);
        str += bbRef;
        str += emitSpaces(bbSpaces);
        str += "|";
        str += emitSpaces(1);
        String endBBRef = emitBasicBlockRef(err.endBB);
        int endBBSpaces = calculateSpaces(endBBRef);
        str += endBBRef;
        str += emitSpaces(endBBSpaces);
        str += "|";
        str += emitSpaces(1);
        String varRef = emitVarRef(err.errorOp);
        int varRefSpaces = calculateSpaces(varRef);
        str += varRef;
        str += emitSpaces(varRefSpaces);
        str += "|";
        return str;
    }

    private int calculateSpaces(String str) {

        return 13 - str.length();
    }
}
