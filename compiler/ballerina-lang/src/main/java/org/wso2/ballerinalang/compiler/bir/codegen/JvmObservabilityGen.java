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
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
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

    public static void rewriteObservableFunctions(BIRNode.BIRPackage pkg) {
        for (BIRNode.BIRFunction func : pkg.functions) {
            rewriteObservableFunctionInvocations(func.basicBlocks, func.localVars, pkg);
        }
        for (BIRNode.BIRTypeDefinition typeDef : pkg.typeDefs) {
            for (BIRNode.BIRFunction attachedFunc : typeDef.attachedFuncs) {
                rewriteObservableFunctionInvocations(attachedFunc.basicBlocks, attachedFunc.localVars, pkg);
            }
        }
    }

    public static void rewriteObservableFunctionInvocations(List<BIRNode.BIRBasicBlock> basicBlocks,
                                                            List<BIRNode.BIRVariableDcl> scopeVarList, BIRNode.BIRPackage pkg) {
        int i = 0;
        while (i < basicBlocks.size()) {
            BIRNode.BIRBasicBlock currentBB = basicBlocks.get(i);
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
                        BIRNode.BIRVariableDcl selfArg = getVariableDcl(callIns.args.get(0).variableDcl);
                        connectorName = getPackageName(selfArg.type.tsymbol.pkgID.orgName,
                                selfArg.type.tsymbol.pkgID.name) + selfArg.type.tsymbol.name.value;
                    } else {
                        connectorName = "";
                    }

                    boolean isErrorCheckRequired = false;
                    if (callIns.lhsOp.variableDcl.type instanceof BUnionType) {
                        BUnionType returnUnionType = (BUnionType) callIns.lhsOp.variableDcl.type;
                        isErrorCheckRequired = returnUnionType.getMemberTypes().stream()
                                .anyMatch(type -> type instanceof BErrorType);
                    } else if (callIns.lhsOp.variableDcl.type instanceof BErrorType) {
                        isErrorCheckRequired = true;
                    }

                    BIRNode.BIRBasicBlock newCurrentBB = insertAndGetNextBasicBlock(basicBlocks, i + 1, "desugaredBB");
                    BIRNode.BIRBasicBlock errorCheckBranchBB = null;
                    BIRNode.BIRBasicBlock errorReportBB = null;
                    if (isErrorCheckRequired) {
                        errorCheckBranchBB = insertAndGetNextBasicBlock(basicBlocks, i + 2, "desugaredBB");
                        errorReportBB = insertAndGetNextBasicBlock(basicBlocks, i + 3, "desugaredBB");
                    }
                    BIRNode.BIRBasicBlock observeEndBB = insertAndGetNextBasicBlock(basicBlocks,
                            i + (isErrorCheckRequired ? 4 : 2), "desugaredBB");

                    newCurrentBB.instructions = currentBB.instructions;
                    newCurrentBB.terminator = currentBB.terminator;
                    currentBB.instructions = new ArrayList<>(0);
                    currentBB.terminator = null;

                    {
                        BIROperand connectorNameOperand = generateConstantOperand(
                                String.format("%s_connector", currentBB.id.value), connectorName, scopeVarList,
                                currentBB, desugaredInsPosition);
                        BIROperand actionNameOperand = generateConstantOperand(
                                String.format("%s_action", currentBB.id.value), action, scopeVarList, currentBB,
                                desugaredInsPosition);
                        Map<String, String> tags = new HashMap<>();
                        tags.put("source.invocation_fqn", String.format("%s:%s:%s:%s:%d:%d", pkg.org.value,
                                pkg.name.value, pkg.version.value, callIns.pos.src.cUnitName, callIns.pos.sLine,
                                callIns.pos.sCol));
                        if (isRemote) {
                            tags.put("source.remote", "true");
                        }
                        BIROperand tagsMapOperand = generateMapOperand(String.format("%s_tags", currentBB.id.value),
                                tags, scopeVarList, currentBB, desugaredInsPosition);

                        InteropMethodGen.JIMethodCall observeStartCallTerminator = new InteropMethodGen.JIMethodCall(desugaredInsPosition);
                        observeStartCallTerminator.invocationType = INVOKESTATIC;
                        observeStartCallTerminator.jClassName = OBSERVE_UTILS;
                        observeStartCallTerminator.jMethodVMSig = String.format("(L%s;L%s;L%s;)V", STRING_VALUE,
                                STRING_VALUE, MAP_VALUE);
                        observeStartCallTerminator.name = "startCallableObservation";
                        observeStartCallTerminator.args = Arrays.asList(connectorNameOperand, actionNameOperand,
                                tagsMapOperand);
                        currentBB.terminator = observeStartCallTerminator;
                    }

                    if (isErrorCheckRequired) {
                        BIRNode.BIRVariableDcl isErrorVariableDcl = new BIRNode.BIRVariableDcl(symbolTable.booleanType,
                                new Name(String.format("$_%s_is_error_$", errorCheckBranchBB.id.value)),
                                VarScope.FUNCTION, VarKind.TEMP);
                        scopeVarList.add(isErrorVariableDcl);
                        BIROperand isErrorOperand = new BIROperand(isErrorVariableDcl);
                        BIRNonTerminator.TypeTest errorTypeTestInstruction = new BIRNonTerminator.TypeTest(desugaredInsPosition, symbolTable.errorType,
                                isErrorOperand, callIns.lhsOp);
                        errorCheckBranchBB.instructions.add(errorTypeTestInstruction);
                        errorCheckBranchBB.terminator = new BIRTerminator.Branch(desugaredInsPosition, isErrorOperand, errorReportBB,
                                observeEndBB);

                        BIRNode.BIRVariableDcl castedErrorVariableDcl = new BIRNode.BIRVariableDcl(symbolTable.errorType,
                                new Name(String.format("$_%s_casted_error_$", errorReportBB.id.value)),
                                VarScope.FUNCTION, VarKind.TEMP);
                        scopeVarList.add(castedErrorVariableDcl);
                        BIROperand castedErrorOperand = new BIROperand(castedErrorVariableDcl);
                        BIRNonTerminator.TypeCast errorCastInstruction = new BIRNonTerminator.TypeCast(desugaredInsPosition, castedErrorOperand,
                                callIns.lhsOp, symbolTable.errorType, false);
                        errorReportBB.instructions.add(errorCastInstruction);

                        InteropMethodGen.JIMethodCall reportErrorCallTerminator = new InteropMethodGen.JIMethodCall(desugaredInsPosition);
                        reportErrorCallTerminator.invocationType = INVOKESTATIC;
                        reportErrorCallTerminator.jClassName = OBSERVE_UTILS;
                        reportErrorCallTerminator.jMethodVMSig = String.format("(L%s;)V", ERROR_VALUE);
                        reportErrorCallTerminator.name = "reportError";
                        reportErrorCallTerminator.args = Collections.singletonList(castedErrorOperand);
                        errorReportBB.terminator = reportErrorCallTerminator;
                    }

                    InteropMethodGen.JIMethodCall observeEndCallTerminator = new InteropMethodGen.JIMethodCall(desugaredInsPosition);
                    observeEndCallTerminator.invocationType = INVOKESTATIC;
                    observeEndCallTerminator.jClassName = OBSERVE_UTILS;
                    observeEndCallTerminator.jMethodVMSig = "()V";
                    observeEndCallTerminator.name = "stopObservation";
                    observeEndCallTerminator.args = Collections.emptyList();
                    observeEndBB.terminator = observeEndCallTerminator;

                    observeEndBB.terminator.thenBB = newCurrentBB.terminator.thenBB;
                    if (isErrorCheckRequired) {
                        errorReportBB.terminator.thenBB = observeEndBB;
                        newCurrentBB.terminator.thenBB = errorCheckBranchBB;
                    } else {
                        newCurrentBB.terminator.thenBB = observeEndBB;
                    }
                    currentBB.terminator.thenBB = newCurrentBB; // Current BB now contains observe start call
                    i += (isErrorCheckRequired ? 4 : 2);
                }
            }
            i++;
        }
    }

    public static BIROperand generateConstantOperand(String uniqueId, String constantValue,
                                                     List<BIRNode.BIRVariableDcl> scopeVarList, BIRNode.BIRBasicBlock basicBlock,
                                                     DiagnosticPos desugaredInsPosition) {
        BIRNode.BIRVariableDcl variableDcl = new BIRNode.BIRVariableDcl(symbolTable.stringType,
                new Name(String.format("$_%s_const_$", uniqueId)), VarScope.FUNCTION, VarKind.TEMP);
        scopeVarList.add(variableDcl);
        BIROperand operand = new BIROperand(variableDcl);
        BIRNonTerminator.ConstantLoad instruction = new BIRNonTerminator.ConstantLoad(desugaredInsPosition, constantValue, symbolTable.stringType,
                operand);
        basicBlock.instructions.add(instruction);
        return operand;
    }

    public static BIROperand generateMapOperand(String uniqueId, Map<String, String> map,
                                                List<BIRNode.BIRVariableDcl> scopeVarList, BIRNode.BIRBasicBlock basicBlock,
                                                DiagnosticPos desugaredInsPosition) {
        BIRNode.BIRVariableDcl variableDcl = new BIRNode.BIRVariableDcl(symbolTable.mapType,
                new Name(String.format("$_%s_map_$", uniqueId)), VarScope.FUNCTION, VarKind.TEMP);
        scopeVarList.add(variableDcl);
        BIROperand tagsMapOperand = new BIROperand(variableDcl);

        BIRNonTerminator.NewStructure bMapNewInstruction = new BIRNonTerminator.NewStructure(desugaredInsPosition,
                new BMapType(TypeTags.MAP, symbolTable.stringType, null), tagsMapOperand);
        basicBlock.instructions.add(bMapNewInstruction);

        int entryIndex = 0;
        for (Map.Entry<String, String> tagEntry: map.entrySet()) {
            BIROperand keyOperand = generateConstantOperand(String.format("%s_map_%d_key", uniqueId, entryIndex),
                    tagEntry.getKey(), scopeVarList, basicBlock, desugaredInsPosition);
            BIROperand valueOperand = generateConstantOperand(String.format("%s_map_%d_value", uniqueId,
                    entryIndex), tagEntry.getValue(), scopeVarList, basicBlock, desugaredInsPosition);
            BIRNonTerminator.FieldAccess fieldAccessIns = new BIRNonTerminator.FieldAccess(desugaredInsPosition,
                    InstructionKind.MAP_STORE, tagsMapOperand, keyOperand, valueOperand);
            basicBlock.instructions.add(fieldAccessIns);
            entryIndex++;
        }
        return tagsMapOperand;
    }
}
