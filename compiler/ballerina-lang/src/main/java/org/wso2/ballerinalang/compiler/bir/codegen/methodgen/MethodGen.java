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
import org.wso2.ballerinalang.compiler.bir.codegen.model.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.SCOPE_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.*;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.*;

/**
 * BIR function to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class MethodGen {

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
        if (isObjectMethodSplit) {
            desc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType, moduleClassName);
        } else {
            desc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType);
        }
        MethodVisitor mv = cw.visitMethod(access, funcName, desc, null, null);
        mv.visitCode();

        visitStartFunction(module.packageID, funcName, mv);

        Label methodStartLabel = new Label();
        mv.visitLabel(methodStartLabel);

        genLocalVars(indexMap, mv, func.localVars);

        int returnVarRefIndex = getReturnVarRefIndex(func, indexMap, retType, mv);
        int channelMapVarIndex = getWorkerChannelMapVarIndex(func, indexMap, mv);
        List<BIRNode.ChannelDetails> sendWorkerChannels = new ArrayList<>();
        List<BIRNode.ChannelDetails> receiveWorkerChannels = new ArrayList<>();
        for (BIRNode.ChannelDetails workerChannel : func.workerChannels) {
            if (workerChannel.send) {
                sendWorkerChannels.add(workerChannel);
            } else {
                receiveWorkerChannels.add(workerChannel);
            }
        }
        int sendWorkerChannelNamesVar = getSendWorkerChannelNamesVarIndex(func, indexMap, mv, sendWorkerChannels);
        int receiveWorkerChannelNamesVar = getReceiveWorkerChannelNamesVarIndex(func, indexMap, mv,
                receiveWorkerChannels);
        LabelGenerator labelGen = new LabelGenerator();

        // handle module start and init specific logic
        handleDependantModuleForInit(mv, module.packageID, funcName);
        handleParentModuleStart(mv, module.packageID, funcName);
        Label tryLabel = new Label();
        Label catchLabel = new Label();
        Label handleThrowableLabel = new Label();
        if ((func.flags & Flags.WORKER) == Flags.WORKER && func.workerChannels.length > 0) {
            mv.visitTryCatchBlock(tryLabel, catchLabel, handleThrowableLabel, THROWABLE);
            mv.visitLabel(tryLabel);
        }

        JvmInstructionGen instGen = new JvmInstructionGen(mv, indexMap, module.packageID, jvmPackageGen, jvmTypeGen,
                jvmCastGen, jvmConstantsGen, asyncDataCollector, types);
        JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instGen);
        JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, module.packageID, instGen,
                jvmPackageGen, jvmTypeGen, jvmCastGen, jvmConstantsGen, asyncDataCollector);

        generateBasicBlocks(mv, labelGen, errorGen, instGen, termGen, func, returnVarRefIndex, channelMapVarIndex,
                localVarOffset, module, attachedType, sendWorkerChannelNamesVar, receiveWorkerChannelNamesVar);

        if ((func.flags & Flags.WORKER) == Flags.WORKER && func.workerChannels.length > 0) {
            mv.visitLabel(catchLabel);
            Label label3 = new Label();
            mv.visitJumpInsn(GOTO, label3);
            mv.visitLabel(handleThrowableLabel);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, localVarOffset);
            mv.visitVarInsn(ALOAD, sendWorkerChannelNamesVar);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_WORKER_PANIC_METHOD, HANDLE_WORKER_PANIC, false);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ATHROW);
            mv.visitLabel(label3);
        }
        termGen.genReturnTerm(returnVarRefIndex, func, localVarOffset, sendWorkerChannelNamesVar,
                receiveWorkerChannelNamesVar);
        Label methodEndLabel = new Label();
        mv.visitLabel(methodEndLabel);



        // Create Local Variable Table
        createLocalVariableTable(func, indexMap, localVarOffset, mv, methodStartLabel, labelGen, methodEndLabel,
                isObjectMethodSplit);


        JvmCodeGenUtil.visitMaxStackForMethod(mv, funcName, moduleClassName);
        mv.visitEnd();
    }

    private void handleDependantModuleForInit(MethodVisitor mv, PackageID packageID, String funcName) {
        if (isModuleInitFunction(funcName)) {
            String moduleClass = JvmCodeGenUtil.getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME);
            mv.visitFieldInsn(GETSTATIC, moduleClass, NO_OF_DEPENDANT_MODULES, "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitFieldInsn(PUTSTATIC, moduleClass, NO_OF_DEPENDANT_MODULES, "I");

            Label labelIf = new Label();
            mv.visitFieldInsn(GETSTATIC, moduleClass, NO_OF_DEPENDANT_MODULES, "I");
            mv.visitInsn(ICONST_1);
            mv.visitJumpInsn(IF_ICMPEQ, labelIf);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitLabel(labelIf);
        }
    }

    private void handleParentModuleStart(MethodVisitor mv, PackageID packageID, String funcName) {
        if (isModuleStartFunction(funcName)) {
            String moduleClass = JvmCodeGenUtil.getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME);
            mv.visitFieldInsn(GETSTATIC, moduleClass, PARENT_MODULE_START_ATTEMPTED, "Z");
            Label labelIf = new Label();
            mv.visitJumpInsn(IFEQ, labelIf);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitLabel(labelIf);
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC, moduleClass, PARENT_MODULE_START_ATTEMPTED, "Z");
        }
    }

    private BType getReturnType(BIRFunction func) {
        BType retType = func.type.retType;
        if (JvmCodeGenUtil.isExternFunc(func) && Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = JvmCodeGenUtil.UNIFIER.build(func.type.retType);
        }
        return retType;
    }

    private void visitStartFunction(PackageID packageID, String funcName, MethodVisitor mv) {
        if (!isStartFunction(funcName)) {
            return;
        }
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, JvmCodeGenUtil.getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME),
                MODULE_START_ATTEMPTED, "Z");
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
        BIRVariableDcl varDcl = func.localVars.getFirst();
        int returnVarRefIndex = indexMap.addIfNotExists(varDcl.name.value, varDcl.type);
        genDefaultValue(mv, retType, returnVarRefIndex);
        return returnVarRefIndex;
    }

    private int getWorkerThrowableVarIndex(BIRFunction func, BIRVarToJVMIndexMap indexMap) {
        if (!func.hasWorkers) {
            return -1;
        }
        return indexMap.addIfNotExists(WORKER_THROWABLE_VAR_NAME, symbolTable.anyType);
    }

    private int getWorkerChannelMapVarIndex(BIRFunction func, BIRVarToJVMIndexMap indexMap, MethodVisitor mv) {
        if (!func.hasWorkers) {
            return -1;
        }
        int channelMapVarIndex = indexMap.addIfNotExists(WORKER_CHANNEL_MAP_VAR_NAME, symbolTable.anyType);
        mv.visitTypeInsn(NEW, WORKER_CHANNEL_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, WORKER_CHANNEL_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitVarInsn(ASTORE, channelMapVarIndex);
        return channelMapVarIndex;
    }


    private int getSendWorkerChannelNamesVarIndex(BIRFunction func, BIRVarToJVMIndexMap indexMap, MethodVisitor mv,
                                                  List<BIRNode.ChannelDetails> sendWorkerChannels) {
        if (func.workerChannels.length == 0) {
            return -1;
        }
        int sendWorkerChannelNamesVar = indexMap.addIfNotExists(SEND_WORKER_CHANNEL_NAMES_VAR_NAME,
                symbolTable.anyType);
        if (sendWorkerChannels.isEmpty()) {
            mv.visitInsn(ACONST_NULL);
        } else {
            int channelSize = sendWorkerChannels.size();
            mv.visitIntInsn(BIPUSH, channelSize);
            mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
            int count = 0;
            for (BIRNode.ChannelDetails sendWorkerChannel : sendWorkerChannels) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, count++);
                mv.visitLdcInsn(sendWorkerChannel.name);
                mv.visitInsn(AASTORE);
            }
        }
        mv.visitVarInsn(ASTORE, sendWorkerChannelNamesVar);
        return sendWorkerChannelNamesVar;
    }

    private int getReceiveWorkerChannelNamesVarIndex(BIRFunction func, BIRVarToJVMIndexMap indexMap, MethodVisitor mv,
                                                     List<BIRNode.ChannelDetails> receiveWorkerChannels) {
        if (func.workerChannels.length == 0) {
            return -1;
        }
        int receiveWorkerChannelNamesVar = indexMap.addIfNotExists(RECEIVE_WORKER_CHANNEL_NAMES_VAR_NAME,
                symbolTable.anyType);
        if (receiveWorkerChannels.isEmpty()) {
            mv.visitInsn(ACONST_NULL);
        } else {
            int channelSize = receiveWorkerChannels.size();
            mv.visitIntInsn(BIPUSH, channelSize);
            mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
            int count = 0;
            for (BIRNode.ChannelDetails sendWorkerChannel : receiveWorkerChannels) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, count++);
                mv.visitLdcInsn(sendWorkerChannel.name);
                mv.visitInsn(AASTORE);
            }
        }
        mv.visitVarInsn(ASTORE, receiveWorkerChannelNamesVar);
        return receiveWorkerChannelNamesVar;
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
            case TypeTags.BYTE, TypeTags.BOOLEAN -> {
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ISTORE, index);
            }
            case TypeTags.FLOAT -> {
                mv.visitInsn(DCONST_0);
                mv.visitVarInsn(DSTORE, index);
            }
            case TypeTags.MAP, TypeTags.ARRAY, TypeTags.STREAM, TypeTags.TABLE, TypeTags.ERROR, TypeTags.NIL,
                 TypeTags.NEVER, TypeTags.ANY, TypeTags.ANYDATA, TypeTags.OBJECT, TypeTags.CHAR_STRING,
                 TypeTags.DECIMAL, TypeTags.UNION, TypeTags.RECORD, TypeTags.TUPLE, TypeTags.FUTURE, TypeTags.JSON,
                 TypeTags.INVOKABLE, TypeTags.FINITE, TypeTags.HANDLE, TypeTags.TYPEDESC, TypeTags.READONLY,
                 TypeTags.REGEXP -> {
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, index);
            }
            case JTypeTags.JTYPE -> genJDefaultValue(mv, (JType) bType, index);
            default -> throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    private void genJDefaultValue(MethodVisitor mv, JType jType, int index) {
        switch (jType.jTag) {
            case JTypeTags.JBYTE, JTypeTags.JCHAR, JTypeTags.JSHORT, JTypeTags.JINT, JTypeTags.JBOOLEAN -> {
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ISTORE, index);
            }
            case JTypeTags.JLONG -> {
                mv.visitInsn(LCONST_0);
                mv.visitVarInsn(LSTORE, index);
            }
            case JTypeTags.JFLOAT -> {
                mv.visitInsn(FCONST_0);
                mv.visitVarInsn(FSTORE, index);
            }
            case JTypeTags.JDOUBLE -> {
                mv.visitInsn(DCONST_0);
                mv.visitVarInsn(DSTORE, index);
            }
            case JTypeTags.JARRAY, JTypeTags.JREF -> {
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, index);
            }
            default -> throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    jType);
        }
    }

    void generateBasicBlocks(MethodVisitor mv, LabelGenerator labelGen, JvmErrorGen errorGen, JvmInstructionGen instGen,
                             JvmTerminatorGen termGen, BIRFunction func, int returnVarRefIndex,
                             int channelMapVarIndex, int localVarOffset, BIRPackage module, BType attachedType,
                             int sendWorkerChannelNamesVar, int receiveWorkerChannelNamesVar) {

        String funcName = func.name.value;
        BirScope lastScope = null;
        Set<BirScope> visitedScopesSet = new HashSet<>();
        for (int i = 0; i < func.basicBlocks.size(); i++) {
            BIRBasicBlock bb = func.basicBlocks.get(i);
            // create jvm label
            Label bbLabel = labelGen.getLabel(funcName + bb.id.value);
            mv.visitLabel(bbLabel);

            // generate instructions
            lastScope = JvmCodeGenUtil.getLastScopeFromBBInsGen(mv, labelGen, instGen, localVarOffset, funcName, bb,
                    visitedScopesSet, lastScope);
            Label bbEndLabel = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
            mv.visitLabel(bbEndLabel);
            BIRTerminator terminator = bb.terminator;
            processTerminator(mv, func, module, funcName, terminator);
            termGen.genTerminator(terminator, func, funcName, localVarOffset, returnVarRefIndex, attachedType,
                    channelMapVarIndex, sendWorkerChannelNamesVar, receiveWorkerChannelNamesVar);
            lastScope = JvmCodeGenUtil.getLastScopeFromTerminator(mv, bb, funcName, labelGen, lastScope,
                    visitedScopesSet);
            errorGen.generateTryCatch(func, funcName, bb, termGen, labelGen, localVarOffset,
                    sendWorkerChannelNamesVar, receiveWorkerChannelNamesVar);
            BIRBasicBlock thenBB = terminator.thenBB;
            if (thenBB != null) {
                JvmCodeGenUtil.genStrandResume(mv, labelGen, thenBB, funcName, localVarOffset);
            }
        }
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
        if (isStartFunction(funcName) && terminator.kind == InstructionKind.RETURN) {
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC, JvmCodeGenUtil.getModuleLevelClassName(module.packageID,
                            MODULE_INIT_CLASS_NAME),
                    MODULE_STARTED, "Z");
        }
    }

    private boolean isModuleTestInitFunction(BIRFunction func) {
        return func.name.value.equals(MethodGenUtils.encodeModuleSpecialFuncName(".<testinit>"));
    }

    private boolean isStartFunction(String functionName) {
        return functionName.equals(MethodGenUtils.encodeModuleSpecialFuncName(MethodGenUtils.START_FUNCTION_SUFFIX));
    }

    private boolean isModuleInitFunction(String functionName) {
        return functionName.equals(MethodGenUtils.encodeModuleSpecialFuncName(JvmConstants.MODULE_INIT_METHOD));
    }

    private boolean isModuleStartFunction(String functionName) {
        return functionName.equals(MethodGenUtils.encodeModuleSpecialFuncName(JvmConstants.MODULE_START_METHOD));
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
                    startLabel = labelGen.getLabel(funcName + SCOPE_PREFIX + localVar.insScope.id());
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
        return metaVarName != null && !metaVarName.isEmpty() &&
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

        return switch (bType.tag) {
            case TypeTags.BYTE -> "I";
            case TypeTags.FLOAT -> "D";
            case TypeTags.BOOLEAN -> "Z";
            case TypeTags.DECIMAL -> GET_BDECIMAL;
            case TypeTags.MAP, TypeTags.RECORD -> GET_MAP_VALUE;
            case TypeTags.STREAM -> GET_STREAM_VALUE;
            case TypeTags.TABLE -> GET_TABLE_VALUE;
            case TypeTags.ARRAY, TypeTags.TUPLE -> GET_ARRAY_VALUE;
            case TypeTags.OBJECT -> GET_BOBJECT;
            case TypeTags.ERROR -> GET_ERROR_VALUE;
            case TypeTags.FUTURE -> GET_FUTURE_VALUE;
            case TypeTags.INVOKABLE -> GET_FUNCTION_POINTER;
            case TypeTags.HANDLE -> GET_HANDLE_VALUE;
            case TypeTags.TYPEDESC -> GET_TYPEDESC;
            case TypeTags.NIL, TypeTags.NEVER, TypeTags.ANY, TypeTags.ANYDATA, TypeTags.UNION,
                 TypeTags.JSON, TypeTags.FINITE, TypeTags.READONLY -> GET_OBJECT;
            case JTypeTags.JTYPE -> InteropMethodGen.getJTypeSignature((JType) bType);
            default -> throw new BLangCompilerException("JVM code generation is not supported for type " +
                    bType);
        };
    }
}
