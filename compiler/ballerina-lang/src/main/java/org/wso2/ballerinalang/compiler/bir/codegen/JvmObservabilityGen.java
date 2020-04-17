/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.model.elements.Flag;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JIMethodCall;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVABLE_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REPORT_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_CALLABLE_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_RESOURCE_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STOP_OBSERVATION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.insertAndGetNextBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.I_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.isBString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;

/**
 * BIR desugar to inject observations class.
 *
 * @since 1.2.0
 */
class JvmObservabilityGen {
    private static final String ENTRY_POINT_MAIN_METHOD_NAME = "main";
    private static final String NEW_BB_PREFIX = "obsDesugaredBB";
    private static final String SERVICE_IDENTIFIER = "$$service$";
    private static final String ANONYMOUS_SERVICE_IDENTIFIER = "$anonService$";

    // Observability tags related constants
    private static final String SRC_TAG_KEYS_PREFIX = "source.";
    private static final String ENTRY_POINT_TAG_KEYS_PREFIX = SRC_TAG_KEYS_PREFIX + "entry_point.";
    private static final String IS_MAIN_ENTRY_POINT_TAG_KEY = ENTRY_POINT_TAG_KEYS_PREFIX + "main";
    private static final String IS_RESOURCE_ENTRY_POINT_TAG_KEY = ENTRY_POINT_TAG_KEYS_PREFIX + "resource";
    private static final String IS_REMOTE_TAG_KEY = SRC_TAG_KEYS_PREFIX + "remote";
    private static final String IS_WORKER_TAG_KEY = SRC_TAG_KEYS_PREFIX + "worker";
    private static final String INVOCATION_FQN_TAG_KEY = SRC_TAG_KEYS_PREFIX + "invocation_fqn";
    private static final String TAG_VALUE_TRUE = "true";

    /**
     * Rewrite all observable functions in a package.
     *
     * @param pkg The package to instrument
     */
    public static void rewriteObservableFunctions(BIRPackage pkg) {
        for (BIRFunction func : pkg.functions) {
            if (ENTRY_POINT_MAIN_METHOD_NAME.equals(func.name.value)) {
                Map<String, String> tags = new HashMap<>();
                tags.put(IS_MAIN_ENTRY_POINT_TAG_KEY, TAG_VALUE_TRUE);
                rewriteObservableFunctionBody(func, pkg, false, StringUtils.EMPTY,
                        func.name.value, tags);
            } else if ((func.flags & Flags.WORKER) == Flags.WORKER) {   // Identifying lambdas generated for workers
                Map<String, String> tags = new HashMap<>();
                tags.put(IS_WORKER_TAG_KEY, func.workerName.value);
                rewriteObservableFunctionBody(func, pkg, false, StringUtils.EMPTY,
                        func.workerName.value, tags);
            }
            rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
        }
        for (BIRTypeDefinition typeDef : pkg.typeDefs) {
            boolean isService = typeDef.type instanceof BServiceType;
            for (BIRFunction func : typeDef.attachedFuncs) {
                if (isService && !func.name.value.contains("$")) {
                    Map<String, String> tags = new HashMap<>();
                    tags.put(IS_RESOURCE_ENTRY_POINT_TAG_KEY, TAG_VALUE_TRUE);
                    rewriteObservableFunctionBody(func, pkg, true, cleanUpServiceName(typeDef.name.value),
                            func.name.value, tags);
                }
                rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
            }
        }
    }

    /**
     * Rewrite a function so that the internal body will be observed. This adds the relevant start and stop calls at
     * the beginning and return basic blocks of the function.
     *
     * This is only to be used in service resource functions, workers and main method.
     *
     * @param func The function to instrument
     * @param pkg The package which contains the function
     * @param isResourceObservation True if the function is a service resource
     * @param serviceName The name of the service
     * @param resourceOrAction The name of the resource or action which will be observed
     * @param additionalTags The map of additional tags to be added to the observation
     */
    private static void rewriteObservableFunctionBody(BIRFunction func, BIRPackage pkg, boolean isResourceObservation,
                                                      String serviceName, String resourceOrAction,
                                                      Map<String, String> additionalTags) {
        // Injecting observe start call at the start of the function body
        {
            BIRBasicBlock startBB = func.basicBlocks.get(0);
            BIRBasicBlock newStartBB = insertAndGetNextBasicBlock(func.basicBlocks, 1, NEW_BB_PREFIX);
            swapBasicBlockContent(startBB, newStartBB);

            injectObserveStartCall(startBB, func.localVars, pkg, null, func.pos, isResourceObservation,
                    serviceName, resourceOrAction, additionalTags);

            // Fix the Basic Blocks linked list
            startBB.terminator.thenBB = newStartBB;
        }
        // Injecting error report call and observe end call just before the return statements
        boolean isErrorCheckRequired = isErrorAssignable(func.returnVariable);
        BIROperand returnValOperand = new BIROperand(func.returnVariable);
        int i = 1;  // Since the first block is now the start observation call
        while (i < func.basicBlocks.size()) {
            BIRBasicBlock currentBB = func.basicBlocks.get(i);
            BIRTerminator currentTerminator = currentBB.terminator;
            if (currentTerminator.kind == InstructionKind.RETURN) {
                if (isErrorCheckRequired) {
                    BIRBasicBlock errorReportBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 1, NEW_BB_PREFIX);
                    BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 2, NEW_BB_PREFIX);
                    BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 3, NEW_BB_PREFIX);
                    swapBasicBlockContent(currentBB, newCurrentBB);

                    injectCheckErrorCalls(currentBB, errorReportBB, observeEndBB, func.localVars, null,
                            returnValOperand);
                    injectReportErrorCall(errorReportBB, func.localVars, null, returnValOperand);
                    injectObserveEndCall(observeEndBB, null);

                    // Fix the Basic Blocks linked list
                    observeEndBB.terminator.thenBB = newCurrentBB;
                    errorReportBB.terminator.thenBB = observeEndBB;
                    i += 3; // Number of inserted BBs
                } else {
                    BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 1, NEW_BB_PREFIX);
                    swapBasicBlockContent(currentBB, newCurrentBB);

                    injectObserveEndCall(currentBB, null);

                    // Fix the Basic Blocks linked list
                    currentBB.terminator.thenBB = newCurrentBB;
                    i += 1; // Number of inserted BBs
                }
            } else if (currentTerminator.kind == InstructionKind.PANIC) {
                BIRTerminator.Panic panicCall = (BIRTerminator.Panic) currentTerminator;
                BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 1, NEW_BB_PREFIX);
                BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 2, NEW_BB_PREFIX);
                swapBasicBlockTerminator(currentBB, newCurrentBB);

                injectReportErrorCall(currentBB, func.localVars, currentTerminator.pos, panicCall.errorOp);
                injectObserveEndCall(observeEndBB, currentTerminator.pos);

                // Fix the Basic Blocks linked list
                currentBB.terminator.thenBB = observeEndBB;
                observeEndBB.terminator.thenBB = newCurrentBB;
                i += 2; // Number of inserted BBs
            }
            i++;
        }
    }

    /**
     * Re-write the relevant basic blocks in the list of basic blocks to observe function invocations.
     *
     * @param basicBlocks The list of basic blocks to check and instrument
     * @param scopeVarList The variables list in the scope
     * @param pkg The package which contains the instruction which will be observed
     */
    private static void rewriteObservableFunctionInvocations(List<BIRBasicBlock> basicBlocks,
                                                             List<BIRVariableDcl> scopeVarList, BIRPackage pkg) {
        int i = 0;
        while (i < basicBlocks.size()) {
            BIRBasicBlock currentBB = basicBlocks.get(i);
            BIRTerminator currentTerminator = currentBB.terminator;
            if (currentTerminator.kind == InstructionKind.CALL) {
                BIRTerminator.Call callIns = (BIRTerminator.Call) currentTerminator;
                boolean isRemote = callIns.calleeFlags.contains(Flag.REMOTE);
                boolean isObservableAnnotationPresent = false;
                for (BIRAnnotationAttachment annot : callIns.calleeAnnotAttachments) {
                    if (OBSERVABLE_ANNOTATION.equals(annot.packageID.orgName.value + "/"
                            + annot.packageID.name.value + "/" + annot.annotTagRef.value)) {
                        isObservableAnnotationPresent = true;
                        break;
                    }
                }
                if (isRemote || isObservableAnnotationPresent) {
                    DiagnosticPos desugaredInsPosition = callIns.pos;
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
                                selfArg.type.tsymbol.pkgID.name) + selfArg.type.tsymbol.name.value;
                    } else {
                        connectorName = "";
                    }
                    boolean isErrorCheckRequired = isErrorAssignable(callIns.lhsOp.variableDcl);

                    Map<String, String> tags = new HashMap<>();
                    if (isRemote) {
                        tags.put(IS_REMOTE_TAG_KEY, TAG_VALUE_TRUE);
                    }
                    if (isErrorCheckRequired) {
                        BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, NEW_BB_PREFIX);
                        BIRBasicBlock errorCheckBranchBB = insertAndGetNextBasicBlock(basicBlocks, i + 2,
                                NEW_BB_PREFIX);
                        BIRBasicBlock errorReportBB = insertAndGetNextBasicBlock(basicBlocks, i + 3, NEW_BB_PREFIX);
                        BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks, i + 4, NEW_BB_PREFIX);
                        swapBasicBlockContent(currentBB, newCurrentBB);

                        injectObserveStartCall(currentBB, scopeVarList, pkg, desugaredInsPosition, callIns.pos, false,
                                connectorName, action, tags);
                        injectCheckErrorCalls(errorCheckBranchBB, errorReportBB, observeEndBB, scopeVarList,
                                desugaredInsPosition, callIns.lhsOp);
                        injectReportErrorCall(errorReportBB, scopeVarList, desugaredInsPosition, callIns.lhsOp);
                        injectObserveEndCall(observeEndBB, desugaredInsPosition);

                        // Fix the Basic Blocks linked list
                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        errorReportBB.terminator.thenBB = observeEndBB;
                        newCurrentBB.terminator.thenBB = errorCheckBranchBB;
                        currentBB.terminator.thenBB = newCurrentBB; // Current BB now contains observe start call
                        i += 4; // Number of inserted BBs
                    } else {
                        BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, NEW_BB_PREFIX);
                        BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks, i + 2, NEW_BB_PREFIX);
                        swapBasicBlockContent(currentBB, newCurrentBB);

                        injectObserveStartCall(currentBB, scopeVarList, pkg, desugaredInsPosition, callIns.pos, false,
                                connectorName, action, tags);
                        injectObserveEndCall(observeEndBB, desugaredInsPosition);

                        // Fix the Basic Blocks linked list
                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        newCurrentBB.terminator.thenBB = observeEndBB;
                        currentBB.terminator.thenBB = newCurrentBB; // Current BB now contains observe start call
                        i += 2; // Number of inserted BBs
                    }
                }
            }
            i++;
        }
    }

    /**
     * Inject start observation call to a basic block.
     *
     * @param observeStartBB The basic block to which the start observation call should be injected
     * @param scopeVarList The variables list in the scope
     * @param pkg The package which contains the instruction which will be observed
     * @param desugaredInsPos The position of all instructions, variables declarations, terminators to be generated
     * @param originalInsPos Position of the original instruction which will be added as a tag to the observation
     * @param isResourceObservation True if a service resource will be observed by the observation
     * @param serviceOrConnector The service or connector name to which the instruction was attached to
     * @param resourceOrAction The name of the resource or action which will be observed
     * @param additionalTags The map of additional tags to be added to the observation
     */
    private static void injectObserveStartCall(BIRBasicBlock observeStartBB, List<BIRVariableDcl> scopeVarList,
                                               BIRPackage pkg, DiagnosticPos desugaredInsPos,
                                               DiagnosticPos originalInsPos, boolean isResourceObservation,
                                               String serviceOrConnector, String resourceOrAction,
                                               Map<String, String> additionalTags) {
        BIROperand connectorNameOperand = generateConstantOperand(
                String.format("%s_service_or_connector", observeStartBB.id.value), serviceOrConnector, scopeVarList,
                observeStartBB, desugaredInsPos);
        BIROperand actionNameOperand = generateConstantOperand(String.format("%s_resource_or_action",
                observeStartBB.id.value), resourceOrAction, scopeVarList, observeStartBB, desugaredInsPos);
        Map<String, String> tags = new HashMap<>();
        tags.put(INVOCATION_FQN_TAG_KEY, String.format("%s:%s:%s:%s:%d:%d", pkg.org.value, pkg.name.value,
                pkg.version.value, originalInsPos.src.cUnitName, originalInsPos.sLine, originalInsPos.sCol));
        tags.putAll(additionalTags);
        BIROperand tagsMapOperand = generateMapOperand(String.format("%s_tags", observeStartBB.id.value), tags,
                scopeVarList, observeStartBB, desugaredInsPos);

        String stringType = isBString ? I_STRING_VALUE : STRING_VALUE;
        JIMethodCall observeStartCallTerminator = new JIMethodCall(desugaredInsPos);
        observeStartCallTerminator.invocationType = INVOKESTATIC;
        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
        observeStartCallTerminator.jMethodVMSig = String.format("(L%s;L%s;L%s;)V", stringType, stringType,
                MAP_VALUE);
        observeStartCallTerminator.name = isResourceObservation
                ? START_RESOURCE_OBSERVATION_METHOD : START_CALLABLE_OBSERVATION_METHOD;
        observeStartCallTerminator.args = Arrays.asList(connectorNameOperand, actionNameOperand, tagsMapOperand);
        observeStartBB.terminator = observeStartCallTerminator;
    }

    /**
     * Inject branch condition for checking if a value is an error.
     *
     * @param errorCheckBranchBB The basic block to which the error check should be injected
     * @param errorReportBB The basic block to which the report error call should be injected
     * @param observeEndBB The basic block which will contain the stop observation call
     * @param scopeVarList The variables list in the scope
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param valueOperand Operand for passing the value which should be checked if it is an error
     */
    private static void injectCheckErrorCalls(BIRBasicBlock errorCheckBranchBB, BIRBasicBlock errorReportBB,
                                              BIRBasicBlock observeEndBB, List<BIRVariableDcl> scopeVarList,
                                              DiagnosticPos pos, BIROperand valueOperand) {
        BIRVariableDcl isErrorVariableDcl = new BIRVariableDcl(symbolTable.booleanType,
                new Name(String.format("$_%s_is_error_$", errorCheckBranchBB.id.value)), VarScope.FUNCTION,
                VarKind.TEMP);
        scopeVarList.add(isErrorVariableDcl);
        BIROperand isErrorOperand = new BIROperand(isErrorVariableDcl);
        BIRNonTerminator.TypeTest errorTypeTestInstruction = new BIRNonTerminator.TypeTest(pos, symbolTable.errorType,
                isErrorOperand, valueOperand);
        errorCheckBranchBB.instructions.add(errorTypeTestInstruction);
        errorCheckBranchBB.terminator = new BIRTerminator.Branch(pos, isErrorOperand, errorReportBB, observeEndBB);
    }

    /**
     * Inject report error call.
     *
     * @param errorReportBB The basic block to which the report error call should be injected
     * @param scopeVarList The variables list in the scope
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param errorOperand Operand for passing the error
     */
    private static void injectReportErrorCall(BIRBasicBlock errorReportBB, List<BIRVariableDcl> scopeVarList,
                                              DiagnosticPos pos, BIROperand errorOperand) {
        BIRVariableDcl castedErrorVariableDcl = new BIRVariableDcl(symbolTable.errorType,
                new Name(String.format("$_%s_casted_error_$", errorReportBB.id.value)), VarScope.FUNCTION,
                VarKind.TEMP);
        scopeVarList.add(castedErrorVariableDcl);
        BIROperand castedErrorOperand = new BIROperand(castedErrorVariableDcl);
        BIRNonTerminator.TypeCast errorCastInstruction = new BIRNonTerminator.TypeCast(pos, castedErrorOperand,
                errorOperand, symbolTable.errorType, false);
        errorReportBB.instructions.add(errorCastInstruction);

        JIMethodCall reportErrorCallTerminator = new JIMethodCall(pos);
        reportErrorCallTerminator.invocationType = INVOKESTATIC;
        reportErrorCallTerminator.jClassName = OBSERVE_UTILS;
        reportErrorCallTerminator.jMethodVMSig = String.format("(L%s;)V", ERROR_VALUE);
        reportErrorCallTerminator.name = REPORT_ERROR_METHOD;
        reportErrorCallTerminator.args = Collections.singletonList(castedErrorOperand);
        errorReportBB.terminator = reportErrorCallTerminator;
    }

    /**
     * Inject a stop observation call to a basic block.
     *
     * @param observeEndBB The basic block to which the stop observation call should be injected
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     */
    private static void injectObserveEndCall(BIRBasicBlock observeEndBB, DiagnosticPos pos) {
        JIMethodCall observeEndCallTerminator = new JIMethodCall(pos);
        observeEndCallTerminator.invocationType = INVOKESTATIC;
        observeEndCallTerminator.jClassName = OBSERVE_UTILS;
        observeEndCallTerminator.jMethodVMSig = "()V";
        observeEndCallTerminator.name = STOP_OBSERVATION_METHOD;
        observeEndCallTerminator.args = Collections.emptyList();
        observeEndBB.terminator = observeEndCallTerminator;
    }

    /**
     * Generate a constant operand from a string value.
     *
     * @param uniqueId A unique ID to identify this constant value
     * @param constantValue The constant value which should end up being passed in the operand
     * @param scopeVarList The variables list in the scope
     * @param basicBlock The basic block to which additional instructions should be added
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @return The generated operand which will pass the constant
     */
    private static BIROperand generateConstantOperand(String uniqueId, String constantValue,
                                                      List<BIRVariableDcl> scopeVarList, BIRBasicBlock basicBlock,
                                                      DiagnosticPos pos) {
        BIRVariableDcl variableDcl = new BIRVariableDcl(symbolTable.stringType,
                new Name(String.format("$_%s_const_$", uniqueId)), VarScope.FUNCTION, VarKind.TEMP);
        scopeVarList.add(variableDcl);
        BIROperand operand = new BIROperand(variableDcl);
        BIRNonTerminator.ConstantLoad instruction = new BIRNonTerminator.ConstantLoad(pos, constantValue,
                symbolTable.stringType, operand);
        basicBlock.instructions.add(instruction);
        return operand;
    }

    /**
     * Generate a Map type operand for a map of string keys and string values.
     *
     * @param uniqueId A unique ID to identify this map
     * @param map The map of which entries should end up being passed in the operand
     * @param scopeVarList The variables list in the scope
     * @param basicBlock The basic block to which additional instructions should be added
     * @param pos The position of all instructions, variables declarations, etc.
     * @return The generated operand which will pass the map
     */
    private static BIROperand generateMapOperand(String uniqueId, Map<String, String> map,
                                                 List<BIRVariableDcl> scopeVarList, BIRBasicBlock basicBlock,
                                                 DiagnosticPos pos) {
        BIRVariableDcl variableDcl = new BIRVariableDcl(symbolTable.mapType,
                new Name(String.format("$_%s_map_$", uniqueId)), VarScope.FUNCTION, VarKind.TEMP);
        scopeVarList.add(variableDcl);
        BIROperand tagsMapOperand = new BIROperand(variableDcl);

        BIRNonTerminator.NewStructure bMapNewInstruction = new BIRNonTerminator.NewStructure(pos,
                new BMapType(TypeTags.MAP, symbolTable.stringType, null), tagsMapOperand);
        basicBlock.instructions.add(bMapNewInstruction);

        int entryIndex = 0;
        for (Map.Entry<String, String> tagEntry: map.entrySet()) {
            BIROperand keyOperand = generateConstantOperand(String.format("%s_map_%d_key", uniqueId, entryIndex),
                    tagEntry.getKey(), scopeVarList, basicBlock, pos);
            BIROperand valueOperand = generateConstantOperand(String.format("%s_map_%d_value", uniqueId,
                    entryIndex), tagEntry.getValue(), scopeVarList, basicBlock, pos);
            BIRNonTerminator.FieldAccess fieldAccessIns = new BIRNonTerminator.FieldAccess(pos,
                    InstructionKind.MAP_STORE, tagsMapOperand, keyOperand, valueOperand);
            basicBlock.instructions.add(fieldAccessIns);
            entryIndex++;
        }
        return tagsMapOperand;
    }

    /**
     * Swap the effective content of two basic blocks.
     *
     * @param firstBB The first BB of which content should end up in second BB
     * @param secondBB The second BB of which content should end up in first BB
     */
    private static void swapBasicBlockContent(BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        List<BIRNonTerminator> firstBBInstructions = firstBB.instructions;
        firstBB.instructions = secondBB.instructions;
        secondBB.instructions = firstBBInstructions;
        swapBasicBlockTerminator(firstBB, secondBB);
    }

    /**
     * Swap the terminators of two basic blocks.
     *
     * @param firstBB The first BB of which terminator should end up in second BB
     * @param secondBB The second BB of which terminator should end up in first BB
     */
    private static void swapBasicBlockTerminator(BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        BIRTerminator firstBBTerminator = firstBB.terminator;
        firstBB.terminator = secondBB.terminator;
        secondBB.terminator = firstBBTerminator;
    }

    /**
     * Check is an error is assignable to a variable declaration.
     *
     * @param variableDcl The variable declaration which should be checked.
     * @return True if an error can be assigned and false otherwise
     */
    private static boolean isErrorAssignable(BIRVariableDcl variableDcl) {
        boolean isErrorAssignable = false;
        if (variableDcl.type instanceof BUnionType) {
            BUnionType returnUnionType = (BUnionType) variableDcl.type;
            isErrorAssignable = returnUnionType.getMemberTypes().stream()
                    .anyMatch(type -> type instanceof BErrorType);
        } else if (variableDcl.type instanceof BErrorType) {
            isErrorAssignable = true;
        }
        return isErrorAssignable;
    }

    /**
     * Remove the additional prefixes and postfixes added by the compiler.
     * This is done to get the original name used by the developer.
     *
     * @param serviceName The service name to be cleaned up
     * @return The cleaned up service name which should be equal to the name given by the developer
     */
    private static String cleanUpServiceName(String serviceName) {
        if (serviceName.contains(SERVICE_IDENTIFIER)) {
            if (serviceName.contains(ANONYMOUS_SERVICE_IDENTIFIER)) {
                return serviceName.replace(SERVICE_IDENTIFIER, "_");
            } else {
                return serviceName.substring(0, serviceName.lastIndexOf(SERVICE_IDENTIFIER));
            }
        }
        return serviceName;
    }
}
