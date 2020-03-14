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

import org.ballerinalang.model.elements.Flag;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JIMethodCall;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStructure;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVABLE_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupFunctionName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunction;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunctionParam;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextVarId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator.toNameString;
import static org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Branch;
import static org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.GOTO;

/**
 * BIR desugar phase related methods at JVM code generation.
 *
 * @since 1.2.0
 */
public class JvmDesugarPhase {

    public static void addDefaultableBooleanVarsToSignature(@Nilable BIRFunction func) {

        BIRFunction currentFunc = getFunction(func);
        currentFunc.type = new BInvokableType(currentFunc.type.paramTypes, currentFunc.type.restType,
                currentFunc.type.retType, currentFunc.type.tsymbol);
        BInvokableType type = currentFunc.type;
        currentFunc.type.paramTypes = updateParamTypesWithDefaultableBooleanVar(currentFunc.type.paramTypes,
                type != null ? type.restType : type);
        int index = 0;
        @Nilable List<BIRVariableDcl> updatedVars = new ArrayList<>();
        @Nilable List<BIRVariableDcl> localVars = currentFunc.localVars;
        int nameIndex = 0;

        for (BIRVariableDcl localVar : localVars) {
            updatedVars.add(index, localVar);
            index += 1;
            if (localVar instanceof BIRFunctionParameter) {
                // An additional boolean arg is gen for each function parameter.
                String argName = "%syn" + nameIndex;
                nameIndex += 1;
                BIRFunctionParameter booleanVar = new BIRFunctionParameter(null, symbolTable.booleanType,
                        new Name(argName), VarScope.FUNCTION, VarKind.ARG, "", false);
                updatedVars.add(index, booleanVar);
                index += 1;
            }
        }
        currentFunc.localVars = updatedVars;
    }

    public static void enrichWithDefaultableParamInits(BIRFunction currentFunc) {

        int k = 1;
        @Nilable List<BIRFunctionParameter> functionParams = new ArrayList<>();
        @Nilable List<BIRVariableDcl> localVars = currentFunc.localVars;
        while (k < localVars.size()) {
            BIRVariableDcl localVar = getVariableDcl(localVars.get(k));
            if (localVar instanceof BIRFunctionParameter) {
                functionParams.add((BIRFunctionParameter) localVar);
            }
            k += 1;
        }

        nextId = -1;
        nextVarId = -1;

        @Nilable List<BIRBasicBlock> basicBlocks = new ArrayList<>();

        BIRBasicBlock nextBB = insertAndGetNextBasicBlock(basicBlocks, "desugaredBB");

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
                @Nilable List<BIRBasicBlock> bbArray = currentFunc.parameters.get(funcParam);
                BIRBasicBlock trueBB = getBasicBlock(bbArray.get(0));
                for (BIRBasicBlock defaultBB : bbArray) {
                    basicBlocks.add(getBasicBlock(defaultBB));
                }
                BIRBasicBlock falseBB = insertAndGetNextBasicBlock(basicBlocks, "desugaredBB");
                nextBB.terminator = new Branch(pos, boolRef, trueBB, falseBB);

                BIRBasicBlock lastBB = getBasicBlock(bbArray.get(bbArray.size() - 1));
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

        BIRBasicBlock firstBB = getBasicBlock(currentFunc.basicBlocks.get(0));

        nextBB.terminator = new GOTO(pos, firstBB);
        basicBlocks.addAll(currentFunc.basicBlocks);

        currentFunc.basicBlocks = basicBlocks;
    }

    public static BIRBasicBlock insertAndGetNextBasicBlock(@Nilable List<BIRBasicBlock> basicBlocks,
                                                           String prefix /* = "desugaredBB" */) {

        BIRBasicBlock nextbb = new BIRBasicBlock(getNextDesugarBBId(prefix));
        basicBlocks.add(nextbb);
        return nextbb;
    }

    public static BIRBasicBlock insertAndGetNextBasicBlock(List<BIRBasicBlock> basicBlocks, int insertIndex,
                                                           String prefix) {
        BIRBasicBlock newBasicBlock = new BIRBasicBlock(getNextDesugarBBId(prefix));
        basicBlocks.add(insertIndex, newBasicBlock);
        return newBasicBlock;
    }

    public static Name getNextDesugarBBId(String prefix) {

        nextId += 1;
        return new Name(prefix + nextId);
    }

    private static @Nilable
    List<BType> updateParamTypesWithDefaultableBooleanVar(@Nilable List<BType> funcParams, @Nilable BType restType) {

        @Nilable List<BType> paramTypes = new ArrayList<>();

        int counter = 0;
        int index = 0;
        // Update the param types to add boolean variables to indicate if the previous variable contains a user
        // given value
        int size = funcParams == null ? 0 : funcParams.size();
        while (counter < size) {
            paramTypes.add(index, funcParams.get(counter));
            paramTypes.add(index + 1, symbolTable.booleanType);
            index += 2;
            counter += 1;
        }
        if (!(restType == null)) {
            paramTypes.add(index, restType);
            paramTypes.add(index + 1, symbolTable.booleanType);
        }
        return paramTypes;
    }

    static void rewriteRecordInits(@Nilable List<BIRTypeDefinition> typeDefs) {

        for (@Nilable BIRTypeDefinition typeDef : typeDefs) {
            @Nilable BType recordType = typeDef.type;
            if (recordType.tag != TypeTags.RECORD) {
                continue;
            }
            @Nilable List<BIRFunction> attachFuncs = typeDef.attachedFuncs;
            for (BIRFunction func : attachFuncs) {
                rewriteRecordInitFunction(func, (BRecordType) recordType);
            }
        }
    }

    private static void rewriteRecordInitFunction(BIRFunction func, BRecordType recordType) {

        BIRVariableDcl receiver = func.receiver;

        // Rename the function name by appending the record name to it.
        // This done to avoid frame class name overlappings.
        func.name = new Name(cleanupFunctionName(toNameString(recordType) + func.name.value));

        // change the kind of receiver to 'ARG'
        receiver.kind = VarKind.ARG;

        // Update the name of the reciever. Then any instruction that was refering to the receiver will
        // now refer to the injected parameter.
        String paramName = "$_" + receiver.name.value;
        receiver.name = new Name(paramName);

        // Inject an additional parameter to accept the self-record value into the init function
        BIRFunctionParameter selfParam = new BIRFunctionParameter(null, receiver.type, receiver.name,
                receiver.scope, VarKind.ARG, paramName, false);

        func.type = new BInvokableType(Collections.singletonList(receiver.type), func.type.restType, func.type.retType
                , null);
        func.type.paramTypes = Collections.singletonList(receiver.type);

        @Nilable List<BIRVariableDcl> localVars = func.localVars;
        @Nilable List<BIRVariableDcl> updatedLocalVars = new ArrayList<>();
        updatedLocalVars.add(localVars.get(0));
        updatedLocalVars.add(selfParam);
        int index = 1;
        while (index < localVars.size()) {
            updatedLocalVars.add(localVars.get(index));
            index += 1;
        }
        func.localVars = updatedLocalVars;
    }

    public static void rewriteObservableFunctionInvocations(BIRPackage pkg) {
        for (BIRFunction func : pkg.functions) {
            rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
        }
        for (BIRTypeDefinition typeDef : pkg.typeDefs) {
            for (BIRFunction attachedFunc : typeDef.attachedFuncs) {
                rewriteObservableFunctionInvocations(attachedFunc.basicBlocks, attachedFunc.localVars, pkg);
            }
        }
    }

    public static void rewriteObservableFunctionInvocations(List<BIRBasicBlock> basicBlocks,
                                                            List<BIRVariableDcl> scopeVarList, BIRPackage pkg) {
        int i = 0;
        while (i < basicBlocks.size()) {
            BIRBasicBlock currentBB = basicBlocks.get(i);
            BIRTerminator currentTerminator = currentBB.terminator;
            if (currentTerminator.kind == InstructionKind.CALL) {
                BIRTerminator.Call callIns = (BIRTerminator.Call) currentTerminator;
                boolean isRemote = callIns.calleeFlags.contains(Flag.REMOTE);
                boolean isObservableAnnotationPresent = false;
                for (BIRNode.BIRAnnotationAttachment annot : callIns.calleeAnnotAttachments) {
                    if (OBSERVABLE_ANNOTATION.equals(annot.packageID.orgName.value + "/"
                            + annot.packageID.name.value + "/" + annot.annotTagRef.value)) {
                        isObservableAnnotationPresent = true;
                        break;
                    }
                }
                if (isRemote || isObservableAnnotationPresent) {
                    String action;
                    if (callIns.name.value.contains(".")) {
                        String[] split = callIns.name.value.split("\\.");
                        action = split[1];
                    } else {
                        action = callIns.name.value;
                    }
                    String connectorName;
                    if (callIns.isVirtual) {
                        BIRVariableDcl selfArg = getVariableDcl(callIns.args.get(0).variableDcl);
                        connectorName = getPackageName(selfArg.type.tsymbol.pkgID.orgName,
                                selfArg.type.tsymbol.pkgID.name) + "/" + selfArg.type.tsymbol.name.value;
                    } else {
                        connectorName = "";
                    }
                    JIMethodCall observeStartCallTerminator;
                    {
                        BIROperand connectorNameOperand = generateConstantOperand(
                                String.format("%s_connector", currentBB.id.value), connectorName, scopeVarList,
                                currentBB);
                        BIROperand actionNameOperand = generateConstantOperand(
                                String.format("%s_action", currentBB.id.value), action, scopeVarList,
                                currentBB);
                        Map<String, String> tags = new HashMap<>();
                        tags.put("source.invocation_fqn", String.format("%s:%s:%s:%s:%d:%d", pkg.org.value,
                                pkg.name.value, pkg.version.value, callIns.pos.src.cUnitName, callIns.pos.sLine,
                                callIns.pos.sCol));
                        if (isRemote) {
                            tags.put("source.remote", "true");
                        }
                        BIROperand tagsMapOperand = generateMapOperand(
                                String.format("%s_tags", currentBB.id.value), tags, scopeVarList,
                                currentBB);

                        observeStartCallTerminator = new JIMethodCall(
                                new DiagnosticPos(null, -1, -1, -1, -1));
                        observeStartCallTerminator.invocationType = INVOKESTATIC;
                        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
                        observeStartCallTerminator.jMethodVMSig = String.format("(L%s;L%s;L%s;)V", STRING_VALUE,
                                STRING_VALUE, MAP_VALUE);
                        observeStartCallTerminator.name = "startCallableObservation";
                        observeStartCallTerminator.args = Arrays.asList(connectorNameOperand, actionNameOperand,
                                tagsMapOperand);
                    }

                    JIMethodCall observeEndCallTerminator = new JIMethodCall(
                            new DiagnosticPos(null, -1, -1, -1, -1));
                    observeEndCallTerminator.invocationType = INVOKESTATIC;
                    observeEndCallTerminator.jClassName = OBSERVE_UTILS;
                    observeEndCallTerminator.jMethodVMSig = "()V";
                    observeEndCallTerminator.name = "stopObservation";
                    observeEndCallTerminator.args = Collections.emptyList();

                    BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, "desugaredBB");
                    BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks, i + 2, "desugaredBB");
                    newCurrentBB.terminator = currentBB.terminator;
                    currentBB.terminator = observeStartCallTerminator;
                    observeEndBB.terminator = observeEndCallTerminator;

                    observeEndBB.terminator.thenBB = currentBB.terminator.thenBB;
                    newCurrentBB.terminator.thenBB = observeEndBB;
                    currentBB.terminator.thenBB = newCurrentBB;
                    i += 2;
                }
            }
            i++;
        }
    }

    public static BIROperand generateConstantOperand(String uniqueId, String constantValue,
                                                     List<BIRVariableDcl> scopeVarList, BIRBasicBlock basicBlock) {
        BIRVariableDcl variableDcl = new BIRVariableDcl(symbolTable.stringType,
                new Name(String.format("$_%s_const_$", uniqueId)), VarScope.FUNCTION, VarKind.LOCAL);
        scopeVarList.add(variableDcl);
        BIROperand operand = new BIROperand(variableDcl);
        BIRNonTerminator instruction = new ConstantLoad(null, constantValue, symbolTable.stringType, operand);
        basicBlock.instructions.add(instruction);
        return operand;
    }

    public static BIROperand generateMapOperand(String uniqueId, Map<String, String> map,
                                                List<BIRVariableDcl> scopeVarList, BIRBasicBlock basicBlock) {
        BIRVariableDcl variableDcl = new BIRVariableDcl(symbolTable.mapType,
                new Name(String.format("$_%s_map_$", uniqueId)), VarScope.FUNCTION, VarKind.LOCAL);
        scopeVarList.add(variableDcl);
        BIROperand tagsMapOperand = new BIROperand(variableDcl);

        NewStructure bMapNewInstruction = new NewStructure(null,
                new BMapType(TypeTags.MAP, symbolTable.stringType, null), tagsMapOperand);
        basicBlock.instructions.add(bMapNewInstruction);

        int entryIndex = 0;
        for (Map.Entry<String, String> tagEntry: map.entrySet()) {
            BIROperand keyOperand = generateConstantOperand(String.format("%s_map_%d_key", uniqueId, entryIndex),
                    tagEntry.getKey(), scopeVarList, basicBlock);
            BIROperand valueOperand = generateConstantOperand(String.format("%s_map_%d_value", uniqueId,
                    entryIndex), tagEntry.getValue(), scopeVarList, basicBlock);
            BIRNonTerminator.FieldAccess fieldAccessIns = new BIRNonTerminator.FieldAccess(null,
                    InstructionKind.MAP_STORE, tagsMapOperand, keyOperand, valueOperand);
            basicBlock.instructions.add(fieldAccessIns);
            entryIndex++;
        }
        return tagsMapOperand;
    }
}
