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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import com.sun.codemodel.internal.JPrimitiveType;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.types.Field;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.Nilable;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.getNextDesugarBBId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.insertAndGetNextBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen.ErrorHandlerGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.InstructionGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.addUnboxInsn;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarLoad;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarStore;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmLabelGen.LabelGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateBasicBlocks;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getMethodDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextVarId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.BIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInsKind.JCAST;


public class InteropMethodGen {
    public type JTermKind
    public type JInsKind

    // Java specific terminator kinds
    public static public const JTERM_CALL =1;

    public static public const JTERM_NEW =2;
    type JInteropFunctionWrapper
    JTERM_CALL |JTERM_NEW;
    type BValueType

    static void genJFieldForInteropField(JFieldFunctionWrapper jFieldFuncWrapper,
                                         ClassWriter cw,
                                         BIRPackage birModule) {
        String currentPackageName = getPackageName(birModule.org.value, birModule.name.value);

        // Create a local variable for the strand
        BalToJVMIndexMap indexMap = new BalToJVMIndexMap();
        BIRVariableDcl strandVarDcl = new BIRVariableDcl(symbolTable.stringType, new Name("$_strand_$"), null, VarKind.ARG);
        int strandParamIndex = indexMap.getIndex(strandVarDcl);

        // Generate method desc
        BIRFunction birFunc = jFieldFuncWrapper.func;
        String desc = getMethodDesc(birFunc.type.paramTypes, birFunc.type.retType, null, false, false);
        int access = ACC_PUBLIC + ACC_STATIC;

        MethodVisitor mv = cw.visitMethod(access, birFunc.name.value, desc, null, null);
        InstructionGenerator instGen = new InstructionGenerator(mv, indexMap, birModule);
        ErrorHandlerGenerator errorGen = new ErrorHandlerGenerator(mv, indexMap, currentPackageName);
        LabelGenerator labelGen = new LabelGenerator();
        TerminatorGenerator termGen = new TerminatorGenerator(mv, indexMap, labelGen, errorGen, birModule);
        mv.visitCode();

        Label paramLoadLabel = labelGen.getLabel("param_load");
        mv.visitLabel(paramLoadLabel);
        mv.visitLineNumber(birFunc.pos.sLine, paramLoadLabel);

        // birFunc.localVars contains all the function parameters as well as added boolean parameters to indicate the
        //  availability of default values.
        // The following line cast localvars to function params. This is guaranteed not to fail.
        // Get a JVM method local variable index for the parameter
        @Nilable List<BIRNode.BIRFunctionParameter> birFuncParams = new ArrayList<>();
        for (BIRVariableDcl birLocalVarOptional : birFunc.localVars) {
            if (birLocalVarOptional instanceof BIRNode.BIRFunctionParameter) {
                BIRNode.BIRFunctionParameter functionParameter = (BIRNode.BIRFunctionParameter) birLocalVarOptional;
                birFuncParams.add(functionParameter);
                indexMap.getIndex(functionParameter);
            }
        }

        // Generate if blocks to check and set default values to parameters
        int birFuncParamIndex = 0;
        int paramDefaultsBBIndex = 0;
        for (BIRNode.BIRFunctionParameter birFuncParam : birFuncParams) {
//            var birFuncParam = (BIRFunctionParam) birFuncParamOptional;
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
            mv.visitVarInsn(ILOAD, indexMap.getIndex(isDefaultValueExist));

            // Gen the if not equal logic
            Label paramNextLabel = labelGen.getLabel(birFuncParam.name.value + "next");
            mv.visitJumpInsn(IFNE, paramNextLabel);

            @Nilable List<BIRBasicBlock> basicBlocks = birFunc.paramDefaultBBs.get(paramDefaultsBBIndex);
            generateBasicBlocks(mv, basicBlocks, labelGen, errorGen, instGen, termGen, birFunc, -1,
                    -1, strandParamIndex, true, birModule, currentPackageName, null, false);
            mv.visitLabel(paramNextLabel);

            birFuncParamIndex += 1;
            paramDefaultsBBIndex += 1;
        }

        Field jField = jFieldFuncWrapper.jField;
        JType jFieldType = jField.getType();

        // Load receiver which is the 0th parameter in the birFunc
        if (!jField.isStatic) {
            var receiverLocalVarIndex = indexMap.getIndex((BIRFunctionParam) birFuncParams.get(0));
            mv.visitVarInsn(ALOAD, receiverLocalVarIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, jField.klass);

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
        birFuncParamIndex = jField.isStatic ? 0 : 2;
        int jMethodParamIndex = 0;
        if (birFuncParamIndex < birFuncParams.size()) {
            var birFuncParam = (BIRFunctionParam) birFuncParams.get(birFuncParamIndex);
            int paramLocalVarIndex = indexMap.getIndex(birFuncParam);
            loadMethodParamToStackInInteropFunction(mv, birFuncParam, jFieldType, currentPackageName, paramLocalVarIndex,
                    indexMap, false);
        }

        if (jField.isStatic {
            if jField.method instanceof ACCESS){
                mv.visitFieldInsn(GETSTATIC, jField.klass, jField.name, jField.sig);
            } else{
                mv.visitFieldInsn(PUTSTATIC, jField.klass, jField.name, jField.sig);
            }
        } else{
            if (jField.method instanceof ACCESS) {
                mv.visitFieldInsn(GETFIELD, jField.klass, jField.name, jField.sig);
            } else {
                mv.visitFieldInsn(PUTFIELD, jField.klass, jField.name, jField.sig);
            }
        }

        // Handle return type
        BType retType = (BType) birFunc.type["retType"];
        BIRVariableDcl retVarDcl = new BIRVariableDcl(type:(BType) retType, name:new (value:"$_ret_var_$" ),kind:
        "LOCAL" );
        int returnVarRefIndex = indexMap.getIndex(retVarDcl);

        if (retType.tag == TypeTags.NIL) {
            mv.visitInsn(ACONST_NULL);
        } else if (retType.tag == TypeTags.HANDLE) {
            // Here the corresponding Java method parameter type is 'jvm:RefType'. This has been verified before
            BIRVariableDcl retJObjectVarDcl = new BIRVariableDcl(type:"any", name:new (value:"$_ret_jobject_var_$" ),
            kind:
            "LOCAL" );
            int returnJObjectVarRefIndex = indexMap.getIndex(retJObjectVarDcl);
            mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
            mv.visitTypeInsn(NEW, HANDLE_VALUE);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
            mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, "<init>", "(Ljava/lang/Object;)V", false);
        } else {
            // bType is a value-type
            if (jFieldType instanceof JPrimitiveType) {
                performWideningPrimitiveConversion(mv, (BValueType) retType, jFieldType);
            } else {
                addUnboxInsn(mv, retType);
            }
        }

        generateVarStore(mv, retVarDcl, currentPackageName, returnVarRefIndex);

        Label retLabel = labelGen.getLabel("return_lable");
        mv.visitLabel(retLabel);
        mv.visitLineNumber(birFunc.pos.sLine, retLabel);
        termGen.genReturnTerm({pos:birFunc.pos, kind:"RETURN"},returnVarRefIndex, birFunc);
        mv.visitMaxs(200, 400);
        mv.visitEnd();
    }

    static void desugarInteropFuncs(BIRPackage module, JMethodFunctionWrapper extFuncWrapper,
                                    BIRFunction birFunc) {
        // resetting the variable generation index
        BType retType = (BType) birFunc.type["retType"];
        Method jMethod = extFuncWrapper.jMethod;
        MethodType jMethodType = jMethod.mType;
        List<JType> jMethodParamTypes = jMethodType.paramTypes;
        JType jMethodRetType = jMethodType.retType;


        nextId = -1;
        nextVarId = -1;
        String bbPrefix = "wrapperGen";

        BIRBasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = bbPrefix);
        BIRBasicBlock retBB = new BIRBasicBlock(id:getNextDesugarBBId(bbPrefix), instructions: []);

        @Nilable List<BIROperand> args = new ArrayList<>();

        @Nilable BIRVariableDcl receiver = birFunc.receiver;

        @Nilable List<BIRFunctionParam> birFuncParams = birFunc.params;
        int birFuncParamIndex = 0;
        // Load receiver which is the 0th parameter in the birFunc
        if (jMethod.kind instanceof METHOD && !jMethod.isStatic) {
            BIRFunctionParam birFuncParam = (BIRFunctionParam) birFuncParams.get(birFuncParamIndex);
            BType bPType = birFuncParam.type;
            BIROperand argRef = new BIROperand(variableDcl:birFuncParam, type:bPType);
            args.add(argRef);
            birFuncParamIndex = 1;
        }

        @Nilable JType varArgType = null;
        int jMethodParamIndex = 0;
        int paramCount = birFuncParams.size();
        while (birFuncParamIndex < paramCount) {
            BIRFunctionParam birFuncParam = (BIRFunctionParam) birFuncParams.get(birFuncParamIndex);
            boolean isVarArg = (birFuncParamIndex == (paramCount - 1)) && birFunc.restParamExist;
            BType bPType = birFuncParam.type;
            JType jPType = jMethodParamTypes.get(jMethodParamIndex);
            BIROperand argRef = new BIROperand(variableDcl:birFuncParam, type:bPType);
            // we generate cast operations for unmatching B to J types
            if (!isVarArg && !isMatchingBAndJType(bPType, jPType)) {
                String varName = "$_param_jobject_var" + birFuncParamIndex.toString() + "_$";
                BIRVariableDcl paramVarDcl = new BIRVariableDcl(type:jPType, name:new (value:varName ),kind:
                "LOCAL" );
                birFunc.localVars.add(paramVarDcl);
                BIROperand paramVarRef = new BIROperand(type:jPType, variableDcl:paramVarDcl);
                JCast jToBCast = new JCast(pos:birFunc.pos, lhsOp:paramVarRef, rhsOp:argRef, targetType:jPType);
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
        if (jMethod.kind instanceof METHOD && !jMethod.isStatic {
            if jMethod.isInterface {
                invocationType = INVOKEINTERFACE;
            } else{
                invocationType = INVOKEVIRTUAL;
            }
        } else if jMethod.kind instanceof METHOD && jMethod.isStatic {
            // nothing to do - remove later
        } else){
            invocationType = INVOKESPECIAL;
        }

        @Nilable BIROperand jRetVarRef = null;

        BIRBasicBlock thenBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = bbPrefix);
        BIRGOTO gotoRet = new BIRGOTO(pos:birFunc.pos, kind:BIRTERMINATOR_GOTO, targetBB:retBB);
        thenBB.terminator = gotoRet;

        if (!(retType.tag == TypeTags.NIL)) {
            BIROperand retRef = new BIROperand(variableDcl:getVariableDcl(birFunc.localVars.get(0)), type:retType);
            if (!(jMethodRetType instanceof JVoid)) {
                BIRVariableDcl retJObjectVarDcl = new BIRVariableDcl(type:jMethodRetType, name:new (value:
                "$_ret_jobject_var_$" ),kind:
                "LOCAL" );
                birFunc.localVars.add(retJObjectVarDcl);
                BIROperand castVarRef = new BIROperand(type:jMethodRetType, variableDcl:retJObjectVarDcl);
                jRetVarRef = castVarRef;
                JCast jToBCast = new JCast(pos:birFunc.pos, lhsOp:retRef, rhsOp:castVarRef, targetType:retType);
                thenBB.instructions.add(jToBCast);
            }

            BIRBasicBlock catchBB = new BIRBasicBlock(id:getNextDesugarBBId(bbPrefix), instructions: []);
            JErrorEntry ee = new JErrorEntry(trapBB:beginBB, endBB:thenBB, errorOp:retRef, targetBB:catchBB, catchIns:[] )
            ;
            for (T exception : extFuncWrapper.jMethod.throws){
                BIRReturn exceptionRet = new BIRReturn(pos:birFunc.pos, kind:BIRTERMINATOR_RETURN);
                CatchIns catchIns = new CatchIns(errorClass:exception, term:exceptionRet );
                ee.catchIns.add(catchIns);
            }

            birFunc.errorEntries.add(ee);
        }

        String jMethodName = birFunc.name.value;
        // We may be able to use the same instruction rather than two, check later
        if (jMethod.kind instanceof CONSTRUCTOR) {
            JIConstructorCall jCall = new JIConstructorCall(pos:birFunc.pos, args:args, varArgExist:
            birFunc.restParamExist, varArgType:varArgType,
                    kind:BIRTERMINATOR_PLATFORM, lhsOp:jRetVarRef, jClassName:jMethod.klass, name:jMethod.name,
                    jMethodVMSig:jMethod.sig, thenBB:thenBB);
            beginBB.terminator = jCall;
        } else {
            JIMethodCall jCall = new JIMethodCall(pos:birFunc.pos, args:args, varArgExist:
            birFunc.restParamExist, varArgType:varArgType,
                    kind:BIRTERMINATOR_PLATFORM, lhsOp:jRetVarRef, jClassName:jMethod.klass, name:jMethod.name,
                    jMethodVMSig:jMethod.sig, invocationType:invocationType, thenBB:thenBB);
            beginBB.terminator = jCall;
        }

        // Adding the returnBB to the end of BB list
        birFunc.basicBlocks.add(retBB);

        BIRReturn ret = new BIRReturn(pos:birFunc.pos, kind:BIRTERMINATOR_RETURN);
        retBB.terminator = ret;

        //json|error j = json.constructFrom(birFunc);
        //if (j is json) {
        //	io:println(j.toJsonString());
        //} else {
        //	io:println(j);
        //}
    }

    // Java specific instruction kinds
    public static public const JCAST =1;

    public static public const JNEW =2;

    static boolean isMatchingBAndJType(BType sourceTypes, JType targetType) {
        if ((sourceTypes.tag == TypeTags.INT && targetType.tag == JTypeTags.JLONG) ||
                (sourceTypes.tag == TypeTags.FLOAT && targetType.tag == JTypeTags.JDOUBLE) ||
                (sourceTypes.tag == TypeTags.BOOLEAN && targetType.tag == JTypeTags.JBOOLEAN)) {
            return true;
        }
        return false;
    }

    JCAST |JNEW;

    // These conversions are already validate beforehand, therefore I am just emitting type conversion instructions here.
    // We can improve following logic with a type lattice.
    static void performWideningPrimitiveConversion(MethodVisitor mv, BValueType bType, JPrimitiveType jType) {
        if (bType.tag == TypeTags.INT && jType.tag == JTypeTags.JLONG {
            return; // NOP
        } else if bType.tag == TypeTags.FLOAT && jType.tag == JTypeTags.JDOUBLE {
            return; // NOP
        } else if bType.tag == TypeTags.INT){
            mv.visitInsn(I2L);
        } else if (bType.tag == TypeTags.FLOAT {
            if jType.tag == JTypeTags.JLONG){
                mv.visitInsn(L2D);
            } else if (jType.tag == JTypeTags.JFLOAT) {
                mv.visitInsn(F2D);
            } else {
                mv.visitInsn(I2D);
            }
        }
    }

    static void loadMethodParamToStackInInteropFunction(MethodVisitor mv,
                                                        BIRFunctionParam birFuncParam,
                                                        JType jMethodParamType,
                                                        String currentPackageName,
                                                        int localVarIndex,
                                                        BalToJVMIndexMap indexMap,
                                                        boolean isVarArg) {
        BType bFuncParamType = birFuncParam.type;
        if (isVarArg) {
            genVarArg(mv, indexMap, bFuncParamType, jMethodParamType, localVarIndex);
        } else {
            // Load the parameter value to the stack
            generateVarLoad(mv, birFuncParam, currentPackageName, localVarIndex);
            generateBToJCheckCast(mv, bFuncParamType, (JType) jMethodParamType);
        }
    }

    static String getJTypeSignature(JType jType) {
        if (jType.tag == JTypeTags.JREF) {
            return "L" + jType.type + ";";
        } else if (jType.tag == JTypeTags.JARRAY) {
            JType eType = jType.elementType;
            return "[" + getJTypeSignature(eType);
        } else {
            if (jType.tag == JTypeTags.JBYTE) {
                return "B";
            } else if (jType.tag == JTypeTags.JCHAR) {
                return "C";
            } else if (jType.tag == JTypeTags.JSHORT) {
                return "S";
            } else if (jType.tag == JTypeTags.JINT) {
                return "I";
            } else if (jType.tag == JTypeTags.JLONG) {
                return "J";
            } else if (jType.tag == JTypeTags.JFLOAT) {
                return "F";
            } else if (jType.tag == JTypeTags.JDOUBLE) {
                return "D";
            } else if (jType.tag == JTypeTags.JBOOLEAN) {
                return "Z";
            } else {
                BLangCompilerException e = new BLangCompilerException(String.format("invalid element type: %s", jType));
                throw e;
            }
        }
    }

    public static String getSignatureForJType(JType jType) {
        if (jType.tag == JTypeTags.JREF) {
            return jType.type;
        } else if (jType.tag == JTypeTags.JARRAY) { //must be JArrayType
            JType eType = jType.elementType;
            String sig = "[";
            while (eType.tag == JTypeTags.JARRAY) {
                eType = eType.elementType;
                sig += "[";
            }

            if (eType.tag == JTypeTags.JREF) {
                return sig + "L" + getSignatureForJType(eType) + ";";
            } else if (eType.tag == JTypeTags.JBYTE) {
                return sig + "B";
            } else if (eType.tag == JTypeTags.JCHAR) {
                return sig + "C";
            } else if (eType.tag == JTypeTags.JSHORT) {
                return sig + "S";
            } else if (eType.tag == JTypeTags.JINT) {
                return sig + "I";
            } else if (eType.tag == JTypeTags.JLONG) {
                return sig + "J";
            } else if (eType.tag == JTypeTags.JFLOAT) {
                return sig + "F";
            } else if (eType.tag == JTypeTags.JDOUBLE) {
                return sig + "D";
            } else if (eType.tag == JTypeTags.JBOOLEAN) {
                return sig + "Z";
            } else {
                BLangCompilerException e = new BLangCompilerException(String.format("invalid element type: %s", eType));
                throw e;
            }
        }
    }

    public static void genVarArg(MethodVisitor mv, BalToJVMIndexMap indexMap, BType bType, JType jvmType,
                                 int varArgIndex) {
        JType jElementType;
        BType bElementType;
        if (jvmType.tag == JTypeTags.JARRAY && bType.tag == TypeTags.ARRAY) {
            jElementType = jvmType.elementType;
            bElementType = bType.eType;
        } else {
            BLangCompilerException e = new BLangCompilerException(String.format("invalid type for var-arg: %s", jvmType));
            throw e;
        }

        BIRVariableDcl varArgsLen = new BIRVariableDcl(type:BIRTYPE_INT,
                name:new (value:"$varArgsLen" ),
        kind:
        BIRVAR_KIND_TEMP );
        BIRVariableDcl index = new BIRVariableDcl(type:BIRTYPE_INT,
                name:new (value:"$index" ),
        kind:
        BIRVAR_KIND_TEMP );
        BIRVariableDcl valueArray = new BIRVariableDcl(type:BIRTYPE_ANY,
                name:new (value:"$valueArray" ),
        kind:
        BIRVAR_KIND_TEMP );

        int varArgsLenVarIndex = indexMap.getIndex(varArgsLen);
        int indexVarIndex = indexMap.getIndex(index);
        int valueArrayIndex = indexMap.getIndex(valueArray);

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

        if (bElementType.tag == TypeTags.INT) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
        } else if (bElementType.tag == TypeTags.STRING) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getString", String.format("(J)L%s;", STRING_VALUE), true);
        } else if (bElementType.tag == TypeTags.BOOLEAN) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
        } else if (bElementType.tag == TypeTags.BYTE) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
        } else if (bElementType.tag == TypeTags.FLOAT) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
        } else if (bElementType.tag == TypeTags.HANDLE) {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", String.format("(J)L%s;", OBJECT), true);
            mv.visitTypeInsn(CHECKCAST, HANDLE_VALUE);
        } else {
            mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", String.format("(J)L%s;", OBJECT), true);
        }

        // unwrap from handleValue
        generateBToJCheckCast(mv, bElementType, (JType) jElementType);

        // valueArray[index] = varArg[index]
        genArrayStore(mv, jElementType);

        // // increment index, and go to the condition again
        mv.visitIincInsn(indexVarIndex, 1);
        mv.visitJumpInsn(GOTO, l1);

        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, valueArrayIndex);
    }

    JMethodFunctionWrapper |JFieldFunctionWrapper;

    static JInteropFunctionWrapper |

    static void genArrayStore(MethodVisitor mv, JType jType) {
        int code;
        if (jType.tag == JTypeTags.JINT {
            code = IASTORE;
        } else if jType.tag == JTypeTags.JLONG {
            code = LASTORE;
        } else if jType.tag == JTypeTags.JDOUBLE {
            code = DASTORE;
        } else if jType.tag == JTypeTags.JBYTE || jType.tag == JTypeTags.JBOOLEAN {
            code = BASTORE;
        } else if jType.tag == JTypeTags.JSHORT {
            code = SASTORE;
        } else if jType.tag == JTypeTags.JCHAR {
            code = CASTORE;
        } else if jType.tag == JTypeTags.JFLOAT {
            code = FASTORE;
        } else){
            code = AASTORE;
        }

        mv.visitInsn(code);
    }

    static JMethodFunctionWrapper |

    static void genArrayNew(MethodVisitor mv, JType elementType) {
        if (elementType.tag == JTypeTags.JINT) {
            mv.visitIntInsn(NEWARRAY, T_INT);
        } else if (elementType.tag == JTypeTags.JLONG) {
            mv.visitIntInsn(NEWARRAY, T_LONG);
        } else if (elementType.tag == JTypeTags.JDOUBLE) {
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        } else if (elementType.tag == JTypeTags.JBYTE || elementType.tag == JTypeTags.JBOOLEAN) {
            mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        } else if (elementType.tag == JTypeTags.JSHORT) {
            mv.visitIntInsn(NEWARRAY, T_SHORT);
        } else if (elementType.tag == JTypeTags.JCHAR) {
            mv.visitIntInsn(NEWARRAY, T_CHAR);
        } else if (elementType.tag == JTypeTags.JFLOAT) {
            mv.visitIntInsn(NEWARRAY, T_FLOAT);
        } else if (elementType.tag == JTypeTags.JREF | JArrayType) {
            mv.visitTypeInsn(ANEWARRAY, getSignatureForJType(elementType));
        } else {
            BLangCompilerException e = new BLangCompilerException(String.format("invalid type for var-arg: %s", elementType));
            throw e;
        }
    }

    static JFieldFunctionWrapper |

    static BIRFunctionWrapper createJInteropFunctionWrapper(InteropValidator interopValidator,
                                                         InteropValidationRequest jInteropValidationReq,
                                                         BIRFunction birFunc,
                                                         String orgName,
                                                         String moduleName,
                                                         String version,
                                                         String birModuleClassName) {

        addDefaultableBooleanVarsToSignature(birFunc);
        // Update the function wrapper only for Java interop functions
        BIRFunctionWrapper birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName,
                version, birModuleClassName);
        if (jInteropValidationReq instanceof MethodValidationRequest) {
            jInteropValidationReq.restParamExist = birFunc.restParamExist;
            return createJMethodWrapper(interopValidator, jInteropValidationReq, birFuncWrapper);
        } else {
            return createJFieldWrapper(interopValidator, jInteropValidationReq, birFuncWrapper);
        }
    }

    BLangCompilerException createJMethodWrapper(InteropValidator interopValidator, MethodValidationRequest jMethodValidationReq,
                                                BIRFunctionWrapper birFuncWrapper) {
        var jMethod = check interopValidator.validateAndGetJMethod(jMethodValidationReq);

        return {
                orgName :birFuncWrapper.orgName,
                moduleName :birFuncWrapper.moduleName,
                version :birFuncWrapper.version,
                func :birFuncWrapper.func,
                fullQualifiedClassName :birFuncWrapper.fullQualifiedClassName,
                jvmMethodDescription :birFuncWrapper.jvmMethodDescription,
                jMethod:(Method) jMethod
    };
    }

    BLangCompilerException createJFieldWrapper(InteropValidator interopValidator, FieldValidationRequest jFieldValidationReq,
                                               BIRFunctionWrapper birFuncWrapper) {
        var jField = check interopValidator.validateAndGetJField(jFieldValidationReq);

        return {
                orgName :birFuncWrapper.orgName,
                moduleName :birFuncWrapper.moduleName,
                version :birFuncWrapper.version,
                func :birFuncWrapper.func,
                fullQualifiedClassName :birFuncWrapper.fullQualifiedClassName,
                jvmMethodDescription :birFuncWrapper.jvmMethodDescription,
                jField:(Field) jField
    };
    }

    static class JMethodFunctionWrapper extends BIRFunctionWrapper {
    *BIRFunctionWrapper;
        Method jMethod;
    }

    static class JFieldFunctionWrapper extends BIRFunctionWrapper  {
    *BIRFunctionWrapper;
        Field jField;
    }

    bir:BTypeInt |bir:BTypeFloat |bir:BTypeBoolean |bir:BTypeByte |bir:BTypeNil;

    // Java specific terminator definitions
    public static class JTerminator {
        DiagnosticPos pos;
        BIRTerminatorKind kind = BIRTERMINATOR_PLATFORM;
        JTermKind jKind;
        anydata...;
    }

    public static class JIMethodCall extends BIRTerminator {
        DiagnosticPos pos;
        @Nilable
        public
        List<BIROperand> args;
        public boolean varArgExist;
        @Nilable
        public
        JType varArgType;
        BIRTerminatorKind kind = BIRTERMINATOR_PLATFORM;
        @Nilable
        public
        BIROperand lhsOp;
        JTermKind jKind = JTERM_CALL;
        public String jClassName;
        public String jMethodVMSig;
        public String name;
        public int invocationType;
        BIRBasicBlock thenBB;

        public JIMethodCall(DiagnosticPos pos, InstructionKind kind) {
            super(pos, kind);
        }

        @Override
        public void accept(BIRVisitor visitor) {

        }
    }

    public static class JIConstructorCall extends BIRTerminator {
        DiagnosticPos pos;
        @Nilable
        public
        List<BIROperand> args;
        boolean varArgExist;
        @Nilable
        JType varArgType;
        BIRTerminatorKind kind = BIRTERMINATOR_PLATFORM;
        @Nilable
        public
        BIROperand lhsOp;
        JTermKind jKind = JTERM_NEW;
        public String jClassName;
        public String jMethodVMSig;
        public String name;
        BIRBasicBlock thenBB;

        public JIConstructorCall(DiagnosticPos pos, InstructionKind kind) {

            super(pos, kind);
        }

        @Override
        public void accept(BIRVisitor visitor) {

        }
    }

    // Java specific instruction definitions
    public static class JInstruction extends BIRNonTerminator {
        public DiagnosticPos pos;
        public JInsKind jKind;

        JInstruction(DiagnosticPos pos, InstructionKind kind) {
            super(pos,  InstructionKind.PLATFORM);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            throw new UnsupportedOperationException();
        }
    }

    public static class JCast extends JInstruction {
        public BIROperand lhsOp;
        public BIROperand rhsOp;
        public BType targetType;

        JCast(DiagnosticPos pos, InstructionKind kind) {
            super(pos, kind);
            jKind = JCAST;
        }
    }

    public static class JErrorEntry extends BIRErrorEntry {
        public List<CatchIns> catchIns;
        BIRBasicBlock trapBB;
        BIRBasicBlock endBB;
        BIROperand errorOp;
        BIRBasicBlock targetBB;

        public JErrorEntry(BIRBasicBlock trapBB, BIRBasicBlock endBB, BIROperand errorOp, BIRBasicBlock targetBB) {
            super(trapBB, endBB, errorOp, targetBB);
        }
    }

    public static class CatchIns {
        public String errorClass;
        public BIRTerminator.Return term;
    }
}