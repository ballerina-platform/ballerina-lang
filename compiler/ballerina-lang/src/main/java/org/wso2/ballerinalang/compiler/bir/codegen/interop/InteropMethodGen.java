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

package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.ballerinalang.compiler.BLangCompilerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.CASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SASTORE;
import static org.objectweb.asm.Opcodes.T_BOOLEAN;
import static org.objectweb.asm.Opcodes.T_CHAR;
import static org.objectweb.asm.Opcodes.T_DOUBLE;
import static org.objectweb.asm.Opcodes.T_FLOAT;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.T_LONG;
import static org.objectweb.asm.Opcodes.T_SHORT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateBToJCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ERROR_REASONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLANG_EXCEPTION_HELPER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WRAPPER_GEN_BB_ID_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.getNextDesugarBBId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.insertAndGetNextBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getFunctionWrapper;

/**
 * Interop related method generation class for JVM byte code generation.
 *
 * @since 1.2.0
 */
public class InteropMethodGen {

    static void genJFieldForInteropField(JFieldFunctionWrapper jFieldFuncWrapper,
                                         ClassWriter classWriter,
                                         BIRPackage birModule,
                                         JvmPackageGen jvmPackageGen,
                                         JvmMethodGen jvmMethodGen,
                                         String moduleClassName,
                                         AsyncDataCollector asyncDataCollector) {

        // Create a local variable for the strand
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        BIRVariableDcl strandVarDcl = new BIRVariableDcl(jvmPackageGen.symbolTable.stringType, new Name("$_strand_$"),
                null, VarKind.ARG);
        int strandParamIndex = indexMap.addToMapIfNotFoundAndGetIndex(strandVarDcl);

        // Generate method desc
        BIRFunction birFunc = jFieldFuncWrapper.func;
        BType retType = birFunc.type.retType;

        if (Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = JvmCodeGenUtil.TYPE_BUILDER.build(birFunc.type.retType);
        }

        String desc = JvmCodeGenUtil.getMethodDesc(birFunc.type.paramTypes, retType);
        int access = birFunc.receiver != null ? ACC_PUBLIC : ACC_PUBLIC + ACC_STATIC;
        MethodVisitor mv = classWriter.visitMethod(access, birFunc.name.value, desc, null, null);
        JvmInstructionGen instGen = new JvmInstructionGen(mv, indexMap, birModule, jvmPackageGen);
        JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instGen);
        LabelGenerator labelGen = new LabelGenerator();
        JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, birModule, instGen,
                jvmPackageGen);
        mv.visitCode();

        Label paramLoadLabel = labelGen.getLabel("param_load");
        mv.visitLabel(paramLoadLabel);
        mv.visitLineNumber(birFunc.pos.sLine, paramLoadLabel);

        // birFunc.localVars contains all the function parameters as well as added boolean parameters to indicate the
        //  availability of default values.
        // The following line cast localvars to function params. This is guaranteed not to fail.
        // Get a JVM method local variable index for the parameter
        List<BIRNode.BIRFunctionParameter> birFuncParams = new ArrayList<>();
        for (BIRVariableDcl birLocalVarOptional : birFunc.localVars) {
            if (birLocalVarOptional instanceof BIRNode.BIRFunctionParameter) {
                BIRNode.BIRFunctionParameter functionParameter = (BIRNode.BIRFunctionParameter) birLocalVarOptional;
                birFuncParams.add(functionParameter);
                indexMap.addToMapIfNotFoundAndGetIndex(functionParameter);
            }
        }

        // Generate if blocks to check and set default values to parameters
        int birFuncParamIndex = 0;
        for (BIRNode.BIRFunctionParameter birFuncParam : birFuncParams) {
            // Skip boolean function parameters to indicate the existence of default values
            if (birFuncParamIndex % 2 != 0 || !birFuncParam.hasDefaultExpr) {
                // Skip the loop if:
                //  1) This birFuncParamIndex had an odd value: indicates a generated boolean parameter
                //  2) This function param doesn't have a default value
                birFuncParamIndex += 1;
                continue;
            }

            // The following boolean parameter indicates the existence of a default value
            BIRNode.BIRFunctionParameter isDefaultValueExist = birFuncParams.get(birFuncParamIndex + 1);
            mv.visitVarInsn(ILOAD, indexMap.addToMapIfNotFoundAndGetIndex(isDefaultValueExist));

            // Gen the if not equal logic
            Label paramNextLabel = labelGen.getLabel(birFuncParam.name.value + "next");
            mv.visitJumpInsn(IFNE, paramNextLabel);

            List<BIRBasicBlock> basicBlocks = birFunc.parameters.get(birFuncParam);
            generateBasicBlocks(mv, basicBlocks, labelGen, errorGen, instGen, termGen, birFunc, moduleClassName,
                                asyncDataCollector);

            mv.visitLabel(paramNextLabel);

            birFuncParamIndex += 1;
        }

        JavaField jField = jFieldFuncWrapper.jField;
        JType jFieldType = JInterop.getJType(jField.getFieldType());

        // Load receiver which is the 0th parameter in the birFunc
        if (!jField.isStatic()) {
            int receiverLocalVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(birFuncParams.get(0));
            mv.visitVarInsn(ALOAD, receiverLocalVarIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD, "()Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, jField.getDeclaringClassName());

            Label ifNonNullLabel = labelGen.getLabel("receiver_null_check");
            mv.visitLabel(ifNonNullLabel);
            mv.visitInsn(DUP);

            Label elseBlockLabel = labelGen.getLabel("receiver_null_check_else");
            mv.visitJumpInsn(IFNONNULL, elseBlockLabel);
            Label thenBlockLabel = labelGen.getLabel("receiver_null_check_then");
            mv.visitLabel(thenBlockLabel);
            mv.visitFieldInsn(GETSTATIC, BAL_ERROR_REASONS, "JAVA_NULL_REFERENCE_ERROR", "L" + STRING_VALUE + ";");
            mv.visitFieldInsn(GETSTATIC, RUNTIME_ERRORS, "JAVA_NULL_REFERENCE", "L" + RUNTIME_ERRORS + ";");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, OBJECT);
            mv.visitMethodInsn(INVOKESTATIC, BLANG_EXCEPTION_HELPER, "getRuntimeException",
                    "(L" + STRING_VALUE + ";L" + RUNTIME_ERRORS + ";[L" + OBJECT + ";)L" + ERROR_VALUE + ";", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(elseBlockLabel);
        }

        // Load java method parameters
        birFuncParamIndex = jField.isStatic() ? 0 : 2;
        if (birFuncParamIndex < birFuncParams.size()) {
            BIRNode.BIRFunctionParameter birFuncParam = birFuncParams.get(birFuncParamIndex);
            int paramLocalVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(birFuncParam);
            loadMethodParamToStackInInteropFunction(mv, birFuncParam, jFieldType,
                                                    paramLocalVarIndex, instGen);
        }

        if (jField.isStatic()) {
            if (jField.method == JFieldMethod.ACCESS) {
                mv.visitFieldInsn(GETSTATIC, jField.getDeclaringClassName(), jField.getName(), jField.getSignature());
            } else {
                mv.visitFieldInsn(PUTSTATIC, jField.getDeclaringClassName(), jField.getName(), jField.getSignature());
            }
        } else {
            if (jField.method == JFieldMethod.ACCESS) {
                mv.visitFieldInsn(GETFIELD, jField.getDeclaringClassName(), jField.getName(), jField.getSignature());
            } else {
                mv.visitFieldInsn(PUTFIELD, jField.getDeclaringClassName(), jField.getName(), jField.getSignature());
            }
        }

        // Handle return type
        BIRVariableDcl retVarDcl = new BIRVariableDcl(retType, new Name("$_ret_var_$"), null, VarKind.LOCAL);
        int returnVarRefIndex = indexMap.addToMapIfNotFoundAndGetIndex(retVarDcl);

        if (retType.tag == TypeTags.NIL) {
            mv.visitInsn(ACONST_NULL);
        } else if (retType.tag == TypeTags.HANDLE) {
            // Here the corresponding Java method parameter type is 'jvm:RefType'. This has been verified before
            BIRVariableDcl retJObjectVarDcl = new BIRVariableDcl(jvmPackageGen.symbolTable.anyType,
                    new Name("$_ret_jobject_var_$"), null, VarKind.LOCAL);
            int returnJObjectVarRefIndex = indexMap.addToMapIfNotFoundAndGetIndex(retJObjectVarDcl);
            mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
            mv.visitTypeInsn(NEW, HANDLE_VALUE);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
            mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, JVM_INIT_METHOD, "(Ljava/lang/Object;)V", false);
        } else {
            // bType is a value-type
            if (jField.getFieldType().isPrimitive() /*jFieldType instanceof JPrimitiveType*/) {
                performWideningPrimitiveConversion(mv, retType, jFieldType);
            } else {
                JvmCastGen.addUnboxInsn(mv, retType);
            }
        }

        instGen.generateVarStore(mv, retVarDcl, returnVarRefIndex);

        Label retLabel = labelGen.getLabel("return_lable");
        mv.visitLabel(retLabel);
        mv.visitLineNumber(birFunc.pos.sLine, retLabel);
        termGen.genReturnTerm(returnVarRefIndex, birFunc);
        mv.visitMaxs(200, 400);
        mv.visitEnd();
    }

    private static void generateBasicBlocks(MethodVisitor mv, List<BIRBasicBlock> basicBlocks, LabelGenerator labelGen,
                                            JvmErrorGen errorGen, JvmInstructionGen instGen, JvmTerminatorGen termGen,
                                            BIRFunction func, String moduleClassName,
                                            AsyncDataCollector asyncDataCollector) {
        String funcName = JvmCodeGenUtil.cleanupFunctionName(func.name.value);
        BirScope lastScope = null;
        Set<BirScope> visitedScopesSet = new HashSet<>();
        for (BIRBasicBlock basicBlock : basicBlocks) {
            Label bbLabel = labelGen.getLabel(funcName + basicBlock.id.value);
            mv.visitLabel(bbLabel);
            lastScope = JvmCodeGenUtil
                    .getLastScopeFromBBInsGen(mv, labelGen, instGen, -1, asyncDataCollector, funcName, basicBlock,
                                              visitedScopesSet, lastScope);
            Label bbEndLabel = labelGen.getLabel(funcName + basicBlock.id.value + "beforeTerm");
            mv.visitLabel(bbEndLabel);
            BIRTerminator terminator = basicBlock.terminator;
            // process terminator
            if (!(terminator instanceof BIRTerminator.Return)) {
                JvmCodeGenUtil.generateDiagnosticPos(terminator.pos, mv);
                termGen.genTerminator(terminator, moduleClassName, func, funcName, -1, -1, null, asyncDataCollector);
            }
            errorGen.generateTryCatch(func, funcName, basicBlock, termGen, labelGen);

            BIRBasicBlock thenBB = terminator.thenBB;
            if (thenBB != null) {
                JvmCodeGenUtil.genYieldCheck(mv, termGen.getLabelGenerator(), thenBB, funcName, -1);
            }
        }
    }

    public static void desugarInteropFuncs(JMethodFunctionWrapper extFuncWrapper, BIRFunction birFunc,
                                           JvmMethodGen jvmMethodGen) {
        // resetting the variable generation index
        BType retType = birFunc.type.retType;
        if (Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = JvmCodeGenUtil.TYPE_BUILDER.build(birFunc.type.retType);
        }
        JMethod jMethod = extFuncWrapper.jMethod;
        Class<?>[] jMethodParamTypes = jMethod.getParamTypes();
        JType jMethodRetType = JInterop.getJType(jMethod.getReturnType());

        if (jMethodRetType == JType.jVoid && jMethod.isBalEnvAcceptingMethod()) {
            jMethodRetType = JType.getPrimitiveJTypeForBType(birFunc.returnVariable.type);
        }

        jvmMethodGen.resetIds();
        String bbPrefix = WRAPPER_GEN_BB_ID_NAME;

        BIRBasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, bbPrefix, jvmMethodGen);
        BIRBasicBlock retBB = new BIRBasicBlock(getNextDesugarBBId(bbPrefix, jvmMethodGen));

        List<BIROperand> args = new ArrayList<>();

        List<BIRNode.BIRFunctionParameter> birFuncParams = new ArrayList<>(birFunc.parameters.keySet());
        int birFuncParamIndex = 0;
        // Load receiver which is the 0th parameter in the birFunc
        if (jMethod.kind == JMethodKind.METHOD && !jMethod.isStatic()) {
            BIRNode.BIRFunctionParameter birFuncParam = birFuncParams.get(birFuncParamIndex);
            BIROperand argRef = new BIROperand(birFuncParam);
            args.add(argRef);
            birFuncParamIndex = 1;
        }

        JType varArgType = null;
        int jMethodParamIndex = 0;
        if (jMethod.getReceiverType() != null) {
            jMethodParamIndex++;
            args.add(new BIROperand(birFunc.receiver));
        }

        if (jMethod.isBalEnvAcceptingMethod()) {
            jMethodParamIndex++;
        }

        int paramCount = birFuncParams.size();
        while (birFuncParamIndex < paramCount) {
            BIRNode.BIRFunctionParameter birFuncParam = birFuncParams.get(birFuncParamIndex);
            boolean isVarArg = (birFuncParamIndex == (paramCount - 1)) && birFunc.restParam != null;
            BType bPType = birFuncParam.type;
            JType jPType = JInterop.getJType(jMethodParamTypes[jMethodParamIndex]);
            BIROperand argRef = new BIROperand(birFuncParam);
            // we generate cast operations for unmatching B to J types
            if (!isVarArg && !isMatchingBAndJType(bPType, jPType)) {
                String varName = "$_param_jobject_var" + birFuncParamIndex + "_$";
                BIRVariableDcl paramVarDcl = new BIRVariableDcl(jPType, new Name(varName), null, VarKind.LOCAL);
                birFunc.localVars.add(paramVarDcl);
                BIROperand paramVarRef = new BIROperand(paramVarDcl);
                JCast jToBCast = new JCast(birFunc.pos);
                jToBCast.lhsOp = paramVarRef;
                jToBCast.rhsOp = argRef;
                jToBCast.targetType = jPType;
                argRef = paramVarRef;
                beginBB.instructions.add(jToBCast);
            }
            // for var args, we have two options
            // 1 - desugar java array creation here,
            // 2 - keep the var arg type in the intstruction and do the array creation in instruction gen
            // we are going with the option two for the time being, hence keeping var arg type in the instructions
            // (drawback with option 2 is, function frame may not have proper variables)
            if (isVarArg) {
                varArgType = jPType;
            }
            args.add(argRef);
            birFuncParamIndex += 1;
            jMethodParamIndex += 1;
        }

        int invocationType = INVOKESTATIC;
        if (jMethod.kind == JMethodKind.METHOD && !jMethod.isStatic()) {
            if (jMethod.isDeclaringClassInterface()) {
                invocationType = INVOKEINTERFACE;
            } else {
                invocationType = INVOKEVIRTUAL;
            }
        } else if (jMethod.kind == JMethodKind.METHOD && jMethod.isStatic()) {
            // nothing to do - remove later
        } else {
            invocationType = INVOKESPECIAL;
        }

        BIROperand jRetVarRef = null;

        BIRBasicBlock thenBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, bbPrefix, jvmMethodGen);
        thenBB.terminator = new BIRTerminator.GOTO(birFunc.pos, retBB);

        if (retType.tag != TypeTags.NIL) {
            BIROperand retRef = new BIROperand(birFunc.localVars.get(0));
            if (JType.jVoid != jMethodRetType) {
                BIRVariableDcl retJObjectVarDcl = new BIRVariableDcl(jMethodRetType, new Name("$_ret_jobject_var_$"),
                        null, VarKind.LOCAL);
                birFunc.localVars.add(retJObjectVarDcl);
                BIROperand castVarRef = new BIROperand(retJObjectVarDcl);
                jRetVarRef = castVarRef;
                JCast jToBCast = new JCast(birFunc.pos);
                jToBCast.lhsOp = retRef;
                jToBCast.rhsOp = castVarRef;
                jToBCast.targetType = retType;
                thenBB.instructions.add(jToBCast);
            }

            BIRBasicBlock catchBB = new BIRBasicBlock(getNextDesugarBBId(bbPrefix, jvmMethodGen));
            JErrorEntry ee = new JErrorEntry(beginBB, thenBB, retRef, catchBB);
            for (Class exception : extFuncWrapper.jMethod.getExceptionTypes()) {
                BIRTerminator.Return exceptionRet = new BIRTerminator.Return(birFunc.pos);
                CatchIns catchIns = new CatchIns();
                catchIns.errorClass = exception.getName().replace(".", "/");
                catchIns.term = exceptionRet;
                ee.catchIns.add(catchIns);
            }

            birFunc.errorTable.add(ee);
        }

        // We may be able to use the same instruction rather than two, check later
        if (jMethod.kind == JMethodKind.CONSTRUCTOR) {
            JIConstructorCall jCall = new JIConstructorCall(birFunc.pos);
            jCall.args = args;
            jCall.varArgExist = birFunc.restParam != null;
            jCall.varArgType = varArgType;
            jCall.lhsOp = jRetVarRef;
            jCall.jClassName = jMethod.getClassName().replace(".", "/");
            jCall.name = jMethod.getName();
            jCall.jMethodVMSig = jMethod.getSignature();
            jCall.thenBB = thenBB;
            beginBB.terminator = jCall;
        } else {
            JIMethodCall jCall = new JIMethodCall(birFunc.pos);
            jCall.args = args;
            jCall.varArgExist = birFunc.restParam != null;
            jCall.varArgType = varArgType;
            jCall.lhsOp = jRetVarRef;
            jCall.jClassName = jMethod.getClassName().replace(".", "/");
            jCall.name = jMethod.getName();
            jCall.jMethodVMSig = jMethod.getSignature();
            jCall.invocationType = invocationType;
            jCall.thenBB = thenBB;
            beginBB.terminator = jCall;
        }

        // Adding the returnBB to the end of BB list
        birFunc.basicBlocks.add(retBB);

        retBB.terminator = new BIRTerminator.Return(birFunc.pos);
    }

    private static boolean isMatchingBAndJType(BType sourceTypes, JType targetType) {

        return (TypeTags.isIntegerTypeTag(sourceTypes.tag) && targetType.jTag == JTypeTags.JLONG) ||
                (sourceTypes.tag == TypeTags.FLOAT && targetType.jTag == JTypeTags.JDOUBLE) ||
                (sourceTypes.tag == TypeTags.BOOLEAN && targetType.jTag == JTypeTags.JBOOLEAN);
    }

    // These conversions are already validate beforehand, therefore I am just emitting type conversion instructions
    // here. We can improve following logic with a type lattice.
    private static void performWideningPrimitiveConversion(MethodVisitor mv, BType bType, JType jType) {

        if (TypeTags.isIntegerTypeTag(bType.tag) && jType.jTag == JTypeTags.JLONG) {
            // NOP
        } else if (bType.tag == TypeTags.FLOAT && jType.jTag == JTypeTags.JDOUBLE) {
            // NOP
        } else if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitInsn(I2L);
        } else if (bType.tag == TypeTags.FLOAT) {
            if (jType.jTag == JTypeTags.JLONG) {
                mv.visitInsn(L2D);
            } else if (jType.jTag == JTypeTags.JFLOAT) {
                mv.visitInsn(F2D);
            } else {
                mv.visitInsn(I2D);
            }
        }
    }

    private static void loadMethodParamToStackInInteropFunction(
            MethodVisitor mv, BIRNode.BIRFunctionParameter birFuncParam, JType jMethodParamType, int localVarIndex,
            JvmInstructionGen jvmInstructionGen) {

        BType bFuncParamType = birFuncParam.type;
        // Load the parameter value to the stack
        jvmInstructionGen.generateVarLoad(mv, birFuncParam, localVarIndex);
        generateBToJCheckCast(mv, bFuncParamType, jMethodParamType);
    }

    public static String getJTypeSignature(JType jType) {

        if (jType.jTag == JTypeTags.JREF) {
            return "L" + ((JType.JRefType) jType).typeValue + ";";
        } else if (jType.jTag == JTypeTags.JARRAY) {
            JType eType = ((JType.JArrayType) jType).elementType;
            return "[" + getJTypeSignature(eType);
        }

        switch (jType.jTag) {
            case JTypeTags.JBYTE:
                return "B";
            case JTypeTags.JCHAR:
                return "C";
            case JTypeTags.JSHORT:
                return "S";
            case JTypeTags.JINT:
                return "I";
            case JTypeTags.JLONG:
                return "J";
            case JTypeTags.JFLOAT:
                return "F";
            case JTypeTags.JDOUBLE:
                return "D";
            case JTypeTags.JBOOLEAN:
                return "Z";
            default:
                throw new BLangCompilerException(String.format("invalid element type: %s", jType));
        }
    }

    public static String getSignatureForJType(JType jType) {

        if (jType.jTag == JTypeTags.JREF) {
            return ((JType.JRefType) jType).typeValue;
        } else if (jType.jTag == JTypeTags.JARRAY) { //must be JArrayType
            JType eType = ((JType.JArrayType) jType).elementType;
            String sig = "[";
            while (eType.jTag == JTypeTags.JARRAY) {
                eType = ((JType.JArrayType) eType).elementType;
                sig += "[";
            }

            switch (eType.jTag) {
                case JTypeTags.JREF:
                    return sig + "L" + getSignatureForJType(eType) + ";";
                case JTypeTags.JBYTE:
                    return sig + "B";
                case JTypeTags.JCHAR:
                    return sig + "C";
                case JTypeTags.JSHORT:
                    return sig + "S";
                case JTypeTags.JINT:
                    return sig + "I";
                case JTypeTags.JLONG:
                    return sig + "J";
                case JTypeTags.JFLOAT:
                    return sig + "F";
                case JTypeTags.JDOUBLE:
                    return sig + "D";
                case JTypeTags.JBOOLEAN:
                    return sig + "Z";
                default:
                    throw new BLangCompilerException(String.format("invalid element type: %s", eType));
            }
        } else {
            throw new BLangCompilerException(String.format("invalid element type: %s", jType));
        }
    }

    public static void genVarArg(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, BType bType, JType jvmType,
                                 int varArgIndex, SymbolTable symbolTable) {

        JType jElementType;
        BType bElementType;
        if (jvmType.jTag == JTypeTags.JARRAY && bType.tag == TypeTags.ARRAY) {
            jElementType = ((JType.JArrayType) jvmType).elementType;
            bElementType = ((BArrayType) bType).eType;
        } else {
            throw new BLangCompilerException(String.format("invalid type for var-arg: %s", jvmType));
        }

        BIRVariableDcl varArgsLen = new BIRVariableDcl(symbolTable.intType, new Name("$varArgsLen"), null,
                VarKind.TEMP);

        BIRVariableDcl index = new BIRVariableDcl(symbolTable.intType, new Name("$index"), null, VarKind.TEMP);

        BIRVariableDcl valueArray = new BIRVariableDcl(symbolTable.anyType, new Name("$valueArray"), null,
                VarKind.TEMP);

        int varArgsLenVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(varArgsLen);
        int indexVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(index);
        int valueArrayIndex = indexMap.addToMapIfNotFoundAndGetIndex(valueArray);

        // get the number of var args provided
        mv.visitVarInsn(ALOAD, varArgIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "size", "()I", true);
        mv.visitInsn(DUP);  // duplicate array size - needed for array new
        mv.visitVarInsn(ISTORE, varArgsLenVarIndex);

        // create an array to hold the results. i.e: jvm values
        genArrayNew(mv, jElementType);
        mv.visitVarInsn(ASTORE, valueArrayIndex);

        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, indexVarIndex);
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitLabel(l1);

        // if index >= varArgsLen, then jump to end
        mv.visitVarInsn(ILOAD, indexVarIndex);
        mv.visitVarInsn(ILOAD, varArgsLenVarIndex);
        mv.visitJumpInsn(IF_ICMPGE, l2);

        // `valueArray` and `index` to stack, for lhs of assignment
        mv.visitVarInsn(ALOAD, valueArrayIndex);
        mv.visitVarInsn(ILOAD, indexVarIndex);

        // load `varArg[index]`
        mv.visitVarInsn(ALOAD, varArgIndex);
        mv.visitVarInsn(ILOAD, indexVarIndex);
        mv.visitInsn(I2L);

        if (TypeTags.isIntegerTypeTag(bElementType.tag)) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
        } else if (TypeTags.isStringTypeTag(bElementType.tag)) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBString",
                               String.format("(J)L%s;", JvmConstants.B_STRING_VALUE), true);
        } else {
            switch (bElementType.tag) {
                case TypeTags.BOOLEAN:
                    mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
                    break;
                case TypeTags.BYTE:
                    mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
                    break;
                case TypeTags.FLOAT:
                    mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
                    break;
                case TypeTags.HANDLE:
                    mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", String.format("(J)L%s;", OBJECT),
                            true);
                    mv.visitTypeInsn(CHECKCAST, HANDLE_VALUE);
                    break;
                default:
                    mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", String.format("(J)L%s;", OBJECT),
                            true);
                    break;
            }
        }

        // unwrap from handleValue
        generateBToJCheckCast(mv, bElementType, jElementType);

        // valueArray[index] = varArg[index]
        genArrayStore(mv, jElementType);

        // // increment index, and go to the condition again
        mv.visitIincInsn(indexVarIndex, 1);
        mv.visitJumpInsn(GOTO, l1);

        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, valueArrayIndex);
    }

    private static void genArrayStore(MethodVisitor mv, JType jType) {

        int code;
        switch (jType.jTag) {
            case JTypeTags.JINT:
                code = IASTORE;
                break;
            case JTypeTags.JLONG:
                code = LASTORE;
                break;
            case JTypeTags.JDOUBLE:
                code = DASTORE;
                break;
            case JTypeTags.JBYTE:
            case JTypeTags.JBOOLEAN:
                code = BASTORE;
                break;
            case JTypeTags.JSHORT:
                code = SASTORE;
                break;
            case JTypeTags.JCHAR:
                code = CASTORE;
                break;
            case JTypeTags.JFLOAT:
                code = FASTORE;
                break;
            default:
                code = AASTORE;
                break;
        }

        mv.visitInsn(code);
    }

    private static void genArrayNew(MethodVisitor mv, JType elementType) {

        switch (elementType.jTag) {
            case JTypeTags.JINT:
                mv.visitIntInsn(NEWARRAY, T_INT);
                break;
            case JTypeTags.JLONG:
                mv.visitIntInsn(NEWARRAY, T_LONG);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitIntInsn(NEWARRAY, T_DOUBLE);
                break;
            case JTypeTags.JBYTE:
            case JTypeTags.JBOOLEAN:
                mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
                break;
            case JTypeTags.JSHORT:
                mv.visitIntInsn(NEWARRAY, T_SHORT);
                break;
            case JTypeTags.JCHAR:
                mv.visitIntInsn(NEWARRAY, T_CHAR);
                break;
            case JTypeTags.JFLOAT:
                mv.visitIntInsn(NEWARRAY, T_FLOAT);
                break;
            case JTypeTags.JREF:
            case JTypeTags.JARRAY:
                mv.visitTypeInsn(ANEWARRAY, getSignatureForJType(elementType));
                break;
            default:
                throw new BLangCompilerException(String.format("invalid type for var-arg: %s", elementType));
        }
    }

    static BIRFunctionWrapper createJInteropFunctionWrapper(InteropValidator interopValidator,
                                                            InteropValidationRequest jInteropValidationReq,
                                                            BIRFunction birFunc,
                                                            String orgName,
                                                            String moduleName,
                                                            String version,
                                                            String birModuleClassName,
                                                            SymbolTable symbolTable) {

        if (interopValidator.isEntryModuleValidation()) {
            addDefaultableBooleanVarsToSignature(birFunc, symbolTable.booleanType);
        }
        // Update the function wrapper only for Java interop functions
        BIRFunctionWrapper birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName,
                version, birModuleClassName);
        if (jInteropValidationReq instanceof InteropValidationRequest.MethodValidationRequest) {
            InteropValidationRequest.MethodValidationRequest methodValidationRequest =
                    ((InteropValidationRequest.MethodValidationRequest) jInteropValidationReq);
            methodValidationRequest.restParamExist = birFunc.restParam != null;
            return createJMethodWrapper(interopValidator, methodValidationRequest, birFuncWrapper);
        } else {
            InteropValidationRequest.FieldValidationRequest fieldValidationRequest =
                    (InteropValidationRequest.FieldValidationRequest) jInteropValidationReq;
            return createJFieldWrapper(interopValidator, fieldValidationRequest, birFuncWrapper);
        }
    }

    private static JMethodFunctionWrapper createJMethodWrapper(InteropValidator interopValidator,
                                                               InteropValidationRequest jMethodValidationReq,
                                                               BIRFunctionWrapper birFuncWrapper) {

        JMethod jMethod = interopValidator.validateAndGetJMethod(
                (InteropValidationRequest.MethodValidationRequest) jMethodValidationReq);
        return new JMethodFunctionWrapper(birFuncWrapper, jMethod);
    }

    private static JFieldFunctionWrapper createJFieldWrapper(InteropValidator interopValidator,
                                                             InteropValidationRequest jFieldValidationReq,
                                                             BIRFunctionWrapper birFuncWrapper) {

        JavaField jField = interopValidator.validateAndGetJField(
                (InteropValidationRequest.FieldValidationRequest) jFieldValidationReq);
        return new JFieldFunctionWrapper(birFuncWrapper, jField);
    }

    private InteropMethodGen() {
    }
}
