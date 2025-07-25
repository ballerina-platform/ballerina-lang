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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import io.ballerina.identifier.Utils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LambdaClass;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LambdaFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.model.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CALL_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_FUNCTION_CALLS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_SELF_INSTANCE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BOBJECT_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.FUNCTION_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INITIAL_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.isSameModule;

/**
 * Generates Jvm byte code for the lambda method.
 *
 * @since 2.0.0
 */
public class LambdaGen {

    private final JvmPackageGen jvmPackageGen;
    private final JvmCastGen jvmCastGen;
    private final BIRNode.BIRPackage module;

    public LambdaGen(JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen, BIRNode.BIRPackage module) {
        this.jvmPackageGen = jvmPackageGen;
        this.jvmCastGen = jvmCastGen;
        this.module = module;
    }

    public void generateLambdaClasses(AsyncDataCollector asyncDataCollector,
                                      JarEntries jarEntries) {
        Map<String, LambdaClass> lambdaClasses = asyncDataCollector.getLambdaClasses();
        for (Map.Entry<String, LambdaClass> entry : lambdaClasses.entrySet()) {
            String lambdaClassName = entry.getKey();
            LambdaClass lambdaClass = entry.getValue();
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            cw.visitSource(lambdaClass.sourceFileName, null);
            generateConstantsClassInit(cw, lambdaClassName);
            List<LambdaFunction> lambdaList = lambdaClass.lambdaFunctionList;
            for (LambdaFunction recordDefaultValueLambda : lambdaList) {
                generateLambdaMethod(recordDefaultValueLambda.callInstruction, cw,
                        recordDefaultValueLambda.lambdaName, lambdaClassName);
            }
            cw.visitEnd();
            jarEntries.put(lambdaClassName + CLASS_FILE_SUFFIX, cw.toByteArray());
        }
    }

    private void generateConstantsClassInit(ClassWriter cw, String lambdaClassName) {
        cw.visit(V21, ACC_PUBLIC | ACC_SUPER, lambdaClassName, null, JvmConstants.OBJECT, null);
        MethodVisitor methodVisitor =
                cw.visitMethod(ACC_PRIVATE, JvmConstants.JVM_INIT_METHOD, VOID_METHOD_DESC, null, null);
        methodVisitor.visitCode();
        Label methodStartLabel = new Label();
        methodVisitor.visitLabel(methodStartLabel);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, JvmConstants.OBJECT, JvmConstants.JVM_INIT_METHOD,
                VOID_METHOD_DESC, false);
        Label methodEndLabel = new Label();
        methodVisitor.visitLabel(methodEndLabel);
        methodVisitor.visitLocalVariable(OBJECT_SELF_INSTANCE, GET_OBJECT, null, methodStartLabel, methodEndLabel, 0);
        genMethodReturn(methodVisitor);
    }

    private void generateLambdaMethod(BIRInstruction ins, ClassWriter cw, String lambdaName, String className) {
        LambdaDetails lambdaDetails = getLambdaDetails(ins);
        boolean isSamePkg = isSameModule(module.packageID, lambdaDetails.packageID);
        MethodVisitor mv = getMethodVisitorAndLoadFirst(cw, lambdaName, lambdaDetails, ins, isSamePkg);

        List<BType> paramBTypes = new ArrayList<>();
        if (ins.getKind() == InstructionKind.ASYNC_CALL) {
            handleAsyncCallLambda((BIRTerminator.AsyncCall) ins, lambdaDetails, mv, paramBTypes, isSamePkg);
        } else {
            handleFpLambda((BIRNonTerminator.FPLoad) ins, lambdaDetails, mv, paramBTypes, isSamePkg);
        }
        MethodGenUtils.visitReturn(mv, lambdaName, className);
    }

    private void genNonVirtual(LambdaDetails lambdaDetails, MethodVisitor mv, List<BType> paramBTypes,
                               boolean isSamePkg) {
        String jvmClass, funcName, methodDesc;
        if (!isSamePkg) {
            // Use call method of function calls class to execute functions from imported modules
            jvmClass = getModuleLevelClassName(lambdaDetails.packageID, MODULE_FUNCTION_CALLS_CLASS_NAME);
            funcName = CALL_FUNCTION;
            methodDesc = FUNCTION_CALL;
            mv.visitMethodInsn(INVOKESTATIC, jvmClass, funcName, methodDesc, false);
            return;
        }
        if (lambdaDetails.functionWrapper != null) {
            jvmClass = lambdaDetails.functionWrapper.fullQualifiedClassName();
        } else {
            jvmClass = getModuleLevelClassName(lambdaDetails.packageID, MODULE_FUNCTION_CALLS_CLASS_NAME);
        }
        methodDesc = getLambdaMethodDesc(paramBTypes, lambdaDetails.returnType, lambdaDetails.closureMapsCount);
        mv.visitMethodInsn(INVOKESTATIC, jvmClass, lambdaDetails.encodedFuncName, methodDesc, false);
        jvmCastGen.addBoxInsn(mv, lambdaDetails.returnType);
    }

    private void handleAsyncCallLambda(BIRTerminator.AsyncCall ins, LambdaDetails lambdaDetails, MethodVisitor mv,
                                       List<BType> paramBTypes, boolean isSamePkg) {
        if (ins.isVirtual) {
            handleLambdaVirtual(ins, lambdaDetails, mv);
        } else {
            handleAsyncNonVirtual(lambdaDetails, mv, paramBTypes, isSamePkg);
        }
    }

    private void handleLambdaVirtual(BIRTerminator.AsyncCall ins, LambdaDetails lambdaDetails, MethodVisitor mv) {
        List<BIROperand> paramTypes = ins.args;
        genLoadDataForObjectAttachedLambdas(ins, mv, lambdaDetails.closureMapsCount, paramTypes);
        int paramIndex = 1;
        for (int paramTypeIndex = 1; paramTypeIndex < paramTypes.size(); paramTypeIndex++) {
            generateObjectArgs(mv, paramIndex);
            paramIndex++;
        }
        mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, CALL_FUNCTION, BOBJECT_CALL, true);
    }

    private void genLoadDataForObjectAttachedLambdas(BIRTerminator.AsyncCall ins, MethodVisitor mv,
                                                     int closureMapsCount, List<BIROperand> paramTypes) {

        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, closureMapsCount);
        mv.visitInsn(ICONST_1);
        BIROperand ref = ins.args.getFirst();
        mv.visitInsn(AALOAD);
        jvmCastGen.addUnboxInsn(mv, ref.variableDcl.type);
        mv.visitVarInsn(ALOAD, closureMapsCount);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);

        mv.visitLdcInsn(JvmCodeGenUtil.rewriteVirtualCallTypeName(ins.name.value, ref.variableDcl.type));
        int objectArrayLength = paramTypes.size() - 1;
        mv.visitIntInsn(BIPUSH, objectArrayLength);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
    }

    private void generateObjectArgs(MethodVisitor mv, int paramIndex) {
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, paramIndex - 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, paramIndex + 1);
        mv.visitInsn(AALOAD);
        mv.visitInsn(AASTORE);
    }

    private void generateFpCallArgs(MethodVisitor mv, int paramIndex) {
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, paramIndex);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, paramIndex + 1);
        mv.visitInsn(AALOAD);
        mv.visitInsn(AASTORE);
    }

    private void handleAsyncNonVirtual(LambdaDetails lambdaDetails, MethodVisitor mv, List<BType> paramBTypes,
                                       boolean isSamePkg) {
        List<BType> paramTypes = getFpParamTypes(lambdaDetails);
        // load and cast param values= asyncIns.args;
        if (isSamePkg) {
            int argIndex = 1;
            for (BType paramType : paramTypes) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, argIndex);
                mv.visitInsn(AALOAD);
                jvmCastGen.addUnboxInsn(mv, paramType);
                paramBTypes.add(argIndex - 1, paramType);
                argIndex++;
            }
        } else {
            mv.visitIntInsn(BIPUSH, paramTypes.size());
            mv.visitTypeInsn(ANEWARRAY, OBJECT);
            for (int paramIndex = 0; paramIndex < paramTypes.size(); paramIndex++) {
                generateFpCallArgs(mv, paramIndex);
            }
        }
        genNonVirtual(lambdaDetails, mv, paramBTypes, isSamePkg);
    }

    private List<BType> getFpParamTypes(LambdaDetails lambdaDetails) {
        List<BType> paramTypes;
        if (lambdaDetails.functionWrapper != null) {
            paramTypes = getInitialParamTypes(lambdaDetails.functionWrapper.func().type.paramTypes,
                    lambdaDetails.functionWrapper.func().argsCount);
        } else {
            BInvokableType type = (BInvokableType) lambdaDetails.funcSymbol.type;
            if (type.restType == null) {
                return type.paramTypes;
            }
            paramTypes = new ArrayList<>(type.paramTypes);
            paramTypes.add(type.restType);
        }
        return paramTypes;
    }

    private void handleFpLambda(BIRNonTerminator.FPLoad ins, LambdaDetails lambdaDetails, MethodVisitor mv,
                                List<BType> paramBTypes, boolean isSamePkg) {
        loadClosureMaps(lambdaDetails, mv);
        // load and cast param values
        loadAndCastParamValues(ins, lambdaDetails, mv, paramBTypes, isSamePkg);
        genNonVirtual(lambdaDetails, mv, paramBTypes, isSamePkg);
    }

    private void loadAndCastParamValues(BIRNonTerminator.FPLoad ins, LambdaDetails lambdaDetails, MethodVisitor mv,
                                        List<BType> paramBTypes, boolean isSamePkg) {
        if (isSamePkg) {
            int argIndex = 1;
            for (BIRNode.BIRVariableDcl dcl : ins.params) {
                mv.visitVarInsn(ALOAD, lambdaDetails.closureMapsCount);
                mv.visitIntInsn(BIPUSH, argIndex);
                mv.visitInsn(AALOAD);
                jvmCastGen.addUnboxInsn(mv, dcl.type);
                paramBTypes.add(argIndex - 1, dcl.type);
                argIndex++;
            }
        } else {
            mv.visitIntInsn(BIPUSH, ins.params.size());
            mv.visitTypeInsn(ANEWARRAY, OBJECT);
            for (int paramIndex = 0; paramIndex < ins.params.size(); paramIndex++) {
                generateFpCallArgs(mv, paramIndex);
            }
        }
    }

    private void loadClosureMaps(LambdaDetails lambdaDetails, MethodVisitor mv) {
        for (int i = 0; i < lambdaDetails.closureMapsCount; i++) {
            mv.visitVarInsn(ALOAD, i);
        }
    }

    private MethodVisitor getMethodVisitorAndLoadFirst(ClassWriter cw, String lambdaName, LambdaDetails lambdaDetails,
                                                       BIRInstruction ins, boolean isSamePkg) {
        String closureMapsDesc = getMapValueDesc(lambdaDetails.closureMapsCount);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, lambdaName,
                "(" + closureMapsDesc + "[L" + OBJECT + ";)L" + OBJECT + ";", null, null);

        mv.visitCode();
        // generate diagnostic position when generating lambda method
        JvmCodeGenUtil.generateDiagnosticPos(((BIRAbstractInstruction) ins).pos, mv);
        // load strand as first arg
        // strand and other args are in an object[] param. This param comes after closure maps.
        // hence the closureMapsCount is equal to the array's param index.
        mv.visitVarInsn(ALOAD, lambdaDetails.closureMapsCount);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        if (!isSamePkg) {
            mv.visitLdcInsn(lambdaDetails.encodedFuncName);
        }
        return mv;
    }

    private String getMapValueDesc(int count) {
        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < count; i++) {
            desc.append("L").append(JvmConstants.MAP_VALUE).append(";");
        }
        return desc.toString();
    }

    private LambdaDetails getLambdaDetails(BIRInstruction ins) {
        InstructionKind kind = ins.getKind();
        LambdaDetails lambdaDetails;
        if (kind == InstructionKind.ASYNC_CALL) {
            lambdaDetails = populateAsyncLambdaDetails((BIRTerminator.AsyncCall) ins);
        } else if (kind == InstructionKind.FP_LOAD) {
            lambdaDetails = populateFpLambdaDetails((BIRNonTerminator.FPLoad) ins);
        } else {
            throw new BLangCompilerException("JVM lambda method generation is not supported for instruction " +
                    ins);
        }
        lambdaDetails.isExternFunction = isExternStaticFunctionCall(ins);
        populateLambdaReturnType(ins, lambdaDetails);
        return lambdaDetails;
    }

    private LambdaDetails populateAsyncLambdaDetails(BIRTerminator.AsyncCall asyncIns) {
        LambdaDetails lambdaDetails = new LambdaDetails();
        lambdaDetails.lhsType = asyncIns.lhsOp != null ? asyncIns.lhsOp.variableDcl.type : null;
        lambdaDetails.packageID = asyncIns.calleePkg;
        lambdaDetails.funcName = asyncIns.name.getValue();
        lambdaDetails.encodedFuncName = Utils.encodeFunctionIdentifier(lambdaDetails.funcName);
        if (!asyncIns.isVirtual) {
            populateLambdaFunctionDetails(lambdaDetails);
        }
        return lambdaDetails;
    }

    private LambdaDetails populateFpLambdaDetails(BIRNonTerminator.FPLoad fpIns) {
        LambdaDetails lambdaDetails = new LambdaDetails();
        lambdaDetails.lhsType = fpIns.lhsOp.variableDcl.type;
        lambdaDetails.packageID = fpIns.pkgId;
        lambdaDetails.funcName = fpIns.funcName.getValue();
        lambdaDetails.closureMapsCount = fpIns.closureMaps.size();
        populateLambdaFunctionDetails(lambdaDetails);
        return lambdaDetails;
    }

    private void populateLambdaFunctionDetails(LambdaDetails lambdaDetails) {
        lambdaDetails.encodedFuncName = Utils.encodeFunctionIdentifier(lambdaDetails.funcName);
        lambdaDetails.lookupKey = getPackageName(lambdaDetails.packageID) + lambdaDetails.encodedFuncName;
        lambdaDetails.functionWrapper = jvmPackageGen.lookupBIRFunctionWrapper(lambdaDetails.lookupKey);
        if (lambdaDetails.functionWrapper == null) {
            BPackageSymbol symbol = jvmPackageGen.packageCache.getSymbol(
                    lambdaDetails.packageID.orgName + "/" + lambdaDetails.packageID.name);
            lambdaDetails.funcSymbol = (BInvokableSymbol) symbol.scope.lookup(
                    new Name(lambdaDetails.funcName)).symbol;
        }
    }

    private boolean isExternStaticFunctionCall(BIRInstruction callIns) {
        String methodName;
        InstructionKind kind = callIns.getKind();

        PackageID packageID;

        switch (kind) {
            case CALL -> {
                BIRTerminator.Call call = (BIRTerminator.Call) callIns;
                if (call.isVirtual) {
                    return false;
                }
                methodName = call.name.value;
                packageID = call.calleePkg;
            }
            case ASYNC_CALL -> {
                BIRTerminator.AsyncCall asyncCall = (BIRTerminator.AsyncCall) callIns;
                methodName = asyncCall.name.value;
                packageID = asyncCall.calleePkg;
            }
            case FP_LOAD -> {
                BIRNonTerminator.FPLoad fpLoad = (BIRNonTerminator.FPLoad) callIns;
                methodName = fpLoad.funcName.value;
                packageID = fpLoad.pkgId;
            }
            default -> throw new BLangCompilerException("JVM static function call generation is not supported for " +
                    "instruction " + callIns);
        }

        String key = getPackageName(packageID) + methodName;

        BIRFunctionWrapper functionWrapper = jvmPackageGen.lookupBIRFunctionWrapper(key);
        return functionWrapper != null && JvmCodeGenUtil.isExternFunc(functionWrapper.func());
    }

    private void populateLambdaReturnType(BIRInstruction ins, LambdaDetails lambdaDetails) {
        BType lhsType = JvmCodeGenUtil.getImpliedType(lambdaDetails.lhsType);
        if (lhsType.tag == TypeTags.FUTURE) {
            lambdaDetails.returnType = ((BFutureType) lhsType).constraint;
        } else if (ins instanceof BIRNonTerminator.FPLoad) {
            lambdaDetails.returnType = ((BInvokableType) ((BIRNonTerminator.FPLoad) ins).type).retType;
        } else {
            throw new BLangCompilerException("JVM generation is not supported for async return type " +
                    lambdaDetails.lhsType);
        }
    }

    private static class LambdaDetails {
        BType lhsType;
        PackageID packageID;
        String funcName;
        boolean isExternFunction;
        String encodedFuncName = null;
        String lookupKey;
        BIRFunctionWrapper functionWrapper = null;
        BInvokableSymbol funcSymbol = null;
        BType returnType;
        int closureMapsCount = 0;
    }

    private String getLambdaMethodDesc(List<BType> paramTypes, BType retType, int closureMapsCount) {
        StringBuilder desc = new StringBuilder(INITIAL_METHOD_DESC);
        appendClosureMaps(closureMapsCount, desc);
        appendParamTypes(paramTypes, desc);
        desc.append(JvmCodeGenUtil.generateReturnType(retType, jvmCastGen.typeEnv()));
        return desc.toString();
    }

    private void appendParamTypes(List<BType> paramTypes, StringBuilder desc) {
        for (BType paramType : paramTypes) {
            desc.append(JvmCodeGenUtil.getArgTypeSignature(paramType));
        }
    }

    private void appendClosureMaps(int closureMapsCount, StringBuilder desc) {
        for (int j = 0; j < closureMapsCount; j++) {
            desc.append("L").append(JvmConstants.MAP_VALUE).append(";");
        }
    }

    private List<BType> getInitialParamTypes(List<BType> paramTypes, int argsCount) {
        List<BType> initialParamTypes = new ArrayList<>();
        for (int index = 0; index < argsCount; index++) {
            initialParamTypes.add(paramTypes.get(index));
        }
        return initialParamTypes;
    }

}
