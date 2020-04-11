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

import org.ballerinalang.model.elements.Flag;
import org.objectweb.asm.MethodVisitor;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVABLE_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.insertAndGetNextBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;

/**
 * BIR observability model to JVM byte code generation class.
 *
 * @since 1.2.0
 */
class JvmObservabilityGen {

    static void emitStopObservationInvocation(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, "stopObservation", "()V", false);
    }

    static void emitReportErrorInvocation(MethodVisitor mv, int errorIndex) {
        mv.visitVarInsn(ALOAD, errorIndex);
        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, "reportError", String.format("(L%s;)V", ERROR_VALUE), false);
    }

    static void emitStartObservationInvocation(MethodVisitor mv, String serviceOrConnectorName,
                                               String resourceOrActionName, String observationStartMethod,
                                               Map<String, String> tags) {
        mv.visitLdcInsn(cleanUpServiceName(serviceOrConnectorName));
        mv.visitLdcInsn(resourceOrActionName);

        mv.visitTypeInsn(NEW, MAP_VALUE_IMPL);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "<init>", "()V", false);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(entry.getKey());
            mv.visitLdcInsn(entry.getValue());
            mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), true);
            mv.visitInsn(POP);
        }

        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, observationStartMethod,
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, MAP_VALUE), false);
    }

    private static String cleanUpServiceName(String serviceName) {
        final String serviceIdentifier = "$$service$";
        if (serviceName.contains(serviceIdentifier)) {
            if (serviceName.contains("$anonService$")) {
                return serviceName.replace(serviceIdentifier, "_");
            } else {
                return serviceName.substring(0, serviceName.lastIndexOf(serviceIdentifier));
            }
        }
        return serviceName;
    }

    static String getFullQualifiedRemoteFunctionName(String moduleOrg, String moduleName, String funcName) {

        if (moduleName.equals("")) {
            return funcName;
        }
        return moduleOrg + "/" + moduleName + "/" + funcName;
    }

    /**
     * Rewrite all observable functions in a package.
     *
     * @param pkg The package to instrument
     */
    public static void rewriteObservableFunctions(BIRPackage pkg) {
        for (BIRFunction func : pkg.functions) {
            if ("main".equals(func.name.value)) {
                Map<String, String> tags = new HashMap<>();
                tags.put("source.entry_point.main", "true");
                rewriteObservableFunctionBody(func, pkg, false, func.workerName.value, tags);
            }
            rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
        }
        for (BIRTypeDefinition typeDef : pkg.typeDefs) {
            boolean isService = typeDef.type instanceof BServiceType;
            for (BIRFunction func : typeDef.attachedFuncs) {
                if (isService && !func.name.value.startsWith("$")) {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("source.entry_point.resource", "true");
                    rewriteObservableFunctionBody(func, pkg, true, cleanUpServiceName(typeDef.name.value), tags);
                }
                rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
            }
        }
    }

    /**
     * Rewrite a function so that the internal body will be observed. This adds the relevant start and stop calls at
     * the beginning and return basic blocks of the function.
     *
     * This is only to be used in entry-point functions such as service resource functions and main method.
     *
     * @param func The function to instrument
     * @param pkg The package which contains the function
     * @param isResourceObservation True if the function is a service resource
     * @param serviceName The name of the service
     * @param additionalTags The map of additional tags to be added to the observation
     */
    private static void rewriteObservableFunctionBody(BIRFunction func, BIRPackage pkg, boolean isResourceObservation,
                                                      String serviceName, Map<String, String> additionalTags) {
        DiagnosticPos desugaredInsPosition = func.pos;
        // Injecting observe start call at the start of the function
        {
            BIRBasicBlock startBB = func.basicBlocks.get(0);
            BIRBasicBlock newStartBB = insertAndGetNextBasicBlock(func.basicBlocks, 1, "desugaredBB");
            swapBasicBlockContent(startBB, newStartBB);

            injectObserveStartCall(startBB, func.localVars, pkg, desugaredInsPosition, isResourceObservation,
                    serviceName, func.name.value, additionalTags);

            // Fix the Basic Blocks linked list
            startBB.terminator.thenBB = newStartBB;
        }
        // Injecting error report call and observe end call at the start of the function
        boolean isErrorCheckRequired = isErrorAssignable(func.returnVariable);
        BIROperand returnValOperand = new BIROperand(func.returnVariable);
        int i = 1;  // Since the first block is now the start observation call
        while (i < func.basicBlocks.size()) {
            BIRBasicBlock currentBB = func.basicBlocks.get(i);
            BIRTerminator currentTerminator = currentBB.terminator;
            if (currentTerminator.kind == InstructionKind.RETURN) {
                if (isErrorCheckRequired) {
                    BIRBasicBlock errorReportBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 1, "desugaredBB");
                    BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 2, "desugaredBB");
                    BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 3, "desugaredBB");
                    swapBasicBlockContent(currentBB, newCurrentBB);

                    injectCheckAndReportErrorCalls(currentBB, errorReportBB, observeEndBB, func.localVars,
                            desugaredInsPosition, returnValOperand);
                    injectObserveEndCall(observeEndBB, desugaredInsPosition);

                    // Fix the Basic Blocks linked list
                    observeEndBB.terminator.thenBB = newCurrentBB;
                    errorReportBB.terminator.thenBB = observeEndBB;
                    i += 3; // Number of inserted BBs
                } else {
                    BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 1, "desugaredBB");
                    swapBasicBlockContent(currentBB, newCurrentBB);

                    injectObserveEndCall(currentBB, desugaredInsPosition);

                    // Fix the Basic Blocks linked list
                    currentBB.terminator.thenBB = newCurrentBB;
                    i += 1; // Number of inserted BBs
                }
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
                    Map<String, String> tags = new HashMap<>();
                    if (isRemote) {
                        tags.put("source.remote", "true");
                    }
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

                    if (isErrorCheckRequired) {
                        BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, "desugaredBB");
                        BIRBasicBlock errorCheckBranchBB = insertAndGetNextBasicBlock(basicBlocks, i + 2, "desugaredBB");
                        BIRBasicBlock errorReportBB = insertAndGetNextBasicBlock(basicBlocks, i + 3, "desugaredBB");
                        BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks, i + 4, "desugaredBB");
                        swapBasicBlockContent(currentBB, newCurrentBB);

                        injectObserveStartCall(currentBB, scopeVarList, pkg, desugaredInsPosition, false, connectorName,
                                action, tags);
                        injectCheckAndReportErrorCalls(errorCheckBranchBB, errorReportBB, observeEndBB, scopeVarList,
                                desugaredInsPosition, callIns.lhsOp);
                        injectObserveEndCall(observeEndBB, desugaredInsPosition);

                        // Fix the Basic Blocks linked list
                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        errorReportBB.terminator.thenBB = observeEndBB;
                        newCurrentBB.terminator.thenBB = errorCheckBranchBB;
                        currentBB.terminator.thenBB = newCurrentBB; // Current BB now contains observe start call
                        i += 4; // Number of inserted BBs
                    } else {
                        BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, "desugaredBB");
                        BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks, i + 2, "desugaredBB");
                        swapBasicBlockContent(currentBB, newCurrentBB);

                        injectObserveStartCall(currentBB, scopeVarList, pkg, desugaredInsPosition, false, connectorName,
                                action, tags);
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
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     *            and the instructions which will be observed
     * @param isResourceObservation True if a service resource will be observed by the observation
     * @param serviceOrConnector The service or connector name to which the instruction was attached to
     * @param resourceOrAction The resource or action name which will be observed
     * @param additionalTags The map of additional tags to be added to the observation
     */
    private static void injectObserveStartCall(BIRBasicBlock observeStartBB, List<BIRVariableDcl> scopeVarList,
                                               BIRPackage pkg, DiagnosticPos pos, boolean isResourceObservation,
                                               String serviceOrConnector, String resourceOrAction,
                                               Map<String, String> additionalTags) {
        BIROperand connectorNameOperand = generateConstantOperand(
                String.format("%s_service_or_connector", observeStartBB.id.value), serviceOrConnector, scopeVarList,
                observeStartBB, pos);
        BIROperand actionNameOperand = generateConstantOperand( String.format("%s_resource_or_action",
                observeStartBB.id.value), resourceOrAction, scopeVarList, observeStartBB, pos);
        Map<String, String> tags = new HashMap<>();
        tags.put("source.invocation_fqn", String.format("%s:%s:%s:%s:%d:%d", pkg.org.value, pkg.name.value,
                pkg.version.value, pos.src.cUnitName, pos.sLine, pos.sCol));
        tags.putAll(additionalTags);
        BIROperand tagsMapOperand = generateMapOperand(String.format("%s_tags", observeStartBB.id.value), tags,
                scopeVarList, observeStartBB, pos);

        JIMethodCall observeStartCallTerminator = new JIMethodCall(pos);
        observeStartCallTerminator.invocationType = INVOKESTATIC;
        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
        observeStartCallTerminator.jMethodVMSig = String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE,
                MAP_VALUE);
        observeStartCallTerminator.name = isResourceObservation
                ? "startResourceObservation" : "startCallableObservation";
        observeStartCallTerminator.args = Arrays.asList(connectorNameOperand, actionNameOperand, tagsMapOperand);
        observeStartBB.terminator = observeStartCallTerminator;
    }

    /**
     * Inject branch condition for checking if the return value is an error and call report error if it is an error.
     *
     * @param errorCheckBranchBB The basic block to which the error check should be injected
     * @param errorReportBB The basic block to which the report error call should be injected
     * @param observeEndBB The basic block which will contain the stop observation call
     * @param scopeVarList The variables list in the scope
     * @param pos The position of all instructions, variables declarations, terminators, etc.
     * @param returnValueOperand Operand for passing the return value which should be checked if it is an error
     */
    private static void injectCheckAndReportErrorCalls(BIRBasicBlock errorCheckBranchBB, BIRBasicBlock errorReportBB,
                                                       BIRBasicBlock observeEndBB, List<BIRVariableDcl> scopeVarList,
                                                       DiagnosticPos pos, BIROperand returnValueOperand) {
        BIRVariableDcl isErrorVariableDcl = new BIRVariableDcl(symbolTable.booleanType,
                new Name(String.format("$_%s_is_error_$", errorCheckBranchBB.id.value)), VarScope.FUNCTION,
                VarKind.TEMP);
        scopeVarList.add(isErrorVariableDcl);
        BIROperand isErrorOperand = new BIROperand(isErrorVariableDcl);
        BIRNonTerminator.TypeTest errorTypeTestInstruction = new BIRNonTerminator.TypeTest(pos, symbolTable.errorType,
                isErrorOperand, returnValueOperand);
        errorCheckBranchBB.instructions.add(errorTypeTestInstruction);
        errorCheckBranchBB.terminator = new BIRTerminator.Branch(pos, isErrorOperand, errorReportBB, observeEndBB);

        BIRVariableDcl castedErrorVariableDcl = new BIRVariableDcl(symbolTable.errorType,
                new Name(String.format("$_%s_casted_error_$", errorReportBB.id.value)), VarScope.FUNCTION,
                VarKind.TEMP);
        scopeVarList.add(castedErrorVariableDcl);
        BIROperand castedErrorOperand = new BIROperand(castedErrorVariableDcl);
        BIRNonTerminator.TypeCast errorCastInstruction = new BIRNonTerminator.TypeCast(pos, castedErrorOperand, returnValueOperand, symbolTable.errorType,
                false);
        errorReportBB.instructions.add(errorCastInstruction);

        JIMethodCall reportErrorCallTerminator = new JIMethodCall(pos);
        reportErrorCallTerminator.invocationType = INVOKESTATIC;
        reportErrorCallTerminator.jClassName = OBSERVE_UTILS;
        reportErrorCallTerminator.jMethodVMSig = String.format("(L%s;)V", ERROR_VALUE);
        reportErrorCallTerminator.name = "reportError";
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
        observeEndCallTerminator.name = "stopObservation";
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
        BIRNonTerminator.ConstantLoad instruction = new BIRNonTerminator.ConstantLoad(pos, constantValue, symbolTable.stringType,
                operand);
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
}
