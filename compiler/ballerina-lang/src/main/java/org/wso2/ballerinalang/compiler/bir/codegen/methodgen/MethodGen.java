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

import org.ballerinalang.compiler.BLangCompilerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
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
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;

/**
 * BIR function to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class MethodGen {

    private static final String STATE = "state";
    private static final String RESUME_INDEX = "resumeIndex";
    private final JvmPackageGen jvmPackageGen;
    private final SymbolTable symbolTable;

    public MethodGen(JvmPackageGen jvmPackageGen) {
        this.jvmPackageGen = jvmPackageGen;
        this.symbolTable = jvmPackageGen.symbolTable;
    }

    public void generateMethod(BIRFunction birFunc, ClassWriter cw, BIRPackage birModule, BType attachedType,
                               String moduleClassName, AsyncDataCollector asyncDataCollector) {
        if (JvmCodeGenUtil.isExternFunc(birFunc)) {
            ExternalMethodGen.genJMethodForBExternalFunc(birFunc, cw, birModule, attachedType, this, jvmPackageGen,
                                                         moduleClassName, asyncDataCollector);
        } else {
            genJMethodForBFunc(birFunc, cw, birModule, moduleClassName, attachedType,
                               asyncDataCollector);
        }
    }

    public void genJMethodForBFunc(BIRFunction func, ClassWriter cw, BIRPackage module, String moduleClassName,
                                   BType attachedType, AsyncDataCollector asyncDataCollector) {

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        indexMap.addIfNotExists(STRAND, symbolTable.stringType);

        // generate method desc
        int access = Opcodes.ACC_PUBLIC;
        int localVarOffset;
        if (attachedType != null) {
            localVarOffset = 1;
            // add the self as the first local var
            indexMap.addIfNotExists("self", symbolTable.anyType);
        } else {
            localVarOffset = 0;
            access += ACC_STATIC;
        }

        String funcName = func.name.value;
        BType retType = getReturnType(func);
        String desc = JvmCodeGenUtil.getMethodDesc(func.type.paramTypes, retType);
        MethodVisitor mv = cw.visitMethod(access, funcName, desc, null, null);
        mv.visitCode();

        visitModuleStartFunction(module, funcName, mv);

        Label methodStartLabel = new Label();
        mv.visitLabel(methodStartLabel);

        // set channel details to strand.
        setChannelDetailsToStrand(func, localVarOffset, mv);

        // panic if this strand is cancelled
        checkStrandCancelled(mv, localVarOffset);

        genLocalVars(indexMap, mv, func.localVars);

        int returnVarRefIndex = getReturnVarRefIndex(func, indexMap, retType, mv);
        int stateVarIndex = getStateVarIndex(indexMap, mv);

        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, RESUME_INDEX, "I");

        LabelGenerator labelGen = new LabelGenerator();
        Label resumeLabel = labelGen.getLabel(funcName + "resume");
        mv.visitJumpInsn(IFGT, resumeLabel);

        Label varinitLabel = labelGen.getLabel(funcName + "varinit");
        mv.visitLabel(varinitLabel);

        // process basic blocks
        List<Label> labels = new ArrayList<>();
        List<Integer> states = new ArrayList<>();

        addCasesForBasicBlocks(func, funcName, labelGen, labels, states);

        JvmInstructionGen instGen = new JvmInstructionGen(mv, indexMap, module, jvmPackageGen);
        JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instGen);
        JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, module, instGen,
                                                        jvmPackageGen);

        mv.visitVarInsn(ILOAD, stateVarIndex);
        Label yieldLable = labelGen.getLabel(funcName + "yield");
        mv.visitLookupSwitchInsn(yieldLable, toIntArray(states), labels.toArray(new Label[0]));

        generateBasicBlocks(mv, labelGen, errorGen, instGen, termGen, func, returnVarRefIndex,
                            stateVarIndex, localVarOffset, module, attachedType, moduleClassName, asyncDataCollector);
        mv.visitLabel(resumeLabel);
        String frameName = MethodGenUtils.getFrameClassName(JvmCodeGenUtil.getPackageName(module), funcName,
                                                            attachedType);
        genGetFrameOnResumeIndex(localVarOffset, mv, frameName);

        generateFrameClassFieldLoad(func.localVars, mv, indexMap, frameName);
        mv.visitFieldInsn(GETFIELD, frameName, STATE, "I");
        mv.visitVarInsn(ISTORE, stateVarIndex);
        mv.visitJumpInsn(GOTO, varinitLabel);

        mv.visitLabel(yieldLable);
        mv.visitTypeInsn(NEW, frameName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, frameName, JVM_INIT_METHOD, "()V", false);

        generateFrameClassFieldUpdate(func.localVars, mv, indexMap, frameName);

        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, stateVarIndex);
        mv.visitFieldInsn(PUTFIELD, frameName, STATE, "I");

        generateGetFrame(indexMap, localVarOffset, mv);

        Label methodEndLabel = new Label();
        mv.visitLabel(methodEndLabel);
        termGen.genReturnTerm(returnVarRefIndex, func);

        // Create Local Variable Table
        createLocalVariableTable(func, indexMap, localVarOffset, mv, methodStartLabel, labelGen, methodEndLabel);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private BType getReturnType(BIRFunction func) {
        BType retType = func.type.retType;
        if (JvmCodeGenUtil.isExternFunc(func) && Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = JvmCodeGenUtil.TYPE_BUILDER.build(func.type.retType);
        }
        return retType;
    }

    private void visitModuleStartFunction(BIRPackage module, String funcName, MethodVisitor mv) {
        if (!isModuleStartFunction(funcName)) {
            return;
        }
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, JvmCodeGenUtil.getModuleLevelClassName(module, MODULE_INIT_CLASS_NAME),
                          MODULE_START_ATTEMPTED, "Z");
    }

    private void setChannelDetailsToStrand(BIRFunction func, int localVarOffset, MethodVisitor mv) {
        // these channel info is required to notify datachannels, when there is a panic
        // we cannot set this during strand creation, because function call do not have this info.
        if (func.workerChannels.length <= 0) {
            return;
        }
        mv.visitVarInsn(ALOAD, localVarOffset);
        JvmCodeGenUtil.loadChannelDetails(mv, Arrays.asList(func.workerChannels));
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "updateChannelDetails",
                           String.format("([L%s;)V", CHANNEL_DETAILS), false);
    }

    private void checkStrandCancelled(MethodVisitor mv, int localVarOffset) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "cancel", "Z");
        Label notCancelledLabel = new Label();
        mv.visitJumpInsn(IFEQ, notCancelledLabel);
        mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, "createCancelledFutureError",
                           String.format("()L%s;", ERROR_VALUE), false);
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
            case TypeTags.INTERSECTION:
            case TypeTags.RECORD:
            case TypeTags.TUPLE:
            case TypeTags.FUTURE:
            case TypeTags.JSON:
            case TypeTags.INVOKABLE:
            case TypeTags.FINITE:
            case TypeTags.HANDLE:
            case TypeTags.TYPEDESC:
            case TypeTags.READONLY:
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, index);
                break;
            case JTypeTags.JTYPE:
                genJDefaultValue(mv, (JType) bType, index);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         String.format("%s", bType));
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
                                                         String.format("%s", jType));
        }
    }

    private int getStateVarIndex(BIRVarToJVMIndexMap indexMap, MethodVisitor mv) {
        int stateVarIndex = indexMap.addIfNotExists(STATE, symbolTable.stringType);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, stateVarIndex);
        return stateVarIndex;
    }

    private void addCasesForBasicBlocks(BIRFunction func, String funcName, LabelGenerator labelGen, List<Label> lables,
                                        List<Integer> states) {
        int caseIndex = 0;
        for (int i = 0; i < func.basicBlocks.size(); i++) {
            BIRBasicBlock bb = func.basicBlocks.get(i);
            if (i == 0) {
                lables.add(caseIndex, labelGen.getLabel(funcName + bb.id.value));
                states.add(caseIndex, caseIndex);
                caseIndex += 1;
            }
            lables.add(caseIndex, labelGen.getLabel(funcName + bb.id.value + "beforeTerm"));
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

    void generateBasicBlocks(MethodVisitor mv, LabelGenerator labelGen, JvmErrorGen errorGen,
                             JvmInstructionGen instGen, JvmTerminatorGen termGen, BIRFunction func,
                             int returnVarRefIndex, int stateVarIndex, int localVarOffset, BIRPackage module,
                             BType attachedType, String moduleClassName,
                             AsyncDataCollector asyncDataCollector) {

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
                    .getLastScopeFromBBInsGen(mv, labelGen, instGen, localVarOffset, asyncDataCollector, funcName, bb,
                                              visitedScopesSet, lastScope);

            Label bbEndLabel = labelGen.getLabel(funcName + bb.id.value + "beforeTerm");
            mv.visitLabel(bbEndLabel);

            BIRTerminator terminator = bb.terminator;
            pushShort(mv, stateVarIndex, caseIndex);
            caseIndex += 1;

            processTerminator(mv, func, module, funcName, terminator, localVarOffset);
            termGen.genTerminator(terminator, moduleClassName, func, funcName, localVarOffset, returnVarRefIndex,
                                  attachedType, asyncDataCollector);

            errorGen.generateTryCatch(func, funcName, bb, termGen, labelGen);

            BIRBasicBlock thenBB = terminator.thenBB;
            if (thenBB != null) {
                JvmCodeGenUtil.genYieldCheck(mv, termGen.getLabelGenerator(), thenBB, funcName, localVarOffset);
            }
        }
    }

    private void pushShort(MethodVisitor mv, int stateVarIndex, int caseIndex) {
        // SIPUSH range is (-32768 to 32767) so if the state index goes beyond that, need to use visitLdcInsn
        mv.visitIntInsn(SIPUSH, caseIndex);
        mv.visitVarInsn(ISTORE, stateVarIndex);
    }

    private void processTerminator(MethodVisitor mv, BIRFunction func, BIRPackage module, String funcName,
                                   BIRTerminator terminator, int localVarOffset) {
        JvmCodeGenUtil.generateDiagnosticPos(terminator.pos, mv);
        if ((MethodGenUtils.isModuleInitFunction(func) || isModuleTestInitFunction(func)) &&
                terminator instanceof Return) {
            generateAnnotLoad(mv, module.typeDefs, JvmCodeGenUtil.getPackageName(module), localVarOffset);
        }
        //set module start success to true for $_init class
        if (isModuleStartFunction(funcName) && terminator.kind == InstructionKind.RETURN) {
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC,
                              JvmCodeGenUtil.getModuleLevelClassName(module, MODULE_INIT_CLASS_NAME),
                              MODULE_STARTED, "Z");
        }
    }

    private boolean isModuleTestInitFunction(BIRFunction func) {
        return func.name.value.equals(
                MethodGenUtils
                        .encodeModuleSpecialFuncName(".<testinit>"));
    }

    private void generateAnnotLoad(MethodVisitor mv, List<BIRTypeDefinition> typeDefs, String pkgName,
                                   int localVarOffset) {
        String typePkgName = ".";
        if (!"".equals(pkgName)) {
            typePkgName = pkgName;
        }

        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            if (optionalTypeDef.isBuiltin) {
                continue;
            }
            BType bType = optionalTypeDef.type;
            if ((bType.flags & Flags.OBJECT_CTOR) == Flags.OBJECT_CTOR) {
                // Annotations for object ctors are populated at object init site.
                continue;
            }

            if (bType.tag != TypeTags.FINITE) {
                loadAnnots(mv, typePkgName, optionalTypeDef, localVarOffset);
            }

        }
    }

    private void loadAnnots(MethodVisitor mv, String pkgName, BIRTypeDefinition typeDef, int localVarOffset) {
        String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                jvmPackageGen.lookupGlobalVarClassName(pkgName, ANNOTATION_MAP_NAME);
        mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));
        loadLocalType(mv, typeDef);
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKESTATIC, String.format("%s", ANNOTATION_UTILS), "processAnnotations",
                           String.format("(L%s;L%s;L%s;)V", MAP_VALUE, TYPE, STRAND_CLASS), false);
    }

    void loadLocalType(MethodVisitor mv, BIRTypeDefinition typeDefinition) {
        JvmTypeGen.loadType(mv, typeDefinition.type);
    }

    private boolean isModuleStartFunction(String functionName) {
        return functionName
                .equals(MethodGenUtils.encodeModuleSpecialFuncName(MethodGenUtils.START_FUNCTION_SUFFIX));
    }

    private void genGetFrameOnResumeIndex(int localVarOffset, MethodVisitor mv, String frameName) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, "[Ljava/lang/Object;");
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitInsn(DUP_X1);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, frameName);
    }

    private void generateFrameClassFieldLoad(List<BIRVariableDcl> localVars, MethodVisitor mv,
                                             BIRVarToJVMIndexMap indexMap, String frameName) {
        for (BIRVariableDcl localVar : localVars) {
            BType bType = localVar.type;
            int index = indexMap.addIfNotExists(localVar.name.value, bType);
            mv.visitInsn(DUP);

            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "J");
                mv.visitVarInsn(LSTORE, index);
            } else if (TypeTags.isStringTypeTag(bType.tag)) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", JvmConstants.B_STRING_VALUE));
                mv.visitVarInsn(ASTORE, index);
            } else if (TypeTags.isXMLTypeTag(bType.tag)) {
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", XML_VALUE));
                mv.visitVarInsn(ASTORE, index);
            } else {
                generateFrameClassFieldLoadByTypeTag(mv, frameName, localVar, index, bType);
            }
        }

    }

    private void generateFrameClassFieldLoadByTypeTag(MethodVisitor mv, String frameName, BIRVariableDcl localVar,
                                                      int index, BType bType) {
        switch (bType.tag) {
            case TypeTags.BYTE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "I");
                mv.visitVarInsn(ISTORE, index);
                break;
            case TypeTags.FLOAT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "D");
                mv.visitVarInsn(DSTORE, index);
                break;
            case TypeTags.DECIMAL:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", DECIMAL_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.BOOLEAN:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "Z");
                mv.visitVarInsn(ISTORE, index);
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", MAP_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.STREAM:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", STREAM_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.TABLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", TABLE_VALUE_IMPL));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", ARRAY_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.OBJECT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", B_OBJECT));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.ERROR:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", ERROR_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.FUTURE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", FUTURE_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.INVOKABLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", FUNCTION_POINTER));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.TYPEDESC:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", TYPEDESC_VALUE));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", OBJECT));
                mv.visitVarInsn(ASTORE, index);
                break;
            case TypeTags.HANDLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", HANDLE_VALUE));
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
        JType jType = (JType) localVar.type;

        switch (jType.jTag) {
            case JTypeTags.JBYTE:
            case JTypeTags.JCHAR:
            case JTypeTags.JSHORT:
            case JTypeTags.JINT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "I");
                mv.visitVarInsn(ISTORE, index);
                break;
            case JTypeTags.JLONG:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "J");
                mv.visitVarInsn(LSTORE, index);
                break;
            case JTypeTags.JFLOAT:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "F");
                mv.visitVarInsn(FSTORE, index);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "D");
                mv.visitVarInsn(DSTORE, index);
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"), "Z");
                mv.visitVarInsn(ISTORE, index);
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                mv.visitFieldInsn(GETFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  InteropMethodGen.getJTypeSignature(jType));
                mv.visitVarInsn(ASTORE, index);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         String.format("%s", jType));
        }
    }

    private void generateFrameClassFieldUpdate(List<BIRVariableDcl> localVars, MethodVisitor mv,
                                               BIRVarToJVMIndexMap indexMap, String frameName) {
        for (BIRVariableDcl localVar : localVars) {
            BType bType = localVar.type;
            int index = indexMap.addIfNotExists(localVar.name.value, bType);
            mv.visitInsn(DUP);

            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                mv.visitVarInsn(LLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "J");
            } else if (TypeTags.isStringTypeTag(bType.tag)) {
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", JvmConstants.B_STRING_VALUE));
            } else if (TypeTags.isXMLTypeTag(bType.tag)) {
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", XML_VALUE));
            } else {
                generateFrameClassFieldUpdateByTypeTag(mv, frameName, localVar, index, bType);
            }
        }
    }

    private void generateFrameClassFieldUpdateByTypeTag(MethodVisitor mv, String frameName, BIRVariableDcl localVar,
                                                        int index, BType bType) {
        switch (bType.tag) {
            case TypeTags.BYTE:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "I");
                break;
            case TypeTags.FLOAT:
                mv.visitVarInsn(DLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "D");
                break;
            case TypeTags.DECIMAL:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", DECIMAL_VALUE));
                break;
            case TypeTags.BOOLEAN:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "Z");
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", MAP_VALUE));
                break;
            case TypeTags.STREAM:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", STREAM_VALUE));
                break;
            case TypeTags.TABLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", TABLE_VALUE_IMPL));
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", ARRAY_VALUE));
                break;
            case TypeTags.ERROR:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", ERROR_VALUE));
                break;
            case TypeTags.FUTURE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", FUTURE_VALUE));
                break;
            case TypeTags.TYPEDESC:
                mv.visitVarInsn(ALOAD, index);
                mv.visitTypeInsn(CHECKCAST, TYPEDESC_VALUE);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", TYPEDESC_VALUE));
                break;
            case TypeTags.OBJECT:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", B_OBJECT));
                break;
            case TypeTags.INVOKABLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", FUNCTION_POINTER));
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", OBJECT));
                break;
            case TypeTags.HANDLE:
                mv.visitVarInsn(ALOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"),
                                  String.format("L%s;", HANDLE_VALUE));
                break;
            case JTypeTags.JTYPE:
                generateFrameClassJFieldUpdate(localVar, mv, index, frameName);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         String.format("%s", bType));
        }
    }

    private void generateFrameClassJFieldUpdate(BIRVariableDcl localVar, MethodVisitor mv,
                                                int index, String frameName) {
        JType jType = (JType) localVar.type;
        switch (jType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "B");
                break;
            case JTypeTags.JCHAR:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "C");
                break;
            case JTypeTags.JSHORT:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "S");
                break;
            case JTypeTags.JINT:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "I");
                break;
            case JTypeTags.JLONG:
                mv.visitVarInsn(LLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "J");
                break;
            case JTypeTags.JFLOAT:
                mv.visitVarInsn(FLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "F");
                break;
            case JTypeTags.JDOUBLE:
                mv.visitVarInsn(DLOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "D");
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitVarInsn(ILOAD, index);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), "Z");
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                String classSig = InteropMethodGen.getJTypeSignature(jType);
                String className = InteropMethodGen.getSignatureForJType(jType);
                mv.visitVarInsn(ALOAD, index);
                mv.visitTypeInsn(CHECKCAST, className);
                mv.visitFieldInsn(PUTFIELD, frameName, localVar.name.value.replace("%", "_"), classSig);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         String.format("%s", jType));
        }
    }

    private void generateGetFrame(BIRVarToJVMIndexMap indexMap, int localVarOffset, MethodVisitor mv) {
        int frameVarIndex = indexMap.addIfNotExists("frame", symbolTable.stringType);
        mv.visitVarInsn(ASTORE, frameVarIndex);
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, "[Ljava/lang/Object;");
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitInsn(DUP_X1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, RESUME_INDEX, "I");
        mv.visitVarInsn(ALOAD, frameVarIndex);
        mv.visitInsn(AASTORE);
    }

    private void createLocalVariableTable(BIRFunction func, BIRVarToJVMIndexMap indexMap, int localVarOffset,
                                          MethodVisitor mv, Label methodStartLabel, LabelGenerator labelGen,
                                          Label methodEndLabel) {
        String funcName = func.name.value;
        // Add strand variable to LVT
        mv.visitLocalVariable("__strand", String.format("L%s;", STRAND_CLASS), null, methodStartLabel, methodEndLabel,
                              localVarOffset);
        BIRBasicBlock endBB = func.basicBlocks.get(func.basicBlocks.size() - 1);
        for (int i = localVarOffset; i < func.localVars.size(); i++) {
            BIRVariableDcl localVar = func.localVars.get(i);
            Label startLabel = methodStartLabel;
            Label endLabel = methodEndLabel;
            if (!isLocalOrArg(localVar)) {
                continue;
            }
            // local vars have visible range information
            if (localVar.kind == VarKind.LOCAL) {
                if (localVar.startBB != null) {
                    startLabel = labelGen.getLabel(funcName + JvmCodeGenUtil.SCOPE_PREFIX + localVar.insScope.id);
                }
                if (localVar.endBB != null) {
                    endLabel = labelGen.getLabel(funcName + endBB.id.value + "beforeTerm");
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

    private boolean isLocalOrArg(BIRVariableDcl localVar) {
        boolean synArg = localVar.type.tag == TypeTags.BOOLEAN && localVar.name.value.startsWith("%syn");
        return !synArg && (localVar.kind == VarKind.LOCAL || localVar.kind == VarKind.ARG);
    }

    private boolean isCompilerAddedVars(String metaVarName) {
        return metaVarName != null && !"".equals(metaVarName) &&
                // filter out compiler added vars
                !((metaVarName.startsWith("$") && metaVarName.endsWith("$"))
                        || (metaVarName.startsWith("$$") && metaVarName.endsWith("$$"))
                        || metaVarName.startsWith("_$$_"));
    }

    private String getJVMTypeSign(BType bType) {
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return String.format("L%s;", STRING_VALUE);
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return String.format("L%s;", XML_VALUE);
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
                jvmType = String.format("L%s;", DECIMAL_VALUE);
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                jvmType = String.format("L%s;", MAP_VALUE);
                break;
            case TypeTags.STREAM:
                jvmType = String.format("L%s;", STREAM_VALUE);
                break;
            case TypeTags.TABLE:
                jvmType = String.format("L%s;", TABLE_VALUE_IMPL);
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                jvmType = String.format("L%s;", ARRAY_VALUE);
                break;
            case TypeTags.OBJECT:
                jvmType = String.format("L%s;", B_OBJECT);
                break;
            case TypeTags.ERROR:
                jvmType = String.format("L%s;", ERROR_VALUE);
                break;
            case TypeTags.FUTURE:
                jvmType = String.format("L%s;", FUTURE_VALUE);
                break;
            case TypeTags.INVOKABLE:
                jvmType = String.format("L%s;", FUNCTION_POINTER);
                break;
            case TypeTags.HANDLE:
                jvmType = String.format("L%s;", HANDLE_VALUE);
                break;
            case TypeTags.TYPEDESC:
                jvmType = String.format("L%s;", TYPEDESC_VALUE);
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                jvmType = String.format("L%s;", OBJECT);
                break;
            case JTypeTags.JTYPE:
                jvmType = InteropMethodGen.getJTypeSignature((JType) bType);
                break;
            default:
                throw new BLangCompilerException("JVM code generation is not supported for type " +
                        String.format("%s", bType));
        }

        return jvmType;
    }
}
