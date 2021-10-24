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

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRArgument;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BLOCKED_ON_EXTERN_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.IS_BLOCKED_ON_EXTERN_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BOBJECT_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INITIAL_METHOD_DESC;
/**
 * Generates Jvm byte code for the lambda method.
 *
 * @since 2.0.0
 */
public class LambdaGen {

    private final SymbolTable symbolTable;
    private final JvmPackageGen jvmPackageGen;
    private final JvmCastGen jvmCastGen;

    public LambdaGen(JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen) {
        this.jvmPackageGen = jvmPackageGen;
        this.symbolTable = jvmPackageGen.symbolTable;
        this.jvmCastGen = jvmCastGen;
    }

    public void generateLambdaMethod(BIRInstruction ins, ClassWriter cw, String lambdaName) {
        LambdaDetails lambdaDetails = getLambdaDetails(ins);
        MethodVisitor mv = getMethodVisitorAndLoadFirst(cw, lambdaName, lambdaDetails, ins);

        List<BType> paramBTypes = new ArrayList<>();
        if (ins.getKind() == InstructionKind.ASYNC_CALL) {
            handleAsyncCallLambda((BIRTerminator.AsyncCall) ins, lambdaDetails, mv, paramBTypes);
        } else {
            handleFpLambda((BIRNonTerminator.FPLoad) ins, lambdaDetails, mv, paramBTypes);
        }
        MethodGenUtils.visitReturn(mv);
    }

    private void genNonVirtual(LambdaDetails lambdaDetails, MethodVisitor mv, List<BType> paramBTypes) {
        String jvmClass;
        String methodDesc = getLambdaMethodDesc(paramBTypes, lambdaDetails.returnType, lambdaDetails.closureMapsCount);
        if (lambdaDetails.functionWrapper != null) {
            jvmClass = lambdaDetails.functionWrapper.fullQualifiedClassName;
        } else {
            String balFileName = lambdaDetails.funcSymbol.source;

            if (balFileName == null || !balFileName.endsWith(JvmConstants.BAL_EXTENSION)) {
                balFileName = JvmConstants.MODULE_INIT_CLASS_NAME;
            }
            jvmClass = JvmCodeGenUtil.getModuleLevelClassName(lambdaDetails.packageID, JvmCodeGenUtil
                    .cleanupPathSeparators(balFileName));
        }
        mv.visitMethodInsn(INVOKESTATIC, jvmClass, lambdaDetails.encodedFuncName, methodDesc, false);
        jvmCastGen.addBoxInsn(mv, lambdaDetails.returnType);
    }

    private void handleAsyncCallLambda(BIRTerminator.AsyncCall ins, LambdaDetails lambdaDetails, MethodVisitor mv,
                                       List<BType> paramBTypes) {
        if (ins.isVirtual) {
            handleLambdaVirtual(ins, lambdaDetails, mv);
        } else {
            handleAsyncNonVirtual(lambdaDetails, mv, paramBTypes);
        }
    }

    private void handleLambdaVirtual(BIRTerminator.AsyncCall ins, LambdaDetails lambdaDetails, MethodVisitor mv) {
        boolean isBuiltinModule = JvmCodeGenUtil.isBallerinaBuiltinModule(lambdaDetails.packageID.orgName.getValue(),
                                                                          lambdaDetails.packageID.name.getValue());
        List<BIRArgument> paramTypes = ins.args;
        genLoadDataForObjectAttachedLambdas(ins, mv, lambdaDetails.closureMapsCount, paramTypes,
                                            isBuiltinModule);
        int paramIndex = 2;
        for (int paramTypeIndex = 1; paramTypeIndex < paramTypes.size(); paramTypeIndex++) {
            generateObjectArgs(mv, paramIndex);
            paramIndex += 1;
            if (!isBuiltinModule) {
                generateObjectArgs(mv, paramIndex);
                paramIndex += 1;
            }
        }
        String methodDesc = BOBJECT_CALL;
        mv.visitMethodInsn(INVOKEINTERFACE , B_OBJECT, "call", methodDesc, true);
    }

    private void genLoadDataForObjectAttachedLambdas(BIRTerminator.AsyncCall ins, MethodVisitor mv,
                                                     int closureMapsCount, List<BIRArgument> paramTypes,
                                                     boolean isBuiltinModule) {

        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, closureMapsCount);
        mv.visitInsn(ICONST_1);
        BIRArgument ref = ins.args.get(0);
        mv.visitInsn(AALOAD);
        jvmCastGen.addUnboxInsn(mv, ref.variableDcl.type);
        mv.visitVarInsn(ALOAD, closureMapsCount);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);

        mv.visitLdcInsn(JvmCodeGenUtil.rewriteVirtualCallTypeName(ins.name.value));
        int objectArrayLength = paramTypes.size() - 1;
        if (!isBuiltinModule) {
            mv.visitIntInsn(BIPUSH, objectArrayLength * 2);
        } else {
            mv.visitIntInsn(BIPUSH, objectArrayLength);
        }
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
    }

    private void generateObjectArgs(MethodVisitor mv, int paramIndex) {
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, paramIndex - 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, paramIndex + 1);
        mv.visitInsn(AALOAD);
        mv.visitInsn(AASTORE);
    }

    private void handleAsyncNonVirtual(LambdaDetails lambdaDetails, MethodVisitor mv, List<BType> paramBTypes) {
        boolean isBuiltinModule = JvmCodeGenUtil.isBallerinaBuiltinModule(lambdaDetails.packageID.orgName.getValue(),
                                                                          lambdaDetails.packageID.name.getValue());
        List<BType> paramTypes = getFpParamTypes(lambdaDetails);
        // load and cast param values= asyncIns.args;
        int argIndex = 1;
        int paramIndex = 1;
        for (BType paramType : paramTypes) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, argIndex);
            mv.visitInsn(AALOAD);
            jvmCastGen.addUnboxInsn(mv, paramType);
            paramBTypes.add(paramIndex - 1, paramType);
            paramIndex += 1;
            argIndex += 1;
            if (!isBuiltinModule) {
                addBooleanTypeToLambdaParamTypes(mv, 0, argIndex);
                paramBTypes.add(paramIndex - 1, symbolTable.booleanType);
                paramIndex += 1;
            }
            argIndex += 1;
        }
        genNonVirtual(lambdaDetails, mv, paramBTypes);
    }

    private void addBooleanTypeToLambdaParamTypes(MethodVisitor mv, int arrayIndex, int paramIndex) {
        mv.visitVarInsn(ALOAD, arrayIndex);
        mv.visitIntInsn(BIPUSH, paramIndex);
        mv.visitInsn(AALOAD);
        jvmCastGen.addUnboxInsn(mv, symbolTable.booleanType);
    }

    private List<BType> getFpParamTypes(LambdaDetails lambdaDetails) {
        List<BType> paramTypes;
        if (lambdaDetails.functionWrapper != null) {
            paramTypes = getInitialParamTypes(lambdaDetails.functionWrapper.func.type.paramTypes,
                                              lambdaDetails.functionWrapper.func.argsCount);
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
                                List<BType> paramBTypes) {
        loadClosureMaps(lambdaDetails, mv);
        // load and cast param values
        loadAndCastParamValues(ins, lambdaDetails, mv, paramBTypes);
        genNonVirtual(lambdaDetails, mv, paramBTypes);
    }

    private void loadAndCastParamValues(BIRNonTerminator.FPLoad ins, LambdaDetails lambdaDetails, MethodVisitor mv,
                                        List<BType> paramBTypes) {
        int paramIndex = 1;
        int argIndex = 1;
        for (BIRNode.BIRVariableDcl dcl : ins.params) {
            mv.visitVarInsn(ALOAD, lambdaDetails.closureMapsCount);
            mv.visitIntInsn(BIPUSH, argIndex);
            mv.visitInsn(AALOAD);
            jvmCastGen.addUnboxInsn(mv, dcl.type);
            paramBTypes.add(paramIndex - 1, dcl.type);
            paramIndex += 1;
            argIndex += 1;

            boolean isBuiltinModule = JvmCodeGenUtil.isBallerinaBuiltinModule(
                    lambdaDetails.packageID.orgName.getValue(),
                    lambdaDetails.packageID.name.getValue());
            if (!isBuiltinModule) {
                addBooleanTypeToLambdaParamTypes(mv, lambdaDetails.closureMapsCount, argIndex);
                paramBTypes.add(paramIndex - 1, symbolTable.booleanType);
                paramIndex += 1;
            }
            argIndex += 1;
        }
    }

    private void loadClosureMaps(LambdaDetails lambdaDetails, MethodVisitor mv) {
        for (int i = 0; i < lambdaDetails.closureMapsCount; i++) {
            mv.visitVarInsn(ALOAD, i);
            mv.visitInsn(ICONST_1);
        }
    }

    private MethodVisitor getMethodVisitorAndLoadFirst(ClassWriter cw, String lambdaName,
                                                       LambdaDetails lambdaDetails, BIRInstruction ins) {
        String closureMapsDesc = getMapValueDesc(lambdaDetails.closureMapsCount);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, lambdaName,
                "(" + closureMapsDesc + "[L" + OBJECT + ";)L" + OBJECT + ";", null, null);

        mv.visitCode();
         // generate diagnostic position when generating lambda method
        JvmCodeGenUtil.generateDiagnosticPos(((BIRAbstractInstruction) ins).pos, mv);
        // load strand as first arg
        // strand and other args are in a object[] param. This param comes after closure maps.
        // hence the closureMapsCount is equal to the array's param index.
        mv.visitVarInsn(ALOAD, lambdaDetails.closureMapsCount);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        if (lambdaDetails.isExternFunction) {
            generateBlockedOnExtern(lambdaDetails.closureMapsCount, mv);
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

    private void generateBlockedOnExtern(int closureMapsCount, MethodVisitor mv) {
        Label blockedOnExternLabel = new Label();

        mv.visitInsn(DUP);

        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS , IS_BLOCKED_ON_EXTERN_FIELD, "()Z",
                           false);
        mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS , BLOCKED_ON_EXTERN_FIELD, "Z");

        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS , PANIC_FIELD,
                          GET_BERROR);
        Label panicLabel = new Label();
        mv.visitJumpInsn(IFNULL, panicLabel);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS , PANIC_FIELD,
                          GET_BERROR);
        mv.visitVarInsn(ASTORE, closureMapsCount + 1);
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS , PANIC_FIELD,
                          GET_BERROR);
        mv.visitVarInsn(ALOAD, closureMapsCount + 1);
        mv.visitInsn(ATHROW);
        mv.visitLabel(panicLabel);

        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "returnValue", "Ljava/lang/Object;");
        mv.visitInsn(ARETURN);

        mv.visitLabel(blockedOnExternLabel);
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
        lambdaDetails.encodedFuncName = IdentifierUtils.encodeFunctionIdentifier(lambdaDetails.funcName);
        lambdaDetails.lookupKey = JvmCodeGenUtil.getPackageName(lambdaDetails.packageID) +
                lambdaDetails.encodedFuncName;
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
            case CALL:
                BIRTerminator.Call call = (BIRTerminator.Call) callIns;
                if (call.isVirtual) {
                    return false;
                }
                methodName = call.name.value;
                packageID = call.calleePkg;
                break;
            case ASYNC_CALL:
                BIRTerminator.AsyncCall asyncCall = (BIRTerminator.AsyncCall) callIns;
                methodName = asyncCall.name.value;
                packageID = asyncCall.calleePkg;
                break;
            case FP_LOAD:
                BIRNonTerminator.FPLoad fpLoad = (BIRNonTerminator.FPLoad) callIns;
                methodName = fpLoad.funcName.value;
                packageID = fpLoad.pkgId;
                break;
            default:
                throw new BLangCompilerException("JVM static function call generation is not supported for " +
                                                         "instruction " + callIns);
        }

        String key = JvmCodeGenUtil.getPackageName(packageID) + methodName;

        BIRFunctionWrapper functionWrapper = jvmPackageGen.lookupBIRFunctionWrapper(key);
        return functionWrapper != null && JvmCodeGenUtil.isExternFunc(functionWrapper.func);
    }

    private void populateLambdaReturnType(BIRInstruction ins, LambdaDetails lambdaDetails) {
        if (lambdaDetails.lhsType.tag == TypeTags.FUTURE) {
            lambdaDetails.returnType = ((BFutureType) lambdaDetails.lhsType).constraint;
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
        desc.append(JvmCodeGenUtil.generateReturnType(retType));
        return desc.toString();
    }

    private void appendParamTypes(List<BType> paramTypes, StringBuilder desc) {
        for (BType paramType : paramTypes) {
            desc.append(JvmCodeGenUtil.getArgTypeSignature(paramType));
        }
    }

    private void appendClosureMaps(int closureMapsCount, StringBuilder desc) {
        for (int j = 0; j < closureMapsCount; j++) {
            desc.append("L").append(JvmConstants.MAP_VALUE).append(";").append("Z");
        }
    }

    private List<BType> getInitialParamTypes(List<BType> paramTypes, int argsCount) {
        List<BType> initialParamTypes = new ArrayList<>();
        for (int index = 0; index < argsCount; index++) {
            initialParamTypes.add(paramTypes.get(index * 2));
        }
        return initialParamTypes;
    }

}
