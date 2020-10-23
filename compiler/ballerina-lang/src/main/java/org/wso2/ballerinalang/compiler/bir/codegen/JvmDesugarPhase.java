/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Branch;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.GOTO;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.IdentifierUtils.encodeIdentifier;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DESUGARED_BB_ID_NAME;

/**
 * BIR desugar phase related methods at JVM code generation.
 *
 * @since 1.2.0
 */
public class JvmDesugarPhase {

    public static void addDefaultableBooleanVarsToSignature(BIRFunction func, BType booleanType) {

        func.type = new BInvokableType(func.type.paramTypes, func.type.restType,
                                       func.type.retType, func.type.tsymbol);
        BInvokableType type = func.type;
        func.type.paramTypes = updateParamTypesWithDefaultableBooleanVar(func.type.paramTypes,
                                                                         type.restType, booleanType);
        int index = 0;
        List<BIRVariableDcl> updatedVars = new ArrayList<>();
        List<BIRVariableDcl> localVars = func.localVars;
        int nameIndex = 0;

        for (BIRVariableDcl localVar : localVars) {
            updatedVars.add(index, localVar);
            index += 1;

            if (!(localVar instanceof BIRFunctionParameter)) {
                continue;
            }

            // An additional boolean arg is gen for each function parameter.
            String argName = "%syn" + nameIndex;
            nameIndex += 1;
            BIRFunctionParameter booleanVar = new BIRFunctionParameter(null, booleanType,
                    new Name(argName), VarScope.FUNCTION, VarKind.ARG, "", false);
            updatedVars.add(index, booleanVar);
            index += 1;
        }
        func.localVars = updatedVars;
    }

    public static void enrichWithDefaultableParamInits(BIRFunction currentFunc, JvmMethodGen jvmMethodGen) {

        int k = 1;
        List<BIRFunctionParameter> functionParams = new ArrayList<>();
        List<BIRVariableDcl> localVars = currentFunc.localVars;
        while (k < localVars.size()) {
            BIRVariableDcl localVar = localVars.get(k);
            if (localVar instanceof BIRFunctionParameter) {
                functionParams.add((BIRFunctionParameter) localVar);
            }
            k += 1;
        }

        jvmMethodGen.resetIds();

        List<BIRBasicBlock> basicBlocks = new ArrayList<>();

        BIRBasicBlock nextBB = insertAndGetNextBasicBlock(basicBlocks, DESUGARED_BB_ID_NAME, jvmMethodGen);

        int paramCounter = 0;
        DiagnosticPos pos = currentFunc.pos;
        while (paramCounter < functionParams.size()) {
            BIRFunctionParameter funcParam = functionParams.get(paramCounter);
            if (funcParam != null && funcParam.hasDefaultExpr) {
                int boolParam = paramCounter + 1;
                BIRFunctionParameter funcBooleanParam = getFunctionParam(functionParams.get(boolParam));
                BIROperand boolRef = new BIROperand(funcBooleanParam);
                UnaryOP notOp = new UnaryOP(pos, InstructionKind.NOT, boolRef, boolRef);
                nextBB.instructions.add(notOp);
                List<BIRBasicBlock> bbArray = currentFunc.parameters.get(funcParam);
                BIRBasicBlock trueBB = bbArray.get(0);
                basicBlocks.addAll(bbArray);
                BIRBasicBlock falseBB = insertAndGetNextBasicBlock(basicBlocks, DESUGARED_BB_ID_NAME, jvmMethodGen);
                nextBB.terminator = new Branch(pos, boolRef, trueBB, falseBB);

                BIRBasicBlock lastBB = bbArray.get(bbArray.size() - 1);
                lastBB.terminator = new GOTO(pos, falseBB);

                nextBB = falseBB;
            }
            paramCounter += 2;
        }

        if (basicBlocks.size() == 1) {
            // this means only one block added, if there are default vars, there must be more than one block
            return;
        }
        if (currentFunc.basicBlocks.size() == 0) {
            currentFunc.basicBlocks = basicBlocks;
            return;
        }

        BIRBasicBlock firstBB = currentFunc.basicBlocks.get(0);

        nextBB.terminator = new GOTO(pos, firstBB);
        basicBlocks.addAll(currentFunc.basicBlocks);

        currentFunc.basicBlocks = basicBlocks;
    }

    public static BIRBasicBlock insertAndGetNextBasicBlock(List<BIRBasicBlock> basicBlocks,
                                                           String prefix, JvmMethodGen jvmMethodGen) {

        BIRBasicBlock nextbb = new BIRBasicBlock(getNextDesugarBBId(prefix, jvmMethodGen));
        basicBlocks.add(nextbb);
        return nextbb;
    }

    public static Name getNextDesugarBBId(String prefix, JvmMethodGen jvmMethodGen) {

        int nextId = jvmMethodGen.incrementAndGetNextId();
        return new Name(prefix + nextId);
    }

    private static List<BType> updateParamTypesWithDefaultableBooleanVar(List<BType> funcParams, BType restType,
                                                                         BType booleanType) {

        List<BType> paramTypes = new ArrayList<>();

        int counter = 0;
        int index = 0;
        // Update the param types to add boolean variables to indicate if the previous variable contains a user
        // given value
        int size = funcParams == null ? 0 : funcParams.size();
        while (counter < size) {
            paramTypes.add(index, funcParams.get(counter));
            paramTypes.add(index + 1, booleanType);
            index += 2;
            counter += 1;
        }
        if (!(restType == null)) {
            paramTypes.add(index, restType);
            paramTypes.add(index + 1, booleanType);
        }
        return paramTypes;
    }

    static void rewriteRecordInits(List<BIRTypeDefinition> typeDefs) {

        for (BIRTypeDefinition typeDef : typeDefs) {
            BType recordType = typeDef.type;
            if (recordType.tag != TypeTags.RECORD) {
                continue;
            }
            List<BIRFunction> attachFuncs = typeDef.attachedFuncs;
            for (BIRFunction func : attachFuncs) {
                rewriteRecordInitFunction(func, (BRecordType) recordType);
            }
        }
    }

    private static void rewriteRecordInitFunction(BIRFunction func, BRecordType recordType) {

        BIRVariableDcl receiver = func.receiver;

        // Rename the function name by appending the record name to it.
        // This done to avoid frame class name overlapping.
        func.name = new Name(JvmCodeGenUtil.cleanupFunctionName(toNameString(recordType) + func.name.value));

        // change the kind of receiver to 'ARG'
        receiver.kind = VarKind.ARG;

        // Update the name of the reciever. Then any instruction that was refering to the receiver will
        // now refer to the injected parameter.
        String paramName = "$_" + receiver.name.value;
        receiver.name = new Name(paramName);

        // Inject an additional parameter to accept the self-record value into the init function
        BIRFunctionParameter selfParam = new BIRFunctionParameter(null, receiver.type, receiver.name,
                                                                  receiver.scope, VarKind.ARG, paramName, false);

        List<BType> updatedParamTypes = Lists.of(receiver.type);
        updatedParamTypes.addAll(func.type.paramTypes);
        func.type = new BInvokableType(updatedParamTypes, func.type.restType, func.type.retType, null);

        List<BIRVariableDcl> localVars = func.localVars;
        List<BIRVariableDcl> updatedLocalVars = new ArrayList<>();
        updatedLocalVars.add(localVars.get(0));
        updatedLocalVars.add(selfParam);
        int index = 1;
        while (index < localVars.size()) {
            updatedLocalVars.add(localVars.get(index));
            index += 1;
        }
        func.localVars = updatedLocalVars;
    }

    private static BIRFunctionParameter getFunctionParam(BIRFunctionParameter localVar) {
        if (localVar == null) {
            throw new BLangCompilerException("Invalid function parameter");
        }

        return localVar;
    }

    private JvmDesugarPhase() {
    }

    static void encodeModuleIdentifiers(BIRNode.BIRPackage module, Names names) {
        encodeGlobalVariableIdentifiers(module.globalVars, names);
        encodeFunctionIdentifiers(module.functions, names);
        encodeTypeDefIdentifiers(module.typeDefs, names);
    }

    private static void encodeTypeDefIdentifiers(List<BIRTypeDefinition> typeDefs, Names names) {
        for (BIRTypeDefinition typeDefinition : typeDefs) {
            typeDefinition.type.tsymbol.name =
                    names.fromString(encodeIdentifier(typeDefinition.type.tsymbol.name.value));
            typeDefinition.name = names.fromString(encodeIdentifier(typeDefinition.name.value));

            encodeFunctionIdentifiers(typeDefinition.attachedFuncs, names);
            BType bType = typeDefinition.type;
            if (bType.tag == TypeTags.OBJECT) {
                BObjectType objectType = (BObjectType) bType;
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bType.tsymbol;
                if (objectTypeSymbol.attachedFuncs != null) {
                    encodeAttachedFunctionIdentifiers(objectTypeSymbol.attachedFuncs, names);
                }
                for (BField field : objectType.fields.values()) {
                    field.name = names.fromString(encodeIdentifier(field.name.value));
                }
            }
            if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                for (BField field : recordType.fields.values()) {
                    field.name = names.fromString(encodeIdentifier(field.name.value));
                }
            }
        }
    }

    private static void encodeFunctionIdentifiers(List<BIRFunction> functions, Names names) {
        for (BIRFunction function : functions) {
            function.name = names.fromString(encodeIdentifier(function.name.value));
            for (BIRNode.BIRVariableDcl localVar : function.localVars) {
                if (localVar.metaVarName == null) {
                    continue;
                }
                localVar.metaVarName = encodeIdentifier(localVar.metaVarName);
            }
            for (BIRNode.BIRParameter parameter : function.requiredParams) {
                if (parameter.name == null) {
                    continue;
                }
                parameter.name = names.fromString(encodeIdentifier(parameter.name.value));
            }
            encodeWorkerName(function, names);
        }
    }

    private static void encodeWorkerName(BIRFunction function, Names names) {
        if (function.workerName != null) {
            function.workerName = names.fromString(encodeIdentifier(function.workerName.value));
        }
        for (BIRNode.ChannelDetails channel : function.workerChannels) {
            channel.name = encodeIdentifier(channel.name);
        }
    }

    private static void encodeAttachedFunctionIdentifiers(List<BAttachedFunction> functions, Names names) {
        for (BAttachedFunction function : functions) {
            function.funcName = names.fromString(encodeIdentifier(function.funcName.value));
            function.symbol.name = names.fromString(encodeIdentifier(function.symbol.name.value));
            if (function.symbol.receiverSymbol != null) {
                function.symbol.receiverSymbol.name =
                        names.fromString(encodeIdentifier(function.symbol.receiverSymbol.name.value));
            }
        }
    }

    private static void encodeGlobalVariableIdentifiers(List<BIRNode.BIRGlobalVariableDcl> globalVars,
                                                        Names names) {
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVars) {
            if (globalVar == null) {
                continue;
            }
            globalVar.name = names.fromString(encodeIdentifier(globalVar.name.value));
        }
    }
}
