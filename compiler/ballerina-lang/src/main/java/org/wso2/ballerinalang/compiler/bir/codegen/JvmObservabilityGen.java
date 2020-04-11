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

    public static void rewriteObservableFunctions(BIRPackage pkg) {
        for (BIRFunction func : pkg.functions) {
            rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
        }
        for (BIRTypeDefinition typeDef : pkg.typeDefs) {
            boolean isService = typeDef.type instanceof BServiceType;
            for (BIRFunction func : typeDef.attachedFuncs) {
                if (isService && !func.name.value.startsWith("$")) {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("source.service", "true");
                    rewriteObservableFunctionBody(func, pkg, true, cleanUpServiceName(typeDef.name.value), tags);
                }
                rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
            }
        }
    }

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
            startBB.terminator.thenBB = newStartBB;
        }
        // Injecting error report call and observe end call at the start of the function
        boolean isErrorCheckRequired = isErrorAssignable(func.returnVariable);
        BIROperand returnValOperand = new BIROperand(func.returnVariable);
        int i = 1;
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

                    observeEndBB.terminator.thenBB = newCurrentBB;
                    errorReportBB.terminator.thenBB = observeEndBB;
                    i += 3;
                } else {
                    BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(func.basicBlocks, i + 1, "desugaredBB");
                    swapBasicBlockContent(currentBB, newCurrentBB);

                    injectObserveEndCall(currentBB, desugaredInsPosition);

                    currentBB.terminator.thenBB = newCurrentBB;
                    i += 1;
                }
            }
            i++;
        }
    }

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

                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        errorReportBB.terminator.thenBB = observeEndBB;
                        newCurrentBB.terminator.thenBB = errorCheckBranchBB;
                        currentBB.terminator.thenBB = newCurrentBB; // Current BB now contains observe start call
                        i += 4;
                    } else {
                        BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, "desugaredBB");
                        BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks, i + 2, "desugaredBB");
                        swapBasicBlockContent(currentBB, newCurrentBB);

                        injectObserveStartCall(currentBB, scopeVarList, pkg, desugaredInsPosition, false, connectorName,
                                action, tags);
                        injectObserveEndCall(observeEndBB, desugaredInsPosition);

                        observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                        newCurrentBB.terminator.thenBB = observeEndBB;
                        currentBB.terminator.thenBB = newCurrentBB; // Current BB now contains observe start call
                        i += 2;
                    }
                }
            }
            i++;
        }
    }

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

    private static void injectObserveEndCall(BIRBasicBlock observeEndBB, DiagnosticPos pos) {
        JIMethodCall observeEndCallTerminator = new JIMethodCall(pos);
        observeEndCallTerminator.invocationType = INVOKESTATIC;
        observeEndCallTerminator.jClassName = OBSERVE_UTILS;
        observeEndCallTerminator.jMethodVMSig = "()V";
        observeEndCallTerminator.name = "stopObservation";
        observeEndCallTerminator.args = Collections.emptyList();
        observeEndBB.terminator = observeEndCallTerminator;
    }

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

    private static void swapBasicBlockContent(BIRBasicBlock firstBB, BIRBasicBlock secondBB) {
        List<BIRNonTerminator> firstBBInstructions = firstBB.instructions;
        firstBB.instructions = secondBB.instructions;
        secondBB.instructions = firstBBInstructions;

        BIRTerminator firstBBTerminator = firstBB.terminator;
        firstBB.terminator = secondBB.terminator;
        secondBB.terminator = firstBBTerminator;
    }

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
