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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import io.ballerina.identifier.Utils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.FunctionParamComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.SCOPE_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.generateReturnType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getMethodDescParams;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATIONS_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANNOTATIONS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_SELF_INSTANCE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_LOCAL_VARIABLE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.YIELD_LOCATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.YIELD_STATUS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_CANCELLED_FUTURE_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BDECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BOBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_REGEXP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_XML;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INITIAL_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STACK_FRAMES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.UPDATE_CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;

/**
 * BIR function to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class MethodGen {

    protected static final String STATE = "state";
    protected static final String LOOP_VAR = "loopVar";
    protected static final String FUNCTION_INVOCATION = "functionInvocation";
    private static final String INVOCATION_COUNT = "%invocationCount";
    private static final String RESUME_INDEX = "resumeIndex";
    private final JvmPackageGen jvmPackageGen;
    private final SymbolTable symbolTable;
    private final Types types;

    public MethodGen(JvmPackageGen jvmPackageGen, Types types) {
        this.jvmPackageGen = jvmPackageGen;
        this.symbolTable = jvmPackageGen.symbolTable;
        this.types = types;
    }

    public void generateMethod(BIRFunction birFunc, ClassWriter cw, BIRPackage birModule, BType attachedType,
                               String moduleClassName, JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                               JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {
        if (JvmCodeGenUtil.isExternFunc(birFunc)) {
            ExternalMethodGen.genJMethodForBExternalFunc(birFunc, cw, birModule, attachedType, this, jvmPackageGen,
                    jvmTypeGen, jvmCastGen, jvmConstantsGen, moduleClassName,
                    asyncDataCollector, types);
        } else {
            genJMethodForBFunc(birFunc, cw, birModule, jvmTypeGen, jvmCastGen, jvmConstantsGen, moduleClassName,
                    attachedType, asyncDataCollector, false);
        }
    }

    public void genJMethodWithBObjectMethodCall(BIRFunction func, ClassWriter cw, BIRPackage module,
                                                JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                                JvmConstantsGen jvmConstantsGen, String moduleClassName,
                                                AsyncDataCollector asyncDataCollector,
                                                String splitClassName) {
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        indexMap.addIfNotExists(OBJECT_SELF_INSTANCE, symbolTable.anyType);
        indexMap.addIfNotExists(STRAND, symbolTable.stringType);
        String funcName = func.name.value;
        BType retType = getReturnType(func);
        String desc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, funcName, desc, null, null);
        mv.visitCode();
        Label methodStartLabel = new Label();
        mv.visitLabel(methodStartLabel);
        JvmInstructionGen instGen = new JvmInstructionGen(mv, indexMap, module.packageID, jvmPackageGen, jvmTypeGen,
                jvmCastGen, jvmConstantsGen, asyncDataCollector, types);
        mv.visitVarInsn(ALOAD, 1); // load strand
        mv.visitVarInsn(ALOAD, 0); // load self
        String encodedMethodName = Utils.encodeFunctionIdentifier(funcName);
        for (BIRNode.BIRFunctionParameter parameter : func.parameters) {
            instGen.generateVarLoad(mv, parameter, indexMap.addIfNotExists(parameter.name.value, parameter.type));
        }
        String methodDesc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType, moduleClassName);
        mv.visitMethodInsn(INVOKESTATIC, splitClassName, encodedMethodName, methodDesc, false);
        Label methodEndLabel = new Label();
        mv.visitLabel(methodEndLabel);
        generateReturnTermFromType(retType, mv);
        mv.visitLocalVariable(OBJECT_SELF_INSTANCE, GET_BOBJECT, null, methodStartLabel, methodEndLabel, 0);
        mv.visitLocalVariable(STRAND_LOCAL_VARIABLE_NAME, GET_STRAND, null, methodStartLabel, methodEndLabel, 1);
        for (BIRNode.BIRFunctionParameter parameter : func.parameters) {
            String metaVarName = parameter.metaVarName;
            if (isValidArg(parameter) && isCompilerAddedVars(metaVarName)) {
                mv.visitLocalVariable(metaVarName, getJVMTypeSign(parameter.type), null, methodStartLabel,
                        methodEndLabel, indexMap.addIfNotExists(parameter.name.value, parameter.type));
            }
        }
        JvmCodeGenUtil.visitMaxStackForMethod(mv, funcName, moduleClassName);
        mv.visitEnd();
    }

    private void generateReturnTermFromType(BType bType, MethodVisitor mv) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitInsn(LRETURN);
            return;
        }
        switch (bType.tag) {
            case TypeTags.BYTE, TypeTags.BOOLEAN -> mv.visitInsn(IRETURN);
            case TypeTags.FLOAT -> mv.visitInsn(DRETURN);
            default -> mv.visitInsn(ARETURN);
        }
    }

    public void genJMethodForBFunc(BIRFunction func, ClassWriter cw, BIRPackage module,
                                   JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                   JvmConstantsGen jvmConstantsGen, String moduleClassName,
                                   BType attachedType, AsyncDataCollector asyncDataCollector,
                                   boolean isObjectMethodSplit) {

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        boolean isWorker = Symbols.isFlagOn(func.flags, Flags.WORKER);

        // generate method desc
        int access = Opcodes.ACC_PUBLIC;
        // localVarOffset is actually the local var index of the strand which is passed as an argument to the function
        int localVarOffset;
        if (attachedType != null && !isObjectMethodSplit) {
            localVarOffset = 1;
            // add the self as the first local var
            indexMap.addIfNotExists(OBJECT_SELF_INSTANCE, symbolTable.anyType);
        } else {
            localVarOffset = 0;
            access += ACC_STATIC;
        }

        indexMap.addIfNotExists(STRAND, symbolTable.stringType);
        if (isObjectMethodSplit) {
            indexMap.addIfNotExists(OBJECT_SELF_INSTANCE, symbolTable.anyType);
        }

        String funcName = func.name.value;
        BType retType = getReturnType(func);
        String desc;
        int invocationCountArgVarIndex = -1;
        if (isWorker) {
            invocationCountArgVarIndex = indexMap.addIfNotExists(INVOCATION_COUNT, symbolTable.stringType);
            desc = INITIAL_METHOD_DESC + "I" + getMethodDescParams(func.type.paramTypes) + generateReturnType(retType);
        } else if (isObjectMethodSplit) {
            desc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType, moduleClassName);
        } else {
            desc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType);
        }
        MethodVisitor mv = cw.visitMethod(access, funcName, desc, null, null);
        mv.visitCode();

        visitModuleStartFunction(module.packageID, funcName, mv);

        Label methodStartLabel = new Label();
        mv.visitLabel(methodStartLabel);

        genLocalVars(indexMap, mv, func.localVars);

        int returnVarRefIndex = getReturnVarRefIndex(func, indexMap, retType, mv);
        int stateVarIndex = getIntVarIndex(STATE, indexMap, mv, ICONST_0);
        int loopVarIndex = getIntVarIndex(LOOP_VAR, indexMap, mv, ICONST_1);
        int yieldLocationVarIndex = getFrameStringVarIndex(indexMap, mv, YIELD_LOCATION);
        int yieldStatusVarIndex = getFrameStringVarIndex(indexMap, mv, YIELD_STATUS);
        int invocationVarIndex = getIntVarIndex(FUNCTION_INVOCATION, indexMap, mv, ICONST_0);

        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, RESUME_INDEX, "I");

        LabelGenerator labelGen = new LabelGenerator();
        Label resumeLabel = labelGen.getLabel(funcName + "resume");
        mv.visitJumpInsn(IFGT, resumeLabel);

        // set function invocation variable
        setFunctionInvocationVar(localVarOffset, mv, invocationVarIndex, invocationCountArgVarIndex);
        // set channel details to strand.
        setChannelDetailsToStrand(func, localVarOffset, mv, invocationVarIndex);

        Label varInitLabel = labelGen.getLabel(funcName + "varinit");
        mv.visitLabel(varInitLabel);

        // panic if this strand is cancelled
        checkStrandCancelled(mv, localVarOffset);

        // handle loops reducible way
        Label loopLabel = new Label();
        mv.visitLabel(loopLabel);
        mv.visitVarInsn(ILOAD, loopVarIndex);
        Label loopConditionLabel = new Label();
        mv.visitJumpInsn(IFEQ, loopConditionLabel);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, loopVarIndex);

        // process basic blocks
        List<Label> labels = new ArrayList<>();
        List<Integer> states = new ArrayList<>();

        addCasesForBasicBlocks(func, funcName, labelGen, labels, states);

        JvmInstructionGen instGen = new JvmInstructionGen(mv, indexMap, module.packageID, jvmPackageGen, jvmTypeGen,
                jvmCastGen, jvmConstantsGen, asyncDataCollector,
                types);
        JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instGen);
        JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, module.packageID, instGen,
                jvmPackageGen, jvmTypeGen, jvmCastGen, asyncDataCollector);

        mv.visitVarInsn(ILOAD, stateVarIndex);
        Label yieldLabel = labelGen.getLabel(funcName + "yield");
        mv.visitLookupSwitchInsn(yieldLabel, toIntArray(states), labels.toArray(new Label[0]));

        generateBasicBlocks(mv, labelGen, errorGen, instGen, termGen, func, returnVarRefIndex, stateVarIndex,
                yieldLocationVarIndex, yieldStatusVarIndex, loopVarIndex, invocationVarIndex, localVarOffset, module,
                attachedType, moduleClassName, loopLabel);
        mv.visitLabel(resumeLabel);
        String frameName = MethodGenUtils.getFrameClassName(JvmCodeGenUtil.getPackageName(module.packageID), funcName,
                attachedType);
        genGetFrameOnResumeIndex(localVarOffset, mv, frameName);

        generateFrameClassFieldLoad(func.localVars, mv, indexMap, frameName);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, frameName, STATE, "I");
        mv.visitVarInsn(ISTORE, stateVarIndex);
        mv.visitFieldInsn(GETFIELD, frameName, FUNCTION_INVOCATION, "I");
        mv.visitVarInsn(ISTORE, invocationVarIndex);
        mv.visitJumpInsn(GOTO, varInitLabel);

        mv.visitLabel(yieldLabel);
        mv.visitTypeInsn(NEW, frameName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, frameName, JVM_INIT_METHOD, VOID_METHOD_DESC, false);

        generateFrameClassFieldUpdate(func.localVars, mv, indexMap, frameName);

        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, stateVarIndex);
        mv.visitFieldInsn(PUTFIELD, frameName, STATE, "I");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, invocationVarIndex);
        mv.visitFieldInsn(PUTFIELD, frameName, FUNCTION_INVOCATION, "I");
        generateFrameStringFieldSet(mv, frameName, yieldLocationVarIndex, YIELD_LOCATION);
        generateFrameStringFieldSet(mv, frameName, yieldStatusVarIndex, YIELD_STATUS);

        generateGetFrame(indexMap, localVarOffset, mv);
        mv.visitJumpInsn(GOTO, loopLabel);
        mv.visitLabel(loopConditionLabel);

        Label methodEndLabel = new Label();
        mv.visitLabel(methodEndLabel);
        termGen.genReturnTerm(returnVarRefIndex, func, invocationVarIndex, localVarOffset);

        // Create Local Variable Table
        createLocalVariableTable(func, indexMap, localVarOffset, mv, methodStartLabel, labelGen, methodEndLabel,
                isObjectMethodSplit);

        JvmCodeGenUtil.visitMaxStackForMethod(mv, funcName, moduleClassName);
        mv.visitEnd();
    }

    private void setFunctionInvocationVar(int localVarOffset, MethodVisitor mv, int invocationVarIndex,
                                          int invocationCountArgVarIndex) {
        if (invocationCountArgVarIndex == -1) {
            mv.visitVarInsn(ALOAD, localVarOffset);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETFIELD, STRAND_CLASS, FUNCTION_INVOCATION, "I");
            mv.visitInsn(DUP_X1);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, FUNCTION_INVOCATION, "I");
        } else {
            // this means this is a function created for a worker
            mv.visitVarInsn(ILOAD, invocationCountArgVarIndex);
        }
        mv.visitVarInsn(ISTORE, invocationVarIndex);
    }

    private void generateFrameStringFieldSet(MethodVisitor mv, String frameName, int rhsVarIndex, String fieldName) {
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, rhsVarIndex);
        mv.visitFieldInsn(PUTFIELD, frameName, fieldName, GET_STRING);
    }

    private BType getReturnType(BIRFunction func) {
        BType retType = func.type.retType;
        if (JvmCodeGenUtil.isExternFunc(func) && Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = JvmCodeGenUtil.UNIFIER.build(func.type.retType);
        }
        return retType;
    }

    private void visitModuleStartFunction(PackageID packageID, String funcName, MethodVisitor mv) {
        if (!isModuleStartFunction(funcName)) {
            return;
        }
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, JvmCodeGenUtil.getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME),
                MODULE_START_ATTEMPTED, "Z");
    }

    private void setChannelDetailsToStrand(BIRFunction func, int localVarOffset, MethodVisitor mv,
                                           int invocationVarIndex) {
        // these channel info is required to notify datachannels, when there is a panic
        // we cannot set this during strand creation, because function call do not have this info.
        if (func.workerChannels.length <= 0) {
            return;
        }
        mv.visitVarInsn(ALOAD, localVarOffset);
        JvmCodeGenUtil.loadChannelDetails(mv, Arrays.asList(func.workerChannels), invocationVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "updateChannelDetails",
                UPDATE_CHANNEL_DETAILS, false);
    }

    private void checkStrandCancelled(MethodVisitor mv, int localVarOffset) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "cancel", "Z");
        Label notCancelledLabel = new Label();
        mv.visitJumpInsn(IFEQ, notCancelledLabel);
        mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, "createCancelledFutureError", CREATE_CANCELLED_FUTURE_ERROR,
                false);
        mv.visitInsn(ATHROW);

        mv.visitLabel(notCancelledLabel);
    }

    private void genLocalVars(BIRVarToJVMIndexMap indexMap, MethodVisitor mv, List<BIRVariableDcl> localVars) {
        localVars.sort(new FunctionParamComparator());
        for (int i = 1; i < localVars.size(); i++) {
            BIRVariableDcl localVar = localVars.get(i);
            int index = indexMap.addIfNotExists(localVar.name.value, localVar.type);
            if (localVar.kind != VarKind.ARG) {
                BType bType = localVar.type;
                genDefaultValue(mv, bType, index);
            }
        }
    }

    private int getReturnVarRefIndex(BIRFunction func, BIRVarToJVMIndexMap indexMap, BType retType, MethodVisitor mv) {
        BIRVariableDcl varDcl = func.localVars.get(0);
        int returnVarRefIndex = indexMap.addIfNotExists(varDcl.name.value, varDcl.type);
        genDefaultValue(mv, retType, returnVarRefIndex);
        return returnVarRefIndex;
    }

    private void genDefaultValue(MethodVisitor mv, BType bType, int index) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitInsn(LCONST_0);
            mv.visitVarInsn(LSTORE, index);
            return;
        } else if (TypeTags.isStringTypeTag(bType.tag) || TypeTags.isXMLTypeTag(bType.tag)) {
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, index);
            return;
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
            case TypeTags.BOOLEAN:
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ISTORE, index);
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(DCONST_0);
                mv.visitVarInsn(DSTORE, index);
                break;
            case TypeTags.MAP:
            case TypeTags.ARRAY:
            case TypeTags.STREAM:
            case TypeTags.TABLE:
            case TypeTags.ERROR:
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.OBJECT:
            case TypeTags.CHAR_STRING:
            case TypeTags.DECIMAL:
            case TypeTags.UNION:
            case TypeTags.RECORD:
            case TypeTags.TUPLE:
            case TypeTags.FUTURE:
            case TypeTags.JSON:
            case TypeTags.INVOKABLE:
            case TypeTags.FINITE:
            case TypeTags.HANDLE:
            case TypeTags.TYPEDESC:
            case TypeTags.READONLY:
            case TypeTags.REGEXP:
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, index);
                break;
            case JTypeTags.JTYPE:
                genJDefaultValue(mv, (JType) bType, index);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    private void genJDefaultValue(MethodVisitor mv, JType jType, int index) {
        switch (jType.jTag) {
            case JTypeTags.JBYTE:
            case JTypeTags.JCHAR:
            case JTypeTags.JSHORT:
            case JTypeTags.JINT:
            case JTypeTags.JBOOLEAN:
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ISTORE, index);
                break;
            case JTypeTags.JLONG:
                mv.visitInsn(LCONST_0);
                mv.visitVarInsn(LSTORE, index);
                break;
            case JTypeTags.JFLOAT:
                mv.visitInsn(FCONST_0);
                mv.visitVarInsn(FSTORE, index);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitInsn(DCONST_0);
                mv.visitVarInsn(DSTORE, index);
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, index);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        jType);
        }
    }

    private int getIntVarIndex(String intVarName, BIRVarToJVMIndexMap indexMap, MethodVisitor mv, int initialValue) {
        int varIndex = indexMap.addIfNotExists(intVarName, symbolTable.stringType);
        mv.visitInsn(initialValue);
        mv.visitVarInsn(ISTORE, varIndex);
        return varIndex;
    }

    private int getFrameStringVarIndex(BIRVarToJVMIndexMap indexMap, MethodVisitor mv, String frameStringFieldName) {
        int stateVarIndex = indexMap.addIfNotExists(frameStringFieldName, symbolTable.stringType);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, stateVarIndex);
        return stateVarIndex;
    }

    private void addCasesForBasicBlocks(BIRFunction func, String funcName, LabelGenerator labelGen, List<Label> labels,
                                        List<Integer> states) {
        int caseIndex = 0;
        for (int i = 0; i < func.basicBlocks.size(); i++) {
            BIRBasicBlock bb = func.basicBlocks.get(i);
            if (i == 0) {
                labels.add(caseIndex, labelGen.getLabel(funcName + bb.id.value));
                states.add(caseIndex, caseIndex);
                caseIndex += 1;
            }
            labels.add(caseIndex, labelGen.getLabel(funcName + bb.id.value + "beforeTerm"));
            states.add(caseIndex, caseIndex);
            caseIndex += 1;
        }
    }

    private int[] toIntArray(List<Integer> states) {
        int[] ints = new int[states.size()];
        for (int i = 0; i < states.size(); i++) {
            ints[i] = states.get(i);
        }
        return ints;
    }

    void generateBasicBlocks(MethodVisitor mv, LabelGenerator labelGen, JvmErrorGen errorGen, JvmInstructionGen instGen,
                             JvmTerminatorGen termGen, BIRFunction func, int returnVarRefIndex, int stateVarIndex,
                             int yieldLocationVarIndex, int yieldStatusVarIndex, int loopVarIndex,
                             int invocationVarIndex, int localVarOffset, BIRPackage module, BType attachedType,
                             String moduleClassName, Label loopLabel) {

        String funcName = func.name.value;
        BirScope lastScope = null;
        Set<BirScope> visitedScopesSet = new HashSet<>();

        int caseIndex = 0;
        for (int i = 0; i < func.basicBlocks.size(); i++) {
            BIRBasicBlock bb = func.basicBlocks.get(i);
            // create jvm label
            Label bbLabel = labelGen.getLabel(funcName + bb.id.value);
            mv.visitLabel(bbLabel);
            if (i == 0) {
                pushShort(mv, stateVarIndex, caseIndex);
                caseIndex += 1;
            }

            // generate instructions
            lastScope = JvmCodeGenUtil
                    .getLastScopeFromBBInsGen(mv, labelGen, instGen, localVarOffset, funcName, bb,
                            visitedScopesSet, lastScope);

            Label bbEndLabel = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
            mv.visitLabel(bbEndLabel);

            String fullyQualifiedFuncName = getFullyQualifiedFuncName(func.type.tsymbol, funcName);
            BIRTerminator terminator = bb.terminator;
            pushShort(mv, stateVarIndex, caseIndex);
            caseIndex += 1;

            processTerminator(mv, func, module, funcName, terminator);
            termGen.genTerminator(terminator, moduleClassName, func, funcName, localVarOffset, stateVarIndex,
                    returnVarRefIndex, attachedType, yieldLocationVarIndex, yieldStatusVarIndex, invocationVarIndex,
                    loopVarIndex, fullyQualifiedFuncName, bb, loopLabel);

            lastScope = JvmCodeGenUtil
                    .getLastScopeFromTerminator(mv, bb, funcName, labelGen, lastScope, visitedScopesSet);

            errorGen.generateTryCatch(func, funcName, bb, termGen, labelGen, invocationVarIndex, localVarOffset);

            String yieldStatus = getYieldStatusByTerminator(terminator);

            BIRBasicBlock thenBB = terminator.thenBB;
            if (thenBB != null) {
                JvmCodeGenUtil.genYieldCheck(mv, termGen.getLabelGenerator(), thenBB, funcName, localVarOffset,
                        yieldLocationVarIndex, terminator.pos, fullyQualifiedFuncName, yieldStatus,
                        yieldStatusVarIndex);
            }
        }
    }

    private String getFullyQualifiedFuncName(BTypeSymbol funcTypeSymbol, String funcName) {
        if (funcTypeSymbol != null) {
            PackageID funcTSymbolPkgID = funcTypeSymbol.pkgID;
            return funcTSymbolPkgID.getOrgName().toString() + "." +
                    funcTSymbolPkgID.getName().toString() + "." + funcTSymbolPkgID.getPackageVersion().toString() +
                    ":" + funcName;
        }
        return funcName;
    }

    private String getYieldStatusByTerminator(BIRTerminator terminator) {
        switch (terminator.kind) {
            case WK_SEND:
                return "BLOCKED ON WORKER MESSAGE SEND";
            case WK_RECEIVE:
                return "BLOCKED ON WORKER MESSAGE RECEIVE";
            case FLUSH:
                return "BLOCKED ON WORKER MESSAGE FLUSH";
            case WAIT:
            case WAIT_ALL:
                return "WAITING";
            default:
                return "BLOCKED";
        }
    }

    private void pushShort(MethodVisitor mv, int stateVarIndex, int caseIndex) {
        // SIPUSH range is (-32768 to 32767) so if the state index goes beyond that, need to use visitLdcInsn
        mv.visitIntInsn(SIPUSH, caseIndex);
        mv.visitVarInsn(ISTORE, stateVarIndex);
    }

    private void processTerminator(MethodVisitor mv, BIRFunction func, BIRPackage module, String funcName,
                                   BIRTerminator terminator) {
        if (terminator.kind == InstructionKind.GOTO &&
                ((BIRTerminator.GOTO) terminator).targetBB.terminator.kind == InstructionKind.RETURN &&
                ((BIRTerminator.GOTO) terminator).targetBB.terminator.pos != null) {
            // The ending line number of the function is added to the line number table to prevent wrong code
            // coverage for generated return statements.
            Label label = new Label();
            mv.visitLabel(label);
            mv.visitLineNumber(
                    ((BIRTerminator.GOTO) terminator).targetBB.terminator.pos.lineRange().endLine().line() + 1, label);
        } else if (terminator.kind != InstructionKind.RETURN) {
            JvmCodeGenUtil.generateDiagnosticPos(terminator.pos, mv);
        }
        if ((MethodGenUtils.isModuleInitFunction(func) || isModuleTestInitFunction(func)) &&
                terminator instanceof Return) {
            String moduleAnnotationsClass = getModuleLevelClassName(module.packageID, MODULE_ANNOTATIONS_CLASS_NAME);
            mv.visitMethodInsn(INVOKESTATIC, moduleAnnotationsClass, ANNOTATIONS_METHOD_PREFIX, VOID_METHOD_DESC,
                    false);
        }
        //set module start success to true for $_init class
        if (isModuleStartFunction(funcName) && terminator.kind == InstructionKind.RETURN) {
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC, JvmCodeGenUtil.getModuleLevelClassName(module.packageID,
                            MODULE_INIT_CLASS_NAME),
                    MODULE_STARTED, "Z");
        }
    }

    private boolean isModuleTestInitFunction(BIRFunction func) {
        return func.name.value.equals(
                MethodGenUtils
                        .encodeModuleSpecialFuncName(".<testinit>"));
    }

    private boolean isModuleStartFunction(String functionName) {
        return functionName
                .equals(MethodGenUtils.encodeModuleSpecialFuncName(MethodGenUtils.START_FUNCTION_SUFFIX));
    }

    private void genGetFrameOnResumeIndex(int localVarOffset, MethodVisitor mv, String frameName) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, STACK_FRAMES);
        mv.visitMethodInsn(INVOKEVIRTUAL, STACK, "pop", RETURN_OBJECT, false);
        mv.visitTypeInsn(CHECKCAST, frameName);
    }

    private void generateFrameClassFieldLoad(List<BIRVariableDcl> localVars, MethodVisitor mv,
                                             BIRVarToJVMIndexMap indexMap, String frameName) {
        for (BIRVariableDcl localVar : localVars) {
            if (localVar.onlyUsedInSingleBB) {
                continue;
            }
            BType bType = JvmCodeGenUtil.getImpliedType(localVar.type);
            int index = indexMap.addIfNotExists(localVar.name.value, bType);
            mv.visitInsn(DUP);

            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "J");
                mv.visitVarInsn(LSTORE, index);
            } else if (TypeTags.isStringTypeTag(bType.tag)) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName,
                        GET_BSTRING);
                mv.visitVarInsn(ASTORE, index);
            } else if (bType.tag == TypeTags.NEVER) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_OBJECT);
                mv.visitVarInsn(ASTORE, index);
            } else if (types.isAssignable(bType, symbolTable.xmlType)) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName,
                        GET_XML);
                mv.visitVarInsn(ASTORE, index);
            } else if (TypeTags.REGEXP == bType.tag) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName,
                        GET_REGEXP);
                mv.visitVarInsn(ASTORE, index);
            } else {
                generateFrameClassFieldLoadByTypeTag(mv, frameName, localVar, index, bType);
            }
        }

    }

    private void generateFrameClassFieldLoadByTypeTag(MethodVisitor mv, String frameName, BIRVariableDcl localVar,
                                                      int index, BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        switch (bType.tag) {
            case TypeTags.BYTE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "I");
                mv.visitVarInsn(ISTORE, index);
                break;
            case TypeTags.FLOAT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "D");
                mv.visitVarInsn(DSTORE, index);
                break;
            case TypeTags.DECIMAL:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_BDECIMAL);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.BOOLEAN:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "Z");
                mv.visitVarInsn(ISTORE, index);
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_MAP_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.STREAM:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_STREAM_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.TABLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_TABLE_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_ARRAY_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.OBJECT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_BOBJECT);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.ERROR:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_ERROR_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.FUTURE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_FUTURE_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.INVOKABLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_FUNCTION_POINTER);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.TYPEDESC:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_TYPEDESC);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.NIL:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_OBJECT);
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.HANDLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, GET_HANDLE_VALUE);
                mv.visitVarInsn(ASTORE, index);
                break;
            case JTypeTags.JTYPE:
                generateFrameClassJFieldLoad(localVar, mv, index, frameName);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    private void generateFrameClassJFieldLoad(BIRVariableDcl localVar, MethodVisitor mv,
                                              int index, String frameName) {
        JType jType = (JType) JvmCodeGenUtil.getImpliedType(localVar.type);

        switch (jType.jTag) {
            case JTypeTags.JBYTE:
            case JTypeTags.JCHAR:
            case JTypeTags.JSHORT:
            case JTypeTags.JINT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "I");
                mv.visitVarInsn(ISTORE, index);
                break;
            case JTypeTags.JLONG:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "J");
                mv.visitVarInsn(LSTORE, index);
                break;
            case JTypeTags.JFLOAT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "F");
                mv.visitVarInsn(FSTORE, index);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "D");
                mv.visitVarInsn(DSTORE, index);
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName, "Z");
                mv.visitVarInsn(ISTORE, index);
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.jvmVarName,
                        InteropMethodGen.getJTypeSignature(jType));
                mv.visitVarInsn(ASTORE, index);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        jType);
        }
    }

    private void generateFrameClassFieldUpdate(List<BIRVariableDcl> localVars, MethodVisitor mv,
                                               BIRVarToJVMIndexMap indexMap, String frameName) {
        for (BIRVariableDcl localVar : localVars) {
            if (localVar.onlyUsedInSingleBB) {
                continue;
            }
            BType bType = JvmCodeGenUtil.getImpliedType(localVar.type);
            int index = indexMap.addIfNotExists(localVar.name.value, bType);
            mv.visitInsn(DUP);

            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                mv.visitVarInsn(LLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "J");
            } else if (TypeTags.isStringTypeTag(bType.tag)) {
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_BSTRING);
            } else if (TypeTags.isXMLTypeTag(bType.tag)) {
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_XML);
            } else if (TypeTags.REGEXP == bType.tag) {
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_REGEXP);
            } else {
                generateFrameClassFieldUpdateByTypeTag(mv, frameName, localVar, index, bType);
            }
        }
    }

    private void generateFrameClassFieldUpdateByTypeTag(MethodVisitor mv, String frameName, BIRVariableDcl localVar,
                                                        int index, BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        switch (bType.tag) {
            case TypeTags.BYTE:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "I");
                break;
            case TypeTags.FLOAT:
                mv.visitVarInsn(DLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "D");
                break;
            case TypeTags.DECIMAL:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName,
                        GET_BDECIMAL);
                break;
            case TypeTags.BOOLEAN:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "Z");
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_MAP_VALUE);
                break;
            case TypeTags.STREAM:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_STREAM_VALUE);
                break;
            case TypeTags.TABLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_TABLE_VALUE);
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_ARRAY_VALUE);
                break;
            case TypeTags.ERROR:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_ERROR_VALUE);
                break;
            case TypeTags.FUTURE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_FUTURE_VALUE);
                break;
            case TypeTags.TYPEDESC:
                mv.visitVarInsn(ALOAD, index);
                mv.visitTypeInsn(CHECKCAST, TYPEDESC_VALUE);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_TYPEDESC);
                break;
            case TypeTags.OBJECT:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_BOBJECT);
                break;
            case TypeTags.INVOKABLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_FUNCTION_POINTER);
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_OBJECT);
                break;
            case TypeTags.HANDLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, GET_HANDLE_VALUE);
                break;
            case JTypeTags.JTYPE:
                generateFrameClassJFieldUpdate(localVar, mv, index, frameName);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    private void generateFrameClassJFieldUpdate(BIRVariableDcl localVar, MethodVisitor mv,
                                                int index, String frameName) {
        JType jType = (JType) localVar.type;
        switch (jType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "B");
                break;
            case JTypeTags.JCHAR:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "C");
                break;
            case JTypeTags.JSHORT:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "S");
                break;
            case JTypeTags.JINT:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "I");
                break;
            case JTypeTags.JLONG:
                mv.visitVarInsn(LLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "J");
                break;
            case JTypeTags.JFLOAT:
                mv.visitVarInsn(FLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "F");
                break;
            case JTypeTags.JDOUBLE:
                mv.visitVarInsn(DLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "D");
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, "Z");
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                String classSig = InteropMethodGen.getJTypeSignature(jType);
                String className = InteropMethodGen.getSignatureForJType(jType);
                mv.visitVarInsn(ALOAD, index);
                mv.visitTypeInsn(CHECKCAST, className);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.jvmVarName, classSig);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        jType);
        }
    }

    private void generateGetFrame(BIRVarToJVMIndexMap indexMap, int localVarOffset, MethodVisitor mv) {
        int frameVarIndex = indexMap.addIfNotExists("frame", symbolTable.stringType);
        mv.visitVarInsn(ASTORE, frameVarIndex);
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, STACK_FRAMES);
        mv.visitVarInsn(ALOAD, frameVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STACK, "push", PASS_OBJECT_RETURN_OBJECT, false);
        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, RESUME_INDEX, "I");
    }

    private void createLocalVariableTable(BIRFunction func, BIRVarToJVMIndexMap indexMap, int localVarOffset,
                                          MethodVisitor mv, Label methodStartLabel, LabelGenerator labelGen,
                                          Label methodEndLabel, boolean isObjectMethodSplit) {
        String funcName = func.name.value;
        // Add strand variable to LVT
        mv.visitLocalVariable(STRAND_LOCAL_VARIABLE_NAME, GET_STRAND, null, methodStartLabel, methodEndLabel,
                localVarOffset);
        // Add self to the LVT
        if (func.receiver != null) {
            int selfIndex = isObjectMethodSplit ? 1 : 0;
            mv.visitLocalVariable(OBJECT_SELF_INSTANCE, GET_BOBJECT, null, methodStartLabel, methodEndLabel, selfIndex);
        }
        for (int i = localVarOffset; i < func.localVars.size(); i++) {
            BIRVariableDcl localVar = func.localVars.get(i);
            Label startLabel = methodStartLabel;
            Label endLabel = methodEndLabel;
            if (!isValidArg(localVar)) {
                continue;
            }
            // local vars have visible range information
            if (localVar.kind == VarKind.LOCAL) {
                if (localVar.startBB != null) {
                    startLabel = labelGen.getLabel(funcName + SCOPE_PREFIX + localVar.insScope.id);
                }
                if (localVar.endBB != null) {
                    endLabel = labelGen.getLabel(funcName + localVar.endBB.id.value + "beforeTerm");
                }
            }
            String metaVarName = localVar.metaVarName;
            if (isCompilerAddedVars(metaVarName)) {
                mv.visitLocalVariable(metaVarName, getJVMTypeSign(localVar.type), null,
                        startLabel, endLabel,
                        indexMap.addIfNotExists(localVar.name.value, localVar.type));
            }
        }
    }

    private boolean isValidArg(BIRVariableDcl localVar) {
        boolean localArg = localVar.kind == VarKind.LOCAL || localVar.kind == VarKind.ARG;
        boolean synArg = JvmCodeGenUtil.getImpliedType(localVar.type).tag == TypeTags.BOOLEAN &&
                localVar.name.value.startsWith("%syn");
        boolean lambdaMapArg = localVar.metaVarName != null && localVar.metaVarName.startsWith("$map$block$") &&
                localVar.kind == VarKind.SYNTHETIC;
        return (localArg && !synArg) || lambdaMapArg;
    }

    private boolean isCompilerAddedVars(String metaVarName) {
        return metaVarName != null && !"".equals(metaVarName) &&
                // filter out compiler added vars
                !((metaVarName.startsWith("$") && metaVarName.endsWith("$"))
                        || (metaVarName.startsWith("$$") && metaVarName.endsWith("$$"))
                        || metaVarName.startsWith("_$$_"));
    }

    private String getJVMTypeSign(BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return GET_STRING;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return GET_XML;
        } else if (TypeTags.REGEXP == bType.tag) {
            return GET_REGEXP;
        }

        String jvmType;
        switch (bType.tag) {
            case TypeTags.BYTE:
                jvmType = "I";
                break;
            case TypeTags.FLOAT:
                jvmType = "D";
                break;
            case TypeTags.BOOLEAN:
                jvmType = "Z";
                break;
            case TypeTags.DECIMAL:
                jvmType = GET_BDECIMAL;
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                jvmType = GET_MAP_VALUE;
                break;
            case TypeTags.STREAM:
                jvmType = GET_STREAM_VALUE;
                break;
            case TypeTags.TABLE:
                jvmType = GET_TABLE_VALUE;
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                jvmType = GET_ARRAY_VALUE;
                break;
            case TypeTags.OBJECT:
                jvmType = GET_BOBJECT;
                break;
            case TypeTags.ERROR:
                jvmType = GET_ERROR_VALUE;
                break;
            case TypeTags.FUTURE:
                jvmType = GET_FUTURE_VALUE;
                break;
            case TypeTags.INVOKABLE:
                jvmType = GET_FUNCTION_POINTER;
                break;
            case TypeTags.HANDLE:
                jvmType = GET_HANDLE_VALUE;
                break;
            case TypeTags.TYPEDESC:
                jvmType = GET_TYPEDESC;
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                jvmType = GET_OBJECT;
                break;
            case JTypeTags.JTYPE:
                jvmType = InteropMethodGen.getJTypeSignature((JType) bType);
                break;
            default:
                throw new BLangCompilerException("JVM code generation is not supported for type " +
                        bType);
        }

        return jvmType;
    }
}
